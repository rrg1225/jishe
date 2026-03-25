<template>
  <div class="resume-builder-layout">
    <div v-if="hoveredPreviewUrl" class="hover-preview-box">
      <div class="preview-header">简历预览</div>
      <img :src="hoveredPreviewUrl" alt="预览" />
    </div>

    <header class="top-nav">
      <div class="left-action">
        <button class="back-btn" @click="router.push('/dashboard')">
          <span class="icon">←</span> 返回大厅
        </button>
        <span class="nav-title">✨ AI 专属简历定制</span>
      </div>
    </header>

    <main class="builder-main">
      <div class="header-titles">
        <h1 class="hero-title">打造你的金牌简历</h1>
        <p class="hero-subtitle">只需两步：选择心仪的排版风格，填入核心经历，AI 将为你生成专业级描述与排版。</p>
      </div>

      <section class="step-card">
        <div class="step-header">
          <div class="step-number">1</div>
          <h2>选择简历模板</h2>
        </div>
        <div class="template-grid" v-if="templates.length > 0">
          <div 
            class="tpl-item" 
            v-for="tpl in templates.slice(0, 8)" 
            :key="'ai-' + tpl.id"
            :class="{ active: selectedAiTemplate?.id === tpl.id }"
            @click="selectedAiTemplate = tpl"
          >
            <div class="tpl-visual preview-bg" :style="{ backgroundImage: `url(${getSafeImageUrl(tpl.previewUrl)})` }">
              <div class="active-overlay" v-if="selectedAiTemplate?.id === tpl.id">
                <span class="check-icon">✓</span>
              </div>
            </div>
            <div class="tpl-info">
              <h4>{{ tpl.name }}</h4>
              <span class="tpl-tag">AI 推荐</span>
            </div>
          </div>
        </div>
        <div v-else class="text-center text-gray-500 py-4 font-bold">
          ⚠ 正在向后端请求模板数据，请稍候... (如果一直转圈，说明后端没连上)
        </div>
      </section>

      <section class="step-card">
        <div class="step-header">
          <div class="step-number">2</div>
          <h2>补充核心经历</h2>
          <span class="header-hint">（哪怕只写大白话，AI 也会帮你润色成专业术语）</span>
        </div>

        <div class="form-wrapper">
          <div class="form-row multi-col">
            <div class="form-group">
              <label>真实姓名 <span class="req">*</span></label>
              <input v-model="resumeData.fullName" type="text" placeholder="例如：张三">
            </div>
            <div class="form-group">
              <label>联系电话</label>
              <input v-model="resumeData.contactPhone" type="text" placeholder="例如：13800138000">
            </div>
            <div class="form-group">
              <label>求职意向</label>
              <input v-model="resumeData.targetJob" type="text" placeholder="例如：Java开发工程师">
            </div>
          </div>

          <div class="form-group">
            <label>教育背景</label>
            <textarea v-model="resumeData.educationInfo" rows="2" placeholder="例如：南京工业大学..."></textarea>
          </div>

          <div class="form-group">
            <label>项目与实习经历 <span class="req">*</span></label>
            <div class="textarea-container">
              <textarea v-model="resumeData.projectExperience" rows="6" placeholder="简单描述你做过的项目..."></textarea>
              <div class="ai-hint-tag">✨ AI 将自动提取并使用 STAR 法则重写</div>
            </div>
          </div>

          <div class="form-group">
            <label>专业技能</label>
            <textarea v-model="resumeData.professionalSkills" rows="3" placeholder="例如：熟悉 Java..."></textarea>
          </div>
        </div>
      </section>

      <div class="action-footer">
        <button class="generate-btn" @click="handleGenerate" :disabled="isGenerating">
          <span class="btn-icon" v-if="!isGenerating">🚀</span>
          <span class="spinner" v-else></span>
          {{ isGenerating ? 'AI 大模型正在奋笔疾书排版中...' : '一键生成专属简历' }}
        </button>
        <p class="safe-tip">🔒 您的数据仅用于本次简历生成，平台严格保密</p>
      </div>

      <hr class="divider" />

      <div class="extra-sections">
        <div class="extra-header">
          <h2>手动模板库</h2>
          <p>想要自己动手？下载 Word 原件进行编辑</p>
        </div>
        <div class="resource-grid mb-40" v-if="templates.length > 0">
          <div 
            v-for="item in templates" 
            :key="'manual-' + item.id" 
            class="resource-card"
            @mouseenter="hoveredPreviewUrl = item.previewUrl"
            @mouseleave="hoveredPreviewUrl = null"
            @click="goToDetail(item.id)"
          >
            <div class="card-icon doc-icon">📄 DOCX</div>
            <p class="card-name">{{ item.name }}</p>
          </div>
        </div>
        <div v-else class="text-gray-400 mb-40 text-sm">暂无数据加载...</div>

        <div class="extra-header mt-40">
          <h2>优秀简历灵感</h2>
          <p>看看行业大牛是如何写简历的，提供各行各业的参考</p>
        </div>
        <div class="resource-grid" v-if="excellentResumes.length > 0">
          <div 
            v-for="item in excellentResumes" 
            :key="'excellent-' + item.id" 
            class="resource-card excellent-card"
            @mouseenter="hoveredPreviewUrl = item.previewUrl"
            @mouseleave="hoveredPreviewUrl = null"
            @click="goToDetail(item.id)"
          >
            <div class="card-icon idea-icon">💡 参考</div>
            <p class="card-name">{{ item.name }}</p>
          </div>
        </div>
        <div v-else class="text-gray-400 text-sm">暂无数据加载...</div>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const isGenerating = ref(false)

const templates = ref([]) 
const excellentResumes = ref([]) 

const selectedAiTemplate = ref(null) 
const hoveredPreviewUrl = ref(null)  

const resumeData = reactive({
  fullName: '',
  contactPhone: '',
  targetJob: '',
  educationInfo: '',
  projectExperience: '',
  professionalSkills: ''
})

onMounted(async () => {
  const savedName = localStorage.getItem('userName')
  if (savedName) resumeData.fullName = savedName

  try {
    // 1. 获取简历模板 (Type = 1)
    const res1 = await request.get('/api/templates/list?type=1')
    if (res1.code === 200 && res1.data) {
      templates.value = res1.data
      if (templates.value.length > 0) {
        selectedAiTemplate.value = templates.value[0]
      }
    }

    // 2. 获取优秀简历 (Type = 2)
    const res2 = await request.get('/api/templates/list?type=2')
    if (res2.code === 200 && res2.data) {
      excellentResumes.value = res2.data
    }
  } catch (error) {
    console.error("加载模板数据失败:", error)
  }
})

const goToDetail = (id) => {
  const routeUrl = router.resolve({ path: `/template-detail/${id}` });
  window.open(routeUrl.href, '_blank');
}

// 安全图片处理，防止列表出现白板或裂图
const getSafeImageUrl = (url) => {
  if (!url) return 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?w=400';
  const lowerUrl = String(url).toLowerCase();
  if (lowerUrl.includes('.doc') || lowerUrl.includes('.docx')) {
    return 'https://images.unsplash.com/photo-1612441804231-77a36b284856?w=400';
  }
  return url;
}

const handleGenerate = () => {
  if (!selectedAiTemplate.value) { alert('请先选择一个简历模板！'); return; }
  if (!resumeData.fullName.trim()) { alert('请至少填写您的真实姓名哦！'); return; }
  if (!resumeData.projectExperience.trim()) { alert('写一点项目经历吧！'); return; }

  isGenerating.value = true
  const promptData = `
# ${resumeData.fullName}的简历
求职意向：${resumeData.targetJob}
联系电话：${resumeData.contactPhone}
## 教育背景
${resumeData.educationInfo}
## 项目经历
${resumeData.projectExperience}
## 专业技能
${resumeData.professionalSkills}
  `
  sessionStorage.setItem('tempResumePrompt', promptData)
  sessionStorage.setItem('selectedTemplateId', selectedAiTemplate.value.id)

  setTimeout(() => {
    isGenerating.value = false
    router.push({ path: '/resume-preview' })
  }, 1500)
}
</script>

<style scoped>
/* 所有的样式保持不变，和之前一模一样 */
.resume-builder-layout { min-height: 100vh; background-color: #f4f7f9; background-image: radial-gradient(#e2e8f0 1px, transparent 1px); background-size: 20px 20px; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; color: #1e293b; padding-bottom: 60px; }
.hover-preview-box { position: fixed; top: 90px; right: 40px; width: 260px; background: #fff; border-radius: 12px; box-shadow: 0 15px 35px rgba(0,0,0,0.15); z-index: 1000; border: 1px solid #e2e8f0; overflow: hidden; animation: fadeIn 0.3s ease; }
.hover-preview-box img { width: 100%; height: auto; display: block; object-fit: cover; }
.preview-header { padding: 8px; text-align: center; font-size: 13px; font-weight: bold; color: #64748b; background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.top-nav { height: 64px; background: rgba(255, 255, 255, 0.8); backdrop-filter: blur(12px); border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; padding: 0 40px; position: sticky; top: 0; z-index: 100; }
.left-action { display: flex; align-items: center; gap: 20px; }
.back-btn { background: #fff; border: 1px solid #cbd5e1; padding: 6px 16px; border-radius: 20px; cursor: pointer; font-size: 14px; color: #475569; transition: 0.2s; }
.back-btn:hover { background: #f8fafc; color: #0f172a; border-color: #94a3b8; }
.nav-title { font-size: 18px; font-weight: bold; color: #00aeec; }
.builder-main { max-width: 860px; margin: 0 auto; padding: 40px 20px; animation: slideUp 0.4s ease-out; }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
.header-titles { text-align: center; margin-bottom: 40px; }
.hero-title { font-size: 32px; font-weight: 800; color: #0f172a; margin: 0 0 12px 0; }
.hero-subtitle { font-size: 15px; color: #64748b; margin: 0; }
.step-card { background: #fff; border-radius: 16px; padding: 30px 40px; box-shadow: 0 10px 30px rgba(0,0,0,0.03); margin-bottom: 30px; border: 1px solid #f1f5f9; }
.step-header { display: flex; align-items: center; gap: 12px; margin-bottom: 25px; }
.step-number { width: 28px; height: 28px; background: #00aeec; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 14px; }
.step-header h2 { font-size: 20px; margin: 0; color: #1e293b; }
.header-hint { font-size: 13px; color: #94a3b8; margin-top: 4px; }
.template-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 20px; }
.tpl-item { cursor: pointer; border-radius: 12px; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.tpl-item:hover { transform: translateY(-4px); }
.tpl-visual { height: 200px; border-radius: 12px; border: 2px solid #e2e8f0; position: relative; overflow: hidden; transition: 0.3s; }
.preview-bg { background-size: cover; background-position: top center; background-color: #f1f5f9; }
.tpl-item.active .tpl-visual { border-color: #00aeec; box-shadow: 0 0 0 4px rgba(0, 174, 236, 0.15); }
.active-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 174, 236, 0.1); display: flex; align-items: center; justify-content: center; }
.check-icon { background: #00aeec; color: #fff; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: bold; box-shadow: 0 4px 10px rgba(0,174,236,0.3); }
.tpl-info { text-align: center; margin-top: 12px; }
.tpl-info h4 { margin: 0 0 4px 0; font-size: 15px; color: #334155; }
.tpl-tag { font-size: 12px; color: #64748b; background: #f1f5f9; padding: 2px 8px; border-radius: 4px; }
.form-wrapper { display: flex; flex-direction: column; gap: 20px; }
.form-row.multi-col { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 14px; font-weight: 600; color: #334155; }
.form-group .req { color: #ef4444; }
input, textarea { width: 100%; padding: 12px 16px; border: 1px solid #cbd5e1; border-radius: 8px; background: #f8fafc; font-size: 15px; color: #1e293b; outline: none; transition: all 0.3s; box-sizing: border-box; font-family: inherit; }
input:focus, textarea:focus { background: #fff; border-color: #00aeec; box-shadow: 0 0 0 3px rgba(0, 174, 236, 0.1); }
textarea { resize: vertical; line-height: 1.6; }
.textarea-container { position: relative; }
.ai-hint-tag { position: absolute; bottom: 12px; right: 12px; background: linear-gradient(135deg, #e0f2fe, #bae6fd); color: #0284c7; padding: 4px 10px; border-radius: 6px; font-size: 12px; pointer-events: none; font-weight: 500; }
.action-footer { text-align: center; margin-top: 40px; margin-bottom: 50px; }
.generate-btn { background: linear-gradient(135deg, #00aeec, #0077ff); color: #fff; border: none; padding: 16px 48px; font-size: 18px; font-weight: bold; border-radius: 40px; cursor: pointer; box-shadow: 0 8px 20px rgba(0, 174, 236, 0.3); transition: all 0.3s; display: inline-flex; align-items: center; gap: 10px; }
.generate-btn:hover:not(:disabled) { transform: translateY(-3px); box-shadow: 0 12px 25px rgba(0, 174, 236, 0.4); }
.generate-btn:disabled { background: #94a3b8; box-shadow: none; cursor: not-allowed; transform: none; }
.safe-tip { font-size: 13px; color: #94a3b8; margin-top: 15px; }
.spinner { width: 20px; height: 20px; border: 3px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.divider { border: none; border-top: 1px dashed #cbd5e1; margin: 40px 0; }
.extra-sections { padding: 0 20px; }
.extra-header { margin-bottom: 20px; }
.extra-header h2 { font-size: 22px; font-weight: bold; color: #1e293b; margin: 0 0 6px 0; }
.extra-header p { font-size: 14px; color: #64748b; margin: 0; }
.mt-40 { margin-top: 40px; }
.mb-40 { margin-bottom: 40px; }
.resource-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; }
.resource-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; text-align: center; cursor: pointer; transition: all 0.2s ease; }
.resource-card:hover { transform: translateY(-4px); box-shadow: 0 10px 20px rgba(0,0,0,0.05); border-color: #cbd5e1; }
.excellent-card { background: #f8fafc; border-color: #f1f5f9; }
.excellent-card:hover { border-color: #bae6fd; background: #f0f9ff; }
.card-icon { width: 100%; height: 80px; display: flex; align-items: center; justify-content: center; border-radius: 8px; margin-bottom: 12px; font-weight: bold; font-size: 14px; }
.doc-icon { background: #f1f5f9; color: #64748b; }
.idea-icon { background: #e0f2fe; color: #0284c7; }
.card-name { margin: 0; font-size: 14px; color: #334155; font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>