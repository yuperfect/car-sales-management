# 车辆图片支持 — 架构决策记录

---

## ADR-001: 图片存储方式 — 本地文件系统

### Status
`Accepted`

### Context / Problem
车辆图片需要存储并可公开访问，备选方案有：本地文件系统、外部对象存储（OSS/S3）、Base64 存库。

### Options Considered

| Option | Pros | Cons |
|--------|------|------|
| 本地文件系统 | 零外部依赖、配置简单 | 不支持分布式、需静态资源映射 |
| OSS/S3 | 高可用、CDN | 需额外接入成本、本项目无部署需要 |
| Base64 存库 | 无文件管理 | 膨胀 30%+、DB 变慢、无法直接访问 |

### Decision
采用本地文件系统，图片存 `uploads/images/{carId}.{ext}`，Spring Boot 静态资源映射公开访问。

### Rationale
本项目为课程设计/单机部署，无高可用和 CDN 需求。本地文件系统改动最小，配置最简单。

### Consequences
- Positive: 零外部依赖，部署只需创建 `uploads/` 目录
- Negative: 不直接支持水平扩展，但当前场景不需
- Migration: 无需迁移

### Impact Scope
- Modules affected: application.yml, CarService
- APIs affected: 无（仅新增静态资源映射）
- DB schema affected: 无
- Configuration changes: application.yml 增加 multipart + static 配置

### Traceability
- Enhancement Design: STORY-IMG-01

---

## ADR-002: API 签名变更 — 从 JSON 改为 multipart/form-data

### Status
`Accepted`

### Context / Problem
新增图片上传后，`POST` / `PUT /api/cars` 需要同时接收普通字段和文件。备选方案：两步提交（先建车再传图）或一步提交（multipart）。

### Options Considered

| Option | Pros | Cons |
|--------|------|------|
| 两步提交 | API 不改、后端简单 | 前端多一次请求、事务不原子、体验割裂 |
| 一步 multipart | 原子提交、体验好 | API 签名变更、需同步改前端 |
| 图片单独接口 + 字段存 ID | API 微改 | 架构过度设计 |

### Decision
一步提交：`POST /api/cars` 和 `PUT /api/cars/{id}` 改为接收 `@RequestParam` + `@RequestPart("image", required=false) MultipartFile`。

### Rationale
用户新增车辆时自然希望一次提交完成，分两步不符合直觉。API 变更影响范围明确（仅管理端两个接口），可控。

### Consequences
- Positive: 用户体验好，事务一致
- Negative: 前端 `CarForm.vue` 和 `api/index.js` 需同步修改
- Migration: 管理端前端适配 FormData

### Impact Scope
- Modules affected: CarController, CarService, CarForm.vue, api/index.js (admin)
- APIs affected: `POST /api/cars`, `PUT /api/cars/{id}`
- DB schema affected: 无
- Configuration changes: 无

### Traceability
- Enhancement Design: STORY-IMG-01, STORY-IMG-02

---

## ADR-003: 图片约束 — 单图 + 选填 + 5MB 上限

### Status
`Accepted`

### Context / Problem
每辆车需要支持几张图？是否必填？文件大小和格式限制？

### Options Considered

| Option | Pros | Cons |
|--------|------|------|
| 单图选填 | 实现简单、不影响现有流程 | 不能满足多图需求 |
| 多图（1-N） | 功能完整 | 复杂度翻倍、UI 需改造、超出当前需求 |
| 单图必填 | 图片统一 | 批量导入/旧数据需要补图、工作量大 |

### Decision
单图选填，格式 jpg/png/webp，≤5MB。

### Rationale
当前需求只是"能不能放真实图片"，选填最小侵入。多图可在后续迭代中补充。

### Consequences
- Positive: 对现有数据和流程零侵入
- Negative: 多图用户需后续迭代

### Impact Scope
- Modules affected: CarController (文件校验)
- APIs affected: 无
- DB schema affected: 无

### Traceability
- Enhancement Design: STORY-IMG-01 AC-6
