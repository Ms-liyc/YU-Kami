<template>
  <div class="page-container">
    <h2>{{ t('nav.myOrders') }}</h2>
    <el-table :data="orders" stripe v-loading="loading">
      <el-table-column prop="orderNo" :label="t('shop.orderNo')" min-width="180" />
      <el-table-column prop="productName" :label="t('order.product')" />
      <el-table-column prop="amount" :label="t('shop.amount')">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="status" :label="t('shop.status')">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('order.createdAt')" prop="createdAt" width="170" />
      <el-table-column label="" width="160">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" link type="primary" @click="goPay(row)">{{ t('shop.pay') }}</el-button>
          <el-button v-if="row.status === 'DELIVERED'" link @click="viewCard(row)">{{ t('shop.cardKey') }}</el-button>
          <el-button v-if="row.status === 'PENDING'" link type="danger" @click="cancelOrder(row)">{{ t('common.cancel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="20" :total="total" @current-change="loadData" style="margin-top:16px" />

    <el-dialog v-model="cardDialog" :title="t('shop.cardKey')" width="480px">
      <el-input type="textarea" :rows="4" :model-value="cardKey" readonly />
      <template #footer>
        <el-button type="primary" @click="navigator.clipboard.writeText(cardKey); ElMessage.success('OK')">{{ t('shop.copyCard') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import shopRequest from '../../api/shopRequest'

const { t } = useI18n()
const router = useRouter()
const orders = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const cardDialog = ref(false)
const cardKey = ref('')

function statusType(s) {
  return { PENDING: 'warning', DELIVERED: 'success', CANCELLED: 'info' }[s] || ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await shopRequest.get('/shop/orders', { params: { page: page.value, size: 20 } })
    orders.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function goPay(row) {
  router.push({ path: '/shop/buy/' + row.productId, query: { orderId: row.id } })
}

async function viewCard(row) {
  try {
    const res = await shopRequest.get(`/shop/orders/${row.id}/card`)
    cardKey.value = res.data.cardKey
    cardDialog.value = true
  } catch {}
}

async function cancelOrder(row) {
  await ElMessageBox.confirm('Confirm?', 'Tip')
  await shopRequest.post(`/shop/orders/${row.id}/cancel`)
  ElMessage.success('OK')
  loadData()
}

onMounted(loadData)
</script>
