<template>
  <div>
    <div class="card" style="max-width: 560px;">
      <div class="card-body">
        <!-- Alert -->
        <div v-if="successMsg" class="alert alert-success">{{ successMsg }}</div>
        <div v-if="errMsg" class="alert alert-error">{{ errMsg }}</div>

        <form @submit.prevent="handleSubmit">
          <!-- 姓名 -->
          <div class="form-group">
            <label class="form-label">姓名</label>
            <input
              type="text"
              class="form-control"
              v-model="form.customerName"
              required
              placeholder="请输入您的姓名"
            />
          </div>

          <!-- 电话 -->
          <div class="form-group">
            <label class="form-label">电话</label>
            <input
              type="tel"
              class="form-control"
              v-model="form.customerPhone"
              required
              maxlength="11"
              pattern="\d{11}"
              title="请输入11位手机号"
              placeholder="请输入11位手机号"
            />
          </div>

          <!-- 选择车辆 -->
          <div class="form-group">
            <label class="form-label">选择车辆</label>
            <select v-model="form.carId" class="form-control" required @change="onCarChange">
              <option value="" disabled>请选择车辆</option>
              <option v-for="car in onSaleCars" :key="car.carId" :value="car.carId">
                {{ car.brand }} {{ car.model }} - ¥{{ formatPrice(car.price) }} (库存: {{ car.stock }})
              </option>
            </select>
          </div>

          <!-- 数量 -->
          <div class="form-group">
            <label class="form-label">数量</label>
            <input
              type="number"
              class="form-control"
              v-model="form.quantity"
              min="1"
              :max="selectedCar?.stock || 99"
              required
              @input="onQuantityChange"
            />
          </div>

          <!-- 价格信息 -->
          <div v-if="selectedCar" class="price-summary" style="background: #f8f9fa; padding: 16px; border-radius: 4px; margin-bottom: 16px;">
            <div class="flex-between" style="margin-bottom: 8px;">
              <span style="color: var(--text-secondary);">单价：</span>
              <span>¥{{ formatPrice(selectedCar.price) }}</span>
            </div>
            <div class="flex-between" style="margin-bottom: 8px;">
              <span style="color: var(--text-secondary);">数量：</span>
              <span>{{ form.quantity || 0 }}</span>
            </div>
            <div class="flex-between" style="border-top: 1px solid var(--border); padding-top: 8px;">
              <span style="font-weight: 600; font-size: 16px;">总价：</span>
              <span style="font-size: 22px; font-weight: 700; color: var(--danger);">
                ¥{{ formatPrice(totalPrice) }}
              </span>
            </div>
          </div>

          <!-- 提交 -->
          <div class="mt-16">
            <button type="submit" class="btn btn-success btn-lg" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交订单' }}
            </button>
            <button type="button" class="btn btn-outline btn-lg" style="margin-left: 8px;" @click="$router.push('/my/orders')">
              查看我的订单
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCars, createOrder } from '../api/index.js'

const route = useRoute()
const router = useRouter()

const cars = ref([])
const submitting = ref(false)
const onSaleCars = computed(() => cars.value.filter(c => c.status === 'on_sale' && c.stock > 0))
const successMsg = ref('')
const errMsg = ref('')

const form = ref({
  customerName: '',
  customerPhone: '',
  carId: '',
  quantity: 1
})

const selectedCar = computed(() => {
  if (!form.value.carId) return null
  return cars.value.find(c => c.carId === Number(form.value.carId)) || null
})

const totalPrice = computed(() => {
  if (!selectedCar.value) return 0
  return Number(selectedCar.value.price) * (Number(form.value.quantity) || 0)
})

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

function onCarChange() {
  form.value.quantity = 1
}

function onQuantityChange() {
  const q = Number(form.value.quantity)
  if (q < 1) form.value.quantity = 1
  if (selectedCar.value && q > selectedCar.value.stock) {
    form.value.quantity = selectedCar.value.stock
  }
}

async function fetchCars() {
  try {
    cars.value = await getCars()
  } catch (e) {
    errMsg.value = '获取车辆列表失败: ' + e.message
  }
}

async function handleSubmit() {
  if (!form.value.customerName.trim()) {
    errMsg.value = '请输入姓名'
    return
  }
  if (!form.value.customerPhone.trim()) {
    errMsg.value = '请输入电话'
    return
  }
  if (!form.value.carId) {
    errMsg.value = '请选择车辆'
    return
  }
  if (!form.value.quantity || form.value.quantity < 1) {
    errMsg.value = '请输入有效数量'
    return
  }

  submitting.value = true
  errMsg.value = ''
  successMsg.value = ''

  try {
    const result = await createOrder({
      customerName: form.value.customerName.trim(),
      customerPhone: form.value.customerPhone.trim(),
      carId: form.value.carId,
      quantity: Number(form.value.quantity)
    })
    const id = result?.orderId || ''
    successMsg.value = `订单提交成功！订单编号: ${id}`
    setTimeout(() => {
      router.push('/my/orders')
    }, 2000)
  } catch (e) {
    errMsg.value = e.message || '提交失败，请重试'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (route.query.carId) {
    form.value.carId = Number(route.query.carId)
  }
  fetchCars()
})
</script>
