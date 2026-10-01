<template>
  <div class="page-container">
    <div class="hero">
      <h1>{{ t('shop.title') }}</h1>
      <p>{{ t('shop.subtitle') }}</p>
    </div>
    <el-row :gutter="20" v-loading="loading">
      <el-col :xs="24" :sm="12" :md="8" v-for="p in products" :key="p.id">
        <div class="product-card">
          <div class="badges">
            <span class="product-badge">{{ typeLabel(p.cardType) }}</span>
            <span v-if="p.holiday" class="holiday-badge">{{ t('shop.holiday') }}</span>
            <span v-else-if="p.onSale" class="sale-badge">{{ t('shop.onSale') }}</span>
          </div>
          <h3>{{ p.name }}</h3>
          <p class="desc">{{ p.description }}</p>
          <div class="price-row">
            <span class="price">¥{{ displayPrice(p) }}</span>
            <span v-if="p.onSale" class="original">¥{{ p.value }}</span>
          </div>
          <p v-if="p.promotionName" class="promo-name">{{ p.promotionName }}</p>
          <div class="meta" v-if="p.durationDays">{{ p.durationDays }} 天</div>
          <el-button type="primary" style="width:100%" @click="handleBuy(p)">{{ t('shop.buyNow') }}</el-button>
        </div>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && products.length === 0" :description="t('shop.noProducts')" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { useShopAuthStore } from '../../stores/shopAuth'

const { t } = useI18n()
const router = useRouter()
const auth = useShopAuthStore()
const products = ref([])
const loading = ref(false)

const typeMap = { DURATION: '时长卡', BALANCE: '余额卡', SINGLE: '单次卡' }
function typeLabel(type) { return typeMap[type] || type }
function displayPrice(p) { return p.onSale ? p.salePrice : p.value }

async function loadProducts() {
  loading.value = true
  try {
    const res = await axios.get('/api/shop/products', { params: { page: 1, size: 50 } })
    products.value = res.data.data?.records || []
  } finally {
    loading.value = false
  }
}

function handleBuy(product) {
  if (!auth.token) {
    router.push('/shop/login?redirect=/shop/buy/' + product.id)
    return
  }
  router.push('/shop/buy/' + product.id)
}

onMounted(loadProducts)
</script>

<style scoped>
.hero { text-align: center; margin-bottom: 40px; }
.hero h1 { font-size: 32px; font-weight: 800; color: #1e293b; }
.hero p { color: #64748b; margin-top: 8px; }
.product-card {
  background: #fff; border-radius: 16px; padding: 28px; margin-bottom: 20px;
  border: 1px solid #e2e8f0; transition: transform 0.2s, box-shadow 0.2s;
}
.product-card:hover { transform: translateY(-4px); box-shadow: 0 12px 40px rgba(79,110,247,0.12); }
.badges { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.product-badge { display: inline-block; background: #eef2ff; color: #4f6ef7; font-size: 12px; padding: 2px 10px; border-radius: 20px; }
.holiday-badge { background: #fef3c7; color: #d97706; font-size: 12px; padding: 2px 10px; border-radius: 20px; }
.sale-badge { background: #fee2e2; color: #ef4444; font-size: 12px; padding: 2px 10px; border-radius: 20px; }
.product-card h3 { font-size: 20px; margin-bottom: 8px; }
.desc { color: #94a3b8; font-size: 14px; min-height: 40px; }
.price-row { display: flex; align-items: baseline; gap: 10px; margin: 16px 0 4px; }
.price { font-size: 28px; font-weight: 800; color: #ef4444; }
.original { font-size: 16px; color: #94a3b8; text-decoration: line-through; }
.promo-name { font-size: 12px; color: #4f6ef7; margin-bottom: 4px; }
.meta { color: #64748b; font-size: 13px; margin-bottom: 16px; }
</style>
