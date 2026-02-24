// Token存储键名
const TOKEN_KEY = 'lost_and_found_token';
const REFRESH_TOKEN_KEY = 'lost_and_found_refresh_token';
const USER_INFO_KEY = 'lost_and_found_user_info';

// 保存Token到localStorage
export const setToken = (token) => {
  localStorage.setItem(TOKEN_KEY, token);
};

// 从localStorage获取Token
export const getToken = () => {
  return localStorage.getItem(TOKEN_KEY);
};

// 保存Refresh Token到localStorage
export const setRefreshToken = (token) => {
  localStorage.setItem(REFRESH_TOKEN_KEY, token);
};

// 从localStorage获取Refresh Token
export const getRefreshToken = () => {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
};

// 删除Token
export const removeToken = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_INFO_KEY);
};

// 保存用户信息到localStorage
export const setUserInfo = (userInfo) => {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
};

// 从localStorage获取用户信息
export const getUserInfo = () => {
  const userInfo = localStorage.getItem(USER_INFO_KEY);
  return userInfo ? JSON.parse(userInfo) : null;
};

// 判断用户是否已登录
export const isLoggedIn = () => {
  return !!getToken();
};

// 判断用户是否为管理员
export const isAdmin = () => {
  const userInfo = getUserInfo();
  return userInfo && userInfo.role === 'ADMIN';
};

// 判断用户是否为学生
export const isStudent = () => {
  const userInfo = getUserInfo();
  return userInfo && userInfo.role === 'STUDENT';
};

// 判断用户是否为教职工
export const isTeacher = () => {
  const userInfo = getUserInfo();
  return userInfo && userInfo.role === 'TEACHER';
};

// 清除所有登录状态
export const clearLoginStatus = () => {
  localStorage.clear();
};
