import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

export function useUserList(props) {
  const router = useRouter()
  
  // 响应式数据
  const individuals = ref([])
  const loading = ref(false)
  const searchKeyword = ref('')
  const currentUser = ref(null)
  
  // 计算属性
  const competitionId = computed(() => {
    return props?.competitionId || router.currentRoute.value.params.id
  })
  
  // 过滤后的个人列表
  const filteredIndividuals = computed(() => {
    if (!searchKeyword.value.trim()) {
      return individuals.value
    }
    
    const keyword = searchKeyword.value.toLowerCase()
    return individuals.value.filter(individual => {
      const name = (individual.realName || individual.username || '').toLowerCase()
      const username = (individual.username || '').toLowerCase()
      const major = (individual.major || '').toLowerCase()
      const skills = (individual.skills || '').toLowerCase()
      
      return name.includes(keyword) || 
             username.includes(keyword) || 
             major.includes(keyword) ||
             skills.includes(keyword)
    })
  })
  
  // 获取当前用户信息
  const getCurrentUser = async () => {
    try {
      const token = localStorage.getItem('token')
      if (!token) return null
      
      const response = await axios.get('/api/users/current', {
        headers: { Authorization: `Bearer ${token}` }
      })
      currentUser.value = response.data
      return response.data
    } catch (error) {
      console.error('获取当前用户信息失败:', error)
      return null
    }
  }
  
  // 获取个人参赛者列表
  const fetchIndividuals = async () => {
    if (!competitionId.value) {
      console.warn('竞赛ID不存在')
      return
    }
    
    loading.value = true
    try {
      const response = await axios.get(`/api/competitions/${competitionId.value}/participants/user`)
      
      // 处理返回的数据，确保包含必要的字段
      individuals.value = (response.data || []).map(user => ({
        id: user.id,
        username: user.username,
        realName: user.realName,
        major: user.major,
        avatarUrl: user.avatarUrl,
        status: user.status,
        skills: user.skills || '', // 如果没有技能信息，设为空字符串
        bio: user.bio || '', // 个人简介
        email: user.email,
        phone: user.phone,
        grade: user.grade,
        studentId: user.studentId
      }))
      
      console.log(`成功获取 ${individuals.value.length} 个参赛个人`)
    } catch (error) {
      console.error('获取个人参赛者失败:', error)
      individuals.value = []
      
      // 根据错误类型给出不同的提示
      if (error.response?.status === 404) {
        console.warn('竞赛不存在或没有个人参赛者')
      } else if (error.response?.status === 403) {
        console.warn('没有权限查看参赛者信息')
      }
    } finally {
      loading.value = false
    }
  }
  
  // 搜索个人参赛者
  const searchIndividuals = async () => {
    // 由于使用计算属性进行前端过滤，这里只需要触发重新计算
    // 如果需要后端搜索，可以在这里实现
    console.log('搜索关键词:', searchKeyword.value)
  }
  
  // 刷新个人列表
  const refreshIndividuals = async () => {
    await fetchIndividuals()
  }
  
  // 清空搜索
  const clearSearch = () => {
    searchKeyword.value = ''
  }
  
  // 获取个人详细信息
  const getIndividualDetail = async (userId) => {
    try {
      const response = await axios.get(`/api/users/${userId}`)
      return response.data
    } catch (error) {
      console.error('获取个人详细信息失败:', error)
      return null
    }
  }
  
  // 格式化技能标签
  const formatSkills = (skillsString) => {
    if (!skillsString || !skillsString.trim()) {
      return []
    }
    return skillsString.split(',').map(skill => skill.trim()).filter(skill => skill)
  }
  
  // 检查是否为当前用户
  const isCurrentUser = (userId) => {
    return currentUser.value && currentUser.value.id === userId
  }
  
  // 获取用户显示名称
  const getUserDisplayName = (user) => {
    return user.realName || user.username || '未知用户'
  }
  
  // 获取用户头像
  const getUserAvatar = (user) => {
    return user.avatarUrl || '/default-avatar.png'
  }
  
  // 导出统计信息
  const getStatistics = computed(() => {
    return {
      total: individuals.value.length,
      filtered: filteredIndividuals.value.length,
      withSkills: individuals.value.filter(user => user.skills && user.skills.trim()).length,
      withBio: individuals.value.filter(user => user.bio && user.bio.trim()).length
    }
  })
  
  // 组件挂载时初始化
  onMounted(async () => {
    await getCurrentUser()
    await fetchIndividuals()
  })
  
  return {
    // 响应式数据
    individuals: filteredIndividuals,
    loading,
    searchKeyword,
    currentUser,
    
    // 计算属性
    competitionId,
    statistics: getStatistics,
    
    // 方法
    fetchIndividuals,
    searchIndividuals,
    refreshIndividuals,
    clearSearch,
    getIndividualDetail,
    formatSkills,
    isCurrentUser,
    getUserDisplayName,
    getUserAvatar,
    getCurrentUser
  }
}