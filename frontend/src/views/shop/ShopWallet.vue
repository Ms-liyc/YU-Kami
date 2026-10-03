<template>
  <div class="wallet-page" v-loading="loading">
    <div class="wallet-card reveal">
      <div class="balance-box">
        <p class="balance-label">{{ t('shop.walletBalance') }}</p>
        <p class="balance-value">¥{{ balance }}</p>
        <p class="balance-hint">{{ t('shop.walletHint') }}</p>
      </div>

      <el-divider />

      <h3>{{ t('shop.walletHistory') }}</h3>
      <el-table :data="transactions" stripe empty-text="—">
        <el-table-column prop="createdAt" :label="t('order.createdAt')" width="170" />
        <el-table-column prop="typeLabel" :label="t('shop.walletType')" width="120" />
        <el-table-column :label="t('shop.amount')" width="120">
          <template #default="{ row }">
            <span :class="row.amount >= 0 ? 'amount-plus' : 'amount-minus'">
              {{ row.amount >= 0 ? '+' : '' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" :label="t('shop.orderNo')" min-width="160" />
        <el-table-column prop="remark" :label="t('shop.walletRemark')" min-width="140" />
      </el-table>
      <el-pagination
        v-if="total > size"
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadTransactions"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import shopRequest from '../../api/shopRequest'
import { useScrollReveal } from '../../composables/useScrollReveal'

useScrollReveal()
const { t } = useI18n()
const loading = ref(false)
const balance = ref('0.00')
const transactions = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

async function loadWallet() {
  const res = await shopRequest.get('/shop/wallet')
  balance.value = Number(res.data?.balance ?? 0).toFixed(2)
}

async function loadTransactions() {
  const res = await shopRequest.get('/shop/wallet/transactions', {
    params: { page: page.value, size: size.value }
  })
  transactions.value = res.data?.records || []
  total.value = res.data?.total || 0
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadWallet(), loadTransactions()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.wallet-page { display: flex; justify-content: center; padding: 48px 0; }
.wallet-card {
  width: 100%; max-width: 720px;
  background: var(--shop-card); border-radius: 20px; padding: 32px;
  border: 1px solid var(--shop-border); box-shadow: var(--shop-card-shadow);
}
.balance-box {
  text-align: center;
  padding: 12px 0 8px;
  background: linear-gradient(135deg, rgba(79,110,247,0.08), rgba(124,58,237,0.06));
  border-radius: 16px;
  margin-bottom: 8px;
}
.balance-label { font-size: 14px; color: var(--shop-text-muted); margin-bottom: 8px; }
.balance-value { font-size: 36px; font-weight: 800; color: #4f6ef7; margin-bottom: 8px; }
.balance-hint { font-size: 13px; color: var(--shop-text-muted); }
.wallet-card h3 { font-size: 16px; font-weight: 700; margin-bottom: 16px; color: var(--shop-text); }
.amount-plus { color: #16a34a; font-weight: 600; }
.amount-minus { color: #ef4444; font-weight: 600; }
</style>
