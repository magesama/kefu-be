# 用户角色管理功能说明

## 功能概述
用户角色管理功能允许系统区分不同类型的用户，并根据角色分配不同的权限。目前系统支持两种角色：
- 普通用户（role=0）：可以使用客服对话、查看产品等基本功能
- 管理员（role=1）：除了普通用户的权限外，还可以管理用户、管理产品等高级功能

## 数据库设计
在用户表(user)中添加了role字段：
```sql
ALTER TABLE `user` 
ADD COLUMN `role` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '用户角色：0-普通用户，1-管理员' AFTER `status`;
```

## 相关类说明

### 实体类
- `User`：用户实体类，添加了role字段
  ```java
  /**
   * 用户角色：0-普通用户，1-管理员
   */
  private Integer role;
  ```

### 请求/响应类
- `UserRegisterRequest`：用户注册请求类，添加了role字段，默认为0（普通用户）
  ```java
  /**
   * 用户角色：0-普通用户，1-管理员
   * 默认为普通用户
   */
  private Integer role = 0;
  ```
- `UserInfoResponse`：用户信息响应类，添加了role字段
  ```java
  /**
   * 用户角色：0-普通用户，1-管理员
   */
  private Integer role;
  ```

### 接口类
- `UserService`：用户服务接口，添加了角色相关方法
  ```java
  /**
   * 获取用户列表
   * @param username 用户名（可选，用于模糊查询）
   * @param status 用户状态（可选）
   * @param role 用户角色（可选）
   * @param page 页码，从1开始
   * @param size 每页大小
   * @return 用户信息列表
   */
  List<UserInfoResponse> getUserList(String username, Integer status, Integer role, int page, int size);
  
  /**
   * 获取用户总数
   * @param username 用户名（可选，用于模糊查询）
   * @param status 用户状态（可选）
   * @param role 用户角色（可选）
   * @return 用户总数
   */
  long getUserCount(String username, Integer status, Integer role);
  
  /**
   * 更新用户角色
   * @param userId 用户ID
   * @param role 角色值：0-普通用户，1-管理员
   * @return 更新是否成功
   */
  boolean updateUserRole(Long userId, Integer role);
  ```

### 控制器类
- `UserController`：用户控制器，添加了角色相关接口
  ```java
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
  ```

### 实现类
- `UserServiceImpl`：用户服务实现类，实现了角色相关方法
  ```java
  @Override
  public List<UserInfoResponse> getUserList(String username, Integer status, Integer role, int page, int size) {
      // 计算偏移量
      int offset = (page - 1) * size;
      
      // 使用自定义方法查询用户列表
      List<User> users = userMapper.selectUserList(username, status, role, offset, size);
      
      // 转换为响应对象列表
      return users.stream()
              .map(this::convertToUserInfoResponse)
              .collect(Collectors.toList());
  }
  
  @Override
  public long getUserCount(String username, Integer status, Integer role) {
      // 使用自定义方法统计用户总数
      return userMapper.countUserList(username, status, role);
  }
  
  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateUserRole(Long userId, Integer role) {
      // 检查角色值是否有效
      if (role != 0 && role != 1) {
          throw new RuntimeException("无效的角色值，只能是0（普通用户）或1（管理员）");
      }
      
      // 查询用户
      User user = userMapper.selectById(userId);
      if (user == null) {
          throw new RuntimeException("用户不存在");
      }
      
      // 使用自定义方法更新用户角色
      LocalDateTime updateTime = LocalDateTime.now();
      return userMapper.updateUserRole(userId, role, updateTime) > 0;
  }
  ```

### Mapper接口
- `UserMapper`：用户Mapper接口，添加了角色相关方法
  ```java
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
  ```

### XML映射文件
- `UserMapper.xml`：用户Mapper的XML映射文件，添加了角色相关SQL
  ```xml
  <!-- 根据角色查询用户列表 -->
  <select id="selectByRole" resultMap="BaseResultMap">
      SELECT <include refid="Base_Column_List" />
      FROM user
      WHERE role = #{role}
      ORDER BY create_time DESC
  </select>
  
  <!-- 更新用户角色 -->
  <update id="updateUserRole">
      UPDATE user
      SET role = #{role}, update_time = #{updateTime}
      WHERE id = #{userId}
  </update>
  
  <!-- 分页查询用户列表（支持按用户名模糊查询、状态和角色筛选） -->
  <select id="selectUserList" resultMap="BaseResultMap">
      SELECT <include refid="Base_Column_List" />
      FROM user
      <where>
          <if test="username != null and username != ''">
              AND username LIKE CONCAT('%', #{username}, '%')
          </if>
          <if test="status != null">
              AND status = #{status}
          </if>
          <if test="role != null">
              AND role = #{role}
          </if>
      </where>
      ORDER BY create_time DESC
      LIMIT #{offset}, #{size}
  </select>
  
  <!-- 统计用户总数（支持按用户名模糊查询、状态和角色筛选） -->
  <select id="countUserList" resultType="long">
      SELECT COUNT(*)
      FROM user
      <where>
          <if test="username != null and username != ''">
              AND username LIKE CONCAT('%', #{username}, '%')
          </if>
          <if test="status != null">
              AND status = #{status}
          </if>
          <if test="role != null">
              AND role = #{role}
          </if>
      </where>
  </select>
  ```

## API接口说明

### 1. 用户注册（支持指定角色）
- URL: `/api/user/register`
- 方法: POST
- 请求参数:
  ```json
  {
    "username": "用户名",
    "password": "密码",
    "confirmPassword": "确认密码",
    "phone": "手机号",
    "email": "邮箱",
    "role": 0  // 用户角色：0-普通用户，1-管理员，默认为0
  }
  ```
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "userId": 1
    }
  }
  ```

### 2. 获取用户列表（支持按角色筛选）
- URL: `/api/user/list`
- 方法: GET
- 请求参数:
  - username: 用户名（可选，模糊查询）
  - status: 状态（可选，0-禁用，1-正常）
  - role: 角色（可选，0-普通用户，1-管理员）
  - page: 页码，默认1
  - size: 每页大小，默认10
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "username": "用户名1",
        "phone": "手机号1",
        "email": "邮箱1",
        "balance": 100.00,
        "status": 1,
        "role": 0,
        "createTime": "2023-01-01 12:00:00"
      },
      {
        "id": 2,
        "username": "用户名2",
        "phone": "手机号2",
        "email": "邮箱2",
        "balance": 200.00,
        "status": 1,
        "role": 1,
        "createTime": "2023-01-02 12:00:00"
      }
    ]
  }
  ```

### 3. 获取用户总数（支持按角色筛选）
- URL: `/api/user/count`
- 方法: GET
- 请求参数:
  - username: 用户名（可选，模糊查询）
  - status: 状态（可选，0-禁用，1-正常）
  - role: 角色（可选，0-普通用户，1-管理员）
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": 100
  }
  ```

### 4. 更新用户角色
- URL: `/api/user/{userId}/role`
- 方法: PUT
- 请求参数:
  - role: 角色值（0-普通用户，1-管理员）
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": true
  }
  ```

## 使用示例

### 注册管理员用户
```http
POST /api/user/register
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "confirmPassword": "admin123",
  "phone": "13800138000",
  "email": "admin@example.com",
  "role": 1
}
```

### 查询所有管理员用户
```http
GET /api/user/list?role=1
```

### 将用户升级为管理员
```http
PUT /api/user/123/role?role=1
```

### 将管理员降级为普通用户
```http
PUT /api/user/123/role?role=0
```

## 权限控制说明
目前系统尚未实现完整的权限控制机制，后续将基于Spring Security实现基于角色的权限控制，确保只有管理员可以访问特定的接口和功能。 