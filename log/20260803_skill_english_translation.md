# Skill 英文化

## 变更内容

- 将 `.agents/skills/` 下 4 个 `SKILL.md` 的中文说明完整翻译为英文。
- 将 4 个 `agents/openai.yaml` 中的中文界面元数据翻译为英文。
- 保留原有命令、参数、执行顺序、安全边界和确认流程。
- 将链接中的中文文件名改为 URL 编码形式，保证 skill 文档只包含 ASCII 文本且链接目标不变。

## 自查结果

- 中文及非 ASCII 字符扫描通过。
- 4 个 skill 均通过 `quick_validate.py` 校验。
- `tests/contracts/01_check_agent_guide.sh` 通过。
- `tests/contracts/02_check_ngsviz_preflight_skills.py` 通过，共 6 项测试。
- 3 个 skill 辅助 Python 脚本通过语法编译检查。
- `git diff --check` 通过。
