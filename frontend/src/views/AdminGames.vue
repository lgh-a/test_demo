<template>
  <GameManager />
</template>

<script setup>
import { defineAsyncComponent } from 'vue'

const GameManager = defineAsyncComponent(() => import('../components/GameManager.vue'))
</script>
