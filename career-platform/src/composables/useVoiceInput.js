import { ref, onUnmounted } from 'vue';

export function useVoiceInput(onTextUpdate) {
  const isRecording = ref(false);
  let recognition = null;

  const initSpeechRecognition = () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      alert('当前浏览器不支持语音输入，请使用新版 Chrome 或 Edge。');
      return false;
    }

    recognition = new SpeechRecognition();
    recognition.lang = 'zh-CN';
    recognition.continuous = true; 
    recognition.interimResults = true; 

    recognition.onstart = () => {
      isRecording.value = true;
    };

    recognition.onresult = (event) => {
      let interim = '';
      let final = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        if (event.results[i].isFinal) {
          final += event.results[i][0].transcript;
        } else {
          interim += event.results[i][0].transcript;
        }
      }
      // 将识别结果实时回调给页面
      if (onTextUpdate) onTextUpdate(final, interim);
    };

    recognition.onerror = (event) => {
      console.error('语音识别错误:', event.error);
      isRecording.value = false;
    };

    recognition.onend = () => {
      isRecording.value = false;
    };

    return true;
  };

  const toggleVoice = () => {
    if (!recognition && !initSpeechRecognition()) return;
    
    if (isRecording.value) {
      recognition.stop();
    } else {
      try { recognition.start(); } catch (e) { console.error(e); }
    }
  };

  const stopVoice = () => {
    if (recognition && isRecording.value) {
      recognition.stop();
    }
  };

  // 组件销毁时自动清理麦克风占用
  onUnmounted(() => {
    stopVoice();
  });

  // 暴露出页面需要的状态和方法
  return {
    isRecording,
    toggleVoice,
    stopVoice
  };
}