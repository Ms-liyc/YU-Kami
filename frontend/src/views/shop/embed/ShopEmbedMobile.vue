<template>
  <div class="embed-mobile">
    <header class="m-header">
      <img src="/logo.png" alt="" class="m-logo" />
      <span class="m-brand">YU-Kami</span>
      <span class="m-action">登录</span>
    </header>
    <div class="m-hero">
      <div class="m-badge">自动发卡 · 付款即发货</div>
      <h1>数字商品发卡网</h1>
      <p>付款后自动发货，支持微信 / 支付宝</p>
    </div>
    <div class="m-section-title">热销商品</div>
    <ShopProductGrid :limit="2" compact :show-toolbar="false" preview />
    <nav class="m-bottom-nav">
      <a class="nav-item active"><span>首页</span></a>
      <a class="nav-item"><span>商品</span></a>
      <a class="nav-item"><span>查单</span></a>
      <a class="nav-item"><span>我的</span></a>
    </nav>
  </div>
</template>

<script setup>
import ShopProductGrid from '../../../components/shop/ShopProductGrid.vue'
</script>

<style scoped>
.embed-mobile {
  max-width: 390px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f8fafc;
  padding-bottom: 64px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
}
.m-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 10;
}
.m-logo { width: 28px; height: 28px; border-radius: 8px; }
.m-brand { font-size: 15px; font-weight: 800; color: #0f172a; }
.m-action {
  margin-left: auto;
  font-size: 13px;
  color: #4f6ef7;
  font-weight: 600;
  padding: 6px 14px;
  background: #eef2ff;
  border-radius: 20px;
}
.m-hero {
  margin: 12px 16px;
  padding: 20px 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eef2ff, #fdf4ff);
}
.m-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  color: #4f6ef7;
  background: rgba(255,255,255,0.8);
  padding: 4px 10px;
  border-radius: 12px;
  margin-bottom: 10px;
}
.m-hero h1 { font-size: 20px; font-weight: 800; color: #0f172a; margin-bottom: 6px; }
.m-hero p { font-size: 12px; color: #64748b; line-height: 1.5; }
.m-section-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
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
  background: #fff;
  border-top: 1px solid #e2e8f0;
  padding: 6px 0 env(safe-area-inset-bottom);
  z-index: 20;
}
.nav-item {
  flex: 1;
  text-align: center;
  font-size: 11px;
  color: #64748b;
  padding: 6px 0;
  text-decoration: none;
}
.nav-item.active { color: #4f6ef7; font-weight: 600; }
.nav-item span { display: block; }
</style>
