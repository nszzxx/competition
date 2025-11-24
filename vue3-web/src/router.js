import { createRouter, createWebHistory } from 'vue-router'
import Home from './views/Home.vue'
import CompetitionList from './views/CompetitionList.vue'
import CompetitionDetail from './views/CompetitionDetail.vue'
import TeamCreate from './views/TeamCreate.vue'
import TeamList from './views/TeamList.vue'
import AICenter from './views/AICenter.vue'
import Profile from './views/Profile.vue'
import TestProfile from './views/TestProfile.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/competitions', name: 'CompetitionList', component: CompetitionList },
  { path: '/competition/:id', name: 'CompetitionDetail', component: CompetitionDetail },
  { path: '/team/create', name: 'TeamCreate', component: TeamCreate },
  { path: '/teams', name: 'TeamList', component: TeamList, props: route => ({ competitionId: route.query.competitionId }) },
  { path: '/ai-center', name: 'AICenter', component: AICenter },
  { path: '/profile', name: 'Profile', component: Profile },
  { path: '/test-profile', name: 'TestProfile', component: TestProfile }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
