<template>
  <div>
    <div class="card-header" style="margin-bottom: 20px;">系统概览</div>

    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-label">车辆总数</span>
        <span class="stat-value">{{ stats.totalCars ?? '-' }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">客户总数</span>
        <span class="stat-value green">{{ stats.totalUsers ?? '-' }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">待确认预约</span>
        <span class="stat-value orange">{{ stats.pendingAppointments ?? '-' }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">待确认订单</span>
        <span class="stat-value purple">{{ stats.pendingOrders ?? '-' }}</span>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error-msg">{{ error }}</div>

    <div v-else>
      <!-- 最近预约 -->
      <div class="section">
        <div class="card-header">最近预约</div>
        <table class="data-table" v-if="recentAppointments.length > 0">
          <thead>
            <tr>
              <th>客户</th>
              <th>车型</th>
              <th>预约时间</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in recentAppointments" :key="item.appointmentId">
              <td>{{ item.customerName }}</td>
              <td>{{ item.carBrand }} {{ item.carModel }}</td>
              <td>{{ formatDate(item.appointmentTime) }}</td>
              <td><span :class="statusClass(item.status)">{{ statusText(item.status) }}</span></td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-tip">暂无预约数据</div>
      </div>

      <!-- 最近订单 -->
      <div class="section">
        <div class="card-header">最近订单</div>
        <table class="data-table" v-if="recentOrders.length > 0">
          <thead>
            <tr>
              <th>客户</th>
              <th>车型</th>
              <th>金额</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in recentOrders" :key="item.orderId">
              <td>{{ item.customerName }}</td>
              <td>{{ item.carBrand }} {{ item.carModel }}</td>
              <td>{{ formatPrice(item.totalAmount) }}</td>
              <td><span :class="statusClass(item.status)">{{ statusText(item.status) }}</span></td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-tip">暂无订单数据</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchCars, fetchCustomers, fetchAppointments, fetchOrders } from '../api/index.js'

const stats = ref({})
const recentAppointments = ref([])
const recentOrders = ref([])
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const [cars, customers, appointments, orders] = await Promise.all([
      fetchCars(),
      fetchCustomers(),
      fetchAppointments(),
      fetchOrders()
    ])
    stats.value = {
      totalCars: Array.isArray(cars) ? cars.length : 0,
      totalUsers: Array.isArray(customers) ? customers.length : 0,
      pendingAppointments: Array.isArray(appointments)
        ? appointments.filter(t => t.status === 'pending').length
        : 0,
      pendingOrders: Array.isArray(orders)
        ? orders.filter(o => o.status === 'pending').length
        : 0
    }
    // 按时间倒序取最近5条
    if (Array.isArray(appointments)) {
      recentAppointments.value = appointments
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
        .slice(0, 5)
    }
    if (Array.isArray(orders)) {
      recentOrders.value = orders
        .sort((a, b) => new Date(b.orderTime) - new Date(a.orderTime))
        .slice(0, 5)
    }
  } catch (e) {
    error.value = '获取统计数据失败：' + (e.message || '网络错误')
  } finally {
    loading.value = false
  }
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function formatPrice(val) {
  if (val == null) return ''
  const n = Number(val)
  return '¥' + n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function statusText(status) {
  const map = { pending: '待确认', confirmed: '已确认', cancelled: '已取消' }
  return map[status] || status
}

function statusClass(status) {
  return 'status-' + status
}
</script>

<style scoped>
.section {
  margin-top: 24px;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.data-table th {
  background: #f5f7fa;
  color: #666;
  font-weight: 600;
  padding: 10px 14px;
  text-align: left;
  font-size: 13px;
}
.data-table td {
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}
.status-pending { color: #f0a020; font-weight: 600; }
.status-confirmed { color: #2da641; font-weight: 600; }
.status-cancelled { color: #999; }
.empty-tip {
  text-align: center;
  color: #999;
  padding: 30px 0;
}
</style>
