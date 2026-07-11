<template>
  <div>
    <!-- Loading -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>正在加载车辆数据...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="fetchCars">重新加载</button>
    </div>

    <!-- Empty -->
    <div v-else-if="cars.length === 0" class="empty-state">
      <div class="empty-icon">🚗</div>
      <p>暂无在售车辆</p>
    </div>

    <!-- 车辆网格 -->
    <div v-else class="grid grid-3">
      <div
        v-for="car in cars"
        :key="car.carId"
        class="card vehicle-card"
        @click="$router.push(`/cars/${car.carId}`)"
      >
        <div class="card-image">
          <img v-if="car.imageUrl" :src="car.imageUrl" :alt="car.brand + ' ' + car.model"
               class="car-thumb" @error="onImageError($event)" />
          <div v-else class="card-image-fallback">🚗</div>
        </div>
        <div class="card-info">
          <div class="card-title">{{ car.brand }} {{ car.model }}</div>
          <div class="card-subtitle">{{ car.displacement }} · {{ car.transmission }} · {{ car.color }}</div>
          <div class="card-meta">
            <span class="card-price">¥{{ formatPrice(car.price) }}</span>
            <span class="card-stock" :class="{ 'text-danger': car.stock === 0 }">
              库存: {{ car.stock }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCars } from '../api/index.js'

const cars = ref([])
const loading = ref(false)
const error = ref('')

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

async function fetchCars() {
  loading.value = true
  error.value = ''
  try {
    cars.value = await getCars()
  } catch (e) {
    error.value = e.message || '获取车辆列表失败'
  } finally {
    loading.value = false
  }
}

function onImageError(e) {
  e.target.style.display = 'none'
  e.target.parentElement.innerHTML = '<div class="card-image-fallback">🚗</div>'
}

onMounted(() => {
  fetchCars()
})
</script>

<style scoped>
.text-danger {
  color: var(--danger);
}
</style>
