<template>
  <button :style="s" :disabled="disabled" @click="$emit('click')"><slot /></button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { TB, TBF } from '@/tokens'

const props = withDefaults(
  defineProps<{ primary?: boolean; danger?: boolean; fullWidth?: boolean; disabled?: boolean }>(),
  {}
)
defineEmits(['click'])

const s = computed(() => ({
  background: props.danger ? TB.RED : props.primary ? TB.INK : TB.CARD,
  color:      props.danger ? TB.CARD : props.primary ? TB.ACCENT : TB.INK,
  border: `2px solid ${TB.INK}`,
  padding: '10px 16px', fontFamily: TBF.mono, fontSize: '12px', fontWeight: '700',
  letterSpacing: '0.04em', cursor: props.disabled ? 'not-allowed' : 'pointer',
  whiteSpace: 'nowrap' as const, width: props.fullWidth ? '100%' : 'auto',
  opacity: props.disabled ? '0.6' : '1',
}))
</script>
