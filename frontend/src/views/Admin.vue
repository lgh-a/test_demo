<template>
  <div class="min-h-[calc(100vh-3.5rem)] h-full bg-[#18181c] p-6 text-white">
    <div class="mb-6 flex items-center justify-between">
      <h1 class="text-2xl font-bold">Admin Panel</h1>
      <n-button @click="$router.push('/')">Back Home</n-button>
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

const AdminOverview = defineAsyncComponent(() => import('../components/AdminOverview.vue'))
const route = useRoute()
const store = useAppStore()

const tabs = [
  { label: 'Users', to: '/admin/users', permission: 'sys:user:list' },
  { label: 'Roles', to: '/admin/roles', permission: 'sys:role:list' },
  { label: 'Menus', to: '/admin/menus', permission: 'sys:menu:list' },
  { label: 'Rate Limit', to: '/admin/rate-limit', permission: 'rate-limit:rule:list' }
]

const visibleTabs = computed(() => tabs.filter((item) => store.hasPerm(item.permission)))
</script>
