<template>
  <div class="page">
    <div class="panel">
      <div class="toolbar">
        <el-select v-if="isHrUser" v-model="query.status" placeholder="全部状态" clearable style="width: 150px"
                   @change="search">
          <el-option label="待审批" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-select v-if="isHrUser" v-model="query.employeeId" placeholder="全部员工" clearable filterable
                   style="width: 200px" @change="search">
          <el-option v-for="employee in employees" :key="employee.id"
                     :label="employee.name + '（' + employee.empNo + '）'" :value="employee.id" />
        </el-select>
        <span v-if="isHrUser" class="text-muted">共 {{ total }} 条申请记录</span>
        <div style="flex: 1"></div>
        <el-button v-if="myEmployeeId" type="primary" :icon="Plus" @click="openApply">提交请假申请</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="empNo" label="工号" width="100" />
        <el-table-column prop="employeeName" label="申请人" width="100" />
        <el-table-column prop="departmentName" label="部门" width="110" />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="115" />
        <el-table-column prop="endDate" label="结束日期" width="115" />
        <el-table-column prop="days" label="天数" width="80" />
        <el-table-column prop="reason" label="请假事由" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type" size="small">{{ statusMeta(row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批信息" min-width="170">
          <template #default="{ row }">
            <div v-if="row.approverName">
              <div>{{ row.approverName }} · {{ row.status === 'APPROVED' ? '已通过' : '已驳回' }}</div>
              <div class="text-muted">{{ row.comment || '无审批意见' }}</div>
            </div>
            <span v-else class="text-muted">等待审批</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="isHrUser && row.status === 'PENDING'">
              <el-button link type="success" @click="approve(row, 'APPROVED')">通过</el-button>
              <el-button link type="danger" @click="approve(row, 'REJECTED')">驳回</el-button>
            </template>
            <template v-else-if="!isHrUser && row.status === 'PENDING'">
              <el-button link type="danger" @click="withdraw(row)">撤回</el-button>
            </template>
            <span v-else class="text-muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" background
                       layout="total, prev, pager, next" :total="total" @current-change="load" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="提交请假申请" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="请假类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="年假" value="年假" />
            <el-option label="事假" value="事假" />
            <el-option label="病假" value="病假" />
            <el-option label="调休" value="调休" />
          </el-select>
        </el-form-item>
        <el-form-item label="请假日期" prop="range">
          <el-date-picker v-model="form.range" type="daterange" value-format="YYYY-MM-DD"
                          range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"
                          style="width: 100%" />
        </el-form-item>
        <el-form-item label="请假天数">
          <el-tag effect="plain" type="primary">{{ computedDays }} 天</el-tag>
        </el-form-item>
        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请填写请假事由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../api'
import { getUser, isHr } from '../utils/auth'

const user = getUser()
const isHrUser = isHr()
const myEmployeeId = user.employeeId || null
const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const employees = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const query = reactive({ status: '', employeeId: null, page: 1, size: 10 })
const form = reactive({ type: '年假', range: [], reason: '' })

const rules = {
  type: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  range: [{ required: true, message: '请选择请假日期', trigger: 'change' }],
  reason: [{ required: true, message: '请填写请假事由', trigger: 'blur' }]
}

const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'danger' }
}

function statusMeta(status) {
  return statusMap[status] || { text: status || '-', type: 'info' }
}

const computedDays = computed(() => {
  if (!form.range || form.range.length !== 2) return 0
  const start = new Date(form.range[0])
  const end = new Date(form.range[1])
  return Math.round((end - start) / 86400000) + 1
})

async function load() {
  loading.value = true
  try {
    const data = await request.get('/leaves', {
      params: {
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

function openApply() {
  form.type = '年假'
  form.range = []
  form.reason = ''
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}

async function submit() {
  await formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      await request.post('/leaves', {
        type: form.type,
        startDate: form.range[0],
        endDate: form.range[1],
        reason: form.reason
      })
      ElMessage.success('申请已提交，请等待审批')
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function approve(row, status) {
  const action = status === 'APPROVED' ? '通过' : '驳回'
  try {
    const { value } = await ElMessageBox.prompt(`请输入${action}意见（可选）`, `${action}「${row.employeeName}」的请假申请`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '审批意见',
      inputValue: status === 'APPROVED' ? '同意' : ''
    })
    await request.post(`/leaves/${row.id}/approve`, { status, comment: value })
    ElMessage.success(`已${action}该申请`)
    load()
  } catch (e) {
    // 取消操作
  }
}

function withdraw(row) {
  ElMessageBox.confirm('确定撤回该请假申请吗？', '撤回确认', {
    type: 'warning',
    confirmButtonText: '撤回',
    cancelButtonText: '取消'
  }).then(async () => {
    await request.delete(`/leaves/${row.id}`)
    ElMessage.success('已撤回')
    load()
  }).catch(() => {})
}

onMounted(async () => {
  if (isHrUser) {
    employees.value = await request.get('/employees/all')
  }
  load()
})
</script>
