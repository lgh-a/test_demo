<template>
  <div class="game-play-page -mx-6 -mt-6 flex min-h-[calc(100vh-4rem)] flex-col">
    <div class="game-play-toolbar z-10 flex flex-wrap items-center justify-between gap-3 px-4 py-3 shadow-sm">
      <div>
        <h1 v-if="game" class="game-play-title text-xl font-bold">{{ game.name }}</h1>
        <div v-if="game" class="game-play-description mt-1 text-xs">{{ game.description }}</div>
      </div>
      <div class="flex gap-2">
        <FavoriteButton v-if="game" :game-id="game.id" />
        <n-button v-if="isPlaying" type="warning" ghost size="small" @click="stopEmulator">{{ t('game.stop') }}</n-button>
        <n-button v-else type="primary" size="small" @click="playGame">{{ t('game.play') }}</n-button>
        <n-button size="small" @click="$router.go(-1)">{{ t('common.back') }}</n-button>
      </div>
    </div>

    <div
      id="emulator-container"
      class="game-play-stage relative flex flex-1 flex-col items-center justify-center overflow-hidden px-4 py-6 sm:px-6"
    >
      <div class="flex w-full max-w-5xl flex-col items-center">
        <div class="game-play-canvas-shell relative aspect-[4/3] w-full max-w-[960px] overflow-hidden rounded-2xl sm:aspect-[16/10]">
          <canvas :key="canvasRenderKey" id="emulator-canvas" class="h-full w-full object-contain"></canvas>
        </div>
      </div>

      <div class="game-play-controller mt-6 hidden w-full max-w-2xl select-none items-center justify-between px-8 sm:flex">
        <div class="relative h-32 w-32">
          <button class="controller-button absolute left-10 top-0 h-12 w-12 rounded-t border-b-2" @pointerdown="triggerKey('ArrowUp', true)" @pointerup="triggerKey('ArrowUp', false)" @pointerleave="triggerKey('ArrowUp', false)">Up</button>
          <button class="controller-button absolute bottom-0 left-10 h-12 w-12 rounded-b border-t-2" @pointerdown="triggerKey('ArrowDown', true)" @pointerup="triggerKey('ArrowDown', false)" @pointerleave="triggerKey('ArrowDown', false)">Down</button>
          <button class="controller-button absolute left-0 top-10 h-12 w-12 rounded-l border-r-2" @pointerdown="triggerKey('ArrowLeft', true)" @pointerup="triggerKey('ArrowLeft', false)" @pointerleave="triggerKey('ArrowLeft', false)">Left</button>
          <button class="controller-button absolute right-0 top-10 h-12 w-12 rounded-r border-l-2" @pointerdown="triggerKey('ArrowRight', true)" @pointerup="triggerKey('ArrowRight', false)" @pointerleave="triggerKey('ArrowRight', false)">Right</button>
          <div class="controller-pad-center pointer-events-none absolute inset-10 h-12 w-12 rounded-sm border"></div>
        </div>

        <div class="flex gap-4 self-end pb-8">
          <button class="controller-button controller-button--meta h-6 w-16 rounded-full text-[10px] font-bold uppercase" @pointerdown="triggerKey('Shift', true)" @pointerup="triggerKey('Shift', false)" @pointerleave="triggerKey('Shift', false)">Select</button>
          <button class="controller-button controller-button--meta h-6 w-16 rounded-full text-[10px] font-bold uppercase" @pointerdown="triggerKey('Enter', true)" @pointerup="triggerKey('Enter', false)" @pointerleave="triggerKey('Enter', false)">Start</button>
        </div>

        <div class="relative flex h-32 w-32 rotate-[-15deg] items-center justify-end gap-2 pr-4">
          <button class="controller-button controller-button--action h-14 w-14 rounded-full text-lg font-bold" @pointerdown="triggerKey('z', true)" @pointerup="triggerKey('z', false)" @pointerleave="triggerKey('z', false)">B</button>
          <button class="controller-button controller-button--action -mt-8 h-14 w-14 rounded-full text-lg font-bold" @pointerdown="triggerKey('x', true)" @pointerup="triggerKey('x', false)" @pointerleave="triggerKey('x', false)">A</button>
        </div>
      </div>

      <div class="game-play-hints mt-4 flex flex-col items-center gap-1 text-xs">
        <p class="sm:hidden">{{ t('game.rotateHint') }}</p>
        <p class="hidden sm:block">
          {{ t('game.keyboardHint') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, useMessage } from 'naive-ui'
import { backendOrigin, request } from '../api/request'
import { useAppStore } from '../store'
import FavoriteButton from '../components/FavoriteButton.vue'
import { useI18n } from '../i18n'

const route = useRoute()
const router = useRouter()
const store = useAppStore()
const message = useMessage()
const { t } = useI18n()
const game = ref(null)
const isPlaying = ref(false)
const canvasRenderKey = ref(0)
let emulatorInstance = null

const resetPagePosition = () => {
  window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
}

const startEmulator = async (romPath, consoleName) => {
  const systemMap = { FC: 'nes', GBA: 'gba', SFC: 'snes' }
  const system = systemMap[consoleName] || 'nes'
  try {
    const { Nostalgist } = await import('nostalgist')
    emulatorInstance = await Nostalgist[system]({
      rom: romPath,
      resolveRom(file) {
        return `${backendOrigin}${file}`
      },
      element: document.getElementById('emulator-canvas')
    })
    isPlaying.value = true
  } catch (error) {
    console.error('Failed to start emulator', error)
    message.error(t('game.emulatorStartFailed'))
  }
}

const stopEmulator = ({ resetCanvas = false } = {}) => {
  if (emulatorInstance) {
    emulatorInstance.exit({ removeCanvas: false })
    emulatorInstance = null
  }
  isPlaying.value = false
  if (resetCanvas) {
    canvasRenderKey.value += 1
  }
}

const triggerKey = (key, isDown) => {
  const eventType = isDown ? 'keydown' : 'keyup'
  document.dispatchEvent(new KeyboardEvent(eventType, { key, code: key, bubbles: true, cancelable: true }))
}

const playGame = async () => {
  if (!game.value?.romUrl) return

  stopEmulator({ resetCanvas: true })
  await nextTick()

  let coreName = 'FC'
  if (game.value.consoleId === 2) coreName = 'GBA'
  if (game.value.consoleId === 3) coreName = 'SFC'

  await startEmulator(game.value.romUrl, coreName)
}

onMounted(async () => {
  try {
    resetPagePosition()
    game.value = await request(`/games/${route.params.id}`)
    if (store.isLoggedIn) {
      try {
        await request(`/history/games/${route.params.id}`, { method: 'POST' })
        await Promise.all([store.fetchRecentHistory(), store.fetchFavoriteIds()])
      } catch (error) {
        console.error(t('game.gameStateUpdateFailed'), error)
      }
    }
    await playGame()
    await nextTick()
    resetPagePosition()
  } catch (error) {
    console.error('Failed to load game detail', error)
    message.error(error.message || t('game.gameLoadFailed'))
    router.push('/')
  }
})

onUnmounted(() => {
  stopEmulator()
})
</script>

<style scoped>
.game-play-page {
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--app-primary) 22%, transparent), transparent 34%),
    linear-gradient(180deg, color-mix(in srgb, var(--app-bg) 92%, #020617 8%), color-mix(in srgb, var(--app-bg) 96%, #000 4%));
}

.game-play-toolbar {
  border-bottom: 1px solid var(--app-border);
  background: color-mix(in srgb, var(--app-surface) 88%, transparent);
  backdrop-filter: blur(18px);
}

.game-play-title {
  color: var(--app-text);
}

.game-play-description,
.game-play-hints {
  color: var(--app-text-muted);
}

.game-play-stage {
  background:
    radial-gradient(circle at center, color-mix(in srgb, var(--app-primary) 16%, transparent), transparent 42%),
    linear-gradient(180deg, color-mix(in srgb, var(--app-bg) 88%, #020617 12%), color-mix(in srgb, var(--app-bg) 94%, #000 6%));
}

.game-play-canvas-shell {
  border: 1px solid color-mix(in srgb, var(--app-border) 92%, transparent);
  background: #000;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.32);
}

.game-play-controller {
  opacity: 0.88;
}

.controller-button {
  border-color: color-mix(in srgb, var(--app-border) 88%, rgba(15, 23, 42, 0.55));
  background: color-mix(in srgb, var(--app-surface-hover) 74%, var(--app-surface));
  color: var(--app-text);
  box-shadow: 0 3px 0 color-mix(in srgb, var(--app-border) 88%, rgba(2, 6, 23, 0.8));
}

.controller-button:active {
  box-shadow: none;
}

.controller-button--meta {
  color: var(--app-text-muted);
}

.controller-button--action {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(180deg, #ef4444, #b91c1c);
  color: #fff;
  box-shadow: 0 4px 0 #7f1d1d;
}

.controller-pad-center {
  border-color: color-mix(in srgb, var(--app-border) 88%, rgba(15, 23, 42, 0.55));
  background: color-mix(in srgb, var(--app-surface-hover) 78%, var(--app-surface-soft));
}

html[data-theme='light'] .game-play-page {
  background:
    radial-gradient(circle at top, rgba(37, 99, 235, 0.14), transparent 34%),
    linear-gradient(180deg, #eef4ff, #e2e8f0);
}

html[data-theme='light'] .game-play-stage {
  background:
    radial-gradient(circle at center, rgba(37, 99, 235, 0.1), transparent 42%),
    linear-gradient(180deg, #e2e8f0, #cbd5e1);
}

html[data-theme='light'] .game-play-toolbar {
  background: rgba(255, 255, 255, 0.84);
}
</style>
