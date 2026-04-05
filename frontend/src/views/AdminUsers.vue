<template>
  <UserManager />
</template>

<script setup>
import { defineAsyncComponent } from 'vue'

const UserManager = defineAsyncComponent(() => import('../components/UserManager.vue'))
</script>
