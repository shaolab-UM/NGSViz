# Phase 6：R 原生绘图运行与可视化 manifest

## 变更

- 实现 `run-plot`，并让旧单目录入口统一进入同一验证、绘图、产物验收和 response 流程。
- 每次绘图前重新调用 `validate_plot_input`；验证失败时不加载或调用绘图函数。
- 依次复用 `mergeAveragePlot`、`mergeMultiVisResult`、`propressData`、`corPlot` 和 `runPCA`，未复制绘图算法。
- 根据 plot-setting 数量和 `file_type` 验收本次应生成的图片，缺失或空文件返回 exit code 6，绘图异常返回 exit code 5。
- 绘图消息与 warning 写入 `NGSViz_visualization.log`，CLI stdout 只输出最终 JSON response。
- 原子写入 `visualization_manifest.json`，记录 R 版本、运行时间、输入 plot-setting、coverage 文件、图片、日志、warnings 和 errors。
- manifest 不记录 BAM、SQLite、database、compute request 或 compute status；R CLI 不调用 Java。
- 原生 CLI 加载既有绘图模块时跳过其顶层依赖安装逻辑，只加载 Phase 5 已验证存在的依赖。

## 测试与自查

- 使用 mock 绘图函数和临时目录覆盖验证短路、绘图异常、图片缺失、成功图片验收、manifest 必需字段和禁止字段。
- `Rscript tests/r/run_r_tests.R`：通过。
- `Rscript lib/ngsVizPlotMain.R describe --format json`：通过，stdout 为单个 JSON response。
- `rg -n "java -jar|NGSViz-1.2.jar" lib/cli`：无匹配。
- 未重新计算或绘制 `Samples`。
