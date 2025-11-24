<template>
  <div class="loading-state" :class="sizeClass">
    <div class="loading-spinner"></div>
    <p v-if="message" class="loading-message">{{ message }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  message: {
    type: String,
    default: '加载中...'
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})

const sizeClass = computed(() => `loading-${props.size}`)
</script>

<style scoped>
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
  min-height: 300px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  box-shadow: 0 0 20px rgba(79, 70, 229, 0.2);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-message {
  margin-top: 1.5rem;
  font-size: 1.1rem;
  color: #94a3b8;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.loading-small .loading-spinner {
  width: 24px;
  height: 24px;
  border-width: 2px;
}

.loading-small .loading-message {
  font-size: 0.9rem;
  margin-top: 0.75rem;
}

.loading-large .loading-spinner {
  width: 64px;
  height: 64px;
  border-width: 4px;
}

.loading-large .loading-message {
  font-size: 1.25rem;
  margin-top: 2rem;
}
</style>