<template>
  <div class="page-container">
    <PageHeader :title="t('admin.dashboardTitle')" :subtitle="t('admin.dashboardSubtitle')" />

    <el-alert
      v-if="stats.lowStockProducts?.length"
      type="warning"
      :closable="false"
      show-icon
      class="stock-alert"
      :title="t('admin.lowStockTitle')"
    >
      <ul class="stock-list">
        <li v-for="item in stats.lowStockProducts" :key="item.productId">
          {{ item.productName }} ({{ item.productCode }}) — {{ t('admin.unused') }} {{ item.unusedCount }}
        </li>
      </ul>
    </el-alert>

    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="8" :md="4" v-for="item in statCards" :key="item.label">
        <StatCard :label="item.label" :value="item.value" :icon="item.icon" :color="item.color" />
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :xs="24" :md="16">
        <div class="page-card" style="margin-bottom:16px">
          <div class="card-header">
            <h3>{{ t('admin.trendTitle') }}</h3>
            <div class="trend-legend">
              <span><i class="legend-dot orders"></i>{{ t('admin.trendLegendOrders') }}</span>
              <span><i class="legend-dot redeems"></i>{{ t('admin.trendLegendRedeems') }}</span>
            </div>
          </div>
          <div class="card-body trend-chart">
            <div v-for="d in trends" :key="d.date" class="trend-bar-group">
              <div class="bars">
                <div class="bar orders" :style="{ height: barHeight(d.orderCount, maxOrders) }" :title="`${t('admin.todayOrders')}: ${d.orderCount}`" />
                <div class="bar redeems" :style="{ height: barHeight(d.redeemCount, maxRedeems) }" :title="`${t('admin.todayRedeems')}: ${d.redeemCount}`" />
              </div>
              <span class="trend-label">{{ d.date.slice(5) }}</span>
            </div>
          </div>
        </div>
        <div class="page-card">
          <div class="card-header"><h3>{{ t('admin.recentRedeems') }}</h3></div>
          <div class="card-body" style="padding: 0">
            <el-table :data="stats.recentRedeems || []" stripe :empty-text="t('admin.noRedeemRecords')">
              <el-table-column prop="redeemUser" :label="t('admin.recordUser')" width="140" />
              <el-table-column prop="result" :label="t('admin.recordResult')" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small" effect="light">
                    {{ row.result }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" :label="t('admin.recordMessage')" show-overflow-tooltip />
              <el-table-column prop="redeemIp" :label="t('admin.recordIp')" width="130" />
              <el-table-column prop="createdAt" :label="t('admin.recordTime')" width="170" />
            </el-table>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :md="8">
        <div class="page-card" style="margin-bottom: 16px">
          <div class="card-header"><h3>{{ t('admin.usageRate') }}</h3></div>
          <div class="card-body usage-body">
            <el-progress type="dashboard" :percentage="Math.round(stats.usageRate || 0)" :width="140"
              :color="progressColors" />
            <div class="usage-detail">
              <div><span class="dot used"></span>{{ t('admin.used') }} {{ stats.usedCards || 0 }}</div>
              <div><span class="dot unused"></span>{{ t('admin.unused') }} {{ stats.unusedCards || 0 }}</div>
            </div>
          </div>
        </div>
        <div class="page-card">
          <div class="card-header"><h3>{{ t('admin.cryptoTitle') }}</h3></div>
          <div class="card-body crypto-list">
            <div class="crypto-item" v-for="c in cryptoItems" :key="c.label">
              <el-icon :style="{ color: c.color }"><component :is="c.icon" /></el-icon>
              <div>
                <div class="crypto-label">{{ c.label }}</div>
                <div class="crypto-value">{{ c.value }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import request from '../api/request'

const { t } = useI18n()
import PageHeader from '../components/PageHeader.vue'
import StatCard from '../components/StatCard.vue'

const stats = ref({})
const trends = ref([])
const maxOrders = computed(() => Math.max(1, ...trends.value.map(d => d.orderCount)))
const maxRedeems = computed(() => Math.max(1, ...trends.value.map(d => d.redeemCount)))
function barHeight(val, max) { return `${Math.max(4, (val / max) * 100)}px` }
const progressColors = [
  { color: '#4f6ef7', percentage: 30 },
  { color: '#7c3aed', percentage: 70 },
  { color: '#10b981', percentage: 100 }
]

const statCards = computed(() => [
  { label: t('admin.totalCards'), value: stats.value.totalCards || 0, icon: 'Ticket', color: '#4f6ef7' },
  { label: t('admin.used'), value: stats.value.usedCards || 0, icon: 'CircleCheck', color: '#10b981' },
  { label: t('admin.unused'), value: stats.value.unusedCards || 0, icon: 'Clock', color: '#f59e0b' },
  { label: t('admin.todayRedeems'), value: stats.value.todayRedeems || 0, icon: 'TrendCharts', color: '#6366f1' },
  { label: t('admin.todayOrders'), value: stats.value.todayOrders || 0, icon: 'ShoppingCart', color: '#0ea5e9' },
  { label: t('admin.totalProducts'), value: stats.value.totalProducts || 0, icon: 'Goods', color: '#ec4899' },
  { label: t('admin.totalBatches'), value: stats.value.totalBatches || 0, icon: 'Files', color: '#14b8a6' }
])

const cryptoItems = computed(() => [
  { label: t('admin.cryptoCardStorage'), value: t('admin.cryptoCardStorageValue'), icon: 'Lock', color: '#4f6ef7' },
  { label: t('admin.cryptoMetadata'), value: t('admin.cryptoMetadataValue'), icon: 'Key', color: '#7c3aed' },
  { label: t('admin.cryptoPassword'), value: t('admin.cryptoPasswordValue'), icon: 'Shield', color: '#10b981' },
  { label: t('admin.cryptoApiSign'), value: t('admin.cryptoApiSignValue'), icon: 'Stamp', color: '#f59e0b' },
  { label: t('admin.cryptoConcurrency'), value: t('admin.cryptoConcurrencyValue'), icon: 'Lightning', color: '#ef4444' }
])

onMounted(async () => {
  const [statsRes, trendsRes] = await Promise.all([
    request.get('/admin/dashboard'),
    request.get('/admin/dashboard/trends')
  ])
  stats.value = statsRes.data
  trends.value = trendsRes.data?.days || []
})
</script>

<style scoped>
.stat-row .el-col { margin-bottom: 16px; }
.stock-alert { margin-bottom: 16px; }
.stock-list { margin: 8px 0 0; padding-left: 18px; font-size: 13px; }
.stock-list li { margin: 4px 0; }
.usage-body { display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 24px !important; }
.usage-detail { display: flex; gap: 24px; font-size: 13px; color: var(--text-secondary); }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; }
.dot.used { background: #10b981; }
.dot.unused { background: #f59e0b; }
.crypto-list { display: flex; flex-direction: column; gap: 14px; }
.crypto-item { display: flex; align-items: center; gap: 12px; }
.crypto-label { font-size: 13px; font-weight: 600; }
.crypto-value { font-size: 12px; color: var(--text-secondary); }
.trend-chart { display: flex; align-items: flex-end; gap: 12px; min-height: 120px; padding-top: 8px; }
.trend-bar-group { flex: 1; text-align: center; }
.bars { display: flex; gap: 4px; justify-content: center; align-items: flex-end; height: 100px; }
.bar { width: 14px; border-radius: 4px 4px 0 0; min-height: 4px; }
.bar.orders { background: #4f6ef7; }
.bar.redeems { background: #10b981; }
.trend-label { font-size: 11px; color: var(--text-secondary); margin-top: 6px; display: block; }
.trend-legend { display: flex; gap: 16px; font-size: 12px; color: var(--text-secondary); }
.legend-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; }
.legend-dot.orders { background: #4f6ef7; }
.legend-dot.redeems { background: #10b981; }
.card-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
</style>
