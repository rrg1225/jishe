<template>
  <div id="app-container">
    <router-view></router-view>
    <AIMentor v-if="showMentor" />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification } from 'element-plus' // 引入 Element Plus 的通知组件
import AIMentor from './components/AIMentor.vue' 

const route = useRoute()
const router = useRouter()
// 直接通过 meta 属性判断，无需维护路径列表
const showMentor = computed(() => !route.meta.hideMentor)

// --- 新增：全局系统通知 WebSocket ---
let sysNotifyWs = null;

// 生成 Markdown 的辅助函数 (专门处理后台异步传来的数据)
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

onMounted(() => {
  // 从本地获取当前登录用户的标识 (假设你存的是 userAccount 或 userName)
  const userAccount = localStorage.getItem('userAccount') || 'default_user'; 
  
  // 【重点修复】：使用已有的 /chat 路由，但加上 _notify 后缀作为专属系统通知频道
  const wsUrl = `ws://localhost:8080/chat/${userAccount}_notify`; 
  
  sysNotifyWs = new WebSocket(wsUrl);

  sysNotifyWs.onopen = () => console.log("🌐 全局通知服务已连接");

  sysNotifyWs.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      
      // 当收到后端发来的生成成功消息时
      if (data.type === 'RESUME_READY') {
        
        // 1. 将后端随 WebSocket 传过来的 AI 生成结果保存到本地
        if (data.resumeData) {
          localStorage.setItem('generatedResumeFullData', JSON.stringify(data.resumeData));
          const md = buildResumeMarkdown(data.resumeData);
          localStorage.setItem('generatedResume', md);
        }

        // 2. 弹出优美的右下角通知
        ElNotification({
          title: '🎉 简历生成完毕',
          message: '您的 AI 专属简历已排版完成，点击此处立即查看！',
          type: 'success',
          duration: 0, // 设置为 0 表示不自动关闭，除非用户手动点掉
          onClick: () => {
            // 用户点击通知卡片时，自动跳转到预览/编辑页
            router.push('/resume-preview');
          }
        });
      } 
      else if (data.type === 'RESUME_FAILED') {
        ElNotification.error({
          title: '生成失败',
          message: 'AI 大模型开小差了，请重试。'
        });
      }
    } catch (e) {
      console.error("解析全局通知失败", e);
    }
  };
})

onUnmounted(() => {
  if (sysNotifyWs) {
    sysNotifyWs.close();
  }
})
</script>

<style>
/* 🌟 修复滚动 Bug：移除死板的 height: 100%，改用 min-height 让页面能够自然向下延伸 */
body, html { 
  margin: 0; 
  padding: 0; 
  min-height: 100vh; /* 允许内容撑开高度 */
  width: 100%; 
  font-family: 'Helvetica Neue', Arial, sans-serif; 
}
#app, #app-container { 
  min-height: 100vh; 
}
</style>