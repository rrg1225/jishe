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

        <div class="message-box" ref="messageBoxRef">
          <div 
            v-for="(msg, index) in wsMessageList" :key="index"
            :class="['message-item', msg.sender === 'me' ? 'message-right' : 'message-left']"
          >
            <div class="message-bubble">
              <template v-if="msg.messageType === 'IMAGE'">
                <img :src="msg.content" class="chat-image" alt="图片" />
              </template>
              <template v-else-if="msg.messageType === 'FILE'">
                <div class="file-card">
                  <a :href="msg.content" target="_blank" class="chat-file-name">📎 {{ msg.fileName || '未知文件' }}</a>
                  <span class="file-size" v-if="msg.fileSize">({{ (msg.fileSize / 1024).toFixed(1) }} KB)</span>
                </div>
              </template>
              <template v-else>{{ msg.content }}</template>
            </div>
          </div>
        </div>

        <ChatInputBar 
          :disabled="isReconnecting || !currentChatUser"
          @send-text="handleSendText"
          @send-file="handleSendFile"
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
import { ref, nextTick } from 'vue';
import request from '../utils/request'; 

// 引入我们的组件与核心通信逻辑
import ChatSidebar from '../components/chat/ChatSidebar.vue';
import ChatInputBar from '../components/chat/ChatInputBar.vue';
import { useWebSocket } from '../composables/useWebSocket';

const myUserId = localStorage.getItem('userId') || "1001"; 
const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080';
const wsUrl = `${wsBaseUrl}/chat/${myUserId}`;

// 核心状态
const currentChatUser = ref(null);
const messageBoxRef = ref(null);

// 辅助方法：平滑滚动到底部
const scrollToBottom = async () => {
  await nextTick();
  if (messageBoxRef.value) messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight;
};

// ================== WebSocket 核心 ==================
const { 
  messageList: wsMessageList, 
  isReconnecting, 
  initWebSocket, 
  sendMessage: sendWsMessage 
} = useWebSocket(wsUrl, scrollToBottom);

// ================== 业务集成事件 ==================

// 侧边栏事件：点击某个好友
const openChatRoom = (friend) => {
  currentChatUser.value = friend;
  wsMessageList.value = []; 
  fetchChatHistory(friend.id);
  initWebSocket(); // 激活WebSocket
};

// 侧边栏事件：删除了当前正在聊天的好友
const onFriendDeleted = (friendId) => {
  if (currentChatUser.value?.id === friendId) {
    currentChatUser.value = null; // 关闭聊天窗口
  }
};

// 底部栏事件：发文本/语音转换后的文本
const handleSendText = (text) => {
  const messageObj = {
    toUserId: currentChatUser.value.id,
    messageType: 'TEXT', 
    content: text
  };
  sendWsMessage(messageObj);
};

// 底部栏事件：发文件/图片
const handleSendFile = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  try {
    const res = await request.post('/api/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' }});
    if (res.code === 200) {
      const fileInfo = res.data; 
      const msgType = file.type.startsWith('image/') ? 'IMAGE' : 'FILE';
      const messageObj = {
        toUserId: currentChatUser.value.id,
        messageType: msgType,
        content: fileInfo.url,
        fileName: fileInfo.fileName,
        fileSize: fileInfo.fileSize
      };
      sendWsMessage(messageObj);
    } else { alert("上传失败：" + res.msg); }
  } catch (error) { console.error("文件上传报错:", error); } 
};

// HTTP 获取历史聊天记录
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
      scrollToBottom();
    }
  } catch (error) { console.error("获取历史记录失败:", error); }
};
</script>

<style scoped>
/* 仅仅保留容器骨架和聊天消息面板的样式 */
.im-page-container { display: flex; width: 100%; height: 100%; background-color: #fff; }
.im-chat-pane { flex-grow: 1; display: flex; flex-direction: column; background-color: #f5f7fa; }
.im-header { height: 60px; background-color: #fff; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; padding: 0 20px; }
.im-header h3 { margin: 0; font-size: 16px; color: #1e293b; }
.message-box { flex-grow: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 15px; scroll-behavior: smooth; }
.message-item { display: flex; width: 100%; }
.message-left { justify-content: flex-start; }
.message-left .message-bubble { background-color: #fff; border: 1px solid #e2e8f0; color: #1e293b; }
.message-right { justify-content: flex-end; }
.message-right .message-bubble { background-color: #4a90e2; color: white; }
.message-bubble { max-width: 60%; padding: 10px 15px; border-radius: 8px; font-size: 14px; line-height: 1.5; word-wrap: break-word; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.chat-image { max-width: 200px; border-radius: 6px; cursor: pointer; display: block; }
.file-card { display: flex; flex-direction: column; gap: 5px; }
.chat-file-name { color: inherit; text-decoration: none; font-weight: bold; word-break: break-all; }
.chat-file-name:hover { text-decoration: underline; }
.file-size { font-size: 12px; opacity: 0.8; }
.empty-chat-state { display: flex; flex-direction: column; justify-content: center; align-items: center; height: 100%; color: #94a3b8; }
.empty-icon { font-size: 60px; margin-bottom: 15px; opacity: 0.5; }
</style>