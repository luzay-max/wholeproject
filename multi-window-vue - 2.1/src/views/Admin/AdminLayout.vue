<template>
  <div class="admin-layout" :class="{ mobile: isMobile, 'aside-open': asideOpen }">
    <div v-if="isMobile && asideOpen" class="aside-mask" @click="asideOpen = false"></div>
    <el-container class="layout-container">
      <!-- 侧边栏 -->
      <el-aside width="220px" class="layout-aside" :class="{ 'is-mobile-open': isMobile && asideOpen }">
        <div class="aside-logo">
          <el-icon class="logo-icon"><Platform /></el-icon>
          <span class="logo-text">管理控制台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="aside-menu"
          router
          unique-opened
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>返回首页</span>
          </el-menu-item>
          <el-menu-item index="/admin/info">
            <el-icon><List /></el-icon>
            <span>信息管理中心</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/whitelist">
            <el-icon><DocumentChecked /></el-icon>
            <span>白名单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/dict">
            <el-icon><Collection /></el-icon>
            <span>数据字典</span>
          </el-menu-item>
          <el-menu-item index="/admin/comments">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/honor">
            <el-icon><Trophy /></el-icon>
            <span>光荣榜管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/activities">
            <el-icon><Operation /></el-icon>
            <span>活动日志</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Monitor /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container class="main-container">
        <!-- 头部：面包屑 + 用户信息 -->
        <el-header class="layout-header">
          <div class="header-left">
            <el-button text class="menu-toggle-btn" @click="toggleAside" v-if="isMobile">
              <el-icon><Menu /></el-icon>
            </el-button>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
                {{ item.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-button text class="home-btn" @click="goHome">
              <el-icon><House /></el-icon>
              <span v-if="!isMobile">首页</span>
            </el-button>
            <el-dropdown>
              <div class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar" />
                <span class="username">{{ userStore.userInfo?.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goHome">返回首页</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/user-center')">个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 多标签页 -->
        <div class="tabs-container">
          <el-tabs
            v-model="activeTab"
            type="card"
            closable
            @tab-click="handleTabClick"
            @tab-remove="handleTabRemove"
          >
            <el-tab-pane
              v-for="tab in tabs"
              :key="tab.path"
              :label="tab.title"
              :name="tab.path"
            />
          </el-tabs>
        </div>

        <!-- 内容区 -->
        <el-main class="layout-main">
          <router-view v-slot="{ Component }">
            <keep-alive :include="cachedTabs">
              <component :is="Component" :key="$route.fullPath" />
            </keep-alive>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../store/userStore';
import { Platform, Checked, User, List, ChatDotRound, Trophy, Operation, DocumentChecked, Monitor, Menu, House } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const MOBILE_BREAKPOINT = 992;

const activeMenu = computed(() => route.path);
const activeTab = ref(route.path);
const tabs = ref([]);
const cachedTabs = computed(() => tabs.value.map(tab => tab.name));
const isMobile = ref(false);
const asideOpen = ref(false);

// 面包屑计算
const breadcrumbs = computed(() => {
  return route.matched
    .filter(item => item.path.startsWith('/admin'))
    .map(item => {
      const title = item.meta?.title?.split(' - ')[0] || item.name || '';
      return {
        ...item,
        title: String(title).trim()
      };
    })
    .filter(item => item.title);
});

// 添加标签页
const addTab = (route) => {
  const { path, meta, name } = route;
  if (!path.startsWith('/admin')) return;
  
  const existingTab = tabs.value.find(tab => tab.path === path);
  if (!existingTab) {
    tabs.value.push({
      title: meta.title?.split(' - ')[0] || name,
      path: path,
      name: name
    });
  }
  activeTab.value = path;
};

// 点击标签页
const handleTabClick = (tab) => {
  router.push(tab.props.name);
};

// 移除标签页
const handleTabRemove = (targetPath) => {
  const index = tabs.value.findIndex(tab => tab.path === targetPath);
  if (index === -1) return;
  
  // 如果移除的是当前激活的 tab，则跳转到相邻的 tab
  if (activeTab.value === targetPath) {
    const nextTab = tabs.value[index + 1] || tabs.value[index - 1];
    if (nextTab) {
      router.push(nextTab.path);
    } else {
      router.push('/admin/info'); // 默认回退
    }
  }
  
  tabs.value.splice(index, 1);
};

const handleLogout = async () => {
  await userStore.logout();
  router.push('/login');
};

const goHome = () => {
  router.push('/');
};

const toggleAside = () => {
  asideOpen.value = !asideOpen.value;
};

const handleMenuSelect = () => {
  if (isMobile.value) {
    asideOpen.value = false;
  }
};

const updateViewportState = () => {
  const mobile = window.innerWidth <= MOBILE_BREAKPOINT;
  isMobile.value = mobile;
  if (!mobile) {
    asideOpen.value = false;
  }
};

watch(() => route.path, () => {
  addTab(route);
  if (isMobile.value) {
    asideOpen.value = false;
  }
}, { immediate: true });

onMounted(() => {
  updateViewportState();
  window.addEventListener('resize', updateViewportState);
  if (tabs.value.length === 0 && route.path.startsWith('/admin')) {
    addTab(route);
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportState);
});
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  width: 100vw;
  overflow-x: hidden;
  overflow-y: auto;
  background: #f0f2f5;
}

.aside-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 20;
}

.layout-container {
  height: 100%;
}

.layout-aside {
  background: #001529;
  color: #fff;
  transition: width 0.3s;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  z-index: 10;
}

.aside-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: #002140;
}

.logo-icon {
  font-size: 24px;
  color: #409eff;
  margin-right: 12px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.aside-menu {
  border-right: none;
  background: transparent;
}

:deep(.el-menu) {
  background: transparent;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.65);
}

:deep(.el-menu-item.is-active) {
  background: #1890ff !important;
  color: #fff;
}

:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.65);
}

.main-container {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.layout-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 64px !important;
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.menu-toggle-btn {
  margin-right: 8px;
  flex-shrink: 0;
}

.menu-toggle-btn :deep(.el-icon) {
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.home-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
  padding: 0 8px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 8px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(0, 0, 0, 0.025);
}

.username {
  margin-left: 8px;
  font-size: 14px;
  color: #666;
}

.tabs-container {
  background: #fff;
  padding: 6px 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-tabs--card > .el-tabs__header) {
  border-bottom: none;
  margin-bottom: 0;
}

:deep(.el-tabs--card > .el-tabs__header .el-tabs__nav) {
  border: none;
}

:deep(.el-tabs--card > .el-tabs__header .el-tabs__item) {
  border: 1px solid #f0f0f0;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  margin-right: 4px;
  height: 32px;
  line-height: 32px;
  font-size: 12px;
  background: #fafafa;
  transition: all 0.3s;
}

:deep(.el-tabs--card > .el-tabs__header .el-tabs__item.is-active) {
  background: #fff;
  border-color: #f0f0f0;
  color: #1890ff;
}

.layout-main {
  flex: 1;
  padding: 0;
  overflow: auto;
  -webkit-overflow-scrolling: touch;
  min-height: 0;
  background: #f0f2f5;
}

/* 滚动条美化 */
.layout-main::-webkit-scrollbar {
  width: 6px;
}

.layout-main::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.layout-main::-webkit-scrollbar-track {
  background: transparent;
}

@media (max-width: 992px) {
  .admin-layout {
    height: 100dvh;
  }

  .layout-aside {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    width: 220px !important;
    transform: translateX(-100%);
    transition: transform 0.24s ease;
    z-index: 30;
  }

  .layout-aside.is-mobile-open {
    transform: translateX(0);
  }

  .layout-header {
    padding: 0 12px;
  }

  .header-right .username {
    max-width: 60px;
  }

  .tabs-container {
    padding: 6px 8px 0;
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
  }

  .tabs-container :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  .tabs-container :deep(.el-tabs__nav-wrap) {
    overflow-x: auto;
  }

  .tabs-container :deep(.el-tabs__nav-scroll) {
    overflow: visible;
  }

  .layout-main {
    padding: 0;
    overflow: auto;
    -webkit-overflow-scrolling: touch;
    padding-bottom: env(safe-area-inset-bottom);
  }
}

@media (max-width: 768px) {
  .layout-header {
    height: 56px !important;
  }

  .header-left :deep(.el-breadcrumb__item:not(:last-child)) {
    display: none;
  }

  .header-right .username {
    display: none;
  }
}
</style>

