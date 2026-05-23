<template>
  <div :style="pageStyle">

    <div v-if="!store.isAuth" :style="guestStyle">
      <span :style="{ fontFamily: TBF.mono, fontSize: '13px' }">Войдите, чтобы просмотреть корзину.</span>
      <RouterLink to="/" :style="btnLinkStyle">→ LOGIN</RouterLink>
    </div>

    <template v-else>
      <div :style="headStyle">
        <span :style="titleStyle">CART [{{ store.cartCount }}]</span>
      </div>

      <div v-if="store.cart.length === 0" :style="emptyStyle">
        CART IS EMPTY. <RouterLink to="/catalog" :style="{ color: TB.INK }">→ CATALOG</RouterLink>
      </div>

      <template v-else>
        <!-- Table -->
        <table :style="tableStyle">
          <thead>
            <tr :style="theadStyle">
              <th :style="thStyle">SKU</th>
              <th :style="thStyle">PRODUCT</th>
              <th :style="thStyle">PRICE</th>
              <th :style="thStyle">QTY</th>
              <th :style="thStyle">TOTAL</th>
              <th :style="thStyle"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in store.cart" :key="item.id" :style="trStyle">
              <td :style="tdStyle">{{ String(item.productId).padStart(3,'0') }}</td>
              <td :style="tdStyle">
                <RouterLink :to="`/product/${item.productId}`" :style="{ color: TB.INK, textDecoration:'none', fontWeight:'700' }">
                  {{ item.productName }}
                </RouterLink>
              </td>
              <td :style="tdStyle">{{ fmt(item.price) }}</td>
              <td :style="tdStyle">
                <div style="display:flex; gap:0">
                  <button :style="qtyBtnStyle" @click="store.updateQty(item.id, item.quantity - 1)">−</button>
                  <span :style="qtyValStyle">{{ item.quantity }}</span>
                  <button :style="qtyBtnStyle" @click="store.updateQty(item.id, item.quantity + 1)">+</button>
                </div>
              </td>
              <td :style="{ ...tdStyle, fontWeight:'700' }">{{ fmt(Number(item.price) * item.quantity) }}</td>
              <td :style="tdStyle">
                <button :style="removeBtnStyle" @click="store.removeFromCart(item.id)">×</button>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Totals -->
        <div :style="totalsRowStyle">
          <div :style="promoStyle">
            <input :style="promoInputStyle" v-model="promo" placeholder="PROMO CODE" />
            <TbButton @click="applyPromo">APPLY</TbButton>
            <span v-if="promoMsg" :style="promoMsgStyle">{{ promoMsg }}</span>
          </div>
          <div :style="summaryStyle">
            <div :style="summaryRowStyle">
              <span :style="{ color: TB.MUTED }">SUBTOTAL</span>
              <span>{{ fmt(store.cartTotal) }}</span>
            </div>
            <div v-if="discount > 0" :style="summaryRowStyle">
              <span :style="{ color: TB.GREEN }">DISCOUNT</span>
              <span :style="{ color: TB.GREEN }">− {{ fmt(discount) }}</span>
            </div>
            <div :style="{ ...summaryRowStyle, fontWeight:'700', fontSize:'16px', marginTop:'8px' }">
              <span>TOTAL</span>
              <span>{{ fmt(store.cartTotal - discount) }}</span>
            </div>
            <TbButton primary fullWidth :disabled="checkingOut" @click="doCheckout" style="margin-top:14px">
              {{ checkingOut ? '...' : '> CHECKOUT' }}
            </TbButton>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useShopStore } from '@/stores/shop'
import { TB, TBF, fmt } from '@/tokens'
import TbButton from '@/components/TbButton.vue'

const store = useShopStore()
const router = useRouter()

const promo       = ref('')
const promoMsg    = ref('')
const discount    = ref(0)
const checkingOut = ref(false)

function applyPromo() {
  if (promo.value.toUpperCase() === 'DEV10') {
    discount.value = Math.round(store.cartTotal * 0.1)
    promoMsg.value = '−10% applied'
  } else {
    discount.value = 0
    promoMsg.value = 'Invalid code'
  }
}

async function doCheckout() {
  checkingOut.value = true
  try {
    await store.checkout()
    store.showToast('Order placed!')
    router.push('/orders')
  } catch { store.showToast('Checkout failed') }
  finally { checkingOut.value = false }
}

// Styles
const pageStyle = { background: TB.BG, padding: '24px 20px', maxWidth: '1100px', margin: '0 auto' }
const guestStyle = {
  display: 'flex', gap: '16px', alignItems: 'center', padding: '40px 0',
  fontFamily: TBF.mono,
}
const btnLinkStyle = {
  border: `2px solid ${TB.INK}`, fontFamily: TBF.mono, fontSize: '12px', fontWeight: '700',
  padding: '8px 14px', color: TB.INK, textDecoration: 'none', letterSpacing: '0.04em',
}
const headStyle  = { borderBottom: `2px solid ${TB.INK}`, paddingBottom: '10px', marginBottom: '20px' }
const titleStyle = { fontFamily: TBF.mono, fontSize: '14px', fontWeight: '700', letterSpacing: '0.08em' }
const emptyStyle = { fontFamily: TBF.mono, fontSize: '13px', color: TB.MUTED, padding: '40px 0' }

const tableStyle = {
  width: '100%', borderCollapse: 'collapse' as const,
  border: `2px solid ${TB.INK}`, background: TB.CARD, marginBottom: '0',
}
const theadStyle = { background: TB.INK }
const thStyle    = {
  fontFamily: TBF.mono, fontSize: '11px', fontWeight: '700', letterSpacing: '0.08em',
  color: TB.ACCENT, padding: '10px 14px', textAlign: 'left' as const,
  borderRight: `1px solid #333`,
}
const trStyle  = { borderBottom: `1px solid ${TB.INK}` }
const tdStyle  = { fontFamily: TBF.mono, fontSize: '13px', padding: '12px 14px' }
const qtyBtnStyle = {
  background: TB.INK, color: TB.CARD, border: 'none',
  width: '28px', height: '28px', fontFamily: TBF.mono, fontSize: '16px',
  cursor: 'pointer', fontWeight: '700',
}
const qtyValStyle = {
  width: '36px', height: '28px', display: 'inline-flex', alignItems: 'center',
  justifyContent: 'center', border: `1px solid ${TB.INK}`,
  fontFamily: TBF.mono, fontSize: '13px', fontWeight: '700',
}
const removeBtnStyle = {
  background: 'transparent', border: 'none', color: TB.RED,
  fontFamily: TBF.mono, fontSize: '18px', cursor: 'pointer', fontWeight: '700',
  padding: '0 4px',
}
const totalsRowStyle = {
  display: 'flex', justifyContent: 'space-between', gap: '32px',
  border: `2px solid ${TB.INK}`, borderTop: 'none', padding: '20px',
  background: TB.CARD,
}
const promoStyle = { display: 'flex', gap: '8px', alignItems: 'flex-start', flexWrap: 'wrap' as const }
const promoInputStyle = {
  border: `2px solid ${TB.INK}`, background: TB.BG, padding: '9px 12px',
  fontFamily: TBF.mono, fontSize: '12px', width: '160px', outline: 'none', letterSpacing: '0.05em',
}
const promoMsgStyle = {
  fontFamily: TBF.mono, fontSize: '11px', color: TB.GREEN, alignSelf: 'center',
}
const summaryStyle    = { minWidth: '260px' }
const summaryRowStyle = {
  display: 'flex', justifyContent: 'space-between',
  fontFamily: TBF.mono, fontSize: '14px', padding: '4px 0',
}
</script>
