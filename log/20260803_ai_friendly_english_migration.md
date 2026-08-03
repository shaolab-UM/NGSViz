# AI-friendly 契约英文化与迁移修复

## 变更内容

- 将 `agent/ai-friendly/` 下 4 份契约文档全部改为英文。
- 将 4 份 Markdown 文件名改为英文 ASCII 名称。
- 保留 Java 计算、R 可视化和 Codex 编排的接口边界、字段约束、状态规则和退出码。
- 修复 `ngsviz-agent` Skill、契约测试和示例请求中的旧 `docs/ai-friendly/` 路径。
- 保留 `docs/superpowers/plans/` 中的历史阶段记录，不改写其当时的路径和文件名。

## 自查结果

- `agent/ai-friendly/` 正文与文件名中文扫描通过。
- 3 份 JSON 示例与 schema 通过 `python3 -m json.tool` 校验。
- `tests/contracts/01_check_agent_guide.sh` 通过。
- 契约测试已同步为英文 Skill 断言，并允许 `agent/` 中保存 Markdown 文档与 JSON 契约。
- 非历史文档范围内的旧路径扫描通过。
- `git diff --check` 通过。
