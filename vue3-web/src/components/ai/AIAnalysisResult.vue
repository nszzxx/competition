<template>
  <div v-if="analysis" class="ai-analysis-result">
    <div class="analysis-header">
      <h3>🤖 AI分析结果</h3>
      <button @click="$emit('close')" class="close-btn">×</button>
    </div>
    
    <div class="analysis-content">
      <div class="analysis-section">
        <h4>意图理解</h4>
        <p>{{ analysis.intent }}</p>
      </div>
      
      <div class="analysis-section">
        <h4>分析推理</h4>
        <p>{{ analysis.reasoning }}</p>
      </div>
      
      <div v-if="analysis.skillMatches && analysis.skillMatches.length > 0" class="analysis-section">
        <h4>技能匹配</h4>
        <div class="skill-tags">
          <span
            v-for="skill in analysis.skillMatches"
            :key="skill"
            class="skill-tag"
          >
            {{ skill }}
          </span>
        </div>
      </div>
      
      <div v-if="analysis.suggestions && analysis.suggestions.length > 0" class="analysis-section">
        <h4>推荐建议</h4>
        <ul class="suggestions-list">
          <li v-for="suggestion in analysis.suggestions" :key="suggestion">
            {{ suggestion }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  analysis: {
    type: Object,
    default: null
  }
})

defineEmits(['close'])
</script>

<style scoped>
@import '../../styles/ai-component.css';
</style>