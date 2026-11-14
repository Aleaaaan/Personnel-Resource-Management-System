<template>
  <div class="page">
    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索姓名 / 工号 / 手机号" clearable style="width: 230px"
                  @keyup.enter="search" @clear="search">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="query.departmentId" placeholder="全部部门" clearable style="width: 160px" @change="search">
          <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="search">
          <el-option label="在职" value="ACTIVE" />
          <el-option label="离职" value="RESIGNED" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="Refresh" @click="reset">重置</el-button>
        <div style="flex: 1"></div>
        <el-button v-if="canEdit" type="primary" :icon="Plus" @click="openCreate">新增员工</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="empNo" label="工号" width="100" />
        <el-table-column label="姓名" width="120">
          <template #default="{ row }">
            <div class="emp-name">
              <el-avatar :size="26" class="emp-avatar">{{ (row.name || '?').slice(0, 1) }}</el-avatar>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="departmentName" label="部门" width="110" />
        <el-table-column prop="positionName" label="岗位" width="140" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="hireDate" label="入职日期" width="115" />
        <el-table-column label="基本工资" width="120">
          <template #default="{ row }">¥ {{ formatMoney(row.baseSalary) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canEdit" label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" background
                       layout="total, prev, pager, next, sizes" :total="total" :page-sizes="[10, 20, 50]"
                       @current-change="load" @size-change="search" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑员工' : '新增员工'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工号" prop="empNo">
              <el-input v-model="form.empNo" placeholder="如 EMP013" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD"
                              placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" prop="departmentId">
              <el-select v-model="form.departmentId" placeholder="请选择部门" style="width: 100%">
                <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="positionId">
              <el-select v-model="form.positionId" placeholder="请选择岗位" style="width: 100%">
                <el-option v-for="position in positions" :key="position.id" :label="position.name" :value="position.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD"
                              placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="在职" value="ACTIVE" />
                <el-option label="离职" value="RESIGNED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基本工资">
              <el-input-number v-model="form.baseSalary" :min="0" :step="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="form.education" style="width: 100%">
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="家庭住址">
              <el-input v-model="form.address" placeholder="请输入家庭住址" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import request from '../api'
import { isHr } from '../utils/auth'

const canEdit = isHr()
const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const departments = ref([])
const positions = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const query = reactive({ keyword: '', departmentId: null, status: '', page: 1, size: 10 })

const emptyForm = () => ({
  empNo: '',
  name: '',
  gender: '男',
  birthday: '',
  phone: '',
  email: '',
  departmentId: null,
  positionId: null,
  hireDate: '',
  status: 'ACTIVE',
  baseSalary: 8000,
  education: '本科',
  address: ''
})

const form = reactive(emptyForm())

const rules = {
  empNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  positionId: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }]
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 0 })
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/employees', {
      params: {
        keyword: query.keyword || undefined,
        departmentId: query.departmentId || undefined,
        status: query.status || undefined,
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

function reset() {
  query.keyword = ''
  query.departmentId = null
  query.status = ''
  search()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, emptyForm(), {
    empNo: row.empNo,
    name: row.name,
    gender: row.gender,
    birthday: row.birthday,
    phone: row.phone,
    email: row.email,
    departmentId: row.departmentId,
    positionId: row.positionId,
    hireDate: row.hireDate,
    status: row.status,
    baseSalary: Number(row.baseSalary || 0),
    education: row.education,
    address: row.address
  })
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}

async function save() {
  await formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      if (editingId.value) {
        await request.put(`/employees/${editingId.value}`, form)
        ElMessage.success('员工信息已更新')
      } else {
        await request.post('/employees', form)
        ElMessage.success('员工创建成功')
      }
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

function remove(row) {
  ElMessageBox.confirm(`确定删除员工「${row.name}」吗？删除后不可恢复。`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await request.delete(`/employees/${row.id}`)
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}

onMounted(async () => {
  const [deptData, positionData] = await Promise.all([
    request.get('/departments'),
    request.get('/positions')
  ])
  departments.value = deptData
  positions.value = positionData
  load()
})
</script>

<style scoped>
.emp-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.emp-avatar {
  background: #e8efff;
  color: #2b5cff;
  font-size: 12px;
  font-weight: 600;
}
</style>
