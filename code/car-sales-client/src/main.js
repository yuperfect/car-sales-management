import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { clearCurrentUser } from './utils/user.js'
import './assets/main.css'

// 每次启动默认无用户状态
clearCurrentUser()

const app = createApp(App)
app.use(router)
app.mount('#app')
