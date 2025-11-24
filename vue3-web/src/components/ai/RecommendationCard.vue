<template>
  <div class="recommendation-card">
    <div class="card-header">
      <h4 class="card-title">{{ recommendation.title }}</h4>
      <div class="match-score">
        <span class="score-label">匹配度</span>
        <span class="score-value">{{ Math.round(recommendation.score || 0) }}%</span>
      </div>
    </div>
    
    <div class="card-content">
      <p class="card-description">{{ recommendation.description }}</p>
      <div class="card-meta">
        <span class="meta-item">{{ recommendation.category }}</span>
        <span class="meta-item">{{ recommendation.organizer }}</span>
      </div>
      
      <div class="ai-suggestions" v-if="recommendation.suggestions">
        <h5>AI建议：</h5>
        <p>{{ recommendation.suggestions }}</p>
      </div>
    </div>
    
    <div class="card-actions">
      <button 
        @click="$emit('view-competition', recommendation.id)" 
        class="btn btn-primary"
      >
        查看详情
      </button>
      <button 
        @click="$emit('get-advice', recommendation)" 
        class="btn btn-secondary"
      >
        获取建议
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  recommendation: {
    type: Object,
    required: true
  }
})

defineEmits(['view-competition', 'get-advice'])
</script>

<style scoped>
@import '../styles/AI-component.css';
</style>