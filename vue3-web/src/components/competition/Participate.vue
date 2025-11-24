<template>
  <!-- 遮罩层和弹窗完全分离 -->
  <template v-if="visible">
    <!-- 背景遮罩层 -->
    <div class="participate-modal-overlay" @click="handleOverlayClick"></div>

    <!-- 弹窗主体 -->
    <div class="participate-modal" @click.stop>
      <div class="modal-header">
        <h3>参加竞赛</h3>
        <button class="close-btn" @click="closeModal">×</button>
      </div>

      <div class="modal-body">
        <!-- 竞赛信息 -->
        <div class="competition-info">
          <h4>{{ competition?.title }}</h4>
          <p class="competition-desc">{{ competition?.description }}</p>
          <div class="participation-mode">
            <span class="mode-label">参赛方式：</span>
            <span class="mode-value" :class="getParticipationModeClass()">
              {{ formatParticipationType(competition?.participationMode) }}
            </span>
          </div>
        </div>

        <!-- 参赛方式选择 -->
        <div class="participation-selection" v-if="showModeSelection">
          <label class="select-label">
            <span class="label-text">参赛方式</span>
            <select
              v-model="selectedMode"
              @change="onModeChange"
              class="mode-select"
            >
              <option value="" disabled>请选择参赛方式</option>
              <option value="individual" v-if="canParticipateIndividual">个人参赛</option>
              <option value="team" v-if="canParticipateTeam">团队参赛</option>
            </select>
          </label>
        </div>

        <!-- 个人参赛信息 -->
        <div class="individual-info" v-if="selectedMode === 'individual'">
          <h5>个人信息</h5>
          <div class="user-card">
            <div class="user-avatar">
              <img :src="currentUser?.avatarUrl || '/default-avatar.png'" :alt="currentUser?.realName">
            </div>
            <div class="user-details">
              <div class="user-name">{{ currentUser?.realName || currentUser?.username }}</div>
              <div class="user-info">
                <span>专业：{{ currentUser?.major || '未设置' }}</span>
                <span>学号：{{ currentUser?.studentId || '未设置' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 团队参赛选择 -->
        <div class="team-selection" v-if="selectedMode === 'team'">
          <h5>选择团队</h5>

          <!-- 团队列表 -->
          <div class="team-list" v-if="userTeams.length > 0">
            <div
              class="team-item"
              v-for="team in userTeams"
              :key="team.id"
              :class="{ active: selectedTeam?.id === team.id }"
              @click="selectTeam(team)"
            >
              <div class="team-info">
                <div class="team-name">{{ team.teamName }}</div>
                <div class="team-desc">{{ team.description || '暂无描述' }}</div>
                <div class="team-members">
                  <span class="member-count">成员数：{{ team.memberCount || 0 }}</span>
                  <span class="team-role" :class="{ 'leader': team.userRole === '队长', 'member': team.userRole === '队员' }">
                    {{ team.userRole }}
                  </span>
                </div>
              </div>
              <div class="team-status" :class="getTeamStatusClass(team)">
                {{ getTeamStatusText(team) }}
              </div>
            </div>
          </div>

          <!-- 无团队提示 -->
          <div class="no-teams" v-else>
            <div class="empty-state">
              <span class="empty-icon">👥</span>
              <p>您还没有加入任何团队</p>
              <button class="btn btn-primary" @click="createNewTeam">
                创建新团队
              </button>
            </div>
          </div>
        </div>

        <!-- 错误信息 -->
        <div class="error-message" v-if="errorMessage">
          <span class="error-icon">⚠️</span>
          {{ errorMessage }}
        </div>

        <!-- 成功信息 -->
        <div class="success-message" v-if="successMessage">
          <span class="success-icon">✅</span>
          {{ successMessage }}
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" @click="closeModal">取消</button>
        <button
          class="btn btn-primary"
          @click="confirmParticipation"
          :disabled="!canConfirm || loading"
        >
          {{ loading ? '提交中...' : '确认参赛' }}
        </button>
      </div>
    </div>
  </template>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'
import { useParticipate } from '../../composables/participate.js'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  competition: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'success'])

const {
  selectedMode,
  selectedTeam,
  userTeams,
  currentUser,
  loading,
  errorMessage,
  showModeSelection,
  canParticipateIndividual,
  canParticipateTeam,
  canConfirm,
  formatParticipationType,
  getParticipationModeClass,
  getTeamStatusClass,
  getTeamStatusText,
  onModeChange,
  selectTeam,
  createNewTeam,
  confirmParticipation: originalConfirmParticipation,
  initializeParticipation
} = useParticipate(props, emit)

// 成功消息
const successMessage = ref('')

// 处理遮罩层点击
const handleOverlayClick = () => {
  closeModal()
}

// 关闭弹窗
const closeModal = () => {
  emit('close')
}

// 确认参赛
const confirmParticipation = async () => {
  try {
    successMessage.value = ''
    await originalConfirmParticipation()
    // 如果成功，会触发success事件，父组件会关闭弹窗
  } catch (error) {
    // 错误已经在useParticipate中处理了
    console.error('参赛确认失败:', error)
  }
}

// 监听弹窗显示状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    initializeParticipation()
    // 禁用背景滚动，确保弹窗始终在视口中央
    document.body.style.overflow = ''
  } else {
    // 恢复背景滚动
    document.body.style.overflow = ''
  }
})

// 组件卸载时确保恢复滚动
onUnmounted(() => {
  document.body.style.overflow = ''
})
</script>

<style>
@import '../../styles/participate.css';
</style>