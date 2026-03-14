<template>
  <div class="login-container">
   
     <!-- 梵高星夜风格背景 -->
    <div class="vangogh-bg">
      <div class="bg-swirl"></div>
      <div class="bg-texture"></div>
      <div class="stars-layer"></div>
      <div class="grass-bottom"></div>

       
        <!-- 漂浮的 CAREER 字母（放在背景层里） -->
        <div class="career-letters">
    <span class="career-letter career-letter-1">C</span>
    <span class="career-letter career-letter-2">A</span>
    <span class="career-letter career-letter-3">R</span>
    <span class="career-letter career-letter-4">E</span>
    <span class="career-letter career-letter-5">E</span>
    <span class="career-letter career-letter-6">R</span>
    <span class="career-letter career-letter-7">C</span>
    <span class="career-letter career-letter-8">R</span>
         </div>


    </div>
    
    <!-- 第一阶段：3条强烈线条（保持不变） -->
    <div class="opening-sequence">
      <svg viewBox="0 0 1200 800" preserveAspectRatio="none">
        <path class="opening-wave opening-wave-1" />
        <path class="opening-wave opening-wave-2" />
        <path class="opening-wave opening-wave-3" />
      </svg>
    </div>
    
    <!-- 第二阶段：梵高星夜风格云朵线条 -->
    <div class="sustained-waves">
      <svg viewBox="0 0 1200 800" preserveAspectRatio="none">
        <!-- 从左边延伸的云朵线条（3条，间距拉开） -->
        <path class="vangogh-line blue-shadow left-cloud-1" />
        <path class="vangogh-line green-shadow left-cloud-2" />
        <path class="vangogh-line mix-shadow left-cloud-3" />
        
        <!-- 从右边延伸的云朵线条（2条，间距拉开） -->
        <path class="vangogh-line green-shadow right-cloud-1" />
        <path class="vangogh-line mix-shadow right-cloud-2" />
      </svg>
    </div>
     

          <!-- 互动角色区域 -->
    <div class="interactive-characters">
      <!-- 左边的小机器人 -->
      <div class="character character-robot" :class="{ typing: isTyping, 'password-visible': isPasswordVisible }">
        <div class="robot-head">
          <div class="antenna"></div>
          <div class="eyes">
            <div class="eye left"></div>
            <div class="eye right"></div>
          </div>
          <div class="mouth"></div>
        </div>
        <div class="robot-body"></div>
      </div>
      
      <!-- 右边的云朵 -->
      <div class="character character-cloud" :class="{ happy: isTyping }">
        <div class="cloud-body">☁️</div>
        <div class="cloud-eyes">
          <span class="cloud-eye">👀</span>
        </div>
      </div>
    </div>



    <div class="glass-card">
      <h2 class="title">账号登录</h2>
      
      <div class="tabs">
        <span :class="{ active: role === 'student' }" @click="role = 'student'">学生</span>
        <span :class="{ active: role === 'enterprise' }" @click="role = 'enterprise'">企业/HR</span>
      </div>

      <form @submit.prevent="handleLogin">
        
        <template v-if="role === 'student'">
          <div class="input-box">
            <input type="text" placeholder="手机号" v-model="phone" required />
          </div>
        </template>
        <template v-else>
          <div class="input-box">
           <!-- 原来的手机号输入框，改成这样 -->
            <input type="text" placeholder="手机号" v-model="phone" required @input="handleInput" />
          </div>
        </template>

        <div class="input-box">
          <!-- 原来的密码输入框，改成这样 -->
           <input type="password" placeholder="密码" v-model="password" required @input="handleInput" />
        </div>
        
        <button type="submit" class="login-btn">登 录</button>
        
        <div class="action-links">
          <router-link to="/forgot-password">忘记密码？</router-link>
          <router-link to="/register">新用户注册</router-link>
        </div>

       



      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request' // 引入 axios

const role = ref('student')
const router = useRouter()

// 新增绑定的变量
const phone = ref('')
const password = ref('')

const handleLogin = async () => {
  if (!phone.value || !password.value) {
    alert('请输入手机号和密码！')
    return
  }

  try {
    // 调用后端登录接口
    const res = await request.post('/api/auth/login', {
      username: phone.value, // 后端统一用 username 接收
      password: password.value,
      role: role.value
    })
    
    // 登录成功！保存后端返回的 token 和基本信息
    // (假设后端返回格式为 { token: "xxx", username: "xxx", role: "student", org: "南京工业大学" })
    localStorage.setItem('token', res.token)
    localStorage.setItem('userRole', res.role || role.value)
    localStorage.setItem('userName', res.username || phone.value)
    localStorage.setItem('userOrg', res.org || (role.value === 'student' ? '南京工业大学' : '腾讯科技'))
    
    // 跳转到首页
    router.push('/dashboard')
    
  } catch (error) {
    console.error('登录失败:', error)
    // 错误拦截统一在 request.js 中提示
  }
}

// 鼠标点住暂停背景动画
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  const container = document.querySelector('.login-container')
  
  const pauseAnimation = () => {
    container.classList.add('paused')
  }
  
  const resumeAnimation = () => {
    container.classList.remove('paused')
  }
  
  container.addEventListener('mousedown', pauseAnimation)
  container.addEventListener('mouseup', resumeAnimation)
  container.addEventListener('mouseleave', resumeAnimation)
  
  // 清理事件监听（防止内存泄漏）
  onUnmounted(() => {
    container.removeEventListener('mousedown', pauseAnimation)
    container.removeEventListener('mouseup', resumeAnimation)
    container.removeEventListener('mouseleave', resumeAnimation)
  })
})



// 互动角色的状态
const isTyping = ref(false)
const isPasswordVisible = ref(false)

// 监听输入事件
const handleInput = () => {
  isTyping.value = true
  // 如果已经有定时器，先清除
  if (window.typingTimer) clearTimeout(window.typingTimer)
  // 0.8秒后恢复
  window.typingTimer = setTimeout(() => {
    isTyping.value = false
  }, 800)
}

// 监听密码可见性
const togglePasswordVisibility = () => {
  isPasswordVisible.value = !isPasswordVisible.value
  // 这里需要结合你现有的密码显示逻辑
}

// 在原有的输入框上绑定事件
// 你需要找到手机号和密码的 input 标签，加上 @input="handleInput"





</script>




<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  position: relative;
  overflow: hidden;
  background: #0a2f4a;
}

/* ===== 梵高星夜风格背景（蓝绿色调，漩涡感） ===== */
.vangogh-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

/* 底层漩涡 - 蓝绿色流动 */
.bg-swirl {
  position: absolute;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(ellipse at 30% 40%, #1a5f8c 0%, transparent 45%),
    radial-gradient(ellipse at 70% 55%, #2d8b6f 0%, transparent 50%),
    radial-gradient(circle at 45% 70%, #3f9cb0 0%, transparent 55%),
    radial-gradient(circle at 60% 25%, #5cb090 0%, transparent 40%),
    radial-gradient(circle at 20% 65%, #2a6d8f 0%, transparent 45%),
    radial-gradient(circle at 85% 35%, #3a7a5a 0%, transparent 50%);
  background-blend-mode: screen;
  animation: swirlMove 14s ease infinite alternate;
}

/* 星夜纹理 - 模拟梵高的短笔触 */
.bg-texture {
  position: absolute;
  width: 100%;
  height: 100%;
  background: 
    repeating-radial-gradient(circle at 30% 40%, rgba(180, 220, 255, 0.12) 0px, transparent 12px),
    repeating-radial-gradient(circle at 70% 60%, rgba(140, 210, 180, 0.1) 0px, transparent 15px),
    repeating-linear-gradient(120deg, 
      rgba(100, 170, 210, 0.08) 0px,
      rgba(100, 170, 210, 0.08) 20px,
      rgba(120, 200, 160, 0.08) 20px,
      rgba(120, 200, 160, 0.08) 40px
    );
  background-blend-mode: overlay;
  opacity: 0.6;
  animation: textureShift 18s linear infinite;
}

/* 星星光点 - 梵高星星的黄色光芒 */
.stars-layer {
  position: absolute;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 15% 25%, rgba(255, 240, 150, 0.4) 0%, transparent 6%),
    radial-gradient(circle at 30% 45%, rgba(250, 220, 100, 0.35) 0%, transparent 7%),
    radial-gradient(circle at 50% 60%, rgba(255, 230, 120, 0.3) 0%, transparent 6%),
    radial-gradient(circle at 70% 30%, rgba(245, 225, 130, 0.3) 0%, transparent 6%),
    radial-gradient(circle at 85% 70%, rgba(255, 235, 140, 0.3) 0%, transparent 7%),
    radial-gradient(circle at 45% 80%, rgba(250, 220, 110, 0.3) 0%, transparent 6%),
    radial-gradient(circle at 25% 75%, rgba(255, 245, 160, 0.25) 0%, transparent 5%),
    radial-gradient(circle at 75% 15%, rgba(255, 225, 100, 0.25) 0%, transparent 5%);
  background-size: 200% 200%;
  animation: starsTwinkle 12s ease infinite alternate;
  opacity: 0.8;
  pointer-events: none;
  z-index: 1;
}

/* 底部绿色草地 - 宁静的村庄感 */
.grass-bottom {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 28%;
  background: linear-gradient(to top, 
    rgba(50, 100, 60, 0.35) 0%,
    rgba(70, 130, 80, 0.2) 30%,
    transparent 100%);
  z-index: 1;
  pointer-events: none;
}

/* 背景动画 */
@keyframes swirlMove {
  0% { transform: scale(1) rotate(0deg); filter: hue-rotate(0deg) brightness(1); }
  33% { transform: scale(1.03) rotate(1.2deg); filter: hue-rotate(2deg) brightness(1.05); }
  66% { transform: scale(0.98) rotate(-0.8deg); filter: hue-rotate(-1deg) brightness(0.98); }
  100% { transform: scale(1) rotate(0deg); filter: hue-rotate(0deg) brightness(1); }
}

@keyframes textureShift {
  0% { transform: rotate(0deg) scale(1); opacity: 0.5; }
  50% { transform: rotate(2deg) scale(1.02); opacity: 0.7; }
  100% { transform: rotate(0deg) scale(1); opacity: 0.5; }
}

@keyframes starsTwinkle {
  0% { opacity: 0.7; background-position: 0% 0%; }
  50% { opacity: 0.9; background-position: 5% 3%; }
  100% { opacity: 0.7; background-position: 0% 0%; }
}

/* ===== 第一阶段：强烈线条（保持不变） ===== */
.opening-sequence {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 5;
  pointer-events: none;
  opacity: 1;
  animation: fadeOutSequence 4.5s ease-in-out forwards;
}

.opening-sequence svg {
  position: absolute;
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.4));
}

.opening-wave {
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.opening-wave-1 {
  stroke: rgba(255, 255, 255, 0.9);
  stroke-width: 4;
  filter: drop-shadow(0 0 15px #4aa3d1);
  d: path("M-200,150 C50,50 350,300 700,120 C1050,-60 1400,250 1600,100");
  animation: surgeWave1 2.8s ease-in-out;
}

.opening-wave-2 {
  stroke: rgba(255, 255, 255, 0.85);
  stroke-width: 4.5;
  filter: drop-shadow(0 0 15px #6cc29a);
  d: path("M-250,380 C100,460 450,280 800,420 C1150,560 1450,340 1650,420");
  animation: surgeWave2 3.2s ease-in-out;
}

.opening-wave-3 {
  stroke: rgba(255, 255, 255, 0.8);
  stroke-width: 4.2;
  filter: drop-shadow(0 0 18px #8ed4e0);
  d: path("M-300,580 C50,520 400,660 750,540 C1100,420 1400,620 1700,520");
  animation: surgeWave3 3s ease-in-out;
}

@keyframes surgeWave1 {
  0% { transform: translateX(-30%) scale(0.7); opacity: 0.2; filter: drop-shadow(0 0 20px #4aa3d1); }
  30% { transform: translateX(10%) scale(1.4); opacity: 1; filter: drop-shadow(0 0 30px #8ed4e0); }
  60% { transform: translateX(-5%) scale(1.2); opacity: 0.9; filter: drop-shadow(0 0 25px #6cc29a); }
  100% { transform: translateX(0) scale(1); opacity: 0.8; filter: drop-shadow(0 0 15px #4aa3d1); }
}

@keyframes surgeWave2 {
  0% { transform: translateX(30%) scale(0.8); opacity: 0.2; filter: drop-shadow(0 0 20px #4aa3d1); }
  35% { transform: translateX(-12%) scale(1.5); opacity: 1; filter: drop-shadow(0 0 30px #a8e6cf); }
  65% { transform: translateX(6%) scale(1.2); opacity: 0.9; filter: drop-shadow(0 0 25px #6cc29a); }
  100% { transform: translateX(0) scale(1); opacity: 0.8; filter: drop-shadow(0 0 15px #4aa3d1); }
}

@keyframes surgeWave3 {
  0% { transform: translateY(-20%) scale(0.9); opacity: 0.3; filter: drop-shadow(0 0 20px #4aa3d1); }
  40% { transform: translateY(15%) scale(1.45); opacity: 1; filter: drop-shadow(0 0 30px #8ed4e0); }
  70% { transform: translateY(-8%) scale(1.2); opacity: 0.9; filter: drop-shadow(0 0 25px #6cc29a); }
  100% { transform: translateY(0) scale(1); opacity: 0.8; filter: drop-shadow(0 0 15px #4aa3d1); }
}

/* ===== 第二阶段：梵高星夜风格云朵线条 ===== */
.sustained-waves {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 6;
  pointer-events: none;
  opacity: 0;
  animation: fadeInSequence 3s ease-in-out 3s forwards;
}

.sustained-waves svg {
  position: absolute;
  width: 100%;
  height: 100%;
}

/* 线条基础样式 - 梵高风格：白色云朵状，有厚度感 */
.vangogh-line {
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke: rgba(255, 255, 255, 0.6);
  stroke-width: 3.2; /* 加粗，有存在感 */
  filter: drop-shadow(0 0 12px currentColor);
  animation: cloudFlow 18s ease-in-out infinite;
}

/* 不同区域的阴影色 - 与背景融合 */
.vangogh-line.blue-shadow { color: #1f6a9a; }
.vangogh-line.green-shadow { color: #2d8b6f; }
.vangogh-line.mix-shadow { color: #2a7d8c; }

/* ===== 从左边延伸的云朵线条（3条，间距拉开） ===== */
.left-cloud-1 {
  /* 云朵形状：向上再向下，像梵高星夜里的云 */
  d: path("M-180,150 C-50,90 80,210 220,150 C360,90 500,210 650,140 C800,70 950,190 1100,120");
  stroke-width: 3.5;
  stroke-dasharray: 1400;
  stroke-dashoffset: 1400;
  animation: drawLine 3.8s ease-out forwards, cloudFlow 18s ease-in-out 3.8s infinite;
  filter: drop-shadow(0 0 15px #1f6a9a);
  color: #1f6a9a;
}

.left-cloud-2 {
  /* 间距拉开：位置下移，形状不同 */
  d: path("M-200,380 C-30,310 140,450 320,370 C500,290 680,430 860,350 C1040,270 1180,390 1300,330");
  stroke-width: 3.2;
  stroke-dasharray: 1500;
  stroke-dashoffset: 1500;
  animation: drawLine 4s ease-out 0.3s forwards, cloudFlow 18s ease-in-out 4.3s infinite;
  filter: drop-shadow(0 0 12px #2d8b6f);
  color: #2d8b6f;
}

.left-cloud-3 {
  /* 再下移，拉开间距 */
  d: path("M-220,580 C-10,520 200,660 420,580 C640,500 860,640 1080,560 C1280,490 1420,610 1520,550");
  stroke-width: 3.4;
  stroke-dasharray: 1700;
  stroke-dashoffset: 1700;
  animation: drawLine 4.2s ease-out 0.6s forwards, cloudFlow 18s ease-in-out 4.6s infinite;
  filter: drop-shadow(0 0 14px #2a7d8c);
  color: #2a7d8c;
}

/* ===== 从右边延伸的云朵线条（2条，间距拉开） ===== */
.right-cloud-1 {
  d: path("M1350,250 C1150,180 950,320 750,240 C550,160 350,300 150,220 C0,160 -80,260 -150,220");
  stroke-width: 3.3;
  stroke-dasharray: 1500;
  stroke-dashoffset: 1500;
  animation: drawLine 3.9s ease-out 0.2s forwards, cloudFlow 18s ease-in-out 4.1s infinite;
  filter: drop-shadow(0 0 13px #2d8b6f);
  color: #2d8b6f;
}

.right-cloud-2 {
  d: path("M1400,480 C1200,410 1000,550 800,470 C600,390 400,530 200,450 C50,390 -30,490 -100,450");
  stroke-width: 3.5;
  stroke-dasharray: 1600;
  stroke-dashoffset: 1600;
  animation: drawLine 4.1s ease-out 0.5s forwards, cloudFlow 18s ease-in-out 4.6s infinite;
  filter: drop-shadow(0 0 15px #2a7d8c);
  color: #2a7d8c;
}
/* 画线动画 - 让线条从一端伸出来 */
@keyframes drawLine {
  0% { stroke-dashoffset: 1500; opacity: 0.2; }
  40% { opacity: 0.7; }
  70% { opacity: 0.9; }
  100% { stroke-dashoffset: 0; opacity: 1; }
}

/* 云朵流动动画 - 梵高风格的律动 */
@keyframes cloudFlow {
  0% { transform: translateY(0) translateX(0) scale(1); filter: drop-shadow(0 0 10px currentColor); }
  20% { transform: translateY(0.8%) translateX(0.6%) scale(1.01); filter: drop-shadow(0 0 18px currentColor); }
  40% { transform: translateY(-0.5%) translateX(1.2%) scale(1.02); filter: drop-shadow(0 0 15px currentColor); }
  60% { transform: translateY(0.6%) translateX(0.4%) scale(1.01); filter: drop-shadow(0 0 20px currentColor); }
  80% { transform: translateY(-0.3%) translateX(0.8%) scale(0.99); filter: drop-shadow(0 0 12px currentColor); }
  100% { transform: translateY(0) translateX(0) scale(1); filter: drop-shadow(0 0 10px currentColor); }
}

/* 淡入淡出 */
@keyframes fadeOutSequence {
  0% { opacity: 1; }
  70% { opacity: 0.8; }
  100% { opacity: 0; visibility: hidden; }
}

@keyframes fadeInSequence {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 鼠标暂停第二阶段 */
.login-container.paused .vangogh-line {
  animation-play-state: paused;
}

/* ===== 登录卡片 ===== */
.glass-card {
  position: relative;
  z-index: 20;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(14px);
  border-radius: 32px;
  padding: 40px;
  width: 440px;
  box-shadow: 0 25px 50px rgba(26, 95, 140, 0.3);
  margin: 0 auto;
  top: 50%;
  transform: translateY(-50%);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #1f6a5a;
  font-size: 28px;
  font-weight: 500;
}

.tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
  gap: 20px;
}

.tabs span {
  cursor: pointer;
  padding: 8px 0;
  color: #2d6b5e;
  font-size: 16px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.tabs span.active {
  color: #1f8b6c;
  border-bottom-color: #1f8b6c;
  font-weight: 600;
}

.input-box {
  margin-bottom: 20px;
}

.input-box input {
  width: 100%;
  padding: 14px 18px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 30px;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.92);
  transition: all 0.3s;
}

.input-box input:focus {
  outline: none;
  border-color: #1f6a9a;
  box-shadow: 0 0 0 4px rgba(31, 106, 154, 0.15);
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #1f6a9a, #2d8b6f);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(31, 106, 154, 0.3);
}

.action-links {
  display: flex;
  justify-content: space-between;
  margin-top: 25px;
}

.action-links a {
  color: #1f8b6c;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.action-links a:hover {
  color: #0d5a4a;
  text-decoration: underline;
}


/* ===== 漂浮的 CAREER 字母（在背景层，颜色更深） ===== */
.career-letters {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2; /* 在背景层内，但在其他背景元素上面 */
  pointer-events: none;
  overflow: hidden;
}

.career-letter {
  position: absolute;
  font-size: 110px; /* 再大一点 */
  font-weight: 900;
  opacity: 0.6; /* 透明度从0.35提高到0.6，更深 */
  background: linear-gradient(145deg, 
    #7ac0e0 0%,    /* 更亮的蓝 */
    #ffffff 30%,
    #9ed8b0 70%,
    #7ac0e0 100%
  );
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 0 30px rgba(74, 163, 209, 0.7);
  animation: letterFloat 20s ease-in-out infinite;
  transform-origin: center;
  white-space: nowrap;
  filter: drop-shadow(0 0 12px rgba(255, 255, 255, 0.5));
}

/* 位置调整 - 完全分布在四周，中间留空给登录框 */
.career-letter-1 { /* C - 左上 */
  top: 5%;
  left: 2%;
  animation-duration: 21s;
  transform: rotate(-5deg);
}

.career-letter-2 { /* A - 右上 */
  top: 3%;
  right: 3%;
  left: auto;
  animation-duration: 23s;
  animation-delay: 1.2s;
  transform: rotate(4deg);
}

.career-letter-3 { /* R - 左下 */
  bottom: 5%;
  left: 4%;
  top: auto;
  animation-duration: 19s;
  animation-delay: 0.7s;
  transform: rotate(6deg);
}

.career-letter-4 { /* E - 右下 */
  bottom: 8%;
  right: 5%;
  left: auto;
  top: auto;
  animation-duration: 22s;
  animation-delay: 1.8s;
  transform: rotate(-3deg);
}

.career-letter-5 { /* E - 左侧中间 */
  top: 40%;
  left: 1%;
  animation-duration: 24s;
  animation-delay: 2.3s;
  transform: rotate(-2deg);
}

.career-letter-6 { /* R - 右侧中间 */
  top: 60%;
  right: 2%;
  left: auto;
  animation-duration: 20s;
  animation-delay: 0.4s;
  transform: rotate(7deg);
}

.career-letter-7 { /* 额外的C - 顶部中间偏左 */
  top: 12%;
  left: 20%;
  font-size: 90px;
  opacity: 0.5;
  animation-duration: 18s;
  animation-delay: 1.5s;
  transform: rotate(-8deg);
}

.career-letter-8 { /* 额外的R - 底部中间偏右 */
  bottom: 15%;
  right: 18%;
  left: auto;
  top: auto;
  font-size: 95px;
  opacity: 0.5;
  animation-duration: 25s;
  animation-delay: 0.9s;
  transform: rotate(10deg);
}

/* 漂浮动画 */
@keyframes letterFloat {
  0% {
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
  25% {
    transform: translate(15px, -20px) rotate(3deg) scale(1.08);
  }
  50% {
    transform: translate(-12px, 15px) rotate(-2deg) scale(0.96);
  }
  75% {
    transform: translate(18px, 10px) rotate(4deg) scale(1.05);
  }
  100% {
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
}



/* ===== 互动角色样式 ===== */
.interactive-characters {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 15; /* 在背景之上，卡片之下，这样角色不会被卡片完全挡住 */
  pointer-events: none; /* 让鼠标可以穿透点击卡片上的输入框 */
}

.character {
  position: absolute;
  transition: all 0.3s ease;
  opacity: 0.5;
  filter: drop-shadow(0 0 15px rgba(255, 255, 255, 0.3));
}

/* 左边的小机器人 */
.character-robot {
  bottom: 15%;
  left: 8%;
  width: 80px;
  height: 100px;
}

.robot-head {
  position: relative;
  width: 60px;
  height: 60px;
  background: linear-gradient(145deg, #4aa3d1, #2d8b6f);
  border-radius: 40% 40% 30% 30%;
  animation: bob 4s ease-in-out infinite;
}

.antenna {
  position: absolute;
  top: -15px;
  left: 25px;
  width: 4px;
  height: 20px;
  background: #ffaa00;
  border-radius: 2px;
  animation: wave 3s ease-in-out infinite;
}

.antenna::after {
  content: '';
  position: absolute;
  top: -6px;
  left: -4px;
  width: 12px;
  height: 12px;
  background: #ffdd44;
  border-radius: 50%;
}

.eyes {
  display: flex;
  justify-content: space-around;
  padding-top: 20px;
}

.eye {
  width: 12px;
  height: 12px;
  background: white;
  border-radius: 50%;
  animation: blink 4s infinite;
}

.eye.left { margin-left: 12px; }
.eye.right { margin-right: 12px; }

.eye::after {
  content: '';
  display: block;
  width: 6px;
  height: 6px;
  background: #1a4d8c;
  border-radius: 50%;
  margin: 3px 0 0 3px;
}

.mouth {
  width: 20px;
  height: 4px;
  background: #ff6b6b;
  border-radius: 2px;
  margin: 5px auto 0;
}

/* 机器人身体 */
.robot-body {
  width: 40px;
  height: 30px;
  background: linear-gradient(145deg, #3b8fb0, #2a6d8f);
  border-radius: 20% 20% 30% 30%;
  margin-left: 10px;
}

/* 右边的云朵 */
.character-cloud {
  bottom: 20%;
  right: 10%;
  font-size: 60px;
  filter: drop-shadow(0 0 20px rgba(255, 255, 255, 0.4));
  animation: cloudFloat 12s ease-in-out infinite;
}

.cloud-eyes {
  position: absolute;
  top: 20px;
  left: 15px;
  font-size: 20px;
  opacity: 0.7;
}

/* 互动状态 */
.character-robot.typing {
  transform: translateY(-5px) rotate(2deg);
}

.character-robot.typing .antenna {
  animation: excitedWave 0.5s ease-in-out infinite;
}

.character-robot.password-visible .eyes {
  opacity: 0.3;
  transform: scale(0.8);
}

.character-cloud.happy {
  transform: scale(1.1);
  filter: drop-shadow(0 0 30px rgba(255, 255, 200, 0.6));
}

/* 动画 */
@keyframes bob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(5deg); }
}

@keyframes excitedWave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(10deg); }
  75% { transform: rotate(-5deg); }
}

@keyframes blink {
  0%, 90%, 100% { transform: scaleY(1); }
  95% { transform: scaleY(0.1); }
}

@keyframes cloudFloat {
  0%, 100% { transform: translateX(0) translateY(0); }
  33% { transform: translateX(-10px) translateY(-8px); }
  66% { transform: translateX(8px) translateY(5px); }
}




</style>