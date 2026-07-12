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
              <th>单价(元)</th>
              <th>总价(元)</th>
              <th>状态</th>
              <th>下单时间</th>
              <th v-if="activeTab === 'pending'">操作</th>
              <th v-else>处理人</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list" :key="item.orderId">
              <td>{{ item.orderId }}</td>
              <td>{{ item.customerName || '-' }}</td>
              <td>{{ item.carBrand || '-' }}</td>
              <td>{{ item.carModel || '-' }}</td>
              <td>{{ item.quantity ?? '-' }}</td>
              <td>{{ formatPrice(item.unitPrice) }}</td>
              <td>{{ formatPrice(item.totalAmount) }}</td>
              <td>
                <span v-if="item.status === 'pending'" class="badge badge-orange">待确认</span>
                <span v-else-if="item.status === 'confirmed'" class="badge badge-green">已确认</span>
                <span v-else class="badge badge-gray">{{ item.status }}</span>
              </td>
              <td>{{ formatDateTime(item.orderTime) }}</td>
              <td v-if="activeTab === 'pending'">
                <button class="btn btn-sm btn-success" :disabled="processingId === item.orderId" @click="openConfirmDialog(item.orderId)">
                  {{ processingId === item.orderId ? '确认中...' : '确认' }}
                </button>
                <button class="btn btn-sm btn-danger" :disabled="processingId === item.orderId" @click="handleCancel(item.orderId)">
                  {{ processingId === item.orderId ? '取消中...' : '取消' }}
                </button>
              </td>
              <td v-else>
                {{ item.handler || '-' }}
                <span v-if="item.handleTime" style="color: #999; font-size: 12px; margin-left: 4px;">
                  ({{ formatDateTime(item.handleTime) }})
                </span>
              </td>
            </tr>
            <tr v-if="list.length === 0">
              <td :colspan="activeTab === 'pending' ? 11 : 11" style="text-align: center; color: #999;">
                暂无{{ activeTab === 'pending' ? '待确认' : '' }}订单
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Confirm Dialog -->
    <div v-if="showConfirmDialog" class="modal-overlay" @click.self="closeConfirmDialog">
      <div class="modal-content">
        <div class="modal-header">确认订单</div>
        <div class="modal-body">
          <label class="form-label">处理人 <span style="color: #ff4d4f;">*</span></label>
          <input v-model="handlerName" class="form-input" placeholder="请输入处理人姓名" />
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" :disabled="!handlerName.trim() || processingId !== null" @click="handleConfirm">
            确定
          </button>
          <button class="btn" @click="closeConfirmDialog">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchOrders, confirmOrder, cancelOrder } from '../api/index.js'

const list = ref([])
const loading = ref(false)
const error = ref('')
const activeTab = ref('pending')
const processingId = ref(null)

// Confirm dialog
const showConfirmDialog = ref(false)
const handlerName = ref('')
const confirmTargetId = ref(null)

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

function openConfirmDialog(id) {
  confirmTargetId.value = id
  handlerName.value = ''
  showConfirmDialog.value = true
}

function closeConfirmDialog() {
  showConfirmDialog.value = false
  confirmTargetId.value = null
  handlerName.value = ''
}

async function handleConfirm() {
  if (!handlerName.value.trim() || !confirmTargetId.value) return
  processingId.value = confirmTargetId.value
  try {
    await confirmOrder(confirmTargetId.value, handlerName.value.trim())
    list.value = list.value.filter(item => item.orderId !== confirmTargetId.value)
    closeConfirmDialog()
  } catch (e) {
    alert('确认失败：' + (e.message || '网络错误'))
  } finally {
    processingId.value = null
  }
}

async function handleCancel(id) {
  if (!confirm('确定要取消该订单吗？')) return
  processingId.value = id
  try {
    await cancelOrder(id)
    list.value = list.value.filter(item => item.orderId !== id)
  } catch (e) {
    alert('取消失败：' + (e.message || '网络错误'))
  } finally {
    processingId.value = null
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

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  min-width: 360px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.modal-header {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.modal-body {
  margin-bottom: 20px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
