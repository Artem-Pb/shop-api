<template>
  <div :style="pageStyle" v-if="product">

    <div :style="breadcrumbStyle">
      <RouterLink to="/catalog" :style="linkStyle">CATALOG</RouterLink>
      <span :style="{ color: TB.MUTED }"> / </span>
      <span>{{ product.categoryName }}</span>
      <span :style="{ color: TB.MUTED }"> / </span>
      <span>SKU_{{ String(product.id).padStart(3, '0') }}</span>
    </div>

    <div :style="mainGridStyle">

      <!-- Image -->
      <div>
        <TbImage :h="320" :label="`p${product.id}`" :tone="(product.id - 1) % 5" />
      </div>

      <!-- Info -->
      <div :style="infoStyle">
        <div :style="{ display:'flex', gap:'8px', marginBottom:'12px', flexWrap:'wrap' }">
          <TbTag>{{ product.categoryName }}</TbTag>
          <TbTag v-if="product.stock > 0" :bg="TB.GREEN" :color="TB.CARD">IN STOCK ({{ product.stock }})</TbTag>
          <TbTag v-else :bg="TB.RED" :color="TB.CARD">OUT OF STOCK</TbTag>
        </div>

        <div :style="{ fontFamily: TBF.mono, fontSize: '11px', color: TB.MUTED, marginBottom: '6px' }">
          SKU_{{ String(product.id).padStart(3, '0') }}
        </div>
        <h1 :style="h1Style">{{ product.name }}</h1>
        <div :style="priceStyle">{{ fmt(product.price) }}</div>

        <p :style="descStyle">{{ product.description }}</p>

        <!-- Qty + Add -->
        <div :style="{ display:'flex', gap:'0', marginBottom:'16px' }">
          <button :style="qtyBtnStyle" @click="qty > 1 && qty--">−</button>
          <div :style="qtyValStyle">{{ qty }}</div>
          <button :style="qtyBtnStyle" @click="qty++">+</button>
        </div>
        <TbButton primary fullWidth :disabled="product.stock === 0 || adding" @click="addToCart">
          {{ adding ? '...' : '> ADD TO CART' }}
        </TbButton>
      </div>
    </div>

    <!-- Tabs -->
    <div :style="tabBarStyle">
      <button
        v-for="t in tabs" :key="t"
        :style="tabBtnStyle(activeTab === t)"
        @click="activeTab = t"
      >{{ t }}</button>
    </div>

    <div :style="tabContentStyle">
      <div v-if="activeTab === 'DESCRIPTION'" :style="{ fontFamily: TBF.display, fontSize: '14px', lineHeight: '1.7' }">
        {{ product.description || 'No description available.' }}
      </div>
      <div v-if="activeTab === 'SPECS'" :style="specsStyle">
        <div :style="specRowStyle">
          <span :style="specKeyStyle">CATEGORY</span>
          <span>{{ product.categoryName }}</span>
        </div>
        <div :style="specRowStyle">
          <span :style="specKeyStyle">SKU</span>
          <span>{{ String(product.id).padStart(3, '0') }}</span>
        </div>
        <div :style="specRowStyle">
          <span :style="specKeyStyle">STOCK</span>
          <span>{{ product.stock }}</span>
        </div>
        <div :style="specRowStyle">
          <span :style="specKeyStyle">PRICE</span>
          <span>{{ fmt(product.price) }}</span>
        </div>
      </div>
    </div>
  </div>

  <div v-else-if="loading" :style="muteStyle">loading...</div>
  <div v-else :style="muteStyle">Product not found.</div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useShopStore } from '@/stores/shop'
import { TB, TBF, fmt } from '@/tokens'
import TbImage  from '@/components/TbImage.vue'
import TbTag    from '@/components/TbTag.vue'
import TbButton from '@/components/TbButton.vue'
import api      from '@/api/client'

const route = useRoute()
const store  = useShopStore()

const product   = ref<any>(null)
const loading   = ref(true)
const qty       = ref(1)
const adding    = ref(false)
const activeTab = ref('DESCRIPTION')
const tabs      = ['DESCRIPTION', 'SPECS']

async function load(id: string | string[]) {
  loading.value = true
  try {
    const { data } = await api.get(`/api/products/${id}`)
    product.value = data
  } catch { product.value = null } finally { loading.value = false }
}

async function addToCart() {
  if (!product.value) return
  adding.value = true
  await store.addToCart(product.value.id, qty.value)
  adding.value = false
}

onMounted(() => load(route.params.id))
watch(() => route.params.id, (id) => { qty.value = 1; load(id) })

// Styles
const pageStyle = { background: TB.BG, padding: '24px 20px', maxWidth: '1000px', margin: '0 auto' }
const breadcrumbStyle = {
  fontFamily: TBF.mono, fontSize: '11px', letterSpacing: '0.04em',
  marginBottom: '20px', color: TB.INK,
}
const linkStyle = { color: TB.INK, textDecoration: 'none' }
const mainGridStyle = {
  display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px', marginBottom: '32px',
}
const infoStyle = { display: 'flex', flexDirection: 'column' as const }
const h1Style   = {
  fontFamily: TBF.display, fontSize: '22px', fontWeight: '700',
  margin: '0 0 12px', lineHeight: '1.2',
}
const priceStyle = {
  fontFamily: TBF.mono, fontSize: '24px', fontWeight: '700',
  marginBottom: '16px', letterSpacing: '0.03em',
}
const descStyle = {
  fontFamily: TBF.display, fontSize: '14px', color: TB.MUTED,
  lineHeight: '1.6', marginBottom: '20px', flexGrow: '1',
}
const qtyBtnStyle = {
  background: TB.INK, color: TB.CARD,
  border: 'none', width: '36px', height: '36px',
  fontFamily: TBF.mono, fontSize: '18px', cursor: 'pointer', fontWeight: '700',
}
const qtyValStyle = {
  width: '48px', height: '36px', border: `2px solid ${TB.INK}`, borderLeft: 'none', borderRight: 'none',
  display: 'flex', alignItems: 'center', justifyContent: 'center',
  fontFamily: TBF.mono, fontSize: '14px', fontWeight: '700', background: TB.CARD,
}
const tabBarStyle = {
  display: 'flex', borderBottom: `2px solid ${TB.INK}`, marginBottom: '20px',
}
const tabBtnStyle = (active: boolean) => ({
  background: active ? TB.INK : 'transparent',
  color:      active ? TB.ACCENT : TB.INK,
  border: 'none', borderRight: `1px solid ${TB.INK}`,
  fontFamily: TBF.mono, fontSize: '12px', fontWeight: '700',
  letterSpacing: '0.06em', padding: '10px 20px', cursor: 'pointer',
})
const tabContentStyle = {
  padding: '20px', border: `2px solid ${TB.INK}`, background: TB.CARD, minHeight: '100px',
}
const specsStyle  = {}
const specRowStyle = {
  display: 'flex', gap: '24px', padding: '8px 0',
  borderBottom: `1px solid ${TB.INK}`, fontFamily: TBF.mono, fontSize: '13px',
}
const specKeyStyle = { color: TB.MUTED, width: '120px', flexShrink: '0', letterSpacing: '0.05em' }
const muteStyle = { fontFamily: TBF.mono, fontSize: '13px', color: TB.MUTED, padding: '40px 20px' }
</script>
