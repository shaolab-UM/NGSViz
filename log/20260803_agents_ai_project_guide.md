# AGENTS AI 项目指南补写

## 变更内容

- 用英文补写根目录 `AGENTS.md`。
- 说明 ngsViz 的项目定位、Java 计算模块、R 可视化模块、R Shiny、SQLite 参考数据库与 Codex skills 的职责。
- 将 AI 文档入口更新为 `agent/ai-friendly/` 下的英文契约文件。
- 明确四类工作流、验证与确认机制、多样本串行规则、Java/R 解耦边界以及结果文件保护规则。
- 保留详细 schema、参数、退出码和 manifest 定义在专门契约文档中，避免重复维护。

## 自查范围

- 检查 `AGENTS.md` 中引用的项目文件均存在。
- 检查旧的 `docs/ai-friendly/` 路径未写入新文档。
- 运行 Agent Guide 契约检查，确认新增根指南未影响既有协作契约。
