<template>
  <div :style="pageStyle">

    <!-- Auth card -->
    <div v-if="!store.isAuth" :style="authCardStyle">
      <div :style="authHeaderStyle">
        <span :style="{ fontFamily: TBF.mono, fontSize: '10px', letterSpacing: '0.1em', color: TB.MUTED }">ACCESS TERMINAL</span>
        <div style="display:flex; gap:0; margin-top:10px">
          <button :style="tabStyle(tab === 'login')"    @click="tab='login'">LOGIN</button>
          <button :style="tabStyle(tab === 'register')" @click="tab='register'">REGISTER</button>
        </div>
      </div>
      <div :style="{ padding: '20px' }">
        <div v-if="authError" :style="errStyle">{{ authError }}</div>
        <input :style="inputStyle" v-model="email"    type="email"    placeholder="email@domain.com" />
        <input :style="inputStyle" v-model="password" type="password" placeholder="••••••••••••" />
        <TbButton primary fullWidth @click="submit" :disabled="loading">
          {{ tab === 'login' ? '> LOGIN' : '> REGISTER' }}
        </TbButton>
      </div>
    </div>

    <!-- Manifesto -->
    <div :style="manifestoStyle">
      <div :style="manifestoInnerStyle">
        <div :style="{ fontFamily: TBF.mono, fontSize: '10px', letterSpacing: '0.1em', color: TB.MUTED, marginBottom: '12px' }">MANIFESTO://</div>
        <p :style="manifestoTextStyle">
          No gradients. No rounded corners. No dark patterns.<br/>
          Just products, prices, and the terminal prompt.
        </p>
      </div>
    </div>

    <!-- Categories -->
    <section :style="sectionStyle">
      <div :style="sectionHeadStyle">
        <span :style="labelStyle">CATEGORIES</span>
      </div>
      <div v-if="loadingCats" :style="muteStyle">loading...</div>
      <div v-else :style="catGridStyle">
        <RouterLink
          v-for="cat in categories" :key="cat.id"
          :to="`/catalog?category=${cat.id}`"
          custom v-slot="{ navigate }"
        >
          <div @click="navigate" :style="catCardStyle">
            <TbImage :h="80" :label="cat.name" :tone="cat.id % 5" />
            <div :style="catLabelStyle">{{ cat.name }}</div>
          </div>
        </RouterLink>
      </div>
    </section>

    <!-- Featured products -->
    <section :style="sectionStyle">
      <div :style="sectionHeadStyle">
        <span :style="labelStyle">FEATURED</span>
        <RouterLink to="/catalog" :style="allLinkStyle">VIEW ALL →</RouterLink>
      </div>
      <div v-if="loadingProds" :style="muteStyle">loading...</div>
      <div v-else :style="prodGridStyle">
        <RouterLink
          v-for="(p, idx) in featured" :key="p.id"
          :to="`/product/${p.id}`"
          custom v-slot="{ navigate }"
        >
          <div @click="navigate" :style="prodCardStyle">
            <TbImage :h="140" :label="`p${p.id}`" :tone="idx % 5" />
            <div :style="{ padding: '12px' }">
              <div :style="skuStyle">SKU_{{ String(p.id).padStart(3, '0') }}</div>
              <div :style="prodNameStyle">{{ p.name }}</div>
              <div :style="priceStyle">{{ fmt(p.price) }}</div>
            </div>
          </div>
        </RouterLink>
      </div>
    </section>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useShopStore } from '@/stores/shop'
import { TB, TBF, fmt } from '@/tokens'
import TbButton from '@/components/TbButton.vue'
import TbImage  from '@/components/TbImage.vue'
import api      from '@/api/client'

const store = useShopStore()

const tab      = ref<'login' | 'register'>('login')
const email    = ref('')
const password = ref('')
const loading  = ref(false)
const authError = ref('')

const categories  = ref<any[]>([])
const featured    = ref<any[]>([])
const loadingCats = ref(true)
const loadingProds = ref(true)

async function submit() {
  if (!email.value || !password.value) return
  loading.value  = true
  authError.value = ''
  try {
    if (tab.value === 'login') await store.login(email.value, password.value)
    else                       await store.register(email.value, password.value)
  } catch (e: any) {
    authError.value = e?.response?.data?.message || 'Ошибка авторизации'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const { data } = await api.get('/api/categories')
    categories.value = data
  } catch { /* empty */ } finally { loadingCats.value = false }

  try {
    const { data } = await api.get('/api/products', { params: { size: 4 } })
    featured.value = data.content || []
  } catch { /* empty */ } finally { loadingProds.value = false }
})

// Styles
const pageStyle = { background: TB.BG, minHeight: '100%', padding: '24px 16px', maxWidth: '1100px', margin: '0 auto' }

const authCardStyle = {
  border: `2px solid ${TB.INK}`, background: TB.CARD,
  maxWidth: '360px', margin: '0 0 32px',
  boxShadow: `4px 4px 0 ${TB.INK}`,
}
const authHeaderStyle = {
  background: TB.INK, padding: '14px 20px',
}
const tabStyle = (active: boolean) => ({
  background: active ? TB.ACCENT : 'transparent',
  color:      active ? TB.INK    : TB.MUTED,
  border: `1px solid ${active ? TB.ACCENT : TB.MUTED}`,
  fontFamily: TBF.mono, fontSize: '11px', fontWeight: '700', letterSpacing: '0.06em',
  padding: '5px 14px', cursor: 'pointer',
})
const inputStyle = {
  display: 'block', width: '100%', boxSizing: 'border-box' as const,
  border: `2px solid ${TB.INK}`, background: TB.BG,
  fontFamily: TBF.mono, fontSize: '13px', padding: '9px 10px', marginBottom: '10px', outline: 'none',
}
const errStyle = {
  background: TB.RED, color: TB.CARD, fontFamily: TBF.mono, fontSize: '11px',
  padding: '6px 10px', marginBottom: '10px',
}

const manifestoStyle = {
  border: `2px solid ${TB.INK}`, background: TB.INK, marginBottom: '32px',
  boxShadow: `4px 4px 0 ${TB.ACCENT}`,
}
const manifestoInnerStyle = {
  padding: '20px 24px', borderLeft: `4px solid ${TB.ACCENT}`,
}
const manifestoTextStyle = {
  fontFamily: TBF.display, fontSize: '16px', fontWeight: '700', color: TB.ACCENT,
  lineHeight: '1.6', margin: '0',
}

const sectionStyle   = { marginBottom: '40px' }
const sectionHeadStyle = {
  display: 'flex', justifyContent: 'space-between', alignItems: 'center',
  borderBottom: `2px solid ${TB.INK}`, paddingBottom: '8px', marginBottom: '16px',
}
const labelStyle = {
  fontFamily: TBF.mono, fontSize: '12px', fontWeight: '700', letterSpacing: '0.08em',
}
const allLinkStyle = {
  fontFamily: TBF.mono, fontSize: '11px', color: TB.INK, textDecoration: 'none', letterSpacing: '0.05em',
}
const muteStyle = { fontFamily: TBF.mono, fontSize: '12px', color: TB.MUTED }

const catGridStyle  = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))', gap: '12px' }
const catCardStyle  = {
  border: `2px solid ${TB.INK}`, background: TB.CARD, cursor: 'pointer',
  boxShadow: `2px 2px 0 ${TB.INK}`,
}
const catLabelStyle = {
  fontFamily: TBF.mono, fontSize: '11px', fontWeight: '700', letterSpacing: '0.05em',
  padding: '8px 10px', borderTop: `1px solid ${TB.INK}`,
}

const prodGridStyle = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '16px' }
const prodCardStyle = {
  border: `2px solid ${TB.INK}`, background: TB.CARD, cursor: 'pointer',
  boxShadow: `3px 3px 0 ${TB.INK}`, transition: 'transform 0.1s',
}
const skuStyle     = { fontFamily: TBF.mono, fontSize: '10px', color: TB.MUTED, letterSpacing: '0.06em' }
const prodNameStyle = { fontFamily: TBF.display, fontSize: '13px', fontWeight: '600', margin: '4px 0 8px', lineHeight: '1.3' }
const priceStyle    = { fontFamily: TBF.mono, fontSize: '14px', fontWeight: '700' }
</script>
