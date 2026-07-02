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
              <th>分类</th>
              <th>年份</th>
              <th>颜色</th>
              <th>价格(元)</th>
              <th>库存</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="car in cars" :key="car.id">
              <td>{{ car.id }}</td>
              <td>{{ car.brand }}</td>
              <td>{{ car.model }}</td>
              <td><span class="badge badge-blue">{{ car.category }}</span></td>
              <td>{{ car.year }}</td>
              <td>{{ car.color }}</td>
              <td>{{ formatPrice(car.price) }}</td>
              <td>
                <span :class="car.stock > 0 ? 'badge badge-green' : 'badge badge-red'">
                  {{ car.stock }}
                </span>
              </td>
              <td>
                <span :class="car.status === '上架' ? 'badge badge-green' : 'badge badge-gray'">
                  {{ car.status || '上架' }}
                </span>
              </td>
              <td>
                <router-link :to="`/admin/cars/${car.id}/edit`" class="btn btn-sm btn-primary">
                  编辑
                </router-link>
                <button class="btn btn-sm btn-danger" @click="handleDelete(car.id)">删除</button>
              </td>
            </tr>
            <tr v-if="cars.length === 0">
              <td colspan="10" style="text-align: center; color: #999;">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchCars, deleteCar } from '../api/index.js'

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

async function handleDelete(id) {
  if (!confirm('确定要删除该车辆吗？')) return
  try {
    await deleteCar(id)
    cars.value = cars.value.filter(c => c.id !== id)
  } catch (e) {
    alert('删除失败：' + (e.message || '网络错误'))
  }
}

function formatPrice(price) {
  if (price == null) return '-'
  return '¥' + Number(price).toLocaleString()
}
</script>
