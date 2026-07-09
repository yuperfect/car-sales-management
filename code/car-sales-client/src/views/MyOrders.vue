<template>
  <div>
    <!-- 查询区域 -->
    <div class="card" style="max-width: 560px; margin-bottom: 20px;">
      <div class="card-body">
        <div class="flex gap-8" style="align-items: flex-end;">
          <div class="form-group" style="flex: 1; margin-bottom: 0;">
            <label class="form-label">订单编号</label>
            <input
              type="text"
              class="form-control"
              v-model="queryCode"
              placeholder="请输入订单编号"
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
    <div v-if="order" class="card">
      <div class="card-body">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
          <h3 style="margin: 0;">订单详情</h3>
          <span class="tag" :class="statusClass(order.status)">
            {{ statusText(order.status) }}
          </span>
        </div>

        <div class="detail-specs">
          <div class="spec-item">
            <div class="spec-label">订单编号</div>
            <div class="spec-value">{{ order.code || order.id }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">车辆</div>
            <div class="spec-value">
              <router-link :to="`/cars/${order.carId}`">
                {{ order.carBrand || order.brand || '—' }} {{ order.carModel || order.model || '' }}
              </router-link>
            </div>
          </div>
          <div class="spec-item">
            <div class="spec-label">数量</div>
            <div class="spec-value">{{ order.quantity }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">单价</div>
            <div class="spec-value">¥{{ formatPrice(order.unitPrice || order.price) }}</div>
          </div>
          <div class="spec-item">
            <div class="spec-label">总价</div>
            <div class="spec-value" style="color: var(--danger); font-weight: 600;">
              ¥{{ formatPrice(order.totalAmount || order.totalPrice) }}
            </div>
          </div>
          <div class="spec-item">
            <div class="spec-label">下单时间</div>
            <div class="spec-value">{{ order.createTime || order.createdAt || '—' }}</div>
          </div>
          <div class="spec-item" v-if="order.handler">
            <div class="spec-label">处理人</div>
            <div class="spec-value">{{ order.handler }}</div>
          </div>
          <div class="spec-item" v-if="order.handleTime">
            <div class="spec-label">处理时间</div>
            <div class="spec-value">{{ order.handleTime }}</div>
          </div>
        </div>

        <div style="margin-top: 20px;">
          <button
            v-if="order.status === 'pending'"
            class="btn btn-danger"
            @click="handleCancel(order)"
            :disabled="cancelling"
          >
            {{ cancelling ? '取消中...' : '取消订单' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getOrderByCode, cancelOrder } from '../api/index.js'

const queryCode = ref('')
const order = ref(null)
const querying = ref(false)
const queryErr = ref('')
const cancelling = ref(false)

function statusClass(status) {
  const map = {
    'pending': 'tag-pending',
    'confirmed': 'tag-confirmed',
    'completed': 'tag-completed',
    'cancelled': 'tag-cancelled'
  }
  return map[status] || 'tag-pending'
}

function statusText(status) {
  const map = {
    'pending': '待处理',
    'confirmed': '已确认',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return map[status] || status
}

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

async function handleQuery() {
  const code = queryCode.value.trim()
  if (!code) {
    queryErr.value = '请输入订单编号'
    return
  }

  querying.value = true
  queryErr.value = ''
  order.value = null

  try {
    order.value = await getOrderByCode(code)
  } catch (e) {
    queryErr.value = e.message || '查询失败，请检查编号是否正确'
  } finally {
    querying.value = false
  }
}

async function handleCancel(item) {
  if (!confirm(`确定要取消订单 #${item.code || item.id} 吗？`)) return
  cancelling.value = true
  try {
    await cancelOrder(item.id)
    order.value.status = 'cancelled'
  } catch (e) {
    alert('取消失败: ' + (e.message || '请重试'))
  } finally {
    cancelling.value = false
  }
}
</script>
