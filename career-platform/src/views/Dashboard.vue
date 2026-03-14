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



<!-- 产教融合与招聘双选 - 招聘信息流风格 -->
<section v-if="activeMenu === 'job'" class="fade-in">
  <div class="recruitment-section">
    <div class="section-header">
      <div>
        <h3>🔥 产教融合 · 招聘双选</h3>
        <p class="section-subtitle">行业前沿风向标：探索大一至大四的硬核技能与软实力进阶路径</p>
      </div>
      <div class="header-actions">
        <span class="filter-tag active">全部</span>
        <span class="filter-tag">春招</span>
        <span class="filter-tag">秋招</span>
        <span class="filter-tag">实习</span>
        <span class="filter-tag">社招</span>
      </div>
    </div>
    
    <!-- 按招聘类型分组 -->
    <div v-for="(category, idx) in jobList" :key="idx" class="job-category">
      <div class="category-header">
        <span class="category-badge" :style="{ backgroundColor: category.typeBg, color: category.typeColor }">
          {{ category.type }}
        </span>
        <span class="category-count">{{ category.jobs.length }}个职位</span>
      </div>
      
      <div class="job-cards">
        <div v-for="(job, index) in category.jobs" :key="index" class="job-card" @click="goToJobDetail(job)">
          <!-- 左侧公司标识 -->
          <div class="job-left">
            <div class="company-logo" :style="{ background: `linear-gradient(135deg, ${category.typeColor}80, ${category.typeColor})` }">
              {{ job.logo }}
            </div>
          </div>
          
          <!-- 中间职位信息 -->
          <div class="job-middle">
            <div class="job-title-row">
              <h4>{{ job.position }}</h4>
              <span v-if="job.hot" class="hot-tag">🔥 热门</span>
            </div>
            
            <div class="company-name">
              <span>{{ job.company }}</span>
              <span class="job-salary">{{ job.salary }}</span>
            </div>
            
            <div class="job-tags">
              <span v-for="(tag, i) in job.tags" :key="i" class="skill-tag">{{ tag }}</span>
            </div>
            
            <div class="job-footer">
              <span class="job-location">📍 {{ job.location }}</span>
              <span class="job-deadline">⏰ {{ job.deadline }}</span>
            </div>
          </div>
          
          <!-- 右侧箭头 -->
          <div class="job-right">
            <span class="job-arrow">查看详情 →</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="view-more">
      <span class="view-more-link">查看更多招聘信息 →</span>
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







// 产教融合招聘数据 - 按招聘类型分类
const jobList = ref([
  // 春招
  {
    id: 1,
    type: '春招',
    typeColor: '#f97316',
    typeBg: '#fff7ed',
    jobs: [
      {
        company: '腾讯',
        logo: '🐧',
        position: '后端开发工程师',
        tags: ['Java', 'Spring Boot', '微服务'],
        salary: '25k-40k',
        location: '北京/深圳',
        deadline: '2026.04.30',
        hot: true
      },
      {
        company: '字节跳动',
        logo: '📱',
        position: '前端开发工程师',
        tags: ['Vue.js', 'React', 'TypeScript'],
        salary: '22k-38k',
        location: '北京/上海',
        deadline: '2026.05.15',
        hot: true
      }
    ]
  },
  // 秋招
  {
    id: 2,
    type: '秋招',
    typeColor: '#3b82f6',
    typeBg: '#eff6ff',
    jobs: [
      {
        company: '阿里巴巴',
        logo: '🛒',
        position: 'Java开发工程师',
        tags: ['Java', 'Spring Cloud', 'Redis'],
        salary: '24k-42k',
        location: '杭州',
        deadline: '2026.10.31'
      },
      {
        company: '美团',
        logo: '🍔',
        position: '算法工程师',
        tags: ['Python', 'PyTorch', '推荐系统'],
        salary: '28k-45k',
        location: '北京',
        deadline: '2026.11.15'
      }
    ]
  },
  // 普通招/实习
  {
    id: 3,
    type: '实习/社招',
    typeColor: '#10b981',
    typeBg: '#f0fdf4',
    jobs: [
      {
        company: '网易',
        logo: '🎵',
        position: '数据分析师',
        tags: ['Python', 'SQL', 'Tableau'],
        salary: '200-300/天',
        location: '杭州',
        deadline: '长期有效',
        intern: true
      },
      {
        company: '京东',
        logo: '🐶',
        position: '产品经理',
        tags: ['Axure', '数据分析', '用户研究'],
        salary: '18k-30k',
        location: '北京',
        deadline: '2026.06.30'
      }
    ]
  }
])





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






/* 产教融合 - 招聘信息流风格 */
.recruitment-section {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 15px;
}

.section-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-tag {
  padding: 6px 14px;
  border-radius: 30px;
  background: #f1f5f9;
  color: #475569;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-tag.active {
  background: #4aa3d1;
  color: white;
}

.filter-tag:hover {
  background: #e2e8f0;
}

/* 招聘分类 */
.job-category {
  margin-bottom: 30px;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.category-badge {
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 600;
}

.category-count {
  font-size: 13px;
  color: #64748b;
}

/* 职位卡片 */
.job-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.job-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  background: #f8fafc;
  border-radius: 20px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid transparent;
}

.job-card:hover {
  background: white;
  border-color: #e2e8f0;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
}

/* 左侧公司logo */
.job-left {
  flex-shrink: 0;
}

.company-logo {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

/* 中间职位信息 */
.job-middle {
  flex: 1;
}

.job-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.job-title-row h4 {
  font-size: 17px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.hot-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 30px;
  background: #fef2f2;
  color: #ef4444;
  font-weight: 500;
}

.company-name {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #475569;
}

.job-salary {
  font-weight: 600;
  color: #4aa3d1;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.skill-tag {
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 30px;
  background: white;
  border: 1px solid #e2e8f0;
  color: #475569;
}

.job-footer {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 12px;
  color: #64748b;
}

/* 右侧箭头 */
.job-right {
  flex-shrink: 0;
  width: 100px;
  text-align: right;
}

.job-arrow {
  font-size: 14px;
  color: #4aa3d1;
  opacity: 0.5;
  transition: all 0.3s;
}

.job-card:hover .job-arrow {
  opacity: 1;
  transform: translateX(5px);
}

/* 查看更多 */
.view-more {
  text-align: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.view-more-link {
  color: #4aa3d1;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
}

.view-more-link:hover {
  color: #1e6f9f;
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