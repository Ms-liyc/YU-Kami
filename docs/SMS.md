# 短信通知配置

YU-Kami 通过可插拔的 **SMS Provider** 发送短信，适用于库存不足告警等场景。凭据仅保存在服务器环境变量中，管理端 API 不返回密钥或完整手机号。

## 提供者类型（SMS_PROVIDER）

| 值 | 说明 |
|----|------|
| `none` | 默认，不发送短信 |
| `log` | 仅写日志（开发/演示） |
| `http` | POST 到自建 HTTP 网关（推荐，可对接任意厂商） |
| `aliyun` | 阿里云 dysmsapi 模板短信 |

## 一、库存告警手机号

```env
STOCK_ALERT_PHONE=13800138000,13900139000
```

多个号码用英文逗号分隔。与 `STOCK_ALERT_EMAIL` 独立，可同时启用邮件 + 短信。

## 二、HTTP 网关（推荐）

适合对接腾讯云函数、Serverless、n8n 或自写转发服务。

```env
SMS_PROVIDER=http
SMS_HTTP_URL=https://your-gateway.example.com/sms/send
SMS_HTTP_SECRET=your-shared-secret
```

请求体示例：

```json
{
  "phone": "13800138000",
  "message": "【YU-Kami】库存告警：...",
  "event": "LOW_STOCK",
  "data": {
    "product": "月度会员卡",
    "code": "VIP_MONTH",
    "unused": "5",
    "threshold": "10"
  }
}
```

若配置了 `SMS_HTTP_SECRET`，请求头携带 `X-SMS-Secret`。

## 三、阿里云短信

```env
SMS_PROVIDER=aliyun
SMS_ALIYUN_ACCESS_KEY_ID=你的AccessKeyId
SMS_ALIYUN_ACCESS_KEY_SECRET=你的AccessKeySecret
SMS_ALIYUN_SIGN_NAME=短信签名
SMS_ALIYUN_TEMPLATE_CODE=SMS_123456789
```

模板变量（`TemplateParam`）：

| 变量 | 说明 |
|------|------|
| `product` | 产品名称 |
| `code` | 产品编码 |
| `unused` | 剩余卡密数 |
| `threshold` | 告警阈值 |

请在阿里云控制台创建包含上述变量的模板。

## 四、开发调试

```env
SMS_PROVIDER=log
```

短信内容仅输出到后端日志，手机号已脱敏。

## 五、管理端测试

1. 使用 **SUPER_ADMIN** 登录
2. **安全设置** → **短信通知** → 输入手机号 → **发送测试短信**

测试接口有频率限制（每管理员每小时 5 次）。

## 六、安全说明

- **切勿**将 `SMS_ALIYUN_ACCESS_KEY_SECRET`、`SMS_HTTP_SECRET` 写入代码或提交 Git
- API 仅返回是否已配置、提供者类型，不返回密钥或完整手机号
- 日志中手机号脱敏为 `138****5678` 格式

扩展新厂商：实现 `com.yuchen.kami.sms.SmsProvider` 接口，并在 `SmsProviderFactory` 中注册。
