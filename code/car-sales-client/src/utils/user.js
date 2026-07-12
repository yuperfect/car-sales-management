/**
 * 用户身份管理工具
 * 使用 localStorage 存储当前用户的身份信息
 */

const STORAGE_KEY = 'car_sales_user'

/**
 * 获取当前登录用户信息
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
 * 清除用户身份（退出绑定）
 */
export function clearCurrentUser() {
  localStorage.removeItem(STORAGE_KEY)
}
