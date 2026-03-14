<template>
  <div class="register-container">
    <transition name="toast-fade">
      <div v-if="toast.show" class="toast-message" :class="toast.type">
        {{ toast.message }}
      </div>
    </transition>

    <div class="left-banner">
      <div class="banner-text">
        <h1>连接学业与职业的桥梁</h1>
        <p>探索你的无限可能，从这里开始</p>
      </div>
    </div>

    <div class="right-form">
      <!-- 漂浮元素层 -->
      <div class="floating-elements">
        <div class="floating-item book-1">📚</div>
        <div class="floating-item book-2">📖</div>
        <div class="floating-item computer-1">💻</div>
        <div class="floating-item computer-2">🖥️</div>
        <div class="floating-item diploma">🎓</div>
        <div class="floating-item lightbulb">💡</div>
        <div class="floating-item chart">📊</div>
        <div class="floating-item target">🎯</div>
      </div>

      <div class="form-box">
        <h2 class="title">欢迎加入平台</h2>

        <div class="tabs">
          <span :class="{ active: role === 'student' }" @click="switchRole('student')">学生注册</span>
          <span :class="{ active: role === 'enterprise' }" @click="switchRole('enterprise')">企业入驻</span>
        </div>

        <form @submit.prevent="handleRegister">
          <div class="form-item autocomplete-wrapper">
            <label class="item-label">
              <span class="required">*</span> {{ role === 'student' ? '学校' : '企业' }}：
            </label>
            <div class="input-with-icon">
              <input
                type="text"
                :placeholder="role === 'student' ? '输入关键词以检索学校' : '输入关键词以检索企业'"
                v-model="orgName"
                @focus="showDropdown = true"
                @blur="handleBlur"
                required
              />
              <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="18" height="18">
                <path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m21 21l-4.343-4.343m0 0A8 8 0 1 0 5.343 5.343a8 8 0 0 0 11.314 11.314Z"/>
              </svg>

              <ul class="dropdown-list" v-show="showDropdown && filteredList.length > 0">
                <li v-for="item in filteredList" :key="item" @click="selectItem(item)">
                  {{ item }}
                </li>
              </ul>
            </div>
          </div>

          <div class="input-box">
            <input type="text" placeholder="手机号" v-model="phone" required />
          </div>

          <div class="input-box">
            <input
              type="password"
              placeholder="设置密码 (8-12位,含英文+数字+符号)"
              v-model="password"
              required
            />
          </div>

          <div class="input-box">
            <input
              type="password"
              placeholder="再次确认密码"
              v-model="confirmPassword"
              required
            />
          </div>

          <div class="input-box captcha-box">
            <input type="text" placeholder="输入验证码" v-model="captcha" required />
            <button
              type="button"
              class="captcha-btn"
              :class="{ 'disabled-btn': countdown > 0 }"
              :disabled="countdown > 0"
              @click="sendCaptcha"
            >
              {{ countdown > 0 ? `${countdown}s 后重试` : '获取验证码' }}
            </button>
          </div>

          <button type="submit" class="submit-btn">立即注册</button>

          <div class="links">
            <span>已有账号？ <router-link to="/login">去登录</router-link></span>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const role = ref('student')
const orgName = ref('')
const showDropdown = ref(false)

// 表单字段
const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const captcha = ref('')

// 倒计时
const countdown = ref(0)
let timer = null

// Toast
const toast = ref({ show: false, message: '', type: 'success' })
const showToast = (msg, type = 'success') => {
  toast.value = { show: true, message: msg, type }
  setTimeout(() => { toast.value.show = false }, 3000)
}

const schoolList = ['南京工业大学', '南京大学', '东南大学', '南京航空航天大学', '南京理工大学', '清华大学', '北京大学', '浙江大学']
const companyList = ['腾讯科技', '阿里巴巴', '字节跳动', '华为技术', '百度', '美团', '京东']

const switchRole = (newRole) => {
  role.value = newRole
  orgName.value = ''
}

const filteredList = computed(() => {
  const currentList = role.value === 'student' ? schoolList : companyList
  if (!orgName.value) return currentList
  return currentList.filter(item => item.toLowerCase().includes(orgName.value.toLowerCase()))
})

const selectItem = (item) => {
  orgName.value = item
  showDropdown.value = false
}

const handleBlur = () => {
  setTimeout(() => { showDropdown.value = false }, 200)
}

const sendCaptcha = () => {
  if (!phone.value) return showToast('请先输入手机号！', 'error')
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(phone.value)) return showToast('请输入正确的11位手机号！', 'error')

  showToast('验证码发送成功（模拟），随便输入6位数字即可！', 'success')
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const handleRegister = async () => {
  const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,12}$/
  if (!passwordRegex.test(password.value)) {
    return showToast('密码必须是8-12位，且包含英文、数字和符号！', 'error')
  }
  if (password.value !== confirmPassword.value) return showToast('两次输入的密码不一致！', 'error')
  if (!captcha.value) return showToast('请输入验证码！', 'error')

  try {
    const requestData = {
      username: phone.value,
      password: password.value,
      phone: phone.value,
      role: role.value,
      org: orgName.value
    }
    await request.post('/api/auth/register', requestData)

    // 静默登录
    const loginRes = await request.post('/api/auth/login', {
      username: phone.value,
      password: password.value
    })

    localStorage.setItem('token', loginRes.token)
    localStorage.setItem('userRole', loginRes.role || role.value)

    showToast('注册成功！正在前往完善信息...', 'success')
    setTimeout(() => {
      router.push({ path: '/profile-setup', query: { role: role.value } })
    }, 1000)

  } catch (error) {
    showToast(error.response?.data?.message || '注册失败，请稍后重试', 'error')
  }
}
</script>

<style scoped>
/* Toast */
.toast-message {
  position: fixed;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  color: white;
  font-size: 15px;
  font-weight: 500;
  z-index: 9999;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.toast-message.success { background-color: #52c41a; }
.toast-message.error   { background-color: #ff4d4f; }
.toast-fade-enter-active, .toast-fade-leave-active { transition: all 0.3s ease; }
.toast-fade-enter-from, .toast-fade-leave-to { opacity: 0; transform: translate(-50%, -20px); }

/* 布局 */
.register-container {
  display: flex;
  height: 100vh;
  width: 100vw;
}

.left-banner {
  flex: 6;
  background: url('https://plus.unsplash.com/premium_photo-1691962723290-12baaaa0fe2a?q=80&w=774&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D') no-repeat center center;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
}

.left-banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.35);
  z-index: 1;
}

.left-banner .banner-text {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 20px;
  max-width: 80%;
}

.banner-text h1 {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.right-form {
  position: relative;
  flex: 4;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.form-box {
  position: relative;
  z-index: 10;
  width: 450px;
  padding: 50px 45px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
}

/* 漂浮元素 */
.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
  overflow: hidden;
}

.floating-item {
  position: absolute;
  font-size: 40px;
  opacity: 0.15;
  animation: float 20s ease-in-out infinite;
  filter: drop-shadow(0 0 10px rgba(74, 144, 226, 0.3));
  transform-origin: center;
  color: #2c3e50;
}

/* 各个漂浮元素位置与延迟（可自行微调） */
.book-1     { top: 15%; left: 10%; font-size: 48px; animation-duration: 22s; animation-delay: 0s;    transform: rotate(-5deg); }
.book-2     { bottom: 20%; left: 15%; font-size: 42px; animation-duration: 25s; animation-delay: 2s;  transform: rotate(10deg); }
.computer-1 { top: 25%; right: 15%; font-size: 50px; animation-duration: 18s; animation-delay: 1s;   transform: rotate(3deg); }
.computer-2 { bottom: 30%; right: 10%; font-size: 45px; animation-duration: 24s; animation-delay: 3s; transform: rotate(-8deg); }
.diploma    { top: 40%; left: 20%; font-size: 44px; animation-duration: 21s; animation-delay: 4s;   transform: rotate(15deg); }
.lightbulb  { top: 60%; right: 20%; font-size: 38px; animation-duration: 19s; animation-delay: 2.5s; transform: rotate(-12deg); }
.chart      { top: 70%; left: 25%; font-size: 40px; animation-duration: 23s; animation-delay: 1.5s;  transform: rotate(5deg); }
.target     { bottom: 15%; right: 25%; font-size: 42px; animation-duration: 26s; animation-delay: 3.5s; transform: rotate(-5deg); }

@keyframes float {
  0%   { transform: translate(0, 0) rotate(0deg) scale(1); }
  25%  { transform: translate(10px, -15px) rotate(5deg) scale(1.05); }
  50%  { transform: translate(-8px, 10px) rotate(-3deg) scale(0.98); }
  75%  { transform: translate(12px, 8px) rotate(4deg) scale(1.02); }
  100% { transform: translate(0, 0) rotate(0deg) scale(1); }
}

/* 网格背景（可选淡化） */
.floating-elements::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    repeating-linear-gradient(0deg, rgba(74, 144, 226, 0.03) 0px, rgba(74, 144, 226, 0.03) 1px, transparent 1px, transparent 30px),
    repeating-linear-gradient(90deg, rgba(74, 144, 226, 0.03) 0px, rgba(74, 144, 226, 0.03) 1px, transparent 1px, transparent 30px);
  pointer-events: none;
  z-index: 0;
}

/* 表单通用样式（来自 2.0 优化版） */
.title { text-align: center; margin-bottom: 30px; color: #333; font-size: 1.8rem; font-weight: 600; }

.tabs {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 2px solid #eee;
}

.tabs span {
  flex: 1;
  text-align: center;
  padding: 12px;
  cursor: pointer;
  color: #666;
  transition: all 0.3s;
}

.tabs span.active {
  color: #4a90e2;
  border-bottom: 3px solid #4a90e2;
  font-weight: 600;
}

.input-box { margin-bottom: 22px; }

.input-box input {
  width: 100%;
  padding: 13px 16px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  box-sizing: border-box;
  outline: none;
  transition: all 0.25s;
  font-size: 15px;
}

.input-box input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.captcha-box { display: flex; gap: 12px; }
.captcha-box input { flex: 2; }
.captcha-btn {
  flex: 1;
  height: 46px;
  background: #eff6ff;
  color: #3b82f6;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.25s;
}

.captcha-btn:hover:not(.disabled-btn) { background: #dbeafe; }
.captcha-btn.disabled-btn {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  margin-top: 12px;
  transition: all 0.3s;
}

.submit-btn:hover {
  background: #2563eb;
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}

.links {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #64748b;
}

.links a {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
}

.form-item { display: flex; align-items: center; margin-bottom: 22px; gap: 12px; }

.item-label {
  font-size: 15px;
  color: #334155;
  white-space: nowrap;
  display: flex;
  align-items: center;
  min-width: 80px;
}

.item-label .required { color: #ef4444; margin-right: 4px; }

.input-with-icon { position: relative; flex: 1; }

.input-with-icon input {
  padding-right: 44px;
}

.search-icon {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  pointer-events: none;
}

.dropdown-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  margin-top: 6px;
  padding: 6px 0;
  list-style: none;
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
  box-shadow: 0 12px 28px rgba(0,0,0,0.12);
}

.dropdown-list li {
  padding: 12px 16px;
  cursor: pointer;
  color: #475569;
  transition: all 0.2s;
}

.dropdown-list li:hover {
  background: #eff6ff;
  color: #3b82f6;
  padding-left: 22px;
}
</style>