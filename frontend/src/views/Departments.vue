<template>
  <div class="page">
    <div class="panel">
      <div class="panel-header">
        <span class="panel-title">部门列表</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增部门</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="name" label="部门名称" width="160" />
        <el-table-column prop="code" label="部门编码" width="130" />
        <el-table-column prop="managerName" label="负责人" width="120" />
        <el-table-column label="员工人数" width="110">
          <template #default="{ row }">
            <el-tag effect="plain" type="primary">{{ row.employeeCount || 0 }} 人</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="职责说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑部门' : '新增部门'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码">
          <el-input v-model="form.code" placeholder="如 TECH" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.managerName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="职责说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入部门职责说明" />
        </el-form-item>
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
import { Plus } from '@element-plus/icons-vue'
import request from '../api'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const emptyForm = () => ({ name: '', code: '', managerName: '', description: '' })
const form = reactive(emptyForm())
const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

async function load() {
  loading.value = true
  try {
    list.value = await request.get('/departments')
  } finally {
    loading.value = false
  }
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
    name: row.name,
    code: row.code,
    managerName: row.managerName,
    description: row.description
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
        await request.put(`/departments/${editingId.value}`, form)
        ElMessage.success('部门信息已更新')
      } else {
        await request.post('/departments', form)
        ElMessage.success('部门创建成功')
      }
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

function remove(row) {
  ElMessageBox.confirm(`确定删除部门「${row.name}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await request.delete(`/departments/${row.id}`)
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>
