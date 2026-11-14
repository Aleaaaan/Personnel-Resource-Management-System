<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-mark">HR</span>
        <div class="logo-text">
          <strong>人事资源管理</strong>
          <small>Personnel System</small>
        </div>
      </div>
      <el-menu :default-active="activeMenu" router class="menu" background-color="transparent"
               text-color="#aab4cc" active-text-color="#ffffff">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-footer">v1.0.0</div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h2>{{ currentTitle }}</h2>
          <span class="text-muted">今天是 {{ today }}</span>
        </div>
        <div class="header-right">
          <el-tag :type="roleTagType" effect="light" round>{{ roleText }}</el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="34" class="avatar">{{ (user?.realName || 'U').slice(0, 1) }}</el-avatar>
              <span class="user-name">{{ user?.realName || user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>账号：{{ user?.username }}</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { getUser, isHr, roleLabels } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const user = getUser()

const menus = computed(() => {
  const list = [{ path: '/dashboard', title: '数据看板', icon: 'Odometer' }]
  if (isHr()) {
    list.push(
      { path: '/employees', title: '员工管理', icon: 'User' },
      { path: '/departments', title: '部门管理', icon: 'OfficeBuilding' },
      { path: '/positions', title: '岗位管理', icon: 'Postcard' }
    )
  }
  list.push(
    { path: '/attendance', title: '考勤管理', icon: 'Clock' },
    { path: '/leaves', title: isHr() ? '请假审批' : '我的请假', icon: 'Calendar' },
    { path: '/salaries', title: isHr() ? '薪资管理' : '我的薪资', icon: 'Money' },
    { path: '/announcements', title: '公告通知', icon: 'Bell' }
  )
  return list
})

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '工作台')
const roleText = computed(() => roleLabels[user?.role] || '用户')
const roleTagType = computed(() => (user?.role === 'ADMIN' ? 'danger' : user?.role === 'HR' ? 'warning' : 'success'))
const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

function handleCommand(command) {
  if (command !== 'logout') return
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('hrms_token')
    localStorage.removeItem('hrms_user')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #101a3c;
  display: flex;
  flex-direction: column;
  color: #fff;
}

.logo {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-mark {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #2b5cff, #6d8dff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.logo-text strong {
  font-size: 15px;
  letter-spacing: 1px;
}

.logo-text small {
  color: rgba(255, 255, 255, 0.42);
  font-size: 11px;
}

.menu {
  border-right: none;
  padding: 14px 10px;
  flex: 1;
}

.menu :deep(.el-menu-item) {
  border-radius: 8px;
  height: 46px;
  margin-bottom: 6px;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
}

.menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #2b5cff, #4a76ff);
  font-weight: 600;
}

.aside-footer {
  padding: 16px 20px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
}

.header {
  height: 72px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 8px rgba(23, 43, 99, 0.06);
  z-index: 5;
}

.header-left h2 {
  margin: 0 0 2px;
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  outline: none;
}

.avatar {
  background: linear-gradient(135deg, #2b5cff, #6d8dff);
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: #1f2d3d;
}

.main {
  background: #f2f4f8;
  padding: 20px 24px 32px;
  overflow-y: auto;
}
</style>
