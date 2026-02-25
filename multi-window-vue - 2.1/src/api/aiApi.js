import request from './request';

// AI 描述建议
export const suggestDescription = (data) => {
  return request({
    url: '/ai/description/suggest',
    method: 'post',
    data
  });
};

