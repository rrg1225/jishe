我非常理解你现在的顾虑。从截图来看，打印预览页面的底部确实没有铺满 A4 纸，而是被拦腰截断，并透出了背后的紫色装饰线条（如 image_1.png）。

这是因为我们在之前的代码中，雖然調整了 .preview-container 的高度，但如果你的这个组件仍然嵌套在一个限制了高度（如 height: 100vh; overflow: hidden;）的外部父级容器中（比如包含侧边栏或顶部导航的父 Layout），它在打印时依然会受到限制，导致无法自然分页。

为了终极解决这两个问题（打印截断和 Canve 配色参考），我为你重新设计了 CSS 方案：

打印排版终极释放：在媒体查询 @media print 中，暴力清除所有 DOM 节点（不仅是预览容器，还追溯到 html, body, #app）的 height 和 overflow 限制，确保内容能自然分页，并彻底隐藏不需要打印的按钮和装饰物，彻底清除底部的透底内容。

简历配色方案重构 (参考 Canva image_8.png)：将原先通透浅色（珍珠白/米灰色）的玻璃拟态 A4 纸样式，改为深灰+纯白的双色布局，侧边栏采用 Canva 风格的深灰色（#4F525A），主内容区域采用纯白（#FFFFFF），并配合柔和的米灰色（#F4F4F0）背景拉开层次，整体更显质感。

以下是具体的优化代码。你可以直接复制替换你的 ResumePreview.vue。

ResumePreview.vue 完整优化代码
代码段
<template>
  <div class="preview-container dark-gray-theme">
    
    <div class="bg-decorations">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <svg class="lines" viewBox="0 0 100 100" preserveAspectRatio="none">
        <path d="M0,20 L100,80 M0,50 L100,50 M0,80 L100,20" stroke="rgba(0,0,0,0.03)" stroke-width="0.5" fill="none"/>
      </svg>
    </div>

    <div class="toolbar glass-panel">
      <div class="actions">
        <el-button size="medium" plain @click="$router.back()" class="glass-btn btn-morandi-blue">
          <i class="el-icon-back"></i> 返回修改条件
        </el-button>
        <el-button size="medium" @click="exportPDF" class="glass-btn btn-morandi-green">
          <i class="el-icon-printer"></i> 导出 PDF / 打印
        </el-button>
      </div>
      <p class="hint">💡 提示：点击下方简历内容的任意文字，可直接在线修改！</p>
    </div>

    <div class="resume-scroll-wrapper">
      <div class="a4-glass-frame glass-panel">
        <div class="a4-page" id="resume-content">
          <div class="resume-dual-column" contenteditable="true" @blur="handleBlur">
            
            <div class="left-sidebar">
             <img src="./public/9(1)(1).jpg" alt="头像" class="avatar" v-if="hasAvatar">
              <div class="loading-avatar" v-else>头像</div>
              <div class="name">张晨果</div>
              <div class="position">前端开发实习生</div>
              <div class="info-section">
                <h3>基本信息</h3>
                <ul>
                  <li><i class="el-icon-school"></i> 毕业院校：2028级毕业生</li>
                  <li><i class="el-icon-user"></i> 年龄：21岁</li>
                  <li><i class="el-icon-setting"></i> 专业方向：前端开发</li>
                </ul>
              </div>
            </div>

            <div class="main-content markdown-theme-blue" v-html="renderedResume"></div>
            
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'

const router = useRouter()
const rawMarkdown = ref('')
const hasAvatar = ref(false) // 这里需要根据实际情况获取头像，这里假设没有头像，显示加载头像占位

// 计算属性：实时将 Markdown 转换为 HTML
const renderedResume = computed(() => {
  if (!rawMarkdown.value) return '<div class="loading">⏳ 大模型正在奋笔疾书中，请稍候...</div>'
  return marked(rawMarkdown.value)
})

// 页面加载时，从本地存储获取 AI 生成的数据
onMounted(() => {
  const savedData = localStorage.getItem('generatedResume')
  if (savedData) {
    rawMarkdown.value = savedData
  } else {
    rawMarkdown.value = '# 暂无简历数据\n请返回首页重新生成。'
  }
})

// 失去焦点时保存修改的内容（可选功能）
const handleBlur = (event) => {
  // console.log('修改后的内容:', event.target.innerHTML);
}

// 简单的导出 PDF 方法（调用浏览器打印）
const exportPDF = () => {
  window.print()
}
</script>

<style scoped>
/* =========================================
   🌐 网页端 UI 样式 (浅色系背景 + 玻璃拟态)
========================================= */

/* 1. 最外层容器：负责背景、居中和页面滚动 */
.preview-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #F4F4F0; 
  background-image: linear-gradient(120deg, #fdfbfb 0%, #ebedee 100%);
  
  /* 🌟 核心修改：将 min-height 改为 height，强制锁定视口高度，内部才会出现滚动条 */
  height: 100vh; 
  width: 100%;
  overflow-y: auto; 
  overflow-x: hidden;
  
  padding: 30px 0;
  box-sizing: border-box;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  z-index: 1;
}

/* 2. 背景装饰线条 (适配浅色) */
.bg-decorations {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  z-index: -1; 
  overflow: hidden;
}
.circle { position: absolute; border-radius: 50%; filter: blur(40px); opacity: 0.6; }
/* 莫兰蒂浅蓝紫点缀 */
.circle-1 { width: 400px; height: 400px; top: -100px; left: -100px; background: rgba(163, 177, 182, 0.2); }
/* 莫兰蒂浅灰绿点缀 */
.circle-2 { width: 500px; height: 500px; bottom: -150px; right: -150px; background: rgba(181, 196, 177, 0.2); }
.lines { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }

/* 3. 通用玻璃拟态面板公用类 (浅色系高斯模糊) */
.glass-panel {
  /* 半透明纯白，透出底层背景色 */
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px); 
  
  /* 细微的白色高光边框 */
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  /* 柔和的浅色阴影 */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
}

/* 4. 顶部工具栏样式 */
.toolbar {
  width: 210mm; 
  padding: 15px 25px;
  margin-bottom: 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
  flex-shrink: 0; 
}
.actions { display: flex; gap: 15px; }
.hint { color: #A88F78; font-size: 14px; margin: 0; font-weight: 500; }

/* =========================================
   🎨 莫兰蒂 + 玻璃拟态 按钮专属样式
========================================= */
.glass-btn {
  border-radius: 10px !important;
  font-weight: 600 !important;
  color: #5C6B73 !important; /* 深蓝灰字体，保证对比度 */
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(10px) !important;
  -webkit-backdrop-filter: blur(10px) !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03) !important;
}

/* 莫兰蒂蓝灰 (返回按钮) */
.btn-morandi-blue {
  background: rgba(163, 177, 182, 0.35) !important;
}
.btn-morandi-blue:hover {
  background: rgba(163, 177, 182, 0.5) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(163, 177, 182, 0.3) !important;
}

/* 莫兰蒂豆绿 (打印按钮) */
.btn-morandi-green {
  background: rgba(181, 196, 177, 0.4) !important;
  color: #4A5D4E !important; /* 加深一点绿色字体 */
}
.btn-morandi-green:hover {
  background: rgba(181, 196, 177, 0.6) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(181, 196, 177, 0.3) !important;
}

/* 5. 简历滚动区域包装 */
.resume-scroll-wrapper {
  display: flex;
  justify-content: center;
  width: 100%;
  padding-bottom: 50px; 
  flex: 1; 
}

/* 6. A4 纸张的外围玻璃框 (模仿立体框架感) */
.a4-glass-frame {
  width: 210mm; 
  padding: 15px; 
  box-sizing: border-box;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  height: max-content; 
}

/* =========================================
   📄 🌟 核心：参考 Canva (image_8.png) 的简历双色布局 🌟
========================================= */
/* 7. 真正的 A4 纸张 (白纸部分) */
.a4-page {
  width: 100%; 
  background: #FFFFFF; /* 简历主体使用纯白 */
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  border-radius: 4px; 
  overflow: hidden; /* 防止内容超出白纸范围 */
  box-shadow: 0 2px 10px rgba(0,0,0,0.02); /* 给白纸加一点微弱的浮现感 */
}

/* A4 纸张内部布局 */
.resume-dual-column {
  display: flex;
  min-height: 297mm; /* 保证 A4 高度 */
  outline: none; /* 去除点击时的蓝色外框 */
}

/* 左侧侧边栏 (深灰色) */
.left-sidebar {
  width: 250px; /* 固定宽度 */
  background-color: #4F525A; /* 🌟 核心：Canva image_8.png 的深灰色 */
  color: #FFFFFF; /* 🌟 核心：白色字体，保证可读性 */
  padding: 30mm 15mm 15mm 15mm; /* A4 的内边距标准调整 */
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.8);
  margin-bottom: 15px;
}
.loading-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
  color: rgba(255,255,255,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 13px;
  margin-bottom: 15px;
}

.name {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}
.position {
  font-size: 14px;
  color: rgba(255,255,255,0.7);
  margin-bottom: 30px;
}

.info-section {
  width: 100%;
  text-align: left;
}
.info-section h3 {
  font-size: 16px;
  font-weight: bold;
  border-bottom: 2px solid rgba(255,255,255,0.2);
  padding-bottom: 5px;
  margin-top: 0;
  margin-bottom: 15px;
}
.info-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.info-section li {
  font-size: 13px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}
.info-section li i {
  margin-right: 8px;
  color: rgba(255,255,255,0.6);
}

/* 右侧主内容 (纯白色) */
.main-content {
  flex: 1; /* 填充剩余空间 */
  background-color: #FFFFFF; /* 🌟 核心：Canva image_8.png 的白色 */
  color: #333; /* 🌟 核心：深蓝灰/黑色字体 */
  padding: 30mm 20mm; /* A4 的标准内边距 */
  box-sizing: border-box;
}

.loading { text-align: center; color: #888; padding: 100px 0; font-size: 16px; }

/* =========================================
   📄 深度定制的 Markdown 内容 CSS (保留幻主简历蓝调风格)
========================================= */
.markdown-theme-blue { color: #333; line-height: 1.8; font-size: 14px; }
.markdown-theme-blue :deep(h1) { font-size: 30px; color: #2c3e50; font-weight: bold; margin-top: 0; margin-bottom: 10px; padding-bottom: 8px; }
.markdown-theme-blue :deep(h2) { font-size: 18px; color: #284c71; font-weight: bold; border-bottom: 2px solid #284c71; padding-bottom: 6px; margin-top: 28px; margin-bottom: 16px; display: flex; align-items: center; }
.markdown-theme-blue :deep(h2)::before { content: ''; display: inline-block; width: 6px; height: 18px; background: #284c71; margin-right: 10px; border-radius: 2px; }
.markdown-theme-blue :deep(h3) { font-size: 15px; color: #333; font-weight: bold; margin-top: 10px; margin-bottom: 6px; display: flex; justify-content: space-between; }
.markdown-theme-blue :deep(h3 span) { color: #7f8c8d; font-weight: normal; font-size: 13px; }
.markdown-theme-blue :deep(ul) { padding-left: 20px; margin-top: 6px; margin-bottom: 10px; list-style-type: disc; }
.markdown-theme-blue :deep(li) { margin-bottom: 6px; color: #555; font-size: 13.5px; }
.markdown-theme-blue :deep(p) { margin: 0 0 10px 0; color: #555; font-size: 13.5px; }

/* =========================================
   🖨 "🌟 终极解决方案：解决打印截断/透底问题 🌟
========================================= */
@media print {
  /* 🌟 核心修复 2：彻底禁用 Flex 布局 (Flex 会导致浏览器打印截断) */
  html, body, #app, 
  .preview-container, 
  .resume-scroll-wrapper, 
  .a4-glass-frame, 
  .a4-page {
    display: block !important; /* 🌟 强制块级元素，允许分页截断 */
    position: static !important;
    height: auto !important;
    min-height: auto !important;
    overflow: visible !important;
    background: transparent !important;
    padding: 0 !important;
    margin: 0 !important;
  }
  
  /* 隐藏装饰和工具栏 */
  .bg-decorations, .toolbar { display: none !important; }
  
  /* 抹除外边框玻璃的占位 */
  .a4-glass-frame {
    width: 100% !important;
    border: none !important;
    box-shadow: none !important;
    backdrop-filter: none !important;
    -webkit-backdrop-filter: none !important;
  }
  
  /* 抹除简历纸张的玻璃透明效果，恢复纯白打印 */
  .a4-page {
    width: 100% !important;
    max-width: none !important;
    background: #FFFFFF !important; 
    border: none !important;
    border-radius: 0 !important;
    box-shadow: none !important;
  }
  
  /* 🌟 核心修复 3：去掉浏览器默认打印边距，让内容彻底铺满 */
  @page {
    size: A4 portrait;
    margin: 0; 
  }
  
  /* 调整简历内部的打印边距 */
  .resume-body {
    padding: 0 !important; /* 边距交给下方的 .left-sidebar 和 .main-content 控制 */
    width: 100% !important;
  }
  
  /* 调整双色布局的打印边距 */
  .resume-dual-column {
    display: block !important; /* 打印时不能用 Flex */
  }
  
  .left-sidebar {
    width: 100% !important; /* 打印时侧边栏占满宽度 */
    padding: 10mm 20mm !important; /* 打印内边距 */
    page-break-after: avoid; /* 侧边栏后不断页 */
  }
  
  .main-content {
    width: 100% !important;
    padding: 0 20mm 15mm 20mm !important; /* 打印内边距，去除顶部边距，因为侧边栏占了 */
  }
  
  .markdown-theme-blue h2, .markdown-theme-blue h3 { page-break-after: avoid; }
  .markdown-theme-blue li { page-break-inside: avoid; }
}

/* =========================================
   🖨️ 打印完美铺满终极方案 (修复双栏排版与背景色丢失)
========================================= */
@media print {
  /* 1. 释放外层高度束缚 */
  html, body, #app, 
  .preview-container, 
  .resume-scroll-wrapper, 
  .a4-glass-frame, 
  .a4-page {
    height: auto !important;
    min-height: auto !important;
    overflow: visible !important;
    background: transparent !important;
    padding: 0 !important;
    margin: 0 !important;
    display: block !important;
  }
  
  /* 2. 隐藏无关元素 */
  .bg-decorations, .toolbar { display: none !important; }
  
  /* 3. 去除纸张的玻璃阴影 */
  .a4-glass-frame {
    width: 100% !important;
    border: none !important;
    box-shadow: none !important;
    backdrop-filter: none !important;
    -webkit-backdrop-filter: none !important;
  }
  .a4-page {
    width: 100% !important;
    box-shadow: none !important;
    border: none !important;
    border-radius: 0 !important;
  }

  /* 🌟 核心修复 4：恢复双列布局，不再上下堆叠 */
  .resume-dual-column {
    display: flex !important; /* 恢复左右并排 */
    width: 100% !important;
    height: auto !important;
  }

  /* 🌟 核心修复 5：强制打印深灰色背景！ */
  .left-sidebar {
    width: 250px !important; /* 保持侧边栏宽度 */
    background-color: #4F525A !important;
    color: #FFFFFF !important;
    padding: 20mm 15mm !important;
    /* 下面两行是让浏览器强制打印背景色和图片的“魔法指令” */
    -webkit-print-color-adjust: exact !important; 
    print-color-adjust: exact !important;
  }

  /* 主内容区域边距调整 */
  .main-content {
    flex: 1 !important;
    background-color: #FFFFFF !important;
    padding: 20mm 20mm !important;
    -webkit-print-color-adjust: exact !important;
    print-color-adjust: exact !important;
  }
  
  /* 🌟 核心修复 6：清空物理页边距，让我们的 padding 接管排版 */
  @page {
    size: A4 portrait;
    margin: 0; 
  }
  
  .markdown-theme-blue h2, .markdown-theme-blue h3 { page-break-after: avoid; }
  .markdown-theme-blue li { page-break-inside: avoid; }
}
</style>