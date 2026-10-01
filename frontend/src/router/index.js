import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useShopAuthStore } from '../stores/shopAuth'

const routes = [
  {
    path: '/shop',
    component: () => import('../layouts/ShopLayout.vue'),
    children: [
      { path: '', name: 'ShopHome', component: () => import('../views/shop/ShopHome.vue') },
      { path: 'login', name: 'ShopLogin', component: () => import('../views/shop/ShopLogin.vue') },
      { path: 'orders', name: 'ShopOrders', meta: { shopAuth: true }, component: () => import('../views/shop/ShopOrders.vue') },
      { path: 'buy/:id', name: 'ShopBuy', meta: { shopAuth: true }, component: () => import('../views/shop/ShopBuy.vue') }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'products', name: 'Products', component: () => import('../views/Products.vue') },
      { path: 'cards', name: 'Cards', component: () => import('../views/Cards.vue') },
      { path: 'batches', name: 'Batches', component: () => import('../views/Batches.vue') },
      { path: 'records', name: 'Records', component: () => import('../views/Records.vue') },
      { path: 'orders', name: 'Orders', component: () => import('../views/Orders.vue') },
      { path: 'api-clients', name: 'ApiClients', meta: { superAdmin: true }, component: () => import('../views/ApiClients.vue') },
      { path: 'users', name: 'Users', meta: { superAdmin: true }, component: () => import('../views/Users.vue') },
      { path: 'audit-logs', name: 'AuditLogs', meta: { superAdmin: true }, component: () => import('../views/AuditLogs.vue') },
      { path: 'webhooks', name: 'Webhooks', meta: { superAdmin: true }, component: () => import('../views/Webhooks.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path.startsWith('/shop')) {
    const shopAuth = useShopAuthStore()
    if (to.meta.shopAuth && !shopAuth.token) {
      next({ path: '/shop/login', query: { redirect: to.fullPath } })
    } else if (to.path === '/shop/login' && shopAuth.token) {
      next('/shop')
    } else {
      next()
    }
    return
  }
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.token) {
    next('/login')
  } else if (to.path === '/login' && auth.token) {
    next('/dashboard')
  } else if (to.meta.superAdmin && !auth.isSuperAdmin) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
