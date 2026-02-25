<template>
  <div class="page-shell notice-page">
    <div class="page-card notice-header">
      <div>
        <h2>通知中心</h2>
        <p>查看审核结果、评论提醒、认领进度通知。</p>
      </div>
      <el-button type="primary" plain @click="handleReadAll">全部已读</el-button>
    </div>

    <div class="page-card notice-list-card">
      <el-table :data="noticeList" class="notice-table">
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="content" label="内容" min-width="320" show-overflow-tooltip />
        <el-table-column prop="bizType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="bizTagType(row.bizType)">{{ row.bizType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'info' : 'danger'">
              {{ row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button v-if="row.isRead !== 1" type="primary" link @click="markRead(row)">标记已读</el-button>
            <el-button
              v-if="hasJumpTarget(row)"
              type="primary"
              link
              @click="goNoticeTarget(row)"
            >
              {{ noticeJumpLabel(row) }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadNotices"
          @size-change="loadNotices"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getNoticeList, markNoticeRead, markAllNoticeRead } from '../api/noticeApi';

const router = useRouter();
const noticeList = ref([]);
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
});

const bizTagType = (bizType) => {
  const map = { AUDIT: 'warning', COMMENT: 'primary', CLAIM: 'success', FOUND_REPORT: 'warning', SYSTEM: 'info' };
  return map[bizType] || 'info';
};

const toUpper = (val) => String(val || '').trim().toUpperCase();
const toText = (val) => String(val || '').trim();

const isFoundReportNotice = (row) => {
  const bizType = toUpper(row?.bizType);
  const title = toText(row?.title);
  const content = toText(row?.content);
  if (bizType === 'FOUND_REPORT') {
    return !!toText(row?.bizId);
  }
  if (bizType !== 'CLAIM') {
    return false;
  }
  const combined = `${title} ${content}`;
  return !!toText(row?.bizId) && /(反馈找到了您的失物|收到新线索|有人反馈找到了)/.test(combined);
};

const hasJumpTarget = (row) => {
  if (!toText(row?.bizId)) return false;
  const bizType = toUpper(row?.bizType);
  return bizType === 'CLAIM' || bizType === 'FOUND_REPORT';
};

const noticeJumpLabel = (row) => (isFoundReportNotice(row) ? '查看失物' : '查看流程');

const loadNotices = async () => {
  const res = await getNoticeList({
    page: pagination.page,
    pageSize: pagination.pageSize
  });
  const data = res?.data || {};
  noticeList.value = data.list || [];
  pagination.total = Number(data.total || 0);
};

const markRead = async (row) => {
  await markNoticeRead(row.id);
  row.isRead = 1;
  ElMessage.success('已标记已读');
};

const handleReadAll = async () => {
  await markAllNoticeRead();
  ElMessage.success('已全部标记已读');
  loadNotices();
};

const goNoticeTarget = (row) => {
  if (isFoundReportNotice(row)) {
    router.push(`/info/detail/lost/${row.bizId}`);
    return;
  }
  router.push('/claim-center');
};

onMounted(loadNotices);
</script>

<style scoped>
.notice-page {
  padding: 16px;
}

.notice-header {
  padding: 16px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.notice-header h2 {
  margin: 0 0 4px;
}

.notice-header p {
  margin: 0;
  color: var(--color-text-secondary);
}

.notice-list-card {
  padding: 14px;
}

.table-pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .notice-page {
    padding: 10px;
  }
  .notice-header,
  .notice-list-card {
    padding: 12px;
  }
  .table-pagination {
    justify-content: center;
  }
}
</style>
