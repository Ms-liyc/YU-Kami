<template>
  <div class="query-page">
    <div class="query-card reveal">
      <h1>{{ t('shop.queryTitle') }}</h1>
      <p>{{ t('shop.queryDesc') }}</p>
      <el-alert v-if="!auth.token" type="info" :closable="false" show-icon :title="t('shop.loginRequired')" class="login-alert">
        <el-button type="primary" link @click="goLogin">{{ t('common.login') }}</el-button>
      </el-alert>
      <el-form @submit.prevent="handleQuery">
        <el-form-item :label="t('shop.orderNo')">
          <el-input v-model="orderNo" :placeholder="t('shop.orderNoPlaceholder')" clearable />
        </el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="handleQuery">
          {{ t('shop.queryBtn') }}
        </el-button>
      </el-form>

      <div v-if="result" class="result-card">
        <div class="result-row"><span>{{ t('shop.orderNo') }}</span><strong>{{ result.orderNo }}</strong></div>
        <div class="result-row"><span>{{ t('order.product') }}</span><strong>{{ result.productName }}</strong></div>
        <div class="result-row"><span>{{ t('shop.amount') }}</span><strong>¥{{ result.amount }}</strong></div>
        <div class="result-row"><span>{{ t('shop.status') }}</span>
          <el-tag :type="statusType(result.status)" size="small">{{ result.statusLabel }}</el-tag>
        </div>
        <div class="result-actions">
          <el-button v-if="result.status === 'PENDING'" type="primary" @click="goPay">{{ t('shop.pay') }}</el-button>
          <el-button v-if="result.status === 'DELIVERED'" type="success" @click="viewCard">{{ t('shop.viewCard') }}</el-button>
          <el-button @click="$router.push('/shop/orders')">{{ t('nav.myOrders') }}</el-button>
        </div>
      </div>

      <el-divider />
      <p class="hint">{{ t('shop.queryHint') }}<router-link to="/shop/login">{{ t('common.login') }}</router-link></p>
    </div>

    <el-dialog v-model="cardDialog" :title="t('shop.cardKey')" width="480px">
      <el-input type="textarea" :rows="4" :model-value="cardKey" readonly />
      <template #footer>
        <el-button type="primary" @click="copyKey">{{ t('shop.copyCard') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useShopAuthStore } from '../../stores/shopAuth'
import { useScrollReveal } from '../../composables/useScrollReveal'
import shopRequest from '../../api/shopRequest'

useScrollReveal()
const { t } = useI18n()
const router = useRouter()
const auth = useShopAuthStore()
const orderNo = ref('')
const loading = ref(false)
const result = ref(null)
const cardDialog = ref(false)
const cardKey = ref('')

function statusType(s) {
  return { PENDING: 'warning', DELIVERED: 'success', CANCELLED: 'info', PAID: 'primary' }[s] || ''
}

async function handleQuery() {
  if (!orderNo.value.trim()) {
    ElMessage.warning(t('shop.orderNoRequired'))
    return
  }
  if (!auth.token) {
    goLogin()
    return
  }
  loading.value = true
  result.value = null
  try {
    const res = await shopRequest.get('/shop/orders/lookup', { params: { orderNo: orderNo.value.trim() } })
    result.value = res.data
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push({ path: '/shop/login', query: { redirect: '/shop/query' } })
}

function goPay() {
  router.push({ path: '/shop/buy/' + result.value.productId, query: { orderId: result.value.id } })
}

async function viewCard() {
  const res = await shopRequest.get(`/shop/orders/${result.value.id}/card`)
  cardKey.value = res.data.cardKey
  cardDialog.value = true
}

function copyKey() {
  navigator.clipboard.writeText(cardKey.value)
  ElMessage.success(t('shop.copyCard'))
}
</script>

<style scoped>
.query-page { display: flex; justify-content: center; padding: 48px 0; }
.query-card {
  width: 100%; max-width: 480px;
  background: var(--shop-card); border-radius: 20px; padding: 40px;
  border: 1px solid var(--shop-border); box-shadow: var(--shop-card-shadow);
}
.query-card h1 { font-size: 24px; font-weight: 800; margin-bottom: 8px; color: var(--shop-text); }
.query-card p { color: var(--shop-text-muted); margin-bottom: 24px; font-size: 14px; }
.result-card {
  margin-top: 24px; padding: 20px; border-radius: 12px;
  background: var(--shop-section-alt); border: 1px solid var(--shop-border);
}
.result-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-size: 14px; }
.result-row span { color: var(--shop-text-muted); }
.result-actions { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px; }
.hint { font-size: 13px; color: var(--shop-text-muted); text-align: center; }
.hint a { color: #4f6ef7; margin-left: 4px; }
.login-alert { margin-bottom: 16px; }
</style>
