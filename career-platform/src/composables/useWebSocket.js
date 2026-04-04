import { ref, onUnmounted } from 'vue';

export function useWebSocket(wsUrl, onScrollToBottom) {
  const socket = ref(null);
  const isReconnecting = ref(false);
  const messageList = ref([]); 
  
  const isTyping = ref(false);
  let typingClearTimer = null;
  
  let reconnectTimer = null;
  let heartbeatTimer = null;

  // 🌟 核心：接收动态网关分配的 URL
  const initWebSocket = (dynamicAuthUrl) => {
    if (socket.value && socket.value.readyState === WebSocket.OPEN) return; 

    const token = localStorage.getItem('token') || ''; 
    // 优先使用动态传递的 URL，没有的话使用原有的 wsUrl 兜底
    const targetUrl = dynamicAuthUrl || wsUrl;
    const finalWsUrl = `${targetUrl.includes('?') ? targetUrl : targetUrl + '?'}token=${token}`;

    socket.value = new WebSocket(finalWsUrl);

    socket.value.onopen = () => {
      console.log("WebSocket 连接成功！", finalWsUrl);
      isReconnecting.value = false;
      startHeartbeat(); 
    };

    socket.value.onmessage = (event) => {
      try {
        const incomingMsg = JSON.parse(event.data);
        
        if (incomingMsg.type === 'PONG') return; 

        if (incomingMsg.type === 'TYPING') {
            isTyping.value = true;
            if (typingClearTimer) clearTimeout(typingClearTimer);
            typingClearTimer = setTimeout(() => { isTyping.value = false; }, 2000);
            return;
        }

        if (incomingMsg.type === 'CHAT' || !incomingMsg.type) {
            // QoS 回执
            if (incomingMsg.msgId) {
                socket.value.send(JSON.stringify({ type: "CLIENT_ACK", msgId: incomingMsg.msgId }));
            }
            messageList.value.push({
              sender: 'other',
              messageType: incomingMsg.messageType || 'TEXT',
              content: incomingMsg.content,
              fileName: incomingMsg.fileName,
              fileSize: incomingMsg.fileSize
            });
        }
      } catch (e) {
        messageList.value.push({ sender: 'other', messageType: 'TEXT', content: event.data });
      }
      
      if (onScrollToBottom) onScrollToBottom();
    };

    socket.value.onerror = (error) => console.error("WebSocket Error:", error);

    socket.value.onclose = (event) => {
      stopHeartbeat(); 
      if (event.code !== 1008) { reconnect(); } 
    };
  };

  const reconnect = () => {
    if (isReconnecting.value) return;
    isReconnecting.value = true;
    reconnectTimer = setTimeout(() => { initWebSocket(); }, 3000);
  };

  const startHeartbeat = () => {
    stopHeartbeat();
    heartbeatTimer = setInterval(() => {
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
        socket.value.send(JSON.stringify({ type: 'PING' }));
      }
    }, 30000);
  };

  const stopHeartbeat = () => { if (heartbeatTimer) clearInterval(heartbeatTimer); };

  const sendMessage = (messageObj) => {
    if (socket.value && socket.value.readyState === WebSocket.OPEN) {
      socket.value.send(JSON.stringify(messageObj));
      messageList.value.push({ sender: 'me', ...messageObj });
      if (onScrollToBottom) onScrollToBottom();
      return true; 
    }
    return false; 
  };

  const sendTypingSignal = (targetUserId) => {
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
          socket.value.send(JSON.stringify({ type: "TYPING", toUserId: targetUserId }));
      }
  };

  const closeWebSocket = () => {
    if (socket.value) { socket.value.onclose = null; socket.value.close(); socket.value = null; }
    stopHeartbeat();
    if (reconnectTimer) clearTimeout(reconnectTimer);
  };

  onUnmounted(() => { closeWebSocket(); });

  return {
    messageList,
    isReconnecting,
    isTyping,
    initWebSocket,
    sendMessage,
    sendTypingSignal,
    closeWebSocket
  };
}