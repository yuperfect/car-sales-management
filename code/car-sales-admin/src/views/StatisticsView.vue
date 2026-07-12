<template>
  <div>
    <div class="card-header" style="margin-bottom: 20px;">统计分析</div>

    <!-- Toggle Buttons -->
    <div class="tab-bar" style="display: flex; gap: 12px; margin-bottom: 20px;">
      <button
        class="btn"
        :class="activeTab === 'sales' ? 'btn-primary' : 'btn-outline'"
        @click="switchTab('sales')"
      >
        销售分析
      </button>
      <button
        class="btn"
        :class="activeTab === 'appointment' ? 'btn-primary' : 'btn-outline'"
        @click="switchTab('appointment')"
      >
        预约分析
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- Error -->
    <div v-else-if="error" class="error-msg">{{ error }}</div>

    <!-- Sales Charts -->
    <div v-else-if="activeTab === 'sales'" class="charts-row">
      <div class="card">
        <div class="card-header">各车型销量排行</div>
        <div ref="salesChartRef" class="chart-container"></div>
      </div>

      <div class="card">
        <div class="card-header">热销价格区间</div>
        <div ref="priceRangeChartRef" class="chart-container"></div>
      </div>

      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">各车型销售额占比</div>
        <div ref="shareChartRef" class="chart-container" style="min-height: 400px; max-width: 600px; margin: 0 auto;"></div>
      </div>
    </div>

    <!-- Appointment Chart -->
    <div v-else-if="activeTab === 'appointment'" class="charts-row">
      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">预约热点车型排行</div>
        <div ref="appointmentChartRef" class="chart-container" style="min-height: 400px; max-width: 800px; margin: 0 auto;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { fetchSalesHotStats, fetchSalesShareStats, fetchPriceRangeStats, fetchAppointmentHotStats } from '../api/index.js'

const activeTab = ref('sales')
const loading = ref(false)
const error = ref('')

const salesChartRef = ref(null)
const priceRangeChartRef = ref(null)
const shareChartRef = ref(null)
const appointmentChartRef = ref(null)

let salesChart = null
let priceRangeChart = null
let shareChart = null
let appointmentChart = null

onMounted(async () => {
  await loadData('sales')
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})

function disposeCharts() {
  salesChart?.dispose()
  salesChart = null
  priceRangeChart?.dispose()
  priceRangeChart = null
  shareChart?.dispose()
  shareChart = null
  appointmentChart?.dispose()
  appointmentChart = null
}

function handleResize() {
  salesChart?.resize()
  priceRangeChart?.resize()
  shareChart?.resize()
  appointmentChart?.resize()
}

async function switchTab(tab) {
  activeTab.value = tab
  disposeCharts()
  await loadData(tab)
}

async function loadData(tab) {
  loading.value = true
  error.value = ''

  try {
    if (tab === 'sales') {
      const [salesData, shareData, priceRangeData] = await Promise.all([
        fetchSalesHotStats(),
        fetchSalesShareStats(),
        fetchPriceRangeStats()
      ])
      loading.value = false
      await nextTick()
      if (salesData) renderSalesChart(salesData)
      if (shareData) renderShareChart(shareData)
      if (priceRangeData) renderPriceRangeChart(priceRangeData)
    } else if (tab === 'appointment') {
      const appointmentData = await fetchAppointmentHotStats()
      loading.value = false
      await nextTick()
      if (appointmentData) renderAppointmentChart(appointmentData)
    }
  } catch (e) {
    error.value = '获取统计数据失败：' + (e.message || '网络错误')
    loading.value = false
  }
}

function renderSalesChart(data) {
  if (!salesChartRef.value) return
  salesChart = echarts.init(salesChartRef.value)

  const items = Array.isArray(data) ? data : (data?.list || data?.data || [])
  const categories = items.map(item => item.name || item.model || item.category || '-')
  const values = items.map(item => item.count || item.sales || item.value || 0)

  salesChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: { rotate: 30, fontSize: 12 }
    },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#52c41a' },
          { offset: 1, color: '#95de64' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barMaxWidth: 50
    }]
  })
}

function renderPriceRangeChart(data) {
  if (!priceRangeChartRef.value) return
  priceRangeChart = echarts.init(priceRangeChartRef.value)

  const items = Array.isArray(data) ? data : (data?.list || data?.data || [])
  const names = items.map(item => item.range || item.name || '-')
  const values = items.map(item => item.count || item.sales || item.value || 0)

  priceRangeChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 辆 ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['30%', '60%'],
      center: ['50%', '55%'],
      data: names.map((name, i) => ({ name, value: values[i] })),
      label: {
        formatter: '{b}: {c} 辆',
        fontSize: 12
      },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      }
    }]
  })
}

function renderShareChart(data) {
  if (!shareChartRef.value) return
  shareChart = echarts.init(shareChartRef.value)

  const items = Array.isArray(data) ? data : (data?.list || data?.data || [])
  const names = items.map(item => item.name || item.model || item.category || '-')
  const values = items.map(item => item.amount || item.sales || item.value || 0)

  shareChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['30%', '60%'],
      center: ['50%', '55%'],
      data: names.map((name, i) => ({ name, value: values[i] })),
      label: {
        formatter: '{b}: ¥{c}',
        fontSize: 12
      },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      }
    }]
  })
}

function renderAppointmentChart(data) {
  if (!appointmentChartRef.value) return
  appointmentChart = echarts.init(appointmentChartRef.value)

  const items = Array.isArray(data) ? data : (data?.list || data?.data || [])
  const categories = items.map(item => item.name || item.model || item.brand + ' ' + (item.model || '') || '-')
  const values = items.map(item => item.count || item.value || 0)

  appointmentChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: { rotate: 30, fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      name: '预约次数'
    },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#1890ff' },
          { offset: 1, color: '#69c0ff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barMaxWidth: 50
    }]
  })
}
</script>
