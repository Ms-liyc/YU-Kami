<template>
  <div class="query-page">
    <div class="query-card reveal">
      <h1>订单查询</h1>
      <p>登录后可查看全部订单；也可凭订单号快速查询（需已登录账号）。</p>
      <el-form @submit.prevent="handleQuery">
        <el-form-item label="订单号">
          <el-input v-model="orderNo" placeholder="例如 O202601011200001234" clearable />
        </el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="handleQuery">
          查询订单
        </el-button>
      </el-form>
      <el-divider />
      <p class="hint">还没有账号？<router-link to="/shop/login">注册登录</router-link> 后可查看完整订单与卡密。</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useShopAuthStore } from '../../stores/shopAuth'
import { useScrollReveal } from '../../composables/useScrollReveal'

useScrollReveal()
const router = useRouter()
const auth = useShopAuthStore()
const orderNo = ref('')
const loading = ref(false)

async function handleQuery() {
  if (!orderNo.value.trim()) {
    ElMessage.warning('请输入订单号')
    return
  }
  if (!auth.token) {
    router.push({ path: '/shop/login', query: { redirect: '/shop/orders' } })
    return
  }
  loading.value = true
  try {
    router.push('/shop/orders')
    ElMessage.info('请在订单列表中查看：' + orderNo.value)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.query-page {
  display: flex;
  justify-content: center;
  padding: 48px 0;
}
.query-card {
  width: 100%;
  max-width: 480px;
  background: var(--shop-card);
  border-radius: 20px;
  padding: 40px;
  border: 1px solid var(--shop-border);
  box-shadow: var(--shop-card-shadow);
}
.query-card h1 { font-size: 24px; font-weight: 800; margin-bottom: 8px; color: var(--shop-text); }
.query-card p { color: var(--shop-text-muted); margin-bottom: 24px; font-size: 14px; }
.hint { font-size: 13px; color: var(--shop-text-muted); text-align: center; }
.hint a { color: #4f6ef7; }
</style>
