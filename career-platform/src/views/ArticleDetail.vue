<template>
  <div class="article-layout">
    <header class="top-nav">
      <button class="article-back-action" @click="router.push('/dashboard')">
        <span class="icon">←</span> 返回主页
      </button>

      <div class="nav-logo" @click="router.push('/dashboard')">📚 学业规划平台 - 专区</div>
      <div class="top-right-placeholder"></div>
    </header>

    <div class="main-container">
      <main class="article-content-box">
        <h1 class="article-title">{{ articleTitle }}</h1>
        
        <div class="article-meta">
          <img class="author-avatar" src="https://api.dicebear.com/7.x/avataaars/svg?seed=writer" alt="作者头像">
          <div class="meta-info">
            <div class="author-name">{{ articleAuthor }} <span class="badge">专栏作者</span></div>
            <div class="publish-time">发布于 2026-03-06 · 1.2w 次阅读</div>
          </div>
          <button class="follow-btn">+ 关注</button>
        </div>

        <div class="divider"></div>

        <article class="rich-text-body" v-html="articleContent"></article>

        <div class="article-actions">
          <div class="action-item" :class="{ 'liked-active': isLiked }" @click="toggleLike">
            <span class="icon">{{ isLiked ? '💖' : '👍' }}</span> 
            {{ isLiked ? '已赞同' : '赞同' }} {{ likeCount }}
          </div>
          <div class="action-item"><span class="icon">⭐</span> 收藏</div>
          <div class="action-item" @click="scrollToComments"><span class="icon">💬</span> 评论 {{ commentList.length }}</div>
        </div>

        <div class="divider" style="margin-top: 40px;"></div>

        <div class="comment-module" id="comment-area">
          <h3>评论 <span class="count">{{ commentList.length }}</span></h3>
          <div class="comment-input-area">
            <img class="my-avatar" src="https://api.dicebear.com/7.x/adventurer/svg?seed=me" alt="我的头像">
            <div class="input-wrapper">
              <textarea placeholder="写下你的评论..." v-model="newCommentContent"></textarea>
              <div class="input-actions">
                <button class="submit-btn" :disabled="!newCommentContent.trim()" @click="submitComment">发布</button>
              </div>
            </div>
          </div>

          <div class="comment-list">
            <div class="comment-item" v-for="item in commentList" :key="item.id">
              <img class="c-avatar" :src="item.authorAvatar || 'https://api.dicebear.com/7.x/adventurer/svg?seed=default'" alt="">
              <div class="c-content-box">
                <div class="c-name">{{ item.authorName }}</div>
                <div class="c-text">{{ item.content }}</div>
                <div class="c-footer">
                  <span class="c-time">{{ formatDate(item.gmtCreated || item.createTime) }}</span>
                  <span class="c-reply">回复</span>
                </div>
              </div>
            </div>
            <div v-if="commentList.length === 0" class="empty-comment">
              还没有人评论，快来抢沙发吧！
            </div>
          </div>
        </div>
      </main>

      <aside class="right-sidebar">
        <div class="sidebar-card author-card">
          <h3>关于作者</h3>
          <div class="auth-desc">资深校招导师，前大厂 HR。专注解决大学生秋招、春招求职难题，分享最硬核的笔试面试经验。</div>
          <div class="auth-stats">
            <div><strong>124</strong><span>文章</span></div>
            <div><strong>2.5w</strong><span>粉丝</span></div>
            <div><strong>10w+</strong><span>获赞</span></div>
          </div>
        </div>
        <div class="sidebar-card">
          <h3>相关推荐阅读</h3>
          <ul class="recommend-list">
            <li>写给大一新生的计算机专业避坑指南</li>
            <li>如何用 STAR 法则写出一份完美的简历？</li>
            <li>秋招群面 (无领导小组) 角色担当与高分技巧</li>
            <li>从 0 到 1：你的第一份互联网大厂实习怎么找</li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()

const articleTitle = ref('加载中...')
const articleAuthor = ref('加载中...')
const articleContent = ref('')
const likeCount = ref(0)
const isLiked = ref(false)
const commentList = ref([])
const newCommentContent = ref('')

onMounted(() => {
  articleTitle.value = route.query.title || '【深度解析】大学生如何做好职业生涯规划？'
  articleAuthor.value = route.query.author || '职场领路人'
  articleContent.value = `<p>这是一段由<strong>富文本编辑器</strong>生成的测试内容...</p>` // 缩略模拟
  fetchLikeStatus()
  fetchComments()
})

const fetchLikeStatus = async () => {
  try {
    const res = await request.get(`/api/interaction/like/status?targetId=${articleTitle.value}`)
    likeCount.value = res.totalLikes
    isLiked.value = res.isLiked
  } catch (error) {}
}

const toggleLike = async () => {
  try {
    await request.post(`/api/interaction/like/toggle?targetId=${articleTitle.value}`)
    fetchLikeStatus()
  } catch (error) { alert('请先登录！') }
}

const fetchComments = async () => {
  try {
    const res = await request.get(`/api/interaction/comments?targetId=${articleTitle.value}`)
    commentList.value = res
  } catch (error) {}
}

const submitComment = async () => {
  if (!newCommentContent.value.trim()) return
  try {
    await request.post('/api/interaction/comment', {
      targetId: articleTitle.value,
      content: newCommentContent.value
    })
    newCommentContent.value = ''
    fetchComments()
  } catch (error) { alert('评论失败！') }
}

const scrollToComments = () => {
  document.getElementById('comment-area').scrollIntoView({ behavior: 'smooth' })
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
/* 原有基础样式... */
.article-layout { min-height: 100vh; background-color: #f6f6f6; font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", Arial, sans-serif; color: #333; }

.top-nav { height: 60px; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); display: flex; justify-content: space-between; align-items: center; padding: 0 40px; position: sticky; top: 0; z-index: 100; }

/* 🌟 文章返回按钮样式 */
.article-back-action {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid #ccd0d7;
  color: #666;
  padding: 6px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.article-back-action:hover {
  background: #f6f6f6;
  color: #121212;
  border-color: #8590a6;
}

.article-back-action .icon { font-weight: bold; }

.nav-logo { font-size: 18px; font-weight: bold; color: #4a90e2; cursor: pointer; flex: 1; margin-left: 20px;}
.top-right-placeholder { width: 120px; } /* 保持平衡的空占位 */

/* ...其他原有样式... */
.main-container { max-width: 1100px; margin: 20px auto; display: flex; gap: 20px; align-items: flex-start; padding: 0 20px; }
.article-content-box { flex: 1; background: #fff; border-radius: 8px; padding: 40px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); min-width: 0; }
.article-title { font-size: 28px; font-weight: bold; color: #121212; line-height: 1.4; margin-top: 0; margin-bottom: 25px; }
.article-meta { display: flex; align-items: center; margin-bottom: 30px; }
.author-avatar { width: 48px; height: 48px; border-radius: 50%; background: #f0f0f0; margin-right: 15px; }
.meta-info { flex: 1; }
.author-name { font-size: 16px; font-weight: 600; color: #121212; margin-bottom: 4px; display: flex; align-items: center; gap: 8px; }
.badge { font-size: 12px; background: #e6f0fa; color: #4a90e2; padding: 2px 8px; border-radius: 4px; font-weight: normal; }
.publish-time { font-size: 14px; color: #8590a6; }
.follow-btn { background: #4a90e2; color: #fff; border: none; padding: 8px 20px; border-radius: 20px; font-size: 14px; cursor: pointer; transition: 0.3s; font-weight: 500; }
.follow-btn:hover { background: #357abd; }
.divider { height: 1px; background: #ebebeb; margin-bottom: 30px; }
.rich-text-body { font-size: 16px; line-height: 1.8; color: #333; }
.article-actions { display: flex; gap: 30px; margin-top: 50px; padding-top: 20px; }
.action-item { display: flex; align-items: center; gap: 8px; color: #8590a6; cursor: pointer; font-size: 15px; transition: color 0.3s; font-weight: 500; }
.action-item:hover { color: #4a90e2; }
.liked-active { color: #00aeec !important; font-weight: bold; }
.comment-module h3 { font-size: 18px; color: #121212; margin-bottom: 20px; }
.comment-module .count { color: #8590a6; font-size: 15px; font-weight: normal; margin-left: 5px; }
.comment-input-area { display: flex; gap: 15px; margin-bottom: 30px; }
.my-avatar { width: 40px; height: 40px; border-radius: 50%; }
.input-wrapper { flex: 1; border: 1px solid #ebebeb; border-radius: 6px; padding: 10px; background: #fbfbfc; transition: 0.3s; }
.input-wrapper:focus-within { border-color: #00aeec; background: #fff; }
.input-wrapper textarea { width: 100%; height: 60px; border: none; background: transparent; resize: none; outline: none; font-size: 14px; font-family: inherit; }
.input-actions { display: flex; justify-content: flex-end; margin-top: 10px; }
.submit-btn { background: #00aeec; color: #fff; border: none; padding: 6px 16px; border-radius: 4px; cursor: pointer; transition: 0.3s; }
.submit-btn:disabled { background: #94cce2; cursor: not-allowed; }
.comment-list { display: flex; flex-direction: column; gap: 20px; }
.comment-item { display: flex; gap: 15px; padding-bottom: 20px; border-bottom: 1px solid #f0f2f7; }
.c-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
.c-content-box { flex: 1; }
.c-name { font-size: 15px; font-weight: 600; color: #444; margin-bottom: 6px; }
.c-text { font-size: 15px; color: #121212; line-height: 1.6; margin-bottom: 10px; }
.c-footer { display: flex; align-items: center; gap: 15px; font-size: 13px; color: #8590a6; }
.c-reply { cursor: pointer; }
.empty-comment { text-align: center; color: #999; padding: 40px 0; font-size: 14px; }
.right-sidebar { width: 300px; flex-shrink: 0; display: flex; flex-direction: column; gap: 20px; position: sticky; top: 80px; }
.sidebar-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.sidebar-card h3 { margin-top: 0; font-size: 16px; color: #121212; border-left: 4px solid #4a90e2; padding-left: 10px; margin-bottom: 15px; }
.auth-desc { font-size: 14px; color: #666; line-height: 1.6; margin-bottom: 15px; }
.auth-stats { display: flex; justify-content: space-between; text-align: center; border-top: 1px solid #ebebeb; padding-top: 15px; }
.auth-stats div { display: flex; flex-direction: column; gap: 4px; }
.auth-stats strong { font-size: 16px; color: #121212; }
.auth-stats span { font-size: 12px; color: #8590a6; }
.recommend-list { list-style: none; padding: 0; margin: 0; }
.recommend-list li { font-size: 14px; color: #333; line-height: 1.5; margin-bottom: 15px; cursor: pointer; transition: 0.2s; }
.recommend-list li:hover { color: #4a90e2; }
</style>