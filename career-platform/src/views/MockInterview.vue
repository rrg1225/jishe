<template>
  <div class="mi-page">
    <header class="mi-header">
      <el-button type="primary" link @click="goBack">
        <span class="mi-back">← 返回工作台</span>
      </el-button>
      <div class="mi-header-title">
        <h1>AI 模拟面试</h1>
        <p class="mi-sub">选择方向 → 多轮问答（每轮一题 + 可选追问）→ 单轮点评与完成度 → 结束生成总评</p>
      </div>
      <div class="mi-header-spacer" />
    </header>

    <el-steps :active="stepActive" align-center finish-status="success" class="mi-steps">
      <el-step title="选择方向" description="决定面试题库风格" />
      <el-step title="答题互动" description="语音或文字作答" />
      <el-step title="评估报告" description="维度得分与建议" />
    </el-steps>

    <!-- 1. 选题 -->
    <el-card v-if="phase === 'select'" class="mi-card" shadow="hover">
      <template #header>
        <div class="mi-card-head">
          <span>应聘方向</span>
          <el-tag type="info" size="small">必选</el-tag>
        </div>
      </template>
      <p class="mi-hint">
        请选择与目标岗位最接近的一项。开始后会有<strong>第一道题</strong>；你每答一轮，面试官会结合历史对话给出<strong>点评与下一道追问或新题</strong>，可多轮进行，直到你点击「结束并生成报告」。
      </p>
      <div class="mi-careers">
        <button
          v-for="c in careers"
          :key="c.id"
          type="button"
          :class="['mi-career-btn', { 'is-active': selectedCareer?.id === c.id }]"
          @click="selectedCareer = c"
        >
          <span class="mi-career-emoji">{{ c.emoji }}</span>
          <span class="mi-career-label">{{ c.label }}</span>
          <span class="mi-career-desc">{{ c.desc }}</span>
        </button>
      </div>
      <div class="mi-row mi-tools">
        <div class="mi-switch-wrap">
          <el-switch v-model="useCamera" :disabled="cameraLoading" active-text="开启摄像头（多模态：表情辅助评估）" />
          <span v-if="cameraLoading" class="mi-loading-text">视觉模型加载中…</span>
        </div>
      </div>
      <div class="mi-actions">
        <el-button type="primary" size="large" round :disabled="!selectedCareer" :loading="isThinking" @click="onClickStart">
          开始面试
        </el-button>
      </div>
    </el-card>

    <!-- 2. 面试中 -->
    <div v-else-if="phase === 'live'" class="mi-live">
      <el-row :gutter="20">
        <el-col :xs="24" :md="useCamera ? 15 : 24">
          <el-card class="mi-card mi-chat-card" shadow="hover">
            <template #header>
              <div class="mi-card-head">
                <span>对话区</span>
                <div class="mi-badges">
                  <el-tag v-if="selectedCareer" type="primary" size="small">{{ selectedCareer.label }}</el-tag>
                  <el-tag v-if="sessionId" size="small">第 {{ currentRound }} 轮</el-tag>
                </div>
              </div>
            </template>
            <el-scrollbar max-height="360px" class="mi-scroll">
              <div v-if="!chatHistory.length" class="mi-empty-chat">
                <el-empty description="等待首题加载…" />
              </div>
              <div
                v-for="(msg, index) in chatHistory"
                :key="index"
                :class="['mi-bubble-row', msg.role === 'ai' ? 'is-ai' : 'is-user']"
              >
                <div :class="['mi-bubble', msg.role === 'ai' ? 'mi-bubble-ai' : 'mi-bubble-user']">
                  <span class="mi-bubble-role">{{ msg.role === 'ai' ? '面试官' : '我' }}</span>
                  <div class="mi-bubble-text">{{ msg.content }}</div>
                </div>
              </div>
            </el-scrollbar>
            <div class="mi-compose">
              <el-input
                v-model="userInput"
                type="textarea"
                :rows="3"
                placeholder="输入回答，或点击下方使用语音输入…"
                maxlength="2000"
                show-word-limit
                :disabled="isThinking"
              />
              <div class="mi-compose-actions">
                <el-button :disabled="isThinking" @click="startVoiceInput">🎙 语音输入</el-button>
                <el-button type="primary" :loading="isThinking" :disabled="!userInput.trim()" @click="submitAnswer">
                  提交本轮回答
                </el-button>
                <el-button type="danger" plain :disabled="isThinking" @click="finishInterview">结束并生成报告</el-button>
              </div>
            </div>
          </el-card>

          <el-card v-if="lastFeedback" class="mi-card mi-round-report" shadow="hover">
            <template #header>
              <span>上一轮 · AI 点评</span>
            </template>
            <div class="mi-round-grid">
              <div class="mi-round-item">
                <span class="mi-k">是否切题</span>
                <el-tag
                  :type="correctnessTagType(lastFeedback.correctness)"
                  effect="dark"
                  size="large"
                  class="mi-tag-lg"
                >
                  {{ lastFeedback.correctness }}
                </el-tag>
              </div>
              <div class="mi-round-item mi-round-wide">
                <span class="mi-k">完成度</span>
                <el-progress :percentage="lastFeedback.completenessScore" :color="progressColors" />
              </div>
              <div class="mi-round-full">
                <span class="mi-k">简要点评</span>
                <p class="mi-feedback-p">{{ lastFeedback.feedback }}</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col v-if="useCamera" :xs="24" :md="9">
          <el-card class="mi-card mi-video-card" shadow="hover">
            <template #header>
              <span>多模态 · 当前状态</span>
            </template>
            <div class="mi-video-wrap">
              <video ref="videoEl" autoplay muted playsinline class="mi-video" />
              <canvas ref="canvasEl" class="mi-canvas" />
            </div>
            <el-alert :title="'当前情绪倾向：' + currentEmotion" type="info" :closable="false" show-icon class="mi-alert" />
            <p class="mi-video-note">表情仅作辅助参考，评分以回答内容为主。</p>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 3. 总评 -->
    <el-card v-else-if="phase === 'report'" class="mi-card mi-report-card" shadow="hover">
      <template #header>
        <div class="mi-card-head">
          <span>📊 面试评估报告</span>
          <el-tag type="success">已完成</el-tag>
        </div>
      </template>
      <div v-if="reportData" class="mi-report-body">
        <el-row :gutter="24">
          <el-col :xs="24" :sm="8">
            <div class="mi-score-block">
              <span class="mi-score-label">逻辑与表达</span>
              <span class="mi-score-num mi-c1">{{ reportData.expressScore ?? '—' }}</span>
              <span class="mi-score-unit">分</span>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="mi-score-block">
              <span class="mi-score-label">专业深度</span>
              <span class="mi-score-num mi-c2">{{ reportData.techScore ?? '—' }}</span>
              <span class="mi-score-unit">分</span>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="mi-score-block">
              <span class="mi-score-label">综合应变</span>
              <span class="mi-score-num mi-c3">{{ reportData.logicScore ?? '—' }}</span>
              <span class="mi-score-unit">分</span>
            </div>
          </el-col>
        </el-row>
        <el-divider />
        <h3 class="mi-h3">综合评价</h3>
        <p class="mi-summary">{{ reportData.summary }}</p>
        <h3 class="mi-h3">改进建议</h3>
        <ul class="mi-sug-list">
          <li v-for="(sug, idx) in reportData.suggestions || []" :key="idx">{{ sug }}</li>
        </ul>
        <div class="mi-actions mi-actions-footer">
          <el-button type="primary" size="large" round @click="resetAll">再面一场</el-button>
          <el-button size="large" round @click="goBack">返回工作台</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import * as faceapi from 'face-api.js'

const router = useRouter()
const route = useRoute()

const careers = [
  { id: 'java', label: 'Java / 后端开发', emoji: '☕', desc: 'Spring、数据库、高并发等', jobTitle: 'Java 后端开发工程师' },
  { id: 'go', label: 'Go / 云原生后端', emoji: '🐹', desc: '微服务、K8s、中间件', jobTitle: 'Go 后端开发工程师' },
  { id: 'fe', label: '前端开发', emoji: '⚡', desc: 'Vue/React、工程化、性能', jobTitle: '前端开发工程师' },
  { id: 'algo', label: '算法 / 机器学习', emoji: '🧠', desc: '数据结构、模型、论文与业务落地', jobTitle: '算法工程师' },
  { id: 'qa', label: '测试 / 质量保障', emoji: '🔍', desc: '用例、自动化、性能与安全测试', jobTitle: '测试开发工程师' },
  { id: 'devops', label: '运维 / DevOps', emoji: '🛠', desc: 'CI/CD、监控、云平台', jobTitle: '运维开发工程师' },
  { id: 'pm', label: '产品经理', emoji: '📋', desc: '需求、协作、数据与迭代', jobTitle: '产品经理' },
  { id: 'data', label: '数据分析', emoji: '📊', desc: 'SQL、指标体系、可视化', jobTitle: '数据分析师' },
  { id: 'ui', label: 'UI / UX 设计', emoji: '🎨', desc: '交互、视觉、可用性与规范', jobTitle: 'UI/UX 设计师' },
  { id: 'ops', label: '运营 / 增长', emoji: '🚀', desc: '增长、活动、用户洞察', jobTitle: '互联网运营' },
  { id: 'mkt', label: '市场营销 / 品牌', emoji: '📣', desc: '品牌、投放、内容与转化', jobTitle: '市场营销专员' },
  { id: 'hr', label: '人力资源 / HR', emoji: '🤝', desc: '招聘、培训、员工关系', jobTitle: '人力资源专员' },
  { id: 'finance', label: '财会 / 审计', emoji: '📒', desc: '核算、税务、内控与报表', jobTitle: '财务会计' },
  { id: 'embedded', label: '嵌入式 / 硬件', emoji: '🔌', desc: '单片机、驱动、电子电路', jobTitle: '嵌入式开发工程师' },
  { id: 'civil', label: '国企 / 事业 / 公考综合', emoji: '🏛', desc: '行测申论、结构化、综合素质', jobTitle: '公职与综合岗位候选人' },
  { id: 'general', label: '通用职能', emoji: '💼', desc: '沟通、职业规划、软实力', jobTitle: '通用岗位候选人' },
]

const phase = ref('select')
const stepActive = computed(() => (phase.value === 'select' ? 0 : phase.value === 'live' ? 1 : 2))

const selectedCareer = ref(null)
const sessionId = ref(null)
const currentRound = ref(0)
const chatHistory = ref([])
const userInput = ref('')
const isThinking = ref(false)
const reportData = ref(null)
const lastFeedback = ref(null)

const useCamera = ref(false)
const cameraLoading = ref(true)
const videoEl = ref(null)
const canvasEl = ref(null)
const currentEmotion = ref('未检测')
let detectInterval = null

const progressColors = [
  { color: '#f56c6c', percentage: 40 },
  { color: '#e6a23c', percentage: 70 },
  { color: '#67c23a', percentage: 100 },
]

function goBack() {
  stopCamera()
  router.push('/dashboard')
}

function correctnessTagType(c) {
  if (!c) return 'info'
  if (c.includes('较好') || c.includes('好')) return 'success'
  if (c.includes('不足') || c.includes('差')) return 'danger'
  return 'warning'
}

/** axios 拦截器已返回 response.data；面试接口为裸 JSON，无 code 包裹 */
function unwrapData(res) {
  if (res && typeof res === 'object' && res.data !== undefined && res.code === 200) {
    return res.data
  }
  return res
}

onMounted(async () => {
  const q = route.query.jobTitle
  if (typeof q === 'string' && q) {
    const found = careers.find((c) => c.jobTitle === q || c.label.includes(q))
    if (found) selectedCareer.value = found
  }
  try {
    await faceapi.nets.tinyFaceDetector.loadFromUri('/models')
    await faceapi.nets.faceExpressionNet.loadFromUri('/models')
  } catch (e) {
    console.warn('face-api 模型加载失败，已关闭摄像头模式', e)
    useCamera.value = false
    ElMessage.warning('视觉模型未就绪，已使用纯文本面试模式。可将 face-api 权重放入 public/models。')
  } finally {
    cameraLoading.value = false
  }
})

watch(useCamera, (on) => {
  if (phase.value !== 'live') return
  if (on) startCameraStream()
  else stopCamera()
})

watch(phase, (p) => {
  if (p === 'live' && useCamera.value) {
    startCameraStream()
  }
  if (p !== 'live') {
    stopCamera()
  }
})

function startCameraStream() {
  if (!useCamera.value || !videoEl.value) return
  navigator.mediaDevices
    .getUserMedia({ video: { facingMode: 'user' }, audio: false })
    .then((stream) => {
      videoEl.value.srcObject = stream
      videoEl.value.play().catch(() => {})
      if (detectInterval) clearInterval(detectInterval)
      detectInterval = setInterval(runFaceDetect, 600)
    })
    .catch(() => {
      ElMessage.error('无法使用摄像头，请检查权限或改用纯文本模式')
      useCamera.value = false
    })
}

function stopCamera() {
  if (detectInterval) {
    clearInterval(detectInterval)
    detectInterval = null
  }
  if (videoEl.value?.srcObject) {
    videoEl.value.srcObject.getTracks().forEach((t) => t.stop())
    videoEl.value.srcObject = null
  }
}

const EMOTION_ZH = {
  neutral: '平静自然',
  happy: '放松自信',
  sad: '略显低落',
  angry: '略显激动',
  fearful: '紧张压力',
  disgusted: '不适',
  surprised: '惊讶',
}

async function runFaceDetect() {
  if (!videoEl.value || !canvasEl.value || videoEl.value.readyState < 2) return
  try {
    const displaySize = { width: videoEl.value.clientWidth, height: videoEl.value.clientHeight }
    if (displaySize.width < 2) return
    faceapi.matchDimensions(canvasEl.value, displaySize)
    const detections = await faceapi
      .detectAllFaces(videoEl.value, new faceapi.TinyFaceDetectorOptions())
      .withFaceExpressions()
    const resized = faceapi.resizeResults(detections, displaySize)
    const ctx = canvasEl.value.getContext('2d')
    ctx.clearRect(0, 0, canvasEl.value.width, canvasEl.value.height)
    faceapi.draw.drawDetections(canvasEl.value, resized)
    if (detections.length > 0) {
      const ex = detections[0].expressions
      const maxKey = Object.keys(ex).reduce((a, b) => (ex[a] > ex[b] ? a : b))
      currentEmotion.value = EMOTION_ZH[maxKey] || maxKey
    } else {
      currentEmotion.value = '未检测到人脸'
    }
  } catch {
    /* 单帧失败忽略 */
  }
}

async function onClickStart() {
  if (!selectedCareer.value) return
  reportData.value = null
  lastFeedback.value = null
  chatHistory.value = []
  sessionId.value = null
  isThinking.value = true
  phase.value = 'live'
  try {
    if (useCamera.value) {
      await new Promise((r) => setTimeout(r, 100))
      startCameraStream()
    }
    const userId = localStorage.getItem('userId') || 'guest'
    const raw = await request.post('/api/interview/start', {
      userId,
      jobTitle: selectedCareer.value.jobTitle,
    })
    const res = unwrapData(raw)
    sessionId.value = res.sessionId
    currentRound.value = 1
    chatHistory.value.push({ role: 'ai', content: res.question || '（未收到题目，请重试）' })
  } catch (e) {
    phase.value = 'select'
    ElMessage.error(e?.message || '开始面试失败，请确认后端已启动')
  } finally {
    isThinking.value = false
  }
}

async function submitAnswer() {
  const text = userInput.value.trim()
  if (!text || !sessionId.value) return
  chatHistory.value.push({ role: 'user', content: text })
  userInput.value = ''
  isThinking.value = true
  try {
    const raw = await request.post('/api/interview/chat', {
      sessionId: sessionId.value,
      answer: text,
      emotion: useCamera.value ? currentEmotion.value : '未开启摄像头',
    })
    const res = unwrapData(raw)
    lastFeedback.value = {
      feedback: res.feedback || '—',
      correctness: res.correctness || '一般',
      completenessScore: Number(res.completenessScore) || 0,
    }
    const nextQ = res.nextQuestion || '请继续补充。'
    chatHistory.value.push({ role: 'ai', content: nextQ })
    currentRound.value = res.round ?? currentRound.value + 1
  } catch (e) {
    chatHistory.value.pop()
    userInput.value = text
    ElMessage.error(e?.message || '提交失败')
  } finally {
    isThinking.value = false
  }
}

async function finishInterview() {
  if (!sessionId.value) return
  try {
    await ElMessageBox.confirm('确定结束面试并生成总评？', '结束面试', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  isThinking.value = true
  stopCamera()
  try {
    const raw = await request.post('/api/interview/finish', { sessionId: sessionId.value })
    const res = unwrapData(raw)
    reportData.value = {
      expressScore: res.expressScore,
      techScore: res.techScore,
      logicScore: res.logicScore,
      summary: res.summary,
      suggestions: Array.isArray(res.suggestions) ? res.suggestions : [],
    }
    phase.value = 'report'
  } catch (e) {
    ElMessage.error(e?.message || '生成报告失败')
  } finally {
    isThinking.value = false
  }
}

function resetAll() {
  stopCamera()
  sessionId.value = null
  currentRound.value = 0
  chatHistory.value = []
  userInput.value = ''
  reportData.value = null
  lastFeedback.value = null
  phase.value = 'select'
}

function startVoiceInput() {
  const SR = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SR) {
    ElMessage.warning('当前浏览器不支持语音识别，请使用 Chrome / Edge')
    return
  }
  const recognition = new SR()
  recognition.lang = 'zh-CN'
  recognition.onresult = (e) => {
    const t = e.results[0][0].transcript
    userInput.value = userInput.value ? `${userInput.value} ${t}` : t
  }
  recognition.start()
  ElMessage.info('请开始说话…')
}

onBeforeUnmount(() => {
  stopCamera()
})
</script>

<style scoped>
.mi-page {
  min-height: 100vh;
  background: linear-gradient(165deg, #0f172a 0%, #1e293b 45%, #0f172a 100%);
  color: #e2e8f0;
  padding: 24px 20px 48px;
  box-sizing: border-box;
}

.mi-header {
  max-width: 1100px;
  margin: 0 auto 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.mi-back {
  font-size: 15px;
}

.mi-header-title h1 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: 0.02em;
}

.mi-sub {
  margin: 0;
  font-size: 14px;
  color: #94a3b8;
  max-width: 520px;
  line-height: 1.5;
}

.mi-header-spacer {
  flex: 1;
}

.mi-steps {
  max-width: 900px;
  margin: 0 auto 28px !important;
}

.mi-steps :deep(.el-step__title) {
  color: #cbd5e1 !important;
  font-size: 14px;
}

.mi-steps :deep(.el-step__description) {
  color: #64748b !important;
}

.mi-card {
  max-width: 1100px;
  margin: 0 auto;
  border-radius: 14px !important;
  border: 1px solid rgba(148, 163, 184, 0.2) !important;
  background: rgba(30, 41, 59, 0.85) !important;
  --el-card-bg-color: transparent;
}

.mi-card :deep(.el-card__header) {
  border-bottom-color: rgba(148, 163, 184, 0.15);
  color: #f1f5f9;
}

.mi-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.mi-hint {
  color: #94a3b8;
  font-size: 14px;
  margin: 0 0 20px;
  line-height: 1.6;
}

.mi-careers {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
  margin-bottom: 24px;
}

.mi-career-btn {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  padding: 16px 14px;
  border-radius: 12px;
  border: 2px solid rgba(148, 163, 184, 0.25);
  background: rgba(15, 23, 42, 0.5);
  color: #e2e8f0;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, transform 0.15s;
}

.mi-career-btn:hover {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.12);
}

.mi-career-btn.is-active {
  border-color: #60a5fa;
  background: rgba(59, 130, 246, 0.2);
  box-shadow: 0 0 0 1px rgba(96, 165, 250, 0.35);
}

.mi-career-emoji {
  font-size: 28px;
  margin-bottom: 8px;
}

.mi-career-label {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 4px;
}

.mi-career-desc {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}

.mi-tools {
  margin-bottom: 20px;
}

.mi-switch-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mi-switch-wrap :deep(.el-switch__label) {
  color: #cbd5e1 !important;
}

.mi-loading-text {
  font-size: 13px;
  color: #94a3b8;
}

.mi-actions {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

.mi-actions-footer {
  margin-top: 24px;
  gap: 12px;
  flex-wrap: wrap;
}

.mi-live {
  max-width: 1100px;
  margin: 0 auto;
}

.mi-chat-card :deep(.el-card__body) {
  padding-top: 12px;
}

.mi-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mi-scroll {
  margin-bottom: 16px;
}

.mi-empty-chat {
  padding: 40px 0;
}

.mi-bubble-row {
  display: flex;
  margin-bottom: 14px;
}

.mi-bubble-row.is-ai {
  justify-content: flex-start;
}

.mi-bubble-row.is-user {
  justify-content: flex-end;
}

.mi-bubble {
  max-width: 88%;
  padding: 12px 16px;
  border-radius: 14px;
  line-height: 1.55;
  font-size: 14px;
}

.mi-bubble-ai {
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.35);
  color: #e0f2fe;
}

.mi-bubble-user {
  background: rgba(16, 185, 129, 0.18);
  border: 1px solid rgba(16, 185, 129, 0.35);
  color: #d1fae5;
}

.mi-bubble-role {
  display: block;
  font-size: 11px;
  opacity: 0.75;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.mi-compose-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.mi-compose :deep(.el-textarea__inner) {
  background: rgba(15, 23, 42, 0.6);
  color: #f1f5f9;
  border-color: rgba(148, 163, 184, 0.3);
}

.mi-round-report {
  margin-top: 18px;
}

.mi-round-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 16px;
  align-items: center;
}

.mi-round-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mi-round-wide {
  grid-column: 2;
}

.mi-round-full {
  grid-column: 1 / -1;
}

.mi-k {
  font-size: 13px;
  color: #94a3b8;
}

.mi-feedback-p {
  margin: 0;
  color: #e2e8f0;
  font-size: 14px;
  line-height: 1.6;
}

.mi-tag-lg {
  width: fit-content;
}

.mi-video-card .mi-video-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: #020617;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 12px;
}

.mi-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mi-canvas {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.mi-alert {
  background: rgba(30, 41, 59, 0.9) !important;
}

.mi-video-note {
  font-size: 12px;
  color: #64748b;
  margin: 10px 0 0;
}

.mi-report-body {
  padding: 8px 0;
}

.mi-score-block {
  text-align: center;
  padding: 20px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.mi-score-label {
  display: block;
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 10px;
}

.mi-score-num {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
}

.mi-c1 {
  color: #38bdf8;
}
.mi-c2 {
  color: #a78bfa;
}
.mi-c3 {
  color: #fbbf24;
}

.mi-score-unit {
  font-size: 14px;
  color: #64748b;
  margin-left: 4px;
}

.mi-h3 {
  font-size: 16px;
  color: #f1f5f9;
  margin: 16px 0 10px;
}

.mi-summary {
  color: #cbd5e1;
  line-height: 1.7;
  margin: 0 0 8px;
}

.mi-sug-list {
  margin: 0;
  padding-left: 20px;
  color: #94a3b8;
  line-height: 1.8;
}

@media (max-width: 768px) {
  .mi-round-grid {
    grid-template-columns: 1fr;
  }
  .mi-round-wide {
    grid-column: 1;
  }
}
</style>
