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
          <img v-if="car.imageUrl" :src="car.imageUrl" :alt="car.brand + ' ' + car.model"
               class="detail-thumb" @error="onImageError($event)" />
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
          <button v-if="car.stock > 0" class="btn btn-primary btn-lg" @click="goAppointment">
            📅 预约试驾
          </button>
          <button v-if="car.stock > 0" class="btn btn-success btn-lg" @click="goOrder">
            🛒 立即购买
          </button>
          <div v-else style="margin-top: 8px; color: var(--danger); font-weight: 500;">
            该车型已售罄
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCarById } from '../api/index.js'

const route = useRoute()
const router = useRouter()

const car = ref(null)
const loading = ref(false)
const error = ref('')

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

function goAppointment() {
  router.push(`/appointment/new?carId=${route.params.id}`)
}

function goOrder() {
  router.push(`/orders/new?carId=${route.params.id}`)
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

function onImageError(e) {
  e.target.style.display = 'none'
  e.target.parentElement.innerHTML = '<div class="detail-image-fallback">🚗</div>'
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.text-danger {
  color: var(--danger);
}
</style>
