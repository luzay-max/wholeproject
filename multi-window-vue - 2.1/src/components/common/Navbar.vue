<template>
  <header class="navbar">
    <div class="navbar-inner">
      <!-- Logo -->
      <div class="logo" @click="navigateTo('/')">
        <span class="logo-text">失物招领</span>
      </div>
      
      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: route.path === item.path || route.path.startsWith(item.path + '/') }"
        >
          {{ item.title }}
        </router-link>
      </nav>
      
      <!-- 右侧操作区 -->
      <div class="actions">
        <div class="actions-desktop">
          <template v-if="!userStore.isLoggedIn">
            <el-button text @click="navigateTo('/login')">登录</el-button>
            <el-button type="primary" @click="navigateTo('/register')">注册</el-button>
          </template>
          
          <template v-else>
            <!-- 管理员菜单 -->
            <el-dropdown v-if="userStore.isAdmin" @command="handleAdminCommand">
              <span class="action-item">
                管理
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/admin/dashboard">管理看板</el-dropdown-item>
                  <el-dropdown-item command="/admin/info?tab=audit">信息审核</el-dropdown-item>
                  <el-dropdown-item command="/admin/users">用户管理</el-dropdown-item>
                  <el-dropdown-item command="/admin/info?tab=manage&type=lost">失物管理</el-dropdown-item>
                  <el-dropdown-item command="/admin/info?tab=manage&type=find">招领管理</el-dropdown-item>
                  <el-dropdown-item command="/admin/comments">评论管理</el-dropdown-item>
                  <el-dropdown-item command="/admin/activities">活动日志</el-dropdown-item>
                  <el-dropdown-item command="/admin/logs">操作日志管理</el-dropdown-item>
                  <el-dropdown-item command="/admin/honor">光荣榜管理</el-dropdown-item>
                  <el-dropdown-item command="/admin/whitelist">白名单管理</el-dropdown-item>
                  <el-dropdown-item command="/admin/dict">字典管理</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            
            <!-- 发布按钮 -->
            <el-dropdown v-else @command="handlePublishCommand">
              <el-button type="primary">
                发布
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/lost/publish">发布失物</el-dropdown-item>
                  <el-dropdown-item command="/find/publish">发布招领</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-badge :value="unreadCount > 99 ? '99+' : unreadCount" :hidden="unreadCount === 0">
              <el-button text class="action-item" @click="navigateTo('/notice-center')">
                通知
              </el-button>
            </el-badge>

            <el-button text class="action-item" @click="navigateTo('/claim-center')">
              认领中心
            </el-button>

            <!-- 用户菜单 -->
            <el-dropdown @command="handleUserCommand">
              <span class="action-item user-action">
                <el-avatar :size="28" :src="userStore.userInfo?.avatar" class="user-avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.username }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/user-center">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>

        <el-button
          text
          class="mobile-menu-trigger"
          @click="mobileMenuVisible = true"
          aria-label="打开移动菜单"
        >
          <el-icon><Menu /></el-icon>
        </el-button>
      </div>
    </div>
  </header>

  <el-drawer
    v-model="mobileMenuVisible"
    direction="rtl"
    size="80%"
    class="mobile-nav-drawer"
    :with-header="false"
    append-to-body
  >
    <div class="mobile-nav-body">
      <div class="mobile-nav-header">
        <div class="mobile-nav-title">失物招领</div>
      </div>

      <div class="mobile-nav-section">
        <div class="mobile-nav-section-title">导航</div>
        <el-button
          v-for="item in menuItems"
          :key="item.path"
          text
          class="mobile-nav-item"
          :class="{ active: route.path === item.path || route.path.startsWith(item.path + '/') }"
          @click="navigateToAndClose(item.path)"
        >
          {{ item.title }}
        </el-button>
      </div>

      <div class="mobile-nav-section">
        <div class="mobile-nav-section-title">功能</div>

        <template v-if="!userStore.isLoggedIn">
          <el-button class="mobile-nav-action" @click="navigateToAndClose('/login')">登录</el-button>
          <el-button class="mobile-nav-action" type="primary" @click="navigateToAndClose('/register')">注册</el-button>
        </template>

        <template v-else>
          <template v-if="userStore.isAdmin">
            <el-button
              v-for="item in adminMenuItems"
              :key="item.path"
              text
              class="mobile-nav-item"
              @click="navigateToAndClose(item.path)"
            >
              {{ item.title }}
            </el-button>
          </template>

          <template v-else>
            <el-button
              v-for="item in publishMenuItems"
              :key="item.path"
              text
              class="mobile-nav-item"
              @click="navigateToAndClose(item.path)"
            >
              {{ item.title }}
            </el-button>
          </template>

          <el-button text class="mobile-nav-item" @click="navigateToAndClose('/user-center')">个人中心</el-button>
          <el-button text class="mobile-nav-item" @click="navigateToAndClose('/notice-center')">通知中心</el-button>
          <el-button text class="mobile-nav-item" @click="navigateToAndClose('/claim-center')">认领中心</el-button>
          <el-button text class="mobile-nav-item danger" @click="handleMobileLogout">退出登录</el-button>
        </template>
      </div>
    </div>
  </el-drawer>
</template>

<script>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, Menu, User } from '@element-plus/icons-vue';
import { useUserStore } from '../../store/userStore';
import { logout } from '../../api/authApi';
import { getNoticeUnreadCount } from '../../api/noticeApi';

export default {
  name: 'Navbar',
  components: {
    ArrowDown,
    Menu,
    User
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const userStore = useUserStore();
    const mobileMenuVisible = ref(false);
    const unreadCount = ref(0);
    
    const menuItems = computed(() => {
      const items = [
        { title: '首页', path: '/' },
        { title: '失物信息', path: '/lost/list' },
        { title: '招领信息', path: '/find/list' },
        { title: '光荣榜', path: '/honor-board' }
      ];
      return items;
    });

    const adminMenuItems = [
      { title: '管理看板', path: '/admin/dashboard' },
      { title: '信息审核', path: '/admin/info?tab=audit' },
      { title: '用户管理', path: '/admin/users' },
      { title: '失物管理', path: '/admin/info?tab=manage&type=lost' },
      { title: '招领管理', path: '/admin/info?tab=manage&type=find' },
      { title: '评论管理', path: '/admin/comments' },
      { title: '活动日志', path: '/admin/activities' },
      { title: '操作日志管理', path: '/admin/logs' },
      { title: '光荣榜管理', path: '/admin/honor' },
      { title: '白名单管理', path: '/admin/whitelist' },
      { title: '字典管理', path: '/admin/dict' }
    ];

    const publishMenuItems = [
      { title: '发布失物', path: '/lost/publish' },
      { title: '发布招领', path: '/find/publish' }
    ];
    
    const navigateTo = (path) => {
      router.push(path);
    };

    const navigateToAndClose = (path) => {
      mobileMenuVisible.value = false;
      router.push(path);
    };
    
    const handleAdminCommand = (command) => {
      navigateToAndClose(command);
    };
    
    const handlePublishCommand = (command) => {
      navigateToAndClose(command);
    };
    
    const handleUserCommand = async (command) => {
      if (command === 'logout') {
        try {
          await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          });
          await logout();
          userStore.logout();
          ElMessage.success('已退出登录');
          router.push('/');
        } catch (e) {
          // 取消退出
        }
      } else {
        router.push(command);
      }
    };

    const handleMobileLogout = async () => {
      await handleUserCommand('logout');
      mobileMenuVisible.value = false;
    };

    const loadUnreadCount = async () => {
      if (!userStore.isLoggedIn) {
        unreadCount.value = 0;
        return;
      }
      try {
        const res = await getNoticeUnreadCount();
        unreadCount.value = Number(res?.data?.unreadCount || 0);
      } catch (_) {
        unreadCount.value = 0;
      }
    };

    onMounted(loadUnreadCount);
    watch(() => route.fullPath, loadUnreadCount);
    watch(() => userStore.isLoggedIn, loadUnreadCount);
    
    return {
      route,
      userStore,
      menuItems,
      adminMenuItems,
      publishMenuItems,
      unreadCount,
      mobileMenuVisible,
      navigateTo,
      navigateToAndClose,
      handleAdminCommand,
      handlePublishCommand,
      handleUserCommand,
      handleMobileLogout
    };
  }
};
</script>

<style scoped>
.navbar {
  height: 56px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid var(--color-border-secondary);
  backdrop-filter: blur(12px);
}

.navbar-inner {
  max-width: 1440px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

/* Logo */
.logo {
  flex-shrink: 0;
  cursor: pointer;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: 0.5px;
}

/* ???? */
.nav-menu {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px;
  border-radius: 999px;
  background: #f8fafc;
  border: 1px solid var(--color-border-secondary);
}

.nav-item {
  padding: 6px 14px;
  font-size: 14px;
  color: var(--color-text-secondary);
  border-radius: 999px;
  transition: all var(--transition-fast);
  font-weight: 500;
}

.nav-item:hover {
  color: var(--color-text-primary);
  background: rgba(37, 99, 235, 0.08);
}

.nav-item.active {
  color: var(--color-primary);
  background: rgba(37, 99, 235, 0.14);
  font-weight: 600;
}

/* ??? */
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.actions-desktop {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: 10px;
  transition: all var(--transition-fast);
}

.action-item:hover {
  color: var(--color-text-primary);
  background: #f8fafc;
}

.user-action {
  padding: 4px 8px 4px 4px;
  border: 1px solid var(--color-border-secondary);
  background: #ffffff;
}

.user-avatar {
  background: var(--color-primary);
  color: #fff;
  font-size: 12px;
}

.username {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  margin-left: 2px;
}

:deep(.el-button) {
  border-radius: 999px;
  font-weight: 600;
}

.mobile-menu-trigger {
  display: none;
  width: 36px;
  height: 36px;
  padding: 0;
}

.mobile-menu-trigger :deep(.el-icon) {
  font-size: 18px;
}

.mobile-nav-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.mobile-nav-header {
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border-secondary);
}

.mobile-nav-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.mobile-nav-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-nav-section-title {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.mobile-nav-item {
  justify-content: flex-start;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.mobile-nav-item.active {
  color: var(--color-primary);
  background: rgba(37, 99, 235, 0.08);
}

.mobile-nav-item.danger {
  color: var(--color-error);
}

.mobile-nav-action {
  width: 100%;
  justify-content: center;
}

@media (max-width: 992px) {
  .nav-menu,
  .actions-desktop {
    display: none;
  }

  .mobile-menu-trigger {
    display: inline-flex;
  }
}

@media (max-width: 768px) {
  .navbar-inner {
    padding: 0 12px;
  }

  .logo-text {
    font-size: 16px;
  }

  .username {
    display: none;
  }
}

:deep(.mobile-nav-drawer .el-drawer__body) {
  padding: 16px 14px;
}
</style>

