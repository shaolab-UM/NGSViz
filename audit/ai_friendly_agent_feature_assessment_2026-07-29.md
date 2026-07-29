# ngsViz AI 友好使用功能评估

## 结论

- 可行，整体难度为中等，不需要重写 Java 计算核心或 R 绘图模块。
- 最合适的首版是新增独立 `agent/` 适配层：`用户需求 → YAML 配置 → 预检查 → Java/R 执行 → 结果验收 → 结构化报告`。
- 不建议首版直接让大模型拼接 CLI 参数，也不建议先修改科学计算逻辑。
- 单人使用 AI 辅助开发时：
  - 可演示原型：2–3 人日。
  - 可实际使用的 MVP：6–9 人日。
  - 增加 MCP、异步任务和回归测试后的投稿级版本：12–18 人日，约 2–4 周。
- 预计 70%–80% 为新增文件，20%–30% 为现有入口的小范围兼容修改。

## 现有基础

- Java 已有统一主入口，依次执行参数解析和主计算流程：`src/main/java/com/NGSViz/NGSViz.java:18-40`。
- CLI 已覆盖 genome、region、BAM、输出目录、数据库、并行数、bin 方法等主要参数：`src/main/java/com/NGSViz/configSet/GetInputParameterValue.java:11-63`。
- 现有输入检查已覆盖必填参数、文件存在性、数据库枚举值和部分数值范围：`src/main/java/com/NGSViz/configSet/CheckLegal2InputParameter.java:16-43`、`src/main/java/com/NGSViz/configSet/CheckLegal2InputParameter.java:168-189`。
- SQLite 的 `defaultTbl` 已能提供 genome、DB、region、analysis type、biotype 和默认 flank size，可直接作为 `Check_db` 的信息源。
- Java 计算会输出 coverage matrix、read count 和绘图 JSON：`src/main/java/com/NGSViz/coverageCalculator/MainCalculator.java:118-131`。
- R 已有以 JSON 目录为唯一参数的绘图入口：`lib/ngsVizPlotMain.R:24-64`。
- Shiny 已经包含一版参数列表到 JAR 命令的转换逻辑，可参考但不宜原样作为 Agent 执行器：`lib/processCalculateParameters.R:3-44`。

## 主要缺口

### 1. 缺少稳定的机器接口

- CLI 只有短参数，没有 `--help`、`--version`、`--dry-run`、JSON 状态输出或正式参数 schema。
- 未知参数不会被明确拒绝；当前解析器只是对每个位置逐项尝试已知参数：`src/main/java/com/NGSViz/configSet/GetInputParameterValue.java:11-63`。
- 多处直接使用 `System.exit`，错误格式和退出码尚未形成统一协议。

### 2. 配置不是运行契约

- 当前 `NGSViz_setting.json` 只保存工具路径、数据库路径和 Java 路径，不包含一次分析的完整参数。
- Java 生成的 JSON 是下游绘图配置和输出索引，不是上游运行配置：`src/main/java/com/NGSViz/configSet/GenerateJsonConfig.java:89-172`。
- 因此需要新增 `config.yaml` 及 schema，并明确默认值来源、用户显式值和最终解析值。

### 3. 结果检查不足

- 当前成功提示主要依赖进程输出：`src/main/java/com/NGSViz/NGSViz.java:37-40`。
- 应检查退出码、必需文件、CSV 行列数、数值可解析性、全零矩阵、NaN/Inf、样本数量、JSON 中路径有效性和图片是否生成。
- Shiny 当前拿到执行状态后仍直接设置 “Operation completed”，没有判断非零状态：`lib/shiny/server_builder.R:193-202`。

### 4. MCP 会引入异步任务复杂度

- BAM 分析可能长时间运行，MCP 工具不应一直阻塞。
- 投稿级实现需要 `submit_run`、`get_run_status`、`read_run_log`、`cancel_run` 和 `check_result`，并保存任务状态。
- 首版若只面向本机 Codex/Claude，可先使用 Skill 调用稳定 wrapper，MCP 后置。

## 推荐架构

```text
用户需求
  ↓
ngsViz Agent Guide / Skill
  ↓
build_config
  ↓
config.yaml + schema validation
  ↓
check_environment + check_database + dry_run
  ↓
run_calculation → run_plot
  ↓
check_result
  ↓
result_report.json + 中文解读
```

建议新增：

```text
agent/
├── ngsViz_agent.md
├── config.schema.json
├── examples/
│   └── config.example.yaml
├── ngsviz_agent/
│   ├── environment.py
│   ├── database.py
│   ├── config.py
│   ├── runner.py
│   ├── result_check.py
│   └── report.py
├── mcp_server.py
└── tests/
```

## 建议的 MCP 工具

- `check_environment`：检查 Java、R、JAR、R 包、磁盘空间和可写输出目录。
- `check_database`：运行 SQLite integrity check，返回可用 genome、DB、region 和 biotype。
- `build_run_config`：把结构化需求转换为 YAML；不直接执行。
- `validate_run_config`：应用 schema、路径、BAM/BAI、染色体命名和参数组合检查。
- `submit_run`：以参数数组执行，不通过 shell 拼接命令，返回 `run_id`。
- `get_run_status`：返回状态、阶段、退出码和最近日志。
- `check_result`：返回文件清单、矩阵统计、警告和失败原因。

## 分阶段工作量

| 阶段 | 主要内容 | 估算 |
|---|---|---:|
| 原型 | Agent guide、单样本 YAML、环境/DB 检查、命令生成、示例运行 | 2–3 人日 |
| MVP | schema、dry-run、安全执行、Java/R 串联、结果检查、单元测试、中文报告 | 4–6 人日 |
| MCP | stdio MCP server、工具定义、任务状态、日志读取 | 2–4 人日 |
| 投稿级加固 | 失败场景、容器测试、跨平台路径、回归样例、文档一致性 | 4–6 人日 |

## 实施边界

- 第一阶段不修改 coverage、坐标、strand、binning 或 normalization 行为。
- `config.yaml` 只映射现有参数；新增科学参数必须单独评审。
- Agent 的“解读”只描述运行质量、矩阵和图形产物，不自动生成未经验证的生物学结论。
- 所有实际命令先经过 `dry-run` 展示，保留最终 YAML、命令数组、软件版本、日志、退出码和结果清单。

## 风险排序

1. 高：自然语言到参数映射错误，尤其 genome、region、single-end fragment length、strand 和 normalization。
2. 高：进程退出码与输出文件不一致，导致假成功。
3. 中：长任务在 MCP 调用中超时或失去状态。
4. 中：系统配置中的绝对路径影响容器和其他用户环境。
5. 低：Java/R 计算模块接入；现有入口已经具备。
