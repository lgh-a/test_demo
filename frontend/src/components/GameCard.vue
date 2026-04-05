<template>
  <div
    class="overflow-hidden rounded-lg border border-transparent bg-[#2c2c31] transition-transform hover:-translate-y-1 hover:border-gray-500"
  >
    <div class="relative h-32 bg-gray-800 cursor-pointer" @click="goPlay">
      <img v-if="game.thumbUrl" :src="baseHost + game.thumbUrl" class="h-full w-full object-cover" />
      <div v-else class="flex h-full w-full items-center justify-center text-gray-500">No image</div>
      <div class="absolute right-2 top-2 flex gap-1">
        <n-tag size="small" type="info">{{ game.language }}</n-tag>
      </div>
    </div>
    <div class="space-y-3 p-3">
      <div class="cursor-pointer truncate font-bold" :title="game.name" @click="goPlay">{{ game.name }}</div>
      <div class="flex items-center justify-between gap-2">
        <button
          class="rounded-md bg-white/10 px-3 py-1 text-xs text-white transition-colors hover:bg-white/20"
          @click="goPlay"
        >
          Play
        </button>
        <FavoriteButton :game-id="game.id" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { NTag } from 'naive-ui'
import { backendOrigin } from '../api/request'
import FavoriteButton from './FavoriteButton.vue'

const props = defineProps({
  game: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const baseHost = backendOrigin

const goPlay = () => {
  router.push(`/play/${props.game.id}`)
}
</script>
