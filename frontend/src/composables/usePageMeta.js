import { watchEffect, onUnmounted } from 'vue'
import i18n from '../i18n'

function defaultTitle() {
  return i18n.global.t('meta.defaultTitle')
}

function defaultDescription() {
  return i18n.global.t('meta.defaultDescription')
}

function upsertMeta(attr, key, content, isProperty = false) {
  if (!content) return
  const selector = isProperty ? `meta[property="${key}"]` : `meta[name="${key}"]`
  let el = document.head.querySelector(selector)
  if (!el) {
    el = document.createElement('meta')
    if (isProperty) el.setAttribute('property', key)
    else el.setAttribute('name', key)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function removeMeta(key, isProperty = false) {
  const selector = isProperty ? `meta[property="${key}"]` : `meta[name="${key}"]`
  document.head.querySelector(selector)?.remove()
}

/**
 * 动态更新页面 title 与 SEO / Open Graph 元信息
 */
export function usePageMeta(getMeta) {
  const stop = watchEffect(() => {
    const meta = typeof getMeta === 'function' ? getMeta() : (getMeta || {})
    document.title = meta.title || defaultTitle()

    upsertMeta('name', 'description', meta.description || defaultDescription())
    upsertMeta('property', 'og:title', meta.title || defaultTitle(), true)
    upsertMeta('property', 'og:description', meta.description || defaultDescription(), true)
    upsertMeta('property', 'og:type', meta.type || 'website', true)
    upsertMeta('name', 'twitter:card', meta.twitterCard || 'summary')

    if (meta.url) {
      upsertMeta('property', 'og:url', meta.url, true)
      upsertMeta('name', 'twitter:url', meta.url)
    } else {
      removeMeta('og:url', true)
      removeMeta('twitter:url')
    }

    if (meta.image) {
      upsertMeta('property', 'og:image', meta.image, true)
      upsertMeta('name', 'twitter:image', meta.image)
    } else {
      removeMeta('og:image', true)
      removeMeta('twitter:image')
    }
  })

  onUnmounted(() => {
    stop()
    document.title = defaultTitle()
    upsertMeta('name', 'description', defaultDescription())
    removeMeta('og:url', true)
    removeMeta('og:image', true)
    removeMeta('twitter:url')
    removeMeta('twitter:image')
  })
}

export function getShareUrl(path = '') {
  const origin = typeof window !== 'undefined' ? window.location.origin : ''
  return `${origin}${path}`
}
