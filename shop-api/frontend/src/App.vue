<template>
  <div style="display: flex; flex-direction: column; min-height: 100vh;">
    <TbTopBar :path="currentPath" />
    <TbNav />
    <main style="flex: 1; display: flex; flex-direction: column;">
      <RouterView />
    </main>
    <TbFooter />
    <TbToast :msg="store.toast" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useShopStore } from '@/stores/shop'
import TbTopBar from '@/components/TbTopBar.vue'
import TbNav    from '@/components/TbNav.vue'
import TbFooter from '@/components/TbFooter.vue'
import TbToast  from '@/components/TbToast.vue'

const route = useRoute()
const store = useShopStore()

const currentPath = computed(() => {
  const name = route.name as string
  const map: Record<string, string> = {
    home: '/', catalog: '/catalog', product: `/product/${route.params.id || ''}`,
    cart: '/cart', orders: '/orders',
  }
  return map[name] || '/'
})

onMounted(() => {
  if (store.isAuth) store.fetchCart()
})
</script>
