<template>
  <div class="admin-page-container">
    <!-- 筛选和搜索区域 -->
    <div class="admin-search-card">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-input
            v-model="searchQuery"
            placeholder="搜索学号/姓名"
            prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          ></el-input>
        </el-col>
        <el-col :span="16" class="btn-group">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 白名单列表 -->
    <div class="admin-content-card admin-content-card--fixed-pagination">
      <div class="admin-table-header">
        <span class="admin-table-title">白名单管理</span>
        <div class="admin-header-actions">
          <el-button type="primary" plain @click="handleAdd">
            <el-icon><Plus /></el-icon>添加
          </el-button>
          <el-button type="success" @click="dialogVisible = true">
            <el-icon><Upload /></el-icon>导入白名单
          </el-button>
          <el-button v-if="!isMobile" type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
            <el-icon><Delete /></el-icon>批量删除
          </el-button>
        </div>
      </div>

      <div class="admin-table-wrapper">
        <el-table
          v-loading="loading"
          :data="whitelist"
          :fit="!isMobile"
          :table-layout="isMobile ? 'auto' : 'fixed'"
          class="admin-table"
          style="width: 100%;"
          @selection-change="handleSelectionChange"
        >
          <el-table-column v-if="!isMobile" type="selection" width="55" />
          <el-table-column prop="studentId" label="学号" width="180" sortable />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="college" label="学院" width="180" />
          <el-table-column v-if="!isMobile" prop="createTime" label="导入时间" sortable width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" prop="updateTime" label="更新时间" sortable width="180">
            <template #default="scope">
              {{ formatDate(scope.row.updateTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isMobile ? 120 : 150" :fixed="isMobile ? false : 'right'">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleEdit(scope.row)">
                编辑
              </el-button>
              <el-button link type="danger" size="small" @click="handleDelete(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="admin-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total || whitelist.length"
          :page-count="pageCount"
          :hide-on-single-page="false"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="导入白名单"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="upload-container">
        <el-upload
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          :on-exceed="handleExceed"
          ref="uploadRef"
          accept=".xlsx, .xls"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              请上传 .xlsx 或 .xls 格式的文件，
              <el-link type="primary" @click="downloadTemplate">下载模板</el-link>
            </div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpload" :loading="uploading">
            确定导入
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      :title="isEdit ? '编辑白名单' : '添加白名单'"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="80px" v-focus-next="submitAdd">
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="addForm.studentId" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学院" prop="college">
          <el-input v-model="addForm.college" placeholder="请输入学院" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdd">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onBeforeUnmount } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Upload, Delete, UploadFilled, Plus } from '@element-plus/icons-vue';
import {
  getWhitelist,
  importWhitelist,
  deleteWhitelistItem,
  batchDeleteWhitelist,
  addWhitelistItem,
  updateWhitelistItem,
  downloadWhitelistTemplate
} from '../../api/adminApi';
import dayjs from 'dayjs';

const loading = ref(false);
const whitelist = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchQuery = ref('');
const selectedIds = ref([]);
const dialogVisible = ref(false);
const uploadRef = ref(null);
const selectedFile = ref(null);
const uploading = ref(false);
const isMobile = ref(false);

const pageCount = computed(() => {
  const effectiveTotal = Number(total.value) || whitelist.value.length || 0;
  return Math.max(1, Math.ceil(effectiveTotal / pageSize.value));
});

// 手动添加/编辑相关
const addDialogVisible = ref(false);
const isEdit = ref(false);
const currentId = ref(null);
const addFormRef = ref(null);
const addForm = reactive({
  studentId: '',
  name: '',
  college: ''
});
const addRules = {
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
};

// 获取列表数据
const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value
    };
    const res = await getWhitelist(params);
    if (res.code === 0 || res.code === 200) {
      whitelist.value = res.data.list || [];
      const totalFromApi = res?.data?.total ?? res?.data?.totalCount ?? res?.data?.count;
      total.value = Number(totalFromApi ?? whitelist.value.length ?? 0);
    }
  } catch (error) {
    console.error('Failed to fetch whitelist:', error);
  } finally {
    loading.value = false;
  }
};

const updateViewportState = () => {
  isMobile.value = window.innerWidth <= 768;
};

onMounted(() => {
  updateViewportState();
  window.addEventListener('resize', updateViewportState);
  fetchData();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportState);
});

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchData();
};

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchData();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchData();
};

// 多选
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id);
};

// 格式化时间
const formatDate = (date) => {
  if (!date) return '-';
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss');
};

// 删除单个
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除该白名单用户“${row.name} (${row.studentId})”吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteWhitelistItem(row.id);
      ElMessage.success('删除成功');
      fetchData();
    } catch (error) {
      console.error('Delete failed:', error);
    }
  }).catch(() => {});
};

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedIds.value.length} 个白名单用户吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await batchDeleteWhitelist(selectedIds.value);
      ElMessage.success('批量删除成功');
      selectedIds.value = [];
      fetchData();
    } catch (error) {
      console.error('Batch delete failed:', error);
    }
  }).catch(() => {});
};

// 文件处理
const handleFileChange = (file) => {
  selectedFile.value = file.raw;
};

const handleExceed = (files) => {
  uploadRef.value.clearFiles();
  const file = files[0];
  uploadRef.value.handleStart(file);
};

const downloadTemplate = async () => {
  try {
    ElMessage.info('正在下载模板...');
    const res = await downloadWhitelistTemplate();
    // 检查响应是否为 Blob
    if (res instanceof Blob) {
      const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', '白名单导入模板.xlsx');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      ElMessage.success('模板下载成功');
    } else {
       // 如果 axios 拦截器处理了 response，可能直接返回了 data
       // 需要确认 axios 封装。通常 responseType: 'blob' 会返回 blob
       // 如果拦截器判断 code !== 200 抛出错误，则会进入 catch
       // 这里假设 res 就是 blob
       const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
       const url = window.URL.createObjectURL(blob);
       const link = document.createElement('a');
       link.href = url;
       link.setAttribute('download', '白名单导入模板.xlsx');
       document.body.appendChild(link);
       link.click();
       document.body.removeChild(link);
       window.URL.revokeObjectURL(url);
       ElMessage.success('模板下载成功');
    }
  } catch (error) {
    console.error('Download template failed:', error);
    ElMessage.error('模板下载失败');
  }
};

const submitUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件');
    return;
  }

  const formData = new FormData();
  formData.append('file', selectedFile.value);

  uploading.value = true;
  try {
    await importWhitelist(formData);
    ElMessage.success('导入成功');
    dialogVisible.value = false;
    uploadRef.value.clearFiles();
    selectedFile.value = null;
    fetchData();
  } catch (error) {
    console.error('Import failed:', error);
    ElMessage.error('导入失败，请检查文件格式');
  } finally {
    uploading.value = false;
  }
};

// 手动添加逻辑
const handleAdd = () => {
  isEdit.value = false;
  currentId.value = null;
  addForm.studentId = '';
  addForm.name = '';
  addForm.college = '';
  addDialogVisible.value = true;
};

// 编辑逻辑
const handleEdit = (row) => {
  isEdit.value = true;
  currentId.value = row.id;
  addForm.studentId = row.studentId;
  addForm.name = row.name;
  addForm.college = row.college || '';
  addDialogVisible.value = true;
};

const submitAdd = async () => {
  if (!addFormRef.value) return;

  try {
    await addFormRef.value.validate();
    if (isEdit.value) {
      await updateWhitelistItem(currentId.value, { ...addForm });
      ElMessage.success('更新成功');
    } else {
      await addWhitelistItem({ ...addForm });
      ElMessage.success('添加成功');
    }
    addDialogVisible.value = false;
    fetchData();
  } catch (error) {
    if (error !== false) {
      ElMessage.error((isEdit.value ? '更新' : '添加') + '失败：' + (error.message || '未知错误'));
    }
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

.upload-container {
  padding: 20px 0;
  text-align: center;
}

.el-upload__tip {
  margin-top: 10px;
  color: #909399;
}

.admin-content-card--fixed-pagination {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  grid-template-columns: minmax(0, 1fr);
  width: 100%;
}

.admin-content-card--fixed-pagination .admin-table-wrapper {
  overflow: auto;
  min-height: 0;
  width: 100%;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .admin-content-card--fixed-pagination .admin-table-wrapper {
    overflow: auto;
  }
}
</style>


