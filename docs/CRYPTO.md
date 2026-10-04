# 卡密加密体系

YU-Kami 卡密**不以明文存入数据库**，采用多层防护：

## 存储层

| 字段 | 算法 | 说明 |
|------|------|------|
| `key_hash` | HMAC-SHA256 | 每卡独立 Pepper + HKDF 派生密钥（v2）；兼容历史 v1 拼接密钥 |
| `key_pepper` | 16 字节随机 hex | 每卡独立，与主密钥组合后哈希 |
| `key_checksum` | HMAC-SHA256 截断 8 位（v2） | 兑换时快速索引；兼容历史 SHA-256 6 位 |
| `encrypted_meta` | AES-256-GCM | 批次/产品元数据（HKDF 派生 AES 密钥） |

## 密钥环境变量

```bash
HMAC_SECRET=至少32位随机字符串   # 卡密哈希 + 校验码
AES_KEY=恰好32位随机字符串       # 元数据 + Redis 临时缓存加密
```

生成示例：

```bash
openssl rand -base64 32   # HMAC_SECRET
openssl rand -base64 24   # AES_KEY（取前32字符或自行裁剪）
```

## 版本兼容

- **v1 卡密**（已发行）：HMAC 主密钥拼接 Pepper；SHA-256 六位校验码 — 兑换时自动识别
- **v2 卡密**（新发行）：HKDF 派生 HMAC 密钥；HMAC 八位校验码
- **Redis 缓存**：新写入 `enc:` 前缀 AES 密文；历史明文条目仍可读取

## 临时明文暴露点

以下场景会短暂出现明文，需 HTTPS + 权限控制：

1. 批量生成 API 响应（一次性返回）
2. 订单发货后 Redis 缓存 24 小时（AES 加密存储）
3. 用户兑换时提交的卡密字符串

## 生产建议

1. 部署前务必修改 `HMAC_SECRET` 与 `AES_KEY`（见 [SECURITY.md](SECURITY.md)）
2. 启用 HTTPS（见 [HTTPS.md](HTTPS.md)）
3. 限制管理端生成/导入权限
4. 定期轮换密钥需重新生成卡密（哈希不可逆，无法迁移明文）
