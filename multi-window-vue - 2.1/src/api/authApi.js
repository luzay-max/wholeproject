import request from './request';

// 用户登录
export const login = (data) => {
  return request.post('/auth/login', data);
};

// 用户注册
export const register = (data) => {
  return request.post('/auth/register', data);
};

// 用户登出
export const logout = () => {
  return request.post('/auth/logout');
};

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/user/info');
};

// 重置密码
export const resetPassword = (data) => {
  return request.post('/auth/resetPassword', data);
};
