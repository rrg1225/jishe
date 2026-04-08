import { ref, computed } from 'vue'
import request from '../../utils/request'
import { getResumeNotifyUserId } from '../../utils/resumeNotifyUserId.js'
import { ElMessage } from 'element-plus'

/**
 * AI 简历录入、生成任务提交与模板库 / OSS 交互。
 */
export function useResumeBuilder(router) {
  const selectedAiTemplate = ref(null)
  const rawResumeInput = ref('')
  const isGeneratingResume = ref(false)

  const detectedInfo = computed(() => {
    const info = []
    const text = rawResumeInput.value
    if (text.match(/[1-9]\d{10}/)) info.push('📱 手机号')
    if (text.match(/@/)) info.push('📧 邮箱')
    if (text.match(/大学|学院|学校/)) info.push('🎓 教育经历')
    if (text.match(/项目|开发|设计|负责|参与/)) info.push('💼 项目经历')
    if (text.match(/熟悉|掌握|精通|了解/)) info.push('⚡ 技能特长')
    if (text.match(/获奖|荣誉|一等奖|奖学金/)) info.push('🏆 获奖经历')
    return info.slice(0, 5)
  })

  const suggestions = computed(() => {
    const sug = []
    const text = rawResumeInput.value
    if (!text.match(/大学|学院|学校/)) sug.push('教育背景')
    if (!text.match(/项目|工作|实习|经历/)) sug.push('项目/工作经历')
    if (!text.match(/熟悉|掌握|精通|了解/)) sug.push('技能特长')
    if (!text.match(/获奖|荣誉|一等奖|奖学金/)) sug.push('获奖/证书')
    if (!text.match(/电话|手机|邮箱|@/)) sug.push('联系方式')
    return sug.slice(0, 4)
  })

  const useExample = () => {
    rawResumeInput.value = `我叫张三，电话 13800000000，邮箱 zhangsan@email.com。2024年毕业于xx大学xx专业，GPA 3.8/4.0。

【项目经历】
校园外卖平台项目 | 2023.09 - 2024.02
- 使用 Vue3 + Vite + Pinia 构建前端，实现商家展示、购物车、订单管理等功能
- 基于 Axios 封装请求，实现 JWT 身份验证和路由守卫
- 项目获得院级优秀项目，服务校内用户 2000+

【技能特长】
- 编程语言：JavaScript、TypeScript、Python
- 框架工具：Vue3、React、Node.js、Git
- 语言能力：CET-6 (550分)

【获奖情况】
- 校一等奖学金 (2022-2023)
- 全国计算机设计大赛省二等奖
- 校优秀学生干部`
  }

  const clearInput = () => {
    rawResumeInput.value = ''
  }

  const addTemplate = (type) => {
    const templates = {
      education: '\n\n【教育经历】\n• 学校名称 | 专业 | 学历 | 2020.09 - 2024.06\n• 主修课程：\n• GPA：3.8/4.0\n',
      work: '\n\n【工作经历】\n• 公司名称 | 职位 | 2023.07 - 至今\n• 工作内容：\n• 主要成果：\n',
      project: '\n\n【项目经历】\n• 项目名称 | 技术栈 | 2023.09 - 2024.02\n• 项目描述：\n• 我的职责：\n• 项目成果：\n',
      skill: '\n\n【专业技能】\n• 编程语言：JavaScript、Python、Java\n• 框架工具：Vue3、React、Node.js\n• 语言能力：英语 CET-6\n',
      award: '\n\n【获奖情况】\n• 奖项名称 | 级别 | 时间\n• 奖项名称 | 级别 | 时间\n',
    }
    rawResumeInput.value += templates[type] || ''
  }

  function buildResumeMarkdownFromAi(data) {
    const name = (data.fullName || '求职者').trim()
    const job = (data.targetJob || '求职意向').trim()
    let md = `# ${name} - ${job}\n\n`
    if (data.educationInfo) md += `## 教育经历\n\n${data.educationInfo}\n\n`
    if (data.projectExperience) md += `## 项目与经历\n\n${data.projectExperience}\n\n`
    if (data.professionalSkills) md += `## 专业技能\n\n${data.professionalSkills}\n\n`
    return md.trim()
  }

  function guessNameFromRaw(text) {
    const m = text.match(/我是\s*([^\s，,。]{2,20})/)
    return m ? m[1].trim() : ''
  }

  const handleGenerateResume = async () => {
    if (!selectedAiTemplate.value || !selectedAiTemplate.value.id) {
      alert('请先选择一个简历模板！')
      return
    }
    if (!rawResumeInput.value.trim()) {
      alert('请写点内容，AI 才能帮您整理哦！')
      return
    }
    const raw = rawResumeInput.value.trim()
    const guessedName = guessNameFromRaw(raw)
    const payload = {
      userId: getResumeNotifyUserId(),
      fullName: guessedName || '求职者',
      contactPhone: '',
      targetJob: '综合求职意向',
      educationInfo: '',
      projectExperience: raw,
      professionalSkills: '',
      templateName: selectedAiTemplate.value.name || '标准通用模板',
    }
    isGeneratingResume.value = true
    sessionStorage.setItem('tempResumePrompt', raw)
    sessionStorage.setItem('selectedTemplateId', String(selectedAiTemplate.value.id))
    try {
      await request.post('/api/resume/ai-generate', payload)
      ElMessage({
        message: '🚀 简历排版任务已提交！AI 正在云端奋笔疾书，您可以先去浏览其他内容，完成后右上角会弹出通知。',
        type: 'success',
        duration: 5000,
      })
    } catch (e) {
      console.error('任务提交失败:', e)
      ElMessage.error('任务提交失败，请检查网络或稍后重试。')
    } finally {
      isGeneratingResume.value = false
    }
  }

  const dbTemplates = ref([])
  const excellentResumes = ref([])
  const hoveredPreviewUrl = ref(null)
  const hoverId = ref(null)

  const fetchTemplatesData = async () => {
    try {
      const res1 = await request.get('/api/templates/list', { params: { type: 1 } })
      dbTemplates.value = res1.data || []
      if (dbTemplates.value.length > 0) {
        selectedAiTemplate.value = dbTemplates.value[0]
      }
      const res2 = await request.get('/api/templates/list', { params: { type: 2 } })
      excellentResumes.value = res2.data || []
    } catch (error) {
      console.error('加载简历数据失败，请确保后端服务已启动且接口路径正确')
    }
  }

  const goToTemplateDetail = (id) => {
    router.push(`/template-detail/${id}`)
  }

  const getSafeImageUrl = (url) => {
    if (!url) return 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?w=400'
    const lowerUrl = String(url).toLowerCase()
    if (lowerUrl.includes('.doc') || lowerUrl.includes('.docx')) {
      return 'https://images.unsplash.com/photo-1612441804231-77a36b284856?w=400'
    }
    return url
  }

  const previewRealDoc = (fileUrl) => {
    if (!fileUrl) {
      alert('该模板暂无源文件，无法预览')
      return
    }
    const officeUrl = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(fileUrl)}`
    window.open(officeUrl, '_blank')
  }

  const handleDownload = (item) => {
    if (item.fileUrl) {
      window.location.href = item.fileUrl
    } else {
      alert('未找到源文件链接')
    }
  }

  return {
    selectedAiTemplate,
    rawResumeInput,
    isGeneratingResume,
    detectedInfo,
    suggestions,
    useExample,
    clearInput,
    addTemplate,
    handleGenerateResume,
    buildResumeMarkdownFromAi,
    guessNameFromRaw,
    dbTemplates,
    excellentResumes,
    hoveredPreviewUrl,
    hoverId,
    fetchTemplatesData,
    goToTemplateDetail,
    getSafeImageUrl,
    previewRealDoc,
    handleDownload,
  }
}
