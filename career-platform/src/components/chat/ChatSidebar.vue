<template>
  <div class="im-list-pane">
    <div class="search-area">
      <input 
        v-model="searchKeyword" type="text" placeholder="搜索昵称或ID添加好友..." 
        class="search-input" @input="handleSearchInput"
      />
      <button v-if="searchKeyword" @click="clearSearch" class="clear-btn">✖</button>
    </div>

    <div class="search-results-list" v-if="searchKeyword">
      <div class="section-title">搜索结果 <span v-if="isSearching" class="loading-dots">...</span></div>
      <div v-if="searchResults.length === 0 && !isSearching" class="no-result">未找到相关用户</div>
      <div v-for="user in searchResults" :key="user.id" class="search-user-item">
        <div class="user-info-row">
          <div class="user-avatar">{{ user.nickName ? user.nickName.charAt(0) : 'U' }}</div>
          <div class="user-details">
            <div class="user-name">{{ user.nickName || user.phone }}</div>
            <div class="user-id">ID: {{ user.id }}</div>
          </div>
        </div>
        <button @click="handleSendRequest(user.id)" class="add-btn-small">添加</button>
      </div>
    </div>

    <template v-else>
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
        <div v-if="friendList.length === 0" class="no-friends">暂无联系人，快去添加好友吧</div>
        <div 
          v-for="friend in friendList" :key="friend.id" class="friend-item-wrapper"
          :class="{ 'active-friend': currentChatUser?.id === friend.id }"
          @mouseenter="hoveredFriendId = friend.id" @mouseleave="hoveredFriendId = null"
        >
          <div class="friend-item" @click="emit('select-friend', friend)">
            <div class="friend-avatar">{{ friend.name.charAt(0) }}</div>
            <div class="friend-info">
              <div class="friend-name">{{ friend.name }}</div>
              <div class="friend-status" :class="friend.isOnline ? 'online' : 'offline'">
                {{ friend.isOnline ? '[在线]' : '[离线]' }}
              </div>
            </div>
          </div>
          <button v-show="hoveredFriendId === friend.id" @click.stop="handleDeleteFriend(friend.id)" class="delete-friend-btn" title="删除好友">🗑️</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useFriendManager } from '../../composables/useFriendManager';

const props = defineProps({ currentChatUser: Object });
const emit = defineEmits(['select-friend', 'friend-deleted']);

const myUserId = localStorage.getItem('userId') || "1001"; 
const searchKeyword = ref('');
const hoveredFriendId = ref(null);

const { friendList, pendingRequests, searchResults, isSearching, fetchFriendList, fetchPendingRequests, searchUser, sendFriendRequest, acceptFriend, deleteFriend } = useFriendManager(myUserId);

let searchTimeout = null;
const handleSearchInput = () => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => { searchUser(searchKeyword.value); }, 500); 
};

const clearSearch = () => { searchKeyword.value = ''; searchResults.value = []; };

const handleSendRequest = async (targetId) => {
  if (await sendFriendRequest(targetId)) clearSearch(); 
};

const handleDeleteFriend = async (friendId) => {
  if (await deleteFriend(friendId)) emit('friend-deleted', friendId);
};

onMounted(() => {
  fetchFriendList();
  fetchPendingRequests();
});
</script>

<style scoped>
/* 包含刚才所有的左侧专属CSS，比如 .im-list-pane, .search-area, .search-results-list 等 */
.im-list-pane { 
  width: 280px; /* PC端保持 280px */
  max-width: 100%; /* 防止溢出 */
  border-right: 1px solid #e2e8f0; 
  display: flex; 
  flex-direction: column; 
  background-color: #f8fafc; 
}
.search-area { padding: 15px; border-bottom: 1px solid #e2e8f0; display: flex; gap: 10px; position: relative; }
.search-input { flex-grow: 1; padding: 8px 30px 8px 12px; border: 1px solid #cbd5e1; border-radius: 8px; outline: none; font-size: 13px; }
.search-input:focus { border-color: #4a90e2; }
.clear-btn { position: absolute; right: 15px; background: none; border: none; color: #94a3b8; cursor: pointer; font-size: 12px; }
.clear-btn:hover { color: #64748b; }
.search-results-list { flex-grow: 1; overflow-y: auto; background-color: #f1f5f9; }
.no-result, .no-friends { padding: 20px; text-align: center; color: #94a3b8; font-size: 13px; }
.loading-dots { animation: blink 1.4s infinite both; }
@keyframes blink { 0% { opacity: .2; } 20% { opacity: 1; } 100% { opacity: .2; } }
.search-user-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 15px; background-color: #fff; border-bottom: 1px solid #f1f5f9; transition: background-color 0.2s; }
.search-user-item:hover { background-color: #f8fafc; }
.user-info-row { display: flex; align-items: center; }
.user-avatar { width: 36px; height: 36px; border-radius: 50%; background-color: #cbd5e1; color: white; display: flex; justify-content: center; align-items: center; font-weight: bold; margin-right: 12px; }
.user-name { font-size: 14px; font-weight: 500; color: #1e293b; }
.user-id { font-size: 12px; color: #94a3b8; }
.add-btn-small { background-color: #fff; color: #4a90e2; border: 1px solid #4a90e2; border-radius: 4px; padding: 4px 10px; font-size: 12px; cursor: pointer; transition: all 0.2s; }
.add-btn-small:hover { background-color: #4a90e2; color: white; }
.section-title { padding: 10px 15px; font-size: 12px; color: #94a3b8; background-color: #f1f5f9; font-weight: bold; }
.pending-requests { border-bottom: 2px solid #e2e8f0; }
.request-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 15px; background-color: #fff; border-bottom: 1px solid #f1f5f9; }
.req-info { display: flex; flex-direction: column; }
.req-name { font-size: 14px; font-weight: bold; color: #1e293b; }
.req-desc { font-size: 12px; color: #64748b; margin-top: 4px; }
.accept-btn { background-color: #10b981; color: white; border: none; border-radius: 4px; padding: 6px 12px; font-size: 12px; cursor: pointer; transition: 0.2s; }
.accept-btn:hover { background-color: #059669; }
.friend-list { flex-grow: 1; overflow-y: auto; }
.friend-item-wrapper { position: relative; display: flex; align-items: center; }
.friend-item { flex-grow: 1; display: flex; align-items: center; padding: 15px; border-bottom: 1px solid #f1f5f9; cursor: pointer; transition: all 0.2s; }
.friend-item-wrapper:hover .friend-item { background-color: #f1f5f9; }
.friend-item-wrapper.active-friend .friend-item { background-color: #e6f0fa; border-left: 4px solid #4a90e2; }
.delete-friend-btn { position: absolute; right: 15px; background: none; border: none; cursor: pointer; font-size: 16px; opacity: 0.6; transition: opacity 0.2s, transform 0.2s; }
.delete-friend-btn:hover { opacity: 1; transform: scale(1.1); color: #ef4444; }
.friend-avatar { width: 40px; height: 40px; border-radius: 50%; background-color: #4a90e2; color: white; display: flex; justify-content: center; align-items: center; font-weight: bold; margin-right: 12px; flex-shrink: 0; }
.friend-name { font-size: 14px; color: #1e293b; font-weight: 500; }
.friend-status { font-size: 12px; margin-top: 4px; }
.friend-status.online { color: #10b981; }
.friend-status.offline { color: #94a3b8; }
</style>