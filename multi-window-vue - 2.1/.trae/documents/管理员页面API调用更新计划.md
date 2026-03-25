# 管理员页面API调用更新计划

## 目标
将管理员页面的模拟数据替换为真实API调用，确保所有管理员功能能够正常使用。

## 实施步骤

### 1. 用户管理页面 (UserManagement.vue)
- 导入adminApi中的用户管理接口
- 修改loadUserList方法，调用getUsers接口
- 修改handleConfirmEdit方法，调用updateUser接口
- 修改handleConfirmRole方法，调用changeUserRole接口
- 修改handleDelete方法，调用deleteUser接口
- 修改handleBatchDelete方法，调用batchDeleteUsers接口

### 2. 失物管理页面 (LostManagement.vue)
- 导入adminApi中的失物管理接口
- 修改loadLostList方法，调用getLostItems接口
- 修改handleConfirmStatus方法，调用updateLostStatus接口
- 修改handleDelete方法，调用deleteLostItem接口
- 修改handleBatchDelete方法，调用batchDeleteLostItems接口

### 3. 招领管理页面 (FindManagement.vue)
- 导入adminApi中的招领管理接口
- 修改loadFindList方法，调用getFindItems接口
- 修改handleConfirmStatus方法，调用updateFindStatus接口
- 修改handleDelete方法，调用deleteFindItem接口
- 修改handleBatchDelete方法，调用batchDeleteFindItems接口

### 4. 评论管理页面 (CommentManagement.vue)
- 导入adminApi中的评论管理接口
- 修改loadCommentList方法，调用getComments接口
- 修改handleDelete方法，调用deleteComment接口
- 修改handleBatchDelete方法，调用batchDeleteComments接口

### 5. 活动日志管理页面 (ActivityManagement.vue)
- 导入adminApi中的活动日志管理接口
- 修改loadActivityList方法，调用getActivities接口
- 修改handleViewDetail方法，调用getActivityDetail接口

## 技术要点

1. **API调用**：使用async/await处理异步请求
2. **数据格式**：确保前端传递的数据格式与后端API要求一致
3. **错误处理**：保留原有的错误处理逻辑，确保用户体验
4. **加载状态**：使用loading变量显示加载状态
5. **分页处理**：确保分页参数正确传递给API

## 注意事项

1. 不修改全局配置，保持现有运行环境不变
2. 只更新管理员相关页面，不影响用户端功能
3. 确保所有修改后的页面能够正常运行和测试
4. 保持代码风格与现有代码一致

## 验证标准

1. 所有管理员页面能够正常加载真实数据
2. 所有增删改查操作能够正常调用API
3. 页面显示与交互正常
4. 没有控制台错误

## 实施顺序

1. 首先更新UserManagement.vue
2. 然后更新LostManagement.vue和FindManagement.vue
3. 接着更新CommentManagement.vue
4. 最后更新ActivityManagement.vue

每个页面更新完成后，进行简单测试，确保功能正常。