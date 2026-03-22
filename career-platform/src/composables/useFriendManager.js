import { ref } from 'vue';
import request from '../utils/request';

export function useFriendManager(myUserId) {
  const friendList = ref([]);
  const pendingRequests = ref([]);
  const searchResults = ref([]); // 存储搜索用户的返回结果
  const isSearching = ref(false); // 搜索状态指示器

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

  // 3. 搜索用户 (新增：支持按昵称或手机号模糊搜索)
  const searchUser = async (keyword) => {
    if (!keyword.trim()) {
      searchResults.value = [];
      return;
    }
    isSearching.value = true;
    try {
      // 假设后端提供了一个模糊搜索用户的接口
      const res = await request.get(`/api/user/search?keyword=${encodeURIComponent(keyword)}`);
      // 如果后端没有此接口，这部分代码需要配合后端开发。这里假设返回的 res.data 是一个用户数组
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

  // 4. 发送好友申请 (修改：接收目标用户的 ID)
  const sendFriendRequest = async (targetUserId) => {
    if (!targetUserId) return;
    if (String(targetUserId) === String(myUserId)) {
      alert("不能添加自己为好友！");
      return false;
    }
    try {
      const res = await request.post(`/api/friend/request?senderId=${myUserId}&receiverId=${targetUserId}`);
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

  // 5. 同意好友申请
  const acceptFriend = async (requestId) => {
    try {
      const res = await request.post(`/api/friend/accept?requestId=${requestId}`);
      if (res.code === 200) {
        alert("已添加对方为好友！");
        fetchPendingRequests();
        fetchFriendList();
        return true;
      } else {
        alert(res.msg || "处理失败");
        return false;
      }
    } catch (error) {
      console.error("同意好友失败:", error);
      return false;
    }
  };

  // 6. 删除好友 (新增)
  const deleteFriend = async (friendId) => {
    if (!confirm("确定要删除该好友吗？删除后聊天记录将被清空。")) return false;
    try {
      // 假设后端提供了一个删除好友的接口
      const res = await request.post(`/api/friend/delete?userId=${myUserId}&friendId=${friendId}`);
      if (res.code === 200) {
        alert("删除成功");
        fetchFriendList(); // 重新加载好友列表
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
    acceptFriend,
    deleteFriend
  };
}