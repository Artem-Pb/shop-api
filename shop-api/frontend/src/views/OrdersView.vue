<template>
  <div :style="pageStyle">

    <div v-if="!store.isAuth" :style="guestStyle">
      <span :style="{ fontFamily: TBF.mono, fontSize: '13px' }">Войдите, чтобы просмотреть заказы.</span>
      <RouterLink to="/" :style="btnLinkStyle">→ LOGIN</RouterLink>
    </div>

    <template v-else>
      <div :style="headStyle">
        <span :style="titleStyle">ORDER HISTORY</span>
        <span :style="{ fontFamily: TBF.mono, fontSize: '11px', color: TB.MUTED }">{{ orders.length }} RECORDS</span>
      </div>

      <div v-if="loading" :style="muteStyle">loading...</div>
      <div v-else-if="orders.length === 0" :style="muteStyle">No orders yet.</div>

      <div v-else>
        <div
          v-for="order in orders" :key="order.id"
          :style="orderBlockStyle"
        >
          <!-- Git log line -->
          <div :style="logLineStyle" @click="toggle(order.id)">
            <span :style="commitHashStyle">{{ hashFromId(order.id) }}</span>
            <span :style="statusBadgeStyle(order.status)">{{ order.status }}</span>
            <span :style="{ fontFamily: TBF.display, fontSize: '13px', fontWeight: '600', flex: '1' }">
              Order #{{ String(order.id).padStart(4,'0') }}
            </span>
            <span :style="{ fontFamily: TBF.mono, fontSize: '13px', fontWeight: '700' }">{{ fmt(order.totalPrice) }}</span>
            <span :style="dateStyle">{{ fmtDate(order.createdAt) }}</span>
            <span :style="{ fontFamily: TBF.mono, fontSize: '12px', color: TB.MUTED }">{{ expanded.has(order.id) ? '▲' : '▼' }}</span>
          </div>

          <!-- Expanded items -->
          <div v-if="expanded.has(order.id)" :style="itemsBlockStyle">
            <div v-if="!order.items || order.items.length === 0" :style="muteStyle">
              No item details available.
            </div>
            <div v-for="item in order.items" :key="item.id" :style="itemRowStyle">
              <span :style="{ color: TB.MUTED }">→</span>
              <RouterLink :to="`/product/${item.productId}`" :style="itemLinkStyle">
                {{ item.productName }}
              </RouterLink>
              <span :style="{ color: TB.MUTED }">×{{ item.quantity }}</span>
              <span :style="{ fontWeight: '700' }">{{ fmt(Number(item.price) * item.quantity) }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useShopStore } from '@/stores/shop'
import { TB, TBF, fmt } from '@/tokens'
import api from '@/api/client'

const store = useShopStore()

const orders   = ref<any[]>([])
const loading  = ref(false)
const expanded = ref(new Set<number>())

function toggle(id: number) {
  if (expanded.value.has(id)) expanded.value.delete(id)
  else expanded.value.add(id)
  expanded.value = new Set(expanded.value)
}

function hashFromId(id: number) {
  return (id * 0xdeadbeef >>> 0).toString(16).slice(0, 7)
}

function fmtDate(raw: string) {
  if (!raw) return ''
  return new Date(raw).toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: '2-digit' })
}

async function load() {
  if (!store.isAuth) return
  loading.value = true
  try {
    const { data } = await api.get('/api/orders')
    orders.value = Array.isArray(data) ? data : (data.content || [])
  } catch { orders.value = [] } finally { loading.value = false }
}

onMounted(load)
watch(() => store.isAuth, (v) => { if (v) load() })

// Styles
const pageStyle = { background: TB.BG, padding: '24px 20px', maxWidth: '900px', margin: '0 auto' }
const guestStyle = { display: 'flex', gap: '16px', alignItems: 'center', padding: '40px 0', fontFamily: TBF.mono }
const btnLinkStyle = {
  border: `2px solid ${TB.INK}`, fontFamily: TBF.mono, fontSize: '12px', fontWeight: '700',
  padding: '8px 14px', color: TB.INK, textDecoration: 'none', letterSpacing: '0.04em',
}
const headStyle = {
  display: 'flex', justifyContent: 'space-between', alignItems: 'center',
  borderBottom: `2px solid ${TB.INK}`, paddingBottom: '10px', marginBottom: '20px',
}
const titleStyle = { fontFamily: TBF.mono, fontSize: '14px', fontWeight: '700', letterSpacing: '0.08em' }
const muteStyle  = { fontFamily: TBF.mono, fontSize: '12px', color: TB.MUTED, padding: '16px 0' }

const orderBlockStyle = {
  border: `2px solid ${TB.INK}`, background: TB.CARD, marginBottom: '8px',
  boxShadow: `2px 2px 0 ${TB.INK}`,
}
const logLineStyle = {
  display: 'flex', gap: '12px', alignItems: 'center', padding: '12px 16px',
  cursor: 'pointer', borderBottom: `1px solid transparent`,
}
const commitHashStyle = {
  fontFamily: TBF.mono, fontSize: '12px', color: TB.MUTED,
  letterSpacing: '0.06em', minWidth: '56px',
}
const statusBadgeStyle = (status: string) => ({
  fontFamily: TBF.mono, fontSize: '10px', fontWeight: '700', letterSpacing: '0.06em',
  padding: '2px 7px',
  background: status === 'COMPLETED' ? TB.GREEN : status === 'CANCELLED' ? TB.RED : TB.ACCENT,
  color:      status === 'COMPLETED' ? TB.CARD  : status === 'CANCELLED' ? TB.CARD : TB.INK,
})
const dateStyle = {
  fontFamily: TBF.mono, fontSize: '11px', color: TB.MUTED, minWidth: '60px', textAlign: 'right' as const,
}
const itemsBlockStyle = {
  borderTop: `1px solid ${TB.INK}`, padding: '12px 16px', background: TB.BG,
}
const itemRowStyle = {
  display: 'flex', gap: '12px', alignItems: 'center',
  fontFamily: TBF.mono, fontSize: '12px', padding: '5px 0',
  borderBottom: `1px dashed ${TB.INK}`,
}
const itemLinkStyle = {
  color: TB.INK, textDecoration: 'none', fontFamily: TBF.display, fontSize: '13px',
  flex: '1',
}
</script>
