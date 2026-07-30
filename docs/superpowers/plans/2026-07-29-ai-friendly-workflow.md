# ngsViz AI-Friendly Workflow Implementation Plan

> **已废弃（2026-07-30）：** 本计划采用 Python Agent Adapter 和 YAML，与最新架构决定冲突，请勿执行。当前唯一活动计划为 `docs/superpowers/plans/2026-07-30-ngsviz-native-ai-friendly-phased-prompts.md`。AI 参数只通过单样本 `compute_request.json` 传入 Java；旧 CLI flags 与 JSON 请求在 Java 内部归一化为同一个 `ComputeRequest`。

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在不修改 ngsViz 科学计算行为的前提下，增加可由 Codex 稳定调用的 YAML 配置、运行前检查、安全执行、结果验收和中文报告功能。

**Architecture:** 新增独立 `agent/` Python 适配层，把 YAML 配置验证后转换为现有 Java CLI 与 Rscript 参数数组。适配层负责环境、SQLite、退出码和结果文件检查；Java coverage 计算与 R 绘图逻辑保持不变。

**Tech Stack:** Python 3.10+、PyYAML 6、jsonschema 4、Python unittest、Java CLI、Rscript、SQLite。

## Global Constraints

- 不实现 MCP。
- 不修改 coverage、坐标、strand、binning、normalization 等科学计算行为。
- 不覆盖原始 BAM、BAI、SQLite 数据库或现有示例结果。
- 每个任务先写失败测试，再写最小实现，测试通过后独立提交。
- 项目文档、报告、日志使用中文；代码注释、docstring、标识符和命令行输出使用英文。
- 每个模块只负责一个功能；函数原则上不超过 50 行。
- 实际命令必须使用参数数组执行，禁止通过 shell 字符串拼接。
- 每次运行保存最终 YAML、命令数组、软件版本、日志、退出码和结果清单。
- Agent 只解释运行质量和产物，不自动生成生物学结论。

---

## 文件结构

```text
agent/
├── requirements.txt
├── config.schema.json
├── examples/
│   └── config.example.yaml
├── ngsviz_agent/
│   ├── __init__.py
│   ├── config.py
│   ├── environment.py
│   ├── database.py
│   ├── commands.py
│   ├── runner.py
│   ├── result_check.py
│   └── report.py
├── scripts/
│   ├── 01_check_environment.py
│   ├── 02_check_database.py
│   ├── 03_build_running_configuration.py
│   ├── 04_run_tool.py
│   └── 05_check_result.py
└── tests/
    ├── test_config.py
    ├── test_environment.py
    ├── test_database.py
    ├── test_commands.py
    ├── test_runner.py
    ├── test_result_check.py
    └── test_workflow.py

.agents/skills/ngsviz-agent/
└── SKILL.md

agent/ngsViz_agent.md
```

## Codex 协作规则

每次只让 Codex 执行一个 Task。推荐固定提示词：

```text
执行 docs/superpowers/plans/2026-07-29-ai-friendly-workflow.md 的 Task N。
严格使用 TDD，只修改该 Task 列出的文件。
先确认失败测试，再写最小实现；运行该 Task 的全部测试和相关回归测试。
自查后更新 log/，创建一个独立 Git commit，然后停止并报告验收结果。
不得修改 Java/R 科学计算行为。
```

每个 Task 完成后检查：

```bash
git status --short
git show --stat --oneline HEAD
```

工作区应为空；若测试、验收条件或提交边界不满足，不进入下一 Task。

---

### Task 1: 建立 YAML 运行配置契约

**Files:**
- Create: `agent/requirements.txt`
- Create: `agent/config.schema.json`
- Create: `agent/examples/config.example.yaml`
- Create: `agent/ngsviz_agent/__init__.py`
- Create: `agent/ngsviz_agent/config.py`
- Create: `agent/tests/test_config.py`
- Create: `log/2026-07-29_task01_config_contract.md`

**Interfaces:**
- Consumes: 用户填写的 YAML 文件。
- Produces: `load_config(path: Path) -> dict[str, object]`、`validate_config(data: dict[str, object], schema_path: Path) -> list[str]`。

- [ ] **Step 1: 写配置失败测试**

```python
def test_valid_config_has_no_errors(self):
    data = load_config(self.example_path)
    self.assertEqual(validate_config(data, self.schema_path), [])

def test_missing_genome_is_rejected(self):
    data = load_config(self.example_path)
    del data["analysis"]["genome"]
    errors = validate_config(data, self.schema_path)
    self.assertTrue(any("genome" in item for item in errors))
```

- [ ] **Step 2: 运行测试并确认失败**

```bash
python -m unittest agent/tests/test_config.py -v
```

Expected: FAIL，原因是 `ngsviz_agent.config` 尚不存在。

- [ ] **Step 3: 实现最小配置读取与 schema 校验**

`config.example.yaml` 必须包含 `version`、`tool`、`analysis`、`options` 和 `workflow`。其中 `analysis` 必须包含：

```yaml
version: 1
tool:
  jar_path: NGSViz-1.2.jar
  system_config: NGSViz_setting.json
  rscript_path: Rscript
analysis:
  title: H3K4me3_TSS
  genome: hg19
  region: tss
  signal_bam: Samples/K562_H3K4me3-ENCFF752MYF_5p.bam
  control_bam: null
  output_dir: agent_output/H3K4me3_TSS
options:
  database: RefSeq
  analysis_type: transcript
  biotype: protein_coding
  flank_region: 2000
  flank_factor: 0.0
  num_datapoints: 100
  scale_ratio: null
  mapping_quality: 20
  fragment_length: 150
  cores: 1
  batch_size: 500
  bin_method: mean
  strand_specific: both
  center_mode: false
  custom_bed: null
workflow:
  run_plot: true
  dry_run: true
```

`config.py` 使用 `yaml.safe_load` 和 `jsonschema.Draft202012Validator`，返回按字段路径排序的全部错误，不在读取阶段执行命令。

- [ ] **Step 4: 运行测试并确认通过**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_config.py -v
```

Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add agent/requirements.txt agent/config.schema.json agent/examples/config.example.yaml agent/ngsviz_agent agent/tests/test_config.py log/2026-07-29_task01_config_contract.md
git commit -m "feat(agent): add YAML configuration contract"
```

**给 Codex 的任务：** 使用上方固定提示词，将 `Task N` 替换为 `Task 1`。

---

### Task 2: 实现 Check_environment

**Files:**
- Create: `agent/ngsviz_agent/environment.py`
- Create: `agent/scripts/01_check_environment.py`
- Create: `agent/tests/test_environment.py`
- Create: `log/2026-07-29_task02_environment_check.md`

**Interfaces:**
- Consumes: Task 1 的已验证配置。
- Produces: `check_environment(config: dict[str, object]) -> dict[str, object]`，包含 `status`、`checks`、`errors`、`warnings`。

- [ ] **Step 1: 写环境检查失败测试**

```python
def test_missing_jar_blocks_execution(self):
    config = self.make_config(jar_path="missing.jar")
    result = check_environment(config)
    self.assertEqual(result["status"], "failed")
    self.assertIn("jar_exists", result["checks"])

def test_missing_bai_blocks_execution(self):
    config = self.make_config(signal_bam="sample.bam")
    result = check_environment(config)
    self.assertTrue(any("BAI" in item for item in result["errors"]))
```

- [ ] **Step 2: 运行测试并确认失败**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_environment.py -v
```

Expected: FAIL，原因是 `environment.py` 尚不存在。

- [ ] **Step 3: 实现只读环境检查**

检查以下项目：

- `java -version` 可执行并记录版本。
- `Rscript --version` 可执行并记录版本。
- JAR、系统配置、signal BAM、可选 control BAM 存在。
- BAM 对应 `.bai` 或替换 `.bam` 后缀得到的 `.bai` 存在。
- 输出目录的最近已存在父目录可写。
- `workflow.run_plot=true` 时，运行一次 `Rscript -e` 检查 `jsonlite`、`ggh4x`、`tidyverse`、`colorspace`、`circlize`、`RColorBrewer`、`purrr`、`data.table`、`RcppRoll`、`zoo`、`corrplot`、`ggrepel`、`FactoMineR`、`aplot`、`cowplot` 和 `scales`。
- 不安装软件包，不创建输出目录。

脚本仅输出 JSON，退出码 `0` 表示通过、`2` 表示预检查失败。

- [ ] **Step 4: 运行测试和本地示例**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_environment.py -v
PYTHONPATH=agent python agent/scripts/01_check_environment.py agent/examples/config.example.yaml
```

Expected: 测试 PASS；本地示例输出合法 JSON。

- [ ] **Step 5: 提交**

```bash
git add agent/ngsviz_agent/environment.py agent/scripts/01_check_environment.py agent/tests/test_environment.py log/2026-07-29_task02_environment_check.md
git commit -m "feat(agent): add environment preflight checks"
```

**给 Codex 的任务：** 使用固定提示词执行 `Task 2`。

---

### Task 3: 实现 Check_db

**Files:**
- Create: `agent/ngsviz_agent/database.py`
- Create: `agent/scripts/02_check_database.py`
- Create: `agent/tests/test_database.py`
- Create: `log/2026-07-29_task03_database_check.md`

**Interfaces:**
- Consumes: `tool.system_config` 和分析选择。
- Produces: `resolve_database_path(config: dict[str, object]) -> Path`、`check_database(config: dict[str, object]) -> dict[str, object]`。

- [ ] **Step 1: 写临时 SQLite 失败测试**

```python
def test_requested_combination_must_exist(self):
    result = check_database(self.make_config(genome="mm10"))
    self.assertEqual(result["status"], "failed")
    self.assertTrue(any("mm10" in item for item in result["errors"]))

def test_integrity_and_supported_values_are_reported(self):
    result = check_database(self.make_config(genome="hg19"))
    self.assertEqual(result["checks"]["integrity"], "ok")
    self.assertIn("hg19", result["supported"]["genomes"])
```

- [ ] **Step 2: 运行测试并确认失败**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_database.py -v
```

Expected: FAIL，原因是数据库检查接口尚不存在。

- [ ] **Step 3: 使用只读 SQLite 实现检查**

使用 URI `file:<path>?mode=ro` 打开数据库，执行：

```sql
PRAGMA integrity_check;
SELECT Species, CoordinateTblName, DB, Genome, Region,
       AnalysisType, Biotype, PointLab, FlankSize
FROM defaultTbl;
```

确认所选 `genome + database + region` 存在，并确认 `CoordinateTblName` 对应表存在。返回支持值和数据库绝对路径，不修改数据库。

- [ ] **Step 4: 运行测试和真实数据库检查**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_database.py -v
PYTHONPATH=agent python agent/scripts/02_check_database.py agent/examples/config.example.yaml
```

Expected: PASS；真实数据库 `integrity` 为 `ok`。

- [ ] **Step 5: 提交**

```bash
git add agent/ngsviz_agent/database.py agent/scripts/02_check_database.py agent/tests/test_database.py log/2026-07-29_task03_database_check.md
git commit -m "feat(agent): add read-only database checks"
```

**给 Codex 的任务：** 使用固定提示词执行 `Task 3`。

---

### Task 4: 构建最终运行配置与命令数组

**Files:**
- Create: `agent/ngsviz_agent/commands.py`
- Create: `agent/scripts/03_build_running_configuration.py`
- Create: `agent/tests/test_commands.py`
- Create: `log/2026-07-29_task04_command_builder.md`

**Interfaces:**
- Consumes: 已通过环境和数据库检查的配置。
- Produces: `build_java_command(config: dict[str, object]) -> list[str]`、`build_plot_command(config: dict[str, object]) -> list[str]`、`write_resolved_config(config: dict[str, object], path: Path) -> None`。

- [ ] **Step 1: 写精确参数映射测试**

```python
def test_signal_and_control_are_joined_for_java_input(self):
    command = build_java_command(self.make_config(control_bam="control.bam"))
    input_index = command.index("-I")
    self.assertEqual(command[input_index + 1], "signal.bam:control.bam")

def test_none_options_are_omitted(self):
    command = build_java_command(self.make_config(scale_ratio=None, custom_bed=None))
    self.assertNotIn("-S", command)
    self.assertNotIn("-BD", command)
```

- [ ] **Step 2: 运行测试并确认失败**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_commands.py -v
```

Expected: FAIL，原因是命令构建器尚不存在。

- [ ] **Step 3: 实现显式白名单映射**

映射必须固定为：

```python
OPTION_FLAGS = {
    "database": "-D",
    "analysis_type": "-A",
    "biotype": "-B",
    "flank_region": "-F",
    "flank_factor": "-N",
    "num_datapoints": "-DP",
    "scale_ratio": "-S",
    "mapping_quality": "-MQ",
    "fragment_length": "-FL",
    "cores": "-P",
    "batch_size": "-BS",
    "bin_method": "-BM",
    "strand_specific": "-SS",
    "center_mode": "-CM",
    "custom_bed": "-BD",
}
```

命令返回字符串数组，不调用 shell，不接受 schema 之外的额外 flag。输出 `resolved_config.yaml` 时把相对路径解析为绝对路径，同时保留原始用户配置副本。

- [ ] **Step 4: 运行测试并检查 dry-run 输出**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_commands.py -v
PYTHONPATH=agent python agent/scripts/03_build_running_configuration.py agent/examples/config.example.yaml
```

Expected: PASS；输出的 Java/R 命令可逐项审阅，未执行计算。

- [ ] **Step 5: 提交**

```bash
git add agent/ngsviz_agent/commands.py agent/scripts/03_build_running_configuration.py agent/tests/test_commands.py log/2026-07-29_task04_command_builder.md
git commit -m "feat(agent): build deterministic ngsViz commands"
```

**给 Codex 的任务：** 使用固定提示词执行 `Task 4`。

---

### Task 5: 安全执行 Java 与 R 工作流

**Files:**
- Create: `agent/ngsviz_agent/runner.py`
- Create: `agent/scripts/04_run_tool.py`
- Create: `agent/tests/test_runner.py`
- Create: `log/2026-07-29_task05_workflow_runner.md`

**Interfaces:**
- Consumes: Task 4 的命令数组。
- Produces: `run_command(command: list[str], log_path: Path) -> dict[str, object]`、`run_workflow(config_path: Path) -> dict[str, object]`。工作流返回值包含 `status`、`java_command`、`plot_command`、`exit_codes` 和 `run_dir`。

- [ ] **Step 1: 写退出码与 dry-run 测试**

```python
from unittest.mock import patch

def test_nonzero_exit_is_failed(self):
    result = run_command(["python", "-c", "raise SystemExit(7)"], self.log_path)
    self.assertEqual(result["status"], "failed")
    self.assertEqual(result["exit_code"], 7)

@patch("ngsviz_agent.runner.run_command")
def test_dry_run_does_not_start_process(self, run_command_mock):
    result = run_workflow(self.dry_run_config)
    self.assertEqual(result["status"], "dry_run")
    run_command_mock.assert_not_called()
    self.assertTrue((self.output_dir / ".ngsviz_agent" / "commands.json").exists())
    self.assertEqual(list(self.output_dir.glob("*coverage_matrix*.csv")), [])
```

- [ ] **Step 2: 运行测试并确认失败**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_runner.py -v
```

Expected: FAIL，原因是 runner 尚不存在。

- [ ] **Step 3: 实现顺序执行和运行目录**

运行目录格式：

```text
<output_dir>/
└── .ngsviz_agent/
    ├── input_config.yaml
    ├── resolved_config.yaml
    ├── commands.json
    ├── environment.json
    ├── database.json
    ├── java.log
    ├── plot.log
    └── run_state.json
```

状态只允许 `preflight_failed`、`dry_run`、`running_java`、`running_plot`、`failed`、`completed`。Java 非零时不得执行 R；`run_plot=false` 时 Java 成功后进入结果检查。使用 `subprocess.run(command, shell=False)`，同时保存 stdout 和 stderr。

- [ ] **Step 4: 运行测试与 dry-run**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_runner.py -v
PYTHONPATH=agent python agent/scripts/04_run_tool.py agent/examples/config.example.yaml
```

Expected: PASS；示例因 `dry_run: true` 不启动 Java/R。

- [ ] **Step 5: 提交**

```bash
git add agent/ngsviz_agent/runner.py agent/scripts/04_run_tool.py agent/tests/test_runner.py log/2026-07-29_task05_workflow_runner.md
git commit -m "feat(agent): run ngsViz workflow safely"
```

**给 Codex 的任务：** 使用固定提示词执行 `Task 5`。

---

### Task 6: 实现 Check_result 与中文运行报告

**Files:**
- Create: `agent/ngsviz_agent/result_check.py`
- Create: `agent/ngsviz_agent/report.py`
- Create: `agent/scripts/05_check_result.py`
- Create: `agent/tests/test_result_check.py`
- Create: `log/2026-07-29_task06_result_validation.md`

**Interfaces:**
- Consumes: 输出目录、resolved config、Java/R 退出码。
- Produces: `check_results(config: dict[str, object]) -> dict[str, object]`、`write_report(result: dict[str, object], output_dir: Path) -> tuple[Path, Path]`。

- [ ] **Step 1: 写缺失文件、全零和正常矩阵测试**

```python
def test_missing_plot_config_is_failed(self):
    result = check_results(self.make_config())
    self.assertEqual(result["status"], "failed")

def test_all_zero_matrix_is_warning(self):
    self.write_matrix([["Gene", "P1"], ["A", "0"], ["B", "0"]])
    result = check_results(self.make_config())
    self.assertTrue(any("all-zero" in item for item in result["warnings"]))
```

- [ ] **Step 2: 运行测试并确认失败**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_result_check.py -v
```

Expected: FAIL，原因是结果检查模块尚不存在。

- [ ] **Step 3: 实现结果验收**

检查并记录：

- `*_coverage_matrix_heatmap.csv`、`*_gene_read_count.csv`、`*_NGSViz_plotSetting.json` 存在且非空。
- CSV 列数一致，数据可转换为数值，不含 NaN/Inf。
- coverage matrix 行数、数据列数、非零比例、最小值、最大值。
- read count 行数与 coverage matrix 行数一致。
- JSON 中 `OutputFile` 路径存在，`num_datapoints` 与矩阵列数关系合理。
- `run_plot=true` 时至少存在预期图片文件；图片大小大于 0。

生成：

```text
result_report.json
result_report.md
```

Markdown 报告使用中文，仅包含运行状态、参数、产物、质量指标、错误、警告和复现命令。

- [ ] **Step 4: 运行测试并验证现有示例结果**

```bash
PYTHONPATH=agent python -m unittest agent/tests/test_result_check.py -v
PYTHONPATH=agent python agent/scripts/05_check_result.py agent/examples/config.example.yaml
```

Expected: PASS；若示例配置尚未实际运行，脚本应明确返回缺失产物而不是假成功。

- [ ] **Step 5: 提交**

```bash
git add agent/ngsviz_agent/result_check.py agent/ngsviz_agent/report.py agent/scripts/05_check_result.py agent/tests/test_result_check.py log/2026-07-29_task06_result_validation.md
git commit -m "feat(agent): validate outputs and write run reports"
```

**给 Codex 的任务：** 使用固定提示词执行 `Task 6`。

---

### Task 7: 编写 Codex Skill 与 Agent Guide

**Files:**
- Create: `.agents/skills/ngsviz-agent/SKILL.md`
- Create: `agent/ngsViz_agent.md`
- Create: `log/2026-07-29_task07_codex_skill.md`

**Interfaces:**
- Consumes: 用户自然语言需求和 Task 1–6 的五个脚本。
- Produces: 固定的 Codex 决策顺序和用户确认点。

- [ ] **Step 1: 写 guide 验收脚本**

```bash
rg -n "Check_environment|Check_db|Build_running_configuration|Run_tool|Check_result" agent/ngsViz_agent.md
rg -n "01_check_environment.py|02_check_database.py|03_build_running_configuration.py|04_run_tool.py|05_check_result.py" .agents/skills/ngsviz-agent/SKILL.md
```

Expected: FAIL，文件尚不存在。

- [ ] **Step 2: 编写固定工作流**

Guide 必须规定：

1. 从用户需求提取已知参数，未知的科学参数不得猜测。
2. 先生成 `config.yaml`，向用户展示 genome、region、BAM、control、strand、fragment length、normalization 和 output。
3. 运行环境和数据库检查。
4. 执行 dry-run，展示最终命令数组。
5. 只有用户明确要求运行时才把 `dry_run` 改为 `false`。
6. 计算后必须运行结果检查。
7. 解释仅基于 `result_report.json`，不把图形模式直接写成生物学结论。

- [ ] **Step 3: 验证 guide 引用全部真实接口**

```bash
test -f agent/scripts/01_check_environment.py
test -f agent/scripts/02_check_database.py
test -f agent/scripts/03_build_running_configuration.py
test -f agent/scripts/04_run_tool.py
test -f agent/scripts/05_check_result.py
```

Expected: 所有命令退出码为 0。

- [ ] **Step 4: 用 Codex 做一次只读模拟**

向 Codex 输入：

```text
使用 ngsviz-agent skill，为 Samples/K562_H3K4me3-ENCFF752MYF_5p.bam
生成 hg19 TSS 分析配置，只做 dry-run，不执行计算。
```

Expected: Codex 生成配置、执行三项预检查并展示命令，不启动 Java/R。

- [ ] **Step 5: 提交**

```bash
git add .agents/skills/ngsviz-agent/SKILL.md agent/ngsViz_agent.md log/2026-07-29_task07_codex_skill.md
git commit -m "docs(agent): add Codex ngsViz workflow skill"
```

**给 Codex 的任务：** 使用固定提示词执行 `Task 7`。

---

### Task 8: 端到端回归、README 和发布验收

**Files:**
- Create: `agent/tests/test_workflow.py`
- Modify: `README.md`
- Modify: `.gitignore`
- Create: `log/2026-07-29_task08_end_to_end_validation.md`

**Interfaces:**
- Consumes: 完整 Agent 适配层和现有 K562 示例。
- Produces: 可重复的 dry-run 回归测试、一次真实 smoke test 和用户文档。

- [ ] **Step 1: 写端到端 dry-run 测试**

```python
def test_example_dry_run_reaches_dry_run_state(self):
    result = run_workflow(Path("agent/examples/config.example.yaml"))
    self.assertEqual(result["status"], "dry_run")
    self.assertIn("-G", result["java_command"])
    self.assertIn("hg19", result["java_command"])
```

- [ ] **Step 2: 运行全量 Python 测试**

```bash
PYTHONPATH=agent python -m unittest discover -s agent/tests -v
```

Expected: 全部 PASS。

- [ ] **Step 3: 执行独立真实 smoke test**

复制示例配置，将：

```yaml
analysis:
  output_dir: agent_output/smoke_hg19_tss
workflow:
  run_plot: true
  dry_run: false
```

运行：

```bash
PYTHONPATH=agent python agent/scripts/04_run_tool.py /tmp/ngsviz_smoke_config.yaml
PYTHONPATH=agent python agent/scripts/05_check_result.py /tmp/ngsviz_smoke_config.yaml
```

Expected: Java 和 R 退出码为 0，`result_report.json` 状态为 `completed`，coverage/read-count/JSON/图片均通过检查。输出只写入 `agent_output/smoke_hg19_tss`。

- [ ] **Step 4: 更新文档与忽略规则**

README 增加中文 “AI 友好工作流” 小节，说明安装、配置、dry-run、实际运行、结果检查和 Codex 使用示例。`.gitignore` 增加：

```gitignore
agent_output/
agent/**/__pycache__/
```

- [ ] **Step 5: 最终回归与提交**

```bash
PYTHONPATH=agent python -m unittest discover -s agent/tests -v
mvn test
git diff --check
git add agent/tests/test_workflow.py README.md .gitignore log/2026-07-29_task08_end_to_end_validation.md
git commit -m "test(agent): validate AI-friendly workflow end to end"
```

Expected: Python 与 Maven 测试全部通过，工作区只剩被忽略的 smoke test 输出。

**给 Codex 的任务：** 使用固定提示词执行 `Task 8`。

---

## 推荐执行节奏

| 顺序 | 功能交付 | 预计时间 | 是否可停在此版本 |
|---:|---|---:|---|
| 1 | YAML 配置契约 | 0.5–1 天 | 否 |
| 2 | 环境预检查 | 0.5–1 天 | 否 |
| 3 | SQLite 预检查 | 0.5–1 天 | 否 |
| 4 | 可审阅命令与 dry-run | 1 天 | 是，形成安全原型 |
| 5 | Java/R 工作流执行 | 1–1.5 天 | 是，形成可用 MVP |
| 6 | 结果验收与报告 | 1–1.5 天 | 是，推荐日常使用 |
| 7 | Codex Skill 与 guide | 0.5–1 天 | 是，完成 AI 友好入口 |
| 8 | 真实回归与文档 | 1–2 天 | 是，形成投稿级候选 |

不含 MCP 的总工作量约 7–10 人日。Task 1–4 完成后先试用 dry-run，再决定是否连续完成 Task 5–8。
