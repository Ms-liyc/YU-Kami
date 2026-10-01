<template>
  <div class="shop-layout">
    <header class="shop-header">
      <div class="header-inner">
        <router-link to="/shop" class="brand">
          <img src="/logo.png" alt="YU-Kami" class="brand-logo" />
          <span>YU-Kami</span>
        </router-link>
        <nav class="nav-links">
          <router-link to="/shop">{{ t('shop.productList') }}</router-link>
          <router-link to="/shop/orders" v-if="auth.token">{{ t('nav.myOrders') }}</router-link>
        </nav>
        <div class="header-actions">
          <LanguageSwitcher />
          <router-link to="/login" class="admin-link">{{ t('nav.admin') }}</router-link>
          <template v-if="auth.token">
            <span class="user-name">{{ auth.nickname || auth.username }}</span>
            <el-button link type="danger" @click="handleLogout">{{ t('common.logout') }}</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/shop/login')">{{ t('common.login') }}</el-button>
          </template>
        </div>
      </div>
    </header>
    <main class="shop-main">
      <router-view />
    </main>
    <footer class="shop-footer">
      <p>© 2026 屿宸科技 YU-Kami · {{ t('shop.subtitle') }}</p>
    </footer>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useShopAuthStore } from '../stores/shopAuth'
import LanguageSwitcher from '../components/LanguageSwitcher.vue'

const { t } = useI18n()
const router = useRouter()
const auth = useShopAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/shop')
}
</script>

<style scoped>
.shop-layout { min-height: 100vh; display: flex; flex-direction: column; background: #f8fafc; }
.shop-header { background: #fff; border-bottom: 1px solid #e2e8f0; position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 64px; display: flex; align-items: center; justify-content: space-between; }
.brand { display: flex; align-items: center; gap: 10px; text-decoration: none; color: #1e293b; font-weight: 700; font-size: 18px; }
.brand-logo { width: 36px; height: 36px; border-radius: 8px; }
.nav-links { display: flex; gap: 24px; }
.nav-links a { color: #64748b; text-decoration: none; font-size: 14px; }
.nav-links a.router-link-active { color: #4f6ef7; font-weight: 600; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.admin-link { color: #94a3b8; font-size: 13px; text-decoration: none; }
.user-name { font-size: 14px; color: #334155; }
.shop-main { flex: 1; max-width: 1200px; width: 100%; margin: 0 auto; padding: 32px 24px; }
.shop-footer { text-align: center; padding: 24px; color: #94a3b8; font-size: 13px; border-top: 1px solid #e2e8f0; }
</style>
