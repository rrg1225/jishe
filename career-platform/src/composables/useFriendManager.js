import { ref } from 'vue';
import request from '../utils/request';

export function useFriendManager(myUserId) {
  const friendList = ref([]);
  const pendingRequests = ref([]);
  const searchResults = ref([]); 
  const isSearching = ref(false); 

  // 1. 获取好友列表
  const fetchFriendList = async () => {
    try {
      const res = await request.get(`/api/friend/list?userId=${myUserId}`);
      if (res.code === 200) friendList.value = res.data;
    } catch (error) {
      console.error("获取好友列表失败:", error);
    }
  };

  // 2. 获取待处理的好友请求
  const fetchPendingRequests = async () => {
    try {
      const res = await request.get(`/api/friend/pending-requests?userId=${myUserId}`);
      if (res.code === 200) pendingRequests.value = res.data;
    } catch (error) {
      console.error("获取好友请求失败:", error);
    }
  };

  // 3. 搜索用户
  const searchUser = async (keyword) => {
    if (!keyword.trim()) {
      searchResults.value = [];
      return;
    }
    isSearching.value = true;
    try {
      const res = await request.get(`/api/user/search?keyword=${encodeURIComponent(keyword)}`);
      if (res.code === 200) {
        searchResults.value = res.data;
      } else {
        searchResults.value = [];
      }
    } catch (error) {
      console.error("搜索用户失败:", error);
      searchResults.value = [];
    } finally {
      isSearching.value = false;
    }
  };

  // 4. 🌟 发送好友申请 (带验证信息)
  const sendFriendRequest = async (targetUserId, message = "你好，我想添加你为好友") => {
    if (!targetUserId) return;
    if (String(targetUserId) === String(myUserId)) {
      alert("不能添加自己为好友！");
      return false;
    }
    
    try {
      // 通过 url params 携带 message
      const res = await request.post(`/api/friend/request`, null, {
          params: { senderId: myUserId, receiverId: targetUserId, message }
      });
      if (res.code === 200) {
        alert("请求已发送，等待对方通过");
        return true;
      } else {
        alert(res.msg || "发送失败");
        return false;
      }
    } catch (error) {
      console.error("添加好友失败:", error);
      return false;
    }
  };

  // 5. 🌟 综合处理好友申请 (同意、拒绝、忽略、并支持设置备注)
  const handleFriendRequest = async (requestId, action, alias = "") => {
    try {
      const res = await request.post(`/api/friend/handle`, null, {
          params: { requestId, action, alias }
      });
      
      if (res.code === 200) {
        alert(res.msg);
        fetchPendingRequests(); // 刷新红点和待办
        if (action === 'ACCEPT') {
            fetchFriendList();  // 如果是同意，立即刷新好友列表
        }
        return true;
      } else {
        alert(res.msg || "处理失败");
        return false;
      }
    } catch (error) {
      console.error("处理好友申请失败:", error);
      return false;
    }
  };

  // 6. 🌟 后期随时修改好友备注
  const updateFriendAlias = async (friendId, newAlias) => {
      try {
          const res = await request.post(`/api/friend/alias`, null, {
              params: { userId: myUserId, friendId, alias: newAlias }
          });
          if (res.code === 200) {
              fetchFriendList(); // 刷新列表展示新名字
              return true;
          }
          return false;
      } catch (error) {
          console.error("修改备注失败", error);
          return false;
      }
  }

  // 7. 删除好友
  const deleteFriend = async (friendId) => {
    if (!confirm("确定要删除该好友吗？删除后聊天记录将被清空。")) return false;
    try {
      const res = await request.post(`/api/friend/delete`, null, {
          params: { userId: myUserId, friendId }
      });
      if (res.code === 200) {
        alert("删除成功");
        fetchFriendList(); 
        return true;
      } else {
        alert(res.msg || "删除失败");
        return false;
      }
    } catch (error) {
      console.error("删除好友失败:", error);
      return false;
    }
  };

  return {
    friendList,
    pendingRequests,
    searchResults,
    isSearching,
    fetchFriendList,
    fetchPendingRequests,
    searchUser,
    sendFriendRequest,
    handleFriendRequest, // 暴露新方法：在您的 vue 页面中调用 handleFriendRequest(id, 'ACCEPT', '张三') 或 handleFriendRequest(id, 'REJECT')
    updateFriendAlias,   // 暴露新方法
    deleteFriend
  };
}