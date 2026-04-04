<script setup>
defineProps({
  dash: { type: Object, required: true },
})
</script>

<template>
  <section class="fade-in matrix-layout">
    <div class="matrix-hero">
      <div class="piano-keys">
        <div class="piano-key" v-for="i in 12" :key="i" :style="{ animationDelay: i * 0.1 + 's' }"></div>
      </div>
      <div class="hero-content">
        <div class="hero-icon">📊</div>
        <h2 class="hero-title">大数据院校筛选实时看板</h2>
        <p class="hero-desc">全网实时抓取考研/保研核心数据，精准匹配目标院校</p>
      </div>
    </div>

    <div class="big-data-board">
      <div class="board-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px">
        <div class="title-area" style="display: flex; align-items: center; gap: 12px">
          <div class="title-badge">实时更新 🔥</div>
          <span style="font-size: 14px; color: #64748b; font-weight: 500">全网院校数据快报</span>
        </div>

        <div class="school-search-box">
          <select v-model="dash.selectedSchoolMajor" class="major-select">
            <option value="全部">所有专业</option>
            <option value="计算机科学与技术">计算机科学与技术</option>
            <option value="软件工程">软件工程</option>
            <option value="人工智能">人工智能</option>
            <option value="电子信息">电子信息</option>
            <option value="临床医学">临床医学</option>
            <option value="信息管理">信息管理</option>
            <option value="土木工程">土木工程</option>
            <option value="能源与动力工程">能源与动力工程</option>
            <option value="环境工程">环境工程</option>
            <option value="化学工程与工艺">化学工程与工艺</option>
          </select>
          <span class="divider-line">|</span>
          <span class="search-icon">🔍</span>
          <input
            type="text"
            v-model="dash.targetSchoolKeyword"
            placeholder="输入院校名称..."
            @keyup.enter="dash.handleSchoolSearch"
          />
          <button class="search-btn" @click="dash.handleSchoolSearch">检索</button>
        </div>
      </div>

      <div class="data-table-container">
        <div class="data-tabs">
          <span :class="{ active: dash.schoolDataType === 'kaoyan' }" @click="dash.schoolDataType = 'kaoyan'">🔥 考研热门情报</span>
          <span :class="{ active: dash.schoolDataType === 'baoyan' }" @click="dash.schoolDataType = 'baoyan'">🏆 保研推免速递</span>
        </div>

        <div class="table-responsive">
          <table class="school-data-table">
            <thead>
              <tr v-if="dash.schoolDataType === 'kaoyan'">
                <th>院校名称</th>
                <th>招生专业</th>
                <th>近三年复试线均值</th>
                <th>近三年复录比均值</th>
                <th>报录比 (激烈程度)</th>
                <th>操作</th>
              </tr>
              <tr v-if="dash.schoolDataType === 'baoyan'">
                <th>院校名称</th>
                <th>招生专业</th>
                <th>夏令营/预推免节点</th>
                <th>成绩排位要求</th>
                <th>四六级要求</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(school, index) in dash.currentSchoolData" :key="index">
                <td class="school-name">
                  <span class="s-logo">{{ school.name.charAt(0) }}</span>
                  <div class="s-info">
                    <span class="s-title">{{ school.name }}</span>
                    <span class="s-tags" v-if="school.tags">{{ school.tags.join(' | ') }}</span>
                  </div>
                </td>

                <template v-if="dash.schoolDataType === 'kaoyan'">
                  <td class="major-col"><span class="major-tag">{{ school.major || '计算机科学与技术' }}</span></td>
                  <td class="score-trend">{{ school.scoreLine }}</td>
                  <td class="time-node">{{ school.reExamRatio }}</td>
                  <td>
                    <span class="intensity-badge" :class="dash.getIntensityClass(school.intensityLevel)">
                      {{ school.intensityLevel }} ({{ school.applyRatio }})
                    </span>
                  </td>
                  <td>
                    <a
                      v-if="school.officialUrl"
                      :href="school.officialUrl"
                      target="_blank"
                      class="detail-btn"
                      style="text-decoration: none; display: inline-block; text-align: center"
                      >查看详情</a
                    >
                    <button v-else class="detail-btn" @click="dash.viewSchoolDetail(school)">查看详情</button>
                  </td>
                </template>

                <template v-if="dash.schoolDataType === 'baoyan'">
                  <td class="major-col"><span class="major-tag">{{ school.major || '计算机科学与技术' }}</span></td>
                  <td class="time-node">{{ school.timeline }}</td>
                  <td><span class="highlight-text">{{ school.rankReq }}</span></td>
                  <td>{{ school.englishReq }}</td>
                  <td>
                    <a
                      :href="school.officialUrl"
                      target="_blank"
                      class="detail-btn"
                      style="text-decoration: none; display: inline-block; text-align: center"
                      >查看详情</a
                    >
                  </td>
                </template>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="home-sections" style="margin-top: 20px">
      <div class="section-block" v-for="(section, index) in dash.matrixSections" :key="index">
        <div class="section-header-optimized">
          <div class="header-left">
            <div class="section-icon" :class="dash.getSectionIconClass(section.dbCategory)">
              {{ dash.getSectionIcon(section.dbCategory) }}
            </div>
            <h3 class="section-title" style="margin-right: 12px">{{ section.title }}</h3>

            <div class="type-selector-optimized">
              <span :class="{ active: section.filterType === 'all' }" @click="section.filterType = 'all'">全部</span>
              <span :class="{ active: section.filterType === 'video' }" @click="section.filterType = 'video'">📹 视频</span>
              <span :class="{ active: section.filterType === 'article' }" @click="section.filterType = 'article'">📄 文章</span>
            </div>
          </div>

          <div class="header-right">
            <span class="more-link" @click="dash.goToListPage(section.dbCategory)"> 查看全部 <span class="arrow">›</span> </span>
          </div>
        </div>

        <div class="card-grid-optimized">
          <div
            class="card-optimized"
            v-for="(item, i) in dash.filteredMatrixItems(section)"
            :key="i"
            @click="dash.handleCardClick(item)"
          >
            <div
              class="card-cover-optimized"
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
              <span class="type-badge-optimized" :class="item.type">{{ item.typeText }}</span>
              <div v-if="item.type === 'video'" class="play-icon">▶</div>
            </div>
            <div class="card-info-optimized">
              <h4 class="card-title-optimized">{{ item.title }}</h4>
              <div class="card-meta">
                <span class="card-author">{{ item.author || '平台官方发布' }}</span>
                <span class="card-date" v-if="item.createTime">{{ dash.formatDateShort(item.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
