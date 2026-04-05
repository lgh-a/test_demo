<template>
  <RoleManager />
</template>

<script setup>
import { defineAsyncComponent } from 'vue'

const RoleManager = defineAsyncComponent(() => import('../components/RoleManager.vue'))
</script>
