import { ref, reactive, computed } from 'vue'
import axios from 'axios'

export function useTeamember() {
  // 响应式数据
  const teamInfo = ref(null)
  const teamMembers = ref([])
  const memberDetail = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // API 基础URL
  const API_BASE_URL = 'http://localhost:8080/api'

  // 数据缓存和请求去重
  const dataCache = new Map()
  const pendingRequests = new Map()
  const CACHE_TTL = 5 * 60 * 1000 // 5分钟缓存

  // 定期清理过期缓存
  if (typeof window !== 'undefined') {
    setInterval(() => {
      const now = Date.now()
      for (const [key, value] of dataCache.entries()) {
        if (now - value.timestamp > CACHE_TTL) {
          dataCache.delete(key)
        }
      }
    }, 60000) // 每分钟清理一次
  }

  /**
   * 从缓存获取数据或执行请求
   */
  const getCachedOrFetch = async (cacheKey, fetchFn) => {
    // 检查缓存
    const cached = dataCache.get(cacheKey)
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      return cached.data
    }

    // 检查是否有进行中的请求
    if (pendingRequests.has(cacheKey)) {
      return pendingRequests.get(cacheKey)
    }

    // 执行请求
    const promise = fetchFn()
    pendingRequests.set(cacheKey, promise)

    try {
      const data = await promise
      // 缓存结果
      dataCache.set(cacheKey, { data, timestamp: Date.now() })
      return data
    } finally {
      pendingRequests.delete(cacheKey)
    }
  }

  /**
   * 加载团队基础信息（包含队长信息）- 优化版本
   */
  const loadTeamInfo = async (teamId) => {
    if (!teamId) return

    // 防止重复加载
    if (teamInfo.value && teamInfo.value.id === teamId) {
      return
    }

    loading.value = true
    error.value = null

    try {
      const cacheKey = `team_${teamId}`
      const team = await getCachedOrFetch(cacheKey, async () => {
        const response = await axios.get(`${API_BASE_URL}/teams/${teamId}`)
        return response.data
      })

      // 如果有队长ID，获取队长信息
      if (team.leaderId && !team.leaderName) {
        try {
          const leaderCacheKey = `user_${team.leaderId}`
          const leader = await getCachedOrFetch(leaderCacheKey, async () => {
            const leaderResponse = await axios.get(`${API_BASE_URL}/user/${team.leaderId}`)
            return leaderResponse.data
          })
          team.leaderName = leader.realName || leader.username || '未知队长'
        } catch (leaderErr) {
          console.warn('获取队长信息失败:', leaderErr)
          team.leaderName = '未知队长'
        }
      }

      teamInfo.value = team
    } catch (err) {
      console.error('加载团队信息失败:', err)
      error.value = err.response?.data?.error || '加载团队信息失败'
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载团队成员列表（包含详细信息）- 优化版本
   */
  const loadTeamMembers = async (teamId, currentUserId) => {
    if (!teamId || !currentUserId) return

    // 防止重复加载相同数据
    const firstMember = teamMembers.value[0]
    if (teamMembers.value.length > 0 && firstMember?.teamId === teamId) {
      return
    }

    loading.value = true
    error.value = null

    try {
      const cacheKey = `team_members_${teamId}_${currentUserId}`
      const members = await getCachedOrFetch(cacheKey, async () => {
        const response = await axios.get(`${API_BASE_URL}/teams/${teamId}/members/details?currentUserId=${currentUserId}`)
        return response.data || []
      })

      teamMembers.value = members
    } catch (err) {
      console.error('加载团队成员失败:', err)
      error.value = err.response?.data?.error || '加载团队成员失败'
      teamMembers.value = []
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载成员详细信息（包含技能和荣誉）- 优化版本
   */
  const loadMemberDetail = async (teamId, userId, currentUserId) => {
    if (!userId) return

    // 防止重复加载相同成员的详细信息
    if (memberDetail.value && memberDetail.value.userId === userId) {
      return
    }

    loading.value = true
    error.value = null

    try {
      // 如果没有teamId或currentUserId，直接获取用户基本信息
      if (!teamId || !currentUserId) {
        const cacheKey = `user_${userId}`
        const user = await getCachedOrFetch(cacheKey, async () => {
          const response = await axios.get(`${API_BASE_URL}/user/${userId}`)
          return response.data
        })

        memberDetail.value = {
          userId: user.id,
          username: user.username,
          realName: user.realName,
          email: user.email,
          major: user.major,
          avatarUrl: user.avatarUrl,
          displayName: user.realName || user.username,
          skills: user.skills || '',
          honours: user.honours || '',
          status: 'active'
        }
        loading.value = false
        return
      }

      // 优化：优先从已加载的团队成员数据中查找
      if (teamMembers.value.length > 0) {
        const existingMember = teamMembers.value.find(member => member.userId === userId)
        if (existingMember) {
          memberDetail.value = { ...existingMember, teamId }
          loading.value = false
          return
        }
      }

      // 如果没有找到，使用缓存机制发起API请求
      const cacheKey = `team_members_${teamId}_${currentUserId}`
      const memberDetails = await getCachedOrFetch(cacheKey, async () => {
        const response = await axios.get(
          `${API_BASE_URL}/teams/${teamId}/members/details?currentUserId=${currentUserId}`
        )
        return response.data || []
      })

      // 从返回的成员列表中找到指定用户的详细信息
      const targetMember = memberDetails.find(member => member.userId === userId)

      if (targetMember) {
        memberDetail.value = { ...targetMember, teamId }
      } else {
        // 如果团队成员中没有，尝试直接获取用户信息
        const userCacheKey = `user_${userId}`
        const user = await getCachedOrFetch(userCacheKey, async () => {
          const response = await axios.get(`${API_BASE_URL}/user/${userId}`)
          return response.data
        })

        memberDetail.value = {
          userId: user.id,
          username: user.username,
          realName: user.realName,
          email: user.email,
          major: user.major,
          avatarUrl: user.avatarUrl,
          displayName: user.realName || user.username,
          skills: user.skills || '',
          honours: user.honours || '',
          status: 'active',
          teamId: teamId
        }
      }
    } catch (err) {
      console.error('加载成员详细信息失败:', err)
      error.value = err.response?.data?.error || '加载成员详细信息失败'
    } finally {
      loading.value = false
      console.log('成员详细信息加载完成，信息为:', memberDetail.value)
    }
  }

  /**
   * 格式化日期
   */
  const formatDate = (dateString) => {
    if (!dateString) return '未知'
    
    try {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    } catch (err) {
      return '日期格式错误'
    }
  }

  /**
   * 解析技能字符串 - 优化版本，添加缓存
   */
  const skillsCache = new Map()
  const parseSkills = (skillsString) => {
    if (!skillsString || typeof skillsString !== 'string') return []

    // 检查缓存
    if (skillsCache.has(skillsString)) {
      return skillsCache.get(skillsString)
    }

    const skills = skillsString
      .split(',')
      .map(skill => skill.trim())
      .filter(skill => skill.length > 0)

    // 缓存结果
    skillsCache.set(skillsString, skills)

    // 限制缓存大小
    if (skillsCache.size > 100) {
      const firstKey = skillsCache.keys().next().value
      skillsCache.delete(firstKey)
    }

    return skills
  }

  /**
   * 解析荣誉字符串（支持证书URL）- 优化版本，添加缓存
   * 格式：title(date)|certificateUrl,title2(date2)|certificateUrl2
   */
  const honoursCache = new Map()
  const parseHonours = (honoursString) => {
    if (!honoursString || typeof honoursString !== 'string') return []

    // 检查缓存
    if (honoursCache.has(honoursString)) {
      return honoursCache.get(honoursString)
    }

    const honours = honoursString
      .split(',')
      .map(honour => {
        // 检查是否包含证书URL（用|分隔）
        const parts = honour.split('|')
        const titleAndDate = parts[0]
        const certificateUrl = parts.length > 1 ? parts[1].trim() : null

        // 解析标题和日期：title(date)
        const match = titleAndDate.match(/^(.+)\((\d{4}-\d{2}-\d{2})\)$/)
        if (match) {
          return {
            title: match[1].trim(),
            date: match[2],
            certificateUrl: certificateUrl
          }
        }
        return {
          title: titleAndDate.trim(),
          date: '未知日期',
          certificateUrl: certificateUrl
        }
      })
      .filter(honour => honour.title.length > 0)

    // 缓存结果
    honoursCache.set(honoursString, honours)

    // 限制缓存大小
    if (honoursCache.size > 100) {
      const firstKey = honoursCache.keys().next().value
      honoursCache.delete(firstKey)
    }

    return honours
  }

  /**
   * 获取角色文本
   */
  const getRoleText = (role) => {
    const roleMap = {
      'leader': '队长',
      'member': '成员',
      'admin': '管理员'
    }
    return roleMap[role] || role || '未知角色'
  }

  /**
   * 获取状态文本
   */
  const getStatusText = (status) => {
    const statusMap = {
      'active': '活跃',
      'inactive': '非活跃',
      'pending': '待审核',
      'suspended': '暂停'
    }
    return statusMap[status] || status || '未知状态'
  }

  /**
   * 处理图片加载错误
   */
  // const handleImageError = (event) => {
  //   event.target.src = '/placeholder-avatar.png'
  // }

  /**
   * 清空数据和缓存
   */
  const clearData = () => {
    teamInfo.value = null
    teamMembers.value = []
    memberDetail.value = null
    error.value = null
  }

  /**
   * 清空所有缓存
   */
  const clearCache = () => {
    dataCache.clear()
    pendingRequests.clear()
  }

  /**
   * 清空特定团队的缓存
   */
  const clearTeamCache = (teamId) => {
    if (!teamId) return
    const keysToDelete = []
    for (const key of dataCache.keys()) {
      if (key.includes(`team_${teamId}`) || key.includes(`team_members_${teamId}`)) {
        keysToDelete.push(key)
      }
    }
    keysToDelete.forEach(key => dataCache.delete(key))
  }

  /**
   * 智能加载团队完整信息（优化版本）
   * 一次性加载团队信息和成员信息，减少API调用
   */
  const loadTeamCompleteInfo = async (teamId, currentUserId) => {
    if (!teamId || !currentUserId) return

    // 防止重复加载
    if (teamInfo.value && teamInfo.value.id === teamId && teamMembers.value.length > 0) {
      return
    }

    loading.value = true
    error.value = null

    try {
      // 使用缓存机制并行加载
      const [team, members] = await Promise.all([
        getCachedOrFetch(`team_${teamId}`, async () => {
          const response = await axios.get(`${API_BASE_URL}/teams/${teamId}`)
          return response.data
        }),
        getCachedOrFetch(`team_members_${teamId}_${currentUserId}`, async () => {
          const response = await axios.get(`${API_BASE_URL}/teams/${teamId}/members/details?currentUserId=${currentUserId}`)
          return response.data || []
        })
      ])

      // 从成员信息中找到队长信息
      const leader = members.find(member => member.role === 'leader')
      if (leader) {
        team.leaderName = leader.displayName || leader.realName || leader.username || '未知队长'
      } else if (team.leaderId && !team.leaderName) {
        // 如果成员信息中没有队长，使用缓存获取队长信息
        try {
          const leaderData = await getCachedOrFetch(`user_${team.leaderId}`, async () => {
            const response = await axios.get(`${API_BASE_URL}/user/${team.leaderId}`)
            return response.data
          })
          team.leaderName = leaderData.realName || leaderData.username || '未知队长'
        } catch (leaderErr) {
          console.warn('获取队长信息失败:', leaderErr)
          team.leaderName = '未知队长'
        }
      }

      teamInfo.value = team
      teamMembers.value = members

    } catch (err) {
      console.error('加载团队完整信息失败:', err)
      error.value = err.response?.data?.error || '加载团队信息失败'
    } finally {
      loading.value = false
    }
  }

  return {
    // 响应式数据
    teamInfo,
    teamMembers,
    memberDetail,
    loading,
    error,

    // 方法
    loadTeamInfo,
    loadTeamMembers,
    loadMemberDetail,
    loadTeamCompleteInfo,
    formatDate,
    parseSkills,
    parseHonours,
    getRoleText,
    getStatusText,
    // handleImageError,
    clearData,
    clearCache,
    clearTeamCache
  }
}