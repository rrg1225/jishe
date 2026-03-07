<template>
  <div class="setup-container">
    <transition name="toast-fade">
      <div v-if="toast.show" class="toast-message" :class="toast.type">
        {{ toast.message }}
      </div>
    </transition>

    <div class="glass-card">
      <h2 class="title">修改个人信息</h2>
      <p class="subtitle">让大家更好地认识你</p>

      <form @submit.prevent="handleComplete">
        <div class="avatar-upload-box">
          <label for="avatar-input" class="avatar-preview">
            <img v-if="avatarUrl" :src="avatarUrl" alt="头像预览" />
            <div v-else class="upload-placeholder">
              <i>📷</i>
              <span>上传头像</span>
            </div>
          </label>
          <input id="avatar-input" type="file" accept="image/*" @change="onFileChange" hidden />
        </div>

        <div class="input-box">
          <input type="text" placeholder="设置一个好听的昵称" required v-model="form.nickName" />
        </div>

        <div class="input-box">
          <input type="text" placeholder="您的学校/企业名称" required v-model="form.org" />
        </div>

        <div class="input-box">
          <input type="number" placeholder="您的年龄" min="1" max="120" required v-model="form.age" />
        </div>

        <div class="input-box gender-group">
          <label :class="{ active: form.gender === '男' }">
            <input type="radio" value="男" v-model="form.gender" hidden /> 👦 男 生
          </label>
          <label :class="{ active: form.gender === '女' }">
            <input type="radio" value="女" v-model="form.gender" hidden /> 👧 女 生
          </label>
        </div>

        <div class="input-box">
          <textarea 
            placeholder="写一句彰显个性的签名吧..." 
            v-model="form.bio" 
            rows="3"
            maxlength="100"
          ></textarea>
          <div class="word-count">{{ form.bio.length }}/100</div>
        </div>

        <button type="submit" class="submit-btn">保 存 资 料</button>
        
        <div class="action-links">
          <span class="skip-link" @click="skipSetup">取消，直接进入主页 ></span>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../utils/request' 

const router = useRouter()
const route = useRoute()

const toast = ref({ show: false, message: '', type: 'success' })
const showToast = (msg, type = 'success') => {
  toast.value = { show: true, message: msg, type }
  setTimeout(() => { toast.value.show = false }, 3000)
}

const form = ref({ 
  nickName: '', 
  org: '', 
  age: '', 
  gender: '男', 
  bio: '' 
})
const avatarUrl = ref('')

onMounted(async () => {
  try {
    const res = await request.get('/api/user/info')
    if (res) {
      form.value.nickName = res.nickName || res.nickname || ''
      form.value.org = res.org || ''
      form.value.age = res.age || ''
      form.value.gender = res.gender || '男'
      form.value.bio = res.bio || ''
      avatarUrl.value = res.avatar || ''
    }
  } catch (error) {
    console.log('可能没有历史数据，或者是新注册用户')
  }
})

const onFileChange = (e) => {
  const file = e.target.files[0]
  if (file) avatarUrl.value = URL.createObjectURL(file)
}

const handleComplete = async () => {
  try {
    await request.post('/api/user/profile', {
      nickName: form.value.nickName,
      org: form.value.org, 
      age: form.value.age,
      gender: form.value.gender,
      bio: form.value.bio
    })
    
    localStorage.setItem('userName', form.value.nickName)
    localStorage.setItem('userOrg', form.value.org)
    
    showToast('保存成功！正在进入主页...', 'success')
    
    // 🌟 修改点 2：保存成功后，延迟一秒跳转到首页 (/dashboard)
    setTimeout(() => {
      router.push('/dashboard') 
    }, 1000)

  } catch (error) {
    showToast('保存失败，请稍后重试', 'error')
  }
}

// 🌟 修改点 3：跳过函数的跳转目标改为首页 (/dashboard)
const skipSetup = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
/* 样式保留你原本完美的样式，未作任何修改 */
.toast-message { position: fixed; top: 30px; left: 50%; transform: translateX(-50%); padding: 12px 24px; border-radius: 8px; color: white; font-size: 15px; font-weight: 500; z-index: 9999; box-shadow: 0 4px 12px rgba(0,0,0,0.15); display: flex; align-items: center; }
.toast-message.success { background-color: #52c41a; }
.toast-message.error { background-color: #ff4d4f; }
.toast-fade-enter-active, .toast-fade-leave-active { transition: all 0.3s ease; }
.toast-fade-enter-from, .toast-fade-leave-to { opacity: 0; transform: translate(-50%, -20px); }
.setup-container { height: 100vh; width: 100vw; background: url('https://images.unsplash.com/photo-1519389950473-47ba0277781c?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80') center/cover no-repeat; display: flex; align-items: center; justify-content: center; }
.glass-card { width: 420px; padding: 40px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); border-radius: 16px; box-shadow: 0 15px 35px rgba(0,0,0,0.2); }
.title { text-align: center; margin-bottom: 5px; color: #333; }
.subtitle { text-align: center; color: #888; font-size: 14px; margin-bottom: 30px; }
.avatar-upload-box { display: flex; justify-content: center; margin-bottom: 25px; }
.avatar-preview { width: 85px; height: 85px; border-radius: 50%; border: 2px dashed #cbd5e1; display: flex; align-items: center; justify-content: center; cursor: pointer; overflow: hidden; transition: border-color 0.3s; background: #f8fafc; }
.avatar-preview:hover { border-color: #4a90e2; }
.avatar-preview img { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; color: #94a3b8; }
.upload-placeholder i { font-size: 24px; font-style: normal; margin-bottom: 2px; }
.upload-placeholder span { font-size: 12px; }
.input-box { margin-bottom: 20px; width: 100%; }
.input-box input { width: 100%; padding: 14px 15px; border: 1px solid #cbd5e1; border-radius: 8px; box-sizing: border-box; outline: none; transition: 0.3s; font-size: 14px; }
.input-box input:focus { border-color: #4a90e2; box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1); }
.gender-group { display: flex; gap: 15px; }
.gender-group label { flex: 1; display: flex; align-items: center; justify-content: center; padding: 12px; border: 1px solid #cbd5e1; border-radius: 8px; cursor: pointer; color: #64748b; transition: all 0.3s; font-size: 15px; background: white; }
.gender-group label.active { background: #eff6ff; border-color: #4a90e2; color: #4a90e2; font-weight: bold; }
textarea { width: 100%; padding: 14px 15px; border: 1px solid #cbd5e1; border-radius: 8px; box-sizing: border-box; outline: none; transition: 0.3s; resize: none; font-family: inherit; font-size: 14px; }
textarea:focus { border-color: #4a90e2; box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1); }
.word-count { text-align: right; font-size: 12px; color: #94a3b8; margin-top: 6px; }
.submit-btn { width: 100%; padding: 14px; background: #4a90e2; color: white; border: none; border-radius: 8px; font-size: 16px; font-weight: 500; cursor: pointer; transition: all 0.3s; margin-top: 5px; }
.submit-btn:hover { background: #3b82f6; box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3); transform: translateY(-1px); }
.action-links { text-align: center; margin-top: 18px; }
.skip-link { color: #94a3b8; font-size: 14px; cursor: pointer; transition: 0.2s; }
.skip-link:hover { color: #4a90e2; }
</style>