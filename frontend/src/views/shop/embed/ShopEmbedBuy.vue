<template>
  <div class="buy-page" v-loading="loading">
    <div class="buy-card" v-if="product">
      <h2>{{ product.name }}</h2>
      <p class="desc">{{ product.description }}</p>
      <div class="price-row">
        <span class="price">¥{{ displayPrice }}</span>
        <span v-if="product.onSale" class="original">¥{{ product.value }}</span>
      </div>
      <p v-if="product.promotionName" class="promo-tip">{{ product.promotionName }}</p>
      <el-divider />
      <el-form label-width="100px">
        <el-form-item :label="t('shop.coupon')">
          <div class="coupon-row">
            <el-input :model-value="demoCoupon" readonly />
            <el-button disabled>{{ t('shop.applyCoupon') }}</el-button>
          </div>
        </el-form-item>
        <el-form-item :label="t('order.paymentMethod')">
          <el-radio-group :model-value="'MOCK'">
            <el-radio value="MOCK">{{ t('shop.mockPay') }}</el-radio>
            <el-radio value="ALIPAY" disabled>{{ t('shop.alipay') }}</el-radio>
            <el-radio value="WECHAT" disabled>{{ t('shop.wechat') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <el-button type="primary" size="large" style="width:100%">{{ t('shop.pay') }}</el-button>
    </div>
    <el-empty v-else-if="!loading" :description="t('shop.noProducts')" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import shopHttp from '../../../api/shopHttp'

const { t } = useI18n()
const route = useRoute()
const product = ref(null)
const loading = ref(false)
const demoCoupon = 'SPRING2026'

const displayPrice = computed(() =>
  product.value?.onSale ? product.value.salePrice : product.value?.value
)

onMounted(async () => {
  loading.value = true
  try {
    const res = await shopHttp.get(`/shop/products/${route.params.id}`)
    if (res.data?.code === 200) product.value = res.data.data
  } catch { /* 静默失败，使用演示数据 */ }
  if (!product.value) {
    product.value = {
      name: t('landing.embedDemo.productName'),
      description: t('landing.embedDemo.productDesc'),
      value: 39.9,
      salePrice: 29.9,
      onSale: true,
      promotionName: t('landing.embedDemo.promotionName')
    }
  }
  loading.value = false
})
</script>

<style scoped>
.buy-page { display: flex; justify-content: center; padding: 48px 16px 24px; min-height: 100vh; background: var(--shop-bg); }
.buy-card {
  width: 100%; max-width: 480px;
  background: var(--shop-card);
  border-radius: 16px;
  padding: 32px;
  border: 1px solid var(--shop-border);
  box-shadow: var(--shop-card-shadow);
}
.buy-card h2 { font-size: 24px; color: var(--shop-text); }
.desc { color: var(--shop-text-muted); margin: 8px 0; }
.price-row { display: flex; align-items: baseline; gap: 12px; }
.price { font-size: 32px; font-weight: 800; color: var(--danger); }
.original { font-size: 18px; color: var(--shop-text-muted); text-decoration: line-through; }
.promo-tip { font-size: 13px; color: var(--shop-link-hover); margin-top: 4px; }
.coupon-row { display: flex; gap: 8px; width: 100%; }
.coupon-row .el-input { flex: 1; }
</style>
