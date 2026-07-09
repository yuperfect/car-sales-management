<template>
  <div>
    <div class="card">
      <div class="card-header">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>车辆列表</span>
          <div>
            <router-link to="/admin/cars/new" class="btn btn-primary">+ 新增车辆</router-link>
            <router-link to="/admin/cars/import" class="btn">📥 Excel导入</router-link>
          </div>
        </div>
      </div>

      <!-- Search -->
      <div class="search-bar">
        <input
          v-model="searchKeyword"
          class="form-input"
          placeholder="搜索品牌 / 车型..."
          @keyup.enter="doSearch"
        />
        <button class="btn btn-primary" @click="doSearch">搜索</button>
        <button class="btn" @click="resetSearch">重置</button>
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
              <th>品牌</th>
              <th>车型</th>
              <th>排量</th>
              <th>变速箱</th>
              <th>颜色</th>
              <th>价格(元)</th>
              <th>库存</th>
              <th>上架时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="car in cars" :key="car.carId">
              <td>{{ car.carId }}</td>
              <td>{{ car.brand }}</td>
              <td>{{ car.model }}</td>
              <td>{{ car.displacement || '-' }}</td>
              <td>{{ car.transmission || '-' }}</td>
              <td>{{ car.color || '-' }}</td>
              <td>{{ formatPrice(car.price) }}</td>
              <td>
                <span :class="car.stock > 0 ? 'badge badge-green' : 'badge badge-red'">
                  {{ car.stock }}
                </span>
              </td>
              <td>{{ formatDate(car.listedTime) }}</td>
              <td>
                <span :class="car.status === 'on_sale' ? 'badge badge-green' : 'badge badge-gray'">
                  {{ car.status === 'on_sale' ? '在售' : '停售' }}
                </span>
              </td>
              <td>
                <router-link :to="`/admin/cars/${car.carId}/edit`" class="btn btn-sm btn-primary">
                  编辑
                </router-link>
                <button
                  v-if="car.status === 'on_sale'"
                  class="btn btn-sm btn-danger"
                  @click="handleStopSell(car.carId)"
                >
                  停售
                </button>
              </td>
            </tr>
            <tr v-if="cars.length === 0">
              <td colspan="11" style="text-align: center; color: #999;">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchCars, updateCar } from '../api/index.js'

const cars = ref([])
const loading = ref(false)
const error = ref('')
const searchKeyword = ref('')

onMounted(() => loadCars())

async function loadCars(keyword) {
  loading.value = true
  error.value = ''
  try {
    const params = {}
    if (keyword) params.keyword = keyword
    cars.value = await fetchCars(params)
  } catch (e) {
    error.value = '获取车辆列表失败：' + (e.message || '网络错误')
  } finally {
    loading.value = false
  }
}

function doSearch() {
  loadCars(searchKeyword.value.trim())
}

function resetSearch() {
  searchKeyword.value = ''
  loadCars()
}

async function handleStopSell(id) {
  if (!confirm('确定要停售该车辆吗？')) return
  try {
    await updateCar(id, { status: 'sold_out' })
    const car = cars.value.find(c => c.carId === id)
    if (car) car.status = 'sold_out'
  } catch (e) {
    alert('操作失败：' + (e.message || '网络错误'))
  }
}

function formatPrice(price) {
  if (price == null) return '-'
  return '¥' + Number(price).toLocaleString()
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN')
}
</script>
