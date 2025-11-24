<template>
  <div class="team-list-page">
    <div class="page-container">
      <header class="detail-header">
        <button class="back-btn" @click="goBack">
          ← 返回
        </button>
        <div class="header-info">
          <h1 class="competition-title">{{ currentFilter === 'team' ? '参赛团队列表' : '参赛个人列表' }}</h1>
          <div class="competition-meta" v-if="competition">
            <span class="category-badge">{{ competition.title }}</span>
            <span class="track-badge" v-if="competition.track">{{ competition.track }}</span>
          </div>
        </div>
        <div class="header-actions">
          <button class="btn-primary" @click="openCreateTeamModal" v-if="currentFilter === 'team'">
            创建团队
          </button>
        </div>
      </header>

      <main class="detail-content">
        <div class="content-grid">
          <div class="main-content">
            <section class="info-section">
              <div class="filter-section">
                <h2>筛选条件</h2>
                <div class="filter-container">
                  <div class="filter-group" v-if="availableFilterOptions.length > 1">
                    <label>参赛类型：</label>
                    <select v-model="currentFilter" @change="onFilterChange" class="filter-select">
                      <option 
                        v-for="option in availableFilterOptions" 
                        :key="option.value" 
                        :value="option.value"
                      >
                        {{ option.label }}
                      </option>
                    </select>
                  </div>
                  <div class="filter-group" v-else-if="availableFilterOptions.length === 1">
                    <label>参赛类型：</label>
                    <span class="filter-text">{{ availableFilterOptions[0].label }}</span>
                  </div>
                  <div class="search-box">
                    <input 
                      v-if="currentFilter === 'team'"
                      type="text" 
                      v-model="searchKeyword" 
                      placeholder="搜索团队名称..." 
                      @input="performSearch"
                      class="search-input"
                    />
                    <input 
                      v-else
                      type="text" 
                      v-model="individualSearchKeyword" 
                      placeholder="搜索个人姓名..." 
                      @input="performSearch"
                      class="search-input"
                    />
                    <button class="search-btn" @click="performSearch">搜索</button>
                  </div>
                </div>
              </div>
            </section>

            <section class="teams-section">
              <h2>{{ currentFilter === 'team' ? '团队列表' : '个人列表' }}</h2>
              
              <!-- 团队列表 -->
              <div v-if="currentFilter === 'team' && !loading && teams.length > 0" class="teams-grid">
                <div v-for="team in teams" :key="team.id" class="team-card">
                  <div class="team-header">
                    <h3 class="team-name">{{ team.name }}</h3>
                    <div class="team-header-right">
                      <span class="team-members-count">{{ team.teamMemberCount || getTeamMemberCount(team.id) }}人</span>
                      <!-- 申请状态显示 -->
                      <span 
                        v-if="currentUser && getApplicationStatusText(team.id)" 
                        class="application-status"
                        :class="getApplicationStatusClass(team.id)"
                      >
                        {{ getApplicationStatusText(team.id) }}
                      </span>
                    </div>
                  </div>
                  <div class="team-leader" v-if="team.leaderDisplayName || team.realName || team.username">
                    <span class="leader-label">队长：</span>
                    <span class="leader-name">{{ team.leaderDisplayName || team.realName || team.username }}</span>
                  </div>
                  <div class="team-description" v-if="team.description">
                    <p>{{ team.description }}</p>
                  </div>
                  <div class="team-description" v-else>
                    <p class="no-description">暂无团队描述</p>
                  </div>
                  
                  <!-- 团队所需技能 -->
                  <div class="team-skills-needed" v-if="team.needSkills && team.needSkills.trim()">
                    <h4>招募技能:</h4>
                    <div class="skills-tags">
                      <span 
                        v-for="skill in team.needSkills.split(',').map(s => s.trim()).filter(s => s)" 
                        :key="skill" 
                        class="skill-tag needed-skill"
                      >
                        {{ skill }}
                      </span>
                    </div>
                  </div>
                  
                  <!-- 团队现有技能 -->
                  <div class="team-skills-current" v-if="team.teamSkills && team.teamSkills.trim()">
                    <h4>团队技能:</h4>
                    <div class="skills-tags">
                      <span 
                        v-for="skill in team.teamSkills.split(',').map(s => s.trim()).filter(s => s)" 
                        :key="skill" 
                        class="skill-tag current-skill"
                      >
                        {{ skill }}
                      </span>
                    </div>
                  </div>
                  
                  <!-- 用户与团队的适配度 -->
                  <div class="user-match" v-if="currentUser && teamUserMatch[team.id] !== undefined">
                    <div class="match-score">
                      <span class="match-label">适配度:</span>
                      <div class="match-progress">
                        <div 
                          class="match-bar" 
                          :style="{width: `${teamUserMatch[team.id]}%`}"
                          :class="getMatchClass(teamUserMatch[team.id])"
                        ></div>
                      </div>
                      <span class="match-percent">{{ teamUserMatch[team.id] }}%</span>
                    </div>
                  </div>
                  
                  <!-- 用户状态显示 -->
                  <div class="user-status" v-if="currentUser">
                    <div class="status-info">
                      <span v-if="isTeamLeader(team)" class="status-badge leader-badge">
                        👑 队长
                      </span>
                      <span v-else-if="isTeamMember(team.id)" class="status-badge member-badge">
                        ✅ 已加入
                      </span>
                      <span v-else class="status-badge guest-badge">
                        👤 访客
                      </span>
                    </div>
                  </div>

                  <div class="team-footer">
                    <button class="action-btn" @click="viewTeamDetailModal(team.id)">
                      查看详情
                    </button>
                    <!-- 申请加入按钮 -->
                    <button 
                      class="action-btn primary" 
                      @click="requestJoin(team.id)" 
                      v-if="currentUser && !isTeamMember(team.id) && !isTeamLeader(team) && !hasUserApplied(team.id)"
                    >
                      申请加入
                    </button>
                    <!-- 取消申请按钮 -->
                    <button 
                      class="action-btn warning" 
                      @click="cancelApplication(team.id)" 
                      v-else-if="currentUser && hasUserApplied(team.id)"
                    >
                      取消申请
                    </button>
                    <!-- 管理团队按钮 -->
                    <button 
                      class="action-btn secondary" 
                      @click="manageTeam(team.id)" 
                      v-else-if="isTeamLeader(team)"
                    >
                      管理团队
                    </button>
                    <!-- 退出团队按钮 -->
                    <button 
                      class="action-btn danger" 
                      @click="leaveTeam(team.id)" 
                      v-else-if="isTeamMember(team.id) && !isTeamLeader(team)"
                    >
                      退出团队
                    </button>
                  </div>
                </div>
              </div>

              <!-- 个人列表 -->
              <div v-if="currentFilter === 'individual' && !individualLoading && individuals.length > 0" class="individuals-grid">
                <div v-for="individual in individuals" :key="individual.id" class="individual-card">
                  <div class="individual-header">
                    <div class="individual-avatar">
                      <img 
                        :src="getUserAvatar(individual)" 
                        :alt="getUserDisplayName(individual)"
                        class="avatar-img"
                      />
                    </div>
                    <div class="individual-info">
                      <h3 class="individual-name">{{ getUserDisplayName(individual) }}</h3>
                      <p class="individual-username" v-if="individual.realName">@{{ individual.username }}</p>
                      <p class="individual-major" v-if="individual.major">{{ individual.major }}</p>
                    </div>
                    <div class="individual-status">
                      <span class="status-badge individual-badge">
                        👤 个人参赛
                      </span>
                    </div>
                  </div>
                  
                  <!-- 个人技能展示 -->
                  <div class="individual-skills" v-if="individual.skills && individual.skills.trim()">
                    <h4>技能标签:</h4>
                    <div class="skills-tags">
                      <span 
                        v-for="skill in formatSkills(individual.skills)" 
                        :key="skill" 
                        class="skill-tag individual-skill"
                      >
                        {{ skill }}
                      </span>
                    </div>
                  </div>
                  
                  <!-- 个人简介 -->
                  <div class="individual-bio" v-if="individual.bio">
                    <p>{{ individual.bio }}</p>
                  </div>
                  <div class="individual-bio" v-else>
                    <p class="no-bio">暂无个人简介</p>
                  </div>

                  <!-- 个人卡片不支持查看详情，只显示基本信息 -->
                  <div class="individual-footer">
                    <span class="individual-note">个人参赛者</span>
                  </div>
                </div>
              </div>

              <!-- 空状态 -->
              <div class="empty-state" v-else-if="(!loading && currentFilter === 'team' && teams.length === 0) || (!individualLoading && currentFilter === 'individual' && individuals.length === 0)">
                <p v-if="currentFilter === 'team'">暂无参赛团队</p>
                <p v-else>暂无参赛个人</p>
                <button class="btn-primary" @click="openCreateTeamModal" v-if="currentFilter === 'team'">创建团队</button>
              </div>

              <div class="loading" v-if="loading || individualLoading">
                <p>加载中...</p>
              </div>
            </section>
          </div>

          <aside class="sidebar">
            <div class="sidebar-card" v-if="competition">
              <h3>竞赛信息</h3>
              <div class="info-grid">
                <div class="info-item">
                  <label>主办方</label>
                  <span>{{ competition.organizer || '未知' }}</span>
                </div>
                <div class="info-item">
                  <label>竞赛类别</label>
                  <span>{{ competition.category || '未知' }}</span>
                </div>
                <div class="info-item">
                  <label>竞赛赛道</label>
                  <span>{{ competition.track || '未知' }}</span>
                </div>
                <div class="info-item">
                  <label>参赛方式</label>
                  <span>{{ formatParticipationType(competition.participationMode) }}</span>
                </div>
              </div>
            </div>

            <div class="sidebar-card">
              <h3>快速操作</h3>
              <div class="quick-actions">
                <button class="action-btn" @click="openCreateTeamModal" v-if="currentFilter === 'team'">
                  创建团队
                </button>
                <button class="action-btn" @click="viewCompetitionDetail">
                  查看竞赛详情
                </button>
                <button class="action-btn" @click="refreshData">
                  刷新列表
                </button>
              </div>
            </div>
          </aside>
        </div>
      </main>
    </div>
    
    <!-- 创建团队弹窗 -->
    <TeamCreateModal 
      v-model:showModal="showCreateTeamModal"
      :currentUser="currentUser"
      :competitionId="competitionId"
      @success="refreshTeams"
    />

    <!-- 团队详情弹窗 -->
    <TeamInfo 
      v-model:showModal="showTeamInfoModal"
      :teamId="selectedTeamId"
      :currentUserId="currentUser?.id"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useTeamList } from '../composables/useTeamList.js'
import { useUserList } from '../composables/useUserList.js'
import TeamCreateModal from '../components/team/TeamCreateModal.vue'
import TeamInfo from '../components/team/TeamInfo.vue'
import { useTeamCreate } from '../composables/useTeamCreate.js'

// 定义 props
const props = defineProps({
  competitionId: {
    type: [String, Number],
    default: null
  }
})

// 使用团队列表组合式函数
const {
  teams,
  competition,
  loading,
  searchKeyword,
  teamMemberCounts,
  teamNeededSkills,
  teamUserMatch,
  currentUser,
  competitionId,
  getTeamMemberCount,
  isTeamMember,
  isTeamLeader,
  viewTeamDetail,
  requestJoin,
  cancelApplication,
  manageTeam,
  leaveTeam,
  viewCompetitionDetail,
  refreshTeams,
  searchTeams,
  formatParticipationType,
  goBack,
  getMatchClass,
  getUserApplicationStatus,
  hasUserApplied,
  getApplicationStatusText,
  getApplicationStatusClass
} = useTeamList(props)

// 使用个人列表组合式函数
const {
  individuals,
  loading: individualLoading,
  searchKeyword: individualSearchKeyword,
  fetchIndividuals,
  searchIndividuals,
  refreshIndividuals,
  formatSkills,
  getUserDisplayName,
  getUserAvatar,
  statistics
} = useUserList(props)

// 使用团队创建组合式函数
const {
  showCreateTeamModal,
  openCreateTeamModal
} = useTeamCreate(currentUser, competitionId, refreshTeams)

// 团队详情弹窗状态
const showTeamInfoModal = ref(false)
const selectedTeamId = ref(null)

// 筛选相关状态
const currentFilter = ref('team') // 'team' 或 'individual'

// 计算可用的参赛类型选项
const availableFilterOptions = computed(() => {
  if (!competition.value || !competition.value.participationMode) {
    return [{ value: 'team', label: '团队' }] // 默认只显示团队
  }
  
  const mode = competition.value.participationMode.toLowerCase()
  
  if (mode === 'team') {
    return [{ value: 'team', label: '团队' }]
  } else if (mode === 'individual') {
    return [{ value: 'individual', label: '个人' }]
  } else if (mode === 'both') {
    return [
      { value: 'team', label: '团队' },
      { value: 'individual', label: '个人' }
    ]
  } else {
    // 兼容其他可能的值
    return [
      { value: 'team', label: '团队' },
      { value: 'individual', label: '个人' }
    ]
  }
})

// 检查当前筛选是否可用
const isCurrentFilterAvailable = computed(() => {
  return availableFilterOptions.value.some(option => option.value === currentFilter.value)
})

// 监听竞赛信息变化，自动调整筛选选项
const adjustFilterBasedOnCompetition = () => {
  if (competition.value && competition.value.participationMode) {
    const mode = competition.value.participationMode.toLowerCase()
    
    // 如果当前筛选不可用，自动切换到第一个可用选项
    if (!isCurrentFilterAvailable.value) {
      const firstOption = availableFilterOptions.value[0]
      if (firstOption) {
        currentFilter.value = firstOption.value
        
        // 根据新的筛选类型加载数据
        if (currentFilter.value === 'individual') {
          fetchIndividuals()
        }
      }
    }
  }
}

// 筛选变更处理
const onFilterChange = () => {
  if (currentFilter.value === 'team') {
    searchKeyword.value = ''
  } else {
    individualSearchKeyword.value = ''
    fetchIndividuals()
  }
}

// 搜索处理
const performSearch = () => {
  if (currentFilter.value === 'team') {
    searchTeams()
  } else {
    searchIndividuals()
  }
}

// 刷新数据
const refreshData = () => {
  if (currentFilter.value === 'team') {
    refreshTeams()
  } else {
    refreshIndividuals()
  }
}

// 查看团队详情方法
const viewTeamDetailModal = (teamId) => {
  selectedTeamId.value = teamId
  showTeamInfoModal.value = true
}

// 组件挂载时初始化
onMounted(() => {
  // 等待竞赛信息加载完成后调整筛选选项
  const checkCompetitionLoaded = () => {
    if (competition.value) {
      adjustFilterBasedOnCompetition()
      
      // 根据当前筛选类型加载相应数据
      if (currentFilter.value === 'individual') {
        fetchIndividuals()
      }
    } else {
      // 如果竞赛信息还未加载，延迟检查
      setTimeout(checkCompetitionLoaded, 100)
    }
  }
  
  checkCompetitionLoaded()
})
</script>

<style scoped>
@import '../styles/views-competition.css';
@import '../styles/views-team.css';
@import '../styles/team-modal.css';
@import '../styles/teamember.css';
@import '../styles/individual-cards.css';

.filter-text {
  display: inline-block;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  color: #495057;
  font-weight: 500;
}
</style>
