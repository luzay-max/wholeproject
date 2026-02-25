import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getToken, getRefreshToken, setToken, removeToken } from './authUtil';

// 是否正在刷新 Token
let isRefreshing = false;
// 存储刷新 Token 期间的请求队列
let requests = [];

const shouldNotifyAuth = () => {
  const now = Date.now();
  const last = globalThis.__authNotifyAt || 0;
  if (now - last < 3000) return false;
  globalThis.__authNotifyAt = now;
  return true;
};

const needGlobalErrorToast = (config) => config?.showErrorMessage === true;
const BASE_API = import.meta.env.VITE_APP_BASE_API || '/api';

const redirectToLogin = () => {
  removeToken();
  window.location.href = '/login';
};

// 创建axios实例
const service = axios.create({
  baseURL: BASE_API,
  timeout: 10000
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从本地存储获取token
    const token = getToken();
    if (token) {
      config.headers['Authorization'] = `${token}`;
    }

    // ✅ 动态设置 Content-Type
    if (config.data instanceof FormData) {
      // 上传文件时，让浏览器自动处理 multipart 边界
      delete config.headers['Content-Type'];
    } else if (!config.headers['Content-Type']) {
      // 普通请求默认使用 JSON
      config.headers['Content-Type'] = 'application/json';
    }

    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    
    // 判断响应状态码 (兼容 0 和 200)
    if (res.code !== 0 && res.code !== 200) {
      if (res.code === 401) {
        // 尝试刷新 Token
        const originalRequest = response.config;
        if (!originalRequest._retry) {
          originalRequest._retry = true;
          const refreshToken = getRefreshToken();
          
          if (refreshToken) {
            if (!isRefreshing) {
              isRefreshing = true;
              // 调用刷新 Token 接口
              return axios.post(`${BASE_API}/auth/refresh`, null, {
                params: { refreshToken }
              }).then(res => {
                if (res.data.code === 0) {
                  const newToken = res.data.data.token;
                  setToken(newToken);
                  service.defaults.headers.common['Authorization'] = newToken;
                  
                  // 重新执行队列中的请求
                  requests.forEach(cb => cb(newToken));
                  requests = [];
                  
                  return service(originalRequest);
                } else {
                  // 刷新失败，清除 Token 并跳转登录
                  redirectToLogin();
                  return Promise.reject(new Error('刷新 Token 失败'));
                }
              }).catch(err => {
                 redirectToLogin();
                 return Promise.reject(err);
              }).finally(() => {
                isRefreshing = false;
              });
            } else {
              // 正在刷新中，将请求加入队列
              return new Promise(resolve => {
                requests.push(token => {
                  originalRequest.headers['Authorization'] = token;
                  resolve(service(originalRequest));
                });
              });
            }
          }
        }

        if (shouldNotifyAuth()) {
          ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            redirectToLogin();
          });
        } else {
          redirectToLogin();
        }
      } else {
        if (needGlobalErrorToast(response.config)) {
          ElMessage.error(res.message || '系统错误');
        }
      }
      return Promise.reject(new Error(res.message || '系统错误'));
    }

    return res;
  },
  error => {
    let message = '网络异常，请稍后重试';
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 401 错误也可以尝试刷新 Token，逻辑同上
          const originalRequest = error.config;
          if (!originalRequest._retry) {
             // ... (复用上面的刷新逻辑，略显复杂，这里简化处理，通常后端返回 401 status code 或 body code 401)
             // 简单起见，如果 status 401 也走统一处理
             message = '未授权，请重新登录';
             redirectToLogin();
          }
          break;
        case 403:
          message = '拒绝访问';
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          message = '服务器内部错误';
          break;
        default:
          message = error.response.data.message || message;
      }
    }
    
    if (!(error && error.response && error.response.status === 401 && !shouldNotifyAuth()) && needGlobalErrorToast(error?.config)) {
       ElMessage.error(message);
    }
    return Promise.reject(error);
  }
);

export default service;
