<template>
  <div class="admin-page-container">
    <!-- 生成光荣榜对话框 -->
    <el-dialog
      v-model="generateDialogVisible"
      title="生成光荣榜"
      width="500px"
      destroy-on-close
    >
      <el-form label-width="100px" v-focus-next="handleGenerate">
        <el-form-item label="周期类型">
          <el-radio-group v-model="generateForm.type">
            <el-radio :label="0">本周</el-radio>
            <el-radio :label="1">上周</el-radio>
            <el-radio :label="2">自定义日期</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="generateForm.type === 2" label="日期范围">
          <el-date-picker
            v-model="generateForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="generateDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="generating" @click="handleGenerate">
            确定生成
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 周期编辑对话框 -->
    <el-dialog
      v-model="periodEditDialogVisible"
      title="编辑光荣榜周期"
      width="500px"
    >
      <el-form :model="periodForm" label-width="100px">
        <el-form-item label="开始日期">
          <el-date-picker v-model="periodForm.periodStart" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="periodForm.periodEnd" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <DictSelect
            v-model="periodForm.status"
            dict-type="honor_status"
            style="width: 100%; min-width: 150px"
          />
        </el-form-item>
        <el-form-item label="总完成次数">
          <el-input-number v-model="periodForm.totalCompletedCount" :min="0" />
        </el-form-item>
        <el-form-item label="榜单人数">
          <el-input-number v-model="periodForm.topN" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="periodEditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPeriodEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 筛选和搜索区域 -->
    <div class="admin-search-card">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="x"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="6">
          <DictSelect
            v-model="filters.status"
            dict-type="honor_status"
            placeholder="选择状态"
            clearable
            style="width: 100%; min-width: 150px"
          />
        </el-col>
        <el-col :span="10" class="btn-group">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" :loading="generating" @click="openGenerateDialog">
            <el-icon><Refresh /></el-icon>
            刷新光荣榜
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 光荣榜周期列表 -->
    <div class="admin-content-card admin-content-card--fixed-pagination">
      <div class="admin-table-header">
        <span class="admin-table-title">光荣榜列表</span>
      </div>

      <div class="admin-table-wrapper">
        <el-table
          v-loading="loading"
          :data="periodList"
          :fit="!isMobile"
          :table-layout="isMobile ? 'auto' : 'fixed'"
          class="admin-table"
          style="width: 100%;"
          @row-dblclick="handleViewDetail"
        >
          <el-table-column v-if="!isMobile" prop="periodId" label="ID" width="110">
            <template #default="scope">
              <el-tooltip :content="String(scope.row.periodId || '')" placement="top">
                <el-button size="small" class="id-chip" @click.stop="handleCopyId(scope.row.periodId)">
                  {{ formatShortId(scope.row.periodId) }}
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="periodStart" label="周期" min-width="260">
            <template #default="scope">
              {{ formatDate(scope.row.periodStart) }} ~ {{ formatDate(scope.row.periodEnd) }}
            </template>
          </el-table-column>
          <el-table-column prop="totalCompletedCount" label="完成次数" width="120" align="center" />
          <el-table-column prop="topN" label="榜单人数" width="120" align="center" />
          <el-table-column prop="status" label="状态" width="140" align="center">
            <template #default="scope">
              <DictTag
                v-if="hasDictValue(honor_status, scope.row.status)"
                :options="honor_status"
                :value="scope.row.status"
              />
              <span v-else>{{ getStatusLabel(scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" prop="sendTime" label="发送时间" width="180" align="center">
            <template #default="scope">
              {{ formatDateTime(scope.row.sendTime) || '-' }}
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" prop="awardTime" label="颁奖时间" width="180" align="center">
            <template #default="scope">
              {{ formatDateTime(scope.row.awardTime) || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isMobile ? 180 : 320" :fixed="isMobile ? false : 'right'">
            <template #default="scope">
              <el-button type="primary" link @click="handleViewDetail(scope.row)">
                查看榜单
              </el-button>
              <el-button type="primary" link @click="handleEditPeriod(scope.row)">
                编辑
              </el-button>
              <el-button
                type="success"
                link
                :disabled="scope.row.status === 'SENT' || scope.row.status === 'AWARDED'"
                @click="handleMarkSent(scope.row)"
              >
                标记已发送
              </el-button>
              <el-button
                type="warning"
                link
                :disabled="scope.row.status === 'AWARDED'"
                @click="handleMarkAwarded(scope.row)"
              >
                标记已颁奖
              </el-button>
              <el-button type="info" link @click="handleExport(scope.row)">
                导出
              </el-button>
              <el-button type="danger" link @click="handleDelete(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="admin-pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total || periodList.length"
          :page-count="pageCount"
          :hide-on-single-page="false"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 榜单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="光荣榜详情"
      width="800px"
      destroy-on-close
    >
      <div v-if="currentPeriod">
        <div class="detail-header">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <p>周期：{{ formatDate(currentPeriod.periodStart) }} ~ {{ formatDate(currentPeriod.periodEnd) }}</p>
              <p>总完成招领：{{ currentPeriod.totalCompletedCount || 0 }} 次</p>
            </div>
            <el-button type="primary" size="small" @click="handleAddItem">新增成员</el-button>
          </div>
        </div>

        <el-table :data="boardList" :fit="!isMobile" :table-layout="isMobile ? 'auto' : 'fixed'" border class="detail-table">
          <el-table-column prop="rank" label="排名" width="80" align="center" />
          <el-table-column prop="name" label="姓名" min-width="120">
            <template #default="scope">
              {{ scope.row.name || scope.row.username }}
            </template>
          </el-table-column>
          <el-table-column prop="departmentName" label="学院" min-width="160" />
          <el-table-column prop="completedCount" label="完成次数" width="120" align="center" />
          <el-table-column prop="points" label="积分" width="100" align="center" />
          <el-table-column v-if="!isMobile" prop="lastCompletedAt" label="最后完成时间" width="180" align="center">
            <template #default="scope">
              {{ formatDateTime(scope.row.lastCompletedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isMobile ? 110 : 150" :fixed="isMobile ? false : 'right'">
            <template #default="scope">
              <el-button type="primary" link size="small" @click="handleEditItem(scope.row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDeleteItem(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 候选人选择对话框 -->
    <el-dialog
      v-model="candidateDialogVisible"
      title="选择添加成员"
      width="700px"
    >
      <div class="candidate-search">
        <el-input
          v-model="candidateSearchKeyword"
          placeholder="搜索姓名/学号/用户名"
          clearable
          @keyup.enter="handleSearchCandidates"
          style="width: 300px; margin-right: 10px;"
        />
        <el-button type="primary" @click="handleSearchCandidates">搜索</el-button>
      </div>

      <el-table
        :data="candidateList"
        v-loading="candidateLoading"
        style="width: 100%; margin-top: 15px;"
        @selection-change="handleCandidateSelectionChange"
        height="400"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="头像" width="70" align="center">
          <template #default="scope">
            <el-avatar :size="32" :src="scope.row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="departmentName" label="学院" min-width="150" />
        <el-table-column prop="studentId" label="学号" width="120" />
      </el-table>

      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>已选择 {{ selectedCandidateIds.length }} 人</span>
          <div>
            <el-button @click="candidateDialogVisible = false">取消</el-button>
            <el-button
              type="primary"
              :disabled="selectedCandidateIds.length === 0"
              @click="handleBatchAddCandidates"
            >
              批量添加
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 榜单成员编辑对话框 -->
    <el-dialog
      v-model="itemDialogVisible"
      :title="isEditItem ? '编辑成员' : '新增成员'"
      width="500px"
    >
      <el-form :model="itemForm" label-width="100px">
        <el-form-item label="排名">
          <el-input-number v-model="itemForm.rank" :min="1" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="itemForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="itemForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="itemForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="itemForm.departmentName" placeholder="请输入学院" />
        </el-form-item>
        <el-form-item label="完成次数">
          <el-input-number v-model="itemForm.completedCount" :min="0" />
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="itemForm.points" :min="0" />
        </el-form-item>
        <el-form-item label="最后完成时间">
          <el-date-picker v-model="itemForm.lastCompletedAt" type="datetime" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="itemDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitItem">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, onBeforeUnmount } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
import DictSelect from '../../components/Dict/DictSelect.vue';
import DictTag from '../../components/Dict/DictTag.vue';
import { getDicts } from '../../api/system/dict/data';
import {
  getHonorPeriods,
  getHonorBoardDetail,
  markHonorSent,
  markHonorAwarded,
  generateHonorBoard,
  deleteHonorBoard,
  exportHonorBoard,
  updateHonorPeriod,
  addHonorPeriodItem,
  updateHonorPeriodItem,
  deleteHonorPeriodItem,
  searchHonorCandidates,
  batchAddHonorPeriodItems
} from '../../api/honorApi';

export default {
  name: 'HonorManagement',
  components: {
    Refresh,
    DictSelect,
    DictTag
  },
  setup() {
    const loading = ref(false);
    const generating = ref(false);
    const periodList = ref([]);
    const honor_status = ref([]);
    const isMobile = ref(false);
    const dateRange = ref([]);
    const filters = reactive({
      status: ''
    });

    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    });

    const pageCount = computed(() => {
      const effectiveTotal = Number(pagination.total) || periodList.value.length || 0;
      return Math.max(1, Math.ceil(effectiveTotal / pagination.pageSize));
    });

    const statusLabelMap = {
      PENDING: '待发送',
      SENT: '已发送',
      AWARDED: '已颁奖'
    };

    const getStatusLabel = (value) => {
      const key = String(value || '').toUpperCase();
      return statusLabelMap[key] || key || '-';
    };

    const hasDictValue = (dictRef, value) => {
      if (!value || !Array.isArray(dictRef?.value)) return false;
      return dictRef.value.some((item) => String(item.value || item.dictValue).toUpperCase() === String(value).toUpperCase());
    };

    const detailDialogVisible = ref(false);
    const currentPeriod = ref(null);
    const boardList = ref([]);

    // 生成光荣榜对话框相关
    const generateDialogVisible = ref(false);
    const generateForm = reactive({
      type: 1, // 0=本周, 1=上周, 2=自定义日期
      dateRange: []
    });

    const openGenerateDialog = () => {
      generateForm.type = 1;
      generateForm.dateRange = [];
      generateDialogVisible.value = true;
    };

    // 周期编辑相关
    const periodEditDialogVisible = ref(false);
    const periodForm = reactive({
      periodId: '',
      periodStart: '',
      periodEnd: '',
      status: '',
      totalCompletedCount: 0,
      topN: 10
    });

    // 榜单成员编辑相关
    const itemDialogVisible = ref(false);
    const isEditItem = ref(false);
    const itemForm = reactive({
      id: '',
      periodId: '',
      rank: 1,
      userId: '',
      username: '',
      name: '',
      className: '',
      departmentName: '',
      completedCount: 0,
      points: 0,
      lastCompletedAt: ''
    });

    // 候选人选择相关
    const candidateDialogVisible = ref(false);
    const candidateLoading = ref(false);
    const candidateList = ref([]);
    const candidateSearchKeyword = ref('');
    const selectedCandidateIds = ref([]);

    const handleOpenCandidateDialog = () => {
      candidateDialogVisible.value = true;
      candidateSearchKeyword.value = '';
      selectedCandidateIds.value = [];
      candidateList.value = [];
      handleSearchCandidates();
    };

    const handleSearchCandidates = async () => {
      candidateLoading.value = true;
      try {
        const res = await searchHonorCandidates({ keyword: candidateSearchKeyword.value });
        candidateList.value = res.data || [];
      } catch (e) {
        ElMessage.error('搜索失败');
      } finally {
        candidateLoading.value = false;
      }
    };

    const handleCandidateSelectionChange = (selection) => {
      selectedCandidateIds.value = selection.map(item => item.userId);
    };

    const handleBatchAddCandidates = async () => {
      if (selectedCandidateIds.value.length === 0) return;
      try {
        await batchAddHonorPeriodItems({
          periodId: currentPeriod.value.periodId,
          userIds: selectedCandidateIds.value
        });
        ElMessage.success('批量添加成功');
        candidateDialogVisible.value = false;
        const res = await getHonorBoardDetail(currentPeriod.value.periodId);
        boardList.value = res.data.list || [];
      } catch (e) {
        ElMessage.error(e.message || '添加失败');
      }
    };

    const formatDate = (value) => {
      if (!value) return '';
      const date = new Date(value);
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      return `${y}-${m}-${d}`;
    };

    const updateViewportState = () => {
      isMobile.value = window.innerWidth <= 768;
    };

    const formatDateTime = (value) => {
      if (!value) return '';
      const date = new Date(value);
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      const hh = String(date.getHours()).padStart(2, '0');
      const mm = String(date.getMinutes()).padStart(2, '0');
      return `${y}-${m}-${d} ${hh}:${mm}`;
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



    const loadPeriods = async () => {
      try {
        loading.value = true;
        const params = {
          page: pagination.currentPage,
          pageSize: pagination.pageSize,
          status: filters.status || undefined,
          startTime: dateRange.value && dateRange.value.length === 2 ? dateRange.value[0] : undefined,
          endTime: dateRange.value && dateRange.value.length === 2 ? dateRange.value[1] : undefined
        };
        const res = await getHonorPeriods(params);
        periodList.value = res.data.list || [];
        const totalFromApi = res?.data?.total ?? res?.data?.totalCount ?? res?.data?.count;
        pagination.total = Number(totalFromApi ?? periodList.value.length ?? 0);
      } catch (error) {
        ElMessage.error('加载光荣榜周期失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleSearch = () => {
      pagination.currentPage = 1;
      loadPeriods();
    };

    const handleReset = () => {
      filters.status = '';
      dateRange.value = [];
      pagination.currentPage = 1;
      loadPeriods();
    };

    const handleSizeChange = (size) => {
      pagination.pageSize = size;
      pagination.currentPage = 1;
      loadPeriods();
    };

    const handleCurrentChange = (page) => {
      pagination.currentPage = page;
      loadPeriods();
    };

    const handleViewDetail = async (period) => {
      try {
        loading.value = true;
        const res = await getHonorBoardDetail(period.periodId);
        currentPeriod.value = {
          periodId: res.data.periodId,
          periodStart: res.data.periodStart,
          periodEnd: res.data.periodEnd,
          totalCompletedCount: res.data.totalCompletedCount
        };
        boardList.value = res.data.list || [];
        detailDialogVisible.value = true;
      } catch (error) {
        ElMessage.error('加载光荣榜详情失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleMarkSent = async (period) => {
      try {
        loading.value = true;
        await markHonorSent(period.periodId);
        ElMessage.success('已标记为已发送');
        loadPeriods();
      } catch (error) {
        ElMessage.error('标记失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleMarkAwarded = async (period) => {
      try {
        loading.value = true;
        await markHonorAwarded(period.periodId);
        ElMessage.success('已标记为已颁奖');
        loadPeriods();
      } catch (error) {
        ElMessage.error('标记失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleExport = async (period) => {
      try {
        loading.value = true;
        const blob = await exportHonorBoard({
          periodId: period.periodId,
          format: 'xlsx'
        });

        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute(
          'download',
          `光荣榜_${formatDate(period.periodStart)}_${formatDate(period.periodEnd)}.xlsx`
        );
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        ElMessage.success('导出成功');
      } catch (error) {
        ElMessage.error('导出失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    const handleDelete = async (period) => {
      try {
        await ElMessageBox.confirm(
          '确定要删除该光荣榜周期吗？',
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );

        loading.value = true;
        await deleteHonorBoard(period.periodId);
        ElMessage.success('删除成功');
        loadPeriods();
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败：' + (error.message || '未知错误'));
        }
      } finally {
        loading.value = false;
      }
    };

    const handleGenerate = async () => {
      try {
        generating.value = true;
        const params = {
          type: generateForm.type,
          startDate: generateForm.type === 2 && generateForm.dateRange ? generateForm.dateRange[0] : undefined,
          endDate: generateForm.type === 2 && generateForm.dateRange ? generateForm.dateRange[1] : undefined
        };

        await generateHonorBoard(params);
        ElMessage.success('光荣榜生成成功');
        generateDialogVisible.value = false;
        loadPeriods();
      } catch (error) {
        ElMessage.error('生成失败：' + (error.message || '未知错误'));
      } finally {
        generating.value = false;
      }
    };

    // 周期编辑逻辑
    const handleEditPeriod = (row) => {
      Object.assign(periodForm, {
        periodId: row.periodId,
        periodStart: row.periodStart,
        periodEnd: row.periodEnd,
        status: row.status,
        totalCompletedCount: row.totalCompletedCount,
        topN: row.topN
      });
      periodEditDialogVisible.value = true;
    };

    const submitPeriodEdit = async () => {
      try {
        loading.value = true;
        await updateHonorPeriod(periodForm.periodId, periodForm);
        ElMessage.success('更新成功');
        periodEditDialogVisible.value = false;
        loadPeriods();
      } catch (error) {
        ElMessage.error('更新失败：' + (error.message || '未知错误'));
      } finally {
        loading.value = false;
      }
    };

    // 榜单成员逻辑
    const handleAddItem = () => {
      if (!currentPeriod.value) return;
      isEditItem.value = false;
      Object.assign(itemForm, {
        id: '',
        periodId: currentPeriod.value.periodId,
        rank: boardList.value.length + 1,
        userId: '',
        username: '',
        name: '',
        className: '',
        departmentName: '',
        completedCount: 0,
        points: 0,
        lastCompletedAt: ''
      });
      itemDialogVisible.value = true;
    };

    const handleEditItem = (row) => {
      isEditItem.value = true;
      Object.assign(itemForm, row);
      itemForm.periodId = currentPeriod.value.periodId;
      itemDialogVisible.value = true;
    };

    const handleDeleteItem = async (row) => {
      try {
        await ElMessageBox.confirm('确定要删除该成员吗？', '提示', { type: 'warning' });
        await deleteHonorPeriodItem(row.id);
        ElMessage.success('删除成功');
        const res = await getHonorBoardDetail(currentPeriod.value.periodId);
        boardList.value = res.data.list || [];
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败：' + (error.message || '未知错误'));
        }
      }
    };

    const submitItem = async () => {
      try {
        if (isEditItem.value) {
          await updateHonorPeriodItem(itemForm.id, itemForm);
          ElMessage.success('更新成功');
        } else {
          await addHonorPeriodItem(itemForm);
          ElMessage.success('添加成功');
        }
        itemDialogVisible.value = false;
        const res = await getHonorBoardDetail(currentPeriod.value.periodId);
        boardList.value = res.data.list || [];
      } catch (error) {
        ElMessage.error('操作失败：' + (error.message || '未知错误'));
      }
    };

    onMounted(() => {
      updateViewportState();
      window.addEventListener('resize', updateViewportState);
      loadPeriods();
      getDicts('honor_status').then(res => {
        honor_status.value = res.data;
      });
    });

    onBeforeUnmount(() => {
      window.removeEventListener('resize', updateViewportState);
    });

    return {
      loading,
      generating,
      periodList,
      dateRange,
      filters,
      pagination,
      pageCount,
      detailDialogVisible,
      currentPeriod,
      boardList,
      generateDialogVisible,
      generateForm,
      openGenerateDialog,
      periodEditDialogVisible,
      periodForm,
      itemDialogVisible,
      isEditItem,
      itemForm,
      candidateDialogVisible,
      candidateLoading,
      candidateList,
      candidateSearchKeyword,
      selectedCandidateIds,
      isMobile,
      honor_status,
      hasDictValue,
      getStatusLabel,
      handleOpenCandidateDialog,
      handleSearchCandidates,
      handleCandidateSelectionChange,
      handleBatchAddCandidates,
      formatDate,
      formatDateTime,
      formatShortId,
      handleCopyId,

      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleViewDetail,
      handleMarkSent,
      handleMarkAwarded,
      handleExport,
      handleDelete,
      handleGenerate,
      handleEditPeriod,
      submitPeriodEdit,
      handleAddItem: handleOpenCandidateDialog,
      handleEditItem,
      handleDeleteItem,
      submitItem
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

.detail-header {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-header p {
  margin: 5px 0;
  font-size: 14px;
  color: #606266;
}

.candidate-search {
  display: flex;
  margin-bottom: 15px;
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

@media (max-width: 768px) {
  .candidate-search {
    flex-direction: column;
    gap: 8px;
  }
}
</style>


