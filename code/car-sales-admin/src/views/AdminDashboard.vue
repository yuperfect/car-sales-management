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
        <span class="stat-value orange">{{ stats.pendingTestDrives ?? '-' }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">待确认订单</span>
        <span class="stat-value purple">{{ stats.pendingOrders ?? '-' }}</span>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error-msg">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchCars, fetchUsers, fetchTestDrives, fetchOrders } from '../api/index.js'

const stats = ref({})
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const [cars, users, testDrives, orders] = await Promise.all([
      fetchCars(),
      fetchUsers(),
      fetchTestDrives(),
      fetchOrders()
    ])
    stats.value = {
      totalCars: Array.isArray(cars) ? cars.length : 0,
      totalUsers: Array.isArray(users) ? users.length : 0,
      pendingTestDrives: Array.isArray(testDrives)
        ? testDrives.filter(t => t.status === 'pending').length
        : 0,
      pendingOrders: Array.isArray(orders)
        ? orders.filter(o => o.status === 'pending').length
        : 0
    }
  } catch (e) {
    error.value = '获取统计数据失败：' + (e.message || '网络错误')
  } finally {
    loading.value = false
  }
})
</script>
