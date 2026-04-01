import { ref } from 'vue';
import request from '../utils/request';

export function useChatManager(myUserId) {
  const chatHistory = ref([]);
  const unreadCounts = ref({}); // 存放所有好友的未读数量字典，例如: { 'friend_123': 2, 'friend_456': 0 }

  // 1. 拉取历史消息
  const fetchChatHistory = async (friendId) => {
    try {
      const res = await request.get(`/api/chat/history?myUserId=${myUserId}&friendId=${friendId}`);
      if (res.code === 200) {
        chatHistory.value = res.data;
      }
    } catch (error) {
      console.error("拉取聊天记录失败:", error);
    }
  };

  // 2. 获取针对某个好友的未读消息数量
  const fetchUnreadCount = async (friendId) => {
    try {
      const res = await request.get(`/api/chat/unread-count?myUserId=${myUserId}&friendId=${friendId}`);
      if (res.code === 200) {
        // 更新本地未读字典
        unreadCounts.value[friendId] = res.data;
      }
    } catch (error) {
      console.error("获取未读数量失败:", error);
    }
  };

  // 3. 标记与某人的消息为已读（点开聊天框时调用）
  const markAsRead = async (friendId) => {
    try {
      const res = await request.post(`/api/chat/mark-read`, null, {
        params: { myUserId, friendId }
      });
      if (res.code === 200) {
        // 清零本地红点
        unreadCounts.value[friendId] = 0;
      }
    } catch (error) {
      console.error("标记已读失败:", error);
    }
  };

  return {
    chatHistory,
    unreadCounts,
    fetchChatHistory,
    fetchUnreadCount,
    markAsRead
  };
}