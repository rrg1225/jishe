<template>
  <div class="preview-container dark-gray-theme">
    
    <div class="bg-decorations">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <svg class="lines" viewBox="0 0 100 100" preserveAspectRatio="none">
        <path d="M0,20 L100,80 M0,50 L100,50 M0,80 L100,20" stroke="rgba(0,0,0,0.03)" stroke-width="0.5" fill="none"/>
      </svg>
    </div>

    <div class="toolbar glass-panel">
      <div class="toolbar-left">
        <button class="icon-btn back-btn" @click="goBackToEdit" title="返回首页">
          <i>←</i> 首页
        </button>
        <div class="divider-v"></div>
        <div class="theme-switcher">
          <span class="label">主题:</span>
          <div class="color-dots">
            <div class="dot dark" :class="{active: currentTheme === 'theme-dark'}" @click="currentTheme = 'theme-dark'" title="极客暗灰"></div>
            <div class="dot blue" :class="{active: currentTheme === 'theme-blue'}" @click="currentTheme = 'theme-blue'" title="商务深蓝"></div>
            <div class="dot light" :class="{active: currentTheme === 'theme-light'}" @click="currentTheme = 'theme-light'" title="清新浅灰"></div>
          </div>
        </div>
      </div>

      <div class="toolbar-right">
        <button class="action-btn ai-btn" @click="openRegenerateModal">
          ✨ AI 重新生成
        </button>
        <button class="action-btn export-btn" @click="exportPDF">
          🖨️ 导出 PDF
        </button>
      </div>
    </div>

    <p class="hint">💡 提示：点击文字可直接修改。<b>选中文本</b>可唤醒 AI 进行润色或扩写！</p>

    <div class="resume-scroll-wrapper">
      <div class="a4-glass-frame glass-panel">
        <div class="a4-page" :class="currentTheme" id="resume-content" @mouseup="handleMouseUp" @mousedown="hideFloatBtn">
          
          <div class="resume-dual-column">
            <div class="left-sidebar">
              <div class="avatar-uploader" @click="triggerUpload" title="点击上传本地头像">
                <img :src="avatarUrl" alt="头像" class="avatar" v-if="avatarUrl">
                <div class="loading-avatar" v-else>
                  <span>+ 上传照片</span>
                </div>
                <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" style="display: none;">
              </div>

              <div class="name" contenteditable="true" @blur="saveSidebarInfo('name', $event)">{{ userName }}</div>
              <div class="position" contenteditable="true" @blur="saveSidebarInfo('job', $event)">{{ targetJob }}</div>
              
              <div class="info-section">
                <h3>基本信息</h3>
                <ul>
                  <li contenteditable="true">🎓 2028级毕业生</li>
                  <li contenteditable="true">🎂 21岁</li>
                  <li contenteditable="true">💻 前端开发方向</li>
                  <li contenteditable="true">📞 138-xxxx-xxxx</li>
                  <li contenteditable="true">✉️ your@email.com</li>
                </ul>
              </div>
            </div>

            <div 
              class="main-content markdown-theme-blue" 
              contenteditable="true" 
              v-html="renderedResume"
              @blur="handleBlur"
            ></div>
          </div>
          
        </div>
      </div>
    </div>

    <div 
      v-show="showAiFloat" 
      class="ai-float-menu" 
      :style="{ top: floatY + 'px', left: floatX + 'px' }"
      @mousedown.prevent
    >
      <button @click="openPolishModal('polish')">✨ 润色</button>
      <button @click="openPolishModal('expand')">📝 扩写</button>
      <button @click="openPolishModal('action')">🎯 优化为STAR法则</button>
    </div>

    <div v-if="isRegenerateModalOpen" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>✨ AI 全文重新生成</h3>
          <span class="close-modal" @click="isRegenerateModalOpen = false">✖</span>
        </div>
        <div class="modal-body">
          <p class="desc">调整您的要求，AI 将为您重新生成整份简历：</p>
          <textarea v-model="editPrompt" rows="6" :disabled="isGenerating"></textarea>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="isRegenerateModalOpen = false" :disabled="isGenerating">取消</button>
          <button class="submit-btn" @click="doRegenerate" :disabled="isGenerating || !editPrompt.trim()">
            {{ isGenerating ? '大模型重写中...' : '🚀 开始重写' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="isPolishModalOpen" class="modal-overlay">
      <div class="modal-content large">
        <div class="modal-header">
          <h3>✨ AI 局部优化 ({{ polishActionName }})</h3>
          <span class="close-modal" @click="isPolishModalOpen = false">✖</span>
        </div>
        <div class="modal-body polish-body">
          <div class="io-box">
            <h4>原文内容</h4>
            <textarea v-model="selectedText" rows="4" readonly class="readonly-text"></textarea>
          </div>
          <div class="ai-action-middle">
            <button class="re-polish-btn" @click="callAiPolish" :disabled="isGenerating">
              {{ isGenerating ? 'AI 思考中...' : '🔄 让 AI 生成优化结果' }}
            </button>
          </div>
          <div class="io-box">
            <h4>AI 优化建议</h4>
            <textarea v-model="aiSuggestedText" rows="5" placeholder="AI 生成的结果将显示在这里，您可以手动微调..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="isPolishModalOpen = false" :disabled="isGenerating">取消</button>
          <button class="submit-btn apply-btn" @click="applyPolishedText" :disabled="!aiSuggestedText.trim() || isGenerating">
            ✅ 一键强制替换到简历
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'
import request from '../utils/request' 

const router = useRouter()
const rawMarkdown = ref('')

const currentTheme = ref('theme-dark') 
const userName = ref('张三')
const targetJob = ref('全栈开发工程师')
const fileInput = ref(null)
const avatarUrl = ref('')

const renderedResume = computed(() => {
  if (!rawMarkdown.value) return '<div class="loading">⏳ 加载中...</div>'
  return marked(rawMarkdown.value)
})

watch(rawMarkdown, (newVal) => {
  const titleMatch = newVal.match(/^#\s+(.+?)(?:\s*[-–—]\s*(.+))?$/m)
  if (titleMatch) {
    userName.value = titleMatch[1].trim()
    if (titleMatch[2]) targetJob.value = titleMatch[2].trim()
  }
}, { immediate: true })

onMounted(() => {
  rawMarkdown.value = localStorage.getItem('generatedResume') || '# 暂无数据\n请返回重新生成'
  avatarUrl.value = localStorage.getItem('resumeAvatar') || ''
})

const goBackToEdit = () => {
  router.push({ path: '/dashboard', query: { edit: 'true' } })
}

const triggerUpload = () => fileInput.value.click()
const onFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (event) => {
      avatarUrl.value = event.target.result
      localStorage.setItem('resumeAvatar', avatarUrl.value) 
    }
    reader.readAsDataURL(file)
  }
}
const saveSidebarInfo = (field, event) => {
  if (field === 'name') userName.value = event.target.innerText
  if (field === 'job') targetJob.value = event.target.innerText
}
const handleBlur = (event) => {} 

// ================= 🌟 AI 重新生成 =================
const isRegenerateModalOpen = ref(false)
const editPrompt = ref('')
const isGenerating = ref(false)

const openRegenerateModal = () => {
  editPrompt.value = sessionStorage.getItem('tempResumePrompt') || ''
  isRegenerateModalOpen.value = true
}

const doRegenerate = async () => {
  isGenerating.value = true
  sessionStorage.setItem('tempResumePrompt', editPrompt.value)
  try {
    const res = await request.post('/api/resume/zhipu/generate', {
      requirements: editPrompt.value
    })
    const content = res.data || res || res.data?.data 
    rawMarkdown.value = content
    localStorage.setItem('generatedResume', content) 
    isRegenerateModalOpen.value = false
  } catch (error) {
    console.error(error)
    alert('重新生成失败，请检查网络')
  } finally {
    isGenerating.value = false
  }
}

// ================= 🌟 划词唤醒 AI =================
const showAiFloat = ref(false)
const floatX = ref(0)
const floatY = ref(0)
const selectedText = ref('')

const hideFloatBtn = () => {
  showAiFloat.value = false
}

const handleMouseUp = async () => {
  await nextTick()
  const selection = window.getSelection()
  const text = selection.toString().trim()
  
  if (text.length > 5) { 
    selectedText.value = text
    const range = selection.getRangeAt(0)
    const rect = range.getBoundingClientRect()
    floatX.value = rect.left + window.scrollX + (rect.width / 2) - 100
    floatY.value = rect.top + window.scrollY - 50
    showAiFloat.value = true
  } else {
    showAiFloat.value = false
  }
}

// ================= 🌟 AI 局部润色与替换 =================
const isPolishModalOpen = ref(false)
const polishActionType = ref('polish')
const aiSuggestedText = ref('')

const polishActionName = computed(() => {
  const map = { 'polish': '语言润色', 'expand': '细节扩写', 'action': '转为STAR法则' }
  return map[polishActionType.value]
})

const openPolishModal = (type) => {
  polishActionType.value = type
  showAiFloat.value = false
  aiSuggestedText.value = '' 
  isPolishModalOpen.value = true
  callAiPolish()
}

const callAiPolish = async () => {
  if (!selectedText.value) return
  isGenerating.value = true
  aiSuggestedText.value = 'AI 正在思考中，请稍候...'
  
  try {
    const prompt = `请作为资深HR，根据以下要求优化这段简历经历：\n要求：${polishActionName.value}\n原段落：${selectedText.value}\n请直接返回优化后的文本，不要加多余的寒暄。`
    const res = await request.post('/api/resume/zhipu/generate', { 
      requirements: prompt 
    })
    aiSuggestedText.value = res.data || res || res.data?.data || '优化结果返回异常'
  } catch (error) {
    console.error(error)
    aiSuggestedText.value = '网络请求失败，请重试。'
  } finally {
    isGenerating.value = false
  }
}

// 🌟🌟 核心魔法：终极暴力强制替换策略 🌟🌟
const applyPolishedText = () => {
  if (!aiSuggestedText.value) return;

  let md = rawMarkdown.value;
  let textToFind = selectedText.value.trim();

  // 1. 尝试精确匹配（如果刚好没选中加粗符号，就会走这里）
  if (md.includes(textToFind)) {
    rawMarkdown.value = md.replace(textToFind, aiSuggestedText.value);
    localStorage.setItem('generatedResume', rawMarkdown.value);
    isPolishModalOpen.value = false;
    return;
  }

  // 2. 🌟 “透视眼”暴力匹配：无视一切 Markdown 符号、空格、标点，直接锁定并覆写整行
  // 专门过滤掉 Markdown 控制符以及常见中英文标点，只留纯文字内核进行比对
  const getCoreText = (str) => str.replace(/[\s\n\r*+\-#>:,.!?()[\]{}`~_：，。！？（）【】；;、“”''""]/g, '');
  
  const textCore = getCoreText(textToFind); 
  const lines = md.split('\n');
  let replaced = false;

  for (let i = 0; i < lines.length; i++) {
    const lineCore = getCoreText(lines[i]);
    
    // 如果底层的某一行和用户选中的文字高度重合 (互相包含)，就认定是这一行！
    if (lineCore.length > 2 && textCore.length > 2) {
      if (lineCore.includes(textCore) || textCore.includes(lineCore)) {
        
        // 智能提取原来的行首符号（比如 "- " 或 "### "），保证替换后不破坏原有列表排版
        const prefixMatch = lines[i].match(/^(\s*[-*+>#]+\s*)/);
        const prefix = prefixMatch ? prefixMatch[1] : '';
        
        // 强制把这一整行覆盖为 AI 生成的新内容！
        lines[i] = prefix + aiSuggestedText.value;
        replaced = true;
        break;
      }
    }
  }

  // 3. 执行覆盖并关闭弹窗
  if (replaced) {
    rawMarkdown.value = lines.join('\n');
    localStorage.setItem('generatedResume', rawMarkdown.value);
    isPolishModalOpen.value = false;
  } else {
    alert("替换失败：选中的跨度过大。请尝试仅选中具体的某一句话进行优化替换。");
  }
}

const exportPDF = () => {
  window.print()
}
</script>

<style scoped>
/* =========================================
   全局架构与 Toolbar
========================================= */
.preview-container {
  position: relative; display: flex; flex-direction: column; align-items: center;
  background-color: #F4F4F0; background-image: linear-gradient(120deg, #fdfbfb 0%, #ebedee 100%);
  height: 100vh; width: 100%; overflow-y: auto; overflow-x: hidden;
  padding: 20px 0; box-sizing: border-box; font-family: "PingFang SC", sans-serif;
}

.bg-decorations { position: fixed; top: 0; left: 0; width: 100%; height: 100%; z-index: -1; overflow: hidden; }
.circle { position: absolute; border-radius: 50%; filter: blur(40px); opacity: 0.6; }
.circle-1 { width: 400px; height: 400px; top: -100px; left: -100px; background: rgba(163, 177, 182, 0.2); }
.circle-2 { width: 500px; height: 500px; bottom: -150px; right: -150px; background: rgba(181, 196, 177, 0.2); }

.glass-panel {
  background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(20px); 
  border: 1px solid rgba(255, 255, 255, 0.8); border-radius: 12px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

.toolbar {
  width: 210mm; padding: 12px 20px; margin-bottom: 15px;
  display: flex; justify-content: space-between; align-items: center; flex-shrink: 0;
}
.toolbar-left, .toolbar-right { display: flex; align-items: center; gap: 15px; }

.divider-v { width: 1px; height: 24px; background: #ddd; }

.icon-btn { background: none; border: none; color: #64748b; font-weight: bold; cursor: pointer; transition: 0.2s; display: flex; align-items: center; gap: 4px; }
.icon-btn:hover { color: #3b82f6; }

/* 主题切换器 */
.theme-switcher { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #64748b; font-weight: bold;}
.color-dots { display: flex; gap: 8px; }
.dot { width: 18px; height: 18px; border-radius: 50%; cursor: pointer; border: 2px solid transparent; transition: 0.2s; }
.dot:hover { transform: scale(1.1); }
.dot.active { border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2); }
.dot.dark { background: #4F525A; }
.dot.blue { background: #1e3a8a; }
.dot.light { background: #e2e8f0; border-color: #cbd5e1; }

.action-btn {
  padding: 8px 16px; border-radius: 8px; font-weight: 600; font-size: 13px; cursor: pointer; transition: all 0.2s; border: none;
}
.ai-btn { background: linear-gradient(135deg, #a855f7, #6366f1); color: white; box-shadow: 0 4px 10px rgba(99, 102, 241, 0.3); }
.ai-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 15px rgba(99, 102, 241, 0.4); }
.export-btn { background: #10b981; color: white; }
.export-btn:hover { background: #059669; }

.hint { color: #8c9bb0; font-size: 13px; margin: 0 0 15px 0; }
.hint b { color: #a855f7; }

/* =========================================
   📄 动态 CSS 变量架构
========================================= */
.theme-dark { --sidebar-bg: #4F525A; --sidebar-text: #FFFFFF; --main-title: #284c71; }
.theme-blue { --sidebar-bg: #1e3a8a; --sidebar-text: #FFFFFF; --main-title: #1e3a8a; }
.theme-light { --sidebar-bg: #f1f5f9; --sidebar-text: #1e293b; --main-title: #0f172a; }

.resume-scroll-wrapper { display: flex; justify-content: center; width: 100%; padding-bottom: 50px; flex: 1; }
.a4-glass-frame { width: 210mm; padding: 0; box-sizing: border-box; display: flex; justify-content: center; height: max-content; overflow: hidden; }

.a4-page {
  width: 100%; background: #FFFFFF; margin: 0; padding: 0; box-sizing: border-box;
  background-image: linear-gradient(to right, var(--sidebar-bg) 0%, var(--sidebar-bg) 250px, #FFFFFF 250px, #FFFFFF 100%);
}

.resume-dual-column { display: flex; min-height: 297mm; }

.left-sidebar {
  width: 250px; color: var(--sidebar-text); padding: 30mm 15mm; 
  box-sizing: border-box; display: flex; flex-direction: column; align-items: center; text-align: center;
}

.avatar-uploader { cursor: pointer; margin-bottom: 15px; }
.avatar { width: 100px; height: 100px; border-radius: 50%; border: 3px solid rgba(255,255,255,0.5); object-fit: cover; }
.loading-avatar { width: 100px; height: 100px; border-radius: 50%; background: rgba(0,0,0,0.1); display: flex; justify-content: center; align-items: center; font-size: 13px; }
.name { font-size: 24px; font-weight: bold; margin-bottom: 5px; outline: none; }
.position { font-size: 14px; opacity: 0.8; margin-bottom: 30px; outline: none; }
.info-section { width: 100%; text-align: left; }
.info-section h3 { font-size: 16px; font-weight: bold; border-bottom: 2px solid rgba(128,128,128,0.3); padding-bottom: 5px; margin-bottom: 15px; }
.info-section ul { list-style: none; padding: 0; margin: 0; }
.info-section li { font-size: 13px; margin-bottom: 10px; outline: none; opacity: 0.9; }

.main-content { flex: 1; padding: 30mm 20mm; box-sizing: border-box; outline: none; }

.markdown-theme-blue :deep(h1) { display: none; }
.markdown-theme-blue { color: #333; line-height: 1.8; font-size: 14px; }
.markdown-theme-blue :deep(h2) { font-size: 18px; color: var(--main-title); font-weight: bold; border-bottom: 2px solid var(--main-title); padding-bottom: 6px; margin-top: 25px; margin-bottom: 12px; display: flex; align-items: center; }
.markdown-theme-blue :deep(h2)::before { content: ''; display: inline-block; width: 6px; height: 18px; background: var(--main-title); margin-right: 10px; border-radius: 2px; }
.markdown-theme-blue :deep(h3) { font-size: 15px; color: #222; font-weight: bold; margin-top: 15px; margin-bottom: 6px; display: flex; justify-content: space-between; }
.markdown-theme-blue :deep(h3 span) { color: #7f8c8d; font-weight: normal; font-size: 13px; }
.markdown-theme-blue :deep(ul) { padding-left: 20px; margin-top: 5px; margin-bottom: 10px; list-style-type: disc; }
.markdown-theme-blue :deep(li) { margin-bottom: 6px; color: #444; font-size: 13.5px; }
.markdown-theme-blue :deep(p) { margin: 0 0 10px 0; color: #444; }

/* =========================================
   ✨ 划词唤醒 AI 悬浮菜单
========================================= */
.ai-float-menu {
  position: absolute; z-index: 1000; background: #fff; padding: 6px;
  border-radius: 8px; box-shadow: 0 10px 25px rgba(0,0,0,0.15);
  display: flex; gap: 5px; border: 1px solid #e2e8f0;
  animation: popUp 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes popUp { from { transform: scale(0.8) translateY(10px); opacity: 0; } to { transform: scale(1) translateY(0); opacity: 1; } }
.ai-float-menu button {
  background: #f8fafc; border: none; padding: 6px 12px; border-radius: 6px; font-size: 13px;
  color: #475569; cursor: pointer; transition: 0.2s; font-weight: 500;
}
.ai-float-menu button:hover { background: #e0e7ff; color: #3b82f6; }

/* =========================================
   弹窗通用样式
========================================= */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 2000; }
.modal-content { background: #fff; width: 500px; border-radius: 12px; box-shadow: 0 20px 40px rgba(0,0,0,0.2); overflow: hidden; }
.modal-content.large { width: 700px; }
.modal-header { padding: 18px 24px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; }
.modal-header h3 { margin: 0; font-size: 18px; color: #1e293b; }
.close-modal { cursor: pointer; color: #94a3b8; font-size: 18px; }
.close-modal:hover { color: #ef4444; }
.modal-body { padding: 24px; }
.desc { margin: 0 0 12px 0; color: #64748b; font-size: 14px; }
textarea { width: 100%; padding: 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-family: inherit; resize: vertical; box-sizing: border-box; }
textarea:focus { border-color: #3b82f6; outline: none; }
.readonly-text { background: #f1f5f9; color: #475569; border-style: dashed; }

.polish-body { display: flex; flex-direction: column; gap: 15px; }
.io-box h4 { margin: 0 0 8px 0; font-size: 14px; color: #334155; }
.ai-action-middle { text-align: center; }
.re-polish-btn { background: #e0e7ff; color: #4f46e5; border: none; padding: 6px 16px; border-radius: 20px; font-size: 13px; cursor: pointer; font-weight: bold; }

.modal-footer { padding: 16px 24px; border-top: 1px solid #e2e8f0; background: #f8fafc; display: flex; justify-content: flex-end; gap: 12px; }
.cancel-btn, .submit-btn { padding: 8px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 14px; border: none; }
.cancel-btn { background: white; border: 1px solid #cbd5e1; color: #475569; }
.submit-btn { background: #3b82f6; color: white; }
.apply-btn { background: #10b981; }

/* =========================================
   🖨️ 打印魔法：永不断层、主题变量继承
========================================= */
@media print {
  html, body, #app, .preview-container, .resume-scroll-wrapper, .a4-glass-frame {
    display: block !important; position: static !important; width: auto !important; height: auto !important; 
    min-height: auto !important; max-height: none !important; overflow: visible !important; 
    background: transparent !important; margin: 0 !important; padding: 0 !important; border: none !important;
  }
  
  .bg-decorations, .toolbar, .ai-float-menu, .hint { display: none !important; }

  .a4-page {
    display: block !important; width: 100% !important; border-radius: 0 !important; box-shadow: none !important;
    border-left: 250px solid var(--sidebar-bg) !important; 
    box-sizing: border-box !important; background: #FFFFFF !important;
    -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important;
  }

  .resume-dual-column { display: block !important; position: relative !important; }

  .left-sidebar {
    float: left !important; width: 250px !important; margin-left: -250px !important; 
    background: transparent !important; color: var(--sidebar-text) !important; 
    padding: 15mm 15mm !important; box-sizing: border-box !important;
  }

  .main-content {
    display: block !important; margin-left: 0 !important; width: 100% !important;
    background: transparent !important; padding: 15mm 20mm !important; box-sizing: border-box !important;
  }

  .resume-dual-column::after { content: ""; display: table; clear: both; }
  
  @page { size: A4 portrait; margin: 0 !important; }
  
  .markdown-theme-blue h2, .markdown-theme-blue h3 { page-break-after: avoid !important; break-after: avoid !important; }
  .markdown-theme-blue li, .markdown-theme-blue p, .info-section { page-break-inside: avoid !important; break-inside: avoid !important; }
}
</style>

<style>
@media print { html, body, #app { display: block !important; height: auto !important; overflow: visible !important; } }
</style>