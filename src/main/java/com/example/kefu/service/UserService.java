package com.example.kefu.service;

import com.example.kefu.entity.User;
import com.example.kefu.model.request.RechargeRequest;
import com.example.kefu.model.request.UserLoginRequest;
import com.example.kefu.model.request.UserRegisterRequest;
import com.example.kefu.model.response.UserInfoResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     * @param request 注册请求
     * @return 注册成功的用户ID
     */
    Long register(UserRegisterRequest request);
    
    /**
     * 用户登录
     * @param request 登录请求
     * @return 用户信息
     */
    UserInfoResponse login(UserLoginRequest request);
    
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoResponse getUserInfo(Long userId);
    
    /**
     * 用户充值
     * @param userId 用户ID
     * @param request 充值请求
     * @return 充值后的余额
     */
    BigDecimal recharge(Long userId, RechargeRequest request);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean checkUsernameExists(String username);
    
    /**
     * 获取用户列表
     * @param username 用户名（可选，用于模糊查询）
     * @param status 用户状态（可选）
     * @param page 页码，从1开始
     * @param size 每页大小
     * @return 用户信息列表
     */
    List<UserInfoResponse> getUserList(String username, Integer status, int page, int size);
    
    /**
     * 获取用户总数
     * @param username 用户名（可选，用于模糊查询）
     * @param status 用户状态（可选）
     * @return 用户总数
     */
    long getUserCount(String username, Integer status);
} 