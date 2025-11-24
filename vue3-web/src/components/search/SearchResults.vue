<template>
  <div class="search-results">
    <h3 class="results-title">{{ title }}</h3>
    <div class="results-list">
      <div 
        v-for="result in results" 
        :key="result.id" 
        class="result-item"
        @click="$emit('view-competition', result.id)"
      >
        <div class="result-header">
          <h4 class="result-title">{{ result.title }}</h4>
          <span class="result-category">{{ result.category }}</span>
        </div>
        <p class="result-description">{{ result.description }}</p>
        <div class="result-meta">
          <span class="result-organizer">{{ result.organizer }}</span>
          <span class="result-time">{{ formatDate(result.startTime) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  results: {
    type: Array,
    default: () => []
  }
})

defineEmits(['view-competition'])

const formatDate = (dateString) => {
  if (!dateString) return '待定'
  try {
    return new Date(dateString).toLocaleDateString('zh-CN')
  } catch {
    return dateString
  }
}
</script>

<style scoped>
@import '../styles/AI-component.css';
</style>