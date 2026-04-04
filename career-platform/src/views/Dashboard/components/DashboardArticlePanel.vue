<script setup>
defineProps({
  dash: { type: Object, required: true },
})
</script>

<template>
  <section class="fade-in">
    <div class="article-layout">
      <header class="article-header">
        <div class="back-btn" @click="dash.goBackToContest">
          <span class="back-icon">❮</span> 返回上一页
        </div>
        <div class="header-actions">
          <button class="action-btn">⭐ 收藏</button>
          <button class="action-btn">🔗 分享</button>
        </div>
      </header>

      <div class="loading-state" v-if="dash.articleLoading">
        <div class="spinner"></div>
        <p>正在努力加载文章内容...</p>
      </div>

      <main class="article-main" v-if="!dash.articleLoading && dash.currentArticleData">
        <article class="article-container">
          <div class="article-title-area">
            <span class="topic-badge">{{ dash.currentArticleData.topic }}</span>
            <h1 class="title">{{ dash.currentArticleData.title }}</h1>
            <div class="meta-info">
              <span class="author">来源：{{ dash.currentArticleData.content || '竞赛信息大厅' }}</span>
              <span class="dot">·</span>
              <span class="time">{{ dash.formatDateStr(dash.currentArticleData.createTime) }}</span>
              <span class="dot">·</span>
              <span class="views">👁️ {{ Math.floor(Math.random() * 500) + 100 }} 次阅读</span>
            </div>
          </div>

          <div class="article-content">
            <div class="hero-image" v-if="dash.parsedImages.length > 0">
              <img :src="dash.parsedImages[0]" alt="文章配图" />
            </div>

            <div class="text-body">
              {{ dash.currentArticleData.content || '点击原链接进行访问...' }}
            </div>

            <div class="image-gallery" v-if="dash.parsedImages.length > 1">
              <h3 class="gallery-title">🔗 相关配图与资料</h3>
              <div class="gallery-grid">
                <img
                  v-for="(imgUrl, index) in dash.parsedImages.slice(1)"
                  :key="index"
                  :src="imgUrl"
                  alt="补充资料图"
                  class="gallery-img"
                  @click="dash.previewImage(imgUrl)"
                />
              </div>
              <p class="gallery-tip">点击图片可查看大图</p>
            </div>
          </div>
        </article>

        <aside class="article-sidebar">
          <div class="sidebar-box">
            <div class="box-title">🔥 相关推荐</div>
            <ul class="recommend-list">
              <li v-for="i in 4" :key="i" class="recommend-item" @click="dash.activeMenu = 'contest'">
                <div class="rec-dot"></div>
                <div class="rec-text">
                  <p class="rec-title">{{ dash.currentArticleData.topic }} 核心指南 {{ i }}</p>
                </div>
              </li>
            </ul>
          </div>
        </aside>
      </main>

      <div class="error-state" v-if="!dash.articleLoading && !dash.currentArticleData">
        <div class="error-icon">📄</div>
        <p>抱歉，未找到该内容</p>
        <button @click="dash.goBackToContest" class="back-home-btn">返回列表</button>
      </div>

      <div class="image-preview-overlay" v-if="dash.previewUrl" @click="dash.previewUrl = null">
        <img :src="dash.previewUrl" alt="全屏预览" @click.stop />
        <span class="close-preview">✖</span>
      </div>
    </div>
  </section>
</template>
