<template>
  <div class="flex min-h-[calc(100vh-8rem)] items-center justify-center">
    <div class="w-full max-w-xl rounded-3xl border border-white/10 bg-[#1f1f24] p-10 text-center shadow-2xl">
      <div class="mb-3 text-sm uppercase tracking-[0.35em] text-gray-400">404</div>
      <h1 class="mb-4 text-3xl font-bold text-white">{{ t('admin.notFound') }}</h1>
      <p class="mb-8 text-sm leading-7 text-gray-300">
        {{ t('admin.notFoundDesc') }}
      </p>

      <n-button type="primary" @click="$router.push('/')">{{ t('common.backHome') }}</n-button>
    </div>
  </div>
</template>

<script setup>
import { NButton } from 'naive-ui'
import { useI18n } from '../i18n'

const { t } = useI18n()
</script>
