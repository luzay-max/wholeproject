import request from './request';

export const getDashboardOverview = () => {
  return request({
    url: '/admin/dashboard/overview',
    method: 'get'
  });
};

export const getDashboardTrend = (params) => {
  return request({
    url: '/admin/dashboard/trend',
    method: 'get',
    params
  });
};

