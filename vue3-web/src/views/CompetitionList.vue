<template>
  <div class="competition-list-page">
    <!-- 通用导航条 -->
    <Header />
    
    <div class="page-content">
      <PageHeader 
        title="竞赛列表" 
        subtitle="发现适合您的竞赛机会，开启创新之旅"
      >
        <template #actions>
          <div class="header-controls">
            <AISearchBox 
              @search="handleAISearch" 
              @aiRecommend="handleAIRecommend"
              placeholder="🤖 智能搜索竞赛（支持自然语言）"
              class="search-input"
            />
            
            <div class="filter-group">
              <select 
                v-model="selectedCategory" 
                @change="handleCategoryChange" 
                class="category-select"
              >
                <option value="">全部类别</option>
                <option value="编程竞赛">编程竞赛</option>
                <option value="创业竞赛">创业竞赛</option>
                <option value="设计竞赛">设计竞赛</option>
                <option value="数学竞赛">数学竞赛</option>
              </select>
              
            </div>
          </div>
        </template>
      </PageHeader>

      <main>
        <LoadingState 
          v-if="loading" 
          message="正在加载竞赛信息..." 
          size="large"
        />
        
        <EmptyState 
          v-else-if="filteredCompetitions.length === 0 && !loading"
          icon="🏆"
          title="暂无竞赛信息"
          description="当前没有符合条件的竞赛，请尝试调整筛选条件或稍后再试"
          action-text="刷新列表"
          @action="refreshCompetitions"
        />
        
        <div v-else class="competition-grid">
          <CompetitionCard
            v-for="competition in filteredCompetitions"
            :key="competition.id"
            :competition="competition"
            :isJoined="hasUserJoinedCompetition(competition.id)"
            @view="viewCompetition"
            @join="joinCompetition"
            @cancel="cancelCompetition"
          />
        </div>
      </main>
    </div>

    <!-- 参赛弹窗 - 使用 Teleport 传送到 body -->
    <Teleport to="body">
      <Participate
        :visible="participateModalVisible"
        :competition="selectedCompetition"
        @close="participateModalVisible = false"
        @success="handleParticipateSuccess"
      />
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { debounce } from '../utils/performance.js'
import { useCompetitionList } from '../composables/useCompetition.js'

// 组件导入
import Header from '../components/common/Header.vue'
import PageHeader from '../components/layout/PageHeader.vue'
import LoadingState from '../components/common/LoadingState.vue'
import EmptyState from '../components/common/EmptyState.vue'
import CompetitionCard from '../components/competition/CompetitionCard.vue'
import AISearchBox from '../components/ai/AISearchBox.vue'
import Participate from '../components/competition/Participate.vue'

const router = useRouter()

// 使用竞赛数据管理
const {
  loading,
  error,
  competitions,
  userParticipations,
  currentUser,
  selectedCategory,
  searchKeyword,
  filteredCompetitions,
  fetchCompetitions,
  fetchUserParticipations,
  searchCompetitions,
  viewCompetition: viewCompetitionFromComposable,
  handleJoinCompetition,
  handleCancelParticipation,
  hasUserJoinedCompetition
} = useCompetitionList()

// 本地状态
const participateModalVisible = ref(false)
const selectedCompetition = ref(null)

onMounted(async () => {
  // 加载竞赛数据
  await fetchCompetitions()

  // 如果用户已登录，加载参赛记录
  if (currentUser.value) {
    await fetchUserParticipations()
  }
})

// 处理参赛成功
const handleParticipateSuccess = async (result) => {
  participateModalVisible.value = false
  console.log('参赛成功:', result)
  // 刷新竞赛列表和用户参赛记录
  await fetchCompetitions()
  if (currentUser.value) {
    await fetchUserParticipations()
  }
}

// 取消参赛
const cancelCompetition = async (competitionId) => {
  const success = await handleCancelParticipation(competitionId)
  if (success) {
    // 刷新竞赛列表
    await fetchCompetitions()
  }
}

// 防抖搜索
const debouncedSearch = debounce(async (keyword) => {
  if (keyword.trim()) {
    await searchCompetitions(keyword)
  } else {
    await fetchCompetitions()
  }
}, 300)

// 监听搜索关键词变化
watch(searchKeyword, (newKeyword) => {
  debouncedSearch(newKeyword)
})

// 处理类别变化
const handleCategoryChange = async () => {
  // 重新获取竞赛数据，filteredCompetitions会自动根据selectedCategory过滤
  await fetchCompetitions()
}

// 查看竞赛详情
const viewCompetition = (id) => {
  viewCompetitionFromComposable(id)
}

// 参加竞赛
const joinCompetition = (competition) => {
  selectedCompetition.value = competition
  participateModalVisible.value = true
}

// 处理AI搜索
const handleAISearch = (query) => {
  console.log('AI搜索:', query)
  searchKeyword.value = query
}

// 处理AI推荐
const handleAIRecommend = (result) => {
  console.log('AI推荐结果:', result)
  if (result.competitions) {
    // 这里可以直接设置竞赛数据或者触发特殊的显示逻辑
    // competitions.value = result.competitions
  }
}

// 跳转到AI中心
const goToAICenter = () => {
  router.push('/ai-center')
}

// 刷新竞赛列表
const refreshCompetitions = async () => {
  selectedCategory.value = ''
  searchKeyword.value = ''
  await fetchCompetitions()
}
</script>

<style scoped>
@import '../styles/views-competition.css';

.header-controls {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  width: 100%;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 1rem;
}

@media (max-width: 1024px) {
  .header-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .filter-group {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .filter-group {
    grid-template-columns: 1fr;
  }
}
</style>
