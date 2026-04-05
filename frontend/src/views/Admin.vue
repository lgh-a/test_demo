<template>
  <div class="min-h-[calc(100vh-3.5rem)] h-full bg-[#18181c] p-6 text-white">
    <div class="mb-6 flex items-center justify-between">
      <h1 class="text-2xl font-bold">{{ t('admin.title') }}</h1>
      <n-button @click="$router.push('/')">{{ t('common.backHome') }}</n-button>
    </div>

    <div class="mb-6" v-auth="'sys:user:list'">
      <AdminOverview />
    </div>

    <div class="mb-6 flex flex-wrap gap-2">
      <n-button
        v-for="item in visibleTabs"
        :key="item.to"
        :type="route.path === item.to ? 'primary' : 'default'"
        @click="$router.push(item.to)"
      >
        {{ item.label }}
      </n-button>
    </div>

    <router-view />
  </div>
</template>

<script setup>
import { computed, defineAsyncComponent } from 'vue'
import { useRoute } from 'vue-router'
import { NButton } from 'naive-ui'
import { useAppStore } from '../store'
import { useI18n } from '../i18n'

const AdminOverview = defineAsyncComponent(() => import('../components/AdminOverview.vue'))
const route = useRoute()
const store = useAppStore()
const { t } = useI18n()

const tabs = computed(() => [
  { label: t('admin.users'), to: '/admin/users', permission: 'sys:user:list' },
  { label: t('admin.roles'), to: '/admin/roles', permission: 'sys:role:list' },
  { label: t('admin.menus'), to: '/admin/menus', permission: 'sys:menu:list' },
  { label: t('admin.rateLimit'), to: '/admin/rate-limit', permission: 'rate-limit:rule:list' }
])

const visibleTabs = computed(() => tabs.value.filter((item) => store.hasPerm(item.permission)))
</script>
