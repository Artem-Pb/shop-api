<template>
  <div :style="barStyle">
    <span :style="labelStyle">TERMINAL SHOP // {{ path }}</span>
    <span :style="timeStyle">{{ time }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { TB, TBF } from '@/tokens'

defineProps<{ path: string }>()

const time = ref('')
let timer: ReturnType<typeof setInterval>

function tick() {
  time.value = new Date().toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

onMounted(() => { tick(); timer = setInterval(tick, 1000) })
onUnmounted(() => clearInterval(timer))

const barStyle = {
  background: TB.INK, color: TB.ACCENT,
  fontFamily: TBF.mono, fontSize: '11px', letterSpacing: '0.06em',
  padding: '6px 16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center',
}
const labelStyle = { fontWeight: '700' }
const timeStyle  = { opacity: '0.7' }
</script>
