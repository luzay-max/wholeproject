import request from './request';

export const reportItem = (data) => {
  return request({
    url: '/report/item',
    method: 'post',
    data
  });
};

