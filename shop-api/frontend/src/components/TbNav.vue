<template>
  <nav :style="navStyle">
    <RouterLink
      v-for="link in links.filter(l => !l.adminOnly || store.isAdmin)"
      :key="link.to" :to="link.to" custom v-slot="{ isActive, navigate }"
    >
      <button @click="navigate" :style="itemStyle(isActive, link.adminOnly)">{{ link.label }}</button>
    </RouterLink>
    <div style="flex:1" />
    <div v-if="store.isAuth" :style="userAreaStyle">
      <span :style="emailStyle">{{ store.userEmail }}</span>
      <button :style="logoutStyle" @click="store.logout()">[ EXIT ]</button>
    </div>
    <div v-else :style="userAreaStyle">
      <span :style="{ fontFamily: TBF.mono, fontSize: '11px', color: TB.MUTED }">GUEST</span>
    </div>
    <RouterLink to="/cart" custom v-slot="{ isActive, navigate }">
      <button @click="navigate" :style="cartStyle(isActive)">
        CART [{{ store.cartCount }}]
      </button>
    </RouterLink>
  </nav>
</template>

<script setup lang="ts">
import { useShopStore } from '@/stores/shop'
import { TB, TBF } from '@/tokens'

const store = useShopStore()

const links = [
  { to: '/',        label: '[ HOME ]',    adminOnly: false },
  { to: '/catalog', label: '[ CATALOG ]', adminOnly: false },
  { to: '/orders',  label: '[ ORDERS ]',  adminOnly: false },
  { to: '/admin',   label: '[ ADMIN ]',   adminOnly: true  },
]

const navStyle = {
  background: TB.BG, borderBottom: `2px solid ${TB.INK}`,
  display: 'flex', alignItems: 'stretch', gap: '0',
}

const itemStyle = (active: boolean, adminOnly = false) => ({
  background: active ? (adminOnly ? TB.ACCENT : TB.INK) : 'transparent',
  color:      active ? (adminOnly ? TB.INK    : TB.ACCENT) : (adminOnly ? TB.RED : TB.INK),
  border: 'none', borderRight: `1px solid ${TB.INK}`,
  fontFamily: TBF.mono, fontSize: '12px', fontWeight: '700',
  letterSpacing: '0.04em', padding: '12px 18px', cursor: 'pointer',
})

const userAreaStyle = {
  display: 'flex', alignItems: 'center', gap: '10px', padding: '0 16px',
  borderLeft: `1px solid ${TB.INK}`,
}

const emailStyle = {
  fontFamily: TBF.mono, fontSize: '11px', color: TB.MUTED, letterSpacing: '0.03em',
}

const logoutStyle = {
  background: 'transparent', border: 'none',
  fontFamily: TBF.mono, fontSize: '11px', color: TB.RED, cursor: 'pointer', fontWeight: '700',
}

const cartStyle = (active: boolean) => ({
  background: active ? TB.ACCENT : TB.INK,
  color:      active ? TB.INK    : TB.ACCENT,
  border: 'none', fontFamily: TBF.mono, fontSize: '12px',
  fontWeight: '700', letterSpacing: '0.05em', padding: '12px 20px', cursor: 'pointer',
})
</script>
