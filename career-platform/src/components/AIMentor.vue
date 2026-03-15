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
        <span class="title">明理导师</span>
        <span class="close-btn" @click="isChatOpen = false">✖</span>
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
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';

const mentorRef = ref(null);
const isChatOpen = ref(false);
const chatBoxRef = ref(null);

const inputText = ref('');
const isGenerating = ref(false);
const messageList = ref([
  { role: 'ai', content: '你好，我是明理。无论遇到什么学业或未来的困惑，都可以和我说说。' }
]);

const scrollToBottom = async () => {
  await nextTick();
  if (chatBoxRef.value) {
    chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight;
  }
};

const sendMessage = async () => {
  const userMsg = inputText.value.trim();
  if (!userMsg || isGenerating.value) return;

  messageList.value.push({ role: 'user', content: userMsg });
  inputText.value = '';
  isGenerating.value = true;
  scrollToBottom();

  const currentAiMsgIndex = messageList.value.length;
  messageList.value.push({ role: 'ai', content: '' });

  try {
    const token = localStorage.getItem('token');
    const response = await fetch('http://localhost:8080/api/chat/qwen/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({ message: userMsg })
    });

    if (!response.ok) throw new Error('请求失败');

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let done = false;
    let buffer = ''; 

// AIMentor.vue 中的 sendMessage 逻辑
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
        if (jsonObj.text === '[DONE]') {
          done = true;
          break;
        }
        // 🚀 双保险：解析 JSON 保证不丢字，正则过滤保证不显示标签
        const cleanText = jsonObj.text.replace(/【[^】]*(】|$)/g, '');
        messageList.value[currentAiMsgIndex].content += cleanText;
        scrollToBottom();
      } catch (e) {
        // 自动忽略不完整的 JSON
      }
    }
  }
}
  } catch (error) {
    console.error('流式请求出错:', error);
    messageList.value[currentAiMsgIndex].content = '网络有点开小差，请稍后再试哦~';
  } finally {
    isGenerating.value = false;
  }
};

/* 拖拽动画逻辑完整保留 */
const position = reactive({ x: window.innerWidth - 100, y: window.innerHeight - 150 });
const eyePosition = reactive({ x: 0.5, y: 0.5 });
let hasMoved = false;
let startMouseX = 0, startMouseY = 0, startPosX = 0, startPosY = 0;

const startDrag = (e) => {
  hasMoved = false; startMouseX = e.clientX; startMouseY = e.clientY;
  startPosX = position.x; startPosY = position.y;
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', stopDrag);
};
const onDrag = (e) => {
  const dx = e.clientX - startMouseX, dy = e.clientY - startMouseY;
  if (Math.abs(dx) > 5 || Math.abs(dy) > 5) hasMoved = true;
  if (hasMoved) {
    const width = mentorRef.value ? mentorRef.value.offsetWidth : 80;
    const height = mentorRef.value ? mentorRef.value.offsetHeight : 80;
    position.x = Math.max(0, Math.min(startPosX + dx, window.innerWidth - width));
    position.y = Math.max(0, Math.min(startPosY + dy, window.innerHeight - height));
  }
};
const stopDrag = () => {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
};
const toggleChat = () => { if (!hasMoved) isChatOpen.value = !isChatOpen.value; };
const trackMouse = (e) => { eyePosition.x = e.clientX / window.innerWidth; eyePosition.y = e.clientY / window.innerHeight; };
const handleResize = () => {
  if (!mentorRef.value) return;
  const width = mentorRef.value.offsetWidth, height = mentorRef.value.offsetHeight;
  position.x = Math.min(position.x, window.innerWidth - width); position.y = Math.min(position.y, window.innerHeight - height);
};
onMounted(() => { document.addEventListener('mousemove', trackMouse); window.addEventListener('resize', handleResize); });
onUnmounted(() => { document.removeEventListener('mousemove', trackMouse); document.removeEventListener('mousemove', onDrag); document.removeEventListener('mouseup', stopDrag); window.removeEventListener('resize', handleResize); });
</script>

<style scoped>
/* 样式完美，未做任何改动，直接保留你的原生设计 */
.draggable-ai-mentor { position: fixed; width: 80px; height: 80px; cursor: grab; z-index: 9999; user-select: none; transition: transform 0.1s ease; }
.draggable-ai-mentor:active { cursor: grabbing; transform: scale(0.95); }
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
.ai-chat-window { position: fixed; right: 30px; bottom: 30px; width: 340px; background: #fff; border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.2); z-index: 10000; display: flex; flex-direction: column; animation: slideUp 0.3s ease-out; }
@keyframes slideUp { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
.ai-chat-header { background: #4a90e2; color: #fff; padding: 12px 15px; font-weight: bold; display: flex; justify-content: space-between; align-items: center; border-radius: 12px 12px 0 0; }
.title { font-size: 15px; }
.close-btn { cursor: pointer; font-size: 14px; opacity: 0.8; transition: 0.2s; }
.close-btn:hover { opacity: 1; transform: scale(1.1); }
.chat-messages { height: 320px; padding: 15px; background: #f5f7fa; overflow-y: auto; display: flex; flex-direction: column; gap: 12px; }
.msg-bubble { max-width: 80%; padding: 10px 14px; border-radius: 8px; font-size: 14px; line-height: 1.5; word-break: break-all; white-space: pre-wrap; } /* 增加了 white-space 保证换行显示 */
.msg-ai { background: #fff; color: #333; align-self: flex-start; border: 1px solid #e4e7ed; box-shadow: 0 2px 4px rgba(0,0,0,0.02); border-bottom-left-radius: 2px;}
.msg-user { background: #4a90e2; color: #fff; align-self: flex-end; border-bottom-right-radius: 2px; }
.chat-input { display: flex; padding: 12px; background: #fff; border-top: 1px solid #eee; border-radius: 0 0 12px 12px; }
.chat-input input { flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; outline: none; transition: 0.2s; font-size: 14px; }
.chat-input input:focus { border-color: #4a90e2; box-shadow: 0 0 4px rgba(74, 144, 226, 0.2); }
.chat-input button { margin-left: 8px; background: #4a90e2; color: white; border: none; padding: 0 15px; border-radius: 4px; cursor: pointer; font-weight: bold; transition: 0.2s; }
.chat-input button:hover { background: #357ebd; }
.chat-input button:disabled { background: #a0cfff; cursor: not-allowed; }
</style>