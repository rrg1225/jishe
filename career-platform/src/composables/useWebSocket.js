import { ref, onUnmounted } from 'vue';

export function useWebSocket(wsUrl, onScrollToBottom) {
  const socket = ref(null);
  const isReconnecting = ref(false);
  const messageList = ref([]); 
  
  // 🌟 新增：暴露给页面的“正在输入”状态
  const isTyping = ref(false);
  let typingClearTimer = null;
  
  let reconnectTimer = null;
  let heartbeatTimer = null;

  const initWebSocket = () => {
    if (socket.value && socket.value.readyState === WebSocket.OPEN) return; 

    const token = localStorage.getItem('token') || ''; 
    const authWsUrl = `${wsUrl.includes('?') ? wsUrl : wsUrl + '?'}token=${token}`;

    socket.value = new WebSocket(authWsUrl);

    socket.value.onopen = () => {
      console.log("WebSocket 连接成功！");
      isReconnecting.value = false;
      startHeartbeat(); 
    };

    socket.value.onmessage = (event) => {
      try {
        const incomingMsg = JSON.parse(event.data);
        if (incomingMsg.type === 'PONG') return; 

        // 🚀 拦截系统信令
        if (incomingMsg.type === 'SYSTEM_NOTICE') {
            console.log("收到系统信令:", incomingMsg.subType);
            return; 
        }

        // 🚀 拦截“正在输入”信令
        if (incomingMsg.type === 'TYPING') {
            isTyping.value = true;
            // 如果 2 秒内没再收到 TYPING，就取消显示
            if (typingClearTimer) clearTimeout(typingClearTimer);
            typingClearTimer = setTimeout(() => {
                isTyping.value = false;
            }, 2000);
            return;
        }

        // 🚀 正常聊天消息处理 (QoS 核心机制)
        if (incomingMsg.type === 'CHAT' || !incomingMsg.type) {
            // 1. 立刻向后端发送已送达回执 (CLIENT_ACK)
            if (incomingMsg.msgId) {
                socket.value.send(JSON.stringify({
                    type: "CLIENT_ACK",
                    msgId: incomingMsg.msgId
                }));
                // console.log("已发送 QoS 消息回执:", incomingMsg.msgId);
            }

            // 2. 渲染到页面
            messageList.value.push({
              sender: 'other',
              messageType: incomingMsg.messageType || 'TEXT',
              content: incomingMsg.content,
              fileName: incomingMsg.fileName,
              fileSize: incomingMsg.fileSize
            });
        }
      } catch (e) {
        console.error("解析消息失败，按纯文本处理:", e);
        messageList.value.push({ sender: 'other', messageType: 'TEXT', content: event.data });
      }
      
      if (onScrollToBottom) onScrollToBottom();
    };

    socket.value.onerror = (error) => console.error("WebSocket 发生错误:", error);

    socket.value.onclose = (event) => {
      stopHeartbeat(); 
      if (event.code !== 1008) { 
        reconnect(); 
      } else {
        console.error("WebSocket 鉴权失败，服务器拒绝连接！");
      }
    };
  };

  const reconnect = () => {
    if (isReconnecting.value) return;
    isReconnecting.value = true;
    reconnectTimer = setTimeout(() => {
      initWebSocket();
    }, 3000);
  };

  const startHeartbeat = () => {
    stopHeartbeat();
    heartbeatTimer = setInterval(() => {
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
        socket.value.send(JSON.stringify({ type: 'PING' }));
      }
    }, 30000);
  };

  const stopHeartbeat = () => {
    if (heartbeatTimer) clearInterval(heartbeatTimer);
  };

  const sendMessage = (messageObj) => {
    if (socket.value && socket.value.readyState === WebSocket.OPEN) {
      socket.value.send(JSON.stringify(messageObj));
      // 注意：发送普通聊天消息时，前端不需要发 ACK，ACK 是别人推给你时才发的
      messageList.value.push({ sender: 'me', ...messageObj });
      if (onScrollToBottom) onScrollToBottom();
      return true; 
    } else {
      alert("连接未建立或已断开，发送失败！");
      return false; 
    }
  };

  // 🚀 新增：发送“正在输入”信令的方法
  const sendTypingSignal = (targetUserId) => {
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
          socket.value.send(JSON.stringify({
              type: "TYPING",
              toUserId: targetUserId
          }));
      }
  };

  const closeWebSocket = () => {
    if (socket.value) {
      socket.value.onclose = null; 
      socket.value.close();
      socket.value = null;
    }
    stopHeartbeat();
    if (reconnectTimer) clearTimeout(reconnectTimer);
  };

  onUnmounted(() => {
    closeWebSocket();
  });

  return {
    messageList,
    isReconnecting,
    isTyping,          // 暴露给组件：对方是否正在输入
    initWebSocket,
    sendMessage,
    sendTypingSignal,  // 暴露给组件：发送输入状态
    closeWebSocket
  };
}