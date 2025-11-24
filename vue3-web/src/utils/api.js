import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 0, // 取消超时限制
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 添加认证token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 对于参赛接口，直接返回响应数据
    if (response.config.url?.includes('/participate')) {
      return response.data
    }
    return response.data
  },
  error => {
    if (error.response?.status === 401) {
      // 清除token并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    
    // 确保错误信息能够正确传递
    if (error.response && error.response.data) {
      error.message = error.response.data
    }
    
    return Promise.reject(error)
  }
)

// 竞赛相关API
export const competitionApi = {
  // 获取所有竞赛
  getAll() {
    return api.get('/competitions')
  },
  
  // 根据ID获取竞赛详情
  getById(id) {
    return api.get(`/competitions/${id}`)
  },
  
  // 搜索竞赛
  search(params) {
    const queryParams = new URLSearchParams()
    if (params.category) queryParams.append('category', params.category)
    if (params.keyword) queryParams.append('keyword', params.keyword)
    if (params.participationMode) queryParams.append('participationMode', params.participationMode)
    
    return api.get(`/competitions/search?${queryParams.toString()}`)
  },
  
  // 获取热门竞赛
  getPopular(limit = 10) {
    return api.get(`/competitions/popular?limit=${limit}`)
  },
  
  // 获取即将开始的竞赛
  getUpcoming(limit = 10) {
    return api.get(`/competitions/upcoming?limit=${limit}`)
  },
  
  // 根据参赛模式获取竞赛
  getByMode(mode) {
    return api.get(`/competitions/by-mode/${mode}`)
  },
  
  // 获取竞赛分类统计
  getCategories() {
    return api.get('/competitions/categories')
  },
  
  // 获取用户参与的竞赛
  getUserParticipated(userId) {
    return api.get(`/competitions/user/${userId}`)
  },
  
  // 参赛相关API
  participate(participationData) {
    return api.post('/competitions/participate', participationData)
  },
  
  // 取消参赛
  cancelParticipation(competitionId, userId, teamId = null) {
    const params = new URLSearchParams()
    params.append('competitionId', competitionId)
    params.append('userId', userId)
    if (teamId) {
      params.append('teamId', teamId)
    }
    return api.delete(`/competitions/participate?${params.toString()}`)
  },
  
  // 获取竞赛参赛者
  getParticipants(competitionId) {
    return api.get(`/competitions/${competitionId}/participants`)
  },
  
  // 获取用户参赛详情
  getUserParticipationDetails(userId) {
    return api.get(`/competitions/user/${userId}/participants`)
  },
  
  // 获取当前用户信息
  getCurrentUser() {
    return api.get('/users/current')
  }
}

// 用户相关API
export const userApi = {
  // 获取用户信息
  getById(id) {
    return api.get(`/user/${id}`)
  },
  
  // 获取用户技能
  getSkills(userId) {
    return api.get(`/user/${userId}/skills`)
  },
  
  // 更新用户信息
  update(id, userData) {
    return api.put(`/user/${id}`, userData)
  },
  
  // 添加用户技能
  addSkill(userId, skill) {
    return api.post(`/user/${userId}/skills`, { skill })
  },
  
  // 删除用户技能
  removeSkill(userId, skillId) {
    return api.delete(`/user/${userId}/skills/${skillId}`)
  },
  
  // 获取用户统计信息
  getStats(userId) {
    return api.get(`/user/${userId}/stats`)
  },
  
  // 登录
  login(credentials) {
    console.log('API层发送登录数据:', credentials)
    return api.post('/user/login', {
      username: credentials.username,
      password: credentials.password
    })
  },
  
  // 注册
  register(userData) {
    return api.post('/user/register', userData)
  },
  
  // 获取所有用户
  getAll() {
    return api.get('/user')
  },
  
  // 搜索用户
  search(params) {
    const queryParams = new URLSearchParams()
    if (params.major) queryParams.append('major', params.major)
    if (params.skill) queryParams.append('skill', params.skill)
    
    return api.get(`/user/search?${queryParams.toString()}`)
  }
}

// 团队相关API
export const teamApi = {
  // 创建团队
  create(teamData) {
    return api.post('/teams', teamData)
  },
  
  // 获取团队详情
  getById(id) {
    return api.get(`/teams/${id}`)
  },
  
  // 获取团队成员
  getMembers(teamId) {
    return api.get(`/teams/${teamId}/members`)
  },
  
  // 获取团队成员详细信息（包含技能和荣誉）
  getMemberDetails(teamId, currentUserId) {
    return api.get(`/teams/${teamId}/members/details?currentUserId=${currentUserId}`)
  },
  
  // 添加团队成员
  addMember(teamId, memberData) {
    return api.post(`/teams/${teamId}/members`, memberData)
  },
  
  // 移除团队成员
  removeMember(teamId, userId) {
    return api.delete(`/teams/${teamId}/members/${userId}`)
  },
  
  // 获取用户的团队
  getUserTeams(userId) {
    return api.get(`/teams/user/${userId}`)
  },
  
  // 获取竞赛的所有团队
  getCompetitionTeams(competitionId) {
    return api.get(`/teams/competition/${competitionId}`)
  },
  
  // 获取竞赛团队卡片信息
  getCompetitionTeamCards(competitionId) {
    return api.get(`/teams/competition/${competitionId}/cards`)
  },
  
  // 获取竞赛团队卡片信息（包含适配度）
  getCompetitionTeamCardsWithMatchScore(competitionId, userId) {
    return api.get(`/teams/competition/${competitionId}/cards/match?userId=${userId}`)
  },
  
  // 更新团队信息
  update(id, teamData) {
    return api.put(`/teams/${id}`, teamData)
  },
  
  // 解散团队
  delete(id) {
    return api.delete(`/teams/${id}`)
  },
  
  // 搜索团队
  search(params) {
    const queryParams = new URLSearchParams()
    if (params.name) queryParams.append('name', params.name)
    if (params.competitionId) queryParams.append('competitionId', params.competitionId)
    
    return api.get(`/teams/search?${queryParams.toString()}`)
  },
  
  // 获取团队所需技能
  getTeamNeededSkills(teamId) {
    return api.get(`/teams/${teamId}/needed-skills`)
  }
}

// 团队申请相关API
export const teamApplicationApi = {
  // 申请加入团队
  applyToJoinTeam(userId, teamId) {
    return api.post('/team-applications/apply', {
      userId,
      teamId
    })
  },
  
  // 审核申请
  reviewApplication(applicationId, approved, rejectionReason = null) {
    return api.put(`/team-applications/${applicationId}/review`, {
      approved,
      rejectionReason
    })
  },
  
  // 获取团队的所有申请
  getTeamApplications(teamId) {
    return api.get(`/team-applications/team/${teamId}`)
  },
  
  // 获取用户的所有申请
  getUserApplications(userId) {
    return api.get(`/team-applications/user/${userId}`)
  },
  
  // 获取队长待审核的申请
  getPendingApplicationsByLeader(leaderId) {
    return api.get(`/team-applications/pending/leader/${leaderId}`)
  },
  
  // 取消申请
  cancelApplication(applicationId, userId) {
    return api.delete(`/team-applications/${applicationId}?userId=${userId}`)
  },
  
  // 检查用户申请状态
  checkUserApplication(userId, teamId) {
    return api.get(`/team-applications/check?userId=${userId}&teamId=${teamId}`)
  },
  
  // 批量审核申请
  batchReviewApplications(applicationIds, approved, rejectionReason = null) {
    return api.put('/team-applications/batch-review', {
      applicationIds,
      approved,
      rejectionReason
    })
  },

  // ==================== 邀请相关 ====================

  // 邀请用户加入团队（通过用户名/邮箱/手机号）
  inviteUserToTeam(teamId, inviterId, identifier, message = '') {
    return api.post('/team-applications/invite', {
      teamId,
      inviterId,
      identifier,
      message
    })
  },

  // 获取用户收到的邀请列表
  getUserInvitations(userId) {
    return api.get(`/team-applications/invitations/user/${userId}`)
  },

  // 响应邀请（接受/拒绝）
  respondToInvitation(invitationId, userId, accepted) {
    return api.put(`/team-applications/invitations/${invitationId}/respond`, {
      userId,
      accepted
    })
  }
}

// AI相关API
export const aiApi = {
  // 获取AI推荐
  getRecommendations(userId = null, category = '', difficulty = '') {
    if (!userId) {
      const user = localStorage.getItem('user')
      if (user) {
        try {
          const userObj = JSON.parse(user)
          userId = userObj.id
        } catch (e) {
          console.warn('解析用户信息失败:', e)
        }
      }
    }
    
    return api.post('/ai/recommendations', {
      userId: userId || 3,
      category: category,
      difficulty: difficulty
    })
  },

  // 智能搜索
  intelligentSearch(query, userId = null) {
    if (!userId) {
      const user = localStorage.getItem('user')
      if (user) {
        try {
          const userObj = JSON.parse(user)
          userId = userObj.id
        } catch (e) {
          console.warn('解析用户信息失败:', e)
        }
      }
    }
    
    return api.post('/ai/search', {
      query: query,
      userId: userId || 3
    })
  },
  
  // AI聊天
  async chat(message, context = [], userId = null, conversationId = null) {
    try {
      if (!userId) {
        const user = localStorage.getItem('user')
        if (user) {
          try {
            const userObj = JSON.parse(user)
            userId = userObj.id
          } catch (e) {
            console.warn('解析用户信息失败:', e)
          }
        }
      }
      
      console.log('发送AI聊天请求:', { message, userId, context, conversationId })
      
      const response = await api.post('/ai/chat', {
        message: message,
        context: context || [],
        userId: userId || 3,
        groupId: conversationId,
        timestamp: new Date().toISOString()
      })
      
      console.log('AI聊天响应:', response)
      
      return {
        message: response.message || '抱歉，我暂时无法回答这个问题。',
        timestamp: response.timestamp || Date.now(),
        messageId: response.messageId || Date.now().toString(),
        status: 'success',
        conversationId: response.conversationId || conversationId
      }
    } catch (error) {
      console.error('AI聊天API调用失败:', error)
      return {
        message: '抱歉，AI服务暂时不可用。这可能是因为：\n\n• 服务器正在维护\n• 网络连接问题\n• AI配置需要更新\n\n请稍后再试，或联系管理员。',
        timestamp: Date.now(),
        messageId: Date.now().toString(),
        status: 'error',
        conversationId: conversationId
      }
    }
  },

  // 获取用户所有AI对话列表
  // 获取用户所有AI对话列表
  async getConversations(userId) {
    try {
      console.log(`获取用户 ${userId} 的AI对话列表`)
      const response = await api.get(`/ai/chat/history/${userId}`)
      console.log(`成功获取 ${response.length} 条对话`)
      return response || []
    } catch (error) {
      console.error('获取AI对话列表失败:', error)
      return []
    }
  },

  // 获取特定对话的聊天记录
  async getConversationHistory(userId, conversationId) {
    try {
      console.log(`获取用户 ${userId} 对话 ${conversationId} 的聊天记录`)
      const response = await api.get(`/ai/chat/conversation/${userId}/${conversationId}`)
      console.log(`成功获取 ${response.length} 条对话记录`)
      return response || []
    } catch (error) {
      console.error('获取特定对话聊天记录失败:', error)
      return []
    }
  },
  
  // 技能分析
  analyzeSkills(userId = null) {
    if (!userId) {
      const user = localStorage.getItem('user')
      if (user) {
        try {
          const userObj = JSON.parse(user)
          userId = userObj.id
        } catch (e) {
          console.warn('解析用户信息失败:', e)
        }
      }
    }
    
    return api.post('/ai/analyze-skills', { 
      userId: userId || 3
    })
  },
  
  // 生成学习路径
  generateLearningPath(requestData) {
    return api.post('/ai/learning-path', requestData)
  },
  
  // 获取竞赛趋势
  getCompetitionTrends(requestData = {}) {
    // 确保传递用户信息和竞赛信息
    const payload = {
      userId: requestData.userId || null,
      participatedCompetitionId: requestData.participatedCompetitionId || null,
      availableCompetitionId: requestData.availableCompetitionId || null,
      userInfo: requestData.userInfo || null,
      ...requestData
    }
    
    console.log('发送趋势分析请求:', payload)
    return api.post('/ai/trends', payload)
  },
  
  // 获取准备建议
  getPreparationAdvice(competitionId, userId) {
    return api.post('/ai/preparation-advice', {
      competitionId,
      userId
    })
  },
  
  // 获取聊天历史记录
  getChatHistory(userId) {
    return api.get(`/ai/chat/history/${userId}`)
  }
}

// 认证相关API
export const authApi = {
  login(credentials) {
    return userApi.login(credentials)
  },
  
  register(userData) {
    return userApi.register(userData)
  },
  
  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    return Promise.resolve({ success: true })
  },
  
  refreshToken() {
    return api.post('/auth/refresh')
  }
}

// 推荐相关API
export const recommendationApi = {
  getUserRecommendations(userId) {
    return api.get(`/ai/recommendations/${userId}`)
  },
  
  getMatchScore(competitionId, userId) {
    return api.post('/ai/match-score', {
      competitionId,
      userId
    })
  },
  
  // 获取用户与团队的适配度
  getTeamMatchScore(teamId, userId) {
    return api.post('/ai/team-match-score', {
      teamId,
      userId
    })
  }
}

// 聊天记录相关API
export const chatApi = {
  // 获取用户所有对话列表
  getConversations(userId) {
    return aiApi.getConversations(userId)
  },
  
  // 获取特定对话的聊天记录
  getConversationHistory(userId, conversationId) {
    return aiApi.getConversationHistory(userId, conversationId)
  },
  
  // 发送聊天消息
  chat(message, userId = null, conversationId = null) {
    return aiApi.chat(message, [], userId, conversationId)
  },
  
  // 删除对话
  deleteConversation(userId, conversationId) {
    return api.delete(`/ai/chat/conversation/${userId}/${conversationId}`)
  }
}

// 项目相关API
export const projectApi = {
  // 上传项目计划书
  uploadDocument(competitionId, teamId, userId, participationMode, file) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('competitionId', competitionId)
    formData.append('participationMode', participationMode)

    if (teamId) {
      formData.append('teamId', teamId)
    }
    if (userId) {
      formData.append('userId', userId)
    }

    return api.post('/projects/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取项目计划书信息
  getDocument(competitionId, teamId = null, userId = null) {
    const params = new URLSearchParams()
    params.append('competitionId', competitionId)
    if (teamId) {
      params.append('teamId', teamId)
    }
    if (userId) {
      params.append('userId', userId)
    }

    return api.get(`/projects/document?${params.toString()}`)
  },

  // 检查是否已上传项目计划书
  checkDocument(competitionId, teamId = null, userId = null) {
    const params = new URLSearchParams()
    params.append('competitionId', competitionId)
    if (teamId) {
      params.append('teamId', teamId)
    }
    if (userId) {
      params.append('userId', userId)
    }

    return api.get(`/projects/check?${params.toString()}`)
  },

  // 获取所有项目
  getAll() {
    return api.get('/projects/list')
  },

  // 根据ID获取项目
  getById(id) {
    return api.get(`/projects/${id}`)
  },

  // 根据团队ID获取项目
  getByTeamId(teamId) {
    return api.get(`/projects/team/${teamId}`)
  }
}

export default api