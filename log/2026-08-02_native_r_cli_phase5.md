# Phase 5：R 原生 CLI 与绘图输入验证

## 变更

- 为 `lib/ngsVizPlotMain.R` 增加原生 `describe`、`validate-plot`、`run-plot` 分发，同时保留单目录参数直接调用旧 `Main(args)` 的行为。
- 新增统一 JSON response、严格参数解析、R 环境检查、CLI 描述和只读绘图输入验证模块。
- `validate-plot` 检查 R 版本与既有绘图依赖，不安装包；验证输入目录、plot-setting JSON、多个样本名、coverage CSV、数值有限性和数据点列数。
- coverage 全零矩阵只记录 `COVERAGE_ALL_ZERO` warning，不阻止验证通过。
- `run-plot` 本阶段固定返回 `unsupported` 和退出码 `2`，未调用现有绘图逻辑。
- 新增基于 base R `stopifnot` 和临时目录的回归测试，不引入 `testthat`。

## 验证

- `Rscript tests/r/run_r_tests.R`：通过。
- `Rscript lib/ngsVizPlotMain.R describe --format json`：通过，退出码 `0`。
- `Rscript lib/ngsVizPlotMain.R validate-plot --input Samples/Result/H3K4me3_point --format json`：通过，退出码 `0`。
- 验证前后示例目录全部文件的 SHA-256 一致，未生成 manifest 或图片。
- `run-plot` 示例返回 `unsupported`，退出码 `2`。
- `rg -n "java -jar|NGSViz-1.2.jar" lib/cli`：无匹配。
- `lib/cli` 中未发现 `install.packages`、`pak::pkg_install`、进程调用或绘图设备调用。
