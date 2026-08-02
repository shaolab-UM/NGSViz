# Java 原生计算运行 Phase 4 实施日志

## 实施范围

- 新增 `run-compute --request <json>`，运行前复用 Phase 3 `ComputeValidator`。
- JSON 请求和旧 flags 均先解析为单样本 `ComputeRequest`，再进入统一验证、参数映射和 `MainCalculator` 调用点。
- 显式映射已验证参数到 `InputParameterAttributes`，沿用现有 Java coverage 计算与 CSV/plot-setting 输出。
- plot-setting JSON 新增 `run_metadata`，写入 `sample_id`、`sample_name`、`group_name`；绘图参数同时写入 `group_list`。
- 计算 stdout/stderr 写入 `NGSViz_compute.log`，并在 `finally` 恢复原始 stream。
- 成功后验收 coverage CSV、read-count CSV 和 plot-setting JSON 均存在且非空。
- 生成单样本 `compute_manifest.json`；计算异常返回 exit code 5，产物异常返回 exit code 6。
- 未调用 R、未检查图片、未修改 coverage、scaling、normalization 等科学算法。

## 测试与自查

- `mvn -Dtest=RunComputeCommandTest,ComputeManifestTest test`：6 个测试通过。
- `mvn test`：38 个测试通过。
- `rg -n "Rscript|ngsVizPlotMain" src/main/java/com/NGSViz/cli`：无匹配。
- 覆盖 validator 阻断、MainCalculator 异常、产物缺失、manifest 必需字段、无可视化状态、单次单样本 manifest、日志隔离、旧 flags 默认值和 JSON 参数等价性、统一计算调用点。
- 未运行真实 Samples；集成运行保留到 Phase 8。
