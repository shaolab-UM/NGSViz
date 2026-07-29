# 2026-07-30 原生 Java/R AI 友好计划修订

- 架构决定：不使用 Python Agent Adapter。
- Java 计算模块和 R 可视化模块保持两个独立 CLI。
- Java 不调用 R，R 不调用 Java；Codex Skill 只做外部编排。
- 计算请求默认使用 Java 已有 `org.json` 可处理的 JSON。
- 新活动计划：`docs/superpowers/plans/2026-07-30-ngsviz-native-ai-friendly-phased-prompts.md`
- 两份旧 Python 方案已标记为废弃。
- 本次只修改计划与日志，未实施任何 Java/R 功能。
