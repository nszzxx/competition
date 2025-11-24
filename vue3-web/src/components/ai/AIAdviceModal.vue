<template>
  <div v-if="visible" class="advice-modal-overlay" @click.self="handleClose">
    <div class="advice-modal" :class="{ loading: isLoading }">
      <!-- 关闭按钮 -->
      <button class="close-btn" @click="handleClose" aria-label="关闭">×</button>

      <!-- 标题 -->
      <div class="modal-header">
        <h3>
          <span class="icon">💡</span>
          {{ competition?.title || '竞赛' }} - AI备赛建议
        </h3>
      </div>

      <!-- 内容区域 -->
      <div class="modal-content">
        <!-- 加载状态 -->
        <div v-if="isLoading" class="loading-container">
          <div class="loading-spinner">
            <div class="spinner"></div>
          </div>
          <p class="loading-text">{{ loadingText }}</p>
        </div>

        <!-- 建议内容 -->
        <div v-else-if="advice" class="advice-content">
          <div class="advice-text" v-html="formattedAdvice"></div>
          <!-- 打字光标 - 只在打字时显示 -->
          <span v-if="isTyping" class="typing-cursor">|</span>
        </div>

        <!-- ���误状态 -->
        <div v-else-if="error" class="error-container">
          <div class="error-icon">⚠️</div>
          <p class="error-text">{{ error }}</p>
          <button class="retry-btn" @click="handleRetry">重试</button>
        </div>
      </div>

      <!-- 底部操作 -->
      <div v-if="!isLoading" class="modal-footer">
        <button class="btn btn-secondary" @click="handleClose">关闭</button>
        <button v-if="advice" class="btn btn-primary" @click="handleCopy">
          <span class="icon">📋</span>
          复制建议
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { formatAIMessage, createTypewriterEffect } from '../../utils/textFormatter.js'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  competition: {
    type: Object,
    default: null
  },
  advice: {
    type: String,
    default: ''
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['close', 'retry'])

// 本地状态
const displayedAdvice = ref('')
const isTyping = ref(false)
const typewriterController = ref(null)
const loadingDots = ref(0)
const loadingTimer = ref(null)

// 加载文字动态显示
const loadingText = computed(() => {
  const dots = '.'.repeat(loadingDots.value)
  return `正在生成AI建议${dots}`
})

// 格式化后的建议文本
const formattedAdvice = computed(() => {
  return formatAIMessage(displayedAdvice.value)
})

// 监听加载状态,控制加载动画
watch(() => props.isLoading, (newValue) => {
  if (newValue) {
    // 开始加载动画
    loadingDots.value = 0
    loadingTimer.value = setInterval(() => {
      loadingDots.value = (loadingDots.value + 1) % 4
    }, 500)
  } else {
    // 停止加载动画
    if (loadingTimer.value) {
      clearInterval(loadingTimer.value)
      loadingTimer.value = null
    }
  }
})

// 监听建议内容变化,启动打字机效果
watch(() => props.advice, (newAdvice) => {
  if (newAdvice && !props.isLoading) {
    // 停止之前的打字机效果
    if (typewriterController.value) {
      typewriterController.value.stop()
      typewriterController.value = null
    }

    // 重置显示内容
    displayedAdvice.value = ''
    isTyping.value = true

    // 创建新的打字机效果
    typewriterController.value = createTypewriterEffect(
      newAdvice,
      (currentText) => {
        displayedAdvice.value = currentText
      },
      30 // 30ms/字符
    )

    // 启动打字机
    typewriterController.value.start()

    // 检查打字完成
    const checkComplete = setInterval(() => {
      if (typewriterController.value && typewriterController.value.isComplete()) {
        clearInterval(checkComplete)
        isTyping.value = false
        typewriterController.value = null
      }
    }, 100)
  }
})

// 监听弹窗关闭,清理打字机效果
watch(() => props.visible, (newVisible) => {
  if (!newVisible) {
    // 清理打字机效果
    if (typewriterController.value) {
      typewriterController.value.stop()
      typewriterController.value = null
    }
    displayedAdvice.value = ''
    isTyping.value = false

    // 清理加载定时器
    if (loadingTimer.value) {
      clearInterval(loadingTimer.value)
      loadingTimer.value = null
    }
  }
})

// 关闭弹窗
const handleClose = () => {
  emit('close')
}

// 重试
const handleRetry = () => {
  emit('retry')
}

// 复制建议
const handleCopy = async () => {
  try {
    // 获取纯文本内容（移除HTML标签）
    const tempDiv = document.createElement('div')
    tempDiv.innerHTML = formattedAdvice.value
    const textContent = tempDiv.textContent || tempDiv.innerText || ''

    await navigator.clipboard.writeText(textContent)
    alert('建议已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)

    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = displayedAdvice.value
    textArea.style.position = 'fixed'
    textArea.style.opacity = '0'
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      alert('建议已复制到剪贴板')
    } catch (err) {
      alert('复制失败，请手动复制')
    }
    document.body.removeChild(textArea)
  }
}
</script>

<style scoped>
/* 弹窗遮罩 */
.advice-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 弹窗主体 */
.advice-modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 700px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  font-size: 24px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 1;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  color: #333;
}

/* 标题 */
.modal-header {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 40px; /* 为关闭按钮留空间 */
}

.modal-header .icon {
  font-size: 24px;
}

/* 内容区域 */
.modal-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  min-height: 200px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  gap: 20px;
}

.loading-spinner {
  position: relative;
  width: 60px;
  height: 60px;
}

.spinner {
  width: 100%;
  height: 100%;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 16px;
  color: #666;
  margin: 0;
  font-weight: 500;
  min-width: 160px;
  text-align: center;
}

/* 建议内容 */
.advice-content {
  animation: fadeIn 0.3s ease;
}

.advice-text {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  display: inline;
}

/* 打字光标 */
.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 1.2em;
  background-color: #667eea;
  margin-left: 2px;
  animation: blink 1s infinite;
  vertical-align: text-bottom;
}

@keyframes blink {
  0%, 49% {
    opacity: 1;
  }
  50%, 100% {
    opacity: 0;
  }
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  gap: 16px;
}

.error-icon {
  font-size: 48px;
}

.error-text {
  font-size: 16px;
  color: #dc3545;
  margin: 0;
  text-align: center;
}

.retry-btn {
  padding: 10px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.retry-btn:hover {
  background: #5568d3;
  transform: translateY(-1px);
}

/* 底部操作 */
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-secondary {
  background: #f8f9fa;
  color: #495057;
}

.btn-secondary:hover {
  background: #e9ecef;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 自定义滚动条 */
.modal-content::-webkit-scrollbar {
  width: 6px;
}

.modal-content::-webkit-scrollbar-track {
  background: transparent;
}

.modal-content::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.modal-content::-webkit-scrollbar-thumb:hover {
  background: #bbb;
}

/* 响应式 */
@media (max-width: 768px) {
  .advice-modal {
    width: 95%;
    max-height: 90vh;
  }

  .modal-header h3 {
    font-size: 18px;
  }

  .advice-text {
    font-size: 14px;
  }
}
</style>
