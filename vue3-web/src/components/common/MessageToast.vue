<template>
  <div class="message-toast" :class="[type, { show: visible }]" v-if="visible">
    <div class="toast-content">
      <span class="toast-icon">{{ getIcon() }}</span>
      <span class="toast-message">{{ message }}</span>
      <button class="toast-close" @click="close">×</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'

const props = defineProps({
  message: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'info', // success, error, warning, info
    validator: (value) => ['success', 'error', 'warning', 'info'].includes(value)
  },
  duration: {
    type: Number,
    default: 3000
  },
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const visible = ref(props.visible)

const getIcon = () => {
  switch (props.type) {
    case 'success':
      return '✅'
    case 'error':
      return '❌'
    case 'warning':
      return '⚠️'
    default:
      return 'ℹ️'
  }
}

const close = () => {
  visible.value = false
  emit('close')
}

// 自动关闭
let timer = null
watch(() => props.visible, (newVal) => {
  visible.value = newVal
  if (newVal && props.duration > 0) {
    timer = setTimeout(() => {
      close()
    }, props.duration)
  }
})

// 清理定时器
onUnmounted(() => {
  if (timer) {
    clearTimeout(timer)
  }
})
</script>

<style scoped>
.message-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  min-width: 300px;
  max-width: 500px;
  opacity: 0;
  transform: translateX(100%);
  transition: all 0.3s ease-in-out;
}

.message-toast.show {
  opacity: 1;
  transform: translateX(0);
}

.toast-content {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: white;
  border-left: 4px solid;
}

.message-toast.success .toast-content {
  border-left-color: #10b981;
  background: #f0fdf4;
}

.message-toast.error .toast-content {
  border-left-color: #ef4444;
  background: #fef2f2;
}

.message-toast.warning .toast-content {
  border-left-color: #f59e0b;
  background: #fffbeb;
}

.message-toast.info .toast-content {
  border-left-color: #3b82f6;
  background: #eff6ff;
}

.toast-icon {
  margin-right: 8px;
  font-size: 16px;
}

.toast-message {
  flex: 1;
  font-size: 14px;
  color: #374151;
}

.toast-close {
  background: none;
  border: none;
  font-size: 18px;
  color: #6b7280;
  cursor: pointer;
  padding: 0;
  margin-left: 8px;
}

.toast-close:hover {
  color: #374151;
}
</style>