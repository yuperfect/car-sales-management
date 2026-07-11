# Test Cases: STORY-IMG-01 — 后端加图片列 + 文件上传接口

## Story Metadata
- **Story:** STORY-IMG-01
- **Points:** 3
- **Priority:** High
- **Dependencies:** None

---

## AC → Test Case Mapping

| AC ID | AC Description | Test Case(s) | Type |
|-------|---------------|:------------:|:----:|
| AC-1 | DDL migration: `car` 表增加 `image_url VARCHAR(500) DEFAULT NULL` | TC-IMG01-001 | Manual |
| AC-2 | POST with image: 成功保存文件、写入 DB、返回 imageUrl | TC-IMG01-002 | Integration / E2E |
| AC-3 | POST without image: `image_url` = NULL，正常创建 | TC-IMG01-003 | Integration / E2E |
| AC-4 | PUT with new image: 覆盖旧图、更新 image_url | TC-IMG01-004 | Integration / E2E |
| AC-5 | PUT without image field: 原有图片不变 | TC-IMG01-005 | Integration / E2E |
| AC-6 | Invalid format (>5MB or not jpg/png/webp) → 400 | TC-IMG01-006, TC-IMG01-007 | Integration |
| AC-7 | Static access `/uploads/images/{filename}` → 正常显示 | TC-IMG01-008 | Manual / E2E |

---

## Test Cases Detail

### TC-IMG01-001: DDL 迁移脚本执行
| Field | Value |
|-------|-------|
| **ID** | TC-IMG01-001 |
| **Description** | 验证 DDL 脚本 `ALTER TABLE car ADD COLUMN image_url VARCHAR(500) DEFAULT NULL` 成功执行 |
| **Precondition** | `car` 表已存在，包含现有字段（id, brand, model, year, price 等），无 `image_url` 列 |
| **Steps** | 1. 连接目标数据库（H2/MySQL）<br>2. 执行 DDL 脚本<br>3. 运行 `DESCRIBE car` 或 `SELECT column_name FROM information_schema.columns WHERE table_name='car'` |
| **Expected Result** | `image_url` 列存在，类型为 `VARCHAR(500)`，默认值为 `NULL`，允许空值 |
| **Type** | Manual |
| **Status** | ⏳ pending |

---

### TC-IMG01-002: POST /api/cars — 含图片成功上传
| Field | Value |
|-------|-------|
| **ID** | TC-IMG01-002 |
| **Description** | 以 `multipart/form-data` 提交新增车辆请求，含 jpg/png/webp 图片，验证返回含 imageUrl |
| **Precondition** | 后端服务运行；`uploads/images/` 目录存在且可写；准备测试图片文件（1.jpg, valid jpg <5MB） |
| **Steps** | 1. 构造 `multipart/form-data` 请求: brand=Toyota, model=Camry, year=2024, price=250000<br>2. 添加 `image` part，上传 test-image.jpg<br>3. 发送 `POST /api/cars`<br>4. 检查响应状态码 200/201<br>5. 检查响应体含 `imageUrl` 字段，格式为 `/uploads/images/{carId}.jpg`<br>6. 检查 `uploads/images/` 目录下存在对应文件<br>7. 查询 DB `car` 表，`image_url` 字段不为 NULL |
| **Expected Result** | - HTTP 200/201<br>- 响应 JSON 含 `imageUrl: "/uploads/images/1.jpg"`<br>- 文件 `uploads/images/1.jpg` 实际存在<br>- DB 中 `image_url` 列值匹配 |
| **Type** | Integration |
| **Status** | ⏳ pending |

---

### TC-IMG01-003: POST /api/cars — 不含图片
| Field | Value |
|-------|-------|
| **ID** | TC-IMG01-003 |
| **Description** | 以 `multipart/form-data` 提交新增车辆请求，不含图片文件，验证 `image_url` = NULL |
| **Precondition** | 后端服务运行 |
| **Steps** | 1. 构造 `multipart/form-data` 请求: brand=Honda, model=Accord, year=2023, price=220000<br>2. **不添加** `image` part<br>3. 发送 `POST /api/cars`<br>4. 检查响应状态码 200/201<br>5. 检查响应体含 `imageUrl` 为 `null` 或不含该字段<br>6. 查询 DB `car` 表对应记录的 `image_url` 为 NULL |
| **Expected Result** | - HTTP 200/201<br>- `imageUrl` = null<br>- DB 中 `image_url` 为 NULL |
| **Type** | Integration |
| **Status** | ⏳ pending |

---

### TC-IMG01-004: PUT /api/cars/{id} — 更新图片
| Field | Value |
|-------|-------|
| **ID** | TC-IMG01-004 |
| **Description** | 编辑已有车辆，上传新图片，验证旧图被覆盖、image_url 更新 |
| **Precondition** | 存在一辆已有图片的车辆（通过 TC-IMG01-002 创建），原图路径 `uploads/images/1.jpg` |
| **Steps** | 1. 构造 `multipart/form-data` PUT 请求到 `/api/cars/1`<br>2. 字段: brand=Toyota, model=Camry (保持其他字段不变)<br>3. 上传新图片 new-image.png<br>4. 发送请求<br>5. 检查响应状态码 200<br>6. 检查 `imageUrl` 更新（扩展名可能保持原样或变更为 .png）<br>7. 验证旧图片文件已被覆盖（同名文件内容不同/时间戳更新） |
| **Expected Result** | - HTTP 200<br>- `imageUrl` 指向新上传的图片<br>- 旧图片内容被新图片内容替换 |
| **Type** | Integration |
| **Status** | ⏳ pending |

---

### TC-IMG01-005: PUT /api/cars/{id} — 不含图片字段
| Field | Value |
|-------|-------|
| **ID** | TC-IMG01-005 |
| **Description** | 编辑已有车辆，不包含图片字段，验证原图片不变 |
| **Precondition** | 存在一辆有图片的车辆（通过 TC-IMG01-002 创建） |
| **Steps** | 1. 构造 `multipart/form-data` PUT 请求到 `/api/cars/1`<br>2. 仅更新文本字段（如 price=260000）<br>3. **不添加** `image` part<br>4. 发送请求<br>5. 检查响应状态码 200<br>6. 检查 `imageUrl` 与原值相同<br>7. 查询 DB，`image_url` 不变<br>8. 验证 `uploads/images/1.jpg` 未被修改 |
| **Expected Result** | - HTTP 200<br>- `imageUrl` 保持不变<br>- DB 记录不变<br>- 文件未被修改 |
| **Type** | Integration |
| **Status** | ⏳ pending |

---

### TC-IMG01-006: POST /api/cars — 图片格式不合法
| Field | Value |
|-------|-------|
| **ID** | TC-IMG01-006 |
| **Description** | 上传非 jpg/png/webp 格式的图片（如 .gif、.bmp、.svg），验证返回 400 |
| **Precondition** | 后端服务运行；准备非法格式测试文件（test.gif, test.bmp） |
| **Steps** | 1. 构造 `multipart/form-data` 请求到 `POST /api/cars`<br>2. 上传 test.gif 到 `image` part<br>3. 发送请求<br>4. 检查响应状态码 400 |
| **Expected Result** | HTTP 400，错误信息提示仅支持 jpg/png/webp |
| **Type** | Integration |
| **Status** | ⏳ pending |

---

### TC-IMG01-007: POST /api/cars — 图片超过 5MB
| Field | Value |
|-------|-------|
| **ID** | TC-IMG01-007 |
| **Description** | 上传超过 5MB 的图片，验证返回 400 |
| **Precondition** | 后端服务运行；准备 >5MB 测试文件（可用 PowerShell 生成） |
| **Steps** | 1. 构造 `multipart/form-data` 请求到 `POST /api/cars`<br>2. 上传 >5MB 文件到 `image` part<br>3. 发送请求<br>4. 检查响应状态码 400 |
| **Expected Result** | HTTP 400，错误信息提示文件大小超过限制（5MB） |
| **Type** | Integration |
| **Status** | ⏳ pending |

---

### TC-IMG01-008: 静态资源直接访问
| Field | Value |
|-------|-------|
| **ID** | TC-IMG01-008 |
| **Description** | 通过浏览器直接访问已上传图片的 URL，验证可正常显示 |
| **Precondition** | 存在已上传图片 `uploads/images/1.jpg`；后端服务运行并已配置静态资源映射 |
| **Steps** | 1. 打开浏览器/发送 GET 到 `http://localhost:8080/uploads/images/1.jpg`<br>2. 检查响应状态码 200<br>3. 检查 Content-Type 为 image/jpeg 或 image/png<br>4. 检查响应体长度 >0<br>5. 尝试访问不存在的图片 `/uploads/images/999.jpg`，验证返回 404 |
| **Expected Result** | - 存在的图片: HTTP 200，正常显示图片内容<br>- 不存在的图片: HTTP 404 |
| **Type** | Integration |
| **Status** | ⏳ pending |

---

## Reverse Index: TC → AC Mapping

| TC ID | AC ID(s) | Description |
|-------|:--------:|-------------|
| TC-IMG01-001 | AC-1 | DDL migration |
| TC-IMG01-002 | AC-2 | POST with image — full success path |
| TC-IMG01-003 | AC-3 | POST without image — null handling |
| TC-IMG01-004 | AC-4 | PUT with new image — overwrite |
| TC-IMG01-005 | AC-5 | PUT without image — keep unchanged |
| TC-IMG01-006 | AC-6 | Invalid format validation |
| TC-IMG01-007 | AC-6 | Oversized file validation |
| TC-IMG01-008 | AC-7 | Static resource access |

---

## Test Data Requirements

| Data Item | Value | Purpose |
|-----------|-------|---------|
| test-image.jpg | 100x100, valid JPEG, <5MB | Positive upload test |
| test-image.png | 100x100, valid PNG, <5MB | PNG format test |
| test-image.webp | 100x100, valid WebP, <5MB | WebP format test |
| test-image.gif | 1x1, valid GIF, <100KB | Invalid format test |
| oversized-image.jpg | 6MB JPEG | Size limit test |
| new-image.png | 50x50, valid PNG, <100KB | Replace/overwrite test |
