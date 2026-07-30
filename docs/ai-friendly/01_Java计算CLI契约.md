# ngsViz Java 计算 CLI 契约

## 适用范围

本文档是 Java 原生计算接口的真源契约，约束后续 CLI 分发、`ComputeRequest`、验证、计算执行和 `compute_manifest.json` 实现。

- Java 只负责计算，不启动 `Rscript`，也不检查或生成图片。
- 旧短参数入口与 JSON 请求入口都归一化为单样本 `ComputeRequest`。
- 两种入口共享同一个 validator 和同一个 `MainCalculator` 调用点，不复制 coverage、binning、normalization 或其他科学计算逻辑。
- 每个 `ComputeRequest` 必须且只能描述一个 signal sample，可选一个对应 control BAM。
- `sample_id`、`sample_name`、`group_name` 仅用于输出元数据、manifest 和 plot-setting 标识，不得改变 coverage 算法。

## 命令

### `describe`

```bash
java -jar NGSViz-1.3.jar describe --format json
```

- 输出 Java 计算 CLI 的版本、子命令、请求字段、支持值、旧 flags 和退出码。
- 只接受 `--format json`；不读取计算请求，不访问 BAM、数据库或输出目录。
- 成功时返回 exit code `0` 和一个 JSON response envelope。

### `validate-compute`

```bash
java -jar NGSViz-1.3.jar validate-compute --request /absolute/path/compute_request.json
```

- 直接从 JSON 构造 `ComputeRequest`，不得先把 JSON 字段转换成旧 flags。
- 执行请求结构、参数组合、系统配置、SQLite、BAM/BAI 和可选 BED 的只读验证。
- 不创建 `output_dir`，不写 manifest、日志或其他文件，不调用 `MainCalculator`，不启动 R。
- 输出解析后的参数、支持值、warnings 和 errors，但不输出可供执行的完整旧 flags 命令。

### `run-compute`

```bash
java -jar NGSViz-1.3.jar run-compute --request /absolute/path/compute_request.json
```

- 运行前必须调用与 `validate-compute` 相同的 validator。
- 验证失败时不得创建输出目录或调用 `MainCalculator`。
- 每次只计算一个 signal sample，可选一个 control BAM。
- 只调用现有 Java `MainCalculator` 计算，不启动 R。
- 成功后验收 coverage matrix CSV、read-count CSV 和 plot-setting JSON，并写入 `output_dir/compute_manifest.json`。

## 输入模式与互斥规则

Java CLI 有且只有以下两种计算输入模式：

1. 兼容模式：第一个参数以 `-` 开头，使用旧短参数。
2. JSON 模式：使用 `validate-compute --request <json>` 或 `run-compute --request <json>`。

规则如下：

- JSON 模式只接受一个 `--request <json>`；不得追加任何旧计算 flag。
- 旧 flags 与 `--request` 混用时，不定义覆盖优先级，必须在读取请求前失败并返回 exit code `2`。
- JSON 请求不得使用 `-J`、YAML 或 YAML loader。
- AI 只能生成单样本 JSON，并以参数数组调用 `--request <json>`；AI 不拼装完整旧 flags 命令。
- JSON 中的 `null` 不转换为“省略某个旧 flag”，而是由 `ComputeRequest` 的默认值规则直接解析。
- 未知 JSON 字段必须验证失败；顶层出现 `samples` 字段时，无论其值为何都必须返回 exit code `2`。

信息入口 `-H` 和 `-V` 保持 ngsViz 1.3 发布版兼容行为：可直接打印帮助或版本并退出，不进入计算参数解析，也不构造 `ComputeRequest`。

## `compute_request.json`

### 单样本结构

```json
{
  "schema_version": "1.0",
  "sample_id": "H3K4me3",
  "sample_name": "K562_H3K4me3",
  "group_name": "K562",
  "title": "K562_H3K4me3_TSS",
  "genome": "hg38",
  "region": "tss",
  "signal_bam": "/data/K562_H3K4me3.bam",
  "control_bam": null,
  "output_dir": "/results/K562_H3K4me3",
  "system_config": null,
  "database": "RefSeq",
  "analysis_type": "transcript",
  "biotype": "protein_coding",
  "flank_region": null,
  "flank_factor": 0.0,
  "num_datapoints": 100,
  "scale_ratio": null,
  "mapping_quality": 20,
  "fragment_length": 150,
  "cores": 1,
  "batch_size": 500,
  "bin_method": "mean",
  "strand_specific": "both",
  "center_mode": false,
  "custom_bed": null,
  "gene_subset": "all",
  "create_result_folder": false
}
```

请求对象中禁止出现 `samples` 数组。多样本 analysis plan 必须由外部编排器拆分为多份独立请求，再顺序启动多个 Java 进程。

### 字段定义

“必填”表示 JSON 请求必须显式提供且不得为 `null`。默认值只适用于“可选”字段缺失或为 `null` 的情况。

| 字段 | JSON 类型 | 必填 | 默认值或解析来源 | 约束与含义 |
|---|---|---:|---|---|
| `schema_version` | string | 否 | `"1.0"` | 当前只接受 `"1.0"` |
| `sample_id` | string | 是 | 无 | 单次运行的稳定标识；非空；仅作输出元数据 |
| `sample_name` | string | 是 | 无 | 样本显示名称；非空；仅作输出元数据 |
| `group_name` | string | 是 | 无 | 分组显示名称；非空；仅作输出元数据 |
| `title` | string | 是 | 无 | 分析标题和 plot-setting 文件命名来源 |
| `genome` | string | 是 | 无 | 必须存在于所选数据库 |
| `region` | string | 是 | 无 | 转为小写后验证；支持值由数据库与 validator 返回 |
| `signal_bam` | string | 是 | 无 | 一个 BAM 路径；必须可读并具有可用 BAI |
| `control_bam` | string 或 null | 否 | `null` | 最多一个 control/input BAM；非 null 时与 signal 配对归一化 |
| `output_dir` | string | 是 | 无 | 目标单样本结果目录 |
| `system_config` | string 或 null | 否 | `null` | null 时使用 JAR 相邻的 `NGSViz_setting.json` |
| `database` | string 或 null | 否 | `"RefSeq"` | 参考注释数据库类型，对应旧 `-D` |
| `analysis_type` | string 或 null | 否 | `"transcript"` | 允许 `transcript` 或 `exon` |
| `biotype` | string 或 null | 否 | `"protein_coding"` | 注释记录 biotype |
| `flank_region` | integer 或 null | 否 | 按 `region` 解析 | null 时使用 region-specific 默认值；必须 `>= 0` |
| `flank_factor` | number 或 null | 否 | `0.0` | 范围 `[0, 1]` |
| `num_datapoints` | integer 或 null | 否 | `100` | 必须 `>= 100` |
| `scale_ratio` | number 或 null | 否 | `null` | null 时执行现有 CPM；非 null 时执行现有 `signal * scale_ratio` |
| `mapping_quality` | integer 或 null | 否 | `20` | 必须 `>= 0` |
| `fragment_length` | integer 或 null | 否 | `150` | 必须 `>= 0`；沿用现有 single-end read extension 语义 |
| `cores` | integer 或 null | 否 | `1` | 必须 `>= 1` |
| `batch_size` | integer 或 null | 否 | `500` | 必须 `>= 1` |
| `bin_method` | string 或 null | 否 | `"mean"` | 允许 `mean`、`median`、`max` |
| `strand_specific` | string 或 null | 否 | `"both"` | 允许 `both`、`same`、`opposite` |
| `center_mode` | boolean 或 null | 否 | `false` | null 解析为 false |
| `custom_bed` | string 或 null | 否 | `null` | 自定义 BED 路径；BED 坐标必须为 0-based half-open |
| `gene_subset` | string 或 null | 否 | `"all"` | 对应旧 `-X`；保留现有 gene list 解释方式 |
| `create_result_folder` | boolean 或 null | 否 | `false` | 对应旧 `-NF`；true 时在 `output_dir` 下使用 `title` 子目录 |

### null 与默认值

- JSON reader 必须保留字段类型，不以字符串 `"null"` 表示 null。
- 必填字段缺失或为 null 时验证失败，不得套用默认值。
- 可选字段缺失或为 null 时，由 `ComputeRequest` 解析为上表默认值。
- `flank_region=null` 是 region-specific 默认值的显式入口：`tss`、`tes`、`genebody`、`exon`、`cgi` 为 `2000`，`enhancer` 为 `1500`，`dhs`、`bed` 为 `1000`。
- 默认值解析完成后形成 `resolved_parameters`；validator 和 `MainCalculator` 只消费这份解析结果。
- `scale_ratio=null` 必须保持现有 CPM 行为，不能隐式转换为 `1.0`。

## 旧短参数兼容契约

### 单 BAM 模式逐字段映射

| 旧 flag | 旧变量 | `ComputeRequest` 字段 | 映射规则 |
|---|---|---|---|
| `-G` | `genome` | `genome` | 原值；region/database 验证前不得改写 |
| `-R` | `region_type` | `region` | 原值，validator 中转为小写 |
| `-T` | `title` | `title` | 原值 |
| `-I` | `input_file` | `signal_bam`, `control_bam` | `signal.bam` 映射 signal；`signal.bam:control.bam` 最多拆分一次 |
| `-O` | `output_path` | `output_dir` | 原值 |
| `-CP` | `sys_config_path` | `system_config` | 空字符串或 `-` 解析为 null |
| `-D` | `DB_type` | `database` | 缺失时 `"RefSeq"` |
| `-A` | `analysis_type` | `analysis_type` | 缺失时 `"transcript"` |
| `-B` | `biotype` | `biotype` | 缺失时 `"protein_coding"` |
| `-F` | `flank_region` | `flank_region` | 未提供或旧值 `0` 时按 region-specific 默认值解析 |
| `-N` | `flank_factor` | `flank_factor` | 缺失时 `0.0` |
| `-DP` | `num_datapoints` | `num_datapoints` | 缺失时 `100`；低于 `100` 失败 |
| `-S` | `scale_ratio` | `scale_ratio` | 缺失时 null |
| `-MQ` | `min_mapq` | `mapping_quality` | 缺失时 `20` |
| `-FL` | `frag_len` | `fragment_length` | 缺失时 `150` |
| `-P` | `core_num` | `cores` | 缺失时 `1` |
| `-BS` | `BATCH_SIZE` | `batch_size` | 缺失时 `500` |
| `-BM` | `bin_method` | `bin_method` | 缺失时 `"mean"` |
| `-SS` | `strand_spec` | `strand_specific` | 缺失时 `"both"` |
| `-CM` | `CenterMode` | `center_mode` | 缺失时 false |
| `-BD` | `bedDB_path` | `custom_bed` | 缺失、空字符串或 `-` 解析为 null |
| `-X` | `genes` | `gene_subset` | 缺失时 `"all"` |
| `-NF` | `new_forder` | `create_result_folder` | 缺失时 false |

旧单 BAM 命令没有 `sample_id`、`sample_name`、`group_name` flags。兼容适配器按以下规则补充元数据：

- `sample_id`：去掉 `.bam` 后缀的 signal BAM 文件名。
- `sample_name`：保留旧直接 BAM 模式的 `"SampleName"`。
- `group_name`：保留旧直接 BAM 模式的 `"GroupName"`。

这些补充值不得参与 coverage 计算。

### TXT/CSV 旧批量输入

旧 `-I <txt|csv>` 能描述多行样本，属于必须保留的兼容入口，但不改变单样本请求约束：

- 兼容层按现有列顺序读取 `sample_name,bam,gene_subset,group_name,title`。
- 每一行独立构造一份 `ComputeRequest`；不得构造含 `samples[]` 的请求。
- `sample_id` 默认取该行 `sample_name`；若为空则验证失败。
- 命令级公共 flags 映射到每一份请求，行内 BAM、gene subset、group、title 和 sample name 覆盖对应元数据。
- 各请求必须逐个通过同一 validator，并顺序进入同一个 `MainCalculator` 调用点。
- 此兼容展开不得成为 JSON 模式的批处理接口。

### 不映射到 `ComputeRequest` 的旧 flags

| 旧 flag | 兼容行为 | 原因 |
|---|---|---|
| `-H` | 直接打印帮助并成功退出 | 信息命令，不是计算参数 |
| `-V` | 直接打印 `ngsViz 1.3` 并成功退出 | 信息命令，不是计算参数 |
| `-J` | 保留已弃用占位解析，不读取 YAML，不进入新接口 | 不属于 AI-friendly JSON 契约 |
| `-DB` | 保留现有数据库合并模式及其退出行为 | 管理操作，不是 coverage 请求字段 |

旧入口的参数名称、类型、默认值、输出命名和科学行为必须保持兼容。兼容适配器只负责构造请求，不得自行验证或计算。

## 统一验证边界

两种计算入口必须遵循：

```text
legacy flags -> LegacyComputeRequestAdapter --+
                                              +-> ComputeRequest -> ComputeValidator -> MainCalculator
JSON file ----> ComputeRequestReader ---------+
```

validator 至少检查：

- 请求只描述一个 sample，恰有一个 `signal_bam`，最多一个 `control_bam`。
- 不存在 `samples` 或其他未知字段。
- 必填字段、JSON 类型、枚举、数值范围和参数组合合法。
- `num_datapoints >= 100`。
- system config、SQLite、genome/database/region 组合和坐标表有效。
- signal/control BAM 可读、有 header、有 BAI，reference 命名风格一致。
- BAM reference 与数据库染色体存在合理交集。
- `custom_bed` 至少三列，`start >= 0`、`end > start`。
- 输出路径不会覆盖已有 ngsViz 产物。

`validate-compute` 与 `run-compute` 使用完全相同的默认值解析和 validator。`run-compute` 不得在验证后再次以另一套规则解释参数。

## `compute_manifest.json`

### Schema

```json
{
  "schema_version": "1.0",
  "status": "completed",
  "ngsviz_version": "1.3",
  "started_at": "2026-07-30T12:00:00Z",
  "finished_at": "2026-07-30T12:01:00Z",
  "duration_ms": 60000,
  "resolved_parameters": {
    "sample_id": "H3K4me3",
    "sample_name": "K562_H3K4me3",
    "group_name": "K562",
    "title": "K562_H3K4me3_TSS",
    "genome": "hg38",
    "region": "tss",
    "signal_bam": "/data/K562_H3K4me3.bam",
    "control_bam": null,
    "output_dir": "/results/K562_H3K4me3"
  },
  "sample_id": "H3K4me3",
  "sample_name": "K562_H3K4me3",
  "group_name": "K562",
  "outputs": {
    "coverage_matrices": [
      "/results/K562_H3K4me3/K562_H3K4me3_coverage_matrix_heatmap.csv"
    ],
    "read_count_files": [
      "/results/K562_H3K4me3/K562_H3K4me3_gene_read_count.csv"
    ],
    "plot_setting_file": "/results/K562_H3K4me3/K562_H3K4me3_TSS_NGSViz_plotSetting.json"
  },
  "log_file": "/results/K562_H3K4me3/NGSViz_compute.log",
  "warnings": [],
  "errors": []
}
```

字段约束：

| 字段 | 类型 | 必需 | 约束 |
|---|---|---:|---|
| `schema_version` | string | 是 | 当前为 `"1.0"` |
| `status` | string | 是 | `completed` 或 `failed` |
| `ngsviz_version` | string | 是 | 实际运行版本 |
| `started_at` | string | 是 | UTC ISO 8601 |
| `finished_at` | string | 是 | UTC ISO 8601 |
| `duration_ms` | integer | 是 | `>= 0` |
| `resolved_parameters` | object | 是 | 完整的单样本解析参数；不得含 `samples` |
| `sample_id` | string | 是 | 与 resolved parameters 一致 |
| `sample_name` | string | 是 | 与 resolved parameters 一致 |
| `group_name` | string | 是 | 与 resolved parameters 一致 |
| `outputs.coverage_matrices` | array<string> | 是 | 成功时至少一个存在且非空的 CSV |
| `outputs.read_count_files` | array<string> | 是 | 成功时至少一个存在且非空的 CSV |
| `outputs.plot_setting_file` | string 或 null | 是 | 成功时为存在且非空的 JSON |
| `log_file` | string 或 null | 是 | 计算日志路径 |
| `warnings` | array<object> | 是 | 可为空 |
| `errors` | array<object> | 是 | 可为空；失败时至少一个 |

manifest 只描述 Java 计算状态，不得包含 `visualization_status`、R 包状态或图片列表。单次 `run-compute` 只能生成一份单样本 manifest。

## JSON response envelope

新子命令的 stdout 最终只输出一个 JSON 对象：

```json
{
  "schema_version": "1.0",
  "command": "validate-compute",
  "status": "success",
  "exit_code": 0,
  "message": "Compute request is valid.",
  "data": {},
  "warnings": [],
  "errors": []
}
```

| 字段 | 类型 | 必需 | 说明 |
|---|---|---:|---|
| `schema_version` | string | 是 | response schema 版本 |
| `command` | string | 是 | `describe`、`validate-compute` 或 `run-compute` |
| `status` | string | 是 | `success` 或 `error` |
| `exit_code` | integer | 是 | 必须与进程退出码一致 |
| `message` | string | 是 | 简短、机器输出安全的英文消息 |
| `data` | object 或 null | 是 | 成功数据或已解析的安全上下文 |
| `warnings` | array<object> | 是 | 可为空 |
| `errors` | array<object> | 是 | 可为空；error 时至少一个 |

warning/error 元素统一使用：

```json
{
  "code": "REQUEST_UNKNOWN_FIELD",
  "field": "samples",
  "message": "Unknown field: samples."
}
```

计算过程的普通日志写入 `output_dir/NGSViz_compute.log`，不得污染最终 response。不得在 response 中输出 BAM 内容、数据库内容或完整旧 flags 命令。

## 退出码

| Exit code | 含义 | 典型情况 |
|---:|---|---|
| `0` | 成功 | describe 完成、请求验证通过、计算及产物验收完成 |
| `2` | 参数或请求格式错误 | 未知子命令、缺少参数、JSON 类型错误、未知字段、出现 `samples`、两种输入模式混用 |
| `3` | 环境或依赖缺失 | system config、运行环境或必要依赖缺失 |
| `4` | 计算输入无效 | 数据库、BAM/BAI、BED 或参数组合验证失败 |
| `5` | 计算执行失败 | `MainCalculator` 或其调用链抛出执行异常 |
| `6` | 产物验收失败 | coverage、read-count 或 plot-setting 产物缺失或为空 |

validator 发现多个问题时可一次返回多个 errors，但进程退出码只取最高层分类，不使用旧流程中的 `-1` 作为新子命令退出码。

## 现有实现依据

- 旧 flags 解析：`src/main/java/com/NGSViz/configSet/GetInputParameterValue.java:11-65`
- Java 字段默认值：`src/main/java/com/NGSViz/configSet/InputParameterAttributes.java:29-64`
- region-specific flank 默认值：`src/main/java/com/NGSViz/configSet/CheckLegal2InputParameter.java:152-166`
- 必填参数和取值验证：`src/main/java/com/NGSViz/configSet/CheckLegal2InputParameter.java:16-44,88-140,167-189`
- 输出目录当前创建位置：`src/main/java/com/NGSViz/configSet/ProcessInputParameters.java:37-58`
- 单 BAM 与 TXT/CSV 输入展开：`src/main/java/com/NGSViz/configSet/ProcessInputFile.java:13-38`
- `num_datapoints` 最低值：`src/main/java/com/NGSViz/configSet/DataPointNum.java:8-11`
- signal/control 拆分和现有计算调用：`src/main/java/com/NGSViz/coverageCalculator/MainCalculator.java:44-108`
- 现有 CSV 与 plot-setting 输出：`src/main/java/com/NGSViz/coverageCalculator/MainCalculator.java:112-131`
- README CLI 与计算结果说明：`README.md:300-404`
