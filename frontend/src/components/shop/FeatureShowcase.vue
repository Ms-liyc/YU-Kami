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
            alt="管理后台预览"
          />
          <ShowcasePreview
            v-else
            screenshot="/screenshots/preview-mobile.png"
            embed-url="/shop/embed/mobile"
            url-label="shop.yu-kami.com"
            alt="手机端预览"
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
import LiveShopPreview from './LiveShopPreview.vue'
import ShowcasePreview from './ShowcasePreview.vue'
import YuIcon from '../icons/YuIcon.vue'

const active = ref('shop')

const tabs = [
  { id: 'shop', label: '商城前台', icon: 'storefront' },
  { id: 'admin', label: '管理后台', icon: 'dashboard' },
  { id: 'mobile', label: '手机端', icon: 'phone' }
]

const featureMap = {
  shop: ['分类导航与热销推荐', '促销价划线展示', '优惠券输入与预览', '游客注册登录购买', '订单页一键复制卡密'],
  admin: ['经营数据仪表盘', '商品/卡密/批次管理', '促销活动与优惠券', '订单筛选与支付配置', 'Webhook 与 API 客户端'],
  mobile: ['移动端完整适配', '微信内 JSAPI 支付', '扫码支付与状态轮询', '底部导航快速切页', '深色模式舒适浏览']
}

const currentFeatures = computed(() => featureMap[active.value])
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
