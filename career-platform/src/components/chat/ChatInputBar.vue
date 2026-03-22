<template>
  <div class="input-area">
    <input type="file" ref="fileInput" @change="handleFileUpload" accept="image/*,.pdf,.doc,.docx,.zip" style="display: none;">
    
    <div class="more-actions">
      <button @click.stop="toggleMoreMenu" class="more-btn" title="更多功能"><span class="plus-icon">+</span></button>
      <div v-if="showMoreMenu" class="more-menu" @click.stop>
        <div class="menu-grid">
          <div class="menu-item" @click="triggerUpload"><span class="menu-icon">🖼️</span><span>相册</span></div>
          <div class="menu-item" @click="() => alert('视频通话功能开发中')"><span class="menu-icon">📹</span><span>视频通话</span></div>
          <div class="menu-item" @click="() => alert('语音通话功能开发中')"><span class="menu-icon">📞</span><span>语音通话</span></div>
          <div class="menu-item" @click="voiceInput"><span class="menu-icon">🎤</span><span>语音输入</span></div>
        </div>
      </div>
    </div>

    <button @click.stop="toggleEmoji" class="emoji-btn" title="表情"><span class="emoji-icon">😊</span></button>
    <div v-if="showEmoji" class="emoji-panel" @click.stop>
      <div class="emoji-tabs"><span class="tab active">😊 表情</span></div>
      <div class="emoji-grid">
        <span v-for="emoji in emojiList" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">{{ emoji }}</span>
      </div>
    </div>

    <input 
      v-model="inputText" type="text" :placeholder="isRecording ? '🎤 正在聆听中，请说话...' : '输入消息...'" 
      class="chat-input" :class="{ 'is-recording-input': isRecording }" @keyup.enter="handleSend" :disabled="disabled"
    >

    <button v-if="isRecording" @click="voiceInput" class="stop-record-btn pulse-anim">⏹ 停止</button>
    <button v-else @click="handleSend" class="send-btn" :disabled="!inputText.trim() || disabled">发送</button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { emojiList as importedEmojis } from '../../constants/emoji'; 
import { useVoiceInput } from '../../composables/useVoiceInput';

const props = defineProps({ disabled: Boolean });
const emit = defineEmits(['send-text', 'send-file']);

const inputText = ref('');
const fileInput = ref(null);
const showMoreMenu = ref(false);
const showEmoji = ref(false);
const emojiList = ref(importedEmojis);

// 语音逻辑
let originalText = ''; 
const { isRecording, toggleVoice, stopVoice } = useVoiceInput((final, interim) => {
  if (final) { originalText += final; inputText.value = originalText; } 
  else { inputText.value = originalText + interim; }
});

const voiceInput = () => {
  showMoreMenu.value = false; 
  if (!isRecording.value) originalText = inputText.value; 
  toggleVoice(); 
};

// UI交互
const toggleMoreMenu = () => { showMoreMenu.value = !showMoreMenu.value; showEmoji.value = false; };
const toggleEmoji = () => { showEmoji.value = !showEmoji.value; showMoreMenu.value = false; };
const insertEmoji = (emoji) => { inputText.value += emoji; };

const handleClickOutside = (event) => {
  if (!event.target.closest('.more-actions') && !event.target.closest('.emoji-btn') && !event.target.closest('.emoji-panel')) {
    showMoreMenu.value = false;
    showEmoji.value = false;
  }
};

// 发送事件
const handleSend = () => {
  stopVoice();
  if (!inputText.value.trim() || props.disabled) return;
  emit('send-text', inputText.value);
  inputText.value = '';
  showEmoji.value = false;
};

const triggerUpload = () => { fileInput.value.click(); showMoreMenu.value = false; };
const handleFileUpload = (event) => {
  const file = event.target.files[0];
  if (file) emit('send-file', file);
  event.target.value = '';
};

onMounted(() => document.addEventListener('click', handleClickOutside));
onUnmounted(() => document.removeEventListener('click', handleClickOutside));
</script>

<style scoped>
/* 包含输入区域、表情包、按钮等全部专属CSS */
.input-area { display: flex; align-items: center; padding: 12px 20px; background-color: #f8fafc; border-top: 1px solid #e2e8f0; position: relative; gap: 10px; }
.more-actions { position: relative; }
.more-btn { width: 36px; height: 36px; border-radius: 50%; background-color: #fff; border: 1px solid #cbd5e1; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #64748b; transition: all 0.3s; padding-bottom: 3px; }
.more-btn:hover { background-color: #e2e8f0; color: #4a90e2; }
.more-menu { position: absolute; bottom: 50px; left: 0; width: 280px; background-color: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15); padding: 12px; z-index: 1000; border: 1px solid #e2e8f0; }
.menu-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px 8px; }
.menu-item { display: flex; flex-direction: column; align-items: center; cursor: pointer; padding: 10px 4px; border-radius: 8px; transition: background-color 0.3s; }
.menu-item:hover { background-color: #f1f5f9; }
.menu-icon { font-size: 24px; margin-bottom: 4px; }
.menu-item span:last-child { font-size: 12px; color: #475569; }
.emoji-btn { width: 36px; height: 36px; border-radius: 50%; background-color: #fff; border: 1px solid #cbd5e1; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 20px; transition: all 0.3s; }
.emoji-btn:hover { background-color: #e2e8f0; }
.emoji-panel { position: absolute; bottom: 55px; left: 60px; width: 320px; background-color: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15); z-index: 1000; overflow: hidden; border: 1px solid #e2e8f0; }
.emoji-tabs { display: flex; border-bottom: 1px solid #e2e8f0; background-color: #f8fafc; }
.emoji-tabs .tab { flex: 1; padding: 10px; text-align: center; cursor: default; font-size: 14px; color: #64748b; }
.emoji-tabs .tab.active { background-color: #fff; color: #4a90e2; border-bottom: 2px solid #4a90e2; font-weight: bold; }
.emoji-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 8px; padding: 12px; max-height: 200px; overflow-y: auto; }
.emoji-item { font-size: 24px; cursor: pointer; text-align: center; padding: 4px; border-radius: 4px; transition: background-color 0.3s; }
.emoji-item:hover { background-color: #f1f5f9; }
.chat-input { flex: 1; height: 40px; padding: 0 16px; border: 1px solid #cbd5e1; border-radius: 20px; font-size: 14px; outline: none; transition: all 0.3s ease; background-color: #fff; }
.chat-input:focus { border-color: #4a90e2; }
.is-recording-input { border-color: #ef4444 !important; background-color: #fef2f2 !important; color: #ef4444; box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15); }
.stop-record-btn { padding: 0 20px; height: 40px; background-color: #ef4444; color: white; border: none; border-radius: 20px; cursor: pointer; font-size: 14px; font-weight: bold; transition: background-color 0.3s; }
.stop-record-btn:hover { background-color: #dc2626; }
.pulse-anim { animation: pulse-red 1.5s infinite; }
@keyframes pulse-red { 0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.7); } 70% { box-shadow: 0 0 0 10px rgba(239, 68, 68, 0); } 100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); } }
.send-btn { padding: 0 20px; height: 40px; background-color: #4a90e2; color: white; border: none; border-radius: 20px; cursor: pointer; font-size: 14px; font-weight: bold; transition: background-color 0.3s; }
.send-btn:hover:not(:disabled) { background-color: #357ebd; }
.send-btn:disabled { background-color: #cbd5e1; cursor: not-allowed; }
</style>