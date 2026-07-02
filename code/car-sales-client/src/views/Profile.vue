<template>
  <div>
    <div class="card" style="max-width: 560px;">
      <div class="card-body">
        <!-- Alert -->
        <div v-if="successMsg" class="alert alert-success">{{ successMsg }}</div>
        <div v-if="errMsg" class="alert alert-error">{{ errMsg }}</div>

        <!-- Loading -->
        <div v-if="loading" class="loading">
          <div class="loading-spinner"></div>
          <p>正在加载个人信息...</p>
        </div>

        <!-- Error on load -->
        <div v-else-if="loadError" class="error-message">
          <p>{{ loadError }}</p>
          <button class="btn btn-primary" @click="fetchProfile">重新加载</button>
        </div>

        <!-- Form -->
        <form v-else @submit.prevent="handleSave">
          <div class="form-group">
            <label class="form-label">用户 ID</label>
            <input type="text" class="form-control" :value="profile.id" disabled />
          </div>

          <div class="form-group">
            <label class="form-label">姓名</label>
            <input
              type="text"
              class="form-control"
              v-model="profile.name"
              required
              placeholder="请输入姓名"
            />
          </div>

          <div class="form-group">
            <label class="form-label">电话</label>
            <input
              type="tel"
              class="form-control"
              v-model="profile.phone"
              required
              placeholder="请输入电话号码"
            />
          </div>

          <div class="form-group">
            <label class="form-label">邮箱</label>
            <input
              type="email"
              class="form-control"
              v-model="profile.email"
              placeholder="请输入邮箱地址"
            />
          </div>

          <div class="form-group">
            <label class="form-label">角色</label>
            <input type="text" class="form-control" :value="profile.role" disabled />
          </div>

          <div class="mt-24">
            <button type="submit" class="btn btn-primary btn-lg" :disabled="saving">
              {{ saving ? '保存中...' : '保存修改' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserById, updateUser } from '../api/index.js'

const profile = reactive({
  id: '',
  name: '',
  phone: '',
  email: '',
  role: ''
})

const loading = ref(false)
const loadError = ref('')
const saving = ref(false)
const successMsg = ref('')
const errMsg = ref('')

async function fetchProfile() {
  loading.value = true
  loadError.value = ''
  try {
    const data = await getUserById()
    Object.assign(profile, data)
  } catch (e) {
    loadError.value = e.message || '获取个人信息失败'
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!profile.name.trim()) {
    errMsg.value = '姓名不能为空'
    return
  }
  if (!profile.phone.trim()) {
    errMsg.value = '电话不能为空'
    return
  }

  saving.value = true
  successMsg.value = ''
  errMsg.value = ''

  try {
    await updateUser(profile.id, {
      name: profile.name,
      phone: profile.phone,
      email: profile.email
    })
    successMsg.value = '个人信息更新成功！'
    setTimeout(() => {
      successMsg.value = ''
    }, 3000)
  } catch (e) {
    errMsg.value = e.message || '保存失败，请重试'
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>
