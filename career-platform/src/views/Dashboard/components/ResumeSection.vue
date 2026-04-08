<script setup>
defineProps({
  dash: { type: Object, required: true },
})
</script>

<template>
  <section class="fade-in resume-hub-container relative">
    <div v-if="dash.hoveredPreviewUrl" class="dashboard-preview">
      <div class="preview-header">简历大图预览</div>
      <img :src="dash.getSafeImageUrl(dash.hoveredPreviewUrl)" alt="预览" />
    </div>

    <div class="rh-module ai-module">
      <div class="rh-header">
        <div class="rh-title">
          <h3>
            <span class="title-icon">✨</span>
            AI 专属简历定制
            <span class="hot-badge-new">🔥 智能生成</span>
          </h3>
          <p>只需两步：选择心仪的排版风格，粘贴经历信息，AI 为您生成专业描述与排版。</p>
        </div>
      </div>

      <div class="step-container">
        <div class="step-header-mini">
          <div class="step-badge">01</div>
          <h4 class="step-title-new">选择简历模板</h4>
          <span class="step-count">共 {{ dash.dbTemplates.slice(0, 8).length }} 款精选模板</span>
        </div>

        <div class="template-grid-new" v-if="dash.dbTemplates.length > 0">
          <div
            v-for="tpl in dash.dbTemplates.slice(0, 8)"
            :key="'ai-' + tpl.id"
            class="tpl-card-new"
            :class="{ active: dash.selectedAiTemplate?.id === tpl.id }"
            @mouseenter="dash.hoveredPreviewUrl = tpl.previewUrl"
            @mouseleave="dash.hoveredPreviewUrl = null"
            @click="dash.selectedAiTemplate = tpl"
          >
            <div
              class="tpl-preview"
              :style="{
                backgroundImage: `url(${dash.getSafeImageUrl(tpl.previewUrl)})`,
                backgroundSize: 'cover',
                backgroundPosition: 'top center',
                backgroundColor: '#f1f5f9',
              }"
            >
              <div class="tpl-badge">AI 推荐</div>
              <div class="active-check" v-if="dash.selectedAiTemplate?.id === tpl.id">✓</div>
            </div>
            <div class="tpl-info-new" style="padding: 12px 0; text-align: center">
              <div class="tpl-name-new" style="font-weight: 600; color: #334155">{{ tpl.name }}</div>
            </div>
          </div>
        </div>
        <div v-else class="loading-state" style="text-align: center; padding: 20px; color: #94a3b8">正在努力加载 OSS 模板数据...</div>
      </div>

      <div class="step-container">
        <div class="step-header-mini">
          <div class="step-badge">02</div>
          <h4 class="step-title-new">录入核心经历</h4>
          <span class="step-hint-new">哪怕只写大白话，AI也会帮您整理成专业简历</span>
        </div>

        <div class="example-card-new">
          <div class="example-icon">💡</div>
          <div class="example-content">
            <div class="example-title">📝 输入示例</div>
            <div class="example-text">我叫张三，电话 13800000000，邮箱 zhangsan@email.com。xx大学xx专业...</div>
          </div>
          <button class="use-example-btn" @click="dash.useExample">一键填充示例</button>
        </div>

        <div class="input-area-new">
          <div class="input-toolbar-new">
            <div class="toolbar-buttons">
              <button class="tool-btn" @click="dash.addTemplate('education')"><span>🎓</span> 教育</button>
              <button class="tool-btn" @click="dash.addTemplate('work')"><span>💼</span> 工作</button>
              <button class="tool-btn" @click="dash.addTemplate('project')"><span>🚀</span> 项目</button>
              <button class="tool-btn" @click="dash.addTemplate('skill')"><span>⚡</span> 技能</button>
              <button class="tool-btn" @click="dash.addTemplate('award')"><span>🏆</span> 获奖</button>
            </div>
            <div class="toolbar-stats">
              <span class="char-count">{{ dash.rawResumeInput.length }}</span>
              <span class="char-max">/ 5000</span>
              <button class="clear-btn" @click="dash.clearInput" v-if="dash.rawResumeInput">清空</button>
            </div>
          </div>
          <textarea v-model="dash.rawResumeInput" placeholder="在这里粘贴或输入您的经历信息..." class="resume-textarea-new"></textarea>
        </div>
      </div>

      <div class="action-footer-new">
        <button class="generate-btn-new" @click="dash.handleGenerateResume" :disabled="dash.isGeneratingResume">
          <span class="btn-icon">🚀</span>
          <span class="btn-text">{{ dash.isGeneratingResume ? 'AI 大模型正在奋笔疾书中...' : '一键生成专属简历' }}</span>
        </button>
      </div>
    </div>

    <div class="rh-module">
      <div class="rh-header" style="display: flex; justify-content: space-between; align-items: flex-end">
        <div class="rh-title">
          <h3>🗂️ 简历模板库 <span class="badge-new">Word 原件下载</span></h3>
          <p>挑选喜欢的格式，直接下载 Word 文档修改，或者进行在线编辑</p>
        </div>
        <div class="view-all-btn" @click="dash.router.push('/resume-template')">进入专门页面挑选 <span>→</span></div>
      </div>

      <div class="rh-grid-new">
        <div
          class="rh-card-new"
          v-for="item in dash.dbTemplates"
          :key="'manual-' + item.id"
          @mouseenter="dash.hoveredPreviewUrl = item.previewUrl; dash.hoverId = 'manual-' + item.id"
          @mouseleave="dash.hoveredPreviewUrl = null; dash.hoverId = null"
          @click="dash.goToTemplateDetail(item.id)"
        >
          <div class="card-inner-new" :style="{ backgroundImage: `url(${dash.getSafeImageUrl(item.previewUrl)})` }">
            <transition name="fade">
              <div class="hover-overlay-new" v-show="dash.hoverId === 'manual-' + item.id">
                <button class="overlay-btn-new primary" @click.stop="dash.goToTemplateDetail(item.id)">在线编辑</button>
                <button class="overlay-btn-new outline" @click.stop="dash.handleDownload(item)">在线下载</button>
              </div>
            </transition>
          </div>
          <div class="rh-info-new">
            <h4>{{ item.name }}</h4>
            <p>📄 DOCX 格式模板</p>
          </div>
        </div>
      </div>
    </div>

    <div class="rh-module">
      <div class="rh-header" style="display: flex; justify-content: space-between; align-items: flex-end">
        <div class="rh-title">
          <h3>💡 优秀简历灵感</h3>
          <p>各行各业大厂真实脱敏简历，可直接预览或下载参考</p>
        </div>
        <div class="view-all-btn" @click="dash.router.push('/resume-template')">查看全部牛人简历 <span>→</span></div>
      </div>

      <div class="rh-grid-new">
        <div
          class="rh-card-new case-card"
          v-for="item in dash.excellentResumes"
          :key="'excellent-' + item.id"
          @mouseenter="dash.hoveredPreviewUrl = item.previewUrl; dash.hoverId = 'excellent-' + item.id"
          @mouseleave="dash.hoveredPreviewUrl = null; dash.hoverId = null"
          @click="dash.goToTemplateDetail(item.id)"
        >
          <div class="card-inner-new" :style="{ backgroundImage: `url(${dash.getSafeImageUrl(item.previewUrl)})` }">
            <transition name="fade">
              <div class="hover-overlay-new" v-show="dash.hoverId === 'excellent-' + item.id">
                <button class="overlay-btn-new primary" @click.stop="dash.previewRealDoc(item.docUrl)">真实原件预览</button>
                <button class="overlay-btn-new outline" @click.stop="dash.handleDownload(item)">直接下载原件</button>
              </div>
            </transition>
          </div>
          <div class="rh-info-new">
            <h4>{{ item.name }}</h4>
            <p>🌟 行业大牛参考案例</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
