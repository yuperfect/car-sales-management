<template>
  <div>
    <!-- 未绑定提示 -->
    <div v-if="!currentUser" class="card" style="max-width: 560px;">
      <div class="card-body" style="text-align: center; padding: 40px;">
        <div style="font-size: 48px; margin-bottom: 16px;">📋</div>
        <p style="font-size: 16px; color: var(--text-secondary);">请先在个人信息页绑定身份，即可查看您的所有预约</p>
        <button class="btn btn-primary" style="margin-top: 16px;" @click="$router.push('/my/profile')">
          去绑定
        </button>
      </div>
    </div>

    <!-- 已绑定：列表视图 -->
    <div v-else>
      <!-- 状态筛选 -->
      <div class="filter-bar">
        <button v-for="tab in statusTabs" :key="tab.value"
                class="filter-btn"
                :class="{ active: activeStatus === tab.value }"
                @click="activeStatus = tab.value">
          {{ tab.label }}
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- Empty -->
      <div v-else-if="filteredList.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>{{ activeStatus === 'all' ? '还没有预约记录' : '没有对应状态的预约记录' }}</p>
        <button class="btn btn-primary" @click="$router.push('/')">去选车</button>
      </div>

      <!-- 列表 -->
      <div v-else class="list-container">
        <div v-for="item in filteredList" :key="item.appointmentId" class="list-card card">
          <div class="list-card-body">
            <div class="list-card-header">
              <span class="list-card-id">#{{ item.appointmentId }}</span>
              <span class="tag" :class="statusClass(item.status)">{{ statusText(item.status) }}</span>
            </div>
            <div class="list-card-info">
              <div class="info-row">
                <span class="info-label">车辆</span>
                <span class="info-value">
                  <router-link :to="`/cars/${item.carId}`">
                    {{ item.carBrand || '—' }} {{ item.carModel || '' }}
                  </router-link>
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">姓名</span>
                <span class="info-value">{{ item.customerName }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">电话</span>
                <span class="info-value">{{ item.customerPhone }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">预约时间</span>
                <span class="info-value">{{ item.appointmentTime }}</span>
              </div>
              <div class="info-row" v-if="item.remark">
                <span class="info-label">备注</span>
                <span class="info-value">{{ item.remark }}</span>
              </div>
            </div>
            <div class="list-card-actions" v-if="item.status === 'pending'">
              <button class="btn btn-danger btn-sm" @click="handleCancel(item)" :disabled="cancellingId === item.appointmentId">
                {{ cancellingId === item.appointmentId ? '取消中...' : '取消预约' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="errMsg" class="alert alert-error" style="margin-top: 12px;">{{ errMsg }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { getAppointmentsByCustomer, cancelAppointment } from '../api/index.js'
import { getCurrentUser } from '../utils/user.js'

const currentUser = computed(() => getCurrentUser())

const appointments = ref([])
const loading = ref(false)
const errMsg = ref('')
const activeStatus = ref('all')
const cancellingId = ref(null)

const statusTabs = [
  { label: '全部', value: 'all' },
  { label: '待确认', value: 'pending' },
  { label: '已确认', value: 'confirmed' },
  { label: '已取消', value: 'cancelled' }
]

const filteredList = computed(() => {
  if (activeStatus.value === 'all') return appointments.value
  return appointments.value.filter(a => a.status === activeStatus.value)
})

function statusClass(status) {
  const map = { 'pending': 'tag-pending', 'confirmed': 'tag-confirmed', 'cancelled': 'tag-cancelled' }
  return map[status] || 'tag-pending'
}

function statusText(status) {
  const map = { 'pending': '待确认', 'confirmed': '已确认', 'cancelled': '已取消' }
  return map[status] || status
}

async function fetchList() {
  const user = getCurrentUser()
  if (!user) return

  loading.value = true
  errMsg.value = ''
  try {
    appointments.value = await getAppointmentsByCustomer(user.customerId)
  } catch (e) {
    errMsg.value = e.message || '获取预约列表失败'
  } finally {
    loading.value = false
  }
}

async function handleCancel(item) {
  if (!confirm(`确定要取消预约 #${item.appointmentId} 吗？`)) return
  cancellingId.value = item.appointmentId
  try {
    await cancelAppointment(item.appointmentId)
    item.status = 'cancelled'
  } catch (e) {
    errMsg.value = e.message || '取消失败'
  } finally {
    cancellingId.value = null
  }
}

onMounted(() => {
  if (currentUser.value) fetchList()
})

// 监听绑定状态变化：如果用户切换到已绑定，重新加载
watch(currentUser, (val) => {
  if (val) fetchList()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.filter-btn {
  padding: 6px 16px;
  border: 1px solid var(--border);
  border-radius: 20px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
  transition: all 0.2s;
}
.filter-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}
.filter-btn.active {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}
.list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.list-card-body {
  padding: 16px;
}
.list-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.list-card-id {
  font-weight: 600;
  font-size: 15px;
  color: var(--text);
}
.info-row {
  display: flex;
  margin-bottom: 6px;
  font-size: 14px;
}
.info-label {
  width: 70px;
  color: var(--text-secondary);
  flex-shrink: 0;
}
.info-value {
  color: var(--text);
  word-break: break-all;
}
.list-card-actions {
  text-align: right;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}
.btn-sm {
  padding: 4px 12px;
  font-size: 13px;
}
</style>
