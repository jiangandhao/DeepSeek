# 数据库设计

引擎 InnoDB,字符集 utf8mb4。建表脚本见 `deploy/init.sql`(容器首次启动自动执行)。

## 表清单

| 表 | 说明 | 关键字段 |
|---|---|---|
| `user` | 用户与登录凭证 | `username`(唯一)、`password`(BCrypt)、`phone`(加密) |
| `health_profile` | 健康档案(健管师/风险预警预留) | `height_cm`、`weight_kg`、`family_history`、`diabetes_type` |
| `health_device` | 智能健康设备绑定与模拟上报 | `device_no`、`status`、`battery_level`、`last_value_mmol` |
| `glucose_record` | 血糖记录 | `value_mmol`、`period`、`measured_at`、`idx_user_time` |
| `diet_record` | 饮食记录 | `meal_type`、`food`、`calories`、`carbs_g`、`eaten_at` |
| `exercise_record` | 运动记录 | `type`、`duration_min`、`intensity`、`done_at` |
| `ai_advice` | AI 生成方案 | `type`、`content`(Markdown)、`context`(数据/检索摘要) |
| `alert` | 预警记录 | `category`、`level`、`message`、`is_read` |
| `chat_message` | AI 对话历史 | `conversation`、`role`、`content` |

## 设计要点

- **索引**:记录表均建 `(user_id, 时间)` 复合索引,支撑「按用户查最近 N 条 / 时间范围」的高频查询。
- **时段枚举**:`glucose_record.period` 区分空腹/餐前/餐后/睡前,用于按时段的异常阈值判定与可视化分组。
- **可追溯**:`ai_advice.context` 保存生成时的数据摘要与 RAG 召回片段,便于复盘与论文展示 AI 决策依据。
- **脱敏**:`phone` 等敏感字段长度预留为加密后密文长度;明文不落库。
- **扩展性**:面向分库分表设计,均以 `user_id` 作为天然分片键。

## E-R 关系

`user` 1—1 `health_profile`;`user` 1—N `health_device / glucose_record / diet_record / exercise_record / ai_advice / alert / chat_message`。
