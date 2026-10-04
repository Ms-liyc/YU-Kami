<template>
  <div class="embed-page-bg">
    <header class="embed-header-bar">
      <img src="/logo.png" alt="YU-Kami" class="embed-logo" />
      <span class="brand">YU-Kami</span>
      <span class="nav-hint">{{ t('nav.shopProducts') }} · {{ t('nav.myOrders') }}</span>
    </header>
    <div class="embed-tagline">{{ t('nav.banner') }}</div>
    <ShopProductGrid :limit="3" compact :show-toolbar="false" preview />
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import ShopProductGrid from '../../../components/shop/ShopProductGrid.vue'

const { t } = useI18n()
</script>

<style scoped>
.embed-logo { width: 28px; height: 28px; border-radius: 8px; }
.embed-page-bg { padding: 0 0 16px; }
</style>
