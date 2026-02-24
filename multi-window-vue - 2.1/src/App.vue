<template>
  <div class="app">
    <!-- 独立页面：登录/注册 - 无导航栏 -->
    <template v-if="isStandalonePage">
      <div v-if="isAuthPage" class="auth-layout">
        <router-view />
      </div>
      <router-view v-else />
    </template>
    
    <!-- 业务页面：有导航栏，占满视口 -->
    <template v-else>
      <div class="business-layout">
        <Navbar class="layout-header" />
        <main class="layout-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </main>
      </div>
    </template>
  </div>
</template>

<script>
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import Navbar from './components/common/Navbar.vue';
import { useUserStore } from './store/userStore';

export default {
  name: 'App',
  components: {
    Navbar
  },
  setup() {
    const route = useRoute();
    const userStore = useUserStore();

    const isAuthPage = computed(() => {
      return route.path === '/login' || route.path === '/register';
    });
    
    // 判断是否为独立页面（登录/注册）
    const isStandalonePage = computed(() => {
      return isAuthPage.value || route.path.startsWith('/admin');
    });
    
    onMounted(() => {
      userStore.checkLoginStatus();
    });
    
    return {
      isStandalonePage,
      isAuthPage
    };
  }
};
</script>

<style>
.app {
  height: 100%;
  overflow: hidden;
}

.auth-layout {
  height: 100dvh;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
}

/* ?????? */
.business-layout {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--color-bg-layout);
}

.layout-header {
  flex-shrink: 0;
  height: 56px;
  background: var(--color-bg-container);
  border-bottom: 1px solid var(--color-border-secondary);
}

.layout-content {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 16px 20px 20px;
}

@media (max-width: 992px) {
  .business-layout {
    height: 100dvh;
  }

  .layout-content {
    overflow-y: auto;
    overflow-x: hidden;
    padding: 12px;
  }
}

/* ?????? */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
