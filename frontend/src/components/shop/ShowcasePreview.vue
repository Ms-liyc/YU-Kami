<template>
  <div class="showcase-preview" :class="{ 'showcase-preview--phone': phone }">
    <div class="mockup-bar"><span /><span /><span /></div>
    <div class="mockup-url">{{ urlLabel }}</div>
    <div class="preview-viewport" ref="viewportRef">
      <img
        v-if="useScreenshot"
        :src="screenshot"
        :alt="alt"
        class="preview-shot"
        loading="lazy"
      />
      <div v-else class="preview-scale">
        <iframe
          :src="embedUrl"
          class="preview-iframe"
          :title="alt"
          scrolling="no"
          loading="lazy"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const IFRAME_W = 1024
const IFRAME_H = 640
const PHONE_W = 390
const PHONE_H = 780

const props = defineProps({
  screenshot: { type: String, required: true },
  embedUrl: { type: String, required: true },
  urlLabel: { type: String, default: 'yu-kami.com' },
  alt: { type: String, default: '界面预览' },
  phone: { type: Boolean, default: false }
})

const viewportRef = ref(null)
const useScreenshot = ref(false)
let resizeObs

function fitIframe() {
  const el = viewportRef.value
  if (!el || useScreenshot.value) return
  const w = props.phone ? PHONE_W : IFRAME_W
  const h = props.phone ? PHONE_H : IFRAME_H
  const scale = Math.min(el.clientWidth / w, el.clientHeight / h, 1)
  el.style.setProperty('--preview-scale', String(scale))
}

async function checkScreenshot() {
  try {
    const res = await fetch(props.screenshot, { method: 'HEAD' })
    useScreenshot.value = res.ok
  } catch {
    useScreenshot.value = false
  }
}

onMounted(async () => {
  await checkScreenshot()
  fitIframe()
  resizeObs = new ResizeObserver(fitIframe)
  if (viewportRef.value) resizeObs.observe(viewportRef.value)
})

onUnmounted(() => resizeObs?.disconnect())
</script>

<style scoped>
.showcase-preview {
  background: var(--shop-mockup-bg);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shop-mockup-shadow);
  border: 1px solid var(--shop-border);
  width: 100%;
}
.mockup-bar {
  background: var(--shop-mockup-bar);
  padding: 10px 14px;
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
}
.showcase-preview--phone .preview-viewport {
  height: 420px;
  display: flex;
  justify-content: center;
  background: #0f172a;
}
.preview-shot {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: top center;
  display: block;
}
.preview-scale {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: calc(1024px * var(--preview-scale));
  height: calc(640px * var(--preview-scale));
  overflow: hidden;
}
.showcase-preview--phone .preview-scale {
  width: calc(390px * var(--preview-scale));
  height: calc(780px * var(--preview-scale));
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
.showcase-preview--phone .preview-iframe {
  width: 390px;
  height: 780px;
}
</style>
