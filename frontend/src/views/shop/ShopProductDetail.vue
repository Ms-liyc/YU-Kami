<template>
  <div class="detail-page" v-loading="loading">
    <el-empty v-if="!loading && !product" :description="t('shop.productNotFound')">
      <el-button type="primary" @click="$router.push('/shop#products')">{{ t('shop.backToShop') }}</el-button>
    </el-empty>

    <div v-else-if="product" class="detail-card reveal">
      <div class="badges">
        <span v-if="product.category" class="category-badge">{{ product.category }}</span>
        <span class="type-badge">{{ typeLabel(product.cardType) }}</span>
        <span v-if="product.holiday" class="holiday-badge">{{ t('shop.holiday') }}</span>
        <span v-else-if="product.onSale" class="sale-badge">{{ t('shop.onSale') }}</span>
      </div>
      <h1>{{ product.name }}</h1>
      <p class="desc">{{ product.description || t('shop.defaultDesc') }}</p>
      <div class="price-row">
        <span class="price">¥{{ displayPrice }}</span>
        <span v-if="product.onSale" class="original">¥{{ product.value }}</span>
      </div>
      <p v-if="product.promotionName" class="promo-name">{{ product.promotionName }}</p>
      <el-divider />
      <div class="meta">
        <span>{{ t('shop.productCode') }}: {{ product.code }}</span>
        <span v-if="product.durationDays">{{ t('shop.durationDays') }}: {{ product.durationDays }}</span>
      </div>
      <div class="actions">
        <el-button type="primary" size="large" @click="goBuy">{{ t('shop.buyNow') }}</el-button>
        <el-button size="large" @click="$router.push('/shop#products')">{{ t('shop.backToShop') }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import shopHttp from '../../api/shopHttp'
import { useShopAuthStore } from '../../stores/shopAuth'
import { useScrollReveal } from '../../composables/useScrollReveal'

useScrollReveal()
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useShopAuthStore()
const product = ref(null)
const loading = ref(false)

const typeMap = computed(() => ({
  DURATION: t('shop.cardTypeDuration'),
  BALANCE: t('shop.cardTypeBalance'),
  SINGLE: t('shop.cardTypeSingle')
}))
function typeLabel(type) { return typeMap.value[type] || type }
const displayPrice = computed(() => product.value?.onSale ? product.value.salePrice : product.value?.value)

onMounted(async () => {
  loading.value = true
  try {
    const res = await shopHttp.get(`/shop/products/${route.params.id}`)
    if (res.data?.code === 200) {
      product.value = res.data.data
    }
  } finally {
    loading.value = false
  }
})

function goBuy() {
  const path = `/shop/buy/${route.params.id}`
  if (!auth.token) {
    router.push({ path: '/shop/login', query: { redirect: path } })
    return
  }
  router.push(path)
}
</script>

<style scoped>
.detail-page { display: flex; justify-content: center; padding: 48px 16px; min-height: 360px; }
.detail-card {
  width: 100%; max-width: 640px;
  background: var(--shop-card); border-radius: 20px; padding: 40px;
  border: 1px solid var(--shop-border); box-shadow: var(--shop-card-shadow);
}
.badges { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.category-badge { background: #ecfdf5; color: #059669; font-size: 12px; padding: 4px 12px; border-radius: 20px; }
.type-badge { background: var(--shop-accent-soft); color: var(--shop-brand-accent, #4f6ef7); font-size: 12px; padding: 4px 12px; border-radius: 20px; }
.holiday-badge { background: #fef3c7; color: #d97706; font-size: 12px; padding: 4px 12px; border-radius: 20px; }
.sale-badge { background: #fee2e2; color: #ef4444; font-size: 12px; padding: 4px 12px; border-radius: 20px; }
.detail-card h1 { font-size: 28px; font-weight: 800; color: var(--shop-text); margin-bottom: 12px; }
.desc { color: var(--shop-text-muted); line-height: 1.7; margin-bottom: 20px; }
.price-row { display: flex; align-items: baseline; gap: 12px; }
.price { font-size: 36px; font-weight: 800; color: #ef4444; }
.original { font-size: 18px; color: #94a3b8; text-decoration: line-through; }
.promo-name { margin-top: 8px; color: #4f6ef7; font-size: 14px; }
.meta { display: flex; flex-wrap: wrap; gap: 16px; color: var(--shop-text-muted); font-size: 14px; margin-bottom: 24px; }
.actions { display: flex; gap: 12px; flex-wrap: wrap; }
.actions .el-button { min-width: 140px; }
@media (max-width: 640px) {
  .detail-card { padding: 24px; }
  .actions { flex-direction: column; }
  .actions .el-button { width: 100%; }
}
</style>
