<template>
  <div class="bg-[#2c2c31] rounded-lg overflow-hidden cursor-pointer hover:-translate-y-1 transition-transform border border-transparent hover:border-gray-500" @click="goPlay">
    <div class="h-32 bg-gray-800 relative">
      <img v-if="game.thumbUrl" :src="baseHost + game.thumbUrl" class="w-full h-full object-cover" />
      <div v-else class="w-full h-full flex items-center justify-center text-gray-500">图片加载中...</div>
      <div class="absolute top-2 right-2 flex gap-1">
        <n-tag size="small" type="info">{{ game.language }}</n-tag>
      </div>
    </div>
    <div class="p-3">
      <div class="font-bold truncate" :title="game.name">{{ game.name }}</div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { NTag } from 'naive-ui'

const props = defineProps({
  game: Object
})

const router = useRouter()
const baseHost = 'http://localhost:8080'

const goPlay = () => {
  router.push(`/play/${props.game.id}`)
}
</script>
