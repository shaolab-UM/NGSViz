# ngsViz Java Compute CLI Contract

## Scope

This is the source-of-truth contract for native Java dispatch, `ComputeRequest`, validation, execution, and `compute_manifest.json`.

- Java computes only. It never starts `Rscript` or checks or generates figures.
- Legacy short options and JSON requests normalize to one single-sample `ComputeRequest`.
- Both inputs share one validator and one `MainCalculator` call site; coverage, binning, normalization, and other scientific logic must not be duplicated.
- A request describes exactly one signal sample and at most one matching control BAM.
- `sample_id`, `sample_name`, and `group_name` are output metadata only and must not affect coverage computation.

## Commands

```bash
java -jar NGSViz-1.3.jar describe --format json
java -jar NGSViz-1.3.jar validate-compute --request /absolute/path/compute_request.json
java -jar NGSViz-1.3.jar run-compute --request /absolute/path/compute_request.json
```

- `describe` accepts only `--format json`, accesses no compute request, BAM, database, or output directory, and returns exit code `0` with a JSON response envelope.
- `validate-compute` constructs `ComputeRequest` directly from JSON and performs read-only validation of structure, parameter combinations, system configuration, SQLite, BAM/BAI, and optional BED. It creates no output directory or file, does not call `MainCalculator`, and does not start R.
- `validate-compute` returns resolved values, supported values, warnings, and errors, but never an executable legacy-flags command.
- `run-compute` uses the identical validator before execution. Validation failure must not create the output directory or call `MainCalculator`.
- A successful `run-compute` accepts coverage-matrix CSV, read-count CSV, and plot-setting JSON artifacts, then writes `output_dir/compute_manifest.json`. It never starts R.

## Input modes and mutual exclusion

The Java CLI has exactly two compute input modes:

1. Compatibility mode: the first argument starts with `-` and legacy short options apply.
2. JSON mode: `validate-compute --request <json>` or `run-compute --request <json>`.

- JSON mode accepts exactly one `--request <json>` and no legacy compute option.
- Mixing modes fails before reading the request with exit code `2`; no override precedence exists.
- JSON requests must not use `-J`, YAML, or a YAML loader.
- AI callers generate single-sample JSON and pass `--request` as an argument array; they do not construct complete legacy commands.
- JSON `null` is resolved by `ComputeRequest` defaults, not translated into omitted legacy flags.
- Unknown fields fail validation. A top-level `samples` field always returns exit code `2`, regardless of its value.
- `-H` and `-V` retain ngsViz 1.3 compatibility behavior and exit without compute parsing or constructing a request.

## `compute_request.json`

```json
{
  "schema_version": "1.3",
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

`samples` is forbidden. An external orchestrator must split a multi-sample plan into independent requests and launch Java sequentially.

### Field definitions

"Required" means explicitly present and non-null. Defaults apply only to missing or null optional fields.

| Field | JSON type | Required | Default/source | Constraint and meaning |
|---|---|---:|---|---|
| `schema_version` | string | No | `"1.3"` | Only `"1.3"` is accepted |
| `sample_id` | string | Yes | none | Non-empty stable run ID; metadata only |
| `sample_name` | string | Yes | none | Non-empty display name; metadata only |
| `group_name` | string | Yes | none | Non-empty group display name; metadata only |
| `title` | string | Yes | none | Analysis title and plot-setting filename source |
| `genome` | string | Yes | none | Must exist in the selected database |
| `region` | string | Yes | none | Lowercased before validation; supported values come from the database and validator |
| `signal_bam` | string | Yes | none | One readable BAM with usable BAI |
| `control_bam` | string or null | No | `null` | At most one control/input BAM; paired normalization when non-null |
| `output_dir` | string | Yes | none | Target single-sample result directory |
| `system_config` | string or null | No | `null` | Null selects `NGSViz_setting.json` beside the JAR |
| `database` | string or null | No | `"RefSeq"` | Reference annotation type; legacy `-D` |
| `analysis_type` | string or null | No | `"transcript"` | `transcript` or `exon` |
| `biotype` | string or null | No | `"protein_coding"` | Annotation-record biotype |
| `flank_region` | integer or null | No | region-specific | Must be `>= 0` |
| `flank_factor` | number or null | No | `0.0` | Range `[0, 1]` |
| `num_datapoints` | integer or null | No | `100` | Must be `>= 100` |
| `scale_ratio` | number or null | No | `null` | Null retains CPM; otherwise use existing `signal * scale_ratio` behavior |
| `mapping_quality` | integer or null | No | `20` | Must be `>= 0` |
| `fragment_length` | integer or null | No | `150` | Must be `>= 0`; existing single-end extension semantics |
| `cores` | integer or null | No | `1` | Must be `>= 1` |
| `batch_size` | integer or null | No | `500` | Must be `>= 1` |
| `bin_method` | string or null | No | `"mean"` | `mean`, `median`, or `max` |
| `strand_specific` | string or null | No | `"both"` | `both`, `same`, or `opposite` |
| `center_mode` | boolean or null | No | `false` | Null resolves to false |
| `custom_bed` | string or null | No | `null` | BED must use 0-based half-open coordinates |
| `gene_subset` | string or null | No | `"all"` | Legacy `-X`; retain current gene-list semantics |
| `create_result_folder` | boolean or null | No | `false` | Legacy `-NF`; true uses a `title` subdirectory under `output_dir` |

### Null and default rules

- Preserve JSON types; never represent null as the string `"null"`.
- A missing or null required field fails validation. A missing or null optional field resolves to the table default.
- `flank_region=null` selects: `2000` for `tss`, `tes`, `genebody`, `exon`, and `cgi`; `1500` for `enhancer`; `1000` for `dhs` and `bed`.
- Default resolution produces `resolved_parameters`; the validator and `MainCalculator` consume only this resolved object.
- `scale_ratio=null` must preserve CPM and must not become `1.0`.

## Legacy short-option compatibility

| Legacy flag | Legacy variable | `ComputeRequest` field | Mapping |
|---|---|---|---|
| `-G` | `genome` | `genome` | unchanged before region/database validation |
| `-R` | `region_type` | `region` | unchanged; lowercased by validator |
| `-T` | `title` | `title` | unchanged |
| `-I` | `input_file` | `signal_bam`, `control_bam` | signal alone, or split `signal:control` at most once |
| `-O` | `output_path` | `output_dir` | unchanged |
| `-CP` | `sys_config_path` | `system_config` | empty or `-` becomes null |
| `-D` | `DB_type` | `database` | default `"RefSeq"` |
| `-A` | `analysis_type` | `analysis_type` | default `"transcript"` |
| `-B` | `biotype` | `biotype` | default `"protein_coding"` |
| `-F` | `flank_region` | `flank_region` | absent or legacy `0` selects region-specific default |
| `-N` | `flank_factor` | `flank_factor` | default `0.0` |
| `-DP` | `num_datapoints` | `num_datapoints` | default `100`; values below `100` fail |
| `-S` | `scale_ratio` | `scale_ratio` | absent becomes null |
| `-MQ` | `min_mapq` | `mapping_quality` | default `20` |
| `-FL` | `frag_len` | `fragment_length` | default `150` |
| `-P` | `core_num` | `cores` | default `1` |
| `-BS` | `BATCH_SIZE` | `batch_size` | default `500` |
| `-BM` | `bin_method` | `bin_method` | default `"mean"` |
| `-SS` | `strand_spec` | `strand_specific` | default `"both"` |
| `-CM` | `CenterMode` | `center_mode` | default false |
| `-BD` | `bedDB_path` | `custom_bed` | absent, empty, or `-` becomes null |
| `-X` | `genes` | `gene_subset` | default `"all"` |
| `-NF` | `new_forder` | `create_result_folder` | default false |

Legacy single-BAM commands lack identity flags. The adapter sets `sample_id` to the signal BAM basename without `.bam`, and retains `"SampleName"` and `"GroupName"` for `sample_name` and `group_name`. These values never participate in coverage computation.

Legacy `-I <txt|csv>` remains supported without weakening the single-sample request rule:

- Read columns in current order: `sample_name,bam,gene_subset,group_name,title`.
- Build one request per row; never build a request containing `samples[]`.
- Default `sample_id` to the row's non-empty `sample_name`.
- Map command-level options to every request and let row values override BAM, gene subset, group, title, and sample name.
- Validate and execute requests sequentially through the same validator and `MainCalculator` call site.
- This compatibility expansion must not become a JSON batch interface.

| Legacy flag | Compatibility behavior | Reason |
|---|---|---|
| `-H` | Print help and exit successfully | Informational command |
| `-V` | Print `ngsViz 1.3` and exit successfully | Informational command |
| `-J` | Retain deprecated placeholder parsing; read no YAML | Not part of the AI-friendly contract |
| `-DB` | Retain current database-merge mode and exit behavior | Administrative operation |

## Unified validation boundary

```text
legacy flags -> LegacyComputeRequestAdapter --+
                                              +-> ComputeRequest -> ComputeValidator -> MainCalculator
JSON file ----> ComputeRequestReader ---------+
```

The validator checks at minimum: exactly one signal and at most one control; no `samples` or unknown fields; required fields, JSON types, enums, ranges, and combinations; `num_datapoints >= 100`; valid system config, SQLite, genome/database/region, and coordinate tables; readable BAM files with headers and BAI plus compatible reference naming; reasonable BAM/database chromosome overlap; at least three BED columns with `start >= 0` and `end > start`; and no overwrite of existing ngsViz artifacts.

`validate-compute` and `run-compute` use identical defaults and validation. `run-compute` must not reinterpret parameters afterward.

## `compute_manifest.json`

```json
{
  "schema_version": "1.0",
  "status": "completed",
  "ngsviz_version": "1.3",
  "started_at": "2026-07-30T12:00:00Z",
  "finished_at": "2026-07-30T12:01:00Z",
  "duration_ms": 60000,
  "resolved_parameters": {"sample_id":"H3K4me3","sample_name":"K562_H3K4me3","group_name":"K562","title":"K562_H3K4me3_TSS","genome":"hg38","region":"tss","signal_bam":"/data/K562_H3K4me3.bam","control_bam":null,"output_dir":"/results/K562_H3K4me3"},
  "sample_id": "H3K4me3",
  "sample_name": "K562_H3K4me3",
  "group_name": "K562",
  "outputs": {
    "coverage_matrices": ["/results/K562_H3K4me3/K562_H3K4me3_coverage_matrix_heatmap.csv"],
    "read_count_files": ["/results/K562_H3K4me3/K562_H3K4me3_gene_read_count.csv"],
    "plot_setting_file": "/results/K562_H3K4me3/K562_H3K4me3_TSS_NGSViz_plotSetting.json"
  },
  "log_file": "/results/K562_H3K4me3/NGSViz_compute.log",
  "warnings": [],
  "errors": []
}
```

- Required fields are exactly those shown. `schema_version` is `"1.0"`; status is `completed` or `failed`; timestamps are UTC ISO 8601; `duration_ms >= 0`.
- `resolved_parameters` is the complete single-sample resolved request and contains no `samples`; identity fields match it.
- On success, each CSV array is non-empty and every file exists and is non-empty; `plot_setting_file` is an existing non-empty JSON. `log_file` may be null.
- `warnings` and `errors` are arrays; a failed manifest has at least one error.
- The manifest describes Java computation only. It contains no visualization state, R package state, or figure list. One `run-compute` writes one single-sample manifest.

## JSON response envelope

```json
{"schema_version":"1.0","command":"validate-compute","status":"success","exit_code":0,"message":"Compute request is valid.","data":{},"warnings":[],"errors":[]}
```

- `command` is `describe`, `validate-compute`, or `run-compute`; `status` is `success` or `error`; `exit_code` equals the process exit code.
- `message` is concise, machine-safe English. `data` is an object or null. Error responses contain at least one error.
- Warning/error objects use `{"code":"REQUEST_UNKNOWN_FIELD","field":"samples","message":"Unknown field: samples."}`.
- Normal logs go to `output_dir/NGSViz_compute.log` and must not contaminate the final stdout JSON. Responses must not expose BAM/database contents or a complete legacy command.

## Exit codes

| Code | Meaning | Typical cases |
|---:|---|---|
| `0` | Success | Describe, valid request, or accepted compute artifacts |
| `2` | CLI/request format error | Unknown command, missing argument, bad JSON type, unknown field, `samples`, mixed modes |
| `3` | Missing environment/dependency | Missing system config, runtime, or required dependency |
| `4` | Invalid compute input | Database, BAM/BAI, BED, or parameter-combination failure |
| `5` | Compute execution failure | Exception from `MainCalculator` or its call chain |
| `6` | Artifact acceptance failure | Missing or empty coverage, read-count, or plot-setting output |

The validator may report multiple errors but returns one top-level category. New subcommands do not use legacy exit code `-1`.

## Implementation references

- Legacy parsing: `src/main/java/com/NGSViz/configSet/GetInputParameterValue.java:11-65`
- Java defaults: `src/main/java/com/NGSViz/configSet/InputParameterAttributes.java:29-64`
- Region-specific flank defaults: `src/main/java/com/NGSViz/configSet/CheckLegal2InputParameter.java:152-166`
- Input validation: `src/main/java/com/NGSViz/configSet/CheckLegal2InputParameter.java:16-44,88-140,167-189`
- Output-directory creation: `src/main/java/com/NGSViz/configSet/ProcessInputParameters.java:37-58`
- Single-BAM and TXT/CSV expansion: `src/main/java/com/NGSViz/configSet/ProcessInputFile.java:13-38`
- Minimum data points: `src/main/java/com/NGSViz/configSet/DataPointNum.java:8-11`
- Signal/control split and compute call: `src/main/java/com/NGSViz/coverageCalculator/MainCalculator.java:44-108`
- Existing CSV and plot-setting outputs: `src/main/java/com/NGSViz/coverageCalculator/MainCalculator.java:112-131`
- CLI and compute-result documentation: `README.md:300-404`
