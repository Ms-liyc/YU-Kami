import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
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
