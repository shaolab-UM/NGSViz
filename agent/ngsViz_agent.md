# ngsViz Codex Agent Guide

This guide orchestrates ngsViz's existing Java compute CLI and R visualization CLI. It does not provide a runner, a unified executor, or a new runtime.

## Native Interfaces

Native Java subcommands:

```bash
java -jar NGSViz-1.3.jar describe --format json
java -jar NGSViz-1.3.jar validate-compute --request /absolute/path/compute_request.json
java -jar NGSViz-1.3.jar run-compute --request /absolute/path/compute_request.json
```

Native R subcommands:

```bash
Rscript lib/ngsVizPlotMain.R describe
Rscript lib/ngsVizPlotMain.R validate-plot --input-dir /absolute/path/visualization_workspace
Rscript lib/ngsVizPlotMain.R run-plot --input-dir /absolute/path/visualization_workspace
```

Java processes one signal sample at a time and never invokes R. R reads existing visualization inputs and never invokes Java. Codex orchestrates multi-sample jobs sequentially and does not generate a unified manifest.

## General Execution Boundaries

- Ask the user about unknown, missing, or ambiguous scientific parameters. Never infer the genome, region, strand, fragment length, control, or normalization.
- By default, prepare the request and run validation only. Before execution, show the validation result, native command, inputs, outputs, and execution scope, then obtain explicit confirmation.
- `analysis_plan.json` may contain `samples[]`; a Java `compute_request.json` must not contain `samples[]`.
- Run Java processes strictly in series. Do not start the next sample until the previous process has exited, its manifest has been verified, and the index has been updated.
- Use `stop_on_error` by default. Use `continue_on_error` only when the user explicitly selects it.
- Assign every sample a unique `sample_id`, `sample_name`, `group_name`, and `output_dir`.
- Do not plot automatically for a compute-only request, and do not recompute for a visualization-only request. Never invoke R after a Java failure.
- Read and report `compute_manifest.json` and `visualization_manifest.json` separately. Do not combine them into a unified manifest.
- Use `analysis_index.json` only to index single-sample requests and compute manifests. Do not aggregate or rewrite scientific results.
- Do not write Codex judgments, explanations, or inferences into raw result files.

## Java Compute Preflight

Skip this section for visualization-only workflows. Before creating a compute request or running `validate-compute`:

1. Use `.agents/skills/ngsviz-setup/` to inspect or repair `NGSViz_setting.json`, Java 17+, the JAR, `tool_path`, and `db_path`.
2. If the user specified a genome, query the configured database first. If the genome is missing but available in the README's prebuilt catalog, use `.agents/skills/ngsviz-install-db/` to download, verify, and import it. If the catalog does not provide that genome, ask the user for a matching UCSC GTF and use `.agents/skills/ngsviz-build-db/` to build and import it.
3. If the user did not specify a genome, confirm the species first. Query the configured database for genomes belonging to that species: ask whether to use the only available genome, or ask the user to select one when multiple genomes are available. If the database does not contain the species, or the user rejects the only installed genome, query the README's prebuilt catalog next.
4. Never infer the species or assembly from a BAM filename, cell line, chromosome prefix, or common experimental practice. The presence of a database does not prove that its assembly matches the BAM alignment.
5. Resolve the environment, species, genome, and database before collecting the remaining scientific parameters and proceeding to validation.

## Workflow 1: Single-Sample Compute Only

```text
Extract requirements
-> write compute_request.json
-> Java validate-compute
-> show validation and native command
-> request confirmation
-> Java run-compute
-> read compute_manifest.json
```

If validation fails, correct the request and validate it again without requesting execution confirmation. After the user confirms, start exactly one Java process and wait for it to exit. Read that sample's `compute_manifest.json`, then stop without creating a visualization workspace or invoking R.

## Workflow 2: Sequential Multi-Sample Compute

```text
Extract shared parameters and samples[]
-> write analysis_plan.json
-> expand compute_request.<sample_id>.json
-> validate every request
-> show the ordered queue
-> request one batch confirmation
-> run one Java process and wait
-> verify its compute manifest
-> update analysis_index.json
-> only then start the next sample
```

Store the shared genome, region, strand, and fragment length under `analysis`. A sample must not override these fields; split samples with different values into separate analysis plans. Expand the `samples[]` array in order into independent, single-sample Java requests, and validate every request before running any sample. After all requests pass validation, show the fixed execution order, each native command, and the failure policy, then request confirmation once for the entire batch.

After each Java process exits, read that sample's `compute_manifest.json` and update `analysis_index.json`. Under `stop_on_error`, stop immediately after a failure. Under an explicitly selected `continue_on_error` policy, wait for the current process to exit, inspect its manifest, and update the index before continuing. Do not invoke R automatically after computation finishes.

## Workflow 3: Visualization Only

```text
receive result directory
-> R validate-plot
-> show validation and native command
-> request confirmation
-> R run-plot
-> read visualization_manifest.json
```

Use the result directory as the R `input_dir`, with plot-setting JSON files at its top level. Invoke only R: do not create a compute request, inspect BAM files, invoke Java, or recompute coverage. A later task may invoke only R. After validation succeeds and the user confirms execution, run R and report only `visualization_manifest.json`.

## Workflow 4: Full Workflow

Complete Workflow 1 or Workflow 2 first. After every started Java process has exited and its compute manifest has been read, separately ask whether the user wants to plot now or later.

- If the user chooses later, stop immediately. A subsequent task may execute Workflow 3 directly.
- If the user chooses now, create a separate visualization workspace. Copy only each completed sample's `*_NGSViz_plotSetting.json` file to the workspace's top level. Do not move or overwrite computation results or coverage CSV files. Then execute all of Workflow 3, including obtaining separate confirmation before running R.

Never proceed to R after any Java failure. Report each sample's `compute_manifest.json` separately from the workspace's `visualization_manifest.json`.
