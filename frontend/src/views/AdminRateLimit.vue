<template>
  <RateLimitRuleManager />
</template>

<script setup>
import { defineAsyncComponent } from 'vue'

const RateLimitRuleManager = defineAsyncComponent(() => import('../components/RateLimitRuleManager.vue'))
</script>
