<template>
  <div>
    <!-- Loading -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>正在加载订单数据...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="fetchData">重新加载</button>
    </div>

    <!-- Empty -->
    <div v-else-if="list.length === 0" class="empty-state">
      <div class="empty-icon">📦</div>
      <p>暂无订单记录</p>
      <router-link to="/orders/new" class="btn btn-success" style="display: inline-block; margin-top: 12px;">
        立即下单
      </router-link>
    </div>

    <!-- 表格 -->
    <div v-else class="card">
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>车辆</th>
              <th>数量</th>
              <th>总价</th>
              <th>状态</th>
              <th>下单时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list" :key="item.id">
              <td>
                <router-link :to="`/cars/${item.carId}`">
                  {{ item.carBrand || item.brand || '—' }} {{ item.carModel || item.model || '' }}
                </router-link>
              </td>
              <td>{{ item.quantity }}</td>
              <td style="font-weight: 600; color: var(--danger);">¥{{ formatPrice(item.totalPrice) }}</td>
              <td>
                <span class="tag" :class="statusClass(item.status)">
                  {{ statusText(item.status) }}
                </span>
              </td>
              <td>{{ item.createTime || item.createdAt || '—' }}</td>
              <td>
                <button
                  v-if="item.status === 'pending'"
                  class="btn btn-danger btn-sm"
                  @click="handleCancel(item)"
                  :disabled="cancellingId === item.id"
                >
                  {{ cancellingId === item.id ? '取消中...' : '取消' }}
                </button>
                <span v-else style="color: var(--text-light); font-size: 13px;">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyOrders, cancelOrder } from '../api/index.js'

const list = ref([])
const loading = ref(false)
const error = ref('')
const cancellingId = ref(null)

function statusClass(status) {
  const map = {
    'pending': 'tag-pending',
    'confirmed': 'tag-confirmed',
    'completed': 'tag-completed',
    'cancelled': 'tag-cancelled'
  }
  return map[status] || 'tag-pending'
}

function statusText(status) {
  const map = {
    'pending': '待处理',
    'confirmed': '已确认',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return map[status] || status
}

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    list.value = await getMyOrders()
  } catch (e) {
    error.value = e.message || '获取订单列表失败'
  } finally {
    loading.value = false
  }
}

async function handleCancel(item) {
  if (!confirm(`确定要取消订单 #${item.id} 吗？`)) return
  cancellingId.value = item.id
  try {
    await cancelOrder(item.id)
    // 将本地状态改为已取消
    item.status = 'cancelled'
  } catch (e) {
    alert('取消失败: ' + (e.message || '请重试'))
  } finally {
    cancellingId.value = null
  }
}

onMounted(() => {
  fetchData()
})
</script>
