import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../store/userStore';
import router from '../router';
const shouldNotifyAuth = () => {
  const now = Date.now();
  const last = globalThis.__authNotifyAt || 0;
  if (now - last < 3000) return false;
  globalThis.__authNotifyAt = now;
  return true;
};

const needGlobalErrorToast = (config) => config?.showErrorMessage === true;


// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 10000
  // 移除默认的Content-Type，让浏览器根据请求类型自动设置
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从 store 获取 token
    const userStore = useUserStore();
    const token = userStore.token;
    
    // 如果 token 存在，添加到请求头
    if (token) {
      config.headers['Authorization'] = `${token}`;
    }
    
    // 设置默认的 Content-Type 为 UTF-8 编码
    if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json;charset=utf-8';
    }
    
    // 确保 Accept 头包含 UTF-8
    if (!config.headers['Accept']) {
      config.headers['Accept'] = 'application/json;charset=utf-8';
    }
    
    return config;
  },
  (error) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    // 导出等文件下载接口通常使用 blob 响应，不符合统一的 { code, message, data } 格式
    // 此类请求直接返回原始数据，由调用方自行处理
    if (response.config && response.config.responseType === 'blob') {
      return response.data;
    }

    const res = response.data;
    
    // 根据后端约定的状态码处理
    if (res.code !== 0) {
      if (res.code === 401) {
        if (shouldNotifyAuth()) {
          ElMessage.error(res.message || '未授权，请重新登录');
        }
        const userStore = useUserStore();
        userStore.logout();
        router.push('/login');
      }
      return Promise.reject(new Error(res.message || '请求失败'));
    }

    return res;
  },
  (error) => {
    // 网络错误处理
    // 静默处理请求取消的情况，避免控制台不必要的错误日志
    if (error && error.code === 'ECONNABORTED') {
      return Promise.reject(error);
    }
    
    let errorMsg = '网络请求失败';
    if (error.response) {
      // 服务器返回错误
      switch (error.response.status) {
        case 400:
          errorMsg = '请求参数错误';
          break;
        case 401:
          errorMsg = '未授权，请重新登录';
          // 清除登录状态并重定向到登录页
          const userStore = useUserStore();
          userStore.logout();
          router.push('/login');
          break;
        case 403:
          errorMsg = '拒绝访问';
          break;
        case 404:
          errorMsg = '请求的资源不存在';
          break;
        case 500:
          errorMsg = '服务器内部错误';
          break;
        default:
          errorMsg = error.response.data?.message || `请求失败(${error.response.status})`;
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      errorMsg = '网络连接失败，请检查网络';
    }
    
    const is401NoNotify = error && error.response && error.response.status === 401 && !shouldNotifyAuth();
    if (!is401NoNotify && needGlobalErrorToast(error?.config)) {
      ElMessage.error(errorMsg);
    }
    // 优化错误对象，优先使用服务器返回的错误信息
    if (error && error.response && error.response.data && error.response.data.message) {
      return Promise.reject(new Error(error.response.data.message));
    }
    return Promise.reject(error);
  }
);

export default service;
