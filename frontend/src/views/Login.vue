<template>
  <div class="login-page">
    <div class="login-brand">
      <div class="brand-logo">HR</div>
      <h1>人事资源管理系统</h1>
      <p class="brand-sub">Personnel Resource Management System</p>
      <ul class="brand-features">
        <li>员工档案、部门岗位一体化管理</li>
        <li>考勤打卡与请假审批全流程线上化</li>
        <li>薪资核算发放与组织数据看板</li>
      </ul>
      <div class="brand-footer">Spring Boot + Vue 3 全栈实现</div>
    </div>

    <div class="login-panel">
      <div class="login-card">
        <h2>欢迎登录</h2>
        <p class="text-muted login-tip">请使用系统账号进入人事工作台</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" size="large" placeholder="用户名" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" size="large" type="password" placeholder="密码"
                      :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="submit">
            登 录
          </el-button>
        </el-form>

        <div class="demo-accounts">
          <span class="text-muted">演示账号，点击快速填入</span>
          <div class="demo-list">
            <el-tag v-for="account in demoAccounts" :key="account.username" class="demo-tag"
                    effect="plain" @click="fill(account)">
              {{ account.label }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import request from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const demoAccounts = [
  { label: '管理员 admin / admin123', username: 'admin', password: 'admin123' },
  { label: '人事 hr / hr123', username: 'hr', password: 'hr123' },
  { label: '员工 emp001 / 123456', username: 'emp001', password: '123456' }
]

function fill(account) {
  form.username = account.username
  form.password = account.password
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      const data = await request.post('/auth/login', form)
      localStorage.setItem('hrms_token', data.token)
      localStorage.setItem('hrms_user', JSON.stringify({
        id: data.id,
        username: data.username,
        realName: data.realName,
        role: data.role,
        employeeId: data.employeeId
      }))
      ElMessage.success('登录成功，欢迎回来')
      router.push('/dashboard')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: #f2f4f8;
}

.login-brand {
  flex: 1.15;
  padding: 72px 80px;
  color: #fff;
  background: linear-gradient(140deg, #101a3c 0%, #1e3a8a 55%, #2b5cff 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-brand::after {
  content: '';
  position: absolute;
  width: 520px;
  height: 520px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  right: -160px;
  bottom: -180px;
}

.brand-logo {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 32px;
}

.login-brand h1 {
  font-size: 38px;
  margin: 0 0 12px;
  letter-spacing: 2px;
}

.brand-sub {
  margin: 0 0 40px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.62);
  font-size: 14px;
}

.brand-features {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.88);
}

.brand-features li::before {
  content: '';
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #7ea2ff;
  margin-right: 12px;
  vertical-align: middle;
}

.brand-footer {
  margin-top: 64px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
}

.login-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px 32px;
  box-shadow: 0 18px 50px rgba(16, 26, 60, 0.12);
}

.login-card h2 {
  margin: 0 0 8px;
  font-size: 24px;
}

.login-tip {
  margin: 0 0 24px;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
  height: 46px;
  font-size: 16px;
  letter-spacing: 4px;
}

.demo-accounts {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px dashed #e4e8f0;
}

.demo-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.demo-tag {
  cursor: pointer;
  justify-content: center;
}

@media (max-width: 900px) {
  .login-brand {
    display: none;
  }
}
</style>
