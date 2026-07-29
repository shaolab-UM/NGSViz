# ngsViz Native AI-Friendly Progressive Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 不增加 Python Agent Adapter，通过增强 Java 计算 CLI、R 可视化 CLI 和 Codex Skill，让用户能够以自然语言安全、渐进地使用 ngsViz。

**Architecture:** Java 计算模块与 R 可视化模块保持两个独立程序，各自提供 `describe`、`validate`、`run` 和机器可读 manifest。Java 不启动、检查或管理 R；R 不启动、检查或管理 Java；Codex Skill 根据用户需求分别调用两个原生入口。

**Tech Stack:** Java 9+、Maven、htsjdk、org.json、SQLite JDBC、R 4.x、jsonlite、现有 ngsViz R 绘图库、JUnit 5、R 自包含测试脚本。

## Global Constraints

- 不创建 Python 包、Python 脚本、Python测试或 Python Agent Adapter。
- 不实现 MCP、HTTP API、聊天网页或云端服务。
- Java 只负责计算环境、数据库、BAM/BED、coverage 计算和计算结果 manifest。
- R 只负责可视化环境、绘图输入、图形生成和可视化 manifest。
- Java 不得调用 `Rscript`；R 不得调用 `java -jar`。
- 保留现有 `java -jar NGSViz-1.2.jar -G ...` 和 `Rscript lib/ngsVizPlotMain.R <JSON_path>` 用法。
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
  │  Java describe
  │    ↓
  │  Java validate-compute
  │    ↓
  │  用户确认
  │    ↓
  │  Java run-compute
  │    ↓
  │  compute_manifest.json
  │
  ├─ 只可视化已有结果
  │    ↓
  │  R describe
  │    ↓
  │  R validate-plot
  │    ↓
  │  用户确认
  │    ↓
  │  R run-plot
  │    ↓
  │  visualization_manifest.json
  │
  └─ 完整流程
       Java 原生流程完成
         ↓
       Codex 读取 compute manifest
         ↓
       R 原生流程完成
         ↓
       Codex 分别读取两个 manifest
```

### Java 计算模块的唯一职责

- 解析计算请求。
- 检查 Java、JAR、系统配置、SQLite、BAM/BAI、BED 和计算参数。
- 调用现有 Java coverage 计算逻辑。
- 生成 coverage CSV、read-count CSV、plot-setting JSON。
- 生成 `compute_manifest.json`。

### R 可视化模块的唯一职责

- 检查 R 版本和绘图包。
- 验证已有 plot-setting JSON 与 coverage CSV。
- 调用现有 R 绘图函数。
- 检查图片产物。
- 生成 `visualization_manifest.json`。

### Codex Skill 的唯一职责

- 从用户需求提取参数。
- 对缺失科学参数提问。
- 生成 Java 计算请求 JSON。
- 展示原生命令和验证结果。
- 得到明确授权后分别调用 Java 或 R。
- 基于 manifest 做有限解读。

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

兼容旧入口：

```bash
java -jar NGSViz-1.2.jar -G hg19 -R tss -T demo -I sample.bam -O output
```

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
2. 旧短参数入口的兼容规则。
3. compute_request.json 的字段、类型、必填性、默认值来源和旧 flag 映射。
4. 参数至少包括 title、genome、region、signal_bam、control_bam、output_dir、
   system_config、database、analysis_type、biotype、flank_region、flank_factor、
   num_datapoints、scale_ratio、mapping_quality、fragment_length、cores、
   batch_size、bin_method、strand_specific、center_mode、custom_bed。
5. null 不生成旧 flag。
6. num_datapoints 最低为 100。
7. validate-compute 只读且不创建输出目录。
8. run-compute 只运行 Java，不启动 R。
9. compute_manifest.json schema。
10. 退出码和 JSON response envelope。

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
4. validate-plot 检查 R 包、JSON schema、OutputFile、coverage CSV。
5. run-plot 只调用现有绘图逻辑。
6. visualization_manifest.json schema。
7. coverage 数值列数等于
   2 × flanking_region_datapoints + middle_datapoints。
8. 图片缺失、空文件、NaN/Inf 和全零矩阵的状态规则。
9. R 模块退出码和 JSON response envelope。

不得把 Java 环境、SQLite、BAM 或计算参数检查放入 R 契约。

提交：
docs(r): define native visualization CLI contract
```

### Phase 1C Prompt — Codex 编排契约

```text
请执行 Phase 1C，只创建 docs/ai-friendly/03_Codex编排与确认契约.md，不写代码。

定义三条独立用户路径：

A. 只计算
User request -> ask missing parameters -> compute request JSON ->
Java validate -> show command -> user confirm -> Java run -> compute manifest

B. 只可视化
User supplies result directory -> R validate -> show command ->
user confirm -> R run -> visualization manifest

C. 完整流程
完整执行 A；Java 完成并通过 compute manifest 后，单独询问是否运行 R；再执行 B。

必须规定：
- 首次请求默认只 validate，不直接 run。
- 用户只要求计算时不得自动绘图。
- 用户只要求绘图时不得重新计算。
- Java 完成不代表 R 完成。
- 两个 manifest 分开报告。
- 不存在统一 Python runner 或统一 runtime manifest。
- 未知科学参数必须向用户提问。

提交：
docs(agent): define native Codex orchestration contract
```

### Phase 1 人工确认

- [ ] compute_request.json 字段与 Java flag 一致
- [ ] R 输入契约与现有 plot-setting JSON 一致
- [ ] 三条 Codex 用户路径符合预期
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
- 输出解析后的参数、支持值、errors、warnings 和 legacy_command_preview。

先写失败测试，覆盖：
- 合法示例
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
- Create: `src/main/java/com/NGSViz/cli/ComputeManifest.java`
- Create: `src/main/java/com/NGSViz/cli/ComputeManifestWriter.java`
- Modify: `src/main/java/com/NGSViz/configSet/ProcessInputParameters.java`
- Modify: `src/main/java/com/NGSViz/coverageCalculator/MainCalculator.java`
- Test: `src/test/java/com/NGSViz/cli/RunComputeCommandTest.java`
- Test: `src/test/java/com/NGSViz/cli/ComputeManifestTest.java`

### Interfaces

- `RunComputeCommand.execute(Path requestPath): CliResponse`
- `ComputeManifest.fromRun(...): ComputeManifest`
- `ComputeManifestWriter.write(ComputeManifest manifest, Path path): void`

### Prompt

```text
请实现 Phase 4：Java run-compute。

要求：
1. 运行前重新调用 Phase 3 validator。
2. 验证失败不得创建结果或调用 MainCalculator。
3. 将 ComputeRequest 显式映射到现有 InputParameterAttributes。
4. 调用现有 MainCalculator，不复制 coverage 逻辑。
5. 不修改 DataScaler、PhysicalCoverageCalculator、GeneCoverageProcessor 等科学逻辑。
6. 成功后检查 coverage CSV、read-count CSV、plot-setting JSON 存在且非空。
7. 生成 output_dir/compute_manifest.json。
8. Java 不检查图片、不加载 R 包、不运行 Rscript。
9. 计算过程 stdout/stderr 写入 output_dir/NGSViz_compute.log，
   finally 中恢复原始 stream；CLI stdout 最终只输出一个 JSON response。

compute manifest 必须包含：
- schema_version
- status
- ngsviz_version
- started_at
- finished_at
- duration_ms
- resolved_parameters
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
- 计算日志不污染最终 JSON response

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
5. JSON 必需字段存在。
6. OutputFile.heatmapDataFile 存在且非空。
7. coverage CSV 可读、列数一致、数值无 NaN/Inf。
8. 数值列数等于
   2 * flanking_region_datapoints + middle_datapoints。
9. 全零矩阵记录 warning。

安全：
- 不调用 install.packages 或 pak::pkg_install。
- 不调用 Java。
- 不生成图片。
- JSON 输出通过 jsonlite::toJSON(auto_unbox=TRUE)。

测试使用 tempdir fixture，覆盖正常、缺 JSON、缺 CSV、NaN、Inf、
全零、列数不匹配和缺少 R 包的 response builder 行为。
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
- Create: `tests/contracts/01_check_agent_guide.sh`

### Prompt

```text
请实现 Phase 7：Codex Agent Guide 与项目 Skill。

注意：agent/ 目录只能包含 Markdown guide，不得包含 Python、Java、R、
shell runner 或统一执行器。

Agent Guide 必须包含三条路径：

只计算：
Extract requirements
-> write compute_request.json
-> Java validate-compute
-> show validation and native command
-> ask confirmation
-> Java run-compute
-> read compute_manifest.json

只可视化：
receive result directory
-> R validate-plot
-> show validation and native command
-> ask confirmation
-> R run-plot
-> read visualization_manifest.json

完整流程：
先完成只计算；单独询问是否继续可视化；用户确认后再完成只可视化。

Skill 规则：
- 未知科学参数必须提问。
- 默认只 validate。
- 用户只要求计算时不得自动绘图。
- 用户只要求绘图时不得重新计算。
- Java 失败时不得调用 R。
- compute manifest 与 visualization manifest 分开报告。
- 不生成统一 manifest。
- 不把 Codex 自己的判断写入原始结果文件。

契约脚本检查：
- Guide 引用了六个原生子命令。
- agent/ 下没有 .py、.R、.java、.sh 执行文件。
- Skill 明确写出 Java 不调用 R、R 不调用 Java。
- Skill 明确写出三个用户路径。

验证：
bash tests/contracts/01_check_agent_guide.sh
find agent -type f
rg -n "validate-compute|run-compute|validate-plot|run-plot" \
  agent/ngsViz_agent.md .agents/skills/ngsviz-agent/SKILL.md

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

---

## Phase 8 — 独立回归、完整 smoke test 与文档

### 目标

分别验证 Java 计算和 R 可视化，再验证 Codex 顺序编排；任何一步都不让一个模块启动另一个模块。

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

第三阶段：Java 真实 smoke test
输出只能写入 agent_output/native_smoke_hg19_tss。
运行 run-compute，检查 compute_manifest.json。
确认 Java 过程没有启动 R，输出目录此时不要求存在新图片。

第四阶段：R 独立验证
运行 validate-plot，展示结果并再次等待我明确批准。
未批准不得运行绘图。

第五阶段：R 真实 smoke test
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
3. Codex 三种使用路径
4. 两次独立确认
5. manifest 解读
6. 旧 CLI 兼容说明

.gitignore 增加：
agent_output/

提交：
test(agent): validate independent Java and R workflows
```

### 最终验收

- [ ] Java 测试通过
- [ ] R 测试通过
- [ ] 旧 Java CLI 可用
- [ ] 旧 R 单目录入口可用
- [ ] Java validate 不创建结果
- [ ] R validate 不生成图片
- [ ] Java 不调用或检查 R
- [ ] R 不调用或检查 Java
- [ ] Codex 可只计算
- [ ] Codex 可只可视化
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
