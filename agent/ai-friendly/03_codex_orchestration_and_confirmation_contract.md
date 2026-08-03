# ngsViz Codex Orchestration and Confirmation Contract

## Scope

- This contract defines how the Codex Skill orchestrates the Java compute CLI and R visualization CLI. It adds no project runtime.
- Codex collects parameters, writes requests, invokes native CLIs, waits for processes, inspects responses and manifests, and requests user confirmation.
- Java and R are always separate stages. Java completion does not imply R completion, and R may run hours or days later.
- A unified Python runner, unified runtime manifest, combined Java/R process, and implicit background job are forbidden.
- Each sample's Java result is defined by its `compute_manifest.json`; an R run is defined by its independent `visualization_manifest.json`. Report them separately and never substitute one for the other.

## General confirmation rules

1. On the initial compute or plot request, Codex only collects parameters, prepares requests, and runs native validation. It must not execute `run-compute` or `run-plot`.
2. Ask the user about every unknown, missing, or ambiguous scientific parameter. Never infer one from a filename, prior task, common practice, or preference. This includes `genome`, `region`, `database`, `analysis_type`, `biotype`, `flank_region`, `num_datapoints`, `mapping_quality`, `fragment_length`, `bin_method`, `strand_specific`, `scale_ratio`, control/input BAM, and custom BED.
3. Resolve non-scientific defaults defined by the native contracts, but display all important resolved values before confirmation.
4. After validation passes, display the exact command, input request or directory, output location, and execution scope, then request explicit confirmation.
5. Confirmation authorizes only the displayed command or ordered batch. Any change to the request, paths, sample order, failure policy, or command requires validation and confirmation again.
6. Never request run confirmation after failed validation. Report native errors, correct the parameters, and validate again.
7. Compute-only requests must not create a visualization workspace, call R, or generate figures.
8. Visualization-only requests must not create a compute request, call Java, recompute coverage, or modify compute results.
9. Codex must not replace `compute_request.json` with legacy Java flags or bypass `validate-compute` or `validate-plot`.

## Compute environment and reference database

1. Visualization-only workflows skip Java checks. Before any compute request or `validate-compute`, run `ngsviz-setup` and verify `NGSViz_setting.json`, Java 17+, the JAR, `tool_path`, and `db_path`.
2. If the user supplies a genome, query the configured database first. If absent but available in the README prebuilt catalog, run `ngsviz-install-db`; otherwise request a matching UCSC GTF and run `ngsviz-build-db`.
3. If the genome is missing, ask for the species first. For a known species, query installed genomes: ask whether to use the single result or ask the user to select among multiple results.
4. If no installed version is accepted, consult the same-species prebuilt catalog. If none exists, use the UCSC GTF build workflow.
5. Never infer species or assembly from BAM names, cell lines, chromosome naming, or chromosome lengths. The user must confirm that the selected genome matches the BAM alignment assembly.
6. Validate downloaded SQLite integrity, `defaultTbl`, the target genome, and non-empty coordinate tables. After Java `-DB` import, inspect the target primary database instead of trusting only the exit code.
7. Do not create a compute request or run `validate-compute` until the environment and reference database are ready.

## Workflow A: single-sample compute only

```text
User request
  -> collect unknown parameters
  -> write one single-sample compute_request.json
  -> Java validate-compute
  -> display run-compute command and resolved parameters
  -> user confirms
  -> Java run-compute
  -> one compute_manifest.json
  -> stop without R
```

- A request contains exactly one signal sample and at most one matching control BAM.
- Validate with `java -jar NGSViz-1.3.jar validate-compute --request /absolute/path/compute_request.json`.
- Display, but do not run, `java -jar NGSViz-1.3.jar run-compute --request /absolute/path/compute_request.json` until confirmation.
- Wait for the Java process. On success, report the manifest path and status. On failure, report the exit code, errors, and failed-manifest path if present.
- Stop when Java ends; do not ask about or start plotting.

## Workflow B: sequential multi-sample compute

```text
User request -> collect parameters -> write analysis_plan.json with samples[]
  -> expand one compute_request.json per sample in array order
  -> validate every request -> display ordered queue and failure policy
  -> one batch confirmation -> run one Java process at a time
  -> record each result -> write analysis_index.json -> stop without R
```

### `analysis_plan.json`

The plan is Codex-only orchestration input. Java, R, `MainCalculator`, and plot functions must not read it. The canonical structure is defined by `schemas/analysis_plan.schema.json` and `examples/analysis_plan.multi_sample.json`.

- `samples` is non-empty and its array order is the only execution order.
- Expand each item into an independently valid Java single-sample request; never pass top-level `samples` to Java.
- Scientific parameters are shared only as allowed by the schema. Do not invent cross-sample override, inheritance, or merge rules.
- `failure_policy` is `stop_on_error` by default and accepts only `stop_on_error` or `continue_on_error`.

### Full validation and batch confirmation

- Run `validate-compute` for every expanded request in queue order. Do not start any computation during this phase.
- A validation failure blocks batch confirmation. Correct and revalidate affected requests; redisplay the queue after any parameter or order change.
- After all requests pass, display the fixed order, each sample ID, signal/control BAM, key scientific values, output directory, `run-compute` command, failure policy, and the guarantee that at most one Java process runs at a time.
- One confirmation may authorize the displayed batch; it never authorizes plotting.

### Sequential execution and failure policy

- Parallel Java processes, background concurrency, process pools, or starting the next process before the current one exits are forbidden.
- For each sample: launch one process, wait, read response/manifest, record status, then advance.
- `stop_on_error`: stop on any nonzero exit code, error response, or failed/missing manifest; mark remaining items `not_started`.
- `continue_on_error`: after recording a failed process, continue sequentially. Never overlap processes.
- On cancellation, start no new process; follow explicit instructions for the current process and mark unstarted items `cancelled`.

### `analysis_index.json`

This file indexes samples, requests, and native manifests. It is not a compute manifest, does not prove computation succeeded, and contains no figures or R state.

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
    }
  ]
}
```

- `status` is one of `completed`, `failed`, `not_started`, or `cancelled`.
- Record an absolute `compute_manifest` path only when the native manifest exists; otherwise use `null`.
- Update the corresponding entry after each Java process and write the index by atomic replacement.
- Inspect the sample's `compute_manifest.json` to determine its result; index existence or status alone is insufficient.

## Workflow C: visualization only

- Treat the supplied result directory as the R `input_dir`. Plot-setting JSON files must be at its top level and referenced coverage CSV files must be accessible. Do not search recursively or infer inputs from other manifests.
- Do not create compute requests, call Java, or recompute a sample.
- Run `Rscript lib/ngsVizPlotMain.R validate-plot --input-dir /absolute/path/visualization_workspace`.
- After successful validation, display `Rscript lib/ngsVizPlotMain.R run-plot --input-dir /absolute/path/visualization_workspace`; run it only after separate confirmation.
- Wait for R. Report only this run's `visualization_manifest.json`; a compute manifest may be identified as an input source but its state must not be merged with R state.

## Workflow D: full workflow

- Complete all of Workflow A or B, including compute confirmation. A full-workflow request does not pre-authorize Java execution.
- After all started Java processes exit, separately ask whether to plot now or later. Never start R while Java is running.
- If later, stop without creating a workspace. A later task may run Workflow C without recomputation.
- If now, create a separate visualization workspace and copy only selected `*_NGSViz_plotSetting.json` files referenced by completed compute manifests to its top level. Never overwrite or move original JSON, compute directories, or CSV files.
- Clearly list excluded failed, missing, or deselected samples. Then perform all of Workflow C, including independent validation and confirmation.
- Report every Java `compute_manifest.json` separately from the R `visualization_manifest.json`. Java `completed` means compute artifacts passed acceptance; only R `completed` means figures passed acceptance.

## Intent boundaries

| User intent | Allowed | Forbidden |
|---|---|---|
| Single-sample compute only | Workflow A | Automatic plotting or batch expansion |
| Multi-sample compute only | Workflow B | Parallel Java or automatic plotting |
| Visualization only | Workflow C | Java computation or coverage backfill |
| Full workflow | Workflow A or B, then a separate choice for C | Treating Java confirmation as R confirmation |

## Artifact ownership

| File | Creator/maintainer | Purpose | Not a |
|---|---|---|---|
| `compute_request.json` | Codex | Single-sample Java request | Multi-sample plan |
| `analysis_plan.json` | Codex | Codex multi-sample orchestration input | Java/R request |
| `compute_manifest.json` | Java | Single-sample compute record | R plotting state |
| `analysis_index.json` | Codex | Sample/request/manifest index | Compute manifest |
| `visualization_manifest.json` | R | One R run record | Java compute state |

Codex may report these paths and statuses but must not replace them with a unified runtime manifest or introduce a Python runner for Java and R.
