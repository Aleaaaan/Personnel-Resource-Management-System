import { createRouter, createWebHashHistory } from 'vue-router'
import { getUser } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '数据看板' }
      },
      {
        path: 'employees',
        name: 'employees',
        component: () => import('../views/Employees.vue'),
        meta: { title: '员工管理', roles: ['ADMIN', 'HR'] }
      },
      {
        path: 'departments',
        name: 'departments',
        component: () => import('../views/Departments.vue'),
        meta: { title: '部门管理', roles: ['ADMIN', 'HR'] }
      },
      {
        path: 'positions',
        name: 'positions',
        component: () => import('../views/Positions.vue'),
        meta: { title: '岗位管理', roles: ['ADMIN', 'HR'] }
      },
      {
        path: 'attendance',
        name: 'attendance',
        component: () => import('../views/Attendance.vue'),
        meta: { title: '考勤管理' }
      },
      {
        path: 'leaves',
        name: 'leaves',
        component: () => import('../views/Leaves.vue'),
        meta: { title: '请假审批' }
      },
      {
        path: 'salaries',
        name: 'salaries',
        component: () => import('../views/Salaries.vue'),
        meta: { title: '薪资管理' }
      },
      {
        path: 'announcements',
        name: 'announcements',
        component: () => import('../views/Announcements.vue'),
        meta: { title: '公告通知' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(to => {
  const token = localStorage.getItem('hrms_token')
  if (!to.meta.public && !token) {
    return { path: '/login' }
  }
  if (to.path === '/login' && token) {
    return { path: '/dashboard' }
  }
  const user = getUser()
  if (to.meta.roles && user && !to.meta.roles.includes(user.role)) {
    return { path: '/dashboard' }
  }
  return true
})

export default router
