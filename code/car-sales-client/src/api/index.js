import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

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

/** 获取在售车辆列表 */
export function getCars(brand, model) {
  const params = {}
  if (brand) params.brand = brand
  if (model) params.model = model
  return request.get('/cars', { params })
}

/** 获取车辆详情 */
export function getCarById(id) {
  return request.get(`/cars/${id}`)
}

// ============ 预约相关 ============

/** 提交预约 */
export function createAppointment(data) {
  return request.post('/appointments', {
    customerName: data.customerName,
    customerPhone: data.customerPhone,
    carId: data.carId,
    appointmentTime: data.appointmentTime,
    remark: data.remark || ''
  })
}

/** 按编号查询预约 */
export function getAppointmentByCode(code) {
  return request.get(`/appointments/${code}`)
}

/** 按客户ID获取预约列表 */
export function getAppointmentsByCustomer(customerId, status) {
  const params = { customerId }
  if (status && status !== 'all') params.status = status
  return request.get('/appointments', { params })
}

/** 取消预约 */
export function cancelAppointment(id) {
  return request.put(`/appointments/${id}/cancel`)
}

// ============ 订单相关 ============

/** 提交订单 */
export function createOrder(data) {
  return request.post('/purchase-orders', {
    customerName: data.customerName,
    customerPhone: data.customerPhone,
    carId: data.carId,
    quantity: data.quantity
  })
}

/** 按编号查询订单 */
export function getOrderByCode(code) {
  return request.get(`/purchase-orders/${code}`)
}

/** 按客户ID获取订单列表 */
export function getOrdersByCustomer(customerId, status) {
  const params = { customerId }
  if (status && status !== 'all') params.status = status
  return request.get('/purchase-orders', { params })
}

/** 取消订单 */
export function cancelOrder(id) {
  return request.put(`/purchase-orders/${id}/cancel`)
}

// ============ 客户相关 ============

/** 按姓名/电话搜索客户（管理端用） */
export function searchCustomers(keyword) {
  return request.get('/customers', { params: { keyword } })
}

/** 获取单个客户 */
export function getCustomer(id) {
  return request.get(`/customers/${id}`)
}

/** 绑定（注册） */
export function bindAccount(data) {
  return request.post('/customers/bind', data)
}

/** 登录 */
export function login(data) {
  return request.post('/customers/login', data)
}

/** 更新个人信息（含头像），需用 FormData */
export function updateCustomer(id, formData) {
  return request.put(`/customers/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export default request
