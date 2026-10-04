<template>
  <div class="screenshot-frame">
    <div class="mockup-bar"><span /><span /><span /></div>
    <div class="mockup-url">shop.yu-kami.com</div>
    <div class="preview-viewport">
      <img :src="src" :alt="alt" class="preview-shot" loading="lazy" />
      <div v-if="showSuccessToast" class="mockup-toast live">
        <YuIcon name="check" size="sm" /> {{ t('landing.preview.successToast') }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import YuIcon from '../icons/YuIcon.vue'

const { t } = useI18n()

defineProps({
  src: { type: String, required: true },
  alt: { type: String, default: '' },
  showSuccessToast: { type: Boolean, default: false }
})
</script>

<style scoped>
.screenshot-frame {
  background: var(--shop-mockup-bg);
  border-radius: 20px;
  box-shadow: var(--shop-mockup-shadow);
  overflow: hidden;
  border: 1px solid var(--shop-border);
  width: 100%;
  max-width: 520px;
  margin: 0 auto;
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
  height: 320px;
  overflow: hidden;
  background: var(--shop-preview-bg);
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}
.preview-shot {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: top center;
  display: block;
}
.mockup-toast.live {
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 12px;
  background: #f0fdf4;
  color: #16a34a;
  font-size: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
</style>
