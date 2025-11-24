<template>
  <div class="competition-control">
    <!-- 竞赛列表容器 -->
    <div class="competitions-container">
      <div class="competitions-header">
        <h3>已报名的竞赛</h3>
        <!-- 参赛方式筛选器 -->
        <div class="filter-section">
          <label class="filter-label">参赛方式：</label>
          <select v-model="participationFilter" @change="filterCompetitions" class="filter-select">
            <option value="all">全部</option>
            <option value="individual">个人参赛</option>
            <option value="team">团队参赛</option>
          </select>
        </div>
      </div>
      
      <div class="competitions-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <p>正在加载竞赛信息...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="competitions.length === 0" class="empty-state">
          <div class="empty-icon">🏆</div>
          <p>您还没有报名任何竞赛</p>
        </div>

        <!-- 竞赛列表 -->
        <div v-else class="competitions-grid">
          <div 
            v-for="competition in competitions" 
            :key="competition.id" 
            class="competition-card"
          >
            <div class="competition-header">
              <div class="competition-info">
                <h4>{{ competition.title }}</h4>
                <div class="competition-meta">
                  <span class="competition-category">{{ competition.category }}</span>
                  <span :class="['participation-badge', getParticipationClass(competition.participationMode)]">
                    {{ formatParticipationMode(competition.participationMode) }}
                  </span>
                  <span v-if="competition.teamName" class="team-badge">
                    {{ competition.teamName }}
                  </span>
                </div>
              </div>
              <span :class="['competition-status', getStatusClass(competition.status)]">
                {{ competition.status }}
              </span>
            </div>
            
            <div class="competition-description">
              {{ competition.description || '暂无描述' }}
            </div>

            <div class="competition-dates">
              <div class="date-item">
                <span class="date-label">报名时间</span>
                <span class="date-value">
                  {{ formatDate(competition.registrationStartTime) }} - 
                  {{ formatDate(competition.registrationEndTime) }}
                </span>
              </div>
              <div class="date-item">
                <span class="date-label">竞赛时间</span>
                <span class="date-value">
                  {{ formatDate(competition.startTime) }} - 
                  {{ formatDate(competition.endTime) }}
                </span>
              </div>
            </div>

            <div class="registration-info">
              <div class="info-label">报名时间</div>
              <div class="info-value">{{ formatDateTime(competition.registrationDate) }}</div>
            </div>

            <div class="project-section">
              <h5>项目计划书</h5>
              <div class="project-actions">
                <button @click="openUploadModal(competition)" class="action-btn btn-success">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M7 10L12 15L17 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  上传计划书
                </button>
                <button @click="viewProjectPlan(competition)" class="action-btn btn-secondary">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 12S5 4 12 4S23 12 23 12S19 20 12 20S1 12 1 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  查看计划书
                </button>
                <button @click="downloadProjectPlan(competition)" class="action-btn btn-secondary">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M7 10L12 15L17 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  下载计划书
                </button>
              </div>
            </div>

            <div class="competition-actions">
              <button @click="viewCompetitionDetail(competition.id)" class="action-btn btn-primary">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M1 12S5 4 12 4S23 12 23 12S19 20 12 20S1 12 1 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                查看详情
              </button>
              <button 
                v-if="competition.canCancel" 
                @click="openCancelModal(competition)" 
                class="action-btn btn-danger"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                取消报名
              </button>
              <button 
                v-else 
                class="action-btn btn-disabled"
                disabled
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 22C6.477 22 2 17.523 2 12S6.477 2 12 2S22 6.477 22 12S17.523 22 12 22Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M8 12L16 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                无法取消
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 取消报名确认模态框 -->
    <div v-if="showCancelModal" class="modal-overlay" @click="closeCancelModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">确认取消报名</h3>
          <button @click="closeCancelModal" class="close-btn">×</button>
        </div>

        <div class="modal-body">
          <p>您确定要取消报名 <span class="competition-name">{{ selectedCompetition?.title }}</span> 吗？</p>
          <p>取消后将无法恢复，如需重新参加需要重新报名。</p>
        </div>

        <div class="modal-actions">
          <button @click="cancelRegistration" class="btn-confirm">确认取消</button>
          <button @click="closeCancelModal" class="btn-cancel">保留报名</button>
        </div>
      </div>
    </div>

    <!-- 上传项目计划书模态框 -->
    <div v-if="showUploadModal" class="modal-overlay" @click="closeUploadModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">上传项目计划书</h3>
          <button @click="closeUploadModal" class="close-btn">×</button>
        </div>

        <div class="modal-body">
          <p class="modal-description">
            竞赛：<span class="competition-name">{{ uploadingCompetition?.title }}</span>
          </p>
          <p class="modal-tip">支持上传 PDF、DOC 或 DOCX 格式的文件，文件大小不超过10MB</p>

          <div class="file-upload-section">
            <input
              type="file"
              @change="handleFileSelect"
              accept=".pdf,.doc,.docx"
              ref="fileInput"
              class="file-input"
            />
            <div v-if="uploadFile" class="file-info">
              <span class="file-name">{{ uploadFile.name }}</span>
              <span class="file-size">({{ (uploadFile.size / 1024 / 1024).toFixed(2) }}MB)</span>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button
            @click="uploadProjectPlan"
            class="btn-confirm"
            :disabled="!uploadFile || uploading"
          >
            {{ uploading ? '上传中...' : '确认上传' }}
          </button>
          <button @click="closeUploadModal" class="btn-cancel" :disabled="uploading">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useCompetitionControl } from '../../composables/useCompetitionControl.js'

const {
  // 响应式数据
  loading,
  currentUser,
  competitions,
  participationFilter,
  showCancelModal,
  selectedCompetition,
  showUploadModal,
  uploadingCompetition,
  uploadFile,
  uploading,

  // 方法
  loadUserInfo,
  loadUserCompetitions,
  filterCompetitions,
  getCompetitionStatus,
  canCancelRegistration,
  getStatusClass,
  getParticipationClass,
  formatParticipationMode,
  openCancelModal,
  closeCancelModal,
  cancelRegistration,
  viewCompetitionDetail,
  downloadProjectPlan,
  openUploadModal,
  closeUploadModal,
  handleFileSelect,
  uploadProjectPlan,
  viewProjectPlan,
  formatDate,
  formatDateTime
} = useCompetitionControl()

onMounted(async () => {
  loadUserInfo()
  await loadUserCompetitions()
})
</script>

<style scoped>
@import '../../styles/competition-control.css';
</style>