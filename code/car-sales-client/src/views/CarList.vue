<template>
  <div>
    <!-- 分类筛选 -->
    <div class="filter-bar flex gap-8 mb-16">
      <button
        v-for="cat in categories"
        :key="cat.value"
        class="btn"
        :class="currentCategory === cat.value ? 'btn-primary' : 'btn-outline'"
        @click="filterByCategory(cat.value)"
      >
        {{ cat.label }}
      </button>
    </div>

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
        :key="car.id"
        class="card vehicle-card"
        @click="$router.push(`/cars/${car.id}`)"
      >
        <div class="card-image">🚗</div>
        <div class="card-info">
          <div class="card-title">{{ car.brand }} {{ car.model }}</div>
          <div class="card-subtitle">{{ translateCategory(car.category) }} · {{ car.color }} · {{ car.year }}</div>
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
const currentCategory = ref('all')

const categories = [
  { value: 'all', label: '全部' },
  { value: 'SUV', label: 'SUV' },
  { value: '轿车', label: '轿车' },
  { value: '新能源', label: '新能源' },
  { value: '跑车', label: '跑车' }
]

function translateCategory(cat) {
  const map = {
    'SUV': 'SUV',
    '轿车': '轿车',
    '新能源': '新能源',
    '跑车': '跑车'
  }
  return map[cat] || cat
}

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

async function fetchCars() {
  loading.value = true
  error.value = ''
  try {
    cars.value = await getCars(currentCategory.value === 'all' ? null : currentCategory.value)
  } catch (e) {
    error.value = e.message || '获取车辆列表失败'
  } finally {
    loading.value = false
  }
}

function filterByCategory(category) {
  currentCategory.value = category
  fetchCars()
}

onMounted(() => {
  fetchCars()
})
</script>

<style scoped>
.filter-bar {
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.text-danger {
  color: var(--danger);
}
</style>
