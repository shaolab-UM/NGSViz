---
name: ngsviz-agent
description: Safely orchestrate ngsViz native Java computation and R visualization CLIs for single-sample compute, sequential multi-sample compute, visualization-only, and full workflows. Use when Codex must collect scientific parameters, prepare compute_request.json or analysis_plan.json, validate before confirmation, run ngsViz, or inspect separate compute and visualization manifests.
---

# ngsViz Agent

遵循 [Agent Guide](../../../agent/ngsViz_agent.md)、[Java CLI 契约](../../../docs/ai-friendly/01_Java计算CLI契约.md)、[R CLI 契约](../../../docs/ai-friendly/02_R可视化CLI契约.md) 和 [编排契约](../../../docs/ai-friendly/03_Codex编排与确认契约.md)。先根据用户意图区分四个用户路径：单样本只计算、多样本顺序计算、只可视化、完整流程。

## 不可越过的边界

- 对未知、缺失或含义不明确的科学参数提问；不得猜测 genome、region、strand、fragment length、control 或 normalization。
- 默认只执行 validate。展示验证结果、原生命令、输入、输出和范围后，等待用户明确确认才执行 run。
- Java 只执行单样本计算，Java 不调用 R。R 只执行已有结果的可视化，R 不调用 Java。
- `analysis_plan.json` 允许 `samples[]`；Java `compute_request.json` 禁止 `samples[]`，且只能包含一个 signal sample 和零或一个 control BAM。
- Codex 负责多样本顺序执行；Java 进程严格串行，禁止并发运行 sample。
- 默认使用 `stop_on_error`。仅在用户明确选择 `continue_on_error` 时继续失败后的队列，且仍须等待当前 Java 进程退出。
- 每个 sample 使用唯一的 `sample_id`、`sample_name`、`group_name` 和 `output_dir`。
- 用户只要求计算时不得自动绘图；用户只要求绘图时不得重新计算。
- 允许用户在后续新任务中只调用 R。Java 失败时不得调用 R。
- 分开报告 `compute_manifest.json` 与 `visualization_manifest.json`；不生成统一 manifest。
- `analysis_index.json` 只索引单样本 request 和 manifest，不汇总或改写科学结果。
- 不把 Codex 的判断写入原始结果文件，不覆盖、移动或改写原始计算结果。

## 路径一：单样本只计算

1. 提取需求并询问未知科学参数。
2. 写入单样本 `compute_request.json`，不得包含 `samples[]`。
3. 运行 Java `validate-compute`。
4. 展示验证结果与 Java `run-compute` 原生命令，询问确认。
5. 确认后运行一个 Java `run-compute` 进程并等待退出。
6. 读取并单独报告 `compute_manifest.json`，然后结束，不调用 R。

## 路径二：多样本顺序计算

1. 提取共享参数和 `samples[]`，写入符合 schema 的 `analysis_plan.json`。
2. 将共享参数与每个 sample 展开为独立的 `compute_request.<sample_id>.json`。使用 `${sample_name}_${analysis.title}` 生成唯一 title；不得让 sample 覆盖 genome、region、strand 或 fragment length，不一致时拆分 analysis plan。
3. 按数组顺序对每个 request 运行 Java `validate-compute`；全部验证成功前不得运行计算。
4. 展示固定有序队列、每项命令、唯一输出目录与失败策略，询问一次批次确认。
5. 每次只运行一个 Java `run-compute` 进程并等待退出。
6. 验证该 sample 的 `compute_manifest.json`，更新 `analysis_index.json`，然后才可启动下一个 sample。
7. `stop_on_error` 遇到失败立即停止；显式 `continue_on_error` 也不得并发。计算结束后不自动调用 R。

## 路径三：只可视化

1. 接收结果目录，并将其作为 R `input_dir`；plot-setting JSON 必须位于目录第一层。
2. 只运行 R `validate-plot`，不得创建 compute request 或调用 Java。
3. 展示验证结果与 R `run-plot` 原生命令，询问独立确认。
4. 确认后运行 R `run-plot` 并等待退出。
5. 读取并单独报告 `visualization_manifest.json`。

## 路径四：完整流程

1. 先完整执行路径一或路径二；任何 Java 失败都不得调用 R。
2. Java 阶段结束后单独询问现在绘图还是以后再绘图。
3. 选择以后再绘图时立即结束，允许后续新任务只执行路径三。
4. 选择现在绘图时，新建独立 visualization workspace，只复制各 completed sample 的 plot-setting JSON 到第一层，不复制、移动或覆盖 coverage CSV。
5. 对 workspace 完整执行路径三，并取得独立的 R 运行确认。
6. 分开报告每个 Java compute manifest 与 R visualization manifest。

## 原生命令模板

```bash
java -jar NGSViz-1.3.jar describe --format json
java -jar NGSViz-1.3.jar validate-compute --request /absolute/path/compute_request.sample_id.json
java -jar NGSViz-1.3.jar run-compute --request /absolute/path/compute_request.sample_id.json
Rscript lib/ngsVizPlotMain.R describe
Rscript lib/ngsVizPlotMain.R validate-plot --input-dir /absolute/path/visualization_workspace
Rscript lib/ngsVizPlotMain.R run-plot --input-dir /absolute/path/visualization_workspace
```
