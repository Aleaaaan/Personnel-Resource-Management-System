<template>
  <div class="page">
    <div class="stat-grid">
      <div v-for="item in statCards" :key="item.label" class="stat-card">
        <div class="stat-icon" :style="{ background: item.bg, color: item.color }">
          <el-icon :size="22"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <div class="chart-grid">
      <div class="panel">
        <div class="panel-header">
          <span class="panel-title">部门人员分布</span>
          <span class="text-muted">仅统计在职员工</span>
        </div>
        <div ref="deptRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-header">
          <span class="panel-title">员工在职状态</span>
        </div>
        <div ref="statusRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-header">
          <span class="panel-title">近半年薪资支出</span>
          <span class="text-muted">单位：元</span>
        </div>
        <div ref="salaryRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-header">
          <span class="panel-title">本月考勤情况</span>
        </div>
        <div ref="attendanceRef" class="chart"></div>
      </div>
    </div>

    <div class="panel">
      <div class="panel-header">
        <span class="panel-title">最新公告</span>
        <el-link type="primary" @click="$router.push('/announcements')">查看全部</el-link>
      </div>
      <div v-if="!announcements.length" class="text-muted">暂无公告</div>
      <div v-for="notice in announcements" :key="notice.id" class="notice">
        <div class="notice-dot"></div>
        <div>
          <div class="notice-title">{{ notice.title }}</div>
          <div class="notice-meta">{{ notice.publisher }} · {{ formatTime(notice.createdAt) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import request from '../api'

const overview = ref({})
const announcements = ref([])
const deptRef = ref(null)
const statusRef = ref(null)
const salaryRef = ref(null)
const attendanceRef = ref(null)
let charts = []

const statCards = computed(() => [
  { label: '员工总数', value: overview.value.employeeCount || 0, icon: 'UserFilled', bg: '#e8efff', color: '#2b5cff' },
  { label: '在职员工', value: overview.value.activeCount || 0, icon: 'User', bg: '#e6f7f1', color: '#12b76a' },
  { label: '部门数量', value: overview.value.departmentCount || 0, icon: 'OfficeBuilding', bg: '#fff4e5', color: '#f79009' },
  { label: '待审批请假', value: overview.value.pendingLeaveCount || 0, icon: 'Calendar', bg: '#fde8ef', color: '#e74694' }
])

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

function money(value) {
  return '¥' + Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

function pie(el, data, colors) {
  const chart = echarts.init(el)
  chart.setOption({
    color: colors,
    tooltip: { trigger: 'item', formatter: '{b}：{c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, textStyle: { color: '#5b6678' } },
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      label: { show: false },
      emphasis: { label: { show: true, fontWeight: 600, fontSize: 13 } },
      data
    }]
  })
  charts.push(chart)
  return chart
}

function renderCharts() {
  const data = overview.value
  if (!data.deptDistribution) return

  pie(deptRef.value, data.deptDistribution, ['#2b5cff', '#4c8dff', '#7aa7ff', '#12b76a', '#f79009', '#e74694'])
  pie(statusRef.value, data.statusDistribution, ['#12b76a', '#c3cad9'])

  const trend = data.salaryTrend || { months: [], values: [] }
  const salaryChart = echarts.init(salaryRef.value)
  salaryChart.setOption({
    grid: { left: 10, right: 20, top: 24, bottom: 6, containLabel: true },
    tooltip: { trigger: 'axis', valueFormatter: value => money(value) },
    xAxis: {
      type: 'category',
      data: trend.months,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e3e8f1' } },
      axisLabel: { color: '#7c8798' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eef1f6' } },
      axisLabel: { color: '#7c8798', formatter: value => (value >= 10000 ? value / 10000 + '万' : value) }
    },
    series: [{
      type: 'bar',
      barWidth: 26,
      data: trend.values,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#2b5cff' },
          { offset: 1, color: '#9dbcff' }
        ])
      }
    }]
  })
  charts.push(salaryChart)

  const attendance = data.attendanceStats || { labels: [], values: [] }
  const attendanceChart = echarts.init(attendanceRef.value)
  attendanceChart.setOption({
    grid: { left: 10, right: 24, top: 20, bottom: 6, containLabel: true },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: {
      type: 'category',
      data: attendance.labels,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e3e8f1' } },
      axisLabel: { color: '#7c8798' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eef1f6' } },
      axisLabel: { color: '#7c8798' }
    },
    series: [{
      type: 'bar',
      barWidth: 30,
      data: attendance.values,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#12b76a' },
          { offset: 1, color: '#8fe0bd' }
        ])
      }
    }]
  })
  charts.push(attendanceChart)
}

function resize() {
  charts.forEach(chart => chart.resize())
}

async function load() {
  const [data, notices] = await Promise.all([
    request.get('/dashboard/overview'),
    request.get('/announcements')
  ])
  overview.value = data
  announcements.value = (notices || []).slice(0, 3)
  await nextTick()
  renderCharts()
}

onMounted(() => {
  load()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  charts.forEach(chart => chart.dispose())
  charts = []
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(23, 43, 99, 0.06);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  margin-top: 4px;
  color: #8b95a7;
  font-size: 13px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 16px;
}

.chart {
  height: 280px;
}

.notice {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f2f4f8;
}

.notice:last-child {
  border-bottom: none;
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2b5cff;
  margin-top: 7px;
  flex-shrink: 0;
}

.notice-title {
  font-size: 14px;
  color: #1f2d3d;
}

.notice-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #9aa4b5;
}
</style>
