<template>
  <button
    class="inline-flex h-9 min-w-9 items-center justify-center rounded-full border px-3 text-sm transition-colors"
    :class="favorite
      ? 'border-amber-400/60 bg-amber-400/15 text-amber-200'
      : 'border-white/15 bg-black/35 text-white/80 hover:border-white/30 hover:text-white'"
    @click.stop="handleToggle"
  >
    {{ favorite ? 'Favorited' : 'Favorite' }}
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAppStore } from '../store'

const props = defineProps({
  gameId: {
    type: Number,
    required: true
  }
})

const store = useAppStore()
const router = useRouter()
const message = useMessage()
const favorite = computed(() => store.favoriteIds.includes(props.gameId))

const handleToggle = async () => {
  if (!store.isLoggedIn) {
    message.warning('Please log in first')
    await router.push({ path: '/', query: { login: '1', redirect: router.currentRoute.value.fullPath } })
    return
  }

  try {
    const nextState = await store.toggleFavorite(props.gameId)
    message.success(nextState ? 'Added to favorites' : 'Removed from favorites')
  } catch (error) {
    message.error(error.message || 'Favorite update failed')
  }
}
</script>
