import request from './request';

// 用户管理接口

// 获取用户列表
export const getUsers = (params) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  });
};

// 新增用户
export const createUser = (data) => {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  });
};

// 更新用户信息
export const updateUser = (id, data) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'put',
    data
  });
};

// 更新用户状态
export const updateUserStatus = (id, status) => {
  // 注意：后端可能复用 updateUser 接口，或者有专门的 status 接口
  // 这里假设后端新增了 /admin/users/{id}/status 接口，或者通过 updateUser 传递 status
  // 如果后端没有 status 接口，可以尝试用 updateUser(id, { status })
  // 但为了匹配 UserManagement.vue 的调用 (handleStatusChange 传的是 row.id, status)
  // 我们这里实现一个专门的函数
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    data: { status }
  });
};

// 修改用户角色
export const changeUserRole = (id, role) => {
  return request({
    url: `/admin/users/${id}/role`,
    method: 'put',
    data: { role }
  });
};

// 删除用户
export const deleteUser = (id) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  });
};

// 批量删除用户
export const batchDeleteUsers = (ids) => {
  return request({
    url: '/admin/users/batch',
    method: 'delete',
    data: { ids }
  });
};

// 重置用户密码
export const resetUserPassword = (id) => {
  return request({
    url: `/admin/users/${id}/password/reset`,
    method: 'put'
  });
};

// 白名单管理接口

// 获取白名单列表
export const getWhitelist = (params) => {
  return request({
    url: '/admin/whitelist',
    method: 'get',
    params
  });
};

// 导入白名单
export const importWhitelist = (formData) => {
  return request({
    url: '/admin/whitelist/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

// 下载白名单模板
export const downloadWhitelistTemplate = () => {
  return request({
    url: '/admin/whitelist/template',
    method: 'get',
    responseType: 'blob'
  });
};

// 添加白名单项
export const addWhitelistItem = (data) => {
  return request({
    url: '/admin/whitelist',
    method: 'post',
    data
  });
};

// 更新白名单项
export const updateWhitelistItem = (id, data) => {
  return request({
    url: `/admin/whitelist/${id}`,
    method: 'put',
    data
  });
};

// 删除白名单项
export const deleteWhitelistItem = (id) => {
  return request({
    url: `/admin/whitelist/${id}`,
    method: 'delete'
  });
};

// 批量删除白名单项
export const batchDeleteWhitelist = (ids) => {
  return request({
    url: '/admin/whitelist/batch',
    method: 'delete',
    data: { ids }
  });
};

// 失物管理接口

// 获取失物列表
export const getLostItems = (params) => {
  return request({
    url: '/admin/lost',
    method: 'get',
    params
  });
};

// 新增失物信息
export const createLostItem = (data) => {
  return request({
    url: '/admin/lost',
    method: 'post',
    data
  });
};

// 更新失物信息
export const updateLostItem = (id, data) => {
  return request({
    url: `/admin/lost/${id}`,
    method: 'put',
    data
  });
};

// 更新失物状态
export const updateLostStatus = (id, data) => {
  return request({
    url: `/admin/lost/${id}/status`,
    method: 'put',
    data
  });
};

// 删除失物
export const deleteLostItem = (id) => {
  return request({
    url: `/admin/lost/${id}`,
    method: 'delete'
  });
};

// 批量删除失物
export const batchDeleteLostItems = (ids) => {
  return request({
    url: '/admin/lost/batch',
    method: 'delete',
    data: { ids }
  });
};

// 招领管理接口

// 获取招领列表
export const getFindItems = (params) => {
  return request({
    url: '/admin/find',
    method: 'get',
    params
  });
};

// 新增招领信息
export const createFindItem = (data) => {
  return request({
    url: '/admin/find',
    method: 'post',
    data
  });
};

// 更新招领信息
export const updateFindItem = (id, data) => {
  return request({
    url: `/admin/find/${id}`,
    method: 'put',
    data
  });
};

// 更新招领状态
export const updateFindStatus = (id, data) => {
  return request({
    url: `/admin/find/${id}/status`,
    method: 'put',
    data
  });
};

// 删除招领
export const deleteFindItem = (id) => {
  return request({
    url: `/admin/find/${id}`,
    method: 'delete'
  });
};

// 批量删除招领
export const batchDeleteFindItems = (ids) => {
  return request({
    url: '/admin/find/batch',
    method: 'delete',
    data: { ids }
  });
};

// 评论管理接口

// 获取评论列表
export const getComments = (params) => {
  return request({
    url: '/admin/comments',
    method: 'get',
    params
  });
};

// 获取评论详情
export const getCommentDetail = (id) => {
  return request({
    url: `/admin/comments/${id}`,
    method: 'get'
  });
};

// 新增评论
export const createComment = (data) => {
  return request({
    url: '/admin/comments',
    method: 'post',
    data
  });
};

// 更新评论
export const updateComment = (id, data) => {
  return request({
    url: `/admin/comments/${id}`,
    method: 'put',
    data
  });
};

// 删除评论
export const deleteComment = (id) => {
  return request({
    url: `/admin/comments/${id}`,
    method: 'delete'
  });
};

// 批量删除评论
export const batchDeleteComments = (ids) => {
  return request({
    url: '/admin/comments/batch',
    method: 'delete',
    data: { ids }
  });
};

// 活动日志管理接口

// 获取活动日志列表
export const getActivities = (params) => {
  return request({
    url: '/admin/activities',
    method: 'get',
    params
  });
};

// 获取活动日志详情
export const getActivityDetail = (id) => {
  return request({
    url: `/admin/activities/${id}`,
    method: 'get'
  });
};

// 新增活动日志
export const createActivity = (data) => {
  return request({
    url: '/admin/activities',
    method: 'post',
    data
  });
};

// 更新活动日志
export const updateActivity = (id, data) => {
  return request({
    url: `/admin/activities/${id}`,
    method: 'put',
    data
  });
};

// 删除活动日志
export const deleteActivity = (id) => {
  return request({
    url: `/admin/activities/${id}`,
    method: 'delete'
  });
};

// 批量删除活动日志
export const batchDeleteActivities = (ids) => {
  return request({
    url: '/admin/activities/batch',
    method: 'delete',
    data: { ids }
  });
};

// 操作日志接口
export const getOperationLogs = (params) => {
  return request({
    url: '/admin/logs/list',
    method: 'get',
    params
  });
};

export const getOperationLogDetail = (id) => {
  return request({
    url: `/admin/logs/${id}`,
    method: 'get'
  });
};
