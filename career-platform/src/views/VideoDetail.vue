<template>
  <div class="bilibili-layout">
    <header class="top-nav">
      <div class="back-home-pill" @click="router.push('/dashboard')">
        <span class="back-arrow">←</span>
        <span class="back-text">返回主页</span>
      </div>

      <div class="nav-logo">📺 学业规划平台视频专区</div>
      <div class="search-box"><input type="text" placeholder="搜索你想看的视频..."></div>
      <div class="user-avatar"><img src="https://api.dicebear.com/7.x/adventurer/svg?seed=user" alt=""></div>
    </header>

    <div class="main-container">
      <div class="left-col">
        <div class="video-header">
          <h1 class="video-title">{{ videoTitle }}</h1>
          <div class="video-meta">
            <span><i class="icon">▶</i> 124.5万播放</span>
            <span><i class="icon">💬</i> 8,432弹幕</span>
            <span>2026-03-05 14:30:00</span>
          </div>
        </div>

      <div class="player-wrapper">
          <video 
            class="b-player" 
            controls 
            autoplay
            :src="videoUrl"
            >
            您的浏览器不支持 HTML5 视频播放。
          </video>
        </div>

        <div class="video-toolbar">
          <div class="action-btn" :class="{ 'liked-text': isLiked }" @click="toggleLike">
            <span class="emoji">{{ isLiked ? '💖' : '🤍' }}</span> 
            点赞 {{ likeCount }}
          </div>
          <div class="action-btn" @click="scrollToComments">
            <span class="emoji">💬</span> 评论 {{ commentList.length }}
          </div>
          <div class="action-btn"><span class="emoji">⭐</span> 收藏</div>
        </div>

        <div class="video-desc">
          这是一期面向新手的全栈开发保姆级教程。手把手教你解决跨域、JWT 鉴权、文件上传等核心痛点...
          <br> 如果觉得有用，请务必给个一键三连支持一下！
        </div>

        <div class="comment-section" id="comment-area">
          <h3>评论 <span class="comment-count">{{ commentList.length }}</span></h3>
          <div class="comment-input-box">
            <img class="my-avatar" src="https://api.dicebear.com/7.x/adventurer/svg?seed=me" alt="">
            <textarea placeholder="发一条友善的评论吧..." v-model="newCommentContent"></textarea>
            <button class="send-btn" @click="submitComment">发表评论</button>
          </div>
          
          <div class="comment-list">
            <div class="comment-item" v-for="item in commentList" :key="item.id">
              <img class="c-avatar" :src="item.authorAvatar || 'https://api.dicebear.com/7.x/adventurer/svg?seed=default'" alt="">
              <div class="c-content">
                <div class="c-name">{{ item.authorName }}</div>
                <div class="c-text">{{ item.content }}</div>
                <div class="c-meta">{{ formatDate(item.gmtCreated || item.createTime) }} | 回复</div>
              </div>
            </div>
            
            <div v-if="commentList.length === 0" style="color: #999; text-align: center; padding: 20px;">
              还没有人评论，快来抢沙发吧！
            </div>
          </div>
        </div>
      </div>

      <div class="right-col">
        <div class="up-card">
          <img class="up-avatar" src="https://api.dicebear.com/7.x/adventurer/svg?seed=up" alt="">
          <div class="up-info">
            <div class="up-name">{{ videoAuthor }}</div>
            <div class="up-desc">粉丝：25.6万</div>
          </div>
          <button class="follow-btn">+ 关注</button>
        </div>

        <div class="danmaku-box">
          <div class="box-title">弹幕列表</div>
          <div class="danmaku-list">
            <div class="d-item">01:23 前方高能！！！</div>
            <div class="d-item">03:45 做好笔记了</div>
            <div class="d-item">05:12 这里没听懂，求弹幕解答</div>
          </div>
        </div>

        <div class="recommend-list">
          <div class="rec-title">相关推荐</div>
          <div class="rec-item" v-for="j in 6" :key="j">
            <div class="rec-cover">
              <img :src="`https://picsum.photos/200/120?random=${j}`" alt="">
              <span class="duration">12:34</span>
            </div>
            <div class="rec-info">
              <div class="r-title">Vue3 + ElementPlus 企业级后台管理系统速成</div>
              <div class="r-up">UP: 全栈小导师</div>
              <div class="r-view">24.5万播放</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router' 
import request from '../utils/request'

const route = useRoute()
const router = useRouter() 

const videoTitle = ref('')
const videoAuthor = ref('')
// 🌟 核心修改点：新增 videoUrl 响应式变量
const videoUrl = ref('') 

const likeCount = ref(0)
const isLiked = ref(false)
const commentList = ref([])
const newCommentContent = ref('')

onMounted(() => {
  // 从路由 query 中获取参数，如果没有则使用备用的假数据保底
  videoTitle.value = route.query.title || '【默认测试视频】'
  videoAuthor.value = route.query.author || '全栈小导师'
  // 🌟 核心修改点：接收从 Dashboard 传过来的 OSS 真实链接
  videoUrl.value = route.query.url || 'https://www.w3schools.com/html/mov_bbb.mp4'

  fetchLikeStatus()
  fetchComments()
})

const fetchLikeStatus = async () => {
  try {
    const res = await request.get(`/api/interaction/like/status?targetId=${videoTitle.value}`)
    likeCount.value = res.totalLikes
    isLiked.value = res.isLiked
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
    commentList.value = res
  } catch (error) {
    console.error('获取评论失败', error)
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
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
/* 保持你的原样 */
.bilibili-layout { 
  background-color: #f4f5f7; 
  height: 100vh; 
  overflow-y: auto; 
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; 
}

.top-nav { height: 64px; background: #fff; box-shadow: 0 2px 4px rgba(0,0,0,0.08); display: flex; justify-content: space-between; align-items: center; padding: 0 40px; position: sticky; top: 0; z-index: 100; }

.back-home-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background-color: #f1f2f3;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  flex-shrink: 0;
}

.back-home-pill:hover {
  background-color: #e3e5e7;
  color: #00aeec;
}

.back-arrow { font-size: 18px; font-weight: bold; }
.back-text { font-size: 14px; font-weight: 500; }

.nav-logo { font-size: 18px; font-weight: bold; color: #18191c; }
.search-box input { width: 300px; padding: 10px 15px; border: 1px solid #e5e9ef; border-radius: 6px; background: #f4f5f7; outline: none; }
.search-box input:focus { border-color: #00aeec; }
.user-avatar img { width: 40px; height: 40px; border-radius: 50%; }

.main-container { max-width: 1400px; margin: 0 auto; padding: 20px; display: flex; gap: 30px; }
.left-col { flex: 1; min-width: 0; }
.right-col { width: 350px; flex-shrink: 0; }
.video-header { margin-bottom: 20px; }
.video-title { font-size: 22px; font-weight: 500; color: #18191c; margin: 0 0 10px 0; line-height: 1.4; }
.video-meta { font-size: 13px; color: #9499a0; display: flex; gap: 15px; }
.player-wrapper { width: 100%; aspect-ratio: 16 / 9; background: #000; border-radius: 6px; overflow: hidden; margin-bottom: 15px; }
.b-player { width: 100%; height: 100%; object-fit: contain; }
.video-toolbar { display: flex; gap: 30px; padding: 15px 0; border-bottom: 1px solid #e3e5e7; margin-bottom: 20px; }
.action-btn { display: flex; align-items: center; gap: 8px; color: #61666d; cursor: pointer; font-size: 14px; font-weight: 500; transition: color 0.3s; }
.action-btn:hover { color: #00aeec; }
.emoji { font-size: 20px; }
.video-desc { font-size: 14px; color: #18191c; line-height: 1.6; margin-bottom: 40px; padding-bottom: 20px; border-bottom: 1px solid #e3e5e7; }
.up-card { display: flex; align-items: center; gap: 12px; padding: 0 0 20px 0; margin-bottom: 20px; border-bottom: 1px solid #e3e5e7; }
.up-avatar { width: 48px; height: 48px; border-radius: 50%; }
.up-info { flex: 1; }
.up-name { font-size: 15px; font-weight: 500; color: #fb7299; margin-bottom: 4px; }
.up-desc { font-size: 12px; color: #9499a0; }
.follow-btn { background: #00aeec; color: #fff; border: none; padding: 6px 18px; border-radius: 4px; cursor: pointer; font-size: 14px; transition: 0.3s; }
.follow-btn:hover { background: #00a1d6; }
.danmaku-box { background: #f1f2f3; border-radius: 6px; padding: 15px; margin-bottom: 25px; height: 200px; overflow-y: auto; }
.box-title { font-size: 14px; font-weight: bold; margin-bottom: 10px; color: #18191c; }
.d-item { font-size: 12px; color: #61666d; margin-bottom: 8px; }
.rec-title { font-size: 16px; font-weight: 500; margin-bottom: 15px; color: #18191c; }
.rec-item { display: flex; gap: 10px; margin-bottom: 15px; cursor: pointer; }
.rec-cover { position: relative; width: 140px; height: 80px; border-radius: 6px; overflow: hidden; flex-shrink: 0; }
.rec-cover img { width: 100%; height: 100%; object-fit: cover; }
.duration { position: absolute; bottom: 4px; right: 4px; background: rgba(0,0,0,0.5); color: #fff; font-size: 12px; padding: 2px 4px; border-radius: 2px; }
.rec-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.r-title { font-size: 14px; color: #18191c; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.4; transition: color 0.3s; }
.rec-item:hover .r-title { color: #00aeec; }
.r-up, .r-view { font-size: 12px; color: #9499a0; }
.comment-section h3 { font-size: 18px; margin-bottom: 20px; }
.comment-count { font-size: 14px; color: #9499a0; margin-left: 10px; }
.comment-input-box { display: flex; gap: 15px; margin-bottom: 30px; }
.my-avatar { width: 48px; height: 48px; border-radius: 50%; }
.comment-input-box textarea { flex: 1; height: 60px; padding: 10px; border: 1px solid #e3e5e7; border-radius: 6px; background: #f1f2f3; outline: none; transition: 0.3s; resize: none; font-family: inherit; }
.comment-input-box textarea:focus { border-color: #00aeec; background: #fff; }
.send-btn { width: 80px; background: #00aeec; color: #fff; border: none; border-radius: 6px; cursor: pointer; transition: 0.3s; }
.send-btn:hover { background: #00a1d6; }
.comment-item { display: flex; gap: 15px; margin-bottom: 20px; border-bottom: 1px solid #f1f2f3; padding-bottom: 20px; }
.c-content { flex: 1; }
.c-name { font-size: 13px; color: #fb7299; font-weight: 500; margin-bottom: 8px; }
.c-text { font-size: 15px; color: #18191c; line-height: 1.5; margin-bottom: 10px; }
.c-meta { font-size: 12px; color: #9499a0; }
.liked-text { color: #fb7299 !important; font-weight: bold; }
.c-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
</style>