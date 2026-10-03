<template>
  <div class="shop-layout" :class="{ dark: isDark }">
    <div v-if="showBanner" class="top-banner">
      <YuIcon name="spark" size="sm" class="banner-icon" />
      {{ t('nav.banner') }}
      <button class="banner-close" @click="showBanner = false" :aria-label="t('common.cancel')">
        <YuIcon name="close" size="sm" />
      </button>
    </div>
    <header class="shop-header">
      <div class="header-inner">
        <router-link to="/shop" class="brand">
          <img src="/logo.png" alt="YU-Kami" class="brand-logo" />
          <span>YU-Kami</span>
        </router-link>
        <nav class="nav-links">
          <a href="#products" @click.prevent="scrollTo('products')">{{ t('nav.shopProducts') }}</a>
          <a href="#flow" @click.prevent="scrollTo('flow')">{{ t('nav.flow') }}</a>
          <a href="#showcase" @click.prevent="scrollTo('showcase')">{{ t('nav.showcase') }}</a>
          <a href="#faq" @click.prevent="scrollTo('faq')">{{ t('nav.faq') }}</a>
          <router-link to="/shop/query">{{ t('nav.orderQuery') }}</router-link>
          <router-link to="/shop/redeem">{{ t('nav.redeem') }}</router-link>
        </nav>
        <div class="header-actions">
          <el-tooltip :content="isDark ? t('shop.lightMode') : t('shop.darkMode')">
            <el-button circle @click="toggleDark">
              <YuIcon :name="isDark ? 'sun' : 'moon'" size="sm" />
            </el-button>
          </el-tooltip>
          <LanguageSwitcher />
          <router-link to="/login" class="admin-link">{{ t('nav.admin') }}</router-link>
          <template v-if="auth.token">
            <router-link to="/shop/profile" class="user-name">{{ auth.nickname || auth.username }}</router-link>
            <el-button link type="danger" @click="handleLogout">{{ t('common.logout') }}</el-button>
          </template>
          <template v-else>
            <el-button type="primary" round @click="$router.push('/shop/login')">{{ t('common.login') }}</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="shop-main" :class="{ 'shop-main--landing': isLanding }">
      <router-view />
    </main>

    <footer class="shop-footer">
      <div class="footer-grid">
        <div class="footer-brand">
          <img src="/logo.png" alt="" class="footer-logo" />
          <div>
            <strong>YU-Kami</strong>
            <p>{{ t('nav.footerBrand') }}</p>
          </div>
        </div>
        <div class="footer-col">
          <h4>{{ t('nav.footerShop') }}</h4>
          <a href="#products" @click.prevent="scrollTo('products')">{{ t('nav.productList') }}</a>
          <router-link to="/shop/query">{{ t('nav.orderQuery') }}</router-link>
          <router-link to="/shop/orders">{{ t('nav.myOrders') }}</router-link>
          <router-link to="/shop/redeem">{{ t('nav.redeem') }}</router-link>
          <router-link to="/shop/profile">{{ t('nav.profile') }}</router-link>
          <router-link to="/shop/wallet">{{ t('nav.wallet') }}</router-link>
        </div>
        <div class="footer-col">
          <h4>{{ t('nav.about') }}</h4>
          <a href="#flow" @click.prevent="scrollTo('flow')">{{ t('nav.flow') }}</a>
          <a href="#security" @click.prevent="scrollTo('security')">{{ t('nav.security') }}</a>
          <a href="#faq" @click.prevent="scrollTo('faq')">{{ t('nav.faq') }}</a>
        </div>
        <div class="footer-col">
          <h4>{{ t('nav.manage') }}</h4>
          <router-link to="/login">{{ t('nav.admin') }}</router-link>
          <a href="https://github.com/Ms-liyc/YU-Kami" target="_blank" rel="noopener">GitHub</a>
        </div>
      </div>
      <p class="footer-copy">© 2026 屿宸科技 YU-Kami · 仅用于合法合规的数字商品销售</p>
    </footer>

    <nav class="mobile-nav">
      <router-link to="/shop" class="mobile-nav-item" exact-active-class="active">
        <YuIcon name="home" size="md" /><small>{{ t('nav.mobileHome') }}</small>
      </router-link>
      <a class="mobile-nav-item" @click.prevent="scrollTo('products')">
        <YuIcon name="shop" size="md" /><small>{{ t('nav.mobileProducts') }}</small>
      </a>
      <router-link to="/shop/query" class="mobile-nav-item">
        <YuIcon name="search" size="md" /><small>{{ t('nav.mobileQuery') }}</small>
      </router-link>
      <router-link to="/shop/orders" class="mobile-nav-item">
        <YuIcon name="orders" size="md" /><small>{{ t('nav.mobileOrders') }}</small>
      </router-link>
      <router-link to="/shop/login" class="mobile-nav-item" v-if="!auth.token">
        <YuIcon name="user" size="md" /><small>{{ t('nav.mobileMine') }}</small>
      </router-link>
      <router-link to="/shop/profile" class="mobile-nav-item" v-else>
        <YuIcon name="user" size="md" /><small>{{ t('nav.mobileMine') }}</small>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { useShopAuthStore } from '../stores/shopAuth'
import LanguageSwitcher from '../components/LanguageSwitcher.vue'
import YuIcon from '../components/icons/YuIcon.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useShopAuthStore()
const showBanner = ref(true)
const isDark = ref(localStorage.getItem('shop-theme') === 'dark')

const isLanding = computed(() => route.path === '/shop' || route.path === '/shop/')

function toggleDark() {
  isDark.value = !isDark.value
  localStorage.setItem('shop-theme', isDark.value ? 'dark' : 'light')
}

function scrollTo(id) {
  if (route.path !== '/shop') {
    router.push('/shop').then(() => {
      setTimeout(() => document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' }), 150)
    })
    return
  }
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
}

function handleLogout() {
  auth.logout()
  router.push('/shop')
}

onMounted(() => {
  if (localStorage.getItem('shop-theme') === 'dark') isDark.value = true
})
</script>

<style scoped>
.shop-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.top-banner {
  background: linear-gradient(90deg, #4f6ef7, #7c3aed);
  color: #fff;
  text-align: center;
  font-size: 13px;
  padding: 8px 40px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.banner-icon { color: #fff; opacity: 0.95; }
.banner-close {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #fff;
  cursor: pointer;
  opacity: 0.85;
  display: flex;
  padding: 4px;
}
.banner-close:hover { opacity: 1; }
.shop-header {
  background: var(--shop-header-bg);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--shop-header-border);
  position: sticky;
  top: 0;
  z-index: 200;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--shop-text);
  font-weight: 800;
  font-size: 18px;
  flex-shrink: 0;
}
.brand-logo { width: 36px; height: 36px; border-radius: 10px; }
.nav-links { display: flex; gap: 24px; }
.nav-links a {
  color: var(--shop-text-muted);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
}
.nav-links a:hover, .nav-links a.router-link-active { color: var(--shop-link-hover); }
.header-actions { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.admin-link { color: #94a3b8; font-size: 13px; text-decoration: none; }
.user-name { font-size: 14px; color: var(--shop-text-soft); text-decoration: none; }
.user-name:hover { color: var(--shop-link-hover); }

.shop-main { flex: 1; max-width: 1200px; width: 100%; margin: 0 auto; padding: 0 24px 32px; }
.shop-main--landing { max-width: none; padding: 0 0 48px; }

.shop-footer {
  background: #0f172a;
  color: #94a3b8;
  padding: 56px 24px 24px;
}
.footer-grid {
  max-width: 1200px;
  margin: 0 auto 40px;
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  gap: 32px;
}
.footer-brand { display: flex; gap: 14px; }
.footer-logo { width: 44px; height: 44px; border-radius: 10px; }
.footer-brand strong { color: #fff; font-size: 16px; display: block; margin-bottom: 4px; }
.footer-brand p { font-size: 13px; }
.footer-col h4 { color: #fff; font-size: 14px; margin-bottom: 12px; }
.footer-col a {
  display: block;
  color: #94a3b8;
  text-decoration: none;
  font-size: 13px;
  margin-bottom: 8px;
}
.footer-col a:hover { color: #fff; }
.footer-copy {
  text-align: center;
  font-size: 12px;
  color: #475569;
  max-width: 1200px;
  margin: 0 auto;
  padding-top: 24px;
  border-top: 1px solid #1e293b;
}

.mobile-nav {
  display: none;
  position: fixed;
  bottom: 0; left: 0; right: 0;
  background: var(--shop-mobile-nav-bg);
  border-top: 1px solid var(--shop-border);
  z-index: 200;
  padding: 4px 0 env(safe-area-inset-bottom);
}
.mobile-nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  text-decoration: none;
  color: var(--shop-text-muted);
  font-size: 10px;
  padding: 6px 0;
  cursor: pointer;
}
.mobile-nav-item .yu-icon { margin-bottom: 2px; }
.mobile-nav-item.active, .mobile-nav-item.router-link-active { color: var(--shop-link-hover); }

@media (max-width: 900px) {
  .nav-links { display: none; }
  .admin-link, .user-name { display: none; }
  .footer-grid { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 768px) {
  .header-inner { padding: 0 16px; }
  .shop-main, .shop-main--landing { padding: 0 16px 80px; }
  .mobile-nav { display: flex; }
  .shop-footer { padding-bottom: 88px; }
}
</style>
