import request from './request';

export const applyClaim = (data) => {
  return request({
    url: '/claim/apply',
    method: 'post',
    data
  });
};

export const reportFound = (data) => {
  return request({
    url: '/claim/report-found',
    method: 'post',
    data
  });
};

export const submitClaimProof = (id, data) => {
  return request({
    url: `/claim/${id}/proof`,
    method: 'post',
    data
  });
};

export const confirmClaim = (id, data) => {
  return request({
    url: `/claim/${id}/confirm`,
    method: 'post',
    data
  });
};

export const completeClaim = (id) => {
  return request({
    url: `/claim/${id}/complete`,
    method: 'post'
  });
};

export const getMyClaimApplications = (params) => {
  return request({
    url: '/claim/my/applications',
    method: 'get',
    params
  });
};

export const getMyClaimPendingConfirm = (params) => {
  return request({
    url: '/claim/my/pending-confirm',
    method: 'get',
    params
  });
};
