<template>
  <div class="page">
    <div class="panel">
      <div class="toolbar">
        <el-date-picker v-model="query.month" type="month" value-format="YYYY-MM" placeholder="选择月份"
                        style="width: 150px" @change="search" />
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="search">
          <el-option label="待发放" value="DRAFT" />
          <el-option label="已发放" value="PAID" />
        </el-select>
        <el-select v-if="isHrUser" v-model="query.employeeId" placeholder="全部员工" clearable filterable
                   style="width: 200px" @change="search">
          <el-option v-for="employee in employees" :key="employee.id"
                     :label="employee.name + '（' + employee.empNo + '）'" :value="employee.id" />
        </el-select>
        <div style="flex: 1"></div>
        <template v-if="isHrUser">
          <el-button type="primary" plain :loading="generating" @click="generate">生成工资单</el-button>
          <el-button type="success" :loading="paying" @click="payBatch">批量发放</el-button>
        </template>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="employeeName" label="员工" width="100" />
        <el-table-column prop="empNo" label="工号" width="100" />
        <el-table-column prop="departmentName" label="部门" width="110" />
        <el-table-column prop="salaryMonth" label="月份" width="95" />
        <el-table-column label="基本工资" width="110" align="right">
          <template #default="{ row }">{{ money(row.baseSalary) }}</template>
        </el-table-column>
        <el-table-column label="奖金" width="100" align="right">
          <template #default="{ row }">{{ money(row.bonus) }}</template>
        </el-table-column>
        <el-table-column label="补贴" width="100" align="right">
          <template #default="{ row }">{{ money(row.allowance) }}</template>
        </el-table-column>
        <el-table-column label="社保" width="100" align="right">
          <template #default="{ row }">{{ money(row.socialSecurity) }}</template>
        </el-table-column>
        <el-table-column label="个税" width="100" align="right">
          <template #default="{ row }">{{ money(row.tax) }}</template>
        </el-table-column>
        <el-table-column label="实发工资" width="120" align="right">
          <template #default="{ row }">
            <span class="net-salary">{{ money(row.netSalary) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PAID' ? 'success' : 'warning'" size="small">
              {{ row.status === 'PAID' ? '已发放' : '待发放' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">明细</el-button>
            <template v-if="isHrUser && row.status !== 'PAID'">
              <el-button link type="primary" @click="openAdjust(row)">调整</el-button>
              <el-button link type="success" @click="pay(row)">发放</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" background
                       layout="total, prev, pager, next" :total="total" @current-change="load" />
      </div>
    </div>

    <el-dialog v-model="adjustVisible" title="调整工资明细" width="480px">
      <div class="adjust-head" v-if="current">
        <span>{{ current.employeeName }} · {{ current.salaryMonth }}</span>
        <span class="text-muted">基本工资 {{ money(current.baseSalary) }}</span>
      </div>
      <el-form label-width="90px">
        <el-form-item label="奖金">
          <el-input-number v-model="adjust.bonus" :min="0" :step="500" style="width: 100%" />
        </el-form-item>
        <el-form-item label="补贴">
          <el-input-number v-model="adjust.allowance" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="扣款">
          <el-input-number v-model="adjust.deduction" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预计实发">
          <span class="net-salary">{{ money(previewNet) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveAdjust">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="工资条明细" width="560px">
      <template v-if="current">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="员工">{{ current.employeeName }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ current.empNo }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ current.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="月份">{{ current.salaryMonth }}</el-descriptions-item>
          <el-descriptions-item label="基本工资">{{ money(current.baseSalary) }}</el-descriptions-item>
          <el-descriptions-item label="奖金">{{ money(current.bonus) }}</el-descriptions-item>
          <el-descriptions-item label="补贴">{{ money(current.allowance) }}</el-descriptions-item>
          <el-descriptions-item label="扣款">{{ money(current.deduction) }}</el-descriptions-item>
          <el-descriptions-item label="社保">{{ money(current.socialSecurity) }}</el-descriptions-item>
          <el-descriptions-item label="个税">{{ money(current.tax) }}</el-descriptions-item>
          <el-descriptions-item label="实发工资">
            <span class="net-salary">{{ money(current.netSalary) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="发放状态">
            <el-tag :type="current.status === 'PAID' ? 'success' : 'warning'" size="small">
              {{ current.status === 'PAID' ? '已发放' : '待发放' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发放日期" :span="2">{{ current.payDate || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api'
import { isHr } from '../utils/auth'

const isHrUser = isHr()
const loading = ref(false)
const generating = ref(false)
const paying = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const employees = ref([])
const adjustVisible = ref(false)
const detailVisible = ref(false)
const current = ref(null)

const now = new Date()
const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
const query = reactive({ month: currentMonth, status: '', employeeId: null, page: 1, size: 10 })
const adjust = reactive({ bonus: 0, allowance: 0, deduction: 0 })

const previewNet = computed(() => {
  if (!current.value) return 0
  const base = Number(current.value.baseSalary || 0)
  const social = Number(current.value.socialSecurity || 0)
  const tax = Number(current.value.tax || 0)
  return (base + Number(adjust.bonus || 0) + Number(adjust.allowance || 0)
    - Number(adjust.deduction || 0) - social - tax).toFixed(2)
})

function money(value) {
  return '¥ ' + Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/salaries', {
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

function search() {
  query.page = 1
  load()
}

async function generate() {
  if (!query.month) {
    ElMessage.warning('请先选择薪资月份')
    return
  }
  generating.value = true
  try {
    const result = await request.post('/salaries/generate', null, { params: { month: query.month } })
    ElMessage.success(`已生成 ${result.created} 条工资单，跳过 ${result.skipped} 条已存在记录`)
    load()
  } finally {
    generating.value = false
  }
}

function pay(row) {
  ElMessageBox.confirm(`确定发放「${row.employeeName}」${row.salaryMonth} 的工资吗？`, '发放确认', {
    type: 'warning',
    confirmButtonText: '确认发放',
    cancelButtonText: '取消'
  }).then(async () => {
    await request.post(`/salaries/${row.id}/pay`)
    ElMessage.success('工资已发放')
    load()
  }).catch(() => {})
}

function payBatch() {
  if (!query.month) {
    ElMessage.warning('请先选择薪资月份')
    return
  }
  ElMessageBox.confirm(`确定批量发放 ${query.month} 的所有待发放工资吗？`, '批量发放', {
    type: 'warning',
    confirmButtonText: '确认发放',
    cancelButtonText: '取消'
  }).then(async () => {
    paying.value = true
    try {
      const result = await request.post('/salaries/pay-batch', null, { params: { month: query.month } })
      ElMessage.success(`已发放 ${result.paid} 条工资记录`)
      load()
    } finally {
      paying.value = false
    }
  }).catch(() => {})
}

function openAdjust(row) {
  current.value = row
  adjust.bonus = Number(row.bonus || 0)
  adjust.allowance = Number(row.allowance || 0)
  adjust.deduction = Number(row.deduction || 0)
  adjustVisible.value = true
}

async function saveAdjust() {
  saving.value = true
  try {
    await request.put(`/salaries/${current.value.id}`, {
      bonus: adjust.bonus,
      allowance: adjust.allowance,
      deduction: adjust.deduction
    })
    ElMessage.success('工资明细已更新')
    adjustVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openDetail(row) {
  current.value = row
  detailVisible.value = true
}

onMounted(async () => {
  if (isHrUser) {
    employees.value = await request.get('/employees/all')
  }
  load()
})
</script>

<style scoped>
.net-salary {
  color: #2b5cff;
  font-weight: 700;
}

.adjust-head {
  display: flex;
  justify-content: space-between;
  padding: 10px 14px;
  margin-bottom: 18px;
  background: #f5f8ff;
  border-radius: 8px;
  font-size: 14px;
}
</style>
