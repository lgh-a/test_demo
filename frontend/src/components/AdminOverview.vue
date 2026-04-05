<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-bold text-white">{{ t('admin.overview') }}</h2>
        <p class="text-sm text-gray-400">{{ t('admin.overviewSubtitle') }}</p>
      </div>
      <n-button size="small" :loading="loading" @click="loadOverview">{{ t('common.refresh') }}</n-button>
    </div>

    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
      <div
        v-for="card in cards"
        :key="card.key"
        class="rounded-2xl border border-white/10 bg-[#23232a] p-5 shadow-sm"
      >
        <div class="text-xs uppercase tracking-[0.28em] text-gray-400">{{ card.label }}</div>
        <div class="mt-3 text-3xl font-bold text-white">{{ card.value }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { NButton } from 'naive-ui'
import { request } from '../api/request'
import { useI18n } from '../i18n'

const loading = ref(false)
const { t } = useI18n()
const overview = ref({
  totalUsers: 0,
  enabledUsers: 0,
  totalRoles: 0,
  totalMenus: 0,
  activeRateLimitRules: 0
})

const cards = computed(() => [
  { key: 'totalUsers', label: t('admin.totalUsers'), value: overview.value.totalUsers },
  { key: 'enabledUsers', label: t('admin.enabledUsers'), value: overview.value.enabledUsers },
  { key: 'totalRoles', label: t('admin.totalRoles'), value: overview.value.totalRoles },
  { key: 'totalMenus', label: t('admin.totalMenus'), value: overview.value.totalMenus },
  { key: 'activeRateLimitRules', label: t('admin.activeRules'), value: overview.value.activeRateLimitRules }
])

const loadOverview = async () => {
  loading.value = true
  try {
    overview.value = await request('/admin/overview')
  } finally {
    loading.value = false
  }
}

onMounted(loadOverview)
</script>
