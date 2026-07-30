# 2026-07-30 AI-friendly 原生架构 Phase 0

## 执行内容

- 新增原生架构与协作边界文档。
- 新增 Phase 0 基线报告。
- 只读记录 Git、Java、R、Maven、SQLite、示例 BAM/BAI 和 `Samples/Result` 产物基线。
- 未修改 Java/R 代码、测试、数据库、示例数据或现有产物。
- 保留采集前已有的用户修改，不纳入本 Phase。

## 基线结果

- Git commit：`bbd9fce6f9856968f4f48fa65eb4a596145dcc49`
- Java：OpenJDK Corretto `22.0.2`
- Rscript：R `4.5.0`
- SQLite `PRAGMA integrity_check`：`ok`
- `mvn test`：失败；3 个测试中 2 个失败，均为 CLI 子进程期望退出码 `0`、实际 `255`

## 变更文件

- `docs/ai-friendly/00_原生架构与协作边界.md`
- `audit/ai_friendly_native_phase0_baseline.md`
- `log/2026-07-30_ai_friendly_native_phase0.md`

## 验证

- `git diff --check`：通过。
- `mvn test`：失败；复现基线中的 2 个 CLI 退出码断言失败。
- `sqlite3 -readonly database/genomeCoordinate.db "PRAGMA integrity_check;"`：通过，输出 `ok`。
- `rg -n "不使用 Python|Java.*计算|R.*可视化|互相启动" docs/ai-friendly/00_原生架构与协作边界.md`：通过，匹配架构边界关键句。

`mvn test` 的既有失败不在 Phase 0 修改范围。
