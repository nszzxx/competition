import { ref, onMounted, onUnmounted } from 'vue'

export function useVoiceRecognition() {
  const isSupported = ref(false)
  const isListening = ref(false)
  const transcript = ref('')
  const error = ref(null)
  
  let recognition = null

  onMounted(() => {
    // 检查浏览器是否支持语音识别
    if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
      isSupported.value = true
      
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
      recognition = new SpeechRecognition()
      
      // 配置语音识别
      recognition.continuous = false
      recognition.interimResults = true
      recognition.lang = 'zh-CN'
      
      // 监听识别结果
      recognition.onresult = (event) => {
        let finalTranscript = ''
        let interimTranscript = ''
        
        for (let i = event.resultIndex; i < event.results.length; i++) {
          const result = event.results[i]
          if (result.isFinal) {
            finalTranscript += result[0].transcript
          } else {
            interimTranscript += result[0].transcript
          }
        }
        
        transcript.value = finalTranscript || interimTranscript
      }
      
      // 监听识别开始
      recognition.onstart = () => {
        isListening.value = true
        error.value = null
      }
      
      // 监听识别结束
      recognition.onend = () => {
        isListening.value = false
      }
      
      // 监听识别错误
      recognition.onerror = (event) => {
        error.value = event.error
        isListening.value = false
        console.error('语音识别错误:', event.error)
      }
    }
  })

  onUnmounted(() => {
    if (recognition) {
      recognition.abort()
    }
  })

  // 开始语音识别
  const startListening = () => {
    if (!isSupported.value || !recognition) {
      error.value = '浏览器不支持语音识别'
      return
    }
    
    if (isListening.value) {
      return
    }
    
    transcript.value = ''
    error.value = null
    
    try {
      recognition.start()
    } catch (err) {
      error.value = '启动语音识别失败'
      console.error('启动语音识别失败:', err)
    }
  }

  // 停止语音识别
  const stopListening = () => {
    if (recognition && isListening.value) {
      recognition.stop()
    }
  }

  // 切换语音识别状态
  const toggleListening = () => {
    if (isListening.value) {
      stopListening()
    } else {
      startListening()
    }
  }

  // 清空识别结果
  const clearTranscript = () => {
    transcript.value = ''
    error.value = null
  }

  return {
    isSupported,
    isListening,
    transcript,
    error,
    startListening,
    stopListening,
    toggleListening,
    clearTranscript
  }
}

export default useVoiceRecognition