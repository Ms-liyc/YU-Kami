<template>
  <div class="buy-page" v-loading="loading">
    <div class="buy-card" v-if="product">
      <h2>{{ product.name }}</h2>
      <p class="desc">{{ product.description }}</p>
      <div class="price">¥{{ product.value }}</div>
      <el-divider />
      <el-form label-width="100px">
        <el-form-item :label="t('shop.amount')">
          <span class="total">¥{{ product.value }}</span>
        </el-form-item>
        <el-form-item :label="t('order.paymentMethod')">
          <el-radio-group v-model="paymentMethod">
            <el-radio v-for="c in channels" :key="c.channel" :value="c.channel">
              {{ channelLabel(c.channel) }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <el-button type="primary" size="large" style="width:100%" :loading="paying" @click="handlePay">
        {{ t('shop.pay') }}
      </el-button>
    </div>

    <el-dialog v-model="successDialog" :title="t('shop.paySuccess')" width="500px" @close="stopPoll">
      <el-alert type="success" :closable="false" show-icon :title="t('shop.cardKey')" style="margin-bottom:16px" />
      <el-input type="textarea" :rows="4" :model-value="cardKey" readonly />
      <template #footer>
        <el-button type="primary" @click="copyKey">{{ t('shop.copyCard') }}</el-button>
        <el-button @click="$router.push('/shop/orders')">{{ t('nav.myOrders') }}</el-button>
      </template>
    </el-dialog>

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
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import shopRequest from '../../api/shopRequest'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const product = ref(null)
const channels = ref([])
const paymentMethod = ref('MOCK')
const loading = ref(false)
const paying = ref(false)
const successDialog = ref(false)
const qrDialog = ref(false)
const cardKey = ref('')
const qrCodeUrl = ref('')
const orderNo = ref('')
const currentOrderId = ref(null)
const existingOrderId = ref(route.query.orderId ? Number(route.query.orderId) : null)
const polling = ref(false)
let pollTimer = null

const labels = { MOCK: 'shop.mockPay', ALIPAY: 'shop.alipay', WECHAT: 'shop.wechat' }
function channelLabel(c) { return t(labels[c] || c) }

onMounted(async () => {
  loading.value = true
  try {
    const [pRes, cRes] = await Promise.all([
      axios.get(`/api/shop/products/${route.params.id}`),
      shopRequest.get('/shop/orders/payment-channels')
    ])
    product.value = pRes.data.data
    channels.value = cRes.data || []
    if (channels.value.length) paymentMethod.value = channels.value[0].channel
  } finally {
    loading.value = false
  }
})

onUnmounted(stopPoll)

async function handlePay() {
  paying.value = true
  try {
    if (!existingOrderId.value) {
      const orderRes = await shopRequest.post('/shop/orders', { productId: product.value.id, quantity: 1 })
      currentOrderId.value = orderRes.data.id
    } else {
      currentOrderId.value = existingOrderId.value
    }
    const prepayRes = await shopRequest.post('/shop/orders/prepay', {
      orderId: currentOrderId.value,
      paymentMethod: paymentMethod.value
    })
    const data = prepayRes.data
    orderNo.value = data.orderNo

    if (data.payType === 'INSTANT') {
      cardKey.value = data.cardKey
      successDialog.value = true
    } else if (data.payType === 'REDIRECT' && data.payUrl) {
      window.location.href = data.payUrl
    } else if (data.payType === 'QRCODE' && data.codeUrl) {
      qrCodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${encodeURIComponent(data.codeUrl)}`
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
      cardKey.value = res.data.cardKey
      successDialog.value = true
      ElMessage.success(t('shop.paySuccess'))
    }
  } finally {
    polling.value = false
  }
}

function copyKey() {
  navigator.clipboard.writeText(cardKey.value)
  ElMessage.success('OK')
}
</script>

<style scoped>
.buy-page { display: flex; justify-content: center; }
.buy-card { width: 480px; background: #fff; border-radius: 16px; padding: 32px; box-shadow: 0 8px 30px rgba(0,0,0,0.06); }
.buy-card h2 { font-size: 24px; }
.desc { color: #64748b; margin: 8px 0; }
.price { font-size: 32px; font-weight: 800; color: #4f6ef7; }
.total { font-size: 24px; font-weight: 700; color: #ef4444; }
.qr-box { text-align: center; }
.qr-tip { color: #64748b; margin-bottom: 16px; }
.qr-img { border: 1px solid #e2e8f0; border-radius: 8px; }
.qr-order { font-family: monospace; color: #94a3b8; font-size: 12px; margin: 12px 0; }
</style>
