<template>
  <div :style="pageStyle">

    <div v-if="!store.isAdmin" :style="denyStyle">
      <span :style="{ fontFamily: TBF.mono, fontSize: '13px', color: TB.RED }">ACCESS DENIED // ADMIN ONLY</span>
    </div>

    <template v-else>
      <div :style="headStyle">
        <span :style="titleStyle">ADMIN // USER MANAGEMENT</span>
        <span :style="{ fontFamily: TBF.mono, fontSize: '11px', color: TB.MUTED }">{{ users.length }} USERS</span>
      </div>

      <div v-if="loading" :style="muteStyle">loading...</div>
      <div v-else-if="error" :style="errorStyle">{{ error }}</div>

      <table v-else :style="tableStyle">
        <thead>
          <tr :style="theadStyle">
            <th :style="thStyle">ID</th>
            <th :style="thStyle">EMAIL</th>
            <th :style="thStyle">ROLE</th>
            <th :style="thStyle">ACTION</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id" :style="trStyle(user.id)">
            <td :style="tdStyle">{{ String(user.id).padStart(3, '0') }}</td>
            <td :style="tdStyle">{{ user.email }}</td>
            <td :style="tdStyle">
              <span :style="roleBadgeStyle(user.role)">{{ user.role }}</span>
            </td>
            <td :style="tdStyle">
              <div v-if="user.email !== store.userEmail" style="display:flex; gap:6px">
                <TbButton
                  v-if="user.role !== 'ADMIN'"
                  :disabled="updating === user.id"
                  @click="setRole(user.id, 'ADMIN')"
                >
                  → ADMIN
                </TbButton>
                <TbButton
                  v-if="user.role !== 'USER'"
                  danger
                  :disabled="updating === user.id"
                  @click="setRole(user.id, 'USER')"
                >
                  → USER
                </TbButton>
              </div>
              <span v-else :style="{ fontFamily: TBF.mono, fontSize: '11px', color: TB.MUTED }">(you)</span>
            </td>
          </tr>
        </tbody>
      </table>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useShopStore } from '@/stores/shop'
import { TB, TBF } from '@/tokens'
import TbButton from '@/components/TbButton.vue'
import api from '@/api/client'

const store = useShopStore()

const users   = ref<any[]>([])
const loading = ref(false)
const error   = ref('')
const updating = ref<number | null>(null)

async function load() {
  if (!store.isAdmin) return
  loading.value = true
  error.value   = ''
  try {
    const { data } = await api.get('/api/admin/users')
    users.value = data
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Failed to load users'
  } finally {
    loading.value = false
  }
}

async function setRole(userId: number, role: string) {
  updating.value = userId
  try {
    await api.put(`/api/admin/users/${userId}/role`, null, { params: { role } })
    const user = users.value.find(u => u.id === userId)
    if (user) user.role = role
    store.showToast(`User ${userId} → ${role}`)
  } catch (e: any) {
    store.showToast('Failed to update role')
  } finally {
    updating.value = null
  }
}

onMounted(load)

// Styles
const pageStyle = { background: TB.BG, padding: '24px 20px', maxWidth: '900px', margin: '0 auto' }
const denyStyle = { padding: '60px 0', textAlign: 'center' as const }
const headStyle = {
  display: 'flex', justifyContent: 'space-between', alignItems: 'center',
  borderBottom: `2px solid ${TB.INK}`, paddingBottom: '10px', marginBottom: '20px',
}
const titleStyle = { fontFamily: TBF.mono, fontSize: '14px', fontWeight: '700', letterSpacing: '0.08em' }
const muteStyle  = { fontFamily: TBF.mono, fontSize: '12px', color: TB.MUTED, padding: '20px 0' }
const errorStyle = { fontFamily: TBF.mono, fontSize: '12px', color: TB.RED, padding: '16px 0' }

const tableStyle = {
  width: '100%', borderCollapse: 'collapse' as const,
  border: `2px solid ${TB.INK}`, background: TB.CARD,
}
const theadStyle = { background: TB.INK }
const thStyle    = {
  fontFamily: TBF.mono, fontSize: '11px', fontWeight: '700', letterSpacing: '0.08em',
  color: TB.ACCENT, padding: '10px 16px', textAlign: 'left' as const,
  borderRight: `1px solid #333`,
}
const trStyle = (id: number) => ({
  borderBottom: `1px solid ${TB.INK}`,
  background: id % 2 === 0 ? TB.CARD : TB.BG,
})
const tdStyle = { fontFamily: TBF.mono, fontSize: '13px', padding: '11px 16px' }
const roleBadgeStyle = (role: string) => ({
  fontFamily: TBF.mono, fontSize: '10px', fontWeight: '700', letterSpacing: '0.06em',
  padding: '2px 8px',
  background: role === 'ADMIN' ? TB.ACCENT : TB.INK,
  color:      role === 'ADMIN' ? TB.INK    : TB.CARD,
})
</script>
