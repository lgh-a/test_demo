import { createApp } from 'vue'
import './index.css'
import App from './App.vue'
import router from './router'
import { pinia } from './store/pinia'

import { authDirective } from './directives/auth'

const app = createApp(App)
app.use(pinia)
app.use(router)
app.directive('auth', authDirective)
app.mount('#app')
