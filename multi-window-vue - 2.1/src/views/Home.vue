<template>
  <div class="home-page">
    <!-- 顶部搜索区 - 现代化设计 -->
    <div class="search-section">
      <div class="search-content">
        <div class="search-header">
          <h2 class="search-title">寻找失物 · 传递温暖</h2>
          <p class="search-subtitle">校园失物招领平台，让丢失的物品找到回家的路</p>
        </div>
        <div class="search-wrapper">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索物品名称、地点或描述..."
            size="large"
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon class="search-icon"><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="handleSearch" class="search-btn">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </template>
          </el-input>
        </div>
        <!-- 快捷操作 -->
        <div class="quick-actions" v-if="userStore.isLoggedIn">
          <el-button type="primary" size="large" @click="$router.push('/lost/publish')">
            <el-icon><Plus /></el-icon>
            发布失物
          </el-button>
          <el-button size="large" @click="$router.push('/find/publish')">
            <el-icon><Plus /></el-icon>
            发布招领
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 内容区 -->
    <div class="content-section">
      <!-- 左侧面板 -->
      <div class="left-panel">
        <!-- 统计卡片 - 渐变背景升级 -->
        <div class="stats-panel" :class="{ 'admin-mode': userStore.isAdmin }">
          <div class="stat-card gradient-blue" shadow="hover">
            <div class="stat-icon">
              <el-icon><Search /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.lostCount }}</div>
              <div class="stat-label">失物信息</div>
            </div>
          </div>
          <div class="stat-card gradient-green" shadow="hover">
            <div class="stat-icon">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.findCount }}</div>
              <div class="stat-label">招领信息</div>
            </div>
          </div>
          <div class="stat-card gradient-orange" shadow="hover">
            <div class="stat-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.solvedCount }}</div>
              <div class="stat-label">已解决</div>
            </div>
          </div>
          <template v-if="userStore.isAdmin">
            <div class="stat-card gradient-purple" shadow="hover">
              <div class="stat-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.pendingCount }}</div>
                <div class="stat-label">待审核</div>
              </div>
            </div>
            <div class="stat-card gradient-pink" shadow="hover">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.userCount }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </div>
            <div class="stat-card gradient-red" shadow="hover">
              <div class="stat-icon">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.rejectCount }}</div>
                <div class="stat-label">已拒绝</div>
              </div>
            </div>
          </template>
        </div>
        
        <!-- 最近动态 - 美化设计 -->
        <div class="activity-panel">
          <div class="panel-header">
            <span class="panel-title">
              <div class="title-icon-wrapper">
                <el-icon><Bell /></el-icon>
              </div>
              最近动态
            </span>
            <el-tag size="small" type="info" effect="plain">实时更新</el-tag>
          </div>
          <div class="panel-content">
            <div v-if="activitiesLoading" class="loading-state">
              <el-skeleton :rows="4" animated />
            </div>
            <div v-else-if="activities.length === 0" class="empty-state">
              <el-empty description="暂无动态" :image-size="80">
                <template #description>
                  <p class="empty-text">还没有任何动态</p>
                  <p class="empty-hint">发布失物或招领信息后，这里会显示最新动态</p>
                </template>
              </el-empty>
            </div>
            <div v-else class="activity-list">
              <div
                v-for="(activity, index) in activities"
                :key="index"
                class="activity-item"
                @click="viewActivityDetail(activity)"
              >
                <div class="activity-timeline">
                  <div class="timeline-dot" :class="activity.type"></div>
                  <div class="timeline-line" v-if="index < activities.length - 1"></div>
                </div>
                <div class="activity-main">
                  <div class="activity-icon" :class="activity.type">
                    <el-icon v-if="activity.type === 'lost'"><Search /></el-icon>
                    <el-icon v-else><Bell /></el-icon>
                  </div>
                  <div class="activity-info">
                    <div class="activity-content">
                      <span class="activity-type-tag" :class="activity.type">
                        {{ activity.type === 'lost' ? '失物' : '招领' }}
                      </span>
                      <span class="activity-name">{{ activity.itemName }}</span>
                    </div>
                    <div class="activity-time-wrapper">
                      <el-icon><Clock /></el-icon>
                      <span class="activity-time">{{ formatRelativeTime(activity.time) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧面板 - 热门信息 -->
      <div class="right-panel">
        <div class="hot-panel">
          <div class="panel-header">
            <div class="panel-title-wrapper">
              <div class="panel-icon">
                <el-icon><HotWater /></el-icon>
              </div>
              <span class="panel-title">热门信息</span>
            </div>
            <el-radio-group v-model="activeTab" size="small" class="tab-switch">
              <el-radio-button label="lost">
                <el-icon><Search /></el-icon>
                失物
              </el-radio-button>
              <el-radio-button label="find">
                <el-icon><Bell /></el-icon>
                招领
              </el-radio-button>
            </el-radio-group>
          </div>
          
          <div class="panel-content">
            <div v-if="loading" class="loading-state">
              <el-skeleton :rows="5" animated />
            </div>
            <div v-else-if="hotItems.length === 0" class="empty-state">
              <el-empty description="暂无信息">
                <template #description>
                  <p class="empty-text">暂时没有热门信息</p>
                  <p class="empty-hint">成为第一个发布者吧</p>
                </template>
              </el-empty>
            </div>
            <div v-else class="hot-grid">
              <div
                v-for="(item, index) in hotItems"
                :key="item.id"
                class="hot-card"
                @click="viewDetail(item.id)"
              >
                <div class="card-image">
                  <img v-if="item.images && item.images.length > 0" :src="item.images[0]" :alt="item.name" />
                  <div v-else class="no-image">
                    <el-icon><Picture /></el-icon>
                    <span>暂无图片</span>
                  </div>
                  <div class="image-overlay">
                    <el-icon><View /></el-icon>
                  </div>
                </div>
                <div class="card-content">
                  <div class="card-header">
                    <h4 class="item-name">{{ item.name }}</h4>
                    <el-tag size="small" :type="activeTab === 'lost' ? 'danger' : 'primary'" effect="light" class="type-tag">
                      {{ activeTab === 'lost' ? '失物' : '招领' }}
                    </el-tag>
                  </div>
                  <p class="item-location">
                    <el-icon><Location /></el-icon>
                    {{ item.location || '未知地点' }}
                  </p>
                  <div class="item-meta">
                    <div class="meta-left">
                      <span class="item-time">{{ formatDate(item.publishTime) }}</span>
                    </div>
                    <div class="meta-right">
                      <span class="item-views">
                        <el-icon><View /></el-icon>
                        {{ item.viewCount || 0 }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="panel-footer">
            <el-button type="primary" text @click="viewMore" class="view-more-btn">
              <span>查看更多{{ activeTab === 'lost' ? '失物' : '招领' }}信息</span>
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
        
        <!-- 平台公告/快捷入口 -->
        <div class="side-widget">
          <div class="widget-header">
            <el-icon><InfoFilled /></el-icon>
            <span>平台公告</span>
          </div>
          <div class="widget-content">
            <div class="announcement-item" @click="$router.push('/honor-board')">
              <el-icon><Trophy /></el-icon>
              <span>查看本周光荣榜，表彰拾金不昧的好同学！</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowRight, Bell, Search, Location, Picture, View, Clock, CircleCheck, CircleClose, User, Plus, Trophy, HotWater, InfoFilled } from '@element-plus/icons-vue';
import { getHotLostList, getHotFindList, getStatistics, getRecentActivities } from '../api';
import { useUserStore } from '../store/userStore';

export default {
  name: 'Home',
  components: {
    ArrowRight,
    Bell,
    Search,
    Location,
    Picture,
    View,
    Clock,
    CircleCheck,
    CircleClose,
    User,
    Plus,
    Trophy,
    HotWater,
    InfoFilled
  },
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    
    const searchKeyword = ref('');
    const activeTab = ref('lost');
    const loading = ref(false);
    const activitiesLoading = ref(false);
    const hotItems = ref([]);
    const activities = ref([]);
    const stats = reactive({
      lostCount: 0,
      findCount: 0,
      solvedCount: 0,
      pendingCount: 0,
      userCount: 0,
      rejectCount: 0
    });
    
    const loadStatistics = async () => {
      try {
        const res = await getStatistics();
        Object.assign(stats, res.data || {});
      } catch (error) {
        console.error('获取统计失败:', error);
      }
    };
    
    const loadHotItems = async () => {
      try {
        loading.value = true;
        const api = activeTab.value === 'lost' ? getHotLostList : getHotFindList;
        const res = await api();
        
        hotItems.value = (res.data || []).map(item => ({
          ...item,
          images: typeof item.images === 'string' ? JSON.parse(item.images || '[]') : (item.images || [])
        })).slice(0, 6);
      } catch (error) {
        console.error('获取热门信息失败:', error);
      } finally {
        loading.value = false;
      }
    };
    
    const loadActivities = async () => {
      try {
        activitiesLoading.value = true;
        const res = await getRecentActivities({ limit: 8 });
        activities.value = res.data || [];
      } catch (error) {
        console.error('获取动态失败:', error);
      } finally {
        activitiesLoading.value = false;
      }
    };
    
    const handleSearch = () => {
      if (searchKeyword.value.trim()) {
        router.push({
          name: 'LostList',
          query: { keyword: searchKeyword.value }
        });
      }
    };
    
    const viewDetail = (id) => {
      router.push({
        name: 'InfoDetailPage',
        params: { type: activeTab.value, id }
      });
    };
    
    const viewActivityDetail = (activity) => {
      router.push({
        name: activity.type === 'lost' ? 'LostList' : 'FindList'
      });
    };
    
    const viewMore = () => {
      router.push({
        name: activeTab.value === 'lost' ? 'LostList' : 'FindList'
      });
    };
    
    const formatDate = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return `${date.getMonth() + 1}月${date.getDate()}日`;
    };
    
    const formatRelativeTime = (timestamp) => {
      if (!timestamp) return '';
      const now = Date.now();
      const diff = now - new Date(timestamp).getTime();
      
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;
      return formatDate(timestamp);
    };
    
    watch(activeTab, loadHotItems);
    
    onMounted(() => {
      loadStatistics();
      loadHotItems();
      loadActivities();
    });
    
    return {
      searchKeyword,
      activeTab,
      loading,
      activitiesLoading,
      hotItems,
      activities,
      stats,
      userStore,
      handleSearch,
      viewDetail,
      viewActivityDetail,
      viewMore,
      formatDate,
      formatRelativeTime
    };
  }
};
</script>

<style scoped>
.home-page {
  --page-accent: #2563eb;
  --page-accent-strong: #1d4ed8;
  --page-accent-soft: rgba(37, 99, 235, 0.12);
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
  background: radial-gradient(1000px 420px at 10% -10%, rgba(37, 99, 235, 0.10), transparent 60%),
    linear-gradient(180deg, #f8fafc 0%, #ffffff 70%);
  padding: 20px;
}

/* ??? */
.search-section {
  flex-shrink: 0;
  padding: 12px 16px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid var(--color-border-secondary);
  box-shadow: var(--shadow-1);
}

.search-content {
  max-width: 820px;
  margin: 0 auto;
}

.search-header {
  text-align: center;
  margin-bottom: 8px;
}

.search-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 4px 0;
  letter-spacing: 1px;
}

.search-subtitle {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 0;
}

.search-wrapper {
  margin-bottom: 8px;
}

.search-wrapper :deep(.el-input__wrapper) {
  border-radius: 12px 0 0 12px;
  padding-left: 16px;
  height: 36px;
}

.search-wrapper :deep(.el-input-group__append) {
  border-radius: 0 12px 12px 0;
  padding: 0;
  border: none;
  display: flex;
  align-items: center;
  background: #ffffff;
}

.search-wrapper :deep(.el-input-group__append button) {
  border-radius: 0 12px 12px 0;
  padding: 0 16px;
  font-size: 14px;
  height: 34px;
  margin: 1px;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.search-icon {
  font-size: 14px;
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.quick-actions :deep(.el-button) {
  padding: 6px 16px;
  border-radius: 10px;
  font-size: 12px;
}

/* ??? */
.content-section {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
  overflow: hidden;
}

.left-panel,
.right-panel {
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

/* ???? */
.stats-panel {
  flex-shrink: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(110px, 1fr));
  gap: 12px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 12px 12px 14px;
  border-radius: 14px;
  border: 1px solid var(--color-border-secondary);
  background: #ffffff;
  box-shadow: var(--shadow-1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.08);
}

.stat-card.gradient-blue { --stat-accent: #2563eb; --stat-accent-soft: rgba(37, 99, 235, 0.12); }
.stat-card.gradient-green { --stat-accent: #0f766e; --stat-accent-soft: rgba(15, 118, 110, 0.12); }
.stat-card.gradient-orange { --stat-accent: #f59e0b; --stat-accent-soft: rgba(245, 158, 11, 0.14); }
.stat-card.gradient-purple { --stat-accent: #6d28d9; --stat-accent-soft: rgba(109, 40, 217, 0.12); }
.stat-card.gradient-pink { --stat-accent: #db2777; --stat-accent-soft: rgba(219, 39, 119, 0.12); }
.stat-card.gradient-red { --stat-accent: #dc2626; --stat-accent-soft: rgba(220, 38, 38, 0.12); }

.stat-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: var(--stat-accent-soft);
  color: var(--stat-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.stat-info {
  text-align: left;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

/* ???? */
.activity-panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid var(--color-border-secondary);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.activity-panel .panel-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8fafc;
  border-bottom: 1px solid var(--color-border-secondary);
}

.activity-panel .panel-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon-wrapper {
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: var(--page-accent-soft);
  color: var(--page-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.activity-panel .panel-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 14px 16px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f8fafc;
  border: 1px solid transparent;
  min-height: 64px;
}

.activity-item:hover {
  background: #ffffff;
  border-color: var(--color-border-secondary);
  box-shadow: 0 6px 12px rgba(15, 23, 42, 0.06);
}

.activity-timeline {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 18px;
  flex-shrink: 0;
}

.timeline-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5f5;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 2px #e2e8f0;
  z-index: 1;
}

.timeline-dot.lost {
  background: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
}

.timeline-dot.find {
  background: #0f766e;
  box-shadow: 0 0 0 2px rgba(15, 118, 110, 0.2);
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: #e2e8f0;
  margin: 4px 0;
}

.activity-main {
  flex: 1;
  display: flex;
  gap: 10px;
  min-width: 0;
  align-items: center;
}

.activity-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
}

.activity-icon.lost {
  background: rgba(37, 99, 235, 0.12);
  color: #2563eb;
}

.activity-icon.find {
  background: rgba(15, 118, 110, 0.12);
  color: #0f766e;
}

.activity-info {
  flex: 1;
  min-width: 0;
}

.activity-content {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  min-height: 22px;
}

.activity-type-tag {
  font-size: 11px;
  padding: 0 8px;
  border-radius: 999px;
  font-weight: 500;
  min-width: 44px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}

.activity-type-tag.lost {
  background: rgba(37, 99, 235, 0.12);
  color: #2563eb;
}

.activity-type-tag.find {
  background: rgba(15, 118, 110, 0.12);
  color: #0f766e;
}

.activity-name {
  font-size: 13px;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-time-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.activity-time-wrapper .el-icon {
  font-size: 12px;
}

/* ???? */
.hot-panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid var(--color-border-secondary);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.hot-panel .panel-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: #f8fafc;
  border-bottom: 1px solid var(--color-border-secondary);
}

.panel-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.panel-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: rgba(245, 158, 11, 0.16);
  color: #f59e0b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.hot-panel .panel-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.tab-switch :deep(.el-radio-button__inner) {
  border-radius: 999px !important;
  border: 1px solid var(--color-border-secondary) !important;
  box-shadow: none !important;
  padding: 6px 14px;
  background: #ffffff;
}

.tab-switch :deep(.el-radio-button__original:checked + .el-radio-button__inner) {
  background: var(--page-accent) !important;
  border-color: var(--page-accent) !important;
  color: #ffffff !important;
}

.hot-panel .panel-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.hot-card {
  background: #ffffff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--color-border-secondary);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.hot-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.08);
  border-color: rgba(37, 99, 235, 0.3);
}

.card-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #f59e0b;
  color: #fff;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 4px 10px rgba(245, 158, 11, 0.3);
}

.card-image {
  position: relative;
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: #f8fafc;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.hot-card:hover .card-image img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s ease;
  opacity: 0;
}

.hot-card:hover .image-overlay {
  opacity: 1;
}

.image-overlay .el-icon {
  font-size: 28px;
  color: #fff;
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 12px;
  gap: 6px;
}

.no-image .el-icon {
  font-size: 28px;
}

.card-content {
  padding: 14px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.type-tag {
  font-size: 11px;
  border-radius: 999px;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 8px;
}

.item-location {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-location .el-icon {
  font-size: 13px;
  color: #cbd5f5;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid #f1f5f9;
}

.meta-left,
.meta-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-time,
.item-views {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.item-views .el-icon {
  font-size: 12px;
}

.hot-panel .panel-footer {
  flex-shrink: 0;
  padding: 12px 16px;
  border-top: 1px solid var(--color-border-secondary);
  text-align: center;
  background: #f8fafc;
}

.view-more-btn {
  font-size: 14px;
  padding: 8px 18px;
  border-radius: 10px;
}

.view-more-btn:hover {
  background: var(--page-accent);
  color: #fff !important;
}

.view-more-btn span {
  margin-right: 6px;
}

/* ????? */
.side-widget {
  flex-shrink: 0;
  background: #ffffff;
  border-radius: 16px;
  padding: 16px 18px;
  border: 1px solid var(--color-border-secondary);
  box-shadow: var(--shadow-1);
}

.widget-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border-secondary);
}

.widget-header .el-icon {
  color: var(--page-accent);
}

.widget-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #fff7ed;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
  color: #b45309;
  border: 1px solid #fed7aa;
}

.announcement-item:hover {
  transform: translateX(2px);
  background: #ffedd5;
}

.announcement-item .el-icon {
  color: #f59e0b;
  font-size: 16px;
}

/* ?????? */
.loading-state,
.empty-state {
  padding: 40px 20px;
}

.empty-text {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 6px 0;
  font-weight: 500;
}

.empty-hint {
  font-size: 12px;
  color: var(--color-text-tertiary);
  margin: 0;
}

/* ??? */
@media (max-width: 1400px) {
  .content-section {
    grid-template-columns: 320px 1fr;
  }
}

@media (max-width: 1200px) {
  .content-section {
    grid-template-columns: 1fr;
  }

  .left-panel {
    flex-direction: row;
    overflow-x: auto;
  }

  .stats-panel {
    min-width: 420px;
  }

  .right-panel {
    min-height: 0;
  }
}

@media (max-width: 992px) {
  .search-section {
    padding: 20px;
  }

  .search-title {
    font-size: 22px;
  }

  .left-panel {
    flex-direction: column;
    overflow: hidden;
  }

  .stats-panel {
    min-width: auto;
  }
}

@media (max-width: 768px) {
  .home-page {
    padding: 16px;
    height: auto;
    min-height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
  }

  .search-section {
    padding: 18px;
    border-radius: 12px;
  }

  .search-title {
    font-size: 20px;
  }

  .quick-actions {
    flex-direction: column;
    gap: 10px;
  }

  .quick-actions:deep(.el-button) {
    width: 100%;
  }

  .stats-panel {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }

  .content-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
    flex: none;
    min-height: auto;
    overflow: visible;
  }

  .left-panel,
  .right-panel {
    min-height: auto;
    flex: none;
    overflow: visible;
  }

  .activity-panel,
  .hot-panel {
    min-height: auto;
    flex: none;
    overflow: visible;
  }

  .activity-panel .panel-content,
  .hot-panel .panel-content {
    max-height: none;
    overflow: visible;
  }

  .stat-value {
    font-size: 18px;
  }

  .hot-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .card-image {
    height: 160px;
  }

  .hot-panel .panel-footer {
    padding: 10px 12px;
  }

  .side-widget {
    display: none;
  }
}
</style>

