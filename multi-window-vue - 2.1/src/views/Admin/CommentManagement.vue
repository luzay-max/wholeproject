﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="admin-page-container">
    <!-- 筛选和搜索区域 -->
    <div class="admin-search-card">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索评论内容"
            prefix-icon="Search"
            clearable
          ></el-input>
        </el-col>
        <el-col :span="6">
          <DictSelect
            v-model="filters.infoType"
            dict-type="activity_item_type"
            placeholder="选择信息类型"
            clearable
            style="width: 100%; min-width: 150px"
          />
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="filters.userId"
            placeholder="搜索用户名字"
            clearable
          ></el-input>
        </el-col>
        <el-col :span="6" class="btn-group">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 评论列表 -->
    <div class="admin-content-card admin-content-card--fixed-pagination">
      <div class="admin-table-header">
        <span class="admin-table-title">评论列表</span>
        <div class="admin-header-actions">
          <el-button v-if="!isMobile" type="danger" @click="handleBatchDelete" :disabled="selectedComments.length === 0">
            批量删除
          </el-button>
        </div>
      </div>

      <div class="admin-table-wrapper">
        <el-table
          v-loading="loading"
          :data="commentList"
          :fit="!isMobile"
          :table-layout="isMobile ? 'auto' : 'fixed'"
          class="admin-table"
          style="width: 100%;"
          @selection-change="handleSelectionChange"
        >
          <el-table-column v-if="!isMobile" type="selection" width="55"></el-table-column>
          <el-table-column v-if="!isMobile" prop="id" label="ID" width="110">
            <template #default="scope">
              <el-tooltip :content="String(scope.row.id || '')" placement="top">
                <el-button size="small" class="id-chip" @click.stop="handleCopyId(scope.row.id)">
                  {{ formatShortId(scope.row.id) }}
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="infoType" label="信息类型" width="100">
            <template #default="scope">
              <DictTag
                v-if="hasDictValue(activity_item_type, getInfoTypeValue(scope.row))"
                :options="activity_item_type"
                :value="getInfoTypeValue(scope.row)"
              />
              <span v-else>{{ getInfoTypeLabel(scope.row) }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" prop="infoId" label="信息ID" width="140">
            <template #default="scope">
              <el-tooltip :content="String(scope.row.infoId || '')" placement="top">
                <el-button size="small" class="id-chip" @click.stop="handleCopyId(scope.row.infoId)">
                  {{ formatShortId(scope.row.infoId) }}
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
          <el-table-column v-if="!isMobile" prop="username" label="用户名" width="120">
            <template #default="scope">
              {{ scope.row.accountName || scope.row.username || '匿名用户' }}
            </template>
          </el-table-column>
          <el-table-column prop="content" label="评论内容" width="300">
            <template #default="scope">
              <span class="comment-content">{{ scope.row.content }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" prop="likeCount" label="点赞数" width="100"></el-table-column>
          <el-table-column v-if="!isMobile" prop="createTime" label="评论时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isMobile ? 120 : 150" :fixed="isMobile ? false : 'right'">
            <template #default="scope">
              <el-button type="primary" link size="small" @click="handleEdit(scope.row)">
                编辑
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="admin-pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total || commentList.length"
          :page-count="pageCount"
          :hide-on-single-page="false"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </div>

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑评论' : '新增评论'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户ID"></el-input>
        </el-form-item>
        <el-form-item label="信息类型" prop="infoType">
          <DictSelect v-model="form.infoType" dict-type="activity_item_type" placeholder="请选择信息类型" style="width: 100%" />
        </el-form-item>
        <el-form-item label="信息ID" prop="infoId">
          <el-input v-model="form.infoId" placeholder="请输入信息ID"></el-input>
        </el-form-item>
        <el-form-item label="评论内容" prop="content">
          <el-input type="textarea" v-model="form.content" placeholder="请输入评论内容"></el-input>
        </el-form-item>
        <el-form-item label="点赞数" prop="likeCount">
          <el-input-number v-model="form.likeCount" :min="0"></el-input-number>
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
import { Search } from '@element-plus/icons-vue';
import {
  getComments,
  deleteComment,
  batchDeleteComments,
  createComment,
  updateComment
} from '../../api/adminApi';
import { getDicts } from '../../api/system/dict/data';

export default {
  name: 'CommentManagement',
  components: {
    Search
  },
  setup() {
    const loading = ref(false);
    const commentList = ref([]);
    const selectedComments = ref([]);
    const activity_item_type = ref([]);
    const isMobile = ref(false);

    // 编辑/新增相关状态
    const editDialogVisible = ref(false);
    const isEdit = ref(false);
    const formRef = ref(null);
    const form = reactive({
      id: '',
      userId: '',
      infoType: '',
      infoId: '',
      content: '',
      likeCount: 0
    });

    const rules = {
      userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
      infoType: [{ required: true, message: '请选择信息类型', trigger: 'change' }],
      infoId: [{ required: true, message: '请输入信息ID', trigger: 'blur' }],
      content: [{ required: true, message: '请输入评论内容', trigger: 'blur' }]
    };

    // 筛选条件
    const filters = reactive({
      keyword: '',
      infoType: '',
      userId: '',
      startTime: '',
      endTime: ''
    });

    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    });

    const pageCount = computed(() => {
      const effectiveTotal = Number(pagination.total) || commentList.value.length || 0;
      return Math.max(1, Math.ceil(effectiveTotal / pagination.pageSize));
    });

    const infoTypeLabelMap = {
      lost: '失物',
      find: '招领'
    };

    const getInfoTypeValue = (row) => {
      return String(row?.infoType ?? row?.type ?? row?.itemType ?? '').toLowerCase();
    };

    const getInfoTypeLabel = (row) => {
      const value = getInfoTypeValue(row);
      return infoTypeLabelMap[value] || value || '-';
    };

    const hasDictValue = (dictRef, value) => {
      if (!value || !Array.isArray(dictRef?.value)) return false;
      return dictRef.value.some((item) => String(item.value || item.dictValue).toLowerCase() === String(value).toLowerCase());
    };

    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
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

    // 加载评论列表
    const loadCommentList = async () => {
      try {
        loading.value = true;
        const params = {
          page: pagination.currentPage,
          pageSize: pagination.pageSize,
          keyword: filters.keyword,
          infoType: filters.infoType,
          userId: filters.userId,
          startTime: filters.startTime,
          endTime: filters.endTime
        };

        const res = await getComments(params);
        commentList.value = res.data.list || [];
        const totalFromApi = res?.data?.total ?? res?.data?.totalCount ?? res?.data?.count;
        pagination.total = Number(totalFromApi ?? commentList.value.length ?? 0);
      } catch (error) {
        ElMessage.error('加载评论列表失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    // 搜索
    const handleSearch = () => {
      pagination.currentPage = 1;
      loadCommentList();
    };

    // 重置筛选条件
    const handleReset = () => {
      filters.keyword = '';
      filters.infoType = '';
      filters.userId = '';
      filters.startTime = '';
      filters.endTime = '';
      pagination.currentPage = 1;
      loadCommentList();
    };

    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size;
      pagination.currentPage = 1;
      loadCommentList();
    };

    // 当前页变化
    const handleCurrentChange = (page) => {
      pagination.currentPage = page;
      loadCommentList();
    };

    // 选择评论
    const handleSelectionChange = (val) => {
      selectedComments.value = val;
    };

    // 删除评论
    const handleDelete = async (comment) => {
      try {
        await ElMessageBox.confirm(
          '确定要删除这条评论吗？',
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );

        await deleteComment(comment.id);
        ElMessage.success('评论删除成功');
        loadCommentList();
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败：' + (error.message || '未知错误'));
        }
      }
    };

    // 批量删除
    const handleBatchDelete = async () => {
      if (selectedComments.value.length === 0) {
        ElMessage.warning('请选择要删除的评论');
        return;
      }

      try {
        await ElMessageBox.confirm(
          `确定要删除选中的 ${selectedComments.value.length} 条评论吗？`,
          '批量删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );

        const idsToDelete = selectedComments.value.map(comment => comment.id);
        await batchDeleteComments(idsToDelete);

        ElMessage.success('批量删除成功');
        selectedComments.value = [];
        loadCommentList();
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('批量删除失败：' + (error.message || '未知错误'));
        }
      }
    };

    // 新增
    const handleAdd = () => {
      isEdit.value = false;
      form.id = '';
      form.userId = '';
      form.infoType = '';
      form.infoId = '';
      form.content = '';
      form.likeCount = 0;
      editDialogVisible.value = true;
    };

    // 编辑
    const handleEdit = (row) => {
      isEdit.value = true;
      Object.assign(form, row);
      editDialogVisible.value = true;
    };

    // 提交表单
    const handleSubmit = async () => {
      if (!formRef.value) return;
      await formRef.value.validate(async (valid) => {
        if (valid) {
          try {
            if (isEdit.value) {
              await updateComment(form.id, form);
              ElMessage.success('更新成功');
            } else {
              await createComment(form);
              ElMessage.success('创建成功');
            }
            editDialogVisible.value = false;
            loadCommentList();
          } catch (error) {
            ElMessage.error((isEdit.value ? '更新' : '创建') + '失败：' + (error.message || '未知错误'));
          }
        }
      });
    };

    // 组件挂载时加载数据
    onMounted(() => {
      updateViewportState();
      window.addEventListener('resize', updateViewportState);
      loadCommentList();
      getDicts('activity_item_type').then(res => {
        activity_item_type.value = res.data;
      });
    });

    onBeforeUnmount(() => {
      window.removeEventListener('resize', updateViewportState);
    });

    return {
      loading,
      commentList,
      selectedComments,
      activity_item_type,
      filters,
      pagination,
      pageCount,
      getInfoTypeValue,
      getInfoTypeLabel,
      hasDictValue,
      formatDate,
      formatShortId,
      handleCopyId,
      loadCommentList,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      handleDelete,
      handleBatchDelete,
      editDialogVisible,
      isEdit,
      form,
      rules,
      formRef,
      handleAdd,
      handleEdit,
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

.comment-content {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal !important;
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
