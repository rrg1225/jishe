<template>
  <div class="ai-mentor-wrapper">
    <!-- 1. 悬浮小精灵 -->
    <div 
      class="draggable-ai-mentor" 
      ref="mentorRef"
      :style="{ left: position.x + 'px', top: position.y + 'px' }"
      @mousedown="startDrag"
      @click="toggleChat"
    >
      <!-- 修正了 Vite/Vue 标准的静态资源引用路径 -->
      <img 
        src="/images11/下载.png" 
        alt="明理导师"
        class="sprite-img"
        draggable="false"
      />
      <!-- 为新机器人定制的发光眼睛 -->
      <div class="eyes-overlay">
        <div class="eye left-eye"><div class="pupil" :style="leftPupilStyle"></div></div>
        <div class="eye right-eye"><div class="pupil" :style="rightPupilStyle"></div></div>
      </div>
    </div>

    <!-- 2. 对话框外壳 -->
    <div class="ai-chat-window" v-show="isChatOpen" :style="chatWindowStyle">
      <!-- 头部 -->
      <div class="ai-chat-header">
        <div class="header-left">
          <div class="menu-icon-wrapper" @click.stop="toggleSidebar">
            <div class="menu-icon">
              <span></span><span></span><span></span>
            </div>
          </div>
          <span class="title">✨ 明理导师</span>
        </div>
        <span class="close-btn" @click.stop="closeChat">✖</span>
      </div>
      
      <div class="window-body">
        
        <!-- 3. 左侧：历史记录栏 -->
        <aside class="chat-sidebar" :class="{ 'closed': !isSidebarOpen }">
          <div class="sidebar-content">
            <button class="new-chat-btn" @click="createNewSession">
              <span class="plus">+</span> 发起新对话
            </button>

            <div class="history-container" @scroll="closeDropdown">
              <div class="history-label">历史对话</div>
              
              <!-- 历史会话列表 -->
              <div 
                v-for="session in sortedSessions" 
                :key="session.id" 
                :class="['history-item', { 'active-session': currentSessionId === session.id }]"
                @click="switchSession(session.id)"
                @mouseenter="hoveredSessionId = session.id"
                @mouseleave="hoveredSessionId = null"
              >
                <!-- 左侧图标 -->
                <svg class="item-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                </svg>

                <!-- 标题 or 重命名输入框 -->
                <input 
                  v-if="editingSessionId === session.id" 
                  v-model="editingTitle" 
                  class="rename-input"
                  ref="renameInputRef"
                  @blur="saveRename(session)" 
                  @keyup.enter="saveRename(session)" 
                  @click.stop 
                />
                <span v-else class="session-title" :title="session.title">{{ session.title }}</span>

                <!-- 置顶图标 -->
                <svg v-if="session.isPinned && hoveredSessionId !== session.id && activeDropdownId !== session.id" class="pin-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-6.16 6.16a2 2 0 0 1-2.83-2.83l6.16-6.16"></path>
                </svg>

                <!-- 更多按钮 -->
                <div 
                  v-show="(hoveredSessionId === session.id || activeDropdownId === session.id) && editingSessionId !== session.id"
                  class="more-btn-wrapper"
                  @click.stop="toggleDropdown(session.id, $event)"
                >
                  <svg class="more-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="1"></circle><circle cx="19" cy="12" r="1"></circle><circle cx="5" cy="12" r="1"></circle>
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </aside>

        <!-- 4. 右侧：主聊天区 -->
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

    <!-- 5. 全局悬浮菜单 -->
    <div 
      v-if="activeDropdownId && isChatOpen" 
      class="session-dropdown" 
      :style="{ top: dropdownPos.y + 'px', left: dropdownPos.x + 'px' }"
      @click.stop
    >
      <div class="dropdown-item" @click.stop="togglePin(activeDropdownId)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-6.16 6.16a2 2 0 0 1-2.83-2.83l6.16-6.16"></path></svg> 
        {{ getSessionById(activeDropdownId)?.isPinned ? '取消置顶' : '置顶' }}
      </div>
      <div class="dropdown-item disabled">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"></path><polyline points="16 6 12 2 8 6"></polyline><line x1="12" y1="2" x2="12" y2="15"></line></svg> 
        分享
      </div>
      <div class="dropdown-item" @click.stop="startRename(activeDropdownId)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"></path><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path></svg> 
        重命名
      </div>
      <div class="dropdown-item disabled">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg> 
        举报
      </div>
      <div class="dropdown-item danger" @click.stop="deleteSession(activeDropdownId)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg> 
        删除
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue';

// ==========================================
// 1. 会话与数据持久化逻辑 (Local Storage)
// ==========================================
const sessions = ref(JSON.parse(localStorage.getItem('mentor_sessions')) || []);
const currentSessionId = ref(localStorage.getItem('mentor_current_id') || '');
const isSidebarOpen = ref(true);
const isChatOpen = ref(false);
const inputText = ref('');
const isGenerating = ref(false);
const chatBoxRef = ref(null);

const defaultMsg = { role: 'ai', content: '你好，我是明理。无论遇到什么学业或未来的困惑，都可以和我说说。' };

const getSessionById = (id) => sessions.value.find(s => s.id === id);

const sortedSessions = computed(() => {
  return [...sessions.value].sort((a, b) => {
    if (a.isPinned === b.isPinned) return 0;
    return a.isPinned ? -1 : 1;
  });
});

const currentMessages = computed(() => {
  const activeSession = getSessionById(currentSessionId.value);
  return activeSession ? activeSession.messages : [defaultMsg];
});

const saveToLocal = () => {
  localStorage.setItem('mentor_sessions', JSON.stringify(sessions.value));
  localStorage.setItem('mentor_current_id', currentSessionId.value);
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatBoxRef.value) {
    chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight;
  }
};

const createNewSession = () => {
  const newId = 'sn_' + Date.now();
  const newSession = {
    id: newId,
    title: '新对话',
    isPinned: false, 
    messages: [defaultMsg]
  };
  sessions.value.unshift(newSession);
  currentSessionId.value = newId;
  saveToLocal();
  scrollToBottom();
};

const switchSession = (id) => {
  currentSessionId.value = id;
  saveToLocal();
  scrollToBottom();
};

// ==========================================
// 1.5. 悬浮菜单与操作逻辑
// ==========================================
const hoveredSessionId = ref(null);
const activeDropdownId = ref(null);
const dropdownPos = reactive({ x: 0, y: 0 });

const editingSessionId = ref(null);
const editingTitle = ref('');
const renameInputRef = ref(null);

const toggleDropdown = (id, e) => {
  if (activeDropdownId.value === id) {
    activeDropdownId.value = null;
    return;
  }
  activeDropdownId.value = id;
  const rect = e.currentTarget.getBoundingClientRect();
  dropdownPos.x = rect.left + 25; 
  dropdownPos.y = rect.bottom + 5;
};

const closeDropdown = () => {
  activeDropdownId.value = null;
};

const togglePin = (id) => {
  const session = getSessionById(id);
  if (session) {
    session.isPinned = !session.isPinned;
    saveToLocal();
  }
  closeDropdown();
};

const deleteSession = (id) => {
  sessions.value = sessions.value.filter(s => s.id !== id);
  if (currentSessionId.value === id) {
    if (sessions.value.length > 0) {
      currentSessionId.value = sessions.value[0].id;
    } else {
      createNewSession();
    }
  }
  saveToLocal();
  closeDropdown();
};

const startRename = async (id) => {
  const session = getSessionById(id);
  if (session) {
    editingSessionId.value = id;
    editingTitle.value = session.title;
    closeDropdown();
    await nextTick();
    if (renameInputRef.value && renameInputRef.value[0]) {
      renameInputRef.value[0].focus();
    }
  }
};

const saveRename = (session) => {
  if (editingTitle.value.trim()) {
    session.title = editingTitle.value.trim();
    saveToLocal();
  }
  editingSessionId.value = null;
};

// ==========================================
// 2. 聊天发送与 AI 对接 (SSE 流式输出)
// ==========================================
const handleSend = async () => {
  const text = inputText.value.trim();
  if (!text || isGenerating.value) return;

  if (!currentSessionId.value || sessions.value.length === 0) createNewSession();

  const session = getSessionById(currentSessionId.value);
  session.messages.push({ role: 'user', content: text });
  
  if (session.title === '新对话') {
    session.title = text.length > 12 ? text.substring(0, 12) + '...' : text;
  }

  inputText.value = '';
  isGenerating.value = true;
  await scrollToBottom();

  const aiMsg = { role: 'ai', content: '' };
  session.messages.push(aiMsg);

  try {
    const token = localStorage.getItem('token');
    const myUserId = localStorage.getItem('userId') || "1001";
    
    const response = await fetch('http://localhost:8080/api/chat/qwen/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': token ? `Bearer ${token}` : '' },
      body: JSON.stringify({ message: text, userId: myUserId })
    });

    if (!response.ok) throw new Error('网络请求失败');

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      
      buffer += decoder.decode(value, { stream: true });
      let lines = buffer.split('\n');
      buffer = lines.pop() || '';
      
      for (const line of lines) {
        if (line.trim().startsWith('data:')) {
          try {
            const dataStr = line.substring(5).trim();
            if (!dataStr) continue;

            const data = JSON.parse(dataStr);
            if (data.text === '[DONE]') break;
            
            const cleanText = data.text.replace(/【[^】]*(】|$)/g, '');
            aiMsg.content += cleanText;
            scrollToBottom();
          } catch (e) {}
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

// ==========================================
// 3. 拖拽与 UI 交互逻辑 (动态计算)
// ==========================================
const mentorRef = ref(null);
const position = reactive({ x: window.innerWidth - 220, y: window.innerHeight - 300 });
const mousePos = reactive({ x: 0.5, y: 0.5 });
const pupilMaxOffset = 5;

const chatWindowStyle = computed(() => {
  const sidebarW = isSidebarOpen.value ? 230 : 0;
  const chatW = 400 + sidebarW; 
  const chatH = 540;
  const spriteWidth = 180; 

  let left = position.x + spriteWidth + 15;
  let top = position.y - 40;

  if (left + chatW > window.innerWidth) left = position.x - chatW - 15;
  if (top + chatH > window.innerHeight) top = window.innerHeight - chatH - 20;
  if (top < 10) top = 10;
  if (left < 10) left = 10;

  return { 
    position: 'fixed', left: left + 'px', top: top + 'px', 
    width: chatW + 'px', height: chatH + 'px', zIndex: 10001,
    transition: 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
  };
});

const leftPupilStyle = computed(() => ({ transform: `translate(${(mousePos.x - 0.5) * 2 * pupilMaxOffset}px, ${(mousePos.y - 0.5) * 2 * pupilMaxOffset}px)` }));
const rightPupilStyle = computed(() => ({ transform: `translate(${(mousePos.x - 0.5) * 2 * pupilMaxOffset}px, ${(mousePos.y - 0.5) * 2 * pupilMaxOffset}px)` }));

let hasMoved = false;
let startX = 0, startY = 0, startPosX = 0, startPosY = 0;

const startDrag = (e) => {
  e.stopPropagation(); hasMoved = false;
  startX = e.clientX; startY = e.clientY;
  startPosX = position.x; startPosY = position.y;
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', stopDrag);
};
const onDrag = (e) => {
  const dx = e.clientX - startX, dy = e.clientY - startY;
  if (Math.abs(dx) > 5 || Math.abs(dy) > 5) hasMoved = true;
  if (hasMoved) {
    position.x = Math.max(0, Math.min(startPosX + dx, window.innerWidth - 180));
    position.y = Math.max(0, Math.min(startPosY + dy, window.innerHeight - 200));
  }
};
const stopDrag = () => {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
};

const closeChat = () => {
  isChatOpen.value = false;
  closeDropdown();
};

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value;
  closeDropdown();
};

const toggleChat = () => {
  if (!hasMoved) {
    isChatOpen.value = !isChatOpen.value;
    if (isChatOpen.value) {
      scrollToBottom();
    } else {
      closeDropdown();
    }
  }
};

const updateMousePosition = (e) => {
  mousePos.x = e.clientX / window.innerWidth;
  mousePos.y = e.clientY / window.innerHeight;
};

onMounted(() => {
  document.addEventListener('mousemove', updateMousePosition);
  document.addEventListener('click', closeDropdown); 
  if (sessions.value.length === 0) createNewSession();
});

onUnmounted(() => {
  document.removeEventListener('mousemove', updateMousePosition);
  document.removeEventListener('click', closeDropdown);
});
</script>

<style scoped>
/* 1. 小精灵图片与专门为新机器人定制的眼睛 */
.draggable-ai-mentor {
  position: fixed; 
  width: 180px; 
  height: 200px;
  cursor: grab; 
  z-index: 9999; 
  filter: drop-shadow(0 8px 15px rgba(0, 0, 0, 0.15));
}
.sprite-img { 
  width: 100%; 
  height: 100%; 
  object-fit: contain; 
  pointer-events: none; 
}
.eyes-overlay { 
  position: absolute; 
  top: 0; 
  left: 0; 
  width: 100%; 
  height: 100%; 
  pointer-events: none; 
}
/* 遮挡原护目镜的暗色眼眶 */
.eye { 
  position: absolute; 
  width: 24px; 
  height: 14px; 
  background: #0a0a0a; /* 与机器人黑色护目镜融为一体 */
  border-radius: 50%; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  overflow: hidden; 
}
/* 调整坐标，刚好卡在机器人的护目镜中心位置 */
.left-eye { left: 37%; top: 25%; }
.right-eye { left: 53.5%; top: 25%; }

/* 青蓝色发光的机器瞳孔 */
.pupil { 
  position: absolute;
  width: 8px; 
  height: 8px; 
  background: #00e5ff; /* 科技感青蓝色 */
  border-radius: 50%; 
  box-shadow: 0 0 8px #00e5ff, 0 0 12px #00e5ff; /* 发光特效 */
  transition: transform 0.05s linear; 
}

/* 2. 对话框外壳 */
.ai-chat-window {
  background: white; border-radius: 12px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  display: flex; flex-direction: column; overflow: hidden;
  border: 1px solid #eef2f5;
}

/* 3. 头部导航栏 */
.ai-chat-header {
  background: #64b7ce;
  color: white; padding: 12px 18px;
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 10px; }
.menu-icon-wrapper { padding: 4px; cursor: pointer; border-radius: 6px; display: flex; align-items: center; }
.menu-icon { width: 18px; height: 12px; display: flex; flex-direction: column; justify-content: space-between; }
.menu-icon span { width: 100%; height: 2px; background: white; border-radius: 2px; }
.title { font-size: 16px; font-weight: bold; }
.close-btn { cursor: pointer; padding: 5px; font-size: 16px; transition: transform 0.2s; }

/* 弹窗主体（分左右） */
.window-body { display: flex; flex: 1; overflow: hidden; background: #fff; }

/* =======================================
   4. 左侧边栏
   ======================================= */
.chat-sidebar {
  width: 230px; background: #fafafa;
  border-right: 1px solid #eef2f5;
  transition: width 0.3s ease;
  display: flex; flex-direction: column;
}
.chat-sidebar.closed { width: 0; border-right: none; }
.sidebar-content { width: 230px; height: 100%; display: flex; flex-direction: column; box-sizing: border-box; }

.new-chat-btn {
  margin: 15px; padding: 10px; background: white;
  color: #64b7ce; border: 1px solid #64b7ce; border-radius: 8px;
  cursor: pointer; font-size: 14px; font-weight: bold;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  flex-shrink: 0;
}
.new-chat-btn:hover { background: #f0f9ff; }

.history-container { flex: 1; overflow-y: auto; padding: 0 10px 10px 10px; }
.history-label { color: #a1a1aa; font-size: 12px; margin: 10px 0 8px 5px; }

.history-item {
  display: flex; align-items: center; padding: 10px 12px;
  border-radius: 8px; cursor: pointer; position: relative;
  color: #4b5563; transition: background 0.2s; margin-bottom: 2px;
}
.history-item:hover { background: #f1f5f9; }
.history-item.active-session { background: #f0f9ff; color: #0ea5e9; font-weight: 500; }

.item-icon { width: 16px; height: 16px; margin-right: 10px; flex-shrink: 0; opacity: 0.6; }
.session-title { flex: 1; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.rename-input {
  flex: 1; width: 100%; border: none; outline: 1px solid #64b7ce;
  border-radius: 4px; padding: 2px 6px; font-size: 13px;
  font-family: inherit; background: white;
}

.pin-icon { width: 14px; height: 14px; color: #94a3b8; flex-shrink: 0; margin-left: 5px; }

.more-btn-wrapper {
  width: 24px; height: 24px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.2s; flex-shrink: 0; margin-left: 5px;
}
.more-btn-wrapper:hover { background: #e2e8f0; color: #1e293b; }
.more-icon { width: 16px; height: 16px; }

/* =======================================
   5. 悬浮菜单 Dropdown
   ======================================= */
.session-dropdown {
  position: fixed; width: 140px; background: white;
  border-radius: 8px; padding: 6px 0; z-index: 10005;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12); border: 1px solid #f1f5f9;
  animation: dropFade 0.15s ease-out;
}
@keyframes dropFade { from { opacity: 0; transform: translateY(-5px); } to { opacity: 1; transform: translateY(0); } }

.dropdown-item {
  padding: 10px 16px; font-size: 13px; color: #334155;
  cursor: pointer; display: flex; align-items: center; gap: 10px;
  transition: background 0.2s;
}
.dropdown-item svg { width: 14px; height: 14px; opacity: 0.8; }
.dropdown-item:hover { background: #f8fafc; color: #0ea5e9; }
.dropdown-item.disabled { color: #cbd5e1; cursor: not-allowed; }
.dropdown-item.disabled:hover { background: transparent; color: #cbd5e1; }
.dropdown-item.danger { color: #ef4444; }
.dropdown-item.danger:hover { background: #fef2f2; color: #dc2626; }


/* =======================================
   6. 聊天主区
   ======================================= */
.chat-main { flex: 1; display: flex; flex-direction: column; background: #fff; min-width: 360px; }
.chat-messages { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; background: #fff; }

.msg-bubble { max-width: 85%; padding: 12px 16px; border-radius: 12px; font-size: 14px; line-height: 1.6; white-space: pre-wrap; word-break: break-word;}
.msg-ai { background: #f4f4f5; color: #374151; align-self: flex-start; border-bottom-left-radius: 2px; }
.msg-user { background: #64b7ce; color: white; align-self: flex-end; border-bottom-right-radius: 2px; }

.chat-input { display: flex; padding: 15px; background: white; border-top: 1px solid #f0f5f8; gap: 10px; align-items: center;}
.chat-input input { flex: 1; padding: 12px 18px; border: 1px solid #e2e8f0; border-radius: 25px; outline: none; font-size: 14px; background: #f8fafc; transition: 0.2s;}
.chat-input input:focus { border-color: #64b7ce; background: #fff; box-shadow: 0 0 0 3px rgba(100,183,206,0.1); }
.chat-input button { background: #64b7ce; color: white; border: none; padding: 10px 20px; border-radius: 20px; cursor: pointer; font-weight: bold; font-size: 14px; transition: 0.2s;}
.chat-input button:disabled { background: #cbd5e1; cursor: not-allowed; }
.chat-input button:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 10px rgba(100,183,206,0.3); }

/* 滚动条美化 */
::-webkit-scrollbar { width: 5px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
</style>