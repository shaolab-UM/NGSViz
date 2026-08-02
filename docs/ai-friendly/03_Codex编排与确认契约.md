# ngsViz Codex 编排与确认契约

## 适用范围

- 本契约定义 Codex Skill 如何编排 Java 计算 CLI 与 R 可视化 CLI，不新增项目运行时。
- Codex 只负责收集参数、生成请求、调用原生 CLI、等待进程、检查 response/manifest，并向用户请求确认。
- Java 与 R 始终是两个独立阶段。Java 完成不代表 R 完成，R 可在数小时或数天后独立调用。
- 不存在统一 Python runner、统一 runtime manifest、Java/R 联合进程或隐式后台任务。
- Java 计算结果以每个 sample 的 `compute_manifest.json` 为准；R 绘图结果以独立的 `visualization_manifest.json` 为准。两类 manifest 必须分开报告，不得合并或互相代替。

## 通用确认规则

1. 首次收到计算或绘图请求时，Codex 默认只完成参数收集、请求准备和原生 CLI 验证，不直接执行 `run-compute` 或 `run-plot`。
2. 任何未知、缺失或含义不明确的科学参数必须向用户提问，不得根据文件名、历史任务、常见实验习惯或默认偏好猜测。科学参数包括但不限于 `genome`、`region`、`database`、`analysis_type`、`biotype`、`flank_region`、`num_datapoints`、`mapping_quality`、`fragment_length`、`bin_method`、`strand_specific`、`scale_ratio`、control/input BAM 和 custom BED。
3. 可由原生契约确定的非科学默认值可以按契约解析，但 Codex 必须在确认前展示解析后的关键参数。
4. 验证通过后，Codex 必须展示待执行命令、输入请求或目录、输出位置以及执行范围，再请求用户明确确认。
5. 用户确认只授权当前展示的命令或有序批次。确认后若请求内容、路径、样本顺序、失败策略或命令发生变化，必须重新验证并重新确认。
6. 验证失败时不得请求用户确认运行。Codex 应报告原生 CLI 的错误，补齐或修正参数后重新验证。
7. 用户只要求计算时，不得自动创建可视化 workspace、调用 R 或生成图片。
8. 用户只要求绘图时，不得生成 `compute_request.json`、调用 Java、重新计算 coverage 或修改现有计算结果。
9. Codex 不得用旧 Java flags 代替 `compute_request.json`，也不得绕过 `validate-compute` 或 `validate-plot` 直接请求确认。

## Java 计算前置环境与参考数据库

1. 只可视化流程不检查 Java 环境。任何计算 request 或 `validate-compute` 前，先执行项目 skill `ngsviz-setup`，确保 `NGSViz_setting.json`、Java 17+、JAR、`tool_path` 和 `db_path` 有效。
2. 用户明确提供 genome 时，先查询配置 DB。目标版本缺失但存在于 README 预构建目录时，执行 `ngsviz-install-db`；README 无该版本时，请用户提供对应 UCSC GTF，并执行 `ngsviz-build-db`。
3. 用户未提供 genome 时，不得先给出特定物种的 genome 选项。未提供物种则先询问物种；已提供物种则查询配置 DB 中的可用 genome，一个时询问是否使用，多个时让用户选择。
4. 配置 DB 没有该物种、或用户拒绝唯一已安装版本时，查询 README 同物种预构建版本；README 无该物种时转入 UCSC GTF 构建流程。
5. 不得根据 BAM 文件名、细胞系、染色体命名或长度推断物种或 genome。必须由用户确认 BAM 比对使用的 assembly。
6. 下载的 DB 必须先验证 SQLite 完整性、`defaultTbl`、目标 genome 和非空坐标表；Java `-DB` 导入后必须复查目标主 DB，不能只依赖进程退出码。
7. 环境或参考数据库未就绪时，不得创建 compute request，也不得执行 `validate-compute`。

## 路径 A：单样本只计算

```text
User request
  -> Codex 询问缺失或未知参数
  -> 写一份单样本 compute_request.json
  -> Java validate-compute
  -> Codex 展示 run-compute 命令与解析参数
  -> 用户确认
  -> Java run-compute
  -> 一份 compute_manifest.json
  -> 结束，不运行 R
```

执行规则：

- 一份 `compute_request.json` 必须且只能描述一个 signal sample，可选一个对应 control BAM。
- Codex 先调用：

```bash
java -jar NGSViz-1.3.jar validate-compute --request /absolute/path/compute_request.json
```

- 验证成功后，Codex 展示但不立即执行：

```bash
java -jar NGSViz-1.3.jar run-compute --request /absolute/path/compute_request.json
```

- 用户确认后才执行 `run-compute`，并等待该 Java 进程退出。
- 成功时报告单样本 `compute_manifest.json` 的路径和状态；失败时报告 Java exit code、错误及已生成的失败 manifest 路径。不得把 plot-setting JSON 或图片当作计算 manifest。
- 本路径在 Java 任务结束后立即结束，不询问或启动绘图。

## 路径 B：多样本顺序计算

```text
User request
  -> Codex 询问缺失或未知参数
  -> 写 analysis_plan.json，包含 samples[]
  -> 按 samples[] 顺序展开为每个 sample 一份 compute_request.json
  -> 依次 validate-compute，验证全部请求
  -> Codex 展示有序队列与 failure_policy
  -> 用户一次确认整个批次
  -> 每次只启动一个 Java run-compute，并等待其退出
  -> 逐项记录状态
  -> 写 analysis_index.json，索引各 compute manifest
  -> 结束，不运行 R
```

### `analysis_plan.json`

`analysis_plan.json` 是 Codex Skill 的编排输入，只允许 Codex Skill 读取。Java、R、`MainCalculator` 和绘图函数均不得读取或解释该文件。

建议结构：

```json
{
  "schema_version": "1.0",
  "analysis_id": "K562_TSS_batch",
  "failure_policy": "stop_on_error",
  "samples": [
    {
      "order": 1,
      "compute_request": {
        "schema_version": "1.0",
        "sample_id": "H3K4me3",
        "sample_name": "K562_H3K4me3",
        "group_name": "K562",
        "title": "K562_H3K4me3_TSS",
        "genome": "hg38",
        "region": "tss",
        "signal_bam": "/data/K562_H3K4me3.bam",
        "control_bam": null,
        "output_dir": "/results/K562_TSS_batch/H3K4me3"
      }
    },
    {
      "order": 2,
      "compute_request": {
        "schema_version": "1.0",
        "sample_id": "H3K27ac",
        "sample_name": "K562_H3K27ac",
        "group_name": "K562",
        "title": "K562_H3K27ac_TSS",
        "genome": "hg38",
        "region": "tss",
        "signal_bam": "/data/K562_H3K27ac.bam",
        "control_bam": null,
        "output_dir": "/results/K562_TSS_batch/H3K27ac"
      }
    }
  ]
}
```

约束：

- `samples` 必须是非空数组；数组顺序是唯一执行顺序，`order` 必须与数组顺序一致且从 `1` 连续递增。
- 每个 `compute_request` 必须独立满足 Java 单样本请求契约。Codex 将其写成独立 `compute_request.json` 后再交给 Java。
- Codex 不得把顶层 `samples` 传给 Java，不得构造一个包含多个 sample 的 Java request。
- 不定义跨 sample 的科学参数覆盖、继承或合并规则。每个 sample 的科学参数必须在其 `compute_request` 中完整解析；不明确时向用户提问。
- 默认 `failure_policy` 为 `stop_on_error`。只允许 `stop_on_error` 或 `continue_on_error`。

### 全量验证与批次确认

- Codex 必须按队列顺序对全部独立请求调用 `validate-compute`。
- 全量验证阶段不得启动任何 `run-compute`。任一请求验证失败时，批次不能进入运行确认；应先修正并重新验证受影响请求，参数或顺序变化时重新展示队列。
- 所有请求验证通过后，Codex 必须展示：
  - 固定执行顺序；
  - 每项的 `sample_id`、signal/control BAM、关键科学参数、输出目录和 `run-compute` 命令；
  - `failure_policy`；
  - 明确说明任意时刻最多只有一个 Java 进程。
- 用户可一次确认整个已展示批次，不要求每个 sample 重复确认。确认不覆盖后续绘图。

### 串行执行与失败策略

- 多样本 Java 任务必须串行执行，禁止并行、后台并发、进程池、同时启动多个 Java 进程或在前一进程退出前启动下一进程。
- 每个 sample 必须经历“启动一个 Java 进程 -> 等待退出 -> 读取 response/manifest -> 记录状态 -> 再处理下一项”。
- `stop_on_error`：任一 `run-compute` 返回非零 exit code、失败 response 或失败/缺失 manifest 时，立即停止队列；其余项目记录为 `not_started`。
- `continue_on_error`：当前项目失败后可以继续下一项，但仍须等待当前 Java 进程完全退出并记录状态；任何时刻仍只允许一个 Java 进程。
- 用户中途取消时，不启动新的 Java 进程；当前进程的处理遵循用户明确指令，未启动项目记录为 `cancelled`。

### `analysis_index.json`

`analysis_index.json` 只是 sample/request/manifest 索引，不是计算 manifest，不证明 coverage 计算成功，也不得包含图片或 R 状态。

建议结构：

```json
{
  "schema_version": "1.0",
  "analysis_id": "K562_TSS_batch",
  "failure_policy": "stop_on_error",
  "samples": [
    {
      "order": 1,
      "sample_id": "H3K4me3",
      "request": "/results/K562_TSS_batch/requests/01_H3K4me3/compute_request.json",
      "status": "completed",
      "exit_code": 0,
      "compute_manifest": "/results/K562_TSS_batch/H3K4me3/compute_manifest.json"
    },
    {
      "order": 2,
      "sample_id": "H3K27ac",
      "request": "/results/K562_TSS_batch/requests/02_H3K27ac/compute_request.json",
      "status": "not_started",
      "exit_code": null,
      "compute_manifest": null
    }
  ]
}
```

- `status` 只允许 `completed`、`failed`、`not_started` 或 `cancelled`。
- `compute_manifest` 仅在原生 Java manifest 实际存在时记录其绝对路径，否则为 `null`。
- Codex 在每个 Java 进程结束后更新对应索引状态，最终以原子替换方式写入 `analysis_index.json`。
- 判断单个 sample 的计算结果时必须读取其 `compute_manifest.json`；不得只依据 `analysis_index.json` 的顶层存在性或索引状态。

## 路径 C：只可视化

```text
User supplies result directory
  -> Codex 确认该目录是 R input_dir
  -> R validate-plot
  -> Codex 展示 run-plot 命令
  -> 用户确认
  -> R run-plot
  -> 一份 visualization_manifest.json
  -> 结束，不运行 Java
```

执行规则：

- 用户提供的结果目录必须符合 R CLI 的 `input_dir` 契约：plot-setting JSON 位于目录第一层，且其引用的 coverage CSV 可访问。Codex 不递归搜索子目录，也不根据 `analysis_index.json` 或 `compute_manifest.json` 让 R 推断输入。
- 只可视化路径不得生成 `compute_request.json`、调用 `validate-compute` 或 `run-compute`，也不得重新计算任何 sample。
- Codex 先调用：

```bash
Rscript lib/ngsVizPlotMain.R validate-plot --input-dir /absolute/path/visualization_workspace
```

- 验证成功后，Codex 展示但不立即执行：

```bash
Rscript lib/ngsVizPlotMain.R run-plot --input-dir /absolute/path/visualization_workspace
```

- 用户确认后才执行 `run-plot`，并等待 R 进程退出。
- 本路径不得检查 BAM/BAI、重新生成 coverage CSV、调用 Java 或修改计算参数。
- R 可以在 Java 计算完成数小时或数天后独立调用；只要 plot-setting JSON 及其引用文件仍可访问，即不依赖先前 Java 进程或 Codex 会话。
- 成功或失败均只报告本次 `visualization_manifest.json`；如用户同时提供计算 manifest，可将其列为输入来源，但不得合并两类 manifest 的状态。

## 路径 D：完整流程

完整流程由计算阶段和一次独立的绘图选择组成：

```text
执行完整路径 A 或路径 B
  -> 等待全部已启动的 Java 任务结束
  -> Codex 单独询问“现在绘图还是以后再绘图”
     -> 以后再绘图：立即结束；后续可单独执行路径 C
     -> 现在绘图：创建独立 visualization workspace
                  -> 执行完整路径 C
```

规则：

- 计算阶段必须完整遵循路径 A 或 B 的验证和确认规则。完整流程请求不等于预先授权 Java 运行，仍须在验证后确认。
- Codex 只有在所有已启动 Java 任务均已结束后，才能询问现在绘图还是以后再绘图。Java 仍在运行时不得提前启动 R。
- “现在绘图还是以后再绘图”是计算与绘图之间的独立选择，不得从用户确认 Java 批次推断。
- 用户选择“以后再绘图”时立即结束，不创建 visualization workspace、不运行 R。后续用户可以只提供结果目录执行路径 C，不重新计算。
- 用户选择“现在绘图”时，Codex 创建独立 visualization workspace，并将准备绘制的 `*_NGSViz_plotSetting.json` 复制到该目录第一层。原始 plot-setting JSON、计算目录及其 CSV 不得被覆盖或移动。
- visualization workspace 只收集状态为 `completed` 的 `compute_manifest.json` 所引用的 plot-setting JSON；若存在失败、缺失或用户不希望绘制的 sample，Codex 必须明确列出实际绘图集合。
- 创建 workspace 后仍须完整执行路径 C：先 `validate-plot`，展示 `run-plot` 命令，再取得一次独立的 R 运行确认。
- Java `compute_manifest.json` 与 R `visualization_manifest.json` 分开报告：

```text
Java 计算：
  sample 1 -> compute_manifest.json
  sample 2 -> compute_manifest.json

R 可视化：
  visualization workspace -> visualization_manifest.json
```

- Java manifest 的 `completed` 只表示计算及计算产物验收完成；只有 R manifest 的 `completed` 才表示本次图片生成及验收完成。

## 用户意图与禁止的隐式扩展

| 用户意图 | 允许执行 | 禁止执行 |
|---|---|---|
| 单样本只计算 | 路径 A | 自动绘图、批量扩展 |
| 多样本只计算 | 路径 B | 并行 Java、自动绘图 |
| 只可视化 | 路径 C | Java 计算、补算 coverage |
| 完整流程 | 路径 A 或 B，之后单独选择是否执行 C | 将 Java 确认视为 R 确认 |

- 用户说“先算”“只计算”“不要画图”时，Java 完成后直接结束。
- 用户说“只画图”“基于现有结果绘图”时，只执行 R 验证与绘图确认。
- 用户没有明确要求完整流程时，不得因为计算成功而主动创建 visualization workspace。
- 用户没有明确提供科学参数或其来源时，必须提问；不得通过扩大任务范围来回避参数确认。

## 产物职责

| 文件 | 创建者/维护者 | 用途 | 不是 |
|---|---|---|---|
| `compute_request.json` | Codex | 单样本 Java 请求 | 多样本计划 |
| `analysis_plan.json` | Codex | Codex 多样本编排输入 | Java/R 请求 |
| `compute_manifest.json` | Java | 单样本计算事实记录 | R 绘图状态 |
| `analysis_index.json` | Codex | sample/request/manifest 索引 | 计算 manifest |
| `visualization_manifest.json` | R | 一次 R 绘图事实记录 | Java 计算状态 |

Codex 可以汇报这些文件的路径与各自状态，但不得生成一个替代它们的统一 runtime manifest，也不得引入 Python runner 统一执行 Java 和 R。
