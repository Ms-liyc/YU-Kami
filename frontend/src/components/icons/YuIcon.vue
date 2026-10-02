<template>
  <svg
    class="yu-icon"
    :class="[`yu-icon--${size}`]"
    :width="px"
    :height="px"
    viewBox="0 0 24 24"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <path
      v-for="(d, i) in paths"
      :key="i"
      :d="d"
      stroke="currentColor"
      :stroke-width="strokeWidth"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </svg>
</template>

<script setup>
import { computed } from 'vue'
import { YU_ICONS } from './yu-icons.js'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: String, default: 'md' },
  strokeWidth: { type: [Number, String], default: 1.75 }
})

const SIZE_MAP = { xs: 14, sm: 16, md: 20, lg: 24, xl: 32, '2xl': 40 }

const px = computed(() => SIZE_MAP[props.size] || Number(props.size) || 20)
const paths = computed(() => YU_ICONS[props.name] || YU_ICONS.spark)
</script>

<style scoped>
.yu-icon {
  display: inline-block;
  vertical-align: middle;
  flex-shrink: 0;
}
</style>
