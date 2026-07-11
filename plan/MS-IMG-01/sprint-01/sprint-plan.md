# Sprint Plan

## Milestone ID
MS-IMG-01 — 车辆图片全链路支持

## Gate Status Summary

| Gate | Status | Notes |
|------|:------:|-------|
| Gate A (Baseline + Handoff) | ✅ PASS | 所有设计文件已确认，architect-plan handoff 已发布 |
| Gate B (Milestone→Sprint) | ✅ PASS | MS-IMG-01 总计 6 点，单 Sprint 完成；Sprint N = 01（无历史 tag） |
| Gate C (Sprint Backlog) | ✅ PASS | sprint-backlog.md 已生成 |
| Gate D (Test Readiness) | ⏳ 待实施 | test-plan + test-cases 由 qa-engineer 产出 |
| Gate E (Acceptance) | ⏳ 待验证 | Sprint 结束时评估 |

## Baseline References
- Approved Design Package: `workspace/car-sales-management/design/car-image-support-approved-design-package.md`
- Enhancement Design: `workspace/car-sales-management/design/car-image-support-enhancement-design.md`
- Architecture Decisions: `workspace/car-sales-management/design/car-image-support-architecture-decisions.md`
- Product Backlog: `workspace/car-sales-management/design/car-image-support-product-backlog.md`
- Milestones: `workspace/car-sales-management/design/car-image-support-milestones.md`

## Pre-flight Checklist
- [x] Git repository exists (`workspace/car-sales-management/.git`)
- [x] `code/` 目录已存在，所有源码将放置在 `code/` 下

## Sprint Goal
**在 MS-IMG-01 中实现车辆图片的完整支持链路**：数据库加列 → 后端上传 API → 管理端表单 → 客户端展示，完成后推送 Gitee。

## Mapped Product Stories

| Story | Points | Dependencies |
|-------|:------:|--------------|
| STORY-IMG-01 | 3 | — |
| STORY-IMG-02 | 2 | STORY-IMG-01 |
| STORY-IMG-03 | 1 | STORY-IMG-01 |
| **Total** | **6** | |

## Story Traceability Mapping

| Story | Product Backlog Ref | Design Ref |
|-------|--------------------|------------|
| STORY-IMG-01 | `car-image-support-product-backlog.md#story-story-img-01` | `enhancement-design.md` Impact Analysis + ADR-001/002 |
| STORY-IMG-02 | 同上 | `enhancement-design.md` STORY-IMG-02 |
| STORY-IMG-03 | 同上 | `enhancement-design.md` STORY-IMG-03 |

## Candidate Stories
全部 3 个 Story 纳入 Sprint-01（6 点）。

## Committed Stories
- STORY-IMG-01 — 后端：数据库加图片列 + 文件上传接口
- STORY-IMG-02 — 管理端：车辆新增/编辑表单支持图片上传与展示
- STORY-IMG-03 — 客户端：在售车辆列表和详情页展示真实图片

## Sprint Backlog

See `workspace/car-sales-management/plan/MS-IMG-01/sprint-01/sprint-backlog.md`

## Out-of-Scope
- 多图支持
- 图片裁剪/压缩/水印
- CDN / 对象存储

## Bug Tracker
- Ref: `workspace/car-sales-management/quality/bug-register.md`
- Sprint Blocker Bugs: 暂无
- Regression Bugs to Monitor: 暂无

## Dependencies
- STORY-IMG-02 和 STORY-IMG-03 需等待 STORY-IMG-01 API 就绪

## Risks
- 无重大风险（设计已评审）

## Change Request Tracking
| CR ID | Status | Impact |
|-------|:------:|--------|
| — | — | — |

## Definition of Ready
- [x] Acceptance Criteria defined and testable
- [x] Story Points estimated
- [x] Design references confirmed
- [x] Known dependencies identified
- [x] Scope boundary clear

## Test Readiness
- [ ] `test-plan.md` produced at `quality/MS-IMG-01/sprint-01/test-plan.md`
- [ ] Per-story test case documents exist at `quality/MS-IMG-01/sprint-01/test-cases/STORY-XXX.md`
- [ ] Coverage confirmation: every AC maps to at least one test case

## Definition of Done
- [ ] All Acceptance Criteria satisfied
- [x] Spring Boot 项目正常编译
- [x] `npm run build` 成功
- [ ] 新增车辆含图片 → 列表/详情正确显示
- [ ] 新增车辆不含图片 → 默认占位图
- [ ] 编辑车辆替换图片 → 生效
- [ ] 编辑车辆移除图片 → image_url 置 NULL
- [ ] 管理端表单图片上传/预览正常
- [ ] 客户端列表/详情页图片展示正常
- [ ] Gitee 推送成功

## Demo Plan
1. 管理端新增车辆（带图片）→ 列表页看到缩略图
2. 编辑车辆换图/移除图
3. 客户端在售车辆列表点进详情页
4. 无图车辆显示默认占位

## Sprint Git Commit Log

| Story | Commit Hash | Commit Message |
|-------|-------------|---------|
| STORY-IMG-01 | | |
| STORY-IMG-02 | | |
| STORY-IMG-03 | | |

## Human Confirmation
- Sprint Backlog Confirmation: ✅ 用户确认开始
- Major Backlog Adjustment Confirmation: —
- Pre-release Go-No-Go Confirmation: —
