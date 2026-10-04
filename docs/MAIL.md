# QQ 邮箱发信配置

YU-Kami 使用 SMTP 发送邮件，适用于：

- 库存不足告警（`STOCK_ALERT_EMAIL`）
- 商城用户邮箱验证
- 管理端 / 商城找回密码

## 一、开启 QQ 邮箱 SMTP

1. 登录 [QQ 邮箱](https://mail.qq.com) → **设置** → **账号**
2. 找到 **POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务**
3. 开启 **IMAP/SMTP服务** 或 **POP3/SMTP服务**
4. 按提示用手机发送短信，获取 **16 位授权码**（不是 QQ 密码）

## 二、环境变量（推荐 465 SSL）

```env
MAIL_HOST=smtp.qq.com
MAIL_PORT=465
MAIL_SSL=true
MAIL_STARTTLS=false
MAIL_USERNAME=你的QQ号@qq.com
MAIL_PASSWORD=16位授权码
MAIL_FROM=你的QQ号@qq.com
APP_URL=https://你的域名

# 库存告警收件人（逗号分隔多个邮箱）
STOCK_ALERT_EMAIL=你的QQ号@qq.com
```

也可使用 **587 + STARTTLS**：

```env
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_SSL=false
MAIL_STARTTLS=true
```

## 三、验证

1. 重启后端
2. 管理端 → **安全设置** → **邮件通知** → 输入收件邮箱 → **发送测试邮件**
3. 若成功，QQ 邮箱会收到主题为 `YU-Kami - 邮件测试` 的邮件

## 四、库存告警

- 每小时检查一次在售商品可用卡密数量
- 低于 `STOCK_LOW_THRESHOLD`（默认 10）时发送 Webhook + 邮件
- 同一商品 24 小时内只告警一次

未配置 `MAIL_*` 时，邮件功能自动跳过，不影响其他业务。

## 五、安全说明

- **授权码与 `MAIL_PASSWORD` 仅通过环境变量配置**，切勿写入代码、前端或提交到 Git
- 项目 `.gitignore` 已忽略 `.env`；请使用 `.env.example` 作模板，真实凭据放服务器本地
- 管理端邮件接口仅 **SUPER_ADMIN** 可访问，且 API **不返回** SMTP 账号、密码或完整收件人地址
- 日志中对邮箱地址做脱敏（如 `1***9@qq.com`），SMTP 异常不向用户返回底层错误详情
- 测试发信接口有频率限制（每管理员每小时 5 次）
