<template>
  <div class="flex min-h-[calc(100vh-4rem)] flex-col bg-[#121212] -mx-6 -mt-6">
    <div class="z-10 flex flex-wrap items-center justify-between gap-3 bg-[#1f1f1f] px-4 py-3 shadow-sm">
      <div>
        <h1 v-if="game" class="text-xl font-bold text-gray-100">{{ game.name }}</h1>
        <div v-if="game" class="mt-1 text-xs text-gray-400">{{ game.description }}</div>
      </div>
      <div class="flex gap-2">
        <FavoriteButton v-if="game" :game-id="game.id" />
        <n-button v-if="isPlaying" type="warning" ghost size="small" @click="stopEmulator">Stop</n-button>
        <n-button v-else type="primary" size="small" @click="playGame">Play</n-button>
        <n-button size="small" @click="$router.go(-1)">Back</n-button>
      </div>
    </div>

    <div
      id="emulator-container"
      class="relative flex flex-1 flex-col items-center justify-center overflow-hidden bg-[#0b0b0b] px-4 py-6 sm:px-6"
    >
      <div class="flex w-full max-w-5xl flex-col items-center">
        <div class="relative w-full max-w-[960px] overflow-hidden rounded-2xl border border-white/10 bg-black shadow-[0_20px_60px_rgba(0,0,0,0.45)] aspect-[4/3] sm:aspect-[16/10]">
          <canvas id="emulator-canvas" class="h-full w-full object-contain"></canvas>
        </div>
      </div>

      <div class="mt-6 hidden w-full max-w-2xl select-none items-center justify-between px-8 opacity-80 sm:flex">
        <div class="relative h-32 w-32">
          <button class="absolute left-10 top-0 h-12 w-12 rounded-t border-b-2 border-gray-900 bg-gray-700 active:translate-y-[2px] active:border-b-0 active:bg-gray-500" @pointerdown="triggerKey('ArrowUp', true)" @pointerup="triggerKey('ArrowUp', false)" @pointerleave="triggerKey('ArrowUp', false)">Up</button>
          <button class="absolute bottom-0 left-10 h-12 w-12 rounded-b border-t-2 border-gray-900 bg-gray-700 active:-translate-y-[2px] active:border-t-0 active:bg-gray-500" @pointerdown="triggerKey('ArrowDown', true)" @pointerup="triggerKey('ArrowDown', false)" @pointerleave="triggerKey('ArrowDown', false)">Down</button>
          <button class="absolute left-0 top-10 h-12 w-12 rounded-l border-r-2 border-gray-900 bg-gray-700 active:translate-x-[2px] active:border-r-0 active:bg-gray-500" @pointerdown="triggerKey('ArrowLeft', true)" @pointerup="triggerKey('ArrowLeft', false)" @pointerleave="triggerKey('ArrowLeft', false)">Left</button>
          <button class="absolute right-0 top-10 h-12 w-12 rounded-r border-l-2 border-gray-900 bg-gray-700 active:-translate-x-[2px] active:border-l-0 active:bg-gray-500" @pointerdown="triggerKey('ArrowRight', true)" @pointerup="triggerKey('ArrowRight', false)" @pointerleave="triggerKey('ArrowRight', false)">Right</button>
          <div class="pointer-events-none absolute inset-10 h-12 w-12 rounded-sm border border-gray-900 bg-gray-600"></div>
        </div>

        <div class="flex gap-4 self-end pb-8">
          <button class="h-6 w-16 rounded-full bg-gray-700 text-[10px] font-bold uppercase text-gray-400 shadow-[0_3px_0_#1a1a1a] active:translate-y-[3px] active:shadow-none active:bg-gray-500" @pointerdown="triggerKey('Shift', true)" @pointerup="triggerKey('Shift', false)" @pointerleave="triggerKey('Shift', false)">Select</button>
          <button class="h-6 w-16 rounded-full bg-gray-700 text-[10px] font-bold uppercase text-gray-400 shadow-[0_3px_0_#1a1a1a] active:translate-y-[3px] active:shadow-none active:bg-gray-500" @pointerdown="triggerKey('Enter', true)" @pointerup="triggerKey('Enter', false)" @pointerleave="triggerKey('Enter', false)">Start</button>
        </div>

        <div class="relative flex h-32 w-32 rotate-[-15deg] items-center justify-end gap-2 pr-4">
          <button class="h-14 w-14 rounded-full bg-red-700 text-lg font-bold text-white shadow-[0_4px_0_#5a0000] active:translate-y-[4px] active:shadow-none active:bg-red-500" @pointerdown="triggerKey('z', true)" @pointerup="triggerKey('z', false)" @pointerleave="triggerKey('z', false)">B</button>
          <button class="-mt-8 h-14 w-14 rounded-full bg-red-700 text-lg font-bold text-white shadow-[0_4px_0_#5a0000] active:translate-y-[4px] active:shadow-none active:bg-red-500" @pointerdown="triggerKey('x', true)" @pointerup="triggerKey('x', false)" @pointerleave="triggerKey('x', false)">A</button>
        </div>
      </div>

      <div class="mt-4 flex flex-col items-center gap-1 text-xs text-gray-400">
        <p class="sm:hidden">Rotate to landscape if the controls do not fit on screen.</p>
        <p class="hidden sm:block">
          Keyboard: Arrow keys to move, X for A, Z for B, Shift for Select, Enter for Start.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, useMessage } from 'naive-ui'
import { backendOrigin, request } from '../api/request'
import { useAppStore } from '../store'
import FavoriteButton from '../components/FavoriteButton.vue'

const route = useRoute()
const router = useRouter()
const store = useAppStore()
const message = useMessage()
const game = ref(null)
const isPlaying = ref(false)
let emulatorInstance = null

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
    message.error('Failed to start the emulator')
  }
}

const stopEmulator = () => {
  if (emulatorInstance) {
    emulatorInstance.exit()
    emulatorInstance = null
    isPlaying.value = false
  }
}

const triggerKey = (key, isDown) => {
  const eventType = isDown ? 'keydown' : 'keyup'
  document.dispatchEvent(new KeyboardEvent(eventType, { key, code: key, bubbles: true, cancelable: true }))
}

const playGame = () => {
  if (!game.value?.romUrl) return

  stopEmulator()

  const oldCanvas = document.getElementById('emulator-canvas')
  const canvasStage = oldCanvas?.parentElement
  if (canvasStage && oldCanvas) {
    oldCanvas.remove()
    const newCanvas = document.createElement('canvas')
    newCanvas.id = 'emulator-canvas'
    newCanvas.className = 'h-full w-full object-contain'
    canvasStage.appendChild(newCanvas)
  }

  let coreName = 'FC'
  if (game.value.consoleId === 2) coreName = 'GBA'
  if (game.value.consoleId === 3) coreName = 'SFC'

  startEmulator(game.value.romUrl, coreName)
}

onMounted(async () => {
  try {
    game.value = await request(`/games/${route.params.id}`)
    if (store.isLoggedIn) {
      try {
        await request(`/history/games/${route.params.id}`, { method: 'POST' })
        await Promise.all([store.fetchRecentHistory(), store.fetchFavoriteIds()])
      } catch (error) {
        console.error('Failed to update user game state', error)
      }
    }
    playGame()
  } catch (error) {
    console.error('Failed to load game detail', error)
    message.error(error.message || 'ROM file is missing or unavailable')
    router.push('/')
  }
})

onUnmounted(() => {
  stopEmulator()
})
</script>
