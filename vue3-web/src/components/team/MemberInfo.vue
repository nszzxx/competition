<template>
  <div v-if="showModal" class="modal-overlay" @click="closeModal">
    <div class="member-info-modal" @click.stop>
      <div class="modal-header">
        <h2>成员详情</h2>
        <button class="close-btn" @click="closeModal">×</button>
      </div>

      <div class="modal-content" v-if="displayMember">
        <!-- 成员基础信息 -->
        <div class="member-basic-info">
          <div class="member-avatar-large">
            <img
              :src="displayMember.avatarUrl || displayMember.avatar"
              :alt="displayMember.displayName || displayMember.username"
              loading="lazy"
            />
          </div>
          <div class="member-details">
            <h3>{{ displayMember.displayName || displayMember.realName || displayMember.username || '未知用户' }}</h3>
            <p class="member-role-large">{{ getRoleText(displayMember.role) }}</p>
            <div class="member-info-grid">
              <div class="info-item">
                <label>用户名</label>
                <span>{{ displayMember.username || '未知' }}</span>
              </div>
              <div class="info-item" v-if="displayMember.realName">
                <label>真实姓名</label>
                <span>{{ displayMember.realName }}</span>
              </div>
              <div class="info-item" v-if="displayMember.email">
                <label>邮箱</label>
                <span>{{ displayMember.email }}</span>
              </div>
              <div class="info-item" v-if="displayMember.major">
                <label>专业</label>
                <span>{{ displayMember.major }}</span>
              </div>
              <div class="info-item">
                <label>加入时间</label>
                <span>{{ formatDate(displayMember.joinedAt || displayMember.createTime) }}</span>
              </div>
              <div class="info-item" v-if="displayMember.status">
                <label>状态</label>
                <span class="status-badge" :class="displayMember.status">
                  {{ getStatusText(displayMember.status) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 技能信息 -->
        <div class="member-skills" v-if="hasSkills">
          <h4>技能专长</h4>
          <div class="skills-container">
            <span
              v-for="skill in skillsList"
              :key="skill"
              class="skill-tag member-skill"
            >
              {{ skill }}
            </span>
          </div>
        </div>

        <!-- 荣誉信息 -->
        <div class="member-honours" v-if="hasHonours">
          <h4>获得荣誉</h4>
          <div class="honours-container">
            <div
              v-for="(honour, index) in honoursList"
              :key="`${honour.title}-${index}`"
              class="honour-item"
            >
              <div class="honour-content">
                <div class="honour-title">{{ honour.title }}</div>
                <div class="honour-date">{{ honour.date }}</div>
              </div>
              <div class="honour-actions" v-if="honour.certificateUrl">
                <button
                  class="view-certificate-btn"
                  @click="viewCertificate(honour.certificateUrl, honour.title)"
                  title="查看证书"
                >
                  📜 查看证书
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态提示 -->
        <div v-if="!hasSkills && !hasHonours" class="no-additional-info">
          <p>暂无更多信息</p>
        </div>
      </div>

      <div class="modal-loading" v-else-if="loading && !displayMember">
        <p>加载中...</p>
      </div>

      <div class="modal-error" v-else-if="error && !displayMember">
        <p>{{ error }}</p>
        <button @click="loadDetail" class="retry-btn">重试</button>
      </div>

      <div class="modal-error" v-else-if="!displayMember">
        <p>暂无成员信息</p>
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

// 计算属性 - 使用 memberDetail 或 fallback 到 props.member
const displayMember = computed(() => {
  return memberDetail.value || props.member
})

// 计算属性 - 优化性能，避免重复解析
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

const loadDetail = () => {
  console.log('Loading member detail:', {
    member: props.member,
    teamId: props.teamId,
    currentUserId: props.currentUserId
  })

  if (props.member && props.teamId && props.currentUserId) {
    loadMemberDetail(props.teamId, props.member.userId, props.currentUserId)
  }
}

// 监听弹窗显示状态 - 防止重复加载
const lastLoadedMemberId = ref(null)

watch(() => props.showModal, (newVal) => {
  console.log('Modal visibility changed:', newVal)
  if (newVal && props.member) {
    console.log('Member data:', props.member)
    // 只有当成员ID变化或首次加载时才重新加载
    if (props.teamId && props.currentUserId && lastLoadedMemberId.value !== props.member.userId) {
      loadDetail()
      lastLoadedMemberId.value = props.member.userId
    }
  }
}, { immediate: false })

// 监听member变化 - 重置加载状态
watch(() => props.member?.userId, (newVal) => {
  if (newVal !== lastLoadedMemberId.value) {
    lastLoadedMemberId.value = null
  }
})
</script>

<style scoped>
@import '../../styles/teamember.css';
</style>