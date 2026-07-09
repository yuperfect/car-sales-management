<template>
  <div>
    <!-- 查询区域 -->
    <div class="card" style="max-width: 560px; margin-bottom: 20px;">
      <div class="card-body">
        <div class="flex gap-8" style="align-items: flex-end;">
          <div class="form-group" style="flex: 1; margin-bottom: 0;">
            <label class="form-label">预约编号</label>
            <input
              type="text"
              class="form-control"
              v-model="queryCode"
              placeholder="请输入预约编号"
              @keyup.enter="handleQuery"
            />
          </div>
          <button class="btn btn-primary" @click="handleQuery" :disabled="querying">
            {{ querying ? '查询中...' : '查询' }}
          </button>
        </div>
        <div v-if="queryErr" class="alert alert-error" style="margin-top: 12px;">{{ queryErr }}</div>
      </div>
    </div>

    <!-- 结果详情 -->
    <div v-if="appointment" class="card">
      <div class="card-body">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
          <h3 style="margin: 0;">预约详情</h3>
          <span class="tag" :class="statusClass(appointment.status)">
            {{ statusText(appointment.status) }}
          </span>
        </div>

        <div class="detail-specs">
          <div class="spec-item">
            <div class="spec-label">预约编号</div>
            <div class="spec-value">{{ appointment.code }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">姓名</div>
            <div class="spec-value">{{ appointment.customerName }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">电话</div>
            <div class="spec-value">{{ appointment.customerPhone }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">车辆</div>
            <div class="spec-value">
              <router-link :to="`/cars/${appointment.carId}`">
                {{ appointment.carBrand || appointment.brand || '—' }} {{ appointment.carModel || appointment.model || '' }}
              </router-link>
            </div>
          </div>
          <div class="spec-item">
            <div class="spec-label">预约时间</div>
            <div class="spec-value">{{ appointment.appointmentTime }}</div>
          </div>
          <div class="spec-item" v-if="appointment.remark">
            <div class="spec-label">备注</div>
            <div class="spec-value">{{ appointment.remark }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getAppointmentByCode } from '../api/index.js'

const queryCode = ref('')
const appointment = ref(null)
const querying = ref(false)
const queryErr = ref('')

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

async function handleQuery() {
  const code = queryCode.value.trim()
  if (!code) {
    queryErr.value = '请输入预约编号'
    return
  }

  querying.value = true
  queryErr.value = ''
  appointment.value = null

  try {
    appointment.value = await getAppointmentByCode(code)
  } catch (e) {
    queryErr.value = e.message || '查询失败，请检查编号是否正确'
  } finally {
    querying.value = false
  }
}
</script>
