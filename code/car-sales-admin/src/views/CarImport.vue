<template>
  <div>
    <div class="card">
      <div class="card-header">Excel 导入车辆</div>

      <p style="color: #666; margin-bottom: 12px;">
        上传 .xlsx 文件，批量导入车辆信息。文件需包含：品牌、车型、排量、变速箱、颜色、价格、库存、车辆图片等列。
      </p>
      <p style="color: #999; font-size: 13px; margin-bottom: 20px;">
        💡 如需为车辆配图，请将图片直接插入到 Excel 的"车辆图片"列对应行的单元格位置。
      </p>

      <!-- Download Template -->
      <div style="margin-bottom: 16px;">
        <button class="btn" :disabled="downloading" @click="handleDownloadTemplate">
          {{ downloading ? '下载中...' : '📄 下载模板' }}
        </button>
      </div>

      <!-- Upload Area -->
      <div
        class="upload-area"
        @click="triggerUpload"
        @dragover.prevent
        @drop.prevent="handleDrop"
      >
        <div class="upload-icon">📁</div>
        <div class="upload-text">点击或拖拽文件到此区域上传</div>
        <div class="upload-hint">仅支持 .xlsx 格式</div>
      </div>
      <input
        ref="fileInput"
        type="file"
        accept=".xlsx"
        style="display: none;"
        @change="handleFileChange"
      />

      <!-- Selected file -->
      <div v-if="selectedFile" style="margin-top: 16px; display: flex; align-items: center; gap: 12px;">
        <span>已选择：{{ selectedFile.name }} ({{ formatSize(selectedFile.size) }})</span>
        <button class="btn btn-primary" :disabled="uploading" @click="uploadFile">
          {{ uploading ? '上传中...' : '开始上传' }}
        </button>
        <button class="btn" @click="clearFile">重新选择</button>
      </div>

      <!-- Error message -->
      <div v-if="error" class="error-msg" style="margin-top: 16px;">{{ error }}</div>

      <!-- Import Result -->
      <div v-if="result" class="import-result" :class="result.success > 0 ? 'success' : 'error'">
        <div style="font-size: 20px; margin-bottom: 8px;">
          {{ result.success > 0 ? '✅ 导入完成' : '❌ 导入失败' }}
        </div>
        <div v-if="result.success > 0">
          成功：{{ result.success }} / {{ result.total }} 条
          <span v-if="result.withImage > 0" style="margin-left: 12px; color: #1890ff;">
            🖼️ 含图片：{{ result.withImage }} 辆
          </span>
        </div>
        <div v-if="result.message" style="margin-top: 8px; font-size: 13px;">
          {{ result.message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { importCars, downloadTemplate } from '../api/index.js'

const fileInput = ref(null)
const selectedFile = ref(null)
const uploading = ref(false)
const downloading = ref(false)
const error = ref('')
const result = ref(null)

function triggerUpload() {
  fileInput.value?.click()
}

function handleFileChange(e) {
  const file = e.target.files[0]
  if (file) {
    if (!file.name.endsWith('.xlsx')) {
      error.value = '请选择 .xlsx 格式的文件'
      return
    }
    selectedFile.value = file
    error.value = ''
    result.value = null
  }
}

function handleDrop(e) {
  const file = e.dataTransfer.files[0]
  if (file) {
    if (!file.name.endsWith('.xlsx')) {
      error.value = '请选择 .xlsx 格式的文件'
      return
    }
    selectedFile.value = file
    error.value = ''
    result.value = null
  }
}

function clearFile() {
  selectedFile.value = null
  result.value = null
  error.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

async function handleDownloadTemplate() {
  downloading.value = true
  try {
    const blob = await downloadTemplate()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '车辆导入模板.xlsx'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    error.value = '模板下载失败：' + (e.message || '网络错误')
  } finally {
    downloading.value = false
  }
}

async function uploadFile() {
  if (!selectedFile.value) return
  uploading.value = true
  error.value = ''
  result.value = null

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    const data = await importCars(formData)
    result.value = {
      success: data.success ?? 0,
      total: data.total ?? 0,
      withImage: data.withImage ?? 0,
      fail: data.fail ?? 0,
      message: data.message || ''
    }
  } catch (e) {
    error.value = '上传失败：' + (e.message || '网络错误')
  } finally {
    uploading.value = false
  }
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}
</script>
