<template>
  <MenuManager />
</template>

<script setup>
import { defineAsyncComponent } from 'vue'

const MenuManager = defineAsyncComponent(() => import('../components/MenuManager.vue'))
</script>
