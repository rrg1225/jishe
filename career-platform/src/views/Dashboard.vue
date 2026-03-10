没问题！我完全懂那种“只想一键复制粘贴，赶紧看到效果”的心情。

我已经把“一滑就消失”的 Bug 完美修复，把透明遮罩层无缝融入了你的代码中，并且清理了之前导致问题的多余代码。

请直接全选你当前页面的所有代码，用下面这份完整的代码覆盖掉：

代码段
<template>
  <div class="layout">
    <header class="header">
      <div class="logo">学业职业规划平台</div>
      <div class="search-bar"><input type="text" placeholder="搜索院校、企业或竞赛..." /></div>
      
      <div class="header-right">
        <button class="go-publish-btn" @click="router.push('/publish')">
          <span class="icon">✍️</span> 发布内容
        </button>

        <div class="user-profile" @click="toggleDropdown">
          <span class="role-badge">{{ userRole === 'enterprise' ? '企业 HR' : '学生' }}</span>
          
          <div class="avatar">
            <img :src="userAvatar" alt="个人头像" />
          </div>

          <div class="dropdown-overlay" v-show="isDropdownOpen" @click.stop="closeDropdown"></div>

          <div class="dropdown-menu" v-show="isDropdownOpen" @click.stop>
            <div class="dropdown-header">
              <div class="username">{{ userName }}</div>
              <div class="user-account">{{ userAccount }}</div>
              <div class="user-org" v-if="userOrg">{{ userOrg }}</div>
            </div>
            <div class="divider"></div>
            <ul class="dropdown-list">
              <li class="dropdown-item" @click.stop="goToProfile">
                <i>👤</i> 个人信息
              </li>
              <li class="dropdown-item" @click.stop="logout">
                <i>🚪</i> 退出登录
              </li>
            </ul>
          </div>
        </div>
      </div>
    </header>

    <div class="main-container">
      <aside class="sidebar">
        <ul>
          <li :class="{ active: activeMenu === 'home' }" @click="activeMenu = 'home'">首页 (推荐)</li>
          <li :class="{ active: activeMenu === 'matrix' }" @click="activeMenu = 'matrix'">学业深造矩阵</li>
          <li :class="{ active: activeMenu === 'job' }" @click="activeMenu = 'job'">产教融合双选</li>
          <li :class="{ active: activeMenu === 'contest' }" @click="activeMenu = 'contest'">竞赛信息大厅</li>
          <li :class="{ active: activeMenu === 'profile' }" @click="activeMenu = 'profile'">能力画像定制</li>
        </ul>
      </aside>

      <main class="content">
        <section v-if="activeMenu === 'home'" class="fade-in">
          <div class="banner-carousel">
            <div class="banner-content">
              <h2>🔥 2026 春季大厂校园招聘全面开启</h2>
              <p>提前批内推 / 简历辅导 / 面试真题全解析，助你拿下心仪 Offer</p>
            </div>
            <div class="banner-dots">
              <span class="dot active"></span>
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>

          <div class="home-sections">
            <div class="section-block" v-for="(section, index) in homeData" :key="index">
              <div class="section-title-bar">
                <h3>{{ section.title }}</h3>
                <span class="more-link" @click="activeMenu = section.target">查看更多 ></span>
              </div>
              <div class="card-grid">
                <div class="card" v-for="(item, i) in section.items" :key="i" @click="handleCardClick(item)">
                  <div class="card-cover" :style="{ background: item.color }">
                    <span class="type-badge" :class="item.type">{{ item.typeText }}</span>
                  </div>
                  <div class="card-info">
                    <h4 class="card-title">{{ item.title }}</h4>
                    <p class="card-desc">{{ item.author || item.desc }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeMenu === 'matrix'" class="card-layout fade-in">
          <h3>学业深造信息矩阵</h3>
          <p class="subtitle">为你提供最新的升学与海外留学情报</p>
          <div class="info-grid">
            <div class="grid-item">保研/考研最新政策</div>
            <div class="grid-item">海外留学资讯库</div>
            <div class="grid-item">大数据院校筛选系统</div>
          </div>
        </section>

        <section v-if="activeMenu === 'job'" class="card-layout fade-in">
          <div class="section-header">
            <div>
              <h3>产教融合与招聘双选</h3>
              <p class="subtitle">行业前沿风向标：探索大一至大四的硬核技能与软实力进阶路径</p>
            </div>
            <button v-if="userRole === 'enterprise'" class="publish-btn" @click="publishJob">
              + 发布招聘信息
            </button>
          </div>
          <div class="job-list">
            <div class="job-item">【名企内推】后端开发工程师 (Java/Spring Boot) - 附能力要求模型</div>
            <div class="job-item">【校招】前端开发工程师 (Vue.js/React) - 面试真题库</div>
            <div class="job-item">【实习】数据分析师 (Python/SQL) - 大厂核心业务</div>
          </div>
        </section>

        <section v-if="activeMenu === 'contest'" class="card-layout fade-in">
          <h3>竞赛信息大厅</h3>
          <p class="subtitle">打标签分类，快速匹配适合你的比赛</p>
          <div class="tags">
            <span class="tag">互联网+</span>
            <span class="tag">挑战杯</span>
            <span class="tag">数学建模(MCM/ICM)</span>
            <span class="tag">ACM程序设计</span>
            <span class="tag">英语竞赛</span>
          </div>
          <div class="contest-placeholder">
             (赛事卡片列表加载中...)
          </div>
        </section>

        <section v-if="activeMenu === 'profile'" class="card-layout fade-in">
          <h3>动态能力画像与简历定制</h3>
          <p class="subtitle">基于你的经历，AI 动态生成的个人雷达图</p>
          <div class="profile-demo">
            <div class="radar-placeholder">🎯 雷达图表区域 (开发中)</div>
            <button class="export-btn" @click="openResumeDialog">一键生成专属简历模板</button>
          </div>
        </section>

        <div v-if="isResumeDialogOpen" class="modal-overlay">
          <div class="modal-content">
            <div class="modal-header">
              <h3>✨ 智谱 AI 专属简历定制</h3>
              <span class="close-modal" @click="closeResumeDialog">✖</span>
            </div>
            
            <div class="modal-body">
              <p class="desc">请详细描述您的基本信息、技能栈、做过的项目以及对排版的要求，AI 将为您量身定制：</p>
              <textarea
                v-model="resumePrompt"
                rows="6"
                placeholder="例如：我叫张三，熟悉 Java 和 Vue，做过一个电商管理系统。请帮我生成一份适合投递大厂全栈工程师的简历，语气要专业..."
                :disabled="isGenerating"
              ></textarea>
              
              <div v-if="generatedResume" class="result-area">
                <p class="success-tip">✅ 生成成功！</p>
                <div class="resume-output">{{ generatedResume }}</div>
              </div>
            </div>

            <div class="modal-footer">
              <button class="cancel-btn" @click="closeResumeDialog" :disabled="isGenerating">取消</button>
              <button class="submit-btn" @click="generateResume" :disabled="isGenerating || !resumePrompt.trim()">
                {{ isGenerating ? '大模型正在奋笔疾书中...' : '🚀 开始生成' }}
              </button>
            </div>
          </div>
        </div>
        </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const userRole = ref('student')
const userName = ref('加载中...')
const userAccount = ref('加载中...') 
const userOrg = ref('加载中...') 
const userAvatar = ref('https://api.dicebear.com/7.x/avataaars/svg?seed=Felix')

const activeMenu = ref('home')

const isDropdownOpen = ref(false)
const toggleDropdown = () => isDropdownOpen.value = !isDropdownOpen.value
const closeDropdown = () => isDropdownOpen.value = false

const fetchHeaderUserInfo = async () => {
  try {
    const res = await request.get('/api/user/info')
    if (res) {
      userName.value = res.nickname || res.nickName || '未命名用户'
      userAccount.value = res.phone || '未绑定手机号'
      userOrg.value = res.org || (userRole.value === 'student' ? '未填写学校' : '未填写企业')
      userAvatar.value = res.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'
      
      localStorage.setItem('userName', userName.value)
      localStorage.setItem('userOrg', userOrg.value)
    }
  } catch (error) {
    console.error('获取顶部导航栏信息失败:', error)
    fallbackToLocalData()
  }
}

const fallbackToLocalData = () => {
  const savedOrg = localStorage.getItem('userOrg')
  userOrg.value = savedOrg || (userRole.value === 'student' ? '未知学校' : '未知企业')
  
  const savedName = localStorage.getItem('userName')
  userName.value = savedName || (userRole.value === 'enterprise' ? 'HR' : '学生用户')
  userAccount.value = '暂无网络'
}

onMounted(() => {
  userRole.value = localStorage.getItem('userRole') || 'student'
  fetchHeaderUserInfo()
})

const goToProfile = () => {
  closeDropdown()
  router.push('/user-profile')
}

const logout = () => {
  closeDropdown()
  localStorage.removeItem('token')
  localStorage.removeItem('userRole')
  localStorage.removeItem('userName')
  localStorage.removeItem('userOrg')
  router.push('/login')
}

const publishJob = () => alert('弹出发布招聘信息的表单（待开发）')

const homeData = ref([
  {
    title: '🎓 学业深造推荐',
    target: 'matrix',
    items: [
      { title: '2026考研数学全程复习规划，如何避坑？', type: 'video', typeText: '视频', author: '考研名师讲堂', color: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)' },
      { title: 'QS前50计算机名校申请时间轴及材料准备', type: 'article', typeText: '文章', author: '留学资讯站', color: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)' },
      { title: '工科保研夏令营面试硬核经验分享', type: 'video', typeText: '视频', author: '学霸研习社', color: 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)' },
      { title: '如何通过大数据筛选出最适合你的院校？', type: 'article', typeText: '文章', author: '平台官方', color: 'linear-gradient(135deg, #cfd9df 0%, #e2ebf0 100%)' }
    ]
  },
  {
    title: '💼 产教融合热榜',
    target: 'job',
    items: [
      { title: 'Java/Spring Boot 后端开发高频面试题解析', type: 'video', typeText: '视频', author: '代码大牛', color: 'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)' },
      { title: '从零到一教你如何打造脱颖而出的前端作品集', type: 'article', typeText: '文章', author: '前端进阶指南', color: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%)' },
      { title: '字节跳动数据分析实习生日常揭秘', type: 'video', typeText: '视频', author: '职场Vlog', color: 'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)' },
      { title: '产品经理必备：大厂真实需求文档(PRD)长啥样？', type: 'article', typeText: '文章', author: 'PM干货铺', color: 'linear-gradient(135deg, #fdcbf1 0%, #fdcbf1 1%, #e6dee9 100%)' }
    ]
  },
  {
    title: '🏆 精选竞赛情报',
    target: 'contest',
    items: [
      { title: '美赛(MCM/ICM) O奖团队经验分享：模型与论文双修', type: 'video', typeText: '视频', author: '数模交流圈', color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' },
      { title: '"挑战杯"国奖项目是如何进行答辩路演的？', type: 'video', typeText: '视频', author: '创赛俱乐部', color: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)' },
      { title: '2026全国大学生计算机设计大赛赛道详解', type: 'article', typeText: '文章', author: '赛事公告栏', color: 'linear-gradient(135deg, #ebc0fd 0%, #d9ded8 100%)' },
      { title: '蓝桥杯/ACM 算法竞赛必刷的 50 道核心题', type: 'article', typeText: '文章', author: '算法进阶', color: 'linear-gradient(135deg, #96fbc4 0%, #f9f586 100%)' }
    ]
  },
  {
    title: '📄 热门简历模板 (定制能力画像)',
    target: 'profile',
    items: [
      { title: '互联网大厂技术岗专用 - 极简极客风模板', type: 'template', typeText: '模板', desc: '适用：后端/前端开发、算法工程师', color: '#334155' },
      { title: '商科/运营岗通用 - 清爽时间轴设计模板', type: 'template', typeText: '模板', desc: '适用：产品、运营、市场营销', color: '#0ea5e9' },
      { title: '应届生/实习生破冰模板 - 重点突出校园经历', type: 'template', typeText: '模板', desc: '适用：无太多实战经验的大一/大二学生', color: '#10b981' },
      { title: '设计/传媒岗创意模板 - 视觉冲击力强', type: 'template', typeText: '模板', desc: '适用：UI设计师、原画师、影视后期', color: '#8b5cf6' }
    ]
  }
])

const handleCardClick = (item) => {
  if (item.type === 'video') {
    router.push({
      path: '/video',
      query: { title: item.title, author: item.author || '平台官方' }
    })
  } else if (item.type === 'article') {
    router.push({
      path: '/article',
      query: { title: item.title, author: item.author || '专栏作家' }
    })
  } else if (item.type === 'template') {
    router.push({
      path: '/resume-template',
      query: { title: item.title }
    })
  }
}

const isResumeDialogOpen = ref(false)
const resumePrompt = ref('')
const isGenerating = ref(false)
const generatedResume = ref('')

const openResumeDialog = () => {
  isResumeDialogOpen.value = true
}

const closeResumeDialog = () => {
  if (isGenerating.value) return 
  isResumeDialogOpen.value = false
}

const generateResume = async () => {
  if (!resumePrompt.value.trim()) return
  
  isGenerating.value = true
  
  try {
    const res = await request.post('/api/resume/zhipu/generate', {
      requirements: resumePrompt.value
    })
    
    // 1. 获取 AI 文本
    const content = res.data || res || res.data?.data 
    
    // 2. 存入本地存储，供预览页读取
    localStorage.setItem('generatedResume', content) 
    
    // 3. 关闭弹窗并跳转到预览页
    closeResumeDialog()
    router.push('/resume-preview') // 🌟 核心：跳转到我们刚写的页面
    
  } catch (error) {
    console.error('简历生成失败:', error)
    alert('AI 生成开小差了，请稍后重试！')
  } finally {
    isGenerating.value = false
  }
}
</script>

<style scoped>
.layout { display: flex; flex-direction: column; height: 100vh; width: 100vw; background: #f5f7fa; }
.header { height: 60px; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); z-index: 10; }
.logo { font-size: 20px; font-weight: bold; color: #4a90e2; }
.search-bar input { width: 300px; padding: 8px 15px; border-radius: 20px; border: 1px solid #ddd; outline: none; background: #f8fafc; }
.header-right { display: flex; align-items: center; gap: 20px; }
.go-publish-btn { background: #00aeec; color: white; border: none; padding: 8px 18px; border-radius: 20px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 6px; transition: all 0.3s; box-shadow: 0 4px 10px rgba(0, 174, 236, 0.3); }
.go-publish-btn:hover { background: #00a1d6; transform: translateY(-2px); box-shadow: 0 6px 15px rgba(0, 174, 236, 0.4); }

.user-profile { position: relative; display: flex; align-items: center; gap: 15px; cursor: pointer; height: 100%; padding: 0 10px; }
.role-badge { background: #e6f0fa; color: #4a90e2; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; z-index: 101; }
.avatar { width: 36px; height: 36px; border-radius: 50%; border: 2px solid #e2e8f0; overflow: hidden; display: flex; align-items: center; justify-content: center; background: #f8fafc; transition: 0.3s; position: relative; z-index: 101; }
.user-profile:hover .avatar { border-color: #4a90e2; box-shadow: 0 0 8px rgba(74, 144, 226, 0.3); }
.avatar img { width: 100%; height: 100%; object-fit: cover; }

/* 🌟 新增：透明的全屏遮罩层 */
.dropdown-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 98; cursor: default; }

.dropdown-menu { position: absolute; top: 60px; right: 10px; width: 160px; background: #fff; border-radius: 8px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); z-index: 100; overflow: hidden; animation: slideDown 0.2s ease-out; border: 1px solid #f1f5f9; }
@keyframes slideDown { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.dropdown-header { padding: 15px; text-align: center; background: #f8fafc; display: flex; flex-direction: column; gap: 4px; align-items: center; }
.username { font-size: 15px; font-weight: bold; color: #1e293b; }
.user-account { font-size: 12px; color: #94a3b8; word-break: break-all; } 
.user-org { font-size: 12px; color: #4a90e2; background: #e6f0fa; padding: 4px 10px; border-radius: 12px; margin-top: 6px; display: inline-block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 90%; font-weight: 500; }
.divider { height: 1px; background: #f1f5f9; margin: 0; }
.dropdown-list { list-style: none; padding: 0; margin: 0; }
.dropdown-item { padding: 12px 15px; display: flex; align-items: center; gap: 10px; font-size: 14px; color: #475569; transition: all 0.2s; }
.dropdown-item i { font-style: normal; font-size: 15px; opacity: 0.8; }
.dropdown-item:hover { background: #f0f7ff; color: #4a90e2; padding-left: 18px; }
.dropdown-item:hover i { opacity: 1; }
.main-container { display: flex; flex: 1; overflow: hidden; }
.sidebar { width: 220px; background: #fff; padding: 20px 0; box-shadow: 2px 0 5px rgba(0,0,0,0.02); }
.sidebar ul { list-style: none; padding: 0; margin: 0; }
.sidebar li { padding: 15px 30px; cursor: pointer; color: #666; transition: 0.2s; border-left: 3px solid transparent; }
.sidebar li:hover { background: #f0f7ff; color: #4a90e2; }
.sidebar li.active { background: #e6f0fa; color: #4a90e2; border-left: 3px solid #4a90e2; font-weight: bold; }
.content { flex: 1; padding: 30px; overflow-y: auto; display: flex; flex-direction: column; }
.card-layout { background: #fff; padding: 25px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); min-height: 400px; }
.card-layout h3 { margin-top: 0; color: #333; font-size: 20px; margin-bottom: 5px; }
.subtitle { color: #888; font-size: 14px; margin-bottom: 25px; border-bottom: 2px solid #f0f2f5; padding-bottom: 15px; }
.banner-carousel { height: 240px; background: url('https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80') center/cover no-repeat; border-radius: 12px; margin-bottom: 30px; position: relative; display: flex; align-items: flex-end; box-shadow: 0 8px 20px rgba(0,0,0,0.1); overflow: hidden; }
.banner-carousel::before { content: ''; position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0.1)); }
.banner-content { position: relative; z-index: 1; padding: 30px; color: white; }
.banner-content h2 { margin: 0 0 10px 0; font-size: 28px; }
.banner-content p { margin: 0; font-size: 15px; opacity: 0.9; }
.banner-dots { position: absolute; right: 30px; bottom: 20px; display: flex; gap: 8px; z-index: 1; }
.dot { width: 8px; height: 8px; background: rgba(255,255,255,0.4); border-radius: 50%; cursor: pointer; }
.dot.active { background: white; width: 24px; border-radius: 4px; }
.home-sections { display: flex; flex-direction: column; gap: 35px; }
.section-title-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.section-title-bar h3 { margin: 0; font-size: 22px; color: #1e293b; display: flex; align-items: center; gap: 8px; }
.more-link { color: #64748b; font-size: 14px; cursor: pointer; transition: 0.2s; }
.more-link:hover { color: #4a90e2; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.card { background: #fff; border-radius: 8px; overflow: hidden; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; border: 1px solid #e2e8f0; }
.card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.08); }
.card-cover { height: 140px; position: relative; display: flex; justify-content: flex-end; align-items: flex-end; padding: 10px; }
.type-badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; color: white; font-weight: bold; }
.type-badge.video { background: rgba(239, 68, 68, 0.9); } 
.type-badge.article { background: rgba(59, 130, 246, 0.9); } 
.type-badge.template { background: rgba(16, 185, 129, 0.9); } 
.card-info { padding: 15px; }
.card-title { margin: 0 0 8px 0; font-size: 15px; color: #1e293b; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; height: 42px; }
.card-desc { margin: 0; font-size: 13px; color: #94a3b8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.section-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px;}
.section-header .subtitle { border-bottom: none; margin-bottom: 0; padding-bottom: 0;}
.publish-btn { background: #10b981; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-size: 14px; transition: 0.3s; box-shadow: 0 4px 6px rgba(16, 185, 129, 0.2); }
.publish-btn:hover { background: #059669; }
.info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
.grid-item { background: #f8fafc; padding: 30px 20px; border-radius: 8px; text-align: center; border: 1px solid #e2e8f0; color: #475569; font-weight: 500; cursor: pointer; transition: 0.3s; }
.grid-item:hover { box-shadow: 0 5px 15px rgba(74,144,226,0.1); border-color: #4a90e2; color: #4a90e2; }
.job-item { padding: 15px 10px; border-bottom: 1px dashed #eee; color: #555; cursor: pointer; transition: 0.2s; }
.job-item:hover { background: #fafbfc; color: #4a90e2; padding-left: 15px; }
.tags { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 20px;}
.tag { background: #e6f0fa; color: #4a90e2; padding: 6px 15px; border-radius: 20px; font-size: 13px; cursor: pointer; transition: 0.2s; }
.tag:hover { background: #4a90e2; color: #fff; }
.contest-placeholder, .radar-placeholder { height: 200px; background: #fafbfc; border: 2px dashed #eee; display: flex; align-items: center; justify-content: center; color: #aaa; border-radius: 8px; margin-bottom: 20px; }
.export-btn { background: #4a90e2; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-size: 14px; }
.fade-in { animation: fadeIn 0.4s ease-in-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.4); backdrop-filter: blur(2px);
  display: flex; justify-content: center; align-items: center; z-index: 1000;
  animation: fadeIn 0.2s ease-out;
}
.modal-content {
  background: #fff; width: 650px; max-width: 90%; border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15); display: flex; flex-direction: column;
  overflow: hidden;
}
.modal-header {
  padding: 18px 24px; background: #f8fafc; border-bottom: 1px solid #e2e8f0;
  display: flex; justify-content: space-between; align-items: center;
}
.modal-header h3 { margin: 0; font-size: 18px; color: #1e293b; }
.close-modal { cursor: pointer; color: #94a3b8; font-size: 16px; transition: 0.2s; }
.close-modal:hover { color: #ef4444; transform: scale(1.1); }
.modal-body { padding: 24px; max-height: 60vh; overflow-y: auto; }
.desc { margin: 0 0 12px 0; color: #64748b; font-size: 14px; }
.modal-body textarea {
  width: 100%; padding: 12px; border: 1px solid #cbd5e1; border-radius: 8px;
  font-family: inherit; font-size: 14px; resize: vertical; outline: none; transition: 0.2s;
  box-sizing: border-box;
}
.modal-body textarea:focus { border-color: #4a90e2; box-shadow: 0 0 0 3px rgba(74,144,226,0.1); }
.modal-body textarea:disabled { background: #f1f5f9; cursor: not-allowed; }
.result-area { margin-top: 20px; border-top: 1px dashed #cbd5e1; padding-top: 15px; }
.success-tip { color: #10b981; font-weight: bold; margin: 0 0 10px 0; font-size: 14px; }
.resume-output {
  background: #1e293b; color: #f8fafc; padding: 16px; border-radius: 8px;
  white-space: pre-wrap; font-family: 'Courier New', Courier, monospace;
  font-size: 14px; line-height: 1.6; max-height: 300px; overflow-y: auto;
}
.modal-footer {
  padding: 16px 24px; border-top: 1px solid #e2e8f0; background: #f8fafc;
  display: flex; justify-content: flex-end; gap: 12px;
}
.cancel-btn {
  padding: 8px 16px; background: white; border: 1px solid #cbd5e1;
  border-radius: 6px; cursor: pointer; color: #475569; transition: 0.2s;
}
.cancel-btn:hover:not(:disabled) { background: #f1f5f9; }
.submit-btn {
  padding: 8px 20px; background: #4a90e2; border: none; color: white;
  border-radius: 6px; cursor: pointer; font-weight: bold; transition: 0.2s;
}
.submit-btn:hover:not(:disabled) { background: #357ebd; box-shadow: 0 4px 6px rgba(74,144,226,0.2); }
.submit-btn:disabled { background: #94a3b8; cursor: not-allowed; }
</style>