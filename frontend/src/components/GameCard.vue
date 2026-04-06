<template>
  <article class="game-card h-full overflow-hidden rounded-[22px]">
    <div class="game-card__media relative h-40 cursor-pointer" @click="goPlay">
      <img v-if="game.thumbUrl" :src="baseHost + game.thumbUrl" class="h-full w-full object-cover" />
      <div v-else class="flex h-full w-full items-center justify-center text-sm text-[var(--app-text-muted)]">
        {{ t('game.noImage') }}
      </div>

      <div class="game-card__overlay"></div>
      <div class="absolute inset-x-0 bottom-0 flex items-end justify-between gap-3 p-4">
        <div class="min-w-0">
          <div class="truncate text-lg font-bold text-white" :title="game.name">{{ game.name }}</div>
          <div class="mt-1 text-xs text-white/70">{{ game.language || '-' }}</div>
        </div>
        <n-tag size="small" :bordered="false" class="game-card__tag">
          {{ game.language || 'N/A' }}
        </n-tag>
      </div>
    </div>

    <div class="flex h-[calc(100%-10rem)] flex-col justify-between gap-4 p-4">
      <div class="space-y-2">
        <div class="text-sm font-medium text-[var(--app-text-muted)]">
          {{ detailText }}
        </div>
        <button type="button" class="cursor-pointer text-left text-base font-bold" :title="game.name" @click="goPlay">
          {{ game.name }}
        </button>
      </div>

      <div class="flex items-center justify-between gap-3">
        <button type="button" class="game-card__action" @click="goPlay">
          {{ t('game.play') }}
        </button>
        <FavoriteButton :game-id="game.id" />
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { NTag } from 'naive-ui'
import { backendOrigin } from '../api/request'
import FavoriteButton from './FavoriteButton.vue'
import { useI18n } from '../i18n'

const props = defineProps({
  game: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const baseHost = backendOrigin
const { t, isZh } = useI18n()

const detailText = computed(() => {
  if (props.game.description) {
    return props.game.description
  }
  return isZh.value ? '点击进入游戏详情与启动入口' : 'Open the game details and launch entry'
})

const goPlay = () => {
  router.push(`/play/${props.game.id}`)
}
</script>

<style scoped>
.game-card {
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  box-shadow: var(--app-shadow-md);
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.game-card:hover {
  transform: translateY(-4px);
  border-color: rgba(59, 130, 246, 0.34);
  box-shadow: var(--app-shadow-lg);
}

.game-card__media {
  background: linear-gradient(135deg, var(--app-surface-soft), var(--app-surface-hover));
}

.game-card__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.06), rgba(15, 23, 42, 0.76));
}

.game-card__tag {
  background: rgba(15, 23, 42, 0.56);
  color: #fff;
}

.game-card__action {
  border: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--app-primary), var(--app-primary-strong));
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  transition: transform 0.2s ease, filter 0.2s ease;
}

.game-card__action:hover {
  transform: translateY(-1px);
  filter: brightness(1.06);
}

html[data-theme='light'] .game-card__overlay {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.04), rgba(15, 23, 42, 0.66));
}

html[data-theme='light'] .game-card__tag {
  background: rgba(15, 23, 42, 0.68);
}
</style>
