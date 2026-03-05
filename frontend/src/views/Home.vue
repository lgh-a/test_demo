<template>
  <div class="space-y-8">
    <!-- Hero / Search -->
    <div class="flex flex-col items-center justify-center py-12">
      <h1 class="text-4xl font-bold mb-6">海量怀旧游戏 在线即玩</h1>
      <n-input
        v-model:value="searchKeyword"
        class="max-w-xl w-full"
        size="large"
        placeholder="搜索游戏 (如: 马里奥)"
        round
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <span>🔍</span>
        </template>
      </n-input>
    </div>

    <!-- Consoles -->
    <div>
      <h2 class="text-2xl font-bold mb-4">全部游戏机</h2>
      <div class="flex gap-4 overflow-x-auto pb-4">
        <div 
          v-for="c in consoles" 
          :key="c.id"
          class="flex-shrink-0 w-24 h-24 bg-[#2c2c31] rounded-xl flex flex-col items-center justify-center cursor-pointer hover:bg-gray-700 transition-colors"
          @click="$router.push(`/games?consoleId=${c.id}`)"
        >
          <div class="text-xl font-bold">{{ c.name }}</div>
          <div class="text-xs text-gray-400 mt-1">{{ c.fullName }}</div>
        </div>
      </div>
    </div>

    <!-- Series -->
    <div>
      <h2 class="text-2xl font-bold mb-4">热门系列</h2>
      <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
        <div 
          v-for="s in series" 
          :key="s.id"
          class="bg-[#2c2c31] p-4 rounded-xl flex items-center justify-center cursor-pointer hover:bg-gray-700 transition-colors"
          @click="$router.push(`/games?seriesId=${s.id}`)"
        >
          <span class="font-bold">{{ s.name }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '../api/request'
import { NInput } from 'naive-ui'

const router = useRouter()
const searchKeyword = ref('')
const consoles = ref([])
const series = ref([])

const handleSearch = () => {
  if (searchKeyword.value) {
    router.push(`/games?keyword=${searchKeyword.value}`)
  }
}

onMounted(async () => {
  consoles.value = await request('/categories/consoles')
  series.value = await request('/categories/series')
})
</script>
