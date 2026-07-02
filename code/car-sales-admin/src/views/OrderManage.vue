<template>
  <div>
    <div class="card">
      <div class="card-header">订单管理</div>

      <!-- Tabs -->
      <div class="tabs">
        <div class="tab-item" :class="{ active: activeTab === 'pending' }" @click="switchTab('pending')">
          待确认
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'all' }" @click="switchTab('all')">
          全部
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading">加载中...</div>

      <!-- Error -->
      <div v-else-if="error" class="error-msg">{{ error }}</div>

      <!-- Table -->
      <div v-else class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>客户名</th>
              <th>品牌</th>
              <th>车型</th>
              <th>数量</th>
              <th>总价(元)</th>
              <th>状态</th>
              <th>下单时间</th>
              <th v-if="activeTab === 'pending'">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.customerName || item.username || '-' }}</td>
              <td>{{ item.brand || '-' }}</td>
              <td>{{ item.model || '-' }}</td>
              <td>{{ item.quantity ?? '-' }}</td>
              <td>{{ formatPrice(item.totalPrice) }}</td>
              <td>
                <span v-if="item.status === 'pending' || item.status === '待确认'" class="badge badge-orange">
                  待确认
                </span>
                <span v-else class="badge badge-green">已确认</span>
              </td>
              <td>{{ formatDateTime(item.createdAt) }}</td>
              <td v-if="activeTab === 'pending'">
                <button class="btn btn-sm btn-success" :disabled="confirmingId === item.id" @click="handleConfirm(item.id)">
                  {{ confirmingId === item.id ? '确认中...' : '确认' }}
                </button>
              </td>
            </tr>
            <tr v-if="list.length === 0">
              <td :colspan="activeTab === 'pending' ? 9 : 8" style="text-align: center; color: #999;">
                暂无{{ activeTab === 'pending' ? '待确认' : '' }}订单
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
import { fetchOrders, confirmOrder } from '../api/index.js'

const list = ref([])
const loading = ref(false)
const error = ref('')
const activeTab = ref('pending')
const confirmingId = ref(null)

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const params = {}
    if (activeTab.value === 'pending') params.status = 'pending'
    list.value = await fetchOrders(params)
  } catch (e) {
    error.value = '获取订单列表失败：' + (e.message || '网络错误')
  } finally {
    loading.value = false
  }
}

function switchTab(tab) {
  activeTab.value = tab
  loadData()
}

async function handleConfirm(id) {
  confirmingId.value = id
  try {
    await confirmOrder(id)
    list.value = list.value.filter(item => item.id !== id)
  } catch (e) {
    alert('确认失败：' + (e.message || '网络错误'))
  } finally {
    confirmingId.value = null
  }
}

function formatPrice(price) {
  if (price == null) return '-'
  return '¥' + Number(price).toLocaleString()
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>
