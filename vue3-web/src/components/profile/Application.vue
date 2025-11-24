<template>
  <div class="application-page">
    <!-- 标签页切换 -->
    <div class="application-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        @click="setActiveTab(tab.key)"
        :class="['tab-button', { active: activeTab === tab.key }]"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-text">{{ tab.label }}</span>
      </button>
    </div>

    <!-- 申请列表容器 -->
    <div class="applications-container">
      <div class="applications-header">
        <h3>{{ tabs.find(t => t.key === activeTab)?.label }}</h3>
        
        <!-- 批量操作按钮（仅在收到的申请页面显示） -->
        <div v-if="activeTab === 'received'" class="batch-actions">
          <button @click="batchApprove" class="batch-btn batch-approve">
            批量通过
          </button>
          <button @click="batchReject" class="batch-btn batch-reject">
            批量拒绝
          </button>
        </div>
      </div>
      
      <div class="applications-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>正在加载申请信息...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="applications[activeTab].length === 0" class="empty-state">
          <div class="empty-icon">
            {{ activeTab === 'received' ? '📥' : '📤' }}
          </div>
          <p>
            {{ activeTab === 'received' ? '暂无收到的申请' : '暂无发送的申请' }}
          </p>
        </div>

        <!-- 申请列表 -->
        <div v-else class="applications-list">
          <!-- 收到的申请/邀请 -->
          <div
            v-if="activeTab === 'received'"
            v-for="application in applications.received"
            :key="application.id"
            class="application-card"
          >
            <div class="application-header">
              <div class="applicant-info">
                <!-- 如果是收到的申请（队长视角），显示申请人 -->
                <!-- 如果是收到的邀请（用户视角），显示团队名 -->
                <h4 v-if="application.type === 'apply'">{{ application.applicantName }}</h4>
                <h4 v-else>{{ application.teamName }}</h4>
              </div>
              <span :class="['application-status', getStatusClass(application.status)]">
                {{ getStatusText(application.status) }}
              </span>
            </div>

            <div v-if="application.message" class="application-message">
              <div class="message-label">{{ application.type === 'apply' ? '申请理由' : '邀请消息' }}</div>
              <p class="message-content">{{ application.message }}</p>
            </div>

            <div class="application-time">
              {{ application.type === 'apply' ? '申请时间' : '邀请时间' }}：{{ formatDateTime(application.applicationTime) }}
            </div>

            <div class="application-actions" v-if="application.status === 'PENDING'">
              <button
                @click="viewDetail(application)"
                class="action-btn btn-info"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M15 12C15 13.6569 13.6569 15 12 15C10.3431 15 9 13.6569 9 12C9 10.3431 10.3431 9 12 9C13.6569 9 15 10.3431 15 12Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M2 12C2 12 5 5 12 5C19 5 22 12 22 12C22 12 19 19 12 19C5 19 2 12 2 12Z" stroke="currentColor" stroke-width="2"/>
                </svg>
                查看{{ application.type === 'apply' ? '用户' : '团队' }}详情
              </button>
              <button @click="approveApplication(application.id)" class="action-btn btn-approve">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                {{ application.type === 'apply' ? '通过申请' : '接受邀请' }}
              </button>
              <button @click="rejectApplication(application.id)" class="action-btn btn-reject">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                {{ application.type === 'apply' ? '拒绝申请' : '拒绝邀请' }}
              </button>
            </div>
            <div v-else class="application-actions">
              <button class="action-btn btn-disabled" disabled>
                已处理
              </button>
            </div>
          </div>

          <!-- 发送的申请/邀请 -->
          <div
            v-if="activeTab === 'sent'"
            v-for="application in applications.sent"
            :key="application.id"
            class="application-card sent-application"
          >
            <div class="application-header">
              <div class="applicant-info">
                <!-- 如果是发送的申请（用户视角），显示团队名 -->
                <!-- 如果是发送的邀请（队长视角），显示被邀请人 -->
                <h4 v-if="application.type === 'apply'">{{ application.teamName }}</h4>
                <h4 v-else>{{ application.applicantName }}</h4>
              </div>
              <span :class="['application-status', getStatusClass(application.status)]">
                {{ getStatusText(application.status) }}
              </span>
            </div>

            <div v-if="application.message" class="application-message">
              <div class="message-label">{{ application.type === 'apply' ? '申请理由' : '邀请消息' }}</div>
              <p class="message-content">{{ application.message }}</p>
            </div>

            <div class="application-time">
              {{ application.type === 'apply' ? '申请时间' : '邀请时间' }}：{{ formatDateTime(application.applicationTime) }}
            </div>

            <div class="application-actions" v-if="application.status === 'PENDING'">
              <button
                @click="viewDetail(application)"
                class="action-btn btn-info"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M15 12C15 13.6569 13.6569 15 12 15C10.3431 15 9 13.6569 9 12C9 10.3431 10.3431 9 12 9C13.6569 9 15 10.3431 15 12Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M2 12C2 12 5 5 12 5C19 5 22 12 22 12C22 12 19 19 12 19C5 19 2 12 2 12Z" stroke="currentColor" stroke-width="2"/>
                </svg>
                查看{{ application.type === 'apply' ? '团队' : '用户' }}详情
              </button>
              <button @click="cancelSentApplication(application.id)" class="action-btn btn-cancel">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                {{ application.type === 'apply' ? '撤回申请' : '撤回邀请' }}
              </button>
            </div>
            <div v-else class="application-actions">
              <button class="action-btn btn-disabled" disabled>
                {{ application.status === 'APPROVED' ? '已通过' : '已拒绝' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 用户详情弹窗 -->
  <MemberDetail
    v-model:showModal="showMemberModal"
    :member="selectedMember"
    :teamId="selectedTeamId"
    :currentUserId="currentUser?.id"
  />

  <!-- 团队详情弹窗 -->
  <TeamInfo
    v-model:showModal="showTeamModal"
    :teamId="selectedTeamId"
    :currentUserId="currentUser?.id"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useApplication } from '../../composables/useApplication.js'
import MemberDetail from './MemberDetail.vue'
import TeamInfo from '../team/TeamInfo.vue'

const showMemberModal = ref(false)
const showTeamModal = ref(false)
const selectedMember = ref(null)
const selectedTeamId = ref(null)

const {
  // 响应式数据
  loading,
  currentUser,
  activeTab,
  applications,
  tabs,

  // 方法
  loadUserInfo,
  loadApplications,
  setActiveTab,
  approveApplication,
  rejectApplication,
  cancelSentApplication,
  getStatusText,
  getStatusClass,
  formatDateTime,
  batchApprove,
  batchReject
} = useApplication()

// 查看详情方法
const viewDetail = (application) => {
  if (application.type === 'apply') {
    // 收到的申请 -> 查看用户详情
    // 发送的申请 -> 查看团队详情
    if (application.userId === currentUser.value.id) {
      // 发送的申请，查看团队
      selectedTeamId.value = application.teamId
      showTeamModal.value = true
    } else {
      // 收到的申请，查看用户
      selectedMember.value = {
        userId: application.userId,
        username: application.applicantName,
        realName: application.applicantName,
        major: application.applicantMajor
      }
      selectedTeamId.value = application.teamId
      showMemberModal.value = true
    }
  } else {
    // invite类型
    if (application.userId === currentUser.value.id) {
      // 收到的邀请，查看团队
      selectedTeamId.value = application.teamId
      showTeamModal.value = true
    } else {
      // 发送的邀请，查看用户
      selectedMember.value = {
        userId: application.userId,
        username: application.applicantName,
        realName: application.applicantName,
        major: application.applicantMajor
      }
      selectedTeamId.value = application.teamId
      showMemberModal.value = true
    }
  }
}

onMounted(async () => {
  loadUserInfo()
  await loadApplications()
})
</script>

<style scoped>
@import '../../styles/application.css';
</style>