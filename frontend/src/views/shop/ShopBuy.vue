<template>
  <div class="buy-page" v-loading="loading">
    <el-empty
      v-if="!loading && !product"
      :description="loadError || t('shop.productNotFound')"
      class="buy-empty"
    >
      <el-button type="primary" @click="$router.push('/shop#products')">{{ t('shop.backToShop') }}</el-button>
    </el-empty>

    <div class="buy-card" v-else-if="product">
      <h2>{{ product.name }}</h2>
      <p class="desc">{{ product.description }}</p>
      <div class="price-row">
        <span class="price">¥{{ pricing.finalAmount ?? displayPrice }}</span>
        <span v-if="pricing.onSale || product.onSale" class="original">¥{{ product.value }}</span>
      </div>
      <p v-if="pricing.promotionName" class="promo-tip">{{ pricing.promotionName }}</p>
      <el-divider />
      <el-form label-width="100px">
        <el-form-item :label="t('shop.quantity')">
          <el-input-number v-model="quantity" :min="1" :max="99" @change="previewPrice" />
        </el-form-item>
        <el-form-item :label="t('shop.coupon')">
          <div class="coupon-row">
            <el-input v-model="couponCode" :placeholder="t('shop.couponPlaceholder')" clearable />
            <el-button @click="previewPrice" :loading="previewing">{{ t('shop.applyCoupon') }}</el-button>
          </div>
        </el-form-item>
        <el-form-item :label="t('shop.amount')">
          <div>
            <span class="total">¥{{ pricing.finalAmount ?? displayPrice }}</span>
            <span v-if="pricing.discountAmount > 0" class="discount-tip">
              {{ t('shop.saved') }} ¥{{ pricing.discountAmount }}
            </span>
          </div>
        </el-form-item>
        <el-form-item :label="t('order.paymentMethod')">
          <el-radio-group v-model="paymentMethod">
            <el-radio v-for="c in channels" :key="c.channel" :value="c.channel">
              {{ channelLabel(c.channel) }}
            </el-radio>
          </el-radio-group>
          <p v-if="walletBalance !== null" class="wallet-tip">
            {{ t('shop.walletBalance') }}: ¥{{ walletBalance }}
            <router-link to="/shop/wallet">{{ t('shop.walletHistory') }}</router-link>
          </p>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import shopHttp from '../../api/shopHttp'
import shopRequest from '../../api/shopRequest'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const product = ref(null)
const pricing = ref({})
const quantity = ref(1)
const couponCode = ref('')
const channels = ref([])
const paymentMethod = ref('MOCK')
const loading = ref(false)
const loadError = ref('')
const previewing = ref(false)
const paying = ref(false)
const successDialog = ref(false)
const qrDialog = ref(false)
const cardKey = ref('')
const qrCodeUrl = ref('')
const orderNo = ref('')
const currentOrderId = ref(null)
const existingOrderId = ref(route.query.orderId ? Number(route.query.orderId) : null)
const walletBalance = ref(null)
const polling = ref(false)
let pollTimer = null

const displayPrice = computed(() => product.value?.onSale ? product.value.salePrice : product.value?.value)

const labels = { MOCK: 'shop.mockPay', ALIPAY: 'shop.alipay', WECHAT: 'shop.wechat', BALANCE: 'shop.balancePay' }
function channelLabel(c) { return t(labels[c] || c) }

onMounted(async () => {
  loading.value = true
  loadError.value = ''
  try {
    const [pRes, cRes, wRes] = await Promise.all([
      shopHttp.get(`/shop/products/${route.params.id}`),
      shopRequest.get('/shop/orders/payment-channels'),
      shopRequest.get('/shop/wallet').catch(() => null)
    ])
    if (pRes.data?.code !== 200 || !pRes.data?.data) {
      loadError.value = pRes.data?.message || t('shop.productNotFound')
      return
    }
    product.value = pRes.data.data
    channels.value = cRes.data || []
    if (wRes?.data?.balance != null) {
      walletBalance.value = Number(wRes.data.balance).toFixed(2)
    }
    const balanceChannel = channels.value.find(c => c.channel === 'BALANCE')
    const mockChannel = channels.value.find(c => c.channel === 'MOCK')
    paymentMethod.value = balanceChannel?.channel || mockChannel?.channel || channels.value[0]?.channel
    await previewPrice()
  } catch (e) {
    loadError.value = e?.response?.data?.message || e?.message || t('shop.productNotFound')
  } finally {
    loading.value = false
  }
})

onUnmounted(stopPoll)

async function previewPrice() {
  if (!product.value) return
  previewing.value = true
  try {
    const res = await shopRequest.post('/shop/orders/pricing/preview', {
      productId: product.value.id,
      quantity: quantity.value,
      couponCode: couponCode.value || undefined
    })
    pricing.value = res.data || {}
    if (couponCode.value && pricing.value.appliedType === 'COUPON') {
      ElMessage.success(t('shop.couponApplied'))
    }
  } catch (e) {
    pricing.value = {
      finalAmount: displayPrice.value,
      originalAmount: product.value.value,
      discountAmount: 0,
      onSale: product.value.onSale
    }
  } finally {
    previewing.value = false
  }
}

const isWechatBrowser = /MicroMessenger/i.test(navigator.userAgent)

async function handlePay() {
  paying.value = true
  try {
    if (!existingOrderId.value) {
      const orderRes = await shopRequest.post('/shop/orders', {
        productId: product.value.id,
        quantity: quantity.value,
        couponCode: couponCode.value || undefined
      })
      currentOrderId.value = orderRes.data.id
    } else {
      currentOrderId.value = existingOrderId.value
    }

    const useWechatJsapi = paymentMethod.value === 'WECHAT' && isWechatBrowser
    if (useWechatJsapi) {
      try {
        await doPrepay(true)
        return
      } catch (err) {
        if (!String(err?.message || '').includes('openid')) {
          throw err
        }
        ElMessage.info(t('shop.wechatOAuth'))
        const oauthRes = await shopRequest.get('/shop/payment/wechat/oauth-url', {
          params: { orderId: currentOrderId.value, redirect: route.fullPath }
        })
        window.location.href = oauthRes.data.url
        return
      }
    }

    await doPrepay(false)
  } finally {
    paying.value = false
  }
}

async function doPrepay(wechatJsapi) {
  const prepayRes = await shopRequest.post('/shop/orders/prepay', {
    orderId: currentOrderId.value,
    paymentMethod: paymentMethod.value,
    wechatJsapi
  })
  const data = prepayRes.data
  orderNo.value = data.orderNo

  if (data.payType === 'INSTANT') {
    cardKey.value = data.cardKey
    successDialog.value = true
  } else if (data.payType === 'REDIRECT' && data.payUrl) {
    window.location.href = data.payUrl
  } else if (data.payType === 'JSAPI' && data.jsapiParams) {
    ElMessage.info(t('shop.wechatJsapiPay'))
    await invokeWechatPay(data.jsapiParams)
    startPoll()
    await checkStatus()
  } else if (data.payType === 'QRCODE' && data.codeUrl) {
    qrCodeUrl.value = await QRCode.toDataURL(data.codeUrl, { width: 220, margin: 1 })
    qrDialog.value = true
    startPoll()
  }
}

function invokeWechatPay(params) {
  return new Promise((resolve, reject) => {
    const pay = () => {
      window.WeixinJSBridge.invoke('getBrandWCPayRequest', {
        appId: params.appId,
        timeStamp: params.timeStamp,
        nonceStr: params.nonceStr,
        package: params.packageValue,
        signType: params.signType,
        paySign: params.paySign
      }, (res) => {
        if (res.err_msg === 'get_brand_wcpay_request:ok') resolve(res)
        else reject(new Error(res.err_msg || 'pay failed'))
      })
    }
    if (typeof window.WeixinJSBridge === 'undefined') {
      document.addEventListener('WeixinJSBridgeReady', pay, false)
    } else {
      pay()
    }
  })
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
.buy-page { display: flex; justify-content: center; padding: 48px 0; min-height: 320px; }
.buy-empty { padding: 48px 0; }
.buy-card { width: 480px; background: var(--shop-card); border-radius: 16px; padding: 32px; border: 1px solid var(--shop-border); box-shadow: var(--shop-card-shadow); }
.buy-card h2 { font-size: 24px; color: var(--shop-text); }
.desc { color: var(--shop-text-muted); margin: 8px 0; }
.price-row { display: flex; align-items: baseline; gap: 12px; }
.price { font-size: 32px; font-weight: 800; color: #ef4444; }
.original { font-size: 18px; color: #94a3b8; text-decoration: line-through; }
.promo-tip { font-size: 13px; color: #4f6ef7; margin-top: 4px; }
.coupon-row { display: flex; gap: 8px; width: 100%; }
.coupon-row .el-input { flex: 1; }
.total { font-size: 24px; font-weight: 700; color: #ef4444; }
.discount-tip { margin-left: 12px; font-size: 14px; color: #22c55e; }
.wallet-tip { margin-top: 10px; font-size: 13px; color: var(--shop-text-muted); }
.wallet-tip a { color: #4f6ef7; margin-left: 8px; text-decoration: none; }
.qr-box { text-align: center; }
.qr-tip { color: var(--shop-text-muted); margin-bottom: 16px; }
.qr-img { border: 1px solid var(--shop-border); border-radius: 8px; }
.qr-order { font-family: monospace; color: #94a3b8; font-size: 12px; margin: 12px 0; }
</style>
