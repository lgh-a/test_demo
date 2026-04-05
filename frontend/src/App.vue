<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <div class="min-h-screen flex flex-col text-white">
        <header class="h-14 flex items-center justify-between px-6 bg-[#212126] shadow-sm">
          <div class="text-xl font-bold cursor-pointer" @click="$router.push('/')">
            Retro Games Online
          </div>

          <div class="flex items-center gap-4">
            <template v-if="store.isLoggedIn">
              <span class="text-sm">Welcome, {{ store.userInfo.username }}</span>
              <n-button size="small" @click="$router.push('/favorites')">Favorites</n-button>
              <n-button size="small" @click="showChangePassword = true">Change Password</n-button>
              <n-button
                v-if="store.permissions.some((p) => p.startsWith('sys:'))"
                size="small"
                @click="$router.push('/admin')"
              >
                Admin
              </n-button>
              <n-button size="small" @click="handleLogout">Logout</n-button>
            </template>

            <n-button v-else type="primary" size="small" @click="showLogin = true">
              Login
            </n-button>
          </div>
        </header>

        <main class="flex-1 overflow-auto p-6 max-w-7xl mx-auto w-full">
          <div
            v-if="!store.authReady"
            class="flex min-h-[calc(100vh-8rem)] items-center justify-center text-sm text-gray-300"
          >
            Restoring session...
          </div>
          <router-view v-else />
        </main>
      </div>

      <LoginModal v-model:show="showLogin" />
      <ChangePasswordModal v-model:show="showChangePassword" @success="handlePasswordChanged" />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { darkTheme, NConfigProvider, NMessageProvider, NButton, createDiscreteApi } from 'naive-ui'
import { useAppStore } from './store'
import LoginModal from './components/LoginModal.vue'
import ChangePasswordModal from './components/ChangePasswordModal.vue'
import { request } from './api/request'

const store = useAppStore()
const route = useRoute()
const router = useRouter()
const { message } = createDiscreteApi(['message'])
const showLogin = ref(false)
const showChangePassword = ref(false)

watch(
  () => [route.query.login, store.authReady, store.isLoggedIn],
  ([loginFlag, authReady, isLoggedIn]) => {
    if (loginFlag === '1' && authReady && !isLoggedIn) {
      showLogin.value = true
    }
  },
  { immediate: true }
)

watch(
  () => store.isLoggedIn,
  (isLoggedIn) => {
    if (isLoggedIn && route.query.login === '1') {
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
      router.replace(redirect)
    }
  }
)

const handleLogout = async () => {
  try {
    await request('/auth/logout', { method: 'POST' })
  } finally {
    store.logout()
    router.push('/')
    showLogin.value = false
  }
}

const handlePasswordChanged = async () => {
  message.success('Please log in again with your new password')
  try {
    await request('/auth/logout', { method: 'POST' })
  } finally {
    store.logout()
    router.push('/')
    showLogin.value = true
  }
}
</script>
