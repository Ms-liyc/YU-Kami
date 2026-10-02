import { ref, onMounted, onUnmounted } from 'vue'

export function useCountUp(target, duration = 1800, startOnVisible = true) {
  const value = ref(0)
  let frame
  let observer

  function animate() {
    const start = performance.now()
    const from = 0
    const to = target
    const tick = (now) => {
      const p = Math.min((now - start) / duration, 1)
      const eased = 1 - Math.pow(1 - p, 3)
      value.value = Math.floor(from + (to - from) * eased)
      if (p < 1) frame = requestAnimationFrame(tick)
      else value.value = to
    }
    frame = requestAnimationFrame(tick)
  }

  onMounted(() => {
    if (!startOnVisible) {
      animate()
      return
    }
    const el = document.querySelector('[data-countup]')
    if (!el) {
      animate()
      return
    }
    observer = new IntersectionObserver(([e]) => {
      if (e.isIntersecting) {
        animate()
        observer.disconnect()
      }
    }, { threshold: 0.3 })
    observer.observe(el)
  })

  onUnmounted(() => {
    cancelAnimationFrame(frame)
    observer?.disconnect()
  })

  return value
}
