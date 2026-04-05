<template>
  <n-config-provider :theme="darkTheme" :locale="naiveLocale" :date-locale="naiveDateLocale">
    <n-message-provider>
      <div class="min-h-screen flex flex-col text-white">
        <header class="h-14 flex items-center justify-between px-6 bg-[#212126] shadow-sm">
          <div class="text-xl font-bold cursor-pointer" @click="$router.push('/')">
            {{ t('app.title') }}
          </div>

          <div class="flex items-center gap-4">
            <n-select
              :value="locale"
              :options="localeOptions"
              size="small"
              class="w-28"
              @update:value="setLocale"
            />
            <template v-if="store.isLoggedIn">
              <span class="text-sm">{{ t('app.welcome', { username: store.userInfo.username }) }}</span>
              <n-button size="small" @click="$router.push('/favorites')">{{ t('app.favorites') }}</n-button>
              <n-button size="small" @click="showChangePassword = true">{{ t('app.changePassword') }}</n-button>
              <n-button
                v-if="store.permissions.some((p) => p.startsWith('sys:'))"
                size="small"
                @click="$router.push('/admin')"
              >
                {{ t('app.admin') }}
              </n-button>
              <n-button size="small" @click="handleLogout">{{ t('app.logout') }}</n-button>
            </template>

            <n-button v-else type="primary" size="small" @click="showLogin = true">
              {{ t('app.login') }}
            </n-button>
          </div>
        </header>

        <main class="flex-1 overflow-auto p-6 max-w-7xl mx-auto w-full">
          <div
            v-if="!store.authReady"
            class="flex min-h-[calc(100vh-8rem)] items-center justify-center text-sm text-gray-300"
          >
            {{ t('app.restoringSession') }}
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
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NButton,
  NConfigProvider,
  NMessageProvider,
  NSelect,
  createDiscreteApi,
  darkTheme,
  dateZhCN,
  enUS,
  zhCN
} from 'naive-ui'
import { useAppStore } from './store'
import LoginModal from './components/LoginModal.vue'
import ChangePasswordModal from './components/ChangePasswordModal.vue'
import { request } from './api/request'
import { useI18n } from './i18n'

const store = useAppStore()
const route = useRoute()
const router = useRouter()
const { message } = createDiscreteApi(['message'])
const showLogin = ref(false)
const showChangePassword = ref(false)
const { locale, localeOptions, setLocale, t, isZh } = useI18n()

const naiveLocale = computed(() => (isZh.value ? zhCN : enUS))
const naiveDateLocale = computed(() => (isZh.value ? dateZhCN : undefined))

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
  message.success(t('auth.reloginAfterPasswordChange'))
  try {
    await request('/auth/logout', { method: 'POST' })
  } finally {
    store.logout()
    router.push('/')
    showLogin.value = true
  }
}
</script>
