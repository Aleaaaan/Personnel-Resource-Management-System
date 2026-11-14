<template>
  <div class="page">
    <div class="panel">
      <div class="panel-header">
        <div>
          <span class="panel-title">公司公告</span>
          <span class="text-muted" style="margin-left: 12px">共 {{ list.length }} 条</span>
        </div>
        <el-button v-if="isHrUser" type="primary" :icon="Plus" @click="openCreate">发布公告</el-button>
      </div>
      <el-empty v-if="!loading && !list.length" description="暂无公告" />
      <div v-else class="notice-grid" v-loading="loading">
        <div v-for="notice in list" :key="notice.id" class="notice-card">
          <div class="notice-top">
            <el-tag size="small" effect="plain" type="primary">公告</el-tag>
            <div v-if="isHrUser" class="notice-actions">
              <el-button link type="primary" @click="openEdit(notice)">编辑</el-button>
              <el-button link type="danger" @click="remove(notice)">删除</el-button>
            </div>
          </div>
          <h3>{{ notice.title }}</h3>
          <p>{{ notice.content }}</p>
          <div class="notice-footer">
            <span>{{ notice.publisher }}</span>
            <span>{{ formatTime(notice.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑公告' : '发布公告'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
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
import { isHr } from '../utils/auth'

const isHrUser = isHr()
const loading = ref(false)
const saving = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({ title: '', content: '' })
const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

async function load() {
  loading.value = true
  try {
    list.value = await request.get('/announcements')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.title = ''
  form.content = ''
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}

function openEdit(notice) {
  editingId.value = notice.id
  form.title = notice.title
  form.content = notice.content
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}

async function save() {
  await formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      if (editingId.value) {
        await request.put(`/announcements/${editingId.value}`, form)
        ElMessage.success('公告已更新')
      } else {
        await request.post('/announcements', form)
        ElMessage.success('公告已发布')
      }
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

function remove(notice) {
  ElMessageBox.confirm(`确定删除公告「${notice.title}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await request.delete(`/announcements/${notice.id}`)
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.notice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.notice-card {
  border: 1px solid #edf0f6;
  border-radius: 10px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s, transform 0.2s;
}

.notice-card:hover {
  box-shadow: 0 8px 24px rgba(23, 43, 99, 0.08);
  transform: translateY(-2px);
}

.notice-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.notice-card h3 {
  margin: 12px 0 8px;
  font-size: 15px;
  line-height: 1.5;
}

.notice-card p {
  margin: 0;
  color: #667085;
  font-size: 13px;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.notice-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #edf0f6;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #9aa4b5;
}
</style>
