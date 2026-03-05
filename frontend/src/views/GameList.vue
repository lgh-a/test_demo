<template>
  <div>
    <div class="mb-6 flex items-center justify-between">
      <h1 class="text-2xl font-bold">游戏列表</h1>
      <n-button @click="$router.push('/')">返回首页</n-button>
    </div>
    
    <div v-if="loading" class="text-center py-10">
      加载中...
    </div>
    
    <div v-else-if="games.length === 0" class="text-center py-10 text-gray-400">
      没有找到相关游戏
    </div>

    <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
      <GameCard v-for="g in games" :key="g.id" :game="g" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { request } from '../api/request'
import { NButton } from 'naive-ui'
import GameCard from '../components/GameCard.vue'

const route = useRoute()
const games = ref([])
const loading = ref(false)

const fetchGames = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (route.query.consoleId) params.append('consoleId', route.query.consoleId)
    if (route.query.seriesId) params.append('seriesId', route.query.seriesId)
    if (route.query.keyword) params.append('keyword', route.query.keyword)
    
    games.value = await request(`/games/list?${params.toString()}`)
  } catch(e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchGames)
watch(() => route.query, fetchGames)
</script>
