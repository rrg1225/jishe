<template>
  <div class="template-preview-layout">
    <header class="top-nav">
      <div class="left-action">
        <button class="back-btn" @click="router.push('/dashboard')">
          <span class="icon">←</span> 返回主页
        </button>
        <span class="nav-title">简历在线编辑：{{ resumeData.templateName }}</span>
      </div>

      <div class="right-actions">
        <input type="file" ref="fileInput" accept=".pdf,.doc,.docx" style="display: none" @change="handleFileUpload">
        
        <button class="action-btn import-btn" @click="triggerImport">
          <span class="icon">📄</span> 智能导入
        </button>
        <button class="action-btn save-btn" @click="handleSave" :disabled="isSaving">
          <span class="icon">💾</span> {{ isSaving ? '保存中...' : '保存简历' }}
        </button>
      </div>
    </header>

    <main class="preview-container">
      <div class="a4-paper">
        <header class="resume-header">
          <input class="input-name" v-model="resumeData.fullName" placeholder="请输入姓名">
          <div class="contact-row">
            <input v-model="resumeData.contactPhone" placeholder="手机号">
            <span class="sep">|</span>
            <input v-model="resumeData.email" placeholder="电子邮箱">
            <span class="sep">|</span>
            <input v-model="resumeData.targetCity" placeholder="意向城市">
          </div>
        </header>

        <div class="resume-section">
          <h2 class="section-title">教育背景</h2>
          <textarea 
            class="res-textarea" 
            v-model="resumeData.educationInfo" 
            placeholder="例如：2022.09 - 2026.06  南京工业大学  建筑环境与能源应用工程 (本科)"
            rows="3"
          ></textarea>
        </div>

        <div class="resume-section">
          <h2 class="section-title">项目经历</h2>
          <textarea 
            class="res-textarea" 
            v-model="resumeData.projectExperience" 
            placeholder="请详细描述你的项目经历、竞赛经历等..."
            rows="12"
          ></textarea>
        </div>

        <div class="resume-section">
          <h2 class="section-title">专业技能</h2>
          <textarea 
            class="res-textarea" 
            v-model="resumeData.professionalSkills" 
            placeholder="例如：熟练使用 Java, Vue3, 能够独立进行全栈开发..."
            rows="5"
          ></textarea>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request' // 引入你的 axios 工具

const route = useRoute()
const router = useRouter()
const fileInput = ref(null)
const isSaving = ref(false)

// 🌟 核心：响应式简历数据对象，与后端 Resume.java 字段一一对应
const resumeData = reactive({
  templateName: '标准通用模板',
  fullName: '',
  contactPhone: '',
  email: '',
  targetCity: '',
  educationInfo: '',
  projectExperience: '',
  professionalSkills: ''
})

// 🚀 页面加载时：从后端 API 拉取数据（数据回显）
onMounted(async () => {
  if (route.query.title) {
    resumeData.templateName = route.query.title
  }

  try {
    const res = await request.get('/api/resume/my')
    if (res && res.fullName !== undefined) {
      // 将后端返回的数据填充到响应式对象中
      Object.assign(resumeData, res)
    }
  } catch (error) {
    console.error('获取简历失败', error)
  }
})

// 💾 保存逻辑：呼叫后端 save 接口
const handleSave = async () => {
  if (!resumeData.fullName) return alert('姓名不能为空哦！')
  
  isSaving.value = true
  try {
    await request.post('/api/resume/save', resumeData)
    alert('✅ 简历已同步到云端数据库！')
  } catch (error) {
    alert('保存失败，请检查网络或登录状态')
  } finally {
    isSaving.value = false
  }
}

// 触发本地文件选择
const triggerImport = () => { fileInput.value.click() }

// 模拟导入逻辑（下一步我们会写文件解析后端）
const handleFileUpload = (e) => {
  const file = e.target.files[0]
  if (!file) return
  alert(`已选中：${file.name}\n即将接入 AI 解析功能，自动填充表单！`)
}
</script>

<style scoped>
.template-preview-layout { min-height: 100vh; background-color: #e5e7eb; display: flex; flex-direction: column; }

/* 顶部导航 */
.top-nav { height: 64px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.05); display: flex; justify-content: space-between; align-items: center; padding: 0 40px; position: sticky; top: 0; z-index: 100; }
.left-action { display: flex; align-items: center; gap: 20px; }
.back-btn { background: #f1f5f9; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
.nav-title { font-weight: bold; color: #1e293b; border-left: 2px solid #cbd5e1; padding-left: 20px; }
.right-actions { display: flex; gap: 15px; }
.action-btn { padding: 8px 20px; border-radius: 6px; cursor: pointer; font-weight: 500; transition: 0.3s; }
.import-btn { background: #fff; border: 1px solid #00aeec; color: #00aeec; }
.save-btn { background: #00aeec; border: none; color: #fff; }
.save-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/* 沉浸式编辑器 */
.preview-container { flex: 1; padding: 40px 0; overflow-y: auto; }
.a4-paper { width: 794px; min-height: 1123px; background: #fff; margin: 0 auto; box-shadow: 0 10px 30px rgba(0,0,0,0.1); padding: 60px; box-sizing: border-box; }

/* 简历内容样式：把 input 做得像文档一部分 */
.resume-header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 20px; margin-bottom: 30px; }
.input-name { width: 100%; border: none; text-align: center; font-size: 32px; font-weight: bold; outline: none; margin-bottom: 10px; }
.input-name:focus { background: #f0faff; }

.contact-row { display: flex; justify-content: center; align-items: center; gap: 10px; }
.contact-row input { border: none; width: 150px; text-align: center; font-size: 13px; color: #555; outline: none; }
.contact-row input:focus { background: #f0faff; }
.sep { color: #ccc; }

.resume-section { margin-bottom: 25px; }
.section-title { font-size: 16px; color: #111; background: #f1f5f9; padding: 6px 12px; border-left: 4px solid #00aeec; margin-bottom: 15px; text-align: left; }

/* 核心编辑区：取消边框，看起来像纸张 */
.res-textarea {
  width: 100%;
  border: 1px transparent solid;
  border-radius: 4px;
  padding: 8px;
  font-size: 14px;
  color: #444;
  line-height: 1.8;
  resize: none;
  outline: none;
  font-family: inherit;
  transition: 0.2s;
}
.res-textarea:hover { border-color: #e5e7eb; background: #fafafa; }
.res-textarea:focus { border-color: #00aeec; background: #fff; box-shadow: 0 0 5px rgba(0,174,236,0.1); }
</style>