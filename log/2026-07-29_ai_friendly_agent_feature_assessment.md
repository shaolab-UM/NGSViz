# 2026-07-29 AI 友好使用功能评估

- 类型：只读架构评估
- 范围：Java CLI、SQLite 配置库、R 绘图入口、Shiny 执行封装、环境与示例产物
- 结果：建议新增独立 Agent 适配层，首版不修改科学计算逻辑
- 难度：中等
- 工期：原型 2–3 人日，MVP 6–9 人日，投稿级版本 12–18 人日
- 报告：`audit/ai_friendly_agent_feature_assessment_2026-07-29.md`
- 自查：
  - 本地 Java 22.0.2 与 R 4.5.0 可用
  - `NGSViz-1.2.jar`、SQLite 主库和示例 BAM/BAI 存在
  - SQLite `PRAGMA integrity_check` 返回 `ok`
  - 未修改 Java、R 或科学计算行为
