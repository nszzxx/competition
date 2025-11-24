<template>
  <div class="skill-analysis" v-if="skillAnalysis">
    <div class="analysis-header">
      <h3>技能分析结果</h3>
      <div class="analysis-date">
        分析时间：{{ formatDate(skillAnalysis.createdAt) }}
      </div>
    </div>
    
    <div class="analysis-content">
      <!-- 技能评分 -->
      <div class="skill-scores" v-if="skillAnalysis.skills">
        <h4>技能评分</h4>
        <div class="skills-grid">
          <div 
            v-for="(score, skill) in skillAnalysis.skills" 
            :key="skill" 
            class="skill-item"
          >
            <div class="skill-name">{{ skill }}</div>
            <div class="skill-score-bar">
              <div 
                class="skill-score-fill" 
                :style="{ width: score + '%' }"
              ></div>
              <span class="skill-score-text">{{ score }}分</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- AI建议 -->
      <div class="ai-advice" v-if="skillAnalysis.advice">
        <h4>AI建议</h4>
        <div class="advice-content" v-html="formatAnalysisText(skillAnalysis.advice)">
        </div>
      </div>
      
      <!-- 完整分析报告 -->
      <div class="full-analysis" v-if="skillAnalysis.fullReport">
        <h4>详细分析报告</h4>
        <div class="analysis-report" v-html="formatAnalysisText(skillAnalysis.fullReport)">
        </div>
      </div>
      
      <!-- 推荐学习资源 -->
      <div class="learning-resources" v-if="skillAnalysis.resources">
        <h4>推荐学习资源</h4>
        <ul class="resources-list">
          <li v-for="resource in skillAnalysis.resources" :key="resource.id">
            <a :href="resource.url" target="_blank">{{ resource.title }}</a>
            <span class="resource-type">{{ resource.type }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
  
  <div v-else class="no-analysis">
    <p>暂无技能分析数据，请点击"开始分析"按钮进行分析。</p>
  </div>
</template>

<script setup>
import { formatSkillAnalysis } from '../../utils/textFormatter.js'

defineProps({
  skillAnalysis: {
    type: Object,
    default: null
  }
})

const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    return new Date(dateString).toLocaleString('zh-CN')
  } catch {
    return dateString
  }
}

// 格式化技能分析文本
const formatAnalysisText = (text) => {
  return formatSkillAnalysis(text)
}
</script>

<style scoped>
@import '../styles/ai-component.css';
@import '../../styles/text-formatter.css';
</style>
