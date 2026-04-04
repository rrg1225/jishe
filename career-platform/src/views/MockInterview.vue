<template>
  <div class="p-6 bg-gray-900 min-h-screen text-white flex gap-6">
    
    <div class="flex-1 bg-gray-800 rounded-xl p-6 shadow-lg flex flex-col relative">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-2xl font-bold text-blue-400">🤖 AI 面试官</h2>
        <div class="flex items-center gap-4">
          <span v-if="sessionId" class="text-sm bg-gray-700 px-3 py-1 rounded-full">Round: {{ currentRound }}</span>
          <label class="flex items-center cursor-pointer">
            <div class="relative">
              <input type="checkbox" v-model="useCamera" class="sr-only" @change="toggleCamera" :disabled="sessionId">
              <div class="block bg-gray-600 w-10 h-6 rounded-full"></div>
              <div :class="useCamera ? 'translate-x-4 bg-green-400' : 'translate-x-0 bg-gray-400'" class="dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition"></div>
            </div>
            <div class="ml-3 font-medium text-sm text-gray-300">多模态(摄像头)</div>
          </label>
        </div>
      </div>

      <div v-show="useCamera" class="relative w-full h-48 bg-black rounded-lg overflow-hidden mb-4 flex justify-center">
        <video ref="videoEl" autoplay muted class="h-full"></video>
        <canvas ref="canvasEl" class="absolute top-0 left-0 w-full h-full"></canvas>
        <div class="absolute top-2 right-2 text-xs px-2 py-1 bg-black/50 rounded">{{ currentEmotion }}</div>
      </div>

      <div class="flex-1 overflow-y-auto mb-4 space-y-4 pr-2 custom-scrollbar">
        <div v-for="(msg, index) in chatHistory" :key="index" :class="msg.role === 'ai' ? 'text-left' : 'text-right'">
          <div :class="msg.role === 'ai' ? 'bg-blue-900/50 text-blue-100' : 'bg-green-900/50 text-green-100'" 
               class="inline-block px-4 py-3 rounded-2xl max-w-[85%] text-sm leading-relaxed">
            <span class="font-bold block mb-1 text-xs opacity-70">{{ msg.role === 'ai' ? '面试官' : '我' }}</span>
            {{ msg.content }}
          </div>
        </div>
      </div>

      <div v-if="!sessionId" class="text-center pb-4">
        <button @click="startInterview" class="px-8 py-3 bg-blue-600 rounded-full font-bold hover:bg-blue-500 w-full">点击开始面试</button>
      </div>
      
      <div v-else class="flex flex-col gap-2">
        <div class="flex gap-2">
           <textarea v-model="userInput" placeholder="输入您的回答，或使用语音..." class="flex-1 bg-gray-900 border border-gray-700 rounded-lg p-3 text-white resize-none" rows="2"></textarea>
           <button @click="submitAnswer" :disabled="isThinking" class="bg-blue-600 px-6 rounded-lg font-bold hover:bg-blue-500 disabled:opacity-50">
             {{ isThinking ? '思考中' : '发送' }}
           </button>
        </div>
        <div class="flex justify-between items-center mt-2">
          <button @click="startVoiceInput" class="text-sm text-gray-400 hover:text-white flex items-center gap-1">🎙️ 语音输入</button>
          <button @click="finishInterview" class="text-sm text-red-400 hover:text-red-300">结束并生成报告</button>
        </div>
      </div>
    </div>

    <div v-if="reportData" class="w-1/3 bg-gray-800 rounded-xl p-6 shadow-lg border-l-4 border-green-500 animate-fade-in">
      <h3 class="text-xl font-bold text-green-400 mb-6">📊 面试评估报告</h3>
      
      <div class="space-y-4 mb-6">
        <div class="flex justify-between items-center"><span class="text-gray-400">逻辑表达力</span><span class="text-xl font-bold text-blue-400">{{ reportData.expressScore }}分</span></div>
        <div class="flex justify-between items-center"><span class="text-gray-400">专业技术度</span><span class="text-xl font-bold text-purple-400">{{ reportData.techScore }}分</span></div>
        <div class="flex justify-between items-center"><span class="text-gray-400">抗压/情绪稳定性</span><span class="text-xl font-bold text-yellow-400">{{ useCamera ? reportData.logicScore : '未评估' }}分</span></div>
      </div>

      <div class="mb-4">
        <h4 class="text-gray-300 font-bold mb-2">综合评价</h4>
        <p class="text-sm text-gray-400 bg-gray-900 p-3 rounded">{{ reportData.summary }}</p>
      </div>

      <div>
        <h4 class="text-gray-300 font-bold mb-2">核心改进建议</h4>
        <ul class="list-disc pl-5 text-sm text-gray-400 space-y-1">
          <li v-for="(sug, idx) in reportData.suggestions" :key="idx">{{ sug }}</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRoute } from 'vue-router';
import request from '@/utils/request'; // 统一使用你们项目的 request.js，解决跨域/端口写死问题
import * as faceapi from 'face-api.js';

const route = useRoute();
const jobTitle = route.query.jobTitle || '后端开发工程师';

// 状态
const sessionId = ref(null);
const currentRound = ref(0);
const chatHistory = ref([]);
const userInput = ref('');
const isThinking = ref(false);
const reportData = ref(null);

// 多模态相关
const useCamera = ref(true);
const videoEl = ref(null);
const canvasEl = ref(null);
const currentEmotion = ref('平静');
let detectInterval = null;

onMounted(async () => {
  // 预加载模型 (若失败则默认文字模式)
  try {
    await faceapi.nets.tinyFaceDetector.loadFromUri('/models');
    await faceapi.nets.faceExpressionNet.loadFromUri('/models');
  } catch(e) {
    console.error("视觉模型加载失败，已降级为纯文本模式");
    useCamera.value = false;
  }
});

const toggleCamera = () => {
  if (useCamera.value) {
    navigator.mediaDevices.getUserMedia({ video: true }).then(stream => {
      videoEl.value.srcObject = stream;
      videoEl.value.play();
      detectInterval = setInterval(detectFace, 500);
    }).catch(() => {
      alert("摄像头权限被拒绝");
      useCamera.value = false;
    });
  } else {
    stopCamera();
  }
};

const stopCamera = () => {
  if (detectInterval) clearInterval(detectInterval);
  if (videoEl.value?.srcObject) {
    videoEl.value.srcObject.getTracks().forEach(track => track.stop());
  }
};

const detectFace = async () => {
  if(!videoEl.value) return;
  const detections = await faceapi.detectAllFaces(videoEl.value, new faceapi.TinyFaceDetectorOptions()).withFaceExpressions();
  if(detections.length > 0) {
    const expr = detections[0].expressions;
    currentEmotion.value = Object.keys(expr).reduce((a, b) => expr[a] > expr[b] ? a : b);
  }
};

// --- 后端接口调用 ---

const startInterview = async () => {
  if(useCamera.value && !videoEl.value?.srcObject) toggleCamera();
  
  isThinking.value = true;
  try {
    // 统一通过 request 走 baseURL
    const res = await request.post('/api/interview/start', { 
      userId: 'user_123', // 替换为真实的登录用户ID
      jobTitle: jobTitle 
    });
    sessionId.value = res.data.sessionId;
    currentRound.value = 1;
    chatHistory.value.push({ role: 'ai', content: res.data.question });
  } finally {
    isThinking.value = false;
  }
};

const submitAnswer = async () => {
  if (!userInput.value.trim() || !sessionId.value) return;
  
  const answerTxt = userInput.value;
  chatHistory.value.push({ role: 'user', content: answerTxt });
  userInput.value = '';
  isThinking.value = true;

  try {
    const res = await request.post('/api/interview/chat', {
      sessionId: sessionId.value,
      answer: answerTxt,
      emotion: useCamera.value ? currentEmotion.value : '未开启摄像头'
    });
    chatHistory.value.push({ role: 'ai', content: res.data.nextQuestion });
    currentRound.value = res.data.round;
  } finally {
    isThinking.value = false;
  }
};

const finishInterview = async () => {
  if (!confirm("确定要结束面试并查看评分吗？")) return;
  isThinking.value = true;
  stopCamera();
  try {
    const res = await request.post('/api/interview/finish', { sessionId: sessionId.value });
    reportData.value = res.data; // 渲染大屏报告
  } finally {
    isThinking.value = false;
  }
};

// 原生浏览器语音识别
const startVoiceInput = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  const recognition = new SpeechRecognition();
  recognition.lang = 'zh-CN';
  recognition.onresult = (e) => { userInput.value += e.results[0][0].transcript; };
  recognition.start();
};

onBeforeUnmount(stopCamera);
</script>

<style scoped>
/* 隐藏滚动条但保留滚动功能 */
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #4b5563; border-radius: 10px; }
.animate-fade-in { animation: fadeIn 0.5s ease-in; }
@keyframes fadeIn { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
</style>