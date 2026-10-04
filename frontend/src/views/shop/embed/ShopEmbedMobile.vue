<template>
  <div class="embed-mobile">
    <header class="m-header">
      <img src="/logo.png" alt="" class="m-logo" />
      <span class="m-brand">YU-Kami</span>
      <span class="m-action">{{ t('common.login') }}</span>
    </header>
    <div class="m-hero">
      <div class="m-badge">{{ t('landing.embedMobile.heroBadge') }}</div>
      <h1>{{ t('landing.embedMobile.heroTitle') }}</h1>
      <p>{{ t('landing.embedMobile.heroDesc') }}</p>
    </div>
    <div class="m-section-title">{{ t('landing.embedMobile.hotProducts') }}</div>
    <ShopProductGrid :limit="2" compact :show-toolbar="false" preview />
    <nav class="m-bottom-nav">
      <a class="nav-item active"><span>{{ t('nav.mobileHome') }}</span></a>
      <a class="nav-item"><span>{{ t('nav.mobileProducts') }}</span></a>
      <a class="nav-item"><span>{{ t('nav.mobileQuery') }}</span></a>
      <a class="nav-item"><span>{{ t('nav.mobileMine') }}</span></a>
    </nav>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import ShopProductGrid from '../../../components/shop/ShopProductGrid.vue'

const { t } = useI18n()
</script>

<style scoped>
.embed-mobile {
  max-width: 390px;
  margin: 0 auto;
  min-height: 100vh;
  background: var(--shop-bg);
  color: var(--shop-text);
  padding-bottom: 64px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
}
.m-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--shop-card);
  border-bottom: 1px solid var(--shop-border);
  position: sticky;
  top: 0;
  z-index: 10;
}
.m-logo { width: 28px; height: 28px; border-radius: 8px; }
.m-brand { font-size: 15px; font-weight: 800; color: var(--shop-text); }
.m-action {
  margin-left: auto;
  font-size: 13px;
  color: var(--shop-link-hover);
  font-weight: 600;
  padding: 6px 14px;
  background: var(--shop-accent-soft);
  border-radius: 20px;
}
.m-hero {
  margin: 12px 16px;
  padding: 20px 16px;
  border-radius: 16px;
  background: var(--shop-m-banner);
}
.m-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  color: var(--shop-link-hover);
  background: var(--shop-badge-bg);
  padding: 4px 10px;
  border-radius: 12px;
  margin-bottom: 10px;
}
.m-hero h1 { font-size: 20px; font-weight: 800; color: var(--shop-text); margin-bottom: 6px; }
.m-hero p { font-size: 12px; color: var(--shop-text-muted); line-height: 1.5; }
.m-section-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--shop-text);
  padding: 8px 16px 4px;
}
.m-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 390px;
  display: flex;
  background: var(--shop-mobile-nav-bg);
  border-top: 1px solid var(--shop-border);
  padding: 6px 0 env(safe-area-inset-bottom);
  z-index: 20;
}
.nav-item {
  flex: 1;
  text-align: center;
  font-size: 11px;
  color: var(--shop-text-muted);
  padding: 6px 0;
  text-decoration: none;
}
.nav-item.active { color: var(--shop-link-hover); font-weight: 600; }
.nav-item span { display: block; }
</style>
