<template>
  <div>
    <!-- ====== 未绑定：绑定/登录表单 ====== -->
    <div v-if="!isBound" class="card" style="max-width: 480px;">
      <div class="card-body">
        <h3 style="margin-bottom: 8px;">绑定个人信息</h3>
        <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 20px;">
          ⚠️ 用户名和密码将作为您的唯一标识，请牢记
        </p>

        <div v-if="errMsg" class="alert alert-error" style="margin-bottom: 12px;">{{ errMsg }}</div>

        <!-- 绑定模式 -->
        <div v-if="!isLoginMode">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input type="text" class="form-control" v-model="bindForm.username" placeholder="设置用户名" />
          </div>
          <div class="form-group">
            <label class="form-label">密码</label>
            <input type="password" class="form-control" v-model="bindForm.password" placeholder="设置密码" />
          </div>
          <div class="form-group">
            <label class="form-label">确认密码</label>
            <input type="password" class="form-control" v-model="bindForm.confirmPassword" placeholder="再次输入密码" />
          </div>
          <div class="form-group">
            <label class="form-label">姓名</label>
            <input type="text" class="form-control" v-model="bindForm.realName" placeholder="请输入姓名" />
          </div>
          <div class="form-group">
            <label class="form-label">手机</label>
            <input type="tel" class="form-control" v-model="bindForm.phone" maxlength="11" placeholder="请输入11位手机号" />
          </div>

          <button class="btn btn-primary btn-lg" style="width: 100%;" @click="handleBind" :disabled="submitting">
            {{ submitting ? '绑定中...' : '绑定并保存' }}
          </button>

          <p style="text-align: center; margin-top: 16px; font-size: 14px; color: var(--text-secondary);">
            已有账号？
            <a href="#" @click.prevent="isLoginMode = true" style="color: var(--primary);">去登录</a>
          </p>
        </div>

        <!-- 登录模式 -->
        <div v-else>
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input type="text" class="form-control" v-model="loginForm.username" placeholder="请输入用户名" />
          </div>
          <div class="form-group">
            <label class="form-label">密码</label>
            <input type="password" class="form-control" v-model="loginForm.password" placeholder="请输入密码" />
          </div>

          <button class="btn btn-primary btn-lg" style="width: 100%;" @click="handleLogin" :disabled="submitting">
            {{ submitting ? '登录中...' : '登录' }}
          </button>

          <p style="text-align: center; margin-top: 16px; font-size: 14px; color: var(--text-secondary);">
            还没有账号？
            <a href="#" @click.prevent="isLoginMode = false; errMsg = ''" style="color: var(--primary);">去绑定</a>
          </p>
        </div>
      </div>
    </div>

    <!-- ====== 已绑定：个人信息展示/编辑 ====== -->
    <div v-else class="card" style="max-width: 560px;">
      <div class="card-body">
        <h3 style="margin-bottom: 20px;">个人信息</h3>

        <div v-if="errMsg" class="alert alert-error" style="margin-bottom: 12px;">{{ errMsg }}</div>
        <div v-if="successMsg" class="alert alert-success" style="margin-bottom: 12px;">{{ successMsg }}</div>

        <!-- 头像 -->
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="triggerAvatarUpload">
            <img v-if="editForm.avatarPreview || profile.avatarUrl"
                 :src="editForm.avatarPreview || profile.avatarUrl"
                 class="avatar-img" />
            <div v-else class="avatar-placeholder">📷</div>
            <div class="avatar-overlay">点击更换</div>
          </div>
          <input type="file" ref="avatarInput" accept="image/jpeg,image/png,image/webp"
                 style="display: none" @change="onAvatarChange" />
        </div>

        <!-- 表单 -->
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input type="text" class="form-control" :value="profile.username" disabled style="background: #f5f5f5;" />
        </div>
        <div class="form-group">
          <label class="form-label">姓名</label>
          <input type="text" class="form-control" v-model="editForm.realName" />
        </div>
        <div class="form-group">
          <label class="form-label">手机</label>
          <input type="tel" class="form-control" v-model="editForm.phone" maxlength="11" />
        </div>
        <div class="form-group">
          <label class="form-label">性别</label>
          <div class="radio-group">
            <label class="radio-label"><input type="radio" v-model="editForm.gender" value="男" /> 男</label>
            <label class="radio-label"><input type="radio" v-model="editForm.gender" value="女" /> 女</label>
            <label class="radio-label"><input type="radio" v-model="editForm.gender" value="保密" /> 保密</label>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">生日</label>
          <input type="date" class="form-control" v-model="editForm.birthday" />
        </div>
        <div class="form-group">
          <label class="form-label">邮箱</label>
          <input type="email" class="form-control" v-model="editForm.email" placeholder="请输入邮箱" />
        </div>
        <div class="form-group">
          <label class="form-label">地址</label>
          <input type="text" class="form-control" v-model="editForm.address" placeholder="请输入地址" />
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

        <!-- 按钮 -->
        <div class="mt-24">
          <button class="btn btn-primary btn-lg" style="width: 100%;" @click="handleSave" :disabled="submitting">
            {{ submitting ? '保存中...' : '保存信息' }}
          </button>
        </div>
        <div class="mt-16" style="text-align: center;">
          <button class="btn btn-outline" style="color: var(--danger); border-color: var(--danger);" @click="handleUnbind">
            退出绑定（切换账号）
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getCustomer, bindAccount, login, updateCustomer,
         getAppointmentsByCustomer, getOrdersByCustomer } from '../api/index.js'
import { getCurrentUser, setCurrentUser, clearCurrentUser } from '../utils/user.js'

// ===== 状态 =====
const isBound = ref(false)
const isLoginMode = ref(false)
const submitting = ref(false)
const errMsg = ref('')
const successMsg = ref('')

// ===== 当前登录用户 =====
const user = ref(null)
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

// ===== 绑定表单 =====
const bindForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: ''
})

// ===== 登录表单 =====
const loginForm = reactive({
  username: '',
  password: ''
})

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
  const u = getCurrentUser()
  if (!u || !u.username) {
    isBound.value = false
    return
  }
  user.value = u
  try {
    const data = await getCustomer(u.customerId)
    profile.value = data
    editForm.realName = data.realName || ''
    editForm.phone = data.phone || ''
    editForm.gender = data.gender || '保密'
    editForm.birthday = data.birthday || ''
    editForm.email = data.email || ''
    editForm.address = data.address || ''

    // 加载统计
    const [appts, ords] = await Promise.all([
      getAppointmentsByCustomer(u.customerId),
      getOrdersByCustomer(u.customerId)
    ])
    stats.appointments = Array.isArray(appts) ? appts.length : 0
    stats.orders = Array.isArray(ords) ? ords.length : 0

    isBound.value = true
  } catch (e) {
    errMsg.value = '加载个人信息失败: ' + e.message
  }
}

// ===== 绑定 =====
async function handleBind() {
  errMsg.value = ''
  if (!bindForm.username.trim()) { errMsg.value = '请输入用户名'; return }
  if (!bindForm.password) { errMsg.value = '请输入密码'; return }
  if (bindForm.password !== bindForm.confirmPassword) { errMsg.value = '两次密码不一致'; return }
  if (!bindForm.realName.trim()) { errMsg.value = '请输入姓名'; return }
  if (!/^\d{11}$/.test(bindForm.phone)) { errMsg.value = '请输入正确的11位手机号'; return }

  submitting.value = true
  try {
    const result = await bindAccount({
      username: bindForm.username.trim(),
      password: bindForm.password,
      realName: bindForm.realName.trim(),
      phone: bindForm.phone
    })
    setCurrentUser(result)
    // 重新加载
    await loadProfile()
    successMsg.value = '绑定成功！'
  } catch (e) {
    errMsg.value = e.message || '绑定失败'
  } finally {
    submitting.value = false
  }
}

// ===== 登录 =====
async function handleLogin() {
  errMsg.value = ''
  if (!loginForm.username.trim()) { errMsg.value = '请输入用户名'; return }
  if (!loginForm.password) { errMsg.value = '请输入密码'; return }

  submitting.value = true
  try {
    const result = await login({
      username: loginForm.username.trim(),
      password: loginForm.password
    })
    setCurrentUser(result)
    await loadProfile()
    successMsg.value = '登录成功！'
  } catch (e) {
    errMsg.value = e.message || '登录失败'
  } finally {
    submitting.value = false
  }
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
    // 更新 localStorage 中的信息
    setCurrentUser(result)
    // 刷新页面
    await loadProfile()
    successMsg.value = '保存成功！'
  } catch (e) {
    errMsg.value = e.message || '保存失败'
  } finally {
    submitting.value = false
  }
}

// ===== 退出绑定 =====
function handleUnbind() {
  if (!confirm('确定要退出绑定吗？退出后需要重新登录才能查看历史记录。')) return
  clearCurrentUser()
  isBound.value = false
  profile.value = { customerId: null, username: '', realName: '', phone: '', email: '', address: '', gender: '保密', birthday: '', avatarUrl: '', firstSubmitTime: '' }
  Object.assign(editForm, { realName: '', phone: '', gender: '保密', birthday: '', email: '', address: '', avatarFile: null, avatarPreview: null })
  Object.assign(bindForm, { username: '', password: '', confirmPassword: '', realName: '', phone: '' })
  Object.assign(loginForm, { username: '', password: '' })
  errMsg.value = ''
  successMsg.value = ''
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
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
  cursor: pointer;
  border: 3px solid var(--border);
  background: #f0f0f0;
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
.mt-24 { margin-top: 24px; }
.mt-16 { margin-top: 16px; }
</style>
