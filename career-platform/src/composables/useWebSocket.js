import { ref, onUnmounted } from 'vue';

/**
 * 封装 WebSocket 核心逻辑的组合式函数
 * @param {String} wsUrl WebSocket 连接地址
 * @param {Function} onScrollToBottom 收到消息后触发页面滚动到底部的回调
 */
export function useWebSocket(wsUrl, onScrollToBottom) {
  const socket = ref(null);
  const isReconnecting = ref(false);
  const messageList = ref([]); // 统一管理当前聊天窗口的消息列表
  
  let reconnectTimer = null;
  let heartbeatTimer = null;

  // 1. 初始化连接
  const initWebSocket = () => {
    // 如果已经有连接，先关掉，防止重复连接
    if (socket.value && socket.value.readyState === WebSocket.OPEN) {
      return; 
    }

    socket.value = new WebSocket(wsUrl);

    socket.value.onopen = () => {
      console.log("WebSocket 连接成功！");
      isReconnecting.value = false;
      startHeartbeat(); // 开启心跳
    };

    socket.value.onmessage = (event) => {
      try {
        const incomingMsg = JSON.parse(event.data);
        // 忽略服务端的心跳返回包
        if (incomingMsg.type === 'PONG') return; 

        // 将对方发来的消息推入列表
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
      
      // 通知 UI 层滚动到底部
      if (onScrollToBottom) onScrollToBottom();
    };

    socket.value.onerror = (error) => console.error("WebSocket 发生错误:", error);

    socket.value.onclose = () => {
      console.log("WebSocket 连接已断开");
      stopHeartbeat(); 
      reconnect(); // 触发断线重连
    };
  };

  // 2. 断线重连机制
  const reconnect = () => {
    if (isReconnecting.value) return;
    isReconnecting.value = true;
    
    // 延迟 3 秒尝试重新连接
    reconnectTimer = setTimeout(() => {
      console.log("尝试重新连接 WebSocket...");
      initWebSocket();
    }, 3000);
  };

  // 3. 心跳保活机制
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

  // 4. 发送消息（对外暴露的方法）
  const sendMessage = (messageObj) => {
    if (socket.value && socket.value.readyState === WebSocket.OPEN) {
      socket.value.send(JSON.stringify(messageObj));
      // 自己发的消息，立刻推入本地列表
      messageList.value.push({ sender: 'me', ...messageObj });
      if (onScrollToBottom) onScrollToBottom();
      return true; // 发送成功
    } else {
      alert("连接未建立或已断开，发送失败！");
      return false; // 发送失败
    }
  };

  // 5. 手动关闭连接
  const closeWebSocket = () => {
    if (socket.value) {
      socket.value.onclose = null; // 清除回调，防止无限重连
      socket.value.close();
      socket.value = null;
    }
    stopHeartbeat();
    if (reconnectTimer) clearTimeout(reconnectTimer);
  };

  // 组件卸载时自动清理资源，防止内存泄漏
  onUnmounted(() => {
    closeWebSocket();
  });

  return {
    messageList,       // 暴露给页面的响应式消息数组
    isReconnecting,    // 暴露给页面的重连状态
    initWebSocket,     // 建立连接的方法
    sendMessage,       // 发送消息的方法
    closeWebSocket     // 关闭连接的方法
  };
}