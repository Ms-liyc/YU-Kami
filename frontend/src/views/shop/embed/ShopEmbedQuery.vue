<template>
  <div class="embed-query-page">
    <div class="query-card">
      <h1>订单查询</h1>
      <p>输入订单号查询状态，已发货订单可查看卡密</p>
      <el-form>
        <el-form-item label="订单号">
          <el-input model-value="O202601011200001234" />
        </el-form-item>
        <el-button type="primary" style="width:100%">查询订单</el-button>
      </el-form>
      <div class="result-card">
        <div class="result-row"><span>订单号</span><strong>O202601011200001234</strong></div>
        <div class="result-row"><span>产品</span><strong>月度会员 VIP</strong></div>
        <div class="result-row"><span>金额</span><strong>¥29.90</strong></div>
        <div class="result-row"><span>状态</span><el-tag type="success" size="small">已发货</el-tag></div>
        <div class="result-actions">
          <el-button type="success">查看卡密</el-button>
          <el-button>我的订单</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.embed-query-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: #f8fafc;
}
.query-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 20px;
  padding: 32px 28px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
}
.query-card h1 { font-size: 22px; font-weight: 800; margin-bottom: 8px; color: #0f172a; }
.query-card p { color: #64748b; margin-bottom: 20px; font-size: 14px; }
.result-card {
  margin-top: 20px;
  padding: 18px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}
.result-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}
.result-row span { color: #64748b; }
.result-actions { display: flex; gap: 8px; margin-top: 14px; flex-wrap: wrap; }
</style>
