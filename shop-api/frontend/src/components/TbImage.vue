<template>
  <div :style="containerStyle">
    <svg width="100%" height="100%" style="position:absolute;inset:0">
      <defs>
        <pattern :id="pid" width="8" height="8" patternUnits="userSpaceOnUse">
          <circle cx="2" cy="2" r="0.8" :fill="tn.dot" opacity="0.55" />
        </pattern>
      </defs>
      <rect width="100%" height="100%" :fill="`url(#${pid})`" />
    </svg>
    <div :style="innerStyle">
      <span :style="labelStyle">{{ label }}.jpg</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { TB, TBF } from '@/tokens'

const TONES = [
  { dot: TB.INK, bg: TB.BG },
  { dot: TB.ACCENT, bg: TB.INK },
  { dot: TB.INK, bg: '#dde5d8' },
  { dot: TB.INK, bg: '#e6dcc8' },
  { dot: TB.INK, bg: '#d6dde3' },
]

const props = withDefaults(defineProps<{ h?: number; label?: string; tone?: number }>(), {
  h: 180, label: 'IMG', tone: 0,
})

const tn  = computed(() => TONES[props.tone % TONES.length])
const pid = computed(() => `tbi-${props.label}-${props.h}-${props.tone}`)

const containerStyle = computed(() => ({
  height: `${props.h}px`, background: tn.value.bg,
  border: `2px solid ${TB.INK}`, position: 'relative' as const, overflow: 'hidden',
}))
const innerStyle = computed(() => ({
  position: 'absolute' as const, inset: '16px',
  border: `1px dashed ${tn.value.dot === TB.ACCENT ? TB.ACCENT : TB.INK}`,
  display: 'flex', alignItems: 'center', justifyContent: 'center', opacity: '0.85',
}))
const labelStyle = computed(() => ({
  fontFamily: TBF.mono, fontSize: '11px',
  background: tn.value.bg, padding: '2px 8px',
  color: tn.value.dot === TB.ACCENT ? TB.ACCENT : TB.INK,
}))
</script>
