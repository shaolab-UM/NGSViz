# Phase 8 原生接口端到端验收日志

## 当前状态

- 环境预检：通过。Java 22、`NGSViz-1.0.jar`、SQLite、`hg38` 和 `hg19` 坐标表均可用。
- `mvn test`：通过，38 个测试全部成功。
- `mvn -DskipTests package`：通过。
- `Rscript tests/r/run_r_tests.R`：通过。
- `tests/contracts/01_check_agent_guide.sh`：通过。
- 独立性契约：首次运行发现脚本缺失，已按 Phase 8 边界补充。
- `git diff --check`：首次发现 Maven 生成的 `dependency-reduced-pom.xml` 含 CRLF 尾随空白；已关闭 Shade 的该生成副作用。
- 独立性契约：补充后复跑通过。
- `git diff --check`：规范化 Maven 生成文件后复跑通过。
- 单样本 `validate-compute`：通过，exit code 0，未创建 `docs/ai-friendly/examples/validate_compute_output`。
- 多样本 dry validation：两份独立 request 均不含 `samples`，标识与输出目录唯一；严格按 H3K4me3、H3K36me3 顺序验证，均为 exit code 0。
- validation 产物检查：未创建 `agent_output/native_smoke/H3K4me3` 或 `agent_output/native_smoke/H3K36me3`。
- Java 批次确认：已取得。
- H3K4me3 `run-compute`：exit code 0，manifest 为 `completed`，CSV 与 plot-setting 验收通过，未生成图片。
- H3K36me3 `run-compute`：在第一项退出、manifest 检查和索引更新后启动；exit code 0，manifest 为 `completed`，未生成图片。
- `analysis_index.json`：按顺序逐项更新，两个样本均为 `completed`。
- Java/R 独立性：Java CLI 包不含 `Rscript` 或 `ngsVizPlotMain` 调用。
- R CLI 契约修正：首次 validation 发现实现使用旧的 `--input --format json`；已改为契约规定的 `--input-dir`，R 回归与两项契约检查通过。
- 延迟绘图 workspace：仅复制两份 plot-setting JSON，未复制 coverage CSV。
- `validate-plot`：exit code 0，识别 2 个 plot setting，未创建图片或 `visualization_manifest.json`。
- R 批次确认：已取得。
- R `run-plot` 首次执行：exit code 5；双样本 PCA 只有一个可用主成分，旧实现无条件赋两个列名导致失败。
- 双样本 PCA 修复：保留真实 PC1，为二维展示补 PC2=0 和方差 0；新增回归测试并通过。
- R `run-plot` 修复后执行：exit code 0，manifest 为 `completed`，四张预期图片均存在且非空。
- R/Java 独立性：R CLI 目录不含 `java -jar` 或 NGSViz JAR 调用。
- CSV 不变性：R 后四个 SHA-256 与 Java 后记录完全一致。
- 最终全量回归：`mvn test`、package、R tests、两项 contract 和 `git diff --check` 全部通过。

## Java 后、R 前 CSV 校验值

- H3K4me3 coverage：`a73da6a77c7540c80bc00949c470c4c0a858868bb39c5a648ceb5c42634e4fc1`
- H3K4me3 read count：`8445577fe43f7d68a19f0459692e86a44506062efb997722ad4baf78539d8c02`
- H3K36me3 coverage：`8620bce76914ed129a5427936f0f23a3da7a9ae5bc98164583a2c8cdd9a33914`
- H3K36me3 read count：`f04907035f18d0e886233be44acf89103f5dd6ae245432780a55cbfc6a9a3af8`
