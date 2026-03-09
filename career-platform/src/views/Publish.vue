<template>
  <div class="publish-layout">
    <header class="top-nav">
      <div class="nav-logo" @click="router.push('/dashboard')">创作中心</div>
      <div class="nav-actions">
        <button class="cancel-btn" @click="router.push('/dashboard')" :disabled="isPublishing">取消</button>
        <button class="publish-btn" @click="handleSubmit" :disabled="isPublishing">
          {{ isPublishing ? '上传 OSS 并发布中...' : '🚀 确认发布' }}
        </button>
      </div>
    </header>

    <div class="main-container">
      <div class="meta-config-area">
        <div class="config-row">
          <span class="row-label">内容类型：</span>
          <div class="tags-group">
            <span class="tag" :class="{ active: publishType === 'article' }" @click="publishType = 'article'">📝 图文文章</span>
            <span class="tag" :class="{ active: publishType === 'video' }" @click="publishType = 'video'">🎬 视频</span>
          </div>
        </div>
        
        <div class="config-row">
          <span class="row-label">所属专区：</span>
          <div class="tags-group">
            <span class="tag" :class="{ active: selectedCategory === 'matrix' }" @click="selectedCategory = 'matrix'">🎓 学业深造</span>
            <span class="tag" :class="{ active: selectedCategory === 'job' }" @click="selectedCategory = 'job'">💼 产教融合</span>
            <span class="tag" :class="{ active: selectedCategory === 'contest' }" @click="selectedCategory = 'contest'">🏆 竞赛大厅</span>
          </div>
        </div>
      </div>

      <input 
        type="text" 
        class="title-input" 
        placeholder="请输入吸引人的标题（5-30字）..." 
        v-model="title"
      >

      <div class="divider"></div>

      <div v-show="publishType === 'article'" class="editor-container">
        <Toolbar style="border-bottom: 1px solid #e5e9ef" :editor="editorRef" :defaultConfig="toolbarConfig" :mode="mode" />
        <Editor style="height: 500px; overflow-y: hidden;" v-model="htmlContent" :defaultConfig="editorConfig" :mode="mode" @onCreated="handleCreated" />
      </div>

      <div v-show="publishType === 'video'" class="video-upload-container">
        <div class="upload-box" v-if="!localVideoPreview" @click="triggerVideoSelect">
          <i class="icon-upload">📁</i>
          <p class="upload-tip">点击选择视频文件</p>
          <p class="upload-sub">支持 mp4, webm，最大 500MB。点击发布时将直传阿里云 OSS</p>
          <input type="file" ref="videoInput" accept="video/*" style="display: none" @change="handleVideoSelect">
        </div>
        
        <div class="video-preview-box" v-else>
          <video :src="localVideoPreview" controls class="preview-video"></video>
          <div class="re-upload" @click="triggerVideoSelect">重新选择</div>
        </div>

        <textarea class="video-desc-input" placeholder="给视频写一段吸引人的简介吧..." v-model="videoDesc" rows="4"></textarea>
      </div>

    </div>
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css'
import { onBeforeUnmount, ref, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import request from '../utils/request'

const router = useRouter()

const publishType = ref('article') 
const selectedCategory = ref('matrix') 
const title = ref('')
const isPublishing = ref(false) 

const videoFile = ref(null) 
const localVideoPreview = ref('') 
const videoDesc = ref('')
const videoInput = ref(null)

const triggerVideoSelect = () => { videoInput.value.click() }

const handleVideoSelect = (e) => {
  const file = e.target.files[0]
  if (!file) return
  videoFile.value = file
  localVideoPreview.value = URL.createObjectURL(file) 
}

const editorRef = shallowRef()
const htmlContent = ref('<p>在这里开启你的创作...</p>')
const mode = 'default' 
const toolbarConfig = { excludeKeys: ['fullScreen'] }

const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        try {
          const formData = new FormData()
          formData.append('file', file)
          const res = await request.post('/api/file/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })
          insertFn(res, file.name, res) 
        } catch (error) {
          alert('图片上传失败')
        }
      }
    }
  }
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
  if (localVideoPreview.value) {
    URL.revokeObjectURL(localVideoPreview.value) 
  }
})

const handleCreated = (editor) => { editorRef.value = editor }

const handleSubmit = async () => {
  if (!title.value.trim()) return alert('标题不能为空哦！')
  
  isPublishing.value = true 

  try {
    let finalVideoUrl = ''
    
    if (publishType.value === 'video') {
      if (!videoFile.value) {
        isPublishing.value = false
        return alert('请先选择一个视频文件！')
      }
      
      const formData = new FormData()
      formData.append('file', videoFile.value)
      
      finalVideoUrl = await request.post('/api/file/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }

    const payload = {
      title: title.value,
      type: publishType.value,
      category: selectedCategory.value,
      content: publishType.value === 'article' ? htmlContent.value : videoDesc.value,
      videoUrl: finalVideoUrl
    }

    await request.post('/api/content/publish', payload)
    
    alert('发布成功！即将返回首页')
    router.push('/dashboard')
    
  } catch (error) {
    console.error('发布失败', error)
    alert('发布遇到了一点小问题，请检查网络或是否登录')
  } finally {
    isPublishing.value = false
  }
}
</script>

<style scoped>
/* 🌟 这里是修复滚动条的关键代码 🌟 */
.publish-layout { 
  height: 100vh; 
  overflow-y: auto; 
  background-color: #f4f5f7; 
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; 
}

.top-nav { height: 60px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; justify-content: space-between; align-items: center; padding: 0 40px; position: sticky; top: 0; z-index: 100; }
.nav-logo { font-size: 18px; font-weight: bold; color: #18191c; cursor: pointer; }
.nav-actions { display: flex; gap: 15px; }
.cancel-btn { background: transparent; border: 1px solid #e3e5e7; color: #61666d; padding: 6px 20px; border-radius: 4px; cursor: pointer; transition: 0.3s; }
.cancel-btn:hover:not(:disabled) { background: #f4f5f7; }
.publish-btn { background: #00aeec; border: none; color: #fff; padding: 6px 20px; border-radius: 4px; cursor: pointer; font-weight: 500; transition: 0.3s; }
.publish-btn:hover:not(:disabled) { background: #00a1d6; }
.publish-btn:disabled, .cancel-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.main-container { max-width: 900px; margin: 30px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); padding: 40px; }

.meta-config-area { background: #f8fafc; padding: 20px; border-radius: 8px; margin-bottom: 30px; border: 1px solid #e2e8f0; }
.config-row { display: flex; align-items: center; margin-bottom: 15px; }
.config-row:last-child { margin-bottom: 0; }
.row-label { width: 90px; font-size: 15px; color: #475569; font-weight: 500; }
.tags-group { display: flex; gap: 12px; }
.tag { padding: 6px 16px; border-radius: 20px; border: 1px solid #cbd5e1; font-size: 14px; color: #64748b; cursor: pointer; transition: all 0.2s; background: #fff; }
.tag:hover { border-color: #00aeec; color: #00aeec; }
.tag.active { background: #00aeec; border-color: #00aeec; color: #fff; font-weight: bold; box-shadow: 0 4px 6px rgba(0, 174, 236, 0.2); }

.title-input { width: 100%; border: none; font-size: 24px; font-weight: bold; color: #18191c; outline: none; padding: 10px 0; }
.title-input::placeholder { color: #c9ccd0; font-weight: normal; }
.divider { height: 1px; background: #e3e5e7; margin: 20px 0 30px 0; }
.editor-container { border: 1px solid #e3e5e7; border-radius: 4px; z-index: 10; }

.video-upload-container { display: flex; flex-direction: column; gap: 20px; }
.upload-box { height: 200px; border: 2px dashed #c9ccd0; border-radius: 8px; background: #fafbfc; display: flex; flex-direction: column; justify-content: center; align-items: center; cursor: pointer; transition: 0.3s; }
.upload-box:hover { border-color: #00aeec; background: #f0faff; }
.icon-upload { font-size: 40px; margin-bottom: 10px; font-style: normal;}
.upload-tip { font-size: 16px; color: #18191c; font-weight: 500; margin: 0 0 5px 0; }
.upload-sub { font-size: 13px; color: #9499a0; margin: 0; }
.video-preview-box { width: 100%; border-radius: 8px; overflow: hidden; background: #000; position: relative; display: flex; justify-content: center; }
.preview-video { max-height: 400px; width: 100%; outline: none; }
.re-upload { position: absolute; top: 15px; right: 15px; background: rgba(0,0,0,0.6); color: #fff; padding: 6px 12px; border-radius: 4px; font-size: 13px; cursor: pointer; transition: 0.3s; }
.re-upload:hover { background: #00aeec; }
.video-desc-input { width: 100%; border: 1px solid #e3e5e7; border-radius: 4px; padding: 15px; font-size: 14px; outline: none; resize: none; font-family: inherit; transition: 0.3s; box-sizing: border-box; }
.video-desc-input:focus { border-color: #00aeec; }
</style>