import request from './request';

// 学生端 - 获取当前周光荣榜
export const getCurrentWeekHonorBoard = () => {
  return request({
    url: '/honor/board/currentWeek',
    method: 'get'
  });
};

// 学生端/通用 - 获取指定周期的光荣榜（通常用于历史周榜）
export const getWeeklyHonorBoard = (params) => {
  return request({
    url: '/honor/board/weekly',
    method: 'get',
    params
  });
};

// 管理端 - 获取光荣榜周期列表
export const getHonorPeriods = (params) => {
  return request({
    url: '/admin/honor/periods',
    method: 'get',
    params
  });
};

// 管理端 - 获取某一期光荣榜详情
export const getHonorBoardDetail = (periodId) => {
  return request({
    url: '/admin/honor/board/detail',
    method: 'get',
    params: { periodId }
  });
};

// 管理端 - 标记某一期已发送给学校
export const markHonorSent = (periodId) => {
  return request({
    url: '/admin/honor/markSent',
    method: 'post',
    data: { periodId }
  });
};

// 管理端 - 标记某一期已完成颁奖
export const markHonorAwarded = (periodId) => {
  return request({
    url: '/admin/honor/markAwarded',
    method: 'post',
    data: { periodId }
  });
};

// 管理端 - 导出光荣榜（Excel / CSV）
export const exportHonorBoard = (params) => {
  return request({
    url: '/admin/honor/export',
    method: 'get',
    params,
    responseType: 'blob'
  });
};

// 管理端 - 手动刷新生成光荣榜
// 参数: { type: 0=本周, 1=上周, 2=自定义日期范围, startDate: "yyyy-MM-dd", endDate: "yyyy-MM-dd" }
export const generateHonorBoard = (data) => {
  return request({
    url: '/admin/honor/generate',
    method: 'post',
    data
  });
};

// 管理端 - 删除光荣榜
export const deleteHonorBoard = (periodId) => {
  return request({
    url: `/admin/honor/periods/${periodId}`,
    method: 'delete'
  });
};

// 管理端 - 更新光荣榜周期
export const updateHonorPeriod = (periodId, data) => {
  return request({
    url: `/admin/honor/periods/${periodId}`,
    method: 'put',
    data
  });
};

// 管理端 - 添加榜单成员
export const addHonorPeriodItem = (data) => {
  return request({
    url: '/admin/honor/items',
    method: 'post',
    data
  });
};

// 管理端 - 批量添加榜单成员
export const batchAddHonorPeriodItems = (data) => {
  return request({
    url: '/admin/honor/items/batch',
    method: 'post',
    data
  });
};

// 管理端 - 搜索候选人
export const searchHonorCandidates = (params) => {
  return request({
    url: '/admin/honor/candidates',
    method: 'get',
    params
  });
};

// 管理端 - 更新榜单成员

// 管理端 - 更新榜单成员
export const updateHonorPeriodItem = (id, data) => {
  return request({
    url: `/admin/honor/items/${id}`,
    method: 'put',
    data
  });
};

// 管理端 - 删除榜单成员
export const deleteHonorPeriodItem = (id) => {
  return request({
    url: `/admin/honor/items/${id}`,
    method: 'delete'
  });
};

