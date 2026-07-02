import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动添加 userId 参数
request.interceptors.request.use(config => {
  config.params = { ...config.params, userId: 1 }
  return config
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

// ==================== 客户管理 ====================
export const fetchUsers = (params) => request.get('/users', { params })

// ==================== 试驾预约管理 ====================
export const fetchTestDrives = (params) => request.get('/test-drives', { params })
export const confirmTestDrive = (id) => request.put(`/test-drives/${id}/confirm`)

// ==================== 订单管理 ====================
export const fetchOrders = (params) => request.get('/purchase-orders', { params })
export const confirmOrder = (id) => request.put(`/purchase-orders/${id}/confirm`)

// ==================== 综合查询 ====================
export const querySales = (params) => request.get('/queries/sales', { params })

// ==================== 统计分析 ====================
export const fetchTestDriveHotStats = () => request.get('/statistics/test-drive-hot')
export const fetchSalesHotStats = () => request.get('/statistics/sales-hot')
export const fetchSalesShareStats = () => request.get('/statistics/sales-share')

export default request
