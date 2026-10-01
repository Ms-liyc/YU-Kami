<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="4" v-for="item in statCards" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top:20px">
      <template #header>加密体系</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="卡密存储">HMAC-SHA256 + 独立 Pepper 盐</el-descriptions-item>
        <el-descriptions-item label="元数据加密">AES-256-GCM</el-descriptions-item>
        <el-descriptions-item label="管理员密码">BCrypt</el-descriptions-item>
        <el-descriptions-item label="API 签名">RSA-SHA256</el-descriptions-item>
        <el-descriptions-item label="卡密校验">SHA-256 校验码</el-descriptions-item>
        <el-descriptions-item label="高并发保障">Redis 分布式锁 + 乐观锁 + 限流</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../api/request'

const stats = ref({})

const statCards = computed(() => [
  { label: '卡密总数', value: stats.value.totalCards || 0 },
  { label: '已使用', value: stats.value.usedCards || 0 },
  { label: '未使用', value: stats.value.unusedCards || 0 },
  { label: '今日兑换', value: stats.value.todayRedeems || 0 },
  { label: '产品数', value: stats.value.totalProducts || 0 },
  { label: '批次数', value: stats.value.totalBatches || 0 }
])

onMounted(async () => {
  const res = await request.get('/admin/dashboard')
  stats.value = res.data
})
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: 700; color: #409eff; }
.stat-label { color: #999; margin-top: 8px; font-size: 14px; }
</style>
