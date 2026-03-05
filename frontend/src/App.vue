<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <div class="min-h-screen flex flex-col text-white">
        <!-- Header -->
        <header class="h-14 flex items-center justify-between px-6 bg-[#212126] shadow-sm">
          <div class="text-xl font-bold cursor-pointer" @click="$router.push('/')">
            🕹️ 老游戏在线玩 (Clone)
          </div>
          <div class="flex items-center gap-4">
             <template v-if="store.isLoggedIn">
               <span class="text-sm">欢迎, {{ store.userInfo.username }}</span>
               <n-button v-if="store.permissions.some(p => p.startsWith('sys:'))" size="small" @click="$router.push('/admin')">进入后台</n-button>
               <n-button size="small" @click="handleLogout">退出</n-button>
             </template>
             <n-button v-else type="primary" size="small" @click="showLogin = true">登录 / 注册</n-button>
          </div>
        </header>

        <!-- Main Content -->
        <main class="flex-1 overflow-auto p-6 max-w-7xl mx-auto w-full">
          <router-view></router-view>
        </main>
      </div>
      
      <LoginModal v-model:show="showLogin" />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { darkTheme, NConfigProvider, NMessageProvider, NButton, useMessage } from 'naive-ui'
import { useAppStore } from './store'
import LoginModal from './components/LoginModal.vue'
import { request } from './api/request'

const store = useAppStore()
const router = useRouter()
const showLogin = ref(false)

onMounted(async () => {
  if (localStorage.getItem('satoken')) {
    await store.fetchUserInfo()
  }
})

const handleLogout = async () => {
  try {
     await request('/auth/logout', { method: 'POST' })
  } finally {
     store.logout()
     router.push('/')
  }
}
</script>
