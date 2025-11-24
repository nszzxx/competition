<template>
  <div class="ai-user-profile">
    <div class="profile-header">
      <div class="user-avatar">
        <img v-if="userProfile?.avatarUrl" :src="userProfile.avatarUrl" :alt="userProfile.username" />
        <div v-else class="avatar-placeholder">
          {{ userProfile?.username?.charAt(0)?.toUpperCase() || '?' }}
        </div>
      </div>
      <div class="user-info">
        <h3 class="username">{{ userProfile?.username || '未登录用户' }}</h3>
        <p class="major">{{ userProfile?.major || '专业未设置' }}</p>
        <p class="email">{{ userProfile?.email || '邮箱未设置' }}</p>
      </div>
    </div>

    <div class="profile-stats">
      <div class="stat-item">
        <span class="stat-number">{{ skillCount }}</span>
        <span class="stat-label">技能数量</span>
      </div>
      <div class="stat-item">
        <span class="stat-number">{{ recommendationCount }}</span>
        <span class="stat-label">AI推荐</span>
      </div>
      <div class="stat-item">
        <span class="stat-number">{{ chatCount }}</span>
        <span class="stat-label">对话次数</span>
      </div>
    </div>

    <div class="profile-actions">
      <button @click="refreshProfile" class="btn btn-primary" :disabled="loading">
        {{ loading ? '刷新中...' : '刷新信息' }}
      </button>
      <button @click="viewFullProfile" class="btn btn-secondary">
        查看完整档案
      </button>
    </div>

    <div v-if="userSkills.length > 0" class="user-skills">
      <h4>技能标签</h4>
      <div class="skills-tags">
        <span 
          v-for="skill in userSkills" 
          :key="skill.id" 
          class="skill-tag"
        >
          {{ skill.skill }}
        </span>
      </div>
    </div>

    <div class="ai-insights">
      <h4>AI洞察</h4>
      <div class="insight-item">
        <span class="insight-label">专业匹配度：</span>
        <span class="insight-value">{{ calculateMajorMatch() }}%</span>
      </div>
      <div class="insight-item">
        <span class="insight-label">技能完整度：</span>
        <span class="insight-value">{{ calculateSkillCompleteness() }}%</span>
      </div>
      <div class="insight-item">
        <span class="insight-label">推荐精准度：</span>
        <span class="insight-value">{{ calculateRecommendationAccuracy() }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../utils/api.js'

const router = useRouter()

// 响应式数据
const userProfile = ref(null)
const userSkills = ref([])
const loading = ref(false)
const skillCount = ref(0)
const recommendationCount = ref(0)
const chatCount = ref(0)

// 获取当前用户信息
const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch (e) {
      console.error('解析用户信息失败:', e)
      return null
    }
  }
  return null
}

// 刷新用户档案
const refreshProfile = async () => {
  loading.value = true
  try {
    const user = getCurrentUser()
    if (!user) {
      console.warn('用户未登录')
      return
    }

    // 获取用户详细信息
    const userDetails = await userApi.getById(user.id)
    userProfile.value = userDetails

    // 获取用户技能
    const skills = await userApi.getSkills(user.id)
    userSkills.value = Array.isArray(skills) ? skills : []
    skillCount.value = userSkills.value.length

    // 获取统计信息
    const stats = await userApi.getStats(user.id)
    recommendationCount.value = stats.recommendationCount || 0
    chatCount.value = stats.chatCount || 0

    console.log('用户档案刷新完成:', userProfile.value)
  } catch (error) {
    console.error('刷新用户档案失败:', error)
  } finally {
    loading.value = false
  }
}

// 查看完整档案
const viewFullProfile = () => {
  router.push('/profile')
}

// 计算专业匹配度
const calculateMajorMatch = () => {
  if (!userProfile.value?.major) return 0
  
  // 基于专业的匹配度计算逻辑
  const majorKeywords = ['计算机', '软件', '信息', '数据', '人工智能']
  const major = userProfile.value.major.toLowerCase()
  
  let matchScore = 0
  majorKeywords.forEach(keyword => {
    if (major.includes(keyword)) {
      matchScore += 20
    }
  })
  
  return Math.min(matchScore, 100)
}

// 计算技能完整度
const calculateSkillCompleteness = () => {
  const skillCount = userSkills.value.length
  if (skillCount === 0) return 0
  if (skillCount >= 10) return 100
  return Math.round((skillCount / 10) * 100)
}

// 计算推荐精准度
const calculateRecommendationAccuracy = () => {
  // 基于用户信息完整度计算推荐精准度
  let accuracy = 0
  
  if (userProfile.value?.major) accuracy += 30
  if (userProfile.value?.email) accuracy += 10
  if (userSkills.value.length > 0) accuracy += 40
  if (userSkills.value.length >= 5) accuracy += 20
  
  return Math.min(accuracy, 100)
}

// 组件挂载时初始化
onMounted(() => {
  userProfile.value = getCurrentUser()
  if (userProfile.value) {
    refreshProfile()
  }
})
</script>

<style scoped>
.ai-user-profile {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.user-avatar {
  width: 60px;
  height: 60px;
  margin-right: 15px;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  font-weight: bold;
}

.user-info {
  flex: 1;
}

.username {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 18px;
}

.major, .email {
  margin: 2px 0;
  color: #666;
  font-size: 14px;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #007bff;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.profile-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.user-skills {
  margin-bottom: 20px;
}

.user-skills h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

.skills-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  background: #e3f2fd;
  color: #1976d2;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  border: 1px solid #bbdefb;
}

.ai-insights {
  border-top: 1px solid #eee;
  padding-top: 15px;
}

.ai-insights h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
}

.insight-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.insight-label {
  color: #666;
  font-size: 14px;
}

.insight-value {
  color: #007bff;
  font-weight: bold;
  font-size: 14px;
}
</style>