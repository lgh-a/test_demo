<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold">My Favorites</h1>
        <p class="mt-1 text-sm text-gray-400">Your saved games are listed here.</p>
      </div>
      <n-button @click="$router.push('/')">Back Home</n-button>
    </div>

    <div v-if="loading" class="py-10 text-center">Loading favorites...</div>
    <div v-else-if="favoriteGames.length === 0" class="py-10 text-center text-gray-400">
      No favorite games yet.
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

const store = useAppStore()
const loading = ref(false)
const favoriteGames = computed(() => store.favoriteGames)

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([store.fetchFavoriteGames(50), store.fetchFavoriteIds()])
  } finally {
    loading.value = false
  }
})
</script>
