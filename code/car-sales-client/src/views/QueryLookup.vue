<template>
  <div>
    <div class="card" style="max-width: 640px;">
      <div class="card-body">
        <h3 style="margin-bottom: 20px;">查询中心</h3>

        <!-- 查询预约 -->
        <div style="margin-bottom: 28px; padding-bottom: 24px; border-bottom: 1px solid var(--border);">
          <h4 style="margin-bottom: 12px;">📅 查询预约</h4>
          <div class="flex gap-8" style="align-items: flex-end;">
            <div class="form-group" style="flex: 1; margin-bottom: 0;">
              <input
                type="text"
                class="form-control"
                v-model="apptCode"
                placeholder="请输入预约编号"
                @keyup.enter="queryAppointment"
              />
            </div>
            <button class="btn btn-primary" @click="queryAppointment" :disabled="apptQuerying">
              {{ apptQuerying ? '查询中...' : '查询' }}
            </button>
          </div>
          <div v-if="apptErr" class="alert alert-error" style="margin-top: 8px;">{{ apptErr }}</div>

          <!-- 预约结果 -->
          <div v-if="apptResult" class="detail-specs" style="margin-top: 12px;">
            <div class="spec-item">
              <div class="spec-label">编号</div>
              <div class="spec-value">{{ apptResult.appointmentId }}</div>
            </div>
            <div class="spec-item">
              <div class="spec-label">车辆</div>
              <div class="spec-value">{{ apptResult.carBrand || '—' }} {{ apptResult.carModel || '' }}</div>
            </div>
            <div class="spec-item">
              <div class="spec-label">预约时间</div>
              <div class="spec-value">{{ apptResult.appointmentTime }}</div>
            </div>
            <div class="spec-item">
              <div class="spec-label">状态</div>
              <div class="spec-value">
                <span class="tag" :class="apptStatusClass(apptResult.status)">
                  {{ apptStatusText(apptResult.status) }}
                </span>
              </div>
            </div>
            <div class="spec-item" v-if="apptResult.remark">
              <div class="spec-label">备注</div>
              <div class="spec-value">{{ apptResult.remark }}</div>
            </div>
          </div>
        </div>

        <!-- 查询订单 -->
        <div>
          <h4 style="margin-bottom: 12px;">📦 查询订单</h4>
          <div class="flex gap-8" style="align-items: flex-end;">
            <div class="form-group" style="flex: 1; margin-bottom: 0;">
              <input
                type="text"
                class="form-control"
                v-model="orderCode"
                placeholder="请输入订单编号"
                @keyup.enter="queryOrder"
              />
            </div>
            <button class="btn btn-success" @click="queryOrder" :disabled="orderQuerying">
              {{ orderQuerying ? '查询中...' : '查询' }}
            </button>
          </div>
          <div v-if="orderErr" class="alert alert-error" style="margin-top: 8px;">{{ orderErr }}</div>

          <!-- 订单结果 -->
          <div v-if="orderResult" class="detail-specs" style="margin-top: 12px;">
            <div class="spec-item">
              <div class="spec-label">编号</div>
              <div class="spec-value">{{ orderResult.orderId }}</div>
            </div>
            <div class="spec-item">
              <div class="spec-label">车辆</div>
              <div class="spec-value">{{ orderResult.carBrand || '—' }} {{ orderResult.carModel || '' }}</div>
            </div>
            <div class="spec-item">
              <div class="spec-label">数量</div>
              <div class="spec-value">{{ orderResult.quantity }}</div>
            </div>
            <div class="spec-item">
              <div class="spec-label">总价</div>
              <div class="spec-value" style="color: var(--danger); font-weight: 600;">
                ¥{{ formatPrice(orderResult.totalAmount || orderResult.totalPrice) }}
              </div>
            </div>
            <div class="spec-item">
              <div class="spec-label">状态</div>
              <div class="spec-value">
                <span class="tag" :class="orderStatusClass(orderResult.status)">
                  {{ orderStatusText(orderResult.status) }}
                </span>
              </div>
            </div>
            <div class="spec-item">
              <div class="spec-label">下单时间</div>
              <div class="spec-value">{{ orderResult.createTime || orderResult.createdAt || '—' }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getAppointmentByCode, getOrderByCode } from '../api/index.js'

// 预约查询
const apptCode = ref('')
const apptResult = ref(null)
const apptQuerying = ref(false)
const apptErr = ref('')

// 订单查询
const orderCode = ref('')
const orderResult = ref(null)
const orderQuerying = ref(false)
const orderErr = ref('')

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

function apptStatusClass(status) {
  const map = { 'pending': 'tag-pending', 'confirmed': 'tag-confirmed', 'cancelled': 'tag-cancelled' }
  return map[status] || 'tag-pending'
}

function apptStatusText(status) {
  const map = { 'pending': '待确认', 'confirmed': '已确认', 'cancelled': '已取消' }
  return map[status] || status
}

function orderStatusClass(status) {
  const map = { 'pending': 'tag-pending', 'confirmed': 'tag-confirmed', 'completed': 'tag-completed', 'cancelled': 'tag-cancelled' }
  return map[status] || 'tag-pending'
}

function orderStatusText(status) {
  const map = { 'pending': '待处理', 'confirmed': '已确认', 'completed': '已完成', 'cancelled': '已取消' }
  return map[status] || status
}

async function queryAppointment() {
  const code = apptCode.value.trim()
  if (!code) { apptErr.value = '请输入预约编号'; return }
  apptQuerying.value = true
  apptErr.value = ''
  apptResult.value = null
  try {
    apptResult.value = await getAppointmentByCode(code)
  } catch (e) {
    apptErr.value = e.message || '查询失败，请检查编号是否正确'
  } finally {
    apptQuerying.value = false
  }
}

async function queryOrder() {
  const code = orderCode.value.trim()
  if (!code) { orderErr.value = '请输入订单编号'; return }
  orderQuerying.value = true
  orderErr.value = ''
  orderResult.value = null
  try {
    orderResult.value = await getOrderByCode(code)
  } catch (e) {
    orderErr.value = e.message || '查询失败，请检查编号是否正确'
  } finally {
    orderQuerying.value = false
  }
}
</script>
