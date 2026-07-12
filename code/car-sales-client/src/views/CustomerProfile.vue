<template>
  <div>
    <!-- ====== 未设置身份：引导提示 ====== -->
    <div v-if="!isIdentified" class="card" style="max-width: 480px;">
      <div class="card-body" style="text-align: center; padding: 40px;">
        <div style="font-size: 48px; margin-bottom: 16px;">👤</div>
        <h3 style="margin-bottom: 8px;">设置您的身份</h3>
        <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 24px;">
          请输入一个用户名，即可开始使用系统
        </p>

        <div v-if="errMsg" class="alert alert-error" style="margin-bottom: 12px;">{{ errMsg }}</div>

        <div class="form-group">
          <label class="form-label">用户名</label>
          <div style="display: flex; gap: 8px;">
            <input type="text" class="form-control" v-model="switchUsername"
                   placeholder="请输入用户名" @keyup.enter="handleSwitchUser" />
            <button class="btn btn-primary" @click="handleSwitchUser" :disabled="switching"
                    style="white-space: nowrap;">
              {{ switching ? '设置中...' : '确认' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 已设置身份：个人信息展示/编辑 ====== -->
    <div v-else class="card" style="max-width: 560px;">
      <div class="card-body">
        <!-- 操作按钮栏 -->
        <div class="action-bar">
          <button class="btn btn-outline" @click="openSwitchModal">
            🔄 切换用户
          </button>
          <button v-if="!isEditing" class="btn btn-primary" @click="enterEditMode">
            ✏️ 修改用户信息
          </button>
          <div v-else class="btn-group">
            <button class="btn btn-outline" @click="cancelEdit">取消</button>
            <button class="btn btn-primary" @click="handleSave" :disabled="submitting">
              {{ submitting ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>

        <div v-if="errMsg" class="alert alert-error" style="margin-bottom: 12px;">{{ errMsg }}</div>
        <div v-if="successMsg" class="alert alert-success" style="margin-bottom: 12px;">{{ successMsg }}</div>

        <!-- 头像 -->
        <div class="avatar-section">
          <div class="avatar-wrapper" :class="{ clickable: isEditing }" @click="isEditing && triggerAvatarUpload()">
            <img v-if="editForm.avatarPreview || profile.avatarUrl"
                 :src="editForm.avatarPreview || profile.avatarUrl"
                 class="avatar-img" />
            <div v-else class="avatar-placeholder">📷</div>
            <div v-if="isEditing" class="avatar-overlay">点击更换</div>
          </div>
          <input type="file" ref="avatarInput" accept="image/jpeg,image/png,image/webp"
                 style="display: none" @change="onAvatarChange" />
        </div>

        <!-- 表单 -->
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input type="text" class="form-control" :value="profile.username" disabled style="background: #f5f5f5; color: #999;" />
        </div>
        <div class="form-group">
          <label class="form-label">姓名</label>
          <input type="text" class="form-control" v-model="editForm.realName" :disabled="!isEditing" />
        </div>
        <div class="form-group">
          <label class="form-label">手机</label>
          <input type="tel" class="form-control" v-model="editForm.phone" maxlength="11" :disabled="!isEditing" />
        </div>
        <div class="form-group">
          <label class="form-label">性别</label>
          <div class="radio-group">
            <label class="radio-label"><input type="radio" v-model="editForm.gender" value="男" :disabled="!isEditing" /> 男</label>
            <label class="radio-label"><input type="radio" v-model="editForm.gender" value="女" :disabled="!isEditing" /> 女</label>
            <label class="radio-label"><input type="radio" v-model="editForm.gender" value="保密" :disabled="!isEditing" /> 保密</label>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">生日</label>
          <input type="date" class="form-control" v-model="editForm.birthday" :disabled="!isEditing" />
        </div>
        <div class="form-group">
          <label class="form-label">邮箱</label>
          <input type="email" class="form-control" v-model="editForm.email" placeholder="请输入邮箱" :disabled="!isEditing" />
        </div>
        <div class="form-group">
          <label class="form-label">地址</label>
          <input type="text" class="form-control" v-model="editForm.address" placeholder="请输入地址" :disabled="!isEditing" />
        </div>

        <!-- 我的概览 -->
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-num">{{ stats.appointments }}</div>
            <div class="stat-label">预约</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">{{ stats.orders }}</div>
            <div class="stat-label">订单</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">{{ formatDate(profile.firstSubmitTime) }}</div>
            <div class="stat-label">注册时间</div>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 切换用户弹窗 ====== -->
    <div v-if="showSwitchModal" class="modal-overlay" @click.self="closeSwitchModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>🔄 切换用户</h3>
          <button class="modal-close" @click="closeSwitchModal">✕</button>
        </div>
        <div class="modal-body">
          <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 16px;">
            输入用户名进行切换。如果该用户不存在，将自动创建新用户。
          </p>
          <div v-if="switchErr" class="alert alert-error" style="margin-bottom: 12px;">{{ switchErr }}</div>

          <div class="form-group">
            <label class="form-label">用户名</label>
            <input type="text" class="form-control" v-model="switchUsername"
                   placeholder="请输入用户名" @keyup.enter="handleSwitchUser" />
          </div>

          <div class="modal-actions">
            <button type="button" class="btn btn-outline" @click="closeSwitchModal">取消</button>
            <button class="btn btn-primary" @click="handleSwitchUser" :disabled="switching">
              {{ switching ? '切换中...' : '确认切换' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getCustomer, findOrCreateCustomer, updateCustomer,
         getAppointmentsByCustomer, getOrdersByCustomer } from '../api/index.js'
import { getCurrentUser, setCurrentUser, isIdentified } from '../utils/user.js'

// ===== 状态 =====
const isEditing = ref(false)
const submitting = ref(false)
const switching = ref(false)
const errMsg = ref('')
const successMsg = ref('')

// ===== 当前用户 =====
const profile = ref({
  customerId: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  address: '',
  gender: '保密',
  birthday: '',
  avatarUrl: '',
  firstSubmitTime: ''
})

// ===== 统计 =====
const stats = reactive({ appointments: 0, orders: 0 })

// ===== 编辑表单 =====
const editForm = reactive({
  realName: '',
  phone: '',
  gender: '保密',
  birthday: '',
  email: '',
  address: '',
  avatarFile: null,
  avatarPreview: null
})

// ===== 切换用户弹窗 =====
const showSwitchModal = ref(false)
const switchUsername = ref('')
const switchErr = ref('')

// ===== 头像上传 =====
const avatarInput = ref(null)

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

function onAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    errMsg.value = '头像文件不能超过 5MB'
    return
  }
  editForm.avatarFile = file
  const reader = new FileReader()
  reader.onload = (ev) => { editForm.avatarPreview = ev.target.result }
  reader.readAsDataURL(file)
}

// ===== 工具 =====
function formatDate(dt) {
  if (!dt) return '—'
  return dt.substring(0, 10)
}

// ===== 加载用户信息 =====
async function loadProfile() {
  const user = getCurrentUser()
  if (!user || !user.customerId) return

  try {
    const data = await getCustomer(user.customerId)
    profile.value = data
    editForm.realName = data.realName || ''
    editForm.phone = data.phone || ''
    editForm.gender = data.gender || '保密'
    editForm.birthday = data.birthday || ''
    editForm.email = data.email || ''
    editForm.address = data.address || ''
    editForm.avatarFile = null
    editForm.avatarPreview = null

    // 加载统计
    const [appts, ords] = await Promise.all([
      getAppointmentsByCustomer(user.customerId),
      getOrdersByCustomer(user.customerId)
    ])
    stats.appointments = Array.isArray(appts) ? appts.length : 0
    stats.orders = Array.isArray(ords) ? ords.length : 0
  } catch (e) {
    errMsg.value = '加载个人信息失败: ' + e.message
  }
}

// ===== 进入编辑模式 =====
function enterEditMode() {
  // 重置编辑表单为当前 profile 的值
  editForm.realName = profile.value.realName || ''
  editForm.phone = profile.value.phone || ''
  editForm.gender = profile.value.gender || '保密'
  editForm.birthday = profile.value.birthday || ''
  editForm.email = profile.value.email || ''
  editForm.address = profile.value.address || ''
  editForm.avatarFile = null
  editForm.avatarPreview = null
  errMsg.value = ''
  successMsg.value = ''
  isEditing.value = true
}

// ===== 取消编辑 =====
function cancelEdit() {
  editForm.realName = profile.value.realName || ''
  editForm.phone = profile.value.phone || ''
  editForm.gender = profile.value.gender || '保密'
  editForm.birthday = profile.value.birthday || ''
  editForm.email = profile.value.email || ''
  editForm.address = profile.value.address || ''
  editForm.avatarFile = null
  editForm.avatarPreview = null
  errMsg.value = ''
  isEditing.value = false
}

// ===== 保存个人信息 =====
async function handleSave() {
  errMsg.value = ''
  if (!editForm.realName.trim()) { errMsg.value = '姓名不能为空'; return }
  if (editForm.phone && !/^\d{11}$/.test(editForm.phone)) { errMsg.value = '请输入正确的11位手机号'; return }

  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('realName', editForm.realName.trim())
    formData.append('phone', editForm.phone)
    formData.append('gender', editForm.gender)
    formData.append('email', editForm.email)
    formData.append('address', editForm.address)
    if (editForm.birthday) formData.append('birthday', editForm.birthday)
    if (editForm.avatarFile) formData.append('avatar', editForm.avatarFile)

    const result = await updateCustomer(profile.value.customerId, formData)
    setCurrentUser(result)
    isEditing.value = false
    await loadProfile()
    successMsg.value = '保存成功！'
    setTimeout(() => { successMsg.value = '' }, 3000)
  } catch (e) {
    errMsg.value = e.message || '保存失败'
  } finally {
    submitting.value = false
  }
}

// ===== 切换用户 =====
function openSwitchModal() {
  switchUsername.value = ''
  switchErr.value = ''
  showSwitchModal.value = true
}

function closeSwitchModal() {
  showSwitchModal.value = false
  switchErr.value = ''
}

async function handleSwitchUser() {
  const name = switchUsername.value.trim()
  if (!name) {
    switchErr.value = '请输入用户名'
    return
  }

  switching.value = true
  switchErr.value = ''
  errMsg.value = ''
  successMsg.value = ''

  try {
    const result = await findOrCreateCustomer(name)
    setCurrentUser(result)
    showSwitchModal.value = false
    isEditing.value = false
    await loadProfile()
    successMsg.value = result.username === name && result.realName === name
      ? '已创建新用户: ' + name
      : '已切换到用户: ' + name
    setTimeout(() => { successMsg.value = '' }, 3000)
  } catch (e) {
    if (showSwitchModal.value) {
      switchErr.value = e.message || '切换失败'
    } else {
      errMsg.value = e.message || '操作失败'
    }
  } finally {
    switching.value = false
  }
}

onMounted(() => {
  if (isIdentified()) {
    loadProfile()
  }
  // else: the template will show the "设置身份" prompt
  // (the switchUsername in the prompt and the switchUsername in the modal are the same ref)
})
</script>

<style scoped>
.action-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.btn-group {
  display: flex;
  gap: 8px;
}
.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}
.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--border);
  background: #f0f0f0;
}
.avatar-wrapper.clickable {
  cursor: pointer;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}
.avatar-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.5);
  color: #fff;
  text-align: center;
  font-size: 12px;
  padding: 4px 0;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
.radio-group {
  display: flex;
  gap: 20px;
  padding-top: 8px;
}
.radio-label {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-size: 14px;
}
.radio-label input[type="radio"]:disabled + span,
.radio-label input[type="radio"]:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
.stats-row {
  display: flex;
  gap: 16px;
  margin: 24px 0;
}
.stat-card {
  flex: 1;
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}
.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary);
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content {
  background: #fff;
  border-radius: 8px;
  width: 90%;
  max-width: 420px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 8px 30px rgba(0,0,0,0.2);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}
.modal-header h3 { margin: 0; font-size: 18px; }
.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 4px 8px;
}
.modal-close:hover { color: var(--text); }
.modal-body { padding: 20px; }
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
