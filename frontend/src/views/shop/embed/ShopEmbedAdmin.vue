<template>
  <div class="embed-admin">
    <aside class="sidebar">
      <div class="logo">
        <img src="/logo.png" alt="" />
        <span>YU-Kami</span>
      </div>
      <nav class="menu">
        <a class="menu-item active"><span class="mi-dot" />仪表盘</a>
        <a class="menu-item"><span class="mi-dot" />商品管理</a>
        <a class="menu-item"><span class="mi-dot" />卡密管理</a>
        <a class="menu-item"><span class="mi-dot" />批次管理</a>
        <a class="menu-item"><span class="mi-dot" />商城订单</a>
        <a class="menu-item"><span class="mi-dot" />促销活动</a>
      </nav>
      <div class="aside-footer">v{{ APP_VERSION }}</div>
    </aside>
    <main class="main">
      <header class="topbar">
        <span class="breadcrumb">首页 / 仪表盘</span>
        <span class="user">admin</span>
      </header>
      <div class="content">
        <h1 class="page-title">经营概览</h1>
        <p class="page-sub">卡密发卡与商城销售数据一览</p>
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
            <div class="panel-head">最近兑换记录</div>
            <table>
              <thead>
                <tr><th>用户</th><th>结果</th><th>消息</th><th>时间</th></tr>
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
            <div class="panel-head">卡密使用率</div>
            <div class="chart-ring">
              <div class="ring">68%</div>
              <div class="ring-legend">
                <div><span class="dot used" />已使用 352</div>
                <div><span class="dot free" />未使用 168</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { APP_VERSION } from '../../../constants/version'

const stats = [
  { label: '卡密总数', value: '520', abbr: '卡', color: '#4f6ef7' },
  { label: '已使用', value: '352', abbr: '用', color: '#10b981' },
  { label: '未使用', value: '168', abbr: '余', color: '#f59e0b' },
  { label: '商城订单', value: '128', abbr: '单', color: '#6366f1' },
  { label: '今日成交', value: '¥2,860', abbr: '¥', color: '#ef4444' },
  { label: '活跃用户', value: '84', abbr: '人', color: '#8b5cf6' }
]

const redeems = [
  { user: 'demo', result: 'SUCCESS', msg: '兑换成功', time: '10-02 12:30' },
  { user: 'user_1024', result: 'SUCCESS', msg: '卡密已发放', time: '10-02 11:15' },
  { user: 'test_buyer', result: 'FAIL', msg: '卡密已使用', time: '10-02 10:42' }
]
</script>

<style scoped>
.embed-admin {
  display: flex;
  min-height: 100vh;
  background: #f1f5f9;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
}
.sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #0f172a;
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
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  font-size: 13px;
}
.breadcrumb { color: #64748b; }
.user { color: #334155; font-weight: 600; }
.content { padding: 20px 24px; flex: 1; }
.page-title { font-size: 20px; font-weight: 700; color: #1e293b; margin-bottom: 4px; }
.page-sub { font-size: 13px; color: #64748b; margin-bottom: 20px; }
.stat-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.stat-icon {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  background: color-mix(in srgb, var(--accent) 12%, transparent);
  font-size: 13px;
  font-weight: 700;
}
.stat-val { font-size: 20px; font-weight: 700; color: #1e293b; line-height: 1.2; }
.stat-label { font-size: 12px; color: #64748b; }
.panels { display: grid; grid-template-columns: 1.6fr 1fr; gap: 12px; }
.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.panel-head {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  border-bottom: 1px solid #e2e8f0;
}
table { width: 100%; border-collapse: collapse; font-size: 12px; }
th {
  text-align: left;
  padding: 10px 16px;
  color: #64748b;
  font-weight: 500;
  background: #f8fafc;
}
td { padding: 10px 16px; color: #334155; border-top: 1px solid #f1f5f9; }
.muted { color: #94a3b8; }
.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}
.tag.ok { background: #dcfce7; color: #16a34a; }
.chart-panel .chart-ring { padding: 20px; text-align: center; }
.ring {
  width: 100px; height: 100px;
  margin: 0 auto 16px;
  border-radius: 50%;
  border: 8px solid #4f6ef7;
  border-right-color: #e2e8f0;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 800; color: #4f6ef7;
}
.ring-legend { font-size: 12px; color: #64748b; line-height: 1.8; }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; }
.dot.used { background: #4f6ef7; }
.dot.free { background: #e2e8f0; }
</style>
