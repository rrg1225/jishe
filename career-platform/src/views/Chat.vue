<template>
  <div class="im-page-container">
    
    <div class="im-list-pane">
      <div class="search-area">
        <input v-model="searchId" type="text" placeholder="输入对方ID加好友..." class="search-input" />
        <button @click="sendFriendRequest" class="add-btn">申请</button>
      </div>

      <div class="pending-requests" v-if="pendingRequests.length > 0">
        <div class="section-title">新朋友请求 ({{ pendingRequests.length }})</div>
        <div v-for="req in pendingRequests" :key="req.id" class="request-item">
          <div class="req-info">
            <span class="req-name">用户_{{ req.senderId }}</span>
            <span class="req-desc">请求加你为好友</span>
          </div>
          <button @click="acceptFriend(req.id)" class="accept-btn">同意</button>
        </div>
      </div>

      <div class="friend-list">
        <div class="section-title">我的联系人</div>
        <div 
          v-for="friend in friendList" 
          :key="friend.id" 
          class="friend-item"
          :class="{ 'active-friend': currentChatUser?.id === friend.id }"
          @click="openChatRoom(friend)"
        >
          <div class="friend-avatar">{{ friend.name.charAt(0) }}</div>
          <div class="friend-info">
            <div class="friend-name">{{ friend.name }}</div>
            <div class="friend-status" :class="friend.isOnline ? 'online' : 'offline'">
              {{ friend.isOnline ? '[在线]' : '[离线]' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="im-chat-pane">
      <template v-if="currentChatUser">
        <div class="im-header">
          <h3>与 {{ currentChatUser.name }} 聊天中</h3>
        </div>

        <div class="message-box">
          <div 
            v-for="(msg, index) in messageList" 
            :key="index"
            :class="['message-item', msg.sender === 'me' ? 'message-right' : 'message-left']"
          >
            <div class="message-bubble">
              
              <template v-if="msg.messageType === 'IMAGE'">
                <img :src="msg.content" class="chat-image" alt="图片" />
              </template>
              
              <template v-else-if="msg.messageType === 'FILE'">
                <div class="file-card">
                  <a :href="msg.content" target="_blank" class="chat-file-name">
                    📎 {{ msg.fileName || '未知文件' }}
                  </a>
                  <span class="file-size" v-if="msg.fileSize">
                    ({{ (msg.fileSize / 1024).toFixed(1) }} KB)
                  </span>
                </div>
              </template>
              
              <template v-else>
                {{ msg.content }}
              </template>

            </div>
          </div>
        </div>

        <div class="input-area">
          <input 
            type="file" 
            ref="fileInput" 
            @change="handleFileUpload" 
            style="display: none" 
            accept="image/*, .pdf, .doc, .docx, .zip" 
          />
          <button @click="triggerUpload" class="upload-btn" title="发送图片或文件">➕</button>
          
          <input 
            v-model="inputText" 
            type="text" 
            placeholder="输入消息..." 
            @keyup.enter="sendMessage"
            class="chat-input"
          />
          <button @click="sendMessage" class="send-btn">发送</button>
        </div>
      </template>

      <div v-else class="empty-chat-state">
        <div class="empty-icon">💬</div>
        <p>请在左侧选择联系人开始聊天</p>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import request from '../utils/request'; 

const currentChatUser = ref(null);
const friendList = ref([]); 
const searchId = ref('');   

const messageList = ref([]);
const inputText = ref('');
let socket = null;
const pendingRequests = ref([]);

const fileInput = ref(null);

const myUserId = localStorage.getItem('userId') || "1001"; 
const wsUrl = `ws://localhost:8080/chat/${myUserId}`;

// ================== HTTP 请求部分 (好友管理) ==================

// 获取好友列表
const fetchFriendList = async () => {
  try {
    const res = await request.get(`/api/friend/list?userId=${myUserId}`);
    if (res.code === 200) {
      friendList.value = res.data;
    }
  } catch (error) {
    console.error("获取好友列表失败:", error);
  }
};

// 获取待处理的好友请求
const fetchPendingRequests = async () => {
  try {
    const res = await request.get(`/api/friend/pending-requests?userId=${myUserId}`);
    if (res.code === 200) {
      pendingRequests.value = res.data;
    }
  } catch (error) {
    console.error("获取好友请求失败:", error);
  }
};

// 🌟 发送好友请求
const sendFriendRequest = async () => {
  if (!searchId.value.trim()) {
    alert("请输入你要添加的好友 ID！");
    return;
  }
  if (searchId.value === myUserId) {
    alert("不能添加自己为好友！");
    return;
  }
  
  try {
    const res = await request.post(`/api/friend/request?senderId=${myUserId}&receiverId=${searchId.value}`);
    if (res.code === 200) {
      alert("请求已发送，等待对方通过");
      searchId.value = '';
    } else {
      alert(res.msg || "发送失败"); 
    }
  } catch (error) {
    console.error("添加好友失败:", error);
  }
};

// 🌟 同意好友请求
const acceptFriend = async (requestId) => {
  try {
    const res = await request.post(`/api/friend/accept?requestId=${requestId}`);
    if (res.code === 200) {
      alert("已添加对方为好友！");
      fetchPendingRequests(); // 刷新顶部的请求列表
      fetchFriendList();      // 刷新下方的好友列表
    } else {
      alert(res.msg || "处理失败");
    }
  } catch (error) {
    console.error("同意好友失败:", error);
  }
};

onMounted(() => {
  fetchFriendList();
  fetchPendingRequests(); // 页面加载时拉取新朋友请求
});

// ================== WebSocket 与 发送逻辑 ==================

const openChatRoom = (friend) => {
  currentChatUser.value = friend;
  messageList.value = []; 
  
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    initWebSocket();
  }
};

const initWebSocket = () => {
  if (socket) return; 
  socket = new WebSocket(wsUrl);
  
  socket.onopen = () => console.log("WebSocket 连接成功！");
  
  socket.onmessage = (event) => {
    try {
      const incomingMsg = JSON.parse(event.data);
      messageList.value.push({
        sender: 'other',
        messageType: incomingMsg.messageType || 'TEXT',
        content: incomingMsg.content,
        fileName: incomingMsg.fileName,
        fileSize: incomingMsg.fileSize
      });
    } catch (e) {
      messageList.value.push({ sender: 'other', messageType: 'TEXT', content: event.data });
    }
  };
  
  socket.onerror = (error) => console.error("WebSocket 发生错误:", error);
  socket.onclose = () => console.log("WebSocket 连接已断开");
};

const sendMessage = () => {
  if (!inputText.value.trim() || !currentChatUser.value) return;

  const messageObj = {
    toUserId: currentChatUser.value.id,
    messageType: 'TEXT', 
    content: inputText.value
  };
  
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify(messageObj));
    messageList.value.push({ sender: 'me', ...messageObj });
    inputText.value = '';
  } else {
    alert("连接未建立或已断开，请刷新页面重试！");
  }
};

const triggerUpload = () => {
  fileInput.value.click();
};

const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);

  try {
    const res = await request.post('/api/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });

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
      
      if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(messageObj));
        messageList.value.push({ sender: 'me', ...messageObj });
      } else {
        alert("WebSocket未连接，发送失败");
      }
    } else {
      alert("上传失败：" + res.msg);
    }
  } catch (error) {
    console.error("文件上传报错:", error);
  } finally {
    event.target.value = ''; 
  }
};

onUnmounted(() => {
  if (socket) socket.close();
});
</script>

<style scoped>
/* ================= 页面整体布局 ================= */
.im-page-container {
  display: flex;
  width: 100%;
  height: 100%;
  background-color: #fff;
}

/* ================= 左侧：联系人列表栏 ================= */
.im-list-pane {
  width: 280px;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
}

.search-area {
  padding: 15px;
  border-bottom: 1px solid #e2e8f0;
  display: flex; 
  gap: 10px;
}

.search-input {
  flex-grow: 1; 
  padding: 8px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px; 
  outline: none;
  font-size: 13px;
}

.add-btn {
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0 15px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.2s;
  white-space: nowrap; 
}

.add-btn:hover {
  background-color: #357ebd;
}

.search-input:focus {
  border-color: #4a90e2;
}

/* 🌟 好友请求验证区样式 */
.section-title {
  padding: 10px 15px;
  font-size: 12px;
  color: #94a3b8;
  background-color: #f1f5f9;
  font-weight: bold;
}

.pending-requests {
  border-bottom: 2px solid #e2e8f0;
}

.request-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background-color: #fff;
  border-bottom: 1px solid #f1f5f9;
}

.req-info {
  display: flex;
  flex-direction: column;
}

.req-name {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
}

.req-desc {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.accept-btn {
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  transition: 0.2s;
}

.accept-btn:hover {
  background-color: #059669;
}

/* 原有好友列表样式 */
.friend-list {
  flex-grow: 1;
  overflow-y: auto;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.2s;
}

.friend-item:hover {
  background-color: #f1f5f9;
}

.friend-item.active-friend {
  background-color: #e6f0fa;
  border-left: 4px solid #4a90e2;
}

.friend-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #4a90e2;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  margin-right: 12px;
  flex-shrink: 0;
}

.friend-name {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
}

.friend-status {
  font-size: 12px;
  margin-top: 4px;
}
.friend-status.online { color: #10b981; }
.friend-status.offline { color: #94a3b8; }

/* ================= 右侧：聊天对话栏 ================= */
.im-chat-pane {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
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

.message-box {
  flex-grow: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  display: flex;
  width: 100%;
}

.message-left { justify-content: flex-start; }
.message-left .message-bubble {
  background-color: #fff;
  border: 1px solid #e2e8f0;
  color: #1e293b;
}

.message-right { justify-content: flex-end; }
.message-right .message-bubble {
  background-color: #4a90e2;
  color: white;
}

.message-bubble {
  max-width: 60%;
  padding: 10px 15px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.input-area {
  padding: 15px 20px;
  background-color: #fff;
  border-top: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.upload-btn {
  background: none;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 18px;
  cursor: pointer;
  padding: 8px 12px;
  color: #64748b;
  transition: 0.2s;
  height: 40px;
}

.upload-btn:hover {
  background-color: #f1f5f9;
  color: #4a90e2;
}

.chat-image {
  max-width: 200px;
  border-radius: 6px;
  cursor: pointer;
  display: block;
}

.file-card {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.chat-file-name {
  color: inherit;
  text-decoration: none;
  font-weight: bold;
  word-break: break-all;
}

.chat-file-name:hover {
  text-decoration: underline;
}

.file-size {
  font-size: 12px;
  opacity: 0.8;
}

.chat-input {
  flex-grow: 1;
  padding: 10px 15px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
  height: 40px;
  box-sizing: border-box;
}

.chat-input:focus {
  border-color: #4a90e2;
}

.send-btn {
  padding: 0 25px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.2s;
  height: 40px;
}

.send-btn:hover {
  background-color: #357ebd;
}

/* ================= 空状态样式 ================= */
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
</style>