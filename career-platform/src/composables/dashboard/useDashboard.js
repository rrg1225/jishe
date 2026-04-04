/**
 * Dashboard 页面状态与业务逻辑（由原 Dashboard.vue script 完整迁移）
 */
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import {
  CATEGORY_MAP,
  createHomeData,
  createMatrixSections,
  CAROUSEL_SLIDES_INITIAL,
  HOT_KAOYAN_INITIAL,
  HOT_BAOYAN_INITIAL,
} from '../../constants/dashboardData.js'
import { JOB_CATEGORIES_INITIAL } from '../../constants/jobCategories.js'
import { MAJOR_CATEGORIES_INITIAL } from '../../constants/majorCategories.js'
import { useDashboardAuth } from './useDashboardAuth.js'
import { useGlobalSearch } from './useGlobalSearch.js'
import { useResumeBuilder } from './useResumeBuilder.js'

export function useDashboard() {
const router = useRouter()
const activeMenu = ref('home')
const {
  userRole,
  userName,
  userAccount,
  userOrg,
  userAvatar,
  isDropdownOpen,
  toggleDropdown,
  closeDropdown,
  fetchHeaderUserInfo,
  fallbackToLocalData,
  goToProfile,
  logout,
} = useDashboardAuth(router, activeMenu)

const {
  selectedAiTemplate,
  rawResumeInput,
  isGeneratingResume,
  detectedInfo,
  suggestions,
  useExample,
  clearInput,
  addTemplate,
  handleGenerateResume,
  buildResumeMarkdownFromAi,
  guessNameFromRaw,
  dbTemplates,
  excellentResumes,
  hoveredPreviewUrl,
  hoverId,
  fetchTemplatesData,
  goToTemplateDetail,
  getSafeImageUrl,
  previewRealDoc,
  handleDownload,
} = useResumeBuilder(router)

const homeData = ref(createHomeData())
const matrixSections = ref(createMatrixSections())

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

const allLocalContentData = ref([])

const {
  hotSearchList,
  fetchRealHotSearch,
  showHotSearch,
  searchKeyword,
  searchResults,
  isSearching,
  hideHotSearch,
  selectHotSearch,
  performSearch,
  cleanupBlurTimer,
} = useGlobalSearch(activeMenu, allLocalContentData, formatToCards)

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

// ================= 首页推荐数据 =================
const carouselSlides = ref([...CAROUSEL_SLIDES_INITIAL])

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

const hotKaoyanData = ref([...HOT_KAOYAN_INITIAL])

const hotBaoyanData = ref([...HOT_BAOYAN_INITIAL])

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
const jobCategories = ref(JOB_CATEGORIES_INITIAL)

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

// 🌟 新增：标识当前是否处于推荐模式
const isRecommendMode = ref(false)

// 🌟 新增：调用后端推荐算法
const fetchRecommendedJobs = async () => {
  const resumeStr = localStorage.getItem('generatedResumeFullData')
  let userSkills = ""
  
  if (resumeStr) {
     try {
       const resumeData = JSON.parse(resumeStr)
       // 拼接用户的技能和项目经历供算法分析
       userSkills = (resumeData.professionalSkills || "") + " " + (resumeData.projectExperience || "")
     } catch (e) {
       console.error("解析本地简历数据失败", e)
     }
  }

  if (!userSkills || userSkills.trim() === '') {
     ElMessage.warning("您还没有生成过包含技能描述的 AI 简历，暂无法进行精准推荐哦！为您展示最新岗位。")
     // 退回到普通列表
     isRecommendMode.value = false
     await fetchJobData()
     return
  }

  loadingJobs.value = true
  isRecommendMode.value = true
  selectedJob.value = '全部' // 清除其它过滤条件

  try {
    const res = await request.post('/api/jobs/recommend', { userSkills: userSkills })
    
    // 解析你后端的 Result<List<JobOffer>> 结构
    let rawList = []
    if (res && res.data && Array.isArray(res.data)) {
        rawList = res.data
    } else if (res && res.code === 200 && Array.isArray(res.data)) {
        rawList = res.data
    }

    if (rawList && rawList.length > 0) {
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
          hot: true, // 推荐出来的都打上热门/推荐标签
          category: job.industry || '互联网/AI', 
          subCategory: '后端开发', 
          jobType: '全部',
          jobDescription: job.jobDescription || '暂无岗位职责描述',
          requirements: job.requirements || '暂无任职要求说明'
        }
      })
      ElMessage.success("✨ 已使用余弦相似度算法为您精准推荐了高匹配度岗位！")
    } else {
      ElMessage.warning("暂时没有匹配到合适的岗位。")
    }
  } catch (error) {
    console.error("推荐失败", error)
    ElMessage.error("推荐算法服务异常，请稍后重试。")
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
const majorCategories = ref(MAJOR_CATEGORIES_INITIAL)

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

  onMounted(() => {
    fetchHeaderUserInfo()
    startAutoPlay()
    initRealData()
    fetchContestArticles()
    fetchInitialBoardData()
    initWebSocket()
    fetchJobData()
    fetchTemplatesData()
    fetchRealHotSearch()
    fetchRealContests()
    contestPollTimer = setInterval(() => {
      fetchRealContests()
    }, 8000)
  })

  onUnmounted(() => {
    cleanupBlurTimer()
    if (slideInterval) clearInterval(slideInterval)
    if (contestPollTimer) clearInterval(contestPollTimer)
    if (ws) ws.close()
  })

  return {
    CATEGORY_MAP,
    router,
    homeData,
    matrixSections,
    formatToCards,
    allLocalContentData,
    fetchContentPage,
    initRealData,
    filteredMatrixItems,
    goToListPage,
    userRole,
    userName,
    userAccount,
    userOrg,
    userAvatar,
    activeMenu,
    isDropdownOpen,
    toggleDropdown,
    closeDropdown,
    fetchHeaderUserInfo,
    fallbackToLocalData,
    fetchInitialBoardData,
    goToProfile,
    logout,
    handleCardClick,
    selectedAiTemplate,
    rawResumeInput,
    isGeneratingResume,
    detectedInfo,
    suggestions,
    useExample,
    clearInput,
    addTemplate,
    handleGenerateResume,
    hotSearchList,
    fetchRealHotSearch,
    showHotSearch,
    searchKeyword,
    searchResults,
    isSearching,
    hideHotSearch,
    selectHotSearch,
    performSearch,
    carouselSlides,
    currentSlide,
    startAutoPlay,
    nextSlide,
    prevSlide,
    goToLink,
    targetSchoolKeyword,
    schoolDataType,
    selectedSchoolMajor,
    initWebSocket,
    hotKaoyanData,
    hotBaoyanData,
    currentSchoolData,
    handleSchoolSearch,
    getIntensityClass,
    viewSchoolDetail,
    getSectionIconClass,
    getSectionIcon,
    formatDateShort,
    jobCategories,
    jobCards,
    loadingJobs,
    fetchJobData,
    isRecommendMode,
    fetchRecommendedJobs,
    selectedCategory,
    selectedSubCategory,
    selectedJob,
    currentJobsList,
    filteredJobCards,
    goToJobDetail,
    majorCategories,
    contestCards,
    loadingContests,
    fetchRealContests,
    selectedMajor,
    selectedDirection,
    selectedSkill,
    contestKeyword,
    contestActiveTab,
    contestArticles,
    loadingArticles,
    guideFilterType,
    fetchContestArticles,
    filteredContestArticles,
    getSnippet,
    formatDateStr,
    articleLoading,
    currentArticleData,
    previewUrl,
    fetchArticleDetail,
    goToArticleDetail,
    goBackToContest,
    parsedImages,
    previewImage,
    currentDirections,
    currentSkills,
    filteredContests,
    formatDate,
    getRemainingDays,
    handleSearch,
    goToContestDetail,
    dbTemplates,
    excellentResumes,
    hoveredPreviewUrl,
    hoverId,
    fetchTemplatesData,
    goToTemplateDetail,
    getSafeImageUrl,
    previewRealDoc,
    handleDownload,
    buildResumeMarkdownFromAi,
    guessNameFromRaw,
  }
}
