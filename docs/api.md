# 接口文档

后端基址:`http://localhost:8080`(开发)。除认证接口外,均需请求头 `Authorization: Bearer <token>`。
统一响应:`{ "code": 0, "message": "success", "data": ... }`,`code != 0` 表示错误。

> 在线交互文档(Knife4j):启动后端后访问 `http://localhost:8080/doc.html`。
> AI 服务文档(Swagger):`http://localhost:8000/docs`。

## 1. 认证 `/api/auth`

| 方法 | 路径 | 说明 | 请求体 |
|---|---|---|---|
| POST | `/register` | 注册 | `{username, password, nickname?}` |
| POST | `/login` | 登录 | `{username, password}` |

返回 `data`:`{token, userId, username, nickname}`。

## 2. 血糖记录 `/api/glucose`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `` | 新增 `{valueMmol, period, measuredAt, note?}` |
| GET | `?from=&to=` | 查询(可选时间范围,ISO 时间) |
| DELETE | `/{id}` | 删除 |

`period`:`FASTING/BEFORE_MEAL/AFTER_MEAL/BEDTIME/RANDOM`。

## 3. 饮食记录 `/api/diet`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `` | `{mealType, food, calories?, carbsG?, eatenAt}` |
| GET | `?from=&to=` | 查询 |
| DELETE | `/{id}` | 删除 |

`mealType`:`BREAKFAST/LUNCH/DINNER/SNACK`。

## 4. 运动记录 `/api/exercise`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `` | `{type, durationMin, intensity, calories?, doneAt}` |
| GET | `?from=&to=` | 查询 |
| DELETE | `/{id}` | 删除 |

`intensity`:`LOW/MEDIUM/HIGH`。

## 5. AI 血糖智能体 `/api/ai`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/advice` | 生成方案/问答(非流式,落库)。Body `{question?}`,为空生成综合方案 |
| GET | `/advice` | 历史方案列表 |
| POST | `/chat/stream` | 流式对话(SSE,`text/event-stream`)。Body `{question?}` |
| POST | `/predict?horizon=6` | 血糖趋势预测 |
| GET | `/anomaly` | 血糖异常检测 |

`/predict` 返回:`{predictions:[{value_mmol, measured_at}], model, metrics:{mae,rmse,slope_per_h}}`。
`/anomaly` 返回:`{anomalies:[{measured_at, value_mmol, category, level, message}]}`。

## 6. 健康档案 / 数智健管师 / 风险预警(阶段4)

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/profile` | 获取健康档案 |
| PUT | `/api/profile` | 保存/更新档案 `{heightCm, weightKg, familyHistory, chronic, diabetesType}` |
| POST | `/api/risk/assess` | 疾病风险评估(中/高风险自动落库预警),返回 `{level, score, factors, metrics}` |
| POST | `/api/risk/health-plan` | AI 数智健管师:风险解读 + 个性化处方 |
| POST | `/api/risk/structured-plan` | 结构化方案，返回营养目标、4 餐食谱、替换项、七日运动和监测建议 |
| POST | `/api/risk/plans` | 显式保存用户确认后的结构化综合方案 |
| GET | `/api/risk/plans` | 已保存方案的历史版本 |
| GET | `/api/risk/alerts` | 预警列表 |
| PUT | `/api/risk/alerts/{id}/read` | 标记预警已读 |

## 7. 智能体检(阶段4)

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/exam/centers?lat=&lng=` | 智能推荐体检中心(距离+繁忙度匹配),返回含 `distanceKm`、`matchScore` |
| POST | `/api/exam/appointments` | 预约 `{centerId, examDate, pkg}` |
| GET | `/api/exam/appointments` | 我的预约 |
| DELETE | `/api/exam/appointments/{id}` | 取消预约 |

## 8. 体检报告与影像分析

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/reports/analyze` | 上传体检报告图片/PDF或医学影像，OCR/文本解析、指标结构化、知识检索并归档 |
| GET | `/api/reports` | 当前用户报告历史摘要 |
| GET | `/api/reports/{id}` | 报告完整结构化详情 |
| DELETE | `/api/reports/{id}` | 删除本人报告 |
| POST | `/api/imaging/detect` | 兼容接口：上传肺 CT 切片，返回候选区域和标注图 |

## 9. 账户 / 隐私脱敏(阶段5)

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/user/me` | 当前用户信息,手机号**脱敏展示**(`phoneMasked`) |
| PUT | `/api/user/me` | 更新账户;手机号 **AES 加密存储** `{nickname, phone, gender}` |

## 10. AI 服务内部接口(后端调用,基址 `http://localhost:8000`)

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health` | 健康检查 |
| POST | `/api/chat` / `/api/chat/stream` | 通用 DeepSeek 对话 |
| POST | `/api/analysis/predict` | 血糖预测(`{history:[{value_mmol,period,measured_at}], horizon}`) |
| POST | `/api/analysis/anomaly` | 异常检测(`{history:[...]}`) |
| POST | `/api/agent/advice` / `/advice/stream` | 智能体方案生成 |
| GET | `/api/agent/retrieve?q=&k=3` | RAG 检索调试(论文展示召回) |
| POST | `/api/risk/assess` | 疾病风险评分(规则模型) |
| POST | `/api/health/plan` / `/plan/stream` | 数智健管师方案 |
| POST | `/api/health/plan/structured` | 结构化饮食与七日运动方案（本地约束引擎 + 向量知识） |
| POST | `/api/reports/analyze` | 通用报告 OCR/PDF/医学图片分析 |
| POST | `/api/imaging/detect` | 肺结节检测(multipart) |

`GET /health` 会返回向量库引擎、维度和知识文档数。当前采用持久化 SQLite 哈希向量库，数据保存在 Docker `chroma_data` 卷中。

> 注意:AI 服务中血糖点字段为 **snake_case**(`value_mmol`/`measured_at`);后端 `AiService` 已做字段映射。

## 错误码

| code | 含义 |
|---|---|
| 0 | 成功 |
| 400 | 参数错误/业务异常 |
| 401 | 未登录或登录过期 |
| 502 | AI 服务/DeepSeek 调用失败(附操作建议) |
| 500 | 服务器内部错误 |
