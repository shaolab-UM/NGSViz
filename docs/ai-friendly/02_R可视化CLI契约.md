# ngsViz 原生 R 可视化 CLI 契约

## 适用范围

- 本契约定义 R 可视化模块的 `describe`、`validate-plot`、`run-plot` 三个子命令，以及旧单目录入口的兼容行为。
- R 只接收一个已经存在的 plot-setting JSON 目录，读取其中一个或多个 `*_NGSViz_plotSetting.json` 及其引用的 coverage matrix CSV。
- 单样本与多样本使用同一个 R CLI。样本数由目录中的 plot-setting JSON 数量决定，不增加第二套多样本入口。
- R 不启动 Java，不读取 `compute_request.json`，不执行 coverage 计算。
- R 不检查 Java、JAR、SQLite、BAM/BAI、BED、GTF/GFF、参考基因组或 Java 计算参数。
- `run-plot` 只编排并调用 `lib/coverageHeatmap.R` 和 `lib/qualityControlViz.R` 中的现有绘图逻辑，不复制或改写绘图算法。

## 命令

```text
Rscript lib/ngsVizPlotMain.R describe
Rscript lib/ngsVizPlotMain.R validate-plot --input-dir <plot_json_directory>
Rscript lib/ngsVizPlotMain.R run-plot --input-dir <plot_json_directory>
```

### `describe`

- 不需要输入目录。
- 输出命令、参数、plot-setting schema 版本、manifest schema 版本、R 包要求、输出类型和退出码。
- 不读取 plot-setting JSON，不读取 coverage CSV，不运行绘图逻辑。
- 成功时返回 exit code `0`。

### `validate-plot`

- 必须且只能接收一个 `--input-dir <directory>`。
- 只执行依赖与输入验证，不生成图片，不调用绘图函数，不写 `visualization_manifest.json`。
- 检查 R 包、输入目录、plot-setting JSON schema、`OutputFile`、coverage CSV，以及多样本合并兼容性。
- 验证通过不代表已经生成图片。

### `run-plot`

- 必须且只能接收一个 `--input-dir <directory>`。
- 首先执行与 `validate-plot` 完全相同的验证，不得使用第二套解析或默认值规则。
- 任一输入验证失败时不得调用任何绘图函数。
- 验证通过后按现有顺序调用：

```text
mergeAveragePlot(input_dir)
mergeMultiVisResult(input_dir)
propressData(input_dir)
if plot_setting_count > 1:
    corPlot(input_dir, merged_matrix)
    runPCA(merged_matrix, input_dir)
```

- 绘图函数返回后必须验收所有预期图片，并写入 `<input-dir>/visualization_manifest.json`。
- `run-plot` 不调用 Java，也不补算、修改或重写 coverage CSV。

## 旧单目录入口兼容

旧调用继续有效：

```text
Rscript lib/ngsVizPlotMain.R <plot_json_directory>
```

兼容规则：

- 恰有一个位置参数且该参数不是已知子命令时，解释为 `run-plot --input-dir <plot_json_directory>`。
- 兼容入口与 `run-plot` 使用同一 validator、同一绘图调用点、同一 manifest 和同一新退出码。
- 目录参数不得与 `--input-dir`、`describe`、`validate-plot` 或 `run-plot` 混用。
- 不新增 `-O`、`--output-dir`、单 JSON 文件参数或 CSV 直传入口。
- 旧入口的图片名称和现有绘图行为保持不变。

## 输入目录与文件发现

- `input_dir` 必须是存在、可读的目录；`run-plot` 还要求该目录可写。
- 只将目录第一层中名称匹配 `*_NGSViz_plotSetting.json` 的普通文件视为 plot-setting JSON。
- 文件按文件名的字节序升序排列，验证、绘图、manifest 记录均使用同一顺序。
- `visualization_manifest.json`、`compute_manifest.json` 和其他 `.json` 文件不得被当作 plot-setting JSON。
- 目录中必须至少有一个 plot-setting JSON；没有候选文件时验证失败。
- 每份 plot-setting JSON 描述一个可视化样本，并且只引用一个 coverage matrix CSV。
- 相对路径沿用现有兼容语义：相对于启动 R 进程时的工作目录解析。新生成的 plot-setting JSON 应优先写绝对路径，避免延迟绘图时工作目录变化。
- R 不递归搜索子目录，也不根据 `compute_manifest.json` 推断输入文件。

## 单样本与多样本行为

| plot-setting 数量 | 必需绘图 |
|---:|---|
| `1` | 合并平均曲线图、coverage heatmap |
| `> 1` | 合并平均曲线图、coverage heatmap、相关性图、PCA 图 |

- “多样本”由同一输入目录中的 plot-setting JSON 数量大于 `1` 定义。
- README 当前写有“超过两个样本才生成 QC”，但现有实现条件是 `length(plot_para_list) > 1`；本契约以现有实现为准，即两个及以上样本生成相关性图和 PCA 图。
- 多样本目录中各 JSON 的 `plot_parameters.plot_title` 必须唯一且解析为单个非空字符串。
- 多样本 coverage matrix 必须具有相同行数、相同且同序的第一列行标识，以及相同的 coverage 数值列数。
- 多样本 JSON 的 `interval_type`、`flank_size`、`middle_datapoints`、`flanking_region_datapoints`、`region_labels` 和 `output_paras.file_type` 必须一致。
- 不满足合并条件时整体验证失败；不得退化为只绘制部分样本。

## plot-setting JSON schema

当前 schema version 为 `"1.0"`。示例骨架：

```json
{
  "versionParameters": {
    "version": "1.0"
  },
  "output_paras": {
    "top_bottom_ratio": [1, 3],
    "high": 12,
    "file_type": "tiff",
    "width": 12,
    "output_path": "/results/sample",
    "dpi": 300
  },
  "OutputFile": [
    {
      "heatmapDataFile": "/results/sample/sample_coverage_matrix_heatmap.csv",
      "readCountFile": "/results/sample/sample_gene_read_count.csv"
    }
  ],
  "split_paras": {
    "hc_m": "ward.D",
    "gene_list_file": "",
    "split_mode": "cluster",
    "cluster_num": 1,
    "sub_gene_list": ["all"],
    "dist_m": "euclidean"
  },
  "plot_parameters": {
    "flank_size": 2000,
    "CenterMode": false,
    "num_datapoints": 100,
    "region_labels": ["TSS"],
    "SEM": true,
    "plot_title": ["H3K4me3"],
    "middle_datapoints": 1,
    "flanking_region_datapoints": 50,
    "sample_list": ["SampleName"],
    "interval_type": "point_interval",
    "smooth": true
  },
  "theme_paras": {
    "title_size": 11,
    "legend_size": 10,
    "title_color": "black",
    "dash_color": "black",
    "axis_text_x_size": 10,
    "text_family": "sans",
    "axis_text_y_size": 10,
    "dash_size": 0.6,
    "sample_color": ["#66C2A5"],
    "text_face": "bold",
    "split_color": ["#BC3C29FF"],
    "title_hjust": 0.5,
    "border_size": 1.2,
    "legend_text_size": 10,
    "line_size": 0.8,
    "ystrip_text_size": 8
  }
}
```

### 顶层对象

| 字段 | JSON 类型 | 必需 | 约束 |
|---|---|---:|---|
| `versionParameters` | object | 是 | 只承载 plot-setting schema 版本 |
| `output_paras` | object | 是 | 现有图片输出参数 |
| `OutputFile` | array<object> | 是 | 长度必须为 `1` |
| `split_paras` | object | 是 | 现有聚类/拆分参数 |
| `plot_parameters` | object | 是 | 现有坐标与绘图参数 |
| `theme_paras` | object | 是 | 现有主题参数 |

- 未知顶层字段验证失败，避免拼写错误被静默忽略。
- 下表定义的嵌套对象同样拒绝未知字段；只有明确标为可选的字段可以缺失。
- 字段名区分大小写；`OutputFile` 和 `CenterMode` 保留现有大小写。
- JSON 必须是单个完整对象，不接受 JSON Lines、YAML、注释或尾随内容。
- JSON `null` 不等于字段缺失；本 schema 未声明可为 null 的字段均不得为 null。

### `versionParameters`

| 字段 | 类型 | 约束 |
|---|---|---|
| `version` | string | 必须为 `"1.0"` |

### `OutputFile`

| 字段 | 类型 | 约束 |
|---|---|---|
| `heatmapDataFile` | string | 必需、非空；指向唯一 coverage matrix CSV |
| `readCountFile` | string | 必需、非空；保留 Java/R 文件契约字段 |

- `validate-plot` 必须验证 `heatmapDataFile` 的存在性、可读性、非空和矩阵内容。
- 当前绘图逻辑不读取 `readCountFile`，因此 R validator 只验证其 schema 类型，不验证文件存在性或内容。
- R validator 不从文件名、read-count 文件或其他 manifest 反推缺失的 `OutputFile`。

### `output_paras`

| 字段 | 类型 | 约束 |
|---|---|---|
| `top_bottom_ratio` | array<number> | 恰有两个大于 `0` 的有限数 |
| `high` | number | 大于 `0` 的有限数 |
| `file_type` | string | `tiff`、`png` 或 `pdf` |
| `width` | number | 大于 `0` 的有限数 |
| `output_path` | string | 必需、非空；保留兼容字段 |
| `dpi` | number | 大于 `0` 的有限数 |

- 现有主入口将图片写入 `input_dir`，不得以 `output_path` 引入第二个 CLI 输出目录。
- `output_path` 不触发目录创建，不作为 Java 结果目录检查入口。

### `split_paras`

| 字段 | 类型 | 约束 |
|---|---|---|
| `hc_m` | string | 非空，必须是现有 `hclust` 支持的方法 |
| `gene_list_file` | string | 可为空字符串；当前主绘图入口不读取该文件 |
| `split_mode` | string | `cluster` 或 `sub_gene_list` |
| `cluster_num` | integer | `>= 1` |
| `sub_gene_list` | array<string> | 非空，元素均为非空字符串 |
| `dist_m` | string | 非空，必须是现有 `dist` 支持的方法 |

- 当前 `getPlotJsonParas()` 在 `sub_gene_list[1] == "all"` 时强制使用 `cluster`。
- 当前 `sub_gene_list` 绘图分支未实现；因此 `sub_gene_list[1]` 不是 `"all"` 时验证失败，不得进入绘图后再产生未定义对象错误。

### `plot_parameters`

| 字段 | 类型 | 约束 |
|---|---|---|
| `flank_size` | number | `>= 0` 的有限数 |
| `CenterMode` | boolean | 保留现有字段 |
| `num_datapoints` | integer | `>= 1` |
| `region_labels` | string 或 array<string> | point 为一个标签，broad 为两个标签 |
| `SEM` | boolean | 是否绘制 SEM |
| `plot_title` | string 或 array<string> | 必须解析为一个非空字符串 |
| `middle_datapoints` | integer | `>= 1` |
| `flanking_region_datapoints` | integer | `>= 0` |
| `sample_list` | array<string> | 非空，元素均为非空字符串 |
| `interval_type` | string | `point_interval` 或 `broad_interval` |
| `smooth` | boolean | 是否调用现有平滑逻辑 |

以下关系必须同时成立：

```text
num_datapoints + 1
  = 2 × flanking_region_datapoints + middle_datapoints
  = coverage matrix 的 coverage 数值列数
```

- coverage CSV 第一列是行标识，不计入 coverage 数值列数。
- point 示例为 `2 × 50 + 1 = 101` 列，broad 示例为 `2 × 20 + 61 = 101` 列。
- `point_interval` 必须有一个 `region_labels` 标签；`broad_interval` 必须有两个标签。

### `theme_paras`

| 字段 | 类型 | 约束 |
|---|---|---|
| `title_size` | number | `>= 0` 的有限数 |
| `legend_size` | number | `>= 0` 的有限数 |
| `title_color` | string | 非空 |
| `border_color` | string | 可选；存在时必须非空且可被 R 解释为颜色 |
| `dash_color` | string | 非空且可被 R 解释为颜色 |
| `axis_text_x_size` | number | `>= 0` 的有限数 |
| `text_family` | string | 非空 |
| `axis_text_y_size` | number | `>= 0` 的有限数 |
| `dash_size` | number | `>= 0` 的有限数 |
| `sample_color` | array<string> | 数量不少于 `sample_list`，均为有效颜色 |
| `text_face` | string | R 主题支持的字体样式 |
| `split_color` | array<string> | 数量不少于 `cluster_num`，均为有效颜色 |
| `title_hjust` | number | `[0, 1]` 内的有限数 |
| `border_size` | number | `>= 0` 的有限数 |
| `legend_text_size` | number | `>= 0` 的有限数 |
| `line_size` | number | `>= 0` 的有限数 |
| `ystrip_text_size` | number | `>= 0` 的有限数 |

## R 包验证

`validate-plot` 和 `run-plot` 必须以 `requireNamespace(..., quietly = TRUE)` 检查现有模块直接加载或调用的包：

```text
jsonlite
ggh4x
tidyverse
colorspace
circlize
RColorBrewer
purrr
data.table
RcppRoll
zoo
aplot
cowplot
corrplot
ggrepel
FactoMineR
scales
```

- 缺包时返回 exit code `3`，并在 `errors` 中逐项列出缺失包。
- validator 不安装包，不调用 `install.packages()`、`pak::pkg_install()` 或其他网络安装器。
- 单样本与多样本使用同一入口；由于现有主脚本会 source QC 模块，依赖检查不因样本数而切换。

## coverage CSV 验证

每个 `OutputFile[0].heatmapDataFile` 必须满足：

- 路径存在，是可读普通文件，且文件大小大于 `0`。
- CSV 至少有一行表头和一行数据。
- 第一列是非空、唯一的行标识；其余列全部为 coverage 数值列。
- coverage 列名必须可按现有 `matrix2LongDf()` 规则解析为位置，且位置不重复。
- coverage 数值列数量必须满足：

```text
2 × flanking_region_datapoints + middle_datapoints
```

- 所有 coverage 单元格必须可解析为有限数值；空值、`NA`、`NaN`、`Inf`、`-Inf` 和非数值文本均不允许。
- 矩阵至少包含一个非零 coverage 值。
- validator 不修改列名、不填补缺失值、不删除异常行，也不进行 Java coverage 参数检查。

## 异常与状态规则

| 情况 | validation 状态 | manifest 状态 | response 状态 | Exit code |
|---|---|---|---|---:|
| R 包缺失 | `failed` | `failed`，仅适用于 `run-plot` | `error` | `3` |
| JSON schema 或 CLI 参数错误 | `failed` | `failed`，仅适用于 `run-plot` | `error` | `2` |
| `OutputFile` 无效、coverage CSV 缺失/空文件 | `failed` | `failed`，仅适用于 `run-plot` | `error` | `4` |
| coverage 含 `NaN`、`Inf`、`-Inf`、空值或非数值 | `failed` | `failed`，仅适用于 `run-plot` | `error` | `4` |
| coverage matrix 全零 | `failed` | `failed`，仅适用于 `run-plot` | `error` | `4` |
| 现有绘图函数抛出异常 | `passed` | `failed` | `error` | `5` |
| 预期图片不存在 | `passed` | `failed`，图片为 `missing` | `error` | `6` |
| 预期图片为零字节 | `passed` | `failed`，图片为 `empty` | `error` | `6` |
| 全部图片存在且非空 | `passed` | `completed` | `success` | `0` |

- 多 JSON 目录按整体事务判定：任一 JSON 或 CSV 无效，整个目录验证失败且不开始绘图。
- `run-plot` 失败时不得把已存在的旧图片当作本次成功产物。产物验收必须确认图片由本次运行创建或更新。
- 若部分图片已经生成后发生失败，manifest 逐项记录实际状态，但顶层仍为 `failed`。
- `validate-plot` 不写 manifest；表中 manifest 状态只适用于 `run-plot`。

## 图片产物

`run-plot` 的预期图片由输入数量和统一的 `file_type` 决定：

| 条件 | `kind` | 路径 |
|---|---|---|
| 所有运行 | `average_profile` | `<input-dir>/merge_average_plot.<file_type>` |
| 所有运行 | `coverage_heatmap` | `<input-dir>/coverage_heatmap.<file_type>` |
| plot-setting 数量 `> 1` | `correlation` | `<input-dir>/correlation_plot.tiff` |
| plot-setting 数量 `> 1` | `pca` | `<input-dir>/PCA_plot.<file_type>` |

- 文件名保持现有绘图逻辑，不由 CLI 重新命名。
- 验收只接受普通文件且大小大于 `0`。
- 本契约不增加缩略图、HTML、PDF 汇总或替代图片。

## `visualization_manifest.json`

### Schema

```json
{
  "schema_version": "1.0",
  "status": "completed",
  "ngsviz_version": "1.3",
  "command": "run-plot",
  "started_at": "2026-07-30T12:00:00Z",
  "finished_at": "2026-07-30T12:00:10Z",
  "duration_ms": 10000,
  "input_dir": "/results/visualization",
  "plot_setting_count": 2,
  "plot_settings": [
    {
      "path": "/results/visualization/sample_1_NGSViz_plotSetting.json",
      "title": "sample_1",
      "coverage_csv": "/results/sample_1/sample_1_coverage_matrix_heatmap.csv",
      "rows": 19000,
      "coverage_columns": 101,
      "expected_coverage_columns": 101,
      "status": "valid"
    },
    {
      "path": "/results/visualization/sample_2_NGSViz_plotSetting.json",
      "title": "sample_2",
      "coverage_csv": "/results/sample_2/sample_2_coverage_matrix_heatmap.csv",
      "rows": 19000,
      "coverage_columns": 101,
      "expected_coverage_columns": 101,
      "status": "valid"
    }
  ],
  "validation": {
    "status": "passed"
  },
  "outputs": [
    {
      "kind": "average_profile",
      "path": "/results/visualization/merge_average_plot.tiff",
      "status": "created",
      "size_bytes": 123456
    },
    {
      "kind": "coverage_heatmap",
      "path": "/results/visualization/coverage_heatmap.tiff",
      "status": "created",
      "size_bytes": 234567
    },
    {
      "kind": "correlation",
      "path": "/results/visualization/correlation_plot.tiff",
      "status": "created",
      "size_bytes": 34567
    },
    {
      "kind": "pca",
      "path": "/results/visualization/PCA_plot.tiff",
      "status": "created",
      "size_bytes": 45678
    }
  ],
  "warnings": [],
  "errors": []
}
```

### 字段约束

| 字段 | 类型 | 必需 | 约束 |
|---|---|---:|---|
| `schema_version` | string | 是 | 当前为 `"1.0"` |
| `status` | string | 是 | `completed` 或 `failed` |
| `ngsviz_version` | string | 是 | 实际运行版本 |
| `command` | string | 是 | 固定为 `run-plot`；兼容入口也记录该值 |
| `started_at` | string | 是 | UTC ISO 8601 |
| `finished_at` | string | 是 | UTC ISO 8601 |
| `duration_ms` | integer | 是 | `>= 0` |
| `input_dir` | string | 是 | 规范化后的绝对目录 |
| `plot_setting_count` | integer | 是 | `>= 0`，与 `plot_settings` 长度一致；失败 manifest 可为 `0` |
| `plot_settings` | array<object> | 是 | 按确定性文件顺序记录全部输入 |
| `validation.status` | string | 是 | `passed` 或 `failed` |
| `outputs` | array<object> | 是 | 记录本次运行的全部预期图片 |
| `warnings` | array<object> | 是 | 可为空 |
| `errors` | array<object> | 是 | 可为空；顶层 `failed` 时至少一个 |

`plot_settings` 元素：

| 字段 | 类型 | 约束 |
|---|---|---|
| `path` | string | plot-setting JSON 绝对路径 |
| `title` | string 或 null | 成功解析时为唯一标题 |
| `coverage_csv` | string 或 null | 成功解析时为规范化路径 |
| `rows` | integer 或 null | 成功读取时 `>= 1` |
| `coverage_columns` | integer 或 null | 实际 coverage 数值列数 |
| `expected_coverage_columns` | integer 或 null | `2 × flank + middle` |
| `status` | string | `valid` 或 `invalid` |

`outputs` 元素：

| 字段 | 类型 | 约束 |
|---|---|---|
| `kind` | string | `average_profile`、`coverage_heatmap`、`correlation` 或 `pca` |
| `path` | string | 预期图片绝对路径 |
| `status` | string | `created`、`missing`、`empty` 或 `failed` |
| `size_bytes` | integer 或 null | `created` 时大于 `0`，其他状态可为 null 或 `0` |

- `run-plot` 一旦能够确定 `input_dir` 且目录可写，无论成功或失败都必须以原子替换方式写 manifest。
- manifest 只描述 R 可视化，不得包含 Java 环境、SQLite、BAM、BED 或计算参数验证结果。
- manifest 不是 plot-setting JSON，后续文件发现必须明确排除它。

## JSON response envelope

三个新子命令以及兼容入口的 stdout 最终只输出一个 JSON 对象：

```json
{
  "schema_version": "1.0",
  "command": "validate-plot",
  "status": "success",
  "exit_code": 0,
  "message": "Visualization inputs are valid.",
  "data": {
    "input_dir": "/results/visualization",
    "plot_setting_count": 2,
    "manifest": null
  },
  "warnings": [],
  "errors": []
}
```

| 字段 | 类型 | 必需 | 说明 |
|---|---|---:|---|
| `schema_version` | string | 是 | response schema 版本，当前为 `"1.0"` |
| `command` | string | 是 | `describe`、`validate-plot` 或 `run-plot` |
| `status` | string | 是 | `success` 或 `error` |
| `exit_code` | integer | 是 | 必须与进程退出码一致 |
| `message` | string | 是 | 简短、机器安全的英文消息 |
| `data` | object 或 null | 是 | 成功数据或已安全解析的上下文 |
| `warnings` | array<object> | 是 | 可为空 |
| `errors` | array<object> | 是 | 可为空；`error` 时至少一个 |

warning/error 元素：

```json
{
  "code": "COVERAGE_NON_FINITE",
  "file": "/results/sample/sample_coverage_matrix_heatmap.csv",
  "field": null,
  "message": "Coverage matrix contains NaN or infinite values."
}
```

- `file`、`field` 不适用时为 null，不能省略。
- 普通诊断信息不得写入 stdout；可写 stderr。
- response 不包含 coverage 数据、JSON 全文或 R 调用栈。
- `validate-plot` 成功时 `data.manifest` 为 null。
- `run-plot` 完成 manifest 写入后，`data.manifest` 为其绝对路径。
- 兼容单目录入口的 response `command` 记录为 `run-plot`。

## R 模块退出码

| Exit code | 含义 | 典型情况 |
|---:|---|---|
| `0` | 成功 | describe 完成、验证通过、全部图片验收通过 |
| `2` | CLI 或 plot-setting schema 错误 | 未知子命令、缺少/混用参数、无 plot-setting JSON、JSON 语法/类型/字段/版本错误 |
| `3` | R 环境或依赖缺失 | 必需 R 包不可用 |
| `4` | 可视化输入无效 | `OutputFile` 无效、coverage CSV 缺失/空、维度不符、非有限值、全零、多样本不可合并 |
| `5` | 绘图执行失败 | 现有绘图函数或其调用链抛出异常 |
| `6` | 图片产物验收失败 | 预期图片缺失、不是普通文件或零字节 |

- validator 可一次返回多个 errors，但进程只返回一个最高层退出码。
- 新子命令和兼容入口不使用现有未分类的 exit code `1`。
- 同时存在多类错误时，按 `2`、`3`、`4` 的验证顺序返回第一个阻止后续阶段的类别；进入绘图后只返回 `5` 或 `6`。

## 现有实现依据

- 单目录参数、JSON 枚举和绘图调用顺序：`lib/ngsVizPlotMain.R:19-21,24-63`
- plot-setting 字段读取：`lib/processJSON.R:27-87`
- coverage CSV 读取与缺失检查：`lib/coverageHeatmap.R:348-372`
- 单样本/多样本 heatmap 与平均曲线输出：`lib/coverageHeatmap.R:412-478`
- coverage 矩阵位置列转换：`lib/coverageHeatmap.R:141-157`
- 多样本矩阵汇总与全零行过滤：`lib/qualityControlViz.R:56-101`
- 相关性固定 TIFF 输出：`lib/qualityControlViz.R:104-139`
- PCA 输出：`lib/qualityControlViz.R:148-174`
- 多样本 QC 的现有触发条件：`lib/ngsVizPlotMain.R:59-63`
- README 的 R 输入目录、单/多样本说明与图片列表：`README.md:423-502,521-538`
- 现有 point/broad 示例均为 101 个 coverage 数值列：`Samples/Result/*/*_NGSViz_plotSetting.json`
