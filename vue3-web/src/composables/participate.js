import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { competitionApi, teamApi, userApi } from '../utils/api.js'
import { showSuccess, showError } from '../utils/message.js'

export function useParticipate(props, emit) {
  const router = useRouter()
  
  // 响应式数据
  const selectedMode = ref('')
  const selectedTeam = ref(null)
  const userTeams = ref([])
  const currentUser = ref(null)
  const loading = ref(false)
  const errorMessage = ref('')

  // 计算属性
  const showModeSelection = computed(() => {
    const mode = props.competition?.participationMode?.toLowerCase()
    return mode === 'both'
  })

  const canParticipateIndividual = computed(() => {
    const mode = props.competition?.participationMode?.toLowerCase()
    return mode === 'individual' || mode === 'both'
  })

  const canParticipateTeam = computed(() => {
    const mode = props.competition?.participationMode?.toLowerCase()
    return mode === 'team' || mode === 'both'
  })

  const canConfirm = computed(() => {
    if (loading.value) return false
    
    if (selectedMode.value === 'individual') {
      return currentUser.value != null
    } else if (selectedMode.value === 'team') {
      return selectedTeam.value != null
    }
    
    return false
  })

  // 格式化参赛类型
  const formatParticipationType = (type) => {
    if (!type) return '不限'
    
    switch(type.toLowerCase()) {
      case 'individual':
        return '个人参赛'
      case 'team':
        return '团队参赛'
      case 'both':
        return '个人/团队均可'
      default:
        return type
    }
  }

  // 获取参赛模式样式类
  const getParticipationModeClass = () => {
    const mode = props.competition?.participationMode?.toLowerCase()
    switch (mode) {
      case 'individual':
        return 'individual'
      case 'team':
        return 'team'
      case 'both':
        return 'both'
      default:
        return ''
    }
  }

  // 获取团队状态样式类
  const getTeamStatusClass = (team) => {
    // 这里可以根据团队状态返回不同的样式类
    // 例如：检查团队是否已经参加了太多竞赛等
    return 'available'
  }

  // 获取团队状态文本
  const getTeamStatusText = (team) => {
    // 这里可以根据团队状态返回不同的文本
    return '可参赛'
  }

  // 获取当前用户信息
  const fetchCurrentUser = async () => {
    try {
      // 首先尝试从localStorage获取
      const userStr = localStorage.getItem('user')
      if (userStr) {
        const userData = JSON.parse(userStr)
        if (userData.id) {
          // 使用用户ID从API获取完整信息
          const response = await userApi.getById(userData.id)
          currentUser.value = response
          return
        }
      }
      
      // 如果localStorage没有用户信息，显示错误
      errorMessage.value = '请先登录'
    } catch (error) {
      console.error('获取用户信息失败:', error)
      errorMessage.value = '获取用户信息失败'
    }
  }

  // 获取用户团队列表
  const fetchUserTeams = async () => {
    if (!currentUser.value?.id) return
    
    try {
      loading.value = true
      const response = await teamApi.getUserTeams(currentUser.value.id)
      if (response && Array.isArray(response)) {
        // 为每个团队确定用户角色
        userTeams.value = response.map(team => {
          // 判断用户在团队中的角色
          let userRole = '队员'
          if (team.leaderId === currentUser.value.id) {
            userRole = '队长'
          }
          
          return {
            ...team,
            userRole: userRole,
            teamName: team.name, // 确保有teamName字段
            memberCount: team.memberCount || 0
          }
        })
      } else {
        userTeams.value = []
      }
    } catch (error) {
      console.error('获取用户团队失败:', error)
      errorMessage.value = '获取团队信息失败'
      userTeams.value = []
    } finally {
      loading.value = false
    }
  }

  // 参赛模式变化处理
  const onModeChange = () => {
    errorMessage.value = ''
    selectedTeam.value = null
    
    if (selectedMode.value === 'team' && userTeams.value.length === 0) {
      fetchUserTeams()
    }
  }

  // 选择团队
  const selectTeam = (team) => {
    selectedTeam.value = team
    errorMessage.value = ''
  }

  // 创建新团队
  const createNewTeam = () => {
    // 跳转到团队创建页面
    router.push('/team/create')
    emit('close')
  }

  // 确认参赛
  const confirmParticipation = async () => {
    if (!canConfirm.value) return
    
    try {
      loading.value = true
      errorMessage.value = ''
      
      const participationData = {
        competitionId: props.competition.id,
        userId: currentUser.value.id,
        participationMode: selectedMode.value
      }
      
      if (selectedMode.value === 'team' && selectedTeam.value) {
        participationData.teamId = selectedTeam.value.id
        participationData.role = selectedTeam.value.userRole
      } else if (selectedMode.value === 'individual') {
        participationData.role = '个人'
      }
      
      console.log('发送参赛请求:', participationData)
      
      const response = await competitionApi.participate(participationData)
      
      console.log('参赛响应:', response)
      
      // 参赛成功 - 后端返回的是字符串消息
      const successMessage = typeof response === 'string' ? response : '报名成功！'
      
      // 显示成功消息
      showSuccess(successMessage)
      
      // 触发成功事件，让父组件处理
      emit('success', {
        message: successMessage,
        mode: selectedMode.value,
        team: selectedTeam.value
      })
      
    } catch (error) {
      console.error('参赛失败:', error)
      
      // 处理不同类型的错误响应
      let errorMsg = '参赛失败，请重试'
      
      if (error.response) {
        // 服务器返回了错误响应
        if (error.response.data) {
          if (typeof error.response.data === 'string') {
            errorMsg = error.response.data
          } else if (error.response.data.message) {
            errorMsg = error.response.data.message
          }
        }
      } else if (error.message) {
        // 网络错误或其他错误
        errorMsg = error.message
      }
      
      errorMessage.value = errorMsg
      
      // 显示错误消息
      showError(errorMsg)
    } finally {
      loading.value = false
    }
  }

  // 初始化参赛信息
  const initializeParticipation = async () => {
    errorMessage.value = ''
    selectedMode.value = ''
    selectedTeam.value = null
    
    // 获取用户信息
    await fetchCurrentUser()
    
    // 根据竞赛参赛模式设置默认选择
    const mode = props.competition?.participationMode?.toLowerCase()
    if (mode === 'individual') {
      selectedMode.value = 'individual'
    } else if (mode === 'team') {
      selectedMode.value = 'team'
      await fetchUserTeams()
    }
    // 如果是 'both'，让用户自己选择
  }

  // 监听竞赛变化
  watch(() => props.competition, (newCompetition) => {
    if (newCompetition && props.visible) {
      initializeParticipation()
    }
  }, { immediate: true })

  return {
    // 响应式数据
    selectedMode,
    selectedTeam,
    userTeams,
    currentUser,
    loading,
    errorMessage,
    
    // 计算属性
    showModeSelection,
    canParticipateIndividual,
    canParticipateTeam,
    canConfirm,
    
    // 方法
    formatParticipationType,
    getParticipationModeClass,
    getTeamStatusClass,
    getTeamStatusText,
    onModeChange,
    selectTeam,
    createNewTeam,
    confirmParticipation,
    initializeParticipation,
    fetchCurrentUser,
    fetchUserTeams
  }
}