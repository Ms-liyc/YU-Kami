<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '240px'" class="aside">
      <div class="logo" @click="collapsed = !collapsed">
        <div class="logo-icon">YK</div>
        <transition name="fade">
          <span v-if="!collapsed" class="logo-text">YU-Kami</span>
        </transition>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="route.path"
          :collapse="collapsed"
          router
          class="side-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>数据概览</template>
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><Goods /></el-icon>
            <template #title>产品管理</template>
          </el-menu-item>
          <el-menu-item index="/cards">
            <el-icon><Ticket /></el-icon>
            <template #title>卡密管理</template>
          </el-menu-item>
          <el-menu-item index="/batches">
            <el-icon><Files /></el-icon>
            <template #title>批次管理</template>
          </el-menu-item>
          <el-menu-item index="/records">
            <el-icon><Document /></el-icon>
            <template #title>兑换记录</template>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><ShoppingCart /></el-icon>
            <template #title>{{ t('nav.orders') }}</template>
          </el-menu-item>
          <el-divider v-if="auth.isSuperAdmin" style="margin: 8px 16px; border-color: #334155" />
          <template v-if="auth.isSuperAdmin">
            <el-menu-item index="/api-clients">
              <el-icon><Connection /></el-icon>
              <template #title>API 客户端</template>
            </el-menu-item>
            <el-menu-item index="/users">
              <el-icon><User /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
            <el-menu-item index="/audit-logs">
              <el-icon><Notebook /></el-icon>
              <template #title>审计日志</template>
            </el-menu-item>
            <el-menu-item index="/webhooks">
              <el-icon><Bell /></el-icon>
              <template #title>Webhook</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
      <div class="aside-footer" v-if="!collapsed">
        <span>v1.4.0</span>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <router-link to="/shop" class="shop-link">{{ t('nav.shop') }}</router-link>
          <LanguageSwitcher />
          <el-tag size="small" effect="plain" type="success">运行中</el-tag>
          <el-dropdown trigger="click">
            <div class="user-dropdown">
              <el-avatar :size="32" class="avatar">{{ (auth.nickname || auth.username || 'A')[0] }}</el-avatar>
              <span class="user-name">{{ auth.nickname || auth.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <el-tag size="small">{{ roleLabel }}</el-tag>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'
import LanguageSwitcher from '../components/LanguageSwitcher.vue'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(false)

const titleMap = {
  '/dashboard': '数据概览',
  '/products': '产品管理',
  '/cards': '卡密管理',
  '/batches': '批次管理',
  '/records': '兑换记录',
  '/api-clients': 'API 客户端',
  '/users': '用户管理',
  '/audit-logs': '审计日志',
  '/webhooks': 'Webhook 管理',
  '/orders': '订单管理'
}

const currentTitle = computed(() => titleMap[route.path] || '')
const roleLabel = computed(() => auth.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员')

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside {
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  overflow: hidden;
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  cursor: pointer;
  border-bottom: 1px solid #1e293b;
}
.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #4f6ef7, #7c3aed);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 800;
  font-size: 13px;
  flex-shrink: 0;
}
.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
}
.menu-scroll { flex: 1; }
.side-menu {
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #94a3b8;
  --el-menu-hover-bg-color: var(--sidebar-hover);
  --el-menu-active-color: #fff;
}
.side-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(79,110,247,0.3), transparent);
  border-right: 3px solid var(--sidebar-active);
}
.aside-footer {
  padding: 12px 20px;
  color: #475569;
  font-size: 12px;
  border-top: 1px solid #1e293b;
}
.main-container { background: var(--page-bg); }
.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--header-bg);
  border-bottom: 1px solid var(--border);
  padding: 0 28px;
}
.header-right { display: flex; align-items: center; gap: 16px; }
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}
.user-dropdown:hover { background: #f1f5f9; }
.avatar { background: linear-gradient(135deg, #4f6ef7, #7c3aed); color: #fff; font-size: 14px; }
.user-name { font-size: 14px; color: var(--text-primary); }
.shop-link { color: #4f6ef7; font-size: 13px; text-decoration: none; margin-right: 4px; }
.main { padding: 24px 28px; }
.page-enter-active, .page-leave-active { transition: all 0.25s ease; }
.page-enter-from { opacity: 0; transform: translateX(12px); }
.page-leave-to { opacity: 0; transform: translateX(-12px); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
