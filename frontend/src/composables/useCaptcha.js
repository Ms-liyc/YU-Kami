import { ref } from 'vue'
import axios from 'axios'

export function useCaptcha() {
  const captchaId = ref('')
  const captchaImage = ref('')
  const captchaCode = ref('')

  async function refreshCaptcha() {
    const res = await axios.get('/api/captcha')
    if (res.data?.code === 200 && res.data.data) {
      captchaId.value = res.data.data.captchaId
      captchaImage.value = res.data.data.imageBase64
        ? `data:image/png;base64,${res.data.data.imageBase64}`
        : ''
    }
  }

  function captchaPayload() {
    return { captchaId: captchaId.value, captchaCode: captchaCode.value }
  }

  return { captchaId, captchaImage, captchaCode, refreshCaptcha, captchaPayload }
}
