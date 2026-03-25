<template>
  <div class="admin-page">
    <!-- 筛选和搜索区域 -->
    <div class="search-area">
      <div class="filter-card">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-input
              v-model="filters.keyword"
              placeholder="搜索物品名称或关键词"
              prefix-icon="Search"
              clearable
            ></el-input>
          </el-col>
          <el-col :span="6">
            <DictSelect
              v-model="filters.type"
              dict-type="info_type"
              placeholder="选择物品类型"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="6">
            <DictSelect
              v-model="filters.status"
              dict-type="info_status"
              placeholder="选择状态"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="6" class="btn-group">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 失物列表 -->
    <div class="table-area">
      <div class="table-header">
        <span class="table-title">失物列表</span>
        <div class="header-actions">
          <el-button v-if="!isMobile" type="danger" @click="handleBatchDelete" :disabled="selectedItems.length === 0">
            批量删除
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="lostList"
        :height="isMobile ? undefined : '100%'"
        :fit="!isMobile"
        :table-layout="isMobile ? 'auto' : 'fixed'"
        style="width: 100%; flex: 1;"
        @selection-change="handleSelectionChange"
      >
        <el-table-column v-if="!isMobile" type="selection" width="55"></el-table-column>
        <el-table-column v-if="!isMobile" prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="物品名称" width="200"></el-table-column>
        <el-table-column prop="type" label="物品类型" width="120">
          <template #default="scope">
            <DictTag :options="info_type" :value="scope.row.type" />
          </template>
        </el-table-column>
        <el-table-column prop="location" label="丢失地点" width="150"></el-table-column>
        <el-table-column v-if="!isMobile" prop="publishTime" label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <DictTag :options="info_status" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="viewCount" label="浏览次数" width="100"></el-table-column>
        <el-table-column label="操作" :width="isMobile ? 180 : 220" :fixed="isMobile ? false : 'right'">
          <template #default="scope">
            <el-button type="primary" size="small" :link="isMobile" @click="handleViewDetail(scope.row)">
              查看详情
            </el-button>
            <el-button type="warning" size="small" :link="isMobile" @click="handleChangeStatus(scope.row)">
              状态
            </el-button>
            <el-button type="danger" size="small" :link="isMobile" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-area">
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
    </div>

    <!-- 修改状态对话框 -->
    <el-dialog v-model="statusDialogVisible" title="修改状态" width="400px">
      <el-form :model="statusForm" :rules="statusRules" ref="statusFormRef" v-focus-next="handleConfirmStatus">
        <el-form-item label="物品名称" prop="name">
          <el-input v-model="statusForm.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="当前状态" prop="currentStatus">
          <el-input v-model="currentStatusText" disabled></el-input>
        </el-form-item>
        <el-form-item label="新状态" prop="status">
          <DictSelect v-model="statusForm.status" dict-type="info_status" placeholder="请选择新状态" style="width: 100%" />
        </el-form-item>
        <el-form-item label="拒绝原因" v-if="statusForm.status === 'REJECTED'" prop="rejectReason">
          <el-input
            v-model="statusForm.rejectReason"
            type="textarea"
            placeholder="请输入拒绝原因"
            :rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmStatus">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import { Search } from '@element-plus/icons-vue';
import { getLostItems, updateLostStatus, deleteLostItem, batchDeleteLostItems } from '../../api/adminApi';
import DictSelect from '../../components/Dict/DictSelect.vue';
import DictTag from '../../components/Dict/DictTag.vue';
import { getDicts } from '../../api/system/dict/data';


export default {
  name: 'LostManagement',
  components: {
    Search,
    DictSelect,
    DictTag
  },
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const lostList = ref([]);
    const selectedItems = ref([]);
    const statusDialogVisible = ref(false);
    const statusFormRef = ref(null);
    const info_type = ref([]);
    const info_status = ref([]);
    const isMobile = ref(false);

    // 筛选条件
    const filters = reactive({
      keyword: '',
      type: '',
      status: '',
      startTime: '',
      endTime: ''
    });

    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    });

    // 状态表单
    const statusForm = reactive({
      id: '',
      name: '',
      status: '',
      rejectReason: ''
    });

    // 表单验证规则
    const statusRules = {
      status: [{ required: true, message: '请选择状态', trigger: 'change' }],
      rejectReason: [
        { required: true, message: '请输入拒绝原因', trigger: 'blur' },
        { min: 5, max: 500, message: '拒绝原因长度在5-500个字符之间', trigger: 'blur' }
      ]
    };

    const updateViewportState = () => {
      isMobile.value = window.innerWidth <= 768;
    };

    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };

    // 当前状态文本（用于显示在对话框中）
    const currentStatusText = computed(() => {
      if (!statusForm.status) return '';
      const option = info_status.value.find(opt => opt.value === statusForm.status);
      return option ? option.label : statusForm.status;
    });

    // 加载失物列表
    const loadLostList = async () => {
      try {
        loading.value = true;
        
        // 调用真实API获取失物列表
        const params = {
          page: pagination.currentPage,
          pageSize: pagination.pageSize,
          keyword: filters.keyword,
          type: filters.type,
          status: filters.status,
          startTime: filters.startTime,
          endTime: filters.endTime
        };
        
        const res = await getLostItems(params);
        lostList.value = res.data.list || [];
        pagination.total = res.data.total || 0;
      } catch (error) {
        ElMessage.error('加载失物列表失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    // 搜索
    const handleSearch = () => {
      pagination.currentPage = 1;
      loadLostList();
    };

    // 重置筛选条件
    const handleReset = () => {
      filters.keyword = '';
      filters.type = '';
      filters.status = '';
      filters.startTime = '';
      filters.endTime = '';
      pagination.currentPage = 1;
      loadLostList();
    };

    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size;
      pagination.currentPage = 1;
      loadLostList();
    };

    // 当前页变化
    const handleCurrentChange = (page) => {
      pagination.currentPage = page;
      loadLostList();
    };

    // 选择物品
    const handleSelectionChange = (val) => {
      selectedItems.value = val;
    };

    // 查看详情
    const handleViewDetail = (item) => {
      router.push({
        name: 'InfoDetailPage',
        params: { id: item.id, type: 'lost' }
      });
    };

    // 修改状态
    const handleChangeStatus = (item) => {
      statusForm.id = item.id;
      statusForm.name = item.name;
      statusForm.status = item.status;
      statusForm.rejectReason = item.rejectReason || '';
      statusDialogVisible.value = true;
    };

    // 确认修改状态
    const handleConfirmStatus = async () => {
      if (!statusFormRef.value) return;
      
      try {
        await statusFormRef.value.validate();
        
        // 调用真实API更新失物状态
        await updateLostStatus(statusForm.id, {
          status: statusForm.status,
          rejectReason: statusForm.status === 'REJECTED' ? statusForm.rejectReason : ''
        });
        
        ElMessage.success('状态更新成功');
        statusDialogVisible.value = false;
        loadLostList();
      } catch (error) {
        if (error !== false) {
          ElMessage.error('更新失败：' + (error.message || '未知错误'));
        }
      }
    };

    // 删除失物
    const handleDelete = async (item) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除失物信息「${item.name}」吗？`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );
        
        // 调用真实API删除失物
        await deleteLostItem(item.id);
        
        ElMessage.success('失物信息删除成功');
        loadLostList();
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败：' + (error.message || '未知错误'));
        }
      }
    };

    // 批量删除
    const handleBatchDelete = async () => {
      if (selectedItems.value.length === 0) {
        ElMessage.warning('请选择要删除的失物信息');
        return;
      }
      
      try {
        await ElMessageBox.confirm(
          `确定要删除选中的 ${selectedItems.value.length} 条失物信息吗？`,
          '批量删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );
        
        // 调用真实API批量删除失物
        const idsToDelete = selectedItems.value.map(item => item.id);
        await batchDeleteLostItems(idsToDelete);
        
        ElMessage.success('批量删除成功');
        selectedItems.value = [];
        loadLostList();
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('批量删除失败：' + (error.message || '未知错误'));
        }
      }
    };

    // 组件加载时加载数据
    onMounted(() => {
      updateViewportState();
      window.addEventListener('resize', updateViewportState);
      loadLostList();
      getDicts('info_type').then(response => {
        info_type.value = response.data;
      });
      getDicts('info_status').then(response => {
        info_status.value = response.data;
      });
    });

    onBeforeUnmount(() => {
      window.removeEventListener('resize', updateViewportState);
    });

    return {
      loading,
      lostList,
      selectedItems,
      statusDialogVisible,
      statusFormRef,
      filters,
      pagination,
      statusForm,
      statusRules,
      formatDate,
      currentStatusText,
      loadLostList,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      handleViewDetail,
      handleChangeStatus,
      handleConfirmStatus,
      handleDelete,
      handleBatchDelete,
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
