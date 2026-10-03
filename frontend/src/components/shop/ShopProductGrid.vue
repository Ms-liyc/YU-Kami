<template>
  <div class="product-grid-wrap">
    <div v-if="showToolbar" class="product-toolbar">
      <el-input v-model="search" :placeholder="t('shop.searchProducts')" clearable class="search-input">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-radio-group v-model="filterType" size="small">
        <el-radio-button value="">{{ t('shop.filterAll') }}</el-radio-button>
        <el-radio-button value="DURATION">{{ t('shop.cardTypeDuration') }}</el-radio-button>
        <el-radio-button value="SINGLE">{{ t('shop.cardTypeSingle') }}</el-radio-button>
        <el-radio-button value="BALANCE">{{ t('shop.cardTypeBalance') }}</el-radio-button>
      </el-radio-group>
    </div>
    <el-row :gutter="compact ? 12 : 20" v-loading="loading">
      <el-col
        v-for="p in visibleProducts"
        :key="p.id"
        :xs="24"
        :sm="compact ? 8 : 12"
        :md="compact ? 8 : 8"
      >
        <div class="product-card" :class="{ compact }">
          <div class="badges">
            <span class="product-badge">{{ typeLabel(p.cardType) }}</span>
            <span v-if="p.holiday" class="holiday-badge">{{ t('shop.holiday') }}</span>
            <span v-else-if="p.onSale" class="sale-badge">{{ t('shop.onSale') }}</span>
          </div>
          <h3>{{ p.name }}</h3>
          <p class="desc">{{ p.description || t('shop.defaultDesc') }}</p>
          <div class="price-row">
            <span class="price">¥{{ displayPrice(p) }}</span>
            <span v-if="p.onSale" class="original">¥{{ p.value }}</span>
          </div>
          <p v-if="p.promotionName" class="promo-name">{{ p.promotionName }}</p>
          <el-button
            type="primary"
            class="buy-btn"
            :disabled="preview"
            @click="handleBuy(p)"
          >
            {{ t('shop.buyNow') }}
          </el-button>
        </div>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && visibleProducts.length === 0" :description="t('shop.noProducts')" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import shopHttp from '../../api/shopHttp'
import { useShopAuthStore } from '../../stores/shopAuth'

const props = defineProps({
  limit: { type: Number, default: 0 },
  compact: { type: Boolean, default: false },
  showToolbar: { type: Boolean, default: true },
  preview: { type: Boolean, default: false }
})

const emit = defineEmits(['loaded'])

const { t } = useI18n()
const router = useRouter()
const auth = useShopAuthStore()
const products = ref([])
const loading = ref(false)
const search = ref('')
const filterType = ref('')

const typeMap = computed(() => ({
  DURATION: t('shop.cardTypeDuration'),
  BALANCE: t('shop.cardTypeBalance'),
  SINGLE: t('shop.cardTypeSingle')
}))
function typeLabel(type) { return typeMap.value[type] || type }
function displayPrice(p) { return p.onSale ? p.salePrice : p.value }

const filteredProducts = computed(() => {
  let list = products.value
  if (filterType.value) list = list.filter(p => p.cardType === filterType.value)
  if (search.value.trim()) {
    const q = search.value.trim().toLowerCase()
    list = list.filter(p => p.name?.toLowerCase().includes(q) || p.description?.toLowerCase().includes(q))
  }
  return list
})

const visibleProducts = computed(() => {
  const list = filteredProducts.value
  return props.limit > 0 ? list.slice(0, props.limit) : list
})

async function loadProducts() {
  loading.value = true
  try {
    const res = await shopHttp.get('/shop/products', { params: { page: 1, size: 50 } })
    products.value = res.data.data?.records || []
    emit('loaded', products.value)
  } finally {
    loading.value = false
  }
}

function handleBuy(product) {
  if (props.preview) return
  if (!auth.token) {
    router.push('/shop/login?redirect=/shop/buy/' + String(product.id))
    return
  }
  router.push('/shop/buy/' + String(product.id))
}

onMounted(loadProducts)

defineExpose({ products, loadProducts })
</script>

<style scoped>
.product-grid-wrap { --brand: #4f6ef7; }
.product-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  margin-bottom: 24px;
}
.search-input { max-width: 280px; }
.product-card {
  background: var(--shop-card);
  border-radius: 18px;
  padding: 28px;
  margin-bottom: 20px;
  border: 1px solid var(--shop-border);
  transition: all 0.3s;
  height: calc(100% - 20px);
  display: flex;
  flex-direction: column;
}
.product-card.compact {
  border-radius: 14px;
  padding: 16px 14px;
  margin-bottom: 0;
  height: 100%;
}
.product-card:hover:not(.compact) {
  transform: translateY(-6px);
  box-shadow: 0 20px 56px rgba(79,110,247,0.14);
}
.badges { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.product-card.compact .badges { margin-bottom: 8px; }
.product-badge { background: var(--shop-accent-soft); color: var(--shop-brand-accent, var(--brand)); font-size: 12px; padding: 2px 10px; border-radius: 20px; }
.product-card.compact .product-badge { font-size: 10px; padding: 1px 8px; }
.holiday-badge { background: #fef3c7; color: #d97706; font-size: 12px; padding: 2px 10px; border-radius: 20px; }
.sale-badge { background: #fee2e2; color: #ef4444; font-size: 12px; padding: 2px 10px; border-radius: 20px; }
.product-card h3 { font-size: 18px; font-weight: 700; margin-bottom: 8px; color: var(--shop-text); }
.product-card.compact h3 { font-size: 14px; margin-bottom: 4px; }
.desc { color: var(--shop-text-muted); font-size: 14px; flex: 1; min-height: 40px; }
.product-card.compact .desc { font-size: 11px; min-height: 28px; }
.price-row { display: flex; align-items: baseline; gap: 10px; margin: 16px 0 4px; }
.product-card.compact .price-row { margin: 8px 0 2px; }
.price { font-size: 28px; font-weight: 800; color: #ef4444; }
.product-card.compact .price { font-size: 20px; }
.original { font-size: 16px; color: #94a3b8; text-decoration: line-through; }
.product-card.compact .original { font-size: 12px; }
.promo-name { font-size: 12px; color: var(--brand); margin-bottom: 12px; }
.buy-btn { width: 100%; border-radius: 10px; height: 44px; font-weight: 600; }
.product-card.compact .buy-btn { height: 34px; font-size: 12px; border-radius: 8px; }
</style>
