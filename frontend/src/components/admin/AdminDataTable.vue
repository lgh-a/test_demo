<template>
  <n-data-table
    v-bind="attrs"
    :scrollbar-props="mergedScrollbarProps"
  />
</template>

<script setup>
import { computed, useAttrs } from 'vue'
import { NDataTable } from 'naive-ui'

defineOptions({
  inheritAttrs: false
})

const props = defineProps({
  scrollbarProps: {
    type: Object,
    default: () => ({})
  }
})

const attrs = useAttrs()

const mergedScrollbarProps = computed(() => ({
  trigger: 'none',
  xPlacement: 'bottom',
  ...props.scrollbarProps
}))
</script>
