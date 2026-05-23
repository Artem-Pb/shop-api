<template>
  <div :style="pageStyle">

    <div v-if="!store.isAdmin" :style="denyStyle">
      <span :style="{ fontFamily: TBF.mono, fontSize: '13px', color: TB.RED }">ACCESS DENIED // ADMIN ONLY</span>
    </div>

    <template v-else>
      <!-- Header + tabs -->
      <div :style="headStyle">
        <span :style="titleStyle">ADMIN PANEL</span>
      </div>
      <div :style="tabBarStyle">
        <button v-for="t in TABS" :key="t" :style="tabStyle(t)" @click="tab = t">{{ t }}</button>
      </div>

      <!-- USERS -->
      <template v-if="tab === 'USERS'">
        <div v-if="loadingUsers" :style="muteStyle">loading...</div>
        <div v-else-if="errorUsers" :style="errorStyle">{{ errorUsers }}</div>
        <table v-else :style="tableStyle">
          <thead><tr :style="theadStyle">
            <th :style="thStyle">ID</th>
            <th :style="thStyle">EMAIL</th>
            <th :style="thStyle">ROLE</th>
            <th :style="thStyle">ACTION</th>
          </tr></thead>
          <tbody>
            <tr v-for="u in users" :key="u.id" :style="trStyle(u.id)">
              <td :style="tdStyle">{{ String(u.id).padStart(3,'0') }}</td>
              <td :style="tdStyle">{{ u.email }}</td>
              <td :style="tdStyle"><span :style="roleBadgeStyle(u.role)">{{ u.role }}</span></td>
              <td :style="tdStyle">
                <div v-if="u.email !== store.userEmail" style="display:flex;gap:6px">
                  <TbButton v-if="u.role !== 'ADMIN'" :disabled="updating === u.id" @click="setRole(u.id,'ADMIN')">→ ADMIN</TbButton>
                  <TbButton v-if="u.role !== 'USER'" danger :disabled="updating === u.id" @click="setRole(u.id,'USER')">→ USER</TbButton>
                </div>
                <span v-else :style="{ fontFamily: TBF.mono, fontSize: '11px', color: TB.MUTED }">(you)</span>
              </td>
            </tr>
          </tbody>
        </table>
      </template>

      <!-- CATEGORIES -->
      <template v-else-if="tab === 'CATEGORIES'">
        <div :style="formRowStyle">
          <input v-model="newCatName" placeholder="Category name" :style="inputStyle" @keyup.enter="createCategory" />
          <TbButton primary :disabled="!newCatName.trim() || savingCat" @click="createCategory">+ ADD</TbButton>
        </div>
        <div v-if="loadingCats" :style="muteStyle">loading...</div>
        <table v-else :style="tableStyle">
          <thead><tr :style="theadStyle">
            <th :style="thStyle">ID</th>
            <th :style="thStyle">NAME</th>
            <th :style="thStyle">ACTION</th>
          </tr></thead>
          <tbody>
            <tr v-for="c in categories" :key="c.id" :style="trStyle(c.id)">
              <td :style="tdStyle">{{ String(c.id).padStart(3,'0') }}</td>
              <td :style="tdStyle">{{ c.name }}</td>
              <td :style="tdStyle">
                <TbButton danger :disabled="deletingCat === c.id" @click="deleteCategory(c.id)">DELETE</TbButton>
              </td>
            </tr>
          </tbody>
        </table>
      </template>

      <!-- PRODUCTS -->
      <template v-else-if="tab === 'PRODUCTS'">
        <!-- Create form -->
        <div :style="cardStyle">
          <div :style="cardHeadStyle">NEW PRODUCT</div>
          <div :style="formGridStyle">
            <input v-model="pForm.name"        placeholder="Name"        :style="inputStyle" />
            <input v-model="pForm.description" placeholder="Description" :style="inputStyle" />
            <input v-model="pForm.price"       placeholder="Price"       type="number" :style="inputStyle" />
            <input v-model="pForm.stock"       placeholder="Stock"       type="number" :style="inputStyle" />
            <select v-model="pForm.categoryId" :style="inputStyle">
              <option disabled value="">Category</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <TbButton primary :disabled="savingProduct" @click="createProduct">+ CREATE PRODUCT</TbButton>
        </div>

        <div v-if="loadingProducts" :style="muteStyle">loading...</div>
        <table v-else :style="[tableStyle, { marginTop: '16px' }]">
          <thead><tr :style="theadStyle">
            <th :style="thStyle">ID</th>
            <th :style="thStyle">NAME</th>
            <th :style="thStyle">CATEGORY</th>
            <th :style="thStyle">PRICE</th>
            <th :style="thStyle">STOCK</th>
            <th :style="thStyle">ACTION</th>
          </tr></thead>
          <tbody>
            <tr v-for="p in products" :key="p.id" :style="trStyle(p.id)">
              <td :style="tdStyle">{{ String(p.id).padStart(3,'0') }}</td>
              <td :style="tdStyle">{{ p.name }}</td>
              <td :style="tdStyle">{{ p.categoryName }}</td>
              <td :style="tdStyle">{{ fmt(p.price) }}</td>
              <td :style="tdStyle">{{ p.stock }}</td>
              <td :style="tdStyle">
                <TbButton danger :disabled="deletingProduct === p.id" @click="deleteProduct(p.id)">DELETE</TbButton>
              </td>
            </tr>
          </tbody>
        </table>
      </template>

      <!-- ORDERS -->
      <template v-else-if="tab === 'ORDERS'">
        <div v-if="loadingOrders" :style="muteStyle">loading...</div>
        <div v-else-if="errorOrders" :style="errorStyle">{{ errorOrders }}</div>
        <table v-else :style="tableStyle">
          <thead><tr :style="theadStyle">
            <th :style="thStyle">ID</th>
            <th :style="thStyle">DATE</th>
            <th :style="thStyle">TOTAL</th>
            <th :style="thStyle">ITEMS</th>
            <th :style="thStyle">STATUS</th>
          </tr></thead>
          <tbody>
            <tr v-for="o in orders" :key="o.id" :style="trStyle(o.id)">
              <td :style="tdStyle">#{{ String(o.id).padStart(4,'0') }}</td>
              <td :style="tdStyle">{{ fmtDate(o.createdAt) }}</td>
              <td :style="tdStyle">{{ fmt(o.totalAmount) }}</td>
              <td :style="tdStyle">
                <span v-for="item in o.items" :key="item.id" :style="{ display:'block', fontFamily: TBF.mono, fontSize:'11px', color: TB.MUTED }">
                  {{ item.productName }} ×{{ item.quantity }}
                </span>
              </td>
              <td :style="tdStyle">
                <select :value="o.orderStatus" :style="selectStyle(o.orderStatus)" @change="updateStatus(o.id, ($event.target as HTMLSelectElement).value)">
                  <option v-for="s in ORDER_STATUSES" :key="s" :value="s">{{ s }}</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </template>

    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useShopStore } from '@/stores/shop'
import { TB, TBF, fmt } from '@/tokens'
import TbButton from '@/components/TbButton.vue'
import api from '@/api/client'

const store = useShopStore()

type Tab = 'USERS' | 'CATEGORIES' | 'PRODUCTS' | 'ORDERS'
const TABS: Tab[] = ['USERS', 'CATEGORIES', 'PRODUCTS', 'ORDERS']
const ORDER_STATUSES = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED']

const tab = ref<Tab>('USERS')

// --- USERS ---
const users      = ref<any[]>([])
const loadingUsers = ref(false)
const errorUsers   = ref('')
const updating     = ref<number | null>(null)

async function loadUsers() {
  if (!store.isAdmin) return
  loadingUsers.value = true; errorUsers.value = ''
  try { const { data } = await api.get('/api/admin/users'); users.value = data }
  catch (e: any) { errorUsers.value = e?.response?.data?.message || 'Failed to load users' }
  finally { loadingUsers.value = false }
}

async function setRole(userId: number, role: string) {
  updating.value = userId
  try {
    await api.put(`/api/admin/users/${userId}/role`, null, { params: { role } })
    const u = users.value.find(u => u.id === userId)
    if (u) u.role = role
    store.showToast(`User ${userId} → ${role}`)
  } catch { store.showToast('Failed to update role') }
  finally { updating.value = null }
}

// --- CATEGORIES ---
const categories  = ref<any[]>([])
const loadingCats = ref(false)
const newCatName  = ref('')
const savingCat   = ref(false)
const deletingCat = ref<number | null>(null)

async function loadCategories() {
  loadingCats.value = true
  try { const { data } = await api.get('/api/categories'); categories.value = data }
  finally { loadingCats.value = false }
}

async function createCategory() {
  if (!newCatName.value.trim()) return
  savingCat.value = true
  try {
    const { data } = await api.post('/api/categories', { name: newCatName.value.trim() })
    categories.value.push(data)
    newCatName.value = ''
    store.showToast('Category created')
  } catch (e: any) { store.showToast(e?.response?.data?.message || 'Error') }
  finally { savingCat.value = false }
}

async function deleteCategory(id: number) {
  deletingCat.value = id
  try {
    await api.delete(`/api/categories/${id}`)
    categories.value = categories.value.filter(c => c.id !== id)
    store.showToast('Category deleted')
  } catch (e: any) { store.showToast(e?.response?.data?.message || 'Error') }
  finally { deletingCat.value = null }
}

// --- PRODUCTS ---
const products        = ref<any[]>([])
const loadingProducts = ref(false)
const savingProduct   = ref(false)
const deletingProduct = ref<number | null>(null)
const pForm = ref({ name: '', description: '', price: '', stock: '', categoryId: '' as any })

async function loadProducts() {
  loadingProducts.value = true
  try { const { data } = await api.get('/api/products', { params: { size: 200 } }); products.value = data.content }
  finally { loadingProducts.value = false }
}

async function createProduct() {
  if (!pForm.value.name || !pForm.value.price || !pForm.value.stock || !pForm.value.categoryId) {
    store.showToast('Fill all fields'); return
  }
  savingProduct.value = true
  try {
    const { data } = await api.post('/api/products', {
      name: pForm.value.name,
      description: pForm.value.description,
      price: Number(pForm.value.price),
      stock: Number(pForm.value.stock),
      categoryId: pForm.value.categoryId,
    })
    products.value.unshift(data)
    pForm.value = { name: '', description: '', price: '', stock: '', categoryId: '' }
    store.showToast('Product created')
  } catch (e: any) { store.showToast(e?.response?.data?.message || 'Error') }
  finally { savingProduct.value = false }
}

async function deleteProduct(id: number) {
  deletingProduct.value = id
  try {
    await api.delete(`/api/products/${id}`)
    products.value = products.value.filter(p => p.id !== id)
    store.showToast('Product deleted')
  } catch (e: any) { store.showToast(e?.response?.data?.message || 'Error') }
  finally { deletingProduct.value = null }
}

// --- ORDERS ---
const orders        = ref<any[]>([])
const loadingOrders = ref(false)
const errorOrders   = ref('')

async function loadOrders() {
  loadingOrders.value = true; errorOrders.value = ''
  try { const { data } = await api.get('/api/admin/orders'); orders.value = data }
  catch (e: any) { errorOrders.value = e?.response?.data?.message || 'Failed to load orders' }
  finally { loadingOrders.value = false }
}

async function updateStatus(orderId: number, status: string) {
  try {
    const { data } = await api.put(`/api/orders/${orderId}/status`, null, { params: { status } })
    const o = orders.value.find(o => o.id === orderId)
    if (o) o.orderStatus = data.orderStatus
    store.showToast(`Order #${orderId} → ${status}`)
  } catch (e: any) { store.showToast(e?.response?.data?.message || 'Error') }
}

function fmtDate(dt: string) {
  return new Date(dt).toLocaleString('ru-RU', { day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' })
}

// load on tab switch
watch(tab, (t) => {
  if (t === 'USERS' && !users.value.length)           loadUsers()
  if (t === 'CATEGORIES' && !categories.value.length) loadCategories()
  if (t === 'PRODUCTS') { loadProducts(); if (!categories.value.length) loadCategories() }
  if (t === 'ORDERS' && !orders.value.length)         loadOrders()
})

onMounted(() => {
  loadUsers()
  loadCategories()
})

// Styles
const pageStyle   = { background: TB.BG, padding: '24px 20px', maxWidth: '1100px', margin: '0 auto' }
const denyStyle   = { padding: '60px 0', textAlign: 'center' as const }
const headStyle   = { borderBottom: `2px solid ${TB.INK}`, paddingBottom: '10px', marginBottom: '0' }
const titleStyle  = { fontFamily: TBF.mono, fontSize: '14px', fontWeight: '700', letterSpacing: '0.08em' }
const muteStyle   = { fontFamily: TBF.mono, fontSize: '12px', color: TB.MUTED, padding: '20px 0' }
const errorStyle  = { fontFamily: TBF.mono, fontSize: '12px', color: TB.RED, padding: '16px 0' }

const tabBarStyle = {
  display: 'flex', borderBottom: `2px solid ${TB.INK}`, marginBottom: '20px',
}
const tabStyle = (t: Tab) => ({
  fontFamily: TBF.mono, fontSize: '12px', fontWeight: '700', letterSpacing: '0.06em',
  padding: '10px 18px', border: 'none', borderBottom: tab.value === t ? `3px solid ${TB.INK}` : '3px solid transparent',
  background: tab.value === t ? TB.ACCENT : 'transparent',
  color: TB.INK, cursor: 'pointer',
})

const tableStyle  = { width: '100%', borderCollapse: 'collapse' as const, border: `2px solid ${TB.INK}`, background: TB.CARD }
const theadStyle  = { background: TB.INK }
const thStyle     = { fontFamily: TBF.mono, fontSize: '11px', fontWeight: '700', letterSpacing: '0.08em', color: TB.ACCENT, padding: '10px 14px', textAlign: 'left' as const, borderRight: '1px solid #333' }
const trStyle     = (id: number) => ({ borderBottom: `1px solid ${TB.INK}`, background: id % 2 === 0 ? TB.CARD : TB.BG })
const tdStyle     = { fontFamily: TBF.mono, fontSize: '12px', padding: '10px 14px', verticalAlign: 'top' as const }

const roleBadgeStyle = (role: string) => ({
  fontFamily: TBF.mono, fontSize: '10px', fontWeight: '700', letterSpacing: '0.06em',
  padding: '2px 8px',
  background: role === 'ADMIN' ? TB.ACCENT : TB.INK,
  color:      role === 'ADMIN' ? TB.INK    : TB.CARD,
})

const formRowStyle  = { display: 'flex', gap: '8px', marginBottom: '16px', alignItems: 'flex-start' }
const formGridStyle = { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', marginBottom: '12px' }
const inputStyle    = {
  fontFamily: TBF.mono, fontSize: '12px', padding: '9px 12px',
  border: `2px solid ${TB.INK}`, background: TB.BG, color: TB.INK, outline: 'none', width: '100%',
}
const cardStyle     = { border: `2px solid ${TB.INK}`, padding: '16px', background: TB.CARD, marginBottom: '0' }
const cardHeadStyle = { fontFamily: TBF.mono, fontSize: '11px', fontWeight: '700', letterSpacing: '0.08em', marginBottom: '12px', color: TB.MUTED }

const selectStyle = (status: string) => ({
  fontFamily: TBF.mono, fontSize: '11px', fontWeight: '700', padding: '4px 8px',
  border: `2px solid ${TB.INK}`,
  background: status === 'DELIVERED' ? TB.GREEN : status === 'CANCELLED' ? TB.RED : status === 'PENDING' ? TB.ACCENT : TB.INK,
  color: status === 'PENDING' ? TB.INK : TB.CARD,
  cursor: 'pointer',
})
</script>
