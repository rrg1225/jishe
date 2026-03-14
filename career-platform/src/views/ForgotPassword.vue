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
  height: 100%; 
  width: 100%; 
  position: relative;
  overflow: hidden;
  display: flex; 
  align-items: center; 
  justify-content: center;
}



/* 背景图片保持不变 */
.forgot-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('/images11/photo-1486406146926-c627a92ad1ab.avif') center/cover no-repeat;
  background-size: cover;
  z-index: -2;
}

/* 添加从下往上的渐变虚化遮罩 */
.forgot-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to bottom,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0) 50%,
    rgba(255, 255, 255, 0.1) 70%,
    rgba(255, 255, 255, 0.2) 100%
  );
  backdrop-filter: blur(5px);  /* 在底部区域加模糊 */
  -webkit-backdrop-filter: blur(5px);
  mask: linear-gradient(to bottom, transparent 0%, black 50%, black 100%);
  /* 让模糊效果只应用在底部 */
  z-index: -1;
  pointer-events: none;
}



/* 卡片内边距稍微加大一点点 */
.glass-card {
  width: 440px;
  padding: 45px 42px; /* 从45px 40px微调到45px 42px */
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 标题区域 */
.title {
  text-align: center;
  margin-bottom: 8px;
  color: white;
  font-size: 28px;
  font-weight: 700;
  text-shadow: 0 4px 15px rgba(0, 0, 0, 0.6);
}

.subtitle {
  text-align: center;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 32px; /* 从40px降到32px，更紧凑但不挤 */
  font-size: 14px;
  font-weight: 500;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

/* 输入框 - 深色背景 + 亮文字 */
.input-box input {
  width: 100%;
  height: 48px; /* 统一高度 */
  padding: 0 18px; /* 左右内边距，上下为0 */
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 30px;
  font-size: 15px;
  background: rgba(0, 0, 0, 0.35);
  color: white;
  font-weight: 500;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.4);
  transition: all 0.3s;
  box-sizing: border-box;
  line-height: 48px; /* 文字垂直居中 */
  margin-bottom: 20px;
}

.input-box input::placeholder {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 400;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.input-box input:focus {
  outline: none;
  border-color: rgba(255, 255, 255, 0.7);
  background: rgba(0, 0, 0, 0.45);
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.1);
}

/* 验证码按钮 */
.captcha-box input {
  flex: 2;
  gap: 12px;
  margin-button: 20px;
  height: 48px; /* 固定高度，和按钮保持一致 */
  padding: 0 18px; /* 左右内边距，上下为0因为高度固定 */
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 30px;
  font-size: 15px;
  background: rgba(0, 0, 0, 0.35);
  color: white;
  font-weight: 500;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.4);
  transition: all 0.3s;
  box-sizing: border-box;
  line-height: 48px; /* 文字垂直居中 */
}

/* 验证码按钮 */
.captcha-btn {
  flex: 1;
  height: 48px; /* 和输入框同高 */
  padding: 0 12px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  background: rgba(0, 0, 0, 0.3);
  color: white;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  backdrop-filter: blur(4px);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  line-height: 48px; /* 文字垂直居中 */
  text-align: center;
  box-sizing: border-box;
}

.captcha-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.45);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-1px);
}

/* 确认重置按钮 */
/* 按钮样式 */
.reset-btn {
  width: 100%;
  height: 48px;
  padding: 0;
  background: rgba(255, 255, 255, 0.25);
  color: white;
  font-weight: 700;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 30px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 8px;
  backdrop-filter: blur(5px);
  text-shadow: 0 2px 5px rgba(0, 0, 0, 0.4);
  line-height: 48px;
}

/* 底部链接区域 */
.action-links {
  text-align: center;
  margin-top: 24px; /* 从30px降到24px */
}

.reset-btn:hover {
  background: rgba(255, 255, 255, 0.35);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
}


/* 验证码行 - 确保在一行且等高 */
.captcha-box {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: stretch; /* 让子元素拉伸到相同高度 */
}



/* 返回登录链接 */
.back-link {
  color: white;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.4);
  opacity: 0.9;
  transition: opacity 0.3s;
}

.back-link:hover {
  opacity: 1;
  text-decoration: underline;
}




</style>