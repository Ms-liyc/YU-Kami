import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN.json'
import enUS from './en-US.json'
import landingZh from './landing-zh.json'
import landingEn from './landing-en.json'

const savedLocale = localStorage.getItem('locale') || 'zh-CN'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': { ...zhCN, landing: landingZh },
    'en-US': { ...enUS, landing: landingEn }
  }
})

export default i18n
