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

          <div class="input-box" v-if="role === 'student'">
            <input type="text" placeholder="手机号" v-model="phone" required />
          </div>
          <div class="input-box" v-else>
            <input type="text" placeholder="手机号" v-model="phone" required />
          </div>

          <div class="input-box">
            <input type="password" placeholder="设置密码 (8-12位,含英文+数字+符号)" v-model="password" required />
          </div>
          <div class="input-box">
            <input type="password" placeholder="再次确认密码" v-model="confirmPassword" required />
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

// 绑定的表单字段
const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const captcha = ref('') 

// 倒计时相关
const countdown = ref(0)
let timer = null

// 🌟 新增：全局 Toast 提示状态
const toast = ref({ show: false, message: '', type: 'success' })
const showToast = (msg, type = 'success') => {
  toast.value = { show: true, message: msg, type }
  setTimeout(() => { toast.value.show = false }, 3000) // 3秒后消失
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

const handleBlur = () => { setTimeout(() => { showDropdown.value = false }, 200) }

// 获取验证码点击事件
const sendCaptcha = () => {
  if (!phone.value) return showToast('请先输入手机号！', 'error')
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(phone.value)) return showToast('请输入正确的11位手机号！', 'error')

  // 完美恢复你想要的简单提示，不自动生成数字，弹窗稳定停留3秒！
  showToast('验证码发送成功（模拟），随便输入6位数字即可！', 'success')

  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

onUnmounted(() => { if (timer) clearInterval(timer) })

// 注册并静默登录
const handleRegister = async () => {
  // 🌟 新增：密码格式校验（8-12位，必须包含字母、数字、特殊符号）
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
    
    // 静默登录获取 Token
    const loginRes = await request.post('/api/auth/login', {
      username: phone.value,
      password: password.value
    })

    localStorage.setItem('token', loginRes.token)
    localStorage.setItem('userRole', loginRes.role || role.value)
    
    showToast('注册成功！正在前往完善信息...', 'success')

    // 延迟 1 秒跳转，让用户看清成功提示
    setTimeout(() => {
      router.push({ path: '/profile-setup', query: { role: role.value } })
    }, 1000)
    
  } catch (error) {
    showToast(error.response?.data || '注册失败，请稍后重试', 'error')
  }
}
</script>

<style scoped>
/* 🌟 新增：Toast 提示框的样式 */
.toast-message {
  position: fixed; top: 30px; left: 50%; transform: translateX(-50%);
  padding: 12px 24px; border-radius: 8px; color: white; font-size: 15px; font-weight: 500;
  z-index: 9999; box-shadow: 0 4px 12px rgba(0,0,0,0.15); display: flex; align-items: center;
}
.toast-message.success { background-color: #52c41a; }
.toast-message.error { background-color: #ff4d4f; }
.toast-fade-enter-active, .toast-fade-leave-active { transition: all 0.3s ease; }
.toast-fade-enter-from, .toast-fade-leave-to { opacity: 0; transform: translate(-50%, -20px); }

/* 下面保留你所有的原有样式 */
.register-container { display: flex; height: 100vh; width: 100vw; }
.left-banner { flex: 6; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center; color: white; }
.banner-text h1 { font-size: 3rem; margin-bottom: 1rem; }
.right-form { flex: 4; background: #fff; display: flex; align-items: center; justify-content: center; }
.form-box { width: 400px; padding: 40px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); border-radius: 12px; }
.title { text-align: center; margin-bottom: 30px; color: #333; }
.tabs { display: flex; margin-bottom: 30px; border-bottom: 2px solid #eee; }
.tabs span { flex: 1; text-align: center; padding: 10px; cursor: pointer; color: #666; transition: all 0.3s; }
.tabs span.active { color: #4a90e2; border-bottom: 2px solid #4a90e2; font-weight: bold; }
.input-box { margin-bottom: 20px; }
.input-box input { width: 100%; padding: 12px 15px; border: 1px solid #ccc; border-radius: 8px; box-sizing: border-box; outline: none; transition: 0.3s; }
.input-box input:focus { border-color: #4a90e2; }
.autocomplete-wrapper { position: relative; }
.dropdown-list { position: absolute; top: 100%; left: 0; right: 0; background: white; border: 1px solid #e2e8f0; border-radius: 8px; margin-top: 5px; padding: 5px 0; list-style: none; max-height: 180px; overflow-y: auto; z-index: 100; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.dropdown-list li { padding: 12px 15px; cursor: pointer; color: #475569; font-size: 14px; transition: 0.2s; }
.dropdown-list li:hover { background: #f0f7ff; color: #4a90e2; padding-left: 20px; }
.captcha-box { display: flex; gap: 10px; }
.captcha-box input { flex: 2; }
.captcha-btn { flex: 1; height: 43px; background: #e6f0fa; color: #4a90e2; border: none; border-radius: 8px; cursor: pointer; transition: 0.3s; }
.captcha-btn:hover { background: #d0e4f7; }
.captcha-btn.disabled-btn { background: #e2e8f0; color: #94a3b8; cursor: not-allowed; box-shadow: none; }
.submit-btn { width: 100%; padding: 12px; background: #4a90e2; color: white; border: none; border-radius: 8px; font-size: 16px; cursor: pointer; margin-top: 10px; transition: 0.3s; }
.submit-btn:hover { background: #357abd; box-shadow: 0 4px 12px rgba(74, 144, 226, 0.4); }
.links { text-align: center; margin-top: 20px; font-size: 14px; }
.links a { color: #4a90e2; text-decoration: none; }
.form-item { display: flex; align-items: center; margin-bottom: 20px; }
.item-label { font-size: 16px; color: #333; margin-right: 10px; white-space: nowrap; display: flex; align-items: center; }
.item-label .required { color: #ff4d4f; margin-right: 4px; font-family: SimSun, sans-serif; }
.input-with-icon { position: relative; flex: 1; }
.input-with-icon input { width: 100%; padding: 10px 40px 10px 15px; border: 1px solid #d9d9d9; border-radius: 6px; outline: none; font-size: 14px; color: #aaa; transition: all 0.3s cubic-bezier(.645,.045,.355,1); }
.input-with-icon input:focus { border-color: #40a9ff; box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2); color: #333; }
.search-icon { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); color: #bfbfbf; pointer-events: none; }
.input-with-icon .dropdown-list { top: calc(100% + 4px); margin-top: 0; }
</style>