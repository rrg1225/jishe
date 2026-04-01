<template>
  <div class="im-page-container">
    
    <ChatSidebar 
      :currentChatUser="currentChatUser"
      @select-friend="openChatRoom"
      @friend-deleted="onFriendDeleted"
    />

    <div class="im-chat-pane">
      <template v-if="currentChatUser">
        <div class="im-header">
          <h3>与 {{ currentChatUser.name }} 聊天中</h3>
          <span v-if="isReconnecting" style="color: #ef4444; font-size: 12px; margin-left: 10px;">
            (连接断开，正在尝试重连...)
          </span>
        </div>

        <ChatMessageBoard :messages="wsMessageList" />

        <div v-if="isTyping" class="typing-indicator">
          <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          对方正在输入
        </div>

        <ChatInputBar 
          :disabled="isReconnecting || !currentChatUser"
          @send-text="handleSendText"
          @send-file="handleSendFile"
          @typing="handleTyping" 
        />
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

// 引入三大核心 UI 积木
import ChatSidebar from '../components/chat/ChatSidebar.vue';
import ChatInputBar from '../components/chat/ChatInputBar.vue';
import ChatMessageBoard from '../components/chat/ChatMessageBoard.vue'; 

// 引入底层通信逻辑
import { useWebSocket } from '../composables/useWebSocket';

const myUserId = localStorage.getItem('userId') || "1001"; 
const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL || 'ws://120.27.205.204:8080';
const wsUrl = `${wsBaseUrl}/chat/${myUserId}`;

// 当前选中的聊天对象
const currentChatUser = ref(null);

// ================== WebSocket 核心逻辑 ==================
const { 
  messageList: wsMessageList, 
  isReconnecting, 
  isTyping,             // 🌟 引入输入状态
  initWebSocket, 
  sendMessage: sendWsMessage,
  sendTypingSignal      // 🌟 引入发信令方法
} = useWebSocket(wsUrl, null);

// ================== 业务交互桥梁 ==================

// 事件：点击侧边栏的好友
const openChatRoom = (friend) => {
  currentChatUser.value = friend;
  wsMessageList.value = []; 
  fetchChatHistory(friend.id);
  initWebSocket();
};

// 事件：侧边栏删除了当前好友
const onFriendDeleted = (friendId) => {
  if (currentChatUser.value?.id === friendId) {
    currentChatUser.value = null; 
  }
};

// 🌟 新增：处理本地输入框打字事件，转发给对方
const handleTyping = () => {
  if (currentChatUser.value) {
    sendTypingSignal(currentChatUser.value.id);
  }
};

// 事件：底部栏发送文字
const handleSendText = (text) => {
  sendWsMessage({
    toUserId: currentChatUser.value.id,
    messageType: 'TEXT', 
    content: text
  });
};

// 事件：底部栏发送文件
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
        content: res.data.url,
        fileName: res.data.fileName,
        fileSize: res.data.fileSize
      });
    } else { 
      alert("上传失败：" + res.msg); 
    }
  } catch (error) { 
    console.error("文件上传报错:", error); 
  } 
};

// ================== HTTP 数据请求 ==================
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
  } catch (error) { 
    console.error("获取历史记录失败:", error); 
  }
};
</script>

<style scoped>
.im-page-container { 
  display: flex; 
  width: 100%; 
  height: 100%; 
  background-color: #fff; 
}
.im-chat-pane { 
  flex-grow: 1; 
  display: flex; 
  flex-direction: column; 
  background-color: #f5f7fa; 
  position: relative;
}
.im-header { 
  height: 60px; 
  background-color: #fff; 
  border-bottom: 1px solid #e2e8f0; 
  display: flex; 
  align-items: center; 
  padding: 0 20px; 
}
.im-header h3 { 
  margin: 0; 
  font-size: 16px; 
  color: #1e293b; 
}
.empty-chat-state { 
  display: flex; 
  flex-direction: column; 
  justify-content: center; 
  align-items: center; 
  height: 100%; 
  color: #94a3b8; 
}
.empty-icon { 
  font-size: 60px; 
  margin-bottom: 15px; 
  opacity: 0.5; 
}

/* 🌟 新增：对方正在输入的专属 UI 样式 */
.typing-indicator {
  position: absolute;
  bottom: 75px; /* 悬浮在输入框正上方 */
  left: 20px;
  font-size: 12px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 3px;
  background-color: rgba(255, 255, 255, 0.8);
  padding: 4px 10px;
  border-radius: 12px;
  backdrop-filter: blur(4px);
  z-index: 10;
}
.dot {
  width: 4px;
  height: 4px;
  background-color: #94a3b8;
  border-radius: 50%;
  display: inline-block;
  animation: bounce 1.4s infinite ease-in-out both;
}
.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>