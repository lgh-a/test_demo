<template>
  <n-config-provider :theme="naiveTheme" :locale="naiveLocale" :date-locale="naiveDateLocale">
    <n-message-provider>
      <div :class="isAdminRoute ? 'app-shell flex h-screen flex-col overflow-hidden' : 'app-shell flex min-h-screen flex-col'">
        <header class="app-header h-14 px-4 sm:px-6">
          <div class="flex h-full items-center justify-between gap-4">
            <div class="app-brand" @click="$router.push('/')">
              {{ t('app.title') }}
            </div>

            <div class="flex items-center gap-2 sm:gap-3">
              <n-button quaternary size="small" @click="toggleTheme">
                {{ themeButtonLabel }}
              </n-button>
              <n-select
                :value="locale"
                :options="localeOptions"
                size="small"
                class="w-24 sm:w-28"
                @update:value="setLocale"
              />
              <template v-if="store.isLoggedIn">
                <span class="hidden text-sm text-[var(--app-text-muted)] lg:inline">
                  {{ t('app.welcome', { username: store.userInfo.username }) }}
                </span>
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
          </div>
        </header>

        <main
          :class="
            isAdminRoute
              ? 'app-main-admin flex-1 min-h-0 w-full overflow-hidden'
              : 'app-main-shell flex-1 w-full'
          "
        >
          <div
            v-if="!store.authReady"
            class="flex min-h-[calc(100vh-8rem)] items-center justify-center text-sm text-[var(--app-text-muted)]"
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
import { useTheme } from './composables/useTheme'

const store = useAppStore()
const route = useRoute()
const router = useRouter()
const { message } = createDiscreteApi(['message'])
const showLogin = ref(false)
const showChangePassword = ref(false)
const { locale, localeOptions, setLocale, t, isZh } = useI18n()
const { isDark, toggleTheme } = useTheme()

const naiveLocale = computed(() => (isZh.value ? zhCN : enUS))
const naiveDateLocale = computed(() => (isZh.value ? dateZhCN : undefined))
const naiveTheme = computed(() => (isDark.value ? darkTheme : null))
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
const themeButtonLabel = computed(() => {
  if (isZh.value) {
    return isDark.value ? '黑夜' : '白天'
  }
  return isDark.value ? 'Dark' : 'Light'
})

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

watch(
  () => showLogin.value,
  (visible) => {
    if (!visible && !store.isLoggedIn && route.query.login === '1') {
      const nextQuery = { ...route.query }
      delete nextQuery.login
      delete nextQuery.redirect
      router.replace({
        path: route.path,
        query: nextQuery
      })
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
