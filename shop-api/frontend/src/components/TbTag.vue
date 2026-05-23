<template>
  <span :style="s"><slot /></span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { TB, TBF } from '@/tokens'

const props = withDefaults(defineProps<{ bg?: string; color?: string }>(), {
  bg: () => TB.INK, color: () => TB.BG,
})
const s = computed(() => ({
  background: props.bg, color: props.color,
  padding: '3px 7px', fontFamily: TBF.mono, fontSize: '10px',
  letterSpacing: '0.05em', textTransform: 'uppercase' as const,
  display: 'inline-block', lineHeight: '1.2',
}))
</script>
