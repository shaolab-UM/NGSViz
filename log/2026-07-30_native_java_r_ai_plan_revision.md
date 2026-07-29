# 2026-07-30 原生 Java/R AI 友好计划修订

- 架构决定：不使用 Python Agent Adapter。
- Java 计算模块和 R 可视化模块保持两个独立 CLI。
- Java 不调用 R，R 不调用 Java；Codex Skill 只做外部编排。
- 计算请求默认使用 Java 已有 `org.json` 可处理的 JSON。
- Java 每个进程只计算一个 sample，可选一个 control BAM。
- AI `analysis_plan.json` 可包含 `samples[]`，由 Codex 展开后严格串行启动多个 Java 进程。
- 每个 sample 保留独立 request、输出目录和 `compute_manifest.json`；`analysis_index.json` 只做索引。
- Java 计算结束不自动绘图，R 可在后续独立任务中调用。
- 多样本 R 可视化使用独立 workspace 汇集 plot-setting JSON，不移动 coverage CSV。
- 新活动计划：`docs/superpowers/plans/2026-07-30-ngsviz-native-ai-friendly-phased-prompts.md`
- 两份旧 Python 方案已标记为废弃。
- 本次只修改计划与日志，未实施任何 Java/R 功能。
