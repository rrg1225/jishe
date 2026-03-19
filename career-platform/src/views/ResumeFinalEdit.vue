<template>
  <div class="final-edit-layout">
    <header class="top-toolbar">
      <div class="left-action">
        <button class="icon-btn" @click="router.back()">
          <span>←</span> 返回修改信息
        </button>
        <span class="divider"></span>
        <span class="status-text">当前状态：AI 已生成完毕 ✨</span>
      </div>

      <div class="center-action">
        <button class="tool-btn" @click="triggerAiRewrite">🔄 AI 重新生成全部</button>
        <button class="tool-btn highlight-btn" @click="showTemplateSelector = true">🎨 切换模板 (当前: {{ currentTemplate.name }})</button>
      </div>

      <div class="right-action">
        <button class="action-btn save-draft" @click="saveDraft">💾 保存草稿</button>
        <button class="action-btn export-pdf" @click="exportToPDF">📄 导出 PDF</button>
      </div>
    </header>

    <main class="workspace">
      <section class="resume-preview-section">
        <div class="a4-paper" :class="currentTemplate.cssClass" id="resume-pdf-container">
          <header class="cv-header">
            <h1 class="cv-name" contenteditable="true" @blur="updateField('fullName', $event)">{{ resumeData.fullName }}</h1>
            <p class="cv-contact" contenteditable="true">
              {{ resumeData.contactPhone }} | {{ resumeData.email }} | {{ resumeData.targetCity }}
            </p>
          </header>

          <div 
            class="cv-module" 
            v-for="(module, index) in resumeModules" 
            :key="index"
            @mouseenter="hoveredModule = index"
            @mouseleave="hoveredModule = null"
          >
            <h2 class="module-title">{{ module.title }}</h2>
            <div 
              class="module-content" 
              contenteditable="true"
              @mouseup="handleTextSelection"
            >
              {{ module.content }}
            </div>

            <div class="module-actions" v-show="hoveredModule === index">
              <button title="上移">↑</button>
              <button title="下移">↓</button>
              <button class="delete-btn" title="删除模块">🗑️</button>
            </div>
          </div>
        </div>
      </section>

      <aside class="ai-sidebar">
        <div class="sidebar-card ai-assistant-card" :class="{ active: selectedText }">
          <h3 class="card-title">✨ AI 选段优化</h3>
          <div v-if="selectedText">
            <p class="selected-text-preview">"{{ selectedText }}"</p>
            <div class="ai-btn-group">
              <button class="ai-btn" @click="applyAiAction('专业润色')">👔 更专业</button>
              <button class="ai-btn" @click="applyAiAction('扩充细节')">➕ 扩充细节</button>
              <button class="ai-btn" @click="applyAiAction('精简提炼')">➖ 精简提炼</button>
              <button class="ai-btn" @click="applyAiAction('转为数据量化')">📊 数据量化</button>
            </div>
          </div>
          <div v-else class="empty-tip">
            👈 请在左侧选中需要优化的句子，AI 将为你提供修改建议。
          </div>
        </div>

        <div class="sidebar-card outline-card">
          <h3 class="card-title">📑 简历大纲管理</h3>
          <ul class="outline-list">
            <li v-for="(module, index) in resumeModules" :key="index" class="outline-item">
              <span class="drag-handle">☰</span>
              <span class="module-name">{{ module.title }}</span>
            </li>
          </ul>
          <button class="add-module-btn">+ 添加新模块</button>
        </div>
      </aside>
    </main>

    <div class="template-modal-overlay" v-if="showTemplateSelector" @click.self="showTemplateSelector = false">
      <div class="template-modal">
        <div class="modal-header">
          <h2>选择简历模板</h2>
          <button class="close-btn" @click="showTemplateSelector = false">✖</button>
        </div>
        <div class="template-grid">
          <div 
            class="tpl-card" 
            v-for="tpl in availableTemplates" 
            :key="tpl.id"
            :class="{ active: currentTemplate.id === tpl.id }"
            @click="selectTemplate(tpl)"
          >
            <div class="tpl-color-preview" :style="{ background: tpl.themeColor }"></div>
            <div class="tpl-info">
              <h4>{{ tpl.name }}</h4>
            </div>
            <div class="active-badge" v-if="currentTemplate.id === tpl.id">使用中</div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import html2pdf from 'html2pdf.js' // 请确保 npm install html2pdf.js

const router = useRouter()

// 🌟 1. 模板库数据定义
const availableTemplates = ref([
  { id: 1, name: '极简学术风', cssClass: 'tpl-academic', themeColor: '#334155' },
  { id: 2, name: '现代科技蓝', cssClass: 'tpl-tech-blue', themeColor: '#00aeec' },
  { id: 3, name: '国际商务金', cssClass: 'tpl-business-gold', themeColor: '#b8860b' }
])

// 🌟 2. 当前选中模板状态 (默认极简学术风)
const currentTemplate = ref(availableTemplates.value[0])
const showTemplateSelector = ref(false)

// 切换模板方法
const selectTemplate = (tpl) => {
  currentTemplate.value = tpl
  showTemplateSelector.value = false // 选完自动关闭
}

// 模拟简历数据
const resumeData = ref({
  fullName: '张三',
  contactPhone: '13800138000',
  email: 'zhangsan@example.com',
  targetCity: '北京'
})

const resumeModules = ref([
  { title: '教育背景', content: '2022.09 - 2026.06 南京工业大学 建筑环境与能源应用工程 (本科)\n主修课程：传热学、流体力学、工程热力学' },
  { title: '项目经历', content: '校园求职招聘系统开发\n• 独立负责后端架构设计，采用 Spring Boot 框架搭建系统\n• 优化数据库查询逻辑，将页面加载速度提升 40%\n• 使用 Vue3 实现前端页面响应式布局，提升用户体验' },
  { title: '专业技能', content: '• 熟练掌握 Java, Vue3, SpringBoot\n• 了解 MySQL 数据库优化及 Redis 缓存技术' }
])

const hoveredModule = ref(null)
const selectedText = ref('')

const handleTextSelection = () => {
  const selection = window.getSelection()
  const text = selection.toString().trim()
  if (text.length > 2) {
    selectedText.value = text
  } else {
    selectedText.value = ''
  }
}

const applyAiAction = (actionType) => {
  alert(`正在调用 AI 进行【${actionType}】，针对文本：\n"${selectedText.value}"`)
  selectedText.value = '' 
}

const updateField = (field, event) => {
  resumeData.value[field] = event.target.innerText
}

const triggerAiRewrite = () => confirm('确定要让 AI 重新生成吗？')
const saveDraft = () => alert('草稿已保存到云端！')

const exportToPDF = () => {
  const element = document.getElementById('resume-pdf-container');
  const opt = {
    margin:       0,
    filename:     `${resumeData.value.fullName}_个人简历.pdf`,
    image:        { type: 'jpeg', quality: 0.98 },
    html2canvas:  { scale: 2, useCORS: true },
    jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
  };
  html2pdf().set(opt).from(element).save();
}
</script>

<style scoped>
.final-edit-layout { min-height: 100vh; background-color: #f1f5f9; display: flex; flex-direction: column; overflow: hidden; height: 100vh; }

/* 顶部工具栏 */
.top-toolbar { height: 60px; background: #fff; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; flex-shrink: 0; z-index: 10; }
.left-action { display: flex; align-items: center; gap: 15px; }
.icon-btn { background: none; border: none; font-size: 14px; cursor: pointer; color: #64748b; }
.divider { width: 1px; height: 16px; background: #cbd5e1; }
.status-text { font-size: 13px; color: #10b981; font-weight: 500; }
.center-action { display: flex; gap: 10px; }
.tool-btn { background: #f8fafc; border: 1px solid #e2e8f0; padding: 6px 16px; border-radius: 6px; cursor: pointer; color: #475569; font-size: 14px; transition: 0.2s; }
.highlight-btn { border-color: #00aeec; color: #00aeec; background: #f0faff; font-weight: bold; }
.right-action { display: flex; gap: 12px; }
.action-btn { padding: 8px 20px; border-radius: 6px; cursor: pointer; font-size: 14px; font-weight: 500; border: none; }
.save-draft { background: #f1f5f9; color: #475569; }
.export-pdf { background: #00aeec; color: white; }

/* 核心工作区 */
.workspace { display: flex; flex: 1; overflow: hidden; }
.resume-preview-section { flex: 0 0 60%; background: #94a3b8; padding: 40px; overflow-y: auto; display: flex; justify-content: center; }
.a4-paper { width: 794px; min-height: 1123px; background: #fff; padding: 50px 60px; box-sizing: border-box; box-shadow: 0 10px 25px rgba(0,0,0,0.1); transition: all 0.3s ease; }

/* 右侧 AI 侧边栏 */
.ai-sidebar { flex: 1; background: #fff; border-left: 1px solid #e2e8f0; padding: 24px; overflow-y: auto; display: flex; flex-direction: column; gap: 24px; }
.sidebar-card { border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; background: #f8fafc; }
.card-title { font-size: 16px; margin: 0 0 15px 0; color: #0f172a; }
.ai-assistant-card { transition: 0.3s; }
.ai-assistant-card.active { border-color: #00aeec; background: #f0faff; box-shadow: 0 4px 12px rgba(0,174,236,0.1); }
.empty-tip { color: #94a3b8; font-size: 14px; text-align: center; padding: 20px 0; }
.selected-text-preview { font-size: 13px; color: #475569; background: #fff; padding: 10px; border-radius: 6px; border-left: 3px solid #10b981; margin-bottom: 15px; }
.ai-btn-group { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.ai-btn { background: #fff; border: 1px solid #cbd5e1; padding: 8px; border-radius: 6px; cursor: pointer; font-size: 13px; color: #334155; }
.outline-list { list-style: none; padding: 0; margin: 0 0 15px 0; }
.outline-item { background: #fff; border: 1px solid #e2e8f0; padding: 10px 15px; margin-bottom: 8px; border-radius: 6px; display: flex; align-items: center; gap: 10px; }
.add-module-btn { width: 100%; background: transparent; border: 1px dashed #94a3b8; color: #64748b; padding: 10px; border-radius: 6px; cursor: pointer; }

/* 🌟 模板选择器弹窗样式 */
.template-modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 999; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.template-modal { background: #fff; width: 600px; border-radius: 12px; padding: 24px; box-shadow: 0 20px 40px rgba(0,0,0,0.2); }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #e2e8f0; padding-bottom: 10px; }
.modal-header h2 { font-size: 18px; margin: 0; }
.close-btn { background: none; border: none; font-size: 20px; cursor: pointer; color: #94a3b8; }
.template-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
.tpl-card { border: 2px solid #e2e8f0; border-radius: 8px; overflow: hidden; cursor: pointer; position: relative; transition: 0.2s; }
.tpl-card:hover { border-color: #94a3b8; transform: translateY(-2px); }
.tpl-card.active { border-color: #00aeec; box-shadow: 0 0 0 2px rgba(0,174,236,0.2); }
.tpl-color-preview { height: 80px; width: 100%; }
.tpl-info { padding: 12px; text-align: center; }
.tpl-info h4 { margin: 0; font-size: 14px; color: #334155; }
.active-badge { position: absolute; top: 8px; right: 8px; background: #00aeec; color: #fff; font-size: 12px; padding: 2px 6px; border-radius: 4px; }


/* =========================================
   🌟🌟🌟 以下是解耦后的多套模板排版引擎样式 🌟🌟🌟
   ========================================= */

/* A4 内容通用基础，确保可编辑时依然美观 */
.cv-module { position: relative; margin-bottom: 20px; border: 1px solid transparent; border-radius: 6px; transition: 0.2s; }
.cv-module:hover { border-color: #bfdbfe; background: #f8fafc; }
.module-content { font-size: 14px; line-height: 1.8; white-space: pre-wrap; outline: none; min-height: 50px; }
.module-content::selection { background: #bbf7d0; color: #000; }
.module-actions { position: absolute; right: -40px; top: 0; display: flex; flex-direction: column; gap: 5px; background: #fff; padding: 4px; border-radius: 4px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
.module-actions button { border: none; background: transparent; cursor: pointer; padding: 4px; }

/* -------------------------------------
   模板 1：极简学术风 (黑白灰，极简线条) 
--------------------------------------*/
.tpl-academic { color: #1e293b; font-family: 'Times New Roman', SimSun, serif; }
.tpl-academic .cv-header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 15px; margin-bottom: 25px; }
.tpl-academic .cv-name { font-size: 32px; margin: 0 0 10px 0; outline: none; font-weight: bold; letter-spacing: 2px; }
.tpl-academic .cv-contact { font-size: 14px; color: #333; outline: none; }
.tpl-academic .module-title { font-size: 18px; color: #000; border-bottom: 1px solid #000; padding-bottom: 5px; margin-bottom: 12px; text-transform: uppercase; font-weight: bold; }
.tpl-academic .module-content { color: #333; }

/* -------------------------------------
   模板 2：现代科技蓝 (互联网风，蓝色主题色) 
--------------------------------------*/
.tpl-tech-blue { color: #334155; font-family: 'Helvetica Neue', Arial, sans-serif; }
.tpl-tech-blue .cv-header { text-align: left; background: #f0faff; padding: 30px; border-radius: 8px; margin-bottom: 25px; margin-top: -10px; }
.tpl-tech-blue .cv-name { font-size: 36px; margin: 0 0 10px 0; outline: none; color: #0f172a; font-weight: 800; }
.tpl-tech-blue .cv-contact { font-size: 14px; color: #0284c7; outline: none; }
.tpl-tech-blue .module-title { font-size: 16px; color: #0f172a; border-left: 4px solid #00aeec; padding-left: 12px; margin-bottom: 15px; font-weight: bold; background: #f8fafc; padding-top: 5px; padding-bottom: 5px; }
.tpl-tech-blue .module-content { color: #475569; padding-left: 16px; }

/* -------------------------------------
   模板 3：国际商务金 (外企风，暗金色调) 
--------------------------------------*/
.tpl-business-gold { color: #2c3e50; font-family: Georgia, serif; }
.tpl-business-gold .cv-header { text-align: right; border-top: 4px solid #b8860b; padding-top: 20px; margin-bottom: 30px; }
.tpl-business-gold .cv-name { font-size: 34px; margin: 0 0 10px 0; outline: none; color: #b8860b; }
.tpl-business-gold .cv-contact { font-size: 13px; color: #7f8c8d; outline: none; letter-spacing: 1px; }
.tpl-business-gold .module-title { font-size: 18px; color: #b8860b; text-align: center; border-bottom: 1px solid #ecf0f1; position: relative; margin-bottom: 20px; }
.tpl-business-gold .module-title::after { content: ''; position: absolute; bottom: -1px; left: 50%; transform: translateX(-50%); width: 40px; height: 2px; background: #b8860b; }
.tpl-business-gold .module-content { color: #34495e; }
</style>