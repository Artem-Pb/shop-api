<template>
  <Transition name="toast">
    <div v-if="msg" :style="toastStyle">{{ msg }}</div>
  </Transition>
</template>

<script setup lang="ts">
import { TB, TBF } from '@/tokens'

defineProps<{ msg: string | null }>()

const toastStyle = {
  position: 'fixed' as const, bottom: '24px', right: '24px', zIndex: '999',
  background: TB.INK, color: TB.ACCENT,
  fontFamily: TBF.mono, fontSize: '12px', fontWeight: '700', letterSpacing: '0.04em',
  padding: '10px 18px', border: `2px solid ${TB.ACCENT}`,
  boxShadow: `4px 4px 0 ${TB.ACCENT}`,
}
</script>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: opacity 0.2s, transform 0.2s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateY(8px); }
</style>
