<template>
  <div class="preview-container" :class="currentTheme">
    <div class="bg-decorations" aria-hidden="true">
      <div class="orb orb-a"></div>
      <div class="orb orb-b"></div>
      <div class="orb orb-c"></div>
      <div class="grain"></div>
    </div>

    <div class="toolbar glass-panel">
      <div class="toolbar-left">
        <button class="icon-btn back-btn" @click="goBackToEdit" title="返回首页">
          <i>←</i> 首页
        </button>
        <div class="divider-v"></div>
        <div class="theme-switcher">
          <span class="label">主题色:</span>
          <div class="color-dots">
            <div class="dot dark" :class="{active: currentTheme === 'theme-dark'}" @click="currentTheme = 'theme-dark'" title="极客暗灰"></div>
            <div class="dot blue" :class="{active: currentTheme === 'theme-blue'}" @click="currentTheme = 'theme-blue'" title="商务深蓝"></div>
            <div class="dot light" :class="{active: currentTheme === 'theme-light'}" @click="currentTheme = 'theme-light'" title="清新浅灰"></div>
          </div>
        </div>
        <div class="divider-v"></div>
        <div class="layout-switcher">
          <span class="label">版式骨架:</span>
          <div class="layout-chips">
            <button
              v-for="opt in layoutOptions"
              :key="opt.key"
              type="button"
              class="layout-chip"
              :class="{ active: currentLayout === opt.key }"
              :title="opt.desc"
              @click="setLayout(opt.key)"
            >
              {{ opt.short }}
            </button>
          </div>
        </div>
      </div>

      <div class="toolbar-right">
        <button class="action-btn save-btn" @click="saveToDatabase" :disabled="isSaving">
          {{ isSaving ? '⏳ 保存中...' : '💾 保存到云端' }}
        </button>
        <button class="action-btn ai-btn" @click="openRegenerateModal">
          ✨ AI 重新生成
        </button>
        <button class="action-btn export-btn" @click="exportPDF">
          🖨️ 导出 PDF
        </button>
      </div>
    </div>

    <p class="hint">💡 提示：点击文字可直接修改。<b>选中文本</b>可唤醒 AI 进行润色或扩写！</p>
    <p class="hint-sub">
      本页根据 <b>Markdown</b> 与所选「版式骨架」实时排版；模板详情里的画面是 <b>Word 预览图</b>，二者来源不同，可在版式 1～8 间切换以贴近心仪风格。
    </p>

    <div class="resume-scroll-wrapper">
      <div class="preview-stage-shell">
        <aside class="rp-rail rp-rail-left" aria-hidden="true">
          <div class="rp-glow"></div>
          <div class="rp-bubble">📋</div>
          <div class="rp-dots"></div>
          <div class="rp-vtext">LIVE</div>
        </aside>
        <div class="a4-glass-frame glass-panel rp-pedestal">
        <div class="a4-page" :class="[currentTheme, currentLayout]" id="resume-content" @mouseup="handleMouseUp" @mousedown="hideFloatBtn">
          
          <div v-if="currentLayout === 'layout-classic'" class="resume-dual-column">
            <div class="left-sidebar">
              <div class="avatar-uploader" @click="triggerUpload" title="点击上传照片">
                <img :src="avatarUrl" alt="头像" class="avatar" v-if="avatarUrl">
                <div class="loading-avatar" v-else><span>+ 照片</span></div>
                <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" style="display: none;">
              </div>
              <div class="name" contenteditable="true" @blur="saveSidebarInfo('name', $event)">{{ userName }}</div>
              <div class="position" contenteditable="true" @blur="saveSidebarInfo('job', $event)">{{ targetJob }}</div>
              <div class="info-section">
                <h3>基本信息</h3>
                <ul>
                  <li contenteditable="true" @blur="saveSidebarInfo('edu', $event)">🎓 {{ eduStatus }}</li>
                  <li contenteditable="true" @blur="saveSidebarInfo('phone', $event)">📞 {{ phone }}</li>
                  <li contenteditable="true" @blur="saveSidebarInfo('email', $event)">✉️ {{ email }}</li>
                  <li contenteditable="true" @blur="saveSidebarInfo('city', $event)">📍 {{ intendedCity }}</li>
                </ul>
              </div>
            </div>
            <div class="main-content markdown-body" contenteditable="true" v-html="renderedResume" @blur="handleBlur"></div>
          </div>

          <div v-else-if="currentLayout === 'layout-top'" class="resume-top-down">
            <div class="top-header">
              <div class="header-content">
                <div class="name" contenteditable="true" @blur="saveSidebarInfo('name', $event)">{{ userName }}</div>
                <div class="position" contenteditable="true" @blur="saveSidebarInfo('job', $event)">{{ targetJob }}</div>
                <div class="contact-row">
                  <span contenteditable="true" @blur="saveSidebarInfo('edu', $event)">🎓 {{ eduStatus }}</span> |
                  <span contenteditable="true" @blur="saveSidebarInfo('phone', $event)">📞 {{ phone }}</span> |
                  <span contenteditable="true" @blur="saveSidebarInfo('email', $event)">✉️ {{ email }}</span>
                </div>
              </div>
              <div class="avatar-uploader" @click="triggerUpload">
                <img :src="avatarUrl" alt="头像" class="avatar" v-if="avatarUrl">
                <div class="loading-avatar" v-else><span>+ 照片</span></div>
                <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" style="display: none;">
              </div>
            </div>
            <div class="main-content markdown-body" contenteditable="true" v-html="renderedResume" @blur="handleBlur"></div>
          </div>

          <div v-else-if="currentLayout === 'layout-minimal'" class="resume-minimal">
            <div class="color-ribbon"></div>
            <div class="minimal-wrapper">
              <div class="minimal-header">
                <div class="avatar-uploader" @click="triggerUpload">
                  <img :src="avatarUrl" alt="头像" class="avatar" v-if="avatarUrl">
                  <div class="loading-avatar" v-else><span>+ 照片</span></div>
                  <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" style="display: none;">
                </div>
                <div class="header-text">
                  <div class="name" contenteditable="true" @blur="saveSidebarInfo('name', $event)">{{ userName }}</div>
                  <div class="position" contenteditable="true" @blur="saveSidebarInfo('job', $event)">{{ targetJob }}</div>
                  <div class="contact-grid">
                    <span contenteditable="true" @blur="saveSidebarInfo('phone', $event)">📞 {{ phone }}</span>
                    <span contenteditable="true" @blur="saveSidebarInfo('email', $event)">✉️ {{ email }}</span>
                    <span contenteditable="true" @blur="saveSidebarInfo('edu', $event)">🎓 {{ eduStatus }}</span>
                    <span contenteditable="true" @blur="saveSidebarInfo('city', $event)">📍 {{ intendedCity }}</span>
                  </div>
                </div>
              </div>
              <div class="main-content markdown-body" contenteditable="true" v-html="renderedResume" @blur="handleBlur"></div>
            </div>
          </div>

          <div v-else-if="currentLayout === 'layout-timeline'" class="resume-timeline">
            <div class="timeline-accent"></div>
            <div class="timeline-inner">
              <div class="timeline-head">
                <div class="avatar-uploader sm" @click="triggerUpload">
                  <img :src="avatarUrl" alt="头像" class="avatar" v-if="avatarUrl">
                  <div class="loading-avatar" v-else><span>+</span></div>
                  <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" style="display: none;">
                </div>
                <div>
                  <div class="name" contenteditable="true" @blur="saveSidebarInfo('name', $event)">{{ userName }}</div>
                  <div class="position" contenteditable="true" @blur="saveSidebarInfo('job', $event)">{{ targetJob }}</div>
                  <div class="contact-inline">
                    <span contenteditable="true" @blur="saveSidebarInfo('phone', $event)">📞 {{ phone }}</span>
                    <span contenteditable="true" @blur="saveSidebarInfo('email', $event)">✉️ {{ email }}</span>
                    <span contenteditable="true" @blur="saveSidebarInfo('edu', $event)">🎓 {{ eduStatus }}</span>
                  </div>
                </div>
              </div>
              <div class="main-content markdown-body timeline-md" contenteditable="true" v-html="renderedResume" @blur="handleBlur"></div>
            </div>
          </div>

          <div v-else-if="currentLayout === 'layout-banner'" class="resume-banner">
            <div class="banner-hero">
              <div class="banner-hero-text">
                <div class="name" contenteditable="true" @blur="saveSidebarInfo('name', $event)">{{ userName }}</div>
                <div class="position" contenteditable="true" @blur="saveSidebarInfo('job', $event)">{{ targetJob }}</div>
                <div class="banner-contact">
                  <span contenteditable="true" @blur="saveSidebarInfo('phone', $event)">📞 {{ phone }}</span>
                  <span contenteditable="true" @blur="saveSidebarInfo('email', $event)">✉️ {{ email }}</span>
                  <span contenteditable="true" @blur="saveSidebarInfo('edu', $event)">🎓 {{ eduStatus }}</span>
                </div>
              </div>
              <div class="avatar-uploader" @click="triggerUpload">
                <img :src="avatarUrl" alt="头像" class="avatar" v-if="avatarUrl">
                <div class="loading-avatar" v-else><span>+ 照片</span></div>
                <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" style="display: none;">
              </div>
            </div>
            <div class="banner-body">
              <div class="main-content markdown-body" contenteditable="true" v-html="renderedResume" @blur="handleBlur"></div>
            </div>
          </div>

          <div v-else-if="currentLayout === 'layout-magazine'" class="resume-magazine">
            <div class="mag-topline"></div>
            <div class="mag-head">
              <div class="mag-title-block">
                <div class="name" contenteditable="true" @blur="saveSidebarInfo('name', $event)">{{ userName }}</div>
                <div class="position" contenteditable="true" @blur="saveSidebarInfo('job', $event)">{{ targetJob }}</div>
              </div>
              <div class="avatar-uploader sm" @click="triggerUpload">
                <img :src="avatarUrl" alt="头像" class="avatar" v-if="avatarUrl">
                <div class="loading-avatar" v-else><span>+</span></div>
                <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" style="display: none;">
              </div>
            </div>
            <div class="mag-meta" contenteditable="false">
              <span contenteditable="true" @blur="saveSidebarInfo('phone', $event)">📞 {{ phone }}</span>
              <span contenteditable="true" @blur="saveSidebarInfo('email', $event)">✉️ {{ email }}</span>
              <span contenteditable="true" @blur="saveSidebarInfo('city', $event)">📍 {{ intendedCity }}</span>
            </div>
            <div class="main-content markdown-body mag-md" contenteditable="true" v-html="renderedResume" @blur="handleBlur"></div>
          </div>

          <div v-else-if="currentLayout === 'layout-card'" class="resume-cardlayout">
            <div class="card-top">
              <div class="avatar-uploader sm" @click="triggerUpload">
                <img :src="avatarUrl" alt="头像" class="avatar" v-if="avatarUrl">
                <div class="loading-avatar" v-else><span>+</span></div>
                <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" style="display: none;">
              </div>
              <div class="card-top-text">
                <div class="name" contenteditable="true" @blur="saveSidebarInfo('name', $event)">{{ userName }}</div>
                <div class="position" contenteditable="true" @blur="saveSidebarInfo('job', $event)">{{ targetJob }}</div>
                <div class="card-contact">
                  <span contenteditable="true" @blur="saveSidebarInfo('phone', $event)">{{ phone }}</span>
                  <span contenteditable="true" @blur="saveSidebarInfo('email', $event)">{{ email }}</span>
                </div>
              </div>
            </div>
            <div class="card-sheet">
              <div class="main-content markdown-body card-md" contenteditable="true" v-html="renderedResume" @blur="handleBlur"></div>
            </div>
          </div>

          <div v-else-if="currentLayout === 'layout-elegant'" class="resume-elegant">
            <div class="elegant-frame">
              <div class="elegant-header">
                <div class="elegant-line"></div>
                <div class="name" contenteditable="true" @blur="saveSidebarInfo('name', $event)">{{ userName }}</div>
                <div class="position" contenteditable="true" @blur="saveSidebarInfo('job', $event)">{{ targetJob }}</div>
                <div class="elegant-contact">
                  <span contenteditable="true" @blur="saveSidebarInfo('phone', $event)">{{ phone }}</span>
                  <span class="dot-sep">·</span>
                  <span contenteditable="true" @blur="saveSidebarInfo('email', $event)">{{ email }}</span>
                  <span class="dot-sep">·</span>
                  <span contenteditable="true" @blur="saveSidebarInfo('edu', $event)">{{ eduStatus }}</span>
                </div>
              </div>
              <div class="avatar-uploader elegant-av" @click="triggerUpload">
                <img :src="avatarUrl" alt="头像" class="avatar" v-if="avatarUrl">
                <div class="loading-avatar" v-else><span>+</span></div>
                <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" style="display: none;">
              </div>
              <div class="main-content markdown-body elegant-md" contenteditable="true" v-html="renderedResume" @blur="handleBlur"></div>
            </div>
          </div>

          <div v-else class="resume-dual-column layout-fallback">
            <div class="left-sidebar">
              <div class="avatar-uploader" @click="triggerUpload">
                <img :src="avatarUrl" alt="头像" class="avatar" v-if="avatarUrl">
                <div class="loading-avatar" v-else><span>+ 照片</span></div>
                <input type="file" ref="fileInput" @change="onFileChange" accept="image/*" style="display: none;">
              </div>
              <div class="name" contenteditable="true" @blur="saveSidebarInfo('name', $event)">{{ userName }}</div>
              <div class="position" contenteditable="true" @blur="saveSidebarInfo('job', $event)">{{ targetJob }}</div>
              <div class="info-section">
                <h3>基本信息</h3>
                <ul>
                  <li contenteditable="true" @blur="saveSidebarInfo('edu', $event)">🎓 {{ eduStatus }}</li>
                  <li contenteditable="true" @blur="saveSidebarInfo('phone', $event)">📞 {{ phone }}</li>
                  <li contenteditable="true" @blur="saveSidebarInfo('email', $event)">✉️ {{ email }}</li>
                </ul>
              </div>
            </div>
            <div class="main-content markdown-body" contenteditable="true" v-html="renderedResume" @blur="handleBlur"></div>
          </div>
          
        </div>
      </div>
        <aside class="rp-rail rp-rail-right" aria-hidden="true">
          <div class="rp-glow rp-glow-alt"></div>
          <div class="rp-bubble">✨</div>
          <div class="rp-dots"></div>
          <div class="rp-vtext rp-vtext-alt">PDF</div>
        </aside>
      </div>
    </div>

    <div v-show="showAiFloat" class="ai-float-menu" :style="{ top: floatY + 'px', left: floatX + 'px' }" @mousedown.prevent>
      <button @click="openPolishModal('polish')">✨ 润色</button>
      <button @click="openPolishModal('expand')">📝 扩写</button>
      <button @click="openPolishModal('action')">🎯 STAR优化</button>
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
          <button class="cancel-btn" @click="isRegenerateModalOpen = false">取消</button>
          <button class="submit-btn" @click="doRegenerate" :disabled="isGenerating || !editPrompt.trim()">
            {{ isGenerating ? '重写中...' : '🚀 开始重写' }}
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
              🔄 生成优化结果
            </button>
          </div>
          <div class="io-box">
            <h4>AI 优化建议</h4>
            <textarea v-model="aiSuggestedText" rows="5" placeholder="AI 生成的结果将显示在这里..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="isPolishModalOpen = false">取消</button>
          <button class="submit-btn apply-btn" @click="applyPolishedText" :disabled="!aiSuggestedText.trim() || isGenerating">
            ✅ 一键应用
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
const currentLayout = ref('layout-classic')
const isSaving = ref(false)
const isGenerating = ref(false)

/** 与能力画像「模板 1～8」一一对应的 8 套版式骨架（数据库 id 取模 8 映射） */
const LAYOUT_KEYS = [
  'layout-classic',
  'layout-top',
  'layout-minimal',
  'layout-timeline',
  'layout-banner',
  'layout-magazine',
  'layout-card',
  'layout-elegant'
]

const layoutOptions = [
  { key: 'layout-classic', short: '1', desc: '经典双栏' },
  { key: 'layout-top', short: '2', desc: '顶栏商务' },
  { key: 'layout-minimal', short: '3', desc: '极简侧条' },
  { key: 'layout-timeline', short: '4', desc: '时间线' },
  { key: 'layout-banner', short: '5', desc: '横幅头图' },
  { key: 'layout-magazine', short: '6', desc: '杂志分栏' },
  { key: 'layout-card', short: '7', desc: '卡片正文' },
  { key: 'layout-elegant', short: '8', desc: '典雅居中' }
]

function mapTemplateIdToLayoutAndTheme(templateId) {
  const n = parseInt(String(templateId), 10)
  const idx = Number.isFinite(n) ? ((n - 1) % 8 + 8) % 8 : 0
  return {
    layout: LAYOUT_KEYS[idx],
    theme: ['theme-dark', 'theme-blue', 'theme-light'][idx % 3]
  }
}

function setLayout(key) {
  currentLayout.value = key
  sessionStorage.setItem('resumePreviewLayout', key)
}

// 动态个人信息
const userName = ref('姓名加载中...')
const targetJob = ref('求职意向')
const avatarUrl = ref('')
const phone = ref('138-xxxx-xxxx')
const email = ref('your@email.com')
const eduStatus = ref('2028级毕业生')
const intendedCity = ref('全国')
const fileInput = ref(null)

// AI 交互
const showAiFloat = ref(false)
const floatX = ref(0)
const floatY = ref(0)
const selectedText = ref('')
const aiSuggestedText = ref('')
const editPrompt = ref('')
const isRegenerateModalOpen = ref(false)
const isPolishModalOpen = ref(false)
const polishActionType = ref('polish')

const renderedResume = computed(() => marked(rawMarkdown.value || ''))

// 监听 Markdown 自动解析姓名岗位
watch(rawMarkdown, (newVal) => {
  const titleMatch = newVal.match(/^#\s+(.+?)(?:\s*[-–—]\s*(.+))?$/m)
  if (titleMatch) {
    userName.value = titleMatch[1].trim()
    if (titleMatch[2]) targetJob.value = titleMatch[2].trim()
  }
}, { immediate: true })

onMounted(() => {
  const savedData = JSON.parse(localStorage.getItem('generatedResumeFullData') || '{}')
  userName.value = savedData.fullName || '求职者'
  targetJob.value = savedData.targetJob || '求职意向'
  phone.value = savedData.contactPhone || '138-xxxx-xxxx'
  email.value = savedData.email || 'your@email.com'
  eduStatus.value = savedData.eduStatus || '2028级毕业生'
  intendedCity.value = savedData.targetCity || '全国'
  
  rawMarkdown.value = localStorage.getItem('generatedResume') || ''
  avatarUrl.value = savedData.avatarUrl || localStorage.getItem('resumeAvatar') || ''

  const tid = sessionStorage.getItem('selectedTemplateId') || '1'
  const { layout, theme } = mapTemplateIdToLayoutAndTheme(tid)
  const lockLayout = sessionStorage.getItem('resumePreviewLockLayoutToTemplate') === '1'
  if (lockLayout) {
    sessionStorage.removeItem('resumePreviewLockLayoutToTemplate')
    currentLayout.value = layout
    currentTheme.value = theme
    sessionStorage.setItem('resumePreviewLayout', layout)
  } else {
    const savedLayout = sessionStorage.getItem('resumePreviewLayout')
    currentLayout.value = savedLayout && LAYOUT_KEYS.includes(savedLayout) ? savedLayout : layout
    currentTheme.value = theme
  }
})

// OSS 上传
const onFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  avatarUrl.value = URL.createObjectURL(file)
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/api/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200 && res.data?.url) {
      avatarUrl.value = res.data.url
      localStorage.setItem('resumeAvatar', res.data.url)
    }
  } catch (err) {
    alert('头像上传服务器失败，暂存本地')
  }
}

// 保存数据库
const saveToDatabase = async () => {
  isSaving.value = true
  const payload = {
    templateName: currentLayout.value,
    fullName: userName.value,
    contactPhone: phone.value,
    email: email.value,
    targetCity: intendedCity.value,
    eduStatus: eduStatus.value,
    avatarUrl: avatarUrl.value,
    projectExperience: rawMarkdown.value 
  }
  try {
    await request.post('/api/resume/save', payload)
    alert('✅ 简历已成功保存到您的账户！')
  } catch (e) {
    alert('保存失败，请检查登录状态')
  } finally {
    isSaving.value = false
  }
}

const saveSidebarInfo = (field, event) => {
  const text = event.target.innerText.trim()
  if (field === 'name') userName.value = text
  if (field === 'job') targetJob.value = text
  if (field === 'phone') phone.value = text.replace('📞', '').trim()
  if (field === 'email') email.value = text.replace('✉️', '').trim()
  if (field === 'edu') eduStatus.value = text.replace('🎓', '').trim()
  if (field === 'city') intendedCity.value = text.replace('📍', '').trim()
}

// AI 交互逻辑
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
  }
}

const hideFloatBtn = () => { showAiFloat.value = false }
const openRegenerateModal = () => {
  editPrompt.value = sessionStorage.getItem('tempResumePrompt') || ''
  isRegenerateModalOpen.value = true
}
const doRegenerate = async () => {
  isGenerating.value = true
  try {
    const res = await request.post('/api/resume/text-polish', {
      requirements: editPrompt.value, originalText: rawMarkdown.value
    })
    const content = typeof res === 'string' ? res : (typeof res?.data === 'string' ? res.data : String(res?.data ?? res ?? ''))
    rawMarkdown.value = content
    localStorage.setItem('generatedResume', content)
    isRegenerateModalOpen.value = false
  } catch (e) { alert('生成失败') } finally { isGenerating.value = false }
}
const polishActionName = computed(() => {
  const map = { 'polish': '润色', 'expand': '扩写', 'action': 'STAR' }
  return map[polishActionType.value]
})
const openPolishModal = (type) => {
  polishActionType.value = type; showAiFloat.value = false;
  aiSuggestedText.value = ''; isPolishModalOpen.value = true;
  callAiPolish();
}
const callAiPolish = async () => {
  isGenerating.value = true; aiSuggestedText.value = 'AI 思考中...';
  try {
    const res = await request.post('/api/resume/text-polish', {
      requirements: polishActionName.value, originalText: selectedText.value
    })
    aiSuggestedText.value = res.data || res
  } catch (e) { aiSuggestedText.value = '失败' } finally { isGenerating.value = false }
}
const applyPolishedText = () => {
  rawMarkdown.value = rawMarkdown.value.replace(selectedText.value, aiSuggestedText.value)
  isPolishModalOpen.value = false
}

const triggerUpload = () => fileInput.value.click()
const goBackToEdit = () => router.push('/dashboard')
const exportPDF = () => {
  nextTick(() => window.print())
}
const handleBlur = () => {}
</script>

<style scoped>
/* =========================================
   1. 基础容器与装饰
========================================= */
.preview-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  overflow-y: visible;
  padding: 20px 12px 48px;
  box-sizing: border-box;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  background: linear-gradient(145deg, #e8eef5 0%, #dce7f2 35%, #e5eaf3 55%, #dde8f0 100%);
}
.bg-decorations {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background: linear-gradient(145deg, #e8eef5 0%, #dce7f2 35%, #e5eaf3 55%, #dde8f0 100%);
}
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}
.orb-a {
  width: min(480px, 55vw);
  height: min(480px, 55vw);
  top: -8%;
  right: -5%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.32), transparent 70%);
}
.orb-b {
  width: min(420px, 50vw);
  height: min(420px, 50vw);
  bottom: 5%;
  left: -10%;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.26), transparent 70%);
}
.orb-c {
  width: min(360px, 45vw);
  height: min(360px, 45vw);
  top: 42%;
  left: 35%;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.18), transparent 70%);
}
.grain {
  position: absolute;
  inset: 0;
  opacity: 0.035;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
}

/* =========================================
   2. 工具栏与按钮
========================================= */
.glass-panel {
  background: rgba(255, 255, 255, 0.48);
  backdrop-filter: blur(20px) saturate(1.3);
  -webkit-backdrop-filter: blur(20px) saturate(1.3);
  border: 1px solid rgba(255, 255, 255, 0.75);
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(30, 58, 95, 0.06), 0 1px 0 rgba(255, 255, 255, 0.85) inset;
}
.toolbar {
  position: relative;
  z-index: 1;
  width: min(100%, 920px);
  max-width: 100%;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}
.toolbar-left, .toolbar-right { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.toolbar-left { flex: 1; min-width: 0; }
.theme-switcher, .layout-switcher { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.theme-switcher .label, .layout-switcher .label { font-size: 12px; color: #64748b; white-space: nowrap; }
.layout-chips { display: flex; flex-wrap: wrap; gap: 6px; max-width: 420px; }
.layout-chip {
  min-width: 28px;
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #475569;
  cursor: pointer;
}
.layout-chip:hover { border-color: #3b82f6; color: #1d4ed8; }
.layout-chip.active { background: #3b82f6; border-color: #3b82f6; color: #fff; }
.divider-v { width: 1px; height: 24px; background: #ddd; flex-shrink: 0; }
.icon-btn { background: none; border: none; color: #64748b; font-weight: bold; cursor: pointer; }
.color-dots { display: flex; gap: 8px; }
.dot { width: 18px; height: 18px; border-radius: 50%; cursor: pointer; border: 2px solid transparent; }
.dot.active { border-color: #3b82f6; }
.dot.dark { background: #4F525A; }
.dot.blue { background: #1e3a8a; }
.dot.light { background: #e2e8f0; }
.hint {
  position: relative;
  z-index: 1;
  width: min(100%, 920px);
  max-width: 100%;
  margin: 0 0 6px;
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.5;
  color: #92400e;
  background: rgba(254, 243, 199, 0.78);
  border: 1px solid rgba(251, 191, 36, 0.4);
  border-radius: 12px;
  box-sizing: border-box;
}
.hint-sub {
  position: relative;
  z-index: 1;
  width: min(100%, 920px);
  max-width: 100%;
  margin: 0 0 16px;
  padding: 0 10px;
  font-size: 12px;
  line-height: 1.55;
  color: #64748b;
  text-align: center;
  box-sizing: border-box;
}
.hint-sub b {
  color: #475569;
}
.action-btn { padding: 8px 16px; border-radius: 8px; font-weight: 600; font-size: 13px; cursor: pointer; border: none; }
.save-btn { background: #3b82f6; color: white; }
.ai-btn { background: linear-gradient(135deg, #a855f7, #6366f1); color: white; }
.export-btn { background: #10b981; color: white; }
.avatar-uploader { cursor: pointer; flex-shrink: 0; }
.loading-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.15);
  color: inherit;
  font-size: 12px;
  border: 2px dashed rgba(255,255,255,0.4);
  border-radius: 50%;
  width: 100px;
  height: 100px;
}
.avatar-uploader.sm .loading-avatar,
.avatar-uploader.sm .avatar { width: 64px; height: 64px; border-radius: 12px; font-size: 11px; }
.theme-light .top-header { color: #1e293b; }
.theme-light .top-header .loading-avatar { border-color: #94a3b8; color: #64748b; background: #f8fafc; }
.theme-light .info-section h3 { border-bottom-color: rgba(15, 23, 42, 0.12); }

/* =========================================
   3. 纸张与主题骨架
========================================= */
.resume-scroll-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  width: 100%;
  max-width: 980px;
  padding: 0 4px 50px;
  box-sizing: border-box;
}
.preview-stage-shell {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  justify-content: center;
  gap: clamp(0.5rem, 1.8vw, 1.1rem);
  width: 100%;
}
.rp-rail {
  position: relative;
  flex: 0 0 56px;
  width: 56px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 0.5rem;
  padding: 14px 8px;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(
    165deg,
    rgba(255, 255, 255, 0.55) 0%,
    rgba(219, 234, 254, 0.4) 50%,
    rgba(255, 255, 255, 0.36) 100%
  );
  backdrop-filter: blur(16px) saturate(1.3);
  -webkit-backdrop-filter: blur(16px) saturate(1.3);
  border: 1px solid rgba(255, 255, 255, 0.82);
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.08), 0 1px 0 rgba(255, 255, 255, 0.9) inset;
}
.rp-glow {
  position: absolute;
  inset: auto -35% 15% -35%;
  height: 40%;
  background: radial-gradient(ellipse, rgba(59, 130, 246, 0.2), transparent 72%);
  pointer-events: none;
}
.rp-glow-alt {
  inset: 12% -30% auto -30%;
  background: radial-gradient(ellipse, rgba(14, 165, 233, 0.18), transparent 72%);
}
.rp-bubble {
  position: relative;
  z-index: 1;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.85), rgba(255, 255, 255, 0.4));
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.1);
}
.rp-dots {
  position: relative;
  z-index: 1;
  width: 4px;
  height: 32px;
  border-radius: 999px;
  background: repeating-linear-gradient(
    180deg,
    rgba(59, 130, 246, 0.4) 0 4px,
    transparent 4px 9px
  );
}
.rp-vtext {
  position: relative;
  z-index: 1;
  writing-mode: vertical-rl;
  font-size: 0.5rem;
  font-weight: 900;
  letter-spacing: 0.28em;
  color: rgba(37, 99, 235, 0.5);
  text-transform: uppercase;
  padding: 4px 0;
}
.rp-vtext-alt {
  color: rgba(14, 116, 144, 0.5);
}
.a4-glass-frame {
  flex: 1 1 auto;
  min-width: 0;
  width: 210mm;
  max-width: 100%;
  padding: 0;
  box-sizing: border-box;
}
.rp-pedestal {
  padding: 10px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.42);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 8px 32px rgba(30, 64, 120, 0.08), 0 2px 0 rgba(255, 255, 255, 0.88) inset;
}
@media (max-width: 720px) {
  .rp-rail {
    display: none;
  }
  .preview-stage-shell {
    gap: 0;
  }
}
/* 屏幕至少 A4 高；打印时由 @media print 改为高度随内容，避免长简历被裁切 */
.a4-page { width: 100%; min-height: 297mm; height: auto; background: white; overflow: visible; box-shadow: 0 8px 40px rgba(0,0,0,0.08); }

/* 主题颜色变量 */
.theme-dark { --sidebar-bg: #4F525A; --sidebar-text: #FFFFFF; --main-title: #284c71; }
.theme-blue { --sidebar-bg: #1e3a8a; --sidebar-text: #FFFFFF; --main-title: #1e3a8a; }
.theme-light { --sidebar-bg: #f1f5f9; --sidebar-text: #1e293b; --main-title: #0f172a; }

/* 骨架 1: 经典双栏 */
.a4-page.layout-classic { background-image: linear-gradient(to right, var(--sidebar-bg) 250px, #fff 250px); }
.resume-dual-column { display: flex; min-height: 297mm; align-items: stretch; }
.layout-fallback .left-sidebar { background: var(--sidebar-bg); color: var(--sidebar-text); }
.left-sidebar { width: 250px; padding: 30mm 15mm; color: var(--sidebar-text); text-align: center; }
.avatar { width: 100px; height: 100px; border-radius: 50%; border: 3px solid rgba(255,255,255,0.5); object-fit: cover; }
.name { font-size: 24px; font-weight: bold; margin-top: 15px; outline: none; }
.position { font-size: 14px; opacity: 0.8; margin-top: 5px; outline: none; }
.info-section { text-align: left; margin-top: 30px; }
.info-section h3 { font-size: 16px; border-bottom: 1px solid rgba(255,255,255,0.3); padding-bottom: 5px; margin-bottom: 15px; }
.info-section li { font-size: 13px; margin-bottom: 8px; outline: none; list-style: none; }

/* 骨架 2: 商务上下 */
.resume-top-down { display: flex; flex-direction: column; min-height: 297mm; }
.top-header { display: flex; justify-content: space-between; align-items: center; padding: 25mm 20mm; background: var(--sidebar-bg); color: var(--sidebar-text); }
.header-content .name { font-size: 36px; margin-bottom: 5px; text-align: left; outline: none; }
.header-content .position { font-size: 18px; text-align: left; outline: none; }
.contact-row { display: flex; gap: 10px; font-size: 13px; margin-top: 12px; }

/* 骨架 3: 极简留白 */
.resume-minimal { display: flex; min-height: 297mm; position: relative; }
.color-ribbon { width: 15px; background: var(--main-title); position: absolute; left: 0; top: 0; bottom: 0; }
.minimal-wrapper { padding: 25mm 20mm 25mm 34mm; flex: 1; }
.minimal-header { display: flex; gap: 20px; border-bottom: 2px solid #eee; padding-bottom: 20px; align-items: center; }
.minimal-header .avatar { width: 80px; height: 80px; border-radius: 8px; }
.contact-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 13px; margin-top: 10px; text-align: left; }

/* 骨架 4: 时间线 */
.resume-timeline { display: flex; min-height: 297mm; position: relative; background: #fafafa; }
.timeline-accent { width: 6px; background: linear-gradient(180deg, var(--main-title), var(--sidebar-bg)); flex-shrink: 0; }
.timeline-inner { flex: 1; padding: 22mm 18mm 22mm 14mm; }
.timeline-head { display: flex; gap: 16px; align-items: center; margin-bottom: 18px; padding-bottom: 16px; border-bottom: 1px solid #e2e8f0; }
.timeline-head .name { margin-top: 0; font-size: 26px; color: #0f172a; }
.timeline-head .position { color: var(--main-title); opacity: 1; font-weight: 600; }
.contact-inline { display: flex; flex-wrap: wrap; gap: 12px; font-size: 12px; color: #64748b; margin-top: 8px; }
.timeline-md :deep(h2) { position: relative; padding-left: 14px; border: none; }
.timeline-md :deep(h2)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.35em;
  width: 6px;
  height: 1em;
  background: var(--main-title);
  border-radius: 2px;
}

/* 骨架 5: 横幅 */
.resume-banner { display: flex; flex-direction: column; min-height: 297mm; }
.banner-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18mm 20mm;
  background: linear-gradient(135deg, var(--sidebar-bg) 0%, #334155 100%);
  color: #fff;
}
.theme-light .banner-hero { background: linear-gradient(135deg, #e2e8f0, #f8fafc); color: #1e293b; }
.banner-hero .name { margin-top: 0; font-size: 32px; }
.banner-hero .position { opacity: 0.95; font-size: 16px; }
.banner-contact { display: flex; flex-wrap: wrap; gap: 14px; font-size: 13px; margin-top: 12px; opacity: 0.9; }
.banner-body { flex: 1; background: #fff; }
.banner-body .main-content { padding: 12mm 20mm 20mm; }

/* 骨架 6: 杂志 */
.resume-magazine { min-height: 297mm; padding: 18mm 16mm 22mm; background: #fff; }
.mag-topline { height: 4px; background: var(--main-title); margin-bottom: 14px; }
.mag-head { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; border-bottom: 3px double #e2e8f0; padding-bottom: 12px; }
.mag-title-block .name { margin-top: 0; font-size: 34px; letter-spacing: -0.02em; color: #0f172a; }
.mag-title-block .position { font-size: 15px; color: var(--main-title); font-weight: 600; text-transform: uppercase; letter-spacing: 0.08em; }
.mag-meta { display: flex; flex-wrap: wrap; gap: 16px; font-size: 12px; color: #64748b; margin: 14px 0 18px; }
.mag-md { column-count: 2; column-gap: 28px; font-size: 13px; line-height: 1.75; }
.mag-md :deep(h2) { break-after: avoid; column-span: all; margin-top: 20px; }
.mag-md :deep(ul) { break-inside: avoid; }

/* 骨架 7: 卡片 */
.resume-cardlayout { min-height: 297mm; padding: 16mm; background: linear-gradient(180deg, #f1f5f9 0%, #e2e8f0 100%); }
.card-top { display: flex; gap: 16px; align-items: center; background: #fff; padding: 14mm 16mm; border-radius: 12px; box-shadow: 0 4px 24px rgba(15,23,42,0.08); }
.card-top-text .name { margin-top: 0; font-size: 28px; color: #0f172a; }
.card-top-text .position { color: var(--main-title); font-weight: 600; }
.card-contact { font-size: 13px; color: #64748b; margin-top: 8px; display: flex; gap: 12px; flex-wrap: wrap; }
.card-sheet { margin-top: 10mm; background: #fff; padding: 12mm 16mm 18mm; border-radius: 12px; box-shadow: 0 4px 24px rgba(15,23,42,0.06); }
.card-md :deep(h2) {
  font-size: 15px;
  background: linear-gradient(90deg, var(--main-title), transparent);
  color: #fff;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  margin-top: 22px;
}

/* 骨架 8: 典雅 */
.resume-elegant { min-height: 297mm; padding: 18mm 14mm; background: #fdfcfa; }
.elegant-frame { max-width: 170mm; margin: 0 auto; border: 1px solid #e7e5e4; padding: 16mm 18mm; background: #fff; box-shadow: 0 1px 0 rgba(0,0,0,0.04); }
.elegant-line { width: 48px; height: 3px; background: var(--main-title); margin-bottom: 12px; }
.elegant-header .name { margin-top: 0; font-family: Georgia, 'Times New Roman', serif; font-size: 30px; font-weight: 400; color: #1c1917; }
.elegant-header .position { font-size: 14px; color: #78716c; letter-spacing: 0.12em; text-transform: uppercase; margin-top: 6px; }
.elegant-contact { font-size: 13px; color: #57534e; margin-top: 14px; }
.dot-sep { margin: 0 6px; color: #d6d3d1; }
.elegant-av { float: right; margin: 0 0 8px 16px; }
.elegant-av .avatar,
.elegant-av .loading-avatar { width: 88px; height: 88px; border-radius: 4px; }
.elegant-md { clear: both; font-family: Georgia, 'Songti SC', serif; font-size: 13px; line-height: 1.85; color: #44403c; }
.elegant-md :deep(h2) { font-family: 'PingFang SC', sans-serif; font-size: 15px; border-bottom: 1px solid #d6d3d1; color: #1c1917; }

/* =========================================
   4. 内容排版
========================================= */
.main-content { flex: 1; padding: 30mm 20mm; outline: none; text-align: left; min-height: 0; }
.top-header + .main-content,
.resume-top-down > .main-content { padding-top: 14mm; }
.minimal-wrapper .main-content { padding-top: 8mm; }
.markdown-body { font-size: 14px; line-height: 1.8; color: #333; }
.markdown-body :deep(h2) { color: var(--main-title); border-bottom: 2px solid var(--main-title); margin-top: 25px; padding-bottom: 5px; font-size: 18px; }
.markdown-body :deep(ul) { padding-left: 20px; margin-bottom: 10px; }
.markdown-body :deep(li) { margin-bottom: 6px; }
.markdown-body :deep(p) { margin: 0.5em 0; }
.markdown-body :deep(strong) { color: #1e293b; }

/* =========================================
   5. AI 浮窗与 Modal
========================================= */
.ai-float-menu { position: absolute; z-index: 1000; background: #fff; padding: 6px; border-radius: 8px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); display: flex; gap: 5px; }
.ai-float-menu button { border: none; background: #f1f5f9; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 2000; }
.modal-content { background: #fff; width: 500px; padding: 20px; border-radius: 12px; }
.modal-content.large { width: 700px; }
textarea { width: 100%; padding: 10px; border-radius: 8px; border: 1px solid #ddd; resize: vertical; }

/* =========================================
   6. 打印 / 导出 PDF（多页、完整内容）
========================================= */
@media print {
  @page { size: A4; margin: 12mm; }
  .toolbar, .bg-decorations, .hint, .hint-sub, .rp-rail, .ai-float-menu, .modal-overlay { display: none !important; }
  .preview-container {
    padding: 0 !important;
    margin: 0 !important;
    background: #fff !important;
    height: auto !important;
    min-height: 0 !important;
    max-height: none !important;
    overflow: visible !important;
    display: block !important;
  }
  .resume-scroll-wrapper {
    display: block !important;
    padding: 0 !important;
    overflow: visible !important;
  }
  .a4-glass-frame {
    width: 100% !important;
    max-width: none !important;
    box-shadow: none !important;
    border: none !important;
  }
  .a4-page {
    width: 100% !important;
    min-height: 0 !important;
    height: auto !important;
    max-height: none !important;
    overflow: visible !important;
    box-shadow: none !important;
    page-break-after: auto;
    break-inside: auto;
  }
  .resume-dual-column,
  .resume-top-down,
  .resume-minimal,
  .resume-timeline,
  .resume-banner,
  .resume-magazine,
  .resume-cardlayout,
  .resume-elegant {
    min-height: 0 !important;
    height: auto !important;
    overflow: visible !important;
    break-inside: auto;
  }
  /* 双栏打印：改为上下堆叠，避免侧栏重复跨页错位 */
  .layout-classic .resume-dual-column {
    flex-direction: column !important;
    background-image: none !important;
  }
  .layout-classic .left-sidebar {
    width: 100% !important;
    max-width: 100% !important;
    padding: 8mm 10mm !important;
    page-break-after: avoid;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
  .layout-classic .main-content { padding: 8mm 10mm !important; }
  .layout-classic.a4-page { border-left: none !important; }
  .mag-md { column-count: 1 !important; }
  .main-content { overflow: visible !important; }
  .markdown-body :deep(h2) {
    break-after: avoid-page;
    page-break-after: avoid;
  }
  .markdown-body :deep(li), .markdown-body :deep(p) {
    orphans: 3;
    widows: 3;
  }
  .top-header, .banner-hero, .color-ribbon, .timeline-accent, .card-top, .mag-topline {
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
}
</style>