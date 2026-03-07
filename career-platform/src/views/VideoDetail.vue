<template>
  <div class="bilibili-layout">
    <header class="top-nav">
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
            poster="https://images.unsplash.com/photo-1517694712202-14dd9538aa97?ixlib=rb-1.2.1&auto=format&fit=crop&w=1200&q=80"
          >
            <source src="https://www.w3schools.com/html/mov_bbb.mp4" type="video/mp4">
            您的浏览器不支持 HTML5 视频播放。
          </video>
        </div>

        <div class="video-toolbar">
          <div class="action-btn"><span class="emoji">👍</span> 点赞 5.2万</div>
          <div class="action-btn"><span class="emoji">🪙</span> 投币 4.1万</div>
          <div class="action-btn"><span class="emoji">⭐</span> 收藏 8.8万</div>
          <div class="action-btn"><span class="emoji">↪️</span> 分享</div>
        </div>

        <div class="video-desc">
          这是一期面向新手的全栈开发保姆级教程。手把手教你解决跨域、JWT 鉴权、文件上传等核心痛点...
          <br> 如果觉得有用，请务必给个一键三连支持一下！
        </div>

        <div class="comment-section">
          <h3>评论 <span class="comment-count">1024</span></h3>
          <div class="comment-input-box">
            <img class="my-avatar" src="https://api.dicebear.com/7.x/adventurer/svg?seed=me" alt="">
            <textarea placeholder="发一条友善的评论吧..."></textarea>
            <button class="send-btn">发表评论</button>
          </div>
          <div class="comment-list">
            <div class="comment-item" v-for="i in 5" :key="i">
              <div class="c-avatar">👦</div>
              <div class="c-content">
                <div class="c-name">热心网友_{{ i }}</div>
                <div class="c-text">太硬核了！正好毕设要做这个，简直是救命稻草！</div>
                <div class="c-meta">昨天 18:00 | 回复</div>
              </div>
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

const route = useRoute()
const router = useRouter()

// 定义响应式变量，用来存放接收到的标题和作者
const videoTitle = ref('')
const videoAuthor = ref('')

onMounted(() => {
  // 从路由的 query 参数中提取数据，如果没有就用默认值兜底
  videoTitle.value = route.query.title || '【默认】如何用 Vue3 + SpringBoot 搭建全栈项目？'
  videoAuthor.value = route.query.author || '全栈小导师 ⚡'
})
</script>

<style scoped>
/* 全局底色 */
.bilibili-layout { background-color: #f4f5f7; min-height: 100vh; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }

/* 顶部导航 */
.top-nav { height: 60px; background: #fff; box-shadow: 0 2px 4px rgba(0,0,0,0.08); display: flex; justify-content: space-between; align-items: center; padding: 0 40px; position: sticky; top: 0; z-index: 100; }
.nav-logo { font-size: 20px; font-weight: bold; color: #00aeec; }
.search-box input { width: 300px; padding: 10px 15px; border: 1px solid #e5e9ef; border-radius: 6px; background: #f4f5f7; outline: none; }
.search-box input:focus { border-color: #00aeec; }
.user-avatar img { width: 40px; height: 40px; border-radius: 50%; }

/* 主体内容区布局 (经典左右结构) */
.main-container { max-width: 1400px; margin: 0 auto; padding: 20px; display: flex; gap: 30px; }
.left-col { flex: 1; min-width: 0; }
.right-col { width: 350px; flex-shrink: 0; }

/* 视频头部 */
.video-header { margin-bottom: 20px; }
.video-title { font-size: 22px; font-weight: 500; color: #18191c; margin: 0 0 10px 0; line-height: 1.4; }
.video-meta { font-size: 13px; color: #9499a0; display: flex; gap: 15px; }

/* 播放器区域 */
.player-wrapper { width: 100%; aspect-ratio: 16 / 9; background: #000; border-radius: 6px; overflow: hidden; margin-bottom: 15px; }
.b-player { width: 100%; height: 100%; object-fit: contain; }

/* 视频操作条 */
.video-toolbar { display: flex; gap: 30px; padding: 15px 0; border-bottom: 1px solid #e3e5e7; margin-bottom: 20px; }
.action-btn { display: flex; align-items: center; gap: 8px; color: #61666d; cursor: pointer; font-size: 14px; font-weight: 500; transition: color 0.3s; }
.action-btn:hover { color: #00aeec; }
.emoji { font-size: 20px; }

/* 视频简介 */
.video-desc { font-size: 14px; color: #18191c; line-height: 1.6; margin-bottom: 40px; padding-bottom: 20px; border-bottom: 1px solid #e3e5e7; }

/* 右侧 UP主卡片 */
.up-card { display: flex; align-items: center; gap: 12px; padding: 0 0 20px 0; margin-bottom: 20px; border-bottom: 1px solid #e3e5e7; }
.up-avatar { width: 48px; height: 48px; border-radius: 50%; }
.up-info { flex: 1; }
.up-name { font-size: 15px; font-weight: 500; color: #fb7299; margin-bottom: 4px; }
.up-desc { font-size: 12px; color: #9499a0; }
.follow-btn { background: #00aeec; color: #fff; border: none; padding: 6px 18px; border-radius: 4px; cursor: pointer; font-size: 14px; transition: 0.3s; }
.follow-btn:hover { background: #00a1d6; }

/* 弹幕列表模拟 */
.danmaku-box { background: #f1f2f3; border-radius: 6px; padding: 15px; margin-bottom: 25px; height: 200px; overflow-y: auto; }
.box-title { font-size: 14px; font-weight: bold; margin-bottom: 10px; color: #18191c; }
.d-item { font-size: 12px; color: #61666d; margin-bottom: 8px; }

/* 相关推荐 */
.rec-title { font-size: 16px; font-weight: 500; margin-bottom: 15px; color: #18191c; }
.rec-item { display: flex; gap: 10px; margin-bottom: 15px; cursor: pointer; }
.rec-cover { position: relative; width: 140px; height: 80px; border-radius: 6px; overflow: hidden; flex-shrink: 0; }
.rec-cover img { width: 100%; height: 100%; object-fit: cover; }
.duration { position: absolute; bottom: 4px; right: 4px; background: rgba(0,0,0,0.5); color: #fff; font-size: 12px; padding: 2px 4px; border-radius: 2px; }
.rec-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.r-title { font-size: 14px; color: #18191c; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.4; transition: color 0.3s; }
.rec-item:hover .r-title { color: #00aeec; }
.r-up, .r-view { font-size: 12px; color: #9499a0; }

/* 评论区简易样式 */
.comment-section h3 { font-size: 18px; margin-bottom: 20px; }
.comment-count { font-size: 14px; color: #9499a0; margin-left: 10px; }
.comment-input-box { display: flex; gap: 15px; margin-bottom: 30px; }
.my-avatar { width: 48px; height: 48px; border-radius: 50%; }
.comment-input-box textarea { flex: 1; height: 60px; padding: 10px; border: 1px solid #e3e5e7; border-radius: 6px; background: #f1f2f3; outline: none; transition: 0.3s; resize: none; font-family: inherit; }
.comment-input-box textarea:focus { border-color: #00aeec; background: #fff; }
.send-btn { width: 80px; background: #00aeec; color: #fff; border: none; border-radius: 6px; cursor: pointer; transition: 0.3s; }
.send-btn:hover { background: #00a1d6; }

.comment-item { display: flex; gap: 15px; margin-bottom: 20px; border-bottom: 1px solid #f1f2f3; padding-bottom: 20px; }
.c-avatar { width: 40px; height: 40px; background: #e3e5e7; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.c-name { font-size: 13px; color: #fb7299; font-weight: 500; margin-bottom: 8px; }
.c-text { font-size: 15px; color: #18191c; line-height: 1.5; margin-bottom: 10px; }
.c-meta { font-size: 12px; color: #9499a0; }
</style>