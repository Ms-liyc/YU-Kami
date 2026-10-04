<template>
  <div class="embed-shell">
    <div class="embed-card">
      <h1>{{ t('shop.redeemTitle') }}</h1>
      <p>{{ t('shop.redeemDesc') }}</p>
      <el-form label-width="88px">
        <el-form-item :label="t('shop.cardKey')">
          <el-input model-value="VIP-K7M9-X2P4-Q8N1" />
        </el-form-item>
        <el-form-item :label="t('shop.redeemUser')">
          <el-input model-value="user_demo_001" />
        </el-form-item>
        <el-button type="primary" style="width:100%">{{ t('shop.redeemBtn') }}</el-button>
      </el-form>
      <el-alert type="success" :title="t('shop.redeemSuccessDetail')" show-icon class="result-alert" />
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<style scoped>
.result-alert { margin-top: 20px; }
</style>
