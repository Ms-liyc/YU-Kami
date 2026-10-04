<template>
  <el-container class="layout">
    <div v-if="mobileMenuOpen" class="mobile-overlay" @click="mobileMenuOpen = false" />
    <el-aside :width="asideWidth" class="aside" :class="{ 'mobile-open': mobileMenuOpen }">
      <div class="logo" @click="collapsed = !collapsed">
        <img src="/logo.png" alt="YU-Kami" class="logo-img" />
        <transition name="fade">
          <span v-if="!collapsed" class="logo-text">YU-Kami</span>
        </transition>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="route.path"
          :collapse="collapsed"
          router
          class="side-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>{{ t('nav.dashboard') }}</template>
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><Goods /></el-icon>
            <template #title>{{ t('nav.products') }}</template>
          </el-menu-item>
          <el-menu-item index="/cards">
            <el-icon><Ticket /></el-icon>
            <template #title>{{ t('nav.cards') }}</template>
          </el-menu-item>
          <el-menu-item index="/batches">
            <el-icon><Files /></el-icon>
            <template #title>{{ t('nav.batches') }}</template>
          </el-menu-item>
          <el-menu-item index="/records">
            <el-icon><Document /></el-icon>
            <template #title>{{ t('nav.records') }}</template>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><ShoppingCart /></el-icon>
            <template #title>{{ t('nav.orders') }}</template>
          </el-menu-item>
          <el-menu-item index="/shop-users">
            <el-icon><UserFilled /></el-icon>
            <template #title>{{ t('nav.shopUsers') }}</template>
          </el-menu-item>
          <el-menu-item index="/promotions">
            <el-icon><Present /></el-icon>
            <template #title>{{ t('nav.promotions') }}</template>
          </el-menu-item>
          <el-menu-item index="/coupons">
            <el-icon><Discount /></el-icon>
            <template #title>{{ t('nav.coupons') }}</template>
          </el-menu-item>
          <el-menu-item index="/security">
            <el-icon><Lock /></el-icon>
            <template #title>{{ t('nav.securitySettings') }}</template>
          </el-menu-item>
          <el-divider v-if="auth.isSuperAdmin" style="margin: 8px 16px; border-color: #334155" />
          <template v-if="auth.isSuperAdmin">
            <el-menu-item index="/api-clients">
              <el-icon><Connection /></el-icon>
              <template #title>{{ t('nav.apiClients') }}</template>
            </el-menu-item>
            <el-menu-item index="/users">
              <el-icon><User /></el-icon>
              <template #title>{{ t('nav.users') }}</template>
            </el-menu-item>
            <el-menu-item index="/audit-logs">
              <el-icon><Notebook /></el-icon>
              <template #title>{{ t('nav.auditLogs') }}</template>
            </el-menu-item>
            <el-menu-item index="/webhooks">
              <el-icon><Bell /></el-icon>
              <template #title>{{ t('nav.webhooks') }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
      <div class="aside-footer" v-if="!collapsed">
        <span>v{{ APP_VERSION }}</span>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-button class="menu-toggle" :icon="Menu" circle @click="mobileMenuOpen = !mobileMenuOpen" />
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">{{ t('common.home') }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <router-link to="/shop" class="shop-link">{{ t('nav.shop') }}</router-link>
          <ThemeSwitcher />
          <LanguageSwitcher />
          <el-tag size="small" effect="plain" type="success">{{ t('common.running') }}</el-tag>
          <el-dropdown trigger="click">
            <div class="user-dropdown">
              <el-avatar :size="32" class="avatar">{{ (auth.nickname || auth.username || 'A')[0] }}</el-avatar>
              <span class="user-name">{{ auth.nickname || auth.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <el-tag size="small">{{ roleLabel }}</el-tag>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>{{ t('common.logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <DefaultPasswordBanner />
        <PaymentSetupBanner />
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Menu } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'
import LanguageSwitcher from '../components/LanguageSwitcher.vue'
import ThemeSwitcher from '../components/ThemeSwitcher.vue'
import PaymentSetupBanner from '../components/PaymentSetupBanner.vue'
import DefaultPasswordBanner from '../components/DefaultPasswordBanner.vue'
import { APP_VERSION } from '../constants/version'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(false)
const mobileMenuOpen = ref(false)

const asideWidth = computed(() => collapsed.value ? '64px' : '240px')

watch(() => route.path, () => { mobileMenuOpen.value = false })

const titleMap = {
  '/dashboard': 'nav.dashboard',
  '/products': 'nav.products',
  '/cards': 'nav.cards',
  '/batches': 'nav.batches',
  '/records': 'nav.records',
  '/api-clients': 'nav.apiClients',
  '/users': 'nav.users',
  '/audit-logs': 'nav.auditLogs',
  '/webhooks': 'nav.webhooks',
  '/orders': 'nav.orders',
  '/shop-users': 'nav.shopUsers',
  '/promotions': 'nav.promotions',
  '/coupons': 'nav.coupons',
  '/security': 'nav.securitySettings'
}

const currentTitle = computed(() => t(titleMap[route.path] || ''))
const roleLabel = computed(() => auth.role === 'SUPER_ADMIN' ? t('admin.roleSuperAdmin') : t('admin.roleAdmin'))

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside {
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  overflow: hidden;
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  cursor: pointer;
  border-bottom: 1px solid #1e293b;
}
.logo-img {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  flex-shrink: 0;
  object-fit: contain;
}
.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
}
.menu-scroll { flex: 1; }
.side-menu {
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #94a3b8;
  --el-menu-hover-bg-color: var(--sidebar-hover);
  --el-menu-active-color: #fff;
}
.side-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(79,110,247,0.3), transparent);
  border-right: 3px solid var(--sidebar-active);
}
.aside-footer {
  padding: 12px 20px;
  color: #475569;
  font-size: 12px;
  border-top: 1px solid #1e293b;
}
.main-container { background: var(--page-bg); }
.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--header-bg);
  border-bottom: 1px solid var(--border);
  padding: 0 28px;
}
.header-right { display: flex; align-items: center; gap: 16px; }
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}
.user-dropdown:hover { background: var(--primary-light); }
.avatar { background: linear-gradient(135deg, #4f6ef7, #7c3aed); color: #fff; font-size: 14px; }
.user-name { font-size: 14px; color: var(--text-primary); }
.shop-link { color: #4f6ef7; font-size: 13px; text-decoration: none; margin-right: 4px; }
.main { padding: 24px 28px; }
.page-enter-active, .page-leave-active { transition: all 0.25s ease; }
.page-enter-from { opacity: 0; transform: translateX(12px); }
.page-leave-to { opacity: 0; transform: translateX(-12px); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.menu-toggle { display: none; margin-right: 8px; }
.mobile-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  z-index: 999;
}
@media (max-width: 768px) {
  .menu-toggle { display: inline-flex; }
  .breadcrumb { display: none; }
  .header { padding: 0 12px; height: 56px; }
  .main { padding: 12px; }
  .user-name { display: none; }
  .shop-link { display: none; }
  .header-right { gap: 8px; }
  .header-right :deep(.el-tag) { display: none; }
  .aside {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  .aside.mobile-open { transform: translateX(0); }
  .mobile-overlay { display: block; }
}
</style>
