<template>
  <div class="im-page-container">
    <ChatSidebar :currentChatUser="currentChatUser" @select-friend="openChatRoom" @friend-deleted="onFriendDeleted" />

    <div class="im-chat-pane">
      <template v-if="currentChatUser">
        <div class="im-header">
          <h3>与 {{ currentChatUser.name }} 聊天中</h3>
          <span v-if="isReconnecting" style="color: #ef4444; font-size: 12px; margin-left: 10px;">(连接断开，正在尝试重连...)</span>
        </div>

        <ChatMessageBoard :messages="wsMessageList" />

        <div v-if="isTyping" class="typing-indicator">
          <span class="dot"></span><span class="dot"></span><span class="dot"></span>对方正在输入
        </div>

        <ChatInputBar :disabled="isReconnecting || !currentChatUser" @send-text="handleSendText" @send-file="handleSendFile" @typing="handleTyping" />
      </template>

      <div v-else class="empty-chat-state">
        <div class="empty-icon">💬</div>
        <p>请在左侧选择联系人开始聊天</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import request from '../utils/request'; 
import ChatSidebar from '../components/chat/ChatSidebar.vue';
import ChatInputBar from '../components/chat/ChatInputBar.vue';
import ChatMessageBoard from '../components/chat/ChatMessageBoard.vue'; 
import { useWebSocket } from '../composables/useWebSocket';

const myUserId = localStorage.getItem('userId') || "1001"; 
const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL || 'ws://120.27.205.204:8080';
const wsUrl = `${wsBaseUrl}/chat/${myUserId}`;
const currentChatUser = ref(null);

const { messageList: wsMessageList, isReconnecting, isTyping, initWebSocket, sendMessage: sendWsMessage, sendTypingSignal } = useWebSocket(wsUrl, null);

const openChatRoom = async (friend) => {
  currentChatUser.value = friend;
  wsMessageList.value = []; 
  fetchChatHistory(friend.id);
  try {
      const res = await request.get('/api/chat/dispatcher');
      if (res.code === 200) initWebSocket(`${res.data}/chat/${myUserId}`);
  } catch (e) {
      initWebSocket(wsUrl);
  }
};

const onFriendDeleted = (friendId) => { if (currentChatUser.value?.id === friendId) currentChatUser.value = null; };
const handleTyping = () => { if (currentChatUser.value) sendTypingSignal(currentChatUser.value.id); };

const handleSendText = (text) => {
  sendWsMessage({ toUserId: currentChatUser.value.id, messageType: 'TEXT', content: text });
};

// 🌟 核心：发送文件时，把原图和缩略图打包成 JSON 发给 WebSocket
const handleSendFile = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  try {
    const res = await request.post('/api/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' }});
    if (res.code === 200) {
      const msgType = file.type.startsWith('image/') ? 'IMAGE' : 'FILE';
      
      sendWsMessage({
        toUserId: currentChatUser.value.id,
        messageType: msgType,
        // 🌟 将高清原图和缩略图包装成 JSON 字符串存入 content
        content: msgType === 'IMAGE' ? JSON.stringify({
          original: res.data.url,
          thumb: res.data.thumbnailUrl
        }) : res.data.url,
        fileName: res.data.fileName,
        fileSize: res.data.fileSize
      });
    } else { 
      alert("上传失败：" + res.msg); 
    }
  } catch (error) { console.error("文件上传报错:", error); } 
};

const fetchChatHistory = async (targetId) => {
  try {
    const res = await request.get(`/api/chat/history?userId=${myUserId}&otherId=${targetId}`);
    const historyData = res.data || res; 
    if (Array.isArray(historyData)) {
      wsMessageList.value = historyData.map(msg => ({
        sender: String(msg.senderId) === String(myUserId) ? 'me' : 'other',
        messageType: msg.messageType || 'TEXT',
        content: msg.content,
        fileName: msg.fileName,
        fileSize: msg.fileSize
      }));
    }
  } catch (error) { console.error("获取历史记录失败:", error); }
};
</script>

<style scoped>
.im-page-container { display: flex; width: 100%; height: 100%; background-color: #fff; }
.im-chat-pane { flex-grow: 1; display: flex; flex-direction: column; background-color: #f5f7fa; position: relative;}
.im-header { height: 60px; background-color: #fff; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; padding: 0 20px; }
.im-header h3 { margin: 0; font-size: 16px; color: #1e293b; }
.empty-chat-state { display: flex; flex-direction: column; justify-content: center; align-items: center; height: 100%; color: #94a3b8; }
.empty-icon { font-size: 60px; margin-bottom: 15px; opacity: 0.5; }
.typing-indicator { position: absolute; bottom: 75px; left: 20px; font-size: 12px; color: #64748b; display: flex; align-items: center; gap: 3px; background-color: rgba(255, 255, 255, 0.8); padding: 4px 10px; border-radius: 12px; backdrop-filter: blur(4px); z-index: 10; }
.dot { width: 4px; height: 4px; background-color: #94a3b8; border-radius: 50%; display: inline-block; animation: bounce 1.4s infinite ease-in-out both; }
.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes bounce { 0%, 80%, 100% { transform: scale(0); } 40% { transform: scale(1); } }
</style>
🟢 5. 前端：修改 ChatMessageBoard.vue (渲染气泡)
文件路径：src/components/chat/ChatMessageBoard.vue
因为现在的 msg.content 变成了一个 JSON 字符串，直接放进 <img> 会报错，必须把它解析出来再渲染！

请完全替换该组件代码：

代码段
<template>
  <div class="message-board-container" ref="boardRef">
    <div class="message-list">
      <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.sender === 'me' ? 'msg-me' : 'msg-other']">
        <div class="avatar">{{ msg.sender === 'me' ? '我' : 'Ta' }}</div>
        
        <div class="bubble">
          <template v-if="msg.messageType === 'TEXT' || !msg.messageType">
            {{ msg.content }}
          </template>

          <template v-else-if="msg.messageType === 'IMAGE'">
            <img 
              :src="getThumb(msg.content)" 
              class="chat-image" 
              title="点击查看高清原图" 
              @click="openOriginalImage(getOriginal(msg.content))" 
            />
          </template>

          <template v-else-if="msg.messageType === 'FILE'">
            <div class="file-box">
              <span class="file-icon">📄</span>
              <a :href="msg.content" target="_blank" class="file-link">{{ msg.fileName || '文件下载' }}</a>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';

const props = defineProps({
  messages: { type: Array, default: () => [] }
});

const boardRef = ref(null);

// 自动滚动到底部
watch(() => props.messages, () => {
  nextTick(() => {
    if (boardRef.value) {
      boardRef.value.scrollTop = boardRef.value.scrollHeight;
    }
  });
}, { deep: true });

// 🌟 解析图片 JSON 字符串（兼容以前的旧数据）
const parseImageContent = (contentStr) => {
  try {
    const data = JSON.parse(contentStr);
    return data;
  } catch (e) {
    // 如果解析失败，说明是以前的老历史记录（直接是一段 URL 字符串）
    return { thumb: contentStr, original: contentStr };
  }
};

const getThumb = (content) => parseImageContent(content).thumb;
const getOriginal = (content) => parseImageContent(content).original;

// 新开页面查看高清原图
const openOriginalImage = (url) => {
  window.open(url, '_blank');
};
</script>

<style scoped>
.message-board-container { flex-grow: 1; padding: 20px; overflow-y: auto; background-color: #f8fafc; }
.message-list { display: flex; flex-direction: column; gap: 15px; }
.message-item { display: flex; align-items: flex-start; max-width: 80%; }
.msg-me { align-self: flex-end; flex-direction: row-reverse; }
.msg-other { align-self: flex-start; }
.avatar { width: 36px; height: 36px; border-radius: 50%; background-color: #3b82f6; color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; margin: 0 10px; flex-shrink: 0; }
.msg-me .avatar { background-color: #10b981; }

.bubble { padding: 10px 14px; border-radius: 8px; font-size: 14px; line-height: 1.5; word-break: break-word; }
.msg-me .bubble { background-color: #10b981; color: white; border-top-right-radius: 2px; }
.msg-other .bubble { background-color: white; color: #333; border: 1px solid #e2e8f0; border-top-left-radius: 2px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }

/* 图片气泡样式 */
.chat-image { max-width: 200px; max-height: 200px; border-radius: 6px; cursor: pointer; object-fit: cover; border: 1px solid #e2e8f0; transition: opacity 0.2s; }
.chat-image:hover { opacity: 0.85; }

.file-box { display: flex; align-items: center; gap: 8px; background-color: rgba(0,0,0,0.05); padding: 8px; border-radius: 4px; }
.file-icon { font-size: 20px; }
.file-link { color: inherit; text-decoration: underline; word-break: break-all; }
</style>