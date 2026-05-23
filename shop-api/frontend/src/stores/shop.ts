import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/client'

export interface CartItem {
  id: number
  productId: number
  productName: string
  price: number
  quantity: number
}

export const useShopStore = defineStore('shop', () => {
  const token     = ref<string>(localStorage.getItem('token') || '')
  const userEmail = ref<string>(localStorage.getItem('userEmail') || '')
  const cart      = ref<CartItem[]>([])
  const toast     = ref<string | null>(null)

  const isAuth     = computed(() => !!token.value)
  const cartCount  = computed(() => cart.value.reduce((s, i) => s + i.quantity, 0))
  const cartTotal  = computed(() => cart.value.reduce((s, i) => s + Number(i.price) * i.quantity, 0))

  function showToast(msg: string) {
    toast.value = msg
    setTimeout(() => { toast.value = null }, 1800)
  }

  async function login(email: string, password: string) {
    const { data } = await api.post('/api/auth/login', { email, password })
    token.value = data.token
    userEmail.value = email
    localStorage.setItem('token', data.token)
    localStorage.setItem('userEmail', email)
    await fetchCart()
  }

  async function register(email: string, password: string) {
    const { data } = await api.post('/api/auth/register', { email, password })
    token.value = data.token
    userEmail.value = email
    localStorage.setItem('token', data.token)
    localStorage.setItem('userEmail', email)
    cart.value = []
  }

  function logout() {
    token.value = ''
    userEmail.value = ''
    cart.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userEmail')
  }

  async function fetchCart() {
    if (!isAuth.value) return
    try {
      const { data } = await api.get('/api/cart')
      cart.value = data.items || []
    } catch {
      cart.value = []
    }
  }

  async function addToCart(productId: number, qty = 1): Promise<boolean> {
    if (!isAuth.value) {
      showToast('Войдите, чтобы добавить в корзину')
      return false
    }
    const { data } = await api.post('/api/cart/items', { productId, quantity: qty })
    cart.value = data.items || []
    showToast(`+ sku_${String(productId).padStart(3, '0')} добавлен`)
    return true
  }

  async function updateQty(itemId: number, quantity: number) {
    if (quantity <= 0) {
      await removeFromCart(itemId)
      return
    }
    const { data } = await api.put(`/api/cart/items/${itemId}`, null, { params: { quantity } })
    cart.value = data.items || []
  }

  async function removeFromCart(itemId: number) {
    await api.delete(`/api/cart/items/${itemId}`)
    cart.value = cart.value.filter(i => i.id !== itemId)
  }

  async function checkout() {
    const { data } = await api.post('/api/orders')
    cart.value = []
    return data
  }

  return {
    token, userEmail, cart, toast,
    isAuth, cartCount, cartTotal,
    showToast, login, register, logout,
    fetchCart, addToCart, updateQty, removeFromCart, checkout,
  }
})
