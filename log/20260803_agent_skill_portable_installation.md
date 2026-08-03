# Agent Skill 跨工作目录安装文档更新

- 将 Agent Skill 源目录从 `.agents/skills/` 更新为 `agent/skills/`，并同步 README、项目约束和 AI 编排合同中的引用。
- 明确首次使用前需将四个 ngsViz skill 安装到当前 AI 框架的用户级 skill 目录，不限定 Codex、Claude 或 Kimi。
- 规定分析项目为正常工作目录；通过已安装 skill 的真实源路径、`NGSVIZ_HOME` 或用户明确提供的绝对路径解析 ngsViz 安装根目录。
- 规定解析安装根目录后，以绝对路径读取 `agent/ngsViz_agent.md`、`agent/ai-friendly/`、Java、JAR、R 脚本和配置文件。
- 将有效用户文档和约束文档中的 Java/JAR、R CLI 示例改为模糊绝对路径，避免依赖 ngsViz 仓库作为当前工作目录。

## 自查

- 检查有效文档中不再引用旧 `.agents/skills/` 源目录。
- 检查 Java/JAR 与 R 可视化入口均使用绝对路径模板。
- 通过临时用户级 skill 符号链接验证可以反向定位 ngsViz 根目录及两组权威 Agent 文档。
- 运行 `git diff --check`，未发现空白错误。
