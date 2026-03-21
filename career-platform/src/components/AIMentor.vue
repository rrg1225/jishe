<template>
  <div class="ai-mentor-wrapper">
    <!-- 可拖拽的小精灵容器 -->
    <div 
      class="draggable-ai-mentor" 
      ref="mentorRef"
      :style="{ 
        left: position.x + 'px', 
        top: position.y + 'px' 
      }"
      @mousedown="startDrag"
      @click="toggleChat"
    >
      <!-- 精灵图片 -->
      <img 
        src="/images11/1e8b234e85fde8ca0482f3fdc3a4225e.png" 
        alt="明理导师"
        class="sprite-img"
        draggable="false"
      />
      
      <!-- 动态眼睛图层 -->
      <div class="eyes-overlay">
        <div class="eye left-eye">
          <div class="pupil" :style="leftPupilStyle"></div>
        </div>
        <div class="eye right-eye">
          <div class="pupil" :style="rightPupilStyle"></div>
        </div>
      </div>
    </div>

    <!-- 对话框 - 实时跟随小精灵坐标 -->
    <div 
      class="ai-chat-window" 
      v-show="isChatOpen"
      :style="chatWindowStyle"
    >
      <div class="ai-chat-header">
        <span class="title">✨ 明理导师</span>
        <span class="close-btn" @click.stop="isChatOpen = false">✖</span>
      </div>
      
      <div class="chat-messages" ref="chatBoxRef">
        <div 
          v-for="(msg, index) in messageList" 
          :key="index" 
          :class="['msg-bubble', msg.role === 'user' ? 'msg-user' : 'msg-ai']"
        >
          {{ msg.content }}
        </div>
      </div>

      <div class="chat-input">
        <input 
          type="text" 
          v-model="inputText" 
          @keyup.enter="sendMessage"
          placeholder="描述你的现状..." 
          :disabled="isGenerating"
        />
        <button @click="sendMessage" :disabled="isGenerating || !inputText.trim()">
          {{ isGenerating ? '...' : '发送' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue';

// --- 基础状态 ---
const mentorRef = ref(null);
const isChatOpen = ref(false);
const chatBoxRef = ref(null);
const inputText = ref('');
const isGenerating = ref(false);
const messageList = ref([]);
const myUserId = localStorage.getItem('userId') || "1001";

// --- 小精灵位置（初始位置） ---
const position = reactive({ x: window.innerWidth - 220, y: window.innerHeight - 300 });
const mousePos = reactive({ x: 0.5, y: 0.5 });
const pupilMaxOffset = 4;

// 瞳孔样式
const leftPupilStyle = computed(() => ({
  transform: `translate(${(mousePos.x - 0.5) * 2 * pupilMaxOffset}px, ${(mousePos.y - 0.5) * 2 * pupilMaxOffset}px)`
}));
const rightPupilStyle = computed(() => ({
  transform: `translate(${(mousePos.x - 0.5) * 2 * pupilMaxOffset}px, ${(mousePos.y - 0.5) * 2 * pupilMaxOffset}px)`
}));

// --- 核心修改：聊天框实时跟随计算属性 ---
const chatWindowStyle = computed(() => {
  const chatW = 360; // 聊天框宽度
  const chatH = 480; // 聊天框高度
  const spriteSize = 170; // 当前精灵大小

  // 1. 基础定位：默认在精灵右侧
  let left = position.x + spriteSize + 15;
  let top = position.y - 40; // 稍微往上一点，显得更自然

  // 2. 边界检查：如果右边放不下了，就弹到精灵左边
  if (left + chatW > window.innerWidth) {
    left = position.x - chatW - 15;
  }

  // 3. 垂直边界检查：确保不会飞出屏幕上下方
  if (top + chatH > window.innerHeight) top = window.innerHeight - chatH - 20;
  if (top < 10) top = 10;
  if (left < 10) left = 10;

  return { 
    position: 'fixed', 
    left: left + 'px', 
    top: top + 'px', 
    width: chatW + 'px', 
    zIndex: 10001,
    // 添加微量过渡，让跟随不那么僵硬（可选）
    transition: 'none' 
  };
});

// --- 业务逻辑：历史记录与发送 ---
const fetchMentorHistory = async () => {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/chat/history?userId=${myUserId}&otherId=0`, {
      headers: { 'Authorization': token ? `Bearer ${token}` : '' }
    });
    if (response.ok) {
      const data = await response.json();
      const defaultMsg = { role: 'ai', content: '你好，我是明理。无论遇到什么学业或未来的困惑，都可以和我说说。' };
      if (Array.isArray(data) && data.length > 0) {
        const historyMsgs = data.map(msg => ({
          role: String(msg.senderId) === '0' ? 'ai' : 'user',
          content: msg.content
        }));
        messageList.value = [defaultMsg, ...historyMsgs];
      } else {
        messageList.value = [defaultMsg];
      }
      scrollToBottom();
    }
  } catch (error) {
    messageList.value = [{ role: 'ai', content: '你好，我是明理。' }];
  }
};

const sendMessage = async () => {
  const userMsg = inputText.value.trim();
  if (!userMsg || isGenerating.value) return;
  messageList.value.push({ role: 'user', content: userMsg });
  inputText.value = '';
  isGenerating.value = true;
  await scrollToBottom();
  const currentAiMsgIndex = messageList.value.length;
  messageList.value.push({ role: 'ai', content: '' });

  try {
    const token = localStorage.getItem('token');
    const response = await fetch('http://localhost:8080/api/chat/qwen/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': token ? `Bearer ${token}` : '' },
      body: JSON.stringify({ message: userMsg, userId: myUserId })
    });
    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let done = false;
    let buffer = ''; 
    while (!done) {
      const { value, done: readerDone } = await reader.read();
      done = readerDone;
      if (value) {
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop(); 
        for (const line of lines) {
          if (!line.startsWith('data:')) continue;
          const dataStr = line.substring(5).trim(); 
          try {
            const jsonObj = JSON.parse(dataStr);
            if (jsonObj.text === '[DONE]') { done = true; break; }
            const cleanText = jsonObj.text.replace(/【[^】]*(】|$)/g, '');
            messageList.value[currentAiMsgIndex].content += cleanText;
            scrollToBottom();
          } catch (e) {}
        }
      }
    }
  } catch (error) {
    messageList.value[currentAiMsgIndex].content = '网络出错啦~';
  } finally {
    isGenerating.value = false;
  }
};

// --- 拖拽交互逻辑 ---
let hasMoved = false;
let startX = 0, startY = 0, startPosX = 0, startPosY = 0;

const startDrag = (e) => {
  e.stopPropagation();
  hasMoved = false;
  startX = e.clientX; startY = e.clientY;
  startPosX = position.x; startPosY = position.y;
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', stopDrag);
};

const onDrag = (e) => {
  const dx = e.clientX - startX, dy = e.clientY - startY;
  if (Math.abs(dx) > 5 || Math.abs(dy) > 5) hasMoved = true;
  if (hasMoved) {
    const w = 170; // 对应 CSS 里的放大尺寸
    const h = 170;
    position.x = Math.max(0, Math.min(startPosX + dx, window.innerWidth - w));
    position.y = Math.max(0, Math.min(startPosY + dy, window.innerHeight - h));
  }
};

const stopDrag = () => {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
};

const toggleChat = () => {
  if (!hasMoved) {
    isChatOpen.value = !isChatOpen.value;
    if (isChatOpen.value) scrollToBottom();
  }
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatBoxRef.value) chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight;
};

const trackMouse = (e) => {
  mousePos.x = e.clientX / window.innerWidth;
  mousePos.y = e.clientY / window.innerHeight;
};

onMounted(() => {
  fetchMentorHistory();
  document.addEventListener('mousemove', trackMouse);
  window.addEventListener('resize', () => {
    position.x = Math.min(position.x, window.innerWidth - 170);
    position.y = Math.min(position.y, window.innerHeight - 170);
  });
});
</script>

<style scoped>
/* 小精灵容器 - 已保持 170px 放大比例 */
.draggable-ai-mentor {
  position: fixed;
  width: 170px; 
  height: 170px;
  cursor: grab;
  z-index: 9999;
  user-select: none;
  filter: drop-shadow(0 8px 15px rgba(0, 0, 0, 0.15));
}

.draggable-ai-mentor:active {
  cursor: grabbing;
  transform: scale(0.96);
}

.sprite-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  pointer-events: none;
}

/* 眼睛定位 */
.eyes-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  pointer-events: none;
}

.eye {
  position: absolute;
  width: 15px;
  height: 15px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: inset 0 1px 2px rgba(0,0,0,0.2);
}

/* 修正后的眼睛高度 */
.left-eye { left: 46%; top: 34%; }
.right-eye { left: 58%; top: 34%; }

.pupil {
  width: 6px;
  height: 6px;
  background: #333;
  border-radius: 50%;
  transition: transform 0.1s linear;
}

/* 对话框样式 */
.ai-chat-window {
  background: white;
  border-radius: 20px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #eef2f5;
  /* 去掉这里的全局 transition 确保移动时不延迟 */
}

.ai-chat-header {
  background: linear-gradient(135deg, #7EC8E0, #5AB0CC);
  color: white;
  padding: 14px 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-messages {
  height: 380px;
  padding: 15px;
  background: #fcfdfe;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.msg-bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 15px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.msg-ai { background: #f0f7f9; color: #3e5a67; align-self: flex-start; border-bottom-left-radius: 2px; }
.msg-user { background: #5AB0CC; color: white; align-self: flex-end; border-bottom-right-radius: 2px; }

.chat-input { display: flex; padding: 12px; background: white; border-top: 1px solid #f0f0f0; gap: 8px; }
.chat-input input { flex: 1; padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 25px; outline: none; }
.chat-input button { background: #5AB0CC; color: white; border: none; padding: 0 18px; border-radius: 20px; cursor: pointer; font-weight: bold; }
</style>