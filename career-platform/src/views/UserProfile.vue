<template>
  <div class="profile-layout">
    <aside class="sidebar">
      <div class="sidebar-title">个人中心</div>
      <ul class="nav-menu">
        <li @click="router.push('/dashboard')">
          <span class="icon">🏠</span> 首页
        </li>
        <li 
          v-for="item in menuItems" 
          :key="item.id"
          :class="{ active: activeTab === item.id }"
          @click="activeTab = item.id"
        >
          <span class="icon">{{ item.icon }}</span> {{ item.name }}
        </li>
      </ul>
    </aside>

    <main class="main-content">
      
      <div v-if="activeTab === 'info'" class="fade-in">
        <h2 class="page-title">我的信息</h2>
        <div class="content-card">
          <div class="user-header">
            <div class="avatar-box">
              <img :src="userInfo.avatar || 'https://api.dicebear.com/7.x/adventurer/svg?seed=default'" alt="头像">
            </div>
            <div class="user-info">
              <div class="name-row">
                <span class="nickname">{{ userInfo.nickname || '未命名用户' }}</span>
                <span class="level-badge">LV 1</span>
                <span class="gender-badge" :class="userInfo.gender === '女' ? 'female' : 'male'">
                  {{ userInfo.gender === '女' ? '♀' : '♂' }}
                </span>
              </div>
              <div class="uid">UID: {{ userInfo.phone }}</div>
              <div class="bio">{{ userInfo.bio || '这个人很懒，什么都没写~' }}</div>
            </div>
            <button class="edit-btn" @click="router.push('/profile-setup')">修改资料</button>
          </div>

          <div class="divider"></div>

          <div class="stats-box">
            <div class="stat-item">
              <div class="stat-num">{{ userInfo.following }}</div>
              <div class="stat-label">关注</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ userInfo.followers }}</div>
              <div class="stat-label">粉丝</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ userInfo.likes }}</div>
              <div class="stat-label">获赞</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'security'" class="fade-in">
        <h2 class="page-title">账号安全</h2>
        <div class="security-list">
          <div class="sec-item">
            <div class="sec-info">
              <div class="sec-title">登录密码</div>
              <div class="sec-desc">安全性高的密码可以使账号更安全。建议您定期更换密码。</div>
            </div>
            <button class="sec-btn">修改密码</button>
          </div>
          <div class="sec-item">
            <div class="sec-info">
              <div class="sec-title">绑定手机</div>
              <div class="sec-desc">您已绑定手机：{{ userInfo.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') }}</div>
            </div>
            <button class="sec-btn outline">更换手机</button>
          </div>
          <div class="sec-item">
            <div class="sec-info">
              <div class="sec-title">实名认证</div>
              <div class="sec-desc">完成实名认证，享受更多平台特权。</div>
            </div>
            <button class="sec-btn highlight">去认证</button>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'history'" class="fade-in">
        <h2 class="page-title">我的足迹</h2>
        <div class="history-grid">
          <div class="history-card" v-for="i in 4" :key="i">
            <div class="cover"></div>
            <div class="info">
              <div class="title">前端Vue3企业级实战开发指北</div>
              <div class="time">昨天 14:30</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'notify'" class="fade-in">
        <h2 class="page-title">通知设置</h2>
        <div class="notify-list">
          <div class="notify-item">
            <span>系统消息通知</span>
            <input type="checkbox" class="toggle-switch" checked>
          </div>
          <div class="notify-item">
            <span>收到点赞与回复</span>
            <input type="checkbox" class="toggle-switch" checked>
          </div>
          <div class="notify-item">
            <span>新增粉丝提醒</span>
            <input type="checkbox" class="toggle-switch" checked>
          </div>
        </div>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()

// 控制右侧显示的标签页
const activeTab = ref('info')

// 菜单配置
const menuItems = [
  { id: 'info', name: '我的信息', icon: '📄' },
  { id: 'security', name: '账号安全', icon: '🛡️' },
  { id: 'history', name: '观看记录', icon: '🕒' },
  { id: 'notify', name: '通知设置', icon: '🔔' }
]

const userInfo = ref({
  avatar: '',
  nickname: '加载中...', 
  phone: '加载中...',
  bio: '加载中...',
  gender: '男',
  following: 0,
  followers: 0,
  likes: 0
})

const fetchUserInfo = async () => {
  try {
    const res = await request.get('/api/user/info')
    
    // 💡 终极防空指针：同时匹配后端可能返回的三种命名！
    const realName = res.nickname || res.nickName || res.nick_name || '未命名用户'
    
    userInfo.value = {
      avatar: res.avatar,
      nickname: realName, 
      phone: res.phone || '',
      bio: res.bio || '',
      gender: res.gender || '男',
      following: res.following || 0,
      followers: res.followers || 0,
      likes: res.likes || 0
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
/* 全局布局 */
.profile-layout { display: flex; min-height: 100vh; background-color: #f4f5f7; padding: 20px 10%; gap: 20px; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }

/* 左侧导航栏 */
.sidebar { width: 220px; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); height: fit-content; padding-bottom: 20px; overflow: hidden; }
.sidebar-title { padding: 20px; font-size: 18px; font-weight: 600; color: #333; border-bottom: 1px solid #f0f0f0; margin-bottom: 10px; }
.nav-menu { list-style: none; padding: 0; margin: 0; }
.nav-menu li { padding: 16px 24px; color: #666; font-size: 15px; cursor: pointer; transition: 0.3s; display: flex; align-items: center; gap: 12px; }
.nav-menu li .icon { font-size: 18px; }
.nav-menu li:hover { background-color: #f4f5f7; color: #00aeec; }
/* 激活状态：B站经典的亮蓝色 */
.nav-menu li.active { background-color: #00aeec; color: #fff; font-weight: 500; }
.nav-menu li.active:hover { background-color: #00a1d6; }

/* 右侧主区域 */
.main-content { flex: 1; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); padding: 40px; min-height: 600px; }
.page-title { font-size: 20px; color: #222; margin-top: 0; margin-bottom: 30px; padding-bottom: 15px; border-bottom: 1px solid #e5e9ef; }
.fade-in { animation: fadeIn 0.4s ease-in-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }

/* === 模块1：我的信息 === */
.user-header { display: flex; align-items: center; gap: 30px; position: relative; }
.avatar-box { width: 100px; height: 100px; border-radius: 50%; background: #f0f0f0; overflow: hidden; border: 2px solid #fff; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
.avatar-box img { width: 100%; height: 100%; object-fit: cover; }
.user-info { display: flex; flex-direction: column; gap: 8px; flex: 1; }
.name-row { display: flex; align-items: center; gap: 10px; }
.nickname { font-size: 24px; font-weight: bold; color: #222; }
.level-badge { background-color: #f69c3a; color: white; font-size: 12px; padding: 2px 8px; border-radius: 4px; font-weight: bold; font-style: italic; }
.gender-badge { font-size: 12px; padding: 2px 6px; border-radius: 4px; color: white; }
.gender-badge.male { background-color: #00aeec; }
.gender-badge.female { background-color: #ff758c; }
.uid { font-size: 14px; color: #999; }
.bio { font-size: 14px; color: #666; margin-top: 5px; }
.edit-btn { position: absolute; right: 0; top: 10px; padding: 8px 20px; border: 1px solid #ccd0d7; background: #fff; border-radius: 4px; color: #666; cursor: pointer; transition: 0.3s; }
.edit-btn:hover { border-color: #00aeec; color: #00aeec; }
.divider { height: 1px; background: #e5e9ef; margin: 40px 0; }
.stats-box { display: flex; gap: 60px; }
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 8px; cursor: pointer; }
.stat-item:hover .stat-num { color: #00aeec; }
.stat-num { font-size: 24px; font-weight: bold; color: #222; transition: 0.2s; }
.stat-label { font-size: 14px; color: #999; }

/* === 模块2：账号安全 === */
.security-list { display: flex; flex-direction: column; gap: 25px; }
.sec-item { display: flex; justify-content: space-between; align-items: center; padding-bottom: 25px; border-bottom: 1px solid #f0f0f0; }
.sec-title { font-size: 16px; font-weight: 600; color: #222; margin-bottom: 6px; }
.sec-desc { font-size: 14px; color: #999; }
.sec-btn { padding: 8px 20px; border: none; background: #f4f5f7; color: #666; border-radius: 4px; cursor: pointer; transition: 0.3s; }
.sec-btn:hover { background: #e5e9ef; }
.sec-btn.outline { background: transparent; border: 1px solid #00aeec; color: #00aeec; }
.sec-btn.outline:hover { background: #eaf8ff; }
.sec-btn.highlight { background: #00aeec; color: #fff; }
.sec-btn.highlight:hover { background: #00a1d6; }

/* === 模块3：观看记录 === */
.history-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; }
.history-card { background: #f4f5f7; border-radius: 6px; overflow: hidden; cursor: pointer; transition: 0.3s; }
.history-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); transform: translateY(-2px); }
.history-card .cover { height: 120px; background: #e5e9ef; }
.history-card .info { padding: 12px; }
.history-card .title { font-size: 14px; color: #222; line-height: 1.4; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.history-card .time { font-size: 12px; color: #999; }

/* === 模块4：通知设置 === */
.notify-list { display: flex; flex-direction: column; gap: 20px; }
.notify-item { display: flex; justify-content: space-between; align-items: center; padding: 15px; background: #f9f9f9; border-radius: 8px; font-size: 15px; color: #333; }
.toggle-switch { appearance: none; width: 44px; height: 24px; background: #e5e9ef; border-radius: 12px; position: relative; cursor: pointer; outline: none; transition: 0.3s; }
.toggle-switch:checked { background: #00aeec; }
.toggle-switch::after { content: ''; position: absolute; top: 2px; left: 2px; width: 20px; height: 20px; background: #fff; border-radius: 50%; box-shadow: 0 2px 4px rgba(0,0,0,0.2); transition: 0.3s; }
.toggle-switch:checked::after { transform: translateX(20px); }
</style>