<template>
  <div class="w-full min-w-0 space-y-6">
    <CategoryManager v-if="store.hasPerm('series:manage:list')" mode="series" />
  </div>
</template>

<script setup>
import { defineAsyncComponent } from 'vue'
import { useAppStore } from '../store'

const CategoryManager = defineAsyncComponent(() => import('../components/CategoryManager.vue'))
const store = useAppStore()
</script>
