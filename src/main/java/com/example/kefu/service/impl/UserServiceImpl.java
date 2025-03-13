package com.example.kefu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kefu.entity.RechargeRecord;
import com.example.kefu.entity.User;
import com.example.kefu.mapper.RechargeRecordMapper;
import com.example.kefu.mapper.UserMapper;
import com.example.kefu.model.request.RechargeRequest;
import com.example.kefu.model.request.UserLoginRequest;
import com.example.kefu.model.request.UserRegisterRequest;
import com.example.kefu.model.response.UserInfoResponse;
import com.example.kefu.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final UserMapper userMapper;
    private final RechargeRecordMapper rechargeRecordMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterRequest request) {
        // 检查用户名是否已存在
        if (checkUsernameExists(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查两次密码是否一致
        if (!Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new RuntimeException("两次密码不一致");
        }
        
        // 创建用户实体
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 实际项目中应该对密码进行加密
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1); // 默认正常状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 保存用户
        userMapper.insert(user);
        
        return user.getId();
    }
    
    @Override
    public UserInfoResponse login(UserLoginRequest request) {
        // 根据用户名查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        
        // 检查用户是否存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查密码是否正确
        if (!Objects.equals(user.getPassword(), request.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }
        
        // 转换为响应对象
        return convertToUserInfoResponse(user);
    }
    
    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        // 根据ID查询用户
        User user = userMapper.selectById(userId);
        
        // 检查用户是否存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 转换为响应对象
        return convertToUserInfoResponse(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal recharge(Long userId, RechargeRequest request) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 记录充值前余额
        BigDecimal beforeBalance = user.getBalance();
        
        // 更新用户余额
        user.setBalance(beforeBalance.add(request.getAmount()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 创建充值记录
        RechargeRecord record = new RechargeRecord();
        record.setUserId(userId);
        record.setAmount(request.getAmount());
        record.setBeforeBalance(beforeBalance);
        record.setAfterBalance(user.getBalance());
        record.setStatus(1); // 充值成功
        record.setPayType(request.getPayType());
        record.setTradeNo(generateTradeNo()); // 生成交易流水号
        record.setRemark(request.getRemark());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        rechargeRecordMapper.insert(record);
        
        return user.getBalance();
    }
    
    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectCount(queryWrapper) > 0;
    }
    
    @Override
    public List<UserInfoResponse> getUserList(String username, Integer status, int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加用户名模糊查询条件（如果提供）
        if (StringUtils.hasText(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        
        // 添加状态查询条件（如果提供）
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(User::getCreateTime);
        
        // 执行分页查询
        Page<User> userPage = new Page<>(page, size);
        Page<User> resultPage = userMapper.selectPage(userPage, queryWrapper);
        
        // 转换为响应对象列表
        return resultPage.getRecords().stream()
                .map(this::convertToUserInfoResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public long getUserCount(String username, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加用户名模糊查询条件（如果提供）
        if (StringUtils.hasText(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        
        // 添加状态查询条件（如果提供）
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        
        // 执行计数查询
        return userMapper.selectCount(queryWrapper);
    }
    
    /**
     * 将User实体转换为UserInfoResponse
     * @param user 用户实体
     * @return 用户信息响应
     */
    private UserInfoResponse convertToUserInfoResponse(User user) {
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }
    
    /**
     * 生成交易流水号
     * @return 交易流水号
     */
    private String generateTradeNo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
} 