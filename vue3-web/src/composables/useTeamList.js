// 团队列表相关的组合式函数
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { competitionApi } from '../utils/api.js'

export function useTeamList(props) {
  const router = useRouter()
  const route = useRoute()
  const competitionId = props?.competitionId || route.query.competitionId

  const teams = ref([])
  const competition = ref(null)
  const loading = ref(false)
  const searchKeyword = ref('')
  const currentUser = ref(null)
  
  // 团队成员数量缓存
  const teamMemberCounts = ref({})
  
  // 用户团队成员身份缓存
  const userTeamMembership = ref({})
  
  // 团队所需技能缓存
  const teamNeededSkills = ref({})
  
  // 用户与团队的适配度缓存
  const teamUserMatch = ref({})
  
  // 用户团队申请状态缓存
  const userApplicationStatus = ref({})

  onMounted(() => {
    if (!competitionId) {
      alert('缺少竞赛ID参数')
      router.push('/competitions')
      return
    }
    
    loadUserInfo()
    fetchCompetition()
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

  // 获取竞赛信息
  const fetchCompetition = async () => {
    try {
      const data = await competitionApi.getById(competitionId)
      competition.value = data
    } catch (err) {
      console.error('获取竞赛信息失败:', err)
      alert('获取竞赛信息失败')
    }
  }

  // 获取团队列表
  const fetchTeams = async () => {
    loading.value = true
    
    try {
      const { teamApi } = await import('../utils/api.js')
      
      // 根据是否有当前用户选择不同的API
      let data;
      if (currentUser.value) {
        // 使用带适配度的API
        data = await teamApi.getCompetitionTeamCardsWithMatchScore(competitionId, currentUser.value.id)
      } else {
        // 使用普通API
        data = await teamApi.getCompetitionTeamCards(competitionId)
      }
      
      teams.value = Array.isArray(data) ? data : []
      
      // 将团队卡片数据映射到缓存中
      teams.value.forEach(team => {
        teamMemberCounts.value[team.id] = team.teamMemberCount || 0
        if (team.teamSkills) {
          teamNeededSkills.value[team.id] = team.teamSkills.split(', ').filter(skill => skill.trim())
        }
        
        // 使用memberIds字段检查用户团队成员身份
        if (currentUser.value && team.memberIds) {
          const memberIdList = team.memberIds.split(',').map(id => id.trim()).filter(id => id)
          const cacheKey = `${team.id}_${currentUser.value.id}`
          userTeamMembership.value[cacheKey] = memberIdList.includes(currentUser.value.id.toString())
        }
        
        // 如果API返回了适配度，直接使用
        if (team.matchScore !== undefined) {
          teamUserMatch.value[team.id] = team.matchScore
        }
      })
      
      // 如果API没有返回适配度，则使用旧方法计算（兼容性处理）
      if (currentUser.value && teams.value.some(team => team.matchScore === undefined)) {
        await loadTeamUserMatches()
      }
      
      // 加载用户申请状态
      if (currentUser.value) {
        await loadUserApplicationStatus()
      }
    } catch (err) {
      console.error('获取团队列表失败:', err)
      teams.value = []
    } finally {
      loading.value = false
    }
  }

  // 搜索团队
  const searchTeams = async () => {
    if (!searchKeyword.value) {
      fetchTeams()
      return
    }
    
    // 实际项目中应该调用API进行搜索
    try {
      const { teamApi } = await import('../utils/api.js')
      const data = await teamApi.searchTeams(searchKeyword.value, competitionId)
      teams.value = Array.isArray(data) ? data : []
    } catch (err) {
      console.error('搜索团队失败:', err)
      // 如果API调用失败，回退到前端过滤
      const keyword = searchKeyword.value.toLowerCase()
      teams.value = teams.value.filter(team => 
        team.name.toLowerCase().includes(keyword) || 
        (team.description && team.description.toLowerCase().includes(keyword))
      )
    }
  }
  
  // 获取团队成员数量
  const loadTeamMemberCounts = async () => {
    if (!teams.value || teams.value.length === 0) return
    
    const { teamApi } = await import('../utils/api.js')
    for (const team of teams.value) {
      try {
        const members = await teamApi.getMembers(team.id)
        teamMemberCounts.value[team.id] = Array.isArray(members) ? members.length : 0
      } catch (err) {
        console.error(`获取团队 ${team.id} 成员数量失败:`, err)
        teamMemberCounts.value[team.id] = 0
      }
    }
  }
  
  // 获取团队成员数量
  const getTeamMemberCount = (teamId) => {
    return teamMemberCounts.value[teamId] || 0
  }
  
  // 检查当前用户是否是团队成员
  // 检查当前用户是否是团队成员（现在直接从团队卡片数据中获取）
  const loadUserTeamMemberships = async () => {
    // 这个方法现在在fetchTeams中直接处理，保留为兼容性
    return Promise.resolve()
  }
  
  // 检查当前用户是否是团队成员
  // 检查当前用户是否是团队成员
  const isTeamMember = (teamId) => {
    if (!currentUser.value) return false
    const cacheKey = `${teamId}_${currentUser.value.id}`
    return userTeamMembership.value[cacheKey] === true
  }

  // 检查当前用户是否是团队队长
  // 检查当前用户是否是团队队长
  // 检查当前用户是否是团队队长
  // 检查当前用户是否是团队队长
  const isTeamLeader = (team) => {
    if (!currentUser.value || !team) return false
    // 检查队长ID匹配
    return String(team.leaderId) === String(currentUser.value.id)
  }

  // 管理团队
  const manageTeam = (teamId) => {
    router.push(`/team/${teamId}/manage`)
  }

  // 退出团队
  const leaveTeam = async (teamId) => {
    if (!currentUser.value) {
      alert('请先登录')
      return
    }

    if (confirm('确定要退出这个团队吗？')) {
      try {
        const { teamApi } = await import('../utils/api.js')
        await teamApi.removeMember(teamId, currentUser.value.id)
        alert('已成功退出团队')
        // 刷新团队列表
        fetchTeams()
      } catch (err) {
        console.error('退出团队失败:', err)
        alert('退出团队失败: ' + (err.message || '未知错误'))
      }
    }
  }

  // 查看团队详情
  const viewTeamDetail = (teamId) => {
    router.push(`/team/${teamId}`)
  }

  // 申请加入团队
  const requestJoin = async (teamId) => {
    if (!currentUser.value) {
      alert('请先登录')
      router.push('/')
      return
    }
    
    // 添加确认框
    if (!confirm('确定要申请加入这个团队吗？')) {
      return
    }
    
    try {
      const { teamApplicationApi } = await import('../utils/api.js')
      const response = await teamApplicationApi.applyToJoinTeam(currentUser.value.id, teamId)
      
      // 检查响应结构
      if (response && response.success) {
        alert(response.message || '申请提交成功，请等待队长审核')
        
        // 立即更新本地申请状态，避免等待API调用
        if (response.data) {
          userApplicationStatus.value[teamId] = response.data
        }
        
        // 然后刷新完整的申请状态
        await loadUserApplicationStatus()
      } else {
        throw new Error(response?.message || '申请提交失败')
      }
    } catch (err) {
      console.error('申请加入团队失败:', err)
      const errorMessage = err.response?.data?.message || err.message || '未知错误'
      alert('申请失败: ' + errorMessage)
    }
  }
  
  // 取消申请
  const cancelApplication = async (teamId) => {
    if (!currentUser.value) {
      alert('请先登录')
      return
    }
    
    if (!confirm('确定要取消申请吗？')) {
      return
    }
    
    try {
      const application = userApplicationStatus.value[teamId]
      if (!application) {
        alert('未找到申请记录')
        return
      }
      
      const { teamApplicationApi } = await import('../utils/api.js')
      const response = await teamApplicationApi.cancelApplication(application.id, currentUser.value.id)
      
      // 检查响应结构
      if (response && response.success) {
        alert(response.message || '申请已取消')
        
        // 立即清除本地申请状态
        delete userApplicationStatus.value[teamId]
        
        // 然后刷新完整的申请状态
        await loadUserApplicationStatus()
      } else {
        throw new Error(response?.message || '取消申请失败')
      }
    } catch (err) {
      console.error('取消申请失败:', err)
      const errorMessage = err.response?.data?.message || err.message || '未知错误'
      alert('取消申请失败: ' + errorMessage)
    }
  }
  
  // 加载用户申请状态
  const loadUserApplicationStatus = async () => {
    if (!currentUser.value) return
    
    try {
      const { teamApplicationApi } = await import('../utils/api.js')
      
      // 清空之前的状态
      userApplicationStatus.value = {}
      
      // 获取用户所有申请
      const response = await teamApplicationApi.getUserApplications(currentUser.value.id)
      
      // 处理响应数据
      let applications = []
      if (Array.isArray(response)) {
        applications = response
      } else if (response && response.data && Array.isArray(response.data)) {
        applications = response.data
      }
      
      // 将申请按团队ID分组
      applications.forEach(application => {
        userApplicationStatus.value[application.teamId] = application
      })
      
      console.log('加载的申请状态:', userApplicationStatus.value)
    } catch (err) {
      console.error('加载用户申请状态失败:', err)
    }
  }
  
  // 获取用户对特定团队的申请状态
  const getUserApplicationStatus = (teamId) => {
    return userApplicationStatus.value[teamId] || null
  }
  
  // 检查用户是否已申请该团队
  const hasUserApplied = (teamId) => {
    const application = userApplicationStatus.value[teamId]
    return application && application.status === 'PENDING'
  }
  
  // 获取申请状态显示文本
  const getApplicationStatusText = (teamId) => {
    const application = userApplicationStatus.value[teamId]
    if (!application) return null
    
    const statusMap = {
      'PENDING': '申请中',
      'APPROVED': '已通过',
      'REJECTED': '已拒绝'
    }
    
    return statusMap[application.status] || '未知状态'
  }
  
  // 获取申请状态样式类
  const getApplicationStatusClass = (teamId) => {
    const application = userApplicationStatus.value[teamId]
    if (!application) return ''
    
    const classMap = {
      'PENDING': 'status-pending',
      'APPROVED': 'status-approved', 
      'REJECTED': 'status-rejected'
    }
    
    return classMap[application.status] || ''
  }

  // 创建团队
  // 创建团队方法已移至 TeamList.vue 组件中直接实现

  // 查看竞赛详情
  const viewCompetitionDetail = () => {
    router.push(`/competition/${competitionId}`)
  }

  // 刷新团队列表
  const refreshTeams = () => {
    fetchTeams()
  }

  // 格式化参赛方式
  const formatParticipationType = (mode) => {
    const modeMap = {
      1: '个人参赛',
      2: '团队参赛',
      3: '个人或团队'
    }
    return modeMap[mode] || '未知'
  }

  // 返回上一页
  const goBack = () => {
    router.back()
  }

  // 获取团队所需技能
  const loadTeamNeededSkills = async () => {
    if (!teams.value || teams.value.length === 0) return
    
    try {
      const { teamApi } = await import('../utils/api.js')
      
      for (const team of teams.value) {
        try {
          const skills = await teamApi.getTeamNeededSkills(team.id)
          teamNeededSkills.value[team.id] = Array.isArray(skills) ? skills : []
        } catch (err) {
          console.error(`获取团队 ${team.id} 所需技能失败:`, err)
          teamNeededSkills.value[team.id] = []
        }
      }
    } catch (err) {
      console.error('加载团队所需技能失败:', err)
    }
  }
  
  // 获取用户与团队的适配度（仅用于API未返回适配度的情况）
  const loadTeamUserMatches = async () => {
    if (!currentUser.value || !teams.value || teams.value.length === 0) return
    
    try {
      const { recommendationApi } = await import('../utils/api.js')
      
      // 仅处理没有适配度的团队
      const teamsWithoutMatchScore = teams.value.filter(team => team.matchScore === undefined)
      
      for (const team of teamsWithoutMatchScore) {
        try {
          // 调用后端API获取用户与团队的适配度
          const matchScore = await recommendationApi.getTeamMatchScore(team.id, currentUser.value.id)
          teamUserMatch.value[team.id] = matchScore || 0
        } catch (err) {
          console.error(`获取用户与团队 ${team.id} 的适配度失败:`, err)
          teamUserMatch.value[team.id] = 0
        }
      }
    } catch (err) {
      console.error('加载用户与团队适配度失败:', err)
    }
  }
  
  // 获取适配度样式类
  const getMatchClass = (score) => {
    if (score >= 80) return 'match-high'
    if (score >= 50) return 'match-medium'
    return 'match-low'
  }

  return {
    // 响应式状态
    teams,
    competition,
    loading,
    searchKeyword,
    currentUser,
    competitionId,
    teamMemberCounts,
    userTeamMembership,
    teamNeededSkills,
    teamUserMatch,
    userApplicationStatus,
    
    // 方法
    loadUserInfo,
    fetchCompetition,
    fetchTeams,
    searchTeams,
    getTeamMemberCount,
    isTeamMember,
    isTeamLeader,
    viewTeamDetail,
    requestJoin,
    cancelApplication,
    manageTeam,
    leaveTeam,
    viewCompetitionDetail,
    refreshTeams,
    formatParticipationType,
    goBack,
    getMatchClass,
    loadUserApplicationStatus,
    getUserApplicationStatus,
    hasUserApplied,
    getApplicationStatusText,
    getApplicationStatusClass
  }
}