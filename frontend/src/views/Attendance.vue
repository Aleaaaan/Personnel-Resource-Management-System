<template>
  <div class="page">
    <div v-if="myEmployeeId" class="panel clock-panel">
      <div class="clock-left">
        <div>
          <div class="clock-title">今日考勤</div>
          <div class="clock-date">{{ longToday }}</div>
        </div>
        <div class="clock-actions">
          <el-button type="primary" size="large" :disabled="!!today.checkIn" @click="checkIn">
            {{ today.checkIn ? '上班已打卡 ' + hm(today.checkIn) : '上班打卡' }}
          </el-button>
          <el-button type="success" size="large" plain :disabled="!today.checkIn || !!today.checkOut" @click="checkOut">
            {{ today.checkOut ? '下班已签退 ' + hm(today.checkOut) : '下班签退' }}
          </el-button>
        </div>
      </div>
      <div class="clock-stats">
        <div class="mini-stat">
          <span>正常</span>
          <b>{{ stats.normal || 0 }}</b>
        </div>
        <div class="mini-stat">
          <span>迟到</span>
          <b class="warn">{{ stats.late || 0 }}</b>
        </div>
        <div class="mini-stat">
          <span>早退</span>
          <b class="warn">{{ stats.earlyLeave || 0 }}</b>
        </div>
        <div class="mini-stat">
          <span>请假</span>
          <b>{{ stats.leave || 0 }}</b>
        </div>
        <div class="mini-stat">
          <span>本月记录</span>
          <b>{{ stats.total || 0 }}</b>
        </div>
      </div>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-date-picker v-model="query.month" type="month" value-format="YYYY-MM" placeholder="选择月份"
                        style="width: 150px" @change="onMonthChange" />
        <el-select v-if="isHrUser" v-model="query.employeeId" placeholder="全部员工" clearable filterable
                   style="width: 200px" @change="onFilterChange">
          <el-option v-for="employee in employees" :key="employee.id"
                     :label="employee.name + '（' + employee.empNo + '）'" :value="employee.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="search">
          <el-option label="正常" value="NORMAL" />
          <el-option label="迟到" value="LATE" />
          <el-option label="早退" value="EARLY_LEAVE" />
          <el-option label="缺勤" value="ABSENT" />
          <el-option label="请假" value="LEAVE" />
        </el-select>
        <el-button :icon="Refresh" @click="reset">重置</el-button>
        <span class="text-muted">上班时间 09:00 · 下班时间 18:00</span>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="workDate" label="日期" width="130" />
        <el-table-column prop="empNo" label="工号" width="110" />
        <el-table-column prop="employeeName" label="姓名" width="110" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column label="上班打卡" width="120">
          <template #default="{ row }">{{ hm(row.checkIn) || '-' }}</template>
        </el-table-column>
        <el-table-column label="下班签退" width="120">
          <template #default="{ row }">{{ hm(row.checkOut) || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type" size="small">{{ statusMeta(row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" background
                       layout="total, prev, pager, next" :total="total" :page-sizes="[10, 20, 50]"
                       @current-change="load" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from '../api'
import { getUser, isHr } from '../utils/auth'

const user = getUser()
const myEmployeeId = user?.employeeId || null
const isHrUser = isHr()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const employees = ref([])
const stats = ref({})
const today = ref({})

const now = new Date()
const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
const query = reactive({ month: currentMonth, status: '', employeeId: null, page: 1, size: 10 })

const longToday = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

const statusMap = {
  NORMAL: { text: '正常', type: 'success' },
  LATE: { text: '迟到', type: 'warning' },
  EARLY_LEAVE: { text: '早退', type: 'info' },
  ABSENT: { text: '缺勤', type: 'danger' },
  LEAVE: { text: '请假', type: 'info' }
}

function statusMeta(status) {
  return statusMap[status] || { text: status || '-', type: 'info' }
}

function hm(value) {
  return value ? String(value).slice(0, 5) : ''
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/attendances', {
      params: {
        month: query.month || undefined,
        status: query.status || undefined,
        employeeId: query.employeeId || undefined,
        page: query.page,
        size: query.size
      }
    })
    list.value = data.content
    total.value = data.totalElements
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  stats.value = await request.get('/attendances/stats', {
    params: { month: query.month || undefined, employeeId: query.employeeId || undefined }
  })
}

async function loadToday() {
  if (!myEmployeeId) return
  const data = await request.get('/attendances/today')
  today.value = data || {}
}

function onMonthChange() {
  query.page = 1
  load()
  loadStats()
}

function onFilterChange() {
  query.page = 1
  load()
  loadStats()
}

function search() {
  query.page = 1
  load()
}

function reset() {
  query.month = currentMonth
  query.status = ''
  query.employeeId = null
  query.page = 1
  load()
  loadStats()
}

async function checkIn() {
  await request.post('/attendances/check-in')
  ElMessage.success('上班打卡成功')
  loadToday()
  load()
  loadStats()
}

async function checkOut() {
  await request.post('/attendances/check-out')
  ElMessage.success('下班签退成功')
  loadToday()
  load()
  loadStats()
}

onMounted(async () => {
  if (isHrUser) {
    employees.value = await request.get('/employees/all')
  }
  load()
  loadStats()
  loadToday()
})
</script>

<style scoped>
.clock-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  background: linear-gradient(120deg, #1e3a8a, #2b5cff);
  color: #fff;
}

.clock-left {
  display: flex;
  align-items: center;
  gap: 32px;
  flex-wrap: wrap;
}

.clock-title {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 1px;
}

.clock-date {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.clock-actions {
  display: flex;
  gap: 12px;
}

.clock-actions :deep(.el-button) {
  border: none;
}

.clock-stats {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
}

.mini-stat {
  min-width: 72px;
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  text-align: center;
}

.mini-stat span {
  display: block;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.mini-stat b {
  font-size: 20px;
}

.mini-stat b.warn {
  color: #ffd48a;
}
</style>
