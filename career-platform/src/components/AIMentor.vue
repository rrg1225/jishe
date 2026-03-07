<template>
  <div class="ai-mentor-wrapper">
    <div 
      class="draggable-ai-mentor" 
      ref="mentorRef"
      :style="{ 
        left: position.x + 'px', 
        top: position.y + 'px', 
        '--px': eyePosition.x, 
        '--py': eyePosition.y 
      }"
      @mousedown="startDrag"
      @click="toggleChat"
    >
      <figure class="boi">
        <div class="boi-blush boi-blush-left"></div>
        <div class="boi-blush boi-blush-right"></div>
        <div class="boi-eye boi-eye-left"></div>
        <div class="boi-eye boi-eye-right"></div>
        <div class="boi-mouth"></div>
      </figure>
    </div>

    <div class="ai-chat-window" v-show="isChatOpen">
      <div class="ai-chat-header">
        <span class="title">🤖 伴生型 AI 导师</span>
        <span class="close-btn" @click="isChatOpen = false">✖</span>
      </div>
      <div class="chat-messages">
        <p class="ai-msg">你好！我是你的专属AI导师。无论是学业规划还是简历修改，我都能为你提供专业的指导。</p>
      </div>
      <div class="chat-input">
        <input type="text" placeholder="描述你的现状..." />
        <button>发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';

const mentorRef = ref(null);
const isChatOpen = ref(false);

// 初始化位置，放在右下角
const position = reactive({
  x: window.innerWidth - 100, 
  y: window.innerHeight - 150
});

// 眼睛位置映射
const eyePosition = reactive({ x: 0.5, y: 0.5 });

// 拖拽与点击的状态区分
let hasMoved = false;
let startMouseX = 0;
let startMouseY = 0;
let startPosX = 0;
let startPosY = 0;

const startDrag = (e) => {
  hasMoved = false; // 每次按下鼠标时，重置移动状态
  startMouseX = e.clientX;
  startMouseY = e.clientY;
  startPosX = position.x;
  startPosY = position.y;
  
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', stopDrag);
};

const onDrag = (e) => {
  const dx = e.clientX - startMouseX;
  const dy = e.clientY - startMouseY;
  
  // 核心逻辑：如果鼠标移动超过 5 像素，判定为“拖拽行为”而不是“点击”
  if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
    hasMoved = true;
  }
  
  // 只有在判定为拖拽时，才改变图标位置
  if (hasMoved) {
    const width = mentorRef.value ? mentorRef.value.offsetWidth : 80;
    const height = mentorRef.value ? mentorRef.value.offsetHeight : 80;
    
    // 边界碰撞检测，防止拖出屏幕边缘
    position.x = Math.max(0, Math.min(startPosX + dx, window.innerWidth - width));
    position.y = Math.max(0, Math.min(startPosY + dy, window.innerHeight - height));
  }
};

const stopDrag = () => {
  // 鼠标抬起时，移除全局监听
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
};

// 专门处理点击事件
const toggleChat = () => {
  // 如果没有发生拖拽（只是单纯的点了一下），就切换聊天窗口的显示状态
  if (!hasMoved) {
    isChatOpen.value = !isChatOpen.value;
  }
};

// 眼球跟随鼠标的逻辑
const trackMouse = (e) => {
  eyePosition.x = e.clientX / window.innerWidth;
  eyePosition.y = e.clientY / window.innerHeight;
};

const handleResize = () => {
  if (!mentorRef.value) return;
  const width = mentorRef.value.offsetWidth;
  const height = mentorRef.value.offsetHeight;
  position.x = Math.min(position.x, window.innerWidth - width);
  position.y = Math.min(position.y, window.innerHeight - height);
};

onMounted(() => {
  document.addEventListener('mousemove', trackMouse);
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  document.removeEventListener('mousemove', trackMouse);
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
/* =========== 悬浮圆脸样式 =========== */
.draggable-ai-mentor {
  position: fixed;
  width: 80px;
  height: 80px;
  cursor: grab;
  z-index: 9999;
  user-select: none; /* 防止拖拽时选中了页面文本 */
  transition: transform 0.1s ease;
}
.draggable-ai-mentor:active {
  cursor: grabbing;
  transform: scale(0.95);
}
.boi { width: 100%; height: 100%; background-image: radial-gradient(#f7e0b2, #eb5); border-radius: 100%; overflow: hidden; margin: 0; border: 2px solid #ecb23e; box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.2); position: relative; box-sizing: border-box; }
.boi * { box-sizing: border-box; }
.boi-blush { position: absolute; width: 20%; height: 10%; background-color: rgba(255, 100, 100, 0.3); border: 3px solid rgba(255, 100, 100, 0.3); top: calc(45% + var(--py) * 10%); border-radius: 100%; }
.boi-blush-left { left: calc(7% + var(--px) * 2%); }
.boi-blush-right { right: calc(9% - var(--px) * 2%); }
.boi-eye { position: absolute; width: 24%; height: 24%; background-color: #f6f6f6; border-radius: 100%; top: calc(25% + var(--py) * 10%); overflow: hidden; }
.boi-eye-left { left: calc(18% + var(--px) * 4%); }
.boi-eye-right { right: calc(22% - var(--px) * 4%); }
.boi-eye::after { content: ''; display: block; background-color: #421; width: 45%; height: 45%; border-radius: 100%; }
.boi-eye-left::after { transform: translate(calc(var(--px) * 100%), calc(var(--py) * 100%)); }
.boi-eye-right::after { transform: translate(calc(var(--px) * 100%), calc(var(--py) * 100%)); }
.boi-mouth { position: absolute; width: 49%; height: 24%; background-color: #a33; border-radius: 0 0 16em 16em; top: calc(57.5% + var(--py) * 5%); left: calc(47.5% + var(--px) * 5%); transform: translateX(-50%); overflow: hidden; border: 3px solid #962d2d; }
.boi-mouth::before { content: ''; display: block; position: absolute; width: 20%; height: 20%; top: 0; left: 50%; background-color: white; border-radius: 0 0 0.5rem 0.5rem; transform: translateX(-50%); }
.boi-mouth::after { content: ''; display: block; position: absolute; width: 60%; height: 50%; left: 20%; bottom: 0; background-color: rgba(0, 0, 0, 0.2); border-radius: 20rem 20rem 0 0; }

/* =========== 聊天窗口样式 =========== */
.ai-chat-window {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 320px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  z-index: 10000; /* 提升层级，确保弹窗不会被覆盖 */
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease-out;
}
@keyframes slideUp { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
.ai-chat-header { background: #4a90e2; color: #fff; padding: 12px 15px; font-weight: bold; display: flex; justify-content: space-between; align-items: center; border-radius: 12px 12px 0 0; }
.title { font-size: 15px; }
.close-btn { cursor: pointer; font-size: 14px; opacity: 0.8; transition: 0.2s; }
.close-btn:hover { opacity: 1; transform: scale(1.1); }
.chat-messages { height: 280px; padding: 15px; background: #f9f9f9; overflow-y: auto; }
.ai-msg { background: #e6f0fa; padding: 12px; border-radius: 8px; font-size: 14px; color: #333; line-height: 1.5; margin: 0; }
.chat-input { display: flex; padding: 12px; background: #fff; border-top: 1px solid #eee; border-radius: 0 0 12px 12px; }
.chat-input input { flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; outline: none; transition: 0.2s; }
.chat-input input:focus { border-color: #4a90e2; box-shadow: 0 0 4px rgba(74, 144, 226, 0.3); }
.chat-input button { margin-left: 8px; background: #4a90e2; color: white; border: none; padding: 0 15px; border-radius: 4px; cursor: pointer; font-weight: bold; transition: 0.2s; }
.chat-input button:hover { background: #357ebd; }
</style>