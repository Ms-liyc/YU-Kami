<template>
  <div class="ticker-wrap" v-if="items.length">
    <div class="ticker-label"><YuIcon name="fire" size="sm" /> 实时成交</div>
    <div class="ticker-track">
      <div class="ticker-content" :style="{ animationDuration: duration + 's' }">
        <span v-for="(item, i) in doubled" :key="i" class="ticker-item">
          <span class="dot" />
          {{ item.user }} 购买了 <strong>{{ item.product }}</strong>
          <em>¥{{ item.price }}</em>
          <span class="time">{{ item.time }}</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import YuIcon from '../icons/YuIcon.vue'

const props = defineProps({
  products: { type: Array, default: () => [] },
  duration: { type: Number, default: 35 }
})

const masks = ['张**', '李**', '王**', '刘**', '陈**', '杨**', '赵**', '周**', '吴**', '徐**']
const times = ['刚刚', '1分钟前', '2分钟前', '3分钟前', '5分钟前']

const items = computed(() => {
  if (!props.products.length) {
    return [
      { user: '张**', product: '月度会员卡', price: '29.00', time: '刚刚' },
      { user: '李**', product: '年度授权码', price: '199.00', time: '1分钟前' },
      { user: '王**', product: '体验卡', price: '9.90', time: '2分钟前' }
    ]
  }
  return props.products.slice(0, 8).map((p, i) => ({
    user: masks[i % masks.length],
    product: p.name,
    price: (p.onSale ? p.salePrice : p.value)?.toFixed?.(2) ?? p.value,
    time: times[i % times.length]
  }))
})

const doubled = computed(() => [...items.value, ...items.value])
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
.ticker-item em { color: #ef4444; font-style: normal; font-weight: 600; }
.ticker-item .time { color: #94a3b8; font-size: 12px; }
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
