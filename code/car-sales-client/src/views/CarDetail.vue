<template>
  <div>
    <!-- Loading -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>正在加载车辆详情...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="fetchDetail">重新加载</button>
    </div>

    <!-- 车辆详情 -->
    <div v-else-if="car" class="detail-header card">
      <div class="detail-image">
        <img v-if="car.imageUrl" :src="car.imageUrl + (car.imageVersion ? '?v=' + car.imageVersion : '')"
             :alt="car.brand + ' ' + car.model" class="detail-thumb" @error="onImageError($event)" />
        <div v-else class="detail-image-fallback">🚗</div>
      </div>
      <div class="detail-info card-body">
        <h1>{{ car.brand }} {{ car.model }}</h1>
        <div class="detail-subtitle">
          {{ car.displacement }} · {{ car.transmission }} · {{ car.color }}
        </div>

        <div class="detail-specs">
          <div class="spec-item">
            <div class="spec-label">品牌</div>
            <div class="spec-value">{{ car.brand }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">车型</div>
            <div class="spec-value">{{ car.model }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">排量</div>
            <div class="spec-value">{{ car.displacement }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">变速箱</div>
            <div class="spec-value">{{ car.transmission }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">颜色</div>
            <div class="spec-value">{{ car.color }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">上架时间</div>
            <div class="spec-value">{{ car.listedTime }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">库存</div>
            <div class="spec-value" :class="{ 'text-danger': car.stock === 0 }">
              {{ car.stock }} 辆
            </div>
          </div>
          <div class="spec-item">
            <div class="spec-label">价格</div>
            <div class="spec-value" style="color: var(--danger); font-size: 20px;">
              ¥{{ formatPrice(car.price) }}
            </div>
          </div>
        </div>

        <div class="detail-actions">
          <button v-if="car.stock > 0" class="btn btn-primary btn-lg" @click="openAppointmentModal">
            📅 预约试驾
          </button>
          <button v-if="car.stock > 0" class="btn btn-success btn-lg" @click="openOrderModal">
            🛒 立即购买
          </button>
          <div v-else style="margin-top: 8px; color: var(--danger); font-weight: 500;">
            该车型已售罄
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 预约弹窗 ===== -->
    <div v-if="showAppointmentModal" class="modal-overlay" @click.self="closeAppointmentModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>📅 提交预约</h3>
          <button class="modal-close" @click="closeAppointmentModal">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="apptSuccess" class="alert alert-success">{{ apptSuccess }}</div>
          <div v-if="apptErr" class="alert alert-error">{{ apptErr }}</div>

          <form @submit.prevent="handleAppointmentSubmit">
            <div class="form-group">
              <label class="form-label">姓名</label>
              <input type="text" class="form-control" v-model="apptForm.customerName" required
                     :disabled="!!currentUser" :readonly="!!currentUser" />
            </div>
            <div class="form-group">
              <label class="form-label">电话</label>
              <input type="tel" class="form-control" v-model="apptForm.customerPhone" required maxlength="11"
                     :disabled="!!currentUser" :readonly="!!currentUser" />
            </div>
            <div class="form-group">
              <label class="form-label">预约时间</label>
              <input type="datetime-local" class="form-control" v-model="apptForm.appointmentTime" :min="now" required />
            </div>
            <div class="form-group">
              <label class="form-label">备注（可选）</label>
              <textarea class="form-control" v-model="apptForm.remark" placeholder="如有特殊需求请备注" rows="2"></textarea>
            </div>

            <div class="modal-actions">
              <button type="button" class="btn btn-outline" @click="closeAppointmentModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="apptSubmitting">
                {{ apptSubmitting ? '提交中...' : '提交预约' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- ===== 订单弹窗 ===== -->
    <div v-if="showOrderModal" class="modal-overlay" @click.self="closeOrderModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>🛒 提交订单</h3>
          <button class="modal-close" @click="closeOrderModal">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="orderSuccess" class="alert alert-success">{{ orderSuccess }}</div>
          <div v-if="orderErr" class="alert alert-error">{{ orderErr }}</div>

          <form @submit.prevent="handleOrderSubmit">
            <div class="form-group">
              <label class="form-label">姓名</label>
              <input type="text" class="form-control" v-model="orderForm.customerName" required
                     :disabled="!!currentUser" :readonly="!!currentUser" />
            </div>
            <div class="form-group">
              <label class="form-label">电话</label>
              <input type="tel" class="form-control" v-model="orderForm.customerPhone" required maxlength="11"
                     :disabled="!!currentUser" :readonly="!!currentUser" />
            </div>
            <div class="form-group">
              <label class="form-label">数量</label>
              <input type="number" class="form-control" v-model.number="orderForm.quantity" min="1" :max="car.stock" required />
            </div>

            <!-- 价格预览 -->
            <div v-if="orderForm.quantity > 0" class="price-summary" style="background: #f8f9fa; padding: 12px; border-radius: 4px; margin-bottom: 16px;">
              <div class="flex-between" style="margin-bottom: 4px;">
                <span style="color: var(--text-secondary);">单价：</span>
                <span>¥{{ formatPrice(car.price) }}</span>
              </div>
              <div class="flex-between" style="margin-bottom: 4px;">
                <span style="color: var(--text-secondary);">数量：</span>
                <span>{{ orderForm.quantity }}</span>
              </div>
              <div class="flex-between" style="border-top: 1px solid var(--border); padding-top: 6px;">
                <span style="font-weight: 600;">总价：</span>
                <span style="font-size: 20px; font-weight: 700; color: var(--danger);">
                  ¥{{ formatPrice(car.price * orderForm.quantity) }}
                </span>
              </div>
            </div>

            <div class="modal-actions">
              <button type="button" class="btn btn-outline" @click="closeOrderModal">取消</button>
              <button type="submit" class="btn btn-success" :disabled="orderSubmitting">
                {{ orderSubmitting ? '提交中...' : '提交订单' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCarById, createAppointment, createOrder } from '../api/index.js'
import { getCurrentUser } from '../utils/user.js'

const route = useRoute()
const router = useRouter()

const car = ref(null)
const loading = ref(false)
const error = ref('')

const currentUser = computed(() => getCurrentUser())

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

function onImageError(e) {
  e.target.style.display = 'none'
  e.target.parentElement.innerHTML = '<div class="detail-image-fallback">🚗</div>'
}

async function fetchDetail() {
  loading.value = true
  error.value = ''
  try {
    car.value = await getCarById(route.params.id)
  } catch (e) {
    error.value = e.message || '获取车辆详情失败'
  } finally {
    loading.value = false
  }
}

// ===== 预约弹窗 =====
const showAppointmentModal = ref(false)
const apptSubmitting = ref(false)
const apptSuccess = ref('')
const apptErr = ref('')
const apptForm = ref({
  customerName: '',
  customerPhone: '',
  appointmentTime: '',
  remark: ''
})

const now = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day}T${h}:${min}`
})

function openAppointmentModal() {
  const user = getCurrentUser()
  if (user && user.realName) {
    apptForm.value.customerName = user.realName
    apptForm.value.customerPhone = user.phone
  } else {
    apptForm.value.customerName = ''
    apptForm.value.customerPhone = ''
  }
  apptForm.value.appointmentTime = ''
  apptForm.value.remark = ''
  apptSuccess.value = ''
  apptErr.value = ''
  showAppointmentModal.value = true
}

function closeAppointmentModal() {
  showAppointmentModal.value = false
}

async function handleAppointmentSubmit() {
  apptErr.value = ''
  if (!apptForm.value.customerName.trim()) { apptErr.value = '请输入姓名'; return }
  if (!apptForm.value.customerPhone.trim()) { apptErr.value = '请输入电话'; return }
  if (!apptForm.value.appointmentTime) { apptErr.value = '请选择预约时间'; return }

  apptSubmitting.value = true
  try {
    const result = await createAppointment({
      customerName: apptForm.value.customerName.trim(),
      customerPhone: apptForm.value.customerPhone.trim(),
      carId: car.value.carId,
      appointmentTime: apptForm.value.appointmentTime,
      remark: apptForm.value.remark.trim()
    })

    apptSuccess.value = `预约提交成功！编号: ${result?.appointmentId || ''}`
    setTimeout(() => {
      closeAppointmentModal()
      router.push('/my/appointments')
    }, 1500)
  } catch (e) {
    apptErr.value = e.message || '提交失败，请重试'
  } finally {
    apptSubmitting.value = false
  }
}

// ===== 订单弹窗 =====
const showOrderModal = ref(false)
const orderSubmitting = ref(false)
const orderSuccess = ref('')
const orderErr = ref('')
const orderForm = ref({
  customerName: '',
  customerPhone: '',
  quantity: 1
})

function openOrderModal() {
  const user = getCurrentUser()
  if (user && user.realName) {
    orderForm.value.customerName = user.realName
    orderForm.value.customerPhone = user.phone
  } else {
    orderForm.value.customerName = ''
    orderForm.value.customerPhone = ''
  }
  orderForm.value.quantity = 1
  orderSuccess.value = ''
  orderErr.value = ''
  showOrderModal.value = true
}

function closeOrderModal() {
  showOrderModal.value = false
}

async function handleOrderSubmit() {
  orderErr.value = ''
  if (!orderForm.value.customerName.trim()) { orderErr.value = '请输入姓名'; return }
  if (!orderForm.value.customerPhone.trim()) { orderErr.value = '请输入电话'; return }
  if (!orderForm.value.quantity || orderForm.value.quantity < 1) { orderErr.value = '请输入有效数量'; return }

  orderSubmitting.value = true
  try {
    const result = await createOrder({
      customerName: orderForm.value.customerName.trim(),
      customerPhone: orderForm.value.customerPhone.trim(),
      carId: car.value.carId,
      quantity: orderForm.value.quantity
    })

    orderSuccess.value = `订单提交成功！编号: ${result?.orderId || ''}`
    setTimeout(() => {
      closeOrderModal()
      router.push('/my/orders')
    }, 1500)
  } catch (e) {
    orderErr.value = e.message || '提交失败，请重试'
  } finally {
    orderSubmitting.value = false
  }
}

// 在打开弹窗前自动填充用户信息
onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.text-danger { color: var(--danger); }

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content {
  background: #fff;
  border-radius: 8px;
  width: 90%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 8px 30px rgba(0,0,0,0.2);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}
.modal-header h3 { margin: 0; font-size: 18px; }
.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 4px 8px;
}
.modal-close:hover { color: var(--text); }
.modal-body { padding: 20px; }
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
.price-summary { margin-bottom: 12px; }
.flex-between { display: flex; justify-content: space-between; }
</style>
