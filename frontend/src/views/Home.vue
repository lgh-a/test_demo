<template>
  <div class="space-y-8">
    <div class="flex flex-col items-center justify-center py-12">
      <h1 class="mb-6 text-4xl font-bold">{{ t('home.heroTitle') }}</h1>
      <n-input
        v-model:value="searchKeyword"
        class="max-w-xl w-full"
        size="large"
        :placeholder="t('home.searchPlaceholder')"
        round
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <span>{{ t('home.searchPrefix') }}</span>
        </template>
      </n-input>
    </div>

    <div>
      <h2 class="mb-4 text-2xl font-bold">{{ t('home.consoles') }}</h2>
      <div class="flex gap-4 overflow-x-auto pb-4">
        <div
          v-for="consoleItem in consoles"
          :key="consoleItem.id"
          class="flex h-24 w-24 flex-shrink-0 cursor-pointer flex-col items-center justify-center rounded-xl bg-[#2c2c31] transition-colors hover:bg-gray-700"
          @click="$router.push(`/games?consoleId=${consoleItem.id}`)"
        >
          <div class="text-xl font-bold">{{ consoleItem.name }}</div>
          <div class="mt-1 text-center text-xs text-gray-400">{{ consoleItem.fullName }}</div>
        </div>
      </div>
    </div>

    <div>
      <h2 class="mb-4 text-2xl font-bold">{{ t('home.series') }}</h2>
      <div class="grid grid-cols-2 gap-4 md:grid-cols-4 lg:grid-cols-6">
        <div
          v-for="seriesItem in series"
          :key="seriesItem.id"
          class="flex cursor-pointer items-center justify-center rounded-xl bg-[#2c2c31] p-4 transition-colors hover:bg-gray-700"
          @click="$router.push(`/games?seriesId=${seriesItem.id}`)"
        >
          <span class="font-bold">{{ seriesItem.name }}</span>
        </div>
      </div>
    </div>

    <div v-if="featuredGames.length > 0">
      <div class="mb-4 flex items-center justify-between">
        <h2 class="text-2xl font-bold">{{ t('home.recommended') }}</h2>
        <n-button quaternary size="small" @click="$router.push('/games?sort=updated')">{{ t('home.browseMore') }}</n-button>
      </div>
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
        <GameCard v-for="game in featuredGames" :key="`featured-${game.id}`" :game="game" />
      </div>
    </div>

    <div v-if="latestGames.length > 0">
      <div class="mb-4 flex items-center justify-between">
        <h2 class="text-2xl font-bold">{{ t('home.latestAdded') }}</h2>
        <n-button quaternary size="small" @click="$router.push('/games?sort=latest')">{{ t('home.browseMore') }}</n-button>
      </div>
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
        <GameCard v-for="game in latestGames" :key="`latest-${game.id}`" :game="game" />
      </div>
    </div>

    <div v-if="store.isLoggedIn && favoriteGames.length > 0">
      <div class="mb-4 flex items-center justify-between">
        <h2 class="text-2xl font-bold">{{ t('home.myFavorites') }}</h2>
        <n-button quaternary size="small" @click="$router.push('/favorites')">{{ t('home.viewAll') }}</n-button>
      </div>
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
        <GameCard v-for="game in favoriteGames.slice(0, 6)" :key="`favorite-${game.id}`" :game="game" />
      </div>
    </div>

    <div v-if="store.isLoggedIn && recentHistory.length > 0">
      <div class="mb-4 flex items-center justify-between">
        <h2 class="text-2xl font-bold">{{ t('home.recentlyPlayed') }}</h2>
        <n-button quaternary size="small" @click="refreshRecentHistory">{{ t('common.refresh') }}</n-button>
      </div>
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
        <GameCard v-for="game in recentHistory" :key="`history-${game.id}`" :game="game" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NInput } from 'naive-ui'
import { request } from '../api/request'
import { useAppStore } from '../store'
import GameCard from '../components/GameCard.vue'
import { useI18n } from '../i18n'

const router = useRouter()
const store = useAppStore()
const { t } = useI18n()
const searchKeyword = ref('')
const consoles = ref([])
const series = ref([])
const featuredGames = ref([])
const latestGames = ref([])
const recentHistory = computed(() => store.recentHistory)
const favoriteGames = computed(() => store.favoriteGames)

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    router.push(`/games?keyword=${encodeURIComponent(keyword)}`)
  }
}

const fetchGameSection = async (sort) => {
  const data = await request(`/games/list?current=1&size=6&sort=${sort}`)
  return Array.isArray(data.records) ? data.records : []
}

const refreshRecentHistory = async () => {
  try {
    await store.fetchRecentHistory()
  } catch (error) {
    console.error('Failed to load recent history', error)
  }
}

const refreshFavorites = async () => {
  try {
    await Promise.all([store.fetchFavoriteGames(6), store.fetchFavoriteIds()])
  } catch (error) {
    console.error('Failed to load favorites', error)
  }
}

onMounted(async () => {
  const [consoleData, seriesData, updatedData, latestData] = await Promise.all([
    request('/categories/consoles'),
    request('/categories/series'),
    fetchGameSection('updated'),
    fetchGameSection('latest')
  ])

  consoles.value = consoleData
  series.value = seriesData
  featuredGames.value = updatedData
  latestGames.value = latestData

  if (store.isLoggedIn) {
    await Promise.all([refreshRecentHistory(), refreshFavorites()])
  }
})
</script>
