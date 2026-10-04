<template>
  <div class="showcase reveal">
    <div class="showcase-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        :class="['tab-btn', { active: active === tab.id }]"
        @click="active = tab.id"
      >
        <YuIcon :name="tab.icon" size="sm" class="tab-icon" />
        {{ tab.label }}
      </button>
    </div>
    <transition name="fade-slide" mode="out-in">
      <div :key="active" class="showcase-panel">
        <div class="panel-mockup">
          <LiveShopPreview v-if="active === 'shop'" class="shop-live" :show-phone="false" />
          <ShowcasePreview
            v-else-if="active === 'admin'"
            screenshot="/screenshots/preview-admin.png"
            embed-url="/shop/embed/admin"
            url-label="admin.yu-kami.com"
            :alt="t('landing.showcase.adminAlt')"
          />
          <ShowcasePreview
            v-else
            screenshot="/screenshots/preview-mobile.png"
            embed-url="/shop/embed/mobile"
            url-label="shop.yu-kami.com"
            :alt="t('landing.showcase.mobileAlt')"
            phone
          />
        </div>
        <ul class="panel-features">
          <li v-for="f in currentFeatures" :key="f">
            <el-icon><Check /></el-icon>{{ f }}
          </li>
        </ul>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import LiveShopPreview from './LiveShopPreview.vue'
import ShowcasePreview from './ShowcasePreview.vue'
import YuIcon from '../icons/YuIcon.vue'

const { t, tm } = useI18n()
const active = ref('shop')

const tabs = computed(() => [
  { id: 'shop', label: t('landing.showcase.tabShop'), icon: 'storefront' },
  { id: 'admin', label: t('landing.showcase.tabAdmin'), icon: 'dashboard' },
  { id: 'mobile', label: t('landing.showcase.tabMobile'), icon: 'phone' }
])

const featureMap = computed(() => ({
  shop: tm('landing.showcase.shopFeatures'),
  admin: tm('landing.showcase.adminFeatures'),
  mobile: tm('landing.showcase.mobileFeatures')
}))

const currentFeatures = computed(() => featureMap.value[active.value] || [])
</script>

<style scoped>
.showcase { max-width: 1100px; margin: 0 auto; }
.showcase-tabs {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}
.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 24px;
  border: 1px solid var(--shop-border);
  background: var(--shop-card);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s;
  color: var(--shop-text-muted);
}
.tab-btn.active {
  background: linear-gradient(135deg, #4f6ef7, #6366f1);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 16px rgba(79,110,247,0.35);
}
.shop-live :deep(.live-preview) {
  box-shadow: var(--shop-mockup-shadow);
  max-width: 100%;
}
.showcase-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
  gap: 40px;
  align-items: center;
}
.panel-mockup {
  min-width: 0;
  max-width: 100%;
  overflow: hidden;
}
.panel-features { list-style: none; padding: 0; margin: 0; min-width: 0; }
.panel-features li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 0;
  font-size: 15px;
  line-height: 1.5;
  color: var(--shop-text-soft);
  border-bottom: 1px solid var(--shop-border);
  word-break: break-word;
}
.panel-features li .el-icon { color: #22c55e; }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateX(20px); }
.fade-slide-leave-to { opacity: 0; transform: translateX(-20px); }
@media (max-width: 768px) {
  .showcase-panel { grid-template-columns: 1fr; }
}
</style>
