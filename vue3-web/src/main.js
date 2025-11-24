import { createApp } from 'vue'
import './styles/variables.css'
import './styles/base.css'
import './styles/components.css'

import './styles/ai-component.css'
import './styles/competition-component.css'
import './styles/profile-control.css'
import './styles/team-control.css'
import './styles/competition-control.css'
import './styles/application.css'
import './style.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 使用路由
app.use(router)

// 挂载应用
app.mount('#app')
