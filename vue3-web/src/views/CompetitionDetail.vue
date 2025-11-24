<template>
  <div class="competition-detail-page">
    <div class="page-container" v-if="competition">
      <header class="detail-header">
        <button class="back-btn" @click="goBack">
          <span>←</span> 返回列表
        </button>
        <div class="header-info">
          <h1 class="competition-title">{{ competition.title }}</h1>
          <div class="competition-meta">
            <span class="category-badge">{{ competition.category }}</span>
            <span class="track-badge">{{ competition.track }}</span>
            <div class="participation-type-badge" :class="getParticipationTypeClass()">
              {{ formatParticipationType(competition.participationMode) }}
            </div>
          </div>
        </div>
        <div class="header-actions">
          <button
            class="btn-primary"
            @click="openParticipateModal"
            v-if="!hasJoined"
            :disabled="getRegistrationStatus !== 1"
            :title="getRegistrationStatus !== 1 ? getRegistrationStatusText() : ''"
          >
            立即报名
          </button>
          <button
            class="btn-danger"
            @click="handleCancelParticipation"
            v-else
          >
            取消参赛
          </button>
        </div>
      </header>

      <main class="detail-content">
        <div class="content-grid">
          <div class="main-content">
            <section class="info-section">
              <h2>基本信息</h2>
              <div class="info-grid">
                <div class="info-item">
                  <label>主办方</label>
                  <span>{{ competition.organizer }}</span>
                </div>
                <div class="info-item">
                  <label>竞赛类别</label>
                  <span>{{ competition.category }}</span>
                </div>
                <div class="info-item">
                  <label>竞赛赛道</label>
                  <span>{{ competition.track }}</span>
                </div>
                <div class="info-item">
                  <label>参赛方式</label>
                  <span>{{ formatParticipationType(competition.participationMode) }}</span>
                </div>
                <div class="info-item">
                  <label>官方网站</label>
                  <a :href="competition.officialUrl" target="_blank" v-if="competition.officialUrl">
                    访问官网 ↗
                  </a>
                  <span v-else>暂无</span>
                </div>
              </div>
            </section>

            <section class="time-section">
              <h2>时间安排</h2>
              <div class="time-grid">
                <div class="time-item">
                  <label>比赛时间</label>
                  <span>{{ formatDate(competition.startTime) }} - {{ formatDate(competition.endTime) }}</span>
                </div>
                <div class="time-item">
                  <label>报名时间</label>
                  <span>{{ formatDate(competition.patiStarttime) }} - {{ formatDate(competition.patiEndtime) }}</span>
                </div>
                <div class="time-item">
                  <label>竞赛状态</label>
                  <span :class="getStatusClass()">{{ getCompetitionStatus() }}</span>
                </div>
                <div class="time-item">
                  <label>报名状态</label>
                  <span :class="getRegistrationStatusClass()">{{ getRegistrationStatusText() }}</span>
                </div>
              </div>
            </section>

            <section class="description-section">
              <h2>竞赛描述</h2>
              <div class="description-content">
                <p>{{ competition.description || '暂无详细描述' }}</p>
              </div>
            </section>

            <section class="rules-section" v-if="competitionRules">
              <h2>竞赛规则</h2>
              <div class="rules-content">
                <div v-for="(value, key) in competitionRules" :key="key" class="rule-item">
                  <strong>{{ formatRuleKey(key) }}</strong>
                  <span v-if="Array.isArray(value)">{{ value.join(', ') }}</span>
                  <span v-else>{{ value }}</span>
                </div>
              </div>
            </section>

            <section class="tags-section" v-if="competition.tags">
              <h2>相关标签</h2>
              <div class="tags-container">
                <span 
                  v-for="tag in competition.tags.split(',')" 
                  :key="tag" 
                  class="tag"
                >
                  # {{ tag.trim() }}
                </span>
              </div>
            </section>
          </div>

          <aside class="sidebar">
            <div class="sidebar-card">
              <h3>参赛团队</h3>
              <div class="teams-list" v-if="teams.length > 0">
                <div v-for="team in teams" :key="team.id" class="team-item">
                  <span class="team-name">{{ team.name }}</span>
                  <span class="team-members">{{ getTeamMemberCount(team.id) }}人</span>
                </div>
              </div>
              <p v-else class="no-teams">暂无参赛团队</p>
            </div>

            <div class="sidebar-card">
              <h3>快速操作</h3>
              <div class="quick-actions">
                <button class="action-btn" @click="openCreateTeamModal">
                  <span>创建团队</span>
                </button>
                <button class="action-btn" @click="viewTeams">
                  <span>查看参赛情况</span>
                </button>
                <button class="action-btn" @click="viewProjects">
                  <span>查看提交项目</span>
                </button>
              </div>
            </div>
          </aside>
        </div>
      </main>
    </div>

    <div v-else-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>正在加载竞赛信息...</p>
    </div>

    <div v-else class="error">
      <p>竞赛信息加载失败</p>
      <button @click="fetchCompetition">重试</button>
    </div>

  </div>

  <!-- 创建团队弹窗 - 使用 Teleport 传送到 body -->
  <Teleport to="body">
    <TeamCreateModal
      v-model:showModal="showCreateTeamModal"
      :currentUser="currentUser"
      :competitionId="competition?.id"
      @success="refreshTeams"
    />
  </Teleport>

  <!-- 参赛弹窗 - 使用 Teleport 传送到 body -->
  <Teleport to="body">
    <Participate
      :visible="participateModalVisible"
      :competition="competition"
      @close="participateModalVisible = false"
      @success="handleParticipateSuccess"
    />
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import { useCompetitionDetailExtended } from '../composables/CompetitionDetail.js'
import TeamCreateModal from '../components/team/TeamCreateModal.vue'
import Participate from '../components/competition/Participate.vue'
import { useTeamCreate } from '../composables/useTeamCreate.js'

// 参赛弹窗状态
const participateModalVisible = ref(false)

// 刷新团队列表函数
const refreshTeams = async () => {
  await fetchCompetition()
}

// 使用扩展的竞赛详情组合式函数
const {
  competition,
  teams,
  loading,
  hasJoined,
  competitionRules,
  goBack,
  cancelParticipation,
  viewTeams,
  viewProjects,
  formatDate,
  getCompetitionStatus,
  getStatusClass,
  formatRuleKey,
  getTeamMemberCount,
  fetchCompetition,
  formatParticipationType,
  getRegistrationStatus,
  getRegistrationStatusText,
  getRegistrationStatusClass,
  getParticipationTypeClass,
  joinCompetition,
  currentUser // 从组合式函数中获取当前用户
} = useCompetitionDetailExtended()

// 显示参赛弹窗
const openParticipateModal = () => {
  if (getRegistrationStatus.value === 1) {
    participateModalVisible.value = true
  }
}

// 处理参赛成功
const handleParticipateSuccess = async (result) => {
  participateModalVisible.value = false
  console.log('参赛成功:', result)

  // 刷新竞赛信息
  await fetchCompetition()

  // 如果是团队参赛，提示上传项目计划书
  if (result && result.participationMode === 'team') {
    const goToUpload = confirm(
      '你的团队参赛成功！是否现在去上传项目计划书？\n\n提示：您可以稍后在"我的竞赛"页面中上传。'
    )

    if (goToUpload) {
      // 跳转到竞赛管理页面
      window.location.href = '/profile?tab=competitions'
    }
  }
}

// 处理取消参赛
const handleCancelParticipation = async () => {
  await cancelParticipation()
  // 刷新竞赛信息
  await fetchCompetition()
}

// 使用团队创建组合式函数
const {
  showCreateTeamModal,
  openCreateTeamModal
} = useTeamCreate(currentUser, competition?.id, refreshTeams)
</script>

<style scoped>
@import '../styles/views-competition.css';
@import '../styles/team-modal.css';

/* 参赛类型徽章 */
.participation-type-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.type-individual {
  background: rgba(14, 165, 233, 0.15);
  color: #38bdf8;
  border: 1px solid rgba(14, 165, 233, 0.3);
}

.type-team {
  background: rgba(236, 72, 153, 0.15);
  color: #f472b6;
  border: 1px solid rgba(236, 72, 153, 0.3);
}

.type-both {
  background: rgba(168, 85, 247, 0.15);
  color: #c084fc;
  border: 1px solid rgba(168, 85, 247, 0.3);
}

/* 报名状态样式 - 已移至 views-competition.css */

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>