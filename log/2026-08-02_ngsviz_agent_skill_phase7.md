# Phase 7：Codex Agent Guide 与项目 Skill

## 变更

- 新增 `agent/ngsViz_agent.md`，定义单样本只计算、多样本顺序计算、只可视化和完整流程四条路径。
- 新增项目 Skill `.agents/skills/ngsviz-agent/`，限定默认只验证、确认后运行、Java 单样本和 Codex 串行多样本边界。
- 新增 `analysis_plan.schema.json` 与双样本示例，区分共享计算参数和 sample 输入，限制执行模式及失败策略。
- 新增契约脚本，检查六个原生子命令、guide 文件类型、Skill 边界和示例唯一性。

## 自查

- `bash tests/contracts/01_check_agent_guide.sh`：通过。
- 用户指定的 `find`、`rg` 和 R 唯一性检查：通过。
- `skill-creator/scripts/quick_validate.py .agents/skills/ngsviz-agent`：通过。
