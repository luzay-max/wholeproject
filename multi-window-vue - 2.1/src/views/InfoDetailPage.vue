<template>
  <div :class="['detail-page', 'detail-' + type]">
    <div class="detail-main">
      <InfoDetail 
        :infoId="id" 
        :infoType="type"
        @back="handleBack"
      />
    </div>
    <div class="detail-sidebar">
      <CommentSection
        :infoId="id"
        :infoType="type"
      />
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import InfoDetail from '../components/lostFind/InfoDetail.vue';
import CommentSection from '../components/lostFind/CommentSection.vue';

export default {
  name: 'InfoDetailPage',
  components: {
    InfoDetail,
    CommentSection
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const id = ref('');
    const type = ref('lost');
    
    // 处理返回
    const handleBack = () => {
      router.back();
    };
    
    // 监听路由参数，保证“同页面切换不同详情”时也会刷新
    watch(
      () => [route.params.id, route.params.type],
      ([nextId, nextType]) => {
        id.value = String(nextId || '');
        type.value = nextType === 'find' ? 'find' : 'lost';
      },
      { immediate: true }
    );
    
    return {
      id,
      type,
      handleBack
    };
  }
};
</script>

<style scoped>
.detail-page {
  height: 100%;
  min-height: 0;
  display: flex;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 70%);
  overflow: hidden;
  box-sizing: border-box;
  --page-accent: #2563eb;
  --page-accent-soft: rgba(37, 99, 235, 0.12);
}

.detail-page.detail-find {
  --page-accent: #0f766e;
  --page-accent-soft: rgba(15, 118, 110, 0.12);
}

.detail-main,
.detail-sidebar {
  background: #ffffff;
  border: 1px solid var(--color-border-secondary);
  border-radius: 16px;
  box-shadow: var(--shadow-1);
  overflow: auto;
}

.detail-main {
  flex: 6;
  min-width: 0;
  padding: 16px;
}

.detail-sidebar {
  flex: 4;
  min-width: 320px;
  width: auto;
  padding: 14px;
}

@media (max-width: 1200px) {
  .detail-sidebar {
    width: 340px;
  }
}

@media (max-width: 992px) {
  .detail-page {
    flex-direction: column;
  }

  .detail-sidebar {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .detail-page {
    padding: 12px;
  }
}
</style>

