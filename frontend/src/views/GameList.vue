<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold">{{ t('gameList.title') }}</h1>
        <p class="mt-1 text-sm text-gray-400">{{ t('gameList.subtitle') }}</p>
      </div>
      <n-button @click="$router.push('/')">{{ t('common.backHome') }}</n-button>
    </div>

    <div class="flex flex-col gap-4 rounded-2xl border border-white/10 bg-[#23232a] p-4 md:flex-row md:items-end md:justify-between">
      <div class="grid flex-1 gap-4 md:grid-cols-3">
        <div>
          <div class="mb-2 text-xs uppercase tracking-[0.28em] text-gray-400">{{ t('gameList.keyword') }}</div>
          <n-input v-model:value="keywordInput" :placeholder="t('gameList.keywordPlaceholder')" @keyup.enter="applyFilters" />
        </div>
        <div>
          <div class="mb-2 text-xs uppercase tracking-[0.28em] text-gray-400">{{ t('gameList.sort') }}</div>
          <n-select v-model:value="sortValue" :options="sortOptions" />
        </div>
        <div>
          <div class="mb-2 text-xs uppercase tracking-[0.28em] text-gray-400">{{ t('gameList.pageSize') }}</div>
          <n-select v-model:value="pageSize" :options="pageSizeOptions" />
        </div>
      </div>
      <div class="flex gap-2">
        <n-button secondary @click="resetFilters">{{ t('common.reset') }}</n-button>
        <n-button type="primary" @click="applyFilters">{{ t('common.apply') }}</n-button>
      </div>
    </div>

    <div class="flex items-center justify-between text-sm text-gray-400">
      <span>{{ t('common.totalGames', { total }) }}</span>
      <span>{{ t('common.page', { page: current }) }}</span>
    </div>

    <div v-if="loading" class="py-10 text-center">{{ t('gameList.loading') }}</div>
    <div v-else-if="games.length === 0" class="py-10 text-center text-gray-400">{{ t('gameList.empty') }}</div>
    <div v-else class="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
      <GameCard v-for="game in games" :key="game.id" :game="game" />
    </div>

    <div class="flex justify-center pt-2">
      <n-pagination
        v-model:page="current"
        :page-count="pageCount"
        :page-size="pageSize"
        :item-count="total"
        @update:page="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NInput, NPagination, NSelect } from 'naive-ui'
import { request } from '../api/request'
import GameCard from '../components/GameCard.vue'
import { useI18n } from '../i18n'

const route = useRoute()
const router = useRouter()
const games = ref([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(24)
const keywordInput = ref('')
const sortValue = ref('latest')
const loading = ref(false)
const { t } = useI18n()

const sortOptions = computed(() => [
  { label: t('gameList.sortLatest'), value: 'latest' },
  { label: t('gameList.sortUpdated'), value: 'updated' },
  { label: t('gameList.sortName'), value: 'name' }
])

const pageSizeOptions = computed(() => [
  { label: t('gameList.perPage', { count: 12 }), value: 12 },
  { label: t('gameList.perPage', { count: 24 }), value: 24 },
  { label: t('gameList.perPage', { count: 36 }), value: 36 }
])

const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const syncStateFromRoute = () => {
  current.value = Number(route.query.page || 1)
  pageSize.value = Number(route.query.size || 24)
  keywordInput.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  sortValue.value = typeof route.query.sort === 'string' ? route.query.sort : 'latest'
}

const fetchGames = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('current', String(current.value))
    params.append('size', String(pageSize.value))
    params.append('sort', sortValue.value)
    if (route.query.consoleId) params.append('consoleId', route.query.consoleId)
    if (route.query.seriesId) params.append('seriesId', route.query.seriesId)
    if (keywordInput.value.trim()) params.append('keyword', keywordInput.value.trim())

    const data = await request(`/games/list?${params.toString()}`)
    games.value = Array.isArray(data.records) ? data.records : []
    total.value = Number(data.total || 0)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const updateRoute = (page = 1) => {
  router.push({
    path: '/games',
    query: {
      ...route.query,
      page: String(page),
      size: String(pageSize.value),
      sort: sortValue.value,
      keyword: keywordInput.value.trim() || undefined
    }
  })
}

const applyFilters = () => updateRoute(1)

const resetFilters = () => {
  keywordInput.value = ''
  sortValue.value = 'latest'
  pageSize.value = 24
  updateRoute(1)
}

const handlePageChange = (page) => {
  updateRoute(page)
}

onMounted(() => {
  syncStateFromRoute()
  fetchGames()
})

watch(
  () => route.query,
  () => {
    syncStateFromRoute()
    fetchGames()
  }
)
</script>
