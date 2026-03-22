<template>
  <div class="message-box" ref="boardRef">
    <div 
      v-for="(msg, index) in messages" 
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
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';

// 接收父组件传来的消息列表
const props = defineProps({
  messages: {
    type: Array,
    required: true,
    default: () => []
  }
});

const boardRef = ref(null);

// 🌟 核心魔法：监听 messages 的变化，只要数组变了（发消息、收消息、拉取历史），自动滚动到底部
watch(
  () => props.messages,
  async () => {
    await nextTick(); // 等待 Vue 渲染完新的 DOM
    if (boardRef.value) {
      boardRef.value.scrollTop = boardRef.value.scrollHeight;
    }
  },
  { deep: true } // 深度监听数组内部的变化
);
</script>

<style scoped>
.message-box {
  flex-grow: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
  scroll-behavior: smooth;
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
</style>