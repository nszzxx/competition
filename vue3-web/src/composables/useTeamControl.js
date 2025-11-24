import { ref, reactive, onMounted } from 'vue'
import { teamApplicationApi } from '../utils/api.js'

export function useTeamControl() {
  const loading = ref(false)
  const currentUser = ref(null)
  const activeTab = ref('created')
  const showInviteModal = ref(false)
  const showMemberModal = ref(false)
  const showMemberDetailModal = ref(false)
  const selectedTeam = ref(null)
  const selectedMember = ref(null)

  const teams = reactive({
    created: [],
    applied: [],
    joined: []
  })

  const teamMembers = ref([])
  const searchKeyword = ref('')
  const inviteForm = reactive({
    identifier: '', // 用户名/邮箱/手机号
    message: ''
  })

  const tabs = [
    { key: 'created', label: '已创建', icon: '👑' },
    // { key: 'applied', label: '申请中', icon: '⏳' },
    { key: 'joined', label: '已加入', icon: '👥' }
  ]

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

  const loadTeams = async () => {
    if (!currentUser.value) return

    loading.value = true
    try {
      // 使用axios发送请求
      const axios = (await import('axios')).default

      // 获取用户创建的团队
      const createdResponse = await axios.get(`/api/teams/user/${currentUser.value.id}`)
      teams.created = (createdResponse.data || []).filter(team => team.leaderId === currentUser.value.id)

      // 获取用户加入的团队
      teams.joined = (createdResponse.data || []).filter(team => team.leaderId !== currentUser.value.id)

      // 获取用户申请的团队
      const appliedResponse = await teamApplicationApi.getUserApplications(currentUser.value.id)
      teams.applied = (appliedResponse.data || []).filter(app => app.status === 'PENDING' && app.type === 'apply')

    } catch (error) {
      console.error('加载团队失败:', error)
    } finally {
      loading.value = false
    }
  }

  const setActiveTab = (tab) => {
    activeTab.value = tab
  }

  const viewTeamMembers = async (team) => {
    selectedTeam.value = team
    try {
      // 动态导入axios
      const axios = (await import('axios')).default
      const response = await axios.get(`/api/teams/${team.id}/members`)
      teamMembers.value = response.data
      showMemberModal.value = true
    } catch (error) {
      console.error('获取团队成员失败:', error)
      alert('获取团队成员失败')
    }
  }

  const removeMember = async (memberId) => {
    if (!selectedTeam.value || !confirm('确定要移除这个成员吗？')) return

    try {
      // 动态导入axios
      const axios = (await import('axios')).default
      await axios.delete(`/api/teams/${selectedTeam.value.id}/members/${memberId}`)
      teamMembers.value = teamMembers.value.filter(member => member.userId !== memberId)
      alert('成员移除成功')
    } catch (error) {
      console.error('移除成员失败:', error)
      alert('移除成员失败')
    }
  }

  const dissolveTeam = async (teamId) => {
    if (!confirm('确定要解散这个团队吗？此操作不可撤销！')) return

    try {
      // 动态导入axios
      const axios = (await import('axios')).default
      await axios.delete(`/api/teams/${teamId}`)
      teams.created = teams.created.filter(team => team.id !== teamId)
      alert('团队解散成功')
    } catch (error) {
      console.error('解散团队失败:', error)
      alert('解散团队失败')
    }
  }

  const leaveTeam = async (teamId) => {
    if (!confirm('确定要退出这个团队吗？')) return

    try {
      // 动态导入axios
      const axios = (await import('axios')).default
      await axios.delete(`/api/teams/${teamId}/members/${currentUser.value.id}`)
      teams.joined = teams.joined.filter(team => team.id !== teamId)
      alert('退出团队成功')
    } catch (error) {
      console.error('退出团队失败:', error)
      alert('退出团队失败')
    }
  }

  const cancelApplication = async (applicationId) => {
    if (!confirm('确定要取消这个申请吗？')) return

    try {
      const response = await teamApplicationApi.cancelApplication(applicationId, currentUser.value.id)
      teams.applied = teams.applied.filter(app => app.id !== applicationId)
      alert('申请已取消')
    } catch (error) {
      console.error('取消申请失败:', error)
      alert('取消申请失败')
    }
  }

  const openInviteModal = (team) => {
    selectedTeam.value = team
    showInviteModal.value = true
    searchKeyword.value = ''
    inviteForm.identifier = ''
    inviteForm.message = ''
  }

  const sendInvite = async () => {
    if (!inviteForm.identifier.trim()) {
      alert('请输入用户名、邮箱或手机号')
      return
    }

    try {
      const response = await teamApplicationApi.inviteUserToTeam(
        selectedTeam.value.id,
        currentUser.value.id,
        inviteForm.identifier.trim(),
        inviteForm.message.trim()
      )

      // 修复：axios已经解包了一层data，所以直接检查response.success
      if (response.success) {
        alert(response.message || '邀请发送成功')
        closeInviteModal()
      } else {
        alert(response.message || '邀请发送失败')
      }
    } catch (error) {
      console.error('发送邀请失败:', error)
      alert(error.response?.data?.message || '邀请发送失败')
    }
  }

  const closeInviteModal = () => {
    showInviteModal.value = false
    selectedTeam.value = null
    searchKeyword.value = ''
    inviteForm.identifier = ''
    inviteForm.message = ''
  }

  const closeMemberModal = () => {
    showMemberModal.value = false
    selectedTeam.value = null
    teamMembers.value = []
  }

  const getStatusText = (status) => {
    const statusMap = {
      'PENDING': '待审核',
      'APPROVED': '已通过',
      'REJECTED': '已拒绝'
    }
    return statusMap[status] || status
  }

  const getStatusClass = (status) => {
    const classMap = {
      'PENDING': 'status-pending',
      'APPROVED': 'status-approved',
      'REJECTED': 'status-rejected'
    }
    return classMap[status] || ''
  }

  // 查看成员详情
  const viewMemberDetail = (member) => {
    selectedMember.value = member
    showMemberDetailModal.value = true
  }

  // 获取角色显示文本
  const getRoleDisplayText = (role) => {
    const roleMap = {
      'leader': '队长',
      'member': '成员',
      'vice_leader': '副队长'
    }
    return roleMap[role] || role || '成员'
  }

  return {
    // 响应式数据
    loading,
    currentUser,
    activeTab,
    showInviteModal,
    showMemberModal,
    showMemberDetailModal,
    selectedTeam,
    selectedMember,
    teams,
    teamMembers,
    searchKeyword,
    inviteForm,
    tabs,

    // 方法
    loadUserInfo,
    loadTeams,
    setActiveTab,
    viewTeamMembers,
    viewMemberDetail,
    getRoleDisplayText,
    removeMember,
    dissolveTeam,
    leaveTeam,
    cancelApplication,
    openInviteModal,
    sendInvite,
    closeInviteModal,
    closeMemberModal,
    getStatusText,
    getStatusClass
  }
}