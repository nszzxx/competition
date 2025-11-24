<template>
  <div v-if="showModal" class="modal-overlay" @click="closeModal">
    <div class="modal-content member-detail-content" @click.stop>
      <div class="modal-header">
        <h3 class="modal-title">成员详细信息</h3>
        <button @click="closeModal" class="close-btn" type="button">×</button>
      </div>

      <div class="modal-body">
        <div v-if="loading" class="loading-state">
          <p>加载中...</p>
        </div>

        <div v-else-if="error" class="error-state">
          <p>{{ error }}</p>
          <button @click="loadDetail" class="action-btn btn-primary">重试</button>
        </div>

        <div v-else-if="displayMember" class="member-detail-body">
          <!-- 成员基本信息卡片 -->
          <div class="detail-card">
            <div class="card-header">
              <h4>基本信息</h4>
            </div>
            <div class="card-body">
              <div class="member-profile">
                <div class="profile-avatar">
                  <img
                    :src="displayMember.avatarUrl || displayMember.avatar || '/placeholder-avatar.png'"
                    :alt="displayMember.displayName || displayMember.username"
                    @error="handleImageError"
                    class="avatar-large"
                  />
                </div>
                <div class="profile-info">
                  <h3 class="profile-name">
                    {{ displayMember.displayName || displayMember.realName || displayMember.username || '未知用户' }}
                  </h3>
                  <div class="profile-role">
                    <span class="role-badge">{{ getRoleText(displayMember.role) }}</span>
                  </div>
                </div>
              </div>

              <div class="info-rows">
                <div class="info-row">
                  <label>用户名</label>
                  <span>{{ displayMember.username || '未知' }}</span>
                </div>
                <div class="info-row" v-if="displayMember.realName">
                  <label>真实姓名</label>
                  <span>{{ displayMember.realName }}</span>
                </div>
                <div class="info-row" v-if="displayMember.email">
                  <label>邮箱</label>
                  <span>{{ displayMember.email }}</span>
                </div>
                <div class="info-row" v-if="displayMember.major">
                  <label>专业</label>
                  <span>{{ displayMember.major }}</span>
                </div>
                <div class="info-row">
                  <label>加入时间</label>
                  <span>{{ formatDate(displayMember.joinedAt || displayMember.createTime) }}</span>
                </div>
                <div class="info-row" v-if="displayMember.status">
                  <label>状态</label>
                  <span :class="['status-tag', getStatusClass(displayMember.status)]">
                    {{ getStatusText(displayMember.status) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 技能信息卡片 -->
          <div class="detail-card" v-if="hasSkills">
            <div class="card-header">
              <h4>技能专长</h4>
            </div>
            <div class="card-body">
              <div class="skills-list">
                <span
                  v-for="skill in skillsList"
                  :key="skill"
                  class="skill-tag"
                >
                  {{ skill }}
                </span>
              </div>
            </div>
          </div>

          <!-- 荣誉信息卡片 -->
          <div class="detail-card" v-if="hasHonours">
            <div class="card-header">
              <h4>获得荣誉</h4>
            </div>
            <div class="card-body">
              <div class="honours-list">
                <div
                  v-for="(honour, index) in honoursList"
                  :key="`${honour.title}-${index}`"
                  class="honour-item"
                >
                  <div class="honour-info">
                    <div class="honour-title">{{ honour.title }}</div>
                    <div class="honour-date">{{ honour.date }}</div>
                  </div>
                  <div class="honour-action" v-if="honour.certificateUrl">
                    <button
                      class="action-btn btn-secondary"
                      @click="viewCertificate(honour.certificateUrl, honour.title)"
                    >
                      查看证书
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!hasSkills && !hasHonours" class="empty-additional">
            <p>暂无更多信息</p>
          </div>
        </div>

        <div v-else class="empty-state">
          <p>暂无成员信息</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useTeamember } from '../../composables/useTeamember.js'

// 定义 props
const props = defineProps({
  showModal: {
    type: Boolean,
    default: false
  },
  member: {
    type: Object,
    default: null
  },
  teamId: {
    type: [String, Number],
    default: null
  },
  currentUserId: {
    type: [String, Number],
    default: null
  }
})

// 定义 emits
const emit = defineEmits(['update:showModal'])

// 使用组合式函数
const {
  memberDetail,
  loading,
  error,
  loadMemberDetail,
  formatDate,
  parseSkills,
  parseHonours,
  getRoleText,
  getStatusText,
  handleImageError
} = useTeamember()

// 计算属性
const displayMember = computed(() => {
  return memberDetail.value || props.member
})

const skillsList = computed(() => {
  return displayMember.value?.skills ? parseSkills(displayMember.value.skills) : []
})

const honoursList = computed(() => {
  return displayMember.value?.honours ? parseHonours(displayMember.value.honours) : []
})

const hasSkills = computed(() => skillsList.value.length > 0)
const hasHonours = computed(() => honoursList.value.length > 0)

const showModal = computed({
  get: () => props.showModal,
  set: (value) => emit('update:showModal', value)
})

// 方法
const closeModal = () => {
  showModal.value = false
}

const viewCertificate = (url, title) => {
  if (url) {
    window.open(url, '_blank', 'noopener,noreferrer')
  }
}

const getStatusClass = (status) => {
  const statusMap = {
    'ACTIVE': 'status-approved',
    'INACTIVE': 'status-rejected',
    'PENDING': 'status-pending'
  }
  return statusMap[status] || 'status-pending'
}

const loadDetail = () => {
  if (props.member && props.teamId && props.currentUserId) {
    loadMemberDetail(props.teamId, props.member.userId, props.currentUserId)
  }
}

// 监听弹窗显示状态
const lastLoadedMemberId = ref(null)

watch(() => props.showModal, (newVal) => {
  if (newVal && props.member) {
    if (props.teamId && props.currentUserId && lastLoadedMemberId.value !== props.member.userId) {
      loadDetail()
      lastLoadedMemberId.value = props.member.userId
    }
  }
}, { immediate: false })

// 监听member变化
watch(() => props.member?.userId, (newVal) => {
  if (newVal !== lastLoadedMemberId.value) {
    lastLoadedMemberId.value = null
  }
})
</script>

<style scoped>
/* 使用父组件的 team-control.css 样式 */
/* 额外的成员详情特定样式 */

/* 确保模态框遮罩层的 z-index 高于 ControlHeader，并使用安全的布局 */
.modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  z-index: 1000 !important; /* 确保高于 ControlHeader 的所有元素 */
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  background: rgba(0, 0, 0, 0.5) !important;
  padding: 20px !important; /* 确保模态框有边距，不会贴边 */
  overflow: auto !important; /* 当内容过高时允许滚动整个遮罩层 */
}

/* 确保模态框结构正确 */
.member-detail-content {
  max-width: 700px !important;
  width: 90% !important;
  max-height: 85vh !important; /* 限制最大高度，确保不会超出视口 */
  margin: auto !important; /* 自动居中 */
  display: flex;
  flex-direction: column;
  padding: 0 !important;
  overflow: hidden;
  position: relative;
  background: white !important;
  border-radius: 12px !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3) !important;
}

/* 修复 modal-header 样式，确保关闭按钮可见且可点击 */
.member-detail-content .modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #f8fafc;
  border-radius: 12px 12px 0 0;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 2;
}

.member-detail-content .modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.member-detail-content .close-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  flex-shrink: 0;
  transition: all 0.2s ease;
  position: relative;
  z-index: 3;
}

.member-detail-content .close-btn:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 50%;
  transform: scale(1.1);
}

/* 修复 modal-body 滚动 */
.member-detail-content .modal-body {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 24px;
  min-height: 0;
}

/* 自定义滚动条样式 */
.member-detail-content .modal-body::-webkit-scrollbar {
  width: 8px;
}

.member-detail-content .modal-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.member-detail-content .modal-body::-webkit-scrollbar-thumb {
  background: #667eea;
  border-radius: 4px;
}

.member-detail-content .modal-body::-webkit-scrollbar-thumb:hover {
  background: #5a67d8;
}

.member-detail-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 详情卡片 */
.detail-card {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 12px 16px;
}

.card-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: white;
}

.card-body {
  padding: 20px;
}

/* 成员档案 */
.member-profile {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 2px solid #e5e7eb;
}

.profile-avatar {
  flex-shrink: 0;
}

.avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-name {
  margin: 0 0 12px 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  overflow-wrap: break-word;
}

.profile-role {
  display: flex;
  gap: 8px;
}

.role-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

/* 信息行 */
.info-rows {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.info-row:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.info-row label {
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
  min-width: 80px;
}

.info-row span {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
  text-align: right;
  overflow-wrap: break-word;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-approved {
  background: #d1fae5;
  color: #059669;
}

.status-rejected {
  background: #fee2e2;
  color: #dc2626;
}

.status-pending {
  background: #fef3c7;
  color: #d97706;
}

/* 技能列表 */
.skills-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.skill-tag {
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  color: #667eea;
  border: 2px solid #667eea;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.skill-tag:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 荣誉列表 */
.honours-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.honour-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.honour-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.honour-info {
  flex: 1;
  min-width: 0;
}

.honour-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
  overflow-wrap: break-word;
}

.honour-date {
  font-size: 13px;
  color: #6b7280;
}

.honour-action {
  flex-shrink: 0;
}

/* 状态提示 */
.loading-state,
.error-state,
.empty-state,
.empty-additional {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
}

.empty-additional {
  padding: 30px 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 2px dashed #e5e7eb;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .member-detail-content {
    max-width: 95vw !important;
    max-height: 95vh !important;
  }

  .member-detail-content .modal-header {
    padding: 16px;
  }

  .member-detail-content .modal-body {
    padding: 16px;
  }

  .member-profile {
    flex-direction: column;
    text-align: center;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .info-row label {
    min-width: auto;
  }

  .info-row span {
    text-align: left;
  }

  .honour-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .honour-action {
    width: 100%;
  }

  .honour-action .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
