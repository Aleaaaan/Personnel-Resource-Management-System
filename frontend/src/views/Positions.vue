<template>
  <div class="page">
    <div class="panel">
      <div class="panel-header">
        <span class="panel-title">岗位列表</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增岗位</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="name" label="岗位名称" width="200" />
        <el-table-column prop="level" label="职级" width="120">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.level || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="岗位职责" min-width="260" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑岗位' : '新增岗位'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="岗位名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="职级">
          <el-input v-model="form.level" placeholder="如 P5、P6" />
        </el-form-item>
        <el-form-item label="岗位职责">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入岗位职责" />
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

const emptyForm = () => ({ name: '', level: '', description: '' })
const form = reactive(emptyForm())
const rules = {
  name: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }]
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

async function load() {
  loading.value = true
  try {
    list.value = await request.get('/positions')
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
    level: row.level,
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
        await request.put(`/positions/${editingId.value}`, form)
        ElMessage.success('岗位信息已更新')
      } else {
        await request.post('/positions', form)
        ElMessage.success('岗位创建成功')
      }
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

function remove(row) {
  ElMessageBox.confirm(`确定删除岗位「${row.name}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await request.delete(`/positions/${row.id}`)
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>
