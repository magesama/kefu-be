package com.example.kefu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.kefu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User selectByUsername(String username);
    
    /**
     * 根据角色查询用户列表
     * @param role 角色
     * @return 用户列表
     */
    List<User> selectByRole(Integer role);
    
    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param role 角色
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int updateUserRole(@Param("userId") Long userId, @Param("role") Integer role, @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 分页查询用户列表
     * @param username 用户名（可选，模糊查询）
     * @param status 状态（可选）
     * @param role 角色（可选）
     * @param offset 偏移量
     * @param size 每页大小
     * @return 用户列表
     */
    List<User> selectUserList(@Param("username") String username, @Param("status") Integer status, 
                             @Param("role") Integer role, @Param("offset") int offset, @Param("size") int size);
    
    /**
     * 统计用户总数
     * @param username 用户名（可选，模糊查询）
     * @param status 状态（可选）
     * @param role 角色（可选）
     * @return 用户总数
     */
    long countUserList(@Param("username") String username, @Param("status") Integer status, @Param("role") Integer role);
} 