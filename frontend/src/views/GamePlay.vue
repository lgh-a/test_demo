<template>
  <div class="flex flex-col h-[calc(100vh-4rem)] -mx-6 -mt-6 bg-[#121212]">
    <div class="p-4 flex items-center justify-between bg-[#1f1f1f] shadow-sm z-10">
      <div>
        <h1 class="text-xl font-bold text-gray-100" v-if="game">{{ game.name }}</h1>
        <div class="text-gray-400 text-xs mt-1" v-if="game">{{ game.description }}</div>
      </div>
      <div class="flex gap-2">
        <n-button v-if="isPlaying" type="warning" ghost size="small" @click="stopEmulator">停止游戏</n-button>
        <n-button v-else type="primary" size="small" @click="playGame">开始游戏</n-button>
        <n-button size="small" @click="$router.go(-1)">返回列表</n-button>
      </div>
    </div>

    <div class="flex-1 w-full bg-black flex flex-col items-center justify-center relative overflow-hidden" id="emulator-container">
      <canvas id="emulator-canvas" class="w-full h-full object-contain max-h-[70vh]"></canvas>
      
      <!-- Virtual Joypad for Touch / Mouse -->
      <div class="w-full max-w-2xl px-8 flex justify-between items-center mt-6 select-none opacity-80 z-10 hidden sm:flex">
        <!-- D-Pad -->
        <div class="relative w-32 h-32">
          <button class="absolute top-0 left-10 w-12 h-12 bg-gray-700 rounded-t border-b-2 border-gray-900 active:bg-gray-500 active:border-b-0 active:translate-y-[2px]" @pointerdown="triggerKey('ArrowUp', true)" @pointerup="triggerKey('ArrowUp', false)" @pointerleave="triggerKey('ArrowUp', false)">▲</button>
          <button class="absolute bottom-0 left-10 w-12 h-12 bg-gray-700 rounded-b border-t-2 border-gray-900 active:bg-gray-500 active:border-t-0 active:-translate-y-[2px]" @pointerdown="triggerKey('ArrowDown', true)" @pointerup="triggerKey('ArrowDown', false)" @pointerleave="triggerKey('ArrowDown', false)">▼</button>
          <button class="absolute left-0 top-10 w-12 h-12 bg-gray-700 rounded-l border-r-2 border-gray-900 active:bg-gray-500 active:border-r-0 active:translate-x-[2px]" @pointerdown="triggerKey('ArrowLeft', true)" @pointerup="triggerKey('ArrowLeft', false)" @pointerleave="triggerKey('ArrowLeft', false)">◀</button>
          <button class="absolute right-0 top-10 w-12 h-12 bg-gray-700 rounded-r border-l-2 border-gray-900 active:bg-gray-500 active:border-l-0 active:-translate-x-[2px]" @pointerdown="triggerKey('ArrowRight', true)" @pointerup="triggerKey('ArrowRight', false)" @pointerleave="triggerKey('ArrowRight', false)">▶</button>
          <div class="absolute inset-10 bg-gray-600 w-12 h-12 rounded-sm border border-gray-900 pointer-events-none"></div>
        </div>

        <!-- Start / Select -->
        <div class="flex gap-4 self-end pb-8">
          <button class="w-16 h-6 bg-gray-700 rounded-full text-[10px] uppercase font-bold text-gray-400 active:bg-gray-500 shadow-[0_3px_0_#1a1a1a] active:shadow-none active:translate-y-[3px]" @pointerdown="triggerKey('Shift', true)" @pointerup="triggerKey('Shift', false)" @pointerleave="triggerKey('Shift', false)">Select</button>
          <button class="w-16 h-6 bg-gray-700 rounded-full text-[10px] uppercase font-bold text-gray-400 active:bg-gray-500 shadow-[0_3px_0_#1a1a1a] active:shadow-none active:translate-y-[3px]" @pointerdown="triggerKey('Enter', true)" @pointerup="triggerKey('Enter', false)" @pointerleave="triggerKey('Enter', false)">Start</button>
        </div>

        <!-- Action Buttons -->
        <div class="relative w-32 h-32 flex items-center justify-end gap-2 pr-4 rotate-[-15deg]">
          <button class="w-14 h-14 bg-red-700 rounded-full text-white font-bold text-lg active:bg-red-500 shadow-[0_4px_0_#5a0000] active:shadow-none active:translate-y-[4px]" @pointerdown="triggerKey('z', true)" @pointerup="triggerKey('z', false)" @pointerleave="triggerKey('z', false)">B</button>
          <button class="w-14 h-14 bg-red-700 rounded-full text-white font-bold text-lg active:bg-red-500 shadow-[0_4px_0_#5a0000] active:shadow-none active:translate-y-[4px] -mt-8" @pointerdown="triggerKey('x', true)" @pointerup="triggerKey('x', false)" @pointerleave="triggerKey('x', false)">A</button>
        </div>
      </div>
      
      <!-- Mobile/Keyboard Notice -->
      <div class="text-gray-400 text-xs mt-4 flex flex-col items-center gap-1">
        <p class="sm:hidden">如果由于屏幕过小导致按键显示不全，建议横屏游玩。</p>
        <p class="hidden sm:block">
          <span class="mr-3">🎮 <strong>键盘操作指南:</strong></span>
          <span class="mr-3">方向键: 上下左右</span>
          <span class="mr-3">A键(跳跃等): <kbd class="bg-gray-700 px-1 rounded">X</kbd></span>
          <span class="mr-3">B键(攻击等): <kbd class="bg-gray-700 px-1 rounded">Z</kbd></span>
          <span class="mr-3">Select: <kbd class="bg-gray-700 px-1 rounded">Shift</kbd></span>
          <span>Start: <kbd class="bg-gray-700 px-1 rounded">Enter</kbd></span>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { request } from '../api/request'
import { NButton } from 'naive-ui'
import { Nostalgist } from 'nostalgist'

const route = useRoute()
const game = ref(null)
const isPlaying = ref(false)
let emulatorInstance = null

const startEmulator = async (romUrl, consoleName) => {
  const coreMap = {
    'FC': 'fceumm',
    'GBA': 'mgba',
    'SFC': 'snes9x'
  }
  const core = coreMap[consoleName] || 'fceumm'
  
  try {
    emulatorInstance = await Nostalgist.launch({
      core: core,
      rom: romUrl,
      element: document.getElementById('emulator-canvas')
    })
    isPlaying.value = true
  } catch (err) {
    console.error('Failed to start emulator', err)
  }
}

const stopEmulator = () => {
  if (emulatorInstance) {
    emulatorInstance.exit()
    emulatorInstance = null
    isPlaying.value = false
  }
}

// Simulate keyboard events for the virtual joypad
const triggerKey = (key, isDown) => {
  const eventType = isDown ? 'keydown' : 'keyup'
  const event = new KeyboardEvent(eventType, {
    key: key,
    code: key,
    bubbles: true,
    cancelable: true,
  })
  document.dispatchEvent(event)
}

const playGame = () => {
  if (!game.value || !game.value.romUrl) return
  
  // Recreate canvas to clear any modifications made by previous Nostalgist instances
  const container = document.getElementById('emulator-container')
  if (container) {
    const oldCanvas = document.getElementById('emulator-canvas')
    if (oldCanvas) oldCanvas.remove()
    const newCanvas = document.createElement('canvas')
    newCanvas.id = 'emulator-canvas'
    newCanvas.className = 'w-full h-full object-contain max-h-[70vh]'
    container.insertBefore(newCanvas, container.firstChild)
  }

  let coreName = 'FC'
  if (game.value.consoleId === 2) coreName = 'GBA'
  if (game.value.consoleId === 3) coreName = 'SFC'
  
  const encodedPath = game.value.romUrl.split('/').map(segment => encodeURIComponent(segment)).join('/')
  const url = 'http://localhost:8080' + encodedPath + '?t=' + Date.now()
  startEmulator(url, coreName)
}

onMounted(async () => {
  game.value = await request(`/games/${route.params.id}`)
  playGame()
})

onUnmounted(() => {
  stopEmulator()
})
</script>
