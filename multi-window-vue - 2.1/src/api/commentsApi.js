import request from './request';

// 添加评论
export const addComment = (data) => {
  return request({
    url: '/comments/add',
    method: 'post',
    data
  });
};

// 获取评论列表
export const getComments = (params) => {
  return request({
    url: '/comments/list',
    method: 'get',
    params
  });
};

// 点赞评论
export const likeComment = (id) => {
  return request({
    url: `/comments/like/${id}`,
    method: 'post'
  });
};

// 删除评论
export const deleteComment = (id) => {
  return request({
    url: `/comments/delete/${id}`,
    method: 'delete'
  });
};