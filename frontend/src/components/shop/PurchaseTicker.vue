<template>
  <div class="ticker-wrap" v-if="items.length">
    <div class="ticker-label"><YuIcon name="fire" size="sm" /> {{ t('landing.ticker.label') }}</div>
    <div class="ticker-track">
      <div class="ticker-content" :style="{ animationDuration: duration + 's' }">
        <span v-for="(item, i) in doubled" :key="i" class="ticker-item">
          <span class="dot" />
          {{ t('landing.ticker.bought', { user: item.user, product: item.product }) }}
          <em>¥{{ item.price }}</em>
          <span class="time">{{ item.time }}</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import YuIcon from '../icons/YuIcon.vue'
import shopHttp from '../../api/shopHttp'

const { t } = useI18n()

defineProps({
  products: { type: Array, default: () => [] },
  duration: { type: Number, default: 35 }
})

const items = ref([])

const doubled = computed(() => [...items.value, ...items.value])

onMounted(async () => {
  try {
    const res = await shopHttp.get('/shop/orders/recent', { params: { limit: 8 } })
    const list = res.data?.data
    if (Array.isArray(list) && list.length) {
      items.value = list.map(r => ({
        user: r.user,
        product: r.product,
        price: Number(r.price).toFixed(2),
        time: r.time
      }))
      return
    }
  } catch { /* fallback */ }
  items.value = [
    { user: '张**', product: '月度会员卡', price: '29.00', time: t('landing.ticker.justNow') },
    { user: '李**', product: '季度授权码', price: '79.00', time: t('landing.ticker.minAgo', { n: 1 }) },
    { user: '王**', product: '年度旗舰版', price: '199.00', time: t('landing.ticker.minAgo', { n: 2 }) }
  ]
})
</script>

<style scoped>
.ticker-wrap {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--shop-ticker-bg);
  border: 1px solid var(--shop-ticker-border);
  border-radius: 12px;
  padding: 10px 16px;
  overflow: hidden;
  margin-bottom: 48px;
}
.ticker-label {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: var(--shop-ticker-label);
  white-space: nowrap;
}
.ticker-track { flex: 1; overflow: hidden; mask-image: linear-gradient(90deg, transparent, #000 8%, #000 92%, transparent); }
.ticker-content {
  display: flex;
  gap: 48px;
  white-space: nowrap;
  animation: ticker-scroll linear infinite;
}
.ticker-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--shop-text-muted);
}
.ticker-item strong { color: var(--shop-ticker-strong); }
.ticker-item em { color: var(--danger); font-style: normal; font-weight: 600; }
.ticker-item .time { color: var(--shop-text-muted); font-size: 12px; opacity: 0.8; }
.dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #22c55e;
  animation: pulse 2s infinite;
}
@keyframes ticker-scroll {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
