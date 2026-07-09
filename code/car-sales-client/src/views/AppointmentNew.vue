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
              placeholder="请输入您的电话号码"
            />
          </div>

          <!-- 选择车辆 -->
          <div class="form-group">
            <label class="form-label">选择车辆</label>
            <select v-model="form.carId" class="form-control" required>
              <option value="" disabled>请选择车辆</option>
              <option v-for="car in cars" :key="car.id" :value="car.id">
                {{ car.brand }} {{ car.model }} - ¥{{ formatPrice(car.price) }}
              </option>
            </select>
          </div>

          <!-- 预约时间 -->
          <div class="form-group">
            <label class="form-label">预约时间</label>
            <input
              type="datetime-local"
              class="form-control"
              v-model="form.appointmentTime"
              :min="now"
              required
            />
          </div>

          <!-- 备注 -->
          <div class="form-group">
            <label class="form-label">备注（可选）</label>
            <textarea
              class="form-control"
              v-model="form.remark"
              placeholder="如有特殊需求请备注"
              rows="3"
            ></textarea>
          </div>

          <!-- 提交 -->
          <div class="mt-24">
            <button type="submit" class="btn btn-primary btn-lg" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交预约' }}
            </button>
            <button type="button" class="btn btn-outline btn-lg" style="margin-left: 8px;" @click="$router.push('/my/appointments')">
              查看我的预约
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
import { getCars, createAppointment } from '../api/index.js'

const route = useRoute()
const router = useRouter()

const cars = ref([])
const submitting = ref(false)
const successMsg = ref('')
const errMsg = ref('')

const now = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day}T${h}:${min}`
})

const form = ref({
  customerName: '',
  customerPhone: '',
  carId: '',
  appointmentTime: '',
  remark: ''
})

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
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
  if (!form.value.appointmentTime) {
    errMsg.value = '请选择预约时间'
    return
  }

  submitting.value = true
  errMsg.value = ''
  successMsg.value = ''

  try {
    const result = await createAppointment({
      customerName: form.value.customerName.trim(),
      customerPhone: form.value.customerPhone.trim(),
      carId: form.value.carId,
      appointmentTime: form.value.appointmentTime,
      remark: form.value.remark.trim()
    })
    const code = result?.code || ''
    successMsg.value = `预约提交成功！预约编号: ${code}`
    setTimeout(() => {
      router.push('/my/appointments')
    }, 2000)
  } catch (e) {
    errMsg.value = e.message || '提交失败，请重试'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 如果从详情页带 carId 过来，预填
  if (route.query.carId) {
    form.value.carId = Number(route.query.carId)
  }
  fetchCars()
})
</script>
