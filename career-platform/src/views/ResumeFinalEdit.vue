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
        <button class="tool-btn" @click="triggerAiRewrite" :disabled="isRewriting">
          {{ isRewriting ? '正在全力重写中...' : '🔄 AI 重新生成全部' }}
        </button>
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
              @blur="updateModuleContent(index, $event)"
            >
              {{ module.content }}
            </div>

            <div class="module-actions" v-show="hoveredModule === index">
              <button @click="moveModule(index, -1)" title="上移">↑</button>
              <button @click="moveModule(index, 1)" title="下移">↓</button>
              <button class="delete-btn" @click="deleteModule(index)" title="删除模块">🗑️</button>
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
            👈 请在左侧选中需要优化的句子，AI 将为您提供修改建议。
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
          <button class="add-module-btn" @click="addEmptyModule">+ 添加新模块</button>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import html2pdf from 'html2pdf.js'
import request from '../utils/request' 

const router = useRouter()
const isRewriting = ref(false)

// 模板数据
const availableTemplates = ref([
  { id: 1, name: '极简学术风', cssClass: 'tpl-academic', themeColor: '#334155' },
  { id: 2, name: '现代科技蓝', cssClass: 'tpl-tech-blue', themeColor: '#00aeec' },
  { id: 3, name: '国际商务金', cssClass: 'tpl-business-gold', themeColor: '#b8860b' }
])

const currentTemplate = ref(availableTemplates.value[0])
const showTemplateSelector = ref(false)

// 响应式简历数据
const resumeData = ref({
  fullName: '',
  contactPhone: '',
  email: '',
  targetCity: ''
})

// 🌟 核心：模块化数据
const resumeModules = ref([])

onMounted(() => {
  // 1. 尝试从本地加载 AI 生成后的完整结构化数据
  const savedData = JSON.parse(localStorage.getItem('generatedResumeFullData') || '{}')
  
  // 2. 对齐基础字段
  resumeData.value.fullName = savedData.fullName || localStorage.getItem('userName') || '张三'
  resumeData.value.contactPhone = savedData.contactPhone || '138-0000-0000'
  resumeData.value.email = savedData.email || 'resume@example.com'
  resumeData.value.targetCity = savedData.targetCity || '待定'

  // 3. 映射 AI 优化的分段内容到预览模块
  if (savedData.educationInfo || savedData.projectExperience) {
    resumeModules.value = [
      { title: '教育背景', content: savedData.educationInfo },
      { title: '项目经历', content: savedData.projectExperience },
      { title: '专业技能', content: savedData.professionalSkills || '• 熟练掌握相关技术栈' }
    ]
  } else {
    // 降级兜底数据
    resumeModules.value = [
      { title: '教育背景', content: '请返回上一步生成 AI 简历' },
      { title: '项目经历', content: '暂无数据' }
    ]
  }
})

const selectTemplate = (tpl) => {
  currentTemplate.value = tpl
  showTemplateSelector.value = false
}

const updateField = (field, event) => {
  resumeData.value[field] = event.target.innerText
}

const updateModuleContent = (index, event) => {
  resumeModules.value[index].content = event.target.innerText
}

const selectedText = ref('')
const handleTextSelection = () => {
  const selection = window.getSelection()
  const text = selection.toString().trim()
  selectedText.value = text.length > 2 ? text : ''
}

const applyAiAction = async (actionType) => {
  if (!selectedText.value) return;
  const original = selectedText.value;
  
  try {
    // 调用 DeepSeek 进行局部优化
    const res = await request.post('/api/resume/text-polish', {
      requirements: `请对以下简历选段进行【${actionType}】处理，保持专业性，直接输出结果，不要包含任何寒暄。`,
      originalText: original
    });
    
    const aiResult = res.data || res;
    
    // 🌟 修正点：不再只是 alert，而是直接替换当前页面中的文本
    resumeModules.value.forEach(mod => {
      if (mod.content.includes(original)) {
        mod.content = mod.content.replace(original, aiResult)
      }
    })
    
    alert('AI 优化已应用！');
  } catch (error) {
    alert('AI 调用失败，请检查网络');
  } finally {
    selectedText.value = ''; 
  }
}

// 模块操作逻辑
const moveModule = (index, direction) => {
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= resumeModules.value.length) return
  const temp = resumeModules.value[index]
  resumeModules.value[index] = resumeModules.value[newIndex]
  resumeModules.value[newIndex] = temp
}

const deleteModule = (index) => {
  if (confirm('确定删除该模块吗？')) resumeModules.value.splice(index, 1)
}

const addEmptyModule = () => {
  resumeModules.value.push({ title: '新模块', content: '在此输入内容' })
}

const triggerAiRewrite = async () => {
  if (!confirm('重新生成将覆盖当前所有手动修改，确定吗？')) return
  
  isRewriting.value = true
  try {
    // 调用后端优化接口
    const res = await request.post('/api/resume/optimize', resumeData.value)
    if (res.code === 200) {
      localStorage.setItem('generatedResumeFullData', JSON.stringify(res.data))
      window.location.reload() // 刷新页面以重新加载数据
    }
  } catch (e) {
    alert('重写失败')
  } finally {
    isRewriting.value = false
  }
}

const saveDraft = () => {
  localStorage.setItem('resume_draft', JSON.stringify({
    data: resumeData.value,
    modules: resumeModules.value
  }))
  alert('草稿已保存！')
}

const exportToPDF = () => {
  const element = document.getElementById('resume-pdf-container');
  const opt = {
    margin: 0,
    filename: `${resumeData.value.fullName}_个人简历.pdf`,
    image: { type: 'jpeg', quality: 0.98 },
    html2canvas: { scale: 2, useCORS: true },
    jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
  };
  html2pdf().set(opt).from(element).save();
}
</script>