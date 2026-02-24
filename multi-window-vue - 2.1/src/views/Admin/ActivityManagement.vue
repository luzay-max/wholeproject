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
            v-model="filters.action"
            dict-type="activity_action"
            placeholder="选择操作类型"
            clearable
            style="width: 100%; min-width: 150px"
          />
        </el-col>
        <el-col :span="6">
          <DictSelect
            v-model="filters.itemType"
            dict-type="activity_item_type"
            placeholder="选择物品类型"
            clearable
            style="width: 100%; min-width: 150px"
          />
        </el-col>
        <el-col :span="6" class="btn-group">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleAdd">新增</el-button>
          <el-button type="info" @click="handleExport">导出</el-button>
        </el-col>
      </el-row>
    </div>

    <div class="admin-content-card admin-content-card--fixed-pagination">
      <div class="admin-table-header">
        <span class="admin-table-title">活动日志列表</span>
      </div>

      <div class="admin-table-wrapper">
        <el-table
          v-loading="loading"
          :data="activityList"
          :fit="!isMobile"
          :table-layout="isMobile ? 'auto' : 'fixed'"
          class="admin-table"
          style="width: 100%"
        >
          <el-table-column v-if="!isMobile" prop="id" label="ID" width="110">
            <template #default="scope">
              <el-tooltip :content="String(scope.row.id || '')" placement="top">
                <el-button size="small" class="id-chip" @click.stop="handleCopyId(scope.row.id)">
                  {{ formatShortId(scope.row.id) }}
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" prop="userId" label="用户ID" width="140">
            <template #default="scope">
              <el-tooltip :content="String(scope.row.userId || '')" placement="top">
                <el-button size="small" class="id-chip" @click.stop="handleCopyId(scope.row.userId)">
                  {{ formatShortId(scope.row.userId) }}
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="action" label="操作类型" width="120">
            <template #default="scope">
              <DictTag :options="activity_action" :value="normalizeActionValue(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column prop="itemType" label="物品类型" width="120">
            <template #default="scope">
              <DictTag :options="activity_item_type" :value="normalizeItemTypeValue(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" prop="itemId" label="物品ID" width="140">
            <template #default="scope">
              <el-tooltip :content="String(scope.row.itemId || '')" placement="top">
                <el-button size="small" class="id-chip" @click.stop="handleCopyId(scope.row.itemId)">
                  {{ formatShortId(scope.row.itemId) }}
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="活动内容" width="300"></el-table-column>
          <el-table-column v-if="!isMobile" prop="createTime" label="活动时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isMobile ? 170 : 200" :fixed="isMobile ? false : 'right'">
            <template #default="scope">
              <el-button type="primary" link size="small" @click="handleViewDetail(scope.row)">详情</el-button>
              <el-button type="primary" link size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(scope.row)">删除</el-button>
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
          :total="pagination.total || activityList.length"
          :page-count="pageCount"
          :hide-on-single-page="false"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" title="活动日志详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentActivity.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentActivity.userId }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <DictTag :options="activity_action" :value="normalizeActionValue(currentActivity)" />
        </el-descriptions-item>
        <el-descriptions-item label="物品类型">
          <DictTag :options="activity_item_type" :value="normalizeItemTypeValue(currentActivity)" />
        </el-descriptions-item>
        <el-descriptions-item label="物品ID">{{ currentActivity.itemId }}</el-descriptions-item>
        <el-descriptions-item label="活动时间">{{ formatDate(currentActivity.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="活动内容" :span="2">{{ currentActivity.content }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑活动日志' : '新增活动日志'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户ID"></el-input>
        </el-form-item>
        <el-form-item label="操作类型" prop="action">
          <DictSelect v-model="form.action" dict-type="activity_action" placeholder="请选择操作类型" style="width: 100%; min-width: 150px" />
        </el-form-item>
        <el-form-item label="物品类型" prop="itemType">
          <DictSelect v-model="form.itemType" dict-type="activity_item_type" placeholder="请选择物品类型" style="width: 100%; min-width: 150px" />
        </el-form-item>
        <el-form-item label="物品ID" prop="itemId">
          <el-input v-model="form.itemId" placeholder="请输入物品ID"></el-input>
        </el-form-item>
        <el-form-item label="活动内容" prop="content">
          <el-input type="textarea" v-model="form.content" placeholder="请输入活动内容"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, onBeforeUnmount } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getActivities, getActivityDetail, createActivity, updateActivity, deleteActivity } from '../../api/adminApi';
import { getDicts } from '../../api/system/dict/data';
import DictTag from '../../components/Dict/DictTag.vue';
import DictSelect from '../../components/Dict/DictSelect.vue';

export default {
  name: 'ActivityManagement',
  components: {
    DictTag,
    DictSelect
  },
  setup() {
    const loading = ref(false);
    const activityList = ref([]);
    const detailDialogVisible = ref(false);
    const currentActivity = ref({});
    const activity_action = ref([]);
    const activity_item_type = ref([]);
    const isMobile = ref(false);

    const editDialogVisible = ref(false);
    const isEdit = ref(false);
    const formRef = ref(null);
    const form = reactive({
      id: '',
      userId: '',
      action: '',
      itemType: '',
      itemId: '',
      content: ''
    });

    const rules = {
      userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
      action: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
      content: [{ required: true, message: '请输入活动内容', trigger: 'blur' }]
    };

    const filters = reactive({
      userId: '',
      action: '',
      itemType: '',
      startTime: '',
      endTime: ''
    });

    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    });

    const pageCount = computed(() => {
      const effectiveTotal = Number(pagination.total) || activityList.value.length || 0;
      return Math.max(1, Math.ceil(effectiveTotal / pagination.pageSize));
    });

    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };

    const updateViewportState = () => {
      isMobile.value = window.innerWidth <= 768;
    };

    const toCleanString = (value) => String(value ?? '').trim();

    const inferActionFromContent = (content) => {
      const text = toCleanString(content);
      if (!text) return '';
      if (text.includes('下架') || /hide/i.test(text)) return 'HIDE';
      if (text.includes('恢复') || text.includes('待审核')) return 'RECOVER';
      if (text.includes('评论') || /comment/i.test(text)) return 'COMMENT';
      if (text.includes('认领') || text.includes('找回') || text.includes('状态')) return 'UPDATE_STATUS';
      if (text.includes('发布') || /publish/i.test(text)) return 'PUBLISH';
      return '';
    };

    const inferItemTypeFromContent = (content) => {
      const text = toCleanString(content);
      if (!text) return '';
      if (text.includes('失物') || /lost/i.test(text)) return 'lost';
      if (text.includes('招领') || /find/i.test(text)) return 'find';
      return '';
    };

    const normalizeActionValue = (row) => {
      const directValue = toCleanString(row?.action ?? row?.actionType ?? row?.action_type);
      if (directValue) return directValue.toUpperCase();
      return inferActionFromContent(row?.content);
    };

    const normalizeItemTypeValue = (row) => {
      const directValue = toCleanString(row?.itemType ?? row?.item_type ?? row?.type);
      if (directValue) return directValue.toLowerCase();
      return inferItemTypeFromContent(row?.content);
    };

    const normalizeActivityRecord = (row = {}) => {
      // 尝试解码可能的乱码数据
      const decodeValue = (value) => {
        if (!value || typeof value !== 'string') return value;
        try {
          // 尝试解码 UTF-8
          return decodeURIComponent(escape(value));
        } catch (e) {
          // 如果解码失败，返回原始值
          return value;
        }
      };

      // 清理 ID 格式，去除可能的乱码前缀
      const cleanId = (id) => {
        if (!id || typeof id !== 'string') return id;
        // 移除可能的乱码前缀，如 "197f02(" 和后缀 ")"
        const match = id.match(/^[a-f0-9-]+$/i);
        if (match) return id;
        // 尝试提取括号中的内容
        const bracketMatch = id.match(/\(([^)]+)\)/);
        if (bracketMatch) return bracketMatch[1];
        // 尝试移除前缀乱码，保留后面的有效部分
        const cleanMatch = id.match(/[a-zA-Z0-9_-]+$/);
        if (cleanMatch) return cleanMatch[0];
        return id;
      };

      // 处理整个对象的所有字符串属性
      const processObject = (obj) => {
        if (!obj || typeof obj !== 'object') return obj;
        const processed = {};
        for (const key in obj) {
          if (Object.prototype.hasOwnProperty.call(obj, key)) {
            const value = obj[key];
            if (typeof value === 'string') {
              // 对于 ID 类型的字段，使用 cleanId
              if (key === 'id' || key === 'userId' || key === 'itemId' || key === 'user_id' || key === 'item_id') {
                processed[key] = cleanId(value);
              } else {
                // 对于其他字符串字段，使用 decodeValue
                processed[key] = decodeValue(value);
              }
            } else if (typeof value === 'object') {
              // 递归处理嵌套对象
              processed[key] = processObject(value);
            } else {
              processed[key] = value;
            }
          }
        }
        return processed;
      };

      // 先处理整个对象
      const processedRow = processObject(row);

      return {
        ...processedRow,
        action: normalizeActionValue(processedRow),
        itemType: normalizeItemTypeValue(processedRow),
        createTime: processedRow.createTime ?? processedRow.create_time ?? processedRow.time ?? null
      };
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

    const loadActivityList = async () => {
      try {
        loading.value = true;
        const params = {
          page: pagination.currentPage,
          pageSize: pagination.pageSize,
          userId: filters.userId,
          action: filters.action,
          itemType: filters.itemType,
          startTime: filters.startTime,
          endTime: filters.endTime
        };

        const res = await getActivities(params);
        const rawList = res?.data?.list || res?.data?.records || [];
        activityList.value = rawList.map((item) => normalizeActivityRecord(item));
        const totalFromApi = res?.data?.total ?? res?.data?.totalCount ?? res?.data?.count;
        pagination.total = Number(totalFromApi ?? activityList.value.length ?? 0);
      } catch (error) {
        ElMessage.error('加载活动日志失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleSearch = () => {
      pagination.currentPage = 1;
      loadActivityList();
    };

    const handleReset = () => {
      filters.userId = '';
      filters.action = '';
      filters.itemType = '';
      filters.startTime = '';
      filters.endTime = '';
      pagination.currentPage = 1;
      loadActivityList();
    };

    const handleSizeChange = (size) => {
      pagination.pageSize = size;
      pagination.currentPage = 1;
      loadActivityList();
    };

    const handleCurrentChange = (page) => {
      pagination.currentPage = page;
      loadActivityList();
    };

    const handleViewDetail = async (activity) => {
      try {
        loading.value = true;
        const res = await getActivityDetail(activity.id);
        currentActivity.value = normalizeActivityRecord(res?.data || {});
        detailDialogVisible.value = true;
      } catch (error) {
        ElMessage.error('获取活动详情失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleExport = async () => {
      try {
        loading.value = true;
        await new Promise((resolve) => setTimeout(resolve, 500));
        ElMessage.success('日志导出成功');
      } catch (error) {
        ElMessage.error('日志导出失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleAdd = () => {
      isEdit.value = false;
      form.id = '';
      form.userId = '';
      form.action = '';
      form.itemType = '';
      form.itemId = '';
      form.content = '';
      editDialogVisible.value = true;
    };

    const handleEdit = (row) => {
      isEdit.value = true;
      Object.assign(form, normalizeActivityRecord(row));
      editDialogVisible.value = true;
    };

    const handleDelete = (row) => {
      ElMessageBox.confirm('确认删除这条活动日志吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteActivity(row.id);
          ElMessage.success('删除成功');
          loadActivityList();
        } catch (error) {
          ElMessage.error('删除失败：' + (error.message || '未知错误'));
        }
      });
    };

    const handleSubmit = async () => {
      if (!formRef.value) return;
      await formRef.value.validate(async (valid) => {
        if (valid) {
          try {
            if (isEdit.value) {
              await updateActivity(form.id, form);
              ElMessage.success('更新成功');
            } else {
              await createActivity(form);
              ElMessage.success('创建成功');
            }
            editDialogVisible.value = false;
            loadActivityList();
          } catch (error) {
            ElMessage.error((isEdit.value ? '更新' : '创建') + '失败：' + (error.message || '未知错误'));
          }
        }
      });
    };

    onMounted(() => {
      updateViewportState();
      window.addEventListener('resize', updateViewportState);
      loadActivityList();
      getDicts('activity_action').then(res => {
        activity_action.value = res.data;
      });
      getDicts('activity_item_type').then(res => {
        activity_item_type.value = res.data;
      });
    });

    onBeforeUnmount(() => {
      window.removeEventListener('resize', updateViewportState);
    });

    return {
      loading,
      activityList,
      detailDialogVisible,
      currentActivity,
      activity_action,
      activity_item_type,
      filters,
      pagination,
      pageCount,
      formatDate,
      normalizeActionValue,
      normalizeItemTypeValue,
      formatShortId,
      handleCopyId,
      loadActivityList,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleViewDetail,
      handleExport,
      editDialogVisible,
      isEdit,
      form,
      rules,
      formRef,
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit,
      isMobile
    };
  }
};
</script>

<style scoped>
.btn-group {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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
</style>
