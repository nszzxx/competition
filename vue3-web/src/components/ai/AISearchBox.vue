<template>
  <div class="ai-search-container">
    <div class="search-box" ref="searchBoxRef">
      <SearchInput
        v-model="searchQuery"
        :loading="loading"
        @search="handleSearch"
        @voice-toggle="handleVoiceToggle"
        @focus="showHistory = true"
        ref="searchInputRef"
      />
      
      <SearchHistory
        :visible="showHistory"
        :history="searchHistory"
        @select="selectHistoryItem"
        @clear="clearHistory"
      />
    </div>
    
    <AIAnalysisResult
      :analysis="aiAnalysis"
      @close="aiAnalysis = null"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import SearchInput from '../search/SearchInput.vue'
import SearchHistory from '../search/SearchHistory.vue'
import AIAnalysisResult from '../ai/AIAnalysisResult.vue'

const emit = defineEmits(['search', 'aiRecommend'])

// 响应式数据
const searchQuery = ref('')
const showHistory = ref(false)
const aiAnalysis = ref(null)
const loading = ref(false)
const searchBoxRef = ref(null)
const searchInputRef = ref(null)
const searchHistory = ref([])

onMounted(() => {
  loadSearchHistory()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 加载搜索历史
const loadSearchHistory = () => {
  const history = localStorage.getItem('searchHistory')
  if (history) {
    try {
      searchHistory.value = JSON.parse(history)
    } catch (e) {
      searchHistory.value = []
    }
  }
}

// 保存搜索历史
const saveToHistory = (query) => {
  if (!searchHistory.value.includes(query)) {
    searchHistory.value.unshift(query)
    if (searchHistory.value.length > 10) {
      searchHistory.value = searchHistory.value.slice(0, 10)
    }
    localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
  }
}

// 事件处理方法
const handleSearch = async () => {
  if (!searchQuery.value.trim()) return
  
  const query = searchQuery.value.trim()
  saveToHistory(query)
  showHistory.value = false
  emit('search', query)
}

const handleAIRecommend = async () => {
  if (!searchQuery.value.trim()) return
  
  loading.value = true
  try {
    const query = searchQuery.value.trim()
    console.log('执行AI推荐搜索:', query)
    
    // 调用AI推荐API
    const result = await fetch('/api/ai/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        query: query,
        userId: getCurrentUser()?.id
      })
    }).then(res => res.json())
    
    aiAnalysis.value = {
      intent: `您想要搜索关于"${query}"的竞赛`,
      reasoning: '基于您的搜索关键词和个人资料，AI为您分析了相关竞赛',
      skillMatches: ['编程', '算法', '创新'],
      suggestions: result.map(comp => comp.title).slice(0, 3)
    }
    
    saveToHistory(query)
    emit('aiRecommend', { competitions: result, analysis: aiAnalysis.value })
  } catch (error) {
    console.error('AI推荐搜索失败:', error)
    aiAnalysis.value = {
      intent: '抱歉，AI分析服务暂时不可用',
      reasoning: '请稍后再试，或直接使用普通搜索功能',
      skillMatches: [],
      suggestions: ['尝试使用更具体的关键词', '检查网络连接', '稍后重试']
    }
  } finally {
    loading.value = false
    showHistory.value = false
  }
}

// 获取当前用户信息
const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch (e) {
      console.warn('解析用户信息失败:', e)
    }
  }
  return null
}

const handleVoiceToggle = (listening) => {
  console.log('语音识别:', listening ? '开始' : '结束')
}

const selectHistoryItem = (item) => {
  searchQuery.value = item
  showHistory.value = false
  emit('search', item)
}

const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('searchHistory')
  showHistory.value = false
}

const handleClickOutside = (event) => {
  if (!event.target.closest('.ai-search-container')) {
    showHistory.value = false
  }
}

// 暴露方法给父组件
defineExpose({
  focus: () => {
    if (searchInputRef.value) {
      searchInputRef.value.focus()
    }
  },
  clear: () => {
    searchQuery.value = ''
    aiAnalysis.value = null
  }
})
</script>

<style scoped>
.ai-search-container {
  position: relative;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  z-index: 10;
}

.search-box {
  position: relative;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 16px;
}
</style>