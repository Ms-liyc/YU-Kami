# 参与贡献

感谢你对 YU-Kami 的关注！欢迎通过 Issue 或 Pull Request 参与项目。

## 开发环境

```bash
# 1. 启动数据库（Docker）
docker compose -f docker-compose.dev.yml up -d

# 2. 后端
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd

# 3. 前端
cd frontend
npm install
npm run dev
```

- 管理后台：http://localhost:5173/login
- 用户商城：http://localhost:5173/shop
- API 文档：http://localhost:8080/swagger-ui.html

## 提交规范

- 分支：`feat/xxx`、`fix/xxx`、`docs/xxx`
- 提交信息：`feat:` / `fix:` / `docs:` / `refactor:` 前缀
- 确保 `./mvnw package` 与 `npm run build` 通过

## Pull Request

1. Fork 本仓库并创建分支
2. 完成修改并自测
3. 提交 PR，说明改动目的与测试方式
4. 等待 Review 合并

## 报告问题

请在 [GitHub Issues](https://github.com/Ms-liyc/YU-Kami/issues) 中提供：

- 版本号（如 v1.6.0）
- 复现步骤
- 期望行为与实际行为
- 相关日志或截图
