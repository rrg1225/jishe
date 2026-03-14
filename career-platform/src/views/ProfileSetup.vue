<template>
  <div class="setup-container">
    <transition name="toast-fade">
      <div v-if="toast.show" class="toast-message" :class="toast.type">
        {{ toast.message }}
      </div>
    </transition>

    <div class="glass-card">
      <h2 class="title">修改个人信息</h2>
      <p class="subtitle">让大家更好地认识你</p >

      <form @submit.prevent="handleComplete">
       <div class="avatar-section">
  <div class="avatar-uploader" @click="triggerFileInput">
    <div class="avatar-preview">
      <img v-if="avatarUrl" :src="avatarUrl" alt="头像" />
      <div v-else class="avatar-placeholder">
        <span class="avatar-icon">📸</span>
        <span class="avatar-text">点击上传头像</span>
      </div>
    </div>
  </div>
  <input 
    id="avatar-input" 
    type="file" 
    accept="image/*" 
    @change="onFileChange" 
    ref="fileInput"
    hidden 
  />
</div>

       

 <!-- 昵称 -->
<div class="input-box">
  <label>昵称</label>
  <div class="input-wrapper">
    <span class="input-icon">👤</span>
    <input type="text" placeholder="设置一个好听的昵称" v-model="form.nickName" required />
  </div>
</div>

<!-- 学校/企业 -->
<div class="input-box">
  <label>学校/企业</label>
  <div class="input-wrapper">
    <span class="input-icon">🏛️</span>
    <input type="text" placeholder="您的学校/企业名称" v-model="form.org" />
  </div>
</div>

<!-- 年龄 -->
<div class="input-box">
  <label>年龄</label>
  <div class="input-wrapper">
    <span class="input-icon">🎂</span>
    <input type="number" placeholder="您的年龄" min="1" max="120" v-model="form.age" required />
  </div>
</div>        
 


<!-- 性别 -->
<div class="gender-box">
  <label>性别</label>
  <div class="gender-cards">
    <div 
      class="gender-card" 
      :class="{ active: form.gender === '男' }"
      @click="form.gender = '男'"
    >
      <span class="gender-icon">👨</span>
      <span class="gender-text">男生</span>
    </div>
    <div 
      class="gender-card" 
      :class="{ active: form.gender === '女' }"
      @click="form.gender = '女'"
    >
      <span class="gender-icon">👩</span>
      <span class="gender-text">女生</span>
    </div>
  </div>
  <!-- 隐藏的radio，保持表单功能 -->
  <input type="radio" value="男" v-model="form.gender" hidden />
  <input type="radio" value="女" v-model="form.gender" hidden />
</div>


<div class="input-box signature-box">
  <label>个性签名</label>
  <textarea 
    placeholder="写一句彰显个性的签名吧..." 
    v-model="form.bio" 
    rows="3" 
    maxlength="100"
  ></textarea>
  <div class="signature-footer">
    <span class="signature-hint">✨ 展现你的态度</span>
    <span class="word-count">{{ form.bio?.length || 0 }}/100</span>
  </div>
</div>



  <div class="action-buttons">
  <button type="submit" class="save-btn">
    <span>保存资料</span>
    <span class="btn-icon">→</span>
  </button>
  <span class="cancel-link" @click="skipSetup">取消，直接进入主页 ></span>
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


.profile-card {
  width: 100%;
  max-width: 550px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.9); /* 稍微透一点背景 */
  backdrop-filter: blur(10px);
  border-radius: 32px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}



.input-with-icon {
  position: relative;
  width: 100%;
}

.input-with-icon input {
  width: 100%;
  padding: 14px 14px 14px 45px; /* 左边留出图标位置 */
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 20px;
  font-size: 15px;
  background: white;
  transition: all 0.3s;
  box-sizing: border-box;
}





.gender-cards {
  display: flex;
  gap: 15px;
}

.gender-card {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.gender-card.active {
  background: linear-gradient(135deg, #4aa3d1, #6cc29a);
  border-color: transparent;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(74, 163, 209, 0.2);
}

.gender-icon {
  font-size: 24px;
}

.gender-text {
  font-weight: 500;
}



.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.avatar-uploader {
  cursor: pointer;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid white;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.avatar-preview:hover {
  transform: scale(1.05);
  box-shadow: 0 15px 30px rgba(74, 163, 209, 0.3);
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #e0e0e0, #f5f5f5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
}

.avatar-icon {
  font-size: 32px;
  margin-bottom: 4px;
}

.avatar-text {
  font-size: 12px;
}


.signature-box textarea {
  border-radius: 20px;
  padding: 16px;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.08);
  resize: vertical;
}

.signature-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.signature-hint {
  color: #4aa3d1;
  opacity: 0.7;
}


.save-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #4aa3d1, #6cc29a);
  color: white;
  border: none;
  border-radius: 40px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(74, 163, 209, 0.3);
}

.btn-icon {
  font-size: 18px;
  transition: transform 0.3s;
}

.save-btn:hover .btn-icon {
  transform: translateX(4px);
}


/* 输入框容器 */
.input-box {
  margin-bottom: 20px;
}

.input-box label {
  display: block;
  margin-bottom: 8px;
  color: #1f4a3d;
  font-weight: 500;
  font-size: 14px;
  margin-left: 5px;
}

/* 图标+输入框的容器 */
.input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;  /* 图标和输入框之间的间距 */
  width: 100%;
}

/* 图标样式 - 放在外面 */
.input-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #4aa3d1, #6cc29a);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  box-shadow: 0 4px 8px rgba(74, 163, 209, 0.2);
  flex-shrink: 0;  /* 防止图标被压缩 */
}

/* 输入框样式 - 去掉左边的内边距 */
.input-wrapper input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 20px;
  font-size: 15px;
  background: white;
  transition: all 0.3s;
  box-sizing: border-box;
}

.input-wrapper input:focus {
  outline: none;
  border-color: #4aa3d1;
  box-shadow: 0 0 0 3px rgba(74, 163, 209, 0.1);
}



.toast-message { position: fixed; top: 30px; left: 50%; transform: translateX(-50%); padding: 12px 24px; border-radius: 8px; color: white; font-size: 15px; font-weight: 500; z-index: 9999; box-shadow: 0 4px 12px rgba(0,0,0,0.15); display: flex; align-items: center; }
.toast-message.success { background-color: #52c41a; }
.toast-message.error { background-color: #ff4d4f; }
.toast-fade-enter-active, .toast-fade-leave-active { transition: all 0.3s ease; }
.toast-fade-enter-from, .toast-fade-leave-to { opacity: 0; transform: translate(-50%, -20px); }


.setup-container { 
  height: 100vh; 
  width: 100vw; 
  background: url('/images11/vector-1758891079993-52adbeccaea7.avif') center/cover no-repeat; 
  display: flex; align-items: center; justify-content: center; }



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

.skip-link { 
  color: #94a3b8; 
  font-size: 14px; 
  cursor: pointer; 
  transition: 0.2s; }

.cancel-link{
  cursor: pointer;
}

.skip-link:hover { color: #4a90e2; }
</style>