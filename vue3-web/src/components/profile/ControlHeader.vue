<template>
  <div class="control-header">
    <div class="sidebar">
      <div class="sidebar-header">
        <div class="user-avatar">
          <img 
            v-if="currentUser?.avatar" 
            :src="currentUser.avatar" 
            :alt="currentUser.username"
            class="avatar-img"
          />
          <div v-else class="avatar-placeholder">
            {{ currentUser?.username?.charAt(0)?.toUpperCase() || 'U' }}
          </div>
        </div>
        <h3 class="user-title">{{ currentUser?.username || '用户' }}</h3>
        <p class="user-subtitle">{{ currentUser?.major || '专业未设置' }}</p>
      </div>

      <nav class="sidebar-nav">
        <button 
          v-for="item in menuItems" 
          :key="item.key"
          @click="setActiveTab(item.key)"
          :class="['nav-button', { active: activeTab === item.key }]"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-text">{{ item.label }}</span>
        </button>
      </nav>
    </div>

    <div class="main-content">
      <div class="content-header">
        <h2 class="content-title">{{ currentMenuItem?.label }}</h2>
        <div class="content-actions">
          <!-- 通知铃铛 -->
          <button 
            v-if="showNotificationBell"
            @click="toggleNotifications" 
            class="notification-btn"
            :class="{ active: showNotifications }"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 8C18 6.4087 17.3679 4.88258 16.2426 3.75736C15.1174 2.63214 13.5913 2 12 2C10.4087 2 8.88258 2.63214 7.75736 3.75736C6.63214 4.88258 6 6.4087 6 8C6 15 3 17 3 17H21C21 17 18 15 18 8Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M13.73 21C13.5542 21.3031 13.3019 21.5547 12.9982 21.7295C12.6946 21.9044 12.3504 21.9965 12 21.9965C11.6496 21.9965 11.3054 21.9044 11.0018 21.7295C10.6982 21.5547 10.4458 21.3031 10.27 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
          </button>
        </div>
      </div>

      <!-- 通知面板 -->
      <div v-if="showNotifications" class="notifications-panel">
        <div class="notifications-header">
          <h3>通知消息</h3>
          <button @click="showNotifications = false" class="close-btn">×</button>
        </div>
        <div class="notifications-content">
          <Application />
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="tab-content" v-show="!showNotifications">
        <ProfileControl v-if="activeTab === 'profile'" />
        <TeamControl v-else-if="activeTab === 'team'" />
        <CompetitionControl v-else-if="activeTab === 'competition'" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import ProfileControl from './ProfileControl.vue'
import TeamControl from './TeamControl.vue'
import CompetitionControl from './CompetitionControl.vue'
import Application from './Application.vue'

const activeTab = ref('profile')
const currentUser = ref(null)
const showNotifications = ref(false)
const unreadCount = ref(0)

const menuItems = [
  { key: 'profile', label: '个人信息', icon: '👤' },
  { key: 'team', label: '团队管理', icon: '👥' },
  { key: 'competition', label: '竞赛管理', icon: '🏆' }
]

const currentMenuItem = computed(() => {
  return menuItems.find(item => item.key === activeTab.value)
})

const showNotificationBell = computed(() => {
  return activeTab.value === 'profile' || activeTab.value === 'team'
})

onMounted(() => {
  loadUserInfo()
  loadNotificationCount()
})

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

const loadNotificationCount = async () => {
  if (!currentUser.value) return

  try {
    const axios = (await import('axios')).default
    // 获取用户相关的所有申请
    const response = await axios.get(`/api/team-applications/user/${currentUser.value.id}`)
    const allApplications = response.data.data || []

    // 计算待处理的数量
    let count = 0
    allApplications.forEach(app => {
      if (app.status !== 'PENDING') return

      // 用户收到的邀请 或 队长收到的申请
      if ((app.userId === currentUser.value.id && app.type === 'invite') ||
          (app.leaderId === currentUser.value.id && app.type === 'apply')) {
        count++
      }
    })

    unreadCount.value = count
  } catch (error) {
    console.error('获取通知数量失败:', error)
    unreadCount.value = 0
  }
}

const setActiveTab = (tab) => {
  activeTab.value = tab
  showNotifications.value = false
}

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
}
</script>

<style scoped>
.control-header {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
  position: relative;
}

.sidebar {
  width: 280px;
  background: white;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10; /* 确保侧边栏在正常内容之上，但低于模态框 */
}

.sidebar-header {
  padding: 32px 24px;
  text-align: center;
  border-bottom: 1px solid #e5e7eb;
}

.user-avatar {
  margin-bottom: 16px;
}

.avatar-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #667eea;
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  margin: 0 auto;
}

.user-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.user-subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.sidebar-nav {
  flex: 1;
  padding: 24px 0;
}

.nav-button {
  width: 100%;
  padding: 16px 24px;
  border: none;
  background: none;
  text-align: left;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  color: #4b5563;
}

.nav-button:hover {
  background: #f3f4f6;
  color: #667eea;
}

.nav-button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  position: relative;
}

.nav-button.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #667eea;
}

.nav-icon {
  font-size: 20px;
}

.nav-text {
  font-weight: 500;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.content-header {
  background: white;
  padding: 24px 32px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 10; /* 确保头部在正常内容之上，但低于模态框 */
}

.content-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.content-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-btn {
  position: relative;
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #6b7280;
}

.notification-btn:hover,
.notification-btn.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.notification-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #ef4444;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.notifications-panel {
  background: white;
  border-bottom: 1px solid #e5e7eb;
  max-height: 400px;
  overflow-y: auto;
  position: relative;
  z-index: 50; /* 通知面板需要高于其他内容 */
}

.notifications-header {
  padding: 16px 32px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notifications-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #ef4444;
}

.notifications-content {
  padding: 16px 32px;
}

.tab-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  position: relative;
  z-index: 1; /* 确保主内容区域的 z-index 最低 */
}

@media (max-width: 768px) {
  .control-header {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    position: relative;
  }

  .sidebar-nav {
    display: flex;
    overflow-x: auto;
    padding: 0;
  }

  .nav-button {
    min-width: 120px;
    padding: 12px 16px;
    text-align: center;
  }

  .content-header {
    padding: 16px 20px;
  }

  .tab-content {
    padding: 20px;
  }
}
</style>