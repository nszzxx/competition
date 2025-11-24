import { useCompetitionDetail } from './useCompetition.js'
import { computed } from 'vue'

export function useCompetitionDetailExtended() {
  // 使用基础竞赛详情组合式函数
  const {
    competition,
    teams,
    loading,
    hasJoined,
    competitionRules,
    goBack,
    joinCompetition: originalJoinCompetition,
    cancelParticipation,
    createTeam,
    viewTeams,
    viewProjects,
    formatDate,
    getCompetitionStatus,
    getStatusClass,
    formatRuleKey,
    getTeamMemberCount,
    fetchCompetition,
    currentUser // 从基础组合式函数中获取当前用户
  } = useCompetitionDetail()

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

  // 获取报名状态
  const getRegistrationStatus = computed(() => {
    if (!competition.value || !competition.value.patiStarttime || !competition.value.patiEndtime) {
      return null
    }
    
    const now = new Date()
    const startTime = new Date(competition.value.patiStarttime)
    const endTime = new Date(competition.value.patiEndtime)
    
    if (now < startTime) {
      return 0 // 报名未开始
    } else if (now > endTime) {
      return 2 // 报名已结束
    } else {
      return 1 // 报名中
    }
  })

  // 获取报名状态文本
  const getRegistrationStatusText = () => {
    switch (getRegistrationStatus.value) {
      case 0:
        return '报名未开始'
      case 1:
        return '报名中'
      case 2:
        return '报名已结束'
      default:
        return '未知状态'
    }
  }

  // 获取报名状态样式类
  const getRegistrationStatusClass = () => {
    switch (getRegistrationStatus.value) {
      case 0:
        return 'status-coming'
      case 1:
        return 'status-open'
      case 2:
        return 'status-closed'
      default:
        return ''
    }
  }

  // 获取参赛类型样式类
  const getParticipationTypeClass = () => {
    if (!competition.value || !competition.value.participationMode) {
      return ''
    }
    
    const type = competition.value.participationMode.toLowerCase()
    switch (type) {
      case 'individual':
        return 'type-individual'
      case 'team':
        return 'type-team'
      case 'both':
        return 'type-both'
      default:
        return ''
    }
  }

  // 格式化报名时间
  const formatRegistrationTime = () => {
    if (!competition.value) return '待定'
    
    const startTime = competition.value.patiStarttime || competition.value.registrationStartTime
    const endTime = competition.value.patiEndtime || competition.value.registrationEndTime
    
    if (!startTime || !endTime) return '待定'
    
    return `${formatDate(startTime)} - ${formatDate(endTime)}`
  }


  return {
    // 从基础组合式函数中继承的属性和方法
    competition,
    teams,
    loading,
    hasJoined,
    competitionRules,
    goBack,
    cancelParticipation,
    viewTeams,
    viewProjects,
    formatDate,
    getCompetitionStatus,
    getStatusClass,
    formatRuleKey,
    getTeamMemberCount,
    fetchCompetition,
    currentUser, // 添加当前用户

    // 扩展的属性和方法
    // 扩展的属性和方法
    formatParticipationType,
    getRegistrationStatus,
    getRegistrationStatusText,
    getRegistrationStatusClass,
    getParticipationTypeClass
  }
}