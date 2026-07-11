# Test Plan — Sprint-01 / MS-IMG-01

## Context
- **Milestone:** MS-IMG-01 — 车辆图片支持（全链路）
- **Sprint:** Sprint-01 (Global Sprint #1)
- **Stories covered:** STORY-IMG-01, STORY-IMG-02, STORY-IMG-03
- **Total Points:** 6
- **Test environment:**
  - Backend: Spring Boot (local dev server, port 8080)
  - Admin Frontend: Vue 3 dev server (port 5173)
  - Client Frontend: Vue 3 dev server (port 5174)
  - Database: H2 (dev) / MySQL (production-like in CI)
  - Browser: Chrome (latest) for manual + E2E
  - File storage: `uploads/images/` directory

## Test Scope

### Overall test scope for this sprint
Full-stack verification of vehicle image support — from database migration through file upload API, admin form interaction, to client-side image display. This sprint establishes the complete vehicle image feature chain.

### Stories included
| Story | Title | Points | Dependencies |
|-------|-------|:------:|--------------|
| STORY-IMG-01 | 后端加图片列 + 文件上传接口 | 3 | None |
| STORY-IMG-02 | 管理端表单支持图片上传 | 2 | STORY-IMG-01 |
| STORY-IMG-03 | 客户端展示真实图片 | 1 | STORY-IMG-01 |

### Stories explicitly excluded
None.

## Test Levels

| Level | Included | Tools | Scope Detail |
|-------|:--------:|-------|--------------|
| Unit (Backend) | ✅ | JUnit 5 + Mockito | CarService 图片保存/删除逻辑、文件格式/大小校验、静态资源映射配置 |
| Integration (Backend API) | ✅ | MockMvc / REST-assured | `POST /api/cars` + `PUT /api/cars/{id}` multipart upload; GET 接口回归; 文件格式/大小校验; 静态资源访问 |
| E2E | ✅ | Playwright (`code/qa-tests/`) | Admin 端新增/编辑车辆图片上传流程、Client 端列表/详情图片展示（第一 Sprint 需先搭建脚手架） |
| Manual (Visual Inspection) | ✅ | Chrome DevTools + checklist | 图片预览、移除按钮交互、布局检查、375px 响应式、占位图降级 |

## Coverage Targets
- **Unit line coverage:** ≥ 80% (new backend code)
- **Integration:** All new/modified endpoints covered — `POST /api/cars` (multipart), `PUT /api/cars/{id}` (multipart); success + error paths (invalid format, oversized, missing file)
- **E2E:** Every core AC mapped to at least one automated spec (via Playwright)
- **No coverage regression** in unchanged modules (existing CRUD endpoints)
- **Visual coverage:** Every frontend page (admin form, client list, client detail) inspected for layout correctness

## Environment

| Component | URL / Config | Notes |
|-----------|-------------|-------|
| Backend API | `http://localhost:8080` | Spring Boot dev server |
| Admin Frontend | `http://localhost:5173` | Vue 3 dev server |
| Client Frontend | `http://localhost:5174` | Vue 3 dev server |
| File Storage | `uploads/images/` | Relative to backend working directory |
| Database | H2 (dev) / MySQL (prod-like) | |
| Browser | Chrome latest | Responsive: 375px mobile width |
| Playwright | `code/qa-tests/` | E2E test runner (first Sprint — need setup) |

### Environment Prerequisites
1. Node.js 18+ installed for frontend + Playwright
2. JDK 17+ for Spring Boot backend
3. Playwright installed (`npx playwright install chromium`)
4. `uploads/images/` directory created and writable
5. `application.yml` configured with:
   - `spring.servlet.multipart.max-file-size: 5MB`
   - `spring.web.resources.static-locations: file:uploads/`

## Schedule

| Activity | Owner | Start | End | Notes |
|----------|:-----:|:-----:|:---:|-------|
| E2E infrastructure setup | qa-engineer | D1 | D1 | Install Playwright, create baseline health-check test |
| Unit test writing | backend-engineer | D1 | D3 | Concurrent with implementation |
| Integration test writing | backend-engineer | D1 | D3 | Concurrent with implementation |
| E2E test writing | qa-engineer | D2 | D4 | After first backend + frontend builds |
| Smoke testing | qa-engineer | D3 | D5 | After each build |
| Manual visual inspection | qa-engineer | D4 | D5 | Admin form + client pages |
| Regression testing | qa-engineer | D5 | D6 | Full suite |
| Suite promotion | qa-engineer | D6 | D6 | |
| Test report | qa-engineer | D6 | D6 | |

## Test Case Mapping (Story → Test Cases)

| Story | Unit | Integration | E2E Spec | Manual |
|-------|:----:|:-----------:|:--------:|:------:|
| STORY-IMG-01 | `CarServiceTest.java` | `CarControllerIntegrationTest.java` | `specs/car-image-api.spec.ts` | File upload directory check |
| STORY-IMG-02 | N/A | N/A | `specs/admin-car-form-image.spec.ts` | CarForm.vue image upload/preview/remove |
| STORY-IMG-03 | N/A | N/A | `specs/client-car-image-display.spec.ts` | Layout inspection, fallback, 375px responsive |

## Test Case Design Completion

| Story | Test Cases Doc (`test-cases/STORY-XXX.md`) | AC Count | Cases Written | Status |
|-------|------------------------------------------|:--------:|:------------:|:------:|
| STORY-IMG-01 | `test-cases/STORY-IMG-01.md` | 7 | 8 | ✅ |
| STORY-IMG-02 | `test-cases/STORY-IMG-02.md` | 6 | 7 | ✅ |
| STORY-IMG-03 | `test-cases/STORY-IMG-03.md` | 5 | 5 | ✅ |

## Coverage Confirmation

| Story | AC ID | Test Case Reference | Status |
|-------|-------|---------------------|:------:|
| **STORY-IMG-01** | | | |
| STORY-IMG-01 | AC-1 (DDL migration) | TC-IMG01-001 | ✅ |
| STORY-IMG-01 | AC-2 (POST with image) | TC-IMG01-002 | ✅ |
| STORY-IMG-01 | AC-3 (POST without image) | TC-IMG01-003 | ✅ |
| STORY-IMG-01 | AC-4 (PUT with new image) | TC-IMG01-004 | ✅ |
| STORY-IMG-01 | AC-5 (PUT without image) | TC-IMG01-005 | ✅ |
| STORY-IMG-01 | AC-6 (Invalid format/size) | TC-IMG01-006, TC-IMG01-007 | ✅ |
| STORY-IMG-01 | AC-7 (Static access) | TC-IMG01-008 | ✅ |
| **STORY-IMG-02** | | | |
| STORY-IMG-02 | AC-1 (Preview on select) | TC-IMG02-001 | ✅ |
| STORY-IMG-02 | AC-2 (FormData submit) | TC-IMG02-002 | ✅ |
| STORY-IMG-02 | AC-3 (Edit shows existing) | TC-IMG02-003 | ✅ |
| STORY-IMG-02 | AC-4 (Replace on edit) | TC-IMG02-004 | ✅ |
| STORY-IMG-02 | AC-5 (Remove image) | TC-IMG02-005 | ✅ |
| STORY-IMG-02 | AC-6 (Keep unchanged) | TC-IMG02-006 | ✅ |
| **STORY-IMG-03** | | | |
| STORY-IMG-03 | AC-1 (List with imageUrl) | TC-IMG03-001 | ✅ |
| STORY-IMG-03 | AC-2 (List no imageUrl) | TC-IMG03-002 | ✅ |
| STORY-IMG-03 | AC-3 (Detail with imageUrl) | TC-IMG03-003 | ✅ |
| STORY-IMG-03 | AC-4 (Detail no imageUrl) | TC-IMG03-004 | ✅ |
| STORY-IMG-03 | AC-5 (Image load failure) | TC-IMG03-005 | ✅ |

> **Every AC has at least one test case mapped.** All 18 ACs across 3 stories are covered.

## Test Case Quality Review (Gate D pre-requisite)

### Review Items

| Item | Description | Pass Criteria |
|------|-------------|---------------|
| **Path verification** | All referenced automation file paths (unit/integration/E2E) must exist | No `⏳ pending` or non-existent references |
| **Data alignment** | Test usernames/passwords match actual seed data (DataSeeder) | Every role account matches seed exactly |
| **Bidirectional traceability** | Both AC→TC mapping table and TC→AC reverse index exist | Both tables present and cross-referenceable |
| **Implementation status** | Each TC has a status flag (✅ implemented / ⏳ pending) | ✅ count ≥ core AC count |

### Review Record

| Story | Path verification | Data alignment | Bidirectional trace | Impl status | Verdict |
|-------|:----------------:|:--------------:|:-------------------:|:-----------:|:-------:|
| STORY-IMG-01 | ⏳ pending | ✅ (no auth needed for backend test) | ✅ | ⏳ pending | ⏳ (pre-implementation) |
| STORY-IMG-02 | ⏳ pending | ⏳ pending (admin auth TBD) | ✅ | ⏳ pending | ⏳ (pre-implementation) |
| STORY-IMG-03 | ⏳ pending | ⏳ pending (public pages, no auth) | ✅ | ⏳ pending | ⏳ (pre-implementation) |

> **Note:** All items are marked ⏳ pending because implementation has not started. The structure ensures every item can be verified after implementation. Path verification will be confirmed once test files are created (after Sprint implementation begins). The AC→TC mapping and reverse index are already complete, satisfying bidirectional traceability.

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|:----------:|:------:|------------|
| API 从 JSON 改为 multipart，破坏现有 POST/PUT | Medium | High | 同步更新 Admin 前端相关代码；回归覆盖 GET 接口不受影响 |
| Playwright E2E 首次搭建需时间 | Medium | Medium | D1 优先搭建脚手架 + health-check |
| 图片上传 5MB 限制测试需准备大文件 | Low | Low | 用 `dd` 或 PowerShell 生成测试文件 |
| 管理端已登录态认证影响 E2E | Medium | Medium | 需确认认证方式（token/cookie），E2E 中处理登录 |
| 客户端图片降级方案难以自动化验证 | Low | Low | 手动模拟网络断开、返回 404 图片 URL |

## Dependencies
- **Blocked by:** Playwright installation (first Sprint — infrastructure task)
- **Blocks:** Sprint-02 — no downstream dependencies

## Go/No-Go Criteria for Sprint Test Completion
- [ ] All unit tests pass (green)
- [ ] All integration tests pass (green)
- [ ] All E2E tests pass — zero failures
- [ ] Coverage targets met (line ≥80%, endpoint 100%, AC 100%)
- [ ] No unresolved sprint-blocker bugs
- [ ] test-report.md produced and reviewed
- [ ] Manual visual inspection completed (all pages, 375px responsive)
- [ ] Suite promoted (functional.md / regression.md updated)
