import { ref, computed } from 'vue'

const STORAGE_KEY = 'competition_search_history'
const MAX_HISTORY_SIZE = 20

export function useSearchHistory() {
  const searchHistory = ref([])

  // 从本地存储加载搜索历史
  const loadHistory = () => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY)
      if (stored) {
        searchHistory.value = JSON.parse(stored)
      }
    } catch (error) {
      console.error('加载搜索历史失败:', error)
      searchHistory.value = []
    }
  }

  // 保存搜索历史到本地存储
  const saveHistory = () => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(searchHistory.value))
    } catch (error) {
      console.error('保存搜索历史失败:', error)
    }
  }

  // 添加搜索记录
  const addSearchRecord = (query) => {
    if (!query || !query.trim()) {
      return
    }

    const trimmedQuery = query.trim()
    
    // 移除重复项
    const existingIndex = searchHistory.value.findIndex(
      item => item.query.toLowerCase() === trimmedQuery.toLowerCase()
    )
    
    if (existingIndex > -1) {
      searchHistory.value.splice(existingIndex, 1)
    }

    // 添加到开头
    searchHistory.value.unshift({
      query: trimmedQuery,
      timestamp: Date.now(),
      count: 1
    })

    // 限制历史记录数量
    if (searchHistory.value.length > MAX_HISTORY_SIZE) {
      searchHistory.value = searchHistory.value.slice(0, MAX_HISTORY_SIZE)
    }

    saveHistory()
  }

  // 删除搜索记录
  const removeSearchRecord = (index) => {
    if (index >= 0 && index < searchHistory.value.length) {
      searchHistory.value.splice(index, 1)
      saveHistory()
    }
  }

  // 清空搜索历史
  const clearHistory = () => {
    searchHistory.value = []
    saveHistory()
  }

  // 获取搜索建议
  const getSearchSuggestions = (query, limit = 5) => {
    if (!query || !query.trim()) {
      return []
    }

    const trimmedQuery = query.trim().toLowerCase()
    
    return searchHistory.value
      .filter(item => 
        item.query.toLowerCase().includes(trimmedQuery) &&
        item.query.toLowerCase() !== trimmedQuery
      )
      .slice(0, limit)
      .map(item => item.query)
  }

  // 获取热门搜索
  const getPopularSearches = (limit = 10) => {
    return searchHistory.value
      .sort((a, b) => b.count - a.count)
      .slice(0, limit)
      .map(item => item.query)
  }

  // 获取最近搜索
  const getRecentSearches = (limit = 10) => {
    return searchHistory.value
      .sort((a, b) => b.timestamp - a.timestamp)
      .slice(0, limit)
      .map(item => item.query)
  }

  // 计算属性：是否有搜索历史
  const hasHistory = computed(() => searchHistory.value.length > 0)

  // 计算属性：搜索历史数量
  const historyCount = computed(() => searchHistory.value.length)

  // 初始化时加载历史记录
  loadHistory()

  return {
    searchHistory,
    hasHistory,
    historyCount,
    addSearchRecord,
    removeSearchRecord,
    clearHistory,
    getSearchSuggestions,
    getPopularSearches,
    getRecentSearches,
    loadHistory,
    saveHistory
  }
}

export default useSearchHistory