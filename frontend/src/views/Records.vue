<template>
  <el-card>
    <template #header>兑换记录</template>
    <el-table :data="tableData" stripe>
      <el-table-column prop="redeemUser" label="用户" />
      <el-table-column prop="result" label="结果">
        <template #default="{ row }">
          <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'">{{ row.result }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="消息" />
      <el-table-column prop="redeemIp" label="IP" />
      <el-table-column prop="createdAt" label="时间" />
    </el-table>
    <el-pagination class="pagination" v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request'

const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

async function loadData() {
  const res = await request.get('/admin/redeem-records', { params: { page: page.value, size: size.value } })
  tableData.value = res.data.records
  total.value = res.data.total
}

onMounted(loadData)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
