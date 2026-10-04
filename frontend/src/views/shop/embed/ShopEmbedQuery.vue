<template>
  <div class="embed-shell">
    <div class="embed-card">
      <h1>{{ t('shop.queryTitle') }}</h1>
      <p>{{ t('shop.queryDesc') }}</p>
      <el-form>
        <el-form-item :label="t('shop.orderNo')">
          <el-input model-value="O202601011200001234" />
        </el-form-item>
        <el-button type="primary" style="width:100%">{{ t('shop.queryBtn') }}</el-button>
      </el-form>
      <div class="embed-panel">
        <div class="embed-row"><span>{{ t('shop.orderNo') }}</span><strong>O202601011200001234</strong></div>
        <div class="embed-row"><span>{{ t('order.product') }}</span><strong>月度会员 VIP</strong></div>
        <div class="embed-row"><span>{{ t('order.amount') }}</span><strong>¥29.90</strong></div>
        <div class="embed-row"><span>{{ t('common.status') }}</span><el-tag type="success" size="small">{{ t('order.statusDelivered') }}</el-tag></div>
        <div class="embed-actions">
          <el-button type="success">{{ t('shop.viewCard') }}</el-button>
          <el-button>{{ t('nav.myOrders') }}</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>
