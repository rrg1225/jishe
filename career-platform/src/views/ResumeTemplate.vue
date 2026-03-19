<template>
  <div class="resume-builder-layout">
    <!-- 顶部导航 -->
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

      <!-- 步骤 1：选择模板 -->
      <section class="step-card">
        <div class="step-header">
          <div class="step-number">1</div>
          <h2>选择简历模板</h2>
        </div>
        <div class="template-grid">
          <div 
            class="tpl-item" 
            v-for="tpl in aiTemplates" 
            :key="tpl.id"
            :class="{ active: selectedAiTemplate.id === tpl.id }"
            @click="selectedAiTemplate = tpl"
          >
            <div class="tpl-visual" :style="{ background: tpl.gradient }">
              <span class="tpl-mock-text line-1"></span>
              <span class="tpl-mock-text line-2"></span>
              <span class="tpl-mock-text line-3"></span>
              <div class="active-overlay" v-if="selectedAiTemplate.id === tpl.id">
                <span class="check-icon">✓</span>
              </div>
            </div>
            <div class="tpl-info">
              <h4>{{ tpl.name }}</h4>
              <span class="tpl-tag">{{ tpl.tag }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 步骤 2：输入信息 -->
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
            <textarea 
              v-model="resumeData.educationInfo" 
              rows="2" 
              placeholder="例如：南京工业大学，计算机科学与技术，2022-2026..."
            ></textarea>
          </div>

          <div class="form-group">
            <label>项目与实习经历 <span class="req">*</span></label>
            <div class="textarea-container">
              <textarea 
                v-model="resumeData.projectExperience" 
                rows="6" 
                placeholder="简单描述你做过的项目、参加的比赛或实习经历。
例如：做过一个校园二手交易网站，用了 Vue3 和 SpringBoot，负责了登录和商品发布功能，项目最后有 500 个同学在使用..."
              ></textarea>
              <div class="ai-hint-tag">✨ AI 将自动提取并使用 STAR 法则重写</div>
            </div>
          </div>

          <div class="form-group">
            <label>专业技能</label>
            <textarea 
              v-model="resumeData.professionalSkills" 
              rows="3" 
              placeholder="例如：熟悉 Java, 了解 Vue, 英语过六级..."
            ></textarea>
          </div>
        </div>
      </section>

      <!-- 底部生成按钮 -->
      <div class="action-footer">
        <button class="generate-btn" @click="handleGenerate" :disabled="isGenerating">
          <span class="btn-icon" v-if="!isGenerating">🚀</span>
          <span class="spinner" v-else></span>
          {{ isGenerating ? 'AI 大模型正在奋笔疾书排版中...' : '一键生成专属简历' }}
        </button>
        <p class="safe-tip">🔒 您的数据仅用于本次简历生成，平台严格保密</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isGenerating = ref(false)

// 视觉化模板数据
const aiTemplates = ref([
  { id: 1, name: '极简学术风', tag: '校招 / 考研', gradient: 'linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%)' },
  { id: 2, name: '现代科技蓝', tag: '互联网 / IT', gradient: 'linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%)' },
  { id: 3, name: '国际商务金', tag: '金融 / 外企', gradient: 'linear-gradient(135deg, #fef3c7 0%, #fbcfe8 100%)' },
  { id: 4, name: '创意时间轴', tag: '设计 / 运营', gradient: 'linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%)' }
])
const selectedAiTemplate = ref(aiTemplates.value[0])

const resumeData = reactive({
  fullName: '',
  contactPhone: '',
  targetJob: '',
  educationInfo: '',
  projectExperience: '',
  professionalSkills: ''
})

onMounted(() => {
  // 可以从 localStorage 预取用户名字
  const savedName = localStorage.getItem('userName')
  if (savedName) resumeData.fullName = savedName
})

const handleGenerate = () => {
  if (!resumeData.fullName.trim()) {
    alert('请至少填写您的真实姓名哦！')
    return
  }
  if (!resumeData.projectExperience.trim()) {
    alert('写一点项目经历吧，这样 AI 才能帮你大显身手！')
    return
  }

  isGenerating.value = true

  // 构建传给大模型的 Prompt（模拟）
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

  // 将拼好的内容保存到 sessionStorage，传给下一个页面
  sessionStorage.setItem('tempResumePrompt', promptData)

  // 模拟请求延迟，然后跳转到刚才建好的最终编辑页
  setTimeout(() => {
    isGenerating.value = false
    // 带着需求跳转到最终编辑页 (ResumePreview.vue)
    router.push({ path: '/resume-preview' })
  }, 1500)
}
</script>

<style scoped>
.resume-builder-layout {
  min-height: 100vh;
  background-color: #f4f7f9;
  background-image: radial-gradient(#e2e8f0 1px, transparent 1px);
  background-size: 20px 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: #1e293b;
  padding-bottom: 60px;
}

/* 导航栏 */
.top-nav {
  height: 64px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  padding: 0 40px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.left-action { display: flex; align-items: center; gap: 20px; }
.back-btn {
  background: #fff; border: 1px solid #cbd5e1; padding: 6px 16px; 
  border-radius: 20px; cursor: pointer; font-size: 14px; color: #475569; transition: 0.2s;
}
.back-btn:hover { background: #f8fafc; color: #0f172a; border-color: #94a3b8; }
.nav-title { font-size: 18px; font-weight: bold; color: #00aeec; }

/* 主内容区 */
.builder-main {
  max-width: 860px;
  margin: 0 auto;
  padding: 40px 20px;
  animation: slideUp 0.4s ease-out;
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.header-titles { text-align: center; margin-bottom: 40px; }
.hero-title { font-size: 32px; font-weight: 800; color: #0f172a; margin: 0 0 12px 0; }
.hero-subtitle { font-size: 15px; color: #64748b; margin: 0; }

/* 卡片模块 */
.step-card {
  background: #fff;
  border-radius: 16px;
  padding: 30px 40px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.03);
  margin-bottom: 30px;
  border: 1px solid #f1f5f9;
}

.step-header { display: flex; align-items: center; gap: 12px; margin-bottom: 25px; }
.step-number {
  width: 28px; height: 28px; background: #00aeec; color: #fff; 
  border-radius: 50%; display: flex; align-items: center; justify-content: center; 
  font-weight: bold; font-size: 14px;
}
.step-header h2 { font-size: 20px; margin: 0; color: #1e293b; }
.header-hint { font-size: 13px; color: #94a3b8; margin-top: 4px; }

/* 模板选择器 (精美重绘) */
.template-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.tpl-item {
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.tpl-item:hover { transform: translateY(-4px); }
.tpl-visual {
  height: 140px;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  position: relative;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
  transition: 0.3s;
}
.tpl-item.active .tpl-visual {
  border-color: #00aeec;
  box-shadow: 0 0 0 4px rgba(0, 174, 236, 0.15);
}
.tpl-mock-text { background: rgba(255,255,255,0.6); height: 8px; border-radius: 4px; }
.line-1 { width: 40%; height: 12px; background: rgba(0,0,0,0.1); margin-bottom: 10px; }
.line-2 { width: 100%; }
.line-3 { width: 80%; }
.active-overlay {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 174, 236, 0.1);
  display: flex; align-items: center; justify-content: center;
}
.check-icon {
  background: #00aeec; color: #fff; width: 32px; height: 32px;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: bold; box-shadow: 0 4px 10px rgba(0,174,236,0.3);
}
.tpl-info { text-align: center; margin-top: 12px; }
.tpl-info h4 { margin: 0 0 4px 0; font-size: 15px; color: #334155; }
.tpl-tag { font-size: 12px; color: #64748b; background: #f1f5f9; padding: 2px 8px; border-radius: 4px; }

/* 表单区域 */
.form-wrapper { display: flex; flex-direction: column; gap: 20px; }
.form-row.multi-col { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }

.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 14px; font-weight: 600; color: #334155; }
.form-group .req { color: #ef4444; }

input, textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
  font-size: 15px;
  color: #1e293b;
  outline: none;
  transition: all 0.3s;
  box-sizing: border-box;
  font-family: inherit;
}
input:focus, textarea:focus {
  background: #fff;
  border-color: #00aeec;
  box-shadow: 0 0 0 3px rgba(0, 174, 236, 0.1);
}
textarea { resize: vertical; line-height: 1.6; }

.textarea-container { position: relative; }
.ai-hint-tag {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: linear-gradient(135deg, #e0f2fe, #bae6fd);
  color: #0284c7;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  pointer-events: none;
  font-weight: 500;
}

/* 底部按钮 */
.action-footer { text-align: center; margin-top: 40px; }
.generate-btn {
  background: linear-gradient(135deg, #00aeec, #0077ff);
  color: #fff;
  border: none;
  padding: 16px 48px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 40px;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(0, 174, 236, 0.3);
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.generate-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 12px 25px rgba(0, 174, 236, 0.4);
}
.generate-btn:disabled {
  background: #94a3b8;
  box-shadow: none;
  cursor: not-allowed;
  transform: none;
}
.safe-tip { font-size: 13px; color: #94a3b8; margin-top: 15px; }

/* 加载动画 */
.spinner {
  width: 20px; height: 20px;
  border: 3px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>