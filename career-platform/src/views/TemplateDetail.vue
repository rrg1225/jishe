<template>
  <div class="template-detail-page template-detail-for-print">
    <div class="page-bg no-print" aria-hidden="true">
      <div class="orb orb-a"></div>
      <div class="orb orb-b"></div>
      <div class="orb orb-c"></div>
      <div class="grain"></div>
    </div>

    <div class="template-detail-inner" :class="{ 'template-detail-inner--wide-edit': isInlineEdit }">
      <!-- 顶栏：玻璃条 -->
      <div class="glass-bar detail-toolbar no-print">
        <button type="button" class="btn-ghost" @click="router.back()">
          <span class="btn-ghost-icon">←</span>
          返回列表
        </button>

        <div v-if="detail" class="toolbar-badge">
          {{ detail.type === 1 ? '精选模板' : '优秀简历' }}
        </div>

        <div class="toolbar-actions">
          <button type="button" class="btn-glass btn-glass-dark" @click="downloadDoc">
            <span class="btn-emoji">📥</span>
            下载 Word 原件
          </button>
          <button
            v-if="detail?.type === 1 && !isInlineEdit"
            type="button"
            class="btn-glass btn-glass-accent"
            @click="enterInlineEdit"
          >
            <span class="btn-emoji">✏️</span>
            在线编辑填写
          </button>
          <button
            v-if="detail?.type === 1 && isInlineEdit"
            type="button"
            class="btn-glass btn-glass-muted"
            @click="exitInlineEdit"
          >
            <span class="btn-emoji">👁️</span>
            仅看预览图
          </button>
          <button
            v-if="detail?.type === 1 && isInlineEdit"
            type="button"
            class="btn-glass btn-glass-accent"
            @click="exportPdfHere"
          >
            <span class="btn-emoji">🖨️</span>
            导出 PDF
          </button>
          <button
            v-if="detail?.type === 1 && isInlineEdit"
            type="button"
            class="btn-glass btn-glass-muted"
            @click="goResumePreview"
          >
            <span class="btn-emoji">✨</span>
            前往预览页（AI 润色）
          </button>
        </div>
      </div>

      <!-- 标题区 -->
      <div v-if="detail" class="glass-panel title-panel no-print">
        <h1 class="title-main">{{ detail.name }}</h1>
        <div class="title-meta">
          <span class="meta-pill">📅 更新 {{ formatDate(detail.updateTime) }}</span>
          <span class="meta-pill">📄 Word (.docx)</span>
        </div>
      </div>

      <!-- 预览：与标题区等宽 + 两侧玻璃装饰 -->
      <div v-if="detail" class="preview-section">
        <div class="preview-glass-pedestal glass-panel pedestal-panel">
          <div class="preview-stage">
            <aside class="stage-rail stage-rail-left no-print" aria-hidden="true">
              <div class="rail-glow"></div>
              <div class="deco-bubble" style="animation-delay: 0s">📋</div>
              <div class="rail-dots"></div>
              <div class="deco-bubble deco-bubble-sm" style="animation-delay: 0.4s">✨</div>
              <div class="rail-vertical-text">RESUME</div>
              <div class="deco-chip">STAR</div>
              <div class="rail-dots rail-dots-soft"></div>
              <div class="rail-spacer"></div>
              <div class="deco-mini-square"></div>
            </aside>

            <div class="preview-card-column">
              <div class="preview-card" :class="{ 'preview-card--edit': isInlineEdit }">
                <div class="preview-card-inner">
                  <template v-if="!isInlineEdit">
                    <img
                      v-if="detail.previewUrl"
                      :src="detail.previewUrl"
                      class="preview-img"
                      @error="handleImgError"
                      alt="简历预览"
                    />
                    <div v-else class="preview-placeholder">
                      <span class="ph-icon">📄</span>
                      <p>预览图加载中...</p>
                    </div>
                  </template>
                  <div v-else class="inline-edit-wrap inline-edit-wrap--wysiwyg">
                    <p class="edit-tip no-print">
                      直接在版式上点击修改正文与侧栏；版式与导出页一致，修改会自动保存。点顶部「导出 PDF」使用浏览器打印保存，无需跳转。
                    </p>
                    <div class="wysiwyg-paper-wrap">
                      <ResumePaperCanvas
                        v-model="draftMarkdown"
                        editable
                        :layout="previewLayout"
                        :theme="previewTheme"
                        :user-name="previewUserName"
                        :target-job="previewTargetJob"
                        :phone="previewPhone"
                        :email="previewEmail"
                        :edu-status="previewEduStatus"
                        :intended-city="previewIntendedCity"
                        :avatar-url="previewAvatarUrl"
                        @contact-change="onPaperContactChange"
                        @avatar-file="onAvatarFile"
                      />
                    </div>
                    <details class="md-source-fold no-print">
                      <summary>查看 / 编辑 Markdown 源码</summary>
                      <textarea
                        v-model="draftMarkdown"
                        class="inline-md-editor inline-md-editor--fold"
                        spellcheck="false"
                        rows="12"
                        placeholder="# 姓名 - 求职意向&#10;&#10;## 教育经历&#10;&#10;..."
                      />
                    </details>
                    <div class="edit-actions no-print">
                      <span class="save-status">{{ saveStatusText }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <p v-if="!isInlineEdit" class="scroll-hint">↓ 使用浏览器右侧滚动条，可查看完整预览长图</p>
              <p v-else class="scroll-hint">↓ 左右分栏可分别上下滚动；窄屏下预览在源码下方</p>
            </div>

            <aside class="stage-rail stage-rail-right no-print" aria-hidden="true">
              <div class="rail-glow rail-glow-alt"></div>
              <div class="deco-bubble" style="animation-delay: 0.2s">🎯</div>
              <div class="rail-dots"></div>
              <div class="deco-bubble deco-bubble-sm" style="animation-delay: 0.6s">💼</div>
              <div class="rail-vertical-text rail-vertical-text-alt">OFFER</div>
              <div class="deco-ring"></div>
              <div class="rail-dots rail-dots-soft"></div>
              <div class="rail-spacer"></div>
              <div class="deco-mini-square deco-mini-square-alt"></div>
            </aside>
          </div>
        </div>
      </div>

      <p v-if="detail" class="footer-note no-print">
        资源版权归 AI 职友平台所有，严禁非法转售
      </p>

      <div v-else-if="loading" class="state-block glass-panel state-loading">
        <div class="spinner"></div>
        <p class="loading-text">正在载入模板…</p>
      </div>

      <div v-else class="state-block glass-panel state-empty">
        <div class="empty-icon">🏜️</div>
        <p>未能加载资源，请检查链接</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import ResumePaperCanvas from '../components/ResumePaperCanvas.vue'

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

function mapTemplateIdToLayoutAndTheme(templateId) {
  const n = parseInt(String(templateId), 10)
  const idx = Number.isFinite(n) ? ((n - 1) % 8 + 8) % 8 : 0
  return {
    layout: LAYOUT_KEYS[idx],
    theme: ['theme-dark', 'theme-blue', 'theme-light'][idx % 3]
  }
}

/** 与 Word 预览结构对应的空白骨架，每个模板独立存于 resumeDraft_template_{id} */
const TEMPLATE_SKELETON_MD = `# 姓名 - 求职意向

## 教育经历

（学校、专业、时间…）

## 项目与经历

（项目名、职责、成果…）

## 专业技能

（技能列表…）
`

function templateDraftStorageKey(id) {
  return `resumeDraft_template_${id}`
}

function loadTemplateDraft(id) {
  if (id == null || id === '') return ''
  try {
    return localStorage.getItem(templateDraftStorageKey(id)) || ''
  } catch {
    return ''
  }
}

function saveTemplateDraft(id, md) {
  if (id == null || id === '') return
  localStorage.setItem(templateDraftStorageKey(id), md)
}

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const loading = ref(true)
const isInlineEdit = ref(false)
const draftMarkdown = ref('')
const saveStatusText = ref('')

const previewLayout = computed(() => {
  const tid =
    detail.value?.id ??
    route.params?.id ??
    sessionStorage.getItem('selectedTemplateId') ??
    '1'
  return mapTemplateIdToLayoutAndTheme(tid).layout
})
const previewTheme = computed(() => {
  const tid =
    detail.value?.id ??
    route.params?.id ??
    sessionStorage.getItem('selectedTemplateId') ??
    '1'
  return mapTemplateIdToLayoutAndTheme(tid).theme
})

const previewUserName = ref('姓名')
const previewTargetJob = ref('求职意向')
const previewPhone = ref('138-xxxx-xxxx')
const previewEmail = ref('your@email.com')
const previewEduStatus = ref('2028级毕业生')
const previewIntendedCity = ref('全国')
const previewAvatarUrl = ref('')

function hydratePreviewSidebars() {
  previewPhone.value = '138-xxxx-xxxx'
  previewEmail.value = 'your@email.com'
  previewEduStatus.value = '2028级毕业生'
  previewIntendedCity.value = '全国'
  previewAvatarUrl.value = localStorage.getItem('resumeAvatar') || ''
  try {
    const d = JSON.parse(localStorage.getItem('generatedResumeFullData') || '{}')
    if (d.contactPhone) previewPhone.value = d.contactPhone
    if (d.email) previewEmail.value = d.email
    if (d.eduStatus) previewEduStatus.value = d.eduStatus
    if (d.targetCity) previewIntendedCity.value = d.targetCity
    if (d.avatarUrl) previewAvatarUrl.value = d.avatarUrl
  } catch {
    /* ignore */
  }
}

function applyTitleFromMarkdown(text) {
  const titleMatch = (text || '').match(/^#\s+(.+?)(?:\s*[-–—]\s*(.+))?$/m)
  if (titleMatch) {
    previewUserName.value = titleMatch[1].trim()
    previewTargetJob.value = titleMatch[2]?.trim() || '求职意向'
  }
}

let saveDebounce = null

const persistDraft = () => {
  const id = detail.value?.id
  if (id == null) return
  try {
    saveTemplateDraft(id, draftMarkdown.value)
    saveStatusText.value = '已保存到本地 ' + new Date().toLocaleTimeString()
  } catch {
    saveStatusText.value = '本地保存失败'
  }
}

function mergeResumeFullData() {
  try {
    const prev = JSON.parse(localStorage.getItem('generatedResumeFullData') || '{}')
    const merged = {
      ...prev,
      fullName: previewUserName.value,
      targetJob: previewTargetJob.value,
      contactPhone: previewPhone.value,
      email: previewEmail.value,
      eduStatus: previewEduStatus.value,
      targetCity: previewIntendedCity.value,
      avatarUrl: previewAvatarUrl.value
    }
    localStorage.setItem('generatedResumeFullData', JSON.stringify(merged))
  } catch {
    /* ignore */
  }
}

watch(draftMarkdown, (newVal) => {
  if (isInlineEdit.value) {
    applyTitleFromMarkdown(newVal)
    mergeResumeFullData()
    clearTimeout(saveDebounce)
    saveDebounce = setTimeout(persistDraft, 500)
  }
})

const enterInlineEdit = () => {
  const id = detail.value?.id
  if (id != null) {
    sessionStorage.setItem('selectedTemplateId', String(id))
  }
  const existing = id != null ? loadTemplateDraft(id) : ''
  draftMarkdown.value = existing && existing.trim() ? existing : TEMPLATE_SKELETON_MD
  hydratePreviewSidebars()
  applyTitleFromMarkdown(draftMarkdown.value)
  isInlineEdit.value = true
  persistDraft()
}

const exitInlineEdit = () => {
  persistDraft()
  isInlineEdit.value = false
}

function onPaperContactChange(patch) {
  if (patch.eduStatus != null) previewEduStatus.value = patch.eduStatus
  if (patch.phone != null) previewPhone.value = patch.phone
  if (patch.email != null) previewEmail.value = patch.email
  if (patch.intendedCity != null) previewIntendedCity.value = patch.intendedCity
  mergeResumeFullData()
}

async function onAvatarFile(file) {
  if (!file) return
  previewAvatarUrl.value = URL.createObjectURL(file)
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/api/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200 && res.data?.url) {
      previewAvatarUrl.value = res.data.url
      localStorage.setItem('resumeAvatar', res.data.url)
      mergeResumeFullData()
    }
  } catch {
    alert('头像上传服务器失败，暂用本地预览')
  }
}

function syncGeneratedResumeFromTemplate() {
  const id = detail.value?.id
  if (id == null) return
  sessionStorage.setItem('selectedTemplateId', String(id))
  const md = (draftMarkdown.value.trim() || loadTemplateDraft(id) || '').trim()
  try {
    localStorage.setItem('generatedResume', md || TEMPLATE_SKELETON_MD)
    mergeResumeFullData()
  } catch {
    /* ignore */
  }
}

function exportPdfHere() {
  persistDraft()
  syncGeneratedResumeFromTemplate()
  nextTick(() => window.print())
}

const goResumePreview = () => {
  if (isInlineEdit.value) {
    persistDraft()
  }
  syncGeneratedResumeFromTemplate()
  sessionStorage.setItem('resumePreviewLockLayoutToTemplate', '1')
  router.push({ path: '/resume-preview' })
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/templates/${route.params.id}`)
    detail.value = (res && res.code === 200) ? res.data : res
    if (route.query.edit === '1' && detail.value?.type === 1) {
      enterInlineEdit()
    }
  } catch (error) {
    console.error("加载详情失败:", error)
  } finally {
    setTimeout(() => { loading.value = false }, 400)
  }
})

onUnmounted(() => {
  clearTimeout(saveDebounce)
  if (isInlineEdit.value) persistDraft()
})

const handleImgError = (e) => {
  e.target.src = 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?w=800'
}

const downloadDoc = () => {
  if (detail.value?.docUrl) window.open(detail.value.docUrl, '_blank')
  else alert("下载链接失效")
}

const formatDate = (dateStr) => {
  if (!dateStr) return '2026-03-26'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.template-detail-page {
  position: relative;
  min-height: 100vh;
  box-sizing: border-box;
  padding: clamp(1.25rem, 4vw, 2.5rem) clamp(1rem, 3vw, 1.5rem) 3.5rem;
  overflow-x: hidden;
  overflow-y: visible;
  font-family: "PingFang SC", "Microsoft YaHei", system-ui, sans-serif;
}

/* 背景：柔和渐变 + 光斑 */
.page-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background:
    linear-gradient(145deg, #e8eef5 0%, #dce7f2 35%, #e5eaf3 55%, #dde8f0 100%);
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.55;
}
.orb-a {
  width: min(480px, 55vw);
  height: min(480px, 55vw);
  top: -8%;
  right: -5%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.35), transparent 70%);
}
.orb-b {
  width: min(420px, 50vw);
  height: min(420px, 50vw);
  bottom: 5%;
  left: -10%;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.28), transparent 70%);
}
.orb-c {
  width: min(360px, 45vw);
  height: min(360px, 45vw);
  top: 42%;
  left: 35%;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.2), transparent 70%);
}

.grain {
  position: absolute;
  inset: 0;
  opacity: 0.04;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
}

.template-detail-inner {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 58rem;
  margin-left: auto;
  margin-right: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.template-detail-inner--wide-edit {
  max-width: min(1240px, 100%);
}

/* 玻璃通用 */
.glass-bar,
.glass-panel {
  background: rgba(255, 255, 255, 0.42);
  backdrop-filter: blur(20px) saturate(1.35);
  -webkit-backdrop-filter: blur(20px) saturate(1.35);
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow:
    0 4px 24px rgba(30, 58, 95, 0.06),
    0 1px 0 rgba(255, 255, 255, 0.8) inset,
    0 -1px 0 rgba(255, 255, 255, 0.35) inset;
}

.glass-bar {
  width: 100%;
  border-radius: 20px;
  padding: 0.85rem 1.25rem;
}

.detail-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1.75rem;
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.5rem 0.85rem;
  border: none;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.35);
  color: #475569;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.9) inset;
}
.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.65);
  color: #2563eb;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.12);
}
.btn-ghost-icon {
  font-size: 1.35rem;
  line-height: 1;
}

.toolbar-badge {
  font-size: 0.7rem;
  font-weight: 800;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #64748b;
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.65rem;
  align-items: center;
}

.btn-glass {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.65rem 1.15rem;
  border: none;
  border-radius: 16px;
  font-weight: 700;
  font-size: 0.875rem;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.2s, filter 0.2s;
  box-shadow:
    0 4px 14px rgba(15, 23, 42, 0.12),
    0 1px 0 rgba(255, 255, 255, 0.25) inset;
}
.btn-glass:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.14);
}
.btn-glass:active {
  transform: translateY(0);
}
.btn-glass-dark {
  background: linear-gradient(145deg, #1e293b, #0f172a);
  color: #f8fafc;
}
.btn-glass-accent {
  background: linear-gradient(145deg, #3b82f6, #2563eb);
  color: #fff;
}
.btn-glass-muted {
  background: rgba(255, 255, 255, 0.55);
  color: #334155;
  border: 1px solid rgba(148, 163, 184, 0.45);
  box-shadow:
    0 2px 10px rgba(15, 23, 42, 0.06),
    0 1px 0 rgba(255, 255, 255, 0.9) inset;
}
.btn-emoji {
  font-size: 1rem;
}

/* 标题玻璃板（预览区与其同宽、同款圆角与水平内边距） */
.title-panel {
  width: 100%;
  border-radius: 22px;
  padding: 1.75rem 1.5rem 1.5rem;
  margin-bottom: 1.5rem;
  text-align: center;
  box-sizing: border-box;
}

.title-main {
  margin: 0 0 0.75rem;
  font-size: clamp(1.5rem, 4vw, 2rem);
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.8);
}

.title-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.65rem;
}

.meta-pill {
  font-size: 0.8125rem;
  color: #64748b;
  padding: 0.4rem 0.9rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

/* 预览区：与标题栏同宽 */
.preview-section {
  width: 100%;
  margin-bottom: 2rem;
  box-sizing: border-box;
}

/* 与 .title-panel 同宽、同水平 padding，整体更一体 */
.preview-glass-pedestal.pedestal-panel {
  width: 100%;
  max-width: 100%;
  padding: 1.75rem 1.5rem 1.75rem;
  border-radius: 22px;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(24px) saturate(1.45);
  -webkit-backdrop-filter: blur(24px) saturate(1.45);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow:
    0 8px 32px rgba(30, 64, 120, 0.08),
    0 2px 0 rgba(255, 255, 255, 0.88) inset,
    0 0 0 1px rgba(255, 255, 255, 0.4) inset;
}

.preview-stage {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  justify-content: center;
  gap: clamp(0.65rem, 2.2vw, 1.35rem);
  width: 100%;
  min-height: 0;
}

.preview-card-column {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  gap: 0.5rem;
}

.scroll-hint {
  margin: 0;
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 600;
}

/* 两侧：通高玻璃装饰轨（与中间预览区等高） */
.stage-rail {
  position: relative;
  flex: 0 0 72px;
  width: 72px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 0.55rem;
  padding: 14px 10px;
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(
    165deg,
    rgba(255, 255, 255, 0.55) 0%,
    rgba(219, 234, 254, 0.42) 45%,
    rgba(255, 255, 255, 0.38) 100%
  );
  backdrop-filter: blur(18px) saturate(1.35);
  -webkit-backdrop-filter: blur(18px) saturate(1.35);
  border: 1px solid rgba(255, 255, 255, 0.85);
  box-shadow:
    0 8px 28px rgba(37, 99, 235, 0.1),
    0 1px 0 rgba(255, 255, 255, 0.95) inset,
    inset 0 0 0 1px rgba(255, 255, 255, 0.35);
}

.rail-glow {
  position: absolute;
  inset: auto -40% 20% -40%;
  height: 45%;
  background: radial-gradient(ellipse, rgba(59, 130, 246, 0.22), transparent 70%);
  pointer-events: none;
}
.rail-glow-alt {
  background: radial-gradient(ellipse, rgba(14, 165, 233, 0.2), transparent 70%);
  inset: 10% -30% auto -30%;
}

.rail-dots {
  width: 4px;
  flex: 0 0 auto;
  height: 40px;
  border-radius: 999px;
  background: repeating-linear-gradient(
    180deg,
    rgba(59, 130, 246, 0.45) 0 4px,
    transparent 4px 10px
  );
  opacity: 0.85;
}
.rail-dots-soft {
  opacity: 0.45;
  height: 28px;
}

.rail-vertical-text {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  font-size: 0.5rem;
  font-weight: 900;
  letter-spacing: 0.35em;
  color: rgba(37, 99, 235, 0.55);
  text-transform: uppercase;
  padding: 6px 0;
  border-top: 1px solid rgba(59, 130, 246, 0.15);
  border-bottom: 1px solid rgba(59, 130, 246, 0.15);
}
.rail-vertical-text-alt {
  color: rgba(14, 116, 144, 0.55);
  border-color: rgba(14, 165, 233, 0.2);
}

.deco-mini-square {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(191, 219, 254, 0.5));
  border: 1px solid rgba(59, 130, 246, 0.2);
  box-shadow: 0 4px 10px rgba(59, 130, 246, 0.12);
  animation: deco-float 4.5s ease-in-out infinite 0.3s;
}
.deco-mini-square-alt {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(186, 230, 253, 0.55));
}

.rail-spacer {
  flex: 1 1 auto;
  min-height: 12px;
  width: 100%;
}

.stage-rail .deco-bubble,
.stage-rail .deco-bubble-sm,
.stage-rail .deco-chip,
.stage-rail .deco-ring {
  position: relative;
  z-index: 1;
}

.deco-bubble {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  line-height: 1;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.75), rgba(255, 255, 255, 0.35));
  backdrop-filter: blur(14px) saturate(1.2);
  -webkit-backdrop-filter: blur(14px) saturate(1.2);
  border: 1px solid rgba(255, 255, 255, 0.85);
  box-shadow:
    0 6px 20px rgba(37, 99, 235, 0.1),
    0 1px 0 rgba(255, 255, 255, 1) inset;
  animation: deco-float 5s ease-in-out infinite;
}

.deco-bubble-sm {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  font-size: 1.05rem;
}

.deco-rod {
  width: 3px;
  height: 36px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(59, 130, 246, 0.45), rgba(99, 102, 241, 0.15));
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.25);
}

.deco-rod-alt {
  background: linear-gradient(180deg, rgba(14, 165, 233, 0.5), rgba(59, 130, 246, 0.12));
}

.deco-chip {
  font-size: 0.55rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  color: #3b82f6;
  padding: 0.35rem 0.45rem;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(59, 130, 246, 0.25);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
}

.deco-ring {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid rgba(59, 130, 246, 0.35);
  border-top-color: rgba(99, 102, 241, 0.75);
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.4) inset;
  animation: deco-spin 10s linear infinite;
}

@keyframes deco-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

@keyframes deco-spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 700px) {
  .stage-rail {
    display: none;
  }
  .preview-glass-pedestal.pedestal-panel {
    padding: 1.35rem 1.1rem;
  }
}

/* 简历预览：随内容增高，由浏览器窗口右侧滚动条滚动整页 */
.preview-card {
  width: 100%;
  max-width: min(720px, 100%);
  border-radius: 20px;
  padding: 3px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.95), rgba(241, 245, 249, 0.9));
  box-shadow:
    0 20px 50px rgba(15, 23, 42, 0.12),
    0 4px 12px rgba(15, 23, 42, 0.06),
    0 1px 0 rgba(255, 255, 255, 1) inset;
  border: 1px solid rgba(255, 255, 255, 0.9);
}

.preview-card--edit {
  max-width: min(1200px, 100%);
}

.preview-card-inner {
  width: 100%;
  border-radius: 17px;
  overflow: visible;
  background: #fafbfc;
  box-shadow: inset 0 1px 3px rgba(15, 23, 42, 0.06);
}

.inline-edit-wrap {
  padding: 1rem 1.1rem 1.25rem;
  text-align: left;
}

.inline-edit-wrap--wysiwyg {
  padding: 0.75rem 0.85rem 1rem;
}

.wysiwyg-paper-wrap {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.md-source-fold {
  margin-top: 1rem;
  padding: 0.65rem 0.85rem;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.md-source-fold summary {
  cursor: pointer;
  font-weight: 700;
  font-size: 0.8125rem;
  color: #475569;
  user-select: none;
}

.inline-md-editor--fold {
  margin-top: 0.65rem;
  min-height: 140px;
}

.inline-edit-split {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 1rem;
  align-items: stretch;
}

@media (max-width: 900px) {
  .inline-edit-split {
    grid-template-columns: 1fr;
  }
}

.inline-edit-col {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.inline-col-label {
  display: block;
  font-size: 0.7rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #94a3b8;
  margin: 0 0 0.45rem;
}

.inline-edit-col--source .inline-md-editor {
  flex: 1;
  min-height: min(48vh, 480px);
}

.live-preview-viewport {
  flex: 1;
  min-height: min(48vh, 480px);
  max-height: min(72vh, 820px);
  overflow: auto;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: linear-gradient(180deg, #e8edf3 0%, #dce3ec 100%);
  padding: 10px;
  box-shadow: inset 0 1px 2px rgba(15, 23, 42, 0.06);
}

.edit-tip {
  margin: 0 0 0.75rem;
  font-size: 0.8125rem;
  line-height: 1.55;
  color: #64748b;
  font-weight: 600;
}

.inline-md-editor {
  display: block;
  width: 100%;
  min-height: 220px;
  box-sizing: border-box;
  padding: 1rem 1.1rem;
  margin: 0;
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 14px;
  font-family: ui-monospace, "Cascadia Code", "SF Mono", Menlo, Consolas, monospace;
  font-size: 0.8125rem;
  line-height: 1.65;
  color: #1e293b;
  background: rgba(255, 255, 255, 0.92);
  resize: vertical;
  box-shadow: inset 0 1px 2px rgba(15, 23, 42, 0.04);
}

.inline-md-editor:focus {
  outline: none;
  border-color: rgba(59, 130, 246, 0.55);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
}

.edit-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
  margin-top: 0.85rem;
}

.save-status {
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 600;
}

.btn-text-link {
  padding: 0;
  border: none;
  background: none;
  font-size: 0.8125rem;
  font-weight: 700;
  color: #2563eb;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}
.btn-text-link:hover {
  color: #1d4ed8;
}

.preview-img {
  display: block;
  width: 100%;
  height: auto;
  max-width: 100%;
  padding: clamp(12px, 2.5vw, 22px);
  box-sizing: border-box;
  background: #fafbfc;
}

.preview-placeholder {
  width: 100%;
  min-height: 220px;
  padding: 2rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  background: linear-gradient(180deg, #f8fafc, #f1f5f9);
}

.ph-icon {
  font-size: 3.5rem;
  margin-bottom: 0.5rem;
  opacity: 0.7;
}

.preview-placeholder p {
  margin: 0;
  font-weight: 700;
  font-size: 1rem;
}

.footer-note {
  width: 100%;
  text-align: center;
  font-size: 0.8125rem;
  color: #94a3b8;
  margin: 0;
}

/* 加载 / 空状态 */
.state-block {
  width: 100%;
  border-radius: 22px;
  padding: 3rem 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.state-loading .loading-text {
  margin: 1rem 0 0;
  color: #64748b;
  font-weight: 600;
  font-size: 0.95rem;
}

.spinner {
  width: 2.5rem;
  height: 2.5rem;
  border: 3px solid rgba(59, 130, 246, 0.2);
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.85s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.state-empty {
  color: #64748b;
  font-weight: 600;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 0.75rem;
  opacity: 0.85;
}

.state-empty p {
  margin: 0;
}
</style>

<style>
@media print {
  .no-print {
    display: none !important;
  }
  .template-detail-for-print {
    background: #fff !important;
    padding: 0 !important;
    min-height: 0 !important;
  }
  .template-detail-for-print .preview-section,
  .template-detail-for-print .preview-glass-pedestal,
  .template-detail-for-print .preview-stage,
  .template-detail-for-print .preview-card-column,
  .template-detail-for-print .preview-card,
  .template-detail-for-print .preview-card-inner {
    max-width: none !important;
    width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
    border: none !important;
    box-shadow: none !important;
    background: transparent !important;
  }
  #template-resume-paper {
    box-shadow: none !important;
    break-inside: auto;
  }
  @page {
    size: A4;
    margin: 12mm;
  }
}
</style>
