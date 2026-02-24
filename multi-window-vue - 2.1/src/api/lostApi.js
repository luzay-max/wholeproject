import request from './request';

// 发布失物信息
export const publishLost = (data) => {
  return request({
    url: '/lost/publish',
    method: 'post',
    data
  });
};

// 获取失物信息列表
export const getLostList = (params) => {
  return request({
    url: '/lost/list',
    method: 'get',
    params
  });
};

// 搜索失物信息
export const searchLost = (params) => {
  params = new URLSearchParams(params)
  return request({
    url: '/lost/list',
    method: 'get',
    params
  });
};

// 获取失物信息详情
export const getLostDetail = (id) => {
  return request({
    url: `/lost/detail/${id}`,
    method: 'get'
  });
};

// 更新失物信息状态
export const updateLostStatus = (data) => {
  return request({
    url: '/lost/updateStatus',
    method: 'post',
    data
  });
};

// 获取用户发布的失物信息
export const getUserLostList = (params) => {
  return request({
    url: '/lost/userList',
    method: 'get',
    params
  });
};

// 获取统计信息
export const getStatistics = () => {
  return request({
    url: '/statistics',
    method: 'get'
  });
};

// 获取热门失物列表
export const getHotLostList = () => {
  return request({
    url: '/lost/hot',
    method: 'get'
  });
};

// 获取热门招领列表
export const getHotFindList = () => {
  return request({
    url: '/find/hot',
    method: 'get'
  });
};

