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
      <div class="detail-image">🚗</div>
      <div class="detail-info card-body">
        <h1>{{ car.brand }} {{ car.model }}</h1>
        <div class="detail-subtitle">
          {{ translateCategory(car.category) }} · {{ car.color }} · {{ car.year }}年款
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
            <div class="spec-label">分类</div>
            <div class="spec-value">{{ translateCategory(car.category) }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">年份</div>
            <div class="spec-value">{{ car.year }}年</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">颜色</div>
            <div class="spec-value">{{ car.color }}</div>
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

        <div class="detail-description" v-if="car.description">
          <h3>车辆描述</h3>
          <p>{{ car.description }}</p>
        </div>

        <div class="detail-actions">
          <button class="btn btn-primary btn-lg" @click="goTestDrive">
            📅 预约试驾
          </button>
          <button class="btn btn-success btn-lg" @click="goOrder">
            🛒 立即购买
          </button>
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

function translateCategory(cat) {
  const map = { 'SUV': 'SUV', '轿车': '轿车', '新能源': '新能源', '跑车': '跑车' }
  return map[cat] || cat
}

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

function goTestDrive() {
  router.push(`/test-drive/new?carId=${route.params.id}`)
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

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.text-danger {
  color: var(--danger);
}
</style>
