---
name: ngsviz-agent
description: Safely orchestrate ngsViz environment preflight, species and genome database resolution, native Java computation, and R visualization for single-sample, sequential multi-sample, visualization-only, and full workflows. Use when Codex must prepare or run an ngsViz analysis, validate scientific parameters, resolve reference databases, or inspect compute and visualization manifests.
---

# ngsViz Agent

Follow the [Agent Guide](../../../agent/ngsViz_agent.md), [Architecture Boundaries](../../../agent/ai-friendly/00_native_architecture_and_collaboration_boundaries.md), [Java CLI Contract](../../../agent/ai-friendly/01_java_compute_cli_contract.md), [R CLI Contract](../../../agent/ai-friendly/02_r_visualization_cli_contract.md), and [Orchestration Contract](../../../agent/ai-friendly/03_codex_orchestration_and_confirmation_contract.md). First classify the user's intent into one of four workflows: single-sample compute only, sequential multi-sample compute, visualization only, or the full workflow.

## Hard Boundaries

- Ask about unknown, missing, or ambiguous scientific parameters. Never guess the genome, region, strand, fragment length, control, or normalization.
- Run validation only by default. Show the validation result, native command, inputs, outputs, and scope, then wait for explicit user confirmation before running the operation.
- Use Java only for single-sample computation; Java must not invoke R. Use R only to visualize existing results; R must not invoke Java.
- Allow `samples[]` in `analysis_plan.json`. Prohibit `samples[]` in Java `compute_request.json`, which may contain only one signal sample and zero or one control BAM.
- Make Codex execute multiple samples sequentially. Run Java processes strictly in series and never process samples concurrently.
- Use `stop_on_error` by default. Continue the remaining queue only when the user explicitly selects `continue_on_error`, and still wait for the current Java process to exit.
- Assign every sample a unique `sample_id`, `sample_name`, `group_name`, and `output_dir`.
- Do not plot automatically when the user requests computation only. Do not recompute when the user requests visualization only.
- Allow the user to invoke only R in a later task. Never invoke R after a Java failure.
- Report `compute_manifest.json` and `visualization_manifest.json` separately. Do not create a unified manifest.
- Use `analysis_index.json` only to index single-sample requests and manifests. Do not aggregate or rewrite scientific results.
- Do not write Codex judgments into raw result files or overwrite, move, or rewrite raw computation results.

## Java Compute Preflight

Skip this section for visualization-only workflows. Before creating any Java compute request or running `validate-compute`, complete these steps in order:

1. Read and execute [ngsviz-setup](../ngsviz-setup/SKILL.md) to inspect or repair `NGSViz_setting.json`, Java 17+, the JAR, `tool_path`, and `db_path`. Pause the compute workflow if the environment is not ready.
2. If the user specified a genome, query `defaultTbl` in the configured database:
   - If the genome is installed, continue collecting the remaining scientific parameters.
   - If it is not installed but appears in the README pre-built catalog, read and execute [ngsviz-install-db](../ngsviz-install-db/SKILL.md) to install the specified version, then rerun the environment check.
   - If the version does not appear in the README, ask the user for a UCSC GTF matching the species and assembly, then read and execute [ngsviz-build-db](../ngsviz-build-db/SKILL.md).
3. If the user did not specify a genome:
   - If no species was provided, ask for the species first. Do not treat a BAM filename, cell-line name, chromosome prefix, or chromosome length as evidence of a species or assembly.
   - If a species was provided, query the configured database for available genomes for that species. If exactly one is available, ask whether to use it; if several are available, list them and ask the user to choose.
   - If the user rejects the only installed version, or the database has no entry for that species, query the README for pre-built versions of the same species and ask the user to choose. After selection, execute `ngsviz-install-db`.
   - If the README also lacks the species, ask for a UCSC GTF and execute `ngsviz-build-db`.
4. Confirm that the genome exactly matches the assembly used to align the BAM. Database availability indicates usability, not that the assembly choice is correct.
5. Only after resolving the environment, species, genome, and database may you collect the remaining unknown scientific parameters, create the request, and run validation.

Explicitly notify the user in commentary whenever invoking a helper skill, modifying configuration, installing Java, downloading or importing a database, or pausing because a GTF is missing.

## Workflow 1: Single-Sample Compute Only

1. Complete the Java compute preflight, extract requirements, and ask for any remaining unknown scientific parameters.
2. Write a single-sample `compute_request.json` without `samples[]`.
3. Run Java `validate-compute`.
4. Show the validation result and native Java `run-compute` command, then request confirmation.
5. After confirmation, run one Java `run-compute` process and wait for it to exit.
6. Read and report `compute_manifest.json` separately, then stop without invoking R.

## Workflow 2: Sequential Multi-Sample Compute

1. Complete the Java compute preflight, extract shared parameters and `samples[]`, and write a schema-compliant `analysis_plan.json`.
2. Expand the shared parameters and each sample into separate `compute_request.<sample_id>.json` files. Generate a unique title with `${sample_name}_${analysis.title}`. Do not let a sample override genome, region, strand, or fragment length; split the analysis plan when these differ.
3. Run Java `validate-compute` on every request in array order. Do not start computation until every request validates successfully.
4. Show the fixed ordered queue, each command, unique output directories, and the failure policy, then request one batch confirmation.
5. Run only one Java `run-compute` process at a time and wait for it to exit.
6. Verify that sample's `compute_manifest.json` and update `analysis_index.json` before starting the next sample.
7. Stop immediately on failure under `stop_on_error`. Even explicit `continue_on_error` must remain sequential. Do not invoke R automatically after computation finishes.

## Workflow 3: Visualization Only

1. Accept the result directory and use it as the R `input_dir`. Place plot-setting JSON files directly in that directory.
2. Run only R `validate-plot`. Do not create a compute request or invoke Java.
3. Show the validation result and native R `run-plot` command, then request separate confirmation.
4. After confirmation, run R `run-plot` and wait for it to exit.
5. Read and report `visualization_manifest.json` separately.

## Workflow 4: Full Workflow

1. Complete Workflow 1 or Workflow 2 first. Never invoke R after any Java failure.
2. After the Java stage, separately ask whether to plot now or later.
3. If the user chooses later, stop immediately and allow a later task to execute only Workflow 3.
4. If the user chooses now, create a separate visualization workspace. Copy only each completed sample's plot-setting JSON files to its top level; do not copy, move, or overwrite coverage CSV files.
5. Execute all of Workflow 3 for the workspace and obtain separate confirmation before running R.
6. Report every Java compute manifest separately from the R visualization manifest.

## Native Command Templates

```bash
java -jar NGSViz-1.3.jar describe --format json
java -jar NGSViz-1.3.jar validate-compute --request /absolute/path/compute_request.sample_id.json
java -jar NGSViz-1.3.jar run-compute --request /absolute/path/compute_request.sample_id.json
Rscript lib/ngsVizPlotMain.R describe
Rscript lib/ngsVizPlotMain.R validate-plot --input-dir /absolute/path/visualization_workspace
Rscript lib/ngsVizPlotMain.R run-plot --input-dir /absolute/path/visualization_workspace
```
