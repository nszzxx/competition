// 团队相关的组合式函数
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { teamApi, competitionApi } from '../utils/api.js'

export function useTeamCreate() {
  const router = useRouter()
  const route = useRoute()

  const competition = ref(null)
  const currentUser = ref(null)
  const loading = ref(false)
  const error = ref('')

  const form = ref({
    name: '',
    description: '',
    isRecruiting: false,
    maxMembers: 3,
    requiredSkills: '',
    recruitmentMessage: ''
  })

  const competitionId = route.query.competitionId

  onMounted(() => {
    // 获取当前用户信息
    const user = localStorage.getItem('user')
    if (user) {
      try {
        currentUser.value = JSON.parse(user)
      } catch (e) {
        console.error('用户信息解析失败:', e)
        router.push('/')
        return
      }
    } else {
      router.push('/')
      return
    }

    // 获取竞赛信息
    if (competitionId) {
      fetchCompetition()
    }
  })

  const fetchCompetition = async () => {
    try {
      const data = await competitionApi.getById(competitionId)
      competition.value = data
    } catch (err) {
      console.error('获取竞赛信息失败:', err)
      error.value = '获取竞赛信息失败'
    }
  }

  const handleCreateTeam = async () => {
    if (!form.value.name.trim()) {
      error.value = '请输入团队名称'
      return
    }

    if (!competitionId) {
      error.value = '缺少竞赛信息'
      return
    }

    loading.value = true
    error.value = ''

    try {
      const teamData = {
        name: form.value.name.trim(),
        leaderId: currentUser.value.id,
        competitionId: parseInt(competitionId),
        description: form.value.description
      }

      await teamApi.create(teamData)
      
      alert('团队创建成功！')
      router.push(`/teams?competitionId=${competitionId}`)
    } catch (err) {
      console.error('创建团队失败:', err)
      error.value = err.response?.data?.message || '创建失败，请重试'
    } finally {
      loading.value = false
    }
  }

  const goBack = () => {
    if (competitionId) {
      router.push(`/competition/${competitionId}`)
    } else {
      router.push('/competitions')
    }
  }

  return {
    // 响应式状态
    competition,
    currentUser,
    loading,
    error,
    form,
    
    // 方法
    handleCreateTeam,
    goBack,
    fetchCompetition
  }
}

// 团队管理相关的组合式函数
export function useTeamManagement() {
  const router = useRouter()
  
  const teams = ref([])
  const loading = ref(false)
  const error = ref('')
  const selectedTeam = ref(null)

  const fetchTeams = async (competitionId) => {
    try {
      loading.value = true
      error.value = ''
      
      const data = await teamApi.getCompetitionTeams(competitionId)
      teams.value = Array.isArray(data) ? data : []
    } catch (err) {
      console.error('获取团队列表失败:', err)
      error.value = '获取团队列表失败'
      teams.value = []
    } finally {
      loading.value = false
    }
  }

  const joinTeam = async (teamId) => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      alert('请先登录')
      return
    }

    try {
      await teamApi.addMember(teamId, { userId: user.id })
      alert('加入团队成功！')
      // 重新获取团队列表
      const competitionId = teams.value.find(t => t.id === teamId)?.competitionId
      if (competitionId) {
        fetchTeams(competitionId)
      }
    } catch (err) {
      console.error('加入团队失败:', err)
      alert(err.response?.data?.message || '加入团队失败')
    }
  }

  const leaveTeam = async (teamId) => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      alert('请先登录')
      return
    }

    if (!confirm('确定要退出团队吗？')) {
      return
    }

    try {
      await teamApi.removeMember(teamId, user.id)
      alert('退出团队成功！')
      // 重新获取团队列表
      const competitionId = teams.value.find(t => t.id === teamId)?.competitionId
      if (competitionId) {
        fetchTeams(competitionId)
      }
    } catch (err) {
      console.error('退出团队失败:', err)
      alert(err.response?.data?.message || '退出团队失败')
    }
  }

  const deleteTeam = async (teamId) => {
    if (!confirm('确定要解散团队吗？此操作不可撤销！')) {
      return
    }

    try {
      await teamApi.delete(teamId)
      alert('团队解散成功！')
      // 重新获取团队列表
      const competitionId = teams.value.find(t => t.id === teamId)?.competitionId
      if (competitionId) {
        fetchTeams(competitionId)
      }
    } catch (err) {
      console.error('解散团队失败:', err)
      alert(err.response?.data?.message || '解散团队失败')
    }
  }

  const viewTeamDetail = (team) => {
    selectedTeam.value = team
    router.push(`/team/${team.id}`)
  }

  const isTeamLeader = (team) => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    return user.id === team.leaderId
  }

  const isTeamMember = (team) => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    return team.members && team.members.some(member => member.id === user.id)
  }

  const getTeamMemberCount = (team) => {
    return team.members ? team.members.length : 1
  }

  return {
    // 响应式状态
    teams,
    loading,
    error,
    selectedTeam,
    
    // 方法
    fetchTeams,
    joinTeam,
    leaveTeam,
    deleteTeam,
    viewTeamDetail,
    isTeamLeader,
    isTeamMember,
    getTeamMemberCount
  }
}

// 团队详情相关的组合式函数
export function useTeamDetail() {
  const router = useRouter()
  const route = useRoute()
  
  const team = ref(null)
  const members = ref([])
  const projects = ref([])
  const loading = ref(false)
  const error = ref('')

  const teamId = route.params.id

  onMounted(() => {
    fetchTeamDetail()
    fetchTeamMembers()
  })

  const fetchTeamDetail = async () => {
    try {
      loading.value = true
      const data = await teamApi.getById(teamId)
      team.value = data
    } catch (err) {
      console.error('获取团队详情失败:', err)
      error.value = '获取团队详情失败'
    } finally {
      loading.value = false
    }
  }

  const fetchTeamMembers = async () => {
    try {
      const data = await teamApi.getMembers(teamId)
      members.value = Array.isArray(data) ? data : []
    } catch (err) {
      console.error('获取团队成员失败:', err)
      members.value = []
    }
  }

  const removeMember = async (memberId) => {
    if (!confirm('确定要移除该成员吗？')) {
      return
    }

    try {
      await teamApi.removeMember(teamId, memberId)
      alert('成员移除成功！')
      fetchTeamMembers()
    } catch (err) {
      console.error('移除成员失败:', err)
      alert(err.response?.data?.message || '移除成员失败')
    }
  }

  const inviteMember = async (email) => {
    try {
      // 这里需要实现邀请API
      alert('邀请功能开发中...')
    } catch (err) {
      console.error('邀请成员失败:', err)
      alert('邀请发送失败')
    }
  }

  const goBack = () => {
    router.go(-1)
  }

  return {
    // 响应式状态
    team,
    members,
    projects,
    loading,
    error,
    
    // 方法
    fetchTeamDetail,
    fetchTeamMembers,
    removeMember,
    inviteMember,
    goBack
  }
}