// 竞赛相关的组合式函数
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { competitionApi } from '../utils/api.js'

// 竞赛详情相关的组合式函数
export function useCompetitionDetail() {
  const router = useRouter()
  const route = useRoute()
  
  const competition = ref(null)
  const teams = ref([])
  const loading = ref(false)
  const error = ref('')
  const currentUser = ref(null)
  const hasJoined = ref(false)

  const competitionId = route.params.id

  onMounted(() => {
    loadUserInfo()
    fetchCompetitionDetail()
    fetchTeams()
  })

  // 加载用户信息
  const loadUserInfo = () => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        currentUser.value = JSON.parse(userStr)
      } catch (e) {
        console.error('用户信息解析失败:', e)
        localStorage.removeItem('user')
      }
    }
  }

  // 获取竞赛详情
  const fetchCompetitionDetail = async () => {
    if (!competitionId) {
      error.value = '竞赛ID不存在'
      return
    }

    loading.value = true
    error.value = ''

    try {
      const data = await competitionApi.getById(competitionId)
      competition.value = data

      // 获取竞赛详情后，检查用户是否已参赛
      if (currentUser.value) {
        await checkUserParticipation()
      }
    } catch (err) {
      console.error('获取竞赛详情失败:', err)
      error.value = '获取竞赛详情失败，请重试'
    } finally {
      loading.value = false
    }
  }

  // 检查用户是否已参赛
  const checkUserParticipation = async () => {
    if (!currentUser.value || !competition.value) return

    try {
      const data = await competitionApi.getUserParticipationDetails(currentUser.value.id)
      const participations = Array.isArray(data) ? data : []
      console.log('检查参赛状态 - 参赛记录:', participations)
      console.log('检查参赛状态 - 当前竞赛ID:', competition.value.id)

      // 尝试不同的字段名来匹配竞赛ID
      hasJoined.value = participations.some(p => {
        const compId = p.competitionId || p.competition?.id || p.competition_id
        return compId == competition.value.id // 使用 == 进行宽松比较
      })

      console.log('用户是否已参赛:', hasJoined.value)
    } catch (err) {
      console.error('获取用户参赛记录失败:', err)
      hasJoined.value = false
    }
  }

  // 获取参赛团队
  const fetchTeams = async () => {
    if (!competitionId) return

    try {
      const { teamApi } = await import('../utils/api.js')
      const data = await teamApi.getCompetitionTeams(competitionId)
      teams.value = Array.isArray(data) ? data : []
    } catch (err) {
      console.error('获取团队列表失败:', err)
      teams.value = []
    }
  }

  // 重新获取竞赛信息（用于重试）
  const fetchCompetition = () => {
    fetchCompetitionDetail()
  }

  // 计算竞赛状态
  const competitionStatus = computed(() => {
    if (!competition.value) return 'unknown'
    
    const now = new Date()
    const startTime = new Date(competition.value.startTime)
    const endTime = new Date(competition.value.endTime)
    
    if (now < startTime) return 'upcoming'
    if (now > endTime) return 'ended'
    return 'ongoing'
  })

  // 格式化日期
  const formatDate = (dateString) => {
    if (!dateString) return '待定'
    
    // 调试输出
    console.log('原始日期字符串:', dateString);
    console.log('日期类型:', typeof dateString);
    
    if (typeof dateString === 'object') {
      console.log('日期对象属性:', Object.keys(dateString));
    }
    
    try {
      // 尝试创建日期对象
      let date;
      
      // 处理不同格式的日期字符串
      if (typeof dateString === 'string') {
        // 如果是ISO格式的字符串
        if (dateString.includes('T')) {
          date = new Date(dateString);
        } 
        // 如果是时间戳格式的字符串
        else if (!isNaN(Number(dateString))) {
          date = new Date(Number(dateString));
        }
        // 其他格式的字符串
        else {
          date = new Date(dateString);
        }
      } 
      // 如果是数字类型的时间戳
      else if (typeof dateString === 'number') {
        date = new Date(dateString);
      }
      // 如果是Date对象
      else if (dateString instanceof Date) {
        date = dateString;
      }
      // 如果是Java的Timestamp对象转换后的格式
      else if (dateString && typeof dateString === 'object') {
        // 检查是否有time属性
        if (dateString.time) {
          date = new Date(dateString.time);
        } 
        // 检查是否有date属性
        else if (dateString.date) {
          date = new Date(dateString.date);
        }
        // 尝试将整个对象转换为时间戳
        else {
          const timestamp = new Date(dateString).getTime();
          if (!isNaN(timestamp)) {
            date = new Date(timestamp);
          }
        }
      }
      
      // 检查日期是否有效
      if (!date || isNaN(date.getTime())) {
        console.warn('无效的日期格式:', dateString);
        return '待定';
      }
      
      // 格式化日期
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      console.error('日期格式化错误:', error, dateString);
      return '待定';
    }
  }

  // 处理报名
  const joinCompetition = async () => {
    if (!currentUser.value) {
      alert('请先登录')
      router.push('/')
      return
    }

    if (competitionStatus.value === 'ended') {
      alert('竞赛已结束，无法报名')
      return
    }

    // 调用参赛弹窗
    return {
      competition: competition.value,
      currentUser: currentUser.value
    }
  }

  // 取消参赛
  const cancelParticipation = async () => {
    if (!currentUser.value) {
      alert('请先登录')
      return
    }

    try {
      const data = await competitionApi.getUserParticipationDetails(currentUser.value.id)
      console.log('用户参赛记录:', data)
      console.log('当前竞赛ID:', competition.value.id)

      const participations = Array.isArray(data) ? data : []
      console.log('参赛记录列表:', participations)

      // 尝试不同的字段名来查找匹配的参赛记录
      const userParticipation = participations.find(p => {
        console.log('检查参赛记录:', p)
        // 尝试不同的字段名
        const compId = p.competitionId || p.competition?.id || p.competition_id
        console.log('参赛记录竞赛ID:', compId, '当前竞赛ID:', competition.value.id)
        return compId == competition.value.id // 使用 == 进行宽松比较
      })

      console.log('找到的参赛记录:', userParticipation)

      if (!userParticipation) {
        alert('未找到参赛记录')
        return
      }

      if (!confirm('确定要取消参赛吗？取消后需要重新报名才能参加。')) {
        return
      }

      // 调用更新后的API：传递竞赛ID、用户ID和团队ID
      const competitionId = competition.value.id
      const userId = currentUser.value.id
      const teamId = userParticipation.teamId || null

      await competitionApi.cancelParticipation(competitionId, userId, teamId)
      hasJoined.value = false
      alert('取消参赛成功')

      // 刷新参赛状态
      await checkUserParticipation()
    } catch (err) {
      console.error('取消参赛失败:', err)
      alert('取消参赛失败：' + (err.message || '请重试'))
    }
  }

  // 创建团队
  // 创建团队
  // 创建团队
  const createTeam = () => {
    if (!currentUser.value) {
      alert('请先登录')
      router.push('/')
      return
    }

    // 显示创建团队弹窗
    // 注意：此方法已被移至 CompetitionDetail.vue 中的 openCreateTeamModal 方法
    router.push(`/team/create?competitionId=${competitionId}`)
  }
  
  // 提交团队创建
  const submitTeamCreation = async (teamData) => {
    if (!currentUser.value) {
      throw new Error('请先登录')
    }
    
    try {
      const { teamApi } = await import('../utils/api.js')
      
      // 添加队长ID
      const fullTeamData = {
        ...teamData,
        leaderId: currentUser.value.id
      }
      
      // 调用API创建团队
      const result = await teamApi.create(fullTeamData)
      
      // 刷新团队列表
      fetchTeams()
      
      return result
    } catch (error) {
      console.error('创建团队失败:', error)
      throw error
    }
  }

  // 查看团队
  const viewTeams = () => {
    router.push(`/teams?competitionId=${competitionId}`)
  }

  // 查看项目
  const viewProjects = () => {
    router.push(`/projects?competitionId=${competitionId}`)
  }

  // 获取竞赛状态文本
  const getCompetitionStatus = () => {
    switch (competitionStatus.value) {
      case 'upcoming': return '即将开始'
      case 'ongoing': return '进行中'
      case 'ended': return '已结束'
      default: return '未知状态'
    }
  }

  // 获取状态样式类
  const getStatusClass = () => {
    return `status-${competitionStatus.value}`
  }

  // 格式化规则键名
  const formatRuleKey = (key) => {
    const keyMap = {
      'time_limit': '时间限制',
      'team_size': '团队规模',
      'language': '编程语言',
      'duration': '竞赛时长',
      'rounds': '竞赛轮次',
      'max_team_size': '最大团队规模',
      'individual': '个人参赛',
      'categories': '参赛类别'
    }
    return keyMap[key] || key
  }

  // 获取团队成员数量
  const getTeamMemberCount = (teamId) => {
    // 这里应该从API获取团队成员数量，暂时返回随机数
    return Math.floor(Math.random() * 5) + 1
  }

  // 计算竞赛规则
  const competitionRules = computed(() => {
    if (!competition.value?.rules_json) return null
    
    try {
      return JSON.parse(competition.value.rules_json)
    } catch (e) {
      console.error('解析竞赛规则失败:', e)
      return null
    }
  })

  // 返回列表
  const goBack = () => {
    router.back()
  }

  return {
    // 响应式状态
    competition,
    teams,
    loading,
    error,
    currentUser,
    hasJoined,
    competitionStatus,
    competitionRules,

    // 方法
    fetchCompetitionDetail,
    fetchCompetition,
    formatDate,
    joinCompetition,
    cancelParticipation,
    createTeam,
    viewTeams,
    viewProjects,
    getCompetitionStatus,
    getStatusClass,
    formatRuleKey,
    getTeamMemberCount,
    goBack
  }
}

// 竞赛列表相关的组合式函数
export function useCompetitionList() {
  const router = useRouter()

  const competitions = ref([])
  const userParticipations = ref([]) // 用户参赛记录
  const loading = ref(false)
  const error = ref('')
  const searchKeyword = ref('')
  const selectedCategory = ref('')
  const selectedMode = ref('')
  const categories = ref([])
  const currentUser = ref(null)

  onMounted(() => {
    loadUserInfo()
    fetchCompetitions()
    fetchCategories()
    if (currentUser.value) {
      fetchUserParticipations()
    }
  })

  // 加载用户信息
  const loadUserInfo = () => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        currentUser.value = JSON.parse(userStr)
      } catch (e) {
        console.error('用户信息解析失败:', e)
      }
    }
  }

  // 获取用户参赛记录
  const fetchUserParticipations = async () => {
    if (!currentUser.value) return

    try {
      const data = await competitionApi.getUserParticipationDetails(currentUser.value.id)
      userParticipations.value = Array.isArray(data) ? data : []
    } catch (err) {
      console.error('获取用户参赛记录失败:', err)
      userParticipations.value = []
    }
  }

  // 检查用户是否已参加某个竞赛
  const hasUserJoinedCompetition = (competitionId) => {
    return userParticipations.value.some(p => p.competitionId === competitionId)
  }

  // 获取用户在某个竞赛中的参赛记录ID（用于取消参赛）
  const getUserParticipationId = (competitionId) => {
    const participation = userParticipations.value.find(p => p.competitionId === competitionId)
    return participation?.id || null
  }

  // 获取竞赛列表
  const fetchCompetitions = async () => {
    loading.value = true
    error.value = ''

    try {
      const data = await competitionApi.getAll()
      competitions.value = Array.isArray(data) ? data : []
    } catch (err) {
      console.error('获取竞赛列表失败:', err)
      error.value = '获取竞赛列表失败，请重试'
      competitions.value = []
    } finally {
      loading.value = false
    }
  }

  // 获取竞赛分类
  const fetchCategories = async () => {
    try {
      const data = await competitionApi.getCategories()
      categories.value = Array.isArray(data) ? data : []
    } catch (err) {
      console.error('获取竞赛分类失败:', err)
      categories.value = []
    }
  }

  // 搜索竞赛
  const searchCompetitions = async () => {
    loading.value = true
    error.value = ''

    try {
      const params = {
        keyword: searchKeyword.value,
        category: selectedCategory.value,
        participationMode: selectedMode.value
      }

      const data = await competitionApi.search(params)
      competitions.value = Array.isArray(data) ? data : []
    } catch (err) {
      console.error('搜索竞赛失败:', err)
      error.value = '搜索失败，请重试'
      competitions.value = []
    } finally {
      loading.value = false
    }
  }

  // 重置搜索
  const resetSearch = () => {
    searchKeyword.value = ''
    selectedCategory.value = ''
    selectedMode.value = ''
    fetchCompetitions()
  }

  // 查看竞赛详情
  const viewCompetition = (competitionId) => {
    router.push(`/competition/${competitionId}`)
  }

  // 处理竞赛报名
  const handleJoinCompetition = async (competition) => {
    const user = localStorage.getItem('user')
    if (!user) {
      alert('请先登录')
      router.push('/')
      return
    }

    // 检查竞赛状态
    const now = new Date()
    const startTime = new Date(competition.startTime)

    if (now > startTime) {
      alert('竞赛已开始，无法报名')
      return
    }

    // 这里应该调用报名API
    alert('报名功能开发中...')
  }

  // 取消参赛
  const handleCancelParticipation = async (competitionId) => {
    if (!currentUser.value) {
      alert('请先登录')
      return false
    }

    try {
      const data = await competitionApi.getUserParticipationDetails(currentUser.value.id)
      const participations = Array.isArray(data) ? data : []

      // 查找匹配的参赛记录
      const userParticipation = participations.find(p => {
        const compId = p.competitionId || p.competition?.id || p.competition_id
        return compId == competitionId
      })

      if (!userParticipation) {
        alert('未找到参赛记录')
        return false
      }

      if (!confirm('确定要取消参赛吗？取消后需要重新报名才能参加。')) {
        return false
      }

      // 调用更新后的API：传递竞赛ID、用户ID和团队ID
      const userId = currentUser.value.id
      const teamId = userParticipation.teamId || null

      await competitionApi.cancelParticipation(competitionId, userId, teamId)

      // 刷新用户参赛记录
      await fetchUserParticipations()
      alert('取消参赛成功')
      return true
    } catch (err) {
      console.error('取消参赛失败:', err)
      alert('取消参赛失败：' + (err.message || '请重试'))
      return false
    }
  }

  // 筛选后的竞赛列表
  const filteredCompetitions = computed(() => {
    let result = competitions.value

    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      result = result.filter(comp => 
        comp.title?.toLowerCase().includes(keyword) ||
        comp.description?.toLowerCase().includes(keyword) ||
        comp.organizer?.toLowerCase().includes(keyword)
      )
    }

    if (selectedCategory.value) {
      result = result.filter(comp => comp.category === selectedCategory.value)
    }

    if (selectedMode.value) {
      result = result.filter(comp => comp.participationMode === selectedMode.value)
    }

    return result
  })

  return {
    // 响应式状态
    competitions,
    userParticipations,
    currentUser,
    loading,
    error,
    searchKeyword,
    selectedCategory,
    selectedMode,
    categories,
    filteredCompetitions,

    // 方法
    fetchCompetitions,
    fetchCategories,
    fetchUserParticipations,
    searchCompetitions,
    resetSearch,
    viewCompetition,
    handleJoinCompetition,
    handleCancelParticipation,
    hasUserJoinedCompetition,
    getUserParticipationId
  }
}