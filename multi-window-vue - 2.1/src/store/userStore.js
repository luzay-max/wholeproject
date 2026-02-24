import { defineStore } from 'pinia';
import { login, register, getUserInfo as getUserInfoApi, updateUserInfo as updateUserInfoApi, updateAvatar as updateAvatarApi } from '../api/userApi';
import { setToken, getToken, removeToken, setUserInfo as setStorageUserInfo, getUserInfo as getStorageUserInfo } from '../utils/authUtil';
import { ElMessage } from 'element-plus';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getStorageUserInfo() || {
      id: '',
      username: '',
      nickname: '',
      role: '',
      avatar: '',
      phone: '',
      email: '',
      createTime: ''
    },
    isLoggedIn: !!getToken(),
    isLoading: false,
    error: ''
  }),
  
  getters: {
    isAdmin: (state) => state.userInfo.role === 'ADMIN',
    isStudent: (state) => state.userInfo.role === 'STUDENT',
    isTeacher: (state) => state.userInfo.role === 'TEACHER',
    displayName: (state) => state.userInfo.nickname || state.userInfo.username,
    isLogin: (state) => state.isLoggedIn // 兼容模板中的使用
  },
  
  actions: {
    // 登录
    async login(loginForm) {
      try {
        this.isLoading = true;
        this.error = '';
        const res = await login(loginForm);
        
        // 保存token
        this.token = res.data.token;
        setToken(res.data.token);
        
        // 直接使用后端返回的用户信息，映射必要字段
        const backendUser = res.data.user;
        this.userInfo = {
          id: backendUser.id,
          username: backendUser.username,
          nickname: backendUser.name || backendUser.username, // 后端用name，前端用nickname
          role: backendUser.role,
          avatar: backendUser.avatar || '',
          phone: backendUser.phone || '',
          email: backendUser.email || '',
          createTime: backendUser.createTime
        };
        setStorageUserInfo(this.userInfo);
        
        this.isLoggedIn = true;
        return res;
      } catch (error) {
        this.error = error.message || '登录失败';
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    // 注册
    async register(registerForm) {
      try {
        this.isLoading = true;
        this.error = '';
        const res = await register(registerForm);
        return res;
      } catch (error) {
        this.error = error.message || '注册失败';
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    // 获取用户信息
    async getUserInfo() {
      try {
        const res = await getUserInfoApi();
        this.userInfo = res.data;
        setStorageUserInfo(res.data);
        this.isLoggedIn = true;
        return res;
      } catch (error) {
        // 如果获取失败，清除登录状态
        this.logout();
        throw error;
      }
    },
    
    // 更新用户信息
    async updateUserInfo(userInfo) {
      try {
        const res = await updateUserInfoApi(userInfo);
        this.userInfo = { ...this.userInfo, ...userInfo };
        setStorageUserInfo(this.userInfo);
        return res;
      } catch (error) {
        throw error;
      }
    },
    
    // 更新头像
    async updateAvatar(url) {
      try {
        // 直接使用传入的URL更新头像
        this.userInfo.avatar = url;
        setStorageUserInfo(this.userInfo);
        ElMessage.success('头像更新成功');
        return { code: 0, data: url };
      } catch (error) {
        ElMessage.error('头像更新失败');
        console.error('头像更新错误:', error);
        throw error;
      }
    },
    
    // 登出
    logout() {
      this.token = '';
      this.userInfo = {
        id: '',
        username: '',
        nickname: '',
        role: '',
        avatar: '',
        phone: '',
        email: '',
        createTime: ''
      };
      this.isLoggedIn = false;
      this.error = '';
      removeToken();
    },
    
    // 重置状态
    resetState() {
      this.logout();
    },
    
    // 检查登录状态
    async checkLoginStatus() {
      // 如果有 token，尝试获取用户信息
      if (this.token) {
        try {
          await this.getUserInfo();
        } catch (error) {
          // 获取用户信息失败，清除登录状态
          this.logout();
        }
      }
    }
  }
});
