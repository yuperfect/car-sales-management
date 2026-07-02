<template>
  <div>
    <div class="card">
      <div class="card-header">综合查询</div>

      <!-- Query form -->
      <div class="form-input-group" style="margin-bottom: 20px;">
        <div class="form-group">
          <label class="form-label">车型分类</label>
          <select v-model="form.category" class="form-select">
            <option value="">全部</option>
            <option value="SUV">SUV</option>
            <option value="轿车">轿车</option>
            <option value="跑车">跑车</option>
            <option value="MPV">MPV</option>
            <option value="新能源">新能源</option>
            <option value="豪华车">豪华车</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">品牌</label>
          <input v-model="form.brand" class="form-input" placeholder="如：宝马" />
        </div>
        <div class="form-group">
          <label class="form-label">最低价</label>
          <input v-model.number="form.minPrice" class="form-input" type="number" placeholder="0" />
        </div>
        <div class="form-group">
          <label class="form-label">最高价</label>
          <input v-model.number="form.maxPrice" class="form-input" type="number" placeholder="999999" />
        </div>
        <div class="form-group">
          <label class="form-label">客户名</label>
          <input v-model="form.customerName" class="form-input" placeholder="客户姓名" />
        </div>
        <div class="form-group">
          <label class="form-label">开始日期</label>
          <input v-model="form.startDate" class="form-input" type="date" />
        </div>
        <div class="form-group">
          <label class="form-label">结束日期</label>
          <input v-model="form.endDate" class="form-input" type="date" />
        </div>
        <div class="form-group" style="align-self: flex-end;">
          <button class="btn btn-primary" @click="doQuery" :disabled="querying">
            {{ querying ? '查询中...' : '🔍 查询' }}
          </button>
          <button class="btn" @click="resetQuery">重置</button>
        </div>
      </div>

      <!-- Error -->
      <div v-if="error" class="error-msg">{{ error }}</div>

      <!-- Results -->
      <div v-if="queried" class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>客户</th>
              <th>品牌</th>
              <th>车型</th>
              <th>分类</th>
              <th>数量</th>
              <th>总价(元)</th>
              <th>下单时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in results" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.customerName || item.username || '-' }}</td>
              <td>{{ item.brand || '-' }}</td>
              <td>{{ item.model || '-' }}</td>
              <td><span class="badge badge-blue">{{ item.category || '-' }}</span></td>
              <td>{{ item.quantity ?? '-' }}</td>
              <td>{{ formatPrice(item.totalPrice) }}</td>
              <td>{{ formatDateTime(item.createdAt) }}</td>
            </tr>
            <tr v-if="results.length === 0">
              <td colspan="8" style="text-align: center; color: #999;">未找到匹配的记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { querySales } from '../api/index.js'

const form = reactive({
  category: '',
  brand: '',
  minPrice: '',
  maxPrice: '',
  customerName: '',
  startDate: '',
  endDate: ''
})

const results = ref([])
const querying = ref(false)
const error = ref('')
const queried = ref(false)

async function doQuery() {
  querying.value = true
  error.value = ''
  queried.value = true

  try {
    const params = {}
    if (form.category) params.category = form.category
    if (form.brand) params.brand = form.brand
    if (form.minPrice !== '' && form.minPrice !== null) params.minPrice = form.minPrice
    if (form.maxPrice !== '' && form.maxPrice !== null) params.maxPrice = form.maxPrice
    if (form.customerName) params.customerName = form.customerName
    if (form.startDate) params.startDate = form.startDate
    if (form.endDate) params.endDate = form.endDate

    results.value = await querySales(params)
  } catch (e) {
    error.value = '查询失败：' + (e.message || '网络错误')
    results.value = []
  } finally {
    querying.value = false
  }
}

function resetQuery() {
  form.category = ''
  form.brand = ''
  form.minPrice = ''
  form.maxPrice = ''
  form.customerName = ''
  form.startDate = ''
  form.endDate = ''
  results.value = []
  queried.value = false
  error.value = ''
}

function formatPrice(price) {
  if (price == null) return '-'
  return '¥' + Number(price).toLocaleString()
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>
