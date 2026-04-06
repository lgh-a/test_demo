<template>
  <div class="admin-stats-bar flex flex-wrap items-center justify-between gap-3 rounded-3xl px-4 py-3">
    <div class="flex flex-wrap gap-2 text-sm">
      <slot />
    </div>
    <div v-if="$slots.actions" class="flex items-center gap-2">
      <slot name="actions" />
    </div>
  </div>
</template>
