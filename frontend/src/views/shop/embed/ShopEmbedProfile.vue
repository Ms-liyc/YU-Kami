<template>
  <div class="embed-profile-page">
    <div class="profile-card">
      <h1>个人中心</h1>
      <el-form label-width="88px">
        <el-form-item label="用户名">
          <el-input model-value="demo" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input model-value="演示买家" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input model-value="demo@yu-kami.com" />
        </el-form-item>
        <el-button type="primary">保存</el-button>
      </el-form>
      <el-divider />
      <h3>修改密码</h3>
      <el-form label-width="88px">
        <el-form-item label="当前密码">
          <el-input type="password" model-value="********" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-button type="warning">修改密码</el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.embed-profile-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: #f8fafc;
}
.profile-card {
  width: 100%;
  max-width: 460px;
  background: #fff;
  border-radius: 20px;
  padding: 32px 28px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
}
.profile-card h1 { font-size: 22px; font-weight: 800; margin-bottom: 20px; color: #0f172a; }
.profile-card h3 { font-size: 15px; font-weight: 700; margin-bottom: 14px; color: #0f172a; }
</style>
