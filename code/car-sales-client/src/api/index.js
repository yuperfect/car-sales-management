import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 默认客户 ID
const DEFAULT_CUSTOMER_ID = 2

// 响应拦截器：提取 data
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

// ============ 车辆相关 ============

/** 获取在售车辆列表，可按分类筛选 */
export function getCars(category) {
  const params = {}
  if (category && category !== 'all') {
    params.category = category
  }
  return request.get('/cars', { params })
}

/** 获取车辆详情 */
export function getCarById(id) {
  return request.get(`/cars/${id}`)
}

/** 获取车辆分类列表 */
export function getCategories() {
  return request.get('/cars/categories')
}

// ============ 试驾预约 ============

/** 提交试驾预约 */
export function createTestDrive(data) {
  return request.post('/test-drives', {
    customerId: DEFAULT_CUSTOMER_ID,
    carId: data.carId,
    driveDate: data.driveDate,
    driveTime: data.driveTime,
    ...data
  })
}

/** 获取我的试驾预约 */
export function getMyTestDrives() {
  return request.get('/test-drives', {
    params: { customerId: DEFAULT_CUSTOMER_ID }
  })
}

// ============ 订单相关 ============

/** 提交订单 */
export function createOrder(data) {
  return request.post('/purchase-orders', {
    customerId: DEFAULT_CUSTOMER_ID,
    carId: data.carId,
    quantity: data.quantity,
    ...data
  })
}

/** 获取我的订单 */
export function getMyOrders() {
  return request.get('/purchase-orders', {
    params: { customerId: DEFAULT_CUSTOMER_ID }
  })
}

/** 取消订单 */
export function cancelOrder(id) {
  return request.delete(`/purchase-orders/${id}`)
}

// ============ 用户相关 ============

/** 获取用户信息 */
export function getUserById(id) {
  return request.get(`/users/${id || DEFAULT_CUSTOMER_ID}`)
}

/** 更新用户信息 */
export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

export default request
