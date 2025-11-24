import { ref, computed } from 'vue'
import { competitionApi } from '../utils/api.js'

export function useCompetitions() {
  // 响应式状态
  const competitions = ref([])
  const loading = ref(false)
  const error = ref(null)
  const selectedCategory = ref('')
  const searchKeyword = ref('')

  // 计算属性 - 过滤后的竞赛列表
  const filteredCompetitions = computed(() => {
    let filtered = competitions.value

    // 按类别过滤
    if (selectedCategory.value) {
      filtered = filtered.filter(comp => comp.category === selectedCategory.value)
    }

    // 按关键词搜索
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      filtered = filtered.filter(comp => 
        comp.title.toLowerCase().includes(keyword) ||
        comp.description.toLowerCase().includes(keyword) ||
        comp.organizer.toLowerCase().includes(keyword) ||
        (comp.tags && comp.tags.some(tag => tag.toLowerCase().includes(keyword)))
      )
    }

    return filtered
  })

  // 获取所有竞赛
  const fetchCompetitions = async () => {
    loading.value = true
    error.value = null
    
    try {
      const response = await competitionApi.getAll()
      competitions.value = response.data || response || []
    } catch (err) {
      console.error('获取竞赛列表失败:', err)
      error.value = '获取竞赛列表失败，请稍后重试'
      competitions.value = []
    } finally {
      loading.value = false
    }
  }

  // 根据类别获取竞赛
  const fetchCompetitionsByCategory = async (category) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await competitionApi.search({ category })
      competitions.value = response.data || response || []
    } catch (err) {
      console.error('按类别获取竞赛失败:', err)
      error.value = '获取竞赛失败，请稍后重试'
      competitions.value = []
    } finally {
      loading.value = false
    }
  }

  // 搜索竞赛
  const searchCompetitions = async (keyword) => {
    if (!keyword.trim()) {
      await fetchCompetitions()
      return
    }

    loading.value = true
    error.value = null
    
    try {
      const response = await competitionApi.search({ keyword })
      competitions.value = response.data || response || []
    } catch (err) {
      console.error('搜索竞赛失败:', err)
      error.value = '搜索失败，请稍后重试'
      competitions.value = []
    } finally {
      loading.value = false
    }
  }

  // 根据ID获取竞赛详情
  const getCompetitionById = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await competitionApi.getById(id)
      return response.data || response
    } catch (err) {
      console.error('获取竞赛详情失败:', err)
      error.value = '获取竞赛详情失败，请稍后重试'
      return null
    } finally {
      loading.value = false
    }
  }

  // 获取热门竞赛
  const fetchPopularCompetitions = async (limit = 10) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await competitionApi.getPopular(limit)
      competitions.value = response.data || response || []
    } catch (err) {
      console.error('获取热门竞赛失败:', err)
      error.value = '获取热门竞赛失败，请稍后重试'
      competitions.value = []
    } finally {
      loading.value = false
    }
  }

  // 获取即将开始的竞赛
  const fetchUpcomingCompetitions = async (limit = 10) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await competitionApi.getUpcoming(limit)
      competitions.value = response.data || response || []
    } catch (err) {
      console.error('获取即将开始的竞赛失败:', err)
      error.value = '获取即将开始的竞赛失败，请稍后重试'
      competitions.value = []
    } finally {
      loading.value = false
    }
  }

  // 根据参赛模式获取竞赛
  const fetchCompetitionsByMode = async (mode) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await competitionApi.getByMode(mode)
      competitions.value = response.data || response || []
    } catch (err) {
      console.error('按参赛模式获取竞赛失败:', err)
      error.value = '获取竞赛失败，请稍后重试'
      competitions.value = []
    } finally {
      loading.value = false
    }
  }

  // 获取竞赛分类统计
  const getCompetitionCategories = async () => {
    try {
      const response = await competitionApi.getCategories()
      return response.data || response || {}
    } catch (err) {
      console.error('获取竞赛分类失败:', err)
      return {}
    }
  }

  // 清空搜索
  const clearSearch = () => {
    searchKeyword.value = ''
    selectedCategory.value = ''
  }

  // 重置状态
  const resetState = () => {
    competitions.value = []
    loading.value = false
    error.value = null
    selectedCategory.value = ''
    searchKeyword.value = ''
  }

  return {
    // 状态
    competitions,
    loading,
    error,
    selectedCategory,
    searchKeyword,
    
    // 计算属性
    filteredCompetitions,
    
    // 方法
    fetchCompetitions,
    fetchCompetitionsByCategory,
    searchCompetitions,
    getCompetitionById,
    fetchPopularCompetitions,
    fetchUpcomingCompetitions,
    fetchCompetitionsByMode,
    getCompetitionCategories,
    clearSearch,
    resetState
  }
}

// 默认导出
export default useCompetitions