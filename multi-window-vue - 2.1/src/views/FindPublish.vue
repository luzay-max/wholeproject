<template>
  <div class="publish-page">
    <div class="publish-header">
      <h2 class="page-title">发布招领信息</h2>
    </div>
    <div class="publish-content">
      <FindForm @publish-success="handlePublishSuccess" />
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';
import { useInfoStore } from '../store/infoStore';
import { ElMessage } from 'element-plus';
import FindForm from '../components/lostFind/FindForm.vue';


export default {
  name: 'FindPublish',
  components: {
    FindForm
  },
  setup() {
    const router = useRouter();
    const infoStore = useInfoStore();
    
    // 处理发布成功
    const handlePublishSuccess = async (data) => {await infoStore.fetchActivities();
      // 跳转到信息详情页
      const infoId = data?.id || data?.data?.id;
      if (!infoId) {
        router.push({ name: 'FindList' });
        return;
      }
      router.push({
        name: 'InfoDetailPage',
        params: { id: infoId, type: 'find' }
      });
    };
    
    return {
      handlePublishSuccess
    };
  }
};
</script>

<style scoped>
.publish-page {
  --page-accent: #0f766e;
  --page-accent-soft: rgba(15, 118, 110, 0.12);
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: radial-gradient(800px 260px at 6% -10%, rgba(15, 118, 110, 0.10), transparent 60%),
    linear-gradient(180deg, #f8fafc 0%, #ffffff 70%);
  overflow: hidden;
  box-sizing: border-box;
}

.publish-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid var(--color-border-secondary);
  box-shadow: var(--shadow-1);
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.publish-content {
  flex: 1;
  min-height: 0;
  background-color: #ffffff;
  border: 1px solid var(--color-border-secondary);
  border-radius: 16px;
  padding: 16px;
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: var(--shadow-1);
}

@media (max-width: 768px) {
  .publish-page {
    padding: 12px;
  }

  .publish-content {
    padding: 14px;
  }
}

@media (max-width: 480px) {
  .publish-page {
    padding: 10px;
  }

  .publish-content {
    padding: 12px;
  }
}
</style>




