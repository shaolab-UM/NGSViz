# AI JSON 计算请求契约统一记录

- 日期：2026-07-30
- 范围：仅修改架构文档、实现计划、README 和历史文档状态说明；未修改 Java/R 代码。

## 架构决定

- AI 计算参数只通过单样本 `compute_request.json` 传入 Java。
- AI 调用只使用 `validate-compute --request <path>` 或 `run-compute --request <path>`，不展开完整旧 CLI flags。
- 旧 CLI flags 与 JSON 请求是互斥入口，不定义参数覆盖优先级。
- 两种入口在 Java 内部归一化为同一个 `ComputeRequest`，共用 validator 和 `MainCalculator`。
- 不使用 `-J <yaml>`、YAML loader、Python、MCP 或 Web API。
- 现有 `-J` 仅作为未加载配置的历史占位参数记录，不属于新 AI-friendly 接口。

## 文档变更

- 更新 `docs/ai-friendly/00_原生架构与协作边界.md`，增加输入归一化关系和互斥规则。
- 重点更新 `docs/superpowers/plans/2026-07-30-ngsviz-native-ai-friendly-phased-prompts.md`：
  - 将统一 `ComputeRequest` 设为全局硬约束。
  - 更新 Phase 0、Phase 1A、Phase 2、Phase 3、Phase 4 和 Phase 8。
  - Phase 3 直接读取 JSON，不生成可执行的完整旧 flags 命令。
  - Phase 4 增加 `LegacyComputeRequestAdapter` 及兼容性测试要求。
- 更新 `README.md`，将 `-J` 标记为不加载配置的 deprecated placeholder，并增加计划中的 JSON 请求入口说明。
- 强化两份 2026-07-29 旧计划和旧评估报告的历史状态说明，明确其 Python/YAML 路径不得执行。

## 自查

- `git diff --check`：通过。
- 文档冲突扫描：活动计划和架构文档中不存在 YAML 执行入口或 JSON 转完整旧 flags 后执行的路径。
- 本次仅修改文档，未运行 Java/R 测试。
