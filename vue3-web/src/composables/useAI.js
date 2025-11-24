import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { aiApi, competitionApi, userApi } from '../utils/api.js'
import { useRouter } from 'vue-router'
import { formatAIMessage, parseLearningPath, formatSkillAnalysis, createTypewriterEffect } from '../utils/textFormatter.js'

export function useAICenter() {
  const router = useRouter()
  
  // 响应式状态
  const activeTab = ref(localStorage.getItem('ai_active_tab') || 'search')
  const loading = ref(false)
  
  // 搜索相关
  const searchResults = ref([])
  const aiRecommendResults = ref([])
  
  // 推荐相关
  const recommendations = ref([])
  const recommendationSummary = ref('')
  const recommendFilter = reactive({
    category: localStorage.getItem('ai_recommend_filter_category') || '',
    difficulty: localStorage.getItem('ai_recommend_filter_difficulty') || ''
  })
  
  // 技能分析相关
  const skillAnalysis = ref(null)
  
  // 学习路径相关
  const learningPath = ref(null)
  const availableCompetitions = ref([])
  const participatedCompetitions = ref([])
  const selectedParticipatedCompetition = ref('')
  const selectedTargetCompetition = ref('')
  
  // 趋势分析相关
  const trends = ref(null)
  const selectedParticipatedForTrends = ref('')
  const selectedAvailableForTrends = ref('')
  
  // 统计数据
  const totalRecommendations = ref(0)
  const chatMessages = ref(0)
  const analysisCount = ref(0)
  
  // 监听activeTab变化，保存到localStorage
  watch(activeTab, (newValue) => {
    localStorage.setItem('ai_active_tab', newValue)
  })
  
  // 监听推荐过滤器变化，保存到localStorage
  watch(() => recommendFilter.category, (newValue) => {
    localStorage.setItem('ai_recommend_filter_category', newValue)
  })
  
  watch(() => recommendFilter.difficulty, (newValue) => {
    localStorage.setItem('ai_recommend_filter_difficulty', newValue)
  })
  
  // AI功能标签
  const aiTabs = [
    { id: 'search', name: '智能搜索', icon: '🔍' },
    { id: 'recommend', name: '个性化推荐', icon: '🎯' },
    { id: 'analysis', name: '技能分析', icon: '📊' },
    { id: 'learning', name: '学习路径', icon: '🎓' },
    { id: 'trends', name: '竞赛趋势', icon: '📈' }
  ]
  
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
  
  // 智能搜索处理
  const handleSearch = async (searchData) => {
    loading.value = true
    try {
      const user = getCurrentUser()
      const userId = user?.id
      
      // 处理不同类型的搜索参数
      let query = searchData;
      if (typeof searchData === 'object' && searchData.query) {
        query = searchData.query;
      }
      
      console.log('执行智能搜索:', query, '用户ID:', userId)
      
      // 调用智能搜索API
      const results = await aiApi.intelligentSearch(query, userId)
      searchResults.value = results.map(competition => ({
        ...competition,
        matchScore: Math.floor(Math.random() * 30) + 70 // 模拟匹配度
      }))
      
      console.log('搜索结果:', searchResults.value)
    } catch (error) {
      console.error('智能搜索失败:', error)
      searchResults.value = []
    } finally {
      loading.value = false
    }
  }
  
  // AI推荐搜索处理
  const handleAIRecommend = async (searchData) => {
    loading.value = true
    try {
      const user = getCurrentUser()
      const userId = user?.id
      
      console.log('执行AI推荐搜索，用户ID:', userId)
      
      // 调用AI推荐API
      const results = await aiApi.getRecommendations(userId)
      aiRecommendResults.value = results.map(competition => ({
        ...competition,
        matchScore: Math.floor(Math.random() * 20) + 80 // AI推荐通常匹配度更高
      }))
      
      console.log('AI推荐结果:', aiRecommendResults.value)
    } catch (error) {
      console.error('AI推荐失败:', error)
      aiRecommendResults.value = []
    } finally {
      loading.value = false
    }
  }
  
  // 获取个性化推荐
  const getPersonalizedRecommendations = async (forceRefresh = true) => {
    loading.value = true
    try {
      const user = getCurrentUser()
      const userId = user?.id
      
      if (!userId) {
        console.warn('用户未登录')
        return
      }
      
      // 获取当前选择的过滤条件
      const category = recommendFilter.category
      const difficulty = recommendFilter.difficulty
      
      console.log('获取个性化推荐，用户信息:', user, '类别:', category, '难度:', difficulty, '强制刷新:', forceRefresh)
      
      // 构建缓存键，包含过滤条件
      const cacheKey = `ai_recommendations_${userId}_${category}_${difficulty}`
      
      // 如果不是强制刷新，尝试从本地存储加载推荐数据
      if (!forceRefresh) {
        const cachedRecommendations = localStorage.getItem(cacheKey)
        if (cachedRecommendations) {
          try {
            const parsedData = JSON.parse(cachedRecommendations)
            // 检查缓存是否过期（24小时）
            const cacheTime = new Date(parsedData.timestamp)
            const now = new Date()
            const cacheAge = now - cacheTime
            
            // 如果缓存不超过24小时，直接使用缓存数据
            if (cacheAge < 24 * 60 * 60 * 1000) {
              recommendations.value = parsedData.recommendations
              recommendationSummary.value = parsedData.summary
              totalRecommendations.value = parsedData.recommendations.length
              console.log('从缓存加载推荐数据:', recommendations.value.length)
              loading.value = false
              return
            }
          } catch (e) {
            console.warn('解析缓存的推荐数据失败:', e)
          }
        }
      }
      
      // 如果没有缓存或缓存已过期，从API获取新数据
      // 将过滤条件传递给API
      const results = await aiApi.getRecommendations(userId, category, difficulty)
      console.log('API返回的推荐结果:', results)
      
      if (results && Array.isArray(results) && results.length > 0) {
        recommendations.value = results.map(competition => ({
          ...competition,
          score: competition.score || Math.floor(Math.random() * 30) + 70,
          suggestions: competition.suggestions || `基于您的${user.major || '专业'}背景和技能，这个${competition.category || ''}竞赛很适合您参加。`
        }))
        console.log('处理后的推荐结果:', recommendations.value)
        
        // 更新推荐摘要
        let summaryText = `基于您的专业背景（${user.major || '未知'}）和技能水平`
        if (category) {
          summaryText += `，在${category}类别中`
        }
        if (difficulty) {
          summaryText += `，难度为${difficulty}`
        }
        summaryText += `，为您精选了${results.length}个竞赛。`
        
        recommendationSummary.value = summaryText
        totalRecommendations.value = results.length
      } else {
        console.warn('API返回的推荐结果为空或无效')
        recommendations.value = []
        
        // 更新推荐摘要，说明没有找到结果
        let summaryText = `抱歉，未能找到符合条件的推荐竞赛。`
        if (category) {
          summaryText += `您选择的类别"${category}"`
        }
        if (difficulty) {
          summaryText += `${category ? '和' : '您选择的'}难度"${difficulty}"`
        }
        summaryText += category || difficulty ? `可能没有匹配的竞赛。请尝试其他过滤条件。` : `请尝试选择不同的过滤条件。`
        
        recommendationSummary.value = summaryText
        totalRecommendations.value = 0
      }
      
      // 根据是否有过滤条件更新推荐摘要
      let summaryText = `基于您的专业背景（${user.major || '未知'}）和技能水平`
      if (category) {
        summaryText += `，在${category}类别中`
      }
      if (difficulty) {
        summaryText += `，难度为${difficulty}`
      }
      summaryText += `，为您精选了以下竞赛。`
      
      recommendationSummary.value = summaryText
      totalRecommendations.value = results.length
      
      // 将新数据保存到本地存储
      localStorage.setItem(cacheKey, JSON.stringify({
        recommendations: recommendations.value,
        summary: recommendationSummary.value,
        timestamp: new Date().toISOString()
      }))
      
      console.log('个性化推荐结果:', recommendations.value)
      
    } catch (error) {
      console.error('获取推荐失败:', error)
      recommendations.value = []
    } finally {
      loading.value = false
    }
  }
  
  // 技能分析
  const analyzeSkills = async (forceRefresh = true) => {
    loading.value = true
    try {
      const user = getCurrentUser()
      const userId = user?.id
      
      if (!userId) {
        console.warn('用户未登录')
        return
      }
      
      console.log('开始技能分析，用户信息:', user, '强制刷新:', forceRefresh)
      
      // 如果不是强制刷新，尝试从本地存储加载技能分析数据
      if (!forceRefresh) {
        const cachedAnalysis = localStorage.getItem(`ai_skill_analysis_${userId}`)
        if (cachedAnalysis) {
          try {
            const parsedData = JSON.parse(cachedAnalysis)
            // 检查缓存是否过期（7天）
            const cacheTime = new Date(parsedData.timestamp)
            const now = new Date()
            const cacheAge = now - cacheTime
            
            // 如果缓存不超过7天，直接使用缓存数据
            if (cacheAge < 7 * 24 * 60 * 60 * 1000) {
              skillAnalysis.value = parsedData.analysis
              analysisCount.value = parsedData.count || 1
              console.log('从缓存加载技能分析数据')
              loading.value = false
              return
            }
          } catch (e) {
            console.warn('解析缓存的技能分析数据失败:', e)
          }
        }
      }
      
      // 如果没有缓存或缓存已过期，从API获取新数据
      const analysis = await aiApi.analyzeSkills(userId)
      console.log('API返回的技能分析数据:', analysis)
      console.log('技能评分数据类型:', typeof analysis.skillScores)
      console.log('技能评分数据:', analysis.skillScores)
      
      // 确保技能评分数据是正确的格式
      let processedSkills = {}
      if (analysis.skillScores) {
        // 如果是字符串，尝试解析JSON
        if (typeof analysis.skillScores === 'string') {
          try {
            processedSkills = JSON.parse(analysis.skillScores)
          } catch (e) {
            console.error('解析技能评分JSON失败:', e)
            processedSkills = {}
          }
        } else if (typeof analysis.skillScores === 'object') {
          // 如果已经是对象，直接使用
          processedSkills = analysis.skillScores
        }
      }
      
      skillAnalysis.value = {
        skills: processedSkills,
        advice: analysis.aiAnalysis || '暂无分析结果',
        overallScore: analysis.overallScore || 0
      }
      
      console.log('处理后的技能分析数据:', skillAnalysis.value)
      
      analysisCount.value += 1
      
      // 将新数据保存到本地存储
      localStorage.setItem(`ai_skill_analysis_${userId}`, JSON.stringify({
        analysis: skillAnalysis.value,
        count: analysisCount.value,
        timestamp: new Date().toISOString()
      }))
      
      console.log('技能分析结果:', skillAnalysis.value)
      
    } catch (error) {
      console.error('技能分析失败:', error)
      skillAnalysis.value = {
        skills: {},
        advice: '技能分析服务暂时不可用',
        overallScore: 0
      }
    } finally {
      loading.value = false
    }
  }
  
  // 生成学习路径
  const generateLearningPath = async (forceRefresh = true) => {
    loading.value = true
    try {
      const user = getCurrentUser()
      const userId = user?.id
      
      if (!userId || !selectedTargetCompetition.value) {
        console.warn('缺少必要参数')
        return
      }
      
      // 获取目标竞赛信息
      let targetCompetitionName = '未知竞赛'
      const targetCompetition = availableCompetitions.value.find(
        comp => comp.id == selectedTargetCompetition.value
      )
      
      // 获取参与过的竞赛信息
      let participatedCompetition = null
      if (selectedParticipatedCompetition.value) {
        participatedCompetition = participatedCompetitions.value.find(
          comp => comp.id == selectedParticipatedCompetition.value
        )
      }
      
      if (targetCompetition) {
        targetCompetitionName = targetCompetition.title || targetCompetition.name || '未知竞赛'
      }
      
      console.log('生成学习路径，用户ID:', userId, '目标竞赛:', targetCompetitionName, '(ID:', selectedTargetCompetition.value, ')')
      
      // 构建详细的请求数据
      const requestData = {
        userId,
        targetCompetitionId: selectedTargetCompetition.value,
        participatedCompetitionId: selectedParticipatedCompetition.value || null,
        userInfo: {
          major: user.major || '未知专业',
          skills: user.skills || [],
          experience: user.experience || 'beginner',
          interests: user.interests || []
        },
        targetCompetitionInfo: targetCompetition ? {
          title: targetCompetition.title,
          category: targetCompetition.category,
          difficulty: targetCompetition.difficulty,
          tags: targetCompetition.tags,
          requirements: targetCompetition.requirements || ''
        } : null,
        participatedCompetitionInfo: participatedCompetition ? {
          title: participatedCompetition.title,
          category: participatedCompetition.category,
          result: participatedCompetition.result || 'participated'
        } : null
      }
      
      console.log('学习路径请求数据:', requestData)
      
      // 直接从API获取新数据，不使用缓存
      const pathData = await aiApi.generateLearningPath(requestData)
      
      // 解析AI返回的学习路径
      const stages = parseLearningPath(pathData.aiPlan || '')
      
      // 如果解析的阶段少于3个，使用默认阶段
      if (stages.length < 3) {
        console.warn('解析的学习路径阶段不足，使用增强版默认路径')
        stages.length = 0 // 清空
        stages.push(
          {
            title: `${targetCompetitionName} - 基础准备阶段`,
            description: `针对${targetCompetitionName}竞赛，学习基础理论知识，掌握核心概念和基本技能。重点关注${targetCompetition?.category || '相关'}领域的基础知识体系建设。`,
            duration: '4周'
          },
          {
            title: `${targetCompetitionName} - 技能强化阶段`,
            description: `深入学习${targetCompetitionName}所需的专业技能，通过实践项目和案例分析提升实战能力。结合您的${user.major || '专业'}背景，重点强化相关技术栈。`,
            duration: '6周'
          },
          {
            title: `${targetCompetitionName} - 实战冲刺阶段`,
            description: `模拟${targetCompetitionName}竞赛环境，进行综合训练和查漏补缺。制定参赛策略，完善作品或解决方案，做好最终准备。`,
            duration: '2周'
          }
        )
      }
      
      learningPath.value = {
        estimatedTime: pathData.totalDuration || '12周',
        difficulty: targetCompetition?.difficulty || '中级',
        targetCompetition: targetCompetitionName,
        stages: stages
      }
      
      console.log('学习路径生成结果:', learningPath.value)
      
    } catch (error) {
      console.error('生成学习路径失败:', error)
      learningPath.value = null
    } finally {
      loading.value = false
    }
  }
  
  // 获取竞赛趋势
  const getCompetitionTrends = async (forceRefresh = true) => {
    loading.value = true
    try {
      const user = getCurrentUser()
      const userId = user?.id
      
      console.log('获取竞赛趋势分析', '用户ID:', userId, '强制刷新:', forceRefresh)
      
      // 构建缓存键，包含用户信息和选择的竞赛
      const cacheKey = `ai_competition_trends_${userId}_${selectedParticipatedForTrends.value}_${selectedAvailableForTrends.value}`
      
      // 如果不是强制刷新，尝试从本地存储加载趋势分析数据
      if (!forceRefresh) {
        const cachedTrends = localStorage.getItem(cacheKey)
        if (cachedTrends) {
          try {
            const parsedData = JSON.parse(cachedTrends)
            // 检查缓存是否过期（7天）
            const cacheTime = new Date(parsedData.timestamp)
            const now = new Date()
            const cacheAge = now - cacheTime
            
            // 如果缓存不超过7天，直接使用缓存数据
            if (cacheAge < 7 * 24 * 60 * 60 * 1000) {
              trends.value = parsedData.trends
              console.log('从缓存加载趋势分析数据')
              loading.value = false
              return
            }
          } catch (e) {
            console.warn('解析缓存的趋势分析数据失败:', e)
          }
        }
      }
      
      // 构建请求数据，包含用户信息和竞赛信息
      const requestData = {
        userId: userId,
        participatedCompetitionId: selectedParticipatedForTrends.value || null,
        availableCompetitionId: selectedAvailableForTrends.value || null,
        userInfo: user ? {
          major: user.major,
          skills: user.skills || [],
          experience: user.experience || 'beginner'
        } : null
      }
      
      console.log('趋势分析请求数据:', requestData)
      
      // 如果没有缓存或缓存已过期，从API获取新数据
      const trendsData = await aiApi.getCompetitionTrends(requestData)
      
      trends.value = {
        hotCategories: [
          { name: '编程竞赛', participation: 85, growth: 15 },
          { name: '创业竞赛', participation: 78, growth: 22 },
          { name: '设计竞赛', participation: 65, growth: 8 },
          { name: '数学建模', participation: 72, growth: 12 }
        ],
        summary: trendsData.aiAnalysis || '当前竞赛发展趋势良好，各类竞赛参与度持续上升。基于您的专业背景和参与经历，为您提供个性化的趋势分析。'
      }
      
      // 将新数据保存到本地存储
      localStorage.setItem(cacheKey, JSON.stringify({
        trends: trends.value,
        timestamp: new Date().toISOString()
      }))
      
      console.log('趋势分析结果:', trends.value)
      
    } catch (error) {
      console.error('获取趋势分析失败:', error)
      trends.value = {
        hotCategories: [
          { name: '编程竞赛', participation: 85, growth: 15 },
          { name: '创业竞赛', participation: 78, growth: 22 },
          { name: '设计竞赛', participation: 65, growth: 8 },
          { name: '数学建模', participation: 72, growth: 12 }
        ],
        summary: '趋势分析服务暂时不可用，显示默认数据。'
      }
    } finally {
      loading.value = false
    }
  }
  
  // 获取准备建议的状态
  const preparationAdvice = ref(null)
  const preparationAdviceLoading = ref(false)
  const preparationAdviceError = ref('')
  const currentCompetition = ref(null)

  // 获取准备建议
  const getPreparationAdvice = async (competition) => {
    try {
      const user = getCurrentUser()
      const userId = user?.id

      if (!userId) {
        console.warn('用户未登录')
        preparationAdviceError.value = '请先登录后再获取建议'
        return
      }

      console.log('获取准备建议，用户ID:', userId, '竞赛:', competition)

      // 重置状态
      preparationAdvice.value = null
      preparationAdviceError.value = ''
      currentCompetition.value = competition
      preparationAdviceLoading.value = true

      const advice = await aiApi.getPreparationAdvice(competition.id, userId)

      console.log('获取到的建议:', advice)

      // 设置建议内容
      preparationAdvice.value = advice.aiAdvice || '暂无具体建议'
      preparationAdviceLoading.value = false

    } catch (error) {
      console.error('获取准备建议失败:', error)
      preparationAdviceError.value = '获取建议失败，请稍后再试'
      preparationAdviceLoading.value = false
    }
  }

  // 关闭建议弹窗
  const closePreparationAdvice = () => {
    preparationAdvice.value = null
    preparationAdviceError.value = ''
    currentCompetition.value = null
    preparationAdviceLoading.value = false
  }

  // 重试获取建议
  const retryPreparationAdvice = () => {
    if (currentCompetition.value) {
      getPreparationAdvice(currentCompetition.value)
    }
  }
  
  // 查看竞赛详情
  const viewCompetition = (competitionId) => {
    router.push(`/competition/${competitionId}`)
  }
  
  // 初始化数据
  const initializeData = async () => {
    try {
      console.log('初始化AI中心数据')
      
      // 获取可用竞赛列表
      const competitions = await competitionApi.getAll()
      availableCompetitions.value = competitions
      
      // 获取用户参与的竞赛
      const user = getCurrentUser()
      if (user?.id) {
        try {
          const participated = await competitionApi.getUserParticipated(user.id)
          participatedCompetitions.value = participated
          
          // 从本地存储加载用户的AI数据
          console.log('开始加载用户AI数据...')
          
          // 加载推荐数据
          try {
            // 使用与getPersonalizedRecommendations相同的缓存键生成逻辑
            const category = recommendFilter.category
            const difficulty = recommendFilter.difficulty
            const cacheKey = `ai_recommendations_${user.id}_${category}_${difficulty}`

            const cachedRecommendations = localStorage.getItem(cacheKey)
            if (cachedRecommendations) {
              const parsedData = JSON.parse(cachedRecommendations)
              if (new Date() - new Date(parsedData.timestamp) < 24 * 60 * 60 * 1000) {
                recommendations.value = parsedData.recommendations
                recommendationSummary.value = parsedData.summary
                totalRecommendations.value = parsedData.recommendations.length
                console.log('从缓存加载推荐数据成功:', recommendations.value.length, '缓存键:', cacheKey)
              }
            }
          } catch (e) {
            console.warn('加载缓存的推荐数据失败:', e)
          }
          
          // 加载技能分析数据
          try {
            const cachedAnalysis = localStorage.getItem(`ai_skill_analysis_${user.id}`)
            if (cachedAnalysis) {
              const parsedData = JSON.parse(cachedAnalysis)
              if (new Date() - new Date(parsedData.timestamp) < 7 * 24 * 60 * 60 * 1000) {
                skillAnalysis.value = parsedData.analysis
                analysisCount.value = parsedData.count || 1
                console.log('从缓存加载技能分析数据成功')
              }
            }
          } catch (e) {
            console.warn('加载缓存的技能分析数据失败:', e)
          }
          
          // 加载趋势分析数据
          try {
            // 使用与getCompetitionTrends相同的缓存键生成逻辑
            const cacheKey = `ai_competition_trends_${user.id}_${selectedParticipatedForTrends.value}_${selectedAvailableForTrends.value}`

            const cachedTrends = localStorage.getItem(cacheKey)
            if (cachedTrends) {
              const parsedData = JSON.parse(cachedTrends)
              if (new Date() - new Date(parsedData.timestamp) < 7 * 24 * 60 * 60 * 1000) {
                trends.value = parsedData.trends
                console.log('从缓存加载趋势分析数据成功，缓存键:', cacheKey)
              }
            }
          } catch (e) {
            console.warn('加载缓存的趋势分析数据失败:', e)
          }
          
          // 注意：已移除学习路径缓存加载逻辑
          
          // 加载统计数据
          chatMessages.value = parseInt(localStorage.getItem(`ai_chat_messages_count_${user.id}`) || '0') || Math.floor(Math.random() * 50) + 20
          
          console.log('用户AI数据加载完成')
        } catch (error) {
          console.warn('获取用户参与竞赛失败:', error)
          participatedCompetitions.value = []
        }
      } else {
        // 初始化统计数据
        totalRecommendations.value = Math.floor(Math.random() * 20) + 10
        chatMessages.value = Math.floor(Math.random() * 50) + 20
        analysisCount.value = Math.floor(Math.random() * 10) + 5
      }
      
      console.log('AI中心数据初始化完成')
      
    } catch (error) {
      console.error('初始化数据失败:', error)
    }
  }
  
  // 加载用户的AI数据
  const loadUserAIData = async (userId) => {
    console.log('加载用户AI数据，用户ID:', userId)
    
    try {
      // 加载推荐数据
      await getPersonalizedRecommendations(false)
      console.log('成功加载推荐数据')
    } catch (e) {
      console.warn('自动加载推荐数据失败:', e)
    }
    
    try {
      // 加载技能分析数据
      await analyzeSkills(false)
      console.log('成功加载技能分析数据')
    } catch (e) {
      console.warn('自动加载技能分析数据失败:', e)
    }
    
    try {
      // 加载趋势分析数据
      await getCompetitionTrends(false)
      console.log('成功加载趋势分析数据')
    } catch (e) {
      console.warn('自动加载趋势分析数据失败:', e)
    }
    
    // 加载统计数据
    chatMessages.value = parseInt(localStorage.getItem(`ai_chat_messages_count_${userId}`) || '0') || Math.floor(Math.random() * 50) + 20
  }
  
  // 组件挂载时初始化
  onMounted(() => {
    initializeData()
  })
  
  return {
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
    viewCompetition,
    getCurrentUser
  }
}

// AI聊天功能
export function useAI() {
  const messages = ref([])
  const inputMessage = ref('')
  const isLoading = ref(false)
  const isMinimized = ref(false)
  const isVisible = ref(false)
  const chatHistory = ref([])
  const currentConversationId = ref(null)
  const typewriterController = ref(null)
  const isTyping = ref(false)
  
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
  
  // 发送消息
  const sendMessage = async () => {
    if (!inputMessage.value.trim() || isLoading.value) return
    
    const userMessage = inputMessage.value.trim()
    const user = getCurrentUser()
    const userId = user?.id
    
    // 如果当前没有对话ID，生成一个新的会话ID
    if (!currentConversationId.value) {
      // 使用时间戳生成会话ID，确保唯一性
      const now = new Date()
      const groupId = `group_${userId}_${now.getTime()}`
      currentConversationId.value = groupId
      console.log('开始新对话会话，组ID:', currentConversationId.value)
      
      // 清空之前的消息（如果有的话）
      messages.value = []
    }
    
    // 添加用户消息到聊天记录
    messages.value.push({
      id: Date.now(),
      type: 'user',
      content: userMessage,
      timestamp: new Date(),
      groupId: currentConversationId.value,
      user: {
        name: user?.username || '用户',
        avatar: user?.avatarUrl || ''
      }
    })
    
    inputMessage.value = ''
    isLoading.value = true
    
    try {
      console.log('发送AI聊天消息:', userMessage, '用户ID:', userId, '当前组ID:', currentConversationId.value)
      
      // 构建上下文（最近的几条消息）
      const context = messages.value
        .slice(-6) // 取最近6条消息作为上下文
        .map(msg => `${msg.type === 'user' ? '用户' : 'AI'}: ${msg.content}`)
      
      // 调用AI聊天API，传入用户ID和组ID
      const requestData = {
        message: userMessage,
        context: context,
        userId: userId,
        groupId: currentConversationId.value
      }
      
      const response = await aiApi.chat(userMessage, context, userId, currentConversationId.value)

      // 停止之前的打字机效果（如果有的话）
      if (typewriterController.value) {
        typewriterController.value.stop()
        typewriterController.value = null
      }

      // 创建AI消息占位符，初始内容为空
      const aiMessageId = Date.now() + 1
      const aiMessage = {
        id: aiMessageId,
        type: 'assistant',
        content: '', // 初始为空，将通过打字机效果逐步填充
        timestamp: new Date(response.timestamp),
        groupId: currentConversationId.value,
        suggestions: response.suggestions || [],
        isTyping: true // 标记正在打字
      }

      messages.value.push(aiMessage)

      // 关闭加载状态，开始打字
      isLoading.value = false
      isTyping.value = true

      // 创建打字机效果
      typewriterController.value = createTypewriterEffect(
        response.message,
        (currentText) => {
          // 更新消息内容
          const messageIndex = messages.value.findIndex(msg => msg.id === aiMessageId)
          if (messageIndex !== -1) {
            messages.value[messageIndex].content = currentText
          }
        },
        30 // 30ms/字符的打字速度
      )

      // 启动打字机效果
      typewriterController.value.start()

      // 等待打字完成
      const checkComplete = setInterval(() => {
        if (typewriterController.value && typewriterController.value.isComplete()) {
          clearInterval(checkComplete)
          isTyping.value = false

          // 移除打字标记
          const messageIndex = messages.value.findIndex(msg => msg.id === aiMessageId)
          if (messageIndex !== -1) {
            messages.value[messageIndex].isTyping = false
          }

          typewriterController.value = null

          // 保存聊天记录到本地存储
          saveChatHistory()
        }
      }, 100)

    } catch (error) {
      console.error('AI聊天失败:', error)

      // 添加错误消息
      messages.value.push({
        id: Date.now() + 1,
        type: 'assistant',
        content: '抱歉，AI服务暂时不可用。这可能是因为：\n\n• 服务器正在维护\n• 网络连接问题\n• API配置需要更新\n\n请稍后再试，或联系管理员。',
        timestamp: new Date(),
        groupId: currentConversationId.value,
        isError: true
      })

      isLoading.value = false
      isTyping.value = false
    }
  }
  
  // 保存聊天记录
  const saveChatHistory = () => {
    try {
      const user = getCurrentUser()
      if (user?.id) {
        const historyKey = `ai_chat_history_${user.id}`
        const historyData = {
          messages: messages.value.slice(-50), // 只保存最近50条消息
          lastUpdated: new Date().toISOString()
        }
        localStorage.setItem(historyKey, JSON.stringify(historyData))
      }
    } catch (error) {
      console.warn('保存聊天记录失败:', error)
    }
  }
  
  // 加载聊天记录 - 总是从数据库加载，不使用缓存
  const loadChatHistory = async () => {
    try {
      const user = getCurrentUser()
      if (user?.id) {
        console.log('开始从数据库加载聊天历史（不使用缓存）...')
        
        // 清空当前对话ID，表示查看所有聊天记录
        currentConversationId.value = null
        
        // 直接从数据库获取聊天历史，不使用缓存
        try {
          const historyData = await aiApi.getChatHistory(user.id)
          console.log('从数据库获取的聊天历史:', historyData)
          
          if (historyData && historyData.length > 0) {
            // 清空现有消息
            messages.value = []
            
            // 处理每条历史记录
            historyData.forEach(record => {
              // 添加用户消息
              if (record.input) {
                messages.value.push({
                  id: `user_${record.id}`,
                  type: 'user',
                  content: record.input,
                  timestamp: new Date(record.timestamp),
                  user: {
                    name: user.username || '用户',
                    avatar: user.avatarUrl || ''
                  }
                })
              }
              
              // 添加AI回复
              if (record.response) {
                messages.value.push({
                  id: `ai_${record.id}`,
                  type: 'assistant',
                  content: record.response,
                  timestamp: new Date(record.timestamp)
                })
              }
            })
            
            console.log(`从数据库成功加载 ${messages.value.length} 条历史消息`)
            return
          } else {
            console.log('数据库中没有聊天历史，显示欢迎消息')
            addWelcomeMessage()
          }
        } catch (dbError) {
          console.error('从数据库加载聊天历史失败:', dbError)
          addWelcomeMessage()
        }
      } else {
        addWelcomeMessage()
      }
    } catch (error) {
      console.warn('加载聊天记录失败:', error)
      addWelcomeMessage()
    }
  }
  
  // 加载聊天历史列表 - 按时间智能分组
  // 加载聊天历史列表 - 按组ID分组
  // 加载聊天历史列表 - 处理后端返回的分组数据
  const loadChatHistoryList = async () => {
    try {
      const user = getCurrentUser()
      if (user?.id) {
        console.log('开始从数据库加载聊天历史列表（处理分组数据）...')
        
        // 直接从数据库获取聊天历史分组数据
        try {
          const historyData = await aiApi.getConversations(user.id)
          console.log('从数据库获取的聊天历史分组数据:', historyData)
          
          if (historyData && historyData.length > 0) {
            // 后端现在返回的是分组后的数据，每个分组包含完整的对话记录
            chatHistory.value = historyData.map(group => ({
              id: group.id || group.groupId,
              title: group.title || '对话记录',
              lastMessage: group.lastMessage || '点击查看详情',
              timestamp: group.timestamp ? new Date(group.timestamp).getTime() : Date.now(),
              messages: group.messages || [] // 包含完整的对话记录
            }))
            
            console.log('处理后的聊天历史分组:', chatHistory.value.length, '个分组')
            console.log('分组详情:', chatHistory.value.map(conv => ({
              id: conv.id,
              title: conv.title,
              messageCount: conv.messages ? conv.messages.length : 0
            })))
          } else {
            console.log('数据库中没有聊天历史')
            chatHistory.value = []
          }
        } catch (dbError) {
          console.error('从数据库加载聊天历史列表失败:', dbError)
          chatHistory.value = []
        }
      } else {
        chatHistory.value = []
      }
    } catch (error) {
      console.warn('加载聊天历史列表失败:', error)
      chatHistory.value = []
    }
  }
  
  // 添加欢迎消息
  const addWelcomeMessage = () => {
    const user = getCurrentUser()
    const welcomeMessage = user 
      ? `您好 ${user.username}！我是您的AI竞赛助手。\n\n我可以帮助您：\n• 推荐适合的竞赛\n• 分析您的技能水平\n• 制定学习计划\n• 解答竞赛相关问题\n\n有什么我可以帮助您的吗？`
      : `您好！我是AI竞赛助手。\n\n我可以帮助您：\n• 推荐适合的竞赛\n• 分析技能水平\n• 制定学习计划\n• 解答竞赛相关问题\n\n有什么我可以帮助您的吗？`
    
    messages.value = [{
      id: Date.now(),
      type: 'assistant',
      content: welcomeMessage,
      timestamp: new Date(),
      isWelcome: true
    }]
  }
  
  // 清空聊天记录
  const clearChat = () => {
    messages.value = []
    currentConversationId.value = null // 清空当前对话ID
    const user = getCurrentUser()
    if (user?.id) {
      const historyKey = `ai_chat_history_${user.id}`
      localStorage.removeItem(historyKey)
    }
    addWelcomeMessage()
  }
  
  // 切换最小化状态
  const toggleMinimize = () => {
    isMinimized.value = !isMinimized.value
  }
  
  // 显示/隐藏聊天窗口
  const toggleVisibility = () => {
    isVisible.value = !isVisible.value
    // 不自动加载历史记录，让组件自己决定显示什么
  }
  
  // 处理快捷操作
  const handleQuickAction = (action) => {
    const quickMessages = {
      recommend: '请为我推荐一些适合的竞赛',
      skills: '请分析一下我的技能水平',
      trends: '当前有哪些热门的竞赛类型？',
      help: '我是新手，应该如何开始参加竞赛？'
    }
    
    if (quickMessages[action]) {
      inputMessage.value = quickMessages[action]
      sendMessage()
    }
  }
  
  // 初始化
  const initializeChat = () => {
    // 清空当前对话ID，确保是全新的状态
    currentConversationId.value = null
    // 显示欢迎消息，不加载历史记录
    addWelcomeMessage()
  }
  
  // 加载对话 - 按会话ID加载所有相关记录
  // 加载对话 - 按组ID加载所有相关记录
  // 加载对话 - 使用新的数据结构
  const loadConversation = async (groupId) => {
    try {
      console.log('开始加载对话组:', groupId)
      currentConversationId.value = groupId
      
      // 获取当前用户
      const user = getCurrentUser()
      
      // 验证groupId格式
      if (!groupId || groupId === 'NaN' || groupId.trim() === '') {
        console.error('无效的组ID:', groupId)
        messages.value = [{
          id: Date.now(),
          type: 'assistant',
          content: '无效的组ID，无法加载对话记录。',
          timestamp: new Date(),
          isError: true
        }]
        return
      }
      
      // 清空当前消息
      messages.value = []
      
      // 从聊天历史中找到对应的会话（现在包含完整的messages数据）
      const conversation = chatHistory.value.find(conv => conv.id === groupId)
      
      if (conversation && conversation.messages) {
        console.log('从本地会话记录加载对话:', conversation.messages.length, '条记录')
        
        // 转换聊天记录为消息格式
        const formattedMessages = []
        
        // 按时间戳排序，确保消息顺序正确
        const sortedMessages = [...conversation.messages].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp))
        
        sortedMessages.forEach((record, index) => {
          console.log(`处理会话记录 ${index + 1}:`, record)
          
          // 根据记录类型添加消息
          if (record.type === 'user' && record.input) {
            formattedMessages.push({
              id: `${record.id}_user`,
              type: 'user',
              content: record.input,
              timestamp: new Date(record.timestamp),
              user: {
                name: user?.username || '用户',
                avatar: user?.avatarUrl || ''
              }
            })
          } else if (record.type === 'assistant' && record.response) {
            formattedMessages.push({
              id: `${record.id}_ai`,
              type: 'assistant',
              content: record.response,
              timestamp: new Date(record.timestamp)
            })
          } else {
            // 处理旧格式的记录
            if (record.input && record.input.trim()) {
              formattedMessages.push({
                id: `${record.id}_user`,
                type: 'user',
                content: record.input,
                timestamp: new Date(record.timestamp),
                user: {
                  name: user?.username || '用户',
                  avatar: user?.avatarUrl || ''
                }
              })
            }
            
            if (record.response && record.response.trim()) {
              formattedMessages.push({
                id: `${record.id}_ai`,
                type: 'assistant',
                content: record.response,
                timestamp: new Date(record.timestamp)
              })
            }
          }
        })
        
        messages.value = formattedMessages
        console.log('加载会话成功，格式化后消息数量:', messages.value.length)
      } else {
        // 如果本地没有找到，尝试从后端获取
        console.log('本地未找到会话记录，尝试从后端获取...')
        
        const chatLogs = await aiApi.getConversationHistory(user?.id || 6, groupId)
        console.log('从后端获取到聊天记录:', chatLogs.length, '条')
        
        // 转换聊天记录为消息格式
        const formattedMessages = []
        
        if (chatLogs && chatLogs.length > 0) {
          // 按时间戳排序，确保消息顺序正确
          const sortedChatLogs = [...chatLogs].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp))
          
          sortedChatLogs.forEach((chat, index) => {
            console.log(`处理后端聊天记录 ${index + 1}:`, chat)
            
            // 根据记录类型添加消息
            if (chat.type === 'user' && chat.input) {
              formattedMessages.push({
                id: `${chat.id}_user`,
                type: 'user',
                content: chat.input,
                timestamp: new Date(chat.timestamp),
                user: {
                  name: user?.username || '用户',
                  avatar: user?.avatarUrl || ''
                }
              })
            } else if (chat.type === 'assistant' && chat.response) {
              formattedMessages.push({
                id: `${chat.id}_ai`,
                type: 'assistant',
                content: chat.response,
                timestamp: new Date(chat.timestamp)
              })
            } else {
              // 处理旧格式的记录
              if (chat.input && chat.input.trim()) {
                formattedMessages.push({
                  id: `${chat.id}_user`,
                  type: 'user',
                  content: chat.input,
                  timestamp: new Date(chat.timestamp),
                  user: {
                    name: user?.username || '用户',
                    avatar: user?.avatarUrl || ''
                  }
                })
              }
              
              if (chat.response && chat.response.trim()) {
                formattedMessages.push({
                  id: `${chat.id}_ai`,
                  type: 'assistant',
                  content: chat.response,
                  timestamp: new Date(chat.timestamp)
                })
              }
            }
          })
        }
        
        messages.value = formattedMessages
        console.log('从后端加载对话成功，格式化后消息数量:', messages.value.length)
      }
      
      // 如果没有找到任何记录，显示提示
      if (messages.value.length === 0) {
        console.warn('没有找到对话记录')
        messages.value = [{
          id: Date.now(),
          type: 'assistant',
          content: '该对话记录暂时无法加载，可能已被清理。请开始新的对话。',
          timestamp: new Date(),
          isError: true
        }]
      }
    } catch (error) {
      console.error('加载对话失败:', error)
      // 显示错误消息
      messages.value = [{
        id: Date.now(),
        type: 'assistant',
        content: '网络错误，无法加载对话记录。',
        timestamp: new Date(),
        isError: true
      }]
    }
  }

  // 删除对话
  // 删除对话
  const deleteConversation = async (groupId) => {
    try {
      console.log('开始删除对话组:', groupId)
      
      // 获取当前用户
      const user = getCurrentUser()
      const userId = user?.id || 6 // 默认用户ID
      
      console.log('用户ID:', userId, '要删除的组ID:', groupId)
      
      // 验证groupId格式
      if (!groupId || groupId === 'NaN' || groupId.trim() === '') {
        console.error('无效的组ID:', groupId)
        return false
      }
      
      // 直接调用删除API
      const response = await fetch(`http://localhost:8080/api/ai/chat/conversation/${userId}/${encodeURIComponent(groupId)}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        }
      })
      
      console.log('删除API响应状态:', response.status)
      
      if (response.ok) {
        const result = await response.json()
        console.log('删除对话组API响应:', result)
        
        // 如果当前正在查看的是被删除的对话，则重新加载聊天记录
        if (currentConversationId.value === groupId) {
          console.log('当前查看的对话组被删除，重新加载聊天记录')
          currentConversationId.value = null
          // 重新加载聊天记录（不使用缓存）
          await loadChatHistory()
        }
        
        // 主动刷新聊天历史列表（不使用缓存）
        console.log('删除成功，主动刷新聊天历史列表')
        await loadChatHistoryList()
        
        console.log('删除对话组成功')
        return true
      } else {
        const errorText = await response.text()
        console.error('删除对话组失败:', response.status, errorText)
        return false
      }
      
    } catch (error) {
      console.error('删除对话组出错:', error)
      return false
    }
  }

  return {
    // 响应式状态
    messages,
    inputMessage,
    isLoading,
    isMinimized,
    isVisible,
    chatHistory,
    currentConversationId,
    isTyping,
    typewriterController,

    // 方法
    sendMessage,
    clearChat,
    toggleMinimize,
    toggleVisibility,
    handleQuickAction,
    initializeChat,
    loadChatHistoryList,
    loadConversation,
    getCurrentUser,
    deleteConversation
  }
}