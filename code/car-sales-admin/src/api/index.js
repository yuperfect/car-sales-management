import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 响应拦截器：统一处理返回数据
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// ==================== 首页统计 ====================
export const fetchDashboardStats = () => request.get('/dashboard/stats')

// ==================== 车辆管理 ====================
export const fetchCars = (params) => request.get('/cars', { params })
export const fetchCarById = (id) => request.get(`/cars/${id}`)
export const createCar = (data) => request.post('/cars', data)
export const updateCar = (id, data) => request.put(`/cars/${id}`, data)
export const deleteCar = (id) => request.delete(`/cars/${id}`)
export const importCars = (formData) => request.post('/cars/import', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
export const downloadTemplate = () => request.get('/cars/template', {
  responseType: 'blob'
})

// ==================== 客户管理 ====================
export const fetchCustomers = (params) => request.get('/customers', { params })

// ==================== 预约管理 ====================
export const fetchAppointments = (params) => request.get('/appointments', { params })
export const confirmAppointment = (id, handler) => request.put(`/appointments/${id}/confirm`, { handler })
export const rejectAppointment = (id) => request.put(`/appointments/${id}/reject`)

// ==================== 订单管理 ====================
export const fetchOrders = (params) => request.get('/purchase-orders', { params })
export const confirmOrder = (id, handler) => request.put(`/purchase-orders/${id}/confirm`, { handler })
export const cancelOrder = (id) => request.put(`/purchase-orders/${id}/cancel`)

// ==================== 综合查询 ====================
export const querySales = (params) => request.get('/queries/sales', { params })

// ==================== 统计分析 ====================
export const fetchAppointmentHotStats = () => request.get('/statistics/appointment-hot')
export const fetchSalesHotStats = () => request.get('/statistics/sales-hot')
export const fetchSalesShareStats = () => request.get('/statistics/sales-share')
export const fetchPriceRangeStats = () => request.get('/statistics/price-range')

export default request
