<template>
  <footer :style="footerStyle">
    <span>TERMINAL SHOP © {{ year }}</span>
    <span :style="{ color: TB.MUTED }">ALL_RIGHTS_RESERVED // v1.0.0</span>
    <span>BUILD OK</span>
  </footer>
</template>

<script setup lang="ts">
import { TB, TBF } from '@/tokens'

const year = new Date().getFullYear()

const footerStyle = {
  background: TB.INK, color: TB.ACCENT,
  fontFamily: TBF.mono, fontSize: '11px', letterSpacing: '0.05em',
  padding: '10px 16px', display: 'flex', justifyContent: 'space-between',
  borderTop: `2px solid ${TB.INK}`, marginTop: 'auto',
}
</script>
