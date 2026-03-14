<template>
  <div class="community-select-page">
    <header class="page-header">
      <div class="header-content">
        <h2>发现你感兴趣的圈子</h2>
        <p>加入专属行业/学业社区，与同行者一起交流成长</p>
      </div>
      <button class="back-btn" @click="router.back()">← 返回首页</button>
    </header>

    <div class="main-layout">
      <aside class="category-menu">
        <div 
          v-for="(category, index) in categoryData" 
          :key="index"
          class="menu-item"
          :class="{ active: activeCategoryIndex === index }"
          @click="activeCategoryIndex = index"
        >
          <span class="icon">{{ category.icon }}</span>
          {{ category.mainName }}
        </div>
      </aside>

      <main class="community-grid">
        <div 
          v-for="community in currentCommunities" 
          :key="community.id" 
          class="community-card"
          @click="goToCommunity(community.id)"
        >
          <div class="card-top">
            <div class="avatar" :style="{ background: community.color }">{{ community.name.substring(0,1) }}</div>
            <div class="info">
              <h3>{{ community.name }}</h3>
              <span class="member-count">👥 {{ community.members }} 人已加入</span>
            </div>
          </div>
          <p class="desc">{{ community.desc }}</p>
          <button class="enter-btn">进入社区</button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeCategoryIndex = ref(0)

// 模拟分类和社区数据（结合Boss直聘风格和大学群体）
const categoryData = ref([
  {
    mainName: '校园专属', icon: '🎓',
    list: [
      { id: 101, name: '考研上岸交流圈', desc: '院校选择、专业课复习、政治英语打卡。', members: '12.5k', color: '#f59e0b' },
      { id: 102, name: '保研推免情报局', desc: '夏令营、九推、联系导师经验分享。', members: '8.2k', color: '#10b981' },
      { id: 103, name: '大厂实习内推', desc: '日常实习、暑期实习面经与内推码。', members: '24k', color: '#3b82f6' }
    ]
  },
  {
    mainName: 'IT / 互联网', icon: '💻',
    list: [
      { id: 201, name: '前端开发进阶', desc: 'Vue, React, JS/TS 技术交流与八股文。', members: '15k', color: '#0ea5e9' },
      { id: 202, name: '后端/架构师', desc: 'Java, Go, 微服务, 高并发解决方案。', members: '18k', color: '#6366f1' },
      { id: 203, name: 'AI / 算法', desc: '大模型、深度学习、PyTorch前沿追踪。', members: '9k', color: '#8b5cf6' },
      { id: 204, name: '产品经理 PM', desc: '需求分析、原型设计、交互体验探讨。', members: '11k', color: '#ec4899' }
    ]
  },
  {
    mainName: '金融 / 经济', icon: '📈',
    list: [
      { id: 301, name: '投行/券商交流', desc: 'CPA, CFA备考，投行行研实习。', members: '6.5k', color: '#ef4444' },
      { id: 302, name: '银行春招秋招', desc: '四大行及股份制银行笔试面试解析。', members: '9.2k', color: '#f43f5e' }
    ]
  },
  {
    mainName: '教育 / 传媒', icon: '📚',
    list: [
      { id: 401, name: '教师编/教资', desc: '教师资格证、各地教师编制考试。', members: '14k', color: '#14b8a6' },
      { id: 402, name: '新媒体运营', desc: '小红书、抖音账号起号与内容变现。', members: '7.8k', color: '#f97316' }
    ]
  }
])

const currentCommunities = computed(() => {
  return categoryData.value[activeCategoryIndex.value].list
})

const goToCommunity = (id) => {
  router.push(`/community/${id}`)
}
</script>

<style scoped>
.community-select-page { min-height: 100vh; background: #f8fafc; padding: 30px; }
.page-header { display: flex; justify-content: space-between; align-items: center; max-width: 1200px; margin: 0 auto 30px; }
.header-content h2 { font-size: 28px; color: #1e293b; margin: 0 0 10px; }
.header-content p { color: #64748b; margin: 0; }
.back-btn { padding: 8px 20px; background: white; border: 1px solid #cbd5e1; border-radius: 20px; cursor: pointer; transition: 0.2s; }
.back-btn:hover { background: #f1f5f9; }

.main-layout { display: flex; gap: 30px; max-width: 1200px; margin: 0 auto; }

.category-menu { width: 220px; background: white; border-radius: 16px; padding: 15px 0; box-shadow: 0 4px 15px rgba(0,0,0,0.03); height: fit-content; }
.menu-item { padding: 16px 24px; cursor: pointer; display: flex; align-items: center; gap: 12px; color: #475569; font-size: 15px; transition: 0.2s; border-left: 3px solid transparent; }
.menu-item:hover { background: #f8fafc; color: #4a90e2; }
.menu-item.active { background: #eff6ff; color: #4a90e2; border-left-color: #4a90e2; font-weight: bold; }
.icon { font-size: 18px; }

.community-grid { flex: 1; display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; align-content: start; }
.community-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 15px rgba(0,0,0,0.03); cursor: pointer; transition: 0.3s; border: 1px solid transparent; display: flex; flex-direction: column; }
.community-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.08); border-color: #bae6fd; }
.card-top { display: flex; gap: 15px; margin-bottom: 15px; align-items: center; }
.avatar { width: 48px; height: 48px; border-radius: 12px; color: white; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold; }
.info h3 { margin: 0 0 6px; font-size: 18px; color: #1e293b; }
.member-count { font-size: 12px; color: #94a3b8; background: #f1f5f9; padding: 3px 8px; border-radius: 10px; }
.desc { color: #64748b; font-size: 14px; line-height: 1.5; margin-bottom: 20px; flex: 1; }
.enter-btn { width: 100%; padding: 10px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; color: #4a90e2; font-weight: bold; transition: 0.2s; cursor: pointer; }
.community-card:hover .enter-btn { background: #4a90e2; color: white; border-color: #4a90e2; }
</style>