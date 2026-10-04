<template>
  <div class="stats-bar reveal" data-delay="100">
    <div v-for="s in stats" :key="s.label" class="stat-item">
      <div class="stat-value">
        <span class="num">{{ animated[s.key] }}</span>
        <span class="suffix">{{ s.suffix }}</span>
      </div>
      <div class="stat-label">{{ s.label }}</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import shopHttp from '../../api/shopHttp'

const { t } = useI18n()
const targets = reactive({ orders: 0, users: 0, cards: 0, uptime: 99.9 })
const animated = reactive({ orders: 0, users: 0, cards: 0, uptime: 0 })

const stats = computed(() => [
  { key: 'orders', label: t('landing.stats.orders'), suffix: '+' },
  { key: 'users', label: t('landing.stats.users'), suffix: '+' },
  { key: 'cards', label: t('landing.stats.cards'), suffix: '+' },
  { key: 'uptime', label: t('landing.stats.uptime'), suffix: '%' }
])

let started = false
let observer

function easeCount(key, to, duration = 2000, isFloat = false) {
  const start = performance.now()
  const tick = (now) => {
    const p = Math.min((now - start) / duration, 1)
    const eased = 1 - Math.pow(1 - p, 3)
    const v = to * eased
    animated[key] = isFloat ? v.toFixed(1) : Math.floor(v)
    if (p < 1) requestAnimationFrame(tick)
    else animated[key] = isFloat ? to.toFixed(1) : to
  }
  requestAnimationFrame(tick)
}

function startAnimation() {
  if (started) return
  started = true
  easeCount('orders', targets.orders)
  easeCount('users', targets.users)
  easeCount('cards', targets.cards)
  easeCount('uptime', targets.uptime, 1500, true)
}

async function loadStats() {
  try {
    const res = await shopHttp.get('/shop/stats')
    if (res.data?.code === 200 && res.data.data) {
      const d = res.data.data
      targets.orders = d.totalOrders > 0 ? d.totalOrders : 12860
      targets.users = d.totalUsers > 0 ? d.totalUsers : 3840
      targets.cards = d.cardsDelivered > 0 ? d.cardsDelivered : 52000
      targets.uptime = d.uptime || 99.9
    }
  } catch { /* defaults */ }
}

onMounted(async () => {
  await loadStats()
  const el = document.querySelector('.stats-bar')
  observer = new IntersectionObserver(([e]) => {
    if (e.isIntersecting) {
      startAnimation()
      observer.disconnect()
    }
  }, { threshold: 0.3 })
  if (el) observer.observe(el)
})

onUnmounted(() => observer?.disconnect())
</script>

<style scoped>
.stats-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 32px;
  background: var(--shop-card);
  border-radius: 20px;
  border: 1px solid var(--shop-border);
  box-shadow: var(--shop-card-shadow);
  margin: -40px auto 56px;
  max-width: 1000px;
  position: relative;
  z-index: 2;
}
.stat-item { text-align: center; }
.stat-value { margin-bottom: 6px; }
.num {
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 800;
  background: linear-gradient(135deg, #4f6ef7, #7c3aed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.suffix { font-size: 20px; font-weight: 700; color: var(--shop-link-hover); }
.stat-label { font-size: 13px; color: var(--shop-text-muted); }
@media (max-width: 640px) {
  .stats-bar { grid-template-columns: repeat(2, 1fr); margin-top: -24px; }
}
</style>
