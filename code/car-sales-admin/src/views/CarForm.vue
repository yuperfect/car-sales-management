<template>
  <div>
    <div class="card">
      <div class="card-header">{{ isEdit ? '编辑车辆' : '新增车辆' }}</div>

      <div v-if="error" class="error-msg">{{ error }}</div>

      <form @submit.prevent="handleSubmit">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">品牌 <span style="color: #ff4d4f;">*</span></label>
            <input v-model="form.brand" class="form-input" placeholder="如：宝马" required />
          </div>
          <div class="form-group">
            <label class="form-label">车型 <span style="color: #ff4d4f;">*</span></label>
            <input v-model="form.model" class="form-input" placeholder="如：X5" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">分类 <span style="color: #ff4d4f;">*</span></label>
            <select v-model="form.category" class="form-select" required>
              <option value="">请选择分类</option>
              <option value="SUV">SUV</option>
              <option value="轿车">轿车</option>
              <option value="跑车">跑车</option>
              <option value="MPV">MPV</option>
              <option value="新能源">新能源</option>
              <option value="豪华车">豪华车</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">年份 <span style="color: #ff4d4f;">*</span></label>
            <input v-model.number="form.year" class="form-input" type="number" placeholder="如：2024" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">颜色</label>
            <input v-model="form.color" class="form-input" placeholder="如：白色" />
          </div>
          <div class="form-group">
            <label class="form-label">价格(元) <span style="color: #ff4d4f;">*</span></label>
            <input v-model.number="form.price" class="form-input" type="number" placeholder="如：500000" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">库存 <span style="color: #ff4d4f;">*</span></label>
            <input v-model.number="form.stock" class="form-input" type="number" placeholder="0" min="0" required />
          </div>
          <div class="form-group">
            <label class="form-label">状态</label>
            <select v-model="form.status" class="form-select">
              <option value="上架">上架</option>
              <option value="下架">下架</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">描述</label>
          <textarea v-model="form.description" class="form-textarea" placeholder="车辆描述信息..."></textarea>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            {{ submitting ? '保存中...' : '保存' }}
          </button>
          <router-link to="/admin/cars" class="btn">取消</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchCarById, createCar, updateCar } from '../api/index.js'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !route.meta.isNew)
const carId = computed(() => route.params.id)
const submitting = ref(false)
const error = ref('')

const form = ref({
  brand: '',
  model: '',
  category: '',
  year: new Date().getFullYear(),
  color: '',
  price: 0,
  stock: 0,
  status: '上架',
  description: ''
})

onMounted(async () => {
  if (isEdit.value && carId.value) {
    try {
      const data = await fetchCarById(carId.value)
      form.value = {
        brand: data.brand || '',
        model: data.model || '',
        category: data.category || '',
        year: data.year || new Date().getFullYear(),
        color: data.color || '',
        price: data.price || 0,
        stock: data.stock || 0,
        status: data.status || '上架',
        description: data.description || ''
      }
    } catch (e) {
      error.value = '获取车辆信息失败：' + (e.message || '网络错误')
    }
  }
})

async function handleSubmit() {
  submitting.value = true
  error.value = ''
  try {
    if (isEdit.value) {
      await updateCar(carId.value, form.value)
    } else {
      await createCar(form.value)
    }
    router.push('/admin/cars')
  } catch (e) {
    error.value = '保存失败：' + (e.message || '网络错误')
  } finally {
    submitting.value = false
  }
}
</script>
