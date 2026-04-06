<template>
  <div class="admin-page-card rounded-[28px] p-5">
    <div v-if="title || $slots.actions" class="mb-5 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div v-if="title" class="min-w-0">
        <div v-if="eyebrow" class="admin-card-eyebrow text-xs uppercase tracking-[0.28em]">{{ eyebrow }}</div>
        <h2 class="mt-2 text-2xl font-semibold text-[var(--app-text)]">{{ title }}</h2>
        <p v-if="subtitle" class="admin-muted mt-1 text-sm">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="flex items-center gap-2">
        <slot name="actions" />
      </div>
    </div>

    <slot />
  </div>
</template>

<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>
