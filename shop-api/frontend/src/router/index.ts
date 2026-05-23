import { createRouter, createWebHashHistory } from 'vue-router'
import HomeView    from '@/views/HomeView.vue'
import CatalogView from '@/views/CatalogView.vue'
import ProductView from '@/views/ProductView.vue'
import CartView    from '@/views/CartView.vue'
import OrdersView  from '@/views/OrdersView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/',            name: 'home',    component: HomeView    },
    { path: '/catalog',     name: 'catalog', component: CatalogView },
    { path: '/product/:id', name: 'product', component: ProductView },
    { path: '/cart',        name: 'cart',    component: CartView    },
    { path: '/orders',      name: 'orders',  component: OrdersView  },
  ],
})

export default router
