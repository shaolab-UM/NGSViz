# ngsViz Native R Visualization CLI Contract

## Scope and commands

- R accepts one existing directory containing one or more `*_NGSViz_plotSetting.json` files and their referenced coverage matrices.
- Single- and multi-sample runs use the same CLI; the number of plot-setting files determines sample count.
- R never starts Java, reads `compute_request.json`, computes coverage, or validates Java, JAR, SQLite, BAM/BAI, BED, GTF/GFF, genomes, or Java parameters.
- `run-plot` orchestrates existing logic in `lib/coverageHeatmap.R` and `lib/qualityControlViz.R`; it must not duplicate or change plot algorithms.

```text
Rscript lib/ngsVizPlotMain.R describe
Rscript lib/ngsVizPlotMain.R validate-plot --input-dir <plot_json_directory>
Rscript lib/ngsVizPlotMain.R run-plot --input-dir <plot_json_directory>
```

- `describe` needs no directory, reads no input, runs no plotting code, and returns exit code `0` with commands, arguments, schema versions, R packages, outputs, and exit codes.
- `validate-plot` accepts exactly one `--input-dir`, validates dependencies and inputs only, creates no figure, calls no plot function, and writes no manifest.
- `run-plot` accepts exactly one `--input-dir` and first uses the identical validator. Any validation error prevents all plot calls.
- After validation, call in order: `mergeAveragePlot(input_dir)`, `mergeMultiVisResult(input_dir)`, `propressData(input_dir)`, and, when the plot-setting count is greater than one, `corPlot(input_dir, merged_matrix)` and `runPCA(merged_matrix, input_dir)`.
- After plot functions return, accept every expected figure and write `<input-dir>/visualization_manifest.json`. Never call Java or modify/rewrite coverage CSV files.

## Legacy directory entry point

`Rscript lib/ngsVizPlotMain.R <plot_json_directory>` remains valid and is interpreted as `run-plot --input-dir <plot_json_directory>` when the single positional argument is not a known subcommand.

- It shares the same validator, plot call site, manifest, and new exit codes.
- Do not mix the positional directory with `--input-dir` or a subcommand.
- Do not add `-O`, `--output-dir`, single-JSON, or direct-CSV inputs.
- Preserve existing figure names and plotting behavior.

## Input discovery

- `input_dir` exists, is readable, and is writable for `run-plot`.
- Only regular top-level files matching `*_NGSViz_plotSetting.json` are candidates. Sort by ascending filename byte order and use that order for validation, plotting, and the manifest.
- Exclude `visualization_manifest.json`, `compute_manifest.json`, and every other JSON file. At least one candidate is required.
- Each plot-setting file describes one visualization sample and references exactly one coverage-matrix CSV.
- Preserve legacy relative-path semantics: resolve relative to the R process working directory. Newly generated files should use absolute paths for reliable deferred plotting.
- Do not recurse or infer inputs from `compute_manifest.json`.

## Single- and multi-sample behavior

| Plot-setting count | Required figures |
|---:|---|
| `1` | Merged average profile and coverage heatmap |
| `> 1` | Merged average profile, coverage heatmap, correlation, and PCA |

- Multi-sample means more than one plot-setting file. The implementation condition `length(plot_para_list) > 1` overrides older README wording about more than two samples.
- Multi-sample `plot_parameters.plot_title` values resolve to unique, non-empty scalar strings.
- Coverage matrices have the same row count, identical first-column row identifiers in identical order, and equal coverage-column counts.
- `interval_type`, `flank_size`, `middle_datapoints`, `flanking_region_datapoints`, `region_labels`, and `output_paras.file_type` are identical across samples.
- Any incompatibility fails the whole directory; never plot a subset implicitly.

## Plot-setting JSON schema

The current schema version is `"1.0"`. Required top-level objects are `versionParameters`, `output_paras`, `OutputFile`, `split_paras`, `plot_parameters`, and `theme_paras`.

- Reject unknown top-level and nested fields except fields explicitly marked optional below.
- Field names are case-sensitive; preserve `OutputFile` and `CenterMode`.
- Accept one complete JSON object only, not JSON Lines, YAML, comments, or trailing content.
- Null differs from absence and is forbidden unless a field explicitly allows it.

### Object contracts

| Object.field | Type | Constraint |
|---|---|---|
| `versionParameters.version` | string | Exactly `"1.0"` |
| `OutputFile` | array<object> | Exactly one item |
| `OutputFile[0].heatmapDataFile` | string | Required, non-empty, unique coverage-matrix path |
| `OutputFile[0].readCountFile` | string | Required, non-empty compatibility field |
| `output_paras.top_bottom_ratio` | array<number> | Exactly two finite values greater than `0` |
| `output_paras.high` | number | Finite and greater than `0` |
| `output_paras.file_type` | string | `tiff`, `png`, or `pdf` |
| `output_paras.width` | number | Finite and greater than `0` |
| `output_paras.output_path` | string | Required and non-empty; compatibility field only |
| `output_paras.dpi` | number | Finite and greater than `0` |
| `split_paras.hc_m` | string | Non-empty method supported by existing `hclust` |
| `split_paras.gene_list_file` | string | May be empty; not read by current main entry point |
| `split_paras.split_mode` | string | `cluster` or `sub_gene_list` |
| `split_paras.cluster_num` | integer | `>= 1` |
| `split_paras.sub_gene_list` | array<string> | Non-empty with non-empty items |
| `split_paras.dist_m` | string | Non-empty method supported by existing `dist` |
| `plot_parameters.flank_size` | number | Finite and `>= 0` |
| `plot_parameters.CenterMode` | boolean | Existing field |
| `plot_parameters.num_datapoints` | integer | `>= 1` |
| `plot_parameters.region_labels` | string or array<string> | One point label or two broad labels |
| `plot_parameters.SEM` | boolean | Whether to draw SEM |
| `plot_parameters.plot_title` | string or array<string> | Resolves to one non-empty string |
| `plot_parameters.middle_datapoints` | integer | `>= 1` |
| `plot_parameters.flanking_region_datapoints` | integer | `>= 0` |
| `plot_parameters.sample_list` | array<string> | Non-empty with non-empty items |
| `plot_parameters.interval_type` | string | `point_interval` or `broad_interval` |
| `plot_parameters.smooth` | boolean | Whether to call existing smoothing logic |

- `heatmapDataFile` must exist, be readable and non-empty, and pass matrix validation. R validates only the schema type of `readCountFile` because current plotting code does not read it.
- The CLI writes figures to `input_dir`; `output_path` neither selects a second CLI output directory nor creates or validates a Java result directory.
- `getPlotJsonParas()` forces `cluster` when `sub_gene_list[1] == "all"`. Because the current `sub_gene_list` plot branch is unimplemented, any other first value fails validation.
- The following equality is mandatory: `num_datapoints + 1 = 2 * flanking_region_datapoints + middle_datapoints = coverage numeric-column count`. The first CSV identifier column is excluded.
- `point_interval` requires one region label; `broad_interval` requires two.

### Theme contract

| Field | Constraint |
|---|---|
| `title_size`, `legend_size`, `axis_text_x_size`, `axis_text_y_size`, `dash_size`, `border_size`, `legend_text_size`, `line_size`, `ystrip_text_size` | Finite number `>= 0` |
| `title_color`, `dash_color` | Non-empty valid R color |
| `border_color` | Optional; non-empty valid R color when present |
| `text_family` | Non-empty string |
| `sample_color` | Valid-color array with at least `sample_list` length |
| `text_face` | Font face supported by the R theme |
| `split_color` | Valid-color array with at least `cluster_num` length |
| `title_hjust` | Finite number in `[0, 1]` |

## R package validation

Both validation and execution use `requireNamespace(..., quietly = TRUE)` for: `jsonlite`, `ggh4x`, `tidyverse`, `colorspace`, `circlize`, `RColorBrewer`, `purrr`, `data.table`, `RcppRoll`, `zoo`, `aplot`, `cowplot`, `corrplot`, `ggrepel`, `FactoMineR`, and `scales`.

- Missing packages return exit code `3` with one error per package.
- The validator never installs packages or invokes a network installer.
- Dependency checks do not vary by sample count because the main script sources the QC module.

## Coverage CSV validation

Every `heatmapDataFile` must:

- Be a readable, non-empty regular file with a header and at least one data row.
- Have a non-empty, unique first-column row identifier and only numeric coverage columns afterward.
- Have coverage headers that existing `matrix2LongDf()` can parse as unique positions.
- Have exactly `2 * flanking_region_datapoints + middle_datapoints` coverage columns.
- Contain only finite numeric cells; empty values, `NA`, `NaN`, `Inf`, `-Inf`, and nonnumeric text are invalid.
- Contain at least one nonzero coverage value.

The validator never renames columns, imputes values, deletes rows, or validates Java compute parameters.

## Status and artifact acceptance

| Condition | Validation | Manifest | Response | Exit |
|---|---|---|---|---:|
| Missing R package | `failed` | `failed` for `run-plot` | `error` | `3` |
| CLI or JSON schema error | `failed` | `failed` for `run-plot` | `error` | `2` |
| Invalid `OutputFile` or missing/empty CSV | `failed` | `failed` for `run-plot` | `error` | `4` |
| Non-finite, empty, or nonnumeric coverage | `failed` | `failed` for `run-plot` | `error` | `4` |
| All-zero coverage | `failed` | `failed` for `run-plot` | `error` | `4` |
| Plot function exception | `passed` | `failed` | `error` | `5` |
| Missing or empty expected figure | `passed` | `failed` | `error` | `6` |
| All figures non-empty | `passed` | `completed` | `success` | `0` |

- Treat a multi-JSON directory transactionally: any invalid input prevents all plotting.
- Do not accept pre-existing old figures as outputs of the current failed run; acceptance verifies creation or update by this run.
- If a failure occurs after partial output, record each actual artifact status and keep top-level status `failed`.
- `validate-plot` never writes a manifest.

Expected outputs:

| Condition | Kind | Path |
|---|---|---|
| Every run | `average_profile` | `<input-dir>/merge_average_plot.<file_type>` |
| Every run | `coverage_heatmap` | `<input-dir>/coverage_heatmap.<file_type>` |
| Count `> 1` | `correlation` | `<input-dir>/correlation_plot.tiff` |
| Count `> 1` | `pca` | `<input-dir>/PCA_plot.<file_type>` |

Preserve filenames. Accept only non-empty regular files. Add no thumbnails, HTML, summary PDF, or substitute figure.

## `visualization_manifest.json`

Required fields and constraints:

| Field | Constraint |
|---|---|
| `schema_version` | Exactly `"1.0"` |
| `status` | `completed` or `failed` |
| `ngsviz_version` | Actual version |
| `command` | Exactly `run-plot`, including the legacy entry point |
| `started_at`, `finished_at` | UTC ISO 8601 |
| `duration_ms` | Integer `>= 0` |
| `input_dir` | Normalized absolute directory |
| `plot_setting_count` | Integer `>= 0`, equal to `plot_settings` length |
| `plot_settings` | All inputs in deterministic order |
| `validation.status` | `passed` or `failed` |
| `outputs` | Every expected figure for this run |
| `warnings`, `errors` | Arrays; failed top-level state has at least one error |

Each `plot_settings` entry has absolute `path`; string-or-null `title` and normalized `coverage_csv`; integer-or-null `rows >= 1`, `coverage_columns`, and `expected_coverage_columns`; and `status` equal to `valid` or `invalid`.

Each output has `kind` (`average_profile`, `coverage_heatmap`, `correlation`, or `pca`), absolute `path`, `status` (`created`, `missing`, `empty`, or `failed`), and integer-or-null `size_bytes`, which is greater than `0` when created.

- Once `run-plot` can determine a writable input directory, it atomically replaces the manifest on success or failure.
- The manifest contains only R visualization state, never Java environment, SQLite, BAM, BED, or compute validation.
- File discovery must explicitly exclude this manifest.

## JSON response envelope

Stdout ends with exactly one JSON object for every new command and the compatibility entry point:

```json
{"schema_version":"1.0","command":"validate-plot","status":"success","exit_code":0,"message":"Visualization inputs are valid.","data":{"input_dir":"/results/visualization","plot_setting_count":2,"manifest":null},"warnings":[],"errors":[]}
```

- `command` is `describe`, `validate-plot`, or `run-plot`; `status` is `success` or `error`; process and response exit codes match.
- `message` is concise, machine-safe English. `data` is an object or null. Error responses contain at least one error.
- Error objects include `code`, `file`, `field`, and `message`; inapplicable `file` or `field` is null, never omitted.
- Ordinary diagnostics go to stderr. Responses exclude coverage data, complete JSON input, and R stack traces.
- Successful validation sets `data.manifest` to null; completed execution sets it to the absolute manifest path.

## Exit codes

| Code | Meaning | Typical cases |
|---:|---|---|
| `0` | Success | Describe, valid inputs, or all accepted figures |
| `2` | CLI or plot-setting schema error | Unknown/mixed arguments, no candidates, syntax/type/field/version error |
| `3` | Missing R environment/dependency | Required package unavailable |
| `4` | Invalid visualization input | Bad `OutputFile`, missing/empty/dimensionally invalid/non-finite/all-zero/incompatible coverage |
| `5` | Plot execution failure | Exception from existing plotting logic |
| `6` | Figure acceptance failure | Missing, non-regular, or empty expected figure |

Validation returns the first blocking category in `2`, `3`, `4` order; after plotting begins, return only `5` or `6`. New and compatibility entry points do not use unclassified exit code `1`.

## Implementation references

- CLI and plot order: `lib/ngsVizPlotMain.R:19-21,24-63`
- Plot-setting parsing: `lib/processJSON.R:27-87`
- Coverage loading: `lib/coverageHeatmap.R:348-372`
- Heatmap and profile outputs: `lib/coverageHeatmap.R:412-478`
- Position-column conversion: `lib/coverageHeatmap.R:141-157`
- Multi-sample matrix/QC: `lib/qualityControlViz.R:56-101`
- Correlation TIFF: `lib/qualityControlViz.R:104-139`
- PCA: `lib/qualityControlViz.R:148-174`
- Multi-sample QC condition: `lib/ngsVizPlotMain.R:59-63`
- README contract: `README.md:423-502,521-538`
- Point/broad 101-column examples: `Samples/Result/*/*_NGSViz_plotSetting.json`
