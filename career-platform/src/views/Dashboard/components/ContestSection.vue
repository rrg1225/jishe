<script setup>
defineProps({
  dash: { type: Object, required: true },
})
</script>

<template>
  <section class="fade-in">
    <div class="contest-section">
      <div class="contest-header">
        <h3>🏆 竞赛信息大厅</h3>
        <p class="section-subtitle">打标签分类，快速匹配适合你的比赛</p>

        <div class="contest-tabs">
          <div class="contest-tab" :class="{ active: dash.contestActiveTab === 'info' }" @click="dash.contestActiveTab = 'info'">
            📊 竞赛基本信息
          </div>
          <div class="contest-tab" :class="{ active: dash.contestActiveTab === 'guide' }" @click="dash.contestActiveTab = 'guide'">
            📝 竞赛指导
          </div>
        </div>
      </div>

      <div v-show="dash.contestActiveTab === 'info'" class="fade-in">
        <div class="contest-search">
          <div class="search-box">
            <span class="search-icon">🔍</span>
            <input type="text" placeholder="搜索竞赛名称、关键词..." v-model="dash.contestKeyword" @keyup.enter="dash.handleSearch" />
            <button class="search-btn" @click="dash.handleSearch">搜索</button>
          </div>
        </div>

        <div class="major-tabs">
          <div
            v-for="major in dash.majorCategories"
            :key="major.name"
            class="major-tab"
            :class="{ active: dash.selectedMajor === major.name }"
            @click="dash.selectedMajor = major.name; dash.selectedDirection = major.directions[0]?.name; dash.selectedSkill = '全部'"
          >
            <span class="major-icon">{{ major.icon }}</span>
            <span class="major-name">{{ major.name }}</span>
          </div>
        </div>

        <div class="direction-tabs" v-if="dash.selectedMajor">
          <div
            v-for="direction in dash.currentDirections"
            :key="direction.name"
            class="direction-tab"
            :class="{ active: dash.selectedDirection === direction.name }"
            @click="dash.selectedDirection = direction.name; dash.selectedSkill = '全部'"
          >
            {{ direction.name }}
          </div>
        </div>

        <div class="skill-tags" v-if="dash.selectedDirection">
          <span class="skill-tag" :class="{ active: dash.selectedSkill === '全部' }" @click="dash.selectedSkill = '全部'">全部</span>
          <span
            v-for="skill in dash.currentSkills"
            :key="skill.name"
            class="skill-tag"
            :class="{ active: dash.selectedSkill === skill.name }"
            @click="dash.selectedSkill = skill.name"
          >
            {{ skill.name }}
            <span class="skill-count">{{ skill.count }}</span>
          </span>
        </div>

        <div class="result-info" v-if="dash.selectedSkill">
          <span class="result-badge">{{ dash.selectedSkill === '全部' ? dash.selectedDirection : dash.selectedSkill }}</span>
          <span class="result-count">共 {{ dash.filteredContests.length }} 个赛事</span>
        </div>

        <div class="contest-grid" v-if="dash.filteredContests.length > 0">
          <div v-for="contest in dash.filteredContests" :key="contest.id" class="contest-card" @click="dash.goToContestDetail(contest)">
            <div class="card-header">
              <span class="contest-level">{{ contest.level }}</span>
              <span class="contest-status" :style="{ backgroundColor: contest.statusColor + '20', color: contest.statusColor }">{{ contest.status }}</span>
            </div>
            <h4 class="contest-title">{{ contest.name }}</h4>
            <div class="contest-organizer">{{ contest.organize }}</div>
            <div class="contest-time">
              <span class="time-icon">📅</span>
              {{ dash.formatDate(contest.startDate) }} - {{ dash.formatDate(contest.endDate) }}
              <span v-if="contest.status === '报名中'" class="time-remain">距截止 {{ dash.getRemainingDays(contest.endDate) }}天</span>
            </div>
            <div class="contest-tags">
              <span v-for="tag in contest.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
            <div class="skills-required">
              <div class="skills-title">✨ 需要掌握的技能：</div>
              <div class="skill-items">
                <span v-for="skill in contest.skills" :key="skill" class="skill-item">{{ skill }}</span>
              </div>
            </div>
            <div class="card-footer"><span class="card-arrow">→</span></div>
          </div>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">🏆</div>
          <p>暂无相关赛事</p>
        </div>
      </div>

      <div v-show="dash.contestActiveTab === 'guide'" class="fade-in">
        <div class="guide-filter-container">
          <div class="type-selector">
            <span :class="{ active: dash.guideFilterType === 'all' }" @click="dash.guideFilterType = 'all'">全部</span>
            <span :class="{ active: dash.guideFilterType === 'video' }" @click="dash.guideFilterType = 'video'">视频</span>
            <span :class="{ active: dash.guideFilterType === 'article' }" @click="dash.guideFilterType = 'article'">文章</span>
          </div>
        </div>

        <div v-if="dash.loadingArticles" class="empty-state">
          <div class="spinner"></div>
          <p>正在努力从数据库加载数据中...</p>
        </div>
        <div v-else-if="dash.filteredContestArticles.length === 0" class="empty-state">
          <div class="empty-icon">📄</div>
          <p>暂无相关指导内容</p>
        </div>
        <div v-else class="article-grid">
          <div v-for="article in dash.filteredContestArticles" :key="article.id" class="article-card" @click="dash.goToArticleDetail(article)">
            <div
              class="article-cover"
              :style="
                article.imageUrls
                  ? {
                      backgroundImage: `url(${article.imageUrls})`,
                      backgroundSize: 'cover',
                      backgroundPosition: 'center',
                    }
                  : { background: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)' }
              "
            >
              <span v-if="article.type === 'video'" class="type-badge video card-corner-badge">视频</span>
              <span v-else class="type-badge article card-corner-badge">文章</span>
              <div v-if="article.type === 'video'" class="play-overlay">▶</div>
            </div>

            <div class="article-info">
              <span class="article-topic">{{ article.topic }}</span>
              <h4 class="article-title">{{ article.title }}</h4>
              <p class="article-desc">{{ dash.getSnippet(article.content) }}</p>
              <div class="article-footer">
                <span class="article-date">{{ dash.formatDateStr(article.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
