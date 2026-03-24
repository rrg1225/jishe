<template>
  <div class="job-detail-layout">
    <header class="detail-header">
      <div class="back-btn" @click="router.back()">
        <span class="icon">❮</span> 返回双选大厅
      </div>
      <div class="actions">
        <button class="action-btn">⭐ 收藏岗位</button>
        <button class="action-btn share">🔗 分享</button>
      </div>
    </header>

    <main class="detail-main" v-if="jobData">
      <section class="top-card">
        <div class="job-primary">
          <div class="title-row">
            <h1>{{ jobData.position }}</h1>
            <span class="salary">{{ jobData.salary }}</span>
          </div>
          <div class="tags-row">
            <span class="info-tag">📍 {{ jobData.location }}</span>
            <span class="info-tag">⏰ 截止: {{ jobData.deadline }}</span>
            <span v-for="tag in jobData.tags" :key="tag" class="benefit-tag">{{ tag }}</span>
          </div>
        </div>
        
        <div class="company-card">
          <div class="c-logo">{{ jobData.company ? jobData.company.charAt(0) : '企' }}</div>
          <div class="c-info">
            <h3>{{ jobData.company }}</h3>
            <p>产教融合合作企业</p>
          </div>
        </div>
      </section>

      <div class="content-split">
        <div class="left-content">
          <div class="oss-image-container" v-if="jobData.detailOssUrl">
            <h3 class="section-title">📄 详细职位描述 (JD)</h3>
            <img :src="jobData.detailOssUrl" alt="职位详情长图" class="jd-image" />
          </div>
          
          <div class="text-desc" v-else>
            <h3 class="section-title">📄 职位详情</h3>
            <div class="desc-box">
              <p><strong>【岗位职责】</strong></p>
              <p style="white-space: pre-wrap;">{{ jobData.jobDescription }}</p>
              <br/>
              <p><strong>【任职要求】</strong></p>
              <p style="white-space: pre-wrap;">{{ jobData.requirements }}</p>
            </div>
          </div>
        </div>

        <div class="right-sidebar">
          <div class="apply-box">
            <button class="apply-btn" @click="handleApply">立即投递简历</button>
            <p class="apply-tip">💡 完善您的 AI 专属简历可极大提高通过率</p>
          </div>
        </div>
      </div>
    </main>
    
    <div v-else class="loading-state">
      <div class="spinner"></div>
      <p>正在加载岗位详情...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const jobData = ref(null)

onMounted(() => {
  // 接收从 Dashboard 传过来的岗位数据
  if (route.query.data) {
    try {
      jobData.value = JSON.parse(decodeURIComponent(route.query.data))
    } catch (e) {
      console.error("解析岗位数据失败", e)
    }
  }
})

const handleApply = () => {
  alert('简历已成功投递！HR将在 3 个工作日内处理。')
}
</script>

<style scoped>
.job-detail-layout { min-height: 100vh; background: #f5f7fa; padding-bottom: 40px; }
.detail-header { height: 60px; background: white; padding: 0 40px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 10px rgba(0,0,0,0.05); position: sticky; top: 0; z-index: 10; }
.back-btn { display: flex; align-items: center; gap: 8px; color: #64748b; cursor: pointer; font-size: 15px; font-weight: 500; transition: color 0.3s; }
.back-btn:hover { color: #4aa3d1; }
.actions { display: flex; gap: 12px; }
.action-btn { padding: 6px 16px; border-radius: 20px; border: 1px solid #e2e8f0; background: white; color: #475569; cursor: pointer; transition: 0.3s; }
.action-btn:hover { background: #f8fafc; border-color: #cbd5e1; }

.detail-main { max-width: 1000px; margin: 20px auto; padding: 0 20px; }
.top-card { background: white; border-radius: 16px; padding: 30px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 15px rgba(0,0,0,0.03); margin-bottom: 20px; flex-wrap: wrap; gap: 20px; }
.job-primary { flex: 1; }
.title-row { display: flex; align-items: center; gap: 20px; margin-bottom: 16px; }
.title-row h1 { margin: 0; font-size: 28px; color: #1e293b; font-weight: 700; }
.salary { font-size: 24px; color: #f97316; font-weight: 600; }
.tags-row { display: flex; gap: 12px; flex-wrap: wrap; }
.info-tag { color: #64748b; font-size: 14px; }
.benefit-tag { background: #f1f5f9; color: #475569; padding: 4px 12px; border-radius: 4px; font-size: 13px; }

.company-card { display: flex; align-items: center; gap: 15px; background: #f8fafc; padding: 15px 25px; border-radius: 12px; border: 1px solid #e2e8f0; }
.c-logo { width: 50px; height: 50px; background: #4aa3d1; color: white; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: bold; }
.c-info h3 { margin: 0 0 4px 0; font-size: 16px; color: #1e293b; }
.c-info p { margin: 0; font-size: 13px; color: #64748b; }

.content-split { display: flex; gap: 20px; align-items: flex-start; }
@media (max-width: 768px) { .content-split { flex-direction: column; } }
.left-content { flex: 1; background: white; border-radius: 16px; padding: 30px; box-shadow: 0 4px 15px rgba(0,0,0,0.03); min-width: 0; }
.section-title { font-size: 18px; color: #1e293b; margin: 0 0 20px 0; padding-bottom: 15px; border-bottom: 1px solid #f1f5f9; }
.jd-image { width: 100%; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); display: block; }
.desc-box { color: #334155; line-height: 1.8; font-size: 15px; }

.right-sidebar { width: 300px; background: white; border-radius: 16px; padding: 25px; box-shadow: 0 4px 15px rgba(0,0,0,0.03); position: sticky; top: 80px; }
.apply-btn { width: 100%; padding: 14px; background: #4aa3d1; color: white; border: none; border-radius: 8px; font-size: 16px; font-weight: 600; cursor: pointer; transition: 0.3s; box-shadow: 0 4px 12px rgba(74, 163, 209, 0.3); margin-bottom: 15px; }
.apply-btn:hover { background: #398ebf; transform: translateY(-2px); }
.apply-tip { margin: 0; font-size: 12px; color: #94a3b8; text-align: center; }
.loading-state { text-align: center; padding: 100px; color: #64748b; }
.spinner { width: 40px; height: 40px; border: 3px solid #f1f5f9; border-top-color: #4aa3d1; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 20px; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>