<template>
  <div class="preview-composite" :class="{ 'has-phone': showPhone }">
  <div class="live-preview">
    <div class="mockup-bar"><span /><span /><span /></div>
    <div class="mockup-url">shop.yu-kami.com</div>
    <div class="preview-viewport" ref="viewportRef">
      <div v-if="checking && !screenshotMode" class="preview-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>正在检测服务状态…</p>
      </div>
      <div v-else-if="!active" class="preview-state preview-offline">
        <div class="state-icon"><YuIcon name="monitor" size="2xl" /></div>
        <p class="state-title">真实界面预览需前后端同时运行</p>
        <p class="state-desc">启动 MySQL 与后端后，此处将展示商城商品、购买页与发卡成功界面。</p>
        <div class="state-steps">
          <code>cd backend && mvnw spring-boot:run</code>
          <code>cd frontend && npm run dev</code>
        </div>
        <el-button size="small" round :loading="checking" @click="recheckAll">重新检测</el-button>
      </div>
      <div v-else-if="screenshotMode" class="preview-shot-wrap">
        <img :key="view" :src="currentScreenshot" class="preview-shot" alt="商城界面预览" />
      </div>
      <div v-else class="preview-scale">
        <iframe
          :key="iframeSrc"
          :src="iframeSrc"
          class="preview-iframe"
          title="商城界面预览"
          scrolling="no"
          loading="lazy"
        />
      </div>
    </div>
    <div v-if="showDots && active && !lockView" class="preview-dots">
      <button
        v-for="v in views"
        :key="v.id"
        :class="['dot', { active: view === v.id }]"
        :title="v.label"
        @click="view = v.id"
      />
    </div>
    <div v-if="active && (lockView ? initialView === 'success' : view === 'success')" class="mockup-toast live">
      <YuIcon name="check" size="sm" /> 支付成功，卡密已发放
    </div>
  </div>

  <div v-if="showPhone && active" class="phone-float floating-delayed">
    <div class="phone-frame">
      <div class="phone-notch" />
      <div class="phone-screen" ref="phoneViewportRef">
        <img
          v-if="screenshotMode"
          src="/screenshots/preview-mobile.png"
          class="phone-shot"
          alt="手机端商城预览"
        />
        <div v-else class="phone-scale">
          <iframe
            src="/shop/embed/mobile"
            class="phone-iframe"
            title="手机端商城预览"
            scrolling="no"
            loading="lazy"
          />
        </div>
      </div>
      <div class="phone-home-bar" />
    </div>
    <span class="phone-label">移动端</span>
  </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { useShopPreviewReady } from '../../composables/useShopPreviewReady'
import YuIcon from '../icons/YuIcon.vue'

const IFRAME_W = 1024
const IFRAME_H = 640
const PHONE_W = 390
const PHONE_H = 780
const viewportRef = ref(null)
const phoneViewportRef = ref(null)

const SCREENSHOT_MAP = {
  list: '/screenshots/preview-list.png',
  buy: '/screenshots/preview-buy.png',
  success: '/screenshots/preview-success.png'
}

const props = defineProps({
  initialView: { type: String, default: 'list' },
  autoRotate: { type: Boolean, default: true },
  showDots: { type: Boolean, default: true },
  /** Hero 区右侧叠加手机预览 */
  showPhone: { type: Boolean, default: false },
  /** 锁定为 initialView 对应截图，不随轮播切换 */
  lockView: { type: Boolean, default: false }
})

const { ready, productId, checking, recheck } = useShopPreviewReady()
const screenshotMode = ref(false)

const view = ref(props.initialView)
let timer
let resizeObs

const views = [
  { id: 'list', label: '商城首页' },
  { id: 'buy', label: '购买页' },
  { id: 'success', label: '发卡成功' }
]

const active = computed(() => screenshotMode.value || ready.value)
const effectiveView = computed(() => (props.lockView ? props.initialView : view.value))
const currentScreenshot = computed(() => SCREENSHOT_MAP[effectiveView.value])

const iframeSrc = computed(() => {
  if (!ready.value || screenshotMode.value) return ''
  if (effectiveView.value === 'list') return '/shop/embed/products'
  if (effectiveView.value === 'buy') return `/shop/embed/buy/${productId.value}`
  return '/shop/embed/success'
})

function rotate() {
  if (!active.value || props.lockView || !props.autoRotate) return
  const order = ['list', 'buy', 'success']
  const idx = order.indexOf(view.value)
  view.value = order[(idx + 1) % order.length]
}

function fitIframe() {
  const el = viewportRef.value
  if (el) {
    const scale = Math.min(el.clientWidth / IFRAME_W, el.clientHeight / IFRAME_H, 1)
    el.style.setProperty('--preview-scale', String(scale))
  }
  const phoneEl = phoneViewportRef.value
  if (phoneEl && props.showPhone && !screenshotMode.value) {
    const phoneScale = phoneEl.clientWidth / PHONE_W
    phoneEl.style.setProperty('--phone-scale', String(phoneScale))
  }
}

function startRotate() {
  clearInterval(timer)
  if (props.autoRotate && active.value) timer = setInterval(rotate, 4500)
}

async function checkScreenshots() {
  try {
    const res = await fetch(SCREENSHOT_MAP.list, { method: 'HEAD' })
    screenshotMode.value = res.ok
  } catch {
    screenshotMode.value = false
  }
}

async function recheckAll() {
  await checkScreenshots()
  if (!screenshotMode.value) recheck()
}

watch(active, (v) => {
  if (v) {
    nextTick(() => fitIframe())
    startRotate()
  } else {
    clearInterval(timer)
  }
})

watch(screenshotMode, () => nextTick(() => fitIframe()))

onMounted(async () => {
  await checkScreenshots()
  fitIframe()
  resizeObs = new ResizeObserver(fitIframe)
  if (viewportRef.value) resizeObs.observe(viewportRef.value)
  if (phoneViewportRef.value) resizeObs.observe(phoneViewportRef.value)
  if (active.value) startRotate()
})

onUnmounted(() => {
  clearInterval(timer)
  resizeObs?.disconnect()
})
</script>

<style scoped>
.preview-composite {
  position: relative;
  width: 100%;
}
.preview-composite.has-phone {
  padding-right: 56px;
}
.live-preview {
  background: var(--shop-mockup-bg);
  border-radius: 20px;
  box-shadow: var(--shop-mockup-shadow);
  overflow: hidden;
  border: 1px solid var(--shop-border);
  width: 100%;
  max-width: 100%;
}
.mockup-bar {
  background: var(--shop-mockup-bar);
  padding: 12px 16px;
  display: flex;
  gap: 6px;
}
.mockup-bar span { width: 10px; height: 10px; border-radius: 50%; background: #cbd5e1; }
.mockup-bar span:first-child { background: #f87171; }
.mockup-bar span:nth-child(2) { background: #fbbf24; }
.mockup-bar span:nth-child(3) { background: #34d399; }
.mockup-url {
  font-size: 11px;
  color: #94a3b8;
  font-family: ui-monospace, monospace;
  padding: 8px 16px 0;
}
.preview-viewport {
  --preview-scale: 0.52;
  height: 360px;
  overflow: hidden;
  position: relative;
  background: var(--shop-preview-bg);
  width: 100%;
}
.preview-composite.has-phone .preview-viewport {
  height: 400px;
}
.preview-shot-wrap {
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: var(--shop-preview-bg);
}
.preview-shot {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: top center;
  display: block;
  background: var(--shop-preview-bg);
}
.preview-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 24px;
  text-align: center;
  color: var(--shop-text-muted);
}
.preview-state .is-loading { font-size: 28px; color: #4f6ef7; }
.preview-offline .state-icon {
  width: 56px; height: 56px;
  display: flex; align-items: center; justify-content: center;
  background: var(--shop-accent-soft); color: #4f6ef7; border-radius: 14px;
}
.preview-offline .state-title { font-size: 14px; font-weight: 700; color: var(--shop-text); margin: 0; }
.preview-offline .state-desc { font-size: 12px; line-height: 1.6; margin: 0; max-width: 320px; }
.state-steps {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin: 8px 0 4px;
  width: 100%;
  max-width: 340px;
}
.state-steps code {
  display: block;
  font-size: 10px;
  background: var(--shop-preview-inner);
  border: 1px solid var(--shop-border);
  border-radius: 6px;
  padding: 6px 8px;
  color: var(--shop-text-soft);
  text-align: left;
  word-break: break-all;
}
.preview-scale {
  position: absolute;
  top: 0;
  left: 0;
  width: calc(1024px * var(--preview-scale));
  height: calc(640px * var(--preview-scale));
  overflow: hidden;
}
.preview-iframe {
  width: 1024px;
  height: 640px;
  border: none;
  transform: scale(var(--preview-scale));
  transform-origin: top left;
  pointer-events: none;
  background: var(--shop-preview-bg);
  display: block;
}
.preview-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: var(--shop-mockup-bg);
}
.dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  border: none;
  background: var(--shop-m-line);
  cursor: pointer;
  padding: 0;
  transition: all 0.25s;
}
.dot.active { background: #4f6ef7; width: 20px; border-radius: 4px; }
.mockup-toast.live {
  margin: 0 12px 12px;
  background: #f0fdf4;
  color: #16a34a;
  font-size: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  text-align: center;
  animation: fadeInUp 0.4s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 手机叠加预览 */
.phone-float {
  position: absolute;
  right: -8px;
  bottom: 56px;
  z-index: 4;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.floating-delayed {
  animation: float 5s ease-in-out infinite;
  animation-delay: 0.6s;
}
.phone-frame {
  width: 156px;
  background: linear-gradient(160deg, #1e293b, #0f172a);
  border-radius: 32px;
  padding: 10px 8px 12px;
  box-shadow:
    0 24px 56px rgba(0, 0, 0, 0.35),
    0 0 0 1px rgba(255, 255, 255, 0.08) inset;
  border: 2px solid #334155;
}
.phone-notch {
  width: 56px;
  height: 5px;
  background: #334155;
  border-radius: 3px;
  margin: 0 auto 8px;
}
.phone-screen {
  --phone-scale: 0.38;
  height: 292px;
  border-radius: 20px;
  overflow: hidden;
  background: #f8fafc;
  position: relative;
}
.phone-shot {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: top center;
  display: block;
}
.phone-scale {
  position: absolute;
  top: 0;
  left: 0;
  width: calc(390px * var(--phone-scale));
  height: calc(780px * var(--phone-scale));
  overflow: hidden;
}
.phone-iframe {
  width: 390px;
  height: 780px;
  border: none;
  transform: scale(var(--phone-scale));
  transform-origin: top left;
  pointer-events: none;
  background: #f8fafc;
  display: block;
}
.phone-home-bar {
  width: 40px;
  height: 4px;
  background: #475569;
  border-radius: 2px;
  margin: 8px auto 0;
}
.phone-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--shop-text-muted, #64748b);
  letter-spacing: 0.02em;
}
@media (max-width: 900px) {
  .preview-composite.has-phone { padding-right: 0; }
  .phone-float { display: none; }
}
</style>
