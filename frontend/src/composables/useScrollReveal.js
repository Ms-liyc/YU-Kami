import { onMounted, onUnmounted } from 'vue'

export function useScrollReveal(selector = '.reveal', options = {}) {
  const { threshold = 0.12, rootMargin = '0px 0px -40px 0px' } = options
  let observer

  onMounted(() => {
    observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const delay = entry.target.dataset.delay || 0
          setTimeout(() => entry.target.classList.add('revealed'), Number(delay))
          observer.unobserve(entry.target)
        }
      })
    }, { threshold, rootMargin })
    document.querySelectorAll(selector).forEach((el) => observer.observe(el))
  })

  onUnmounted(() => observer?.disconnect())
}
