<template>
  <div class="flex min-h-[calc(100vh-8rem)] items-center justify-center">
    <div class="w-full max-w-xl rounded-3xl border border-white/10 bg-[#1f1f24] p-10 text-center shadow-2xl">
      <div class="mb-3 text-sm uppercase tracking-[0.35em] text-gray-400">404</div>
      <h1 class="mb-4 text-3xl font-bold text-white">Page not found</h1>
      <p class="mb-8 text-sm leading-7 text-gray-300">
        The page you requested does not exist or the link is no longer valid.
      </p>

      <n-button type="primary" @click="$router.push('/')">Back Home</n-button>
    </div>
  </div>
</template>

<script setup>
import { NButton } from 'naive-ui'
</script>
