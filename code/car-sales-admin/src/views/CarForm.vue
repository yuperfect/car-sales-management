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
            <label class="form-label">排量 <span style="color: #ff4d4f;">*</span></label>
            <input v-model="form.displacement" class="form-input" placeholder="如：2.0L" required />
          </div>
          <div class="form-group">
            <label class="form-label">变速箱 <span style="color: #ff4d4f;">*</span></label>
            <input v-model="form.transmission" class="form-input" placeholder="如：CVT无级变速" required />
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
              <option value="on_sale">在售</option>
              <option value="sold_out">停售</option>
            </select>
          </div>
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
  displacement: '',
  transmission: '',
  color: '',
  price: 0,
  stock: 0,
  status: 'on_sale'
})

onMounted(async () => {
  if (isEdit.value && carId.value) {
    try {
      const data = await fetchCarById(carId.value)
      form.value = {
        brand: data.brand || '',
        model: data.model || '',
        displacement: data.displacement || '',
        transmission: data.transmission || '',
        color: data.color || '',
        price: data.price || 0,
        stock: data.stock || 0,
        status: data.status || 'on_sale'
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
