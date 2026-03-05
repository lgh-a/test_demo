import { createApp } from 'vue'
import './index.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

import { authDirective } from './directives/auth'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.directive('auth', authDirective)
app.mount('#app')
