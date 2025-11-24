<template>
  <div class="ai-center-page">
    <!-- 通用导航条 -->
    <Header />
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1>🤖 AI智能助手中心</h1>
          <p>基于大模型的竞赛辅助系统，为您提供个性化的竞赛建议和智能分析</p>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <span class="stat-number">{{ totalRecommendations }}</span>
            <span class="stat-label">推荐竞赛</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ chatMessages }}</span>
            <span class="stat-label">对话次数</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ analysisCount }}</span>
            <span class="stat-label">技能分析</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="page-content">
      <!-- AI功能导航 -->
      <nav class="ai-nav">
        <button
          v-for="tab in aiTabs"
          :key="tab.id"
          @click="activeTab = tab.id"
          class="nav-btn"
          :class="{ active: activeTab === tab.id }"
        >
          <span class="nav-icon">{{ tab.icon }}</span>
          <span class="nav-text">{{ tab.name }}</span>
        </button>
      </nav>

      <!-- 智能搜索区域 -->
      <section v-if="activeTab === 'search'" class="ai-section">
        <div class="section-header">
          <h2>🔍 智能搜索</h2>
          <p>使用自然语言描述您的需求，AI将为您找到最合适的竞赛</p>
        </div>
        
        <div class="search-container">
          <AISearchBox @search="handleSearch" @aiRecommend="handleAIRecommend" />
        </div>
        
        <!-- AI推荐结果 -->
        <div v-if="aiRecommendResults.length > 0" class="search-results">
          <h3>🤖 AI推荐结果</h3>
          <div class="results-grid">
            <div
              v-for="result in aiRecommendResults"
              :key="result.id"
              class="result-card"
              @click="viewCompetition(result.id)"
            >
              <h4>{{ result.title }}</h4>
              <p>{{ result.description }}</p>
              <div class="result-meta">
                <span class="category">{{ result.category }}</span>
                <span class="match-score">匹配度: {{ result.matchScore }}%</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 搜索结果 -->
        <div v-if="searchResults.length > 0" class="search-results">
          <h3>📋 搜索结果</h3>
          <div class="results-grid">
            <div
              v-for="result in searchResults"
              :key="result.id"
              class="result-card"
              @click="viewCompetition(result.id)"
            >
              <h4>{{ result.title }}</h4>
              <p>{{ result.description }}</p>
              <div class="result-meta">
                <span class="category">{{ result.category }}</span>
                <span class="match-score">匹配度: {{ result.matchScore }}%</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="!loading" class="no-recommendations">
          <p>抱歉，没有找到符合条件的推荐竞赛。</p>
          <p>您可以尝试更改过滤条件或点击"获取推荐"按钮重新获取。</p>
        </div>
      </section>

      <!-- 个性化推荐 -->
      <section v-if="activeTab === 'recommend'" class="ai-section">
        <div class="section-header">
          <h2>🎯 个性化推荐</h2>
          <p>基于您的技能、兴趣和历史参赛记录，为您推荐最适合的竞赛</p>
        </div>
        
        <div class="recommend-controls">
          <button @click="getPersonalizedRecommendations(true)" class="btn btn-primary" :disabled="loading">
            {{ loading ? '分析中...' : '获取推荐' }}
          </button>
          <div class="filter-options">
            <select v-model="recommendFilter.category" class="form-select">
              <option value="">所有类别</option>
              <option value="编程竞赛">编程竞赛</option>
              <option value="创业竞赛">创业竞赛</option>
              <option value="设计竞赛">设计竞赛</option>
            </select>
            <select v-model="recommendFilter.difficulty" class="form-select">
              <option value="">所有难度</option>
              <option value="初级">初级</option>
              <option value="中级">中级</option>
              <option value="高级">高级</option>
            </select>
          </div>
        </div>

        <!-- 推荐结果 -->
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>正在获取推荐结果...</p>
        </div>
        
        <div v-else-if="recommendations && recommendations.length > 0" class="recommendations">
          <div class="recommendation-summary">
            <h3>为您推荐了 {{ recommendations.length }} 个竞赛</h3>
            <p>{{ recommendationSummary }}</p>
          </div>
          
          <div class="recommendations-grid">
            <div
              v-for="rec in recommendations"
              :key="rec.id"
              class="recommendation-card"
              @click="viewCompetition(rec.id)"
            >
              <div class="card-header">
                <h4 class="card-title">{{ rec.title }}</h4>
                <div class="match-score">
                  <span class="score-label">匹配度</span>
                  <span class="score-value">{{ Math.round(rec.score || 0) }}%</span>
                </div>
              </div>
              <div class="card-content">
                <p class="card-description">{{ rec.description }}</p>
                <div class="card-meta">
                  <span class="meta-item">{{ rec.category }}</span>
                  <span class="meta-item">{{ rec.organizer }}</span>
                </div>
                <div class="ai-suggestions" v-if="rec.suggestions">
                  <h5>AI建议：</h5>
                  <p>{{ rec.suggestions }}</p>
                </div>
              </div>
              <div class="card-actions">
                <button class="btn btn-primary">查看详情</button>
                <button class="btn btn-secondary" @click.stop="getPreparationAdvice(rec)">获取建议</button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 技能分析 -->
      <section v-if="activeTab === 'analysis'" class="ai-section">
        <div class="section-header">
          <h2>📊 技能分析</h2>
          <p>AI分析您的技能水平，提供个性化的提升建议</p>
        </div>
        
        <button @click="analyzeSkills(true)" class="btn btn-primary" :disabled="loading">
          {{ loading ? '分析中...' : '开始分析' }}
        </button>

        <div v-if="skillAnalysis" class="skill-analysis">
          <div class="analysis-header">
            <h3>技能分析结果</h3>
            <div class="overall-score-card" v-if="skillAnalysis.overallScore">
              <div class="score-label">综合评分</div>
              <div class="score-value">{{ skillAnalysis.overallScore }}</div>
              <div class="score-hint">满分100分</div>
            </div>
          </div>
          <div class="analysis-content">
            <div class="skill-scores" v-if="skillAnalysis.skills && Object.keys(skillAnalysis.skills).length > 0">
              <h4>📊 技能评分详情</h4>
              <div class="skills-grid">
                <div
                  v-for="(score, skill) in skillAnalysis.skills"
                  :key="skill"
                  class="skill-item"
                >
                  <div class="skill-header">
                    <span class="skill-name">{{ skill }}</span>
                    <span class="skill-score-badge" :class="getScoreLevel(score)">
                      {{ score }}分
                    </span>
                  </div>
                  <div class="skill-score-bar">
                    <div
                      class="skill-score-fill"
                      :class="getScoreLevel(score)"
                      :style="{ width: `${score}%` }"
                    >
                      <span class="score-percentage">{{ score }}%</span>
                    </div>
                  </div>
                  <div class="skill-level-text">{{ getSkillLevelText(score) }}</div>
                </div>
              </div>
            </div>
            <div class="no-skills-message" v-else>
              <p>暂无技能评分数据</p>
              <p class="hint">请先在个人资料中添加您的技能信息</p>
            </div>
            <div class="ai-advice" v-if="skillAnalysis.advice">
              <h4>🤖 AI专业建议</h4>
              <div class="scrollable-content">
                <div class="advice-content" v-html="formatSkillAnalysis(skillAnalysis.advice)">
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-analysis">
          <p>暂无技能分析数据，请点击"开始分析"按钮进行分析。</p>
        </div>
      </section>

      <!-- 学习路径 -->
      <section v-if="activeTab === 'learning'" class="ai-section">
        <div class="section-header">
          <h2>🎓 学习路径</h2>
          <p>AI为您制定个性化的学习计划，助您在竞赛中取得佳绩</p>
        </div>
        
        <div class="learning-controls">
          <CompetitionSelector
            :participated-competitions="participatedCompetitions"
            :available-competitions="availableCompetitions"
            available-label="目标比赛"
            available-placeholder="选择目标比赛"
            v-model:model-participated="selectedParticipatedCompetition"
            v-model:model-available="selectedTargetCompetition"
          />
          <button
            @click="generateLearningPath(true)"
            :disabled="!selectedTargetCompetition || loading"
            class="btn btn-primary"
          >
            {{ loading ? '生成中...' : '生成学习路径' }}
          </button>
        </div>

        <div v-if="learningPath" class="learning-path">
          <div class="path-header">
            <h3>个性化学习路径</h3>
            <div class="path-meta">
              <span class="estimated-time">预计用时：{{ learningPath.estimatedTime }}</span>
              <span class="difficulty">难度：{{ learningPath.difficulty }}</span>
            </div>
          </div>
          <div class="path-content">
            <div class="learning-stages" v-if="learningPath.stages">
              <div 
                v-for="(stage, index) in learningPath.stages" 
                :key="index" 
                class="stage-item"
              >
                <div class="stage-number">{{ index + 1 }}</div>
                <div class="stage-content">
                  <h4 class="stage-title">{{ stage.title }}</h4>
                  <p class="stage-description">{{ stage.description }}</p>
                  <div class="stage-duration">预计时间：{{ stage.duration }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-path">
          <p>暂无学习路径，请选择目标竞赛并点击"生成学习路径"。</p>
        </div>
      </section>

      <!-- 竞赛趋势 -->
      <section v-if="activeTab === 'trends'" class="ai-section">
        <div class="section-header">
          <h2>📈 竞赛趋势</h2>
          <p>AI分析当前竞赛热点和发展趋势，帮您把握机会</p>
        </div>
        
        <div class="trends-controls">
          <CompetitionSelector
            :participated-competitions="participatedCompetitions"
            :available-competitions="availableCompetitions"
            v-model:model-participated="selectedParticipatedForTrends"
            v-model:model-available="selectedAvailableForTrends"
          />
          <button @click="getCompetitionTrends(true)" class="btn btn-primary" :disabled="loading">
            {{ loading ? '分析中...' : '获取趋势分析' }}
          </button>
        </div>

        <div v-if="trends" class="trends-analysis">
          <div class="trends-header">
            <h3>竞赛趋势分析</h3>
          </div>
          <div class="trends-content">
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
            <div class="ai-summary" v-if="trends.summary">
              <h4>🤖 AI分析总结</h4>
              <div class="scrollable-content">
                <div class="summary-content" v-html="formatTrendsAnalysis(trends.summary)">
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-trends">
          <p>暂无趋势分析数据，请点击"获取趋势分析"按钮进行分析。</p>
        </div>
      </section>
    </main>

    <!-- AI建议弹窗 -->
    <AIAdviceModal
      :visible="!!preparationAdvice || preparationAdviceLoading || !!preparationAdviceError"
      :competition="currentCompetition"
      :advice="preparationAdvice"
      :isLoading="preparationAdviceLoading"
      :error="preparationAdviceError"
      @close="closePreparationAdvice"
      @retry="retryPreparationAdvice"
    />
  </div>
</template>

<script setup>
// 组件导入
import Header from '../components/common/Header.vue'
import AISearchBox from '../components/ai/AISearchBox.vue'
import AIAdviceModal from '../components/ai/AIAdviceModal.vue'
import CompetitionSelector from '../components/competition/CompetitionSelector.vue'
import { formatSkillAnalysis, formatTrendsAnalysis } from '../utils/textFormatter.js'
import { watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 技能评分等级判断函数
const getScoreLevel = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 80) return 'good'
  if (score >= 70) return 'average'
  if (score >= 60) return 'fair'
  return 'poor'
}

// 技能等级文本描述
const getSkillLevelText = (score) => {
  if (score >= 90) return '优秀 - 已达到专家级水平'
  if (score >= 80) return '良好 - 熟练掌握该技能'
  if (score >= 70) return '中等 - 基本掌握该技能'
  if (score >= 60) return '及格 - 具备基础能力'
  return '待提升 - 需要加强学习'
}

// 使用AI中心组合式函数
import { useAICenter } from '../composables/useAI.js'

const {
  // 响应式状态
  activeTab,
  loading,
  searchResults,
  aiRecommendResults,
  recommendations,
  recommendationSummary,
  skillAnalysis,
  learningPath,
  trends,
  availableCompetitions,
  participatedCompetitions,
  selectedParticipatedCompetition,
  selectedTargetCompetition,
  selectedParticipatedForTrends,
  selectedAvailableForTrends,
  totalRecommendations,
  chatMessages,
  analysisCount,
  recommendFilter,
  aiTabs,
  preparationAdvice,
  preparationAdviceLoading,
  preparationAdviceError,
  currentCompetition,

  // 方法
  handleSearch,
  handleAIRecommend,
  getPersonalizedRecommendations,
  analyzeSkills,
  generateLearningPath,
  getCompetitionTrends,
  getPreparationAdvice,
  closePreparationAdvice,
  retryPreparationAdvice,
  viewCompetition
} = useAICenter()

// 检测滚动内容并设置属性
const checkScrollableContent = () => {
  nextTick(() => {
    const scrollableElements = document.querySelectorAll('.scrollable-content')
    scrollableElements.forEach(element => {
      const isScrollable = element.scrollHeight > element.clientHeight
      if (isScrollable) {
        element.setAttribute('data-scrollable', 'true')
      } else {
        element.removeAttribute('data-scrollable')
      }
    })
  })
}

// 监听标签页变化，自动加载相应的数据
watch(activeTab, (newTab) => {
  console.log('标签页切换到:', newTab)
  
  // 根据不同的标签页加载相应的数据
  if (newTab === 'recommend' && recommendations.value.length === 0) {
    console.log('自动加载推荐数据')
    getPersonalizedRecommendations(false)
  } else if (newTab === 'analysis' && !skillAnalysis.value) {
    console.log('自动加载技能分析数据')
    analyzeSkills(false)
  } else if (newTab === 'trends' && !trends.value) {
    console.log('自动加载趋势分析数据')
    getCompetitionTrends(false)
  }
  // 移除了学习路径的自动加载逻辑
})

// 监听技能分析和趋势数据变化，检测滚动内容
watch([skillAnalysis, trends], () => {
  checkScrollableContent()
})

// 移除了目标竞赛变化时自动更新学习路径的逻辑
</script>

<style scoped>
@import '../styles/views-ai.css';
</style>

<style>
@import '../styles/text-formatter.css';
</style>
