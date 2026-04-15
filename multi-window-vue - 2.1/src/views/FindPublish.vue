<template>
  <div class="publish-page">
    <div class="publish-header">
      <h2 class="page-title">{{ isEditMode ? '编辑招领信息' : '发布招领信息' }}</h2>
    </div>
    <div class="publish-content">
      <FindForm
        v-loading="loadingEdit"
        :submit-mode="isEditMode ? 'edit' : 'publish'"
        :initial-data="editData"
        @publish-success="handlePublishSuccess"
        @update-success="handleUpdateSuccess"
      />
    </div>
  </div>
</template>

<script>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useInfoStore } from '../store/infoStore';
import { ElMessage } from 'element-plus';
import FindForm from '../components/lostFind/FindForm.vue';
import { getFindDetail } from '../api/findApi';


export default {
  name: 'FindPublish',
  components: {
    FindForm
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const infoStore = useInfoStore();
    const editData = ref(null);
    const loadingEdit = ref(false);
    const editId = computed(() => route.query.editId || '');
    const isEditMode = computed(() => Boolean(editId.value));

    const loadEditDetail = async () => {
      if (!editId.value) {
        editData.value = null;
        return;
      }
      try {
        loadingEdit.value = true;
        const res = await getFindDetail(editId.value);
        editData.value = res?.data || null;
      } catch (error) {
        ElMessage.error(error?.message || '加载待编辑信息失败');
        router.replace({ name: 'UserCenterPage', query: { tab: 'publishes' } });
      } finally {
        loadingEdit.value = false;
      }
    };
    
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

    const handleUpdateSuccess = async () => {
      await infoStore.fetchActivities();
      if (!editId.value) {
        router.push({ name: 'UserCenterPage', query: { tab: 'publishes' } });
        return;
      }
      router.replace({
        name: 'InfoDetailPage',
        params: { id: editId.value, type: 'find' }
      });
    };

    watch(() => route.query.editId, loadEditDetail, { immediate: true });
    
    return {
      isEditMode,
      editData,
      loadingEdit,
      handlePublishSuccess,
      handleUpdateSuccess
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




