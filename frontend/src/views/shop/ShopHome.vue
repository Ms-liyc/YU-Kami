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
          <span class="badge-dot" />自动发卡 · 付款即发货
        </div>
        <h1>
          付款即发货的<br />
          <span class="gradient-text">数字商品发卡网</span>
        </h1>
        <p class="hero-desc">
          屿宸科技 YU-Kami — 卡密生成、商城售卖、支付回调、自动发货一体化。
          支持优惠券促销、支付宝/微信、Webhook 对接。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round class="cta-btn" @click="scrollTo('products')">
            立即选购
          </el-button>
          <el-button size="large" round @click="$router.push('/shop/query')">订单查询</el-button>
          <el-button size="large" round plain @click="$router.push('/shop/redeem')">卡密兑换</el-button>
          <el-button size="large" round plain @click="$router.push(auth.token ? '/shop/orders' : '/shop/login')">
            {{ auth.token ? '我的订单' : '登录 / 注册' }}
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
      <h2 class="section-title reveal">适合销售各类虚拟商品</h2>
      <p class="section-sub reveal" data-delay="80">卡密、兑换码、授权码——个人站长与小团队都能轻松上手</p>
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
      <h2 class="section-title reveal">四大核心能力，全链路自动运转</h2>
      <p class="section-sub reveal" data-delay="80">从卡密生成到售出发货，每一环都有保障</p>
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
      <h2 class="section-title reveal">一笔订单，从下单到发货全程自动</h2>
      <p class="section-sub reveal" data-delay="80">买家付款后无需人工介入</p>
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
      <h2 class="section-title reveal">前台好用，后台省心</h2>
      <p class="section-sub reveal" data-delay="80">商城、管理后台、移动端完整适配</p>
      <FeatureShowcase />
    </section>

    <!-- 安全 -->
    <section class="section" id="security">
      <h2 class="section-title reveal">卖得放心，管得省心</h2>
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
      <h2 class="section-title reveal">界面一览</h2>
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
      <h2 class="section-title reveal">热销商品</h2>
      <p class="section-sub reveal" data-delay="80">支持优惠券与限时促销</p>
      <ShopProductGrid class="reveal" data-delay="100" @loaded="onProductsLoaded" />
    </section>

    <!-- 订单查询 CTA -->
    <section class="section section-cta reveal">
      <div class="cta-box">
        <div class="cta-icon"><YuIcon name="search" size="xl" /></div>
        <div class="cta-text">
          <h2>已有订单？立即查询卡密</h2>
          <p>输入订单号即可查状态；登录后在「我的订单」筛选、搜索并一键复制卡密</p>
        </div>
        <div class="cta-actions">
          <el-button type="primary" size="large" round class="cta-btn" @click="$router.push('/shop/query')">
            订单查询
          </el-button>
          <el-button size="large" round plain @click="$router.push('/shop/redeem')">卡密兑换</el-button>
        </div>
      </div>
    </section>

    <!-- FAQ -->
    <section class="section section-alt" id="faq">
      <h2 class="section-title reveal">常见问题</h2>
      <el-collapse class="faq-collapse reveal" data-delay="100">
        <el-collapse-item v-for="q in faqs" :key="q.q" :title="q.q" :name="q.q">
          <p>{{ q.a }}</p>
        </el-collapse-item>
      </el-collapse>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
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
const { t } = useI18n()
const auth = useShopAuthStore()
const products = ref([])

const techStack = ['Vue 3', 'Vite', 'Spring Boot', 'MySQL', 'Redis', 'Docker']

const notices = [
  { icon: 'spark', text: '新用户注册即可体验完整购买与自动发卡流程' },
  { icon: 'payment', text: '支持支付宝 / 微信支付（需商家自行配置密钥）' },
  { icon: 'coupon', text: '限时优惠券活动进行中，购买页输入券码即可抵扣' },
  { icon: 'lock', text: '卡密五重加密存储，付款成功秒级自动发货' }
]

const categories = [
  { icon: 'game', title: '游戏点卡', desc: 'Steam、Xbox、游戏充值码' },
  { icon: 'media', title: '影音会员', desc: '视频、音乐会员兑换码' },
  { icon: 'software', title: '软件激活码', desc: '授权码、注册码、序列号' },
  { icon: 'timer', title: '授权卡密', desc: '天卡、月卡、年卡等时长卡' },
  { icon: 'gift', title: '礼品卡', desc: '电商与平台礼品卡' },
  { icon: 'document', title: '文本资源', desc: '账号、教程链接、兑换说明' }
]

const syncFeatures = [
  { icon: 'bolt', title: '即时自动发卡', desc: '支付成功秒级生成卡密，订单页一键复制，无需人工处理。', pulse: true, stat: '<3s', statLabel: '平均发货' },
  { icon: 'payment', title: '多渠道支付', desc: '模拟支付开箱即用，生产环境可对接支付宝、微信 Native/JSAPI。', stat: '3+', statLabel: '支付渠道' },
  { icon: 'coupon', title: '营销促销', desc: '满减、折扣、节日特价、优惠券码，统一定价引擎取最优价。', stat: '自动', statLabel: '算价' },
  { icon: 'bell', title: 'Webhook 回调', desc: '兑换成功异步通知第三方，HMAC-SHA256 签名验签。', stat: '24h', statLabel: '卡密缓存' }
]

const steps = [
  { title: '买家下单', desc: '选择商品，可使用优惠券，创建待支付订单。' },
  { title: '锁定库存', desc: '事务内校验商品状态，防止超卖与重复下单。' },
  { title: '扫码付款', desc: '展示二维码或跳转支付，微信内支持 JSAPI 调起。' },
  { title: '回调验签', desc: '支付宝/微信异步通知，乐观锁防重复发货。' },
  { title: '自动发货', desc: '即时生成卡密，订单页展示并支持复制。' }
]

const features = [
  { icon: 'lock', title: '五重加密', desc: 'HMAC 哈希 + AES 加密 + Pepper，卡密不明文存储。' },
  { icon: 'shield', title: '绝不超卖', desc: '分布式锁 + 乐观锁，并发下单安全可靠。' },
  { icon: 'ban', title: '限流防刷', desc: 'Redis 滑动窗口限流，抵御暴力兑换与刷单。' },
  { icon: 'chart', title: '全链路审计', desc: '兑换记录、订单流水、管理端操作全程可追溯。' },
  { icon: 'globe', title: '中英双语', desc: '商城与管理后台支持中英文切换。' },
  { icon: 'package', title: 'Docker 部署', desc: '一键 Compose 启动，健康检查自动就绪。' }
]

const securityBadges = [
  { value: 'HMAC-SHA256', label: 'Webhook 签名' },
  { value: '±300s', label: '时间戳校验' },
  { value: 'RBAC', label: '角色权限' },
  { value: 'JWT', label: '接口鉴权' }
]

const screenshots = [
  { title: '商城首页 — 真实商品与价格', view: 'list', image: '/screenshots/preview-list.png' },
  { title: '购买页 — 优惠券与支付渠道', view: 'buy', image: '/screenshots/preview-buy.png' },
  { title: '支付成功 — 卡密一键复制', view: 'success', image: '/screenshots/preview-success.png' },
  { title: '订单查询 — 凭订单号查状态', view: 'query', image: '/screenshots/preview-query.png' },
  { title: '卡密兑换 — 开放 API 前台入口', view: 'redeem', image: '/screenshots/preview-redeem.png' },
  { title: '个人中心 — 资料与密码管理', view: 'profile', image: '/screenshots/preview-profile.png' }
]

const faqs = [
  { q: '购买后如何获取卡密？', a: '支付成功后，在「我的订单」中查看已发货订单，点击即可复制卡密；也可在「订单查询」页输入订单号直接查看。卡密在 Redis 中缓存 24 小时。' },
  { q: '没有登录能查订单吗？', a: '可以。在顶部「订单查询」输入完整订单号即可查看状态；查看卡密需订单已发货且为本人账号下的订单（需登录）。' },
  { q: '支持哪些支付方式？', a: '默认支持模拟支付（测试用）；生产环境可在管理后台配置支付宝、微信支付（含微信内 JSAPI）。' },
  { q: '可以使用优惠券吗？', a: '可以。在购买页输入优惠券码并选择数量，系统自动计算活动价与券后最优价格。' },
  { q: '如何兑换已有卡密？', a: '访问「卡密兑换」页面，输入卡密与用户标识即可；第三方系统也可直接调用 POST /api/v1/redeem 接口。' },
  { q: '如何对接第三方系统？', a: '支持 Webhook 兑换成功回调（HMAC 签名）、API 客户端管理，以及标准化 REST 兑换接口。' },
  { q: '卡密会重复出售吗？', a: '不会。支付成功后在事务内生成唯一卡密，回调使用乐观锁防止重复发货。' },
  { q: '如何部署自己的发卡网？', a: 'Docker Compose 一键部署，或手动编译 Jar + Nginx 托管前端静态文件。详见项目 README。' }
]

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
