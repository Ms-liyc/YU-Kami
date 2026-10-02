<template>
  <div class="embed-products">
    <header class="embed-header">
      <img src="/logo.png" alt="YU-Kami" class="embed-logo" />
      <span class="embed-brand">YU-Kami</span>
      <span class="embed-nav">商品 · 订单</span>
    </header>
    <div class="embed-banner">数字商品发卡网</div>
    <ShopProductGrid :limit="3" compact :show-toolbar="false" preview />
  </div>
</template>

<script setup>
import ShopProductGrid from '../../../components/shop/ShopProductGrid.vue'
</script>

<style scoped>
.embed-products {
  padding: 0 16px 16px;
  background: #f8fafc;
  min-height: 100vh;
}
.embed-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  margin: 0 -16px 12px;
  padding-left: 16px;
  padding-right: 16px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
}
.embed-logo { width: 28px; height: 28px; border-radius: 8px; }
.embed-brand { font-size: 15px; font-weight: 800; color: #0f172a; }
.embed-nav { margin-left: auto; font-size: 12px; color: #94a3b8; }
.embed-banner {
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  padding: 10px;
  margin-bottom: 14px;
  border-radius: 10px;
  color: #4f6ef7;
  background: linear-gradient(135deg, #eef2ff, #fce7f3);
}
</style>
