export function getUser() {
  try {
    return JSON.parse(localStorage.getItem('hrms_user') || 'null')
  } catch (e) {
    return null
  }
}

export function hasRole(...roles) {
  const user = getUser()
  return !!user && roles.includes(user.role)
}

export function isHr() {
  return hasRole('ADMIN', 'HR')
}

export const roleLabels = {
  ADMIN: '系统管理员',
  HR: '人事专员',
  EMPLOYEE: '普通员工'
}
