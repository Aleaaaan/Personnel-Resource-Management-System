import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('hrms_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => response.data.data,
  error => {
    const status = error.response && error.response.status
    const message = (error.response && error.response.data && error.response.data.message) || '网络请求失败'
    if (status === 401) {
      localStorage.removeItem('hrms_token')
      localStorage.removeItem('hrms_user')
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
