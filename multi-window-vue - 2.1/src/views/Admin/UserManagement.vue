﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="admin-page-container">
    <!-- 筛选和搜索区域 -->
    <div class="admin-search-card">
      <el-row :gutter="16">
        <el-col :span="4">
          <el-input
            v-model="filters.id"
            placeholder="搜索用户ID"
            prefix-icon="Search"
            clearable
          ></el-input>
        </el-col>
        <el-col :span="4">
          <el-input
            v-model="filters.username"
            placeholder="搜索用户名"
            prefix-icon="Search"
            clearable
          ></el-input>
        </el-col>
        <el-col :span="4">
          <DictSelect
            v-model="filters.role"
            dict-type="sys_user_role"
            placeholder="选择角色"
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
            style="width: 100%"
          ></el-date-picker>
        </el-col>
        <el-col :span="4" class="btn-group">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 用户列表 -->
    <div class="admin-content-card admin-content-card--fixed-pagination">
      <div class="admin-table-header">
        <span class="admin-table-title">用户列表</span>
        <div class="admin-header-actions">
          <el-button type="success" @click="handleAdd">新增用户</el-button>
          <el-button
            v-if="!isMobile"
            type="danger"
            @click="handleBatchDelete"
            :disabled="selectedUsers.length === 0"
          >
            批量删除
          </el-button>
        </div>
      </div>

      <div class="admin-table-wrapper">
        <el-table
          v-loading="loading"
          :data="userList"
          class="admin-table"
          :fit="!isMobile"
          :table-layout="isMobile ? 'auto' : 'fixed'"
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
          <el-table-column prop="username" label="用户名" width="120" show-overflow-tooltip></el-table-column>
          <el-table-column v-if="!isMobile" prop="name" label="真实姓名" width="120" show-overflow-tooltip></el-table-column>
          <el-table-column prop="role" label="角色" width="100">
            <template #default="scope">
              <DictTag :options="userRoleOptions" :value="scope.row.role" />
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="isMobile ? '状态' : '状态'" :width="isMobile ? 84 : 100">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                :active-value="0"
                :inactive-value="1"
                :active-text="isMobile ? '' : '正常'"
                :inactive-text="isMobile ? '' : '封禁'"
                :inline-prompt="!isMobile"
                style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
                :before-change="() => handleStatusBeforeChange(scope.row)"
                :disabled="scope.row.role === 'ADMIN'"
              />
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" prop="phone" label="手机号" width="150" show-overflow-tooltip></el-table-column>
          <el-table-column v-if="!isMobile" prop="email" label="邮箱" width="200" show-overflow-tooltip></el-table-column>
          <el-table-column v-if="!isMobile" prop="createTime" label="注册时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isMobile ? 190 : 350" :fixed="isMobile ? false : 'right'">
            <template #default="scope">
              <div class="action-group" :class="{ mobile: isMobile }">
                <el-button type="primary" size="small" :link="isMobile" @click="handleEdit(scope.row)">
                  编辑
                </el-button>
                <el-button type="warning" size="small" :link="isMobile" @click="handleChangeRole(scope.row)">
                  角色
                </el-button>
                <el-button type="info" size="small" :link="isMobile" @click="handleResetPassword(scope.row)">
                  重置密码
                </el-button>
                <el-button type="danger" size="small" :link="isMobile" @click="handleDelete(scope.row)">
                  删除
                </el-button>
              </div>
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
          :total="pagination.total || userList.length"
          :page-count="pageCount"
          :hide-on-single-page="false"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </div>

    <!-- 用户对话框(新增/编辑) -->
    <el-dialog v-model="userDialogVisible" :title="isEditMode ? '编辑用户' : '新增用户'" width="600px">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="isEditMode"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="name">
              <el-input v-model="userForm.name" placeholder="请输入真实姓名"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="userForm.password"
                placeholder="留空则不修改(默认123456)"
                type="password"
                show-password
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <DictSelect v-model="userForm.role" dict-type="sys_user_role" placeholder="请选择角色" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentId">
              <el-input v-model="userForm.studentId" placeholder="请输入学号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学院" prop="college">
              <el-input v-model="userForm.college" placeholder="请输入学院"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio v-for="dict in userStatusOptions" :key="dict.value" :label="Number(dict.value)">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmUser">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="修改角色" width="400px">
      <el-form :model="roleForm" :rules="roleRules" ref="roleFormRef" v-focus-next="handleConfirmRole">
        <el-form-item label="用户" prop="username">
          <el-input v-model="roleForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <DictSelect v-model="roleForm.role" dict-type="sys_user_role" placeholder="请选择角色" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmRole">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, onBeforeUnmount } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import DictSelect from '../../components/Dict/DictSelect.vue';
import DictTag from '../../components/Dict/DictTag.vue';
import { getDicts } from '../../api/system/dict/data';
import {
  getUsers,
  updateUser,
  changeUserRole,
  deleteUser,
  batchDeleteUsers,
  updateUserStatus,
  createUser,
  resetUserPassword
} from '../../api/adminApi';
import { nameRegex } from '../../utils/validators';

export default {
  name: 'UserManagement',
  components: {
    Search,
    DictSelect,
    DictTag
  },
  setup() {
    const loading = ref(false);
    const userList = ref([]);
    const selectedUsers = ref([]);
    const userDialogVisible = ref(false);
    const roleDialogVisible = ref(false);
    const userFormRef = ref(null);
    const roleFormRef = ref(null);
    const dateRange = ref([]);
    const isEditMode = ref(false);
    const isMobile = ref(false);
    
    // 字典选项
    const userRoleOptions = ref([]);
    const userStatusOptions = ref([]);

    // 筛选条件
    const filters = reactive({
      id: '',
      username: '',
      role: '',
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
      const total = Number(pagination.total) || userList.value.length || 0;
      return Math.max(1, Math.ceil(total / pagination.pageSize));
    });

    const updateViewportState = () => {
      isMobile.value = window.innerWidth <= 768;
    };

    // 用户表单 (新增/编辑)
    const userForm = reactive({
      id: '',
      username: '',
      password: '',
      name: '',
      role: 'STUDENT',
      studentId: '',
      college: '',
      phone: '',
      email: '',
      status: 0
    });

    // 角色表单
    const roleForm = reactive({
      id: '',
      username: '',
      role: ''
    });

    // 表单验证规则
    const userRules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      name: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { pattern: nameRegex, message: '请输入正确的姓名（2-20位，支持中英文、点、空格）', trigger: 'blur' }
      ],
      role: [{ required: true, message: '请选择角色', trigger: 'change' }],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ]
    };

    const roleRules = {
      role: [{ required: true, message: '请选择角色', trigger: 'change' }]
    };

    const handleResetPassword = (row) => {
      ElMessageBox.confirm(
        `确定要重置用户“${row.username}”的密码吗？重置后密码将变为123456`,
        '重置密码确认',
        {
          confirmButtonText: '确定重置',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await resetUserPassword(row.id);
          ElMessage.success('密码重置成功，新密码：123456');
        } catch (error) {
          ElMessage.error('密码重置失败：' + (error.message || '未知错误'));
        }
      }).catch(() => {});
    };

    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
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

    // 加载用户列表
    const loadUserList = async () => {
      try {
        loading.value = true;

        const params = {
          page: pagination.currentPage,
          pageSize: pagination.pageSize,
          id: filters.id,
          username: filters.username,
          role: filters.role,
          startTime: filters.startTime,
          endTime: filters.endTime
        };

        const res = await getUsers(params);
        userList.value = res.data.list || [];
        const totalFromApi = res?.data?.total ?? res?.data?.totalCount ?? res?.data?.count;
        pagination.total = Number(totalFromApi ?? userList.value.length ?? 0);
      } catch (error) {
        ElMessage.error('加载用户列表失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    // 搜索
    const handleSearch = () => {
      if (dateRange.value && dateRange.value.length === 2) {
        filters.startTime = dateRange.value[0];
        filters.endTime = dateRange.value[1];
      } else {
        filters.startTime = '';
        filters.endTime = '';
      }

      pagination.currentPage = 1;
      loadUserList();
    };

    // 重置筛选条件
    const handleReset = () => {
      filters.id = '';
      filters.username = '';
      filters.role = '';
      dateRange.value = [];
      filters.startTime = '';
      filters.endTime = '';
      pagination.currentPage = 1;
      loadUserList();
    };

    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size;
      pagination.currentPage = 1;
      loadUserList();
    };

    // 当前页变化
    const handleCurrentChange = (page) => {
      pagination.currentPage = page;
      loadUserList();
    };

    // 选择用户
    const handleSelectionChange = (val) => {
      selectedUsers.value = val;
    };

    // 新增用户
    const handleAdd = () => {
      isEditMode.value = false;
      userForm.id = '';
      userForm.username = '';
      userForm.password = '';
      userForm.name = '';
      userForm.role = 'STUDENT';
      userForm.studentId = '';
      userForm.college = '';
      userForm.phone = '';
      userForm.email = '';
      userForm.status = 0;
      userDialogVisible.value = true;
    };

    // 编辑用户
    const handleEdit = (user) => {
      isEditMode.value = true;
      userForm.id = user.id;
      userForm.username = user.username;
      userForm.password = '';
      userForm.name = user.name;
      userForm.role = user.role;
      userForm.studentId = user.studentId || '';
      userForm.college = user.college || '';
      userForm.phone = user.phone;
      userForm.email = user.email;
      userForm.status = user.status;
      userDialogVisible.value = true;
    };

    // 确认保存用户 (新增/编辑)
    const handleConfirmUser = async () => {
      if (!userFormRef.value) return;

      try {
        await userFormRef.value.validate();

        if (isEditMode.value) {
          await updateUser(userForm.id, userForm);
          ElMessage.success('用户信息更新成功');
        } else {
          await createUser(userForm);
          ElMessage.success('新增用户成功');
        }

        userDialogVisible.value = false;
        loadUserList();
      } catch (error) {
        if (error !== false) {
          ElMessage.error((isEditMode.value ? '更新' : '新增') + '失败：' + (error.message || '未知错误'));
        }
      }
    };

    // 修改角色
    const handleChangeRole = (user) => {
      roleForm.id = user.id;
      roleForm.username = user.username;
      roleForm.role = user.role;
      roleDialogVisible.value = true;
    };

    // 确认修改角色
    const handleConfirmRole = async () => {
      if (!roleFormRef.value) return;

      try {
        await roleFormRef.value.validate();

        await changeUserRole(roleForm.id, roleForm.role);

        ElMessage.success('角色修改成功');
        roleDialogVisible.value = false;
        loadUserList();
      } catch (error) {
        if (error !== false) {
          ElMessage.error('修改失败：' + (error.message || '未知错误'));
        }
      }
    };

    // 修改用户状态(封禁/解封)
    const handleStatusBeforeChange = (user) => {
      if (user.role === 'ADMIN') {
        ElMessage.warning('管理员不可被封禁');
        return Promise.reject(false);
      }

      const isBanned = user.status === 1;
      const actionText = isBanned ? '解封' : '封禁';
      const newStatus = isBanned ? 0 : 1;

      return new Promise((resolve, reject) => {
        ElMessageBox.confirm(
          `确定要${actionText}用户“${user.username}”吗？`,
          `${actionText}确认`,
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: isBanned ? 'success' : 'warning'
          }
        ).then(async () => {
          try {
            await updateUserStatus(user.id, newStatus);
            ElMessage.success(`用户${actionText}成功`);
            resolve(true);
          } catch (error) {
            ElMessage.error(`${actionText}失败：` + (error.message || '未知错误'));
            reject(false);
          }
        }).catch(() => {
          reject(false);
        });
      });
    };

    // 删除用户
    const handleDelete = async (user) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除用户“${user.username}”吗？`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );

        await deleteUser(user.id);

        ElMessage.success('用户删除成功');
        loadUserList();
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败：' + (error.message || '未知错误'));
        }
      }
    };

    // 批量删除
    const handleBatchDelete = async () => {
      if (selectedUsers.value.length === 0) {
        ElMessage.warning('请选择要删除的用户');
        return;
      }

      try {
        await ElMessageBox.confirm(
          `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？`,
          '批量删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );

        const idsToDelete = selectedUsers.value.map(user => user.id);
        await batchDeleteUsers(idsToDelete);

        ElMessage.success('批量删除成功');
        selectedUsers.value = [];
        loadUserList();
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('批量删除失败：' + (error.message || '未知错误'));
        }
      }
    };

    // 组件挂载时加载数据
    onMounted(() => {
      updateViewportState();
      window.addEventListener('resize', updateViewportState);
      loadUserList();
      getDicts('sys_user_role').then(response => {
        userRoleOptions.value = response.data;
      });
      getDicts('sys_user_status').then(response => {
        userStatusOptions.value = response.data;
      });
    });

    onBeforeUnmount(() => {
      window.removeEventListener('resize', updateViewportState);
    });

    return {
      loading,
      userList,
      selectedUsers,
      userDialogVisible,
      roleDialogVisible,
      userFormRef,
      roleFormRef,
      filters,
      dateRange,
      pagination,
      pageCount,
      userForm,
      roleForm,
      userRules,
      roleRules,
      formatDate,
      formatShortId,
      handleCopyId,
      loadUserList,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      handleAdd,
      handleEdit,
      handleConfirmUser,
      handleChangeRole,
      handleConfirmRole,
      handleStatusBeforeChange,
      handleDelete,
      handleResetPassword,
      handleBatchDelete,
      isEditMode,
      isMobile,
      userRoleOptions,
      userStatusOptions
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.action-group {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

@media (max-width: 768px) {
  .action-group.mobile {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
}
</style>
