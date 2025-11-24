import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'

export function useApplication() {
  const loading = ref(false)
  const currentUser = ref(null)
  const activeTab = ref('received')
  
  const applications = reactive({
    received: [], // 收到的申请（作为队长）
    sent: []     // 发送的申请（作为申请者）
  })

  const tabs = [
    { key: 'received', label: '收到的申请', icon: '📥' },
    { key: 'sent', label: '发送的申请', icon: '📤' }
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

  const loadApplications = async () => {
    if (!currentUser.value) return

    loading.value = true
    try {
      // 获取用户相关的所有申请（包括作为申请人和作为队长的）
      const userResponse = await axios.get(`/api/team-applications/user/${currentUser.value.id}`)
      const allApplications = userResponse.data.data || []

      console.log('所有申请记录:', allApplications)

      // 分类逻辑：
      // 1. 普通用户视角：
      //    - userId 匹配当前用户 + type=apply => 发送的申请
      //    - userId 匹配当前用户 + type=invite => 收到的邀请
      // 2. 队长视角：
      //    - leaderId 匹配当前用户 + type=apply => 收到的申请
      //    - leaderId 匹配当前用户 + type=invite => 发送的邀请

      const receivedApps = []
      const sentApps = []

      allApplications.forEach(app => {
        // 从关联对象中提取信息
        const applicantName = app.user?.realName || app.user?.username || '未知用户'
        const applicantMajor = app.user?.major || '专业未设置'
        const teamName = app.team?.name || '未知团队'
        const competitionName = app.team?.competitionName || '未知竞赛'
        const leaderName = app.leader?.realName || app.leader?.username || '未知队长'

        // 创建格式化的申请对象
        const formattedApp = {
          id: app.id,
          userId: app.userId,
          teamId: app.teamId,
          leaderId: app.leaderId,
          applicantName: applicantName,
          applicantMajor: applicantMajor,
          teamName: teamName,
          competitionName: competitionName,
          inviterName: leaderName,
          message: app.message || '',
          applicationTime: app.applicationTime || app.createdAt,
          status: app.status,
          type: app.type,
          rejectionReason: app.rejectionReason
        }

        // 分类逻辑
        if (app.userId === currentUser.value.id) {
          // 当前用户是申请人/被邀请人
          if (app.type === 'apply') {
            // 用户发送的申请
            sentApps.push(formattedApp)
          } else if (app.type === 'invite') {
            // 用户收到的邀请
            receivedApps.push(formattedApp)
          }
        } else if (app.leaderId === currentUser.value.id) {
          // 当前用户是队长
          if (app.type === 'apply') {
            // 队长收到的申请
            receivedApps.push(formattedApp)
          } else if (app.type === 'invite') {
            // 队长发送的邀请
            sentApps.push(formattedApp)
          }
        }
      })

      applications.received = receivedApps
      applications.sent = sentApps

      console.log('收到的申请/邀请:', receivedApps)
      console.log('发送的申请/邀请:', sentApps)

    } catch (error) {
      console.error('加载申请失败:', error)
      alert('加载申请失败，请刷新页面重试')
    } finally {
      loading.value = false
    }
  }

  const setActiveTab = (tab) => {
    activeTab.value = tab
  }

  const approveApplication = async (applicationId) => {
    // 查找当前申请，判断是申请还是邀请
    const application = [...applications.received].find(app => app.id === applicationId)
    if (!application) return

    const confirmText = application.type === 'apply'
      ? '确定要通过这个申请吗？'
      : '确定要接受这个邀请吗？'

    if (!confirm(confirmText)) return

    try {
      let response
      if (application.type === 'apply') {
        // 处理申请审核
        response = await axios.put(`/api/team-applications/${applicationId}/review`, {
          approved: true
        })
      } else {
        // 处理邀请响应
        response = await axios.put(`/api/team-applications/invitations/${applicationId}/respond`, {
          userId: currentUser.value.id,
          accepted: true
        })
      }

      if (response.data.success) {
        // 从列表中移除已处理的申请
        applications.received = applications.received.filter(app => app.id !== applicationId)
        alert(application.type === 'apply' ? '申请已通过' : '已接受邀请')
      }
    } catch (error) {
      console.error('处理失败:', error)
      alert('操作失败，请重试')
    }
  }

  const rejectApplication = async (applicationId, reason = '') => {
    const application = [...applications.received].find(app => app.id === applicationId)
    if (!application) return

    const confirmText = application.type === 'apply'
      ? '请输入拒绝理由（可选）：'
      : '确定要拒绝这个邀请吗？'

    const rejectionReason = reason || (application.type === 'apply' ? prompt(confirmText) : '')

    if (application.type === 'invite' && !confirm(confirmText)) return

    try {
      let response
      if (application.type === 'apply') {
        // 处理申请拒绝
        response = await axios.put(`/api/team-applications/${applicationId}/review`, {
          approved: false,
          rejectionReason: rejectionReason
        })
      } else {
        // 处理邀请拒绝
        response = await axios.put(`/api/team-applications/invitations/${applicationId}/respond`, {
          userId: currentUser.value.id,
          accepted: false
        })
      }

      if (response.data.success) {
        // 从列表中移除已处理的申请
        applications.received = applications.received.filter(app => app.id !== applicationId)
        alert(application.type === 'apply' ? '申请已拒绝' : '已拒绝邀请')
      }
    } catch (error) {
      console.error('处理失败:', error)
      alert('操作失败，请重试')
    }
  }

  const cancelSentApplication = async (applicationId) => {
    if (!confirm('确定要撤回这个申请吗？')) return

    try {
      const response = await axios.delete(`/api/team-applications/${applicationId}?userId=${currentUser.value.id}`)
      
      if (response.data.success) {
        applications.sent = applications.sent.filter(app => app.id !== applicationId)
        alert('申请已撤回')
      }
    } catch (error) {
      console.error('撤回申请失败:', error)
      alert('撤回申请失败，请重试')
    }
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

  const formatDateTime = (dateString) => {
    if (!dateString) return '未知'
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN')
  }

  const batchApprove = async () => {
    const pendingApplications = applications.received.filter(app => app.status === 'PENDING')
    if (pendingApplications.length === 0) {
      alert('没有待审核的申请/邀请')
      return
    }

    // 分离申请和邀请
    const applies = pendingApplications.filter(app => app.type === 'apply')
    const invites = pendingApplications.filter(app => app.type === 'invite')

    if (!confirm(`确定要批量通过 ${pendingApplications.length} 个申请/邀请吗？（申请：${applies.length}个，邀请：${invites.length}个）`)) return

    try {
      const processedIds = []

      // 处理申请
      if (applies.length > 0) {
        const applyIds = applies.map(app => app.id)
        const response = await axios.put('/api/team-applications/batch-review', {
          applicationIds: applyIds,
          approved: true
        })
        if (response.data.success) {
          processedIds.push(...applyIds)
        }
      }

      // 处理邀请
      if (invites.length > 0) {
        const inviteIds = invites.map(app => app.id)
        const response = await axios.put('/api/team-applications/batch-respond', {
          invitationIds: inviteIds,
          userId: currentUser.value.id,
          accepted: true
        })
        if (response.data.success) {
          processedIds.push(...inviteIds)
        }
      }

      // 只移除成功处理的记录
      applications.received = applications.received.filter(app => !processedIds.includes(app.id))
      alert(`成功处理 ${processedIds.length} 个申请/邀请`)

    } catch (error) {
      console.error('批量审核失败:', error)
      alert('批量审核失败，请重试')
    }
  }

  const batchReject = async () => {
    const pendingApplications = applications.received.filter(app => app.status === 'PENDING')
    if (pendingApplications.length === 0) {
      alert('没有待审核的申请/邀请')
      return
    }

    // 分离申请和邀请
    const applies = pendingApplications.filter(app => app.type === 'apply')
    const invites = pendingApplications.filter(app => app.type === 'invite')

    const reason = prompt('请输入批量拒绝的理由（可选）：')
    if (!confirm(`确定要批量拒绝 ${pendingApplications.length} 个申请/邀请吗？（申请：${applies.length}个，邀请：${invites.length}个）`)) return

    try {
      const processedIds = []

      // 处理申请
      if (applies.length > 0) {
        const applyIds = applies.map(app => app.id)
        const response = await axios.put('/api/team-applications/batch-review', {
          applicationIds: applyIds,
          approved: false,
          rejectionReason: reason
        })
        if (response.data.success) {
          processedIds.push(...applyIds)
        }
      }

      // 处理邀请
      if (invites.length > 0) {
        const inviteIds = invites.map(app => app.id)
        const response = await axios.put('/api/team-applications/batch-respond', {
          invitationIds: inviteIds,
          userId: currentUser.value.id,
          accepted: false
        })
        if (response.data.success) {
          processedIds.push(...inviteIds)
        }
      }

      // 只移除成功处理的记录
      applications.received = applications.received.filter(app => !processedIds.includes(app.id))
      alert(`成功处理 ${processedIds.length} 个申请/邀请`)

    } catch (error) {
      console.error('批量审核失败:', error)
      alert('批量审核失败，请重试')
    }
  }

  return {
    // 响应式数据
    loading,
    currentUser,
    activeTab,
    applications,
    tabs,

    // 方法
    loadUserInfo,
    loadApplications,
    setActiveTab,
    approveApplication,
    rejectApplication,
    cancelSentApplication,
    getStatusText,
    getStatusClass,
    formatDateTime,
    batchApprove,
    batchReject
  }
}