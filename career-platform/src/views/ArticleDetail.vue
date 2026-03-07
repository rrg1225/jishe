<template>
  <div class="article-layout">
    <header class="top-nav">
      <div class="nav-logo" @click="router.push('/dashboard')">📚 学业规划平台 - 专区</div>
      <div class="back-btn" @click="router.push('/dashboard')">返回主页</div>
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
          <div class="action-item"><span class="icon">👍</span> 赞同 856</div>
          <div class="action-item"><span class="icon">⭐</span> 收藏 128</div>
          <div class="action-item"><span class="icon">💬</span> 评论 45</div>
        </div>
      </main>

      <aside class="right-sidebar">
        <div class="sidebar-card author-card">
          <h3>关于作者</h3>
          <div class="auth-desc">
            资深校招导师，前大厂 HR。专注解决大学生秋招、春招求职难题，分享最硬核的笔试面试经验。
          </div>
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

const route = useRoute()
const router = useRouter()

const articleTitle = ref('加载中...')
const articleAuthor = ref('加载中...')
const articleContent = ref('')

onMounted(() => {
  // 1. 接收主页传过来的标题和作者
  articleTitle.value = route.query.title || '【深度解析】大学生如何做好职业生涯规划？'
  articleAuthor.value = route.query.author || '职场领路人'

  // 2. 模拟从数据库里查出来的“富文本”字符串 (未来这里将替换成 axios.get 请求)
  articleContent.value = `
    <p>这是一段由<strong>富文本编辑器</strong>生成的测试内容。在真实的开发中，你在编辑器里敲下的每一个字、加粗的每一段话、上传的每一张图片，都会被转变成带有 HTML 标签的字符串存进 MySQL 的 <code>LONGTEXT</code> 字段里。</p>
    
    <p>当我们打开这个页面时，Vue 会用 <code>v-html</code> 把这些标签原封不动地渲染成漂亮的网页！</p>
    
    <img src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-1.2.1&auto=format&fit=crop&w=1200&q=80" style="max-width: 100%; border-radius: 8px; margin: 20px 0; box-shadow: 0 4px 12px rgba(0,0,0,0.1);" />
    
    <h3>一、为什么要尽早做规划？</h3>
    <p>很多同学到了大四才开始海投简历，这时候往往会发现自己<strong>缺乏实习经历</strong>、<strong>项目单薄</strong>。提前规划能让你在每一次寒暑假都有的放矢。</p>
    
    <h3>二、我们的建议：</h3>
    <ul>
      <li><strong>大一：</strong>探索兴趣，提升 GPA，多参加社团锻炼软实力。</li>
      <li><strong>大二：</strong>确定方向，参加含金量高的专业竞赛（如蓝桥杯、挑战杯）。</li>
      <li><strong>大三：</strong>疯狂积累项目经验，寻找第一份日常实习。</li>
      <li><strong>大四：</strong>全力冲刺秋招/春招，或者安心准备考研/出国复试。</li>
    </ul>
    
    <p><em>（注：以上内容为系统自动生成的模拟富文本数据排版演示）</em></p>
  `
})
</script>

<style scoped>
/* 全局阅读环境背景 */
.article-layout { min-height: 100vh; background-color: #f6f6f6; font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", Arial, sans-serif; color: #333; }

/* 简易顶部导航 */
.top-nav { height: 60px; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); display: flex; justify-content: space-between; align-items: center; padding: 0 40px; position: sticky; top: 0; z-index: 100; }
.nav-logo { font-size: 18px; font-weight: bold; color: #4a90e2; cursor: pointer; }
.back-btn { font-size: 14px; color: #666; cursor: pointer; transition: 0.2s; }
.back-btn:hover { color: #4a90e2; }

/* 主体内容双栏布局 */
.main-container { max-width: 1100px; margin: 20px auto; display: flex; gap: 20px; align-items: flex-start; padding: 0 20px; }

/* 左侧文章区 */
.article-content-box { flex: 1; background: #fff; border-radius: 8px; padding: 40px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); min-width: 0; }
.article-title { font-size: 28px; font-weight: bold; color: #121212; line-height: 1.4; margin-top: 0; margin-bottom: 25px; }

/* 作者信息栏 */
.article-meta { display: flex; align-items: center; margin-bottom: 30px; }
.author-avatar { width: 48px; height: 48px; border-radius: 50%; background: #f0f0f0; margin-right: 15px; }
.meta-info { flex: 1; }
.author-name { font-size: 16px; font-weight: 600; color: #121212; margin-bottom: 4px; display: flex; align-items: center; gap: 8px; }
.badge { font-size: 12px; background: #e6f0fa; color: #4a90e2; padding: 2px 8px; border-radius: 4px; font-weight: normal; }
.publish-time { font-size: 14px; color: #8590a6; }
.follow-btn { background: #4a90e2; color: #fff; border: none; padding: 8px 20px; border-radius: 20px; font-size: 14px; cursor: pointer; transition: 0.3s; font-weight: 500; }
.follow-btn:hover { background: #357abd; }

.divider { height: 1px; background: #ebebeb; margin-bottom: 30px; }

/* 🌟 富文本内容的全局样式排版控制 */
.rich-text-body { font-size: 16px; line-height: 1.8; color: #333; letter-spacing: 0.5px; }
.rich-text-body :deep(h2), .rich-text-body :deep(h3) { margin-top: 30px; margin-bottom: 15px; color: #121212; }
.rich-text-body :deep(p) { margin-bottom: 20px; }
.rich-text-body :deep(ul), .rich-text-body :deep(ol) { margin-bottom: 20px; padding-left: 20px; }
.rich-text-body :deep(li) { margin-bottom: 8px; }
.rich-text-body :deep(code) { background: #f6f6f6; padding: 2px 6px; border-radius: 4px; color: #c7254e; font-family: monospace; }

/* 底部互动区 */
.article-actions { display: flex; gap: 30px; margin-top: 50px; padding-top: 20px; border-top: 1px solid #ebebeb; }
.action-item { display: flex; align-items: center; gap: 8px; color: #8590a6; cursor: pointer; font-size: 15px; transition: color 0.3s; }
.action-item:hover { color: #4a90e2; }
.icon { font-size: 18px; }

/* 右侧边栏 */
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
.recommend-list li:last-child { margin-bottom: 0; }
.recommend-list li:hover { color: #4a90e2; }
</style>