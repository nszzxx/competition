<template>
  <div class="search-input-wrapper">
    <input
      v-model="query"
      @input="handleInput"
      @focus="$emit('focus')"
      @blur="$emit('blur')"
      :placeholder="placeholder"
      class="search-input"
    />
    <div class="search-actions">
      <button 
        @click="toggleVoice" 
        class="action-btn voice-btn" 
        :class="{ active: isListening }"
        :title="isListening ? '停止录音' : '语音搜索'"
      >
        🎤
      </button>
      <button 
        @click="handleSearch" 
        class="action-btn search-btn" 
        :disabled="loading"
        :title="loading ? '搜索中...' : 'AI智能搜索'"
      >
        {{ loading ? '🔄' : '🤖' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '🤖 智能搜索竞赛（支持自然语言描述）'
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'voice-toggle', 'focus', 'blur'])

const query = ref(props.modelValue)
const isListening = ref(false)

watch(() => props.modelValue, (newVal) => {
  query.value = newVal
})

watch(query, (newVal) => {
  emit('update:modelValue', newVal)
})

const handleInput = () => {
  emit('update:modelValue', query.value)
}

const handleSearch = () => {
  if (query.value.trim()) {
    emit('search', query.value.trim())
  }
}

const toggleVoice = () => {
  isListening.value = !isListening.value
  emit('voice-toggle', isListening.value)
}

// 暴露方法给父组件
defineExpose({
  focus: () => {
    // 可以在这里添加聚焦逻辑
  },
  setListening: (listening) => {
    isListening.value = listening
  }
})
</script>

<style scoped>
.search-input-wrapper {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 8px 16px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.search-input-wrapper:focus-within {
  background: rgba(255, 255, 255, 0.1);
  border-color: #4f46e5;
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
}

.search-input {
  flex: 1;
  background: transparent;
  border: none;
  color: white;
  font-size: 1.1rem;
  padding: 12px 0;
  outline: none;
}

.search-input::placeholder {
  color: #94a3b8;
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  font-size: 1.2rem;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.voice-btn {
  color: #94a3b8;
}

.voice-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.voice-btn.active {
  color: #ef4444;
  animation: pulse 1.5s infinite;
}

.search-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  border-radius: 10px;
  padding: 8px 12px;
}

.search-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.search-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
}
</style>
