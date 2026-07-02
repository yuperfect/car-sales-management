<template>
  <div>
    <!-- Loading -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>正在加载预约数据...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="fetchData">重新加载</button>
    </div>

    <!-- Empty -->
    <div v-else-if="list.length === 0" class="empty-state">
      <div class="empty-icon">📅</div>
      <p>暂无试驾预约记录</p>
      <router-link to="/test-drive/new" class="btn btn-primary" style="display: inline-block; margin-top: 12px;">
        立即预约试驾
      </router-link>
    </div>

    <!-- 表格 -->
    <div v-else class="card">
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>车辆</th>
              <th>预约日期</th>
              <th>预约时间</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list" :key="item.id">
              <td>
                <router-link :to="`/cars/${item.carId}`">
                  {{ item.carBrand || item.brand || '—' }} {{ item.carModel || item.model || '' }}
                </router-link>
              </td>
              <td>{{ item.driveDate }}</td>
              <td>{{ item.driveTime }}</td>
              <td>
                <span class="tag" :class="statusClass(item.status)">
                  {{ statusText(item.status) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyTestDrives } from '../api/index.js'

const list = ref([])
const loading = ref(false)
const error = ref('')

function statusClass(status) {
  const map = {
    'pending': 'tag-pending',
    'confirmed': 'tag-confirmed',
    'cancelled': 'tag-cancelled'
  }
  return map[status] || 'tag-pending'
}

function statusText(status) {
  const map = {
    'pending': '待确认',
    'confirmed': '已确认',
    'cancelled': '已取消'
  }
  return map[status] || status
}

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    list.value = await getMyTestDrives()
  } catch (e) {
    error.value = e.message || '获取预约列表失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
