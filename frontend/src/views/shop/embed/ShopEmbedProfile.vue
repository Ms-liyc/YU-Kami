<template>
  <div class="embed-shell">
    <div class="embed-card">
      <h1>{{ t('nav.profile') }}</h1>
      <el-form label-width="88px">
        <el-form-item :label="t('shop.username')">
          <el-input model-value="demo" disabled />
        </el-form-item>
        <el-form-item :label="t('shop.nickname')">
          <el-input :model-value="t('landing.embedDemo.buyerNickname')" />
        </el-form-item>
        <el-form-item :label="t('shop.email')">
          <el-input model-value="demo@yu-kami.com" />
        </el-form-item>
        <el-button type="primary">{{ t('common.save') }}</el-button>
      </el-form>
      <el-divider />
      <h3>{{ t('shop.changePassword') }}</h3>
      <el-form label-width="88px">
        <el-form-item :label="t('auth.currentPassword')">
          <el-input type="password" model-value="********" show-password />
        </el-form-item>
        <el-form-item :label="t('auth.newPassword')">
          <el-input type="password" :placeholder="t('auth.newPassword')" />
        </el-form-item>
        <el-button type="warning">{{ t('shop.changePassword') }}</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>
