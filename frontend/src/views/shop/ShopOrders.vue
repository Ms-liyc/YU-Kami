<template>
  <div class="page-container orders-page">
    <h2>{{ t('nav.myOrders') }}</h2>
    <div class="filter-bar">
      <el-input v-model="filters.orderNo" :placeholder="t('shop.searchOrderNo')" clearable style="width:220px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="filters.status" :placeholder="t('shop.status')" clearable style="width:140px" @change="loadData">
        <el-option :label="t('shop.pending')" value="PENDING" />
        <el-option :label="t('order.statusPaid')" value="PAID" />
        <el-option :label="t('shop.delivered')" value="DELIVERED" />
        <el-option :label="t('shop.cancelled')" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="loadData">{{ t('common.search') }}</el-button>
    </div>
    <el-table :data="orders" stripe v-loading="loading">
      <el-table-column prop="orderNo" :label="t('shop.orderNo')" min-width="180" />
      <el-table-column prop="productName" :label="t('order.product')" />
      <el-table-column prop="quantity" :label="t('shop.quantity')" width="80" />
      <el-table-column prop="amount" :label="t('shop.amount')">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="status" :label="t('shop.status')">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('order.createdAt')" prop="createdAt" width="170" />
      <el-table-column label="" width="180">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" link type="primary" @click="goPay(row)">{{ t('shop.pay') }}</el-button>
          <el-button v-if="row.status === 'DELIVERED'" link @click="viewCard(row)">{{ t('shop.viewCard') }}</el-button>
          <el-button v-if="row.status === 'PENDING'" link type="danger" @click="cancelOrder(row)">{{ t('common.cancel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="20" :total="total" @current-change="loadData" style="margin-top:16px" />

    <el-dialog v-model="cardDialog" :title="t('shop.cardKey')" width="480px">
      <el-input type="textarea" :rows="4" :model-value="cardKey" readonly />
      <template #footer>
        <el-button type="primary" @click="copyKey">{{ t('shop.copyCard') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import shopRequest from '../../api/shopRequest'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const orders = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const cardDialog = ref(false)
const cardKey = ref('')
const filters = reactive({ orderNo: route.query.orderNo || '', status: '' })

function statusType(s) {
  return { PENDING: 'warning', DELIVERED: 'success', CANCELLED: 'info', PAID: 'primary' }[s] || ''
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (filters.status) params.status = filters.status
    if (filters.orderNo?.trim()) params.orderNo = filters.orderNo.trim()
    const res = await shopRequest.get('/shop/orders', { params })
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
  const res = await shopRequest.get(`/shop/orders/${row.id}/card`)
  cardKey.value = res.data.cardKey
  cardDialog.value = true
}

function copyKey() {
  navigator.clipboard.writeText(cardKey.value)
  ElMessage.success(t('shop.copyCard'))
}

async function cancelOrder(row) {
  await ElMessageBox.confirm(t('shop.cancelConfirm'), t('common.tip'))
  await shopRequest.post(`/shop/orders/${row.id}/cancel`)
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.orders-page { max-width: 1000px; margin: 0 auto; padding: 32px 0; }
.orders-page h2 { font-size: 24px; font-weight: 800; margin-bottom: 20px; color: var(--shop-text); }
.filter-bar { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 16px; }
</style>
