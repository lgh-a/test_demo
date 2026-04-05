<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold">{{ t('favorites.title') }}</h1>
        <p class="mt-1 text-sm text-gray-400">{{ t('favorites.subtitle') }}</p>
      </div>
      <n-button @click="$router.push('/')">{{ t('common.backHome') }}</n-button>
    </div>

    <div v-if="loading" class="py-10 text-center">{{ t('favorites.loading') }}</div>
    <div v-else-if="favoriteGames.length === 0" class="py-10 text-center text-gray-400">
      {{ t('favorites.empty') }}
    </div>
    <div v-else class="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
      <GameCard v-for="game in favoriteGames" :key="game.id" :game="game" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { NButton } from 'naive-ui'
import GameCard from '../components/GameCard.vue'
import { useAppStore } from '../store'
import { useI18n } from '../i18n'

const store = useAppStore()
const loading = ref(false)
const favoriteGames = computed(() => store.favoriteGames)
const { t } = useI18n()

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([store.fetchFavoriteGames(50), store.fetchFavoriteIds()])
  } finally {
    loading.value = false
  }
})
</script>
