# ngsViz Codex Agent Guide

本 guide 只编排 ngsViz 已有的 Java 计算 CLI 与 R 可视化 CLI，不提供 runner、统一执行器或新的运行时。

## 原生接口

Java 原生子命令：

```bash
java -jar NGSViz-1.3.jar describe --format json
java -jar NGSViz-1.3.jar validate-compute --request /absolute/path/compute_request.json
java -jar NGSViz-1.3.jar run-compute --request /absolute/path/compute_request.json
```

R 原生子命令：

```bash
Rscript lib/ngsVizPlotMain.R describe
Rscript lib/ngsVizPlotMain.R validate-plot --input-dir /absolute/path/visualization_workspace
Rscript lib/ngsVizPlotMain.R run-plot --input-dir /absolute/path/visualization_workspace
```

Java 每次只处理一个 signal sample，且不调用 R。R 只读取现有绘图输入，且不调用 Java。Codex 负责多样本顺序编排，不生成统一 manifest。

## 通用执行边界

- 对未知、缺失或含义不明确的科学参数先向用户提问，不猜测 genome、region、strand、fragment length、control 或 normalization。
- 默认只完成请求准备和 validate；运行前展示验证结果、原生命令、输入、输出和执行范围，并取得明确确认。
- `analysis_plan.json` 可以包含 `samples[]`；交给 Java 的 `compute_request.json` 禁止包含 `samples[]`。
- Java 进程严格串行。前一个 sample 的进程退出、manifest 验证和索引更新完成前，不启动下一个。
- 默认 `stop_on_error`；只有用户明确选择时才使用 `continue_on_error`。
- 每个 sample 使用唯一的 `sample_id`、`sample_name`、`group_name` 和 `output_dir`。
- 计算请求不自动绘图；绘图请求不重新计算。Java 失败后不得调用 R。
- 分开读取和报告 `compute_manifest.json` 与 `visualization_manifest.json`，不得合并为统一 manifest。
- `analysis_index.json` 只索引单样本 request 与 compute manifest，不汇总或改写科学结果。
- 不把 Codex 的判断、解释或推断写入原始结果文件。

## 路径一：单样本只计算

```text
Extract requirements
-> write compute_request.json
-> Java validate-compute
-> show validation and native command
-> ask confirmation
-> Java run-compute
-> read compute_manifest.json
```

验证失败时修正请求并重新验证，不请求运行确认。用户确认后只启动一个 Java 进程并等待退出；读取该 sample 的 `compute_manifest.json` 后结束，不创建可视化 workspace，不调用 R。

## 路径二：多样本顺序计算

```text
Extract shared parameters and samples[]
-> write analysis_plan.json
-> expand compute_request.<sample_id>.json
-> validate every request
-> show ordered queue
-> ask one batch confirmation
-> run one Java process and wait
-> verify its compute manifest
-> update analysis_index.json
-> only then start the next sample
```

共享的 genome、region、strand 和 fragment length 写入 `analysis`；sample 不得覆盖这些字段，不一致的 sample 必须拆分为不同 analysis plan。按 `samples[]` 数组顺序展开独立的单样本 Java request，并在运行任何 sample 前验证全部 request。全部通过后展示固定顺序、每项原生命令与失败策略，只询问一次批次确认。

每个 Java 进程结束后读取该 sample 的 `compute_manifest.json`，再更新 `analysis_index.json`。`stop_on_error` 遇到失败立即停止；明确选择的 `continue_on_error` 也必须完成当前进程退出、manifest 检查和索引更新后才继续。计算结束后不自动绘图。

## 路径三：只可视化

```text
receive result directory
-> R validate-plot
-> show validation and native command
-> ask confirmation
-> R run-plot
-> read visualization_manifest.json
```

结果目录必须是 R 的 `input_dir`，plot-setting JSON 位于目录第一层。只调用 R，不创建 compute request、不检查 BAM、不调用 Java、不补算 coverage。用户可以在后续新任务中仅调用 R。验证成功并确认后运行 R，最后只读取和报告 `visualization_manifest.json`。

## 路径四：完整流程

先完整完成路径一或路径二。全部已启动的 Java 进程结束且相应 compute manifest 已读取后，单独询问用户“现在绘图还是以后再绘图”。

- 选择以后再绘图：立即结束；后续新任务可直接走路径三。
- 选择现在绘图：新建独立 visualization workspace，只复制各个已完成 sample 的 `*_NGSViz_plotSetting.json` 到 workspace 第一层，不移动或覆盖计算结果及 coverage CSV；然后完整执行路径三，包括独立的 R 运行确认。

任何 Java 失败都不得进入 R。最终分别报告各 sample 的 `compute_manifest.json` 和 workspace 的 `visualization_manifest.json`。
