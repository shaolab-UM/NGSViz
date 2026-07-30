# ngsViz AI-Friendly Progressive Upgrade Implementation Plan

> **已废弃（2026-07-30）：** 本计划采用 Python Agent Adapter 和 YAML，与最新架构决定冲突，请勿执行。当前唯一活动计划为 `docs/superpowers/plans/2026-07-30-ngsviz-native-ai-friendly-phased-prompts.md`。AI 参数只通过单样本 `compute_request.json` 传入 Java；旧 CLI flags 与 JSON 请求在 Java 内部归一化为同一个 `ComputeRequest`。

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 通过渐进式模块升级，让 Codex 能把用户需求安全转换为 ngsViz 配置，完成环境与数据库检查、dry-run、Java/R 执行、结果验收和运行解读。

**Architecture:** 保留现有 Java 计算核心、R 绘图脚本、RShiny GUI 和 SQLite 数据库，在其外部新增独立 Python Agent Adapter。升级顺序从文档契约和只读检查开始，直到 dry-run 验证通过后才允许接入真实计算。

**Tech Stack:** Java CLI、Rscript、SQLite、Python 3.10+、PyYAML、jsonschema、Python unittest、Codex project skill。

## Global Constraints

- 不实现 MCP、HTTP API、聊天网页或云端服务。
- 不修改 coverage、坐标、TSS/TES、strand、binning、normalization 等科学计算行为。
- 不覆盖原始 BAM、BAI、SQLite 数据库和 `Samples/Result/` 现有示例。
- 用户未明确提供的科学参数不得由 AI 猜测。
- Java/R 实际执行前必须依次通过配置验证、环境检查、数据库检查和 dry-run。
- 每个 Phase 只完成本阶段模块，不提前实现后续 Phase。
- 每个 Phase 使用 TDD，测试通过后更新 `log/` 并创建一个独立 Git commit。
- 项目文档、分析报告、变更说明和日志使用中文。
- 代码注释、docstring、变量名、函数名、类名和命令行输出使用英文。
- Agent 只解释运行质量和结果产物，不自动生成未经验证的生物学结论。

---

## 一、系统定位

### AI 友好功能是什么

这不是在 ngsViz 内部加入一个自由发挥的聊天机器人，而是增加一个受约束的分析编排层：

```text
用户需求
  ↓
ngsViz Agent Guide
  ↓
结构化需求 + 人工确认
  ↓
config.yaml
  ↓
Check_environment
  ↓
Check_db_and_input
  ↓
Build_running_configuration + dry-run
  ↓
Run_tool
  ↓
Check_result
  ↓
结果清单 + 运行质量解读
```

### 三层职责

- **Agent Guide / Skill**：理解需求、识别缺失信息、决定调用顺序，不执行科学计算。
- **Python Agent Adapter**：验证 YAML、检查环境和数据库、构建命令、管理运行日志、验收结果。
- **ngsViz Java / R**：执行真实 coverage 计算和绘图，是科学结果的唯一计算来源。

### 不可推翻的七条铁律

1. Java/R 是科学计算真源，Python 适配层不得重新计算 coverage。
2. 坐标、strand、fragment length、normalization 等参数不允许静默推断。
3. 所有命令使用参数数组和 `shell=False` 执行，不拼接 shell 字符串。
4. dry-run 只能生成配置、检查结果和命令，不启动 Java/R。
5. 退出码为 0 不等于结果正确，必须继续执行文件和数值验收。
6. 运行报告必须保存输入配置、最终配置、版本、命令、日志、退出码和结果清单。
7. 每个阶段有人工确认门；未通过不得进入下一阶段。

---

## 二、渐进式路线图

| Phase | 模块 | 阶段产物 | 达成状态 |
|---:|---|---|---|
| 0 | AI 协作基础设施 | 项目规则、基线清单、执行约定 | 可以安全开始 vibe coding |
| 1 | 三份真源文档 | 使用场景、配置契约、结果契约 | 需求和边界固定 |
| 2 | YAML 配置模块 | schema、loader、示例配置 | 配置可机器验证 |
| 3 | 环境检查模块 | Java/R/JAR/R 包/BAM/BAI 检查 | 本机是否能运行可确定 |
| 4 | 数据库与输入检查 | SQLite、组合合法性、BAM header 检查 | 科学输入风险可提前发现 |
| 5 | 最终配置与 dry-run | resolved config、Java/R 命令 | 形成安全原型 |
| 6 | 实际运行模块 | Java/R 顺序执行、状态和日志 | 形成可运行 MVP |
| 7 | 结果验收与解读 | JSON/Markdown 运行报告 | 形成日常可用版本 |
| 8 | Codex Agent Guide / Skill | 自然语言编排入口 | 形成 AI 友好入口 |
| 9 | 端到端回归与文档 | smoke test、README、发布清单 | 形成投稿级候选 |

### 推荐停止点

- Phase 0–2：只有配置能力，不建议交付使用。
- Phase 0–5：安全 dry-run 原型，可先试用并调整交互。
- Phase 0–7：可运行、可验收，适合日常使用。
- Phase 0–9：完整 AI 友好工作流。

---

## 三、所有模块共用的 Codex 前置 Prompt

每次开始一个新 Phase，先把下面内容与该 Phase 的专用 Prompt 一起交给 Codex：

```text
你正在升级 ngsViz 的 AI 友好工作流。

开始前必须：
1. 阅读仓库根目录 AGENTS.md。
2. 阅读 docs/superpowers/plans/2026-07-29-ngsviz-ai-friendly-phased-prompts.md。
3. 阅读本 Phase 列出的真源文件和现有实现。
4. 检查 git status，保护用户已有修改。

执行规则：
- 只实现当前 Phase，不实现后续 Phase。
- 严格使用 TDD：先写失败测试，运行确认失败，再写最小实现。
- 不修改 Java/R 科学计算行为。
- 不覆盖原始数据和现有示例结果。
- 遇到科学参数、现有接口或文档不一致时，停止修改并向我提问，不自行选择。
- 完成后运行本 Phase 全部测试和相关回归测试。
- 更新 log/，一个 Phase 只创建一个独立 Git commit。
- 最终报告：改了什么、测试证据、未解决问题、下一 Phase 是否具备前置条件。
```

---

## Phase 0 — AI 协作基础设施与基线冻结

### 目标

让后续每轮 Codex 都遵守同一套边界，并记录升级前 ngsViz 的可运行基线。本阶段不得创建 Agent Adapter 功能。

### Context

参考项目先建立 AI “宪法”。ngsViz 已有 `AGENTS.md`，因此本阶段不是重复创建规则，而是补充 AI 友好升级的真源入口、基线检查和工作目录约定。

### Prompt

```text
请完成 ngsViz AI 友好升级的 Phase 0，仅建立协作基础设施和基线，不实现配置解析、环境检查或运行器。

必须先阅读：
- AGENTS.md
- README.md 的 CLI、数据库、计算结果和绘图部分
- audit/ai_friendly_agent_feature_assessment_2026-07-29.md
- docs/superpowers/plans/2026-07-29-ngsviz-ai-friendly-phased-prompts.md

任务：
1. 在 docs/ai-friendly/ 创建 00_项目边界与协作规则.md。
2. 文档明确三层职责：Agent Guide、Python Agent Adapter、Java/R computation。
3. 原样记录七条不可推翻铁律。
4. 建立“唯一功能真源”顺序：
   - 本 Phase 计划
   - docs/ai-friendly/ 下三份契约文档
   - README 和现有 Java/R 接口
   - 若文档冲突，停止并询问用户。
5. 建立目录职责表，规划 agent/ngsviz_agent、agent/scripts、agent/tests、agent/examples。
6. 只读记录当前基线：
   - git commit
   - Java、R、Maven 版本
   - JAR 是否存在
   - SQLite PRAGMA integrity_check
   - hg19/hg38/mm39 支持组合
   - 示例 BAM/BAI 是否存在
   - mvn test 结果
7. 把基线写入 audit/ai_friendly_phase0_baseline.md。

禁止：
- 不新增 Python 依赖。
- 不创建 agent 代码。
- 不修改 Java、R、README、数据库或示例结果。

验证：
- git diff --check
- rg -n "七条不可推翻的铁律|唯一功能真源|Python Agent Adapter" docs/ai-friendly/00_项目边界与协作规则.md
- sqlite3 -readonly database/genomeCoordinate.db "PRAGMA integrity_check;"
- mvn test

完成后更新 log/ 并提交：
docs(agent): freeze AI-friendly upgrade baseline
```

### 预期产出

- `docs/ai-friendly/00_项目边界与协作规则.md`
- `audit/ai_friendly_phase0_baseline.md`
- Phase 0 日志与独立 commit

### 人工确认点

- [ ] 七条铁律是否符合你的真实意图
- [ ] Java/R 是否继续作为唯一科学计算真源
- [ ] 是否接受 Python 作为外部适配层
- [ ] 基线测试是否真实通过

---

## Phase 1 — 三份真源文档

### 目标

先用文档固定使用场景、运行配置和结果验收，后续代码只能实现这三份文档中已经定义的功能。

### Context

参考项目把复杂实现拆成真源文档。ngsViz 对应的三份文档不是算法设计，而是 Agent 与现有 Java/R 之间的接口法律。

### Phase 1A Prompt — 使用场景与交互边界

```text
请执行 Phase 1A，只编写使用场景文档，不写代码。

创建 docs/ai-friendly/01_使用场景与交互边界.md，包含：

第 0 章：功能定位
- AI 是受约束的分析编排器，不是 coverage 计算器。

第 1 章：支持的输入模式
- 单个 signal BAM
- signal BAM + control BAM
- 现有 txt/csv 批量输入模式
- 自定义 BED
- 内置 SQLite annotation

第 2 章：用户需求字段
- 必须明确：title、genome、region、signal/input、output
- 条件必填：control、custom BED、strand、fragment length、scale ratio
- 可使用程序默认值的字段
- 不允许 AI 推断的字段

第 3 章：交互流程
- 信息提取
- 缺失项提问
- 配置预览
- 预检查
- dry-run
- 用户确认
- 实际运行
- 结果验收
- 解读

第 4 章：失败与暂停条件
- genome/region/database 组合不存在
- BAM/BAI 缺失
- BAM header 与 genome 不一致
- control 和 normalization 语义不清
- 输出目录已有同名结果
- Java/R 版本或包缺失

第 5 章：解读边界
- 可以描述文件、矩阵规模、数值范围、全零比例、图片是否生成
- 不自动将 coverage 模式解释为生物学机制

完成后只提交文档和日志。
```

### Phase 1B Prompt — 运行配置契约

```text
请执行 Phase 1B，只编写运行配置契约，不写解析代码。

创建 docs/ai-friendly/02_运行配置契约.md。

必须定义 config.yaml 的完整层级：
- version
- tool
- analysis
- options
- workflow

对每个字段用表格记录：
- YAML 路径
- 类型
- 是否必填
- 默认值来源
- 映射到的 Java/R 参数
- 合法范围
- 错误等级
- 是否需要人工确认

必须明确以下语义：
- signal_bam 与 control_bam 如何转换为 -I
- system_config 如何转换为 -CP
- database 如何转换为 -D
- custom_bed 如何转换为 -BD
- null 值不生成 CLI flag
- center_mode、strand_specific、scale_ratio 的组合限制
- 所有相对路径相对于项目根目录解析
- input_config.yaml 与 resolved_config.yaml 的区别
- dry_run=true 不启动 Java/R

附一份完整单样本示例和一份 signal+control 示例。

遇到 README、Java 默认值或 R 参数之间不一致时，不替用户决定；在文档末尾列为“需要用户裁决的问题”。
```

### Phase 1C Prompt — 结果验收契约

```text
请执行 Phase 1C，只编写结果验收契约，不写检查器代码。

创建 docs/ai-friendly/03_结果验收契约.md。

定义三层状态：
1. process_status：Java/R 是否正常退出
2. artifact_status：必需文件是否存在且非空
3. data_quality_status：CSV/JSON/图片内容是否满足规则

定义必需产物：
- *_coverage_matrix_heatmap.csv
- *_gene_read_count.csv
- *_NGSViz_plotSetting.json
- run_plot=true 时的绘图文件

定义检查指标：
- CSV 行列数
- 列数一致性
- 数值可解析
- NaN/Inf
- 全零矩阵
- 非零比例
- 最小值/最大值
- read count 与 coverage 行数一致
- JSON OutputFile 路径有效
- coverage 数值列数必须等于 JSON 中
  2 × flanking_region_datapoints + middle_datapoints
- 图片存在且大小大于 0

定义状态枚举：
- completed
- completed_with_warnings
- failed

定义 result_report.json 的字段结构，以及中文 result_report.md 的章节结构。
解释部分不得包含未经验证的生物学机制判断。
```

### Phase 1 总验收

- [ ] 三份文档之间字段名称一致
- [ ] 每个 Java/R 参数有且只有一个 YAML 映射
- [ ] 默认值冲突已列出并由你裁决
- [ ] Phase 1 未新增执行代码

---

## Phase 2 — YAML 配置与 Schema 模块

### 目标

把 Phase 1B 的配置契约变成可机器验证的 YAML schema 和 Python loader。本阶段不检查环境、不访问数据库、不执行 Java/R。

### Prompt

```text
请实现 Phase 2：YAML 配置与 schema 模块。

必须阅读：
- docs/ai-friendly/00_项目边界与协作规则.md
- docs/ai-friendly/01_使用场景与交互边界.md
- docs/ai-friendly/02_运行配置契约.md

只允许创建：
- agent/requirements.txt
- agent/config.schema.json
- agent/examples/config.single.yaml
- agent/examples/config.signal_control.yaml
- agent/ngsviz_agent/__init__.py
- agent/ngsviz_agent/config.py
- agent/tests/test_config.py
- 本 Phase 日志

接口：
- load_config(path: Path) -> dict[str, object]
- validate_config(data: dict[str, object], schema_path: Path) -> list[str]
- resolve_config_paths(data: dict[str, object], project_root: Path) -> dict[str, object]

实现要求：
- requirements.txt 固定为 PyYAML>=6.0,<7 和 jsonschema>=4.23,<5。
- 使用 yaml.safe_load。
- 使用 jsonschema Draft 2020-12。
- 一次返回全部 schema 错误，按字段路径排序。
- 禁止未知字段。
- 输入 dict 不得被原地修改。
- 相对路径统一相对于 project_root 解析。
- 本阶段不判断路径是否存在。

先写失败测试，至少覆盖：
- 两份示例配置通过
- 缺少 genome 失败
- region 非法枚举失败
- num_datapoints 小于 100 失败
- 未知字段失败
- null 可选字段通过
- 路径解析不修改原始对象

验证：
PYTHONPATH=agent python -m unittest agent/tests/test_config.py -v
git diff --check

完成后更新日志并提交：
feat(agent): add validated YAML configuration
```

### 预期产出

- 可验证的 YAML 配置
- 两个可复制的用户模板
- 不产生任何真实分析输出

### 人工确认点

- [ ] YAML 字段是否容易理解
- [ ] 是否保留 txt/csv 批量模式在首版
- [ ] 默认值是否与 Java、README 一致

---

## Phase 3 — Check_environment 模块

### 目标

只读判断当前机器能否运行指定配置，不安装依赖、不创建结果目录。

### Prompt

```text
请实现 Phase 3：Check_environment。

只允许创建：
- agent/ngsviz_agent/environment.py
- agent/scripts/01_check_environment.py
- agent/tests/test_environment.py
- 本 Phase 日志

接口：
- check_environment(config: dict[str, object]) -> dict[str, object]

返回结构必须包含：
- status: passed | failed
- checks: dict
- errors: list[str]
- warnings: list[str]

检查：
1. java -version 可执行并记录版本。
2. Rscript --version 可执行并记录版本。
3. JAR 和 system config 存在且为普通文件。
4. signal BAM、可选 control BAM 存在。
5. 每个 BAM 有可识别的 BAI。
6. custom BED 非空时存在且可读。
7. 输出目录最近已存在的父目录可写。
8. run_plot=true 时，以只读方式检查绘图依赖包。

安全约束：
- 不调用 install.packages、conda、pip 或网络。
- 不创建输出目录。
- 所有 subprocess 使用参数数组。
- CLI 输出 JSON；passed 返回 0，failed 返回 2。

测试必须 mock 外部命令，覆盖：
- 缺少 JAR
- 缺少 BAM
- 缺少 BAI
- Java 不可执行
- R 包缺失
- run_plot=false 时跳过 R 包检查
- 完整环境通过

验证：
PYTHONPATH=agent python -m unittest agent/tests/test_environment.py -v
PYTHONPATH=agent python agent/scripts/01_check_environment.py agent/examples/config.single.yaml
git diff --check

完成后更新日志并提交：
feat(agent): add environment preflight
```

### 人工确认点

- [ ] 缺少绘图包是否应阻止只运行 Java
- [ ] 警告和错误的划分是否符合使用习惯

---

## Phase 4 — Check_db_and_input 模块

### 目标

在运行前验证 SQLite annotation 和 BAM header，提前发现 genome、染色体命名和输入组合错误。

### Phase 4A Prompt — SQLite 数据库检查

```text
请实现 Phase 4A：只读 SQLite 数据库检查。

只允许创建：
- agent/ngsviz_agent/database.py
- agent/scripts/02_check_database.py
- agent/tests/test_database.py
- 本阶段日志

接口：
- resolve_database_path(config: dict[str, object]) -> Path
- check_database(config: dict[str, object]) -> dict[str, object]

要求：
- 从 NGSViz_setting.json 解析 db_path。
- 使用 SQLite URI mode=ro。
- 执行 PRAGMA integrity_check。
- 读取 defaultTbl。
- 验证 genome + database + region 组合。
- 验证 CoordinateTblName 对应表存在。
- 返回 supported.genomes、databases、regions、biotypes。
- 不修改、复制或 merge 数据库。

测试使用临时 SQLite，覆盖：
- integrity ok
- 缺少 defaultTbl
- 非法组合
- coordinate table 缺失
- 相对 db_path

验证：
PYTHONPATH=agent python -m unittest agent/tests/test_database.py -v
PYTHONPATH=agent python agent/scripts/02_check_database.py agent/examples/config.single.yaml

完成后提交：
feat(agent): add read-only database preflight
```

### Phase 4B Prompt — BAM 与 BED 科学输入检查

开始 Phase 4B 前必须由你选择 BAM header 读取方式：

- **方案 A（推荐）**：增加 `pysam>=0.23,<0.24`，Python 适配层可直接做小型 fixture 测试。
- **方案 B**：新增 Java/htsjdk 只读检查入口，避免 Python 新依赖，但需要修改 Java 接口并增加 Java 测试。

未得到你的选择前，Codex 必须停止在 Phase 4A，不得自行决定。

```text
请实现 Phase 4B：BAM/BED 输入一致性检查。

允许创建或修改：
- agent/ngsviz_agent/input_check.py
- agent/scripts/03_check_input.py
- agent/tests/test_input_check.py
- agent/requirements.txt（只有用户选择方案 A 时增加 pysam>=0.23,<0.24）
- Java/htsjdk 检查入口及其测试（只有用户选择方案 B 时创建）
- 本阶段日志

接口：
- check_bam_header(bam_path: Path, genome: str) -> dict[str, object]
- check_bed_file(bed_path: Path) -> dict[str, object]
- check_analysis_input(config: dict[str, object]) -> dict[str, object]

检查：
- BAM 可打开、header 非空、至少一个 reference。
- BAI 与 BAM 匹配并可查询。
- signal/control reference 命名风格一致。
- 检测 chr1 与 1 风格并记录。
- BAM reference 与所选 genome 的数据库染色体存在交集。
- BED 至少 3 列。
- BED start/end 为整数，start >= 0，end > start。
- 检测但不自动修正疑似 1-based BED。
- 检测重复、短区间和超出染色体范围风险。

遇到以下情况必须返回需要用户确认，不得自动修正：
- signal/control genome 风格不一致
- BAM 与数据库染色体交集过低
- BED 坐标基准不明确

测试使用小型 fixture，不读取或改写 Samples 原始 BAM。

验证：
PYTHONPATH=agent python -m unittest agent/tests/test_input_check.py -v
git diff --check

完成后提交：
feat(agent): validate BAM and BED inputs
```

### Phase 4 总人工确认

- [ ] BAM/genome 风险提示是否过严或过松
- [ ] BED 坐标不明确时是否坚持阻止运行
- [ ] 是否接受首版引入 pysam；若不接受，改为调用现有 Java/htsjdk 只读检查

---

## Phase 5 — Build_running_configuration 与 dry-run

### 目标

把已验证配置转换为确定性的 Java/R 命令，保存 resolved configuration，形成第一个可安全试用的版本。

### Prompt

```text
请实现 Phase 5：Build_running_configuration 和 dry-run。

只允许创建：
- agent/ngsviz_agent/commands.py
- agent/ngsviz_agent/preflight.py
- agent/scripts/04_build_running_configuration.py
- agent/tests/test_commands.py
- agent/tests/test_preflight.py
- 本 Phase 日志

接口：
- build_java_command(config: dict[str, object]) -> list[str]
- build_plot_command(config: dict[str, object]) -> list[str]
- run_preflight(config_path: Path) -> dict[str, object]
- write_preflight_bundle(result: dict[str, object], output_dir: Path) -> Path

Java 必填映射：
- genome -> -G
- region -> -R
- title -> -T
- signal/control -> -I
- output_dir -> -O
- system_config -> -CP

可选参数只能通过显式白名单映射：
- database -> -D
- analysis_type -> -A
- biotype -> -B
- flank_region -> -F
- flank_factor -> -N
- num_datapoints -> -DP
- scale_ratio -> -S
- mapping_quality -> -MQ
- fragment_length -> -FL
- cores -> -P
- batch_size -> -BS
- bin_method -> -BM
- strand_specific -> -SS
- center_mode -> -CM
- custom_bed -> -BD

要求：
- null 不生成 flag。
- unknown field 不进入命令。
- signal+control 精确转换为 signal:control。
- 生成 input_config.yaml、resolved_config.yaml、commands.json、environment.json、database.json、input_check.json。
- dry-run 成功状态为 ready，任何检查失败状态为 blocked。
- 本 Phase 永远不调用 Java/R 计算。

测试覆盖：
- 全部 flag 映射
- null 省略
- boolean 序列化
- 含空格路径仍是单个数组元素
- control BAM 拼接
- 任一 preflight 失败则 blocked
- ready 时命令稳定可重复

验证：
PYTHONPATH=agent python -m unittest agent/tests/test_commands.py agent/tests/test_preflight.py -v
PYTHONPATH=agent python agent/scripts/04_build_running_configuration.py agent/examples/config.single.yaml

完成后提交：
feat(agent): add deterministic dry-run workflow
```

### dry-run 人工验收清单

- [ ] 最终 genome、region、BAM、control、strand、fragment length、normalization 正确
- [ ] Java 命令与手工运行命令一致
- [ ] R 命令只指向本次输出目录
- [ ] dry-run 没有生成 coverage、read count 或图片
- [ ] 同一 YAML 连续执行两次得到相同命令

---

## Phase 6 — Run_tool 安全执行模块

### 目标

只有 dry-run 为 ready 且用户明确要求运行时，才顺序执行 Java 和 R。

### Prompt

```text
请实现 Phase 6：Run_tool。

只允许创建：
- agent/ngsviz_agent/runner.py
- agent/scripts/05_run_tool.py
- agent/tests/test_runner.py
- 本 Phase 日志

接口：
- run_command(command: list[str], log_path: Path) -> dict[str, object]
- run_workflow(config_path: Path, confirmed: bool = False) -> dict[str, object]

状态机：
- blocked
- ready
- running_java
- running_plot
- failed
- completed_pending_validation

要求：
1. confirmed=false 时只返回 ready，不执行。
2. confirmed=true 前重新执行完整 preflight。
3. subprocess.run 使用 shell=False。
4. stdout/stderr 都保存到阶段日志。
5. Java 非零时停止，不运行 R。
6. run_plot=false 时跳过 R。
7. R 非零时标记 failed。
8. Java/R 成功后只能标记 completed_pending_validation，不得直接标记 completed。
9. 不删除或覆盖已有同名结果；检测到已有结果时停止并询问用户选择新目录。

运行目录保存：
- run_state.json
- java.log
- plot.log
- exit_codes.json
- 前序 preflight bundle

测试必须 mock Java/R，覆盖：
- 未确认不执行
- preflight blocked 不执行
- Java 成功后运行 R
- Java 失败后不运行 R
- run_plot=false
- R 失败
- 已有结果阻止覆盖

验证：
PYTHONPATH=agent python -m unittest agent/tests/test_runner.py -v
先只使用 dry-run 示例，不运行真实 Samples。

完成后提交：
feat(agent): add guarded Java and R execution
```

### 人工确认点

- [ ] 是否接受 `confirmed` 双重确认设计
- [ ] 同名输出默认阻止覆盖是否符合预期
- [ ] 在你明确批准前不得运行真实 smoke test

---

## Phase 7 — Check_result 与运行解读

### 目标

把“进程运行完成”升级为“产物和数据质量通过验收”，并生成机器可读与人类可读报告。

### Phase 7A Prompt — 结果检查器

```text
请实现 Phase 7A：Check_result。

只允许创建：
- agent/ngsviz_agent/result_check.py
- agent/scripts/06_check_result.py
- agent/tests/test_result_check.py
- 本阶段日志

接口：
- check_results(config: dict[str, object], run_state: dict[str, object]) -> dict[str, object]

严格实现 docs/ai-friendly/03_结果验收契约.md。

检查：
- 必需 CSV/JSON/图片存在且非空
- CSV header、行列数、列数一致
- coverage 数值可解析且无 NaN/Inf
- 非零比例、最小值、最大值
- all-zero 为 warning
- read count 行数匹配
- JSON OutputFile 指向真实文件
- coverage 数值列数等于
  2 × flanking_region_datapoints + middle_datapoints
- run_plot=true 时图片存在且非空

状态：
- 任一必需文件缺失或格式错误 -> failed
- 只有非致命质量问题 -> completed_with_warnings
- 全部通过 -> completed

测试全部使用临时 fixture，覆盖正常、缺失、空文件、NaN、Inf、全零、行数不匹配和 JSON 路径失效。

验证：
PYTHONPATH=agent python -m unittest agent/tests/test_result_check.py -v

完成后提交：
feat(agent): validate ngsViz result artifacts
```

### Phase 7B Prompt — 报告与解读

```text
请实现 Phase 7B：运行报告与有限解读。

只允许创建：
- agent/ngsviz_agent/report.py
- agent/tests/test_report.py
- 本阶段日志

接口：
- build_report(result: dict[str, object]) -> dict[str, object]
- write_json_report(report: dict[str, object], path: Path) -> None
- write_markdown_report(report: dict[str, object], path: Path) -> None

result_report.json 必须包含：
- schema_version
- run_status
- tool_versions
- resolved_parameters
- commands
- exit_codes
- artifacts
- metrics
- warnings
- errors
- reproducibility

result_report.md 使用中文，章节为：
1. 运行结论
2. 输入与关键参数
3. 产物清单
4. 数据质量
5. 警告与错误
6. 复现信息
7. 解读边界

允许的解读：
- coverage matrix 的规模、数值范围、非零比例
- 样本和产物是否完整
- 是否存在全零、缺失或异常值

禁止的解读：
- 不从 coverage 图直接断言基因调控机制
- 不声称组间差异有统计学显著性
- 不替代实验或独立数据验证

测试验证 JSON 可解析、Markdown 章节完整、错误状态不会写成成功。

完成后提交：
feat(agent): generate reproducible run reports
```

### Phase 7 人工确认点

- [ ] all-zero 应作为 warning 还是 failure
- [ ] 图形缺失是否在 run_plot=true 时一律 failure
- [ ] 报告中的指标是否足够支持你判断结果可信度

---

## Phase 8 — ngsViz Agent Guide 与 Codex Skill

### 目标

让 Codex 按固定决策顺序调用已完成模块，但不赋予其跳过确认或修改科学参数的权限。

### Prompt

```text
请实现 Phase 8：ngsViz Agent Guide 和项目级 Codex Skill。

只允许创建：
- agent/ngsViz_agent.md
- .agents/skills/ngsviz-agent/SKILL.md
- agent/tests/test_agent_contract.py
- 本 Phase 日志

Guide 工作流必须固定为：
User_request
→ Extract_requirements
→ Ask_missing_scientific_parameters
→ Build_config
→ Check_environment
→ Check_db_and_input
→ Build_running_configuration
→ Show_dry_run
→ Ask_run_confirmation
→ Run_tool
→ Check_result
→ Result_and_interpretation

Skill 必须规定：
- 未知科学参数必须向用户提问。
- 首次生成配置默认 dry_run=true。
- 只有用户明确要求实际运行才能调用 Run_tool。
- 不得跳过结果检查。
- 解释只能读取 result_report.json。
- 用户只要求配置或检查时，不扩大到实际运行。

为以下对话写契约测试：
1. 用户只给 BAM：Agent 必须询问 genome、region、output。
2. 用户给齐参数但未要求运行：只到 dry-run。
3. 用户明确要求运行：仍需 preflight ready。
4. 数据库组合非法：停止并给出支持值。
5. 结果缺失：不得报告成功。

验证：
PYTHONPATH=agent python -m unittest agent/tests/test_agent_contract.py -v
rg -n "Check_environment|Check_db_and_input|Build_running_configuration|Run_tool|Check_result" agent/ngsViz_agent.md

完成后提交：
docs(agent): add guided Codex workflow
```

### 手工对话验收

使用下面三个 Prompt 分别测试，不执行真实计算：

```text
使用 ngsviz-agent skill，分析 Samples/K562_H3K4me3-ENCFF752MYF_5p.bam。
```

Expected: 询问缺失的 genome、region、output 等信息。

```text
使用 ngsviz-agent skill，为示例 BAM 生成 hg19 TSS 配置，只做 dry-run。
```

Expected: 生成配置、执行预检查、展示命令，不启动 Java/R。

```text
解释一个不存在的输出目录。
```

Expected: 返回结果缺失，不伪造运行结论。

---

## Phase 9 — 端到端回归、README 与发布验收

### 目标

使用现有 K562 示例完成一次经你批准的真实 smoke test，验证从 YAML 到结果报告的完整链路。

### Prompt

```text
请执行 Phase 9：端到端回归和发布验收。

开始前：
- 先展示 smoke test 的 resolved config 和完整 dry-run。
- 等待我明确批准后才能启动真实 Java/R。
- 输出只能写入 agent_output/smoke_hg19_tss。

允许创建或修改：
- agent/tests/test_workflow.py
- README.md 的“AI 友好工作流”章节
- .gitignore
- audit/ai_friendly_end_to_end_acceptance.md
- 本 Phase 日志

自动测试：
- PYTHONPATH=agent python -m unittest discover -s agent/tests -v
- mvn test
- git diff --check

真实 smoke test：
- input: Samples/K562_H3K4me3-ENCFF752MYF_5p.bam
- genome: hg19
- region: tss
- output: agent_output/smoke_hg19_tss
- 其他参数必须来自 resolved config 并在执行前展示

验收：
- 环境、数据库、输入检查全部 passed
- Java/R exit code 为 0
- result status 为 completed 或 completed_with_warnings
- coverage/read-count/JSON/图片存在
- result_report.json 与 result_report.md 存在
- 原始 Samples 和现有 Samples/Result 未修改

README 说明：
- 安装 Agent Adapter 依赖
- 复制 YAML 模板
- 运行 preflight
- 运行 dry-run
- 明确确认后实际运行
- 检查结果
- 使用 Codex Skill

.gitignore 增加：
- agent_output/
- agent/**/__pycache__/

完成后提交：
test(agent): validate AI-friendly workflow end to end
```

### 完整验收清单

- [ ] 所有 Python 测试通过
- [ ] Maven 测试通过
- [ ] 同一配置两次 dry-run 命令完全一致
- [ ] dry-run 不启动 Java/R
- [ ] Java 失败时不运行 R
- [ ] 结果缺失时不报告成功
- [ ] 原始数据和示例结果未被覆盖
- [ ] README 参数与 schema、Java、R 一致
- [ ] 工作区清洁

---

## 四、可选增强模块

这些模块不属于首版，只有 Phase 0–9 稳定后再单独立项：

### Optional A — 分析模板

- ChIP-seq TSS
- ChIP-seq gene body
- CUT&Tag TSS
- RNA-seq gene body
- 自定义 BED center mode

模板只预填参数，不绕过用户确认。

### Optional B — 批量样本配置

将现有 txt/csv 输入模式提升为 YAML samples 数组，并明确 sample/group/control 对应关系。

### Optional C — 运行进度

只解析 Java/R 日志形成阶段进度，不通过日志文本推测科学结果。

### Optional D — 结果对比

对多个已通过验收的结果报告汇总文件、参数和矩阵质量，不自动做未定义的统计检验。

---

## 五、Phase 执行记录模板

每完成一个 Phase，在日志中使用：

```markdown
# Phase N 执行记录

- 目标：
- 使用的 Prompt：
- 修改文件：
- 失败测试证据：
- 通过测试证据：
- 人工确认：
- 未解决问题：
- Git commit：
- 下一 Phase 前置条件：满足 / 不满足
```

---

## 六、推荐手动执行方式

1. 新开一个 Codex 任务。
2. 粘贴“所有模块共用的 Codex 前置 Prompt”。
3. 再粘贴当前 Phase 的专用 Prompt。
4. Codex 若提出科学参数或接口冲突问题，先讨论，不允许它继续猜测。
5. 检查测试和 diff。
6. 确认本 Phase commit 后关闭该任务。
7. 手动进入下一个 Phase。

首轮从 Phase 0 开始。不要一次把 Phase 0–9 全部交给 Codex 执行。
