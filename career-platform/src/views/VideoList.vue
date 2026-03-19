<template>
  <div class="list-layout">
    <header class="list-header">
      <div class="header-left">
        <span class="back-btn" @click="router.push('/dashboard')">❮ 返回首页</span>
        <h2 class="page-title">{{ currentCategory }} 专区</h2>
      </div>
      
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input 
          type="text" 
          v-model="searchQuery" 
          :placeholder="`在 ${currentCategory} 中搜索视频...`"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
    </header>

    <main class="list-content">
      <div v-if="loading" class="loading-state">
        <span class="spinner">⏳</span> 正在努力加载内容...
      </div>

      <template v-else>
        <div class="card-grid" v-if="videoList.length > 0">
          <div class="card" v-for="item in videoList" :key="item.id" @click="goToVideo(item)">
            <div class="card-cover" :style="{ background: item.color }">
              <span class="type-badge">{{ item.typeText }}</span>
            </div>
            <div class="card-info">
              <h4 class="card-title" :title="item.title">{{ item.title }}</h4>
              <p class="card-author">UP主: {{ item.author }}</p>
              <p class="card-date">{{ item.date }}</p>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-state">
          <div class="empty-icon">📭</div>
          <p>哎呀，没有找到相关的视频呢~</p>
          <button class="reset-btn" v-if="searchQuery" @click="resetSearch">清除搜索</button>
        </div>

        <div class="pagination-container" v-if="totalPages > 1">
          <button class="page-btn" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">
            ❮ 上一页
          </button>
          
          <span class="page-info">
            第 <span class="current">{{ currentPage + 1 }}</span> 页 / 共 {{ totalPages }} 页
            <span class="total-elements"> (共 {{ totalElements }} 个视频)</span>
          </span>
          
          <button class="page-btn" :disabled="currentPage >= totalPages - 1 || isLastPage" @click="changePage(currentPage + 1)">
            下一页 ❯
          </button>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const route = useRoute()

// 状态变量
const currentCategory = ref('视频')
const searchQuery = ref('')
const videoList = ref([])
const loading = ref(true)

// 分页变量
const currentPage = ref(0)
const pageSize = ref(12) // 每页显示 12 个
const totalPages = ref(0)
const totalElements = ref(0)
const isLastPage = ref(false)

// 颜色池（让没有封面的视频也好看）
const colors = [
  'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
  'linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%)',
  'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
  'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)'
]

// 获取数据的核心函数
const fetchVideos = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/content/list', {
      params: {
        category: currentCategory.value,
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchQuery.value, // 搜索关键词传给后端
        type: 'video'
      }
    })

    // 🌟 核心修改：更严谨地解析 JPA Page 对象 (兼容多层 axios 拦截器封装)
    const responseBody = res.data?.data || res.data || res;
    const contentData = responseBody.content || [];
    
    // 更新分页状态（拿到正确的数据后，分页器就会自动显示出来了）
    totalPages.value = responseBody.totalPages || 0;
    totalElements.value = responseBody.totalElements || 0;
    isLastPage.value = responseBody.last !== undefined ? responseBody.last : true;

    // 格式化卡片
    videoList.value = contentData.map((item, index) => ({
      id: item.id,
      title: item.title,
      typeText: '视频',
      author: item.authorName || '官方发布',
      date: item.gmtCreated ? item.gmtCreated.substring(0, 10) : '近期发布',
      videoUrl: item.videoUrl,
      color: colors[index % colors.length]
    }))

  } catch (error) {
    console.error('获取列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 页面加载和路由参数变化时触发
onMounted(() => {
  if (route.query.category) {
    currentCategory.value = route.query.category
  }
  fetchVideos()
})

watch(() => route.query.category, (newVal) => {
  if (newVal) {
    currentCategory.value = newVal
    currentPage.value = 0
    searchQuery.value = ''
    fetchVideos()
  }
})

// 交互操作
const handleSearch = () => {
  currentPage.value = 0 // 搜索时重置为第一页
  fetchVideos()
}

const resetSearch = () => {
  searchQuery.value = ''
  currentPage.value = 0
  fetchVideos()
}

const changePage = (newPage) => {
  if (newPage >= 0 && newPage < totalPages.value) {
    currentPage.value = newPage
    fetchVideos()
    
    // 🌟 核心修改：翻页后让滚动容器滚回顶部
    const listLayout = document.querySelector('.list-layout')
    if (listLayout) {
      listLayout.scrollTo({ top: 0, behavior: 'smooth' })
    } else {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }
}

const goToVideo = (item) => {
  router.push({ 
    path: '/video', 
    query: { title: item.title, author: item.author, url: item.videoUrl } 
  })
}
</script>

<style scoped>
.list-layout {
  height: 100vh;         
  overflow-y: auto;       
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.list-header {
  height: 70px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  color: #64748b;
  font-size: 15px;
  cursor: pointer;
  transition: 0.3s;
  padding: 8px 12px;
  border-radius: 8px;
}
.back-btn:hover {
  background: #f1f5f9;
  color: #4a90e2;
}

.page-title {
  margin: 0;
  font-size: 20px;
  color: #1e293b;
  border-left: 4px solid #4a90e2;
  padding-left: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 30px;
  padding: 4px 6px 4px 16px;
  width: 400px;
  transition: 0.3s;
}
.search-box:focus-within {
  border-color: #4a90e2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
  background: #fff;
}
.search-icon { color: #94a3b8; margin-right: 8px; }
.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  padding: 8px 0;
}
.search-btn {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 8px 24px;
  border-radius: 20px;
  cursor: pointer;
  transition: 0.3s;
}
.search-btn:hover { background: #357ebd; }

.list-content {
  flex: 1;
  padding: 30px 40px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 25px;
}

.card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}
.card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.08);
  border-color: #cbd5e1;
}

.card-cover {
  height: 150px;
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
  padding: 12px;
}

.type-badge {
  background: rgba(0,0,0,0.6);
  color: white;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  backdrop-filter: blur(4px);
}

.card-info { padding: 16px; flex: 1; display: flex; flex-direction: column; }
.card-title {
  margin: 0 0 10px 0;
  font-size: 15px;
  color: #1e293b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 45px;
}
.card-author { margin: 0 0 6px 0; font-size: 13px; color: #64748b; }
.card-date { margin: 0; font-size: 12px; color: #94a3b8; }

/* 分页器样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
  padding: 20px 0;
}
.page-btn {
  background: white;
  border: 1px solid #cbd5e1;
  color: #475569;
  padding: 8px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.2s;
  font-weight: 500;
}
.page-btn:hover:not(:disabled) {
  background: #f0f7ff;
  border-color: #4a90e2;
  color: #4a90e2;
}
.page-btn:disabled { background: #f1f5f9; color: #94a3b8; cursor: not-allowed; }
.page-info { font-size: 14px; color: #64748b; }
.page-info .current { color: #4a90e2; font-weight: bold; font-size: 16px; }
.total-elements { font-size: 13px; opacity: 0.7; }

/* 状态样式 */
.loading-state, .empty-state {
  text-align: center;
  padding: 100px 0;
  color: #64748b;
  font-size: 16px;
}
.empty-icon { font-size: 50px; margin-bottom: 15px; }
.reset-btn {
  margin-top: 15px;
  background: #f1f5f9;
  border: none;
  padding: 8px 20px;
  border-radius: 20px;
  color: #475569;
  cursor: pointer;
}
.reset-btn:hover { background: #e2e8f0; }
</style>