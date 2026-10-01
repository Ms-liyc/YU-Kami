<template>
  <div class="page-container">
    <PageHeader title="数据概览" subtitle="卡密系统运营数据实时监控" />

    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="8" :md="4" v-for="item in statCards" :key="item.label">
        <StatCard :label="item.label" :value="item.value" :icon="item.icon" :color="item.color" />
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :span="16">
        <div class="page-card">
          <div class="card-header"><h3>最近兑换记录</h3></div>
          <div class="card-body" style="padding: 0">
            <el-table :data="stats.recentRedeems || []" stripe empty-text="暂无兑换记录">
              <el-table-column prop="redeemUser" label="用户" width="140" />
              <el-table-column prop="result" label="结果" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small" effect="light">
                    {{ row.result }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="消息" show-overflow-tooltip />
              <el-table-column prop="redeemIp" label="IP" width="130" />
              <el-table-column prop="createdAt" label="时间" width="170" />
            </el-table>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="page-card" style="margin-bottom: 16px">
          <div class="card-header"><h3>使用率</h3></div>
          <div class="card-body usage-body">
            <el-progress type="dashboard" :percentage="Math.round(stats.usageRate || 0)" :width="140"
              :color="progressColors" />
            <div class="usage-detail">
              <div><span class="dot used"></span>已使用 {{ stats.usedCards || 0 }}</div>
              <div><span class="dot unused"></span>未使用 {{ stats.unusedCards || 0 }}</div>
            </div>
          </div>
        </div>
        <div class="page-card">
          <div class="card-header"><h3>加密体系</h3></div>
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
import request from '../api/request'
import PageHeader from '../components/PageHeader.vue'
import StatCard from '../components/StatCard.vue'

const stats = ref({})
const progressColors = [
  { color: '#4f6ef7', percentage: 30 },
  { color: '#7c3aed', percentage: 70 },
  { color: '#10b981', percentage: 100 }
]

const statCards = computed(() => [
  { label: '卡密总数', value: stats.value.totalCards || 0, icon: 'Ticket', color: '#4f6ef7' },
  { label: '已使用', value: stats.value.usedCards || 0, icon: 'CircleCheck', color: '#10b981' },
  { label: '未使用', value: stats.value.unusedCards || 0, icon: 'Clock', color: '#f59e0b' },
  { label: '今日兑换', value: stats.value.todayRedeems || 0, icon: 'TrendCharts', color: '#6366f1' },
  { label: '产品数', value: stats.value.totalProducts || 0, icon: 'Goods', color: '#ec4899' },
  { label: '批次数', value: stats.value.totalBatches || 0, icon: 'Files', color: '#14b8a6' }
])

const cryptoItems = [
  { label: '卡密存储', value: 'HMAC-SHA256 + Pepper', icon: 'Lock', color: '#4f6ef7' },
  { label: '元数据', value: 'AES-256-GCM', icon: 'Key', color: '#7c3aed' },
  { label: '密码', value: 'BCrypt', icon: 'Shield', color: '#10b981' },
  { label: 'API 签名', value: 'RSA-SHA256', icon: 'Stamp', color: '#f59e0b' },
  { label: '高并发', value: 'Redis 锁 + 乐观锁', icon: 'Lightning', color: '#ef4444' }
]

onMounted(async () => {
  const res = await request.get('/admin/dashboard')
  stats.value = res.data
})
</script>

<style scoped>
.stat-row .el-col { margin-bottom: 16px; }
.usage-body { display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 24px !important; }
.usage-detail { display: flex; gap: 24px; font-size: 13px; color: var(--text-secondary); }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; }
.dot.used { background: #10b981; }
.dot.unused { background: #f59e0b; }
.crypto-list { display: flex; flex-direction: column; gap: 14px; }
.crypto-item { display: flex; align-items: center; gap: 12px; }
.crypto-label { font-size: 13px; font-weight: 600; }
.crypto-value { font-size: 12px; color: var(--text-secondary); }
</style>
