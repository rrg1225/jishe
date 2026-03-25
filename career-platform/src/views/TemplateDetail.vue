<template>
  <div class="min-h-screen bg-gray-100 py-12 px-4 flex flex-col items-center">
    
    <div class="w-full max-w-4xl">
      
      <!-- ==================== 顶部工具栏 ==================== -->
      <div class="mb-10 flex items-center justify-between">
        <button 
          @click="router.back()" 
          class="flex items-center text-gray-500 hover:text-blue-600 font-bold transition-all"
        >
          <span class="mr-2 text-2xl">←</span> 返回列表
        </button>

        <div v-if="detail" class="text-xs font-bold text-gray-400 tracking-widest uppercase">
          {{ detail.type === 1 ? '精选模板' : '优秀简历' }}
        </div>

        <div class="flex items-center gap-3">
          <button 
            @click="downloadDoc" 
            class="px-6 py-3 bg-gray-900 hover:bg-black text-white font-black rounded-2xl shadow-xl flex items-center gap-2 transition-all"
          >
            📥 下载 Word 原件
          </button>
          
          <button 
            v-if="detail?.type === 1" 
            @click="editOnline" 
            class="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-black rounded-2xl shadow-xl flex items-center gap-2 transition-all"
          >
            ✏️ 在线编辑填写
          </button>
        </div>
      </div>

      <!-- ==================== 标题 ==================== -->
      <div v-if="detail" class="text-center mb-10">
        <h1 class="text-3xl font-black text-gray-900 mb-3">{{ detail.name }}</h1>
        <div class="flex items-center justify-center gap-6 text-sm text-gray-400">
          <span>📅 更新: {{ formatDate(detail.updateTime) }}</span>
          <span>📄 格式: Word (.docx)</span>
        </div>
      </div>

      <!-- ==================== 强制缩小 + 居中预览（关键修复） ==================== -->
      <div v-if="detail" class="flex justify-center mb-12">
        <div 
          class="preview-card"
          style="width: 380px; max-width: 380px; box-shadow: 0 25px 50px -12px rgb(0 0 0 / 0.25);"
        >
          <img 
            v-if="detail.previewUrl"
            :src="detail.previewUrl" 
            class="w-full h-full object-contain bg-white rounded-3xl p-5"
            @error="handleImgError"
            alt="简历预览"
          />
          <div v-else class="w-full h-full flex flex-col items-center justify-center text-gray-300 bg-white rounded-3xl">
            <span class="text-7xl mb-4">📄</span>
            <p class="font-bold text-xl">预览图加载中...</p>
          </div>
        </div>
      </div>

      <!-- 版权 -->
      <p v-if="detail" class="text-center text-sm text-gray-400 mt-6">
        资源版权归 AI 职友平台所有，严禁非法转售
      </p>

      <!-- 加载 -->
      <div v-else-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="w-10 h-10 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
      </div>

      <!-- 空状态 -->
      <div v-else class="text-center py-20">
        <div class="text-6xl mb-4">🏜️</div>
        <p class="text-gray-500 font-bold">未能加载资源，请检查链接</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/templates/${route.params.id}`)
    detail.value = (res && res.code === 200) ? res.data : res
  } catch (error) {
    console.error("加载详情失败:", error)
  } finally {
    setTimeout(() => { loading.value = false }, 400)
  }
})

const handleImgError = (e) => {
  e.target.src = 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?w=800'
}

const downloadDoc = () => {
  if (detail.value?.docUrl) window.open(detail.value.docUrl, '_blank')
  else alert("下载链接失效")
}

const editOnline = () => {
  if (detail.value?.id) {
    router.push({ path: '/resume-final-edit', query: { templateId: detail.value.id } })
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '2026-03-26'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
/* 强制覆盖所有老样式 */
.preview-card {
  width: 380px !important;
  max-width: 380px !important;
  aspect-ratio: 1 / 1.414 !important;
  background: white !important;
  border-radius: 24px !important;
  overflow: hidden !important;
  border: 1px solid #e5e7eb !important;
}

/* 确保图片强制缩小 */
.preview-card img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  padding: 20px !important;
  background: white !important;
}
</style>