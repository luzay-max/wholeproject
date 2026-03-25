<template>
  <div class="admin-page-container">
    <div class="admin-search-card">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input
            v-model="filters.userId"
            placeholder="搜索用户ID"
            clearable
          ></el-input>
        </el-col>
        <el-col :span="6">
          <DictSelect
            v-model="filters.actionType"
            dict-type="sys_oper_type"
            placeholder="搜索操作类型"
            clearable
            style="width: 100%; min-width: 150px"
          />
        </el-col>
        <el-col :span="8">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="4" class="btn-group">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <div class="admin-content-card admin-content-card--fixed-pagination">
      <div class="admin-table-header">
        <span class="admin-table-title">操作日志列表</span>
      </div>

      <div class="admin-table-wrapper">
        <el-table
          v-loading="loading"
          :data="logList"
          :fit="!isMobile"
          :table-layout="isMobile ? 'auto' : 'fixed'"
          class="admin-table"
          style="width: 100%"
        >
          <el-table-column v-if="!isMobile" prop="id" label="ID" width="120">
            <template #default="scope">
              <el-tooltip :content="String(scope.row.id || '')" placement="top">
                <el-button size="small" class="id-chip" @click.stop="handleCopyId(scope.row.id)">
                  {{ formatShortId(scope.row.id) }}
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" prop="userId" label="操作人ID" width="140">
            <template #default="scope">
              <el-tooltip v-if="scope.row.userId" :content="String(scope.row.userId)" placement="top">
                <el-button size="small" class="id-chip" @click.stop="handleCopyId(scope.row.userId)">
                  {{ formatShortId(scope.row.userId) }}
                </el-button>
              </el-tooltip>
              <span v-else>系统/未登录</span>
            </template>
          </el-table-column>
          <el-table-column prop="actionType" label="操作类型" width="180">
            <template #default="scope">
              <DictTag :options="sys_oper_type" :value="scope.row.actionType" />
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" prop="createTime" label="操作时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="content" label="详情内容" min-width="300">
            <template #default="scope">
              <span class="text-truncate">{{ scope.row.content }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isMobile ? 90 : 100" :fixed="isMobile ? false : 'right'">
            <template #default="scope">
              <el-button type="primary" link size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="admin-pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total || logList.length"
          :page-count="pageCount"
          :hide-on-single-page="false"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" title="操作日志详情" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">
          <div class="id-inline">
            <span class="id-text">{{ currentLog.id || '-' }}</span>
            <el-button size="small" text class="copy-btn" @click.stop="handleCopyId(currentLog.id)">
              复制
            </el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="操作人ID">
          <div v-if="currentLog.userId" class="id-inline">
            <span class="id-text">{{ currentLog.userId }}</span>
            <el-button size="small" text class="copy-btn" @click.stop="handleCopyId(currentLog.userId)">
              复制
            </el-button>
          </div>
          <span v-else>系统/未登录</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <DictTag :options="sys_oper_type" :value="currentLog.actionType" />
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDate(currentLog.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="可视化内容">
          <div v-if="parsedContent" class="log-visual">
            <el-descriptions :column="2" size="small" border class="log-visual-grid">
              <el-descriptions-item label="操作类型">
                {{ parsedContent.operationType || currentLog.actionType || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="结果">
                <DictTag :options="sys_oper_status" :value="parsedContent.resultCode" />
              </el-descriptions-item>
              <el-descriptions-item label="方法">{{ parsedContent.method || '-' }}</el-descriptions-item>
              <el-descriptions-item label="URI">{{ parsedContent.uri || '-' }}</el-descriptions-item>
              <el-descriptions-item label="账号">
                {{ parsedContent.username || parsedContent.accountName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="IP">{{ parsedContent.ip || '-' }}</el-descriptions-item>
              <el-descriptions-item label="时间">
                {{ formatDate(parsedContent.timestamp || currentLog.createTime) || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="结果码">
                {{ parsedContent.resultCode !== undefined ? parsedContent.resultCode : '-' }}
              </el-descriptions-item>
            </el-descriptions>

            <div v-if="parsedArgs.length" class="log-args">
              <div class="log-args-title">参数</div>
              <div class="log-args-list">
                <div v-for="item in parsedArgs" :key="item.key" class="log-arg-item">
                  <span class="log-arg-key">{{ item.key }}</span>
                  <span class="log-arg-val">{{ item.value }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="log-visual-empty">无法解析结构化内容</div>
        </el-descriptions-item>
        <el-descriptions-item label="完整内容">
          <pre class="json-content">{{ formatJson(currentLog.content) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, onBeforeUnmount } from 'vue';
import { ElMessage } from 'element-plus';
import { getOperationLogs } from '../../api/adminApi';
import { getDicts } from '../../api/system/dict/data';
import DictTag from '../../components/Dict/DictTag.vue';
import DictSelect from '../../components/Dict/DictSelect.vue';

export default {
  name: 'LogManagement',
  components: {
    DictTag,
    DictSelect
  },
  setup() {
    const loading = ref(false);
    const logList = ref([]);
    const detailDialogVisible = ref(false);
    const currentLog = ref({});
    const dateRange = ref([]);
    const sys_oper_type = ref([]);
    const sys_oper_status = ref([]);
    const isMobile = ref(false);

    const filters = reactive({
      userId: '',
      actionType: '',
      startTime: '',
      endTime: ''
    });

    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    });

    const pageCount = computed(() => {
      const effectiveTotal = Number(pagination.total) || logList.value.length || 0;
      return Math.max(1, Math.ceil(effectiveTotal / pagination.pageSize));
    });

    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
    };

    const updateViewportState = () => {
      isMobile.value = window.innerWidth <= 768;
    };

    const formatShortId = (value) => {
      if (value === null || value === undefined) return '-';
      const str = String(value);
      if (str.length <= 8) return str;
      return `${str.slice(0, 4)}…${str.slice(-4)}`;
    };

    const copyToClipboard = async (text) => {
      try {
        if (navigator?.clipboard?.writeText) {
          await navigator.clipboard.writeText(text);
          return true;
        }
      } catch (_) {}
      try {
        const textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.top = '-9999px';
        document.body.appendChild(textarea);
        textarea.select();
        const ok = document.execCommand('copy');
        document.body.removeChild(textarea);
        return ok;
      } catch (_) {
        return false;
      }
    };

    const handleCopyId = async (id) => {
      if (id === null || id === undefined || id === '') return;
      const text = String(id);
      const ok = await copyToClipboard(text);
      if (ok) {
        ElMessage.success(`已复制：${text}`);
      } else {
        ElMessage.error('复制失败');
      }
    };

    const formatJson = (jsonStr) => {
      try {
        if (!jsonStr) return '';
        const obj = JSON.parse(jsonStr);
        return JSON.stringify(obj, null, 2);
      } catch (e) {
        return jsonStr;
      }
    };

    const parseLogContent = (raw) => {
      if (!raw) return null;
      if (typeof raw === 'object') return raw;
      try {
        return JSON.parse(raw);
      } catch {
        return null;
      }
    };

    const parsedContent = computed(() => parseLogContent(currentLog.value?.content));

    const parsedArgs = computed(() => {
      const args = parsedContent.value?.args;
      if (!args || typeof args !== 'object') return [];
      return Object.entries(args).map(([key, value]) => ({
        key,
        value: typeof value === 'string' ? value : JSON.stringify(value)
      }));
    });

    const handleDateChange = (val) => {
      if (val && val.length === 2) {
        filters.startTime = val[0] + ' 00:00:00';
        filters.endTime = val[1] + ' 23:59:59';
      } else {
        filters.startTime = '';
        filters.endTime = '';
      }
    };

    const loadLogList = async () => {
      try {
        loading.value = true;
        const params = {
          page: pagination.currentPage,
          pageSize: pagination.pageSize,
          userId: filters.userId,
          actionType: filters.actionType,
          startTime: filters.startTime,
          endTime: filters.endTime
        };

        const res = await getOperationLogs(params);
        logList.value = res?.data?.records || res?.data?.list || [];
        const totalFromApi = res?.data?.total ?? res?.data?.totalCount ?? res?.data?.count;
        pagination.total = Number(totalFromApi ?? logList.value.length ?? 0);
      } catch (error) {
        ElMessage.error('加载操作日志失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleSearch = () => {
      pagination.currentPage = 1;
      loadLogList();
    };

    const handleReset = () => {
      filters.userId = '';
      filters.actionType = '';
      filters.startTime = '';
      filters.endTime = '';
      dateRange.value = [];
      handleSearch();
    };

    const handleViewDetail = (row) => {
      currentLog.value = row;
      detailDialogVisible.value = true;
    };

    const handleSizeChange = (val) => {
      pagination.pageSize = val;
      loadLogList();
    };

    const handleCurrentChange = (val) => {
      pagination.currentPage = val;
      loadLogList();
    };

    onMounted(() => {
      updateViewportState();
      window.addEventListener('resize', updateViewportState);
      loadLogList();
      getDicts('sys_oper_type').then(res => {
        sys_oper_type.value = res.data;
      });
      getDicts('sys_oper_status').then(res => {
        sys_oper_status.value = res.data;
      });
    });

    onBeforeUnmount(() => {
      window.removeEventListener('resize', updateViewportState);
    });

    return {
      loading,
      logList,
      detailDialogVisible,
      currentLog,
      filters,
      pagination,
      pageCount,
      dateRange,
      formatDate,
      formatShortId,
      handleCopyId,
      formatJson,
      parsedContent,
      parsedArgs,
      handleDateChange,
      handleSearch,
      handleReset,
      handleViewDetail,
      handleSizeChange,
      handleCurrentChange,
      sys_oper_type,
      sys_oper_status,
      isMobile
    };
  }
};
</script>

<style scoped>
.admin-page-container {
  padding: 20px;
}

.admin-search-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.admin-content-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.admin-table-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-table-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.text-truncate {
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.json-content {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}

.log-visual {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.log-visual-grid :deep(.el-descriptions__body) {
  background: #ffffff;
}

.log-args {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 10px;
  background: #f8fafc;
}

.log-args-title {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

.log-args-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px 12px;
}

.log-arg-item {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #334155;
}

.log-arg-key {
  color: #64748b;
  min-width: 72px;
}

.log-arg-val {
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-visual-empty {
  font-size: 12px;
  color: #94a3b8;
}

.id-inline {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.id-text {
  font-family: var(--font-mono, monospace);
  color: #0f172a;
}

.copy-btn {
  padding: 0 6px;
  font-size: 12px;
}

.btn-group {
  display: flex;
  justify-content: flex-end;
}

.admin-table :deep(.el-table__cell) {
  padding-top: 8px;
  padding-bottom: 8px;
}

.admin-table :deep(.el-table__cell .cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.id-chip {
  height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 20px;
}

.admin-content-card--fixed-pagination {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
}

.admin-content-card--fixed-pagination .admin-table-wrapper {
  overflow: auto;
  min-height: 0;
}

.admin-content-card--fixed-pagination .admin-pagination {
  position: sticky;
  bottom: 0;
  padding-top: 8px;
  background: #fff;
  z-index: 3;
}

.admin-content-card--fixed-pagination :deep(.el-pagination__jump),
.admin-content-card--fixed-pagination :deep(.el-pagination__sizes) {
  display: inline-flex !important;
  align-items: center;
  visibility: visible !important;
}
</style>

