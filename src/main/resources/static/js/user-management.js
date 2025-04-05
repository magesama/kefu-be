/**
 * 用户管理相关JavaScript
 */

// 页面加载完成后执行
$(document).ready(function() {
    // 加载用户列表
    loadUserList();
    
    // 绑定搜索按钮事件
    $('#searchBtn').click(function() {
        loadUserList();
    });
    
    // 绑定重置按钮事件
    $('#resetBtn').click(function() {
        $('#usernameSearch').val('');
        $('#statusSearch').val('');
        $('#roleSearch').val('');
        loadUserList();
    });
});

/**
 * 加载用户列表
 * @param page 页码，默认为1
 */
function loadUserList(page = 1) {
    const username = $('#usernameSearch').val();
    const status = $('#statusSearch').val();
    const role = $('#roleSearch').val();
    const size = 10; // 每页显示10条
    
    // 显示加载中
    $('#userTableBody').html('<tr><td colspan="7" class="text-center">加载中...</td></tr>');
    
    // 发送AJAX请求获取用户列表
    $.ajax({
        url: '/api/user/list',
        type: 'GET',
        data: {
            username: username,
            status: status,
            role: role,
            page: page,
            size: size
        },
        success: function(response) {
            if (response.code === 200) {
                renderUserTable(response.data);
                
                // 获取用户总数
                $.ajax({
                    url: '/api/user/count',
                    type: 'GET',
                    data: {
                        username: username,
                        status: status,
                        role: role
                    },
                    success: function(countResponse) {
                        if (countResponse.code === 200) {
                            renderPagination(countResponse.data, page, size);
                        } else {
                            showError('获取用户总数失败：' + countResponse.message);
                        }
                    },
                    error: function(xhr) {
                        showError('获取用户总数失败：' + xhr.statusText);
                    }
                });
            } else {
                showError('获取用户列表失败：' + response.message);
            }
        },
        error: function(xhr) {
            showError('获取用户列表失败：' + xhr.statusText);
        }
    });
}

/**
 * 渲染用户表格
 * @param users 用户列表数据
 */
function renderUserTable(users) {
    let html = '';
    
    if (users.length === 0) {
        html = '<tr><td colspan="7" class="text-center">暂无数据</td></tr>';
    } else {
        for (let i = 0; i < users.length; i++) {
            const user = users[i];
            html += `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>${user.phone || '-'}</td>
                    <td>${user.email || '-'}</td>
                    <td>${user.balance}</td>
                    <td>${user.status === 1 ? '<span class="badge bg-success">正常</span>' : '<span class="badge bg-danger">禁用</span>'}</td>
                    <td>${user.role === 1 ? '<span class="badge bg-primary">管理员</span>' : '<span class="badge bg-secondary">普通用户</span>'}</td>
                    <td>${formatDateTime(user.createTime)}</td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="showUpdateRoleModal(${user.id}, ${user.role})">修改角色</button>
                    </td>
                </tr>
            `;
        }
    }
    
    $('#userTableBody').html(html);
}

/**
 * 渲染分页
 * @param total 总记录数
 * @param currentPage 当前页码
 * @param size 每页大小
 */
function renderPagination(total, currentPage, size) {
    const totalPages = Math.ceil(total / size);
    let html = '';
    
    if (totalPages > 1) {
        html += `
            <nav aria-label="用户列表分页">
                <ul class="pagination justify-content-center">
                    <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                        <a class="page-link" href="javascript:void(0);" onclick="loadUserList(${currentPage - 1})">上一页</a>
                    </li>
        `;
        
        // 显示页码
        const startPage = Math.max(1, currentPage - 2);
        const endPage = Math.min(totalPages, startPage + 4);
        
        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="loadUserList(${i})">${i}</a>
                </li>
            `;
        }
        
        html += `
                    <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                        <a class="page-link" href="javascript:void(0);" onclick="loadUserList(${currentPage + 1})">下一页</a>
                    </li>
                </ul>
            </nav>
        `;
    }
    
    $('#paginationContainer').html(html);
}

/**
 * 显示修改角色模态框
 * @param userId 用户ID
 * @param currentRole 当前角色
 */
function showUpdateRoleModal(userId, currentRole) {
    $('#updateRoleUserId').val(userId);
    $('#updateRoleSelect').val(currentRole);
    $('#updateRoleModal').modal('show');
}

/**
 * 更新用户角色
 */
function updateUserRole() {
    const userId = $('#updateRoleUserId').val();
    const role = $('#updateRoleSelect').val();
    
    $.ajax({
        url: `/api/user/${userId}/role`,
        type: 'PUT',
        data: {
            role: role
        },
        success: function(response) {
            if (response.code === 200) {
                $('#updateRoleModal').modal('hide');
                showSuccess('角色更新成功');
                loadUserList(); // 重新加载用户列表
            } else {
                showError('角色更新失败：' + response.message);
            }
        },
        error: function(xhr) {
            showError('角色更新失败：' + xhr.statusText);
        }
    });
}

/**
 * 格式化日期时间
 * @param dateTimeStr 日期时间字符串
 * @returns {string} 格式化后的日期时间
 */
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    const date = new Date(dateTimeStr);
    return date.getFullYear() + '-' + 
           padZero(date.getMonth() + 1) + '-' + 
           padZero(date.getDate()) + ' ' + 
           padZero(date.getHours()) + ':' + 
           padZero(date.getMinutes()) + ':' + 
           padZero(date.getSeconds());
}

/**
 * 数字补零
 * @param num 数字
 * @returns {string} 补零后的字符串
 */
function padZero(num) {
    return num < 10 ? '0' + num : num;
}

/**
 * 显示成功提示
 * @param message 提示消息
 */
function showSuccess(message) {
    toastr.success(message);
}

/**
 * 显示错误提示
 * @param message 错误消息
 */
function showError(message) {
    toastr.error(message);
} 