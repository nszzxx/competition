<template>
  <div v-if="visible && history.length > 0" class="search-history">
    <div class="history-header">
      <span class="history-title">📚 搜索历史</span>
      <button @click="$emit('clear')" class="clear-btn">清除</button>
    </div>
    <div class="history-items">
      <span
        v-for="item in history"
        :key="item"
        @click="$emit('select', item)"
        class="history-item"
      >
        {{ item }}
      </span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  history: {
    type: Array,
    default: () => []
  }
})

defineEmits(['select', 'clear'])
</script>

<style scoped>
.search-history {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 12px;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 16px;
  z-index: 100;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.history-title {
  font-size: 0.9rem;
  color: #94a3b8;
  font-weight: 600;
}

.clear-btn {
  background: transparent;
  border: none;
  color: #64748b;
  font-size: 0.85rem;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.history-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-item {
  background: rgba(255, 255, 255, 0.05);
  color: #e2e8f0;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.history-item:hover {
  background: rgba(79, 70, 229, 0.1);
  color: #818cf8;
  border-color: rgba(79, 70, 229, 0.3);
  transform: translateY(-1px);
}
</style>
