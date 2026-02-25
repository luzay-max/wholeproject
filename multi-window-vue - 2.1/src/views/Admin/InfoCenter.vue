<template>
  <div class="admin-page-container">
    <el-tabs v-model="activeTab" class="main-tabs" @tab-click="handleTabClick">
      <!-- 待审核信息管理 -->
      <el-tab-pane label="待审核" name="audit">
        <template #label>
          <span class="custom-tab-label">
            <el-icon><Bell /></el-icon>
            <span>待审核</span>
            <el-badge :value="pendingCount" class="item-badge" v-if="pendingCount > 0" />
          </span>
        </template>
        
        <div class="tab-content">
          <!-- 审核筛选 -->
          <div class="admin-search-card filter-bar">
            <DictSelect v-model="auditFilter.type" dict-type="info_type" placeholder="信息类型" clearable style="width: 150px" />
            <el-input 
              v-model="auditFilter.keyword" 
              placeholder="搜索物品名称/发布者"
              style="width: 250px"
              clearable 
              @keyup.enter="fetchAuditData"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="fetchAuditData">搜索</el-button>
            <div class="flex-spacer"></div>
            <el-button v-if="!isMobile" type="success" :disabled="selectedAuditIds.length === 0" @click="handleBatchApprove">
              <el-icon><Check /></el-icon> 批量通过
            </el-button>
          </div>

          <!-- 审核表格 -->
          <div class="admin-content-card admin-content-card--fixed-pagination">
            <div class="admin-table-wrapper">
              <el-table 
                v-loading="auditLoading" 
                :data="auditList" :fit="!isMobile" :table-layout="isMobile ? 'auto' : 'fixed'" 
                class="admin-table"
                style="width: 100%;" 
                @selection-change="handleAuditSelectionChange"
              >
              <el-table-column v-if="!isMobile" type="selection" width="55" />
              <el-table-column prop="type" label="类型" width="100">
                <template #default="scope">
                  <DictTag
                    v-if="hasDictValue(activity_item_type, getAuditTypeValue(scope.row))"
                    :options="activity_item_type"
                    :value="getAuditTypeValue(scope.row)"
                  />
                  <span v-else>{{ getAuditTypeLabel(scope.row) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="物品名称" width="180" show-overflow-tooltip />
              <el-table-column label="发布者" width="120" show-overflow-tooltip>
                <template #default="scope">
                  {{ getPublisherDisplay(scope.row) }}
                </template>
              </el-table-column>
              <el-table-column v-if="!isMobile" prop="publishTime" label="发布时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.publishTime) }}
                </template>
              </el-table-column>
              <el-table-column v-if="!isMobile" prop="description" label="描述" show-overflow-tooltip />
              <el-table-column label="操作" :width="isMobile ? 180 : 200" :fixed="isMobile ? false : 'right'">
                <template #default="scope">
                  <el-button link type="primary" @click="handleApprove(scope.row)">通过</el-button>
                  <el-button link type="danger" @click="openRejectDialog(scope.row)">拒绝</el-button>
                  <el-button link @click="handleViewDetail(scope.row)">详情</el-button>
                </template>
              </el-table-column>
              </el-table>
            </div>

            <div class="admin-pagination">
              <el-pagination
                v-model:current-page="auditPagination.page"
                v-model:page-size="auditPagination.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="auditDisplayTotal"
                :page-count="auditPageCount"
                layout="total, sizes, prev, pager, next, jumper"
                background
                @current-change="fetchAuditData"
                @size-change="fetchAuditData"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 信息管理页面 -->
      <el-tab-pane label="信息管理" name="manage">
        <template #label>
          <span class="custom-tab-label">
            <el-icon><List /></el-icon>
            <span>信息管理</span>
          </span>
        </template>

        <div class="tab-content">
          <!-- 二级分类切换 -->
          <div class="sub-tabs-container">
            <el-radio-group v-model="manageType" @change="fetchManageData" size="large">
              <el-radio-button label="lost">失物信息</el-radio-button>
              <el-radio-button label="find">招领信息</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 管理搜索 -->
          <div class="admin-search-card filter-bar">
            <DictSelect v-model="manageFilter.status" :options="info_status" placeholder="状态" clearable style="width: 150px" />
            <el-input 
              v-model="manageFilter.keyword"  
              placeholder="搜索物品名称" 
              style="width: 250px"
              clearable
              @keyup.enter="fetchManageData"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="fetchManageData">搜索</el-button>
            <el-button type="success" @click="handleAdd">添加</el-button>
            <div class="flex-spacer"></div>
            <el-button v-if="!isMobile" type="danger" :disabled="selectedManageIds.length === 0" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon> 批量删除
            </el-button>
          </div>

          <!-- 管理表格 -->
          <div class="admin-content-card admin-content-card--fixed-pagination">
            <div class="admin-table-wrapper">
              <el-table 
                v-loading="manageLoading" 
                :data="manageList" :fit="!isMobile" :table-layout="isMobile ? 'auto' : 'fixed'" 
                class="admin-table"
                style="width: 100%;" 
                @selection-change="handleManageSelectionChange"
              >
              <el-table-column v-if="!isMobile" type="selection" width="55" />
              <el-table-column prop="name" label="物品名称" width="180" show-overflow-tooltip />
              <el-table-column prop="type" label="分类" width="120">
                 <template #default="scope">
                   <DictTag :options="info_type" :value="scope.row.type" />
                 </template>
              </el-table-column>
              <el-table-column prop="location" label="地点" width="150" show-overflow-tooltip />
              <el-table-column v-if="!isMobile" prop="publishTime" label="发布时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.publishTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="scope">
                  <DictTag :options="info_status" :value="scope.row.status" />
                </template>
              </el-table-column>
              <el-table-column label="操作" :width="isMobile ? 160 : 180" :fixed="isMobile ? false : 'right'">
                <template #default="scope">
                  <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
                  <el-button link type="primary" @click="handleViewDetail(scope.row)">详情</el-button>
                  <el-popconfirm title="确认删除吗？" @confirm="handleDelete(scope.row)">
                    <template #reference>
                      <el-button link type="danger">删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
              </el-table>
            </div>

            <div class="admin-pagination">
              <el-pagination
                v-model:current-page="managePagination.page"
                v-model:page-size="managePagination.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="manageDisplayTotal"
                :page-count="managePageCount"
                layout="total, sizes, prev, pager, next, jumper"
                background
                @current-change="fetchManageData"
                @size-change="fetchManageData"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑信息' : '添加信息'" width="600px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="物品名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入物品名称"></el-input>
        </el-form-item>
        <el-form-item label="物品分类" prop="type">
          <DictSelect v-model="editForm.type" dict-type="info_type" placeholder="请选择分类" style="width: 100%; min-width: 150px" />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="editForm.location" placeholder="请输入地点"></el-input>
        </el-form-item>
        <el-form-item label="时间" prop="time">
          <el-date-picker
            v-model="editForm.time"
            type="datetime"
            placeholder="选择日期时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>
        <el-form-item label="图片JSON" prop="images">
          <el-input v-model="editForm.images" placeholder='可选，格式: ["url1", "url2"]'></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="editForm.contactName" placeholder="请输入联系人"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="editForm.contactPhone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="editForm.contactEmail" placeholder="请输入联系邮箱"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <DictSelect v-model="editForm.status" dict-type="info_status" placeholder="请选择状态" style="width: 100%; min-width: 150px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝审核" width="400px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="3"
        placeholder="请输入拒绝原因"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="信息详情" width="600px">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="物品名称">{{ currentDetail.name }}</el-descriptions-item>
          <el-descriptions-item label="发布类型">
            <DictTag :options="activity_item_type" :value="manageType" />
          </el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentDetail.contactInfo || currentDetail.contactName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentDetail.contactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ currentDetail.contactEmail || '-' }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ formatDate(currentDetail.publishTime || currentDetail.lostTime || currentDetail.foundTime || currentDetail.time) }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ currentDetail.location }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ currentDetail.description }}</el-descriptions-item>
          <el-descriptions-item label="图片" v-if="currentDetail.images && currentDetail.images.length">
             <div class="image-list">
               <el-image 
                 v-for="(img, idx) in currentDetail.images" 
                 :key="idx" 
                 :src="img" 
                 :preview-src-list="currentDetail.images"
                 style="width: 100px; height: 100px; margin-right: 8px"
                 fit="cover"
               />
             </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed, onBeforeUnmount } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Bell, List, Search, Check, Delete } from '@element-plus/icons-vue';
import { getAuditList, auditApprove, auditReject, batchAuditApprove } from '../../api/auditApi';
import { 
  getLostItems, deleteLostItem, batchDeleteLostItems, createLostItem, updateLostItem,
  getFindItems, deleteFindItem, batchDeleteFindItems, createFindItem, updateFindItem 
} from '../../api/adminApi';
import { getDicts } from '../../api/system/dict/data';
import DictSelect from '../../components/Dict/DictSelect.vue';
import DictTag from '../../components/Dict/DictTag.vue';
import dayjs from 'dayjs';

const route = useRoute();

// 状态管理
const activeTab = ref('audit');
const manageType = ref('lost');
const pendingCount = ref(0);

// 审核流数据
const auditLoading = ref(false);
const auditList = ref([]);
const auditPagination = reactive({ page: 1, size: 10, total: 0 });
const auditFilter = reactive({ type: '', keyword: '' });
const selectedAuditIds = ref([]);
const rejectDialogVisible = ref(false);
const rejectReason = ref('');
const currentAuditItem = ref(null);

// 信息管理数据
const manageLoading = ref(false);
const manageList = ref([]);
const managePagination = reactive({ page: 1, size: 10, total: 0 });
const manageFilter = reactive({ status: '', keyword: '' });
const selectedManageIds = ref([]);
const info_type = ref([]);
const info_status = ref([]);
const activity_item_type = ref([]);
const isMobile = ref(false);

const auditTypeLabelMap = {
  lost: '失物',
  find: '招领'
};

const getAuditTypeValue = (row) => {
  return String(row?.type ?? row?.itemType ?? '').toLowerCase();
};

const getAuditTypeLabel = (row) => {
  const value = getAuditTypeValue(row);
  return auditTypeLabelMap[value] || value || '-';
};

const hasDictValue = (options, value) => {
  if (!value) return false;
  const dictOptions = Array.isArray(options) ? options : (Array.isArray(options?.value) ? options.value : []);
  if (!Array.isArray(dictOptions)) return false;
  const currentValue = String(value).toLowerCase();
  return dictOptions.some((item) => String(item.value || item.dictValue).toLowerCase() === currentValue);
};

const getPublisherDisplay = (row) => {
  return row?.publisherName
    || row?.accountName
    || row?.username
    || row?.publisher
    || row?.contactName
    || row?.contactInfo
    || '-';
};

// 计算属性 - 分页相关
const auditDisplayTotal = computed(() => {
  return Number(auditPagination.total) || auditList.value.length || 0;
});

const auditPageCount = computed(() => {
  const total = auditDisplayTotal.value;
  const pageSize = Number(auditPagination.size) || 10;
  return Math.ceil(total / pageSize) || 1;
});

const manageDisplayTotal = computed(() => {
  return Number(managePagination.total) || manageList.value.length || 0;
});

const managePageCount = computed(() => {
  const total = manageDisplayTotal.value;
  const pageSize = Number(managePagination.size) || 10;
  return Math.ceil(total / pageSize) || 1;
});

// 新增/编辑
const editDialogVisible = ref(false);
const isEdit = ref(false);
const editFormRef = ref(null);
const editForm = reactive({
  id: '',
  name: '',
  type: '',
  location: '',
  description: '',
  images: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  time: '', // lostTime or foundTime
  status: 'APPROVED'
});
const editRules = {
  name: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择分类', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
};

// 详情
const detailVisible = ref(false);
const currentDetail = ref(null);

const updateViewportState = () => {
  isMobile.value = window.innerWidth <= 768;
};

// 生命周期钩子
onMounted(() => {
  updateViewportState();
  window.addEventListener('resize', updateViewportState);
  // 初始化根据路由参数设置当前tab
  if (route.query.tab) {
    activeTab.value = route.query.tab;
  }
  if (route.query.type) {
    manageType.value = route.query.type;
  }

  // 初始化根据当前tab加载对应数据
  if (activeTab.value === 'manage') {
    fetchManageData();
  } else {
    fetchAuditData();
  }

  getDicts('info_type').then(response => {
    info_type.value = response.data;
  });
  getDicts('info_status').then(response => {
    info_status.value = response.data;
  });
  getDicts('activity_item_type').then(response => {
    activity_item_type.value = response.data;
  });
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportState);
});

// 监听路由参数变化
watch(() => route.query, (newQuery) => {
  if (newQuery.tab) {
    activeTab.value = newQuery.tab;
  }
  if (newQuery.type) {
    manageType.value = newQuery.type;
  }
  
  if (activeTab.value === 'manage') {
    fetchManageData();
  } else {
    fetchAuditData();
  }
});

// ---------------- 审核流程相关 ----------------
const fetchAuditData = async () => {
  auditLoading.value = true;
  try {
    const params = {
      page: auditPagination.page,
      pageSize: auditPagination.size,
      type: auditFilter.type,
      keyword: auditFilter.keyword,
      status: 'PENDING'
    };
    const res = await getAuditList(params);
    if (res && (res.code === 0 || res.code === 200)) {
      const data = res.data || {};
      auditList.value = data.list || [];
      auditPagination.total = data.total || 0;
      pendingCount.value = data.total || 0; // 同步待审核数量
    }
  } catch (error) {
    console.error('Fetch audit failed', error);
  } finally {
    auditLoading.value = false;
  }
};

const handleAuditSelectionChange = (selection) => {
  selectedAuditIds.value = selection.map(item => item.id);
};

const handleApprove = async (row) => {
  try {
    await auditApprove({ id: row.id, type: row.type }); // 假设API需要type
    ElMessage.success('审核通过');
    fetchAuditData();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleBatchApprove = async () => {
  try {
    // 批量通过统一使用 { ids: [] } 请求体
    await batchAuditApprove({ ids: selectedAuditIds.value });
    ElMessage.success('批量通过成功');
    fetchAuditData();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const openRejectDialog = (row) => {
  currentAuditItem.value = row;
  rejectReason.value = '';
  rejectDialogVisible.value = true;
};

const confirmReject = async () => {
  if (!rejectReason.value) return ElMessage.warning('请输入拒绝原因');
  try {
    await auditReject({ 
      id: currentAuditItem.value.id, 
      type: currentAuditItem.value.type, // 假设需要type
      reason: rejectReason.value 
    });
    ElMessage.success('已拒绝');
    rejectDialogVisible.value = false;
    fetchAuditData();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

// ---------------- 信息管理相关 ----------------
const fetchManageData = async () => {
  manageLoading.value = true;
  try {
    const params = {
      page: managePagination.page,
      pageSize: managePagination.size,
      status: manageFilter.status,
      keyword: manageFilter.keyword
    };
    
    let res;
    if (manageType.value === 'lost') {
      res = await getLostItems(params);
    } else {
      res = await getFindItems(params);
    }

    console.log('Manage Response:', res);

    if (res && (res.code === 0 || res.code === 200)) {
      const data = res.data || {};
      manageList.value = data.list || [];
      managePagination.total = data.total || 0;
    }
  } catch (error) {
    console.error('Fetch manage failed', error);
  } finally {
    manageLoading.value = false;
  }
};

const handleManageSelectionChange = (selection) => {
  selectedManageIds.value = selection.map(item => item.id);
};

const handleDelete = async (row) => {
  try {
    if (manageType.value === 'lost') {
      await deleteLostItem(row.id);
    } else {
      await deleteFindItem(row.id);
    }
    ElMessage.success('删除成功');
    fetchManageData();
  } catch (error) {
    ElMessage.error('删除失败');
  }
};

const handleBatchDelete = async () => {
  try {
    if (manageType.value === 'lost') {
      await batchDeleteLostItems(selectedManageIds.value);
    } else {
      await batchDeleteFindItems(selectedManageIds.value);
    }
    ElMessage.success('批量删除成功');
    fetchManageData();
  } catch (error) {
    ElMessage.error('批量删除失败');
  }
};

const handleAdd = () => {
  isEdit.value = false;
  resetEditForm();
  editDialogVisible.value = true;
};

const handleEdit = (row) => {
  isEdit.value = true;
  resetEditForm();
  Object.assign(editForm, {
    id: row.id,
    name: row.name,
    type: row.type,
    location: row.location,
    description: row.description,
    images: typeof row.images === 'string' ? row.images : JSON.stringify(row.images || []),
    contactName: row.contactInfo || row.contactName, // 兼容不同字段名
    contactPhone: row.contactPhone,
    contactEmail: row.contactEmail,
    time: row.lostTime || row.foundTime || row.publishTime,
    status: row.status
  });
  editDialogVisible.value = true;
};

const resetEditForm = () => {
  Object.assign(editForm, {
    id: '',
    name: '',
    type: '',
    location: '',
    description: '',
    images: '',
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    time: '',
    status: 'APPROVED'
  });
};

const handleSave = async () => {
  if (!editFormRef.value) return;
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const data = {
          name: editForm.name,
          type: editForm.type,
          location: editForm.location,
          description: editForm.description,
          images: editForm.images,
          contactName: editForm.contactName,
          contactInfo: editForm.contactName,
          contactPhone: editForm.contactPhone,
          contactEmail: editForm.contactEmail,
          status: editForm.status
        };

        if (manageType.value === 'lost') {
           data.lostTime = editForm.time;
           if (isEdit.value) {
             await updateLostItem(editForm.id, data);
           } else {
             await createLostItem(data);
           }
        } else {
           data.foundTime = editForm.time;
           if (isEdit.value) {
             await updateFindItem(editForm.id, data);
           } else {
             await createFindItem(data);
           }
        }
        
        ElMessage.success(isEdit.value ? '保存成功' : '添加成功');
        editDialogVisible.value = false;
        fetchManageData();
      } catch (error) {
        console.error(error);
        ElMessage.error(isEdit.value ? '保存失败' : '添加失败');
      }
    }
  });
};

// ---------------- 其他辅助函数 ----------------
const handleTabClick = (tab) => {
  if (tab.props.name === 'manage') {
    fetchManageData();
  } else {
    fetchAuditData();
  }
};

const normalizeImageList = (images) => {
  if (!images) return [];

  const parseToArray = (value) => {
    if (Array.isArray(value)) return value;
    if (typeof value !== 'string') return [];

    const text = value.trim();
    if (!text) return [];

    try {
      const parsed = JSON.parse(text);
      if (Array.isArray(parsed)) return parsed;
      if (typeof parsed === 'string') return parseToArray(parsed);
    } catch (_) {}

    if (/^https?:\/\//i.test(text)) return [text];
    if (text.includes(',')) return text.split(',').map((item) => item.trim());

    return [];
  };

  return parseToArray(images)
    .map((url) => String(url || '').trim().replace(/^['"`]+|['"`]+$/g, ''))
    .filter((url) => /^https?:\/\//i.test(url));
};

const handleViewDetail = (row) => {
  const detail = { ...row };
  detail.images = normalizeImageList(detail.images);
  currentDetail.value = detail;
  detailVisible.value = true;
};

const formatDate = (date) => {
  if (!date) return '-';
  const parsed = dayjs(date);
  if (parsed.isValid()) return parsed.format('YYYY-MM-DD HH:mm:ss');

  const asNumber = Number(date);
  if (Number.isFinite(asNumber)) {
    const parsedNum = dayjs(asNumber);
    if (parsedNum.isValid()) return parsedNum.format('YYYY-MM-DD HH:mm:ss');
  }

  return '-';
};
</script>

<style scoped>
/* Tabs 主布局 */
:deep(.el-tabs) {
  display: flex;
  flex-direction: column;
  height: 100% !important;
  overflow: hidden;
}

:deep(.el-tabs__header) {
  margin-bottom: 16px;
  background-color: #fff;
  padding: 10px 20px 0;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 0;
}


:deep(.el-tabs__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 0 !important;
  overflow: hidden !important;
  height: 100% !important;
}

:deep(.el-tab-pane) {
  height: 100% !important;
  display: flex;
  flex-direction: column;
  min-height: 0;
}


.tab-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

.filter-bar {
  flex-shrink: 0;
}

.custom-tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.item-badge {
  margin-left: 4px;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  flex-shrink: 0; 
}

.flex-spacer {
  flex: 1;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
}


.admin-content-card--fixed-pagination {
  display: grid;
  grid-template-rows: 1fr auto; 
  overflow: hidden;
  flex: 1; 
  min-height: 0;
}

.admin-content-card--fixed-pagination .admin-table-wrapper {
  overflow: auto;
  min-height: 0;
  margin-bottom: 0;
}

.admin-content-card--fixed-pagination .admin-pagination {
  padding: 10px 0;
  border-top: 1px solid #f0f2f5;
  background: #fff;
  z-index: 10;
  display: flex;
  justify-content: flex-end;
  
  position: sticky;
  bottom: 0;
}


.admin-content-card--fixed-pagination :deep(.el-pagination__jump),
.admin-content-card--fixed-pagination :deep(.el-pagination__sizes) {
  display: inline-flex !important;
  align-items: center;
  visibility: visible !important;
}

@media (max-width: 992px) {
  :deep(.el-tabs) {
    height: auto !important;
    overflow: visible;
  }

  :deep(.el-tabs__content) {
    height: auto !important;
    overflow: visible !important;
  }

  :deep(.el-tab-pane) {
    height: auto !important;
  }

  .tab-content {
    height: auto;
    overflow: visible;
    min-height: auto;
  }

  .admin-content-card--fixed-pagination {
    display: flex;
    flex-direction: column;
    overflow: visible;
  }

  .admin-content-card--fixed-pagination .admin-table-wrapper {
    overflow: auto;
  }

  .admin-content-card--fixed-pagination .admin-pagination {
    position: static;
  }
}

.status-tag {
  text-transform: capitalize;
}
</style>





