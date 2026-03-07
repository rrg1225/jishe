<template>
  <div class="login-container">
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
            <input type="text" placeholder="手机号" v-model="phone" required />
          </div>
        </template>

        <div class="input-box">
          <input type="password" placeholder="密码" v-model="password" required />
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
</script>

<style scoped>
/* 你的原有样式完全保持不变 */
.login-container { height: 100vh; width: 100vw; background: url('https://images.unsplash.com/photo-1519389950473-47ba0277781c?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80') center/cover no-repeat; display: flex; align-items: center; justify-content: center; }
.glass-card { width: 380px; padding: 40px; background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(10px); border-radius: 16px; box-shadow: 0 15px 35px rgba(0,0,0,0.2); }
.title { text-align: center; margin-bottom: 25px; color: #333; }
.tabs { display: flex; justify-content: center; gap: 20px; margin-bottom: 25px; }
.tabs span { cursor: pointer; color: #666; padding-bottom: 5px; }
.tabs span.active { color: #4a90e2; border-bottom: 2px solid #4a90e2; font-weight: bold; }
.input-box input { width: 100%; padding: 12px 15px; margin-bottom: 20px; border: 1px solid #ccc; border-radius: 8px; box-sizing: border-box; background: rgba(255,255,255,0.9); transition: border 0.3s; outline: none; }
.input-box input:focus { border-color: #4a90e2; }
.login-btn { width: 100%; padding: 12px; background: #4a90e2; color: white; border: none; border-radius: 8px; font-size: 16px; cursor: pointer; transition: 0.3s; }
.login-btn:hover { background: #357abd; box-shadow: 0 4px 12px rgba(74, 144, 226, 0.4); }
.action-links { display: flex; justify-content: space-between; margin-top: 15px; font-size: 14px; }
.action-links a { color: #666; text-decoration: none; }
.action-links a:hover { color: #4a90e2; }
</style>