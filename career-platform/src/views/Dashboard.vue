<template>
  <div class="layout">
    <header class="header">
      <div class="logo">学业职业规划平台</div>

      <div class="search-container">
        <div class="search-bar">
          <span class="search-icon">🔍</span>
          <input 
            type="text" 
            placeholder="搜索院校、考研或竞赛..." 
            v-model="searchKeyword"
            @focus="showHotSearch = true"
            @blur="hideHotSearch"
            @keyup.enter="performSearch(searchKeyword)"
          />
        </div>
        
        <div class="hot-search-dropdown" v-show="showHotSearch">
          <div class="hot-search-header">
            <span class="hot-title">🔥 热搜榜</span>
            <span class="update-time">实时更新</span>
          </div>
          
          <div class="hot-search-list">
            <div 
              v-for="(item, index) in hotSearchList" 
              :key="index"
              class="hot-search-item"
              @mousedown.prevent="selectHotSearch(item)"
            >
              <span class="rank-number" :class="{ 'top-three': index < 3 }">{{ index + 1 }}</span>
              <span class="hot-icon" v-if="index < 3">🔥</span>
              <span class="hot-text">{{ item.keyword }}</span>
              <span class="hot-count" :class="{ 'top-three-count': index < 3 }">{{ item.count }}万</span>
            </div>
          </div>
          
          <div class="hot-search-footer" @click="performSearch(searchKeyword || '竞赛')">
            <span>查看更多结果 →</span>
          </div>
        </div>
      </div>
      
      <div class="header-right">
        <button class="go-publish-btn" @click="router.push('/publish')">
          <span class="icon">✍️</span> 发布内容
        </button>

        <button 
          v-if="userRole === 'enterprise'" 
          class="post-offer-btn" 
          @click="router.push('/post-offer')"
        >
          <span class="icon">📋</span> 发布 岗位
        </button>

        <div class="user-profile" @click="toggleDropdown">
          <span class="role-badge">{{ userRole === 'enterprise' ? '企业 HR' : '学生' }}</span>
          
          <div class="avatar">
            <img :src="userAvatar" alt="个人头像" />
          </div>

          <div class="dropdown-overlay" v-show="isDropdownOpen" @click.stop="closeDropdown"></div>

          <div class="dropdown-menu" v-show="isDropdownOpen" @click.stop>
            <div class="dropdown-header">
              <div class="username">{{ userName }}</div>
              <div class="user-account">{{ userAccount }}</div>
              <div class="user-org" v-if="userOrg">{{ userOrg }}</div>
            </div>
            <div class="divider"></div>
            <ul class="dropdown-list">
              <li class="dropdown-item" @click.stop="goToProfile">
                <i>👤</i> 个人信息
              </li>
              <li class="dropdown-item" @click.stop="logout">
                <i>🚪</i> 退出登录
              </li>
            </ul>
          </div>
        </div>
      </div>
    </header>

    <div class="main-container">
      <aside class="sidebar">
        <ul>
          <li :class="{ active: activeMenu === 'home' }" @click="activeMenu = 'home'">首页 (推荐)</li>
          <li :class="{ active: activeMenu === 'matrix' }" @click="activeMenu = 'matrix'">学业深造矩阵</li>
          <li :class="{ active: activeMenu === 'job' }" @click="activeMenu = 'job'">产教融合双选</li>
          <li :class="{ active: activeMenu === 'contest' }" @click="activeMenu = 'contest'">竞赛信息大厅</li>
          <li :class="{ active: activeMenu === 'profile' }" @click="activeMenu = 'profile'">能力画像定制</li>
          <li :class="{ active: activeMenu === 'chat' }" @click="activeMenu = 'chat'">💬 私信交流</li>
        </ul>
        <div class="community-entry">
          <button class="industry-glow-btn" @click="router.push('/community-select')">
            🌐 进入社区
          </button>
        </div>
      </aside>

      <main class="content">
        <section v-if="activeMenu === 'search'" class="fade-in search-layout">
          <div class="section-title-bar">
            <div class="title-left">
              <h3>🔍 全局搜索结果</h3>
              <p class="search-keyword-display">为您检索包含 <span class="highlight">"{{ searchKeyword }}"</span> 的内容</p>
            </div>
            <span class="more-link" @click="activeMenu = 'home'">返回首页 ></span>
          </div>

          <div class="loading-state" v-if="isSearching">
            <div class="spinner"></div>
            <p>正在全网为您检索中...</p>
          </div>

          <div v-else-if="searchResults.length === 0" class="empty-state">
            <div class="empty-icon">📭</div>
            <p>抱歉，未找到与 "{{ searchKeyword }}" 相关的结果</p>
            <button @click="activeMenu = 'home'" class="back-home-btn">返回推荐页面</button>
          </div>

          <div class="card-grid" v-else>
            <div class="card" v-for="(item, i) in searchResults" :key="i" @click="handleCardClick(item)">
              <div class="card-cover" :style="item.coverImg ? { backgroundImage: `url(${item.coverImg})`, backgroundSize: 'cover', backgroundPosition: 'center' } : { background: item.color || 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)' }">
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

        <section v-if="activeMenu === 'home'" class="fade-in">
          <div class="carousel-container">
            <div class="carousel-wrapper" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
              <div v-for="(slide, index) in carouselSlides" :key="index" class="carousel-slide">
                <img :src="slide.image" :alt="slide.title" class="carousel-image" />
                <div class="carousel-content">
                  <h2>{{ slide.title }}</h2>
                  <p>{{ slide.description }}</p>
                  <button class="carousel-btn" @click="goToLink(slide.link)">了解更多</button>
                </div>
              </div>
            </div>
            
            <div class="carousel-dots">
              <span 
                v-for="(slide, index) in carouselSlides" 
                :key="index"
                class="dot"
                :class="{ active: currentSlide === index }"
                @click="currentSlide = index"
              ></span>
            </div>
            
            <button class="carousel-arrow prev" @click="prevSlide">❮</button>
            <button class="carousel-arrow next" @click="nextSlide">❯</button>
          </div>
          
          <div class="home-sections">
            <div class="section-block" v-for="(section, index) in homeData" :key="index">
              <div class="section-title-bar">
                <h3>{{ section.title }}</h3>
                <span class="more-link" @click="activeMenu = section.target">查看更多 ></span>
              </div>
              <div class="card-grid">
                <div class="card" v-for="(item, i) in section.items" :key="i" @click="handleCardClick(item)">
                  <div class="card-cover" :style="item.coverImg ? { backgroundImage: `url(${item.coverImg})`, backgroundSize: 'cover', backgroundPosition: 'center' } : { background: item.color || 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)' }">
                    <span class="type-badge" :class="item.type">{{ item.typeText }}</span>
                    <div v-if="item.type === 'video'" class="play-overlay">▶</div>
                  </div>
                  <div class="card-info">
                    <h4 class="card-title">{{ item.title }}</h4>
                    <p class="card-desc">{{ item.author || item.desc }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

<section v-if="activeMenu === 'matrix'" class="fade-in matrix-layout">
          
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
          <div class="board-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
            <div class="title-area" style="display: flex; align-items: center; gap: 12px;">
              <div class="title-badge">实时更新 🔥</div>
              <span style="font-size: 14px; color: #64748b; font-weight: 500;">全网院校数据快报</span>
            </div>
            
            <div class="school-search-box">
              <select v-model="selectedSchoolMajor" class="major-select">
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
                v-model="targetSchoolKeyword" 
                placeholder="输入院校名称..." 
                @keyup.enter="handleSchoolSearch"
              />
              <button class="search-btn" @click="handleSchoolSearch">检索</button>
            </div>
          </div>

            <div class="data-table-container">
              <div class="data-tabs">
                <span :class="{ active: schoolDataType === 'kaoyan' }" @click="schoolDataType = 'kaoyan'">🔥 考研热门情报</span>
                <span :class="{ active: schoolDataType === 'baoyan' }" @click="schoolDataType = 'baoyan'">🏆 保研推免速递</span>
              </div>
              
              <div class="table-responsive">
                <table class="school-data-table">
                  <thead>
                    <tr v-if="schoolDataType === 'kaoyan'">
                      <th>院校名称</th>
                      <th>招生专业</th>
                      <th>近三年复试线均值</th>
                      <th>近三年复录比均值</th>
                      <th>报录比 (激烈程度)</th>
                      <th>操作</th>
                    </tr>
                    <tr v-if="schoolDataType === 'baoyan'">
                      <th>院校名称</th>
                      <th>招生专业</th>
                      <th>夏令营/预推免节点</th>
                      <th>成绩排位要求</th>
                      <th>四六级要求</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(school, index) in currentSchoolData" :key="index">
                      <td class="school-name">
                        <span class="s-logo">{{ school.name.charAt(0) }}</span>
                        <div class="s-info">
                          <span class="s-title">{{ school.name }}</span>
                          <span class="s-tags" v-if="school.tags">{{ school.tags.join(' | ') }}</span>
                        </div>
                      </td>
                      
                      <template v-if="schoolDataType === 'kaoyan'">
                        <td class="major-col"><span class="major-tag">{{ school.major || '计算机科学与技术' }}</span></td>
                        <td class="score-trend">{{ school.scoreLine }}</td>
                        <td class="time-node">{{ school.reExamRatio }}</td>
                        <td>
                          <span class="intensity-badge" :class="getIntensityClass(school.intensityLevel)">
                            {{ school.intensityLevel }} ({{ school.applyRatio }})
                          </span>
                        </td>
                        <td>
                          <a v-if="school.officialUrl" :href="school.officialUrl" target="_blank" class="detail-btn" style="text-decoration: none; display: inline-block; text-align: center;">查看详情</a>
                          <button v-else class="detail-btn" @click="viewSchoolDetail(school)">查看详情</button>
                        </td>
                      </template>
                      
                      <template v-if="schoolDataType === 'baoyan'">
                        <td class="major-col"><span class="major-tag">{{ school.major || '计算机科学与技术' }}</span></td>
                        <td class="time-node">{{ school.timeline }}</td>
                        <td><span class="highlight-text">{{ school.rankReq }}</span></td>
                        <td>{{ school.englishReq }}</td>
                        <td>
                          <a :href="school.officialUrl" target="_blank" class="detail-btn" style="text-decoration: none; display: inline-block; text-align: center;">查看详情</a>
                        </td>
                      </template>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div class="home-sections" style="margin-top: 20px;">
            <div class="section-block" v-for="(section, index) in matrixSections" :key="index">
              <div class="section-header-optimized">
                
                <div class="header-left">
                  <div class="section-icon" :class="getSectionIconClass(section.dbCategory)">
                    {{ getSectionIcon(section.dbCategory) }}
                  </div>
                  <h3 class="section-title" style="margin-right: 12px;">{{ section.title }}</h3>
                  
                  <div class="type-selector-optimized">
                    <span :class="{ active: section.filterType === 'all' }" @click="section.filterType = 'all'">全部</span>
                    <span :class="{ active: section.filterType === 'video' }" @click="section.filterType = 'video'">📹 视频</span>
                    <span :class="{ active: section.filterType === 'article' }" @click="section.filterType = 'article'">📄 文章</span>
                  </div>
                </div>

                <div class="header-right">
                  <span class="more-link" @click="goToListPage(section.dbCategory)">
                    查看全部 <span class="arrow">›</span>
                  </span>
                </div>
              </div>

              <div class="card-grid-optimized">
                <div class="card-optimized" v-for="(item, i) in filteredMatrixItems(section)" :key="i" @click="handleCardClick(item)">
                  <div class="card-cover-optimized" :style="item.coverImg ? { backgroundImage: `url(${item.coverImg})`, backgroundSize: 'cover', backgroundPosition: 'center' } : { background: item.color || 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)' }">
                    <span class="type-badge-optimized" :class="item.type">{{ item.typeText }}</span>
                    <div v-if="item.type === 'video'" class="play-icon">▶</div>
                  </div>
                  <div class="card-info-optimized">
                    <h4 class="card-title-optimized">{{ item.title }}</h4>
                    <div class="card-meta">
                      <span class="card-author">{{ item.author || '平台官方发布' }}</span>
                      <span class="card-date" v-if="item.createTime">{{ formatDateShort(item.createTime) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeMenu === 'job'" class="fade-in">
          <div class="recruitment-section">
            <div class="section-header">
              <div>
                <h3>🔥 产教融合 · 招聘双选</h3>
              </div>
            </div>
            
            <div class="category-tabs">
              <div 
                v-for="cat in jobCategories" 
                :key="cat.name"
                class="category-tab"
                :class="{ active: selectedCategory === cat.name }"
                @click="selectedCategory = cat.name; selectedSubCategory = cat.subCategories[0].name; selectedJob = '全部'"
              >
                <span class="cat-icon">{{ cat.icon }}</span>
                <span class="cat-name">{{ cat.name }}</span>
              </div>
            </div>
            
            <div class="subcategory-tabs" v-if="selectedCategory">
              <div 
                v-for="sub in jobCategories.find(c => c.name === selectedCategory)?.subCategories" 
                :key="sub.name"
                class="subcategory-tab"
                :class="{ active: selectedSubCategory === sub.name }"
                @click="selectedSubCategory = sub.name; selectedJob = '全部'"
              >
                {{ sub.name }}
              </div>
            </div>
            
            <div class="job-tags" v-if="selectedSubCategory">
              <span class="job-tag" :class="{ active: selectedJob === '全部' }" @click="selectedJob = '全部'">全部</span>
              <span 
                v-for="job in currentJobsList" 
                :key="job.name"
                class="job-tag"
                :class="{ active: selectedJob === job.name, hot: job.hot }"
                @click="selectedJob = job.name"
              >
                {{ job.name }}
                <span class="job-count">{{ job.count }}</span>
                <span v-if="job.hot" class="hot-badge">🔥</span>
              </span>
            </div>

            <div class="job-cards">
              <div v-if="loadingJobs" class="loading-state" style="grid-column: 1 / -1; min-height: 200px;">
                <div class="spinner"></div>
                <p>正在努力从数据库拉取最新岗位...</p>
              </div>

              <template v-else-if="filteredJobCards.length > 0">
                <div v-for="job in filteredJobCards" :key="job.id" class="job-card" @click="goToJobDetail(job)">
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
              
              <div v-else class="empty-state" style="grid-column: 1 / -1;">
                <div class="empty-icon">📭</div>
                <p>当前分类下暂无相关职位</p>
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeMenu === 'contest'" class="fade-in">
          <div class="contest-section">
            <div class="contest-header">
              <h3>🏆 竞赛信息大厅</h3>
              <p class="section-subtitle">打标签分类，快速匹配适合你的比赛</p>
              
              <div class="contest-tabs">
                <div class="contest-tab" :class="{ active: contestActiveTab === 'info' }" @click="contestActiveTab = 'info'">
                  📊 竞赛基本信息
                </div>
                <div class="contest-tab" :class="{ active: contestActiveTab === 'guide' }" @click="contestActiveTab = 'guide'">
                  📝 竞赛指导
                </div>
              </div>
            </div>
            
            <div v-show="contestActiveTab === 'info'" class="fade-in">
              <div class="contest-search">
                <div class="search-box">
                  <span class="search-icon">🔍</span>
                  <input type="text" placeholder="搜索竞赛名称、关键词..." v-model="contestKeyword" @keyup.enter="handleSearch" />
                  <button class="search-btn" @click="handleSearch">搜索</button>
                </div>
              </div>
              
              <div class="major-tabs">
                <div 
                  v-for="major in majorCategories" 
                  :key="major.name"
                  class="major-tab"
                  :class="{ active: selectedMajor === major.name }"
                  @click="selectedMajor = major.name; selectedDirection = major.directions[0]?.name; selectedSkill = '全部'"
                >
                  <span class="major-icon">{{ major.icon }}</span>
                  <span class="major-name">{{ major.name }}</span>
                </div>
              </div>
              
              <div class="direction-tabs" v-if="selectedMajor">
                <div 
                  v-for="direction in currentDirections" 
                  :key="direction.name"
                  class="direction-tab"
                  :class="{ active: selectedDirection === direction.name }"
                  @click="selectedDirection = direction.name; selectedSkill = '全部'"
                >
                  {{ direction.name }}
                </div>
              </div>
              
              <div class="skill-tags" v-if="selectedDirection">
                <span class="skill-tag" :class="{ active: selectedSkill === '全部' }" @click="selectedSkill = '全部'">全部</span>
                <span 
                  v-for="skill in currentSkills" 
                  :key="skill.name"
                  class="skill-tag"
                  :class="{ active: selectedSkill === skill.name }"
                  @click="selectedSkill = skill.name"
                >
                  {{ skill.name }}
                  <span class="skill-count">{{ skill.count }}</span>
                </span>
              </div>
              
              <div class="result-info" v-if="selectedSkill">
                <span class="result-badge">{{ selectedSkill === '全部' ? selectedDirection : selectedSkill }}</span>
                <span class="result-count">共 {{ filteredContests.length }} 个赛事</span>
              </div>
              
              <div class="contest-grid" v-if="filteredContests.length > 0">
                <div v-for="contest in filteredContests" :key="contest.id" class="contest-card" @click="goToContestDetail(contest)">
                  <div class="card-header">
                    <span class="contest-level">{{ contest.level }}</span>
                    <span class="contest-status" :style="{ backgroundColor: contest.statusColor + '20', color: contest.statusColor }">{{ contest.status }}</span>
                  </div>
                  <h4 class="contest-title">{{ contest.name }}</h4>
                  <div class="contest-organizer">{{ contest.organize }}</div>
                  <div class="contest-time">
                    <span class="time-icon">📅</span>
                    {{ formatDate(contest.startDate) }} - {{ formatDate(contest.endDate) }}
                    <span v-if="contest.status === '报名中'" class="time-remain">距截止 {{ getRemainingDays(contest.endDate) }}天</span>
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

            <div v-show="contestActiveTab === 'guide'" class="fade-in">
              <div class="guide-filter-container">
                <div class="type-selector">
                  <span :class="{ active: guideFilterType === 'all' }" @click="guideFilterType = 'all'">全部</span>
                  <span :class="{ active: guideFilterType === 'video' }" @click="guideFilterType = 'video'">视频</span>
                  <span :class="{ active: guideFilterType === 'article' }" @click="guideFilterType = 'article'">文章</span>
                </div>
              </div>

              <div v-if="loadingArticles" class="empty-state">
                <div class="spinner"></div>
                <p>正在努力从数据库加载数据中...</p>
              </div>
              <div v-else-if="filteredContestArticles.length === 0" class="empty-state">
                <div class="empty-icon">📄</div>
                <p>暂无相关指导内容</p>
              </div>
              <div v-else class="article-grid">
                <div v-for="article in filteredContestArticles" :key="article.id" class="article-card" @click="goToArticleDetail(article)">
                  <div class="article-cover" :style="article.imageUrls ? { backgroundImage: `url(${article.imageUrls})`, backgroundSize: 'cover', backgroundPosition: 'center' } : { background: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)' }">
                    <span v-if="article.type === 'video'" class="type-badge video card-corner-badge">视频</span>
                    <span v-else class="type-badge article card-corner-badge">文章</span>
                    <div v-if="article.type === 'video'" class="play-overlay">▶</div>
                  </div>
                  
                  <div class="article-info">
                    <span class="article-topic">{{ article.topic }}</span>
                    <h4 class="article-title">{{ article.title }}</h4>
                    <p class="article-desc">{{ getSnippet(article.content) }}</p>
                    <div class="article-footer">
                      <span class="article-date">{{ formatDateStr(article.createTime) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </section>

        <section v-if="activeMenu === 'articleDetail'" class="fade-in">
          <div class="article-layout">
            <header class="article-header">
              <div class="back-btn" @click="goBackToContest">
                <span class="back-icon">❮</span> 返回上一页
              </div>
              <div class="header-actions">
                <button class="action-btn">⭐ 收藏</button>
                <button class="action-btn">🔗 分享</button>
              </div>
            </header>

            <div class="loading-state" v-if="articleLoading">
              <div class="spinner"></div>
              <p>正在努力加载文章内容...</p>
            </div>

            <main class="article-main" v-if="!articleLoading && currentArticleData">
              <article class="article-container">
                <div class="article-title-area">
                  <span class="topic-badge">{{ currentArticleData.topic }}</span>
                  <h1 class="title">{{ currentArticleData.title }}</h1>
                  <div class="meta-info">
                    <span class="author">来源：{{ currentArticleData.content || '竞赛信息大厅' }}</span>
                    <span class="dot">·</span>
                    <span class="time">{{ formatDateStr(currentArticleData.createTime) }}</span>
                    <span class="dot">·</span>
                    <span class="views">👁️ {{ Math.floor(Math.random() * 500) + 100 }} 次阅读</span>
                  </div>
                </div>

                <div class="article-content">
                  <div class="hero-image" v-if="parsedImages.length > 0">
                    <img :src="parsedImages[0]" alt="文章配图" />
                  </div>

                  <div class="text-body">
                    {{ currentArticleData.content || '点击原链接进行访问...' }}
                  </div>

                  <div class="image-gallery" v-if="parsedImages.length > 1">
                    <h3 class="gallery-title">🔗 相关配图与资料</h3>
                    <div class="gallery-grid">
                      <img 
                        v-for="(imgUrl, index) in parsedImages.slice(1)" 
                        :key="index" 
                        :src="imgUrl" 
                        alt="补充资料图"
                        class="gallery-img"
                        @click="previewImage(imgUrl)"
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
                    <li v-for="i in 4" :key="i" class="recommend-item" @click="activeMenu='contest'">
                      <div class="rec-dot"></div>
                      <div class="rec-text">
                        <p class="rec-title">{{ currentArticleData.topic }} 核心指南 {{ i }}</p>
                      </div>
                    </li>
                  </ul>
                </div>
              </aside>
            </main>

            <div class="error-state" v-if="!articleLoading && !currentArticleData">
              <div class="error-icon">📄</div>
              <p>抱歉，未找到该内容</p>
              <button @click="goBackToContest" class="back-home-btn">返回列表</button>
            </div>

            <div class="image-preview-overlay" v-if="previewUrl" @click="previewUrl = null">
              <img :src="previewUrl" alt="全屏预览" @click.stop />
              <span class="close-preview">✖</span>
            </div>
          </div>
        </section>

        <section v-if="activeMenu === 'profile'" class="fade-in resume-hub-container relative">
          
          <div v-if="hoveredPreviewUrl" class="dashboard-preview">
            <div class="preview-header">简历大图预览</div>
            <img :src="getSafeImageUrl(hoveredPreviewUrl)" alt="预览" />
          </div>

          <div class="rh-module ai-module">
            <div class="rh-header">
              <div class="rh-title">
                <h3>
                  <span class="title-icon">✨</span> 
                  AI 专属简历定制
                  <span class="hot-badge-new">🔥 智能生成</span>
                </h3>
                <p>只需两步：选择心仪的排版风格，粘贴经历信息，AI 为您生成专业描述与排版。</p>
              </div>
            </div>

            <div class="step-container">
              <div class="step-header-mini">
                <div class="step-badge">01</div>
                <h4 class="step-title-new">选择简历模板</h4>
                <span class="step-count">共 {{ dbTemplates.slice(0, 8).length }} 款精选模板</span>
              </div>

              <div class="template-grid-new" v-if="dbTemplates.length > 0">
                <div
                  v-for="tpl in dbTemplates.slice(0, 8)"
                  :key="'ai-' + tpl.id"
                  class="tpl-card-new"
                  :class="{ active: selectedAiTemplate?.id === tpl.id }"
                  @mouseenter="hoveredPreviewUrl = tpl.previewUrl"
                  @mouseleave="hoveredPreviewUrl = null"
                  @click="selectedAiTemplate = tpl"
                >
                  <div class="tpl-preview" :style="{ backgroundImage: `url(${getSafeImageUrl(tpl.previewUrl)})`, backgroundSize: 'cover', backgroundPosition: 'top center', backgroundColor: '#f1f5f9' }">
                    <div class="tpl-badge">AI 推荐</div>
                    <div class="active-check" v-if="selectedAiTemplate?.id === tpl.id">✓</div>
                  </div>
                  <div class="tpl-info-new" style="padding: 12px 0; text-align: center;">
                    <div class="tpl-name-new" style="font-weight: 600; color: #334155;">{{ tpl.name }}</div>
                  </div>
                </div>
              </div>
              <div v-else class="loading-state" style="text-align: center; padding: 20px; color: #94a3b8;">
                正在努力加载 OSS 模板数据...
              </div>
            </div>

            <div class="step-container">
              <div class="step-header-mini">
                <div class="step-badge">02</div>
                <h4 class="step-title-new">录入核心经历</h4>
                <span class="step-hint-new">哪怕只写大白话，AI也会帮您整理成专业简历</span>
              </div>

              <div class="example-card-new">
                <div class="example-icon">💡</div>
                <div class="example-content">
                  <div class="example-title">📝 输入示例</div>
                  <div class="example-text">
                    我叫张三，电话 13800000000，邮箱 zhangsan@email.com。南京工业大学计算机专业...
                  </div>
                </div>
                <button class="use-example-btn" @click="useExample">一键填充示例</button>
              </div>
              
              <div class="input-area-new">
                <div class="input-toolbar-new">
                  <div class="toolbar-buttons">
                    <button class="tool-btn" @click="addTemplate('education')"><span>🎓</span> 教育</button>
                    <button class="tool-btn" @click="addTemplate('work')"><span>💼</span> 工作</button>
                    <button class="tool-btn" @click="addTemplate('project')"><span>🚀</span> 项目</button>
                    <button class="tool-btn" @click="addTemplate('skill')"><span>⚡</span> 技能</button>
                    <button class="tool-btn" @click="addTemplate('award')"><span>🏆</span> 获奖</button>
                  </div>
                  <div class="toolbar-stats">
                    <span class="char-count">{{ rawResumeInput.length }}</span>
                    <span class="char-max">/ 5000</span>
                    <button class="clear-btn" @click="clearInput" v-if="rawResumeInput">清空</button>
                  </div>
                </div>
                <textarea v-model="rawResumeInput" placeholder="在这里粘贴或输入您的经历信息..." class="resume-textarea-new"></textarea>
              </div>
            </div>

            <div class="action-footer-new">
              <button class="generate-btn-new" @click="handleGenerateResume" :disabled="isGeneratingResume">
                <span class="btn-icon">🚀</span>
                <span class="btn-text">{{ isGeneratingResume ? 'AI 大模型正在奋笔疾书中...' : '一键生成专属简历' }}</span>
              </button>
            </div>
          </div>

          <div class="rh-module">
            <div class="rh-header" style="display: flex; justify-content: space-between; align-items: flex-end;">
              <div class="rh-title">
                <h3>🗂️ 简历模板库 <span class="badge-new">Word 原件下载</span></h3>
                <p>挑选喜欢的格式，直接下载 Word 文档修改，或者进行在线编辑</p>
              </div>
              <div class="view-all-btn" @click="router.push('/resume-template')">
                进入专门页面挑选 <span>→</span>
              </div>
            </div>
            
            <div class="rh-grid-new">
              <div class="rh-card-new" v-for="item in dbTemplates" :key="'manual-'+item.id"
                  @mouseenter="hoveredPreviewUrl = item.previewUrl; hoverId = 'manual-'+item.id"
                  @mouseleave="hoveredPreviewUrl = null; hoverId = null"
                  @click="goToTemplateDetail(item.id)">
                <div class="card-inner-new" :style="{ backgroundImage: `url(${getSafeImageUrl(item.previewUrl)})` }">
                  <transition name="fade">
                    <div class="hover-overlay-new" v-show="hoverId === 'manual-'+item.id">
                      <button class="overlay-btn-new primary" @click.stop="goToTemplateDetail(item.id)">在线编辑</button>
                      <button class="overlay-btn-new outline" @click.stop="handleDownload(item)">在线下载</button>
                    </div>
                  </transition>
                </div>
                <div class="rh-info-new">
                  <h4>{{ item.name }}</h4>
                  <p>📄 DOCX 格式模板</p>
                </div>
              </div>
            </div>
          </div>

          <div class="rh-module">
            <div class="rh-header" style="display: flex; justify-content: space-between; align-items: flex-end;">
              <div class="rh-title">
                <h3>💡 优秀简历灵感</h3>
                <p>各行各业大厂真实脱敏简历，可直接预览或下载参考</p>
              </div>
              <div class="view-all-btn" @click="router.push('/resume-template')">
                查看全部牛人简历 <span>→</span>
              </div>
            </div>
            
            <div class="rh-grid-new">
              <div class="rh-card-new case-card" v-for="item in excellentResumes" :key="'excellent-'+item.id"
                  @mouseenter="hoveredPreviewUrl = item.previewUrl; hoverId = 'excellent-'+item.id"
                  @mouseleave="hoveredPreviewUrl = null; hoverId = null"
                  @click="goToTemplateDetail(item.id)"> <div class="card-inner-new" :style="{ backgroundImage: `url(${getSafeImageUrl(item.previewUrl)})` }">
                  <transition name="fade">
                    <div class="hover-overlay-new" v-show="hoverId === 'excellent-'+item.id">
                      <button class="overlay-btn-new primary" @click.stop="previewRealDoc(item.docUrl)">真实原件预览</button>
                      <button class="overlay-btn-new outline" @click.stop="handleDownload(item)">直接下载原件</button>
                    </div>
                  </transition>
                </div>
                <div class="rh-info-new">
                  <h4>{{ item.name }}</h4>
                  <p>🌟 行业大牛参考案例</p>
                </div>
              </div>
            </div>
          </div>

        </section>
        
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../utils/request'
import Chat from './Chat.vue' 

const route = useRoute()
const router = useRouter()
const isChatSidebarOpen = ref(false)

const goToPage = (path) => {
  router.push(path)
}

// ================= ★ 核心：后端数据分类映射 ★ =================
const CATEGORY_MAP = {
  KAOYAN: '考研动态',
  BAOYAN: '保研推免',
  LIUXUE: '海外留学',
  CONTEST: '竞赛信息大厅'
}

const homeData = ref([
  {
    title: '🎓 学业深造推荐',
    target: 'matrix',
    items: [] 
  },
  {
    title: '💼 产教融合热榜',
    target: 'job',
    items: [
      { title: 'Java/Spring Boot 后端开发高频面试题解析', type: 'video', typeText: '视频', author: '代码大牛', color: 'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)' },
      { title: '从零到一教你如何打造脱颖而出的前端作品集', type: 'article', typeText: '文章', author: '前端进阶指南', color: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%)' },
      { title: '字节跳动数据分析实习生日常揭秘', type: 'video', typeText: '视频', author: '职场Vlog', color: 'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)' },
      { title: '产品经理必备：大厂真实需求文档(PRD)长啥样？', type: 'article', typeText: '文章', author: 'PM干货铺', color: 'linear-gradient(135deg, #fdcbf1 0%, #fdcbf1 1%, #e6dee9 100%)' }
    ]
  },
  {
    title: '🏆 精选竞赛情报',
    target: 'contest',
    items: [] 
  }
])

const matrixSections = ref([
  { title: '📚 考研最新政策与动态', dbCategory: CATEGORY_MAP.KAOYAN, items: [], page: 0, isLast: false, loading: false, filterType: 'all' },
  { title: '🏆 保研推免全通关攻略', dbCategory: CATEGORY_MAP.BAOYAN, items: [], page: 0, isLast: false, loading: false, filterType: 'all' },
  { title: '✈️ 海外留学情报局', dbCategory: CATEGORY_MAP.LIUXUE, items: [], page: 0, isLast: false, loading: false, filterType: 'all' }
])

const formatToCards = (dataList) => {
  const colors = [
    'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
    'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)',
    'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
    'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)'
  ];
  return dataList.map((item, index) => {
    let coverImg = '';
    if (item.type === 'video' && item.videoUrl && item.videoUrl.includes('aliyuncs.com')) {
       coverImg = `${item.videoUrl}?x-oss-process=video/snapshot,t_3000,f_jpg,w_600,h_400,m_fast`;
    }

    return {
      id: item.id,               
      title: item.title,         
      type: item.type || 'video', 
      typeText: item.type === 'video' ? '视频' : '文章',
      author: item.authorName || '官方发布',
      videoUrl: item.videoUrl,   
      color: colors[index % colors.length],
      coverImg: coverImg,
      category: item.category 
    }
  });
};

const allLocalContentData = ref([]);

const fetchContentPage = async (category, page, size = 12) => {
  try {
    const res = await request.get('/api/content/list', {
      params: { category, page, size } 
    });
    
    let list = [];
    if (Array.isArray(res)) list = res;
    else if (Array.isArray(res.data)) list = res.data;
    else if (Array.isArray(res.data?.content)) list = res.data.content;
    else if (Array.isArray(res.content)) list = res.content;
    else if (res.data?.data && Array.isArray(res.data.data)) list = res.data.data;

    return { list, isLast: list.length < size };
  } catch (error) {
    return { list: [], isLast: true };
  }
};

const initRealData = async () => {
  const [kaoyan, baoyan, liuxue, contest] = await Promise.all([
    fetchContentPage(CATEGORY_MAP.KAOYAN, 0, 12),
    fetchContentPage(CATEGORY_MAP.BAOYAN, 0, 12),
    fetchContentPage(CATEGORY_MAP.LIUXUE, 0, 12),
    fetchContentPage(CATEGORY_MAP.CONTEST, 0, 12)
  ]);

  if (kaoyan.list.length > 0) matrixSections.value[0].items = formatToCards(kaoyan.list);
  if (baoyan.list.length > 0) matrixSections.value[1].items = formatToCards(baoyan.list);
  if (liuxue.list.length > 0) matrixSections.value[2].items = formatToCards(liuxue.list);

  if (kaoyan.list.length > 0) homeData.value[0].items = formatToCards(kaoyan.list.slice(0, 4));
  if (contest.list.length > 0) homeData.value[2].items = formatToCards(contest.list.slice(0, 4));

  allLocalContentData.value = [
    ...kaoyan.list, ...baoyan.list, ...liuxue.list, ...contest.list
  ];
};

const filteredMatrixItems = (section) => {
  if (section.filterType === 'all') {
    return section.items;
  }
  return section.items.filter(item => item.type === section.filterType);
}

const goToListPage = (category) => {
  router.push({ 
    path: '/video-list', 
    query: { category: category } 
  })
}

// ================= 用户信息及导航状态 =================
const userRole = ref(localStorage.getItem('userRole') || 'student')
const userName = ref('加载中...')
const userAccount = ref('加载中...') 
const userOrg = ref('加载中...') 
const userAvatar = ref('https://api.dicebear.com/7.x/avataaars/svg?seed=Felix')

const activeMenu = ref('home')
const isDropdownOpen = ref(false)

const toggleDropdown = () => isDropdownOpen.value = !isDropdownOpen.value
const closeDropdown = () => isDropdownOpen.value = false

const fetchHeaderUserInfo = async () => {
  try {
    const res = await request.get('/api/user/info')
    if (res) {
      userName.value = res.nickname || res.nickName || '未命名用户'
      userAccount.value = res.phone || '未绑定手机号'
      
      if (res.role) {
        userRole.value = res.role
        localStorage.setItem('userRole', res.role)
      }
      
      if (userRole.value === 'enterprise') {
        userOrg.value = res.companyName || res.org || '未填写企业名称'
      } else {
        userOrg.value = res.schoolName || res.org || '未填写学校'
      }
      
      userAvatar.value = res.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'
      
      localStorage.setItem('userName', userName.value)
      localStorage.setItem('userOrg', userOrg.value)
    }
  } catch (error) {
    fallbackToLocalData()
  }
}

const fallbackToLocalData = () => {
  const savedRole = localStorage.getItem('userRole')
  if (savedRole) {
    userRole.value = savedRole
  }
  const savedOrg = localStorage.getItem('userOrg')
  userOrg.value = savedOrg || (userRole.value === 'enterprise' ? '未知企业' : '未知学校')
  const savedName = localStorage.getItem('userName')
  userName.value = savedName || (userRole.value === 'enterprise' ? '企业HR' : '学生用户')
  userAccount.value = '暂无网络'
}

// ================= 初次进入页面加载 Redis 最新数据 =================
const fetchInitialBoardData = async () => {
  try {
    const res = await request.get('/api/university/getLatestData');
    if (res && res.length > 0) {
      hotKaoyanData.value = res;
    }
  } catch (error) {
    console.error("拉取看板初始快照失败", error);
  }
}

onMounted(() => {
  fetchHeaderUserInfo()
  startAutoPlay()
  initRealData()
  fetchContestArticles()
  fetchInitialBoardData()
  initWebSocket()
  fetchJobData()
  fetchTemplatesData()
})

watch(() => route.query.edit, (newVal) => {
  if (newVal === 'true') {
    activeMenu.value = 'profile' 
    router.replace({ path: '/dashboard' }) 
  }
}, { immediate: true })

const goToProfile = () => {
  closeDropdown()
  router.push('/user-profile')
}

const logout = () => {
  closeDropdown()
  localStorage.removeItem('token')
  localStorage.removeItem('userRole')
  localStorage.removeItem('userName')
  localStorage.removeItem('userOrg')
  router.push('/login')
}

const handleCardClick = (item) => {
  if (item.type === 'video') {
    router.push({ 
      path: '/video', 
      query: { 
        title: item.title, 
        author: item.author || '平台官方',
        url: item.videoUrl 
      } 
    })
  } else if (item.type === 'article') {
    if (item.videoUrl && item.videoUrl.startsWith('http')) {
       window.open(item.videoUrl, '_blank');
    } else {
       activeMenu.value = 'articleDetail'
       fetchArticleDetail(item.id)
    }
  }
}

// ================= 🌟 简历中心：恢复为数据库模板驱动的写法 =================
const selectedAiTemplate = ref(null)
const rawResumeInput = ref('')
const isGeneratingResume = ref(false)

const detectedInfo = computed(() => {
  const info = []
  const text = rawResumeInput.value
  if (text.match(/[1-9]\d{10}/)) info.push('📱 手机号')
  if (text.match(/@/)) info.push('📧 邮箱')
  if (text.match(/大学|学院|学校/)) info.push('🎓 教育经历')
  if (text.match(/项目|开发|设计|负责|参与/)) info.push('💼 项目经历')
  if (text.match(/熟悉|掌握|精通|了解/)) info.push('⚡ 技能特长')
  if (text.match(/获奖|荣誉|一等奖|奖学金/)) info.push('🏆 获奖经历')
  return info.slice(0, 5)
})

const suggestions = computed(() => {
  const sug = []
  const text = rawResumeInput.value
  if (!text.match(/大学|学院|学校/)) sug.push('教育背景')
  if (!text.match(/项目|工作|实习|经历/)) sug.push('项目/工作经历')
  if (!text.match(/熟悉|掌握|精通|了解/)) sug.push('技能特长')
  if (!text.match(/获奖|荣誉|一等奖|奖学金/)) sug.push('获奖/证书')
  if (!text.match(/电话|手机|邮箱|@/)) sug.push('联系方式')
  return sug.slice(0, 4)
})

const useExample = () => {
  rawResumeInput.value = `我叫张三，电话 13800000000，邮箱 zhangsan@email.com。2024年毕业于南京工业大学计算机科学与技术专业，GPA 3.8/4.0。

【项目经历】
校园外卖平台项目 | 2023.09 - 2024.02
- 使用 Vue3 + Vite + Pinia 构建前端，实现商家展示、购物车、订单管理等功能
- 基于 Axios 封装请求，实现 JWT 身份验证和路由守卫
- 项目获得院级优秀项目，服务校内用户 2000+

【技能特长】
- 编程语言：JavaScript、TypeScript、Python
- 框架工具：Vue3、React、Node.js、Git
- 语言能力：CET-6 (550分)

【获奖情况】
- 校一等奖学金 (2022-2023)
- 全国计算机设计大赛省二等奖
- 校优秀学生干部`
}

const clearInput = () => {
  rawResumeInput.value = ''
}

const addTemplate = (type) => {
  const templates = {
    education: '\n\n【教育经历】\n• 学校名称 | 专业 | 学历 | 2020.09 - 2024.06\n• 主修课程：\n• GPA：3.8/4.0\n',
    work: '\n\n【工作经历】\n• 公司名称 | 职位 | 2023.07 - 至今\n• 工作内容：\n• 主要成果：\n',
    project: '\n\n【项目经历】\n• 项目名称 | 技术栈 | 2023.09 - 2024.02\n• 项目描述：\n• 我的职责：\n• 项目成果：\n',
    skill: '\n\n【专业技能】\n• 编程语言：JavaScript、Python、Java\n• 框架工具：Vue3、React、Node.js\n• 语言能力：英语 CET-6\n',
    award: '\n\n【获奖情况】\n• 奖项名称 | 级别 | 时间\n• 奖项名称 | 级别 | 时间\n'
  }
  rawResumeInput.value += templates[type] || ''
}

// ================= 🌟 简历生成核心逻辑 🌟 =================

/** 把 AI 返回的字段拼成预览页用的 Markdown */
function buildResumeMarkdownFromAi(data) {
  const name = (data.fullName || '求职者').trim()
  const job = (data.targetJob || '求职意向').trim()
  let md = `# ${name} - ${job}\n\n`
  
  if (data.educationInfo) {
    md += `## 教育经历\n\n${data.educationInfo}\n\n`
  }
  if (data.projectExperience) {
    md += `## 项目与经历\n\n${data.projectExperience}\n\n`
  }
  if (data.professionalSkills) {
    md += `## 专业技能\n\n${data.professionalSkills}\n\n`
  }
  return md.trim()
}

/** 从大白话里粗提姓名 */
function guessNameFromRaw(text) {
  const m = text.match(/我是\s*([^\s，,。]{2,20})/)
  return m ? m[1].trim() : ''
}

const handleGenerateResume = async () => {
  if (!selectedAiTemplate.value || !selectedAiTemplate.value.id) {
    alert('请先选择一个简历模板！')
    return
  }
  if (!rawResumeInput.value.trim()) {
    alert('请写点内容，AI 才能帮您整理哦！')
    return
  }
  
  const raw = rawResumeInput.value.trim()
  const guessedName = guessNameFromRaw(raw)
  
  const payload = {
    fullName: guessedName || '求职者',
    contactPhone: '',
    targetJob: '综合求职意向',
    educationInfo: '',
    projectExperience: raw,
    professionalSkills: '',
    templateName: selectedAiTemplate.value.name || '标准通用模板'
  }
  
  isGeneratingResume.value = true
  sessionStorage.setItem('tempResumePrompt', raw)
  sessionStorage.setItem('selectedTemplateId', String(selectedAiTemplate.value.id))
  
  try {
    // 🌟 核心修改点：单独为这个极耗时的接口设置 180 秒（180000 毫秒）的超长超时
    const res = await request.post('/api/resume/ai-generate', payload, {
      timeout: 180000 
    })
    
    // 兼容后端直接返回 Map 的情况
    const finalData = res && typeof res === 'object' && !Array.isArray(res) ? res : {}
    const merged = { ...payload, ...finalData }
    
    const md = buildResumeMarkdownFromAi(merged)
    
    // 写入 localStorage 供预览页读取
    localStorage.setItem('generatedResume', md)
    localStorage.setItem('generatedResumeFullData', JSON.stringify(merged))
    const tid = selectedAiTemplate.value?.id
    if (tid) {
      try {
        localStorage.setItem(`resumeDraft_template_${tid}`, md)
      } catch {
        /* ignore */
      }
    }

    sessionStorage.setItem('resumePreviewLockLayoutToTemplate', '1')
    router.push({ path: '/resume-preview' })
  } catch (e) {
    console.error('简历生成失败:', e)
    // 针对超时的专门提示
    if (e.message && e.message.includes('timeout')) {
      alert('AI 大模型思考时间过长，已超时，请精简经历内容或稍后重试！')
    } else {
      alert('AI 生成失败，请检查后端与网络，或稍后重试')
    }
  } finally {
    isGeneratingResume.value = false
  }
}

// ================= 全局搜索逻辑 =================
const hotSearchList = ref([
  { keyword: '2026考研大纲', count: 128 },
  { keyword: '互联网+大赛', count: 96 },
  { keyword: '字节跳动实习', count: 85 },
  { keyword: '春招时间表', count: 64 },
  { keyword: '雅思机考', count: 52 },
  { keyword: '计算机二级', count: 43 },
  { keyword: '保研夏令营', count: 38 },
  { keyword: '腾讯校招', count: 31 }
])

const showHotSearch = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const isSearching = ref(false)

let blurTimer = null
const hideHotSearch = () => {
  blurTimer = setTimeout(() => {
    showHotSearch.value = false
  }, 200)
}

const selectHotSearch = (item) => {
  searchKeyword.value = item.keyword
  showHotSearch.value = false
  performSearch(item.keyword)
}

const performSearch = async (keyword) => {
  if (!keyword || !keyword.trim()) return;
  searchKeyword.value = keyword;
  showHotSearch.value = false;
  activeMenu.value = 'search'; 
  isSearching.value = true;
  searchResults.value = [];

  try {
    const res = await request.get('/api/content/search', { params: { keyword } });
    let list = [];
    if (Array.isArray(res)) list = res;
    else if (Array.isArray(res.data)) list = res.data;
    else if (res.data?.content) list = res.data.content;
    
    if (list && list.length > 0) {
      searchResults.value = formatToCards(list);
      isSearching.value = false;
      return;
    }
    throw new Error("Backend search returned empty");
  } catch (error) {
    const keywordLower = keyword.toLowerCase();
    const filtered = allLocalContentData.value.filter(item => {
      const titleMatch = item.title && item.title.toLowerCase().includes(keywordLower);
      const authorMatch = item.authorName && item.authorName.toLowerCase().includes(keywordLower);
      const catMatch = item.category && item.category.toLowerCase().includes(keywordLower);
      return titleMatch || authorMatch || catMatch;
    });

    setTimeout(() => {
      searchResults.value = formatToCards(filtered);
      isSearching.value = false;
    }, 500); 
  }
}

onUnmounted(() => {
  if (blurTimer) clearTimeout(blurTimer)
  if (slideInterval) clearInterval(slideInterval)
  if (ws) {
    ws.close()
  }
})

// ================= 首页推荐数据 =================
const carouselSlides = ref([
  { image: '/images11/photo-1522071820081-009f0129c71c.jpg', title: '2026春季大厂校园招聘全面开启', description: '提前批内推 / 简历辅导 / 面试真题全解析，助你拿下心仪 Offer', link: '/recruitment' },
  { image: '/images11/photo-1517245386807-bb43f82c33c4.jpg', title: '考研倒计时：冲刺阶段复习规划', description: '名师押题 / 真题精讲 / 模拟考试，最后三个月如何高效提分', link: '/graduate-exam' },
  { image: '/images11/photo-1434030216411-0b793f4b4173.jpg', title: '留学申请季：全球TOP50院校指南', description: '文书写作 / 推荐信 / 面试辅导，一站式留学服务', link: '/abroad' },
  { image: '/images11/photo-1524178232363-1fb2b075b655.jpg', title: '互联网大厂实习内推专场', description: '字节 / 腾讯 / 阿里 / 美团，50+岗位等你来投', link: '/internship' },
  { image: '/images11/photo-1552664730-d307ca884978.jpg', title: '竞赛集训营：冲击国奖', description: '互联网+ / 挑战杯 / 数学建模，金牌导师带队', link: '/competition' }
])

const currentSlide = ref(0)
let slideInterval = null

const startAutoPlay = () => {
  slideInterval = setInterval(() => {
    nextSlide()
  }, 5000)
}
const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % carouselSlides.value.length
}
const prevSlide = () => {
  currentSlide.value = (currentSlide.value - 1 + carouselSlides.value.length) % carouselSlides.value.length
}
const goToLink = (link) => {
  router.push(link)
}

// ================= 学业深造矩阵 =================
const targetSchoolKeyword = ref('')
const schoolDataType = ref('kaoyan') 
const selectedSchoolMajor = ref('全部')

let ws = null;

const initWebSocket = () => {
  const wsUrl = `ws://localhost:8080/chat/dashboard_viewer_${new Date().getTime()}`; 
  ws = new WebSocket(wsUrl);

  ws.onopen = () => console.log("📊 实时数据看板：WebSocket 连接成功");

  ws.onmessage = (event) => {
    try {
      const newData = JSON.parse(event.data);
      hotKaoyanData.value = newData;
    } catch (e) {
      // ignore
    }
  };

  ws.onerror = (error) => console.error("WebSocket 发生错误", error);
  
  ws.onclose = () => {
    console.log("WebSocket 断开，3秒后尝试重连...");
    setTimeout(initWebSocket, 3000);
  };
};

const hotKaoyanData = ref([
  { name: '北京大学', tags: ['985', '双一流', '综合类'], scoreLine: '356 -> 365 -> 372', reExamRatio: '1.2:1', applyRatio: '12:1', intensityLevel: '高' },
  { name: '清华大学', tags: ['985', '双一流', '理工类'], scoreLine: '340 -> 355 -> 360', reExamRatio: '1.3:1', applyRatio: '9:1', intensityLevel: '中等' },
  { name: '复旦大学', tags: ['985', '双一流', '综合类'], scoreLine: '335 -> 348 -> 355', reExamRatio: '1.2:1', applyRatio: '8.5:1', intensityLevel: '中等' },
  { name: '浙江大学', tags: ['985', '双一流', '综合类'], scoreLine: '360 -> 375 -> 382', reExamRatio: '1.5:1', applyRatio: '18:1', intensityLevel: '极高' }
])

const hotBaoyanData = ref([
  { name: '清华大学', tags: ['985', '双一流', '理工类'], major: '计算机科学与技术', timeline: '夏令营:6月上旬 | 预推免:8月底', rankReq: '专业排名前3%', englishReq: 'CET6 520+', officialUrl: 'https://yz.tsinghua.edu.cn/' },
  { name: '北京航空航天大学', tags: ['985', '双一流', '理工类'], major: '计算机科学与技术', timeline: '夏令营:6月中旬 | 预推免:9月初', rankReq: '专业排名前5%', englishReq: 'CET6 480+', officialUrl: 'http://yzb.buaa.edu.cn/' },
  { name: '浙江大学', tags: ['985', '双一流', '综合类'], major: '人工智能', timeline: '夏令营:6月中旬 | 预推免:8月下旬', rankReq: '专业排名前5%', englishReq: 'CET6 500+', officialUrl: 'http://grs.zju.edu.cn/yjszs/' },
  { name: '上海交通大学', tags: ['985', '双一流', '理工类'], major: '软件工程', timeline: '夏令营:7月上旬 | 预推免:9月上旬', rankReq: '专业排名前10%', englishReq: 'CET6 460+', officialUrl: 'https://yzb.sjtu.edu.cn/' },
  { name: '南京大学', tags: ['985', '双一流', '综合类'], major: '人工智能', timeline: '夏令营:6月底 | 预推免:9月中旬', rankReq: '专业排名前10%', englishReq: 'CET6 460+', officialUrl: 'https://gsec.nju.edu.cn/' },
  { name: '同济大学', tags: ['985', '双一流', '理工类'], major: '电子信息', timeline: '夏令营:7月中旬 | 预推免:9月初', rankReq: '专业排名前8%', englishReq: 'CET6 425+', officialUrl: 'https://yz.tongji.edu.cn/' }
])

const currentSchoolData = computed(() => {
  let data = schoolDataType.value === 'kaoyan' ? hotKaoyanData.value : hotBaoyanData.value;
  const keyword = targetSchoolKeyword.value.trim();
  const major = selectedSchoolMajor.value;

  if (major !== '全部') {
    data = data.filter(s => s.major === major);
  }

  if (keyword) {
    return data.filter(s => s.name.includes(keyword));
  } else {
    return data.slice(0, 4);
  }
})

const handleSchoolSearch = () => {
  console.log('搜索目标院校:', targetSchoolKeyword.value)
}

const getIntensityClass = (level) => {
  if (level === '极高') return 'danger'
  if (level === '高') return 'warning'
  return 'normal'
}

const viewSchoolDetail = (school) => {
  if (school.officialUrl) {
    window.open(school.officialUrl, '_blank');
  } else {
    alert(`暂未收录【${school.name}】的官方网址`);
  }
}

const getSectionIconClass = (category) => {
  const map = {
    '考研动态': 'kaoyan',
    '保研推免': 'baoyan',
    '海外留学': 'liuxue'
  }
  return map[category] || ''
}

const getSectionIcon = (category) => {
  const map = {
    '考研动态': '🎓',
    '保研推免': '🏆',
    '海外留学': '✈️'
  }
  return map[category] || '📚'
}

const formatDateShort = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// ================= ★ 产教融合真实数据获取与过滤逻辑 ★ =================
const jobCategories = ref([{name:'互联网/AI',icon:'💻',subCategories:[{name:'后端开发',jobs:[{name:'Java',count:245,hot:true},{name:'Python',count:189,hot:true},{name:'C++',count:156},{name:'Go',count:98},{name:'Node.js',count:76},{name:'.NET',count:45},{name:'C#',count:67},{name:'PHP',count:89},{name:'Golang',count:54},{name:'全栈工程师',count:123,hot:true},{name:'架构师',count:67,hot:true},{name:'后端开发',count:112}]},{name:'前端开发',jobs:[{name:'Vue.js',count:203,hot:true},{name:'React',count:187,hot:true},{name:'JavaScript',count:165},{name:'TypeScript',count:112},{name:'HTML/CSS',count:98},{name:'小程序开发',count:67},{name:'移动端开发',count:78},{name:'鸿蒙开发',count:34,hot:true},{name:'前端架构师',count:56},{name:'跨端开发',count:43},{name:'WebGL',count:23}]},{name:'测试',jobs:[{name:'软件测试',count:134},{name:'自动化测试',count:98},{name:'性能测试',count:56},{name:'测试开发',count:67},{name:'功能测试',count:45},{name:'渗透测试',count:34,hot:true},{name:'游戏测试',count:23},{name:'硬件测试',count:32}]},{name:'运维',jobs:[{name:'运维工程师',count:112},{name:'网络安全',count:78},{name:'DBA',count:45},{name:'系统工程师',count:67},{name:'网络工程师',count:56},{name:'运维开发',count:34},{name:'云计算',count:89,hot:true},{name:'SRE',count:43},{name:'IDC运维',count:28}]},{name:'算法',jobs:[{name:'推荐算法',count:67,hot:true},{name:'大模型算法',count:54,hot:true},{name:'图像算法',count:43},{name:'自然语言处理',count:38,hot:true},{name:'搜索算法',count:29},{name:'语音算法',count:23},{name:'数据挖掘',count:45},{name:'机器学习',count:78,hot:true},{name:'深度学习',count:56,hot:true}]},{name:'产品',jobs:[{name:'产品经理',count:156,hot:true},{name:'产品专员',count:98},{name:'产品总监',count:34},{name:'电商产品',count:45},{name:'AI产品',count:67,hot:true},{name:'B端产品',count:54},{name:'数据产品',count:43},{name:'策略产品',count:32}]},{name:'数据分析',jobs:[{name:'数据分析师',count:134,hot:true},{name:'数据开发',count:78},{name:'数据仓库',count:56},{name:'数据产品',count:34},{name:'数据科学家',count:23},{name:'大数据开发',count:45,hot:true},{name:'ETL工程师',count:32}]},{name:'移动开发',jobs:[{name:'Android',count:89},{name:'iOS',count:76},{name:'Flutter',count:45,hot:true},{name:'React Native',count:38},{name:'鸿蒙',count:29,hot:true}]},{name:'人工智能',jobs:[{name:'计算机视觉',count:67,hot:true},{name:'语音识别',count:34},{name:'NLP',count:56,hot:true},{name:'强化学习',count:23},{name:'AIGC',count:45,hot:true}]},{name:'高端技术',jobs:[{name:'技术总监',count:34},{name:'架构师',count:45,hot:true},{name:'技术专家',count:38},{name:'首席科学家',count:12},{name:'CTO',count:8}]}]},{name:'电子/通信/硬件',icon:'📡',subCategories:[{name:'硬件开发',jobs:[{name:'嵌入式',count:89},{name:'FPGA',count:45},{name:'硬件工程师',count:67},{name:'PCB设计',count:34},{name:'单片机',count:56},{name:'ARM开发',count:43},{name:'电路设计',count:38},{name:'射频工程师',count:29}]},{name:'通信',jobs:[{name:'5G',count:56},{name:'网络优化',count:34},{name:'通信工程师',count:45},{name:'射频工程师',count:23},{name:'光通信',count:18},{name:'基站工程师',count:27}]},{name:'半导体',jobs:[{name:'芯片设计',count:34,hot:true},{name:'集成电路',count:45},{name:'版图设计',count:23},{name:'半导体工艺',count:28},{name:'封测',count:15}]},{name:'自动化',jobs:[{name:'自动化工程师',count:67},{name:'PLC',count:43},{name:'工业控制',count:34},{name:'机器人',count:28,hot:true}]},{name:'仪器仪表',jobs:[{name:'测控',count:34},{name:'仪器开发',count:23},{name:'传感器',count:28}]}]},{name:'金融',icon:'💰',subCategories:[{name:'量化',jobs:[{name:'量化研究员',count:45},{name:'量化开发',count:34},{name:'量化交易',count:23},{name:'高频交易',count:18,hot:true}]},{name:'风控',jobs:[{name:'风控模型',count:56},{name:'反欺诈',count:34},{name:'信用分析',count:45},{name:'风险策略',count:28}]},{name:'金融科技',jobs:[{name:'支付系统',count:34},{name:'区块链',count:23,hot:true},{name:'数字货币',count:12},{name:'FinTech',count:28,hot:true}]},{name:'投资银行',jobs:[{name:'投行分析师',count:34},{name:'并购',count:18},{name:'承销',count:15}]},{name:'证券/基金',jobs:[{name:'行业研究',count:45},{name:'投资顾问',count:28},{name:'基金经理',count:16},{name:'交易员',count:32}]},{name:'保险',jobs:[{name:'精算师',count:12},{name:'保险核保',count:23},{name:'保险理赔',count:28}]}]},{name:'产品/运营/设计',icon:'🎨',subCategories:[{name:'产品',jobs:[{name:'产品经理',count:156,hot:true},{name:'产品专员',count:98},{name:'产品总监',count:34},{name:'电商产品',count:45},{name:'AI产品',count:67,hot:true},{name:'B端产品',count:54},{name:'数据产品',count:43}]},{name:'运营',jobs:[{name:'用户运营',count:87},{name:'内容运营',count:76},{name:'活动运营',count:65},{name:'产品运营',count:78},{name:'新媒体运营',count:92,hot:true},{name:'电商运营',count:67},{name:'社群运营',count:54},{name:'私域运营',count:43}]},{name:'设计',jobs:[{name:'UI设计',count:98},{name:'UX设计',count:67},{name:'平面设计',count:76},{name:'交互设计',count:54},{name:'视觉设计',count:65},{name:'品牌设计',count:43},{name:'包装设计',count:32}]},{name:'UX/UE',jobs:[{name:'用户体验',count:45},{name:'用户研究',count:34},{name:'人机交互',count:23}]},{name:'游戏设计',jobs:[{name:'游戏策划',count:45},{name:'游戏UI',count:32},{name:'游戏原画',count:28},{name:'游戏场景',count:21}]},{name:'视觉/交互',jobs:[{name:'视觉设计',count:56},{name:'交互设计',count:43},{name:'动效设计',count:23}]}]},{name:'市场/销售/商务',icon:'📈',subCategories:[{name:'市场营销',jobs:[{name:'市场营销',count:87},{name:'品牌策划',count:56},{name:'公关',count:34},{name:'市场推广',count:65},{name:'SEO/SEM',count:43},{name:'网络营销',count:54}]},{name:'销售',jobs:[{name:'销售代表',count:156},{name:'销售经理',count:98},{name:'大客户销售',count:76,hot:true},{name:'电话销售',count:87},{name:'渠道销售',count:54},{name:'销售总监',count:23},{name:'区域销售',count:45}]},{name:'商务',jobs:[{name:'商务拓展',count:67},{name:'商务经理',count:45},{name:'渠道合作',count:34},{name:'BD',count:56,hot:true}]},{name:'广告',jobs:[{name:'广告投放',count:43},{name:'广告优化',count:32},{name:'媒介',count:23},{name:'广告策划',count:34}]},{name:'会展/活动',jobs:[{name:'活动策划',count:34},{name:'会展执行',count:23},{name:'会议活动',count:18}]}]},{name:'人事/财务/行政',icon:'📋',subCategories:[{name:'人力资源',jobs:[{name:'HRBP',count:56,hot:true},{name:'招聘',count:78},{name:'培训',count:34},{name:'薪酬绩效',count:43},{name:'员工关系',count:32},{name:'HRD',count:15},{name:'组织发展',count:12}]},{name:'财务',jobs:[{name:'会计',count:89},{name:'出纳',count:54},{name:'财务分析',count:43},{name:'审计',count:38},{name:'税务',count:27},{name:'CFO',count:5},{name:'成本会计',count:34}]},{name:'行政',jobs:[{name:'行政专员',count:67},{name:'行政经理',count:34},{name:'前台',count:28},{name:'后勤',count:23},{name:'行政主管',count:32}]},{name:'法务',jobs:[{name:'法务专员',count:34},{name:'法务经理',count:23},{name:'合规',count:18},{name:'知识产权',count:12}]}]},{name:'生产制造/供应链',icon:'🏭',subCategories:[{name:'生产管理',jobs:[{name:'生产主管',count:45},{name:'生产经理',count:34},{name:'厂长',count:12},{name:'生产计划',count:28},{name:'车间主任',count:23}]},{name:'质量管理',jobs:[{name:'QC',count:56},{name:'QA',count:43},{name:'质量工程师',count:38},{name:'体系工程师',count:23},{name:'SQE',count:19}]},{name:'供应链',jobs:[{name:'采购',count:67},{name:'物流',count:54},{name:'仓储',count:43},{name:'供应链经理',count:23},{name:'报关员',count:15}]},{name:'工艺/制程',jobs:[{name:'工艺工程师',count:45},{name:'IE工程师',count:32},{name:'PE工程师',count:28},{name:'NPI工程师',count:19}]},{name:'设备维护',jobs:[{name:'设备工程师',count:34},{name:'维修工程师',count:23},{name:'自动化维护',count:18}]}]},{name:'零售/生活服务',icon:'🛒',subCategories:[{name:'零售',jobs:[{name:'店员/营业员',count:156},{name:'店长',count:67},{name:'导购',count:98},{name:'收银',count:76},{name:'商品管理',count:34},{name:'品类经理',count:28}]},{name:'餐饮',jobs:[{name:'服务员',count:123},{name:'厨师',count:67},{name:'店长',count:45},{name:'餐饮管理',count:34},{name:'奶茶/水吧',count:56},{name:'西点师',count:23}]},{name:'酒店/旅游',jobs:[{name:'前台',count:67},{name:'客房服务',count:45},{name:'酒店管理',count:34},{name:'导游',count:23},{name:'旅行顾问',count:19}]},{name:'美容/美发',jobs:[{name:'美容师',count:56},{name:'美发师',count:43},{name:'美甲师',count:32},{name:'纹绣师',count:18}]},{name:'运动/健身',jobs:[{name:'健身教练',count:34},{name:'瑜伽老师',count:23},{name:'游泳教练',count:18}]}]},{name:'教育培训',icon:'📚',subCategories:[{name:'教师',jobs:[{name:'语数外教师',count:89},{name:'艺术教师',count:67},{name:'体育教师',count:34},{name:'科学教师',count:45},{name:'音乐教师',count:28},{name:'美术教师',count:32}]},{name:'教务管理',jobs:[{name:'教务专员',count:56},{name:'教务主管',count:34},{name:'班主任',count:67}]},{name:'课程顾问',jobs:[{name:'课程销售',count:78},{name:'课程顾问',count:89,hot:true}]},{name:'培训',jobs:[{name:'培训讲师',count:45},{name:'企业培训',count:34},{name:'技能培训',count:28}]},{name:'留学/语言',jobs:[{name:'留学顾问',count:34},{name:'语言老师',count:45},{name:'雅思/托福',count:28,hot:true}]}]},{name:'房地产/建筑',icon:'🏗️',subCategories:[{name:'工程管理',jobs:[{name:'项目经理',count:45},{name:'施工员',count:67},{name:'资料员',count:34},{name:'材料员',count:23},{name:'安全员',count:38},{name:'质量员',count:28},{name:'监理工程师',count:32},{name:'工程经理',count:27}]},{name:'工程造价',jobs:[{name:'造价工程师',count:56},{name:'预算员',count:43},{name:'成本控制',count:32},{name:'招投标',count:27}]},{name:'装饰装修',jobs:[{name:'室内设计',count:45},{name:'软装设计',count:32},{name:'工长',count:28},{name:'装修监理',count:23},{name:'橱柜设计',count:18}]},{name:'物业管理',jobs:[{name:'物业经理',count:34},{name:'物业管家',count:45},{name:'维修工',count:38},{name:'保安',count:56},{name:'保洁',count:43}]},{name:'建筑设计',jobs:[{name:'建筑设计师',count:34},{name:'结构设计',count:28},{name:'暖通设计',count:19},{name:'电气设计',count:21},{name:'给排水设计',count:16}]},{name:'房地产规划开发',jobs:[{name:'房地产策划',count:23},{name:'土地拓展',count:15},{name:'开发报建',count:18},{name:'投资分析',count:21}]}]},{name:'物流/仓储/司机',icon:'🚚',subCategories:[{name:'物流',jobs:[{name:'物流专员',count:67},{name:'物流经理',count:34},{name:'调度',count:43},{name:'快递员',count:78},{name:'配送员',count:56}]},{name:'仓储',jobs:[{name:'仓管员',count:78},{name:'仓储主管',count:34},{name:'理货员',count:56},{name:'拣货员',count:43},{name:'仓库管理员',count:45}]},{name:'司机',jobs:[{name:'司机',count:89},{name:'货运司机',count:67},{name:'网约车司机',count:78},{name:'班车司机',count:34},{name:'叉车司机',count:45}]}]},{name:'医疗/健康',icon:'⚕️',subCategories:[{name:'医生',jobs:[{name:'内科医生',count:34},{name:'外科医生',count:28},{name:'全科医生',count:23},{name:'儿科医生',count:19},{name:'牙医',count:16}]},{name:'护士',jobs:[{name:'护士',count:56},{name:'护师',count:23},{name:'护理',count:45},{name:'ICU护士',count:18}]},{name:'医药',jobs:[{name:'医药代表',count:67},{name:'药学',count:34},{name:'药品注册',count:12},{name:'临床监查员',count:19},{name:'药理研究员',count:15}]},{name:'医疗器械',jobs:[{name:'医疗器械销售',count:45},{name:'医疗器械维修',count:23},{name:'临床工程师',count:18}]},{name:'健康管理',jobs:[{name:'健康顾问',count:23},{name:'营养师',count:18},{name:'康复治疗师',count:21}]}]},{name:'汽车',icon:'🚗',subCategories:[{name:'汽车研发',jobs:[{name:'汽车设计',count:34},{name:'底盘工程师',count:23},{name:'动力工程师',count:19},{name:'车身工程师',count:21},{name:'三电工程师',count:28,hot:true}]},{name:'汽车销售',jobs:[{name:'汽车销售',count:67},{name:'展厅经理',count:23},{name:'二手车评估',count:18}]},{name:'汽车维修',jobs:[{name:'机修工',count:45},{name:'钣金工',count:32},{name:'喷漆工',count:28},{name:'汽车电工',count:23}]},{name:'新能源汽车',jobs:[{name:'电池工程师',count:34,hot:true},{name:'电控工程师',count:28,hot:true},{name:'充电桩运维',count:19}]}]},{name:'采购/贸易',icon:'📦',subCategories:[{name:'采购',jobs:[{name:'采购专员',count:56},{name:'采购经理',count:34},{name:'供应商开发',count:28},{name:'战略采购',count:19}]},{name:'贸易',jobs:[{name:'外贸专员',count:45},{name:'外贸经理',count:28},{name:'报关员',count:19},{name:'单证员',count:23}]},{name:'跨境电商',jobs:[{name:'亚马逊运营',count:38,hot:true},{name:'速卖通运营',count:27},{name:'TikTok运营',count:23,hot:true}]}]},{name:'直播/影视/传媒',icon:'📺',subCategories:[{name:'直播',jobs:[{name:'主播',count:67,hot:true},{name:'带货主播',count:78,hot:true},{name:'直播运营',count:45},{name:'场控',count:23}]},{name:'影视',jobs:[{name:'编导',count:34},{name:'摄像',count:32},{name:'剪辑',count:45},{name:'后期制作',count:28}]},{name:'传媒',jobs:[{name:'新媒体运营',count:56},{name:'内容运营',count:43},{name:'文案策划',count:38},{name:'媒介专员',count:23}]},{name:'短视频',jobs:[{name:'短视频编导',count:34},{name:'短视频剪辑',count:45},{name:'抖音运营',count:56,hot:true}]}]},{name:'酒店/旅游',icon:'🏨',subCategories:[{name:'酒店',jobs:[{name:'前台接待',count:45},{name:'客房服务',count:38},{name:'酒店管理',count:28},{name:'礼宾',count:19}]},{name:'旅游',jobs:[{name:'导游',count:34},{name:'旅行顾问',count:23},{name:'计调',count:19},{name:'旅游策划',count:16}]},{name:'会展',jobs:[{name:'会展策划',count:23},{name:'会议服务',count:19},{name:'活动执行',count:21}]}]},{name:'设计/创意',icon:'🎯',subCategories:[{name:'平面设计',jobs:[{name:'平面设计',count:67},{name:'品牌设计',count:34},{name:'包装设计',count:28},{name:'画册设计',count:23}]},{name:'UI/UX',jobs:[{name:'UI设计',count:56},{name:'UX设计',count:34},{name:'交互设计',count:28},{name:'产品设计',count:32}]},{name:'工业设计',jobs:[{name:'产品造型',count:23},{name:'结构设计',count:28},{name:'CMF设计',count:16}]},{name:'服装设计',jobs:[{name:'服装设计',count:34},{name:'面料设计',count:15},{name:'配饰设计',count:12}]}]},{name:'能源/化工/环保',icon:'⚡',subCategories:[{name:'能源',jobs:[{name:'新能源',count:45,hot:true},{name:'光伏',count:34},{name:'风电',count:28},{name:'储能',count:23,hot:true}]},{name:'化工',jobs:[{name:'化工工艺',count:34},{name:'化学分析',count:28},{name:'研发工程师',count:23}]},{name:'环保',jobs:[{name:'环境工程',count:34},{name:'环评',count:19},{name:'水处理',count:23}]}]},{name:'咨询/法律/翻译',icon:'⚖️',subCategories:[{name:'咨询',jobs:[{name:'咨询顾问',count:45},{name:'战略咨询',count:28},{name:'管理咨询',count:34},{name:'人力咨询',count:19}]},{name:'法律',jobs:[{name:'律师',count:34},{name:'法务',count:45},{name:'合规',count:23},{name:'知识产权',count:16}]},{name:'翻译',jobs:[{name:'英语翻译',count:34},{name:'日语翻译',count:19},{name:'韩语翻译',count:15},{name:'同声传译',count:8,hot:true}]}]},{name:'高级管理',icon:'👔',subCategories:[{name:'高层管理',jobs:[{name:'CEO',count:5},{name:'COO',count:4},{name:'CFO',count:6},{name:'CTO',count:8},{name:'总经理',count:12}]},{name:'部门管理',jobs:[{name:'部门总监',count:23},{name:'高级经理',count:34},{name:'区域经理',count:28}]},{name:'项目管理',jobs:[{name:'项目经理',count:56},{name:'项目总监',count:23},{name:'PMO',count:15}]}]}])

const jobCards = ref([])
const loadingJobs = ref(false)

const fetchJobData = async () => {
  loadingJobs.value = true
  try {
    const res = await request.get('/api/jobs/list')
    
    let rawList = []
    if (res && Array.isArray(res.data)) rawList = res.data
    else if (Array.isArray(res)) rawList = res
    else if (res?.data?.records) rawList = res.data.records

    if (!rawList || rawList.length === 0) {
      jobCards.value = []
      return
    }

    jobCards.value = rawList.map(job => {
      let parsedTags = []
      if (job.techStack && typeof job.techStack === 'string') {
        parsedTags = job.techStack.split(',').filter(t => t.trim() !== '')
      }

      return {
        id: job.id,
        position: job.jobName || '未命名岗位', 
        company: job.companyName || '未知企业',
        salary: job.salary || '薪资面议',
        location: job.area || '全国',
        tags: parsedTags,
        deadline: '长期有效',
        hot: job.financingStage === '不需要融资', 
        category: job.industry || '互联网/AI', 
        subCategory: '后端开发', 
        jobType: '全部',
        jobDescription: job.jobDescription || '暂无岗位职责描述',
        requirements: job.requirements || '暂无任职要求说明'
      }
    })
  } catch (error) {
    console.error('获取产教融合岗位数据彻底失败:', error)
  } finally {
    loadingJobs.value = false
  }
}

const selectedCategory = ref('互联网/AI')
const selectedSubCategory = ref('后端开发')
const selectedJob = ref('全部')

const currentJobsList = computed(() => {
  const cat = jobCategories.value.find(c => c.name === selectedCategory.value)
  if (!cat) return []
  const sub = cat.subCategories.find(s => s.name === selectedSubCategory.value)
  return sub?.jobs || []
})

const filteredJobCards = computed(() => {
  if (!jobCards.value || jobCards.value.length === 0) return [];
  
  return jobCards.value.filter(card => {
    const matchCategory = !selectedCategory.value || 
                          (card.category && card.category.includes(selectedCategory.value));
    const matchSubCategory = !selectedSubCategory.value || card.subCategory === selectedSubCategory.value;
    const matchJob = !selectedJob.value || selectedJob.value === '全部' || card.jobType === selectedJob.value;
    return matchCategory && matchSubCategory && matchJob;
  });
})

const goToJobDetail = (job) => {
  router.push({
    path: '/job-detail',
    query: {
      data: encodeURIComponent(JSON.stringify(job))
    }
  })
}

// ================= 竞赛大厅数据 =================
const majorCategories = ref([
  {
    name: '哲学',
    icon: '📚',
    directions: [
      { name: '哲学类', skills: [ { name: '逻辑学', count: 8 }, { name: '伦理学', count: 6 }, { name: '美学', count: 5 }, { name: '宗教学', count: 4 } ] }
    ]
  },
  {
    name: '经济学',
    icon: '📈',
    directions: [
      { name: '经济学类', skills: [ { name: '微观经济学', count: 18 }, { name: '宏观经济学', count: 15 }, { name: '计量经济学', count: 12 }, { name: '产业经济学', count: 10 } ] },
      { name: '金融学类', skills: [ { name: '金融工程', count: 22 }, { name: '投资学', count: 19 }, { name: '保险学', count: 14 }, { name: '信用管理', count: 9 } ] },
      { name: '经济与贸易', skills: [ { name: '国际贸易', count: 16 }, { name: '国际商务', count: 13 }, { name: '跨境电商', count: 11 } ] }
    ]
  },
  {
    name: '法学',
    icon: '⚖️',
    directions: [
      { name: '法学类', skills: [ { name: '民商法', count: 16 }, { name: '刑法', count: 14 }, { name: '经济法', count: 12 }, { name: '国际法', count: 9 }, { name: '知识产权', count: 8 } ] },
      { name: '政治学类', skills: [ { name: '政治学理论', count: 10 }, { name: '国际政治', count: 8 }, { name: '行政管理', count: 15 } ] },
      { name: '社会学类', skills: [ { name: '社会学', count: 12 }, { name: '社会工作', count: 10 }, { name: '人类学', count: 6 } ] },
      { name: '马克思主义理论', skills: [ { name: '马克思主义原理', count: 9 }, { name: '思想政治教育', count: 14 }, { name: '中共党史', count: 7 } ] }
    ]
  },
  {
    name: '教育学',
    icon: '📖',
    directions: [
      { name: '教育学类', skills: [ { name: '教育学原理', count: 14 }, { name: '课程与教学论', count: 16 }, { name: '教育技术学', count: 12 }, { name: '学前教育', count: 11 }, { name: '特殊教育', count: 7 } ] },
      { name: '体育学类', skills: [ { name: '体育教育', count: 15 }, { name: '运动训练', count: 13 }, { name: '社会体育', count: 10 }, { name: '体育产业', count: 9 } ] }
    ]
  },
  {
    name: '文学',
    icon: '✍️',
    directions: [
      { name: '中国语言文学', skills: [ { name: '古代文学', count: 13 }, { name: '现当代文学', count: 15 }, { name: '语言学', count: 12 }, { name: '创意写作', count: 11 }, { name: '比较文学', count: 8 } ] },
      { name: '外国语言文学', skills: [ { name: '英语', count: 22 }, { name: '日语', count: 16 }, { name: '俄语', count: 9 }, { name: '法语', count: 8 }, { name: '翻译', count: 14 } ] },
      { name: '新闻传播学', skills: [ { name: '新闻学', count: 18 }, { name: '广播电视学', count: 15 }, { name: '广告学', count: 14 }, { name: '网络与新媒体', count: 21 }, { name: '传播学', count: 12 } ] }
    ]
  },
  {
    name: '历史学',
    icon: '🏛️',
    directions: [
      { name: '历史学类', skills: [ { name: '中国史', count: 12 }, { name: '世界史', count: 10 }, { name: '考古学', count: 7 }, { name: '文物与博物馆学', count: 8 }, { name: '文化遗产', count: 6 } ] }
    ]
  },
  {
    name: '理学',
    icon: '🔬',
    directions: [
      { name: '数学', skills: [ { name: '基础数学', count: 24 }, { name: '应用数学', count: 28 }, { name: '计算数学', count: 19 }, { name: '概率论与数理统计', count: 16 }, { name: '运筹学', count: 13 } ] },
      { name: '物理学', skills: [ { name: '理论物理', count: 16 }, { name: '凝聚态物理', count: 18 }, { name: '光学', count: 14 }, { name: '声学', count: 9 }, { name: '无线电物理', count: 11 } ] },
      { name: '化学', skills: [ { name: '无机化学', count: 17 }, { name: '有机化学', count: 19 }, { name: '分析化学', count: 16 }, { name: '物理化学', count: 14 }, { name: '高分子化学', count: 12 } ] },
      { name: '生物学', skills: [ { name: '植物学', count: 14 }, { name: '动物学', count: 13 }, { name: '微生物学', count: 15 }, { name: '生物化学', count: 16 }, { name: '分子生物学', count: 17 }, { name: '生态学', count: 12 } ] },
      { name: '地理学', skills: [ { name: '自然地理', count: 13 }, { name: '人文地理', count: 11 }, { name: '地理信息科学', count: 18 }, { name: '遥感科学与技术', count: 14 } ] },
      { name: '地质学', skills: [ { name: '古生物学', count: 7 }, { name: '构造地质学', count: 9 }, { name: '地球化学', count: 8 } ] }
    ]
  },
  {
    name: '工学',
    icon: '⚙️',
    directions: [
      { name: '电子信息类', skills: [ { name: '通信工程', count: 28 }, { name: '电子信息工程', count: 26 }, { name: '电子科学与技术', count: 23 }, { name: '微电子', count: 19 }, { name: '光电信息', count: 17 }, { name: '集成电路', count: 21 } ] },
      { name: '自动化类', skills: [ { name: '自动化', count: 25 }, { name: '机器人工程', count: 22 }, { name: '轨道交通信号', count: 13 }, { name: '智能科学与技术', count: 24 } ] },
      { name: '计算机类', skills: [ { name: '计算机科学与技术', count: 38 }, { name: '软件工程', count: 35 }, { name: '网络工程', count: 24 }, { name: '信息安全', count: 22 }, { name: '物联网工程', count: 26 }, { name: '数字媒体技术', count: 19 }, { name: '数据科学与大数据', count: 31 }, { name: '人工智能', count: 29 } ] },
      { name: '土木类', skills: [ { name: '土木工程', count: 27 }, { name: '建筑环境与能源', count: 16 }, { name: '给排水科学与工程', count: 14 }, { name: '道路桥梁', count: 18 }, { name: '智能建造', count: 15 } ] },
      { name: '机械类', skills: [ { name: '机械设计制造', count: 26 }, { name: '车辆工程', count: 22 }, { name: '工业设计', count: 17 }, { name: '过程装备', count: 13 }, { name: '智能制造工程', count: 21 } ] },
      { name: '化工与制药', skills: [ { name: '化学工程与工艺', count: 19 }, { name: '制药工程', count: 18 }, { name: '能源化学工程', count: 14 }, { name: '精细化工', count: 12 } ] },
      { name: '材料类', skills: [ { name: '材料科学与工程', count: 21 }, { name: '金属材料工程', count: 14 }, { name: '高分子材料', count: 17 }, { name: '复合材料', count: 11 }, { name: '新能源材料', count: 16 } ] },
      { name: '环境科学与工程', skills: [ { name: '环境工程', count: 20 }, { name: '环境科学', count: 16 }, { name: '环境生态工程', count: 13 }, { name: '资源循环科学', count: 10 } ] },
      { name: '生物医学工程', skills: [ { name: '生物医学工程', count: 15 }, { name: '医学影像技术', count: 13 }, { name: '康复工程', count: 9 } ] },
      { name: '食品科学与工程', skills: [ { name: '食品科学', count: 16 }, { name: '食品质量与安全', count: 14 }, { name: '粮食工程', count: 8 } ] },
      { name: '航空航天类', skills: [ { name: '飞行器设计', count: 12 }, { name: '航空航天工程', count: 14 }, { name: '飞行器动力', count: 11 }, { name: '无人机技术', count: 16 } ] }
    ]
  },
  {
    name: '农学',
    icon: '🌾',
    directions: [
      { name: '植物生产类', skills: [ { name: '农学', count: 15 }, { name: '园艺', count: 13 }, { name: '植物保护', count: 12 }, { name: '种子科学', count: 8 } ] },
      { name: '林学类', skills: [ { name: '林学', count: 12 }, { name: '森林保护', count: 9 }, { name: '园林', count: 16 }, { name: '水土保持', count: 7 } ] },
      { name: '动物生产类', skills: [ { name: '动物科学', count: 13 }, { name: '蚕学', count: 4 }, { name: '蜂学', count: 3 } ] },
      { name: '动物医学类', skills: [ { name: '动物医学', count: 14 }, { name: '动物药学', count: 7 }, { name: '动植物检疫', count: 8 } ] },
      { name: '水产类', skills: [ { name: '水产养殖学', count: 9 }, { name: '海洋渔业科学', count: 7 }, { name: '水族科学与技术', count: 5 } ] },
      { name: '草学类', skills: [ { name: '草业科学', count: 6 }, { name: '草坪管理', count: 4 } ] }
    ]
  },
  {
    name: '医学',
    icon: '⚕️',
    directions: [
      { name: '临床医学类', skills: [ { name: '临床医学', count: 22 }, { name: '麻醉学', count: 12 }, { name: '医学影像学', count: 15 }, { name: '眼视光医学', count: 8 }, { name: '精神医学', count: 7 }, { name: '放射医学', count: 6 } ] },
      { name: '口腔医学', skills: [ { name: '口腔医学', count: 14 }, { name: '口腔正畸', count: 8 } ] },
      { name: '公共卫生与预防医学', skills: [ { name: '预防医学', count: 16 }, { name: '流行病学', count: 12 }, { name: '卫生检验', count: 9 }, { name: '全球健康学', count: 5 } ] },
      { name: '中医学类', skills: [ { name: '中医学', count: 15 }, { name: '针灸推拿学', count: 13 }, { name: '中西医临床', count: 11 }, { name: '中药学', count: 14 } ] },
      { name: '药学类', skills: [ { name: '药学', count: 18 }, { name: '药物制剂', count: 12 }, { name: '临床药学', count: 14 }, { name: '药理学', count: 11 }, { name: '药物分析', count: 10 } ] },
      { name: '医学技术类', skills: [ { name: '医学检验技术', count: 14 }, { name: '康复治疗学', count: 13 }, { name: '听力与言语康复', count: 6 } ] },
      { name: '护理学类', skills: [ { name: '护理学', count: 19 }, { name: '助产学', count: 8 } ] }
    ]
  },
  {
    name: '管理学',
    icon: '📊',
    directions: [
      { name: '管理科学与工程', skills: [ { name: '信息管理与信息系统', count: 21 }, { name: '工程管理', count: 18 }, { name: '大数据管理与应用', count: 23 }, { name: '工程造价', count: 16 } ] },
      { name: '工商管理类', skills: [ { name: '工商管理', count: 24 }, { name: '市场营销', count: 22 }, { name: '会计学', count: 28 }, { name: '财务管理', count: 25 }, { name: '人力资源管理', count: 19 }, { name: '审计学', count: 21 }, { name: '资产评估', count: 12 } ] },
      { name: '农业经济管理', skills: [ { name: '农林经济管理', count: 11 }, { name: '农村区域发展', count: 7 } ] },
      { name: '公共管理类', skills: [ { name: '行政管理', count: 18 }, { name: '公共事业管理', count: 15 }, { name: '劳动与社会保障', count: 12 }, { name: '土地资源管理', count: 11 }, { name: '城市管理', count: 10 } ] },
      { name: '图书情报与档案', skills: [ { name: '图书馆学', count: 7 }, { name: '档案学', count: 6 }, { name: '信息资源管理', count: 9 } ] },
      { name: '物流管理与工程', skills: [ { name: '物流管理', count: 17 }, { name: '物流工程', count: 14 }, { name: '供应链管理', count: 19 } ] },
      { name: '电子商务类', skills: [ { name: '电子商务', count: 21 }, { name: '跨境电商', count: 16 } ] },
      { name: '旅游管理类', skills: [ { name: '旅游管理', count: 15 }, { name: '酒店管理', count: 12 }, { name: '会展经济与管理', count: 9 } ] }
    ]
  },
  {
    name: '艺术学',
    icon: '🎨',
    directions: [
      { name: '艺术学理论', skills: [ { name: '艺术史论', count: 8 }, { name: '艺术管理', count: 10 } ] },
      { name: '音乐与舞蹈学', skills: [ { name: '音乐表演', count: 16 }, { name: '音乐学', count: 14 }, { name: '作曲与作曲技术', count: 7 }, { name: '舞蹈表演', count: 12 }, { name: '舞蹈编导', count: 8 } ] },
      { name: '戏剧与影视学', skills: [ { name: '表演', count: 11 }, { name: '戏剧影视文学', count: 10 }, { name: '广播电视编导', count: 15 }, { name: '播音与主持', count: 14 }, { name: '动画', count: 16 }, { name: '影视摄影与制作', count: 12 } ] },
      { name: '美术学', skills: [ { name: '绘画', count: 15 }, { name: '雕塑', count: 7 }, { name: '美术学', count: 12 }, { name: '书法学', count: 9 }, { name: '中国画', count: 10 } ] },
      { name: '设计学', skills: [ { name: '视觉传达设计', count: 24 }, { name: '环境设计', count: 22 }, { name: '产品设计', count: 20 }, { name: '服装与服饰设计', count: 16 }, { name: '数字媒体艺术', count: 23 }, { name: '艺术与科技', count: 14 }, { name: '陶瓷艺术设计', count: 7 } ] }
    ]
  },
  {
    name: '交叉学科',
    icon: '🔄',
    directions: [
      { name: '集成电路科学与工程', skills: [ { name: '芯片设计', count: 14 }, { name: '工艺集成', count: 9 } ] },
      { name: '国家安全学', skills: [ { name: '国家安全战略', count: 6 }, { name: '网络安全', count: 15 } ] },
      { name: '设计学', skills: [ { name: '智能交互设计', count: 12 }, { name: '服务设计', count: 8 } ] },
      { name: '遥感科学与技术', skills: [ { name: '遥感应用', count: 11 }, { name: '地理信息工程', count: 13 } ] }
    ]
  }
])

// 🌟 核心修改 1：定义竞赛容器和定时器
const contestCards = ref([])
const loadingContests = ref(false)
let contestPollTimer = null // 新增：用于存储定时器

// 🌟 核心修改 2：向后端内存中转站获取实时比赛数据
const fetchRealContests = async () => {
  // 如果是定时器后台刷新，就不显示 loading，避免页面一直闪烁
  if (contestCards.value.length === 0) {
    loadingContests.value = true
  }
  
  try {
    // 请求 ContentController 里的 /api/content/list
    const res = await request.get('/api/content/list', {
      params: { 
        category: '竞赛信息大厅'
      }
    })
    
    // 兼容后端 Result 的返回结构
    let rawList = []
    if (res && res.data && res.data.content) {
      rawList = res.data.content
    } else if (res && res.data) {
      rawList = res.data
    } else if (Array.isArray(res)) {
      rawList = res
    }

    // 🌟 核心映射：直接读取 Python 传来的精细化字段
    contestCards.value = rawList.map(item => {
      // 动态判断状态颜色
      const colorMap = { '报名中': '#10b981', '即将开始': '#f97316', '备赛中': '#8b5cf6', '预报名': '#3b82f6' }
      
      return {
        id: item.title + Math.random(), // 随机ID，防止重复
        name: item.title || '未命名赛事', 
        officialUrl: item.officialUrl || '', // 接收真实官网链接或百度兜底链接
        level: item.level || '国家级', 
        status: item.status || '报名中',
        statusColor: colorMap[item.status] || '#10b981',
        organize: item.authorName || '官方组委会', 
        startDate: '2026-05-01',
        endDate: '2026-06-01',
        tags: [item.major, item.direction].filter(Boolean),
        skills: ['基础理论', '实践操作'],
        
        // 🚨 完美匹配：直接使用 Python 生成的分类，前端筛选器会自动识别！
        major: item.major,
        direction: item.direction,
        skill: item.skill 
      }
    })
    
  } catch (error) {
    console.error('获取实时竞赛数据失败:', error)
  } finally {
    loadingContests.value = false
  }
}

// 🌟 核心修改 3：页面加载时启动自动刷新
onMounted(() => {
  fetchRealContests()
  
  // 每 8 秒从后端内存拉取一次最新数据（和 Python 频率保持一致）
  contestPollTimer = setInterval(() => {
    fetchRealContests()
  }, 8000)
})

// 🌟 离开页面时记得销毁定时器，节省性能
onUnmounted(() => {
  if (contestPollTimer) {
    clearInterval(contestPollTimer)
  }
})

const selectedMajor = ref('工学')
const selectedDirection = ref('计算机类')
const selectedSkill = ref('全部')
const contestKeyword = ref('')

const contestActiveTab = ref('info') 
const contestArticles = ref([])
const loadingArticles = ref(false)
const guideFilterType = ref('all') 

const fetchContestArticles = async () => {
  loadingArticles.value = true
  try {
    const contentRes = await fetchContentPage(CATEGORY_MAP.CONTEST, 0, 50)
    let videoList = []
    
    // 兼容 Result 结构解析列表
    const actualList = (contentRes && contentRes.list) ? contentRes.list : []
    
    if (actualList.length > 0) {
      videoList = actualList.map(item => {
        let coverUrl = '';
        if (item.type === 'video' && item.videoUrl && item.videoUrl.includes('aliyuncs.com')) {
           coverUrl = `${item.videoUrl}?x-oss-process=video/snapshot,t_3000,f_jpg,w_600,h_400,m_fast`;
        }
        return {
          id: 'v_' + item.id, 
          title: item.title,
          type: item.type || 'video',
          topic: item.category || '赛事指南',
          content: item.authorName || '官方精选视频',
          createTime: item.gmtCreated || item.createTime || new Date().toISOString(),
          videoUrl: item.videoUrl,
          imageUrls: coverUrl 
        }
      })
    }
    
    const articleRes = await request.get('/api/articles')
    let articleList = []
    let rawArticles = []
    if (Array.isArray(articleRes)) rawArticles = articleRes
    else if (Array.isArray(articleRes.data)) rawArticles = articleRes.data

    if (rawArticles.length > 0) {
      articleList = rawArticles.map(item => {
        let coverUrl = ''
        if (item.imageUrls) coverUrl = item.imageUrls.split(',')[0].trim()
        
        return {
          id: 'a_' + item.id, 
          title: item.title,
          type: 'article', 
          topic: item.topic || '赛事指南',
          content: item.content || '暂无内容',
          createTime: item.createTime || new Date().toISOString(),
          videoUrl: null,
          imageUrls: coverUrl 
        }
      })
    }

    contestArticles.value = [...videoList, ...articleList]

  } catch (error) {
    console.error('获取竞赛指导数据失败:', error)
  } finally {
    loadingArticles.value = false
  }
}

const filteredContestArticles = computed(() => {
  if (guideFilterType.value === 'all') {
    return contestArticles.value;
  }
  return contestArticles.value.filter(article => {
    const type = article.type || 'article'; 
    return type === guideFilterType.value;
  });
})

const getSnippet = (content) => {
  if (!content) return '暂无内容简介'
  return content.replace(/<[^>]+>/g, '').substring(0, 60) + '...'
}

const formatDateStr = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

const articleLoading = ref(false)
const currentArticleData = ref(null)
const previewUrl = ref(null)

const fetchArticleDetail = async (id) => {
  articleLoading.value = true
  try {
    const res = await request.get(`/api/articles/${id}`)
    currentArticleData.value = res.data || res
  } catch (error) {
    console.error('获取文章详情失败:', error)
  } finally {
    articleLoading.value = false
  }
}

const goToArticleDetail = (article) => {
  if (article.type === 'video') {
    router.push({
      path: '/video',
      query: {
        title: article.title,
        author: article.topic || '官方收录',
        url: article.videoUrl
      }
    })
  } else {
    activeMenu.value = 'articleDetail'
    fetchArticleDetail(article.id)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const goBackToContest = () => {
  activeMenu.value = 'contest'
  contestActiveTab.value = 'guide' 
  currentArticleData.value = null
}

const parsedImages = computed(() => {
  if (!currentArticleData.value || !currentArticleData.value.imageUrls) return []
  return currentArticleData.value.imageUrls.split(',').map(url => url.trim()).filter(url => url)
})

const previewImage = (url) => {
  previewUrl.value = url
}

const currentDirections = computed(() => {
  const major = majorCategories.value.find(m => m.name === selectedMajor.value)
  return major?.directions || []
})

const currentSkills = computed(() => {
  const major = majorCategories.value.find(m => m.name === selectedMajor.value)
  const direction = major?.directions.find(d => d.name === selectedDirection.value)
  return direction?.skills || []
})

const filteredContests = computed(() => {
  let filtered = contestCards.value.filter(card => 
    card.major === selectedMajor.value &&
    card.direction === selectedDirection.value
  )
  if (selectedSkill.value !== '全部') {
    filtered = filtered.filter(card => card.skill === selectedSkill.value)
  }
  return filtered
})

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getMonth()+1}.${date.getDate()}`
}

const getRemainingDays = (endDateStr) => {
  const end = new Date(endDateStr)
  const now = new Date()
  const diff = Math.ceil((end - now) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 0
}

const handleSearch = () => {
  console.log('搜索赛事:', contestKeyword.value)
}

const goToContestDetail = (contest) => {
  const url = contest.officialUrl;
  if (url) {
    window.open(url, '_blank');
  } else {
    const searchUrl = 'https://www.baidu.com/s?wd=' + encodeURIComponent(contest.name);
    window.open(searchUrl, '_blank');
  }
}

watch(activeMenu, (newVal) => {
  if (newVal === 'contest') {
    fetchContestArticles()
  }
})

watch(contestActiveTab, (newVal) => {
  if (newVal === 'guide') {
    fetchContestArticles()
  }
})

// ================= 🌟 全新升级：内嵌简历中心与 OSS 对接 =================
const dbTemplates = ref([]) 
const excellentResumes = ref([]) 
const hoveredPreviewUrl = ref(null) 
const hoverId = ref(null) // 用于追踪鼠标悬浮于哪个真实模板以便弹出遮罩按钮

// 核心：数据获取兼容 Result 结构
const fetchTemplatesData = async () => {
  try {
    const res1 = await request.get('/api/templates/list', { params: { type: 1 } });
    // 后端返回 Result<List>，数据在 res1.data 中
    dbTemplates.value = res1.data || [];
    
    if (dbTemplates.value.length > 0) {
      selectedAiTemplate.value = dbTemplates.value[0];
    }

    const res2 = await request.get('/api/templates/list', { params: { type: 2 } });
    excellentResumes.value = res2.data || [];
  } catch (error) {
    console.error("加载简历数据失败，请确保后端服务已启动且接口路径正确");
  }
}
  // 原页面跳转
const goToTemplateDetail = (id) => {
  // 直接使用 router.push 在当前窗口跳转，不开启新标签
  router.push(`/template-detail/${id}`);
}

// 🌟 核心：安全图片渲染，解决缩略图不显示问题
const getSafeImageUrl = (url) => {
  if (!url) return 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?w=400';
  
  const lowerUrl = String(url).toLowerCase();
  // 如果后端传过来的是 .docx 下载链接，前端无法作为图片渲染，显示一个高颜值的占位图
  if (lowerUrl.includes('.doc') || lowerUrl.includes('.docx')) {
    return 'https://images.unsplash.com/photo-1612441804231-77a36b284856?w=400';
  }
  return url;
}

// 🌟 核心：调用微软官方引擎预览真实的 DOCX 原件
const previewRealDoc = (fileUrl) => {
  if (!fileUrl) {
    alert('该模板暂无源文件，无法预览');
    return;
  }
  // 使用微软官方在线预览，解决浏览器无法直接打开 Word 的问题
  const officeUrl = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(fileUrl)}`;
  // 这里可以根据需求决定是在当前页面跳转还是如何，通常预览建议开启新窗口但不作为"挑选页面"跳转
  window.open(officeUrl, '_blank');
}

// 真实的下载逻辑
const handleDownload = (item) => {
  if(item.fileUrl) {
    window.location.href = item.fileUrl; // 直接触发浏览器下载真实的 OSS 文件
  } else {
    alert('未找到源文件链接');
  }
}
</script>

<style scoped>
/* ================= 原有基础样式 ================= */
.layout { 
  display: flex; flex-direction: column; height: 100vh; width: 100vw; 
  background-image: url('/images11/photo-1603252199513-79a959321f00.avif');
  background-size: cover; background-position: center; background-attachment: fixed;
  position: relative;
}
.layout::before {
  content: ''; position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255, 255, 255, 0.85); 
  z-index: 0; pointer-events: none;
}
.layout > * { position: relative; z-index: 1; }
.header { height: 60px; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); z-index: 10; }
.logo { font-size: 20px; font-weight: bold; color: #4a90e2; }
.search-bar input { width: 300px; padding: 8px 15px; border-radius: 20px; border: 1px solid #ddd; outline: none; background: #f8fafc; }
.header-right { display: flex; align-items: center; gap: 20px; }
.go-publish-btn { background: #00aeec; color: white; border: none; padding: 8px 18px; border-radius: 20px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 6px; transition: all 0.3s; box-shadow: 0 4px 10px rgba(0, 174, 236, 0.3); }
.go-publish-btn:hover { background: #00a1d6; transform: translateY(-2px); box-shadow: 0 6px 15px rgba(0, 174, 236, 0.4); }

.post-offer-btn { background: #f97316; color: white; border: none; padding: 8px 18px; border-radius: 20px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 6px; transition: all 0.3s; box-shadow: 0 4px 10px rgba(249, 115, 22, 0.3); }
.post-offer-btn:hover { background: #ea580c; transform: translateY(-2px); box-shadow: 0 6px 15px rgba(249, 115, 22, 0.4); }
.post-offer-btn .icon { font-size: 16px; }

@media (max-width: 768px) {
  .post-offer-btn .icon { margin-right: 0; }
  .post-offer-btn span:not(.icon) { display: none; }
  .post-offer-btn { padding: 8px 12px; }
}

.user-profile { position: relative; display: flex; align-items: center; gap: 15px; cursor: pointer; height: 100%; padding: 0 10px; }
.role-badge { background: #e6f0fa; color: #4a90e2; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; z-index: 101; }
.avatar { width: 36px; height: 36px; border-radius: 50%; border: 2px solid #e2e8f0; overflow: hidden; display: flex; align-items: center; justify-content: center; background: #f8fafc; transition: 0.3s; position: relative; z-index: 101; }
.user-profile:hover .avatar { border-color: #4a90e2; box-shadow: 0 0 8px rgba(74, 144, 226, 0.3); }
.avatar img { width: 100%; height: 100%; object-fit: cover; }

.dropdown-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 98; cursor: default; }
.dropdown-menu { position: absolute; top: 60px; right: 10px; width: 160px; background: #fff; border-radius: 8px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); z-index: 100; overflow: hidden; animation: slideDown 0.2s ease-out; border: 1px solid #f1f5f9; }
@keyframes slideDown { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.dropdown-header { padding: 15px; text-align: center; background: #f8fafc; display: flex; flex-direction: column; gap: 4px; align-items: center; }
.username { font-size: 15px; font-weight: bold; color: #1e293b; }
.user-account { font-size: 12px; color: #94a3b8; word-break: break-all; } 
.user-org { font-size: 12px; color: #4a90e2; background: #e6f0fa; padding: 4px 10px; border-radius: 12px; margin-top: 6px; display: inline-block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 90%; font-weight: 500; }
.divider { height: 1px; background: #f1f5f9; margin: 0; }
.dropdown-list { list-style: none; padding: 0; margin: 0; }
.dropdown-item { padding: 12px 15px; display: flex; align-items: center; gap: 10px; font-size: 14px; color: #475569; transition: all 0.2s; }
.dropdown-item i { font-style: normal; font-size: 15px; opacity: 0.8; }
.dropdown-item:hover { background: #f0f7ff; color: #4a90e2; padding-left: 18px; }
.dropdown-item:hover i { opacity: 1; }

.main-container { display: flex; flex: 1; overflow: hidden; }
.sidebar { width: 220px; background: #fff; box-shadow: 2px 0 5px rgba(0,0,0,0.02); display: flex; flex-direction: column; }
.sidebar ul { list-style: none; padding: 20px 0; margin: 0; flex: 1; overflow-y: auto; }
.sidebar li { padding: 15px 30px; cursor: pointer; color: #666; transition: 0.2s; border-left: 3px solid transparent; }
.sidebar li:hover { background: #f0f7ff; color: #4a90e2; }
.sidebar li.active { background: #e6f0fa; color: #4a90e2; border-left: 3px solid #4a90e2; font-weight: bold; }
.content { flex: 1; padding: 30px; overflow-y: auto; display: flex; flex-direction: column; }
.card-layout { background: #fff; padding: 25px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); min-height: 400px; }
.card-layout h3 { margin-top: 0; color: #333; font-size: 20px; margin-bottom: 5px; }
.subtitle { color: #888; font-size: 14px; margin-bottom: 25px; border-bottom: 2px solid #f0f2f5; padding-bottom: 15px; }

/* 搜索栏与热搜样式 */
.search-container { position: relative; width: 100%; max-width: 500px; margin: 0 auto 20px; }
.search-bar { display: flex; align-items: center; background: white; border-radius: 40px; padding: 8px 16px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); border: 1px solid #e2e8f0; }
.search-icon { font-size: 18px; color: #94a3b8; margin-right: 10px; }
.search-bar input { flex: 1; border: none; outline: none; font-size: 15px; padding: 8px 0; background: transparent; }
.search-bar input::placeholder { color: #94a3b8; }
.hot-search-dropdown { position: absolute; top: 60px; left: 0; right: 0; background: white; border-radius: 20px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15); z-index: 1000; overflow: hidden; animation: slideDown 0.2s ease; }
.hot-search-header { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; border-bottom: 1px solid #f1f5f9; }
.hot-title { font-weight: 600; color: #1e293b; font-size: 16px; }
.update-time { font-size: 12px; color: #94a3b8; }
.hot-search-list { padding: 8px 0; }
.hot-search-item { display: flex; align-items: center; padding: 10px 20px; cursor: pointer; transition: background 0.2s; }
.hot-search-item:hover { background: #f8fafc; }
.rank-number { width: 30px; font-size: 16px; font-weight: 500; color: #94a3b8; }
.rank-number.top-three { color: #f43f5e; font-weight: 700; }
.hot-icon { margin-right: 8px; font-size: 16px; }
.hot-text { flex: 1; font-size: 15px; color: #1e293b; }
.hot-count { font-size: 13px; color: #94a3b8; }
.hot-count.top-three-count { color: #f43f5e; font-weight: 600; }
.hot-search-footer { padding: 12px 20px; border-top: 1px solid #f1f5f9; text-align: center; color: #4aa3d1; font-size: 13px; cursor: pointer; transition: background 0.2s; }
.hot-search-footer:hover { background: #f8fafc; }

/* 🌟 全局搜索结果样式 */
.search-layout { display: flex; flex-direction: column; gap: 20px; }
.search-keyword-display { font-size: 14px; color: #64748b; margin: 0; }
.search-keyword-display .highlight { color: #ef4444; font-weight: bold; }

/* 轮播图样式 */
.carousel-container { position: relative; width: 100%; height: 400px; overflow: hidden; border-radius: 16px; margin-bottom: 30px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15); }
.carousel-wrapper { display: flex; width: 100%; height: 100%; transition: transform 0.5s ease-in-out; }
.carousel-slide { flex: 0 0 100%; height: 100%; position: relative; }
.carousel-image { width: 100%; height: 100%; object-fit: cover; }
.carousel-content { position: absolute; bottom: 60px; left: 50px; color: white; text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3); z-index: 2; max-width: 60%; }
.carousel-content h2 { font-size: 32px; font-weight: 700; margin-bottom: 10px; line-height: 1.2; }
.carousel-content p { font-size: 16px; margin-bottom: 20px; opacity: 0.9; }
.carousel-btn { padding: 12px 30px; background: linear-gradient(135deg, #4aa3d1, #6cc29a); color: white; border: none; border-radius: 40px; font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.3s; border: 1px solid rgba(255, 255, 255, 0.2); }
.carousel-btn:hover { transform: translateY(-2px); box-shadow: 0 10px 25px rgba(74, 163, 209, 0.4); }
.carousel-slide::after { content: ''; position: absolute; bottom: 0; left: 0; width: 100%; height: 50%; background: linear-gradient(to top, rgba(0,0,0,0.5), transparent); z-index: 1; }
.carousel-dots { position: absolute; bottom: 20px; left: 50%; transform: translateX(-50%); display: flex; gap: 10px; z-index: 3; }
.dot { width: 10px; height: 10px; border-radius: 50%; background: rgba(255, 255, 255, 0.5); cursor: pointer; transition: all 0.3s; }
.dot.active { width: 30px; border-radius: 10px; background: white; }
.carousel-arrow { position: absolute; top: 50%; transform: translateY(-50%); width: 40px; height: 40px; border-radius: 50%; background: rgba(255, 255, 255, 0.3); border: none; color: white; font-size: 24px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s; z-index: 3; backdrop-filter: blur(5px); border: 1px solid rgba(255, 255, 255, 0.2); }
.carousel-arrow:hover { background: rgba(255, 255, 255, 0.5); transform: translateY(-50%) scale(1.1); }
.carousel-arrow.prev { left: 20px; }
.carousel-arrow.next { right: 20px; }

/* 首页分块与卡片公共样式 */
.home-sections { display: flex; flex-direction: column; gap: 35px; }
.section-title-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.section-title-bar h3 { margin: 0; font-size: 22px; color: #1e293b; display: flex; align-items: center; gap: 8px; }

.title-left { display: flex; align-items: center; gap: 16px; }
.type-selector { display: flex; background: #f1f5f9; border-radius: 20px; padding: 3px; border: 1px solid #e2e8f0; }
.type-selector span { padding: 4px 16px; font-size: 13px; color: #64748b; cursor: pointer; border-radius: 16px; transition: all 0.3s ease; font-weight: 500; }
.type-selector span:hover:not(.active) { color: #4aa3d1; }
.type-selector span.active { background: #fff; color: #4aa3d1; font-weight: bold; box-shadow: 0 2px 6px rgba(0,0,0,0.08); }
.guide-filter-container { display: flex; margin-bottom: 16px; }

.more-link { color: #4a90e2; font-size: 14px; cursor: pointer; transition: 0.2s; background: #f0f7ff; padding: 4px 12px; border-radius: 20px; }
.more-link:hover { color: white; background: #4a90e2; }

.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.card { background: #fff; border-radius: 8px; overflow: hidden; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; border: 1px solid #e2e8f0; }
.card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.08); }
.card-cover { height: 140px; position: relative; display: flex; justify-content: flex-end; align-items: flex-end; padding: 10px; }

/* 视频播放按钮样式 */
.play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 36px;
  color: rgba(255, 255, 255, 0.85);
  text-shadow: 0 4px 10px rgba(0,0,0,0.5);
  pointer-events: none;
  transition: all 0.3s ease;
}
.card:hover .play-overlay,
.article-card:hover .play-overlay {
  transform: translate(-50%, -50%) scale(1.2);
  color: white;
}

.type-badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; color: white; font-weight: bold; }
.type-badge.video { background: rgba(239, 68, 68, 0.9); } 
.type-badge.article { background: rgba(59, 130, 246, 0.9); } 
.type-badge.template { background: rgba(16, 185, 129, 0.9); } 
.card-info { padding: 15px; }
.card-title { margin: 0 0 8px 0; font-size: 15px; color: #1e293b; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; height: 42px; }
.card-desc { margin: 0; font-size: 13px; color: #94a3b8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* 产教融合招聘样式 */
.recruitment-section { background: white; border-radius: 24px; padding: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.section-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px;}
.category-tabs { display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }
.category-tab { display: flex; align-items: center; gap: 8px; padding: 8px 20px; background: #f8fafc; border-radius: 40px; cursor: pointer; transition: all 0.3s; border: 1px solid #e2e8f0; }
.category-tab.active { background: #4aa3d1; border-color: #4aa3d1; color: white; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(74, 163, 209, 0.3); }
.cat-icon { font-size: 18px; }
.subcategory-tabs { display: flex; gap: 8px; margin-bottom: 20px; border-bottom: 2px solid #f1f5f9; padding-bottom: 12px; }
.subcategory-tab { padding: 6px 16px; cursor: pointer; color: #64748b; transition: all 0.3s; border-radius: 30px; font-size: 14px; }
.subcategory-tab:hover { color: #4aa3d1; background: #f0f9ff; }
.subcategory-tab.active { color: #4aa3d1; font-weight: 600; background: #e0f2fe; }
.job-tags { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 24px; }
.job-tag { display: flex; align-items: center; gap: 6px; padding: 6px 16px; background: #f8fafc; border-radius: 30px; cursor: pointer; transition: all 0.2s; border: 1px solid #e2e8f0; font-size: 13px; }
.job-tag.active { background: #4aa3d1; border-color: #4aa3d1; color: white; }
.job-tag.hot { border-color: #f97316; background: #fff7ed; }
.job-tag.active.hot { background: #f97316; border-color: #f97316; color: white; }
.job-count { font-size: 11px; color: #64748b; }
.active .job-count { color: rgba(255, 255, 255, 0.8); }
.hot-badge { font-size: 11px; }

.job-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; margin-top: 20px; }
.job-card { background: #f8fafc; border-radius: 16px; padding: 16px; transition: all 0.3s; cursor: pointer; border: 1px solid transparent; }
.job-card:hover { background: white; border-color: #e2e8f0; transform: translateY(-2px); box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05); }
.job-card-header { display: flex; gap: 12px; margin-bottom: 12px; }
.company-logo { width: 48px; height: 48px; border-radius: 12px; background: linear-gradient(135deg, #4aa3d1, #6cc29a); display: flex; align-items: center; justify-content: center; font-size: 20px; color: white; font-weight: 600; }
.job-info { flex: 1; }
.job-title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.job-title-row h4 { font-size: 16px; font-weight: 600; color: #1e293b; margin: 0; }
.hot-tag { font-size: 11px; padding: 2px 8px; background: #fef2f2; color: #ef4444; border-radius: 30px; }
.company-name { font-size: 13px; color: #64748b; }
.job-salary { font-size: 16px; font-weight: 600; color: #4aa3d1; }
.job-tags-list { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }
.job-tags-list .tag { padding: 4px 10px; background: #e2e8f0; border-radius: 30px; font-size: 11px; color: #334155; }
.job-footer { display: flex; align-items: center; justify-content: space-between; font-size: 12px; color: #64748b; border-top: 1px solid #e2e8f0; padding-top: 12px; }
.job-arrow { color: #4aa3d1; opacity: 0.5; transition: all 0.3s; }
.job-card:hover .job-arrow { opacity: 1; transform: translateX(5px); }

/* 竞赛信息大厅样式 */
.contest-section { background: white; border-radius: 24px; padding: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.contest-header { margin-bottom: 20px; }
.contest-header h3 { font-size: 20px; font-weight: 600; color: #1e293b; margin: 0 0 4px 0; }
.contest-tabs { display: flex; gap: 24px; margin-top: 15px; border-bottom: 2px solid #f1f5f9; }
.contest-tab { padding: 10px 16px; font-size: 16px; color: #64748b; cursor: pointer; position: relative; transition: all 0.3s; font-weight: 500; display: flex; align-items: center; gap: 6px; }
.contest-tab:hover { color: #4aa3d1; background: #f8fafc; border-radius: 8px 8px 0 0; }
.contest-tab.active { color: #4aa3d1; font-weight: 600; }
.contest-tab.active::after { content: ''; position: absolute; bottom: -2px; left: 0; width: 100%; height: 3px; background: #4aa3d1; border-radius: 3px 3px 0 0; }

.contest-search { margin-bottom: 24px; margin-top: 15px; }
.search-box { display: flex; align-items: center; background: #f8fafc; border-radius: 40px; padding: 4px 4px 4px 16px; border: 1px solid #e2e8f0; }
.search-icon { color: #94a3b8; margin-right: 8px; }
.search-box input { flex: 1; border: none; outline: none; background: transparent; padding: 12px 0; font-size: 15px; }
.search-btn { padding: 8px 24px; background: #4aa3d1; color: white; border: none; border-radius: 40px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.3s; }
.search-btn:hover { background: #1e6f9f; }

.major-tabs { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; border-bottom: 2px solid #f1f5f9; padding-bottom: 15px; }
.major-tab { display: flex; align-items: center; gap: 6px; padding: 8px 16px; background: #f8fafc; border-radius: 30px; cursor: pointer; transition: all 0.2s; border: 1px solid #e2e8f0; }
.major-tab.active { background: #4aa3d1; border-color: #4aa3d1; color: white; }
.major-icon { font-size: 18px; }
.direction-tabs { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 20px; }
.direction-tab { padding: 6px 16px; cursor: pointer; color: #64748b; border-radius: 30px; transition: all 0.2s; font-size: 14px; }
.direction-tab:hover { color: #4aa3d1; background: #f0f9ff; }
.direction-tab.active { color: #4aa3d1; font-weight: 600; background: #e0f2fe; }
.skill-tags { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }
.skill-tag { display: flex; align-items: center; gap: 6px; padding: 6px 16px; background: #f8fafc; border-radius: 30px; cursor: pointer; border: 1px solid #e2e8f0; font-size: 13px; transition: all 0.2s; }
.skill-tag.active { background: #4aa3d1; border-color: #4aa3d1; color: white; }
.skill-count { font-size: 11px; color: #64748b; background: #e2e8f0; padding: 2px 6px; border-radius: 30px; margin-left: 4px; }
.active .skill-count { background: rgba(255, 255, 255, 0.2); color: white; }

.result-info { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.result-badge { padding: 4px 16px; background: #e0f2fe; color: #0369a1; border-radius: 30px; font-size: 14px; font-weight: 500; }
.result-count { font-size: 13px; color: #64748b; }

.contest-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; }
.contest-card { background: #f8fafc; border-radius: 20px; padding: 20px; transition: all 0.3s ease; cursor: pointer; border: 1px solid transparent; display: flex; flex-direction: column; }
.contest-card:hover { background: white; border-color: #e2e8f0; transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0, 0, 0, 0.05); }
.card-header { display: flex; justify-content: space-between; margin-bottom: 12px; }
.contest-level { padding: 4px 12px; border-radius: 30px; font-size: 11px; font-weight: 500; }
.contest-status { padding: 4px 12px; border-radius: 30px; font-size: 11px; font-weight: 500; }
.contest-title { font-size: 16px; font-weight: 600; color: #1e293b; margin: 0 0 8px 0; line-height: 1.4; }
.contest-organizer { font-size: 13px; color: #475569; margin-bottom: 12px; }
.contest-time { display: flex; flex-direction: column; gap: 4px; margin-bottom: 12px; font-size: 12px; color: #475569; }
.time-remain { font-size: 12px; color: #f97316; font-weight: 500; }
.contest-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }
.contest-tags .tag { padding: 4px 10px; background: #e2e8f0; border-radius: 30px; font-size: 11px; color: #334155; }
.skills-required { margin: 12px 0; padding: 10px; background: #f0f9ff; border-radius: 12px; border-left: 3px solid #4aa3d1; }
.skills-title { font-size: 12px; color: #0369a1; margin-bottom: 8px; font-weight: 500; }
.skill-items { display: flex; flex-wrap: wrap; gap: 6px; }
.skill-item { padding: 4px 10px; background: white; border-radius: 20px; font-size: 11px; color: #334155; border: 1px solid #bae6fd; }
.card-footer { margin-top: auto; text-align: right; }
.card-arrow { font-size: 18px; color: #4aa3d1; opacity: 0.5; transition: all 0.3s; }
.contest-card:hover .card-arrow { opacity: 1; transform: translateX(5px); }
.empty-state { text-align: center; padding: 60px 20px; background: #f8fafc; border-radius: 20px; }
.empty-icon { font-size: 48px; margin-bottom: 16px; opacity: 0.5; }

/* 竞赛指导文章列表样式 */
.article-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; margin-top: 10px; }
.article-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; cursor: pointer; transition: all 0.3s; display: flex; flex-direction: column; }
.article-card:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0,0,0,0.08); border-color: #cbd5e1; }
.article-cover { height: 150px; background: #f8fafc; overflow: hidden; display: flex; align-items: center; justify-content: center; position: relative; }
.article-cover img { width: 100%; height: 100%; object-fit: cover; }
.default-cover { background: linear-gradient(135deg, #e0f2fe, #bae6fd); font-size: 40px; }
.card-corner-badge { position: absolute; top: 10px; left: 10px; z-index: 2; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.article-info { padding: 16px; flex: 1; display: flex; flex-direction: column; }
.article-topic { font-size: 12px; color: #0369a1; background: #e0f2fe; padding: 2px 8px; border-radius: 4px; align-self: flex-start; margin-bottom: 8px; }
.article-title { font-size: 16px; color: #1e293b; margin: 0 0 8px 0; line-height: 1.4; font-weight: 600; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.article-desc { font-size: 13px; color: #64748b; margin: 0 0 12px 0; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; flex: 1; }
.article-footer { font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; padding-top: 12px; display: flex; justify-content: flex-end; }

/* 内嵌文章详情专属样式 */
.article-layout { background: #fff; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); overflow: hidden; min-height: 500px; }
.article-header { height: 60px; background: #f8fafc; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; }
.back-btn { font-size: 14px; color: #64748b; cursor: pointer; display: flex; align-items: center; gap: 8px; transition: color 0.3s; font-weight: 500; }
.back-btn:hover { color: #4aa3d1; }
.header-actions { display: flex; gap: 12px; }
.action-btn { background: white; border: 1px solid #e2e8f0; padding: 6px 14px; border-radius: 20px; color: #475569; font-size: 13px; cursor: pointer; transition: all 0.3s; }
.action-btn:hover { background: #f1f5f9; color: #1e293b; border-color: #cbd5e1; }
.article-main { display: flex; padding: 30px; gap: 30px; }
.article-container { flex: 1; min-width: 0; }
.article-title-area { margin-bottom: 24px; border-bottom: 1px solid #f1f5f9; padding-bottom: 20px; }
.topic-badge { display: inline-block; background: #e0f2fe; color: #0369a1; padding: 4px 12px; border-radius: 4px; font-size: 12px; font-weight: 600; margin-bottom: 12px; }
.title { font-size: 26px; color: #1e293b; line-height: 1.4; margin: 0 0 16px 0; font-weight: 700; }
.meta-info { display: flex; align-items: center; gap: 10px; color: #94a3b8; font-size: 13px; }
.dot { font-weight: bold; }
.article-content { font-size: 15px; color: #334155; line-height: 1.8; }
.hero-image { margin-bottom: 24px; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.hero-image img { width: 100%; max-height: 400px; object-fit: cover; display: block; }
.text-body { white-space: pre-wrap; text-align: justify; margin-bottom: 30px; }
.image-gallery { background: #f8fafc; padding: 20px; border-radius: 12px; margin-top: 30px; border: 1px solid #f1f5f9; }
.gallery-title { font-size: 15px; color: #1e293b; margin: 0 0 15px 0; }
.gallery-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; }
.gallery-img { width: 100%; height: 120px; object-fit: cover; border-radius: 8px; cursor: pointer; transition: transform 0.3s; border: 1px solid #e2e8f0; }
.gallery-img:hover { transform: scale(1.03); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.gallery-tip { text-align: center; color: #94a3b8; font-size: 12px; margin: 12px 0 0 0; }
.article-sidebar { width: 280px; flex-shrink: 0; }
.sidebar-box { background: #f8fafc; border-radius: 12px; padding: 20px; border: 1px solid #f1f5f9; }
.box-title { font-size: 15px; font-weight: 600; color: #1e293b; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #e2e8f0; }
.recommend-list { list-style: none; padding: 0; margin: 0; }
.recommend-item { display: flex; align-items: flex-start; gap: 10px; padding: 10px 0; cursor: pointer; border-bottom: 1px dashed #e2e8f0; transition: all 0.2s; }
.recommend-item:hover .rec-title { color: #4aa3d1; }
.rec-dot { width: 6px; height: 6px; background: #cbd5e1; border-radius: 50%; margin-top: 6px; }
.rec-text { flex: 1; }
.rec-title { margin: 0; font-size: 13px; color: #475569; line-height: 1.4; }
.loading-state, .error-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 0; color: #64748b; }
.spinner { width: 36px; height: 36px; border: 3px solid #f1f5f9; border-top-color: #4aa3d1; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 15px; }
@keyframes spin { to { transform: rotate(360deg); } }
.error-icon { font-size: 50px; margin-bottom: 15px; opacity: 0.5; }
.back-home-btn { margin-top: 15px; background: #4aa3d1; color: white; border: none; padding: 8px 20px; border-radius: 20px; cursor: pointer; }
.image-preview-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.85); z-index: 2000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(5px); cursor: zoom-out; }
.image-preview-overlay img { max-width: 90%; max-height: 90vh; object-fit: contain; border-radius: 8px; box-shadow: 0 10px 30px rgba(0,0,0,0.3); cursor: default; }
.close-preview { position: absolute; top: 30px; right: 40px; color: white; font-size: 30px; cursor: pointer; opacity: 0.7; }
.close-preview:hover { opacity: 1; }

.community-entry { padding: 20px; border-top: 1px solid #f1f5f9; }
.industry-glow-btn { width: 100%; padding: 14px 0; background: linear-gradient(135deg, #667eea, #764ba2); color: white; border: none; border-radius: 40px; font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); display: flex; align-items: center; justify-content: center; gap: 8px; position: relative; overflow: hidden; }
.industry-glow-btn::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent); transition: left 0.6s ease; }
.industry-glow-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(102, 126, 234, 0.6); }
.industry-glow-btn:hover::before { left: 100%; }
.industry-glow-btn:active { transform: translateY(0); }

.matrix-layout { display: flex; flex-direction: column; gap: 24px; }
.big-data-board { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04); }
.board-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 24px; flex-wrap: wrap; gap: 16px; }
.title-area h3 { font-size: 22px; color: #1e293b; margin: 0 0 8px 0; }
.title-area p { color: #64748b; font-size: 14px; margin: 0; }
.school-search-box { display: flex; align-items: center; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 40px; padding: 6px 6px 6px 16px; width: 380px; transition: all 0.3s ease; }
.school-search-box:focus-within { border-color: #4aa3d1; box-shadow: 0 0 0 3px rgba(74, 163, 209, 0.1); background: white; }
.school-search-box input { flex: 1; border: none; background: transparent; outline: none; font-size: 14px; padding: 8px 0; }
.school-search-box .search-btn { background: #4aa3d1; color: white; border: none; padding: 8px 20px; border-radius: 30px; font-size: 14px; cursor: pointer; transition: 0.2s; }
.school-search-box .search-btn:hover { background: #398ebf; }
.data-tabs { display: flex; gap: 12px; margin-bottom: 20px; border-bottom: 1px solid #e2e8f0; padding-bottom: 10px; }
.data-tabs span { font-size: 16px; font-weight: 500; color: #64748b; padding: 8px 16px; cursor: pointer; border-radius: 8px; transition: all 0.2s; }
.data-tabs span:hover { background: #f1f5f9; }
.data-tabs span.active { color: #4aa3d1; background: #f0f7ff; font-weight: 600; }
.table-responsive { overflow-x: auto; }
.school-data-table { width: 100%; border-collapse: collapse; text-align: left; }
.school-data-table th { padding: 14px 16px; font-size: 14px; color: #475569; background: #f8fafc; border-bottom: 1px solid #e2e8f0; font-weight: 600; }
.school-data-table td { padding: 16px; border-bottom: 1px solid #f1f5f9; font-size: 14px; color: #334155; vertical-align: middle; }
.school-data-table tbody tr { transition: background 0.2s; }
.school-data-table tbody tr:hover { background: #f8fafc; }
.school-name { display: flex; align-items: center; gap: 12px; }
.s-logo { width: 40px; height: 40px; background: linear-gradient(135deg, #e0f2fe, #bae6fd); color: #0369a1; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: bold; }
.s-info { display: flex; flex-direction: column; gap: 4px; }
.s-title { font-size: 15px; font-weight: 600; color: #1e293b; }
.s-tags { font-size: 12px; color: #94a3b8; }
.score-trend, .time-node { font-family: 'Courier New', Courier, monospace; font-weight: 600; color: #4aa3d1; }
.intensity-badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.intensity-badge.danger { background: #fef2f2; color: #ef4444; }
.intensity-badge.warning { background: #fff7ed; color: #f97316; }
.intensity-badge.normal { background: #f0fdf4; color: #10b981; }
.detail-btn { background: white; border: 1px solid #cbd5e1; color: #475569; padding: 6px 14px; border-radius: 6px; font-size: 13px; cursor: pointer; transition: all 0.2s; }
.detail-btn:hover { border-color: #4aa3d1; color: #4aa3d1; background: #f0f7ff; }
.major-select { border: none; background: transparent; outline: none; font-size: 14px; color: #475569; padding: 8px 10px; cursor: pointer; font-weight: 500; }
.divider-line { color: #e2e8f0; margin: 0 8px; }
.major-tag { background: #f1f5f9; color: #0369a1; padding: 4px 8px; border-radius: 4px; font-size: 13px; font-weight: 500; }

/* ================= 🌟 优化后的全新简历中心样式 🌟 ================= */
.resume-hub-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
  padding: 0 0 40px 0;
}

.rh-module {
  background: white;
  border-radius: 24px;
  padding: 28px 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f2f5;
  transition: all 0.3s;
}

.ai-module {
  border-top: 4px solid #4aa3d1;
  background: linear-gradient(to bottom, #ffffff, #fefefe);
}

.rh-header {
  margin-bottom: 28px;
}

.rh-title h3 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.title-icon {
  font-size: 28px;
}

.hot-badge-new {
  background: linear-gradient(135deg, #f97316, #ef4444);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.badge-new {
  background: #f1f5f9;
  color: #64748b;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.rh-title p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.step-header-mini {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.step-badge {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #4aa3d1, #2c7da0);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.step-title-new {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.step-count {
  font-size: 12px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 2px 10px;
  border-radius: 20px;
}

.step-hint-new {
  font-size: 12px;
  color: #94a3b8;
}

.template-grid-new {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 20px;
}

.tpl-card-new {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 16px;
  background: white;
}

.tpl-card-new:hover {
  transform: translateY(-4px);
}

.tpl-preview {
  height: 150px;
  border-radius: 16px;
  position: relative;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  border: 2px solid #e2e8f0;
  overflow: hidden;
}

.tpl-card-new.active .tpl-preview {
  border-color: #4aa3d1;
  box-shadow: 0 0 0 3px rgba(74, 163, 209, 0.2);
}

.preview-content {
  text-align: center;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.preview-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.preview-line {
  width: 30px;
  height: 2px;
  background: rgba(255, 255, 255, 0.5);
  margin: 8px auto;
}

.preview-desc {
  font-size: 11px;
  opacity: 0.9;
}

.tpl-badge {
  position: absolute;
  bottom: 8px;
  right: 8px;
  font-size: 10px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
}

.active-check {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: #4aa3d1;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: bold;
}

.tpl-info-new {
  margin-top: 12px;
  text-align: center;
}

.tpl-name-new {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
  font-size: 14px;
}

.tpl-tags {
  display: flex;
  gap: 6px;
  justify-content: center;
  flex-wrap: wrap;
}

.tpl-tag {
  font-size: 10px;
  padding: 2px 8px;
  background: #f1f5f9;
  border-radius: 12px;
  color: #64748b;
}

.example-card-new {
  background: linear-gradient(135deg, #fff9e6 0%, #fff4e0 100%);
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.example-icon {
  font-size: 32px;
}

.example-content {
  flex: 1;
}

.example-title {
  font-weight: 600;
  color: #b45309;
  margin-bottom: 6px;
  font-size: 13px;
}

.example-text {
  font-size: 13px;
  color: #78350f;
  line-height: 1.5;
}

.use-example-btn {
  padding: 6px 16px;
  background: #f59e0b;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.use-example-btn:hover {
  background: #d97706;
  transform: translateY(-1px);
}

.input-area-new {
  background: #f8fafc;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  transition: all 0.3s;
}

.input-area-new:focus-within {
  border-color: #4aa3d1;
  box-shadow: 0 0 0 3px rgba(74, 163, 209, 0.1);
}

.input-toolbar-new {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tool-btn {
  padding: 4px 12px;
  background: #f1f5f9;
  border: none;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: #475569;
}

.tool-btn:hover {
  background: #4aa3d1;
  color: white;
  transform: translateY(-1px);
}

.toolbar-stats {
  display: flex;
  align-items: center;
  gap: 8px;
}

.char-count {
  font-size: 13px;
  color: #4aa3d1;
  font-weight: 600;
}

.char-max {
  font-size: 12px;
  color: #94a3b8;
}

.clear-btn {
  padding: 4px 12px;
  background: #fee2e2;
  color: #ef4444;
  border: none;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
}

.resume-textarea-new {
  width: 100%;
  min-height: 260px;
  padding: 20px;
  border: none;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  font-family: inherit;
  outline: none;
  background: #f8fafc;
}

.resume-textarea-new::placeholder {
  color: #cbd5e1;
}

.ai-analysis-new {
  margin-top: 20px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  border-radius: 16px;
  border-left: 4px solid #4aa3d1;
}

.analysis-header-new {
  font-weight: 600;
  color: #4aa3d1;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.analysis-content-new {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.analysis-item {
  font-size: 13px;
}

.item-label {
  color: #64748b;
}

.item-value {
  color: #1e293b;
  font-weight: 500;
}

.item-value.suggestion {
  color: #f59e0b;
}

.action-footer-new {
  text-align: center;
  margin-top: 32px;
}

.generate-btn-new {
  background: linear-gradient(135deg, #4aa3d1, #2c7da0);
  color: white;
  border: none;
  padding: 14px 36px;
  border-radius: 48px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s;
  box-shadow: 0 6px 20px rgba(74, 163, 209, 0.4);
}

.generate-btn-new:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(74, 163, 209, 0.5);
}

.generate-btn-new:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 20px;
}

.btn-text {
  font-size: 16px;
}

/* ================= 🌟 重点替换新增的样式 🌟 ================= */
.view-all-btn {
  font-size: 14px; color: #4aa3d1; cursor: pointer; transition: 0.3s;
  display: flex; align-items: center; gap: 4px; padding: 6px 18px;
  background: #f0f9ff; border-radius: 20px; font-weight: 500;
  margin-bottom: 5px;
}
.view-all-btn:hover { background: #4aa3d1; color: white; transform: translateX(2px); }

.rh-grid-new {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.rh-card-new {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  border: 1px solid #e2e8f0;
  position: relative;
  cursor: pointer;
}

.rh-card-new:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
  border-color: #4aa3d1;
}

.card-inner-new {
  position: relative;
  width: 100%;
  height: 220px; 
  background-size: cover;
  background-position: top center;
  background-color: #f1f5f9;
  border-bottom: 1px solid #e2e8f0;
}

/* 悬浮遮罩与按钮特效 */
.hover-overlay-new {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.overlay-btn-new {
  width: 110px;
  padding: 10px 0;
  border-radius: 24px;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.overlay-btn-new.primary {
  background: #4aa3d1;
  color: white;
  box-shadow: 0 4px 10px rgba(74, 163, 209, 0.4);
}

.overlay-btn-new.primary:hover {
  background: #398ebf;
  transform: scale(1.05);
}

.overlay-btn-new.outline {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid white;
  color: white;
}

.overlay-btn-new.outline:hover {
  background: white;
  color: #4aa3d1;
}

.rh-info-new { padding: 14px; text-align: center; }
.rh-info-new h4 { margin: 0 0 6px 0; font-size: 15px; font-weight: 600; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.rh-info-new p { margin: 0; font-size: 12px; color: #94a3b8; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 响应式调整 */
@media (max-width: 768px) {
  .rh-module { padding: 20px; }
  .template-grid-new { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
  .tpl-preview { height: 120px; }
  .rh-grid-new { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 16px; }
  .generate-btn-new { padding: 12px 24px; font-size: 14px; }
  .card-inner-new { height: 180px; }
}

/* 胶囊搜索框样式 */
.school-search-box { display: flex; align-items: center; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 48px; padding: 4px 4px 4px 10px; width: 480px; transition: all 0.3s; }
.school-search-box:focus-within { border-color: #667eea; background: white; }
.school-search-box input { flex: 1; border: none; background: transparent; outline: none; font-size: 14px; padding: 10px 0; }
.school-search-box .search-btn { background: linear-gradient(135deg, #667eea, #764ba2); color: white; border: none; padding: 8px 24px; border-radius: 40px; font-size: 14px; font-weight: 500; cursor: pointer; white-space: nowrap; }

/* 矩阵下方内容卡片优化 */
.section-header-optimized { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-left { display: flex; align-items: center; gap: 12px; }
.section-icon { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.section-icon.kaoyan { background: linear-gradient(135deg, #ffecd2, #fcb69f); }
.section-icon.baoyan { background: linear-gradient(135deg, #a1c4fd, #c2e9fb); }
.section-icon.liuxue { background: linear-gradient(135deg, #d4fc79, #96e6a1); }
.section-title { font-size: 18px; font-weight: 600; color: #1e293b; margin: 0; }
.header-right { display: flex; align-items: center; gap: 16px; }
.type-selector-optimized { display: flex; background: #f1f5f9; border-radius: 40px; padding: 3px; }
.type-selector-optimized span { padding: 4px 16px; font-size: 12px; cursor: pointer; border-radius: 30px; transition: all 0.3s; color: #64748b; }
.type-selector-optimized span.active { background: white; color: #667eea; font-weight: 500; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05); }
.card-grid-optimized { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; }
.card-optimized { background: white; border-radius: 16px; overflow: hidden; cursor: pointer; border: 1px solid #e2e8f0; transition: 0.3s; }
.card-optimized:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1); border-color: #cbd5e1; }
.card-cover-optimized { height: 140px; position: relative; background-size: cover; background-position: center; }
.type-badge-optimized { position: absolute; top: 12px; left: 12px; padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; color: white; }
.type-badge-optimized.video { background: linear-gradient(135deg, #ef4444, #dc2626); }
.type-badge-optimized.article { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.play-icon { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 40px; height: 40px; background: rgba(0, 0, 0, 0.6); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 18px; transition: 0.3s; }
.card-optimized:hover .play-icon { transform: translate(-50%, -50%) scale(1.1); background: rgba(0, 0, 0, 0.8); }
.card-info-optimized { padding: 14px; }
.card-title-optimized { font-size: 14px; font-weight: 600; color: #1e293b; margin: 0 0 8px 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-meta { display: flex; justify-content: space-between; font-size: 11px; color: #94a3b8; }
.matrix-hero {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 24px;
  padding: 32px 40px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(26, 26, 46, 0.3);
}

.piano-keys {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  display: flex;
  align-items: flex-end;
  gap: 4px;
  padding: 0 20px;
  z-index: 0;
}

.piano-key {
  flex: 1;
  height: 30px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 4px 4px 0 0;
  animation: pianoJump 1.2s ease-in-out infinite;
  transform-origin: bottom;
}

/* 钢琴键跳动错落感 */
.piano-key:nth-child(even) { height: 45px; background: rgba(102, 126, 234, 0.4); animation-delay: 0.2s; }
.piano-key:nth-child(3n) { height: 20px; background: rgba(236, 72, 153, 0.3); animation-delay: 0.4s; }
.piano-key:nth-child(5n) { height: 55px; background: rgba(16, 185, 129, 0.3); animation-delay: 0.6s; }

@keyframes pianoJump {
  0%, 100% { transform: scaleY(0.6); opacity: 0.5; }
  50% { transform: scaleY(1.2); opacity: 1; }
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.hero-icon {
  font-size: 48px;
  margin-bottom: 16px;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.3));
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin: 0 0 12px 0;
  letter-spacing: 1px;
}

.hero-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}


/* ================= 🌟 颜值重塑：高级胶囊搜索框 🌟 ================= */
.school-search-box {
  display: flex !important;
  align-items: center !important;
  background: #ffffff !important;
  border: 2px solid #e2e8f0 !important;
  border-radius: 50px !important;
  padding: 6px 8px 6px 20px !important;
  width: 500px !important;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03) !important;
  transition: all 0.3s ease !important;
}

.school-search-box:focus-within {
  border-color: #667eea !important;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.15) !important;
  transform: translateY(-1px) !important;
}

.major-select {
  border: none !important;
  background: transparent !important;
  outline: none !important;
  font-size: 14px !important;
  color: #334155 !important;
  font-weight: 600 !important;
  cursor: pointer !important;
  appearance: none !important;
  -webkit-appearance: none !important;
  padding-right: 16px !important;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%2364748b' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e") !important;
  background-repeat: no-repeat !important;
  background-position: right center !important;
  background-size: 14px !important;
}

.divider-line {
  color: #cbd5e1 !important;
  margin: 0 15px !important;
  font-size: 16px !important;
}

.school-search-box .search-icon {
  color: #667eea !important;
  font-size: 18px !important;
  margin-right: 8px !important;
}

.school-search-box input {
  flex: 1 !important;
  border: none !important;
  background: transparent !important;
  outline: none !important;
  font-size: 14px !important;
  color: #1e293b !important;
}

.school-search-box input::placeholder {
  color: #94a3b8 !important;
}

.school-search-box .search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  border: none !important;
  padding: 10px 26px !important;
  border-radius: 40px !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  cursor: pointer !important;
  transition: all 0.3s !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4) !important;
  letter-spacing: 1px !important;
}

.school-search-box .search-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.6) !important;
}

/* 右上角悬浮预览框 */
.dashboard-preview {
  position: fixed;
  top: 90px;
  right: 40px;
  width: 260px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.15);
  z-index: 9999;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  animation: fadeIn 0.3s ease;
}
.dashboard-preview img {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
}
.dashboard-preview .preview-header {
  padding: 8px;
  text-align: center;
  font-size: 13px;
  font-weight: bold;
  color: #64748b;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>