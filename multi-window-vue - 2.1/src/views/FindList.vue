<template>
  <div class="list-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb-container">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>招领信息</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 椤甸潰澶撮儴 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon><Search /></el-icon>
        </div>
        <div class="header-text">
          <h1 class="header-title">招领信息</h1>
          <p class="header-subtitle">发现失物，完璧归赵</p>
        </div>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-value">{{ pagination.total }}</span>
          <span class="stat-label">条招领记录</span>
        </div>
      </div>
    </div>

    <!-- 筛选卡片 -->
    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-item search-item">
          <span class="filter-label">关键词</span>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索物品名称"
            clearable
            @keyup.enter="handleSearchDebounced"
            class="filter-input"
          >
            <template #prefix>
              <el-icon class="input-icon"><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="filter-item">
          <span class="filter-label">物品类型</span>
          <DictSelect v-model="searchForm.type" dict-type="info_type" placeholder="全部" clearable class="filter-select" style="width: 140px" />
        </div>

        <div class="filter-item" v-if="userStore.isAdmin">
          <span class="filter-label">状态筛选</span>
          <DictSelect v-model="searchForm.status" dict-type="info_status" placeholder="全部" clearable class="filter-select" style="width: 140px" />
        </div>

        <div class="filter-item date-item">
          <span class="filter-label">发布日期</span>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="x"
            class="filter-date"
          />
        </div>

        <div class="filter-actions">
          <el-button type="primary" @click="handleSearchDebounced" class="action-btn search-btn">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch" class="action-btn reset-btn">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 视图切换与发布按钮 -->
    <div class="toolbar">
      <div class="view-switch">
        <el-button-group>
          <el-button
            :type="viewMode === 'table' ? 'primary' : ''"
            @click="viewMode = 'table'"
            class="view-btn"
          >
            <el-icon><Grid /></el-icon>
          </el-button>
          <el-button
            :type="viewMode === 'card' ? 'primary' : ''"
            @click="viewMode = 'card'"
            class="view-btn"
          >
            <el-icon><Menu /></el-icon>
          </el-button>
        </el-button-group>
        <span class="view-label">当前 {{ findList.length }} 条记录</span>
</div>

      <el-button
        type="primary"
        @click="handlePublish"
        v-if="userStore.isLoggedIn && !userStore.isAdmin"
        class="publish-btn"
      >
        <el-icon><Plus /></el-icon>
        发布招领
      </el-button>
    </div>
    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 表格视图 -->
      <div v-if="viewMode === 'table'">
        <div v-if="loading" class="loading-wrapper">
          <el-skeleton :rows="5" animated />
          <el-skeleton :rows="5" animated />
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="findList.length === 0" class="empty-wrapper">
          <el-empty description="暂无招领信息" :image-size="200">
            <template #description>
              <p class="empty-text">暂时没有找到招领信息</p>
              <p class="empty-tip">您可以尝试其他搜索条件，或发布新的招领信息</p>
            </template>
          </el-empty>
        </div>

        <template v-else>
          <el-table
            :data="findList"
            stripe
            class="data-table"
            @row-click="handleRowClick"
          >
            <el-table-column prop="name" label="物品名称" min-width="140">
              <template #default="{ row }">
                <div class="item-name">
                  <div class="item-icon" :class="'type-' + row.type?.toLowerCase()">
                    <el-icon><Box /></el-icon>
                  </div>
                  <span class="item-title">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="110">
              <template #default="{ row }">
                <DictTag :options="info_type" :value="row.type" size="small" class="type-tag" />
              </template>
            </el-table-column>
            <el-table-column prop="location" label="发现地点" min-width="150" show-overflow-tooltip />
            <el-table-column prop="description" label="物品描述" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="description-text">{{ row.description || '暂无描述' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <DictTag :options="info_status" :value="row.status" size="small" class="status-tag" />
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="120">
              <template #default="{ row }">
                <span class="time-text">{{ formatDate(row.publishTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click.stop="viewDetail(row.id)" class="detail-btn">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </div>

      <!-- 卡片视图 -->
      <el-row v-else :gutter="20" class="card-list">
        <el-col
          v-for="item in findList"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="card-col"
        >
          <el-card class="item-card" shadow="hover" @click="viewDetail(item.id)">
            <div class="card-image-wrapper">
              <el-image
                :src="getFirstImage(item)"
                class="card-image"
                fit="cover"
              >
                <template #placeholder>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="card-tag-wrapper">
                <DictTag :options="info_status" :value="item.status" effect="dark" size="small" class="status-tag" />
              </div>
            </div>

            <div class="card-content">
              <div class="card-header-info">
                <h3 class="card-title" :title="item.name">{{ item.name }}</h3>
                <DictTag :options="info_type" :value="item.type" size="small" effect="plain" class="type-tag" />
              </div>

              <div class="card-info-row">
                <el-icon><Location /></el-icon>
                <span class="info-text" :title="item.location">{{ item.location }}</span>
              </div>

              <div class="card-info-row">
                <el-icon><Clock /></el-icon>
                <span class="info-text">{{ formatDate(item.foundTime) }}</span>
              </div>

              <div class="card-desc" :title="item.description">
                {{ item.description || '暂无描述' }}
              </div>

              <div class="card-footer">
                <div class="publisher-info">
                  <el-avatar :size="24" :src="item.publisherAvatar" class="publisher-avatar">
                    {{ item.publisherName?.charAt(0) }}
                  </el-avatar>
                  <span class="publisher-name">{{ item.publisherName }}</span>
                </div>
                <div class="view-count">
                  <el-icon><View /></el-icon>
                  <span>{{ item.viewCount || 0 }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total || findList.length"
        :page-count="pageCount"
        :hide-on-single-page="false"
        :page-sizes="[8, 12, 16, 24]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  Plus,
  Search,
  RefreshRight,
  Grid,
  Menu,
  Box,
  View,
  Picture,
  Location,
  Clock
} from '@element-plus/icons-vue';
import { getFindList } from '../api/findApi';
import { useUserStore } from '../store/userStore';
import { getDicts } from '../api/system/dict/data';
import DictSelect from '../components/Dict/DictSelect.vue';
import DictTag from '../components/Dict/DictTag.vue';

export default {
  name: 'FindList',
  components: {
    DictSelect,
    DictTag,
    Plus,
    Search,
    RefreshRight,
    Grid,
    Menu,
    Box,
    View,
    Picture,
    Location,
    Clock
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const userStore = useUserStore();

    const loading = ref(false);
    const viewMode = ref('table');
    const findList = ref([]);
    const info_type = ref([]);
    const info_status = ref([]);

    const searchForm = reactive({
      keyword: route.query.keyword || '',
      type: '',
      status: '',
      dateRange: []
    });

    const pagination = reactive({
      page: 1,
      pageSize: 8,
      total: 0
    });

    const pageCount = computed(() => {
      const effectiveTotal = Number(pagination.total) || findList.value.length || 0;
      return Math.max(1, Math.ceil(effectiveTotal / pagination.pageSize));
    });

    const loadData = async () => {
      loading.value = true;
      try {
        const params = {
          page: pagination.page,
          pageSize: pagination.pageSize,
          keyword: searchForm.keyword,
          type: searchForm.type,
          status: userStore.isAdmin ? searchForm.status : 'APPROVED'
        };

        if (searchForm.dateRange && searchForm.dateRange.length === 2) {
          params.startDate = searchForm.dateRange[0];
          // 结束时间设置为当天 23:59:59.999
          params.endDate = Number(searchForm.dateRange[1]) + 86399999;
        }

        const res = await getFindList(params);
        findList.value = res.data?.list || [];
        const totalFromApi = res?.data?.total ?? res?.data?.totalCount ?? res?.data?.count;
        pagination.total = Number(totalFromApi ?? findList.value.length ?? 0);
      } catch (error) {
        console.error('获取招领列表失败:', error);
      } finally {
        loading.value = false;
      }
    };

    const handleSearch = () => {
      pagination.page = 1;
      loadData();
    };

    let searchTimer = null;
    const handleSearchDebounced = () => {
      if (searchTimer) clearTimeout(searchTimer);
      searchTimer = setTimeout(() => {
        handleSearch();
      }, 300);
    };

    const resetSearch = () => {
      searchForm.keyword = '';
      searchForm.type = '';
      searchForm.status = '';
      searchForm.dateRange = [];
      handleSearch();
    };

    const handlePublish = () => {
      router.push('/find/publish');
    };

    const viewDetail = (id) => {
      router.push({
        name: 'InfoDetailPage',
        params: { type: 'find', id }
      });
    };

    const handleRowClick = (row) => {
      viewDetail(row.id);
    };

    const handlePageChange = (page) => {
      pagination.page = page;
      loadData();
    };

    const handleSizeChange = (size) => {
      pagination.pageSize = size;
      pagination.page = 1;
      loadData();
    };

    const formatDate = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    };

    const getDefaultImage = (type) => {
      // 返回默认图片路径，后续可按业务类型细分
      return '/images/default-find.png';
    };

    const getFirstImage = (item) => {
      let images = [];
      try {
        if (Array.isArray(item.images)) {
          images = item.images;
        } else if (typeof item.images === 'string') {
          images = JSON.parse(item.images);
        }
      } catch (e) {
        // ignore parse error
      }
      return (images && images.length > 0) ? images[0] : getDefaultImage(item.type);
    };

    watch(
      () => [searchForm.type, searchForm.status, searchForm.dateRange?.[0], searchForm.dateRange?.[1]],
      () => {
        handleSearch();
      }
    );

    onBeforeUnmount(() => {
      if (searchTimer) {
        clearTimeout(searchTimer);
        searchTimer = null;
      }
    });

    onMounted(() => {
      loadData();
      getDicts('info_type').then(response => {
        info_type.value = response.data;
      });
      getDicts('info_status').then(response => {
        info_status.value = response.data;
      });
    });

    return {
      userStore,
      loading,
      viewMode,
      findList,
      searchForm,
      pagination,
      pageCount,
      info_type,
      info_status,
      handleSearch,
      handleSearchDebounced,
      resetSearch,
      handlePublish,
      viewDetail,
      handleRowClick,
      handlePageChange,
      handleSizeChange,
      formatDate,
      // 瀵煎嚭鏂板鏂规硶
      getDefaultImage,
      getFirstImage
    };
  }
};
</script>

<style scoped>
.list-page {
  --page-accent: #0f766e;
  --page-accent-strong: #0c5f59;
  --page-accent-soft: rgba(15, 118, 110, 0.12);
  --page-surface: #ffffff;
  --page-border: #e6e8ef;
  --page-text: #0f172a;
  --page-text-muted: #64748b;
  height: 100%;
  min-height: 0;
  background: radial-gradient(900px 380px at 8% -10%, rgba(15, 118, 110, 0.12), transparent 60%),
    linear-gradient(180deg, #f8fafc 0%, #ffffff 60%);
  padding: 24px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  gap: 16px;
  color: var(--page-text);
  font-family: "Manrope", "Noto Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--page-surface);
  border-radius: 16px;
  border: 1px solid var(--page-border);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  flex-shrink: 0;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--page-accent-soft);
  color: var(--page-accent);
  border-radius: 12px;
  border: 1px solid rgba(15, 118, 110, 0.2);
}

.header-icon .el-icon {
  font-size: 18px;
}

.header-text {
  color: var(--page-text);
}

.header-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.header-subtitle {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--page-text-muted);
}

.header-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  align-items: center;
  padding: 4px 12px;
  background: #f8fafc;
  border-radius: 999px;
  border: 1px solid var(--page-border);
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--page-text);
}

.stat-label {
  font-size: 12px;
  color: var(--page-text-muted);
}

.filter-card {
  background: var(--page-surface);
  border-radius: 16px;
  padding: 12px 16px;
  border: 1px solid var(--page-border);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  flex-shrink: 0;
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  gap: 4px;
}

.filter-label {
  font-size: 12px;
  color: var(--page-text-muted);
  font-weight: 500;
}

.search-item {
  flex: 1;
  min-width: 220px;
}

.filter-input {
  width: 100%;
}

.filter-select {
  width: 140px;
}

.date-item {
  flex: 0 0 auto;
}

.filter-date {
  width: 260px;
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.search-btn {
  background: var(--page-accent);
  border: 1px solid var(--page-accent);
}

.search-btn:hover {
  background: var(--page-accent-strong);
  border-color: var(--page-accent-strong);
  box-shadow: 0 6px 14px rgba(15, 118, 110, 0.2);
}

.reset-btn {
  background: #ffffff;
  border: 1px solid var(--page-border);
  color: var(--page-text);
}

.reset-btn:hover {
  border-color: var(--page-accent);
  color: var(--page-accent);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2px;
  flex-shrink: 0;
  min-height: 40px;
}

.view-switch {
  display: flex;
  align-items: center;
  gap: 10px;
}

.view-btn {
  padding: 6px 12px;
  border-radius: 8px;
}

.view-label {
  font-size: 12px;
  color: var(--page-text-muted);
}

.publish-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  background: var(--page-accent);
  border: 1px solid var(--page-accent);
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.publish-btn:hover {
  background: var(--page-accent-strong);
  border-color: var(--page-accent-strong);
  box-shadow: 0 6px 14px rgba(15, 118, 110, 0.2);
}

.content-area {
  flex: 1;
  min-height: 0;
  background: var(--page-surface);
  border-radius: 16px;
  padding: 16px;
  border: 1px solid var(--page-border);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  overflow: auto;
}

.loading-wrapper,
.empty-wrapper {
  padding: 60px 20px;
}

.empty-text {
  font-size: 15px;
  color: var(--page-text);
  margin-bottom: 6px;
}

.empty-tip {
  font-size: 13px;
  color: var(--page-text-muted);
}

.data-table {
  width: 100%;
}

:deep(.el-table) {
  --el-table-border-color: var(--page-border);
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #eef7f6;
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table th) {
  font-weight: 600;
  color: var(--page-text);
}

:deep(.el-table .cell) {
  padding: 12px 16px;
}

.item-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-icon {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 118, 110, 0.12);
  border-radius: 10px;
  color: var(--page-accent);
}

.item-title {
  font-weight: 500;
  color: var(--page-text);
}

.type-tag,
.status-tag {
  border-radius: 999px;
  font-weight: 500;
}

.description-text {
  color: var(--page-text-muted);
  font-size: 13px;
}

.time-text {
  color: var(--page-text-muted);
  font-size: 13px;
}

.detail-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

:deep(.row-pending) {
  background: #fff7ed;
}

:deep(.row-approved) {
  background: #ecfdf3;
}

:deep(.row-solved) {
  background: #f8fafc;
}

.card-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.item-card {
  border: none;
  transition: all 0.3s ease;
  cursor: pointer;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.1);
}

.card-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 60%; /* 16:9 aspect ratio */
  overflow: hidden;
  background-color: #f5f7fa;
}

.card-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.item-card:hover .card-image {
  transform: scale(1.05);
}

.card-tag-wrapper {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}

.card-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  flex: 1;
  margin-right: 8px;
}

.card-info-row {
  display: flex;
  align-items: center;
  color: #606266;
  font-size: 13px;
  margin-bottom: 6px;
}

.card-info-row .el-icon {
  margin-right: 6px;
  font-size: 14px;
  color: #909399;
}

.info-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  color: #909399;
  font-size: 13px;
  margin: 8px 0 16px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  margin-top: auto;
}

.publisher-info {
  display: flex;
  align-items: center;
}

.publisher-avatar {
  margin-right: 6px;
  background-color: #409eff;
  color: #fff;
  font-size: 12px;
}

.publisher-name {
  font-size: 12px;
  color: #606266;
}

.view-count {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 12px;
}

.view-count .el-icon {
  margin-right: 4px;
}

.image-placeholder, .image-error {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 32px;
  background-color: #f5f7fa;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 12px 0 4px;
  width: 100%;
}

.pagination {
  width: 100%;
  max-width: 100%;
  padding: 10px 16px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  border: 1px solid var(--page-border);
  display: flex;
  justify-content: center;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: var(--page-accent);
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  row-gap: 8px;
  column-gap: 8px;
}

:deep(.el-pagination__sizes),
:deep(.el-pagination__jump) {
  margin-left: 0;
  display: inline-flex !important;
  align-items: center;
  visibility: visible !important;
}

:deep(.el-pagination .el-pager li.is-active) {
  background: var(--page-accent);
}

:deep(.el-pagination .el-pager li:hover) {
  color: var(--page-accent);
}

@media (max-width: 1200px) {
  .filter-row {
    gap: 12px;
  }

  .date-item {
    width: 100%;
  }

  .filter-date {
    width: 100%;
  }
}

@media (max-width: 992px) {
  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .header-content {
    flex-direction: row;
  }

  .header-stats {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .list-page {
    padding: 16px;
    gap: 16px;
    height: auto;
    min-height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
  }

  .page-header {
    padding: 16px;
    border-radius: 12px;
  }

  .header-icon {
    width: 40px;
    height: 40px;
  }

  .header-icon .el-icon {
    font-size: 20px;
  }

  .header-title {
    font-size: 19px;
  }

  .filter-card {
    padding: 14px;
    border-radius: 12px;
  }

  .filter-row {
    flex-direction: column;
    gap: 12px;
  }

  .filter-item {
    width: 100%;
  }

  .filter-select,
  .filter-date {
    width: 100%;
  }

  .filter-actions {
    width: 100%;
    margin-left: 0;
  }

  .action-btn {
    flex: 1;
    justify-content: center;
  }

  .toolbar {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }

  .publish-btn {
    width: 100%;
    justify-content: center;
  }

  .content-area {
    flex: none;
    min-height: 320px;
    overflow-x: auto;
    overflow-y: visible;
  }

  .content-area :deep(.el-table),
  .content-area :deep(.el-table__inner-wrapper) {
    min-width: 880px;
  }

  .card-grid {
    grid-template-columns: 1fr;
  }

  .stat-item {
    padding: 6px 12px;
  }

  .stat-value {
    font-size: 16px;
  }

  .pagination-section {
    padding-bottom: 12px;
  }
}
</style>

