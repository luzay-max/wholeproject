import request from './request';

// 发布招领信息
export const publishFind = (data) => {
  return request({
    url: '/find/publish',
    method: 'post',
    data
  });
};

// 获取招领信息列表
export const getFindList = (params) => {
  return request({
    url: '/find/list',
    method: 'get',
    params
  });
};

// 搜索招领信息
export const searchFind = (params) => {
  return request({
    url: '/find/search',
    method: 'get',
    params
  });
};

// 获取招领信息详情
export const getFindDetail = (id) => {
  return request({
    url: `/find/detail/${id}`,
    method: 'get'
  });
};

// 更新招领信息状态
export const updateFindStatus = (data) => {
  return request({
    url: '/find/updateStatus',
    method: 'post',
    data
  });
};

// 获取用户发布的招领信息
export const getUserFindList = (params) => {
  return request({
    url: '/find/userList',
    method: 'get',
    params
  });
};