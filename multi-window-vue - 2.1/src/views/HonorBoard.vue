<template>
  <div class="honor-page">
    <!-- 顶部标题区域 - 红色主题 -->
    <div class="honor-header-section">
      <div class="header-banner">
        <div class="banner-decoration left"></div>
        <div class="banner-content">
          <div class="banner-icon">🏆</div>
          <h2 class="page-title">拾金不昧光荣榜</h2>
          <p class="banner-subtitle">弘扬美德 · 传递正能量</p>
        </div>
        <div class="banner-decoration right"></div>
      </div>
      
      <!-- 周期信息卡片 -->
      <div class="summary-card">
        <div class="summary-border">
          <div class="summary-content">
            <div class="summary-icon">📅</div>
            <div class="summary-text">
              <h3>本周榜单</h3>
              <p class="period-text">
                <span v-if="currentPeriod">
                  {{ formatDate(currentPeriod.periodStart) }} ~ {{ formatDate(currentPeriod.periodEnd) }}
                </span>
                <span v-else>加载中...</span>
              </p>
              <p v-if="currentPeriod" class="summary-tip">
                本周已成功完成 <span class="highlight">{{ currentPeriod.totalCompletedCount || 0 }}</span> 次招领
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 榜单内容区域 -->
    <div class="honor-content">
      <div class="honor-card">
        <!-- 金色边框装饰 -->
        <div class="card-border-top"></div>
        <div class="card-border-bottom"></div>
        
        <div class="card-header">
          <div class="card-title-area">
            <div class="title-with-icon">
              <span class="title-icon">⭐</span>
              <h3>荣誉榜单</h3>
            </div>
            <span class="card-subtitle">按积分排名，展示前 {{ currentPeriod?.topN || 10 }} 名优秀同学</span>
          </div>
          <el-button type="primary" link @click="loadCurrentWeek" class="refresh-btn">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>

        <el-table
          v-loading="loading"
          :data="honorList"
          border
          class="honor-table"
          height="100%"
          empty-text="本周还没有上榜记录，快去发布招领吧！"
        >
          <el-table-column prop="rank" label="排名" width="90" align="center">
            <template #default="scope">
              <div :class="['rank-badge', getRankClass(scope.row.rank)]">
                <span v-if="scope.row.rank <= 3" class="rank-crown">{{ getRankIcon(scope.row.rank) }}</span>
                <span v-else>{{ scope.row.rank }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="学生" min-width="200">
            <template #default="scope">
              <div class="user-cell">
                <div :class="['avatar-wrapper', getRankClass(scope.row.rank)]">
                  <el-avatar :size="scope.row.rank <= 3 ? 40 : 32" :src="scope.row.avatar" class="user-avatar" />
                  <div v-if="scope.row.rank <= 3" class="avatar-glow"></div>
                </div>
                <div class="user-info">
                  <div class="user-name">
                    {{ scope.row.name || scope.row.username }}
                    <span v-if="scope.row.rank === 1" class="champion-tag">冠军</span>
                  </div>
                  <div class="user-meta">
                    <span v-if="scope.row.departmentName">{{ scope.row.departmentName }}</span>
                    <span v-else>学院未知</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="completedCount" label="本周完成" width="100" align="center">
            <template #default="scope">
              <div class="count-badge">
                {{ scope.row.completedCount }} 次
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="points" label="荣誉积分" width="100" align="center">
            <template #default="scope">
              <div :class="['points-badge', getRankClass(scope.row.rank)]">
                <span class="points-icon">★</span>
                <span class="points-value">{{ scope.row.points }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="lastCompletedAt" label="最后完成时间" min-width="150" align="center">
            <template #default="scope">
              <span class="time-text">{{ formatDateTime(scope.row.lastCompletedAt) }}</span>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 底部装饰 -->
        <div class="card-footer-decoration">
          <span class="footer-text">拾金不昧是中华民族的传统美德</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
import { getCurrentWeekHonorBoard } from '../api/honorApi';

export default {
  name: 'HonorBoard',
  components: {
    Refresh
  },
  setup() {
    const loading = ref(false);
    const honorList = ref([]);
    const currentPeriod = ref(null);

    const formatDate = (value) => {
      if (!value) return '';
      const date = new Date(value);
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      return `${y}-${m}-${d}`;
    };

    const formatDateTime = (value) => {
      if (!value) return '-';
      const date = new Date(value);
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      const hh = String(date.getHours()).padStart(2, '0');
      const mm = String(date.getMinutes()).padStart(2, '0');
      return `${y}-${m}-${d} ${hh}:${mm}`;
    };

    const getRankClass = (rank) => {
      if (rank === 1) return 'rank-first';
      if (rank === 2) return 'rank-second';
      if (rank === 3) return 'rank-third';
      return 'rank-normal';
    };
    
    const getRankIcon = (rank) => {
      if (rank === 1) return '👑';
      if (rank === 2) return '🥈';
      if (rank === 3) return '🥉';
      return rank;
    };

    const loadCurrentWeek = async () => {
      try {
        loading.value = true;
        const res = await getCurrentWeekHonorBoard();
        currentPeriod.value = {
          periodStart: res.data.periodStart,
          periodEnd: res.data.periodEnd,
          topN: res.data.topN,
          totalCompletedCount: res.data.totalCompletedCount,
          status: res.data.status
        };
        honorList.value = res.data.list || [];
      } catch (error) {
        if (error && error.response && error.response.status === 401) return;
        ElMessage.error('加载光荣榜失败');
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      loadCurrentWeek();
    });

    return {
      loading,
      honorList,
      currentPeriod,
      formatDate,
      formatDateTime,
      getRankClass,
      getRankIcon,
      loadCurrentWeek
    };
  }
};
</script>

<style scoped>
.honor-page {
  --page-accent: #b91c1c;
  --page-accent-strong: #991b1b;
  --page-gold: #f59e0b;
  --page-gold-soft: rgba(245, 158, 11, 0.18);
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: radial-gradient(900px 360px at 10% -10%, rgba(185, 28, 28, 0.12), transparent 60%),
    linear-gradient(180deg, #fff7ed 0%, #ffffff 70%);
  padding: 18px;
  gap: 16px;
}

/* ????? */
.honor-header-section {
  flex-shrink: 0;
}

.header-banner {
  background: linear-gradient(135deg, #b91c1c 0%, #991b1b 50%, #7f1d1d 100%);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 24px rgba(185, 28, 28, 0.24);
}

.header-banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 10%, rgba(255, 255, 255, 0.16), transparent 45%);
  opacity: 0.8;
}

.banner-decoration {
  position: absolute;
  width: 70px;
  height: 70px;
  border: 2px solid rgba(245, 158, 11, 0.5);
  border-radius: 50%;
}

.banner-decoration.left {
  left: -18px;
  top: -18px;
}

.banner-decoration.right {
  right: -18px;
  bottom: -18px;
}

.banner-content {
  text-align: center;
  position: relative;
  z-index: 1;
}

.banner-icon {
  font-size: 34px;
  margin-bottom: 6px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 4px 0;
  letter-spacing: 2px;
}

.banner-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  letter-spacing: 3px;
}

/* ????? */
.summary-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 2px;
  box-shadow: var(--shadow-1);
  border: 1px solid #fde68a;
}

.summary-border {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 50%, #f59e0b 100%);
  border-radius: 12px;
  padding: 2px;
}

.summary-content {
  background: #ffffff;
  border-radius: 10px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.summary-icon {
  font-size: 28px;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff7ed;
  border-radius: 12px;
  border: 2px solid #fbbf24;
}

.summary-text h3 {
  font-size: 16px;
  font-weight: 600;
  color: #92400e;
  margin: 0 0 4px 0;
}

.period-text {
  margin: 0;
  color: #78716c;
  font-size: 13px;
}

.summary-tip {
  margin: 4px 0 0 0;
  color: #78716c;
  font-size: 12px;
}

.summary-tip .highlight {
  color: #b91c1c;
  font-weight: 700;
  font-size: 14px;
}

/* ????? */
.honor-content {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.honor-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 2px solid #f59e0b;
  border-radius: 16px;
  padding: 16px;
  position: relative;
  box-shadow: 0 12px 28px rgba(245, 158, 11, 0.15);
  overflow: hidden;
}

.card-border-top,
.card-border-bottom {
  position: absolute;
  left: 18px;
  right: 18px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #f59e0b, #fbbf24, #f59e0b, transparent);
}

.card-border-top { top: 6px; }
.card-border-bottom { bottom: 6px; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-shrink: 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #fde68a;
}

.title-with-icon {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 18px;
}

.card-title-area h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #92400e;
}

.card-subtitle {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #a8a29e;
}

.refresh-btn {
  color: #b45309;
}

.honor-table {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

/* ???? */
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  font-weight: 700;
  font-size: 13px;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}

.rank-crown {
  font-size: 18px;
  filter: drop-shadow(0 1px 2px rgba(0,0,0,0.2));
}

.rank-first {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border: 2px solid #f59e0b;
}

.rank-second {
  background: linear-gradient(135deg, #e5e7eb 0%, #d1d5db 100%);
  border: 2px solid #cbd5f5;
  color: #475569;
}

.rank-third {
  background: linear-gradient(135deg, #fdba74 0%, #fb923c 100%);
  border: 2px solid #fb923c;
}

.rank-normal {
  background: #e5e7eb;
  color: #64748b;
  border: 2px solid #e2e8f0;
}

/* ????? */
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-wrapper.rank-first .user-avatar {
  border: 2px solid #fbbf24;
  box-shadow: 0 0 16px rgba(251, 191, 36, 0.5);
}

.avatar-glow {
  position: absolute;
  inset: -4px;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  z-index: -1;
}

.user-avatar {
  border: 2px solid #e2e8f0;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #44403c;
  display: flex;
  align-items: center;
  gap: 6px;
}

.champion-tag {
  font-size: 10px;
  padding: 2px 6px;
  background: #b91c1c;
  color: #ffffff;
  border-radius: 999px;
  font-weight: 600;
}

.user-meta {
  font-size: 12px;
  color: #a8a29e;
  margin-top: 2px;
}

/* ?????? */
.count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  background: #ecfdf3;
  border: 1px solid #bbf7d0;
  border-radius: 999px;
  font-size: 12px;
  color: #166534;
  font-weight: 500;
}

/* ???? */
.points-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 12px;
}

.points-badge.rank-first {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fbbf24;
}

.points-badge.rank-second {
  background: #f3f4f6;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.points-badge.rank-third {
  background: #ffedd5;
  color: #9a3412;
  border: 1px solid #fdba74;
}

.points-badge.rank-normal {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.points-icon {
  font-size: 12px;
}

.points-value {
  font-size: 13px;
}

.time-text {
  font-size: 12px;
  color: #78716c;
}

/* ???? */
.card-footer-decoration {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #fde68a;
  text-align: center;
}

.footer-text {
  font-size: 12px;
  color: #b45309;
  font-style: italic;
  letter-spacing: 2px;
}

/* ??? */
@media (max-width: 768px) {
  .honor-page {
    padding: 14px;
  }

  .header-banner {
    padding: 16px;
  }

  .page-title {
    font-size: 20px;
  }

  .summary-content {
    padding: 12px 14px;
    flex-direction: column;
    text-align: center;
  }

  .honor-card {
    padding: 14px;
  }

  .card-title-area h3 {
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .honor-page {
    padding: 10px;
  }

  .banner-icon {
    font-size: 28px;
  }

  .page-title {
    font-size: 18px;
  }
}
</style>

