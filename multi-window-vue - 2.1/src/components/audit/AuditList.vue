<template>
  <div class="audit-list">
    <!-- 筛选和搜索区域 -->
    <div class="search-filter">
      <!-- 第一行：筛选条件 -->
      <el-row :gutter="20" class="filter-row">
        <el-col :span="6">
          <DictSelect v-model="filters.type" dict-type="activity_item_type" placeholder="信息类型" clearable />
        </el-col>
        <el-col :span="6">
          <DictSelect v-model="filters.status" dict-type="info_status" placeholder="审核状态" clearable />
        </el-col>
        <el-col :span="12">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="x"
            style="width: 100%"
          ></el-date-picker>
        </el-col>
      </el-row>

      <!-- 第二行：搜索框和按钮 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="18">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索物品名称或发布者"
            prefix-icon="Search"
          ></el-input>
        </el-col>
        <el-col :span="6" class="btn-group">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 审核列表 -->
    <el-card class="card-container">
      <template #header>
        <div class="card-header">
          <span>信息审核列表</span>
          <el-button type="primary" size="small" @click="handleBatchApprove" :disabled="!hasSelectedItems">
            批量通过
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="auditList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="type" label="信息类型" width="100">
          <template #default="scope">
            <DictTag :options="activity_item_type" :value="scope.row.type" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="物品名称" width="200"></el-table-column>
        <el-table-column prop="publisherName" label="发布者" width="120"></el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="scope">
            <DictTag :options="info_status" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PENDING'"
              type="primary"
              size="small"
              @click="handleApprove(scope.row)"
            >通过</el-button>
            <el-button
              v-if="scope.row.status === 'PENDING'"
              type="danger"
              size="small"
              @click="handleReject(scope.row)"
            >拒绝</el-button>
            <el-button type="text" size="small" @click="viewDetail(scope.row.id, scope.row.type)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef">
        <el-form-item prop="reason">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            placeholder="请输入拒绝原因"
            :rows="4"
            maxlength="200"
          ></el-input>
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
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getAuditList, auditApprove, auditReject, batchAuditApprove } from '../../api/auditApi';
import { getDicts } from '../../api/system/dict/data';
import DictSelect from '../Dict/DictSelect.vue';
import DictTag from '../Dict/DictTag.vue';

export default {
  name: 'AuditList',
  components: {
    DictSelect,
    DictTag
  },
  emits: ['view-detail'],
  setup(props, { emit }) {
    const loading = ref(false);
    const auditList = ref([]);
    const selectedItems = ref([]);
    const rejectDialogVisible = ref(false);
    const rejectFormRef = ref(null);
    const currentAuditItem = ref(null);

    const filters = reactive({
      type: '',
      status: '',
      dateRange: [],
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

    const hasSelectedItems = computed(() => selectedItems.value.length > 0);

    const formatDate = (ts) => {
      if (!ts) return '';
      const d = new Date(ts);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
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
        if (filters.dateRange?.length === 2) {
          params.startTime = filters.dateRange[0];
          params.endTime = filters.dateRange[1];
        }
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
      filters.infoType = '';
      filters.status = '';
      filters.dateRange = [];
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

    const viewDetail = (id, type) => emit('view-detail', { id, type });

    onMounted(() => {
      loadData();
      getDicts('info_status').then(res => info_status.value = res.data);
      getDicts('activity_item_type').then(res => activity_item_type.value = res.data);
    });

    return {
      loading,
      auditList,
      filters,
      info_status,
      activity_item_type,
      pagination,
      rejectDialogVisible,
      rejectForm,
      rejectRules,
      rejectFormRef,
      hasSelectedItems,
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
      viewDetail
    };
  }
};
</script>

<style scoped>
.audit-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-filter {
  background: transparent;
}

.filter-row,
.search-row {
  margin-bottom: 10px;
}

.btn-group {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
}

.card-container {
  background: var(--app-surface);
  border-radius: var(--app-radius-lg);
  overflow: hidden;
  box-shadow: var(--app-shadow-sm);
  border: 1px solid var(--border-light);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 1024px) {
  .search-filter .el-col {
    width: 50%;
  }
}

@media (max-width: 768px) {
  .search-filter .el-col {
    width: 100%;
  }
  .btn-group {
    justify-content: center;
  }
  .pagination {
    justify-content: center;
  }
}
</style>
