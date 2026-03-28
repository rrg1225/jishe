<template>
  <div ref="paperRootRef" class="resume-paper-canvas" :class="{ 'resume-paper-canvas--editable': editable }">
    <input
      v-if="editable"
      ref="avatarFileRef"
      type="file"
      class="rp-hidden-file"
      accept="image/*"
      @change="onAvatarFileChange"
    />
    <div id="template-resume-paper" class="a4-page" :class="[theme, layout]">
      <div v-if="layout === 'layout-classic'" class="resume-dual-column">
        <div class="left-sidebar">
          <div
            class="avatar-uploader"
            :class="{ 'rp-avatar-click': editable }"
            :title="editable ? '点击上传头像' : ''"
            @click="editable && triggerAvatarPick()"
          >
            <img v-if="avatarUrl" :src="avatarUrl" alt="" class="avatar">
            <div v-else class="loading-avatar"><span>+ 照片</span></div>
          </div>
          <div v-if="!editable" class="name">{{ userName }}</div>
          <div v-else class="name rp-editable" contenteditable="true" @blur="onPaperBlur">{{ userName }}</div>
          <div v-if="!editable" class="position">{{ targetJob }}</div>
          <div v-else class="position rp-editable" contenteditable="true" @blur="onPaperBlur">{{ targetJob }}</div>
          <div class="info-section">
            <h3>基本信息</h3>
            <ul>
              <li v-if="!editable">🎓 {{ eduStatus }}</li>
              <li v-else class="rp-editable" contenteditable="true" @blur="onPaperBlur">🎓 {{ eduStatus }}</li>
              <li v-if="!editable">📞 {{ phone }}</li>
              <li v-else class="rp-editable" contenteditable="true" @blur="onPaperBlur">📞 {{ phone }}</li>
              <li v-if="!editable">✉️ {{ email }}</li>
              <li v-else class="rp-editable" contenteditable="true" @blur="onPaperBlur">✉️ {{ email }}</li>
              <li v-if="!editable">📍 {{ intendedCity }}</li>
              <li v-else class="rp-editable" contenteditable="true" @blur="onPaperBlur">📍 {{ intendedCity }}</li>
            </ul>
          </div>
        </div>
        <div
          v-if="!editable"
          class="main-content markdown-body"
          v-html="renderedHtml"
        />
        <div
          v-else
          ref="mainBodyRef"
          class="main-content markdown-body rp-editable"
          contenteditable="true"
          @blur="onPaperBlur"
        />
      </div>

      <div v-else-if="layout === 'layout-top'" class="resume-top-down">
        <div class="top-header">
          <div class="header-content">
            <div v-if="!editable" class="name">{{ userName }}</div>
            <div v-else class="name rp-editable" contenteditable="true" @blur="onPaperBlur">{{ userName }}</div>
            <div v-if="!editable" class="position">{{ targetJob }}</div>
            <div v-else class="position rp-editable" contenteditable="true" @blur="onPaperBlur">{{ targetJob }}</div>
            <div class="contact-row">
              <template v-if="!editable">
                <span>🎓 {{ eduStatus }}</span> |
                <span>📞 {{ phone }}</span> |
                <span>✉️ {{ email }}</span>
              </template>
              <template v-else>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">🎓 {{ eduStatus }}</span>
                <span aria-hidden="true"> | </span>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">📞 {{ phone }}</span>
                <span aria-hidden="true"> | </span>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">✉️ {{ email }}</span>
              </template>
            </div>
          </div>
          <div class="avatar-uploader" :class="{ 'rp-avatar-click': editable }" @click="editable && triggerAvatarPick()">
            <img v-if="avatarUrl" :src="avatarUrl" alt="" class="avatar">
            <div v-else class="loading-avatar"><span>+ 照片</span></div>
          </div>
        </div>
        <div v-if="!editable" class="main-content markdown-body" v-html="renderedHtml" />
        <div v-else ref="mainBodyRef" class="main-content markdown-body rp-editable" contenteditable="true" @blur="onPaperBlur" />
      </div>

      <div v-else-if="layout === 'layout-minimal'" class="resume-minimal">
        <div class="color-ribbon"></div>
        <div class="minimal-wrapper">
          <div class="minimal-header">
            <div class="avatar-uploader" :class="{ 'rp-avatar-click': editable }" @click="editable && triggerAvatarPick()">
              <img v-if="avatarUrl" :src="avatarUrl" alt="" class="avatar">
              <div v-else class="loading-avatar"><span>+ 照片</span></div>
            </div>
            <div class="header-text">
              <div v-if="!editable" class="name">{{ userName }}</div>
              <div v-else class="name rp-editable" contenteditable="true" @blur="onPaperBlur">{{ userName }}</div>
              <div v-if="!editable" class="position">{{ targetJob }}</div>
              <div v-else class="position rp-editable" contenteditable="true" @blur="onPaperBlur">{{ targetJob }}</div>
              <div class="contact-grid">
                <template v-if="!editable">
                  <span>📞 {{ phone }}</span>
                  <span>✉️ {{ email }}</span>
                  <span>🎓 {{ eduStatus }}</span>
                  <span>📍 {{ intendedCity }}</span>
                </template>
                <template v-else>
                  <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">📞 {{ phone }}</span>
                  <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">✉️ {{ email }}</span>
                  <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">🎓 {{ eduStatus }}</span>
                  <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">📍 {{ intendedCity }}</span>
                </template>
              </div>
            </div>
          </div>
          <div v-if="!editable" class="main-content markdown-body" v-html="renderedHtml" />
          <div v-else ref="mainBodyRef" class="main-content markdown-body rp-editable" contenteditable="true" @blur="onPaperBlur" />
        </div>
      </div>

      <div v-else-if="layout === 'layout-timeline'" class="resume-timeline">
        <div class="timeline-accent"></div>
        <div class="timeline-inner">
          <div class="timeline-head">
            <div class="avatar-uploader sm" :class="{ 'rp-avatar-click': editable }" @click="editable && triggerAvatarPick()">
              <img v-if="avatarUrl" :src="avatarUrl" alt="" class="avatar">
              <div v-else class="loading-avatar"><span>+</span></div>
            </div>
            <div>
              <div v-if="!editable" class="name">{{ userName }}</div>
              <div v-else class="name rp-editable" contenteditable="true" @blur="onPaperBlur">{{ userName }}</div>
              <div v-if="!editable" class="position">{{ targetJob }}</div>
              <div v-else class="position rp-editable" contenteditable="true" @blur="onPaperBlur">{{ targetJob }}</div>
              <div class="contact-inline">
                <template v-if="!editable">
                  <span>📞 {{ phone }}</span>
                  <span>✉️ {{ email }}</span>
                  <span>🎓 {{ eduStatus }}</span>
                </template>
                <template v-else>
                  <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">📞 {{ phone }}</span>
                  <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">✉️ {{ email }}</span>
                  <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">🎓 {{ eduStatus }}</span>
                </template>
              </div>
            </div>
          </div>
          <div v-if="!editable" class="main-content markdown-body timeline-md" v-html="renderedHtml" />
          <div v-else ref="mainBodyRef" class="main-content markdown-body timeline-md rp-editable" contenteditable="true" @blur="onPaperBlur" />
        </div>
      </div>

      <div v-else-if="layout === 'layout-banner'" class="resume-banner">
        <div class="banner-hero">
          <div class="banner-hero-text">
            <div v-if="!editable" class="name">{{ userName }}</div>
            <div v-else class="name rp-editable" contenteditable="true" @blur="onPaperBlur">{{ userName }}</div>
            <div v-if="!editable" class="position">{{ targetJob }}</div>
            <div v-else class="position rp-editable" contenteditable="true" @blur="onPaperBlur">{{ targetJob }}</div>
            <div class="banner-contact">
              <template v-if="!editable">
                <span>📞 {{ phone }}</span>
                <span>✉️ {{ email }}</span>
                <span>🎓 {{ eduStatus }}</span>
              </template>
              <template v-else>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">📞 {{ phone }}</span>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">✉️ {{ email }}</span>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">🎓 {{ eduStatus }}</span>
              </template>
            </div>
          </div>
          <div class="avatar-uploader" :class="{ 'rp-avatar-click': editable }" @click="editable && triggerAvatarPick()">
            <img v-if="avatarUrl" :src="avatarUrl" alt="" class="avatar">
            <div v-else class="loading-avatar"><span>+ 照片</span></div>
          </div>
        </div>
        <div class="banner-body">
          <div v-if="!editable" class="main-content markdown-body" v-html="renderedHtml" />
          <div v-else ref="mainBodyRef" class="main-content markdown-body rp-editable" contenteditable="true" @blur="onPaperBlur" />
        </div>
      </div>

      <div v-else-if="layout === 'layout-magazine'" class="resume-magazine">
        <div class="mag-topline"></div>
        <div class="mag-head">
          <div class="mag-title-block">
            <div v-if="!editable" class="name">{{ userName }}</div>
            <div v-else class="name rp-editable" contenteditable="true" @blur="onPaperBlur">{{ userName }}</div>
            <div v-if="!editable" class="position">{{ targetJob }}</div>
            <div v-else class="position rp-editable" contenteditable="true" @blur="onPaperBlur">{{ targetJob }}</div>
          </div>
          <div class="avatar-uploader sm" :class="{ 'rp-avatar-click': editable }" @click="editable && triggerAvatarPick()">
            <img v-if="avatarUrl" :src="avatarUrl" alt="" class="avatar">
            <div v-else class="loading-avatar"><span>+</span></div>
          </div>
        </div>
        <div class="mag-meta">
          <template v-if="!editable">
            <span>📞 {{ phone }}</span>
            <span>✉️ {{ email }}</span>
            <span>📍 {{ intendedCity }}</span>
          </template>
          <template v-else>
            <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">📞 {{ phone }}</span>
            <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">✉️ {{ email }}</span>
            <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">📍 {{ intendedCity }}</span>
          </template>
        </div>
        <div v-if="!editable" class="main-content markdown-body mag-md" v-html="renderedHtml" />
        <div v-else ref="mainBodyRef" class="main-content markdown-body mag-md rp-editable" contenteditable="true" @blur="onPaperBlur" />
      </div>

      <div v-else-if="layout === 'layout-card'" class="resume-cardlayout">
        <div class="card-top">
          <div class="avatar-uploader sm" :class="{ 'rp-avatar-click': editable }" @click="editable && triggerAvatarPick()">
            <img v-if="avatarUrl" :src="avatarUrl" alt="" class="avatar">
            <div v-else class="loading-avatar"><span>+</span></div>
          </div>
          <div class="card-top-text">
            <div v-if="!editable" class="name">{{ userName }}</div>
            <div v-else class="name rp-editable" contenteditable="true" @blur="onPaperBlur">{{ userName }}</div>
            <div v-if="!editable" class="position">{{ targetJob }}</div>
            <div v-else class="position rp-editable" contenteditable="true" @blur="onPaperBlur">{{ targetJob }}</div>
            <div class="card-contact">
              <template v-if="!editable">
                <span>{{ phone }}</span>
                <span>{{ email }}</span>
              </template>
              <template v-else>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">{{ phone }}</span>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">{{ email }}</span>
              </template>
            </div>
          </div>
        </div>
        <div class="card-sheet">
          <div v-if="!editable" class="main-content markdown-body card-md" v-html="renderedHtml" />
          <div v-else ref="mainBodyRef" class="main-content markdown-body card-md rp-editable" contenteditable="true" @blur="onPaperBlur" />
        </div>
      </div>

      <div v-else-if="layout === 'layout-elegant'" class="resume-elegant">
        <div class="elegant-frame">
          <div class="elegant-header">
            <div class="elegant-line"></div>
            <div v-if="!editable" class="name">{{ userName }}</div>
            <div v-else class="name rp-editable" contenteditable="true" @blur="onPaperBlur">{{ userName }}</div>
            <div v-if="!editable" class="position">{{ targetJob }}</div>
            <div v-else class="position rp-editable" contenteditable="true" @blur="onPaperBlur">{{ targetJob }}</div>
            <div class="elegant-contact">
              <template v-if="!editable">
                <span>{{ phone }}</span>
                <span class="dot-sep">·</span>
                <span>{{ email }}</span>
                <span class="dot-sep">·</span>
                <span>{{ eduStatus }}</span>
              </template>
              <template v-else>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">{{ phone }}</span>
                <span class="dot-sep">·</span>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">{{ email }}</span>
                <span class="dot-sep">·</span>
                <span class="rp-editable" contenteditable="true" @blur="onPaperBlur">{{ eduStatus }}</span>
              </template>
            </div>
          </div>
          <div class="avatar-uploader elegant-av" :class="{ 'rp-avatar-click': editable }" @click="editable && triggerAvatarPick()">
            <img v-if="avatarUrl" :src="avatarUrl" alt="" class="avatar">
            <div v-else class="loading-avatar"><span>+</span></div>
          </div>
          <div v-if="!editable" class="main-content markdown-body elegant-md" v-html="renderedHtml" />
          <div v-else ref="mainBodyRef" class="main-content markdown-body elegant-md rp-editable" contenteditable="true" @blur="onPaperBlur" />
        </div>
      </div>

      <div v-else class="resume-dual-column layout-fallback">
        <div class="left-sidebar">
          <div class="avatar-uploader" :class="{ 'rp-avatar-click': editable }" @click="editable && triggerAvatarPick()">
            <img v-if="avatarUrl" :src="avatarUrl" alt="" class="avatar">
            <div v-else class="loading-avatar"><span>+ 照片</span></div>
          </div>
          <div v-if="!editable" class="name">{{ userName }}</div>
          <div v-else class="name rp-editable" contenteditable="true" @blur="onPaperBlur">{{ userName }}</div>
          <div v-if="!editable" class="position">{{ targetJob }}</div>
          <div v-else class="position rp-editable" contenteditable="true" @blur="onPaperBlur">{{ targetJob }}</div>
          <div class="info-section">
            <h3>基本信息</h3>
            <ul>
              <template v-if="!editable">
                <li>🎓 {{ eduStatus }}</li>
                <li>📞 {{ phone }}</li>
                <li>✉️ {{ email }}</li>
              </template>
              <template v-else>
                <li class="rp-editable" contenteditable="true" @blur="onPaperBlur">🎓 {{ eduStatus }}</li>
                <li class="rp-editable" contenteditable="true" @blur="onPaperBlur">📞 {{ phone }}</li>
                <li class="rp-editable" contenteditable="true" @blur="onPaperBlur">✉️ {{ email }}</li>
              </template>
            </ul>
          </div>
        </div>
        <div v-if="!editable" class="main-content markdown-body" v-html="renderedHtml" />
        <div v-else ref="mainBodyRef" class="main-content markdown-body rp-editable" contenteditable="true" @blur="onPaperBlur" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { marked } from 'marked'
import TurndownService from 'turndown'

const markdown = defineModel({ type: String, default: '' })

const props = defineProps({
  layout: { type: String, default: 'layout-classic' },
  theme: { type: String, default: 'theme-dark' },
  userName: { type: String, default: '姓名' },
  targetJob: { type: String, default: '求职意向' },
  phone: { type: String, default: '138-xxxx-xxxx' },
  email: { type: String, default: 'your@email.com' },
  eduStatus: { type: String, default: '2028级毕业生' },
  intendedCity: { type: String, default: '全国' },
  avatarUrl: { type: String, default: '' },
  editable: { type: Boolean, default: false }
})

const emit = defineEmits(['contact-change', 'avatar-file'])

const paperRootRef = ref(null)
const mainBodyRef = ref(null)
const avatarFileRef = ref(null)

const turndown = new TurndownService({
  headingStyle: 'atx',
  codeBlockStyle: 'fenced',
  bulletListMarker: '-'
})

const renderedHtml = computed(() => marked(markdown.value || ''))

function stripLeadingH1(md) {
  return (md || '').replace(/^#\s+[^\n]+\n*/m, '').trim()
}

function parseLabeledLine(text, emoji) {
  let t = (text || '').replace(/\s+/g, ' ').trim()
  if (t.startsWith(emoji)) t = t.slice(emoji.length).trim()
  return t
}

function collectContactFromDom(root) {
  const out = {}
  const sec = root.querySelector('.info-section')
  if (sec) {
    const lis = sec.querySelectorAll('li')
    if (lis[0]) out.eduStatus = parseLabeledLine(lis[0].innerText, '🎓')
    if (lis[1]) out.phone = parseLabeledLine(lis[1].innerText, '📞')
    if (lis[2]) out.email = parseLabeledLine(lis[2].innerText, '✉️')
    if (lis[3]) out.intendedCity = parseLabeledLine(lis[3].innerText, '📍')
    return out
  }
  const row = root.querySelector('.contact-row')
  if (row) {
    const spans = row.querySelectorAll('span.rp-editable')
    const parts = [...spans].map((s) => s.innerText.trim()).filter(Boolean)
    if (parts[0]) out.eduStatus = parseLabeledLine(parts[0], '🎓')
    if (parts[1]) out.phone = parseLabeledLine(parts[1], '📞')
    if (parts[2]) out.email = parseLabeledLine(parts[2], '✉️')
    return out
  }
  const grid = root.querySelector('.contact-grid')
  if (grid) {
    const spans = grid.querySelectorAll('span')
    if (spans[0]) out.phone = parseLabeledLine(spans[0].innerText, '📞')
    if (spans[1]) out.email = parseLabeledLine(spans[1].innerText, '✉️')
    if (spans[2]) out.eduStatus = parseLabeledLine(spans[2].innerText, '🎓')
    if (spans[3]) out.intendedCity = parseLabeledLine(spans[3].innerText, '📍')
    return out
  }
  const inline = root.querySelector('.contact-inline')
  if (inline) {
    const spans = inline.querySelectorAll('span')
    if (spans[0]) out.phone = parseLabeledLine(spans[0].innerText, '📞')
    if (spans[1]) out.email = parseLabeledLine(spans[1].innerText, '✉️')
    if (spans[2]) out.eduStatus = parseLabeledLine(spans[2].innerText, '🎓')
    return out
  }
  const ban = root.querySelector('.banner-contact')
  if (ban) {
    const spans = ban.querySelectorAll('span')
    if (spans[0]) out.phone = parseLabeledLine(spans[0].innerText, '📞')
    if (spans[1]) out.email = parseLabeledLine(spans[1].innerText, '✉️')
    if (spans[2]) out.eduStatus = parseLabeledLine(spans[2].innerText, '🎓')
    return out
  }
  const mag = root.querySelector('.mag-meta')
  if (mag) {
    const spans = mag.querySelectorAll('span')
    if (spans[0]) out.phone = parseLabeledLine(spans[0].innerText, '📞')
    if (spans[1]) out.email = parseLabeledLine(spans[1].innerText, '✉️')
    if (spans[2]) out.intendedCity = parseLabeledLine(spans[2].innerText, '📍')
    return out
  }
  const card = root.querySelector('.card-contact')
  if (card) {
    const spans = card.querySelectorAll('span')
    if (spans[0]) out.phone = spans[0].innerText.trim()
    if (spans[1]) out.email = spans[1].innerText.trim()
    return out
  }
  const elg = root.querySelector('.elegant-contact')
  if (elg) {
    const eds = elg.querySelectorAll('span.rp-editable')
    if (eds[0]) out.phone = eds[0].innerText.trim()
    if (eds[1]) out.email = eds[1].innerText.trim()
    if (eds[2]) out.eduStatus = eds[2].innerText.trim()
    return out
  }
  return out
}

function collectMarkdownFromDom() {
  const root = paperRootRef.value
  if (!root) return markdown.value
  const nameEl = root.querySelector('.name')
  const jobEl = root.querySelector('.position')
  const mainEl = root.querySelector('.main-content.markdown-body')
  const name = nameEl?.innerText?.trim() || props.userName
  const job = jobEl?.innerText?.trim() || props.targetJob
  let bodyMd = ''
  if (mainEl) {
    bodyMd = turndown.turndown(mainEl.innerHTML).trim()
    bodyMd = stripLeadingH1(bodyMd)
  }
  const head = `# ${name} - ${job}`
  return bodyMd ? `${head}\n\n${bodyMd}` : head
}

let skipMarkdownWatch = false

function onPaperBlur() {
  if (!props.editable) return
  nextTick(() => {
    const root = paperRootRef.value
    if (!root) return
    const contact = collectContactFromDom(root)
    if (Object.keys(contact).length) emit('contact-change', contact)
    const nextMd = collectMarkdownFromDom()
    if (nextMd !== markdown.value) {
      skipMarkdownWatch = true
      markdown.value = nextMd
    }
  })
}

watch(
  () => [markdown.value, props.editable, props.layout],
  async () => {
    if (!props.editable) return
    if (skipMarkdownWatch) {
      skipMarkdownWatch = false
      return
    }
    await nextTick()
    const main = mainBodyRef.value
    if (!main) return
    if (main.contains(document.activeElement)) return
    main.innerHTML = marked(markdown.value || '')
  },
  { immediate: true }
)

function triggerAvatarPick() {
  avatarFileRef.value?.click()
}

function onAvatarFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  emit('avatar-file', file)
  e.target.value = ''
}
</script>

<style scoped>
.avatar-uploader {
  flex-shrink: 0;
  cursor: default;
}
.loading-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  color: inherit;
  font-size: 12px;
  border: 2px dashed rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  width: 100px;
  height: 100px;
}
.avatar-uploader.sm .loading-avatar,
.avatar-uploader.sm .avatar {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  font-size: 11px;
}
.theme-light .top-header {
  color: #1e293b;
}
.theme-light .top-header .loading-avatar {
  border-color: #94a3b8;
  color: #64748b;
  background: #f8fafc;
}
.theme-light .info-section h3 {
  border-bottom-color: rgba(15, 23, 42, 0.12);
}

.a4-page {
  width: 100%;
  min-height: 297mm;
  height: auto;
  background: white;
  overflow: visible;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
}

.theme-dark {
  --sidebar-bg: #4f525a;
  --sidebar-text: #ffffff;
  --main-title: #284c71;
}
.theme-blue {
  --sidebar-bg: #1e3a8a;
  --sidebar-text: #ffffff;
  --main-title: #1e3a8a;
}
.theme-light {
  --sidebar-bg: #f1f5f9;
  --sidebar-text: #1e293b;
  --main-title: #0f172a;
}

.a4-page.layout-classic {
  background-image: linear-gradient(to right, var(--sidebar-bg) 250px, #fff 250px);
}
.resume-dual-column {
  display: flex;
  min-height: 297mm;
  align-items: stretch;
}
.layout-fallback .left-sidebar {
  background: var(--sidebar-bg);
  color: var(--sidebar-text);
}
.left-sidebar {
  width: 250px;
  padding: 30mm 15mm;
  color: var(--sidebar-text);
  text-align: center;
}
.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.5);
  object-fit: cover;
}
.name {
  font-size: 24px;
  font-weight: bold;
  margin-top: 15px;
}
.position {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 5px;
}
.info-section {
  text-align: left;
  margin-top: 30px;
}
.info-section h3 {
  font-size: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  padding-bottom: 5px;
  margin-bottom: 15px;
}
.info-section li {
  font-size: 13px;
  margin-bottom: 8px;
  list-style: none;
}

.resume-top-down {
  display: flex;
  flex-direction: column;
  min-height: 297mm;
}
.top-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25mm 20mm;
  background: var(--sidebar-bg);
  color: var(--sidebar-text);
}
.header-content .name {
  font-size: 36px;
  margin-bottom: 5px;
  text-align: left;
}
.header-content .position {
  font-size: 18px;
  text-align: left;
}
.contact-row {
  display: flex;
  gap: 10px;
  font-size: 13px;
  margin-top: 12px;
}

.resume-minimal {
  display: flex;
  min-height: 297mm;
  position: relative;
}
.color-ribbon {
  width: 15px;
  background: var(--main-title);
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
}
.minimal-wrapper {
  padding: 25mm 20mm 25mm 34mm;
  flex: 1;
}
.minimal-header {
  display: flex;
  gap: 20px;
  border-bottom: 2px solid #eee;
  padding-bottom: 20px;
  align-items: center;
}
.minimal-header .avatar {
  width: 80px;
  height: 80px;
  border-radius: 8px;
}
.contact-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  font-size: 13px;
  margin-top: 10px;
  text-align: left;
}

.resume-timeline {
  display: flex;
  min-height: 297mm;
  position: relative;
  background: #fafafa;
}
.timeline-accent {
  width: 6px;
  background: linear-gradient(180deg, var(--main-title), var(--sidebar-bg));
  flex-shrink: 0;
}
.timeline-inner {
  flex: 1;
  padding: 22mm 18mm 22mm 14mm;
}
.timeline-head {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}
.timeline-head .name {
  margin-top: 0;
  font-size: 26px;
  color: #0f172a;
}
.timeline-head .position {
  color: var(--main-title);
  opacity: 1;
  font-weight: 600;
}
.contact-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #64748b;
  margin-top: 8px;
}
.timeline-md :deep(h2) {
  position: relative;
  padding-left: 14px;
  border: none;
}
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

.resume-banner {
  display: flex;
  flex-direction: column;
  min-height: 297mm;
}
.banner-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18mm 20mm;
  background: linear-gradient(135deg, var(--sidebar-bg) 0%, #334155 100%);
  color: #fff;
}
.theme-light .banner-hero {
  background: linear-gradient(135deg, #e2e8f0, #f8fafc);
  color: #1e293b;
}
.banner-hero .name {
  margin-top: 0;
  font-size: 32px;
}
.banner-hero .position {
  opacity: 0.95;
  font-size: 16px;
}
.banner-contact {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  font-size: 13px;
  margin-top: 12px;
  opacity: 0.9;
}
.banner-body {
  flex: 1;
  background: #fff;
}
.banner-body .main-content {
  padding: 12mm 20mm 20mm;
}

.resume-magazine {
  min-height: 297mm;
  padding: 18mm 16mm 22mm;
  background: #fff;
}
.mag-topline {
  height: 4px;
  background: var(--main-title);
  margin-bottom: 14px;
}
.mag-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  border-bottom: 3px double #e2e8f0;
  padding-bottom: 12px;
}
.mag-title-block .name {
  margin-top: 0;
  font-size: 34px;
  letter-spacing: -0.02em;
  color: #0f172a;
}
.mag-title-block .position {
  font-size: 15px;
  color: var(--main-title);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.mag-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12px;
  color: #64748b;
  margin: 14px 0 18px;
}
.mag-md {
  column-count: 2;
  column-gap: 28px;
  font-size: 13px;
  line-height: 1.75;
}
.mag-md :deep(h2) {
  break-after: avoid;
  column-span: all;
  margin-top: 20px;
}
.mag-md :deep(ul) {
  break-inside: avoid;
}

.resume-cardlayout {
  min-height: 297mm;
  padding: 16mm;
  background: linear-gradient(180deg, #f1f5f9 0%, #e2e8f0 100%);
}
.card-top {
  display: flex;
  gap: 16px;
  align-items: center;
  background: #fff;
  padding: 14mm 16mm;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.08);
}
.card-top-text .name {
  margin-top: 0;
  font-size: 28px;
  color: #0f172a;
}
.card-top-text .position {
  color: var(--main-title);
  font-weight: 600;
}
.card-contact {
  font-size: 13px;
  color: #64748b;
  margin-top: 8px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.card-sheet {
  margin-top: 10mm;
  background: #fff;
  padding: 12mm 16mm 18mm;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.06);
}
.card-md :deep(h2) {
  font-size: 15px;
  background: linear-gradient(90deg, var(--main-title), transparent);
  color: #fff;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  margin-top: 22px;
}

.resume-elegant {
  min-height: 297mm;
  padding: 18mm 14mm;
  background: #fdfcfa;
}
.elegant-frame {
  max-width: 170mm;
  margin: 0 auto;
  border: 1px solid #e7e5e4;
  padding: 16mm 18mm;
  background: #fff;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.04);
}
.elegant-line {
  width: 48px;
  height: 3px;
  background: var(--main-title);
  margin-bottom: 12px;
}
.elegant-header .name {
  margin-top: 0;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 30px;
  font-weight: 400;
  color: #1c1917;
}
.elegant-header .position {
  font-size: 14px;
  color: #78716c;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-top: 6px;
}
.elegant-contact {
  font-size: 13px;
  color: #57534e;
  margin-top: 14px;
}
.dot-sep {
  margin: 0 6px;
  color: #d6d3d1;
}
.elegant-av {
  float: right;
  margin: 0 0 8px 16px;
}
.elegant-av .avatar,
.elegant-av .loading-avatar {
  width: 88px;
  height: 88px;
  border-radius: 4px;
}
.elegant-md {
  clear: both;
  font-family: Georgia, 'Songti SC', serif;
  font-size: 13px;
  line-height: 1.85;
  color: #44403c;
}
.elegant-md :deep(h2) {
  font-family: 'PingFang SC', sans-serif;
  font-size: 15px;
  border-bottom: 1px solid #d6d3d1;
  color: #1c1917;
}

.main-content {
  flex: 1;
  padding: 30mm 20mm;
  text-align: left;
  min-height: 0;
}
.top-header + .main-content,
.resume-top-down > .main-content {
  padding-top: 14mm;
}
.minimal-wrapper .main-content {
  padding-top: 8mm;
}
.markdown-body {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
}
.markdown-body :deep(h2) {
  color: var(--main-title);
  border-bottom: 2px solid var(--main-title);
  margin-top: 25px;
  padding-bottom: 5px;
  font-size: 18px;
}
.markdown-body :deep(ul) {
  padding-left: 20px;
  margin-bottom: 10px;
}
.markdown-body :deep(li) {
  margin-bottom: 6px;
}
.markdown-body :deep(p) {
  margin: 0.5em 0;
}
.markdown-body :deep(strong) {
  color: #1e293b;
}

.rp-hidden-file {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}

.rp-avatar-click {
  cursor: pointer;
}

.resume-paper-canvas--editable .rp-editable {
  outline: 1px dashed rgba(59, 130, 246, 0.35);
  outline-offset: 2px;
  border-radius: 4px;
}

.resume-paper-canvas--editable .main-content.rp-editable:focus,
.resume-paper-canvas--editable .rp-editable:focus {
  outline: 2px solid rgba(59, 130, 246, 0.65);
}
</style>
