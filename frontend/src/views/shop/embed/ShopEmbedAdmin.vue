<template>
  <div class="embed-admin">
    <aside class="sidebar">
      <div class="logo">
        <img src="/logo.png" alt="" />
        <span>YU-Kami</span>
      </div>
      <nav class="menu">
        <a class="menu-item active"><span class="mi-dot" />{{ t('nav.dashboard') }}</a>
        <a class="menu-item"><span class="mi-dot" />{{ t('nav.products') }}</a>
        <a class="menu-item"><span class="mi-dot" />{{ t('nav.cards') }}</a>
        <a class="menu-item"><span class="mi-dot" />{{ t('nav.batches') }}</a>
        <a class="menu-item"><span class="mi-dot" />{{ t('nav.orders') }}</a>
        <a class="menu-item"><span class="mi-dot" />{{ t('nav.promotions') }}</a>
      </nav>
      <div class="aside-footer">v{{ APP_VERSION }}</div>
    </aside>
    <main class="main">
      <header class="topbar">
        <span class="breadcrumb">{{ t('landing.embedAdmin.breadcrumb') }}</span>
        <span class="user">admin</span>
      </header>
      <div class="content">
        <h1 class="page-title">{{ t('landing.embedAdmin.pageTitle') }}</h1>
        <p class="page-sub">{{ t('landing.embedAdmin.pageSub') }}</p>
        <div class="stat-row">
          <div v-for="s in stats" :key="s.label" class="stat-card" :style="{ '--accent': s.color }">
            <div class="stat-icon">{{ s.abbr }}</div>
            <div>
              <div class="stat-val">{{ s.value }}</div>
              <div class="stat-label">{{ s.label }}</div>
            </div>
          </div>
        </div>
        <div class="panels">
          <div class="panel table-panel">
            <div class="panel-head">{{ t('landing.embedAdmin.recentRedeems') }}</div>
            <table>
              <thead>
                <tr>
                  <th>{{ t('admin.recordUser') }}</th>
                  <th>{{ t('admin.recordResult') }}</th>
                  <th>{{ t('admin.recordMessage') }}</th>
                  <th>{{ t('admin.recordTime') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in redeems" :key="r.user">
                  <td>{{ r.user }}</td>
                  <td><span class="tag ok">{{ r.result }}</span></td>
                  <td>{{ r.msg }}</td>
                  <td class="muted">{{ r.time }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="panel chart-panel">
            <div class="panel-head">{{ t('landing.embedAdmin.usageRate') }}</div>
            <div class="chart-ring">
              <div class="ring">68%</div>
              <div class="ring-legend">
                <div><span class="dot used" />{{ t('landing.embedAdmin.used') }} 352</div>
                <div><span class="dot free" />{{ t('landing.embedAdmin.unused') }} 168</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { APP_VERSION } from '../../../constants/version'

const { t } = useI18n()

const stats = computed(() => [
  { label: t('admin.totalCards'), value: '520', abbr: t('landing.embedAdmin.statAbbr.cards'), color: '#4f6ef7' },
  { label: t('admin.used'), value: '352', abbr: t('landing.embedAdmin.statAbbr.used'), color: '#10b981' },
  { label: t('admin.unused'), value: '168', abbr: t('landing.embedAdmin.statAbbr.unused'), color: '#f59e0b' },
  { label: t('nav.orders'), value: '128', abbr: t('landing.embedAdmin.statAbbr.orders'), color: '#6366f1' },
  { label: t('admin.todayOrders'), value: '¥2,860', abbr: '¥', color: '#ef4444' },
  { label: t('landing.stats.users'), value: '84', abbr: t('landing.embedAdmin.statAbbr.users'), color: '#8b5cf6' }
])

const redeems = [
  { user: 'demo', result: 'SUCCESS', msg: 'OK', time: '10-02 12:30' },
  { user: 'user_1024', result: 'SUCCESS', msg: 'Delivered', time: '10-02 11:15' },
  { user: 'test_buyer', result: 'FAIL', msg: 'Used', time: '10-02 10:42' }
]
</script>

<style scoped>
.embed-admin {
  display: flex;
  min-height: 100vh;
  background: var(--shop-bg, var(--page-bg));
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
}
.sidebar {
  width: 200px;
  flex-shrink: 0;
  background: var(--sidebar-bg);
  color: #94a3b8;
  display: flex;
  flex-direction: column;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 16px;
  border-bottom: 1px solid #1e293b;
}
.logo img { width: 32px; height: 32px; border-radius: 8px; }
.logo span { color: #fff; font-weight: 800; font-size: 15px; }
.menu { flex: 1; padding: 12px 8px; }
.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  margin-bottom: 4px;
  color: #94a3b8;
}
.menu-item.active { background: #4f6ef7; color: #fff; }
.mi-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: currentColor; opacity: 0.5; flex-shrink: 0;
}
.menu-item.active .mi-dot { opacity: 1; background: #fff; }
.aside-footer { padding: 12px 16px; font-size: 11px; color: #475569; border-top: 1px solid #1e293b; }
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: var(--shop-card, var(--header-bg));
  border-bottom: 1px solid var(--shop-border, var(--border));
  font-size: 13px;
}
.breadcrumb { color: var(--shop-text-muted, var(--text-secondary)); }
.user { color: var(--shop-text, var(--text-primary)); font-weight: 600; }
.content { padding: 20px 24px; flex: 1; }
.page-title { font-size: 20px; font-weight: 700; color: var(--shop-text, var(--text-primary)); margin-bottom: 4px; }
.page-sub { font-size: 13px; color: var(--shop-text-muted, var(--text-secondary)); margin-bottom: 20px; }
.stat-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.stat-card {
  background: var(--shop-card, var(--card-bg));
  border: 1px solid var(--shop-border, var(--border));
  border-radius: 12px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: var(--shadow);
}
.stat-icon {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  background: color-mix(in srgb, var(--accent) 12%, transparent);
  font-size: 13px;
  font-weight: 700;
}
.stat-val { font-size: 20px; font-weight: 700; color: var(--shop-text, var(--text-primary)); line-height: 1.2; }
.stat-label { font-size: 12px; color: var(--shop-text-muted, var(--text-secondary)); }
.panels { display: grid; grid-template-columns: 1.6fr 1fr; gap: 12px; }
.panel {
  background: var(--shop-card, var(--card-bg));
  border: 1px solid var(--shop-border, var(--border));
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow);
}
.panel-head {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--shop-text, var(--text-primary));
  border-bottom: 1px solid var(--shop-border, var(--border));
}
table { width: 100%; border-collapse: collapse; font-size: 12px; }
th {
  text-align: left;
  padding: 10px 16px;
  color: var(--shop-text-muted, var(--text-secondary));
  font-weight: 500;
  background: var(--shop-section-alt, var(--page-bg));
}
td { padding: 10px 16px; color: var(--shop-text, var(--text-primary)); border-top: 1px solid var(--shop-border, var(--border)); }
.muted { color: var(--shop-text-muted, var(--text-secondary)); }
.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}
.tag.ok { background: color-mix(in srgb, var(--success) 15%, transparent); color: var(--success); }
.chart-panel .chart-ring { padding: 20px; text-align: center; }
.ring {
  width: 100px; height: 100px;
  margin: 0 auto 16px;
  border-radius: 50%;
  border: 8px solid var(--primary);
  border-right-color: var(--shop-border, var(--border));
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 800; color: var(--primary);
}
.ring-legend { font-size: 12px; color: var(--shop-text-muted, var(--text-secondary)); line-height: 1.8; }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; }
.dot.used { background: var(--primary); }
.dot.free { background: var(--shop-border, var(--border)); }
</style>
