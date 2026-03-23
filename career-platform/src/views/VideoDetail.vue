<template>
  <div class="video-detail-layout">
    <header class="top-nav">
      <div class="nav-left">
        <button class="back-btn" @click="router.push('/dashboard')">
          <span class="icon">←</span> 返回大厅
        </button>
        <span class="nav-title">📺 学业规划平台 · 视频专区</span>
      </div>
      
      <div class="nav-right">
        <div class="user-profile" @click="toggleDropdown">
          <span class="role-badge">{{ userRole === 'enterprise' ? '企业 HR' : '学生' }}</span>
          <div class="avatar">
            <img :src="userAvatar" alt="头像" />
          </div>
        </div>
      </div>
    </header>

    <div class="main-container">
      <div class="left-col">
        <div class="video-header">
          <h1 class="video-title">{{ videoTitle }}</h1>
          <div class="video-meta">
            <span class="meta-item">
              <span class="meta-icon">▶</span> {{ formatNumber(124.5) }}万播放
            </span>
            <span class="meta-item">
              <span class="meta-icon">📅</span> {{ videoDate }}
            </span>
          </div>
        </div>

        <div class="player-wrapper">
          <video 
            class="video-player" 
            controls 
            autoplay
            :src="videoUrl"
          >
            您的浏览器不支持 HTML5 视频播放。
          </video>
        </div>

        <div class="video-actions">
          <button class="action-btn" :class="{ active: isLiked }" @click="toggleLike">
            <span class="btn-icon">{{ isLiked ? '❤️' : '🤍' }}</span>
            <span>{{ likeCount }} 点赞</span>
          </button>
          
          <button class="action-btn" @click="scrollToComments">
            <span class="btn-icon">💬</span>
            <span>{{ commentList?.length || 0 }} 评论</span>
          </button>
          
          <button class="action-btn star-btn">
            <span class="btn-icon">⭐</span>
            <span>收藏</span>
          </button>
        </div>

        <div class="video-description">
          <div class="author-row">
            <div class="author-avatar">
              <img :src="authorAvatar" alt="作者头像" />
            </div>
            <div class="author-info">
              <span class="author-name">{{ videoAuthor }}</span>
              <span class="author-stats">粉丝 {{ formatNumber(25.6) }}万 · 已发布 48 个视频</span>
            </div>
            <button class="follow-btn">+ 关注</button>
          </div>
          <div class="desc-text">
            {{ videoDesc }}
          </div>
        </div>

        <div class="comment-section" id="comment-area">
          <h3 class="comment-title">📝 学员评论</h3>
          
          <div class="comment-input">
            <div class="input-avatar">
              <img :src="userAvatar" alt="我的头像" />
            </div>
            <div class="input-wrapper">
              <textarea 
                v-model="newCommentContent" 
                placeholder="分享你的学习心得..."
                rows="2"
              ></textarea>
              <button class="submit-btn" @click="submitComment">发表</button>
            </div>
          </div>
          
          <div class="comment-list">
            <div class="comment-item" v-for="item in commentList" :key="item.id">
              <div class="comment-avatar">
                <img :src="item.authorAvatar || defaultAvatar" alt="" />
              </div>
              <div class="comment-content">
                <div class="comment-name">{{ item.authorName }}</div>
                <div class="comment-text">{{ item.content }}</div>
                <div class="comment-meta">
                  <span>{{ formatDate(item.gmtCreated || item.createTime) }}</span>
                  <span class="reply-btn">回复</span>
                </div>
              </div>
            </div>
            
            <div v-if="!commentList?.length" class="empty-comment">
              <span>💬</span>
              <p>还没有评论，快来分享你的学习心得吧！</p>
            </div>
          </div>
        </div>
      </div>

      <div class="right-col">
        <div class="recommend-section">
          <h3 class="section-title">📖 你可能还想看</h3>
          <div class="recommend-list">
            <div 
              class="recommend-item" 
              v-for="video in recommendList" 
              :key="video.id" 
              @click="goToRecommend(video)"
            >
              <div class="rec-cover">
                <img :src="video.coverUrl" :alt="video.title" />
                <span class="rec-duration">{{ video.duration }}</span>
              </div>
              <div class="rec-info">
                <div class="rec-title">{{ video.title }}</div>
                <div class="rec-author">{{ video.author }} · {{ formatNumber(video.views) }}播放</div>
              </div>
            </div>
            
            <div v-if="!recommendList?.length" style="text-align: center; color: #94a3b8; padding: 20px 0; font-size: 13px;">
              暂无推荐视频
            </div>
          </div>
        </div>

        <div class="hot-topics">
          <h3 class="section-title">🔥 大家都在学</h3>
          <div class="topic-list">
            <div class="topic-item" v-for="(topic, index) in hotTopics" :key="index">
              <span class="topic-rank">{{ index + 1 }}</span>
              <span class="topic-name">{{ topic.title }}</span>
              <span class="topic-hot">🔥 {{ formatNumber(topic.hotValue) }}人在学</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()

// 用户信息
const userRole = ref(localStorage.getItem('userRole') || 'student')
const userAvatar = ref(localStorage.getItem('userAvatar') || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default')

// 视频数据
const videoTitle = ref('')
const videoAuthor = ref('')
const videoUrl = ref('')
const videoDate = ref('2026-03-05')
const videoDesc = ref('这是一期面向新手的全栈开发保姆级教程。手把手教你解决跨域、JWT 鉴权、文件上传等核心痛点。如果觉得有用，请务必给个一键三连支持一下！')
const authorAvatar = ref('https://api.dicebear.com/7.x/adventurer/svg?seed=author')
const defaultAvatar = 'https://api.dicebear.com/7.x/adventurer/svg?seed=default'

const likeCount = ref(0)
const isLiked = ref(false)
const commentList = ref([])
const newCommentContent = ref('')

// 添加推荐视频和热门话题的响应式数据
const recommendList = ref([])
const hotTopics = ref([])

// 获取侧边栏推荐数据
const fetchRecommendData = async () => {
  try {
    const res = await request.get('/api/content/video/recommend')
    
    // ✅ 强行数组校验，避免一切由于后端数据结构不对引起的渲染崩溃
    if (res && res.recommendVideos) {
      recommendList.value = Array.isArray(res.recommendVideos) ? res.recommendVideos : []
      hotTopics.value = Array.isArray(res.hotTopics) ? res.hotTopics : []
    } else {
      loadMockRecommendData()
    }
  } catch (error) {
    console.error('获取侧边栏数据失败', error)
    loadMockRecommendData()
  }
}

// 提取出模拟数据函数，避免代码污染
const loadMockRecommendData = () => {
  recommendList.value = [
    {
      id: 'v101',
      title: 'Vue3 + ElementPlus 企业级后台管理系统速成',
      author: '全栈小导师',
      coverUrl: 'https://picsum.photos/100/56?random=11',
      duration: '12:34',
      views: 215000,
      url: 'https://www.w3schools.com/html/mov_bbb.mp4'
    },
    {
      id: 'v102',
      title: '2026春招前端面试高频手写题全解析',
      author: '前端大牛',
      coverUrl: 'https://picsum.photos/100/56?random=12',
      duration: '45:12',
      views: 184000,
      url: 'https://www.w3schools.com/html/mov_bbb.mp4'
    },
    {
      id: 'v103',
      title: '十分钟搞懂计算机网络 TCP/IP 三次握手',
      author: '网络极客',
      coverUrl: 'https://picsum.photos/100/56?random=13',
      duration: '10:05',
      views: 350000,
      url: 'https://www.w3schools.com/html/mov_bbb.mp4'
    },
    {
      id: 'v104',
      title: 'Spring Boot 3.0 微服务架构实战',
      author: 'Java架构师',
      coverUrl: 'https://picsum.photos/100/56?random=14',
      duration: '58:20',
      views: 98000,
      url: 'https://www.w3schools.com/html/mov_bbb.mp4'
    },
    {
      id: 'v105',
      title: '如何写出一份HR无法拒绝的求职简历？',
      author: '职场达人',
      coverUrl: 'https://picsum.photos/100/56?random=15',
      duration: '18:45',
      views: 420000,
      url: 'https://www.w3schools.com/html/mov_bbb.mp4'
    }
  ]
  hotTopics.value = [
    { title: '考研数学全程复习规划', hotValue: 124000 },
    { title: 'Java并发编程实战', hotValue: 87000 },
    { title: 'Vue3源码解析', hotValue: 62000 },
    { title: '产品经理入门指南', hotValue: 51000 },
    { title: '数据分析师必备技能', hotValue: 43000 }
  ]
}

// 视频跳转逻辑
const goToRecommend = (video) => {
  // 按照你的路由配置，确保跳转到 /video 
  router.push({
    path: '/video',
    query: {
      title: video.title,
      author: video.author,
      url: video.url
    }
  })
}

// 辅助函数
const formatNumber = (num) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toString()
}

onMounted(() => {
  videoTitle.value = route.query.title || '【默认测试视频】'
  videoAuthor.value = route.query.author || '全栈小导师'
  videoUrl.value = route.query.url || 'https://www.w3schools.com/html/mov_bbb.mp4'
  
  fetchLikeStatus()
  fetchComments()
  fetchRecommendData()

  document.documentElement.style.overflow = 'auto'
  document.documentElement.style.height = 'auto'
  document.body.style.overflow = 'auto'
  document.body.style.height = 'auto'
})

// 监听路由参数变化，实现丝滑切换视频
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.title) {
      videoTitle.value = newQuery.title
      videoAuthor.value = newQuery.author || '全栈小导师'
      videoUrl.value = newQuery.url
      
      fetchLikeStatus()
      fetchComments()
      // fetchRecommendData() 可以不加，保持侧边栏推荐列表稳定
      
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }
)

const fetchLikeStatus = async () => {
  try {
    const res = await request.get(`/api/interaction/like/status?targetId=${videoTitle.value}`)
    likeCount.value = res.totalLikes || 0
    isLiked.value = res.isLiked || false
  } catch (error) {
    console.error('获取点赞状态失败', error)
  }
}

const toggleLike = async () => {
  try {
    await request.post(`/api/interaction/like/toggle?targetId=${videoTitle.value}`)
    fetchLikeStatus()
  } catch (error) {
    alert('请先登录后再点赞！')
  }
}

const fetchComments = async () => {
  try {
    const res = await request.get(`/api/interaction/comments?targetId=${videoTitle.value}`)
    
    // ✅ 提取真实数据层
    const rawData = res?.data || res
    
    // ✅ 强行数组校验：不管后端报没报错、数据格式怎么变，如果是数组就赋值，不是数组就给 []
    commentList.value = Array.isArray(rawData) ? rawData : []
  } catch (error) {
    console.error('获取评论失败', error)
    commentList.value = []
  }
}

const submitComment = async () => {
  if (!newCommentContent.value.trim()) return alert('评论内容不能为空！')
  try {
    await request.post('/api/interaction/comment', {
      targetId: videoTitle.value,
      content: newCommentContent.value
    })
    newCommentContent.value = ''
    fetchComments()
  } catch (error) {
    alert('评论失败，请确认是否已登录。')
  }
}

const scrollToComments = () => {
  const commentArea = document.getElementById('comment-area')
  if (commentArea) {
    commentArea.scrollIntoView({ behavior: 'smooth' })
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const toggleDropdown = () => {
  // 用户下拉菜单
}
</script>

<style scoped>
/* 强制页面可滚动 */
html, body {
  overflow: auto !important;
  height: auto !important;
}

.video-detail-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef2f5 100%);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 导航栏 */
.top-nav {
  height: 64px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(74, 163, 209, 0.15);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(74, 163, 209, 0.3);
  padding: 8px 20px;
  border-radius: 40px;
  cursor: pointer;
  font-size: 14px;
  color: #1e293b;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.back-btn:hover {
  background: white;
  border-color: #4aa3d1;
  transform: translateY(-2px);
}

.nav-title {
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #1e4f6b, #2d8b6f);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.role-badge {
  background: #e0f2fe;
  color: #0369a1;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #4aa3d1;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 主内容区 */
.main-container {
  max-width: 1300px;
  margin: 0 auto;
  padding: 30px;
  display: flex;
  gap: 30px;
}

.left-col {
  flex: 1;
  min-width: 0;
}

.right-col {
  width: 320px;
  flex-shrink: 0;
}
/* 视频头部 */
.video-header {
  margin-bottom: 20px;
}

.video-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.video-meta {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

/* 播放器 */
.player-wrapper {
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #000;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 操作栏 - 简化版 */
.video-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(74, 163, 209, 0.2);
  padding: 8px 20px;
  border-radius: 40px;
  cursor: pointer;
  font-size: 14px;
  color: #475569;
  transition: all 0.3s;
}

.action-btn:hover {
  background: white;
  border-color: #4aa3d1;
  transform: translateY(-2px);
}

.action-btn.active {
  background: #fee2e2;
  border-color: #ef4444;
  color: #ef4444;
}

.star-btn:hover {
  border-color: #f59e0b;
  color: #f59e0b;
}

/* 作者信息 - 更简洁 */
.video-description {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 30px;
  border: 1px solid rgba(74, 163, 209, 0.15);
}

.author-row {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-info {
  flex: 1;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  display: block;
}

.author-stats {
  font-size: 12px;
  color: #64748b;
}

.follow-btn {
  background: linear-gradient(135deg, #4aa3d1, #2d8b6f);
  color: white;
  border: none;
  padding: 6px 18px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.follow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(74, 163, 209, 0.3);
}

.desc-text {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
}

/* 评论区 */
.comment-section {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 20px;
  border: 1px solid rgba(74, 163, 209, 0.15);
  margin-bottom: 30px;
}

.comment-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 20px 0;
}

.comment-input {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
}

.input-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.input-wrapper {
  flex: 1;
  position: relative;
}

.input-wrapper textarea {
  width: 100%;
  padding: 12px 80px 12px 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 20px;
  font-size: 14px;
  background: white;
  resize: none;
  font-family: inherit;
}

.input-wrapper textarea:focus {
  outline: none;
  border-color: #4aa3d1;
  box-shadow: 0 0 0 3px rgba(74, 163, 209, 0.1);
}

.submit-btn {
  position: absolute;
  right: 10px;
  bottom: 10px;
  background: linear-gradient(135deg, #4aa3d1, #2d8b6f);
  color: white;
  border: none;
  padding: 6px 20px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.comment-list {
  max-height: 400px;
  overflow-y: auto;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 15px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.comment-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.comment-text {
  font-size: 14px;
  color: #475569;
  line-height: 1.5;
  margin-bottom: 6px;
}

.comment-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #94a3b8;
}

.reply-btn {
  cursor: pointer;
}

.reply-btn:hover {
  color: #4aa3d1;
}

.empty-comment {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
}

.empty-comment span {
  font-size: 40px;
  display: block;
  margin-bottom: 10px;
}

/* 右侧推荐区 - 学习推荐风格 */
.recommend-section,
.hot-topics {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(74, 163, 209, 0.15);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.recommend-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.recommend-item:hover {
  transform: translateX(4px);
}

.rec-cover {
  position: relative;
  width: 100px;
  height: 56px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.rec-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.rec-duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.rec-info {
  flex: 1;
}

.rec-title {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
  line-height: 1.4;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rec-author {
  font-size: 11px;
  color: #64748b;
}

/* 热门话题列表 */
.topic-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

.topic-item:hover {
  transform: translateX(4px);
}

.topic-rank {
  width: 24px;
  height: 24px;
  background: #f1f5f9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
}

.topic-item:first-child .topic-rank {
  background: #fef3c7;
  color: #d97706;
}

.topic-item:nth-child(2) .topic-rank {
  background: #f1f5f9;
  color: #475569;
}

.topic-item:nth-child(3) .topic-rank {
  background: #fef2f2;
  color: #ef4444;
}

.topic-name {
  flex: 1;
  font-size: 13px;
  color: #1e293b;
  font-weight: 500;
}

.topic-hot {
  font-size: 11px;
  color: #f97316;
  background: #fff7ed;
  padding: 2px 8px;
  border-radius: 20px;
}
</style>