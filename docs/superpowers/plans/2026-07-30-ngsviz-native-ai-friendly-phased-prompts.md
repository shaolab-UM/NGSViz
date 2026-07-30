# ngsViz Native AI-Friendly Progressive Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 不增加 Python Agent Adapter，通过增强 Java 计算 CLI、R 可视化 CLI 和 Codex Skill，让用户能够以自然语言安全、渐进地使用 ngsViz。

**Architecture:** Java 计算模块与 R 可视化模块保持两个独立程序，各自提供 `describe`、`validate`、`run` 和机器可读 manifest。AI 参数只通过单样本 `compute_request.json` 传入 Java；旧 CLI flags 与 JSON 请求是互斥入口，二者在 Java 内部归一化为同一个 `ComputeRequest`，经过同一验证边界后调用同一个 `MainCalculator`。Java 每个进程只处理一个 sample；Codex Skill 可读取包含 `samples[]` 的上层 analysis plan，将其展开为多个单样本请求并顺序调用 Java。R 可在计算完成后的任意时间独立调用，不由 Java 或批处理流程自动启动。

**Tech Stack:** Java 9+、Maven、htsjdk、org.json、SQLite JDBC、R 4.x、jsonlite、现有 ngsViz R 绘图库、JUnit 5、R 自包含测试脚本。

## Global Constraints

- 不创建 Python 包、Python 脚本、Python测试或 Python Agent Adapter。
- 不实现 MCP、HTTP API、聊天网页或云端服务。
- Java 只负责计算环境、数据库、BAM/BED、coverage 计算和计算结果 manifest。
- R 只负责可视化环境、绘图输入、图形生成和可视化 manifest。
- Java 不得调用 `Rscript`；R 不得调用 `java -jar`，Rshiny GUI 除外。
- Java `run-compute` 每次严格处理一个 signal sample，可选一个对应 control BAM。
- 多个 sample 只能由 Codex Skill 顺序启动多个独立 Java 进程，不得在 Java 计算核心内增加批量循环。
- Java 计算完成后不得自动启动 R；R 可在同一任务或后续独立任务中调用。
- 保留现有 `java -jar NGSViz-1.2.jar -G ...` 和 `Rscript lib/ngsVizPlotMain.R <JSON_path>` 用法。
- AI 不得拼装包含全部计算参数的旧 flags 命令；AI 只能生成单样本 `compute_request.json`，并通过 `validate-compute --request <path>` 或 `run-compute --request <path>` 传入 Java。
- `--request` 模式不得混用 `-G`、`-R`、`-I`、`-O` 等旧计算 flags，也不得用 CLI 参数覆盖 JSON 字段。
- 不使用 `-J <yaml>`、YAML 配置或 YAML loader；现有 `-J` 占位参数不属于新 AI-friendly 接口。
- 旧计算 flags 和 JSON 请求必须复用同一个 `ComputeRequest`、validator 与 `MainCalculator`，不得形成两套验证或计算逻辑。
- 不修改 coverage、坐标、TSS/TES、strand、binning、normalization 等科学行为。
- 用户未明确提供的 genome、region、strand、fragment length、control、normalization 不得由 AI 猜测。
- 每个 Phase 只完成当前模块，先写失败测试，验证通过后独立提交。
- 不覆盖原始 BAM、BAI、SQLite 数据库或 `Samples/Result/`。
- 项目文档、报告和日志使用中文；代码注释、docstring、标识符和命令行输出使用英文。

---

## 一、最终职责边界

```text
用户需求
  ↓
ngsViz Codex Skill / Agent Guide
  ├─ 只计算
  │    ↓
  │  analysis_plan.json（可含 samples[]）
  │    ↓
  │  Codex 展开单样本 compute_request.<sample_id>.json
  │    ↓
  │  Java describe / validate-compute
  │    ↓
  │  用户确认批次
  │    ↓
  │  按 samples[] 顺序逐个 Java run-compute
  │    ↓
  │  每个样本独立 compute_manifest.json
  │
  ├─ 只可视化已有结果
  │    ↓
  │  单样本结果目录
  │  或独立 visualization workspace
  │    ↓
  │  R describe / validate-plot
  │    ↓
  │  用户确认
  │    ↓
  │  R run-plot
  │    ↓
  │  visualization_manifest.json
  │
  └─ 完整流程
       多个 Java 单样本流程顺序完成
         ↓
       Codex 分别读取各 compute manifest
         ↓
       询问现在绘图还是以后再绘图
         ↓
       R 原生流程完成
         ↓
       Codex 分别读取各 compute manifest 与 visualization manifest
```

### Java 输入归一化

```text
旧 CLI flags ───────────┐
                       ├─> ComputeRequest ─> validate ─> MainCalculator
compute_request.json ──┘
```

- 旧 CLI flags 是面向既有用户和脚本的兼容入口。
- `compute_request.json` 是 AI 向 Java 传递计算参数的唯一入口。
- 两种输入模式互斥；检测到混用时返回参数错误，不定义覆盖优先级。
- 两种入口只负责构造同一个 `ComputeRequest`；后续验证、默认值解析和计算调用必须共享。
- `-J` 当前只保存路径且不加载配置，不属于上述 JSON 入口，也不得扩展为 YAML 配置入口。
- Codex 启动 Java 时传递参数数组，不拼接 shell 字符串。AI-friendly 调用数组只包含 Java、JAR、子命令、`--request` 和 JSON 绝对路径。

### Java 计算模块的唯一职责

- 将旧 CLI flags 或 `compute_request.json` 归一化为同一个 `ComputeRequest`。
- 检查 Java、JAR、系统配置、SQLite、BAM/BAI、BED 和计算参数。
- 调用现有 Java coverage 计算逻辑。
- 生成 coverage CSV、read-count CSV、plot-setting JSON。
- 生成 `compute_manifest.json`。
- 一个 Java 进程只对一个 sample 生成一份 manifest。

### R 可视化模块的唯一职责

- 检查 R 版本和绘图包。
- 验证一个或多个已有 plot-setting JSON 与 coverage CSV。
- 调用现有 R 绘图函数。
- 检查图片产物。
- 生成 `visualization_manifest.json`。

### Codex Skill 的唯一职责

- 从用户需求提取参数。
- 读取可包含多个 sample 的 `analysis_plan.json`。
- 把每个 sample 展开为独立 Java 请求，并严格顺序执行。
- 对缺失科学参数提问。
- 生成 Java 计算请求 JSON。
- 以参数数组调用 Java；不把 JSON 字段重新展开成旧 flags。
- 展示只包含 `--request` 路径的原生命令和验证结果。
- 得到明确授权后分别调用 Java 或 R。
- 基于 manifest 做有限解读。
- 记录 `analysis_index.json`，只索引各 sample 的请求和 manifest，不替代单样本 manifest。
- 用户以后单独要求可视化时，根据已有 manifest 重新建立可视化工作目录。

### 两层计算配置

`analysis_plan.json` 是 Codex Skill 的编排配置，不由 Java 或 R 直接读取：

```json
{
  "schema_version": "1.0",
  "analysis": {
    "title": "K562_histone_TSS",
    "genome": "hg19",
    "region": "tss",
    "database": "RefSeq",
    "output_root": "agent_output/K562_histone_TSS"
  },
  "samples": [
    {
      "sample_id": "H3K4me3",
      "sample_name": "K562_H3K4me3",
      "group_name": "K562",
      "signal_bam": "Samples/K562_H3K4me3-ENCFF752MYF_5p.bam",
      "control_bam": null,
      "scale_ratio": null
    },
    {
      "sample_id": "H3K36me3",
      "sample_name": "K562_H3K36me3",
      "group_name": "K562",
      "signal_bam": "Samples/K562_H3K36me3-ENCFF975JFV_5P.bam",
      "control_bam": null,
      "scale_ratio": null
    }
  ],
  "execution": {
    "mode": "sequential",
    "failure_policy": "stop_on_error",
    "visualization": "deferred"
  }
}
```

Codex 为每个元素生成独立 `compute_request.<sample_id>.json`。Java 请求中只能出现一个 `signal_bam` 和零或一个 `control_bam`；每个 sample 使用独立输出目录：

```text
<output_root>/
├── H3K4me3/
│   ├── compute_request.H3K4me3.json
│   └── compute_manifest.json
├── H3K36me3/
│   ├── compute_request.H3K36me3.json
│   └── compute_manifest.json
└── analysis_index.json
```

默认 `failure_policy=stop_on_error`。可显式选择 `continue_on_error`，但 Codex 仍须等待当前 Java 进程退出并记录 manifest 后才能启动下一个，禁止并行启动。

### 后置可视化

计算阶段结束不自动创建图片。用户可立即绘图，也可在之后的新 Codex 任务中仅提供单样本输出目录或 `analysis_index.json`。

多样本可视化时，Codex 新建独立目录：

```text
<output_root>/visualization_workspace/
├── H3K4me3_NGSViz_plotSetting.json
└── H3K36me3_NGSViz_plotSetting.json
```

只复制 plot-setting JSON，不移动、不覆盖 coverage CSV；JSON 内的 `OutputFile` 路径继续指向各自单样本结果。R 只读取这个工作目录，不读取 `analysis_plan.json`、`analysis_index.json` 或 BAM。

### 模块独立性验收

```bash
rg -n "Rscript|ngsVizPlotMain" src/main/java
# Expected: no matches

rg -n "java -jar|NGSViz-1.2.jar|system2\\(.*java|system\\(.*java" lib
# Expected: 现有 Shiny 历史封装需单独列出；新增 native CLI 文件不得出现
```

现有 Shiny 计算按钮属于历史 GUI 集成，不在本计划中扩展；新 AI 友好接口不得依赖它。

---

## 二、原生接口契约

### Java 计算 CLI

```bash
java -jar NGSViz-1.2.jar describe --format json
java -jar NGSViz-1.2.jar validate-compute --request compute_request.json
java -jar NGSViz-1.2.jar run-compute --request compute_request.json
```

AI-friendly 模式只允许上述 `--request` 路径参数。以下混用无效：

```bash
java -jar NGSViz-1.2.jar run-compute \
  --request compute_request.json \
  -G hg38
```

兼容旧入口：

```bash
java -jar NGSViz-1.2.jar -G hg19 -R tss -T demo -I sample.bam -O output
```

旧入口和 JSON 入口最终必须执行相同关系：

```text
input -> ComputeRequest -> validate -> MainCalculator
```

`-J <yaml>` 不是 AI-friendly 配置入口，不得在新接口、示例或 Codex Skill 中使用。

### R 可视化 CLI

```bash
Rscript lib/ngsVizPlotMain.R describe --format json
Rscript lib/ngsVizPlotMain.R validate-plot --input output_dir --format json
Rscript lib/ngsVizPlotMain.R run-plot --input output_dir
```

兼容旧入口：

```bash
Rscript lib/ngsVizPlotMain.R output_dir
```

### 进程退出码

| Exit code | 含义 |
|---:|---|
| 0 | 成功 |
| 2 | 参数或请求格式错误 |
| 3 | 环境或依赖缺失 |
| 4 | 数据库、BAM、BED 或绘图输入无效 |
| 5 | 计算或绘图执行失败 |
| 6 | 产物验收失败 |

---

## 三、所有 Phase 共用的 Codex Prompt

```text
你正在执行 ngsViz 原生 AI 友好升级。

开始前必须：
1. 阅读 AGENTS.md。
2. 阅读 docs/superpowers/plans/2026-07-30-ngsviz-native-ai-friendly-phased-prompts.md。
3. 阅读当前 Phase 指定的现有 Java/R 文件。
4. 检查 git status，保护用户已有修改。

架构铁律：
- 不创建或使用 Python Agent Adapter。
- Java 计算模块和 R 可视化模块保持独立。
- Java 不调用 R，R 不调用 Java。
- 不改变科学计算和绘图行为。
- 保持现有 CLI 向后兼容。
- 脚本中的注释和命令必须使用英文

执行规则：
- 只实现当前 Phase，不提前实现后续 Phase。
- 严格 TDD：先写失败测试并运行确认，再写最小实现。
- 遇到参数默认值、科学含义或现有文档冲突时停止并向我提问。
- 完成后运行当前模块测试和相关回归测试。
- 更新 log/，创建一个独立 Git commit，然后停止。
- 最终报告修改文件、失败测试证据、通过测试证据、接口兼容性和遗留问题。
```

---

## Phase 0 — 架构决定与原始基线冻结

### 目标

记录“不使用 Python Adapter、Java/R 独立”的正式决定，并冻结升级前的 Java、R 和示例结果基线。

### Prompt

```text
请执行 Phase 0，只写架构文档和基线报告，不修改 Java/R 代码。

创建：
- docs/ai-friendly/00_原生架构与协作边界.md
- audit/ai_friendly_native_phase0_baseline.md
- 本 Phase 日志

文档必须明确：
1. Java 是计算模块，R 是可视化模块。
2. 二者是两个独立 CLI，不互相启动。
3. Codex Skill 是外部编排规则，不是项目运行时。
4. 不使用 Python、MCP、Web API。
5. 现有 plot-setting JSON 是 Java 输出、R 输入的唯一文件契约。
6. 旧 CLI 必须向后兼容。
7. 科学行为不在本次升级范围。
8. Java 每次只计算一个 sample。
9. AI 多样本模式由 Codex 顺序启动多个 Java 进程。
10. R 可在计算后的任意时间独立调用。
11. AI 参数只通过单样本 compute_request.json 传入 Java。
12. 旧 CLI flags 与 JSON 请求都归一化为同一个 ComputeRequest。
13. 两种入口互斥，共用 validator 和 MainCalculator。
14. 不使用 -J、YAML 或完整旧 flags 命令传递 AI 参数。

只读记录基线：
- git commit
- java -version
- Rscript --version
- mvn test
- SQLite PRAGMA integrity_check
- 数据库支持的 genome/database/region
- 示例 BAM/BAI
- Samples/Result 中现有 CSV/JSON/图片及其校验值

验证：
git diff --check
mvn test
sqlite3 -readonly database/genomeCoordinate.db "PRAGMA integrity_check;"
rg -n "不使用 Python|Java.*计算|R.*可视化|互相启动" docs/ai-friendly/00_原生架构与协作边界.md

提交：
docs(agent): freeze native AI-friendly architecture
```

### 人工确认点

- [ ] Java/R 独立边界是否准确
- [ ] 计算请求默认采用 JSON 是否接受
- [ ] 旧 Shiny 集成保留但不扩展是否接受

---

## Phase 1 — 三份原生接口真源文档

### 目标

在修改代码前分别固定 Java、R 和 Codex 的接口契约。

### Phase 1A Prompt — Java 计算契约

```text
请执行 Phase 1A，只创建 docs/ai-friendly/01_Java计算CLI契约.md，不写代码。

必须阅读：
- src/main/java/com/NGSViz/NGSViz.java
- src/main/java/com/NGSViz/configSet/GetInputParameterValue.java
- src/main/java/com/NGSViz/configSet/InputParameterAttributes.java
- src/main/java/com/NGSViz/configSet/CheckLegal2InputParameter.java
- src/main/java/com/NGSViz/coverageCalculator/MainCalculator.java
- README.md CLI 与计算结果章节

文档定义：
1. describe、validate-compute、run-compute 三个子命令。
2. 旧短参数入口的兼容规则，以及旧 flags 到 `ComputeRequest` 的逐字段映射。
3. compute_request.json 的字段、类型、必填性和默认值来源。
4. 每份 compute request 必须且只能描述一个 sample。
5. 参数至少包括 sample_id、sample_name、group_name、title、genome、region、
   signal_bam、control_bam、output_dir、
   system_config、database、analysis_type、biotype、flank_region、flank_factor、
   num_datapoints、scale_ratio、mapping_quality、fragment_length、cores、
   batch_size、bin_method、strand_specific、center_mode、custom_bed。
6. sample_id、sample_name、group_name 是输出元数据，不改变 coverage 算法。
7. request 中不得出现 samples 数组；出现时 validation 必须失败。
8. JSON 字段不转换成旧 flags；null 由 `ComputeRequest` 默认值规则处理。
9. num_datapoints 最低为 100。
10. validate-compute 只读且不创建输出目录。
11. run-compute 只运行一个 sample 的 Java 计算，不启动 R。
12. compute_manifest.json schema。
13. 退出码和 JSON response envelope。
14. 旧 flags 模式与 `--request` 模式互斥，混用返回 exit code 2。
15. AI 只使用 `--request <json>`，不使用 `-J`、YAML 或完整 flags 命令。
16. 两种入口共享同一个 validator 和 MainCalculator，不复制科学计算逻辑。

遇到 README、Java 默认值和实际行为冲突时列出证据与行号，停止并等待用户裁决。

提交：
docs(java): define native computation CLI contract
```

### Phase 1B Prompt — R 可视化契约

```text
请执行 Phase 1B，只创建 docs/ai-friendly/02_R可视化CLI契约.md，不写代码。

必须阅读：
- lib/ngsVizPlotMain.R
- lib/processJSON.R
- lib/coverageHeatmap.R
- lib/qualityControlViz.R
- README.md 可视化章节
- Samples/Result 下现有 plot-setting JSON

文档定义：
1. describe、validate-plot、run-plot 三个子命令。
2. 旧单目录参数入口的兼容规则。
3. R 只接收已有 JSON 目录，不运行 Java。
4. 一个输入目录可包含一个或多个 plot-setting JSON。
5. 单样本和多样本可视化使用同一个 R CLI。
6. validate-plot 检查 R 包、JSON schema、OutputFile、coverage CSV。
7. run-plot 只调用现有绘图逻辑。
8. visualization_manifest.json schema。
9. coverage 数值列数等于
   2 × flanking_region_datapoints + middle_datapoints。
10. 图片缺失、空文件、NaN/Inf 和全零矩阵的状态规则。
11. R 模块退出码和 JSON response envelope。

不得把 Java 环境、SQLite、BAM 或计算参数检查放入 R 契约。

提交：
docs(r): define native visualization CLI contract
```

### Phase 1C Prompt — Codex 编排契约

```text
请执行 Phase 1C，只创建 docs/ai-friendly/03_Codex编排与确认契约.md，不写代码。

定义四条独立用户路径：

A. 单样本只计算
User request -> ask missing parameters -> one compute request JSON ->
Java validate -> show command -> user confirm -> Java run -> one compute manifest

B. 多样本顺序计算
User request -> analysis_plan.json with samples[] ->
expand one compute request per sample -> validate all requests ->
show ordered queue -> user confirms batch -> run Java one process at a time ->
write analysis_index.json referencing each compute manifest

C. 只可视化
User supplies result directory -> R validate -> show command ->
user confirm -> R run -> visualization manifest

D. 完整流程
完整执行 A 或 B；全部 Java 任务结束后，单独询问现在绘图还是以后再绘图。
用户选择现在绘图时创建独立 visualization workspace 并执行 C；
用户选择以后绘图时立即结束，后续可只执行 C。

必须规定：
- 首次请求默认只 validate，不直接 run。
- Java request 一次只能包含一个 sample。
- analysis_plan.json 可包含 samples[]，但只由 Codex Skill 读取。
- 多样本必须串行，禁止并行 Java 进程。
- 默认 failure_policy=stop_on_error。
- continue_on_error 时仍须逐个等待、记录状态。
- 用户只要求计算时不得自动绘图。
- 用户只要求绘图时不得重新计算。
- Java 完成不代表 R 完成。
- R 可以在数小时或数天后独立调用。
- 两个 manifest 分开报告。
- 不存在统一 Python runner 或统一 runtime manifest。
- analysis_index.json 只是 sample/request/manifest 索引，不是计算 manifest。
- 未知科学参数必须向用户提问。

提交：
docs(agent): define native Codex orchestration contract
```

### Phase 1 人工确认

- [ ] compute_request.json 字段与 Java flag 一致
- [ ] 旧 flags 与 JSON 请求均归一化为同一个 ComputeRequest
- [ ] 两种输入模式互斥且不存在参数覆盖优先级
- [ ] analysis_plan.json 与单样本 compute request 边界明确
- [ ] R 输入契约与现有 plot-setting JSON 一致
- [ ] 四条 Codex 用户路径符合预期
- [ ] 所有默认值冲突已由你裁决

---

## Phase 2 — Java CLI 分发与机器响应基础

### 目标

增加原生 Java 子命令分发、统一退出码和 JSON response，但本阶段不做真实验证或计算。

### Files

- Create: `src/main/java/com/NGSViz/cli/CliCommand.java`
- Create: `src/main/java/com/NGSViz/cli/CliExitCode.java`
- Create: `src/main/java/com/NGSViz/cli/CliResponse.java`
- Create: `src/main/java/com/NGSViz/cli/CliDispatcher.java`
- Create: `src/main/java/com/NGSViz/cli/DescribeCommand.java`
- Modify: `src/main/java/com/NGSViz/NGSViz.java`
- Test: `src/test/java/com/NGSViz/cli/CliDispatcherTest.java`
- Test: `src/test/java/com/NGSViz/cli/CliResponseTest.java`

### Interfaces

- `CliDispatcher.dispatch(String[] args): int`
- `CliResponse.toJson(): JSONObject`
- `DescribeCommand.execute(String[] args): CliResponse`

### Prompt

```text
请实现 Phase 2：Java 原生 CLI 分发基础。

行为：
- args[0] 为 describe、validate-compute、run-compute 时进入新 dispatcher。
- args[0] 以 "-" 开头时完整保留旧 ProcessInputParameters 流程。
- 新子命令只接受 `--request <json>`；不得接受 `-J` 或旧计算 flags。
- `--request` 与任何旧计算 flag 混用时返回 exit code 2。
- describe --format json 返回 version、commands、legacy_flags、exit_codes。
- validate-compute 和 run-compute 暂时返回 unsupported 状态与 exit code 2，
  不提前实现 Phase 3/4。
- CliDispatcher 返回 int，便于 JUnit 测试。
- 只有 NGSViz.main 最外层决定进程退出，不在 service 中调用 System.exit。
- JSON response 至少包含 schema_version、command、status、exit_code、
  message、data、warnings、errors。

先写失败测试，覆盖：
- describe JSON
- 未知子命令
- 缺少 --format 值
- legacy 参数识别
- `--request` 与旧 flags 混用失败
- response 必需字段

验证：
mvn -Dtest=CliDispatcherTest,CliResponseTest test
mvn test
mvn -DskipTests package
java -jar target/NGSViz-1.0.jar describe --format json

提交：
feat(cli): add native Java command dispatcher
```

### Phase 2 验收

- [ ] 旧 CLI 仍可进入原流程
- [ ] describe 输出合法 JSON
- [ ] 新 service 未调用 System.exit
- [ ] 未涉及 R 文件

---

## Phase 3 — Java validate-compute

### 目标

以 Java/htsjdk/SQLite 原生能力完成计算前只读检查，不创建输出、不运行 coverage。

### Files

- Create: `src/main/java/com/NGSViz/cli/ComputeRequest.java`
- Create: `src/main/java/com/NGSViz/cli/ComputeRequestReader.java`
- Create: `src/main/java/com/NGSViz/cli/ComputeValidator.java`
- Create: `src/main/java/com/NGSViz/cli/DatabaseInspector.java`
- Create: `src/main/java/com/NGSViz/cli/BamInspector.java`
- Create: `src/main/java/com/NGSViz/cli/BedInspector.java`
- Create: `src/main/java/com/NGSViz/cli/ValidateComputeCommand.java`
- Create: `docs/ai-friendly/schemas/compute_request.schema.json`
- Create: `docs/ai-friendly/examples/compute_request.single.json`
- Create: `docs/ai-friendly/examples/compute_request.signal_control.json`
- Test: `src/test/java/com/NGSViz/cli/ComputeRequestReaderTest.java`
- Test: `src/test/java/com/NGSViz/cli/ComputeValidatorTest.java`
- Test: `src/test/java/com/NGSViz/cli/DatabaseInspectorTest.java`
- Test: `src/test/java/com/NGSViz/cli/BamInspectorTest.java`
- Test: `src/test/java/com/NGSViz/cli/BedInspectorTest.java`

### Interfaces

- `ComputeRequestReader.read(Path path): ComputeRequest`
- `ComputeValidator.validate(ComputeRequest request): CliResponse`
- `DatabaseInspector.inspect(Path dbPath, ComputeRequest request): JSONObject`
- `BamInspector.inspect(Path bamPath): JSONObject`
- `BedInspector.inspect(Path bedPath): JSONObject`

### Prompt

```text
请实现 Phase 3：Java validate-compute。

ComputeRequest 字段必须严格来自
docs/ai-friendly/01_Java计算CLI契约.md，不允许未知字段。
每个 request 必须且只能包含一个 signal_bam；允许零或一个 control_bam；
出现 samples 数组必须返回 exit code 2。

JSON 输入规则：
- `ComputeRequestReader` 直接从 JSON 构造 `ComputeRequest`，不得先转换为旧 flags。
- 新子命令只接受 `--request <json>`，不得读取 `-J` 或 YAML。
- JSON 字段与旧 flags 混用时，在进入 reader 前返回 exit code 2。

检查：
1. system config 和 JAR 信息。
2. SQLite 以只读连接打开，PRAGMA integrity_check。
3. genome + database + region 组合存在。
4. CoordinateTblName 对应表存在。
5. signal/control BAM 存在、可打开、有 header、有 BAI。
6. signal/control reference 命名风格一致。
7. BAM reference 与数据库染色体有合理交集。
8. BED 至少 3 列，start >= 0，end > start。
9. 参数类型、范围和组合合法。
10. 输出路径不覆盖已有 ngsViz 产物。

validate-compute 必须：
- 只读。
- 不创建 output_dir。
- 不调用 MainCalculator。
- 不调用 R。
- 输出请求文件、解析后的参数、支持值、errors 和 warnings；不生成供执行使用的完整旧 flags 命令。

先写失败测试，覆盖：
- 合法示例
- `--request` 与旧 flags 混用失败
- sample_id、sample_name、group_name 元数据
- request 出现 samples 数组
- 缺失 genome
- num_datapoints < 100
- 非法数据库组合
- BAM 缺少 index
- signal/control 命名不一致
- BED 非法坐标
- 输出目录已有结果

验证：
mvn -Dtest=ComputeRequestReaderTest,ComputeValidatorTest,DatabaseInspectorTest,BamInspectorTest,BedInspectorTest test
mvn test
mvn -DskipTests package
java -jar target/NGSViz-1.0.jar validate-compute \
  --request docs/ai-friendly/examples/compute_request.single.json

提交：
feat(cli): add native computation validation
```

### 人工确认点

- [ ] BAM 与数据库染色体交集阈值
- [ ] 疑似 1-based BED 是 warning 还是 failure
- [ ] 发现旧结果时是否始终阻止覆盖

---

## Phase 4 — Java run-compute 与计算 manifest

### 目标

让新 Java 请求入口调用现有计算流程，并独立生成计算 manifest；不得涉及 R。

### Files

- Create: `src/main/java/com/NGSViz/cli/RunComputeCommand.java`
- Create: `src/main/java/com/NGSViz/cli/LegacyComputeRequestAdapter.java`
- Create: `src/main/java/com/NGSViz/cli/ComputeManifest.java`
- Create: `src/main/java/com/NGSViz/cli/ComputeManifestWriter.java`
- Modify: `src/main/java/com/NGSViz/configSet/ProcessInputParameters.java`
- Modify: `src/main/java/com/NGSViz/coverageCalculator/MainCalculator.java`
- Test: `src/test/java/com/NGSViz/cli/RunComputeCommandTest.java`
- Test: `src/test/java/com/NGSViz/cli/LegacyComputeRequestAdapterTest.java`
- Test: `src/test/java/com/NGSViz/cli/ComputeManifestTest.java`

### Interfaces

- `RunComputeCommand.execute(Path requestPath): CliResponse`
- `LegacyComputeRequestAdapter.fromArgs(String[] args): ComputeRequest`
- `ComputeManifest.fromRun(...): ComputeManifest`
- `ComputeManifestWriter.write(ComputeManifest manifest, Path path): void`

### Prompt

```text
请实现 Phase 4：Java run-compute。

要求：
1. 运行前重新调用 Phase 3 validator。
2. 验证失败不得创建结果或调用 MainCalculator。
3. JSON 请求和旧 flags 都必须先构造 `ComputeRequest`；将经过验证的 `ComputeRequest` 显式映射到现有 InputParameterAttributes。
4. 将 sample_name 和 group_name 写入本次 plot-setting JSON 元数据。
5. 调用现有 MainCalculator，不复制 coverage 逻辑。
6. 不修改 DataScaler、PhysicalCoverageCalculator、GeneCoverageProcessor 等科学逻辑。
7. 成功后检查 coverage CSV、read-count CSV、plot-setting JSON 存在且非空。
8. 生成 output_dir/compute_manifest.json。
9. Java 不检查图片、不加载 R 包、不运行 Rscript。
10. 计算过程 stdout/stderr 写入 output_dir/NGSViz_compute.log，
   finally 中恢复原始 stream；CLI stdout 最终只输出一个 JSON response。
11. JSON 请求不得转换成完整旧 flags 命令。
12. `LegacyComputeRequestAdapter` 必须保留旧 flags 的参数类型、默认值与解释方式。
13. 旧 flags 与等价 JSON 必须调用同一个 validator 和 MainCalculator。

compute manifest 必须包含：
- schema_version
- status
- ngsviz_version
- started_at
- finished_at
- duration_ms
- resolved_parameters
- sample_id
- sample_name
- group_name
- outputs.coverage_matrices
- outputs.read_count_files
- outputs.plot_setting_file
- log_file
- warnings
- errors

测试：
- validator 失败时不运行
- MainCalculator 异常映射为 exit code 5
- 缺失计算产物映射为 exit code 6
- manifest 必需字段
- manifest 中不出现 visualization_status
- 单次调用只生成一个 sample 的 manifest
- 计算日志不污染最终 JSON response
- 旧 flags 与等价 JSON 生成相同 resolved parameters
- 旧 flags 与 JSON 请求进入同一个 MainCalculator 调用点

验证：
mvn -Dtest=RunComputeCommandTest,ComputeManifestTest test
mvn test
rg -n "Rscript|ngsVizPlotMain" src/main/java/com/NGSViz/cli

不要在本 Phase 运行真实 Samples；集成运行留到 Phase 8。

提交：
feat(cli): add native computation run manifest
```

### Phase 4 验收

- [ ] Java 完成后状态只代表计算完成
- [ ] manifest 不含 R 或图片状态
- [ ] 旧 CLI 计算结果未变化

---

## Phase 5 — R 原生 CLI、validate-plot 与环境检查

### 目标

给 R 可视化模块增加独立子命令和只读验证，不调用 Java、不生成图形。

### Files

- Create: `lib/cli/cli_response.R`
- Create: `lib/cli/cli_parser.R`
- Create: `lib/cli/describe_visualization.R`
- Create: `lib/cli/validate_plot_input.R`
- Create: `lib/cli/check_visualization_environment.R`
- Modify: `lib/ngsVizPlotMain.R`
- Create: `tests/r/test_cli_parser.R`
- Create: `tests/r/test_validate_plot_input.R`
- Create: `tests/r/run_r_tests.R`

### Interfaces

- `parse_cli_args(args) -> list`
- `build_cli_response(command, status, exit_code, data, warnings, errors) -> list`
- `check_visualization_environment() -> list`
- `validate_plot_input(input_dir) -> list`

### Prompt

```text
请实现 Phase 5：R 原生 CLI 和 validate-plot。

行为：
- 无子命令且只有一个目录参数时，保持旧 Main(args) 行为。
- describe --format json 返回版本、命令、输入和退出码。
- validate-plot --input DIR --format json 只读验证。
- run-plot 在本 Phase 暂时返回 unsupported，不提前实现 Phase 6。

validate-plot 检查：
1. R 版本。
2. 现有绘图依赖包已安装，但不得自动安装。
3. input_dir 存在。
4. 至少一个 *_NGSViz_plotSetting.json。
5. 对多个 JSON，sample_name 必须存在且唯一。
6. JSON 必需字段存在。
7. OutputFile.heatmapDataFile 存在且非空。
8. coverage CSV 可读、列数一致、数值无 NaN/Inf。
9. 数值列数等于
   2 * flanking_region_datapoints + middle_datapoints。
10. 全零矩阵记录 warning。

安全：
- 不调用 install.packages 或 pak::pkg_install。
- 不调用 Java。
- 不生成图片。
- JSON 输出通过 jsonlite::toJSON(auto_unbox=TRUE)。

测试使用 tempdir fixture，覆盖正常、缺 JSON、缺 CSV、NaN、Inf、
全零、列数不匹配、多个 JSON、重复 sample_name 和缺少 R 包的 response builder 行为。
R 测试只使用 base R `stopifnot` 和临时目录，不新增 testthat 依赖。

验证：
Rscript tests/r/run_r_tests.R
Rscript lib/ngsVizPlotMain.R describe --format json
Rscript lib/ngsVizPlotMain.R validate-plot \
  --input Samples/Result/H3K4me3_point --format json
rg -n "java -jar|NGSViz-1.2.jar" lib/cli

提交：
feat(r-cli): add native visualization validation
```

### 人工确认点

- [ ] R 缺包是否返回 exit code 3
- [ ] 全零矩阵是否只 warning
- [ ] 多个 plot-setting JSON 的验证策略

---

## Phase 6 — R run-plot 与可视化 manifest

### 目标

让 R 原生入口调用现有绘图流程，并独立验收图片和生成 visualization manifest。

### Files

- Create: `lib/cli/run_plot.R`
- Create: `lib/cli/visualization_manifest.R`
- Modify: `lib/ngsVizPlotMain.R`
- Create: `tests/r/test_run_plot.R`
- Create: `tests/r/test_visualization_manifest.R`

### Interfaces

- `run_plot(input_dir) -> list`
- `build_visualization_manifest(input_dir, outputs, started_at, finished_at) -> list`
- `write_visualization_manifest(manifest, path) -> invisible(path)`

### Prompt

```text
请实现 Phase 6：R run-plot 和 visualization manifest。

要求：
1. 运行前重新调用 validate_plot_input。
2. 验证失败不得调用绘图函数。
3. 调用现有 mergeAveragePlot、mergeMultiVisResult、propressData、
   corPlot 和 runPCA 逻辑，不复制绘图算法。
4. 保持旧单目录入口行为。
5. 检查本次应生成的图片存在且大小 > 0。
6. 生成 input_dir/visualization_manifest.json。
7. R 不读取 BAM、SQLite 或 compute request。
8. R 不运行 Java。
9. 绘图过程消息写入 input_dir/NGSViz_visualization.log；
   CLI stdout 最终只输出一个 JSON response。

visualization manifest 必须包含：
- schema_version
- status
- r_version
- started_at
- finished_at
- duration_ms
- input_plot_settings
- input_coverage_files
- output_images
- log_file
- warnings
- errors

测试使用 mock 绘图函数和临时目录，覆盖：
- validate 失败不绘图
- 绘图异常为 exit code 5
- 图片缺失为 exit code 6
- manifest 必需字段
- manifest 不包含 BAM、database 或 compute_status

验证：
Rscript tests/r/run_r_tests.R
rg -n "java -jar|NGSViz-1.2.jar" lib/cli

不要在本 Phase 重新计算 Samples。

提交：
feat(r-cli): add native visualization run manifest
```

### Phase 6 验收

- [ ] R 可单独可视化已有 Java 结果
- [ ] R manifest 不代表计算状态
- [ ] Java 代码未被本 Phase 修改

---

## Phase 7 — Codex Agent Guide 与项目 Skill

### 目标

让 Codex 直接编排 Java 和 R 两个原生 CLI，不引入任何项目运行时适配层。

### Files

- Create: `agent/ngsViz_agent.md`
- Create: `.agents/skills/ngsviz-agent/SKILL.md`
- Create: `docs/ai-friendly/schemas/analysis_plan.schema.json`
- Create: `docs/ai-friendly/examples/analysis_plan.multi_sample.json`
- Create: `tests/contracts/01_check_agent_guide.sh`

### Prompt

```text
请实现 Phase 7：Codex Agent Guide 与项目 Skill。

注意：agent/ 目录只能包含 Markdown guide，不得包含 Python、Java、R、
shell runner 或统一执行器。

Agent Guide 必须包含四条路径：

单样本只计算：
Extract requirements
-> write compute_request.json
-> Java validate-compute
-> show validation and native command
-> ask confirmation
-> Java run-compute
-> read compute_manifest.json

多样本顺序计算：
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

只可视化：
receive result directory
-> R validate-plot
-> show validation and native command
-> ask confirmation
-> R run-plot
-> read visualization_manifest.json

完整流程：
先完成单样本或多样本计算；单独询问现在绘图还是以后再绘图。
选择以后绘图时结束。选择现在绘图时，新建 visualization workspace，
只复制各 sample 的 plot-setting JSON，再完成只可视化。

Skill 规则：
- 未知科学参数必须提问。
- 默认只 validate。
- analysis_plan 允许 samples[]。
- Java compute request 禁止 samples[]。
- Java 进程严格串行；禁止并发运行 sample。
- 默认 stop_on_error；continue_on_error 必须由用户明确选择。
- 每个 sample 使用唯一 output_dir、sample_name 和 group_name。
- 用户只要求计算时不得自动绘图。
- 用户只要求绘图时不得重新计算。
- 允许用户在后续新任务中只调用 R。
- Java 失败时不得调用 R。
- compute manifest 与 visualization manifest 分开报告。
- 不生成统一 manifest。
- analysis_index 只索引单样本 manifest，不汇总或改写科学结果。
- 不把 Codex 自己的判断写入原始结果文件。

analysis_plan.schema.json 必须规定：
- analysis 为共享计算参数。
- samples 为非空数组。
- 每个 sample 必须有唯一 sample_id、sample_name、group_name、
  signal_bam；control_bam 和 scale_ratio 可为 null。
- execution.mode 只能为 sequential。
- execution.failure_policy 为 stop_on_error 或 continue_on_error。
- execution.visualization 为 deferred 或 after_compute。
- 不允许 sample 覆盖 genome、region、strand、fragment length；
  这些参数不一致时必须拆分为不同 analysis plan。

契约脚本检查：
- Guide 引用了六个原生子命令。
- agent/ 下没有 .py、.R、.java、.sh 执行文件。
- Skill 明确写出 Java 不调用 R、R 不调用 Java。
- Skill 明确写出四个用户路径。
- Skill 明确写出 Java 单样本和 Codex 顺序多样本。
- 多样本示例的 sample_id、sample_name 和 output_dir 唯一。

验证：
bash tests/contracts/01_check_agent_guide.sh
find agent -type f
rg -n "validate-compute|run-compute|validate-plot|run-plot" \
  agent/ngsViz_agent.md .agents/skills/ngsviz-agent/SKILL.md
Rscript -e 'x <- jsonlite::fromJSON("docs/ai-friendly/examples/analysis_plan.multi_sample.json"); stopifnot(length(x$samples$sample_id) >= 2, !anyDuplicated(x$samples$sample_id), !anyDuplicated(x$samples$sample_name))'

提交：
docs(agent): add native Java and R orchestration skill
```

### 手工对话验收

```text
使用 ngsviz-agent skill，分析 Samples/K562_H3K4me3-ENCFF752MYF_5p.bam。
```

Expected: 询问 genome、region、output 等缺失参数，不运行。

```text
使用 ngsviz-agent skill，检查 Samples/Result/H3K4me3_point 能否绘图。
```

Expected: 只调用 R validate-plot，不运行 Java。

```text
使用 ngsviz-agent skill，只计算 hg19 TSS，不需要绘图。
```

Expected: Java 流程结束后停止，不调用 R。

```text
使用 ngsviz-agent skill，按同一套 hg19 TSS 参数依次计算示例 H3K4me3
和 H3K36me3 BAM，只做 validation，不运行。
```

Expected: 生成一个含两个 sample 的 analysis plan 和两个单样本 compute request，
展示固定顺序，不并行、不调用 R。

```text
使用 ngsviz-agent skill，把昨天已经完成的 analysis_index.json 做多样本可视化。
```

Expected: 不调用 Java；新建 visualization workspace，复制各 sample 的
plot-setting JSON，调用 R validate-plot 后等待确认。

---

## Phase 8 — 独立回归、完整 smoke test 与文档

### 目标

分别验证 Java 单样本计算、Codex 多样本顺序编排和 R 后置可视化；任何一步都不让一个模块启动另一个模块。

### Files

- Modify: `README.md`
- Modify: `.gitignore`
- Create: `audit/ai_friendly_native_end_to_end_acceptance.md`
- Create: `tests/contracts/02_check_module_independence.sh`

### Prompt

```text
请执行 Phase 8：原生接口端到端验收。

第一阶段：自动回归
- mvn test
- mvn -DskipTests package
- Rscript tests/r/run_r_tests.R
- bash tests/contracts/01_check_agent_guide.sh
- bash tests/contracts/02_check_module_independence.sh
- git diff --check

第二阶段：Java dry validation
使用：
docs/ai-friendly/examples/compute_request.single.json

运行：
java -jar target/NGSViz-1.0.jar validate-compute \
  --request docs/ai-friendly/examples/compute_request.single.json

展示结果并等待我明确批准。未批准不得运行计算。

第三阶段：多样本顺序 dry validation
使用：
docs/ai-friendly/examples/analysis_plan.multi_sample.json

Codex 展开两个单样本 request，依次运行 validate-compute。
确认：
- 两个 request 都不含 samples 数组。
- sample_id、sample_name、output_dir 唯一。
- 第二个 validate 在第一个退出后才启动。
- validation 阶段不创建计算结果。

展示有序队列并等待我明确批准。未批准不得运行计算。

第四阶段：Java 真实多样本 smoke test
输出只能写入：
- agent_output/native_smoke/H3K4me3
- agent_output/native_smoke/H3K36me3

严格按 analysis plan 顺序逐个运行 run-compute。
每个进程结束后检查自己的 compute_manifest.json 并更新 analysis_index.json，
再决定是否启动下一个。
确认 Java 过程没有启动 R，两个输出目录此时不要求存在新图片。

第五阶段：延迟 R 独立验证
Java 阶段结束后先停止，模拟用户以后才要求绘图。
新建 agent_output/native_smoke/visualization_workspace，
只复制两个 plot-setting JSON。
运行 validate-plot，展示结果并再次等待我明确批准。
未批准不得运行绘图。

第六阶段：R 真实 smoke test
运行 run-plot，检查 visualization_manifest.json。
确认 R 过程没有启动 Java，计算 CSV 校验值未改变。

独立性脚本必须检查：
- 新 Java CLI 包没有 Rscript、ngsVizPlotMain。
- 新 R CLI 目录没有 java -jar、NGSViz JAR。
- agent/ 没有运行时脚本。
- 两个 manifest 独立存在且字段无越界。

README 增加：
1. Java 原生计算 CLI
2. R 原生可视化 CLI
3. Codex 四种使用路径
4. analysis_plan 多样本顺序执行
5. R 的立即调用与后期独立调用
6. 两次独立确认
7. compute manifest、analysis index、visualization manifest 的区别
8. 旧 CLI 兼容说明
9. AI 参数只通过单样本 compute_request.json 传入 Java
10. 旧 flags 与 JSON 请求归一化为同一个 ComputeRequest 且禁止混用

.gitignore 增加：
agent_output/

提交：
test(agent): validate independent Java and R workflows
```

### 最终验收

- [ ] Java 测试通过
- [ ] R 测试通过
- [ ] 旧 Java CLI 可用
- [ ] AI 调用只包含 --request 和 JSON 路径，不展开完整旧 flags
- [ ] 旧 flags 与 JSON 请求共享 ComputeRequest、validator 和 MainCalculator
- [ ] --request 与旧 flags 混用会失败
- [ ] 旧 R 单目录入口可用
- [ ] Java validate 不创建结果
- [ ] R validate 不生成图片
- [ ] Java 单次只处理一个 sample
- [ ] 多个 sample 由 Codex 严格串行启动
- [ ] 每个 sample 有独立 request、output 和 compute manifest
- [ ] analysis index 不替代 compute manifest
- [ ] Java 不调用或检查 R
- [ ] R 不调用或检查 Java
- [ ] Codex 可只计算
- [ ] Codex 可只可视化
- [ ] Codex 可在计算结束后的新任务中单独调用 R
- [ ] Codex 可在两次确认后完成完整流程
- [ ] compute manifest 与 visualization manifest 独立
- [ ] 原始数据和示例结果未覆盖

---

## 四、推荐手动执行顺序

| 顺序 | 模块 | 可停止状态 |
|---:|---|---|
| 0 | 架构与基线 | 决策已固定 |
| 1A–1C | 三份真源契约 | 接口可评审 |
| 2 | Java CLI 基础 | 可机器查询 Java 能力 |
| 3 | Java validate-compute | 可安全验证计算请求 |
| 4 | Java run-compute | AI 友好的独立计算模块 |
| 5 | R validate-plot | 可安全验证绘图输入 |
| 6 | R run-plot | AI 友好的独立可视化模块 |
| 7 | Codex Skill | 自然语言入口完成 |
| 8 | 独立回归 | 发布候选 |

每次只把“所有 Phase 共用的 Codex Prompt”和当前一个 Phase Prompt 交给 Codex。不要一次执行多个 Phase。

---

## 五、Phase 日志模板

```markdown
# Phase N 执行记录

- 目标：
- 修改范围：
- 失败测试证据：
- 通过测试证据：
- Java/R 独立性检查：
- 旧接口兼容性：
- 人工确认：
- 未解决问题：
- Git commit：
- 下一 Phase 前置条件：满足 / 不满足
```

本计划是唯一活动实现计划。任何旧计划中的 Python Adapter、YAML loader、Python runner、Python result checker 均不得执行。
