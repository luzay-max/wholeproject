// 最近动态相关的 API 接口
import request from './request';

/**
 * 获取最近动态列表
 * @param {Object} params - 查询参数
 * @param {number} params.limit - 限制返回的记录数，默认10条
 * @param {string} params.type - 动态类型筛选（可选）：lost/find/all
 * @returns {Promise} 返回动态列表数据
 */
export const getRecentActivities = (params = {}) => {
  const queryParams = {
    limit: 10, // 默认返回10条
    type: 'all', // 默认获取所有类型
    ...params
  };
  
  return request({
    url: '/activity/recent',
    method: 'get',
    params: queryParams
  });
};