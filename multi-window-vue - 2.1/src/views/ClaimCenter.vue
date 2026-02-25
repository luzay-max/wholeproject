<template>
  <div class="page-shell claim-page">
    <div class="page-card claim-header">
      <h2>认领中心</h2>
      <p>在这里管理您的认领申请、提交凭证、确认归还流程。</p>
    </div>

    <div class="page-card claim-body">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我发起的认领" name="apply">
          <el-table :data="applyList" class="claim-table">
            <el-table-column prop="itemName" label="物品名称" min-width="160" />
            <el-table-column prop="itemType" label="类型" width="90" />
            <el-table-column prop="status" label="状态" width="140">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applyTime" label="申请时间" width="180" />
            <el-table-column label="操作" min-width="250">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'APPLIED'"
                  type="primary"
                  size="small"
                  @click="openProofDialog(row)"
                >
                  提交凭证
                </el-button>
                <el-button
                  v-if="row.status === 'CONFIRMED'"
                  type="success"
                  size="small"
                  @click="handleComplete(row)"
                >
                  完成归还
                </el-button>
                <el-button size="small" @click="viewItem(row)">查看信息</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="applyPagination.page"
              v-model:page-size="applyPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              :total="applyPagination.total"
              @current-change="loadApplyList"
              @size-change="loadApplyList"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="待我确认" name="confirm">
          <el-table :data="confirmList" class="claim-table">
            <el-table-column prop="itemName" label="物品名称" min-width="160" />
            <el-table-column prop="status" label="状态" width="140">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="proofText" label="凭证说明" min-width="240" show-overflow-tooltip />
            <el-table-column prop="proofSubmitTime" label="凭证时间" width="180" />
            <el-table-column label="操作" min-width="260">
              <template #default="{ row }">
                <template v-if="row.status === 'PROOF_SUBMITTED'">
                  <el-button type="success" size="small" @click="handleConfirm(row, true)">通过</el-button>
                  <el-button type="danger" size="small" @click="handleConfirm(row, false)">驳回</el-button>
                </template>
                <el-button size="small" @click="openCompareDialog(row)">查看对比</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="confirmPagination.page"
              v-model:page-size="confirmPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              :total="confirmPagination.total"
              @current-change="loadConfirmList"
              @size-change="loadConfirmList"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="proofDialogVisible" title="提交认领凭证" width="520px">
      <el-form :model="proofForm" label-width="90px">
        <el-form-item label="凭证说明">
          <el-input
            v-model="proofForm.proofText"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请描述可以证明您是失主/领取人的信息"
          />
        </el-form-item>
        <el-form-item label="凭证图片">
          <el-input
            v-model="proofForm.proofImages"
            placeholder='可选，多个图片URL可传JSON数组，如 ["url1","url2"]'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="proofDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProof">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="compareDialogVisible" title="凭证与信息对比" width="760px">
      <div class="compare-grid" v-if="compareClaim">
        <div class="compare-card">
          <h4>申请人凭证</h4>
          <p><strong>申请说明：</strong>{{ compareClaim.applyNote || '无' }}</p>
          <p><strong>凭证说明：</strong>{{ compareClaim.proofText || '无' }}</p>
          <div v-if="compareProofImages.length" class="proof-images">
            <img v-for="(img, idx) in compareProofImages" :key="idx" :src="img" alt="凭证图片" />
          </div>
        </div>

        <div class="compare-card">
          <h4>原信息详情</h4>
          <p><strong>物品名称：</strong>{{ compareItem.name || '-' }}</p>
          <p><strong>类型：</strong>{{ compareClaim.itemType }}</p>
          <p><strong>地点：</strong>{{ compareItem.location || '-' }}</p>
          <p><strong>描述：</strong>{{ compareItem.description || '-' }}</p>
          <div v-if="compareItemImages.length" class="proof-images">
            <img v-for="(img, idx) in compareItemImages" :key="idx" :src="img" alt="信息图片" />
          </div>
        </div>
      </div>
      <template #footer>
        <template v-if="compareClaim && compareClaim.status === 'PROOF_SUBMITTED'">
          <el-button
            type="danger"
            @click="handleConfirm(compareClaim, false, { fromCompareDialog: true })"
          >
            驳回
          </el-button>
          <el-button
            type="success"
            @click="handleConfirm(compareClaim, true, { fromCompareDialog: true })"
          >
            通过
          </el-button>
        </template>
        <el-button @click="compareDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getMyClaimApplications,
  getMyClaimPendingConfirm,
  submitClaimProof,
  confirmClaim,
  completeClaim
} from '../api/claimApi';
import { getLostDetail } from '../api/lostApi';
import { getFindDetail } from '../api/findApi';

const router = useRouter();
const activeTab = ref('apply');

const applyList = ref([]);
const confirmList = ref([]);
const applyPagination = reactive({ page: 1, pageSize: 10, total: 0 });
const confirmPagination = reactive({ page: 1, pageSize: 10, total: 0 });

const proofDialogVisible = ref(false);
const currentClaimId = ref('');
const proofForm = reactive({
  proofText: '',
  proofImages: ''
});
const compareDialogVisible = ref(false);
const compareClaim = ref(null);
const compareItem = ref({});

const statusText = (status) => {
  const map = {
    APPLIED: '已申请',
    PROOF_SUBMITTED: '已提交凭证',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    REJECTED: '已驳回'
  };
  return map[status] || status;
};

const statusTagType = (status) => {
  const map = {
    APPLIED: 'warning',
    PROOF_SUBMITTED: 'primary',
    CONFIRMED: 'success',
    COMPLETED: 'success',
    REJECTED: 'danger'
  };
  return map[status] || 'info';
};

const loadApplyList = async () => {
  const res = await getMyClaimApplications({
    page: applyPagination.page,
    pageSize: applyPagination.pageSize
  });
  const data = res?.data || {};
  applyList.value = data.list || [];
  applyPagination.total = Number(data.total || 0);
};

const loadConfirmList = async () => {
  const res = await getMyClaimPendingConfirm({
    page: confirmPagination.page,
    pageSize: confirmPagination.pageSize
  });
  const data = res?.data || {};
  confirmList.value = data.list || [];
  confirmPagination.total = Number(data.total || 0);
};

const openProofDialog = (row) => {
  currentClaimId.value = row.id;
  proofForm.proofText = '';
  proofForm.proofImages = '';
  proofDialogVisible.value = true;
};

const parseImages = (value) => {
  if (!value) return [];
  if (Array.isArray(value)) return value;
  try {
    const arr = JSON.parse(value);
    return Array.isArray(arr) ? arr : [];
  } catch (_) {
    return [];
  }
};

const compareProofImages = computed(() => parseImages(compareClaim.value?.proofImages));
const compareItemImages = computed(() => parseImages(compareItem.value?.images));

const openCompareDialog = async (row) => {
  compareClaim.value = row;
  compareItem.value = {};
  try {
    const res = row.itemType === 'lost'
      ? await getLostDetail(row.itemId)
      : await getFindDetail(row.itemId);
    compareItem.value = res?.data || {};
    compareDialogVisible.value = true;
  } catch (e) {
    ElMessage.error(e?.message || '加载信息详情失败');
  }
};

const submitProof = async () => {
  if (!proofForm.proofText.trim()) {
    ElMessage.warning('请填写凭证说明');
    return;
  }
  await submitClaimProof(currentClaimId.value, {
    proofText: proofForm.proofText,
    proofImages: proofForm.proofImages
  });
  ElMessage.success('凭证提交成功');
  proofDialogVisible.value = false;
  loadApplyList();
  loadConfirmList();
};

const handleConfirm = async (row, approved, options = {}) => {
  const { fromCompareDialog = false } = options;
  if (approved) {
    await confirmClaim(row.id, { approved: true });
    ElMessage.success('已通过认领申请');
    if (fromCompareDialog) {
      compareDialogVisible.value = false;
    }
    loadConfirmList();
    return;
  }
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回认领', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputPattern: /^.{2,200}$/,
    inputErrorMessage: '驳回原因需2-200字符'
  });
  await confirmClaim(row.id, { approved: false, rejectReason: value });
  ElMessage.success('已驳回认领申请');
  if (fromCompareDialog) {
    compareDialogVisible.value = false;
  }
  loadConfirmList();
};

const handleComplete = async (row) => {
  await completeClaim(row.id);
  ElMessage.success('已完成归还流程');
  loadApplyList();
  loadConfirmList();
};

const viewItem = (row) => {
  router.push(`/info/detail/${row.itemType}/${row.itemId}`);
};

onMounted(() => {
  loadApplyList();
  loadConfirmList();
});
</script>

<style scoped>
.claim-page {
  padding: 16px;
}

.claim-header {
  padding: 18px 20px;
}

.claim-header h2 {
  margin: 0 0 4px;
}

.claim-header p {
  margin: 0;
  color: var(--color-text-secondary);
}

.claim-body {
  padding: 12px 14px 16px;
}

.claim-table {
  width: 100%;
}

.table-pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.compare-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.compare-card {
  border: 1px solid var(--color-border-secondary);
  border-radius: 10px;
  padding: 10px 12px;
  background: #f8fafc;
}

.compare-card h4 {
  margin: 0 0 8px;
}

.compare-card p {
  margin: 0 0 6px;
  line-height: 1.5;
}

.proof-images {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 8px;
}

.proof-images img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

@media (max-width: 768px) {
  .claim-page {
    padding: 10px;
  }

  .claim-header,
  .claim-body {
    padding: 12px;
  }

  .table-pagination {
    justify-content: center;
  }

  .compare-grid {
    grid-template-columns: 1fr;
  }
}
</style>
