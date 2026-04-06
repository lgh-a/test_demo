<template>
  <AdminPageCard :eyebrow="t('admin.overview')" :title="t('admin.title')" :subtitle="t('admin.overviewSubtitle')">
    <template #actions>
      <n-button size="small" secondary :loading="loading" @click="loadOverview">{{ t('common.refresh') }}</n-button>
    </template>

    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-6">
      <div v-for="card in cards" :key="card.key" class="admin-overview-card rounded-3xl p-5">
        <div class="admin-muted text-xs uppercase tracking-[0.28em]">{{ card.label }}</div>
        <div class="mt-4 text-3xl font-semibold text-[var(--app-text)]">{{ card.value }}</div>
      </div>
    </div>
  </AdminPageCard>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { NButton } from 'naive-ui'
import { request } from '../api/request'
import { useI18n } from '../i18n'
import AdminPageCard from './admin/AdminPageCard.vue'

const loading = ref(false)
const { t } = useI18n()
const overview = ref({
  totalGames: 0,
  totalUsers: 0,
  enabledUsers: 0,
  totalRoles: 0,
  totalMenus: 0,
  activeRateLimitRules: 0
})

const cards = computed(() => [
  { key: 'totalGames', label: t('admin.totalGames'), value: overview.value.totalGames },
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
