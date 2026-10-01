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

    <el-dialog v-model="successDialog" :title="t('shop.paySuccess')" width="500px">
      <el-alert type="success" :closable="false" show-icon :title="t('shop.cardKey')" style="margin-bottom:16px" />
      <el-input type="textarea" :rows="4" :model-value="cardKey" readonly />
      <template #footer>
        <el-button type="primary" @click="copyKey">{{ t('shop.copyCard') }}</el-button>
        <el-button @click="$router.push('/shop/orders')">{{ t('nav.myOrders') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
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
const cardKey = ref('')

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

async function handlePay() {
  paying.value = true
  try {
    const orderRes = await shopRequest.post('/shop/orders', { productId: product.value.id, quantity: 1 })
    const payRes = await shopRequest.post('/shop/orders/pay', {
      orderId: orderRes.data.id,
      paymentMethod: paymentMethod.value
    })
    cardKey.value = payRes.data.cardKey
    successDialog.value = true
  } finally {
    paying.value = false
  }
}

function copyKey() {
  navigator.clipboard.writeText(cardKey.value)
  ElMessage.success('Copied')
}
</script>

<style scoped>
.buy-page { display: flex; justify-content: center; }
.buy-card { width: 480px; background: #fff; border-radius: 16px; padding: 32px; box-shadow: 0 8px 30px rgba(0,0,0,0.06); }
.buy-card h2 { font-size: 24px; }
.desc { color: #64748b; margin: 8px 0; }
.price { font-size: 32px; font-weight: 800; color: #4f6ef7; }
.total { font-size: 24px; font-weight: 700; color: #ef4444; }
</style>
