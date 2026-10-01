<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon><Key /></el-icon>
        <span>YU-Kami</span>
      </div>
      <el-menu :default-active="route.path" router background-color="#1d1e2c" text-color="#a0a3bd" active-text-color="#fff">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon>数据概览</el-menu-item>
        <el-menu-item index="/products"><el-icon><Goods /></el-icon>产品管理</el-menu-item>
        <el-menu-item index="/cards"><el-icon><Ticket /></el-icon>卡密管理</el-menu-item>
        <el-menu-item index="/batches"><el-icon><Files /></el-icon>批次管理</el-menu-item>
        <el-menu-item index="/records"><el-icon><Document /></el-icon>兑换记录</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">屿宸科技 · 企业级卡密系统</span>
        <div class="user-info">
          <span>{{ auth.nickname || auth.username }}</span>
          <el-button type="danger" link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #1d1e2c; }
.logo {
  height: 60px; display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 18px; font-weight: 600; gap: 8px;
}
.header {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; border-bottom: 1px solid #eee; padding: 0 24px;
}
.title { font-size: 16px; color: #333; }
.user-info { display: flex; align-items: center; gap: 12px; }
.main { background: #f5f7fa; padding: 20px; }
</style>
