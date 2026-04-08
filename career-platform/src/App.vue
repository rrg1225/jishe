<template>
  <div id="app-container">
    <router-view></router-view>
    <AIMentor v-if="showMentor" />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import AIMentor from './components/AIMentor.vue'
import { getResumeNotifyUserId } from './utils/resumeNotifyUserId.js'

const route = useRoute()
const router = useRouter()
const showMentor = computed(() => !route.meta.hideMentor)

let sysNotifyWs = null
let reconnectTimer = null
let lastNotifyWsUrl = ''

function buildResumeMarkdown(data) {
  const name = (data.fullName || '求职者').trim()
  const job = (data.targetJob || '求职意向').trim()
  let md = `# ${name} - ${job}\n\n`
  const phone = (data.contactPhone || '').trim()
  if (phone) md += `📞 ${phone}\n\n`
  if (data.educationInfo) md += `## 教育经历\n\n${data.educationInfo}\n\n`
  if (data.projectExperience) md += `## 项目与经历\n\n${data.projectExperience}\n\n`
  if (data.professionalSkills) md += `## 专业技能\n\n${data.professionalSkills}\n\n`
  return md.trim()
}

/** 与私信 WebSocket 一致：必须带 token，且路径 userId 与简历推送 default_notify 前缀一致 */
function getSysNotifyWsUrl() {
  const token = localStorage.getItem('token')
  if (!token || token === 'undefined' || token === 'null') return null
  const uid = encodeURIComponent(getResumeNotifyUserId())
  const wsBase = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080'
  return `${wsBase}/chat/${uid}_notify?token=${encodeURIComponent(token)}`
}

function attachNotifyHandlers(socket) {
  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'RESUME_READY') {
        if (data.resumeData) {
          localStorage.setItem('generatedResumeFullData', JSON.stringify(data.resumeData))
          const md = buildResumeMarkdown(data.resumeData)
          localStorage.setItem('generatedResume', md)
        }
        ElNotification({
          title: '🎉 简历生成完毕',
          message: '您的 AI 专属简历已排版完成，点击此处立即查看！',
          type: 'success',
          duration: 0,
          onClick: () => {
            router.push('/resume-preview')
          },
        })
      } else if (data.type === 'RESUME_FAILED') {
        ElNotification.error({
          title: '生成失败',
          message: 'AI 大模型开小差了，请重试。',
        })
      }
    } catch (e) {
      console.error('解析全局通知失败', e)
    }
  }
  socket.onerror = (e) => console.warn('简历通知 WebSocket error', e)
}

function connectSysNotifyWs() {
  clearTimeout(reconnectTimer)
  reconnectTimer = setTimeout(() => {
    const url = getSysNotifyWsUrl()
    if (!url) {
      lastNotifyWsUrl = ''
      if (sysNotifyWs) {
        sysNotifyWs.close()
        sysNotifyWs = null
      }
      return
    }
    if (url === lastNotifyWsUrl && sysNotifyWs?.readyState === WebSocket.OPEN) {
      return
    }
    lastNotifyWsUrl = url
    if (sysNotifyWs) {
      sysNotifyWs.close()
      sysNotifyWs = null
    }
    try {
      sysNotifyWs = new WebSocket(url)
      sysNotifyWs.onopen = () => console.log('全局通知 WebSocket 已连接')
      attachNotifyHandlers(sysNotifyWs)
      sysNotifyWs.onclose = (ev) => {
        if (ev.code !== 1008) {
          console.warn('简历通知连接关闭', ev.code, ev.reason)
        }
      }
    } catch (e) {
      console.error('连接简历通知失败', e)
    }
  }, 80)
}

onMounted(() => {
  connectSysNotifyWs()
})

router.afterEach(() => {
  connectSysNotifyWs()
})

onUnmounted(() => {
  clearTimeout(reconnectTimer)
  if (sysNotifyWs) {
    sysNotifyWs.close()
    sysNotifyWs = null
  }
})
</script>

<style>
body,
html {
  margin: 0;
  padding: 0;
  min-height: 100vh;
  width: 100%;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}
#app,
#app-container {
  min-height: 100vh;
}
</style>
