package com.example.kefu.controller;

import com.example.kefu.model.request.RechargeRequest;
import com.example.kefu.model.request.UserLoginRequest;
import com.example.kefu.model.request.UserRegisterRequest;
import com.example.kefu.model.response.ApiResponse;
import com.example.kefu.model.response.UserInfoResponse;
import com.example.kefu.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 用户注册
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody UserRegisterRequest request) {
        Long userId = userService.register(request);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        return ApiResponse.success(result);
    }
    
    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public ApiResponse<UserInfoResponse> login(@Valid @RequestBody UserLoginRequest request) {
        UserInfoResponse userInfo = userService.login(request);
        return ApiResponse.success(userInfo);
    }
    
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/info/{userId}")
    public ApiResponse<UserInfoResponse> getUserInfo(@PathVariable Long userId) {
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return ApiResponse.success(userInfo);
    }
    
    /**
     * 用户充值
     * @param userId 用户ID
     * @param request 充值请求
     * @return 充值结果
     */
    @PostMapping("/recharge/{userId}")
    public ApiResponse<Map<String, Object>> recharge(@PathVariable Long userId, @Valid @RequestBody RechargeRequest request) {
        BigDecimal balance = userService.recharge(userId, request);
        Map<String, Object> result = new HashMap<>();
        result.put("balance", balance);
        return ApiResponse.success(result);
    }
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username")
    public ApiResponse<Map<String, Object>> checkUsername(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取用户列表
     * @param username 用户名（可选，模糊查询）
     * @param status 状态（可选）
     * @param role 角色（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 用户列表
     */
    @GetMapping("/list")
    public ApiResponse<List<UserInfoResponse>> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<UserInfoResponse> userList = userService.getUserList(username, status, role, page, size);
        return ApiResponse.success(userList);
    }
    
    /**
     * 获取用户总数
     * @param username 用户名（可选，模糊查询）
     * @param status 状态（可选）
     * @param role 角色（可选）
     * @return 用户总数
     */
    @GetMapping("/count")
    public ApiResponse<Long> getUserCount(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer role) {
        long count = userService.getUserCount(username, status, role);
        return ApiResponse.success(count);
    }
    
    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param role 角色（0-普通用户，1-管理员）
     * @return 更新结果
     */
    @PutMapping("/{userId}/role")
    public ApiResponse<Boolean> updateUserRole(
            @PathVariable Long userId,
            @RequestParam Integer role) {
        boolean success = userService.updateUserRole(userId, role);
        return ApiResponse.success(success);
    }
} 