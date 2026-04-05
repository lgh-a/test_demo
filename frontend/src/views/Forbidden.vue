<template>
  <div class="flex min-h-[calc(100vh-8rem)] items-center justify-center">
    <div class="w-full max-w-xl rounded-3xl border border-red-500/20 bg-[#1f1f24] p-10 text-center shadow-2xl">
      <div class="mb-3 text-sm uppercase tracking-[0.35em] text-red-300">403</div>
      <h1 class="mb-4 text-3xl font-bold text-white">Access denied</h1>
      <p class="mb-8 text-sm leading-7 text-gray-300">
        Your account is logged in, but it does not have permission to open this page.
      </p>

      <div class="flex justify-center gap-3">
        <n-button @click="$router.push('/')">Back Home</n-button>
        <n-button type="primary" @click="$router.push('/admin')">Retry Admin</n-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { NButton } from 'naive-ui'
</script>
