# Risk Register

| Risk ID | 描述 | 概率 | 影响 | 等级 | 处理方式 | 状态 |
|---------|------|:---:|:---:|:---:|---------|:----:|
| RSK-001 | MySQL 密码不一致导致后端启动失败 | M | H | 高 | 配置检查.exe 自动检测并更新 application.yml | ✅ 已解决 |
| RSK-002 | 端口 8080/5173/3000 被占用 | M | H | 高 | 启动系统.exe 自动检查端口；配置检查.exe 单独检测 | ✅ 已解决 |
| RSK-003 | 网络环境无法访问 GitHub | L | L | 低 | Gitee 作为主远程仓库，GitHub 可选同步 | ✅ 已规避 |
| RSK-004 | 前端 node_modules 缺失导致构建失败 | L | H | 高 | 启动脚本自动检测；配置检查.exe 可自动 npm install | ✅ 已解决 |
| RSK-005 | JDK 版本不兼容 | L | H | 高 | 使用 JDK 17+，配置检查.exe 检测版本 | ✅ 已解决 |
| RSK-006 | .NET 8 运行时缺失无法运行 exe | L | M | 中 | exe 编译为 PublishSingleFile 独立程序，不依赖运行时 | ✅ 已规避 |

## 当前风险
- 无未解决的风险