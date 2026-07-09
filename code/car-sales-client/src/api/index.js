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

/** 获取在售车辆列表，可按品牌/车型筛选 */
export function getCars(brand, model) {
  const params = {}
  if (brand && brand !== '') {
    params.brand = brand
  }
  if (model && model !== '') {
    params.model = model
  }
  return request.get('/cars', { params })
}

/** 获取车辆详情 */
export function getCarById(id) {
  return request.get(`/cars/${id}`)
}

// ============ 预约（原试驾预约） ============

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

/** 取消订单 */
export function cancelOrder(id) {
  return request.put(`/purchase-orders/${id}/cancel`)
}

/** 取消预约 */
export function cancelAppointment(id) {
  return request.put(`/appointments/${id}/cancel`)
}

export default request
