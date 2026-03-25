<template>
  <div class="admin-page">
    <!-- 筛选和搜索区域 -->
    <div class="search-area">
      <div class="filter-card">
        <el-row :gutter="16">
          <el-col :span="6">
            <DictSelect v-model="filters.type" dict-type="activity_item_type" placeholder="信息类型" clearable style="width: 100%" />
          </el-col>
          <el-col :span="6">
            <DictSelect v-model="filters.status" dict-type="info_status" placeholder="审核状态" style="width: 100%" />
          </el-col>
          <el-col :span="8">
            <el-input v-model="filters.keyword" placeholder="搜索物品名称或发布者" prefix-icon="Search" clearable></el-input>
          </el-col>
          <el-col :span="4" class="btn-group">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-area">
      <div class="table-header">
        <span class="table-title">信息审核列表</span>
        <div class="header-actions">
          <el-button v-if="!isMobile" type="primary" @click="handleBatchApprove" :disabled="selectedItems.length === 0">
            批量通过
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="auditList"
        :height="isMobile ? undefined : '100%'"
        :fit="!isMobile"
        :table-layout="isMobile ? 'auto' : 'fixed'"
        style="width: 100%; flex: 1;"
        @selection-change="handleSelectionChange"
      >
        <el-table-column v-if="!isMobile" type="selection" width="55"></el-table-column>
        <el-table-column v-if="!isMobile" prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="type" label="信息类型" width="100">
          <template #default="scope">
            <DictTag :options="activity_item_type" :value="scope.row.type" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="物品名称" width="200"></el-table-column>
        <el-table-column v-if="!isMobile" prop="publisherName" label="发布者" width="120"></el-table-column>
        <el-table-column v-if="!isMobile" prop="publishTime" label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="scope">
            <DictTag :options="info_status" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 170 : 180" :fixed="isMobile ? false : 'right'">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'PENDING'" type="primary" size="small" :link="isMobile" @click="handleApprove(scope.row)">通过</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" type="danger" size="small" :link="isMobile" @click="handleReject(scope.row)">拒绝</el-button>
            <el-button type="primary" size="small" :link="isMobile" plain @click="handleViewDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-area">
        <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total" @size-change="handleSizeChange" @current-change="handleCurrentChange"></el-pagination>
      </div>
    </div>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef">
        <el-form-item prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" placeholder="请输入拒绝原因" :rows="4" maxlength="200"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmReject">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getAuditList, auditApprove, auditReject, batchAuditApprove } from '../../api/auditApi';
import { getDicts } from '../../api/system/dict/data';
import DictSelect from '../../components/Dict/DictSelect.vue';
import DictTag from '../../components/Dict/DictTag.vue';

export default {
  name: 'AuditPage',
  components: {
    DictSelect,
    DictTag
  },
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const auditList = ref([]);
    const selectedItems = ref([]);
    const rejectDialogVisible = ref(false);
    const rejectFormRef = ref(null);
    const currentAuditItem = ref(null);
    const isMobile = ref(false);

    const filters = reactive({
      type: '',
      status: 'PENDING',
      keyword: ''
    });

    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    });

    const rejectForm = reactive({
      reason: ''
    });

    const info_status = ref([]);
    const activity_item_type = ref([]);

    const rejectRules = {
      reason: [
        { required: true, message: '请输入拒绝原因', trigger: 'blur' },
        { min: 5, max: 200, message: '拒绝原因长度在5-200个字符之间', trigger: 'blur' }
      ]
    };

    const formatDate = (ts) => {
      if (!ts) return '';
      const d = new Date(ts);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    };

    const updateViewportState = () => {
      isMobile.value = window.innerWidth <= 768;
    };

    const loadData = async () => {
      try {
        loading.value = true;
        const params = {
          page: pagination.currentPage,
          pageSize: pagination.pageSize,
          type: filters.type,
          status: filters.status,
          keyword: filters.keyword
        };
        const res = await getAuditList(params);
        auditList.value = res.data.list || [];
        pagination.total = res.data.total || 0;
        selectedItems.value = [];
      } catch (e) {
        ElMessage.error('加载失败：' + (e.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleSearch = () => {
      pagination.currentPage = 1;
      loadData();
    };

    const handleReset = () => {
      filters.type = '';
      filters.status = 'PENDING';
      filters.keyword = '';
      pagination.currentPage = 1;
      loadData();
    };

    const handleSelectionChange = (val) => {
      selectedItems.value = val;
    };

    const handleApprove = async (item) => {
      try {
        await auditApprove({ id: item.id, type: item.type });
        item.status = 'APPROVED';
        ElMessage.success('审核通过');
      } catch (e) {
        ElMessage.error('审核失败：' + (e.message || '未知错误'));
      }
    };

    const handleReject = (item) => {
      currentAuditItem.value = item;
      rejectForm.reason = '';
      rejectDialogVisible.value = true;
    };

    const handleConfirmReject = async () => {
      if (!rejectFormRef.value || !currentAuditItem.value) return;
      try {
        await rejectFormRef.value.validate();
        await auditReject({
          id: currentAuditItem.value.id,
          type: currentAuditItem.value.type,
          reason: rejectForm.reason
        });
        currentAuditItem.value.status = 'REJECTED';
        rejectDialogVisible.value = false;
        ElMessage.success('已拒绝');
      } catch (e) {
        if (e !== false) ElMessage.error('操作失败：' + (e.message || '未知错误'));
      }
    };

    const handleBatchApprove = async () => {
      if (!selectedItems.value.length) {
        return ElMessage.warning('请选择要通过的项目');
      }
      try {
        loading.value = true;
        await batchAuditApprove(selectedItems.value.map(i => ({ id: i.id, type: i.type })));
        selectedItems.value.forEach(i => {
          const found = auditList.value.find(a => a.id === i.id);
          if (found) found.status = 'APPROVED';
        });
        selectedItems.value = [];
        ElMessage.success('批量通过成功');
      } catch (e) {
        ElMessage.error('批量操作失败：' + (e.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleSizeChange = (s) => {
      pagination.pageSize = s;
      loadData();
    };

    const handleCurrentChange = (p) => {
      pagination.currentPage = p;
      loadData();
    };

    const handleViewDetail = (item) => {
      router.push({
        name: 'InfoDetailPage',
        params: { id: item.id, type: item.type }
      });
    };

    onMounted(() => {
      updateViewportState();
      window.addEventListener('resize', updateViewportState);
      loadData();
      getDicts('info_status').then(res => info_status.value = res.data);
      getDicts('activity_item_type').then(res => activity_item_type.value = res.data);
    });

    onBeforeUnmount(() => {
      window.removeEventListener('resize', updateViewportState);
    });

    return {
      loading,
      auditList,
      filters,
      info_status,
      activity_item_type,
      pagination,
      selectedItems,
      rejectDialogVisible,
      rejectForm,
      rejectRules,
      rejectFormRef,
      formatDate,
      handleSearch,
      handleReset,
      handleSelectionChange,
      handleApprove,
      handleReject,
      handleConfirmReject,
      handleBatchApprove,
      handleSizeChange,
      handleCurrentChange,
      handleViewDetail,
      isMobile
    };
  }
};
</script>

<style scoped>
.admin-page {
  --page-accent: #2563eb;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: radial-gradient(900px 360px at 10% -10%, rgba(37, 99, 235, 0.08), transparent 60%),
    linear-gradient(180deg, #f8fafc 0%, #ffffff 70%);
  box-sizing: border-box;
  overflow: hidden;
}

.search-area {
  flex-shrink: 0;
}

.filter-card {
  background: #ffffff;
  border: 1px solid var(--color-border-secondary);
  border-radius: 14px;
  padding: 14px;
  box-shadow: var(--shadow-1);
}

.btn-group {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.table-area {
  flex: 1;
  min-height: 0;
  background: #ffffff;
  border: 1px solid var(--color-border-secondary);
  border-radius: 16px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: var(--shadow-1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 10px;
  flex-shrink: 0;
}

.table-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-area {
  flex-shrink: 0;
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  row-gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .admin-page {
    height: auto;
    min-height: 100%;
    overflow: auto;
    padding: 12px;
  }

  .filter-card,
  .table-area {
    padding: 12px;
  }

  .table-area {
    overflow: visible;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .pagination-area {
    justify-content: center;
  }
}
</style>


