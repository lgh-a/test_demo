<template>
  <div class="admin-shell flex h-full min-h-0 overflow-hidden">
    <div
      v-if="mobileNavOpen"
      class="admin-overlay fixed inset-x-0 bottom-0 top-14 z-30 md:hidden"
      @click="mobileNavOpen = false"
    />

    <aside
      :class="[
        'admin-sidebar fixed bottom-0 left-0 top-14 z-40 flex flex-col transition-[width,transform] duration-200 md:static md:top-0 md:translate-x-0',
        isSidebarCollapsed ? 'md:w-24' : 'md:w-72',
        mobileNavOpen ? 'translate-x-0' : '-translate-x-full'
      ]"
    >
      <div class="admin-sidebar-section px-4 py-4">
        <div class="flex items-start justify-between gap-3">
          <div v-if="!isSidebarCollapsed" class="min-w-0">
            <div class="admin-card-eyebrow text-xs uppercase tracking-[0.28em]">{{ t('app.admin') }}</div>
            <div class="mt-2 text-2xl font-semibold text-[var(--app-text)]">{{ t('admin.title') }}</div>
          </div>
          <div
            v-else
            class="admin-logo-badge flex h-12 w-12 items-center justify-center rounded-2xl text-sm font-semibold"
          >
            ADM
          </div>
          <n-button quaternary circle class="hidden md:inline-flex" @click="isSidebarCollapsed = !isSidebarCollapsed">
            {{ isSidebarCollapsed ? '>' : '<' }}
          </n-button>
        </div>
      </div>

      <div class="admin-sidebar-section px-4 py-4">
        <div class="admin-side-panel rounded-3xl p-3" :class="isSidebarCollapsed ? 'flex justify-center' : 'space-y-3'">
          <div class="flex items-center gap-3" :class="isSidebarCollapsed ? 'justify-center' : ''">
            <div class="admin-user-badge flex h-11 w-11 items-center justify-center rounded-2xl text-sm font-semibold">
              {{ userInitials }}
            </div>
            <div v-if="!isSidebarCollapsed" class="min-w-0">
              <div class="truncate text-sm font-medium text-[var(--app-text)]">
                {{ store.userInfo?.username || t('app.admin') }}
              </div>
              <div class="admin-muted truncate text-xs">{{ roleLabel }}</div>
            </div>
          </div>
          <div v-if="!isSidebarCollapsed" class="admin-filter-panel rounded-2xl px-3 py-2 text-xs admin-muted">
            {{ dashboardText.workbench }}
          </div>
        </div>
      </div>

      <div class="flex-1 min-h-0 overflow-y-auto px-3 py-4">
        <div v-if="!isSidebarCollapsed" class="admin-muted mb-3 px-2 text-[11px] uppercase tracking-[0.28em]">
          {{ dashboardText.navigation }}
        </div>
        <button
          v-for="item in visibleTabs"
          :key="item.to"
          class="admin-nav-item mb-2 flex w-full items-center gap-3 rounded-2xl px-3 py-3 text-left transition-colors"
          :class="route.path === item.to ? 'admin-nav-item--active' : ''"
          @click="navigateTo(item.to)"
        >
          <span class="admin-nav-icon flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-2xl text-xs font-semibold">
            {{ item.code }}
          </span>
          <span v-if="!isSidebarCollapsed" class="min-w-0 flex-1">
            <span class="block truncate font-medium">{{ item.label }}</span>
            <span class="admin-muted block truncate text-xs">{{ item.hint }}</span>
          </span>
          <span
            v-if="!isSidebarCollapsed && route.path === item.to"
            class="admin-nav-indicator h-2.5 w-2.5 rounded-full bg-current"
          />
        </button>
      </div>

      <div class="px-4 py-4" style="border-top: 1px solid var(--app-border)">
        <n-button block secondary @click="navigateTo('/')">
          {{ isSidebarCollapsed ? t('common.back') : t('common.backHome') }}
        </n-button>
      </div>
    </aside>

    <div class="flex min-w-0 flex-1 min-h-0 flex-col">
      <div class="admin-topbar flex items-center justify-between px-4 py-4 md:px-6">
        <div class="min-w-0">
          <div class="admin-muted text-xs uppercase tracking-[0.28em]">{{ t('admin.navigation') }}</div>
          <h1 class="truncate text-2xl font-semibold text-[var(--app-text)]">{{ currentTabLabel }}</h1>
        </div>

        <div class="flex items-center gap-2">
          <n-button class="md:hidden" quaternary @click="mobileNavOpen = true">{{ t('admin.navigation') }}</n-button>
          <n-button class="hidden md:inline-flex" quaternary @click="isSidebarCollapsed = !isSidebarCollapsed">
            {{ isSidebarCollapsed ? dashboardText.expand : dashboardText.collapse }}
          </n-button>
          <n-button @click="navigateTo('/')">{{ t('common.backHome') }}</n-button>
        </div>
      </div>

      <div class="flex-1 min-h-0 overflow-y-auto px-4 py-4 md:px-6 md:py-6">
        <div class="mb-6 w-full min-w-0" v-auth="'sys:user:list'">
          <AdminOverview />
        </div>

        <div class="w-full min-w-0 min-h-0">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, defineAsyncComponent, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton } from 'naive-ui'
import { useAppStore } from '../store'
import { useI18n } from '../i18n'

const AdminOverview = defineAsyncComponent(() => import('../components/AdminOverview.vue'))
const route = useRoute()
const router = useRouter()
const store = useAppStore()
const { t, isZh } = useI18n()
const mobileNavOpen = ref(false)
const isSidebarCollapsed = ref(false)

const copy = computed(() =>
  isZh.value
    ? {
        workbench: '管理工作台',
        navigation: '功能导航',
        consolesLabel: '主机管理',
        seriesLabel: '系列管理',
        gamesHint: '游戏与运营',
        consolesHint: '主机分类与平台',
        seriesHint: '系列分类与归属',
        usersHint: '账号与用户',
        rolesHint: '角色与权限',
        menusHint: '菜单与路由',
        rateLimitHint: '限流规则',
        expand: '展开',
        collapse: '收起'
      }
    : {
        workbench: 'Admin workbench',
        navigation: 'Navigation',
        consolesLabel: 'Console Management',
        seriesLabel: 'Series Management',
        gamesHint: 'Games and operations',
        consolesHint: 'Console catalogs and platforms',
        seriesHint: 'Series catalogs and grouping',
        usersHint: 'Accounts and users',
        rolesHint: 'Roles and permissions',
        menusHint: 'Menus and routes',
        rateLimitHint: 'Rate limit rules',
        expand: 'Expand',
        collapse: 'Collapse'
      }
)

const dashboardText = computed(() => copy.value)

const tabs = computed(() => [
  { label: t('admin.games'), hint: copy.value.gamesHint, code: 'GM', to: '/admin/games', permissions: ['game:manage:list'] },
  {
    label: copy.value.consolesLabel,
    hint: copy.value.consolesHint,
    code: 'CS',
    to: '/admin/consoles',
    permissions: ['console:manage:list']
  },
  {
    label: copy.value.seriesLabel,
    hint: copy.value.seriesHint,
    code: 'SR',
    to: '/admin/series',
    permissions: ['series:manage:list']
  },
  { label: t('admin.users'), hint: copy.value.usersHint, code: 'US', to: '/admin/users', permissions: ['sys:user:list'] },
  { label: t('admin.roles'), hint: copy.value.rolesHint, code: 'RL', to: '/admin/roles', permissions: ['sys:role:list'] },
  { label: t('admin.menus'), hint: copy.value.menusHint, code: 'MN', to: '/admin/menus', permissions: ['sys:menu:list'] },
  {
    label: t('admin.rateLimit'),
    hint: copy.value.rateLimitHint,
    code: 'RT',
    to: '/admin/rate-limit',
    permissions: ['rate-limit:rule:list']
  }
])

const visibleTabs = computed(() =>
  tabs.value.filter((item) => item.permissions.some((permission) => store.hasPerm(permission)))
)

const currentTabLabel = computed(
  () => visibleTabs.value.find((item) => item.to === route.path)?.label || t('admin.title')
)

const roleLabel = computed(() => {
  if (store.roles.length === 0) {
    return t('app.admin')
  }
  return store.roles.join(' / ')
})

const userInitials = computed(() => {
  const username = store.userInfo?.username || 'AD'
  return username.slice(0, 2).toUpperCase()
})

const navigateTo = (path) => {
  mobileNavOpen.value = false
  router.push(path)
}

watch(
  () => route.path,
  () => {
    mobileNavOpen.value = false
  }
)
</script>
