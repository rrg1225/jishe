<script setup>
defineProps({
  dash: { type: Object, required: true },
})
</script>

<template>
  <section class="fade-in search-layout">
    <div class="section-title-bar">
      <div class="title-left">
        <h3>🔍 全局搜索结果</h3>
        <p class="search-keyword-display">为您检索包含 <span class="highlight">"{{ dash.searchKeyword }}"</span> 的内容</p>
      </div>
      <span class="more-link" @click="dash.activeMenu = 'home'">返回首页 ></span>
    </div>

    <div class="loading-state" v-if="dash.isSearching">
      <div class="spinner"></div>
      <p>正在全网为您检索中...</p>
    </div>

    <div v-else-if="dash.searchResults.length === 0" class="empty-state">
      <div class="empty-icon">📭</div>
      <p>抱歉，未找到与 "{{ dash.searchKeyword }}" 相关的结果</p>
      <button @click="dash.activeMenu = 'home'" class="back-home-btn">返回推荐页面</button>
    </div>

    <div class="card-grid" v-else>
      <div class="card" v-for="(item, i) in dash.searchResults" :key="i" @click="dash.handleCardClick(item)">
        <div
          class="card-cover"
          :style="
            item.coverImg
              ? {
                  backgroundImage: `url(${item.coverImg})`,
                  backgroundSize: 'cover',
                  backgroundPosition: 'center',
                }
              : { background: item.color || 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)' }
          "
        >
          <span class="type-badge" :class="item.type">{{ item.typeText }}</span>
          <div v-if="item.type === 'video'" class="play-overlay">▶</div>
        </div>
        <div class="card-info">
          <h4 class="card-title">{{ item.title }}</h4>
          <p class="card-desc">{{ item.author || '平台官方发布' }}</p>
        </div>
      </div>
    </div>
  </section>
</template>
