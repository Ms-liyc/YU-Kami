<template>
  <div class="embed-redeem-page">
    <div class="redeem-card">
      <h1>卡密兑换</h1>
      <p>输入卡密与用户标识完成兑换验证</p>
      <el-form label-width="88px">
        <el-form-item label="您的卡密">
          <el-input model-value="VIP-K7M9-X2P4-Q8N1" />
        </el-form-item>
        <el-form-item label="用户标识">
          <el-input model-value="user_demo_001" />
        </el-form-item>
        <el-button type="primary" style="width:100%">立即兑换</el-button>
      </el-form>
      <el-alert type="success" title="兑换成功，权益已生效" show-icon class="result-alert" />
    </div>
  </div>
</template>

<style scoped>
.embed-redeem-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: #f8fafc;
}
.redeem-card {
  width: 100%;
  max-width: 460px;
  background: #fff;
  border-radius: 20px;
  padding: 32px 28px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
}
.redeem-card h1 { font-size: 22px; font-weight: 800; margin-bottom: 8px; color: #0f172a; }
.redeem-card p { color: #64748b; margin-bottom: 20px; font-size: 14px; }
.result-alert { margin-top: 20px; }
</style>
