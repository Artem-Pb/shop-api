<template>
  <div :style="pageStyle">

    <!-- Sidebar -->
    <aside :style="sidebarStyle">
      <div :style="sideHeadStyle">FILTER</div>

      <!-- Categories -->
      <div :style="filterGroupStyle">
        <div :style="filterLabelStyle">CATEGORY</div>
        <button
          :style="filterItemStyle(!selectedCat)"
          @click="selectCat(null)"
        >ALL</button>
        <button
          v-for="cat in categories" :key="cat.id"
          :style="filterItemStyle(selectedCat === cat.id)"
          @click="selectCat(cat.id)"
        >{{ cat.name }}</button>
      </div>

      <!-- Sort -->
      <div :style="filterGroupStyle">
        <div :style="filterLabelStyle">SORT</div>
        <button
          v-for="opt in sortOptions" :key="opt.value"
          :style="filterItemStyle(sortField === opt.value)"
          @click="setSort(opt.value)"
        >{{ opt.label }}</button>
      </div>
    </aside>

    <!-- Main -->
    <div style="flex:1; min-width:0">
      <div :style="resultsHeadStyle">
        <span :style="{ fontFamily: TBF.mono, fontSize: '12px', letterSpacing: '0.05em' }">
          PRODUCTS // {{ total }} FOUND
        </span>
      </div>

      <div v-if="loading" :style="muteStyle">loading...</div>

      <div v-else :style="gridStyle">
        <RouterLink
          v-for="(p, idx) in products" :key="p.id"
          :to="`/product/${p.id}`"
          custom v-slot="{ navigate }"
        >
          <div @click="navigate" :style="cardStyle">
            <TbImage :h="160" :label="`p${p.id}`" :tone="idx % 5" />
            <div :style="{ padding: '14px' }">
              <div :style="skuStyle">SKU_{{ String(p.id).padStart(3, '0') }}</div>
              <div :style="nameStyle">{{ p.name }}</div>
              <div :style="{ display:'flex', justifyContent:'space-between', alignItems:'center', marginTop:'10px' }">
                <span :style="priceStyle">{{ fmt(p.price) }}</span>
                <TbTag>{{ p.categoryName }}</TbTag>
              </div>
            </div>
          </div>
        </RouterLink>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" :style="paginationStyle">
        <button
          v-for="n in totalPages" :key="n"
          :style="pageBtn(n === page + 1)"
          @click="page = n - 1; load()"
        >{{ n }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { TB, TBF, fmt } from '@/tokens'
import TbImage from '@/components/TbImage.vue'
import TbTag   from '@/components/TbTag.vue'
import api     from '@/api/client'

const route = useRoute()

const categories  = ref<any[]>([])
const products    = ref<any[]>([])
const loading     = ref(false)
const selectedCat = ref<number | null>(null)
const sortField   = ref('id')
const page        = ref(0)
const total       = ref(0)
const totalPages  = ref(1)

const sortOptions = [
  { label: 'ID ↑',    value: 'id' },
  { label: 'PRICE ↑', value: 'price,asc' },
  { label: 'PRICE ↓', value: 'price,desc' },
  { label: 'NAME',    value: 'name' },
]

function selectCat(id: number | null) { selectedCat.value = id; page.value = 0; load() }
function setSort(v: string)           { sortField.value = v;   page.value = 0; load() }

async function load() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: page.value, size: 12, sort: sortField.value }
    if (selectedCat.value) params.categoryId = selectedCat.value
    const { data } = await api.get('/api/products', { params })
    products.value  = data.content || []
    total.value     = data.totalElements || 0
    totalPages.value = data.totalPages || 1
  } catch { products.value = [] } finally { loading.value = false }
}

onMounted(async () => {
  const catResp = await api.get('/api/categories').catch(() => ({ data: [] }))
  categories.value = catResp.data

  const qCat = Number(route.query.category)
  if (qCat) selectedCat.value = qCat
  await load()
})

watch(() => route.query.category, (v) => {
  selectedCat.value = v ? Number(v) : null
  page.value = 0
  load()
})

// Styles
const pageStyle = { display: 'flex', gap: '0', background: TB.BG, minHeight: '100%' }

const sidebarStyle = {
  width: '200px', flexShrink: '0', borderRight: `2px solid ${TB.INK}`,
  background: TB.CARD,
}
const sideHeadStyle = {
  background: TB.INK, color: TB.ACCENT,
  fontFamily: TBF.mono, fontSize: '11px', fontWeight: '700', letterSpacing: '0.08em',
  padding: '10px 14px',
}
const filterGroupStyle  = { padding: '14px 0', borderBottom: `1px solid ${TB.INK}` }
const filterLabelStyle  = {
  fontFamily: TBF.mono, fontSize: '10px', color: TB.MUTED, letterSpacing: '0.1em',
  padding: '0 14px 8px',
}
const filterItemStyle = (active: boolean) => ({
  display: 'block', width: '100%', textAlign: 'left' as const,
  background: active ? TB.ACCENT : 'transparent',
  color:      active ? TB.INK    : TB.INK,
  border: 'none', borderLeft: active ? `3px solid ${TB.INK}` : '3px solid transparent',
  fontFamily: TBF.mono, fontSize: '12px', fontWeight: active ? '700' : '400',
  padding: '6px 14px', cursor: 'pointer', letterSpacing: '0.03em',
})

const resultsHeadStyle = {
  padding: '12px 20px', borderBottom: `2px solid ${TB.INK}`,
  background: TB.BG,
}
const muteStyle = { fontFamily: TBF.mono, fontSize: '12px', color: TB.MUTED, padding: '24px 20px' }
const gridStyle = {
  display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
  gap: '0', padding: '20px',
}
const cardStyle = {
  border: `2px solid ${TB.INK}`, background: TB.CARD, cursor: 'pointer',
  margin: '-1px 0 0 -1px', transition: 'box-shadow 0.1s',
}
const skuStyle   = { fontFamily: TBF.mono, fontSize: '10px', color: TB.MUTED, letterSpacing: '0.06em' }
const nameStyle  = { fontFamily: TBF.display, fontSize: '13px', fontWeight: '600', marginTop: '4px', lineHeight: '1.3' }
const priceStyle = { fontFamily: TBF.mono, fontSize: '14px', fontWeight: '700' }

const paginationStyle = {
  display: 'flex', gap: '4px', padding: '16px 20px', borderTop: `2px solid ${TB.INK}`,
}
const pageBtn = (active: boolean) => ({
  background: active ? TB.INK : 'transparent',
  color:      active ? TB.ACCENT : TB.INK,
  border: `2px solid ${TB.INK}`, fontFamily: TBF.mono, fontSize: '12px',
  width: '32px', height: '32px', cursor: 'pointer', fontWeight: '700',
})
</script>
