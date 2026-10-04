<template>
  <div class="wallet-page" v-loading="loading">
    <div class="wallet-card reveal">
      <div class="balance-box">
        <p class="balance-label">{{ t('shop.walletBalance') }}</p>
        <p class="balance-value">¥{{ balance }}</p>
        <p class="balance-hint">{{ t('shop.walletHint') }}</p>
      </div>

      <el-divider />

      <h3>{{ t('shop.recharge') }}</h3>
      <el-form label-width="100px" class="recharge-form">
        <el-form-item :label="t('shop.rechargeAmount')">
          <el-input-number v-model="rechargeAmount" :min="1" :max="10000" :precision="2" :step="10" style="width:100%" />
          <p class="field-hint">{{ t('shop.rechargeMin') }}</p>
        </el-form-item>
        <el-form-item :label="t('order.paymentMethod')">
          <el-radio-group v-model="paymentMethod">
            <el-radio v-for="c in channels" :key="c.channel" :value="c.channel">
              {{ channelLabel(c.channel) }}
            </el-radio>
          </el-radio-group>
          <p class="field-hint">{{ t('shop.rechargeHint') }}</p>
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="paying" @click="handleRecharge">
          {{ t('shop.recharge') }}
        </el-button>
      </el-form>

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

    <el-dialog v-model="qrDialog" :title="t('shop.wechat')" width="400px" @close="stopPoll">
      <div class="qr-box">
        <p class="qr-tip">{{ t('shop.scanToPay') }}</p>
        <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="QR" class="qr-img" />
        <p class="qr-order">{{ orderNo }}</p>
        <el-button type="primary" :loading="polling" @click="checkStatus">{{ t('shop.checkPayStatus') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import shopRequest from '../../api/shopRequest'
import { useScrollReveal } from '../../composables/useScrollReveal'

useScrollReveal()
const { t } = useI18n()
const loading = ref(false)
const paying = ref(false)
const balance = ref('0.00')
const transactions = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const rechargeAmount = ref(100)
const channels = ref([])
const paymentMethod = ref('MOCK')
const qrDialog = ref(false)
const qrCodeUrl = ref('')
const orderNo = ref('')
const currentOrderId = ref(null)
const polling = ref(false)
let pollTimer = null

const labels = { MOCK: 'shop.mockPay', ALIPAY: 'shop.alipay', WECHAT: 'shop.wechat' }
function channelLabel(c) { return t(labels[c] || c) }

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

async function loadChannels() {
  const res = await shopRequest.get('/shop/wallet/recharge/channels')
  channels.value = res.data || []
  paymentMethod.value = channels.value.find(c => c.channel === 'MOCK')?.channel
      || channels.value[0]?.channel
      || 'MOCK'
}

async function handleRecharge() {
  paying.value = true
  try {
    const res = await shopRequest.post('/shop/wallet/recharge', {
      amount: rechargeAmount.value,
      paymentMethod: paymentMethod.value
    })
    const data = res.data
    currentOrderId.value = data.orderId
    orderNo.value = data.orderNo

    if (data.payType === 'INSTANT') {
      ElMessage.success(t('shop.rechargeSuccess'))
      await Promise.all([loadWallet(), loadTransactions()])
      return
    }
    if (data.payType === 'REDIRECT' && data.payUrl) {
      window.location.href = data.payUrl
      return
    }
    if (data.payType === 'QRCODE' && data.codeUrl) {
      qrCodeUrl.value = await QRCode.toDataURL(data.codeUrl, { width: 220, margin: 1 })
      qrDialog.value = true
      startPoll()
    }
  } finally {
    paying.value = false
  }
}

function startPoll() {
  stopPoll()
  pollTimer = setInterval(checkStatus, 3000)
}

function stopPoll() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

async function checkStatus() {
  if (!currentOrderId.value) return
  polling.value = true
  try {
    const res = await shopRequest.get(`/shop/orders/${currentOrderId.value}/status`)
    if (res.data.status === 'DELIVERED') {
      stopPoll()
      qrDialog.value = false
      ElMessage.success(t('shop.rechargeSuccess'))
      await Promise.all([loadWallet(), loadTransactions()])
    }
  } finally {
    polling.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadWallet(), loadTransactions(), loadChannels()])
  } finally {
    loading.value = false
  }
})

onUnmounted(stopPoll)
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
.recharge-form { margin-bottom: 8px; }
.field-hint { margin-top: 6px; font-size: 12px; color: var(--shop-text-muted); }
.amount-plus { color: #16a34a; font-weight: 600; }
.amount-minus { color: #ef4444; font-weight: 600; }
.qr-box { text-align: center; }
.qr-tip { color: var(--shop-text-muted); margin-bottom: 16px; }
.qr-img { border: 1px solid var(--shop-border); border-radius: 8px; }
.qr-order { font-family: monospace; color: #94a3b8; font-size: 12px; margin: 12px 0; }
</style>
