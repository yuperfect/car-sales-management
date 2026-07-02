<template>
  <div>
    <div class="card" style="max-width: 560px;">
      <div class="card-body">
        <!-- Alert -->
        <div v-if="successMsg" class="alert alert-success">{{ successMsg }}</div>
        <div v-if="errMsg" class="alert alert-error">{{ errMsg }}</div>

        <form @submit.prevent="handleSubmit">
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

          <!-- 预约日期 -->
          <div class="form-group">
            <label class="form-label">预约日期</label>
            <input
              type="date"
              class="form-control"
              v-model="form.driveDate"
              :min="today"
              required
            />
          </div>

          <!-- 预约时间 -->
          <div class="form-group">
            <label class="form-label">预约时间</label>
            <input
              type="time"
              class="form-control"
              v-model="form.driveTime"
              required
            />
          </div>

          <!-- 提交 -->
          <div class="mt-24">
            <button type="submit" class="btn btn-primary btn-lg" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交预约' }}
            </button>
            <button type="button" class="btn btn-outline btn-lg" style="margin-left: 8px;" @click="$router.push('/my/test-drives')">
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
import { getCars, createTestDrive } from '../api/index.js'

const route = useRoute()
const router = useRouter()

const cars = ref([])
const submitting = ref(false)
const successMsg = ref('')
const errMsg = ref('')

const today = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
})

const form = ref({
  carId: '',
  driveDate: '',
  driveTime: ''
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
  if (!form.value.carId || !form.value.driveDate || !form.value.driveTime) {
    errMsg.value = '请完整填写所有字段'
    return
  }

  submitting.value = true
  errMsg.value = ''
  successMsg.value = ''

  try {
    await createTestDrive({
      carId: form.value.carId,
      driveDate: form.value.driveDate,
      driveTime: form.value.driveTime
    })
    successMsg.value = '试驾预约提交成功！正在跳转到我的预约...'
    setTimeout(() => {
      router.push('/my/test-drives')
    }, 1500)
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
