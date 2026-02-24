import request from './request';

// 获取待审核信息列表
export const getAuditList = (params) => {
  return request({
    url: '/audit/list',
    method: 'get',
    params
  });
};

// 审核通过
export const auditPass = (data) => {
  return request({
    url: '/audit/pass',
    method: 'post',
    data
  });
};

// 审核通过的别名（兼容其他组件使用）
export const auditApprove = auditPass;

// 批量审核通过
export const batchAuditApprove = (data) => {
  return request({
    url: '/audit/batchPass',
    method: 'post',
    data
  });
};

// 审核驳回
export const auditReject = (data) => {
  return request({
    url: '/audit/reject',
    method: 'post',
    data
  });
};

// 获取管理员统计信息
export const getAdminStatistics = () => {
  return request({
    url: '/admin/statistics',
    method: 'get'
  });
};
