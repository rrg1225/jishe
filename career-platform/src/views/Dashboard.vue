<template>
  <div class="layout">
    <header class="header">
      <div class="logo">学业职业规划平台</div>


       <!-- 搜索栏区域 -->
<div class="search-container">
  <div class="search-bar">
    <span class="search-icon">🔍</span>
    <input 
      type="text" 
      placeholder="搜索院校、企业或竞赛..." 
      v-model="searchKeyword"
      @focus="showHotSearch = true"
      @blur="hideHotSearch"
    />
  </div>
  
  <!-- 热搜下拉框 -->
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
    
    <div class="hot-search-footer">
      <span>更多热搜 →</span>
    </div>
  </div>
</div>




      
      <div class="header-right">
        <button class="go-publish-btn" @click="router.push('/publish')">
          <span class="icon">✍️</span> 发布内容
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
        </ul>
      </aside>

      <main class="content">
        
          <!-- 首页轮播图区域 -->
<section v-if="activeMenu === 'home'" class="fade-in">
  <!-- 轮播图容器 -->
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
    
    <!-- 轮播图指示点 -->
    <div class="carousel-dots">
      <span 
        v-for="(slide, index) in carouselSlides" 
        :key="index"
        class="dot"
        :class="{ active: currentSlide === index }"
        @click="currentSlide = index"
      ></span>
    </div>
    
    <!-- 左右箭头 -->
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
                  <div class="card-cover" :style="{ background: item.color }">
                    <span class="type-badge" :class="item.type">{{ item.typeText }}</span>
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

        <section v-if="activeMenu === 'matrix'" class="card-layout fade-in">
          <h3>学业深造信息矩阵</h3>
          <p class="subtitle">为你提供最新的升学与海外留学情报</p>
          

           
           <!-- 右侧信息卡片 - 2x2网格 -->
         <!-- 右侧学业深造信息矩阵 - 田字格按钮卡片 -->
<div class="academy-grid">
  <!-- 考研卡片 -->
  <div class="academy-card" @click="goToPage('/graduate-exam')">
    <div class="card-content">
      <span class="card-icon">📚</span>
      <h4>考研最新政策</h4>
      <p>大纲解析 · 报名时间 · 院校动态</p>
      <span class="card-arrow">→</span>
    </div>
  </div>
  
  <!-- 保研卡片 -->
  <div class="academy-card" @click="goToPage('/recommendation')">
    <div class="card-content">
      <span class="card-icon">🏆</span>
      <h4>保研推免攻略</h4>
      <p>夏令营 · 预推免 · 九推指南</p>
      <span class="card-arrow">→</span>
    </div>
  </div>
  
  <!-- 留学卡片 -->
  <div class="academy-card" @click="goToPage('/abroad')">
    <div class="card-content">
      <span class="card-icon">✈️</span>
      <h4>海外留学资讯</h4>
      <p>QS排名 · 申请时间轴 · 签证政策</p>
      <span class="card-arrow">→</span>
    </div>
  </div>
  
  <!-- 院校筛选卡片 -->
  <div class="academy-card" @click="goToPage('/school-filter')">
    <div class="card-content">
      <span class="card-icon">🔍</span>
      <h4>大数据院校筛选</h4>
      <p>智能匹配 · 录取概率 · 专业推荐</p>
      <span class="card-arrow">→</span>
    </div>
  </div>
</div>




        </section>








<!-- 产教融合与招聘双选 - 多级标签版本 -->
<section v-if="activeMenu === 'job'" class="fade-in">
  <div class="recruitment-section">
    <div class="section-header">
      <div>
        <h3>🔥 产教融合 · 招聘双选</h3>
        <!-- 删除原来的行业前沿文字 -->
      </div>
    </div>
    
    <!-- 一级标签 - 行业 -->
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
    
    <!-- 二级标签 - 职位大类 -->
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
    
   


   <!-- 三级标签 - 具体职位 -->
<div class="job-tags" v-if="selectedSubCategory">
  <span 
    class="job-tag"
    :class="{ active: selectedJob === '全部' }"
    @click="selectedJob = '全部'"
  >
    全部
  </span>
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






    
    <!-- 职位卡片列表 -->
    <div class="job-cards">
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
      
      <div v-if="filteredJobCards.length === 0" class="empty-state">
        暂无相关职位
      </div>
    </div>
  </div>
</section>














       <!-- 竞赛信息大厅 -->
<section v-if="activeMenu === 'contest'" class="fade-in">
  <div class="contest-section">
    <!-- 头部 -->
    <div class="contest-header">
      <div>
        <h3>🏆 竞赛信息大厅</h3>
        <p class="section-subtitle">打标签分类，快速匹配适合你的比赛</p>
      </div>
    </div>
    
    <!-- 搜索栏 -->
    <div class="contest-search">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input 
          type="text" 
          placeholder="搜索竞赛名称、关键词..." 
          v-model="contestKeyword"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
    </div>
    
    <!-- 标签栏 - 可横向滚动 -->
    <div class="tags-wrapper">
      <div class="tags-scroll">
        <span 
          class="tag-chip" 
          :class="{ active: selectedTag === '全部' }"
          @click="selectTag('全部')"
        >
          🏷️ 全部
        </span>
        <span 
          v-for="tag in contestTags" 
          :key="tag.name"
          class="tag-chip"
          :class="{ active: selectedTag === tag.name }"
          :style="{ borderColor: selectedTag === tag.name ? tag.color : '#e2e8f0' }"
          @click="selectTag(tag.name)"
        >
          <span class="tag-icon">{{ tag.icon }}</span>
          <span class="tag-name">{{ tag.name }}</span>
          <span class="tag-count" :style="{ backgroundColor: tag.color + '20', color: tag.color }">{{ tag.count }}</span>
        </span>
      </div>
    </div>
    
    <!-- 当前选中标签提示 -->
    <div class="selected-tag-info" v-if="selectedTag !== '全部'">
      <span class="selected-tag-badge" :style="{ backgroundColor: contestTags.find(t => t.name === selectedTag)?.color + '20', color: contestTags.find(t => t.name === selectedTag)?.color }">
        {{ selectedTag }}
      </span>
      <span class="result-count">共 {{ filteredContests.length }} 个赛事</span>
    </div>
    
    <!-- 赛事卡片列表 -->
    <div class="contest-grid" v-if="filteredContests.length > 0">
      <div 
        v-for="contest in filteredContests" 
        :key="contest.id"
        class="contest-card"
        @click="goToContestDetail(contest)"
      >
        <!-- 卡片头部 - 状态标识 -->
        <div class="card-header">
          <span class="contest-level" :style="{ backgroundColor: contest.level === '国际级' ? '#fef3c7' : contest.level === '国家级' ? '#e0f2fe' : '#f1f5f9' }">
            {{ contest.level }}
          </span>
          <span class="contest-status" :style="{ backgroundColor: contest.statusColor + '20', color: contest.statusColor }">
            {{ contest.status }}
          </span>
        </div>
        
        <!-- 标题 -->
        <h4 class="contest-title">{{ contest.title }}</h4>
        
        <!-- 主办方 -->
        <div class="contest-organizer">
          <span class="organizer-label">主办：</span>
          {{ contest.organize }}
        </div>
        
        <!-- 时间信息 -->
        <div class="contest-time">
          <div class="time-item">
            <span class="time-icon">📅</span>
            <span>{{ formatDate(contest.startDate) }} - {{ formatDate(contest.endDate) }}</span>
          </div>
          <div class="time-remain" v-if="contest.status === '报名中'">
            距报名截止 {{ getRemainingDays(contest.endDate) }}天
          </div>
        </div>
        
        <!-- 标签 -->
        <div class="contest-tags">
          <span v-for="tag in contest.tags" :key="tag" class="contest-tag">
            {{ tag }}
          </span>
        </div>
        
        <!-- 能力要求 -->
        <div class="ability-section">
          <div class="section-label">✨ 能力要求：</div>
          <div class="ability-tags">
            <span v-for="ability in contest.abilities" :key="ability" class="ability-tag">
              {{ ability }}
            </span>
          </div>
        </div>
        
        <!-- 参赛人数 -->
        <div class="contest-participants">
          <span class="participants-icon">👥</span>
          {{ contest.participants }}
        </div>
        
        <!-- 查看详情 -->
        <div class="card-footer">
          <span class="view-detail">查看详情 →</span>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">🏆</div>
      <p>暂无相关赛事</p>
      <span class="empty-sub">试试其他标签或关键词</span>
    </div>
  </div>
</section>









        <section v-if="activeMenu === 'profile'" class="card-layout fade-in">
          <h3>动态能力画像与简历定制</h3>
          <p class="subtitle">基于你的经历，AI 动态生成的个人雷达图</p>
          <div class="profile-demo">
            <div class="radar-placeholder">🎯 雷达图表区域 (开发中)</div>
            <button class="export-btn" @click="openResumeDialog">一键生成专属简历模板</button>
          </div>
        </section>

        <div v-if="isResumeDialogOpen" class="modal-overlay">
          <div class="modal-content">
            <div class="modal-header">
              <h3>✨ 智谱 AI 专属简历定制</h3>
              <span class="close-modal" @click="closeResumeDialog">✖</span>
            </div>
            
            <div class="modal-body">
              <p class="desc">请详细描述您的基本信息、技能栈、做过的项目以及对排版的要求，AI 将为您量身定制：</p>
              <textarea
                v-model="resumePrompt"
                rows="6"
                placeholder="例如：我叫张三，熟悉 Java 和 Vue，做过一个电商管理系统。请帮我生成一份适合投递大厂全栈工程师的简历，语气要专业..."
                :disabled="isGenerating"
              ></textarea>
              
              <div v-if="generatedResume" class="result-area">
                <p class="success-tip">✅ 生成成功！</p>
                <div class="resume-output">{{ generatedResume }}</div>
              </div>
            </div>

            <div class="modal-footer">
              <button class="cancel-btn" @click="closeResumeDialog" :disabled="isGenerating">取消</button>
              <button class="submit-btn" @click="generateResume" :disabled="isGenerating || !resumePrompt.trim()">
                {{ isGenerating ? '大模型正在奋笔疾书中...' : '🚀 开始生成' }}
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>




// 热搜数据
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

// 延迟关闭，让点击事件能触发
let blurTimer = null
const hideHotSearch = () => {
  blurTimer = setTimeout(() => {
    showHotSearch.value = false
  }, 200)
}

// 点击热搜词
const selectHotSearch = (item) => {
  searchKeyword.value = item.keyword
  showHotSearch.value = false
  // 执行搜索
  performSearch(item.keyword)
}

// 执行搜索
const performSearch = (keyword) => {
  console.log('搜索：', keyword)
  // 这里可以调搜索接口或跳转
  // router.push(`/search?q=${keyword}`)
}

// 组件销毁时清除定时器
onUnmounted(() => {
  if (blurTimer) clearTimeout(blurTimer)
})





// ===== 产教融合招聘数据 - BOSS直聘完整版 =====
const jobCategories = ref([
  // 1. 互联网/IT
  {
    name: '互联网/AI',
    icon: '💻',
    subCategories: [
      {
        name: '后端开发',
        jobs: [
          { name: 'Java', count: 245, hot: true },
          { name: 'Python', count: 189, hot: true },
          { name: 'C++', count: 156 },
          { name: 'Go', count: 98 },
          { name: 'Node.js', count: 76 },
          { name: '.NET', count: 45 },
          { name: 'C#', count: 67 },
          { name: 'PHP', count: 89 },
          { name: 'Golang', count: 54 },
          { name: '全栈工程师', count: 123, hot: true },
          { name: '架构师', count: 67, hot: true },
          { name: '后端开发', count: 112 }
        ]
      },
      {
        name: '前端开发',
        jobs: [
          { name: 'Vue.js', count: 203, hot: true },
          { name: 'React', count: 187, hot: true },
          { name: 'JavaScript', count: 165 },
          { name: 'TypeScript', count: 112 },
          { name: 'HTML/CSS', count: 98 },
          { name: '小程序开发', count: 67 },
          { name: '移动端开发', count: 78 },
          { name: '鸿蒙开发', count: 34, hot: true },
          { name: '前端架构师', count: 56 },
          { name: '跨端开发', count: 43 },
          { name: 'WebGL', count: 23 }
        ]
      },
      {
        name: '测试',
        jobs: [
          { name: '软件测试', count: 134 },
          { name: '自动化测试', count: 98 },
          { name: '性能测试', count: 56 },
          { name: '测试开发', count: 67 },
          { name: '功能测试', count: 45 },
          { name: '渗透测试', count: 34, hot: true },
          { name: '游戏测试', count: 23 },
          { name: '硬件测试', count: 32 }
        ]
      },
      {
        name: '运维',
        jobs: [
          { name: '运维工程师', count: 112 },
          { name: '网络安全', count: 78 },
          { name: 'DBA', count: 45 },
          { name: '系统工程师', count: 67 },
          { name: '网络工程师', count: 56 },
          { name: '运维开发', count: 34 },
          { name: '云计算', count: 89, hot: true },
          { name: 'SRE', count: 43 },
          { name: 'IDC运维', count: 28 }
        ]
      },
      {
        name: '算法',
        jobs: [
          { name: '推荐算法', count: 67, hot: true },
          { name: '大模型算法', count: 54, hot: true },
          { name: '图像算法', count: 43 },
          { name: '自然语言处理', count: 38, hot: true },
          { name: '搜索算法', count: 29 },
          { name: '语音算法', count: 23 },
          { name: '数据挖掘', count: 45 },
          { name: '机器学习', count: 78, hot: true },
          { name: '深度学习', count: 56, hot: true }
        ]
      },
      {
        name: '产品',
        jobs: [
          { name: '产品经理', count: 156, hot: true },
          { name: '产品专员', count: 98 },
          { name: '产品总监', count: 34 },
          { name: '电商产品', count: 45 },
          { name: 'AI产品', count: 67, hot: true },
          { name: 'B端产品', count: 54 },
          { name: '数据产品', count: 43 },
          { name: '策略产品', count: 32 }
        ]
      },
      {
        name: '数据分析',
        jobs: [
          { name: '数据分析师', count: 134, hot: true },
          { name: '数据开发', count: 78 },
          { name: '数据仓库', count: 56 },
          { name: '数据产品', count: 34 },
          { name: '数据科学家', count: 23 },
          { name: '大数据开发', count: 45, hot: true },
          { name: 'ETL工程师', count: 32 }
        ]
      },
      {
        name: '移动开发',
        jobs: [
          { name: 'Android', count: 89 },
          { name: 'iOS', count: 76 },
          { name: 'Flutter', count: 45, hot: true },
          { name: 'React Native', count: 38 },
          { name: '鸿蒙', count: 29, hot: true }
        ]
      },
      {
        name: '人工智能',
        jobs: [
          { name: '计算机视觉', count: 67, hot: true },
          { name: '语音识别', count: 34 },
          { name: 'NLP', count: 56, hot: true },
          { name: '强化学习', count: 23 },
          { name: 'AIGC', count: 45, hot: true }
        ]
      },
      {
        name: '高端技术',
        jobs: [
          { name: '技术总监', count: 34 },
          { name: '架构师', count: 45, hot: true },
          { name: '技术专家', count: 38 },
          { name: '首席科学家', count: 12 },
          { name: 'CTO', count: 8 }
        ]
      }
    ]
  },
  // 2. 电子/通信/硬件
  {
    name: '电子/通信/硬件',
    icon: '📡',
    subCategories: [
      {
        name: '硬件开发',
        jobs: [
          { name: '嵌入式', count: 89 },
          { name: 'FPGA', count: 45 },
          { name: '硬件工程师', count: 67 },
          { name: 'PCB设计', count: 34 },
          { name: '单片机', count: 56 },
          { name: 'ARM开发', count: 43 },
          { name: '电路设计', count: 38 },
          { name: '射频工程师', count: 29 }
        ]
      },
      {
        name: '通信',
        jobs: [
          { name: '5G', count: 56 },
          { name: '网络优化', count: 34 },
          { name: '通信工程师', count: 45 },
          { name: '射频工程师', count: 23 },
          { name: '光通信', count: 18 },
          { name: '基站工程师', count: 27 }
        ]
      },
      {
        name: '半导体',
        jobs: [
          { name: '芯片设计', count: 34, hot: true },
          { name: '集成电路', count: 45 },
          { name: '版图设计', count: 23 },
          { name: '半导体工艺', count: 28 },
          { name: '封测', count: 15 }
        ]
      },
      {
        name: '自动化',
        jobs: [
          { name: '自动化工程师', count: 67 },
          { name: 'PLC', count: 43 },
          { name: '工业控制', count: 34 },
          { name: '机器人', count: 28, hot: true }
        ]
      },
      {
        name: '仪器仪表',
        jobs: [
          { name: '测控', count: 34 },
          { name: '仪器开发', count: 23 },
          { name: '传感器', count: 28 }
        ]
      }
    ]
  },
  // 3. 金融
  {
    name: '金融',
    icon: '💰',
    subCategories: [
      {
        name: '量化',
        jobs: [
          { name: '量化研究员', count: 45 },
          { name: '量化开发', count: 34 },
          { name: '量化交易', count: 23 },
          { name: '高频交易', count: 18, hot: true }
        ]
      },
      {
        name: '风控',
        jobs: [
          { name: '风控模型', count: 56 },
          { name: '反欺诈', count: 34 },
          { name: '信用分析', count: 45 },
          { name: '风险策略', count: 28 }
        ]
      },
      {
        name: '金融科技',
        jobs: [
          { name: '支付系统', count: 34 },
          { name: '区块链', count: 23, hot: true },
          { name: '数字货币', count: 12 },
          { name: 'FinTech', count: 28, hot: true }
        ]
      },
      {
        name: '投资银行',
        jobs: [
          { name: '投行分析师', count: 34 },
          { name: '并购', count: 18 },
          { name: '承销', count: 15 }
        ]
      },
      {
        name: '证券/基金',
        jobs: [
          { name: '行业研究', count: 45 },
          { name: '投资顾问', count: 28 },
          { name: '基金经理', count: 16 },
          { name: '交易员', count: 32 }
        ]
      },
      {
        name: '保险',
        jobs: [
          { name: '精算师', count: 12 },
          { name: '保险核保', count: 23 },
          { name: '保险理赔', count: 28 }
        ]
      }
    ]
  },
  // 4. 产品/运营/设计
  {
    name: '产品/运营/设计',
    icon: '🎨',
    subCategories: [
      {
        name: '产品',
        jobs: [
          { name: '产品经理', count: 156, hot: true },
          { name: '产品专员', count: 98 },
          { name: '产品总监', count: 34 },
          { name: '电商产品', count: 45 },
          { name: 'AI产品', count: 67, hot: true },
          { name: 'B端产品', count: 54 },
          { name: '数据产品', count: 43 }
        ]
      },
      {
        name: '运营',
        jobs: [
          { name: '用户运营', count: 87 },
          { name: '内容运营', count: 76 },
          { name: '活动运营', count: 65 },
          { name: '产品运营', count: 78 },
          { name: '新媒体运营', count: 92, hot: true },
          { name: '电商运营', count: 67 },
          { name: '社群运营', count: 54 },
          { name: '私域运营', count: 43 }
        ]
      },
      {
        name: '设计',
        jobs: [
          { name: 'UI设计', count: 98 },
          { name: 'UX设计', count: 67 },
          { name: '平面设计', count: 76 },
          { name: '交互设计', count: 54 },
          { name: '视觉设计', count: 65 },
          { name: '品牌设计', count: 43 },
          { name: '包装设计', count: 32 }
        ]
      },
      {
        name: 'UX/UE',
        jobs: [
          { name: '用户体验', count: 45 },
          { name: '用户研究', count: 34 },
          { name: '人机交互', count: 23 }
        ]
      },
      {
        name: '游戏设计',
        jobs: [
          { name: '游戏策划', count: 45 },
          { name: '游戏UI', count: 32 },
          { name: '游戏原画', count: 28 },
          { name: '游戏场景', count: 21 }
        ]
      },
      {
        name: '视觉/交互',
        jobs: [
          { name: '视觉设计', count: 56 },
          { name: '交互设计', count: 43 },
          { name: '动效设计', count: 23 }
        ]
      }
    ]
  },
  // 5. 市场/销售/商务
  {
    name: '市场/销售/商务',
    icon: '📈',
    subCategories: [
      {
        name: '市场营销',
        jobs: [
          { name: '市场营销', count: 87 },
          { name: '品牌策划', count: 56 },
          { name: '公关', count: 34 },
          { name: '市场推广', count: 65 },
          { name: 'SEO/SEM', count: 43 },
          { name: '网络营销', count: 54 }
        ]
      },
      {
        name: '销售',
        jobs: [
          { name: '销售代表', count: 156 },
          { name: '销售经理', count: 98 },
          { name: '大客户销售', count: 76, hot: true },
          { name: '电话销售', count: 87 },
          { name: '渠道销售', count: 54 },
          { name: '销售总监', count: 23 },
          { name: '区域销售', count: 45 }
        ]
      },
      {
        name: '商务',
        jobs: [
          { name: '商务拓展', count: 67 },
          { name: '商务经理', count: 45 },
          { name: '渠道合作', count: 34 },
          { name: 'BD', count: 56, hot: true }
        ]
      },
      {
        name: '广告',
        jobs: [
          { name: '广告投放', count: 43 },
          { name: '广告优化', count: 32 },
          { name: '媒介', count: 23 },
          { name: '广告策划', count: 34 }
        ]
      },
      {
        name: '会展/活动',
        jobs: [
          { name: '活动策划', count: 34 },
          { name: '会展执行', count: 23 },
          { name: '会议活动', count: 18 }
        ]
      }
    ]
  },
  // 6. 人事/财务/行政
  {
    name: '人事/财务/行政',
    icon: '📋',
    subCategories: [
      {
        name: '人力资源',
        jobs: [
          { name: 'HRBP', count: 56, hot: true },
          { name: '招聘', count: 78 },
          { name: '培训', count: 34 },
          { name: '薪酬绩效', count: 43 },
          { name: '员工关系', count: 32 },
          { name: 'HRD', count: 15 },
          { name: '组织发展', count: 12 }
        ]
      },
      {
        name: '财务',
        jobs: [
          { name: '会计', count: 89 },
          { name: '出纳', count: 54 },
          { name: '财务分析', count: 43 },
          { name: '审计', count: 38 },
          { name: '税务', count: 27 },
          { name: 'CFO', count: 5 },
          { name: '成本会计', count: 34 }
        ]
      },
      {
        name: '行政',
        jobs: [
          { name: '行政专员', count: 67 },
          { name: '行政经理', count: 34 },
          { name: '前台', count: 28 },
          { name: '后勤', count: 23 },
          { name: '行政主管', count: 32 }
        ]
      },
      {
        name: '法务',
        jobs: [
          { name: '法务专员', count: 34 },
          { name: '法务经理', count: 23 },
          { name: '合规', count: 18 },
          { name: '知识产权', count: 12 }
        ]
      }
    ]
  },
  // 7. 生产制造/供应链
  {
    name: '生产制造/供应链',
    icon: '🏭',
    subCategories: [
      {
        name: '生产管理',
        jobs: [
          { name: '生产主管', count: 45 },
          { name: '生产经理', count: 34 },
          { name: '厂长', count: 12 },
          { name: '生产计划', count: 28 },
          { name: '车间主任', count: 23 }
        ]
      },
      {
        name: '质量管理',
        jobs: [
          { name: 'QC', count: 56 },
          { name: 'QA', count: 43 },
          { name: '质量工程师', count: 38 },
          { name: '体系工程师', count: 23 },
          { name: 'SQE', count: 19 }
        ]
      },
      {
        name: '供应链',
        jobs: [
          { name: '采购', count: 67 },
          { name: '物流', count: 54 },
          { name: '仓储', count: 43 },
          { name: '供应链经理', count: 23 },
          { name: '报关员', count: 15 }
        ]
      },
      {
        name: '工艺/制程',
        jobs: [
          { name: '工艺工程师', count: 45 },
          { name: 'IE工程师', count: 32 },
          { name: 'PE工程师', count: 28 },
          { name: 'NPI工程师', count: 19 }
        ]
      },
      {
        name: '设备维护',
        jobs: [
          { name: '设备工程师', count: 34 },
          { name: '维修工程师', count: 23 },
          { name: '自动化维护', count: 18 }
        ]
      }
    ]
  },
  // 8. 零售/生活服务
  {
    name: '零售/生活服务',
    icon: '🛒',
    subCategories: [
      {
        name: '零售',
        jobs: [
          { name: '店员/营业员', count: 156 },
          { name: '店长', count: 67 },
          { name: '导购', count: 98 },
          { name: '收银', count: 76 },
          { name: '商品管理', count: 34 },
          { name: '品类经理', count: 28 }
        ]
      },
      {
        name: '餐饮',
        jobs: [
          { name: '服务员', count: 123 },
          { name: '厨师', count: 67 },
          { name: '店长', count: 45 },
          { name: '餐饮管理', count: 34 },
          { name: '奶茶/水吧', count: 56 },
          { name: '西点师', count: 23 }
        ]
      },
      {
        name: '酒店/旅游',
        jobs: [
          { name: '前台', count: 67 },
          { name: '客房服务', count: 45 },
          { name: '酒店管理', count: 34 },
          { name: '导游', count: 23 },
          { name: '旅行顾问', count: 19 }
        ]
      },
      {
        name: '美容/美发',
        jobs: [
          { name: '美容师', count: 56 },
          { name: '美发师', count: 43 },
          { name: '美甲师', count: 32 },
          { name: '纹绣师', count: 18 }
        ]
      },
      {
        name: '运动/健身',
        jobs: [
          { name: '健身教练', count: 34 },
          { name: '瑜伽老师', count: 23 },
          { name: '游泳教练', count: 18 }
        ]
      }
    ]
  },
  // 9. 教育培训
  {
    name: '教育培训',
    icon: '📚',
    subCategories: [
      {
        name: '教师',
        jobs: [
          { name: '语数外教师', count: 89 },
          { name: '艺术教师', count: 67 },
          { name: '体育教师', count: 34 },
          { name: '科学教师', count: 45 },
          { name: '音乐教师', count: 28 },
          { name: '美术教师', count: 32 }
        ]
      },
      {
        name: '教务管理',
        jobs: [
          { name: '教务专员', count: 56 },
          { name: '教务主管', count: 34 },
          { name: '班主任', count: 67 }
        ]
      },
      {
        name: '课程顾问',
        jobs: [
          { name: '课程销售', count: 78 },
          { name: '课程顾问', count: 89, hot: true }
        ]
      },
      {
        name: '培训',
        jobs: [
          { name: '培训讲师', count: 45 },
          { name: '企业培训', count: 34 },
          { name: '技能培训', count: 28 }
        ]
      },
      {
        name: '留学/语言',
        jobs: [
          { name: '留学顾问', count: 34 },
          { name: '语言老师', count: 45 },
          { name: '雅思/托福', count: 28, hot: true }
        ]
      }
    ]
  },
  // 10. 房地产/建筑
  {
    name: '房地产/建筑',
    icon: '🏗️',
    subCategories: [
      {
        name: '工程管理',
        jobs: [
          { name: '项目经理', count: 45 },
          { name: '施工员', count: 67 },
          { name: '资料员', count: 34 },
          { name: '材料员', count: 23 },
          { name: '安全员', count: 38 },
          { name: '质量员', count: 28 },
          { name: '监理工程师', count: 32 },
          { name: '工程经理', count: 27 }
        ]
      },
      {
        name: '工程造价',
        jobs: [
          { name: '造价工程师', count: 56 },
          { name: '预算员', count: 43 },
          { name: '成本控制', count: 32 },
          { name: '招投标', count: 27 }
        ]
      },
      {
        name: '装饰装修',
        jobs: [
          { name: '室内设计', count: 45 },
          { name: '软装设计', count: 32 },
          { name: '工长', count: 28 },
          { name: '装修监理', count: 23 },
          { name: '橱柜设计', count: 18 }
        ]
      },
      {
        name: '物业管理',
        jobs: [
          { name: '物业经理', count: 34 },
          { name: '物业管家', count: 45 },
          { name: '维修工', count: 38 },
          { name: '保安', count: 56 },
          { name: '保洁', count: 43 }
        ]
      },
      {
        name: '建筑设计',
        jobs: [
          { name: '建筑设计师', count: 34 },
          { name: '结构设计', count: 28 },
          { name: '暖通设计', count: 19 },
          { name: '电气设计', count: 21 },
          { name: '给排水设计', count: 16 }
        ]
      },
      {
        name: '房地产规划开发',
        jobs: [
          { name: '房地产策划', count: 23 },
          { name: '土地拓展', count: 15 },
          { name: '开发报建', count: 18 },
          { name: '投资分析', count: 21 }
        ]
      }
    ]
  },
  // 11. 物流/仓储/司机
  {
    name: '物流/仓储/司机',
    icon: '🚚',
    subCategories: [
      {
        name: '物流',
        jobs: [
          { name: '物流专员', count: 67 },
          { name: '物流经理', count: 34 },
          { name: '调度', count: 43 },
          { name: '快递员', count: 78 },
          { name: '配送员', count: 56 }
        ]
      },
      {
        name: '仓储',
        jobs: [
          { name: '仓管员', count: 78 },
          { name: '仓储主管', count: 34 },
          { name: '理货员', count: 56 },
          { name: '拣货员', count: 43 },
          { name: '仓库管理员', count: 45 }
        ]
      },
      {
        name: '司机',
        jobs: [
          { name: '司机', count: 89 },
          { name: '货运司机', count: 67 },
          { name: '网约车司机', count: 78 },
          { name: '班车司机', count: 34 },
          { name: '叉车司机', count: 45 }
        ]
      }
    ]
  },
  // 12. 医疗/健康
  {
    name: '医疗/健康',
    icon: '⚕️',
    subCategories: [
      {
        name: '医生',
        jobs: [
          { name: '内科医生', count: 34 },
          { name: '外科医生', count: 28 },
          { name: '全科医生', count: 23 },
          { name: '儿科医生', count: 19 },
          { name: '牙医', count: 16 }
        ]
      },
      {
        name: '护士',
        jobs: [
          { name: '护士', count: 56 },
          { name: '护师', count: 23 },
          { name: '护理', count: 45 },
          { name: 'ICU护士', count: 18 }
        ]
      },
      {
        name: '医药',
        jobs: [
          { name: '医药代表', count: 67 },
          { name: '药学', count: 34 },
          { name: '药品注册', count: 12 },
          { name: '临床监查员', count: 19 },
          { name: '药理研究员', count: 15 }
        ]
      },
      {
        name: '医疗器械',
        jobs: [
          { name: '医疗器械销售', count: 45 },
          { name: '医疗器械维修', count: 23 },
          { name: '临床工程师', count: 18 }
        ]
      },
      {
        name: '健康管理',
        jobs: [
          { name: '健康顾问', count: 23 },
          { name: '营养师', count: 18 },
          { name: '康复治疗师', count: 21 }
        ]
      }
    ]
  },
  // 13. 汽车
  {
    name: '汽车',
    icon: '🚗',
    subCategories: [
      {
        name: '汽车研发',
        jobs: [
          { name: '汽车设计', count: 34 },
          { name: '底盘工程师', count: 23 },
          { name: '动力工程师', count: 19 },
          { name: '车身工程师', count: 21 },
          { name: '三电工程师', count: 28, hot: true }
        ]
      },
      {
        name: '汽车销售',
        jobs: [
          { name: '汽车销售', count: 67 },
          { name: '展厅经理', count: 23 },
          { name: '二手车评估', count: 18 }
        ]
      },
      {
        name: '汽车维修',
        jobs: [
          { name: '机修工', count: 45 },
          { name: '钣金工', count: 32 },
          { name: '喷漆工', count: 28 },
          { name: '汽车电工', count: 23 }
        ]
      },
      {
        name: '新能源汽车',
        jobs: [
          { name: '电池工程师', count: 34, hot: true },
          { name: '电控工程师', count: 28, hot: true },
          { name: '充电桩运维', count: 19 }
        ]
      }
    ]
  },
  // 14. 采购/贸易
  {
    name: '采购/贸易',
    icon: '📦',
    subCategories: [
      {
        name: '采购',
        jobs: [
          { name: '采购专员', count: 56 },
          { name: '采购经理', count: 34 },
          { name: '供应商开发', count: 28 },
          { name: '战略采购', count: 19 }
        ]
      },
      {
        name: '贸易',
        jobs: [
          { name: '外贸专员', count: 45 },
          { name: '外贸经理', count: 28 },
          { name: '报关员', count: 19 },
          { name: '单证员', count: 23 }
        ]
      },
      {
        name: '跨境电商',
        jobs: [
          { name: '亚马逊运营', count: 38, hot: true },
          { name: '速卖通运营', count: 27 },
          { name: 'TikTok运营', count: 23, hot: true }
        ]
      }
    ]
  },
  // 15. 直播/影视/传媒
  {
    name: '直播/影视/传媒',
    icon: '📺',
    subCategories: [
      {
        name: '直播',
        jobs: [
          { name: '主播', count: 67, hot: true },
          { name: '带货主播', count: 78, hot: true },
          { name: '直播运营', count: 45 },
          { name: '场控', count: 23 }
        ]
      },
      {
        name: '影视',
        jobs: [
          { name: '编导', count: 34 },
          { name: '摄像', count: 32 },
          { name: '剪辑', count: 45 },
          { name: '后期制作', count: 28 }
        ]
      },
      {
        name: '传媒',
        jobs: [
          { name: '新媒体运营', count: 56 },
          { name: '内容运营', count: 43 },
          { name: '文案策划', count: 38 },
          { name: '媒介专员', count: 23 }
        ]
      },
      {
        name: '短视频',
        jobs: [
          { name: '短视频编导', count: 34 },
          { name: '短视频剪辑', count: 45 },
          { name: '抖音运营', count: 56, hot: true }
        ]
      }
    ]
  },
  // 16. 酒店/旅游
  {
    name: '酒店/旅游',
    icon: '🏨',
    subCategories: [
      {
        name: '酒店',
        jobs: [
          { name: '前台接待', count: 45 },
          { name: '客房服务', count: 38 },
          { name: '酒店管理', count: 28 },
          { name: '礼宾', count: 19 }
        ]
      },
      {
        name: '旅游',
        jobs: [
          { name: '导游', count: 34 },
          { name: '旅行顾问', count: 23 },
          { name: '计调', count: 19 },
          { name: '旅游策划', count: 16 }
        ]
      },
      {
        name: '会展',
        jobs: [
          { name: '会展策划', count: 23 },
          { name: '会议服务', count: 19 },
          { name: '活动执行', count: 21 }
        ]
      }
    ]
  },
  // 17. 设计/创意
  {
    name: '设计/创意',
    icon: '🎯',
    subCategories: [
      {
        name: '平面设计',
        jobs: [
          { name: '平面设计', count: 67 },
          { name: '品牌设计', count: 34 },
          { name: '包装设计', count: 28 },
          { name: '画册设计', count: 23 }
        ]
      },
      {
        name: 'UI/UX',
        jobs: [
          { name: 'UI设计', count: 56 },
          { name: 'UX设计', count: 34 },
          { name: '交互设计', count: 28 },
          { name: '产品设计', count: 32 }
        ]
      },
      {
        name: '工业设计',
        jobs: [
          { name: '产品造型', count: 23 },
          { name: '结构设计', count: 28 },
          { name: 'CMF设计', count: 16 }
        ]
      },
      {
        name: '服装设计',
        jobs: [
          { name: '服装设计', count: 34 },
          { name: '面料设计', count: 15 },
          { name: '配饰设计', count: 12 }
        ]
      }
    ]
  },
  // 18. 能源/化工/环保
  {
    name: '能源/化工/环保',
    icon: '⚡',
    subCategories: [
      {
        name: '能源',
        jobs: [
          { name: '新能源', count: 45, hot: true },
          { name: '光伏', count: 34 },
          { name: '风电', count: 28 },
          { name: '储能', count: 23, hot: true }
        ]
      },
      {
        name: '化工',
        jobs: [
          { name: '化工工艺', count: 34 },
          { name: '化学分析', count: 28 },
          { name: '研发工程师', count: 23 }
        ]
      },
      {
        name: '环保',
        jobs: [
          { name: '环境工程', count: 34 },
          { name: '环评', count: 19 },
          { name: '水处理', count: 23 }
        ]
      }
    ]
  },
  // 19. 咨询/法律/翻译
  {
    name: '咨询/法律/翻译',
    icon: '⚖️',
    subCategories: [
      {
        name: '咨询',
        jobs: [
          { name: '咨询顾问', count: 45 },
          { name: '战略咨询', count: 28 },
          { name: '管理咨询', count: 34 },
          { name: '人力咨询', count: 19 }
        ]
      },
      {
        name: '法律',
        jobs: [
          { name: '律师', count: 34 },
          { name: '法务', count: 45 },
          { name: '合规', count: 23 },
          { name: '知识产权', count: 16 }
        ]
      },
      {
        name: '翻译',
        jobs: [
          { name: '英语翻译', count: 34 },
          { name: '日语翻译', count: 19 },
          { name: '韩语翻译', count: 15 },
          { name: '同声传译', count: 8, hot: true }
        ]
      }
    ]
  },
  // 20. 高级管理
  {
    name: '高级管理',
    icon: '👔',
    subCategories: [
      {
        name: '高层管理',
        jobs: [
          { name: 'CEO', count: 5 },
          { name: 'COO', count: 4 },
          { name: 'CFO', count: 6 },
          { name: 'CTO', count: 8 },
          { name: '总经理', count: 12 }
        ]
      },
      {
        name: '部门管理',
        jobs: [
          { name: '部门总监', count: 23 },
          { name: '高级经理', count: 34 },
          { name: '区域经理', count: 28 }
        ]
      },
      {
        name: '项目管理',
        jobs: [
          { name: '项目经理', count: 56 },
          { name: '项目总监', count: 23 },
          { name: 'PMO', count: 15 }
        ]
      }
    ]
  }
])



// ===== 职位卡片数据（全量覆盖）=====
const jobCards = ref([
  // ==================== 互联网/AI ====================
  // ---- 后端开发 ----
  // Java
  {
    id: 1001,
    position: 'Java开发工程师',
    company: '腾讯',
    salary: '25k-40k',
    location: '深圳',
    tags: ['Java', 'Spring Boot', 'MySQL'],
    deadline: '2026.04.30',
    hot: true,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: 'Java'
  },
  {
    id: 1002,
    position: 'Java高级工程师',
    company: '字节跳动',
    salary: '35k-55k',
    location: '北京',
    tags: ['Java', '微服务', '高并发'],
    deadline: '2026.05.15',
    hot: true,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: 'Java'
  },
  {
    id: 1003,
    position: 'Java架构师',
    company: '阿里巴巴',
    salary: '45k-70k',
    location: '杭州',
    tags: ['Java', '架构设计', '分布式'],
    deadline: '2026.06.01',
    hot: true,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: 'Java'
  },
  // Python
  {
    id: 1004,
    position: 'Python后端开发',
    company: '字节跳动',
    salary: '28k-45k',
    location: '上海',
    tags: ['Python', 'Django', 'Flask'],
    deadline: '2026.05.20',
    hot: true,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: 'Python'
  },
  {
    id: 1005,
    position: 'Python开发工程师',
    company: '美团',
    salary: '22k-38k',
    location: '北京',
    tags: ['Python', 'FastAPI', 'MongoDB'],
    deadline: '2026.06.10',
    hot: false,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: 'Python'
  },
  // Go
  {
    id: 1006,
    position: 'Go开发工程师',
    company: '字节跳动',
    salary: '30k-48k',
    location: '北京',
    tags: ['Go', '微服务', 'K8s'],
    deadline: '2026.05.25',
    hot: true,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: 'Go'
  },
  {
    id: 1007,
    position: 'Go后端开发',
    company: '哔哩哔哩',
    salary: '26k-42k',
    location: '上海',
    tags: ['Go', 'gin', 'Redis'],
    deadline: '2026.07.01',
    hot: false,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: 'Go'
  },
  // C++
  {
    id: 1008,
    position: 'C++开发工程师',
    company: '腾讯',
    salary: '27k-45k',
    location: '深圳',
    tags: ['C++', 'Linux', '网络编程'],
    deadline: '2026.05.10',
    hot: true,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: 'C++'
  },
  {
    id: 1009,
    position: 'C++后端开发',
    company: '百度',
    salary: '24k-40k',
    location: '北京',
    tags: ['C++', '搜索引擎', '算法'],
    deadline: '2026.06.15',
    hot: false,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: 'C++'
  },
  // Node.js
  {
    id: 1010,
    position: 'Node.js开发',
    company: '阿里巴巴',
    salary: '22k-38k',
    location: '杭州',
    tags: ['Node.js', 'Express', 'MongoDB'],
    deadline: '2026.07.20',
    hot: false,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: 'Node.js'
  },
  // 全栈
  {
    id: 1011,
    position: '全栈工程师',
    company: '美团',
    salary: '28k-45k',
    location: '北京',
    tags: ['Vue', 'Node.js', 'MySQL'],
    deadline: '2026.05.30',
    hot: true,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: '全栈工程师'
  },
  // 架构师
  {
    id: 1012,
    position: '后端架构师',
    company: '字节跳动',
    salary: '50k-80k',
    location: '北京',
    tags: ['架构', '高并发', '分布式'],
    deadline: '2026.06.01',
    hot: true,
    category: '互联网/AI',
    subCategory: '后端开发',
    jobType: '架构师'
  },

  // ---- 前端开发 ----
  // Vue.js
  {
    id: 2001,
    position: 'Vue前端开发',
    company: '阿里巴巴',
    salary: '20k-35k',
    location: '杭州',
    tags: ['Vue3', 'Element Plus', 'Vite'],
    deadline: '2026.05.15',
    hot: true,
    category: '互联网/AI',
    subCategory: '前端开发',
    jobType: 'Vue.js'
  },
  {
    id: 2002,
    position: '高级前端工程师',
    company: '腾讯',
    salary: '30k-50k',
    location: '深圳',
    tags: ['Vue', 'React', 'Node.js'],
    deadline: '2026.04.30',
    hot: true,
    category: '互联网/AI',
    subCategory: '前端开发',
    jobType: 'Vue.js'
  },
  // React
  {
    id: 2003,
    position: 'React前端开发',
    company: '字节跳动',
    salary: '25k-42k',
    location: '上海',
    tags: ['React', 'Next.js', 'TypeScript'],
    deadline: '2026.05.20',
    hot: true,
    category: '互联网/AI',
    subCategory: '前端开发',
    jobType: 'React'
  },
  {
    id: 2004,
    position: 'React开发工程师',
    company: '美团',
    salary: '23k-38k',
    location: '北京',
    tags: ['React', 'Redux', 'Webpack'],
    deadline: '2026.06.10',
    hot: false,
    category: '互联网/AI',
    subCategory: '前端开发',
    jobType: 'React'
  },
  // JavaScript
  {
    id: 2005,
    position: 'JavaScript开发',
    company: '百度',
    salary: '18k-30k',
    location: '北京',
    tags: ['JavaScript', 'jQuery', 'Ajax'],
    deadline: '2026.07.01',
    hot: false,
    category: '互联网/AI',
    subCategory: '前端开发',
    jobType: 'JavaScript'
  },
  // TypeScript
  {
    id: 2006,
    position: 'TypeScript开发',
    company: '微软',
    salary: '28k-48k',
    location: '苏州',
    tags: ['TypeScript', 'React', 'Node.js'],
    deadline: '2026.08.01',
    hot: true,
    category: '互联网/AI',
    subCategory: '前端开发',
    jobType: 'TypeScript'
  },
  // 小程序
  {
    id: 2007,
    position: '小程序开发',
    company: '京东',
    salary: '20k-35k',
    location: '北京',
    tags: ['微信小程序', 'Vue', 'Taro'],
    deadline: '2026.06.15',
    hot: true,
    category: '互联网/AI',
    subCategory: '前端开发',
    jobType: '小程序开发'
  },
  // 鸿蒙
  {
    id: 2008,
    position: '鸿蒙开发工程师',
    company: '华为',
    salary: '25k-45k',
    location: '东莞',
    tags: ['鸿蒙', 'ArkTS', '鸿蒙应用'],
    deadline: '2026.09.01',
    hot: true,
    category: '互联网/AI',
    subCategory: '前端开发',
    jobType: '鸿蒙开发'
  },

  // ---- 测试 ----
  // 软件测试
  {
    id: 3001,
    position: '软件测试工程师',
    company: '京东',
    salary: '15k-25k',
    location: '北京',
    tags: ['功能测试', '性能测试', 'JIRA'],
    deadline: '2026.06.01',
    hot: false,
    category: '互联网/AI',
    subCategory: '测试',
    jobType: '软件测试'
  },
  {
    id: 3002,
    position: '高级测试工程师',
    company: '腾讯',
    salary: '22k-38k',
    location: '深圳',
    tags: ['自动化测试', '性能测试', 'Python'],
    deadline: '2026.05.20',
    hot: true,
    category: '互联网/AI',
    subCategory: '测试',
    jobType: '软件测试'
  },
  // 自动化测试
  {
    id: 3003,
    position: '自动化测试',
    company: '字节跳动',
    salary: '20k-35k',
    location: '北京',
    tags: ['Selenium', 'Python', 'CI/CD'],
    deadline: '2026.06.15',
    hot: true,
    category: '互联网/AI',
    subCategory: '测试',
    jobType: '自动化测试'
  },
  // 性能测试
  {
    id: 3004,
    position: '性能测试工程师',
    company: '阿里巴巴',
    salary: '20k-35k',
    location: '杭州',
    tags: ['JMeter', 'LoadRunner', '性能分析'],
    deadline: '2026.07.01',
    hot: false,
    category: '互联网/AI',
    subCategory: '测试',
    jobType: '性能测试'
  },
  // 测试开发
  {
    id: 3005,
    position: '测试开发',
    company: '美团',
    salary: '22k-38k',
    location: '北京',
    tags: ['Python', '自动化框架', 'CI/CD'],
    deadline: '2026.06.20',
    hot: true,
    category: '互联网/AI',
    subCategory: '测试',
    jobType: '测试开发'
  },
  // 渗透测试
  {
    id: 3006,
    position: '渗透测试工程师',
    company: '奇安信',
    salary: '18k-32k',
    location: '北京',
    tags: ['网络安全', '渗透测试', '漏洞挖掘'],
    deadline: '2026.08.01',
    hot: true,
    category: '互联网/AI',
    subCategory: '测试',
    jobType: '渗透测试'
  },

  // ---- 算法 ----
  // 推荐算法
  {
    id: 4001,
    position: '推荐算法工程师',
    company: '字节跳动',
    salary: '35k-60k',
    location: '北京',
    tags: ['推荐系统', 'Python', 'TensorFlow'],
    deadline: '2026.05.15',
    hot: true,
    category: '互联网/AI',
    subCategory: '算法',
    jobType: '推荐算法'
  },
  // 大模型算法
  {
    id: 4002,
    position: '大模型算法工程师',
    company: '阿里巴巴',
    salary: '40k-70k',
    location: '杭州',
    tags: ['LLM', 'PyTorch', 'NLP'],
    deadline: '2026.06.01',
    hot: true,
    category: '互联网/AI',
    subCategory: '算法',
    jobType: '大模型算法'
  },
  // 图像算法
  {
    id: 4003,
    position: '计算机视觉算法',
    company: '商汤科技',
    salary: '30k-55k',
    location: '上海',
    tags: ['CV', '图像识别', 'PyTorch'],
    deadline: '2026.07.01',
    hot: true,
    category: '互联网/AI',
    subCategory: '算法',
    jobType: '图像算法'
  },
  // NLP
  {
    id: 4004,
    position: 'NLP算法工程师',
    company: '百度',
    salary: '32k-58k',
    location: '北京',
    tags: ['NLP', 'BERT', 'Python'],
    deadline: '2026.06.15',
    hot: true,
    category: '互联网/AI',
    subCategory: '算法',
    jobType: '自然语言处理'
  },
  // 机器学习
  {
    id: 4005,
    position: '机器学习工程师',
    company: '腾讯',
    salary: '28k-50k',
    location: '深圳',
    tags: ['机器学习', 'Python', 'Scikit-learn'],
    deadline: '2026.05.30',
    hot: true,
    category: '互联网/AI',
    subCategory: '算法',
    jobType: '机器学习'
  },

  // ---- 产品 ----
  // 产品经理
  {
    id: 5001,
    position: '产品经理',
    company: '腾讯',
    salary: '20k-38k',
    location: '深圳',
    tags: ['产品设计', '用户研究', 'Axure'],
    deadline: '2026.05.10',
    hot: true,
    category: '互联网/AI',
    subCategory: '产品',
    jobType: '产品经理'
  },
  {
    id: 5002,
    position: 'AI产品经理',
    company: '字节跳动',
    salary: '28k-48k',
    location: '北京',
    tags: ['AI产品', '大模型', '产品规划'],
    deadline: '2026.06.01',
    hot: true,
    category: '互联网/AI',
    subCategory: '产品',
    jobType: 'AI产品'
  },
  // 数据产品
  {
    id: 5003,
    position: '数据产品经理',
    company: '阿里巴巴',
    salary: '25k-42k',
    location: '杭州',
    tags: ['数据产品', 'BI', '数据分析'],
    deadline: '2026.07.01',
    hot: false,
    category: '互联网/AI',
    subCategory: '产品',
    jobType: '数据产品'
  },

  // ---- 数据分析 ----
  {
    id: 6001,
    position: '数据分析师',
    company: '字节跳动',
    salary: '18k-32k',
    location: '北京',
    tags: ['SQL', 'Python', 'Tableau'],
    deadline: '2026.05.20',
    hot: true,
    category: '互联网/AI',
    subCategory: '数据分析',
    jobType: '数据分析师'
  },
  {
    id: 6002,
    position: '数据开发工程师',
    company: '阿里巴巴',
    salary: '22k-40k',
    location: '杭州',
    tags: ['Spark', 'Hadoop', '数据仓库'],
    deadline: '2026.06.15',
    hot: false,
    category: '互联网/AI',
    subCategory: '数据分析',
    jobType: '数据开发'
  },

  // ==================== 电子/通信/硬件 ====================
  // 嵌入式
  {
    id: 7001,
    position: '嵌入式工程师',
    company: '华为',
    salary: '18k-32k',
    location: '东莞',
    tags: ['C', 'RTOS', 'Linux'],
    deadline: '2026.07.01',
    hot: true,
    category: '电子/通信/硬件',
    subCategory: '硬件开发',
    jobType: '嵌入式'
  },
  {
    id: 7002,
    position: '嵌入式软件工程师',
    company: '大疆',
    salary: '20k-38k',
    location: '深圳',
    tags: ['C++', 'ARM', '嵌入式Linux'],
    deadline: '2026.08.01',
    hot: true,
    category: '电子/通信/硬件',
    subCategory: '硬件开发',
    jobType: '嵌入式'
  },
  // 硬件工程师
  {
    id: 7003,
    position: '硬件工程师',
    company: '小米',
    salary: '16k-30k',
    location: '北京',
    tags: ['原理图', 'PCB', 'ARM'],
    deadline: '2026.06.15',
    hot: false,
    category: '电子/通信/硬件',
    subCategory: '硬件开发',
    jobType: '硬件工程师'
  },
  // FPGA
  {
    id: 7004,
    position: 'FPGA工程师',
    company: '中兴',
    salary: '20k-38k',
    location: '南京',
    tags: ['FPGA', 'Verilog', '数字电路'],
    deadline: '2026.09.01',
    hot: true,
    category: '电子/通信/硬件',
    subCategory: '硬件开发',
    jobType: 'FPGA'
  },
  // 5G
  {
    id: 7005,
    position: '5G通信工程师',
    company: '华为',
    salary: '18k-32k',
    location: '上海',
    tags: ['5G', '通信协议', '网络优化'],
    deadline: '2026.08.15',
    hot: true,
    category: '电子/通信/硬件',
    subCategory: '通信',
    jobType: '5G'
  },
  // 芯片设计
  {
    id: 7006,
    position: '芯片设计工程师',
    company: '海思',
    salary: '30k-55k',
    location: '深圳',
    tags: ['芯片设计', 'Verilog', 'SoC'],
    deadline: '2026.10.01',
    hot: true,
    category: '电子/通信/硬件',
    subCategory: '半导体',
    jobType: '芯片设计'
  },

  // ==================== 金融 ====================
  // 量化
  {
    id: 8001,
    position: '量化研究员',
    company: '幻方量化',
    salary: '35k-60k',
    location: '上海',
    tags: ['Python', '金融数学', '统计'],
    deadline: '2026.07.01',
    hot: true,
    category: '金融',
    subCategory: '量化',
    jobType: '量化研究员'
  },
  {
    id: 8002,
    position: '量化开发',
    company: '九坤投资',
    salary: '30k-55k',
    location: '北京',
    tags: ['C++', 'Python', '交易系统'],
    deadline: '2026.08.01',
    hot: true,
    category: '金融',
    subCategory: '量化',
    jobType: '量化开发'
  },
  // 风控
  {
    id: 8003,
    position: '风控模型工程师',
    company: '蚂蚁集团',
    salary: '25k-45k',
    location: '杭州',
    tags: ['风控模型', '机器学习', 'Python'],
    deadline: '2026.06.15',
    hot: true,
    category: '金融',
    subCategory: '风控',
    jobType: '风控模型'
  },
  // 金融科技
  {
    id: 8004,
    position: '区块链开发',
    company: '微众银行',
    salary: '28k-50k',
    location: '深圳',
    tags: ['区块链', '智能合约', 'Solidity'],
    deadline: '2026.09.01',
    hot: true,
    category: '金融',
    subCategory: '金融科技',
    jobType: '区块链'
  },

  // ==================== 市场/销售/商务 ====================
  // 销售
  {
    id: 9001,
    position: '大客户销售',
    company: '阿里巴巴',
    salary: '15k-30k+提成',
    location: '北京',
    tags: ['大客户', 'SaaS', '解决方案'],
    deadline: '2026.06.01',
    hot: true,
    category: '市场/销售/商务',
    subCategory: '销售',
    jobType: '大客户销售'
  },
  {
    id: 9002,
    position: '销售代表',
    company: '美团',
    salary: '10k-20k+提成',
    location: '上海',
    tags: ['地推', 'BD', '客户开发'],
    deadline: '2026.07.01',
    hot: false,
    category: '市场/销售/商务',
    subCategory: '销售',
    jobType: '销售代表'
  },
  // 市场营销
  {
    id: 9003,
    position: '市场营销专员',
    company: '字节跳动',
    salary: '12k-22k',
    location: '北京',
    tags: ['品牌策划', '活动执行', '市场调研'],
    deadline: '2026.05.30',
    hot: false,
    category: '市场/销售/商务',
    subCategory: '市场营销',
    jobType: '市场营销'
  },

  // ==================== 人事/财务/行政 ====================
  // HR
  {
    id: 10001,
    position: 'HRBP',
    company: '腾讯',
    salary: '18k-32k',
    location: '深圳',
    tags: ['HRBP', '组织发展', '员工关系'],
    deadline: '2026.06.10',
    hot: true,
    category: '人事/财务/行政',
    subCategory: '人力资源',
    jobType: 'HRBP'
  },
  // 财务
  {
    id: 10002,
    position: '会计',
    company: '阿里巴巴',
    salary: '12k-22k',
    location: '杭州',
    tags: ['总账', '财务报表', '税务'],
    deadline: '2026.07.01',
    hot: false,
    category: '人事/财务/行政',
    subCategory: '财务',
    jobType: '会计'
  },

  // ==================== 房地产/建筑 ====================
  // 施工员
  {
    id: 11001,
    position: '施工员',
    company: '中建集团',
    salary: '8k-15k',
    location: '全国',
    tags: ['施工管理', '现场协调', '安全监督'],
    deadline: '2026.08.01',
    hot: false,
    category: '房地产/建筑',
    subCategory: '工程管理',
    jobType: '施工员'
  },
  // 项目经理
  {
    id: 11002,
    position: '项目经理',
    company: '万科',
    salary: '18k-35k',
    location: '深圳',
    tags: ['项目管理', '施工管理', '团队协调'],
    deadline: '2026.09.01',
    hot: true,
    category: '房地产/建筑',
    subCategory: '工程管理',
    jobType: '项目经理'
  },
  // 造价工程师
  {
    id: 11003,
    position: '造价工程师',
    company: '碧桂园',
    salary: '12k-25k',
    location: '佛山',
    tags: ['工程造价', '预算', '成本控制'],
    deadline: '2026.07.15',
    hot: false,
    category: '房地产/建筑',
    subCategory: '工程造价',
    jobType: '造价工程师'
  },
  // 室内设计
  {
    id: 11004,
    position: '室内设计师',
    company: '尚层装饰',
    salary: '10k-22k',
    location: '北京',
    tags: ['室内设计', '3D Max', '施工图'],
    deadline: '2026.08.20',
    hot: false,
    category: '房地产/建筑',
    subCategory: '装饰装修',
    jobType: '室内设计'
  },

  // ==================== 物流/仓储 ====================
  {
    id: 12001,
    position: '物流专员',
    company: '京东物流',
    salary: '7k-14k',
    location: '北京',
    tags: ['物流管理', '仓储', '配送'],
    deadline: '2026.07.01',
    hot: false,
    category: '物流/仓储/司机',
    subCategory: '物流',
    jobType: '物流专员'
  },
  {
    id: 12002,
    position: '仓库管理员',
    company: '顺丰速运',
    salary: '6k-12k',
    location: '上海',
    tags: ['仓储管理', 'WMS', '盘点'],
    deadline: '2026.08.01',
    hot: false,
    category: '物流/仓储/司机',
    subCategory: '仓储',
    jobType: '仓管员'
  },

  // ==================== 教育培训 ====================
  {
    id: 13001,
    position: '英语教师',
    company: '新东方',
    salary: '10k-20k',
    location: '北京',
    tags: ['英语教学', '雅思', '托福'],
    deadline: '2026.07.15',
    hot: true,
    category: '教育培训',
    subCategory: '教师',
    jobType: '语数外教师'
  },
  {
    id: 13002,
    position: '课程顾问',
    company: '好未来',
    salary: '8k-18k+提成',
    location: '上海',
    tags: ['课程销售', '客户咨询', '家长沟通'],
    deadline: '2026.08.01',
    hot: true,
    category: '教育培训',
    subCategory: '课程顾问',
    jobType: '课程顾问'
  },

  // ==================== 医疗/健康 ====================
  {
    id: 14001,
    position: '护士',
    company: '协和医院',
    salary: '8k-16k',
    location: '北京',
    tags: ['护理', '静脉输液', '病房管理'],
    deadline: '2026.09.01',
    hot: false,
    category: '医疗/健康',
    subCategory: '护士',
    jobType: '护士'
  },
  {
    id: 14002,
    position: '医药代表',
    company: '辉瑞',
    salary: '12k-25k+提成',
    location: '上海',
    tags: ['医药销售', '医院开发', '学术推广'],
    deadline: '2026.08.15',
    hot: true,
    category: '医疗/健康',
    subCategory: '医药',
    jobType: '医药代表'
  },

  // ==================== 汽车 ====================
  {
    id: 15001,
    position: '电池工程师',
    company: '宁德时代',
    salary: '15k-30k',
    location: '宁德',
    tags: ['锂电池', 'BMS', '电芯'],
    deadline: '2026.09.01',
    hot: true,
    category: '汽车',
    subCategory: '新能源汽车',
    jobType: '电池工程师'
  },
  {
    id: 15002,
    position: '汽车销售顾问',
    company: '特斯拉',
    salary: '10k-25k+提成',
    location: '北京',
    tags: ['汽车销售', '试驾', '客户服务'],
    deadline: '2026.08.01',
    hot: true,
    category: '汽车',
    subCategory: '汽车销售',
    jobType: '汽车销售'
  },

  // ==================== 直播/影视 ====================
  {
    id: 16001,
    position: '带货主播',
    company: '交个朋友',
    salary: '15k-40k+提成',
    location: '杭州',
    tags: ['直播带货', '抖音', '口播'],
    deadline: '2026.07.01',
    hot: true,
    category: '直播/影视/传媒',
    subCategory: '直播',
    jobType: '带货主播'
  },
  {
    id: 16002,
    position: '视频剪辑',
    company: 'B站',
    salary: '10k-22k',
    location: '上海',
    tags: ['PR', 'AE', '短视频'],
    deadline: '2026.08.15',
    hot: false,
    category: '直播/影视/传媒',
    subCategory: '影视',
    jobType: '剪辑'
  }
])







// 根据筛选条件过滤职位 
const filteredJobCards = computed(() => {
  let filtered = jobCards.value.filter(card => 
    card.category === selectedCategory.value &&
    card.subCategory === selectedSubCategory.value
  )
  
  if (selectedJob.value !== '全部') {
    filtered = filtered.filter(card => card.jobType === selectedJob.value)
  }
  
  return filtered
})

// 获取当前选中子分类下的职位列表
const currentJobsList = computed(() => {
  const cat = jobCategories.value.find(c => c.name === selectedCategory.value)
  if (!cat) return []
  const sub = cat.subCategories.find(s => s.name === selectedSubCategory.value)
  return sub?.jobs || []
})


const selectedCategory = ref('互联网/AI')
const selectedSubCategory = ref('后端开发')
const selectedJob = ref('全部')










// ===== 竞赛信息大厅数据 =====
const contestTags = ref([
  { name: '互联网+', icon: '💡', count: 24, color: '#f97316' },
  { name: '挑战杯', icon: '🏆', count: 18, color: '#3b82f6' },
  { name: '数学建模', icon: '📊', count: 32, color: '#10b981' },
  { name: 'ACM程序设计', icon: '💻', count: 15, color: '#8b5cf6' },
  { name: '英语竞赛', icon: '📝', count: 12, color: '#ec4899' },
  { name: '蓝桥杯', icon: '🖥️', count: 21, color: '#6366f1' },
  { name: '计算机设计大赛', icon: '🎨', count: 9, color: '#14b8a6' },
  { name: '创新创业', icon: '🚀', count: 16, color: '#f43f5e' }
])

const selectedTag = ref('互联网+')
const contestKeyword = ref('')

// 赛事卡片数据
const contestList = ref([
  {
    id: 1,
    title: '第九届中国国际“互联网+”大学生创新创业大赛',
    tags: ['互联网+', '创新创业'],
    level: '国家级',
    organize: '教育部',
    startDate: '2026-03-15',
    endDate: '2026-07-20',
    status: '报名中',
    statusColor: '#10b981',
    requirements: ['商业计划书', '项目演示视频', '团队介绍'],
    abilities: ['创新思维', '团队协作', '商业分析'],
    participants: '3-5人/队',
    link: '/contest/1'
  },
  {
    id: 2,
    title: '第十八届“挑战杯”全国大学生课外学术科技作品竞赛',
    tags: ['挑战杯'],
    level: '国家级',
    organize: '共青团中央',
    startDate: '2026-04-01',
    endDate: '2026-10-15',
    status: '即将开始',
    statusColor: '#f97316',
    requirements: ['学术论文', '调研报告', '答辩PPT'],
    abilities: ['学术研究', '数据分析', '论文写作'],
    participants: '1-3人/队',
    link: '/contest/2'
  },
  {
    id: 3,
    title: '2026年高教社杯全国大学生数学建模竞赛',
    tags: ['数学建模'],
    level: '国家级',
    organize: '中国工业与应用数学学会',
    startDate: '2026-09-10',
    endDate: '2026-09-13',
    status: '备赛中',
    statusColor: '#8b5cf6',
    requirements: ['数学模型', '论文', '程序代码'],
    abilities: ['数学建模', '编程', '论文写作'],
    participants: '3人/队',
    link: '/contest/3'
  },
  {
    id: 4,
    title: '第47届ACM国际大学生程序设计竞赛亚洲区域赛',
    tags: ['ACM程序设计'],
    level: '国际级',
    organize: 'ACM',
    startDate: '2026-10-20',
    endDate: '2026-10-21',
    status: '预报名',
    statusColor: '#3b82f6',
    requirements: ['算法设计', '编程实现'],
    abilities: ['算法思维', 'C++/Java', '团队配合'],
    participants: '3人/队',
    link: '/contest/4'
  },
  {
    id: 5,
    title: '2026年全国大学生英语竞赛（NECCS）',
    tags: ['英语竞赛'],
    level: '国家级',
    organize: '国际英语外语教师协会',
    startDate: '2026-04-15',
    endDate: '2026-05-20',
    status: '报名中',
    statusColor: '#10b981',
    requirements: ['听力', '阅读', '写作', '翻译'],
    abilities: ['英语综合能力'],
    participants: '个人',
    link: '/contest/5'
  },
  {
    id: 6,
    title: '第十六届蓝桥杯全国软件和信息技术专业人才大赛',
    tags: ['蓝桥杯'],
    level: '国家级',
    organize: '工业和信息化部',
    startDate: '2026-03-25',
    endDate: '2026-06-10',
    status: '报名中',
    statusColor: '#10b981',
    requirements: ['编程能力', '算法设计'],
    abilities: ['Java/C++/Python', '算法'],
    participants: '个人',
    link: '/contest/6'
  }
])

// 根据选中标签过滤赛事
const filteredContests = computed(() => {
  if (selectedTag.value === '全部') return contestList.value
  return contestList.value.filter(contest => contest.tags.includes(selectedTag.value))
})

// 处理标签点击
const selectTag = (tagName) => {
  selectedTag.value = tagName
}

// 处理搜索
const handleSearch = () => {
  console.log('搜索:', contestKeyword.value)
  // 实现搜索逻辑
}

// 处理卡片点击
const goToContestDetail = (contest) => {
  console.log('查看赛事:', contest)
  // router.push(`/contest/${contest.id}`)
}




// 日期格式化
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getMonth()+1}.${date.getDate()}`
}

// 计算剩余天数
const getRemainingDays = (endDateStr) => {
  const end = new Date(endDateStr)
  const now = new Date()
  const diff = Math.ceil((end - now) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 0
}
















const goToJobDetail = (job) => {
  console.log('查看职位：', job)
  // 可以跳转到详情页
  // router.push(`/job/${job.id}`)
}






// 页面跳转函数
const goToPage = (path) => {
  router.push(path)
}





import { ref, onMounted, onUnmounted,watch,computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const route = useRoute()

const userRole = ref('student')
const userName = ref('加载中...')
const userAccount = ref('加载中...') 
const userOrg = ref('加载中...') 
const userAvatar = ref('https://api.dicebear.com/7.x/avataaars/svg?seed=Felix')

const activeMenu = ref('home')
const isDropdownOpen = ref(false)

const isResumeDialogOpen = ref(false)
const resumePrompt = ref('') 
const isGenerating = ref(false)
const generatedResume = ref('')

const toggleDropdown = () => isDropdownOpen.value = !isDropdownOpen.value
const closeDropdown = () => isDropdownOpen.value = false

const fetchHeaderUserInfo = async () => {
  try {
    const res = await request.get('/api/user/info')
    if (res) {
      userName.value = res.nickname || res.nickName || '未命名用户'
      userAccount.value = res.phone || '未绑定手机号'
      userOrg.value = res.org || (userRole.value === 'student' ? '未填写学校' : '未填写企业')
      userAvatar.value = res.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'
      
      localStorage.setItem('userName', userName.value)
      localStorage.setItem('userOrg', userOrg.value)
    }
  } catch (error) {
    console.error('获取顶部导航栏信息失败:', error)
    fallbackToLocalData()
  }
}

const fallbackToLocalData = () => {
  const savedOrg = localStorage.getItem('userOrg')
  userOrg.value = savedOrg || (userRole.value === 'student' ? '未知学校' : '未知企业')
  const savedName = localStorage.getItem('userName')
  userName.value = savedName || (userRole.value === 'enterprise' ? 'HR' : '学生用户')
  userAccount.value = '暂无网络'
}

onMounted(() => {
  userRole.value = localStorage.getItem('userRole') || 'student'
  fetchHeaderUserInfo()
})

// 🌟 终极修复：只在有 edit=true 的时候执行恢复，去掉了害人的 else 分支
watch(() => route.query.edit, (newVal) => {
  if (newVal === 'true') {
    activeMenu.value = 'profile' 
    // 1. 先把文字填进去
    resumePrompt.value = sessionStorage.getItem('tempResumePrompt') || '' 
    // 2. 然后再打开弹窗
    isResumeDialogOpen.value = true 
    // 3. 最后清理 URL（因为我们去掉了 else，现在清理 URL 绝对安全了！）
    router.replace({ path: '/dashboard' }) 
  }
}, { immediate: true })

// 点击一键生成按钮手动打开弹窗时，确保清空数据
const openResumeDialog = () => {
  resumePrompt.value = '' 
  sessionStorage.removeItem('tempResumePrompt')
  isResumeDialogOpen.value = true
}

// 手动点击关闭或取消时，彻底销毁数据
const closeResumeDialog = () => {
  if (isGenerating.value) return 
  isResumeDialogOpen.value = false
  resumePrompt.value = '' 
  sessionStorage.removeItem('tempResumePrompt') 
}

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

const publishJob = () => alert('弹出发布招聘信息的表单（待开发）')




// 轮播图数据
const carouselSlides = ref([
  {
    image: '/images11/photo-1522071820081-009f0129c71c.jpg',
    title: '2026春季大厂校园招聘全面开启',
    description: '提前批内推 / 简历辅导 / 面试真题全解析，助你拿下心仪 Offer',
    link: '/recruitment'
  },
  {
    image: '/images11/photo-1517245386807-bb43f82c33c4.jpg',
    title: '考研倒计时：冲刺阶段复习规划',
    description: '名师押题 / 真题精讲 / 模拟考试，最后三个月如何高效提分',
    link: '/graduate-exam'
  },
  {
    image: '/images11/photo-1434030216411-0b793f4b4173.jpg',
    title: '留学申请季：全球TOP50院校指南',
    description: '文书写作 / 推荐信 / 面试辅导，一站式留学服务',
    link: '/abroad'
  },
  {
    image: '/images11/photo-1524178232363-1fb2b075b655.jpg',
    title: '互联网大厂实习内推专场',
    description: '字节 / 腾讯 / 阿里 / 美团，50+岗位等你来投',
    link: '/internship'
  },
  {
    image: '/images11/photo-1552664730-d307ca884978.jpg',
    title: '竞赛集训营：冲击国奖',
    description: '互联网+ / 挑战杯 / 数学建模，金牌导师带队',
    link: '/competition'
  }
])

const currentSlide = ref(0)
let slideInterval = null

// 自动轮播
const startAutoPlay = () => {
  slideInterval = setInterval(() => {
    nextSlide()
  }, 5000) // 5秒切换一次
}

// 下一张
const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % carouselSlides.value.length
}

// 上一张
const prevSlide = () => {
  currentSlide.value = (currentSlide.value - 1 + carouselSlides.value.length) % carouselSlides.value.length
}

// 跳转到链接
const goToLink = (link) => {
  router.push(link)
}

// 组件挂载时开始自动轮播
onMounted(() => {
  startAutoPlay()
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (slideInterval) {
    clearInterval(slideInterval)
  }
})





const homeData = ref([
  {
    title: '🎓 学业深造推荐',
    target: 'matrix',
    items: [
      { title: '2026考研数学全程复习规划，如何避坑？', type: 'video', typeText: '视频', author: '考研名师讲堂', color: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)' },
      { title: 'QS前50计算机名校申请时间轴及材料准备', type: 'article', typeText: '文章', author: '留学资讯站', color: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)' },
      { title: '工科保研夏令营面试硬核经验分享', type: 'video', typeText: '视频', author: '学霸研习社', color: 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)' },
      { title: '如何通过大数据筛选出最适合你的院校？', type: 'article', typeText: '文章', author: '平台官方', color: 'linear-gradient(135deg, #cfd9df 0%, #e2ebf0 100%)' }
    ]
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
    items: [
      { title: '美赛(MCM/ICM) O奖团队经验分享：模型与论文双修', type: 'video', typeText: '视频', author: '数模交流圈', color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' },
      { title: '"挑战杯"国奖项目是如何进行答辩路演的？', type: 'video', typeText: '视频', author: '创赛俱乐部', color: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)' },
      { title: '2026全国大学生计算机设计大赛赛道详解', type: 'article', typeText: '文章', author: '赛事公告栏', color: 'linear-gradient(135deg, #ebc0fd 0%, #d9ded8 100%)' },
      { title: '蓝桥杯/ACM 算法竞赛必刷的 50 道核心题', type: 'article', typeText: '文章', author: '算法进阶', color: 'linear-gradient(135deg, #96fbc4 0%, #f9f586 100%)' }
    ]
  },
  {
    title: '📄 热门简历模板 (定制能力画像)',
    target: 'profile',
    items: [
      { title: '互联网大厂技术岗专用 - 极简极客风模板', type: 'template', typeText: '模板', desc: '适用：后端/前端开发、算法工程师', color: '#334155' },
      { title: '商科/运营岗通用 - 清爽时间轴设计模板', type: 'template', typeText: '模板', desc: '适用：产品、运营、市场营销', color: '#0ea5e9' },
      { title: '应届生/实习生破冰模板 - 重点突出校园经历', type: 'template', typeText: '模板', desc: '适用：无太多实战经验的大一/大二学生', color: '#10b981' },
      { title: '设计/传媒岗创意模板 - 视觉冲击力强', type: 'template', typeText: '模板', desc: '适用：UI设计师、原画师、影视后期', color: '#8b5cf6' }
    ]
  }
])

const handleCardClick = (item) => {
  if (item.type === 'video') {
    router.push({ path: '/video', query: { title: item.title, author: item.author || '平台官方' } })
  } else if (item.type === 'article') {
    router.push({ path: '/article', query: { title: item.title, author: item.author || '专栏作家' } })
  } else if (item.type === 'template') {
    router.push({ path: '/resume-template', query: { title: item.title } })
  }
}

const generateResume = async () => {
  if (!resumePrompt.value.trim()) return
  
  isGenerating.value = true
  
  // 生成时记录留作“返回修改”的退路
  sessionStorage.setItem('tempResumePrompt', resumePrompt.value)
  
  try {
    const res = await request.post('/api/resume/zhipu/generate', {
      requirements: resumePrompt.value
    })
    
    const content = res.data || res || res.data?.data 
    localStorage.setItem('generatedResume', content) 
    
    // 只单纯隐藏弹窗并跳转，决不能清空数据
    isResumeDialogOpen.value = false 
    router.push('/resume-preview') 
    
  } catch (error) {
    console.error('简历生成失败:', error)
    alert('AI 生成开小差了，请稍后重试！')
  } finally {
    isGenerating.value = false
  }
}
</script>

<style scoped>


/* 轮播图容器 */
.carousel-container {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: hidden;
  border-radius: 16px;
  margin-bottom: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

/* 轮播图轨道 */
.carousel-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease-in-out;
}

/* 单个幻灯片 */
.carousel-slide {
  flex: 0 0 100%;
  height: 100%;
  position: relative;
}

/* 轮播图图片 */
.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 图片上的文字内容 */
.carousel-content {
  position: absolute;
  bottom: 60px;
  left: 50px;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  z-index: 2;
  max-width: 60%;
}

.carousel-content h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
  line-height: 1.2;
}

.carousel-content p {
  font-size: 16px;
  margin-bottom: 20px;
  opacity: 0.9;
}

.carousel-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #4aa3d1, #6cc29a);
  color: white;
  border: none;
  border-radius: 40px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.carousel-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(74, 163, 209, 0.4);
}

/* 半透明遮罩，让文字更清楚 */
.carousel-slide::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 50%;
  background: linear-gradient(to top, rgba(0,0,0,0.5), transparent);
  z-index: 1;
}

/* 指示点 */
.carousel-dots {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  z-index: 3;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s;
}

.dot.active {
  width: 30px;
  border-radius: 10px;
  background: white;
}

/* 左右箭头 */
.carousel-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  z-index: 3;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.carousel-arrow:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: translateY(-50%) scale(1.1);
}

.carousel-arrow.prev {
  left: 20px;
}

.carousel-arrow.next {
  right: 20px;
}




/* 右侧信息卡片网格 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;  /* 两列 */
  gap: 16px;                        /* 卡片间距 */
  margin-top: 20px;
}

.info-card {
  background: white;
  border-radius: 20px;
  padding: 20px 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.info-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(74, 163, 209, 0.12);
}

.card-icon {
  font-size: 28px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #4aa3d1, #6cc29a);
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 6px 12px rgba(74, 163, 209, 0.2);
}

.info-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px 0;
}

.info-card p {
  font-size: 13px;
  color: #64748b;
  margin: 0;
  line-height: 1.4;
}




/* 学业深造田字格 */
.academy-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;  /* 两列，形成田字格 */
  gap: 16px;
  margin: 20px 0;
}

/* 卡片样式 - 像按钮一样可点击 */
.academy-card {
  background: white;
  border-radius: 16px;
  padding: 14px 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.03);
  cursor: pointer;  /* 鼠标变成小手 */
  display:flex;
  flex-direction:column;
  position: relative;
  overflow: hidden;
}

.academy-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(74, 163, 209, 0.15);
  border-color: #4aa3d1;
}

.academy-card:active {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(74, 163, 209, 0.2);
}

/* 卡片内容 */
.academy-card .card-content {
  position: relative;
  z-index: 1;
}

/* 图标 */
.card-icon {
  font-size: 28px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #4aa3d1, #6cc29a);
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 6px 12px rgba(74, 163, 209, 0.2);
  transition: all 0.3s ease;
}

.academy-card:hover .card-icon {
  transform: scale(1.05);
}

/* 标题 */
.academy-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px 0;
  transition: color 0.3s;
}

.academy-card:hover h4 {
  color: #4aa3d1;
}

/* 描述文字 */
.academy-card p {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

/* 箭头 - 提示可点击 */
.card-arrow {
  font-size: 14px;
  color: #4aa3d1;
  opacity: 0.5;
  transition: all 0.3s ease;
  margin-top:2px;
  display: inline-block;
}

.academy-card:hover .card-arrow {
  opacity: 1;
  transform: translateX(5px);
}

/* 点击涟漪效果 */
.academy-card::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 5px;
  height: 5px;
  background: rgba(74, 163, 209, 0.3);
  opacity: 0;
  border-radius: 100%;
  transform: scale(1, 1) translate(-50%);
  transform-origin: 50% 50%;
}

.academy-card:focus:not(:active)::after {
  animation: ripple 0.6s ease-out;
}

@keyframes ripple {
  0% {
    transform: scale(0, 0);
    opacity: 0.3;
  }
  100% {
    transform: scale(20, 20);
    opacity: 0;
  }
}





/* 产教融合 - 多级标签版本 */
.recruitment-section {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

/* 一级标签 - 行业 */
.category-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: #f8fafc;
  border-radius: 40px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e2e8f0;
}

.category-tab.active {
  background: #4aa3d1;
  border-color: #4aa3d1;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 163, 209, 0.3);
}

.cat-icon {
  font-size: 18px;
}

/* 二级标签 - 职位大类 */
.subcategory-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  border-bottom: 2px solid #f1f5f9;
  padding-bottom: 12px;
}

.subcategory-tab {
  padding: 6px 16px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.3s;
  border-radius: 30px;
  font-size: 14px;
}

.subcategory-tab:hover {
  color: #4aa3d1;
  background: #f0f9ff;
}

.subcategory-tab.active {
  color: #4aa3d1;
  font-weight: 600;
  background: #e0f2fe;
}

/* 三级标签 - 具体职位 */
.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.job-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: #f8fafc;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e2e8f0;
  font-size: 13px;
}

.job-tag.active {
  background: #4aa3d1;
  border-color: #4aa3d1;
  color: white;
}

.job-tag.hot {
  border-color: #f97316;
  background: #fff7ed;
}

.job-tag.active.hot {
  background: #f97316;
  border-color: #f97316;
  color: white;
}

.job-count {
  font-size: 11px;
  color: #64748b;
}

.active .job-count {
  color: rgba(255, 255, 255, 0.8);
}

.hot-badge {
  font-size: 11px;
}

/* 职位卡片 */
.job-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.job-card {
  background: #f8fafc;
  border-radius: 16px;
  padding: 16px;
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid transparent;
}

.job-card:hover {
  background: white;
  border-color: #e2e8f0;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
}

.job-card-header {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.company-logo {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #4aa3d1, #6cc29a);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  font-weight: 600;
}

.job-info {
  flex: 1;
}

.job-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.job-title-row h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.hot-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: #fef2f2;
  color: #ef4444;
  border-radius: 30px;
}

.company-name {
  font-size: 13px;
  color: #64748b;
}

.job-salary {
  font-size: 16px;
  font-weight: 600;
  color: #4aa3d1;
}

.job-tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.job-tags-list .tag {
  padding: 4px 10px;
  background: #e2e8f0;
  border-radius: 30px;
  font-size: 11px;
  color: #334155;
}

.job-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
  border-top: 1px solid #e2e8f0;
  padding-top: 12px;
}

.job-arrow {
  color: #4aa3d1;
  opacity: 0.5;
  transition: all 0.3s;
}

.job-card:hover .job-arrow {
  opacity: 1;
  transform: translateX(5px);
}
















/* 搜索容器 */
.search-container {
  position: relative;
  width: 100%;
  max-width: 500px;
  margin: 0 auto 20px;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 40px;
  padding: 8px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
}

.search-icon {
  font-size: 18px;
  color: #94a3b8;
  margin-right: 10px;
}

.search-bar input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  padding: 8px 0;
  background: transparent;
}

.search-bar input::placeholder {
  color: #94a3b8;
}

/* 热搜下拉框 */
.hot-search-dropdown {
  position: absolute;
  top: 60px;
  left: 0;
  right: 0;
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
  animation: slideDown 0.2s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 热搜头部 */
.hot-search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.hot-title {
  font-weight: 600;
  color: #1e293b;
  font-size: 16px;
}

.update-time {
  font-size: 12px;
  color: #94a3b8;
}

/* 热搜列表 */
.hot-search-list {
  padding: 8px 0;
}

.hot-search-item {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.hot-search-item:hover {
  background: #f8fafc;
}

/* 排名数字 */
.rank-number {
  width: 30px;
  font-size: 16px;
  font-weight: 500;
  color: #94a3b8;
}

.rank-number.top-three {
  color: #f43f5e;  /* 红色 */
  font-weight: 700;
}

/* 小火苗图标 */
.hot-icon {
  margin-right: 8px;
  font-size: 16px;
}

/* 热搜词 */
.hot-text {
  flex: 1;
  font-size: 15px;
  color: #1e293b;
}

/* 热度数字 */
.hot-count {
  font-size: 13px;
  color: #94a3b8;
}

.hot-count.top-three-count {
  color: #f43f5e;  /* 红色 */
  font-weight: 600;
}

/* 底部 */
.hot-search-footer {
  padding: 12px 20px;
  border-top: 1px solid #f1f5f9;
  text-align: center;
  color: #4aa3d1;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.hot-search-footer:hover {
  background: #f8fafc;
}




/* 竞赛信息大厅 */
.contest-section {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.contest-header {
  margin-bottom: 20px;
}

.contest-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

/* 搜索栏 */
.contest-search {
  margin-bottom: 24px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f8fafc;
  border-radius: 40px;
  padding: 4px 4px 4px 16px;
  border: 1px solid #e2e8f0;
}

.search-icon {
  color: #94a3b8;
  margin-right: 8px;
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  padding: 12px 0;
  font-size: 15px;
}

.search-btn {
  padding: 8px 24px;
  background: #4aa3d1;
  color: white;
  border: none;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.search-btn:hover {
  background: #1e6f9f;
}

/* 标签区域 */
.tags-wrapper {
  margin-bottom: 24px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.tags-wrapper::-webkit-scrollbar {
  display: none;
}

.tags-scroll {
  display: flex;
  gap: 10px;
  padding-bottom: 4px;
}

.tag-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f8fafc;
  border-radius: 40px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.tag-chip.active {
  background: white;
  border-width: 2px;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}

.tag-icon {
  font-size: 16px;
}

.tag-count {
  padding: 2px 8px;
  border-radius: 30px;
  font-size: 11px;
  font-weight: 500;
}

/* 选中标签提示 */
.selected-tag-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.selected-tag-badge {
  padding: 4px 16px;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 500;
}

.result-count {
  font-size: 13px;
  color: #64748b;
}

/* 赛事卡片网格 */
.contest-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

/* 赛事卡片 */
.contest-card {
  background: #f8fafc;
  border-radius: 20px;
  padding: 20px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
}

.contest-card:hover {
  background: white;
  border-color: #e2e8f0;
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.contest-level {
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 11px;
  font-weight: 500;
}

.contest-status {
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 11px;
  font-weight: 500;
}

.contest-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.contest-organizer {
  font-size: 13px;
  color: #475569;
  margin-bottom: 12px;
}

.organizer-label {
  color: #64748b;
}

.contest-time {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #475569;
}

.time-remain {
  font-size: 12px;
  color: #f97316;
  font-weight: 500;
}

.contest-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.contest-tag {
  padding: 4px 10px;
  background: #e2e8f0;
  border-radius: 30px;
  font-size: 11px;
  color: #334155;
}

.ability-section {
  margin-bottom: 12px;
}

.section-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

.ability-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.ability-tag {
  padding: 4px 10px;
  background: #e0f2fe;
  border-radius: 30px;
  font-size: 11px;
  color: #0369a1;
}

.contest-participants {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 12px;
}

.card-footer {
  margin-top: auto;
  text-align: right;
}

.view-detail {
  font-size: 13px;
  color: #4aa3d1;
  opacity: 0.7;
  transition: all 0.3s;
}

.contest-card:hover .view-detail {
  opacity: 1;
  transform: translateX(5px);
  display: inline-block;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #f8fafc;
  border-radius: 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.empty-sub {
  font-size: 14px;
  color: #64748b;
}









.layout { display: flex; flex-direction: column; height: 100vh; width: 100vw; background: #f5f7fa; }
.header { height: 60px; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); z-index: 10; }
.logo { font-size: 20px; font-weight: bold; color: #4a90e2; }
.search-bar input { width: 300px; padding: 8px 15px; border-radius: 20px; border: 1px solid #ddd; outline: none; background: #f8fafc; }
.header-right { display: flex; align-items: center; gap: 20px; }
.go-publish-btn { background: #00aeec; color: white; border: none; padding: 8px 18px; border-radius: 20px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 6px; transition: all 0.3s; box-shadow: 0 4px 10px rgba(0, 174, 236, 0.3); }
.go-publish-btn:hover { background: #00a1d6; transform: translateY(-2px); box-shadow: 0 6px 15px rgba(0, 174, 236, 0.4); }

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
.sidebar { width: 220px; background: #fff; padding: 20px 0; box-shadow: 2px 0 5px rgba(0,0,0,0.02); }
.sidebar ul { list-style: none; padding: 0; margin: 0; }
.sidebar li { padding: 15px 30px; cursor: pointer; color: #666; transition: 0.2s; border-left: 3px solid transparent; }
.sidebar li:hover { background: #f0f7ff; color: #4a90e2; }
.sidebar li.active { background: #e6f0fa; color: #4a90e2; border-left: 3px solid #4a90e2; font-weight: bold; }
.content { flex: 1; padding: 30px; overflow-y: auto; display: flex; flex-direction: column; }
.card-layout { background: #fff; padding: 25px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); min-height: 400px; }
.card-layout h3 { margin-top: 0; color: #333; font-size: 20px; margin-bottom: 5px; }
.subtitle { color: #888; font-size: 14px; margin-bottom: 25px; border-bottom: 2px solid #f0f2f5; padding-bottom: 15px; }
.banner-carousel { height: 240px; background: url('https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80') center/cover no-repeat; border-radius: 12px; margin-bottom: 30px; position: relative; display: flex; align-items: flex-end; box-shadow: 0 8px 20px rgba(0,0,0,0.1); overflow: hidden; }
.banner-carousel::before { content: ''; position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0.1)); }
.banner-content { position: relative; z-index: 1; padding: 30px; color: white; }
.banner-content h2 { margin: 0 0 10px 0; font-size: 28px; }
.banner-content p { margin: 0; font-size: 15px; opacity: 0.9; }
.banner-dots { position: absolute; right: 30px; bottom: 20px; display: flex; gap: 8px; z-index: 1; }
.dot { width: 8px; height: 8px; background: rgba(255,255,255,0.4); border-radius: 50%; cursor: pointer; }
.dot.active { background: white; width: 24px; border-radius: 4px; }
.home-sections { display: flex; flex-direction: column; gap: 35px; }
.section-title-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.section-title-bar h3 { margin: 0; font-size: 22px; color: #1e293b; display: flex; align-items: center; gap: 8px; }
.more-link { color: #64748b; font-size: 14px; cursor: pointer; transition: 0.2s; }
.more-link:hover { color: #4a90e2; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.card { background: #fff; border-radius: 8px; overflow: hidden; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; border: 1px solid #e2e8f0; }
.card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.08); }
.card-cover { height: 140px; position: relative; display: flex; justify-content: flex-end; align-items: flex-end; padding: 10px; }
.type-badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; color: white; font-weight: bold; }
.type-badge.video { background: rgba(239, 68, 68, 0.9); } 
.type-badge.article { background: rgba(59, 130, 246, 0.9); } 
.type-badge.template { background: rgba(16, 185, 129, 0.9); } 
.card-info { padding: 15px; }
.card-title { margin: 0 0 8px 0; font-size: 15px; color: #1e293b; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; height: 42px; }
.card-desc { margin: 0; font-size: 13px; color: #94a3b8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.section-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px;}
.section-header .subtitle { border-bottom: none; margin-bottom: 0; padding-bottom: 0;}
.publish-btn { background: #10b981; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-size: 14px; transition: 0.3s; box-shadow: 0 4px 6px rgba(16, 185, 129, 0.2); }
.publish-btn:hover { background: #059669; }
.info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
.grid-item { background: #f8fafc; padding: 30px 20px; border-radius: 8px; text-align: center; border: 1px solid #e2e8f0; color: #475569; font-weight: 500; cursor: pointer; transition: 0.3s; }
.grid-item:hover { box-shadow: 0 5px 15px rgba(74,144,226,0.1); border-color: #4a90e2; color: #4a90e2; }
.job-item { padding: 15px 10px; border-bottom: 1px dashed #eee; color: #555; cursor: pointer; transition: 0.2s; }
.job-item:hover { background: #fafbfc; color: #4a90e2; padding-left: 15px; }
.tags { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 20px;}
.tag { background: #e6f0fa; color: #4a90e2; padding: 6px 15px; border-radius: 20px; font-size: 13px; cursor: pointer; transition: 0.2s; }
.tag:hover { background: #4a90e2; color: #fff; }
.contest-placeholder, .radar-placeholder { height: 200px; background: #fafbfc; border: 2px dashed #eee; display: flex; align-items: center; justify-content: center; color: #aaa; border-radius: 8px; margin-bottom: 20px; }
.export-btn { background: #4a90e2; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-size: 14px; }
.fade-in { animation: fadeIn 0.4s ease-in-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.4); backdrop-filter: blur(2px);
  display: flex; justify-content: center; align-items: center; z-index: 1000;
  animation: fadeIn 0.2s ease-out;
}
.modal-content {
  background: #fff; width: 650px; max-width: 90%; border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15); display: flex; flex-direction: column;
  overflow: hidden;
}
.modal-header {
  padding: 18px 24px; background: #f8fafc; border-bottom: 1px solid #e2e8f0;
  display: flex; justify-content: space-between; align-items: center;
}
.modal-header h3 { margin: 0; font-size: 18px; color: #1e293b; }
.close-modal { cursor: pointer; color: #94a3b8; font-size: 16px; transition: 0.2s; }
.close-modal:hover { color: #ef4444; transform: scale(1.1); }
.modal-body { padding: 24px; max-height: 60vh; overflow-y: auto; }
.desc { margin: 0 0 12px 0; color: #64748b; font-size: 14px; }
.modal-body textarea {
  width: 100%; padding: 12px; border: 1px solid #cbd5e1; border-radius: 8px;
  font-family: inherit; font-size: 14px; resize: vertical; outline: none; transition: 0.2s;
  box-sizing: border-box;
}
.modal-body textarea:focus { border-color: #4a90e2; box-shadow: 0 0 0 3px rgba(74,144,226,0.1); }
.modal-body textarea:disabled { background: #f1f5f9; cursor: not-allowed; }
.result-area { margin-top: 20px; border-top: 1px dashed #cbd5e1; padding-top: 15px; }
.success-tip { color: #10b981; font-weight: bold; margin: 0 0 10px 0; font-size: 14px; }
.resume-output {
  background: #1e293b; color: #f8fafc; padding: 16px; border-radius: 8px;
  white-space: pre-wrap; font-family: 'Courier New', Courier, monospace;
  font-size: 14px; line-height: 1.6; max-height: 300px; overflow-y: auto;
}
.modal-footer {
  padding: 16px 24px; border-top: 1px solid #e2e8f0; background: #f8fafc;
  display: flex; justify-content: flex-end; gap: 12px;
}
.cancel-btn {
  padding: 8px 16px; background: white; border: 1px solid #cbd5e1;
  border-radius: 6px; cursor: pointer; color: #475569; transition: 0.2s;
}
.cancel-btn:hover:not(:disabled) { background: #f1f5f9; }
.submit-btn {
  padding: 8px 20px; background: #4a90e2; border: none; color: white;
  border-radius: 6px; cursor: pointer; font-weight: bold; transition: 0.2s;
}
.submit-btn:hover:not(:disabled) { background: #357ebd; box-shadow: 0 4px 6px rgba(74,144,226,0.2); }
.submit-btn:disabled { background: #94a3b8; cursor: not-allowed; }
</style>