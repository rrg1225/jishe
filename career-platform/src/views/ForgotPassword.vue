<template>
  <div class="forgot-container">
    <div class="glass-card">
      <h2 class="title">找回密码</h2>
      <p class="subtitle">安全验证与密码重置</p>

      <form @submit.prevent="handleReset">
        <div class="input-box">
          <input type="text" placeholder="请输入绑定的手机号" required />
        </div>
        
        <div class="input-box captcha-box">
          <input type="text" placeholder="请输入验证码" required />
          <button type="button" class="captcha-btn" @click="sendCode" :disabled="countdown > 0">
            {{ countdown > 0 ? `${countdown}s 后重新获取` : '获取验证码' }}
          </button>
        </div>

        <div class="input-box">
          <input type="password" placeholder="请输入新密码" required />
        </div>
        
        <div class="input-box">
          <input type="password" placeholder="请再次确认新密码" required />
        </div>
        
        <button type="submit" class="reset-btn">确 认 重 置</button>
        
        <div class="action-links">
          <router-link to="/login" class="back-link">返回登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const countdown = ref(0)

// 模拟发送验证码倒计时
const sendCode = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
  alert('验证码已发送，请注意查收！')
}

const handleReset = () => {
  alert('密码重置成功！请使用新密码登录。')
  router.push('/login')
}
</script>

<style scoped>
.forgot-container { 
  height: 100%; width: 100%; 
  background: url('https://images.unsplash.com/photo-1519389950473-47ba0277781c?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80') center/cover no-repeat;
  display: flex; align-items: center; justify-content: center;
}
.glass-card {
  width: 400px; padding: 40px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.15);
}
.title { text-align: center; margin-bottom: 5px; color: #333; font-size: 24px; }
.subtitle { text-align: center; color: #888; margin-bottom: 30px; font-size: 14px; }
.input-box { margin-bottom: 20px; }
.input-box input { width: 100%; padding: 12px 15px; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; background: #fff; outline: none; transition: border 0.3s; }
.input-box input:focus { border-color: #4a90e2; }
.captcha-box { display: flex; gap: 10px; }
.captcha-box input { flex: 2; }
.captcha-btn { flex: 1; border: none; background: #e6f0fa; color: #4a90e2; border-radius: 6px; cursor: pointer; transition: 0.3s; }
.captcha-btn:hover:not(:disabled) { background: #d0e4f7; }
.captcha-btn:disabled { color: #999; background: #f0f0f0; cursor: not-allowed; }
.reset-btn { width: 100%; padding: 12px; background: #4a90e2; color: white; border: none; border-radius: 6px; font-size: 16px; cursor: pointer; transition: 0.3s; margin-top: 10px; }
.reset-btn:hover { background: #357abd; }
.action-links { text-align: center; margin-top: 20px; }
.back-link { color: #666; text-decoration: none; font-size: 14px; }
.back-link:hover { color: #4a90e2; }
</style>