import { defineStore } from 'pinia';
import { getLostList, searchLost, getLostDetail } from '../api/lostApi';
import { getRecentActivities } from '../api/activityApi';
import { getFindList, searchFind, getFindDetail } from '../api/findApi';

export const useInfoStore = defineStore('info', {
  state: () => ({
    // 动态数据
    activities: [],
    activitiesLoading: false,
    activitiesError: '',
    // 失物列表数据
    lostList: [],
    lostTotal: 0,
    lostLoading: false,
    lostError: '',
    
    // 招领列表数据
    findList: [],
    findTotal: 0,
    findLoading: false,
    findError: '',
    
    // 当前详情数据
    currentInfo: null,
    detailLoading: false,
    detailError: '',
    
    // 筛选条件
    filters: {
      lost: {
        keyword: '',
        location: '',
        type: '',
        startTime: '',
        endTime: '',
        status: 'UNFOUND',
        page: 1,
        pageSize: 10
      },
      find: {
        keyword: '',
        location: '',
        type: '',
        startTime: '',
        endTime: '',
        status: 'PENDING',
        page: 1,
        pageSize: 10
      }
    },
    
    // 分类数据
    categories: [
      { label: '全部', value: '' },
      { label: '证件', value: 'ID_CARD' },
      { label: '电子产品', value: 'ELECTRONICS' },
      { label: '书籍文具', value: 'BOOKS' },
      { label: '衣物', value: 'CLOTHES' },
      { label: '生活用品', value: 'DAILY_NECESSITIES' },
      { label: '其他', value: 'OTHER' }
    ],
    
    // 地点数据（从后端获取）
    locations: []
  }),
  
  getters: {
    // 获取热门失物信息
    hotLostItems: (state) => {
      return state.lostList.slice(0, 5);
    },
    
    // 获取热门招领信息
    hotFindItems: (state) => {
      return state.findList.slice(0, 5);
    },
    
    // 获取当前失物筛选条件
    currentLostFilters: (state) => state.filters.lost,
    
    // 获取当前招领筛选条件
    currentFindFilters: (state) => state.filters.find,

    // 获取动态列表
    recentActivities: (state) => state.activities
  },
  
  actions: {
    // 获取失物列表
    async fetchLostList(filters = {}) {
      try {
        this.lostLoading = true;
        this.lostError = '';
        
        // 合并筛选条件
        const queryParams = { ...this.filters.lost, ...filters };
        
        // 如果传入了新的page，更新状态
        if (filters.page !== undefined) {
          this.filters.lost.page = filters.page;
        }
        
        const res = await getLostList(queryParams);
        this.lostList = res.data.list;
        this.lostTotal = res.data.total;
        
        return res;
      } catch (error) {
        this.lostError = error.message || '获取失物列表失败';
        throw error;
      } finally {
        this.lostLoading = false;
      }
    },
    
    // 搜索失物信息
    async searchLostItems(keyword, filters = {}) {
      try {
        this.lostLoading = true;
        this.lostError = '';
        
        const queryParams = { ...filters, keyword };
        const res = await searchLost(queryParams);
        this.lostList = res.data.list;
        this.lostTotal = res.data.total;
        
        return res;
      } catch (error) {
        this.lostError = error.message || '搜索失物信息失败';
        throw error;
      } finally {
        this.lostLoading = false;
      }
    },
    
    // 获取招领列表
    async fetchFindList(filters = {}) {
      try {
        this.findLoading = true;
        this.findError = '';
        
        // 合并筛选条件
        const queryParams = { ...this.filters.find, ...filters };
        
        // 如果传入了新的page，更新状态
        if (filters.page !== undefined) {
          this.filters.find.page = filters.page;
        }
        
        const res = await getFindList(queryParams);
        this.findList = res.data.list;
        this.findTotal = res.data.total;
        
        return res;
      } catch (error) {
        this.findError = error.message || '获取招领列表失败';
        throw error;
      } finally {
        this.findLoading = false;
      }
    },
    
    // 搜索招领信息
    async searchFindItems(keyword, filters = {}) {
      try {
        this.findLoading = true;
        this.findError = '';
        
        const queryParams = { ...filters, keyword };
        const res = await searchFind(queryParams);
        this.findList = res.data.list;
        this.findTotal = res.data.total;
        
        return res;
      } catch (error) {
        this.findError = error.message || '搜索招领信息失败';
        throw error;
      } finally {
        this.findLoading = false;
      }
    },
    
    // 获取失物详情
    async fetchLostDetail(id) {
      try {
        this.detailLoading = true;
        this.detailError = '';
        
        const res = await getLostDetail(id);
        this.currentInfo = res.data;
        
        return res;
      } catch (error) {
        this.detailError = error.message || '获取失物详情失败';
        throw error;
      } finally {
        this.detailLoading = false;
      }
    },
    
    // 获取招领详情
    async fetchFindDetail(id) {
      try {
        this.detailLoading = true;
        this.detailError = '';
        
        const res = await getFindDetail(id);
        this.currentInfo = res.data;
        
        return res;
      } catch (error) {
        this.detailError = error.message || '获取招领详情失败';
        throw error;
      } finally {
        this.detailLoading = false;
      }
    },
    
    // 获取动态列表
    async fetchActivities({ limit = 10, type = 'all' } = {}) {
      try {
        this.activitiesLoading = true;
        this.activitiesError = '';
        
        const res = await getRecentActivities({ limit, type });
        this.activities = res.data;
        
        return res;
      } catch (error) {
        this.activitiesError = error.message || '获取动态失败';
        throw error;
      } finally {
        this.activitiesLoading = false;
      }
    },
    
    // 更新失物筛选条件
    updateLostFilters(filters) {
      this.filters.lost = { ...this.filters.lost, ...filters };
    },
    
    // 更新招领筛选条件
    updateFindFilters(filters) {
      this.filters.find = { ...this.filters.find, ...filters };
    },
    
    // 重置失物筛选条件
    resetLostFilters() {
      this.filters.lost = {
        keyword: '',
        location: '',
        type: '',
        startTime: '',
        endTime: '',
        status: 'UNFOUND',
        page: 1,
        pageSize: 10
      };
    },
    
    // 重置招领筛选条件
    resetFindFilters() {
      this.filters.find = {
        keyword: '',
        location: '',
        type: '',
        startTime: '',
        endTime: '',
        status: 'PENDING',
        page: 1,
        pageSize: 10
      };
    },
    
    // 清除当前详情
    clearCurrentInfo() {
      this.currentInfo = null;
    }
  }
});
