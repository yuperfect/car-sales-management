/**
 * 用户身份管理工具
 * 使用 localStorage 存储当前用户的身份信息
 * 不再使用密码登录机制，仅通过用户名切换身份
 */

const STORAGE_KEY = 'car_sales_user'

/**
 * 获取当前用户信息
 * @returns {{ customerId: number, username: string, realName: string, phone: string } | null}
 */
export function getCurrentUser() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

/**
 * 保存用户身份到 localStorage
 * @param {{ customerId: number, username?: string, realName: string, phone: string }} user
 */
export function setCurrentUser(user) {
  if (!user || !user.customerId) {
    console.warn('setCurrentUser: 缺少 customerId')
    return
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify({
    customerId: user.customerId,
    username: user.username || '',
    realName: user.realName || '',
    phone: user.phone || ''
  }))
}

/**
 * 判断用户是否已选择身份
 * @returns {boolean}
 */
export function isIdentified() {
  const user = getCurrentUser()
  return !!(user && user.customerId)
}

/**
 * @deprecated 保留兼容，请使用 isIdentified()
 */
export function isLoggedIn() {
  return isIdentified()
}

/**
 * 清除用户身份
 */
export function clearCurrentUser() {
  localStorage.removeItem(STORAGE_KEY)
}
