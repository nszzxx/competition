<template>
  <div v-if="showModal" class="modal-overlay" @click="closeModal">
    <div class="team-info-modal" @click.stop>
      <div class="modal-header">
        <h2>团队详情</h2>
        <button class="close-btn" @click="closeModal">×</button>
      </div>

      <div class="modal-content" v-if="teamInfo">
        <!-- 团队基础信息 -->
        <div class="team-basic-info">
          <h3>基础信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>团队名称</label>
              <span>{{ teamInfo.name }}</span>
            </div>
            <div class="info-item">
              <label>队长</label>
              <span>{{ teamInfo.leaderName || '未知' }}</span>
            </div>
            <div class="info-item">
              <label>成员数量</label>
              <span>{{ teamMembers.length }}人</span>
            </div>
            <div class="info-item">
              <label>创建时间</label>
              <span>{{ formatDate(teamInfo.createdAt) }}</span>
            </div>
          </div>

          <div class="team-description" v-if="teamInfo.description">
            <label>团队描述</label>
            <p>{{ teamInfo.description }}</p>
          </div>

          <div class="team-skills" v-if="teamInfo.needSkills">
            <label>招募技能</label>
            <div class="skills-tags">
              <span
                v-for="skill in parseSkills(teamInfo.needSkills)"
                :key="skill"
                class="skill-tag"
              >
                {{ skill }}
              </span>
            </div>
          </div>
        </div>

        <!-- 团队成员信息 -->
        <div class="team-members-info">
          <h3>团队成员</h3>
          <div class="members-grid" v-if="teamMembers.length > 0">
            <div
              v-for="member in teamMembers"
              :key="member.userId"
              class="member-card"
              @click="viewMemberDetail(member)"
            >
              <div class="member-avatar">
                <img
                  v-if="member.avatarUrl"
                  :src="member.avatarUrl"
                  :alt="member.displayName || member.realName || member.username"
                  loading="lazy"
                />
                <div v-else class="avatar-placeholder">
                  {{ (member.displayName || member.realName || member.username)?.charAt(0)?.toUpperCase() || 'U' }}
                </div>
              </div>
              <div class="member-info">
                <h4>{{ member.displayName || member.username }}</h4>
                <p class="member-role">{{ getRoleText(member.role) }}</p>
                <p class="member-major" v-if="member.major">{{ member.major }}</p>
                <p class="join-time">{{ formatDate(member.joinedAt) }} 加入</p>
              </div>
              <div class="member-actions">
                <button class="view-detail-btn" @click.stop="viewMemberDetail(member)">
                  查看详情
                </button>
              </div>
            </div>
          </div>
          <div v-else class="no-members">
            <p>暂无团队成员</p>
          </div>
        </div>
      </div>

      <div class="modal-loading" v-else-if="loading">
        <p>加载中...</p>
      </div>

      <div class="modal-error" v-else-if="error">
        <p>{{ error }}</p>
        <button @click="loadTeamInfoData" class="retry-btn">重试</button>
      </div>
    </div>
  </div>

  <!-- 成员详情弹窗 -->
  <MemberInfo
    v-if="showMemberModal"
    v-model:showModal="showMemberModal"
    :member="selectedMember"
    :teamId="teamId"
    :currentUserId="currentUserId"
  />
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useTeamember } from '../../composables/useTeamember.js'
import MemberInfo from './MemberInfo.vue'

// 定义 props
const props = defineProps({
  showModal: {
    type: Boolean,
    default: false
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
  teamInfo,
  teamMembers,
  loading,
  error,
  loadTeamCompleteInfo,
  formatDate,
  parseSkills,
  getRoleText,
  handleImageError
} = useTeamember()

// 成员详情弹窗相关
const showMemberModal = ref(false)
const selectedMember = ref(null)

// 计算属性
const showModal = computed({
  get: () => props.showModal,
  set: (value) => emit('update:showModal', value)
})

// 方法
const closeModal = () => {
  showModal.value = false
}

const viewMemberDetail = (member) => {
  selectedMember.value = member
  showMemberModal.value = true
}

// 加载团队信息的包装函数
const loadTeamInfoData = () => {
  if (props.teamId) {
    loadTeamCompleteInfo(props.teamId, props.currentUserId)
  }
}

// 监听弹窗显示状态 - 只在打开时加载
let hasLoaded = false
watch(() => props.showModal, (newVal) => {
  if (newVal && props.teamId && !hasLoaded) {
    loadTeamInfoData()
    hasLoaded = true
  }

  // 关闭时重置标记
  if (!newVal) {
    hasLoaded = false
  }
})

// 监听 teamId 变化
watch(() => props.teamId, () => {
  hasLoaded = false
})
</script>

<style scoped>
@import '../../styles/teamember.css';
</style>