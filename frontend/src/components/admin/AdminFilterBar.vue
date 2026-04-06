<template>
  <div class="admin-filter-bar rounded-2xl px-4 py-4">
    <div class="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
      <div class="min-w-0 flex-1">
        <slot />
      </div>
      <div v-if="$slots.actions" class="admin-filter-bar__actions flex flex-wrap justify-end gap-2">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>
