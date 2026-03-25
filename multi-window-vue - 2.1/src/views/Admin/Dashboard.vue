<template>
  <div class="admin-page-container">
    <div class="admin-content-card">
      <div class="panel-header">
        <h3>管理看板</h3>
        <el-select v-model="days" style="width: 140px" @change="loadTrend">
          <el-option :value="7" label="近7天" />
          <el-option :value="14" label="近14天" />
          <el-option :value="30" label="近30天" />
        </el-select>
      </div>

      <div class="overview-grid">
        <div class="overview-card">
          <span class="label">发布总量</span>
          <strong>{{ overview.totalPublished || 0 }}</strong>
        </div>
        <div class="overview-card">
          <span class="label">认领申请</span>
          <strong>{{ overview.totalClaims || 0 }}</strong>
        </div>
        <div class="overview-card">
          <span class="label">完成归还</span>
          <strong>{{ overview.completedClaims || 0 }}</strong>
        </div>
        <div class="overview-card">
          <span class="label">认领成功率</span>
          <strong>{{ overview.claimSuccessRate || 0 }}%</strong>
        </div>
        <div class="overview-card">
          <span class="label">平均处理时长</span>
          <strong>{{ overview.avgProcessHours || 0 }}h</strong>
        </div>
        <div class="overview-card">
          <span class="label">近7天活跃用户</span>
          <strong>{{ overview.activeUsers7d || 0 }}</strong>
        </div>
      </div>

      <div class="trend-chart-card">
        <div class="trend-chart-header">
          <h4>趋势折线图</h4>
          <div class="legend">
            <span class="legend-item"><i class="dot lost"></i>失物发布</span>
            <span class="legend-item"><i class="dot find"></i>招领发布</span>
            <span class="legend-item"><i class="dot apply"></i>认领申请</span>
            <span class="legend-item"><i class="dot complete"></i>完成归还</span>
          </div>
        </div>

        <div class="trend-chart-body" v-if="chartReady">
          <svg :viewBox="`0 0 ${chartWidth} ${chartHeight}`" class="trend-svg" preserveAspectRatio="none">
            <g>
              <line
                v-for="line in gridLines"
                :key="`grid-${line.value}`"
                :x1="padding.left"
                :x2="chartWidth - padding.right"
                :y1="line.y"
                :y2="line.y"
                class="grid-line"
              />
            </g>

            <polyline :points="seriesPoints.lost" class="line line-lost" />
            <polyline :points="seriesPoints.find" class="line line-find" />
            <polyline :points="seriesPoints.apply" class="line line-apply" />
            <polyline :points="seriesPoints.complete" class="line line-complete" />

            <g>
              <circle v-for="(p, idx) in pointDots.lost" :key="`lost-${idx}`" :cx="p.x" :cy="p.y" r="3" class="dot-point lost" />
              <circle v-for="(p, idx) in pointDots.find" :key="`find-${idx}`" :cx="p.x" :cy="p.y" r="3" class="dot-point find" />
              <circle v-for="(p, idx) in pointDots.apply" :key="`apply-${idx}`" :cx="p.x" :cy="p.y" r="3" class="dot-point apply" />
              <circle v-for="(p, idx) in pointDots.complete" :key="`complete-${idx}`" :cx="p.x" :cy="p.y" r="3" class="dot-point complete" />
            </g>

            <g>
              <text
                v-for="line in gridLines"
                :key="`label-y-${line.value}`"
                :x="padding.left - 8"
                :y="line.y + 4"
                class="axis-y-text"
                text-anchor="end"
              >
                {{ line.value }}
              </text>
            </g>

            <g>
              <text
                v-for="(tick, idx) in xTicks"
                :key="`label-x-${idx}`"
                :x="tick.x"
                :y="chartHeight - 6"
                class="axis-x-text"
                text-anchor="middle"
              >
                {{ tick.label }}
              </text>
            </g>
          </svg>
        </div>
        <el-empty v-else description="暂无趋势数据" :image-size="72" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { getDashboardOverview, getDashboardTrend } from '../../api/dashboardApi';

const days = ref(7);
const overview = ref({});
const trendList = ref([]);

const chartWidth = 960;
const chartHeight = 320;
const padding = {
  top: 24,
  right: 24,
  bottom: 40,
  left: 44
};

const numericTrend = computed(() =>
  (trendList.value || []).map((item) => ({
    date: item.date,
    lostPublished: Number(item.lostPublished || 0),
    findPublished: Number(item.findPublished || 0),
    claimApplied: Number(item.claimApplied || 0),
    claimCompleted: Number(item.claimCompleted || 0)
  }))
);

const chartReady = computed(() => numericTrend.value.length > 0);

const yMax = computed(() => {
  const values = numericTrend.value.flatMap((row) => [
    row.lostPublished,
    row.findPublished,
    row.claimApplied,
    row.claimCompleted
  ]);
  const max = values.length ? Math.max(...values) : 0;
  if (max <= 5) return 5;
  return Math.ceil(max / 5) * 5;
});

const xStep = computed(() => {
  const len = numericTrend.value.length;
  if (len <= 1) return chartWidth - padding.left - padding.right;
  return (chartWidth - padding.left - padding.right) / (len - 1);
});

const mapY = (value) => {
  const innerH = chartHeight - padding.top - padding.bottom;
  if (yMax.value <= 0) return padding.top + innerH;
  return padding.top + (1 - value / yMax.value) * innerH;
};

const pointsByKey = (key) =>
  numericTrend.value.map((row, idx) => ({
    x: padding.left + idx * xStep.value,
    y: mapY(row[key])
  }));

const toPointsString = (points) => points.map((p) => `${p.x},${p.y}`).join(' ');

const seriesPoints = computed(() => ({
  lost: toPointsString(pointsByKey('lostPublished')),
  find: toPointsString(pointsByKey('findPublished')),
  apply: toPointsString(pointsByKey('claimApplied')),
  complete: toPointsString(pointsByKey('claimCompleted'))
}));

const pointDots = computed(() => ({
  lost: pointsByKey('lostPublished'),
  find: pointsByKey('findPublished'),
  apply: pointsByKey('claimApplied'),
  complete: pointsByKey('claimCompleted')
}));

const gridLines = computed(() => {
  const step = Math.max(1, Math.floor(yMax.value / 4));
  const values = [0, step, step * 2, step * 3, yMax.value];
  const uniq = Array.from(new Set(values)).sort((a, b) => a - b);
  return uniq.map((v) => ({ value: v, y: mapY(v) }));
});

const xTicks = computed(() =>
  numericTrend.value.map((row, idx) => ({
    x: padding.left + idx * xStep.value,
    label: String(row.date || '').slice(5)
  }))
);

const loadOverview = async () => {
  const res = await getDashboardOverview();
  overview.value = res?.data || {};
};

const loadTrend = async () => {
  const res = await getDashboardTrend({ days: days.value });
  trendList.value = res?.data?.list || [];
};

onMounted(async () => {
  await loadOverview();
  await loadTrend();
});
</script>

<style scoped>
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.panel-header h3 {
  margin: 0;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.overview-card {
  border: 1px solid var(--color-border-secondary);
  border-radius: 10px;
  padding: 12px;
  background: #f8fafc;
}

.overview-card .label {
  display: block;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}

.overview-card strong {
  font-size: 24px;
  line-height: 1;
}

.trend-chart-card {
  border: 1px solid var(--color-border-secondary);
  border-radius: 10px;
  background: #fff;
  padding: 12px;
  min-height: 360px;
}

.trend-chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.trend-chart-header h4 {
  margin: 0;
  font-size: 15px;
}

.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--color-text-secondary);
  font-size: 12px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.dot.lost { background: #2563eb; }
.dot.find { background: #16a34a; }
.dot.apply { background: #f59e0b; }
.dot.complete { background: #ef4444; }

.trend-chart-body {
  width: 100%;
  height: 300px;
}

.trend-svg {
  width: 100%;
  height: 100%;
}

.grid-line {
  stroke: #e5e7eb;
  stroke-width: 1;
}

.line {
  fill: none;
  stroke-width: 2.2;
}

.line-lost { stroke: #2563eb; }
.line-find { stroke: #16a34a; }
.line-apply { stroke: #f59e0b; }
.line-complete { stroke: #ef4444; }

.dot-point {
  stroke: #ffffff;
  stroke-width: 1.5;
}

.dot-point.lost { fill: #2563eb; }
.dot-point.find { fill: #16a34a; }
.dot-point.apply { fill: #f59e0b; }
.dot-point.complete { fill: #ef4444; }

.axis-y-text,
.axis-x-text {
  fill: #6b7280;
  font-size: 11px;
}

@media (max-width: 992px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .trend-chart-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .trend-chart-card {
    padding: 10px;
    min-height: 330px;
  }

  .trend-chart-body {
    height: 270px;
  }
}
</style>
