<template>
  <div>
    <div class="card-header" style="margin-bottom: 20px;">统计分析</div>

    <!-- Loading -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- Error -->
    <div v-else-if="error" class="error-msg">{{ error }}</div>

    <!-- Charts -->
    <div v-else class="charts-row">
      <!-- Test Drive Hot -->
      <div class="card">
        <div class="card-header">各车型试驾预约次数排行</div>
        <div ref="testDriveChartRef" class="chart-container"></div>
      </div>

      <!-- Sales Hot -->
      <div class="card">
        <div class="card-header">各车型销量排行</div>
        <div ref="salesChartRef" class="chart-container"></div>
      </div>

      <!-- Sales Share (spans full width) -->
      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">各车型销售额占比</div>
        <div ref="shareChartRef" class="chart-container" style="min-height: 400px; max-width: 600px; margin: 0 auto;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { fetchTestDriveHotStats, fetchSalesHotStats, fetchSalesShareStats } from '../api/index.js'

const loading = ref(false)
const error = ref('')

const testDriveChartRef = ref(null)
const salesChartRef = ref(null)
const shareChartRef = ref(null)

let testDriveChart = null
let salesChart = null
let shareChart = null

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const [testDriveData, salesData, shareData] = await Promise.all([
      fetchTestDriveHotStats(),
      fetchSalesHotStats(),
      fetchSalesShareStats()
    ])
    await nextTick()
    renderTestDriveChart(testDriveData)
    renderSalesChart(salesData)
    renderShareChart(shareData)
  } catch (e) {
    error.value = '获取统计数据失败：' + (e.message || '网络错误')
  } finally {
    loading.value = false
  }

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  testDriveChart?.dispose()
  salesChart?.dispose()
  shareChart?.dispose()
})

function handleResize() {
  testDriveChart?.resize()
  salesChart?.resize()
  shareChart?.resize()
}

function renderTestDriveChart(data) {
  if (!testDriveChartRef.value) return
  testDriveChart = echarts.init(testDriveChartRef.value)

  const items = Array.isArray(data) ? data : (data?.list || data?.data || [])
  const categories = items.map(item => item.name || item.model || item.category || '-')
  const values = items.map(item => item.count || item.value || 0)

  testDriveChart.setOption({
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
          { offset: 0, color: '#1890ff' },
          { offset: 1, color: '#69c0ff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barMaxWidth: 50
    }]
  })
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
</script>
