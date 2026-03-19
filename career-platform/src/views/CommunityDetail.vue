<template>
  <div class="community-page">
    <header class="community-header">
      <div class="header-inner">
        <button class="back-btn" @click="router.back()">← 返回大厅</button>
        <h2>互联网/IT 技术交流圈</h2>
      </div>
    </header>

    <div class="content-layout">
      <main class="feed-column">
        <div class="publish-box">
          <div class="user-avatar">👤</div>
          <div class="input-area">
            <textarea v-model="newPostContent" placeholder="分享你的技术见解、面经或疑问..." rows="3"></textarea>
            <div class="publish-actions">
              <button class="pic-btn">📷 图片</button>
              <button class="submit-btn" @click="publishPost" :disabled="!newPostContent.trim()">发布博客</button>
            </div>
          </div>
        </div>

        <div class="post-list">
          <div v-for="post in posts" :key="post.id" class="post-card">
            <div class="post-header">
              <div class="author-avatar">{{ post.author.substring(0,1) }}</div>
              <div class="author-info">
                <span class="author-name">{{ post.author }}</span>
                <span class="post-time">{{ post.time }}</span>
              </div>
            </div>
            
            <div class="post-content">
              <p>{{ post.content }}</p>
            </div>
            
            <div class="interaction-bar">
              <button class="action-btn" :class="{ active: post.isLiked }" @click="toggleLike(post)">
                <span class="icon">{{ post.isLiked ? '❤️' : '🤍' }}</span> 
                {{ post.likeCount > 0 ? post.likeCount : '点赞' }}
              </button>
              <button class="action-btn" @click="post.showComments = !post.showComments">
                <span class="icon">💬</span> 
                {{ post.comments.length > 0 ? post.comments.length : '评论' }}
              </button>
              <button class="action-btn"><span class="icon">↗️</span> 分享</button>
            </div>

            <div v-if="post.showComments" class="comments-section">
              <div class="comment-input-box">
                <input type="text" v-model="post.newComment" placeholder="写下你的评论..." @keyup.enter="submitComment(post)" />
                <button @click="submitComment(post)">发送</button>
              </div>
              
              <div class="comment-list">
                <div v-for="(comment, idx) in post.comments" :key="idx" class="comment-item">
                  <span class="comment-author">{{ comment.user }}:</span>
                  <span class="comment-text">{{ comment.text }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      <aside class="side-column">
        <div class="side-card">
          <h3>社区公告</h3>
          <p class="notice-text">欢迎来到互联网/IT交流圈！请友好交流分享面经、技术干货。禁止发布广告及违规内容。</p>
        </div>
        <div class="side-card">
          <h3>🔥 圈内热议</h3>
          <ul class="hot-topics">
            <li># 2026春招第一波面经</li>
            <li># 大模型开发如何入门</li>
            <li># 简历上写精通Vue被怼了</li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const newPostContent = ref('')

// 模拟帖子数据（加入了点赞、评论的状态）
const posts = ref([
  {
    id: 1,
    author: '前端摸鱼侠',
    time: '2小时前',
    content: '今天面试字节跳动，面试官问我 Vue3 的响应式原理和 Proxy，我答得不是很好，感觉凉了。大家有没有好的八股文总结推荐？',
    isLiked: false,
    likeCount: 12,
    showComments: false,
    newComment: '',
    comments: [
      { user: 'Java卷王', text: '别灰心，可以看看《Vue.js设计与实现》这本书。' },
      { user: 'HR小姐姐', text: '一次面试说明不了什么，继续加油！' }
    ]
  },
  {
    id: 2,
    author: '算法工程师',
    time: '5小时前',
    content: '最近在研究 DeepSeek 的大模型微调，感觉门槛越来越低了。写了一篇简单的入门博客，准备下周发出来给大家避坑。',
    isLiked: true,
    likeCount: 45,
    showComments: false,
    newComment: '',
    comments: []
  }
])

// 发布帖子
const publishPost = () => {
  if (!newPostContent.value.trim()) return
  
  posts.value.unshift({
    id: Date.now(),
    author: '我 (当前用户)',
    time: '刚刚',
    content: newPostContent.value,
    isLiked: false,
    likeCount: 0,
    showComments: false,
    newComment: '',
    comments: []
  })
  
  newPostContent.value = '' // 清空输入框
}

// 切换点赞状态
const toggleLike = (post) => {
  if (post.isLiked) {
    post.likeCount--
  } else {
    post.likeCount++
  }
  post.isLiked = !post.isLiked
}

// 提交评论
const submitComment = (post) => {
  if (!post.newComment.trim()) return
  
  post.comments.push({
    user: '我',
    text: post.newComment
  })
  post.newComment = '' // 清空该帖子的评论输入框
}
</script>

<style scoped>
/* ================= 页面整体与背景 ================= */
.community-page {
  min-height: 100vh;
  position: relative;
  background: #f8fafc; /* 底部纯色背景 */
}

/* 背景图片层 - 从上到下逐渐虚化 */
.community-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 70%; /* 只占上半部分70%高度 */
  background: url('/images11/vector-1752213931744-234231b1c08c.avif') no-repeat center top;
  background-size: cover;
  mask: linear-gradient(to bottom, black 0%, black 50%, transparent 100%);
  -webkit-mask: linear-gradient(to bottom, black 0%, black 50%, transparent 100%);
  z-index: 0;
  pointer-events: none;
  opacity: 0.3;
}

/* 为了让文字更清晰，加一个半透明遮罩层（只在上半部分） */
.community-page::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 70%;
  background: linear-gradient(to bottom, 
    rgba(255, 255, 255, 0.2) 0%, 
    rgba(255, 255, 255, 0.1) 70%, 
    transparent 100%);
  z-index: 0;
  pointer-events: none;
}

/* 让所有内容浮在背景层上面 */
.community-page > * {
  position: relative;
  z-index: 2;
}

/* ================= 顶部导航 ================= */
.community-header {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(74, 163, 209, 0.2);
  padding: 16px 20px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-inner { max-width: 1000px; margin: 0 auto; display: flex; align-items: center; gap: 20px; padding: 0 20px; }
.back-btn { background: none; border: none; color: #64748b; cursor: pointer; font-size: 15px; }
.header-inner h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  background: linear-gradient(135deg, #1e4f6b, #2d8b6f);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-left: 16px;
}

/* ================= 布局容器 ================= */
.content-layout { display: flex; max-width: 1000px; margin: 20px auto; gap: 20px; padding: 0 20px; align-items: flex-start; }
.feed-column { flex: 1; display: flex; flex-direction: column; gap: 20px; }

/* ================= 发帖区 ================= */
.publish-box {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 20px;
  display: flex;
  gap: 16px;
  border: 1px solid rgba(74, 163, 209, 0.2);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.03);
}
.user-avatar { width: 40px; height: 40px; background: #e2e8f0; border-radius: 50%; display: flex; justify-content: center; align-items: center; font-size: 20px; flex-shrink: 0; }
.input-area { flex: 1; }
.input-area textarea { width: 100%; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; font-family: inherit; resize: vertical; outline: none; transition: 0.2s; background: #f8fafc; box-sizing: border-box; }
.input-area textarea:focus { border-color: #4a90e2; background: white; }
.publish-actions { display: flex; justify-content: space-between; margin-top: 10px; align-items: center; }
.pic-btn { background: none; border: none; color: #64748b; cursor: pointer; }
.submit-btn { background: #4a90e2; color: white; border: none; padding: 8px 20px; border-radius: 20px; cursor: pointer; font-weight: bold; }
.submit-btn:disabled { background: #94a3b8; cursor: not-allowed; }

/* ================= 帖子卡片 ================= */
.post-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(74, 163, 209, 0.15);
  transition: all 0.3s ease;
}
.post-card:hover {
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(74, 163, 209, 0.3);
  box-shadow: 0 12px 24px rgba(74, 163, 209, 0.1);
}

.post-header { display: flex; align-items: center; gap: 12px; margin-bottom: 15px; }
.author-avatar {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  background: linear-gradient(135deg, #4aa3d1, #2d8b6f);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  box-shadow: 0 6px 12px rgba(74, 163, 209, 0.2);
}
.author-info { display: flex; flex-direction: column; }
.author-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 16px;
  margin-right: 8px;
}
.post-time {
  font-size: 12px;
  color: #64748b;
  background: rgba(100, 116, 139, 0.1);
  padding: 2px 8px;
  border-radius: 30px;
}
.post-content { color: #334155; line-height: 1.6; margin-bottom: 15px; font-size: 15px; }

/* ================= 互动条 ================= */
.interaction-bar {
  display: flex;
  gap: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  padding-top: 16px;
  margin-top: 8px;
}
.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  padding: 6px 12px;
  border-radius: 30px;
}
.action-btn:hover {
  background: rgba(74, 163, 209, 0.1);
  color: #4aa3d1;
}
.action-btn.active {
  color: #ef4444;
  font-weight: 500;
}
.action-btn.active .icon {
  transform: scale(1.1);
}

/* ================= 评论区 ================= */
.comments-section { margin-top: 15px; background: #f8fafc; border-radius: 8px; padding: 15px; animation: slideDown 0.3s ease; }
.comment-input-box { display: flex; gap: 10px; margin-bottom: 15px; }
.comment-input-box input { flex: 1; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 20px; outline: none; }
.comment-input-box button { padding: 8px 15px; background: #4a90e2; color: white; border: none; border-radius: 20px; cursor: pointer; }
.comment-item { padding: 8px 0; border-bottom: 1px dashed #e2e8f0; font-size: 14px; line-height: 1.5; }
.comment-item:last-child { border-bottom: none; }
.comment-author { font-weight: bold; color: #4a90e2; margin-right: 8px; }

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ================= 右侧边栏 ================= */
.side-column {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.side-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 20px;
  border: 1px solid rgba(74, 163, 209, 0.15);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.02);
}
.side-card h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #1e293b;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(74, 163, 209, 0.2);
}
.notice-text {
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}
.hot-topics {
  list-style: none;
  padding: 0;
  margin: 0;
}
.hot-topics li {
  padding: 10px 0;
  color: #1e293b;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}
.hot-topics li:last-child {
  border-bottom: none;
}
.hot-topics li:hover {
  color: #4aa3d1;
  padding-left: 8px;
}
</style>