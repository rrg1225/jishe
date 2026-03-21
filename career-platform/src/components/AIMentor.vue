<template>
  <div class="ai-mentor-wrapper">
    <!-- 1. 可拖拽的小精灵容器 -->
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
      <img 
        src="/images11/1e8b234e85fde8ca0482f3fdc3a4225e.png" 
        alt="明理导师"
        class="sprite-img"
        draggable="false"
      />
      
      <!-- 动态眼睛 -->
      <div class="eyes-overlay">
        <div class="eye left-eye"><div class="pupil" :style="leftPupilStyle"></div></div>
        <div class="eye right-eye"><div class="pupil" :style="rightPupilStyle"></div></div>
      </div>
    </div>

    <!-- 2. 对话框主体 -->
    <div 
      class="ai-chat-window" 
      v-show="isChatOpen"
      :style="chatWindowStyle"
    >
      <!-- 头部 -->
      <div class="ai-chat-header">
        <div class="header-left">
          <div class="menu-icon-wrapper" @click.stop="isSidebarOpen = !isSidebarOpen">
            <div class="menu-icon">
              <span></span><span></span><span></span>
            </div>
          </div>
          <span class="title">✨ 明理导师</span>
        </div>
        <span class="close-btn" @click.stop="isChatOpen = false">✖</span>
      </div>
      
      <div class="window-body">
        <!-- 左侧：功能性侧边栏 -->
        <aside class="chat-sidebar" :class="{ 'closed': !isSidebarOpen }">
          <div class="sidebar-content">
            <button class="new-chat-btn" @click="createNewSession">
              <span class="plus">+</span> 发起新对话
            </button>

            <div class="history-list">
              <div class="history-label">最近对话</div>
              <div 
                v-for="session in sessions" 
                :key="session.id" 
                :class="['history-item', { 'active-session': currentSessionId === session.id }]"
                @click="switchSession(session.id)"
              >
                {{ session.title }}
              </div>
            </div>
          </div>
        </aside>

        <!-- 右侧：聊天主区域 -->
        <main class="chat-main">
          <div class="chat-messages" ref="chatBoxRef">
            <div 
              v-for="(msg, index) in currentMessages" 
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
              @keyup.enter="handleSend"
              placeholder="描述你的现状..." 
              :disabled="isGenerating"
            />
            <button @click="handleSend" :disabled="isGenerating || !inputText.trim()">
              {{ isGenerating ? '...' : '发送' }}
            </button>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue';

// --- 1. 会话与数据持久化逻辑 ---
const sessions = ref(JSON.parse(localStorage.getItem('mentor_sessions')) || []);
const currentSessionId = ref(localStorage.getItem('mentor_current_id') || '');
const isSidebarOpen = ref(true);
const isChatOpen = ref(false);
const inputText = ref('');
const isGenerating = ref(false);
const chatBoxRef = ref(null);

const defaultMsg = { role: 'ai', content: '你好，我是明理。无论遇到什么学业或未来的困惑，都可以和我说说。' };

// 计算属性：获取当前激活会话的消息列表
const currentMessages = computed(() => {
  const activeSession = sessions.value.find(s => s.id === currentSessionId.value);
  return activeSession ? activeSession.messages : [defaultMsg];
});

// 保存到本地存储
const saveToLocal = () => {
  localStorage.setItem('mentor_sessions', JSON.stringify(sessions.value));
  localStorage.setItem('mentor_current_id', currentSessionId.value);
};

// 创建新会话
const createNewSession = () => {
  const newId = 'sn_' + Date.now();
  const newSession = {
    id: newId,
    title: '新对话',
    messages: [defaultMsg]
  };
  sessions.value.unshift(newSession);
  currentSessionId.value = newId;
  saveToLocal();
};

// 切换会话
const switchSession = (id) => {
  currentSessionId.value = id;
  saveToLocal();
  scrollToBottom();
};

// --- 2. 聊天发送与 AI 对接 ---
const handleSend = async () => {
  const text = inputText.value.trim();
  if (!text || isGenerating.value) return;

  // 如果没有会话，先创建一个
  if (!currentSessionId.value || sessions.value.length === 0) {
    createNewSession();
  }

  const session = sessions.value.find(s => s.id === currentSessionId.value);
  
  // 1. 用户消息入队
  session.messages.push({ role: 'user', content: text });
  
  // 2. 自动更新标题 (如果还是默认标题)
  if (session.title === '新对话') {
    session.title = text.length > 12 ? text.substring(0, 12) + '...' : text;
  }

  inputText.value = '';
  isGenerating.value = true;
  await scrollToBottom();

  // 3. AI 占位消息
  const aiMsg = { role: 'ai', content: '' };
  session.messages.push(aiMsg);

  try {
    const token = localStorage.getItem('token');
    const response = await fetch('http://localhost:8080/api/chat/qwen/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': token ? `Bearer ${token}` : '' },
      body: JSON.stringify({ message: text, userId: localStorage.getItem('userId') || "1001" })
    });

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      
      const chunk = decoder.decode(value);
      const lines = chunk.split('\n');
      
      for (const line of lines) {
        if (line.startsWith('data:')) {
          try {
            const data = JSON.parse(line.substring(5).trim());
            if (data.text === '[DONE]') break;
            // 过滤掉自定义标签
            const cleanText = data.text.replace(/【[^】]*(】|$)/g, '');
            aiMsg.content += cleanText;
            scrollToBottom();
          } catch (e) { /* 解析错误略过 */ }
        }
      }
    }
  } catch (error) {
    aiMsg.content = '网络有点拥挤，请稍后再试～';
  } finally {
    isGenerating.value = false;
    saveToLocal();
  }
};

// --- 3. 拖拽与 UI 交互逻辑 ---
const position = reactive({ x: window.innerWidth - 220, y: window.innerHeight - 300 });
const mousePos = reactive({ x: 0.5, y: 0.5 });
const pupilMaxOffset = 5;

const chatWindowStyle = computed(() => {
  const sidebarW = isSidebarOpen.value ? 200 : 0;
  const chatW = 360 + sidebarW; 
  const chatH = 480;
  const spriteSize = 170;

  let left = position.x + spriteSize + 15;
  let top = position.y - 40;

  if (left + chatW > window.innerWidth) left = position.x - chatW - 15;
  if (top + chatH > window.innerHeight) top = window.innerHeight - chatH - 20;
  if (top < 10) top = 10;
  if (left < 10) left = 10;

  return { 
    position: 'fixed', 
    left: left + 'px', 
    top: top + 'px', 
    width: chatW + 'px', 
    zIndex: 10001,
    transition: 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
  };
});

const leftPupilStyle = computed(() => ({ transform: `translate(${(mousePos.x - 0.5) * 2 * pupilMaxOffset}px, ${(mousePos.y - 0.5) * 2 * pupilMaxOffset}px)` }));
const rightPupilStyle = computed(() => ({ transform: `translate(${(mousePos.x - 0.5) * 2 * pupilMaxOffset}px, ${(mousePos.y - 0.5) * 2 * pupilMaxOffset}px)` }));

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
    position.x = Math.max(0, Math.min(startPosX + dx, window.innerWidth - 170));
    position.y = Math.max(0, Math.min(startPosY + dy, window.innerHeight - 170));
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

onMounted(() => {
  document.addEventListener('mousemove', (e) => {
    mousePos.x = e.clientX / window.innerWidth;
    mousePos.y = e.clientY / window.innerHeight;
  });
  // 初始化检测
  if (sessions.value.length === 0) createNewSession();
});
</script>

<style scoped>
/* 1. 小精灵 */
.draggable-ai-mentor {
  position: fixed; width: 170px; height: 170px;
  cursor: grab; z-index: 9999; filter: drop-shadow(0 8px 15px rgba(0, 0, 0, 0.15));
}
.sprite-img { width: 100%; height: 100%; object-fit: contain; pointer-events: none; }
.eyes-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; }
.eye { position: absolute; width: 15px; height: 15px; background: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.left-eye { left: 46%; top: 34%; }
.right-eye { left: 58%; top: 34%; }
.pupil { width: 6px; height: 6px; background: #333; border-radius: 50%; transition: transform 0.1s linear; }

/* 2. 对话框 */
.ai-chat-window {
  background: white; border-radius: 20px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  display: flex; flex-direction: column; overflow: hidden;
  border: 1px solid #eef2f5;
}

/* 3. 头部 */
.ai-chat-header {
  background: linear-gradient(135deg, #7EC8E0, #5AB0CC);
  color: white; padding: 10px 18px;
  display: flex; justify-content: space-between; align-items: center;
  height: 50px; flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 10px; }
.menu-icon-wrapper {
  padding: 8px; cursor: pointer; border-radius: 6px; transition: background 0.2s;
  display: flex; align-items: center; justify-content: center;
}
.menu-icon-wrapper:hover { background: rgba(255, 255, 255, 0.2); }
.menu-icon { width: 18px; height: 12px; display: flex; flex-direction: column; justify-content: space-between; }
.menu-icon span { width: 100%; height: 2px; background: white; border-radius: 2px; }
.close-btn { cursor: pointer; padding: 5px; font-size: 14px; }

.window-body { display: flex; flex: 1; overflow: hidden; background: #fff; }

/* 4. 侧边栏（浅色） */
.chat-sidebar {
  width: 200px; background: #f8fbff;
  border-right: 1px solid #eef2f5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden; display: flex; flex-direction: column;
}
.chat-sidebar.closed { width: 0; border-right: none; }
.sidebar-content { width: 200px; padding: 15px; box-sizing: border-box; }

.new-chat-btn {
  width: 100%; padding: 10px; background: white;
  color: #5AB0CC; border: 1px solid #5AB0CC; border-radius: 12px;
  cursor: pointer; margin-bottom: 20px; font-size: 13px; font-weight: bold;
  transition: all 0.2s; display: flex; align-items: center; justify-content: center; gap: 8px;
}
.new-chat-btn:hover { background: #f0f9ff; transform: translateY(-1px); }

.history-label { color: #9bb2bb; font-size: 11px; margin-bottom: 10px; padding-left: 5px; font-weight: bold; }
.history-item {
  color: #5a7a8a; padding: 10px; font-size: 13px; border-radius: 8px;
  cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  margin-bottom: 4px; transition: all 0.2s;
}
.history-item:hover { background: #edf4f8; color: #5AB0CC; }
.active-session { background: #edf4f8; color: #5AB0CC; font-weight: bold; border-left: 3px solid #5AB0CC; }

/* 5. 聊天主区 */
.chat-main { flex: 1; display: flex; flex-direction: column; background: #fff; min-width: 360px; }
.chat-messages { height: 370px; padding: 15px; overflow-y: auto; display: flex; flex-direction: column; gap: 12px; background: #fcfdfe; }

.msg-bubble { max-width: 85%; padding: 10px 14px; border-radius: 15px; font-size: 14px; line-height: 1.6; }
.msg-ai { background: #f0f7f9; color: #3e5a67; align-self: flex-start; border-bottom-left-radius: 2px; }
.msg-user { background: #5AB0CC; color: white; align-self: flex-end; border-bottom-right-radius: 2px; }

.chat-input { display: flex; padding: 12px; background: white; border-top: 1px solid #f0f5f8; gap: 8px; }
.chat-input input { flex: 1; padding: 10px 15px; border: 1px solid #e0e9ee; border-radius: 25px; outline: none; }
.chat-input button { background: #5AB0CC; color: white; border: none; padding: 0 18px; border-radius: 20px; cursor: pointer; font-weight: bold; }
.chat-input button:disabled { background: #ccc; }

/* 滚动条美化 */
::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-thumb { background: #d0e0e6; border-radius: 10px; }
</style>