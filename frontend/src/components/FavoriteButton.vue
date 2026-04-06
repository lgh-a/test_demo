<template>
  <button
    class="favorite-button inline-flex h-9 min-w-9 items-center justify-center rounded-full border px-3 text-sm"
    :class="favorite ? 'favorite-button--active' : 'favorite-button--idle'"
    @click.stop="handleToggle"
  >
    {{ favorite ? t('game.favorited') : t('game.favorite') }}
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAppStore } from '../store'
import { useI18n } from '../i18n'

const props = defineProps({
  gameId: {
    type: Number,
    required: true
  }
})

const store = useAppStore()
const router = useRouter()
const message = useMessage()
const { t } = useI18n()
const favorite = computed(() => store.favoriteIds.includes(props.gameId))

const handleToggle = async () => {
  if (!store.isLoggedIn) {
    message.warning(t('game.favoriteLoginFirst'))
    await router.push({ path: '/', query: { login: '1', redirect: router.currentRoute.value.fullPath } })
    return
  }

  try {
    const nextState = await store.toggleFavorite(props.gameId)
    message.success(nextState ? t('game.favoriteAdded') : t('game.favoriteRemoved'))
  } catch (error) {
    message.error(error.message || t('game.favoriteUpdateFailed'))
  }
}
</script>

<style scoped>
.favorite-button {
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease;
}

.favorite-button:hover {
  transform: translateY(-1px);
}

.favorite-button--idle {
  border-color: var(--app-border);
  background: var(--app-surface-soft);
  color: var(--app-text);
}

.favorite-button--idle:hover {
  border-color: rgba(59, 130, 246, 0.3);
  background: var(--app-surface-hover);
}

.favorite-button--active {
  border-color: rgba(245, 158, 11, 0.34);
  background: rgba(245, 158, 11, 0.14);
  color: #f59e0b;
}
</style>
