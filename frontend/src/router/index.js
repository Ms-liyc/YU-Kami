import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useShopAuthStore } from '../stores/shopAuth'

const routes = [
  {
    path: '/shop',
    component: () => import('../layouts/ShopLayout.vue'),
    children: [
      { path: '', name: 'ShopHome', component: () => import('../views/shop/ShopHome.vue') },
      { path: 'query', name: 'ShopQuery', component: () => import('../views/shop/ShopQuery.vue') },
      { path: 'login', name: 'ShopLogin', component: () => import('../views/shop/ShopLogin.vue') },
      { path: 'forgot-password', name: 'ShopForgotPassword', component: () => import('../views/shop/ShopForgotPassword.vue') },
      { path: 'verify-email', name: 'ShopVerifyEmail', component: () => import('../views/shop/ShopVerifyEmail.vue') },
      { path: 'reset-password', name: 'ShopResetPassword', meta: { scope: 'shop' }, component: () => import('../views/ResetPassword.vue') },
      { path: 'orders', name: 'ShopOrders', meta: { shopAuth: true }, component: () => import('../views/shop/ShopOrders.vue') },
      { path: 'buy/:id', name: 'ShopBuy', meta: { shopAuth: true }, component: () => import('../views/shop/ShopBuy.vue') },
      { path: 'product/:id', name: 'ShopProductDetail', component: () => import('../views/shop/ShopProductDetail.vue') },
      { path: 'profile', name: 'ShopProfile', meta: { shopAuth: true }, component: () => import('../views/shop/ShopProfile.vue') },
      { path: 'wallet', name: 'ShopWallet', meta: { shopAuth: true }, component: () => import('../views/shop/ShopWallet.vue') },
      { path: 'redeem', name: 'ShopRedeem', component: () => import('../views/shop/ShopRedeem.vue') }
    ]
  },
  {
    path: '/shop/embed',
    component: () => import('../layouts/ShopEmbedLayout.vue'),
    meta: { preview: true },
    children: [
      { path: 'products', name: 'ShopEmbedProducts', meta: { embed: true }, component: () => import('../views/shop/embed/ShopEmbedProducts.vue') },
      { path: 'buy/:id', name: 'ShopEmbedBuy', meta: { preview: true, embed: true }, component: () => import('../views/shop/embed/ShopEmbedBuy.vue') },
      { path: 'success', name: 'ShopEmbedSuccess', meta: { embed: true }, component: () => import('../views/shop/embed/ShopEmbedSuccess.vue') },
      { path: 'admin', name: 'ShopEmbedAdmin', meta: { embed: true }, component: () => import('../views/shop/embed/ShopEmbedAdmin.vue') },
      { path: 'mobile', name: 'ShopEmbedMobile', meta: { embed: true }, component: () => import('../views/shop/embed/ShopEmbedMobile.vue') },
      { path: 'query', name: 'ShopEmbedQuery', meta: { embed: true }, component: () => import('../views/shop/embed/ShopEmbedQuery.vue') },
      { path: 'redeem', name: 'ShopEmbedRedeem', meta: { embed: true }, component: () => import('../views/shop/embed/ShopEmbedRedeem.vue') },
      { path: 'profile', name: 'ShopEmbedProfile', meta: { embed: true }, component: () => import('../views/shop/embed/ShopEmbedProfile.vue') }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('../views/ForgotPassword.vue') },
  { path: '/reset-password', name: 'ResetPassword', component: () => import('../views/ResetPassword.vue') },
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
      { path: 'shop-users', name: 'ShopUsers', component: () => import('../views/ShopUsers.vue') },
      { path: 'promotions', name: 'Promotions', component: () => import('../views/Promotions.vue') },
      { path: 'coupons', name: 'Coupons', component: () => import('../views/Coupons.vue') },
      { path: 'security', name: 'Security', component: () => import('../views/SecuritySettings.vue') },
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
    const publicShopPaths = ['/shop/login', '/shop/forgot-password', '/shop/reset-password', '/shop/verify-email']
    if (to.meta.shopAuth && !shopAuth.token && !to.meta.preview) {
      next({ path: '/shop/login', query: { redirect: to.fullPath } })
    } else if (to.path === '/shop/login' && shopAuth.token) {
      next('/shop')
    } else {
      next()
    }
    return
  }
  const auth = useAuthStore()
  const publicPaths = ['/login', '/forgot-password', '/reset-password']
  if (!publicPaths.includes(to.path) && !auth.token) {
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
