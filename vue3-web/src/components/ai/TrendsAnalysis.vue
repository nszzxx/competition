<template>
  <div class="trends-analysis" v-if="trends">
    <div class="trends-header">
      <h3>竞赛趋势分析</h3>
      <div class="analysis-date">
        分析时间：{{ formatDate(trends.createdAt) }}
      </div>
    </div>
    
    <div class="trends-content">
      <!-- 热门竞赛类别 -->
      <div class="hot-categories" v-if="trends.hotCategories">
        <h4>🔥 热门竞赛类别</h4>
        <div class="categories-grid">
          <div 
            v-for="category in trends.hotCategories" 
            :key="category.name" 
            class="category-item"
          >
            <div class="category-name">{{ category.name }}</div>
            <div class="category-stats">
              <span class="participation">参与度：{{ category.participation }}%</span>
              <span class="growth">增长率：{{ category.growth }}%</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 技能需求趋势 -->
      <div class="skill-trends" v-if="trends.skillTrends">
        <h4>📈 技能需求趋势</h4>
        <div class="skills-chart">
          <div 
            v-for="skill in trends.skillTrends" 
            :key="skill.name" 
            class="skill-trend-item"
          >
            <div class="skill-info">
              <span class="skill-name">{{ skill.name }}</span>
              <span class="skill-demand">需求度：{{ skill.demand }}%</span>
            </div>
            <div class="skill-bar">
              <div 
                class="skill-fill" 
                :style="{ width: skill.demand + '%' }"
              ></div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 竞赛发展预测 -->
      <div class="predictions" v-if="trends.predictions">
        <h4>🔮 发展预测</h4>
        <div class="predictions-list">
          <div 
            v-for="prediction in trends.predictions" 
            :key="prediction.id" 
            class="prediction-item"
          >
            <div class="prediction-title">{{ prediction.title }}</div>
            <div class="prediction-content">{{ prediction.content }}</div>
            <div class="prediction-confidence">
              可信度：{{ prediction.confidence }}%
            </div>
          </div>
        </div>
      </div>
      
      <!-- AI分析总结 -->
      <div class="ai-summary" v-if="trends.summary">
        <h4>🤖 AI分析总结</h4>
        <div class="summary-content" v-html="formatTrendsText(trends.summary)">
        </div>
      </div>
      
      <!-- 完整趋势报告 -->
      <div class="full-trends-report" v-if="trends.fullReport">
        <h4>📊 完整趋势报告</h4>
        <div class="trends-report" v-html="formatTrendsText(trends.fullReport)">
        </div>
      </div>
      
      <!-- 建议行动 -->
      <div class="recommendations" v-if="trends.recommendations">
        <h4>💡 建议行动</h4>
        <ul class="recommendations-list">
          <li v-for="rec in trends.recommendations" :key="rec.id">
            <strong>{{ rec.title }}：</strong>{{ rec.description }}
          </li>
        </ul>
      </div>
    </div>
  </div>
  
  <div v-else class="no-trends">
    <p>暂无趋势分析数据，请点击"获取趋势分析"按钮进行分析。</p>
  </div>
</template>

<script setup>
import { formatTrendsAnalysis } from '../../utils/textFormatter.js'

defineProps({
  trends: {
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

// 格式化趋势分析文本
const formatTrendsText = (text) => {
  return formatTrendsAnalysis(text)
}
</script>

<style scoped>
@import '../styles/AI-component.css';
@import '../../styles/text-formatter.css';
</style>
