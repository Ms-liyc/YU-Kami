<template>
  <div class="faka-landing">
    <!-- Hero 通栏背景 -->
    <div class="hero-band">
      <div class="hero-particles">
        <span v-for="i in 12" :key="i" class="hero-particle" :style="particleStyle(i)" />
      </div>
      <section class="hero">
      <div class="hero-inner reveal">
        <div class="hero-badge">
          <span class="badge-dot" />{{ t('landing.heroBadge') }}
        </div>
        <h1>
          {{ t('landing.heroTitle') }}<br />
          <span class="gradient-text">{{ t('landing.heroTitleHighlight') }}</span>
        </h1>
        <p class="hero-desc">
          {{ t('landing.heroDesc') }}
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round class="cta-btn" @click="scrollTo('products')">
            {{ t('landing.buyNow') }}
          </el-button>
          <el-button size="large" round @click="$router.push('/shop/query')">{{ t('shop.queryTitle') }}</el-button>
          <el-button size="large" round plain @click="$router.push('/shop/redeem')">{{ t('nav.redeem') }}</el-button>
          <el-button size="large" round plain @click="$router.push(auth.token ? '/shop/orders' : '/shop/login')">
            {{ auth.token ? t('nav.myOrders') : t('landing.loginRegister') }}
          </el-button>
        </div>
        <div class="tech-row">
          <span v-for="t in techStack" :key="t" class="tech-tag">{{ t }}</span>
        </div>
      </div>
      <div class="hero-mockup reveal" data-delay="150">
        <LiveShopPreview
          class="floating"
          show-phone
          lock-view
          initial-view="list"
          :auto-rotate="false"
          :show-dots="false"
        />
      </div>
      </section>

      <div class="hero-after">
        <PurchaseTicker :products="products" />
        <StatsBar />
      </div>
    </div>

    <!-- 公告 -->
    <section class="notice-bar reveal" data-delay="50">
      <el-carousel height="40px" direction="vertical" :autoplay="true" :interval="4000" indicator-position="none">
        <el-carousel-item v-for="n in notices" :key="n.text">
          <span class="notice-item"><YuIcon :name="n.icon" size="sm" />{{ n.text }}</span>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 品类 -->
    <section class="section" id="categories">
      <h2 class="section-title reveal">{{ t('landing.categoriesTitle') }}</h2>
      <p class="section-sub reveal" data-delay="80">{{ t('landing.categoriesSub') }}</p>
      <div class="category-grid">
        <div
          v-for="(c, i) in categories"
          :key="c.title"
          class="category-card reveal"
          :data-delay="i * 60"
          @click="scrollTo('products')"
        >
          <div class="cat-icon"><YuIcon :name="c.icon" size="lg" /></div>
          <h3>{{ c.title }}</h3>
          <p>{{ c.desc }}</p>
          <span class="cat-arrow"><YuIcon name="arrowRight" size="sm" /></span>
        </div>
      </div>
    </section>

    <!-- 核心能力（对标四重同步） -->
    <section class="section section-alt" id="sync">
      <h2 class="section-title reveal">{{ t('landing.syncTitle') }}</h2>
      <p class="section-sub reveal" data-delay="80">{{ t('landing.syncSub') }}</p>
      <div class="sync-grid">
        <div
          v-for="(s, i) in syncFeatures"
          :key="s.title"
          class="sync-card reveal"
          :class="{ pulse: s.pulse }"
          :data-delay="i * 80"
        >
          <div class="sync-icon"><YuIcon :name="s.icon" size="xl" /></div>
          <h3>{{ s.title }}</h3>
          <p>{{ s.desc }}</p>
          <div v-if="s.stat" class="sync-stat">
            <span class="sync-stat-val">{{ s.stat }}</span>
            <span class="sync-stat-label">{{ s.statLabel }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 购买流程 -->
    <section class="section" id="flow">
      <h2 class="section-title reveal">{{ t('landing.flowTitle') }}</h2>
      <p class="section-sub reveal" data-delay="80">{{ t('landing.flowSub') }}</p>
      <div class="timeline">
        <div class="timeline-line" />
        <div
          v-for="(s, i) in steps"
          :key="s.title"
          class="timeline-item reveal"
          :data-delay="i * 100"
        >
          <div class="timeline-dot">{{ i + 1 }}</div>
          <div class="timeline-card">
            <div class="step-num">STEP {{ String(i + 1).padStart(2, '0') }}</div>
            <h3>{{ s.title }}</h3>
            <p>{{ s.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能展示 Tab -->
    <section class="section section-alt" id="showcase">
      <h2 class="section-title reveal">{{ t('landing.showcaseTitle') }}</h2>
      <p class="section-sub reveal" data-delay="80">{{ t('landing.showcaseSub') }}</p>
      <FeatureShowcase />
    </section>

    <!-- 安全 -->
    <section class="section" id="security">
      <h2 class="section-title reveal">{{ t('landing.securityTitle') }}</h2>
      <div class="security-grid">
        <div v-for="(f, i) in features" :key="f.title" class="security-card reveal" :data-delay="i * 50">
          <div class="feature-icon"><YuIcon :name="f.icon" size="lg" /></div>
          <h3>{{ f.title }}</h3>
          <p>{{ f.desc }}</p>
        </div>
      </div>
      <div class="security-badges reveal" data-delay="200">
        <div v-for="b in securityBadges" :key="b.label" class="badge-item">
          <strong>{{ b.value }}</strong>
          <span>{{ b.label }}</span>
        </div>
      </div>
    </section>

    <!-- 截图轮播 -->
    <section class="section section-alt" id="screenshots">
      <h2 class="section-title reveal">{{ t('landing.screenshotsTitle') }}</h2>
      <el-carousel :interval="5000" type="card" height="440px" class="screenshot-carousel reveal" data-delay="100">
        <el-carousel-item v-for="shot in screenshots" :key="shot.title">
          <div class="shot-card">
            <ShopScreenshotFrame
              :src="shot.image"
              :alt="shot.title"
              :show-success-toast="shot.view === 'success'"
            />
            <p>{{ shot.title }}</p>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 商品 -->
    <section class="section" id="products">
      <h2 class="section-title reveal">{{ t('landing.productsTitle') }}</h2>
      <p class="section-sub reveal" data-delay="80">{{ t('landing.productsSub') }}</p>
      <ShopProductGrid class="reveal" data-delay="100" @loaded="onProductsLoaded" />
    </section>

    <!-- 订单查询 CTA -->
    <section class="section section-cta reveal">
      <div class="cta-box">
        <div class="cta-icon"><YuIcon name="search" size="xl" /></div>
        <div class="cta-text">
          <h2>{{ t('landing.ctaTitle') }}</h2>
          <p>{{ t('landing.ctaDesc') }}</p>
        </div>
        <div class="cta-actions">
          <el-button type="primary" size="large" round class="cta-btn" @click="$router.push('/shop/query')">
            {{ t('shop.queryTitle') }}
          </el-button>
          <el-button size="large" round plain @click="$router.push('/shop/redeem')">{{ t('nav.redeem') }}</el-button>
        </div>
      </div>
    </section>

    <!-- FAQ -->
    <section class="section section-alt" id="faq">
      <h2 class="section-title reveal">{{ t('landing.faqTitle') }}</h2>
      <el-collapse class="faq-collapse reveal" data-delay="100">
        <el-collapse-item v-for="q in faqs" :key="q.q" :title="q.q" :name="q.q">
          <p>{{ q.a }}</p>
        </el-collapse-item>
      </el-collapse>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useShopAuthStore } from '../../stores/shopAuth'
import { useScrollReveal } from '../../composables/useScrollReveal'
import PurchaseTicker from '../../components/shop/PurchaseTicker.vue'
import StatsBar from '../../components/shop/StatsBar.vue'
import FeatureShowcase from '../../components/shop/FeatureShowcase.vue'
import LiveShopPreview from '../../components/shop/LiveShopPreview.vue'
import ShopScreenshotFrame from '../../components/shop/ShopScreenshotFrame.vue'
import ShopProductGrid from '../../components/shop/ShopProductGrid.vue'
import YuIcon from '../../components/icons/YuIcon.vue'

useScrollReveal('.reveal')
const { t, tm } = useI18n()
const auth = useShopAuthStore()
const products = ref([])

const techStack = ['Vue 3', 'Vite', 'Spring Boot', 'MySQL', 'Redis', 'Docker']

const notices = computed(() => tm('landing.notices'))
const categories = computed(() => tm('landing.categories'))
const syncFeatures = computed(() => tm('landing.syncFeatures'))
const steps = computed(() => tm('landing.steps'))
const features = computed(() => tm('landing.features'))
const securityBadges = computed(() => tm('landing.securityBadges'))
const screenshots = computed(() => tm('landing.screenshots'))
const faqs = computed(() => tm('landing.faqs'))

function onProductsLoaded(list) {
  products.value = list
}

function particleStyle(i) {
  return {
    left: `${(i * 17 + 5) % 95}%`,
    top: `${(i * 23 + 10) % 80}%`,
    animationDelay: `${i * 0.35}s`,
    animationDuration: `${3 + (i % 4)}s`
  }
}

function scrollTo(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
}

</script>

<style scoped>
.faka-landing { --brand: #4f6ef7; --brand-dark: #3b5bdb; overflow-x: hidden; }

/* Hero 通栏 */
.hero-band {
  position: relative;
  width: 100vw;
  margin-left: calc(50% - 50vw);
  background: var(--shop-hero-bg);
  background-size: 200% 200%;
  animation: gradient-shift 8s ease infinite;
  border-radius: 0 0 48px 48px;
  overflow: hidden;
}
.hero {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 48px;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 72px 24px 24px;
  min-height: 520px;
}
.hero-inner, .hero-mockup {
  position: relative;
  z-index: 1;
  min-width: 0;
  max-width: 100%;
}
.hero-mockup { overflow: visible; }
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--shop-badge-bg);
  backdrop-filter: blur(8px);
  color: var(--shop-brand-accent, var(--brand));
  font-size: 13px;
  font-weight: 600;
  padding: 8px 18px;
  border-radius: 24px;
  margin-bottom: 20px;
  border: 1px solid var(--shop-badge-border);
}
.badge-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #22c55e;
  animation: pulse 2s infinite;
}
.hero h1 {
  font-size: clamp(34px, 5vw, 56px);
  font-weight: 800;
  line-height: 1.12;
  color: var(--shop-text);
  margin-bottom: 20px;
}
.gradient-text {
  background: linear-gradient(135deg, var(--brand), #7c3aed, #ec4899);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradient-shift 4s linear infinite;
}
.hero-desc { font-size: 16px; color: var(--shop-text-muted); line-height: 1.75; max-width: 520px; margin-bottom: 28px; }
.hero-actions { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 28px; }
.cta-btn { box-shadow: 0 8px 24px rgba(79,110,247,0.35); }
.tech-row { display: flex; gap: 8px; flex-wrap: wrap; }
.tech-tag {
  font-size: 12px; color: var(--shop-text-muted);
  background: var(--shop-tag-bg);
  padding: 5px 12px; border-radius: 8px;
  font-weight: 500; border: 1px solid var(--shop-border);
}
.floating { animation: float 5s ease-in-out infinite; }
.mockup-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 32px 80px rgba(79,110,247,0.18);
  overflow: hidden;
  border: 1px solid rgba(0,0,0,0.04);
}
.mockup-bar { background: #f1f5f9; padding: 12px 16px; display: flex; gap: 6px; }
.mockup-bar span { width: 10px; height: 10px; border-radius: 50%; background: #cbd5e1; }
.mockup-bar span:first-child { background: #f87171; }
.mockup-bar span:nth-child(2) { background: #fbbf24; }
.mockup-bar span:nth-child(3) { background: #34d399; }
.mockup-body { padding: 20px; }
.mockup-url { font-size: 12px; color: #94a3b8; font-family: monospace; margin-bottom: 12px; }
.mockup-hero-mini {
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(90deg, #eef2ff, #fce7f3, #eef2ff);
  background-size: 200% 100%;
  animation: shimmer 3s linear infinite;
  margin-bottom: 14px;
}
.mockup-products { display: flex; gap: 10px; margin-bottom: 14px; }
.mockup-item { flex: 1; border: 1px solid #e2e8f0; border-radius: 10px; padding: 8px; }
.mi-img { height: 36px; background: #f1f5f9; border-radius: 6px; margin-bottom: 6px; }
.mi-price { font-size: 11px; font-weight: 700; color: #ef4444; }
.mockup-toast {
  background: #f0fdf4;
  color: #16a34a;
  font-size: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  text-align: center;
  animation: fadeInUp 0.5s ease 1s both;
}
.hero-after {
  position: relative;
  z-index: 2;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 8px;
}

/* Notice */
.notice-bar {
  max-width: 800px;
  margin: 0 auto 48px;
  width: calc(100% - 48px);
  background: var(--shop-notice-bg);
  border: 1px solid var(--shop-notice-border);
  border-radius: 10px;
  padding: 0 16px;
  font-size: 13px;
  color: var(--shop-notice-text);
  text-align: center;
  line-height: 40px;
}

/* Sections */
.section {
  padding: 80px 24px;
  max-width: 1200px;
  margin: 0 auto;
}
.section-alt {
  background: var(--shop-section-alt);
  max-width: none;
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  padding: 80px 24px;
  box-sizing: border-box;
}
.section-alt > .section-title,
.section-alt > .section-sub,
.section-alt > .category-grid,
.section-alt > .sync-grid,
.section-alt > .screenshot-carousel,
.section-alt > .faq-collapse,
.section-alt :deep(.showcase),
.section-alt :deep(.product-grid-wrap) {
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}
.section-title {
  font-size: clamp(26px, 3.5vw, 40px);
  font-weight: 800;
  text-align: center;
  color: var(--shop-text);
  margin-bottom: 12px;
}
.section-sub { text-align: center; color: var(--shop-text-muted); margin-bottom: 48px; font-size: 15px; }
.section-cta { padding: 48px 0 80px; }
.cta-box {
  display: flex;
  align-items: center;
  gap: 28px;
  max-width: 880px;
  margin: 0 auto;
  padding: 32px 36px;
  background: var(--shop-card);
  border: 1px solid var(--shop-border);
  border-radius: 20px;
  box-shadow: var(--shop-card-shadow-lg);
}
.cta-icon {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--shop-cta-icon-bg);
  color: var(--shop-brand-accent, var(--brand));
  border-radius: 16px;
  border: 1px solid var(--shop-icon-border);
}
.cta-text { flex: 1; min-width: 0; text-align: left; }
.cta-actions {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: flex-end;
}
.cta-box h2 {
  font-size: 22px;
  font-weight: 800;
  color: var(--shop-text);
  margin-bottom: 6px;
}
.cta-box p {
  font-size: 14px;
  color: var(--shop-text-muted);
  margin: 0;
  line-height: 1.6;
}
.cta-btn {
  flex-shrink: 0;
  padding: 0 28px;
  height: 44px;
  box-shadow: 0 6px 20px rgba(79, 110, 247, 0.28);
}
@media (max-width: 768px) {
  .cta-box {
    flex-direction: column;
    text-align: center;
    padding: 28px 24px;
  }
  .cta-text { text-align: center; }
  .cta-actions { width: 100%; flex-direction: column; }
  .cta-btn { width: 100%; }
}

/* Categories */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 16px;
  max-width: 1000px;
  margin: 0 auto;
}
.category-card {
  background: var(--shop-card);
  border: 1px solid var(--shop-border);
  border-radius: 16px;
  padding: 28px 18px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  position: relative;
  overflow: hidden;
}
.category-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 48px rgba(79,110,247,0.15);
  border-color: #c7d2fe;
}
.cat-icon {
  width: 48px; height: 48px;
  display: flex; align-items: center; justify-content: center;
  background: var(--shop-icon-bg); color: var(--shop-brand-accent, var(--brand));
  border-radius: 14px; margin-bottom: 12px;
}
.cat-arrow { color: var(--brand); display: inline-flex; }
.notice-item {
  display: inline-flex; align-items: center; gap: 8px;
  font-size: 13px; color: var(--shop-notice-item);
}
.notice-item .yu-icon { color: var(--brand); }
.category-card h3 { font-size: 15px; font-weight: 700; color: var(--shop-text); margin-bottom: 6px; }
.category-card p { font-size: 12px; color: var(--shop-text-muted); line-height: 1.5; }
.cat-arrow {
  position: absolute;
  bottom: 12px; right: 16px;
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.3s;
  color: var(--brand);
}
.category-card:hover .cat-arrow { opacity: 1; transform: translateX(0); }

/* Sync */
.sync-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  max-width: 1100px;
  margin: 0 auto;
}
.sync-card {
  background: var(--shop-card);
  border: 1px solid var(--shop-border);
  border-radius: 18px;
  padding: 28px;
  transition: transform 0.3s;
}
.sync-card:hover { transform: translateY(-4px); }
.sync-card.pulse { border-color: #c7d2fe; box-shadow: 0 0 0 1px #c7d2fe; }
.sync-icon {
  width: 52px; height: 52px;
  display: flex; align-items: center; justify-content: center;
  background: var(--shop-m-banner);
  color: var(--shop-brand-accent, var(--brand)); border-radius: 14px; margin-bottom: 12px;
}
.sync-card h3 { font-size: 17px; font-weight: 700; margin-bottom: 8px; color: var(--shop-text); }
.sync-card p { font-size: 13px; color: var(--shop-text-muted); line-height: 1.65; margin-bottom: 16px; }
.sync-stat { display: flex; align-items: baseline; gap: 6px; }
.sync-stat-val { font-size: 24px; font-weight: 800; color: var(--brand); }
.sync-stat-label { font-size: 12px; color: var(--shop-text-muted); }

/* Timeline */
.timeline {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  max-width: 1100px;
  margin: 0 auto;
  position: relative;
}
.timeline-line {
  position: absolute;
  top: 28px;
  left: 10%;
  right: 10%;
  height: 2px;
  background: linear-gradient(90deg, #c7d2fe, #4f6ef7, #c7d2fe);
  z-index: 0;
}
.timeline-item { position: relative; z-index: 1; text-align: center; }
.timeline-dot {
  width: 40px; height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f6ef7, #6366f1);
  color: #fff;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 4px 16px rgba(79,110,247,0.4);
}
.timeline-card {
  background: var(--shop-card);
  border: 1px solid var(--shop-border);
  border-radius: 14px;
  padding: 20px 14px;
  text-align: left;
}
.step-num { font-size: 10px; font-weight: 700; color: var(--brand); margin-bottom: 6px; }
.timeline-card h3 { font-size: 14px; font-weight: 700; margin-bottom: 6px; color: var(--shop-text); }
.timeline-card p { font-size: 12px; color: var(--shop-text-muted); line-height: 1.55; }

/* Security */
.security-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  max-width: 1100px;
  margin: 0 auto 40px;
}
.security-card {
  background: var(--shop-card);
  border: 1px solid var(--shop-border);
  border-radius: 14px;
  padding: 24px;
}
.feature-icon {
  width: 44px; height: 44px;
  display: flex; align-items: center; justify-content: center;
  background: var(--shop-section-alt); color: var(--shop-brand-accent, var(--brand));
  border-radius: 12px; margin-bottom: 10px;
  border: 1px solid var(--shop-border);
}
.security-card h3 { font-size: 16px; font-weight: 700; margin-bottom: 6px; color: var(--shop-text); }
.security-card p { font-size: 13px; color: var(--shop-text-muted); line-height: 1.6; }
.security-badges {
  display: flex;
  justify-content: center;
  gap: 32px;
  flex-wrap: wrap;
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  background: var(--shop-security-badges);
  border: 1px solid var(--shop-border);
  border-radius: 16px;
}
.badge-item { text-align: center; }
.badge-item strong { display: block; font-size: 16px; color: var(--brand); margin-bottom: 4px; }
.badge-item span { font-size: 12px; color: var(--shop-text-muted); }

/* Screenshots */
.screenshot-carousel {
  max-width: 960px;
  margin: 0 auto;
  width: 100%;
}
.screenshot-carousel :deep(.el-carousel__container) {
  margin: 0 auto;
}
.screenshot-carousel :deep(.el-carousel__item) {
  display: flex;
  align-items: center;
  justify-content: center;
}
.screenshot-carousel :deep(.el-carousel__item--card) {
  width: 62%;
}
.shot-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  width: 100%;
  max-width: 520px;
  margin: 0 auto;
  padding: 8px 16px 16px;
  overflow: hidden;
}
.shot-card :deep(.screenshot-frame),
.shot-card :deep(.live-preview) {
  width: 100%;
  max-width: 520px;
  margin: 0 auto;
}
.shot-preview {
  height: 220px;
  border-radius: 16px;
  margin-bottom: 16px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}
.shot-shop { background: linear-gradient(180deg, #eef2ff, #fff); padding: 20px; }
.sp-grid { display: flex; gap: 12px; justify-content: center; margin-top: 40px; }
.sp-item { width: 80px; height: 100px; background: #fff; border-radius: 10px; border: 1px solid #e2e8f0; box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.shot-admin { background: #1e293b; }
.sp-dash {
  margin: 30px 20px;
  height: 120px;
  background: linear-gradient(180deg, rgba(79,110,247,0.4), transparent);
  border-radius: 10px;
}
.shot-pay { background: #f8fafc; display: flex; align-items: center; justify-content: center; }
.sp-qr {
  width: 120px; height: 120px;
  background: repeating-linear-gradient(45deg, #1e293b 0, #1e293b 3px, #fff 3px, #fff 6px);
  border-radius: 10px;
  animation: qr-pulse 2s infinite;
}
.shot-card p { font-size: 14px; color: var(--shop-text-muted); font-weight: 500; }

.faq-collapse { max-width: 760px; margin: 0 auto; border: none; }
.faq-collapse p { color: var(--shop-text-muted); line-height: 1.75; font-size: 14px; }

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.85); }
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 900px) {
  .hero { grid-template-columns: 1fr; padding: 48px 0 16px; min-height: auto; }
  .hero-mockup { display: none; }
  .timeline { grid-template-columns: 1fr; }
  .timeline-line { display: none; }
  .hero { padding: 48px 16px 16px; }
  .hero-after { padding: 0 16px 8px; }
  .section { padding: 56px 16px; }
  .section-alt { padding: 56px 16px; }
  .screenshot-carousel :deep(.el-carousel__item--card) { width: 88%; }
  .notice-bar { width: calc(100% - 32px); }
}
</style>
