# Agent Guide 英文化

## 变更内容

- 将 `agent/ngsViz_agent.md` 完整改写为标准技术英文。
- 保留 Java 计算、R 可视化、四类工作流、执行确认及失败处理等原有约束。
- 统一文档与 `ngsviz-agent` skill 中的英文术语。

## 自查结果

- `tests/contracts/01_check_agent_guide.sh` 通过。
- 中文字符扫描通过，目标文档未残留中文内容。
- `git diff --check` 通过。
