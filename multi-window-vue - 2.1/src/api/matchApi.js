import request from './request';

export const getMatchRecommendations = (params) => {
  return request({
    url: '/match/recommend',
    method: 'get',
    params
  });
};

