<script setup>
defineProps({
  dash: { type: Object, required: true },
})
</script>

<template>
  <section class="fade-in">
    <div class="recruitment-section">
      <div class="section-header" style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <h3>🔥 产教融合 · 招聘双选</h3>
        </div>

        <div>
          <button
            @click="dash.fetchRecommendedJobs"
            style="
              background: linear-gradient(135deg, #f97316, #ef4444);
              color: white;
              border: none;
              padding: 8px 16px;
              border-radius: 20px;
              cursor: pointer;
              font-weight: bold;
              box-shadow: 0 4px 10px rgba(249, 115, 22, 0.3);
              transition: all 0.3s;
            "
            onmouseover="this.style.transform='translateY(-2px)'"
            onmouseout="this.style.transform='translateY(0)'"
          >
            ✨ 基于简历 AI 智能匹配
          </button>

          <button
            v-if="dash.isRecommendMode"
            @click="dash.isRecommendMode = false; dash.fetchJobData()"
            style="
              margin-left: 10px;
              background: white;
              color: #64748b;
              border: 1px solid #e2e8f0;
              padding: 8px 16px;
              border-radius: 20px;
              cursor: pointer;
              transition: all 0.3s;
            "
            onmouseover="this.style.color='#4aa3d1'; this.style.borderColor='#4aa3d1'"
            onmouseout="this.style.color='#64748b'; this.style.borderColor='#e2e8f0'"
          >
            返回全部岗位
          </button>
        </div>
      </div>

      <div class="category-tabs">
        <div
          v-for="cat in dash.jobCategories"
          :key="cat.name"
          class="category-tab"
          :class="{ active: dash.selectedCategory === cat.name }"
          @click="dash.selectedCategory = cat.name; dash.selectedSubCategory = cat.subCategories[0].name; dash.selectedJob = '全部'"
        >
          <span class="cat-icon">{{ cat.icon }}</span>
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </div>

      <div class="subcategory-tabs" v-if="dash.selectedCategory">
        <div
          v-for="sub in dash.jobCategories.find((c) => c.name === dash.selectedCategory)?.subCategories"
          :key="sub.name"
          class="subcategory-tab"
          :class="{ active: dash.selectedSubCategory === sub.name }"
          @click="dash.selectedSubCategory = sub.name; dash.selectedJob = '全部'"
        >
          {{ sub.name }}
        </div>
      </div>

      <div class="job-tags" v-if="dash.selectedSubCategory">
        <span class="job-tag" :class="{ active: dash.selectedJob === '全部' }" @click="dash.selectedJob = '全部'">全部</span>
        <span
          v-for="job in dash.currentJobsList"
          :key="job.name"
          class="job-tag"
          :class="{ active: dash.selectedJob === job.name, hot: job.hot }"
          @click="dash.selectedJob = job.name"
        >
          {{ job.name }}
          <span class="job-count">{{ job.count }}</span>
          <span v-if="job.hot" class="hot-badge">🔥</span>
        </span>
      </div>

      <div class="job-cards">
        <div v-if="dash.loadingJobs" class="loading-state" style="grid-column: 1 / -1; min-height: 200px">
          <div class="spinner"></div>
          <p>正在努力从数据库拉取最新岗位...</p>
        </div>

        <template v-else-if="dash.filteredJobCards.length > 0">
          <div v-for="job in dash.filteredJobCards" :key="job.id" class="job-card" @click="dash.goToJobDetail(job)">
            <div class="job-card-header">
              <div class="company-logo">{{ job.company.charAt(0) }}</div>
              <div class="job-info">
                <div class="job-title-row">
                  <h4>{{ job.position }}</h4>
                  <span v-if="job.hot" class="hot-tag">热门</span>
                </div>
                <div class="company-name">{{ job.company }}</div>
              </div>
              <span class="job-salary">{{ job.salary }}</span>
            </div>

            <div class="job-tags-list">
              <span v-for="tag in job.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>

            <div class="job-footer">
              <span class="job-location">📍 {{ job.location }}</span>
              <span class="job-deadline">⏰ {{ job.deadline }}</span>
              <span class="job-arrow">查看详情 →</span>
            </div>
          </div>
        </template>

        <div v-else class="empty-state" style="grid-column: 1 / -1">
          <div class="empty-icon">📭</div>
          <p>当前分类下暂无相关职位</p>
        </div>
      </div>
    </div>
  </section>
</template>
