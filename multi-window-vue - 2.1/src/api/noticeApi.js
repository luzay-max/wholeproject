import request from './request';

export const getNoticeList = (params) => {
  return request({
    url: '/notice/list',
    method: 'get',
    params
  });
};

export const getNoticeUnreadCount = () => {
  return request({
    url: '/notice/unread-count',
    method: 'get'
  });
};

export const markNoticeRead = (id) => {
  return request({
    url: `/notice/${id}/read`,
    method: 'put'
  });
};

export const markAllNoticeRead = () => {
  return request({
    url: '/notice/read-all',
    method: 'put'
  });
};

export const banReporterByNotice = (noticeId) => {
  return request({
    url: `/notice/report/${noticeId}/ban-reporter`,
    method: 'put'
  });
};
