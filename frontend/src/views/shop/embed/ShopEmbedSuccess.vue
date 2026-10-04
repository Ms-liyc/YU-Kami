<template>
  <div class="embed-success-page">
    <div class="buy-card">
      <el-result icon="success" :title="t('shop.paySuccess')">
        <template #sub-title>
          <el-alert type="success" :closable="false" show-icon :title="t('shop.cardKey')" style="margin-bottom:16px" />
          <el-input type="textarea" :rows="3" :model-value="cardKey" readonly />
        </template>
        <template #extra>
          <el-button type="primary">{{ t('shop.copyCard') }}</el-button>
          <el-button>{{ t('nav.myOrders') }}</el-button>
        </template>
      </el-result>
    </div>
    <div class="success-toast"><YuIcon name="check" size="sm" /> {{ t('shop.deliveredToast') }}</div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import YuIcon from '../../../components/icons/YuIcon.vue'

const { t } = useI18n()
const route = useRoute()
const productName = ref('')

const cardKey = computed(() => {
  const prefix = (productName.value || route.query.name || 'YK').slice(0, 2).toUpperCase()
  return route.query.key || `${prefix}-K7M9-X2P4-Q8N1`
})

onMounted(async () => {
  try {
    const res = await axios.get('/api/shop/products', { params: { page: 1, size: 1 } })
    productName.value = res.data.data?.records?.[0]?.name || ''
  } catch { /* demo key */ }
})
</script>

<style scoped>
.embed-success-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 16px 16px;
  background: var(--shop-bg);
}
.buy-card {
  width: 100%;
  max-width: 420px;
  background: var(--shop-card);
  border-radius: 16px;
  padding: 24px 20px 12px;
  border: 1px solid var(--shop-border);
  box-shadow: var(--shop-card-shadow);
}
.success-toast {
  width: 100%;
  max-width: 420px;
  margin-top: 12px;
  background: color-mix(in srgb, var(--success) 12%, transparent);
  color: var(--success);
  font-size: 13px;
  padding: 10px 14px;
  border-radius: 8px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid color-mix(in srgb, var(--success) 25%, transparent);
}
</style>
