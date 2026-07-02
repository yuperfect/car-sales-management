<template>
  <div>
    <div class="card">
      <div class="card-header">客户管理</div>

      <!-- Search -->
      <div class="search-bar">
        <input
          v-model="searchKeyword"
          class="form-input"
          placeholder="搜索姓名 / 电话..."
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
              <th>用户名</th>
              <th>姓名</th>
              <th>电话</th>
              <th>邮箱</th>
              <th>注册时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.name }}</td>
              <td>{{ user.phone }}</td>
              <td>{{ user.email }}</td>
              <td>{{ formatDate(user.createdAt) }}</td>
            </tr>
            <tr v-if="users.length === 0">
              <td colspan="6" style="text-align: center; color: #999;">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchUsers } from '../api/index.js'

const users = ref([])
const loading = ref(false)
const error = ref('')
const searchKeyword = ref('')

onMounted(() => loadUsers())

async function loadUsers(keyword) {
  loading.value = true
  error.value = ''
  try {
    const params = {}
    if (keyword) params.keyword = keyword
    users.value = await fetchUsers(params)
  } catch (e) {
    error.value = '获取客户列表失败：' + (e.message || '网络错误')
  } finally {
    loading.value = false
  }
}

function doSearch() {
  loadUsers(searchKeyword.value.trim())
}

function resetSearch() {
  searchKeyword.value = ''
  loadUsers()
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>
