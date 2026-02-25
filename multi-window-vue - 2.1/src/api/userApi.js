import request from './request';

// 用户登录
export const login = async (data) => {
  // 使用URLSearchParams序列化表单数据
  const params = new URLSearchParams();
  params.append('accountName', data.username);
  params.append('username', data.username);
  params.append('password', data.password);
  if (data.captchaId) {
    params.append('captchaId', data.captchaId);
  }
  if (data.captchaCode) {
    params.append('captchaCode', data.captchaCode);
  }
  if (data.remember) {
    params.append('rememberMe', data.remember);
  }
  return request({
    url: 'auth/login',
    method: 'post',
    data: params,
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  
  });
};

// 用户注册
export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  });
};

// 检查用户名是否存在
export const checkUsername = (username) => {
  return request({
    url: '/auth/check-username',
    method: 'get',
    params: { username }
  });
};

// 获取验证码
export const getCaptcha = () => {
  return request({
    url: '/auth/captcha',
    method: 'get'
  });
};

export const getUserInfo = async () => {
  // 直接调用实际API
  return request({
    url: '/user/info',
    method: 'get'
  });
};

// 更新用户信息
export const updateUserInfo = (data) => {
  return request({
    url: '/user/update',
    method: 'post',
    data
  });
};

// 更新用户头像
export const updateAvatar = (file) => {
  const formData = new FormData();
  formData.append('avatar', file); // 参数名必须为'avatar'
  return request({
    url: '/user/updateAvatar',
    method: 'post',
    data: formData,
    //  headers: { 'Content-Type': 'multipart/form-data' } 这个可以加或者不加
  
  });
};
