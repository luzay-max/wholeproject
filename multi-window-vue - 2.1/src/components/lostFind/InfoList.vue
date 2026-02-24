<template>
  <div class="info-list">
    <!-- 筛选和搜索区域 -->
    <div class="search-filter">
      <div class="container">
        <el-card shadow="hover" class="filter-card">
          <div class="filter-header">
            <h3>筛选条件</h3>
            <div class="filter-actions">
              <el-button type="primary" @click="handleSearch" class="search-btn">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset" class="reset-btn">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </div>
          </div>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="shouldShowAdminFeatures && showFilters ? 6 : 8">
              <div class="filter-item">
                <label class="filter-label">物品类型</label>
                <DictSelect 
                  v-model="filters.type" 
                  dict-type="info_type"
                  placeholder="选择物品类型" 
                  clearable
                  class="filter-select"
                />
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <div class="filter-item">
                <label class="filter-label">时间范围</label>
                <el-date-picker
                  v-model="filters.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="x"
                  class="filter-date-picker"
                ></el-date-picker>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" v-if="shouldShowAdminFeatures && showFilters">
              <div class="filter-item">
                <label class="filter-label">状态筛选</label>
                <DictSelect 
                  v-model="filters.status" 
                  dict-type="info_status"
                  placeholder="选择状态" 
                  clearable
                  class="filter-select"
                />
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="shouldShowAdminFeatures && showFilters ? 6 : 10">
              <div class="filter-item">
                <label class="filter-label">关键词搜索</label>
                <el-input
                  v-model="filters.keyword"
                  placeholder="输入物品名称或描述关键词"
                  prefix-icon="Search"
                  class="filter-input"
                  @keyup.enter="handleSearch"
                ></el-input>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </div>
    </div>

    <!-- 列表显示 -->
    <div class="list-container">
      <div class="container">
        <el-card class="card-container" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <h2 class="card-title">
                  <span class="title-icon" :class="infoType">
                    {{ infoType === 'lost' ? '🔍' : '📢' }}
                  </span>
                  {{ infoType === 'lost' ? '失物信息列表' : '招领信息列表' }}
                </h2>
                <span class="card-subtitle">共 {{ pagination.total }} 条记录</span>
              </div>
              <div class="header-actions">
                <el-button 
                  type="primary" 
                  :class="infoType === 'lost' ? 'lost-btn' : 'find-btn'"
                  @click="navigateToPublish"
                >
                  <el-icon><Plus /></el-icon>
                  发布{{ infoType === 'lost' ? '失物' : '招领' }}
                </el-button>
              </div>
            </div>
          </template>
          
          <el-table
            v-loading="loading"
            :data="infoList"
            style="width: 100%"
            @row-click="handleRowClick"
            class="info-table"
            :empty-text="emptyText"
          >
            <el-table-column prop="name" label="物品名称" width="200">
              <template #default="scope">
                <div class="item-name-cell">
                  <span class="item-name">{{ scope.row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="物品类型" width="130">
              <template #default="scope">
                <DictTag :options="itemTypeOptions" :value="scope.row.type" />
              </template>
            </el-table-column>
            <el-table-column prop="location" label="地点" width="180">
              <template #default="scope">
                <div class="location-cell">
                  <el-icon><Location /></el-icon>
                  <span>{{ scope.row.location }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="240">
              <template #default="scope">
                <div class="description-cell">
                  {{ scope.row.description }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="170">
              <template #default="scope">
                <div class="time-cell">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatDate(scope.row.publishTime) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column 
              v-if="shouldShowAdminFeatures" 
              prop="status" 
              label="状态" 
              width="110"
              align="center"
            >
              <template #default="scope">
                <DictTag :options="infoStatusOptions" :value="scope.row.status" />
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              class="custom-pagination"
            ></el-pagination>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import debounce from '../../utils/debounce';
import { Search, Refresh, Plus, Location, Clock } from '@element-plus/icons-vue';
import { getLostList } from '../../api/lostApi';
import { getFindList } from '../../api/findApi';
import { useUserStore } from '../../store/userStore';
import { useRouter } from 'vue-router';
import { getDicts } from '../../api/system/dict/data';
import DictSelect from '../Dict/DictSelect.vue';
import DictTag from '../Dict/DictTag.vue';

export default {
  name: 'InfoList',
  components: {
    DictSelect,
    DictTag,
    Search,
    Refresh,
    Plus,
    Location,
    Clock
  },
  props: {
    // 信息类型：lost（失物）或 find（招领）
    infoType: {
      type: String,
      default: 'lost',
      validator: (value) => ['lost', 'find'].includes(value)
    },
    // 是否显示筛选条件
    showFilters: {
      type: Boolean,
      default: true
    },
    // 是否显示管理员相关功能（状态标签、状态筛选等）
    showAdminFeatures: {
      type: Boolean,
      default: false
    },
    // 初始筛选条件
    initialFilters: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['view-detail', 'update:filters'],
  setup(props, { emit }) {
    const loading = ref(false);
    const infoList = ref([]);
    const userStore = useUserStore();
    const router = useRouter();
    
    const itemTypeOptions = ref([]);
    const infoStatusOptions = ref([]);

    onMounted(() => {
      getDicts('info_type').then(response => {
    itemTypeOptions.value = response.data;
  });
      getDicts('info_status').then(response => {
        infoStatusOptions.value = response.data;
      });
    });
    
    // 判断是否显示管理员功能
    // 计算属性：是否展示管理员功能
    const shouldShowAdminFeatures = computed(() => {
      // 如果父组件明确传入了 showAdminFeatures 值，则优先使用该值
      if (props.showAdminFeatures !== undefined) {
        return props.showAdminFeatures;
      }
      // 否则根据当前登录用户角色判断
      return userStore.isAdmin;
    });
    
    // 筛选条件
    const filters = reactive({
      type: '',
      dateRange: [],
      status: '',
      keyword: '',
      ...props.initialFilters
    });
    
    // 分页信息
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    });
    
    // 空数据提示文本
    const emptyText = computed(() => {
      if (loading.value) return '';
      return filters.keyword || filters.type || filters.status || (filters.dateRange && filters.dateRange.length)
        ? '没有找到匹配的记录'
        : `暂无${props.infoType === 'lost' ? '失物' : '招领'}信息`;
    });
    
    // 格式化日期
    const formatDate = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };
    
    // 加载数据
    const loadData = async () => {
      try {
        loading.value = true;
        
        // 构建查询参数，只包含非空参数
        const params = {
          page: pagination.currentPage,
          pageSize: pagination.pageSize
        };
        
        // 只添加非空的筛选条件
        if (filters.type) {
          params.type = filters.type;
        }
        if (filters.keyword) {
          params.keyword = filters.keyword;
        }
        // 只有管理员或明确允许显示管理员功能时才添加状态筛选
        if (shouldShowAdminFeatures.value && filters.status) {
          params.status = filters.status;
        }
        
        // 添加日期范围
        if (filters.dateRange && filters.dateRange.length === 2) {
          params.startDate = filters.dateRange[0];
          params.endDate = filters.dateRange[1];
        }
        
        // 根据类型调用不同的API
        let res;
        if (props.infoType === 'lost') {
          res = await getLostList(params);
        } else {
          res = await getFindList(params);
        }
        
        infoList.value = res.data.list || [];
        pagination.total = res.data.total || 0;
      } catch (error) {
        ElMessage.error('加载失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };
    
    // 搜索
    const handleSearchImmediate = () => {
      pagination.currentPage = 1;
      loadData();
      emit('update:filters', { ...filters });
    };
    const handleSearch = debounce(handleSearchImmediate, 400);
    
    // 重置
    const handleReset = () => {
      filters.type = '';
      filters.dateRange = [];
      // 只有管理员或明确允许显示管理员功能时才重置状态筛选
      if (shouldShowAdminFeatures.value) {
        filters.status = '';
      }
      filters.keyword = '';
      pagination.currentPage = 1;
      loadData();
      emit('update:filters', { ...filters });
    };
    
    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size;
      loadData();
    };
    
    // 当前页变化
    const handleCurrentChange = (current) => {
      pagination.currentPage = current;
      loadData();
    };
    
    // 行点击事件
    const handleRowClick = (row) => {
      emit('view-detail', { id: row.id, type: props.infoType });
    };
    
    // 导航到发布页面
    const navigateToPublish = () => {
      const routeName = props.infoType === 'lost' ? 'LostPublish' : 'FindPublish';
      router.push({ name: routeName });
    };
    
    // 监听初始筛选条件变化
    watch(
      () => props.initialFilters,
      (newFilters) => {
        Object.assign(filters, newFilters);
        loadData();
      },
      { deep: true }
    );
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadData();
    });
    
    return {
      loading,
      infoList,
      filters,
      pagination,
      shouldShowAdminFeatures,
      emptyText,
      itemTypeOptions,
      infoStatusOptions,
      formatDate,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleRowClick,
      navigateToPublish
    };
  }
};
</script>

<style scoped>
.info-list {
  width: 100%;
  min-height: calc(100vh - 120px);
  background: transparent;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 筛选区域样式 */
.search-filter {
  padding: 20px 0;
  background: transparent;
}

.filter-card {
  border-radius: var(--app-radius-lg);
  border: 1px solid var(--border-light);
  background: var(--app-surface);
  box-shadow: var(--app-shadow-sm);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.filter-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.search-btn, .reset-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 6px;
  font-weight: 500;
}

.filter-item {
  margin-bottom: 15px;
}

.filter-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 500;
}

.filter-select,
.filter-date-picker,
.filter-input {
  width: 100%;
}

.filter-select :deep(.el-input__inner),
.filter-date-picker :deep(.el-input__inner),
.filter-input :deep(.el-input__inner) {
  border-radius: 6px;
}

/* 列表区域样式 */
.list-container {
  padding: 20px 0 40px;
}

.card-container {
  border-radius: var(--app-radius-lg);
  overflow: hidden;
  box-shadow: var(--app-shadow-sm);
  border: 1px solid var(--border-light);
  background: var(--app-surface);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 5px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.card-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 24px;
}

.title-icon.lost {
  color: var(--app-danger);
}

.title-icon.find {
  color: var(--app-primary);
}

.card-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.lost-btn, .find-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 6px;
  font-weight: 500;
  padding: 10px 16px;
}

.lost-btn {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.98), rgba(255, 142, 142, 0.96));
  border: none;
}

.find-btn {
  background: linear-gradient(135deg, rgba(79, 109, 245, 0.94), rgba(124, 92, 255, 0.90));
  border: none;
}

/* 表格样式 */
.info-table {
  margin: 10px 0;
}

.info-table :deep(.el-table__header) th {
  background-color: rgba(255, 255, 255, 0.44);
  color: var(--text-primary);
  font-weight: 600;
}

.info-table :deep(.el-table__row) {
  cursor: pointer;
  transition: background-color 0.2s;
}

.info-table :deep(.el-table__row:hover) {
  background-color: rgba(79, 109, 245, 0.06);
}

.info-table :deep(.el-table__empty-block) {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-table :deep(.el-table__empty-text) {
  color: var(--text-secondary);
  font-size: 16px;
}

.item-name-cell {
  padding: 5px 0;
}

.item-name {
  font-weight: 500;
  color: var(--text-primary);
}

.category-tag {
  border: none;
  font-weight: 500;
}

.location-cell,
.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-regular);
}

.description-cell {
  line-height: 1.5;
  color: var(--text-regular);
  max-height: 3em;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  box-orient: vertical;
  -webkit-box-orient: vertical;
}

.status-tag {
  font-weight: 500;
  border-radius: 4px;
}

/* 分页样式 */
.pagination-container {
  margin-top: 25px;
  display: flex;
  justify-content: center;
}

.custom-pagination {
  padding: 10px 0;
}

.custom-pagination :deep(.btn-prev),
.custom-pagination :deep(.btn-next),
.custom-pagination :deep(.number) {
  border-radius: 6px;
}

.custom-pagination :deep(.el-pager li.active) {
  background-color: var(--primary-color);
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }
  
  .filter-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .filter-actions {
    width: 100%;
  }
  
  .search-btn, .reset-btn {
    flex: 1;
    justify-content: center;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .lost-btn, .find-btn {
    width: 100%;
    justify-content: center;
  }
  
  .info-table :deep(.el-table) {
    font-size: 14px;
  }
  
  .pagination-container {
    overflow-x: auto;
  }
}

@media (max-width: 480px) {
  .search-filter {
    padding: 15px 0;
  }
  
  .list-container {
    padding: 15px 0 30px;
  }
  
  .card-title {
    font-size: 18px;
  }
  
  .filter-item {
    margin-bottom: 12px;
  }
}
</style>
