# ngsViz AI Project Guide

## Project Purpose

ngsViz is a local analysis and visualization toolkit for BAM-based next-generation sequencing data. It calculates coverage and enrichment over genomic features, writes reusable result contracts, and renders static or interactive visualizations.

## Main Components

- **Java compute engine**: reads BAM/BAI files and the reference-coordinate database, validates compute requests, calculates coverage and read counts, and writes coverage CSV files, read-count CSV files, plot-setting JSON files, and `compute_manifest.json`.
- **R visualization CLI**: reads existing `*_NGSViz_plotSetting.json` files and their referenced CSV files, creates figures, and writes `visualization_manifest.json`.
- **R Shiny interface**: provides an interactive user interface for computation, visualization, and help. AI orchestration should use the native Java and R CLIs instead of automating the Shiny UI.
- **SQLite reference database**: stores installed species, genome assemblies, genomic feature coordinates, region definitions, and default analysis parameters.
- **Codex skills**: prepare the environment, resolve or build reference databases, validate scientific parameters, and orchestrate Java and R without becoming part of the ngsViz runtime.

## AI Entry Points

Read these files before preparing or running an ngsViz workflow:

1. `agent/ngsViz_agent.md` — complete orchestration guide.
2. `agent/ai-friendly/00_native_architecture_and_collaboration_boundaries.md` — component ownership and compatibility boundaries.
3. `agent/ai-friendly/01_java_compute_cli_contract.md` — compute request, validation, execution, outputs, and exit behavior.
4. `agent/ai-friendly/02_r_visualization_cli_contract.md` — visualization input, validation, execution, and manifest behavior.
5. `agent/ai-friendly/03_codex_orchestration_and_confirmation_contract.md` — multi-sample sequencing, confirmations, workspaces, and indexing.

Use `.agents/skills/ngsviz-agent/SKILL.md` as the workflow router. For compute workflows, follow its required preflight skills in order:

- `.agents/skills/ngsviz-setup/SKILL.md`
- `.agents/skills/ngsviz-install-db/SKILL.md` when a supported prebuilt database is missing
- `.agents/skills/ngsviz-build-db/SKILL.md` when a matching database must be built from a UCSC GTF

The README is the human-facing installation and usage reference. The AI-friendly contracts are authoritative for AI orchestration. Do not duplicate schemas, parameter tables, exit codes, or manifest definitions in this file.

## Supported Workflows

Classify each request into exactly one workflow before acting:

1. **Single-sample compute only**: validate and run one Java compute request, report its compute manifest, and stop without invoking R.
2. **Sequential multi-sample compute**: expand an analysis plan into independent single-sample requests, validate all requests, and run Java strictly in array order.
3. **Visualization only**: validate and run R against existing plot-setting JSON files without inspecting BAM files or invoking Java.
4. **Full workflow**: complete the compute stage first, then obtain a separate decision and confirmation for visualization.

## Non-Negotiable Execution Boundaries

- Never guess a species, genome assembly, region, strand, fragment length, control, normalization method, or other scientific parameter. Ask the user when a value is missing or ambiguous.
- Confirm that the selected genome assembly matches the BAM alignment. An installed database proves availability, not scientific compatibility.
- Run validation by default. Before any state-changing compute or plot command, show the validation result, native command, inputs, outputs, and execution scope, then obtain explicit user confirmation.
- Java computes exactly one signal sample per invocation and may accept at most one control BAM. A Java `compute_request.json` must never contain `samples[]`.
- Multi-sample computation is external orchestration. Run Java processes strictly sequentially; never start the next sample before the current process exits and its manifest is checked.
- Use `stop_on_error` by default. Use `continue_on_error` only when the user explicitly requests it, while preserving strict serial execution.
- Java must never invoke R, and R must never invoke Java. Do not plot automatically after a compute-only request or recompute during a visualization-only request.
- Never invoke R after a Java failure. A full workflow requires separate confirmations for `run-compute` and `run-plot`.
- Keep `compute_manifest.json` and `visualization_manifest.json` separate. `analysis_index.json` may index requests and manifests but must not aggregate or rewrite scientific results.
- Never overwrite, move, or rewrite raw inputs or computation results. Keep intermediate results, final results, and visualization workspaces in purpose-specific directories.
- Do not write AI judgments or explanations into raw result files.

## Native CLI Shape

```bash
java -jar NGSViz-1.3.jar describe --format json
java -jar NGSViz-1.3.jar validate-compute --request /absolute/path/compute_request.json
java -jar NGSViz-1.3.jar run-compute --request /absolute/path/compute_request.json

Rscript lib/ngsVizPlotMain.R describe
Rscript lib/ngsVizPlotMain.R validate-plot --input-dir /absolute/path/visualization_workspace
Rscript lib/ngsVizPlotMain.R run-plot --input-dir /absolute/path/visualization_workspace
```

Resolve the actual JAR path from `NGSViz_setting.json` and the environment preflight. Do not assume that the release JAR name and Maven build artifact name are identical.

## Repository Change Rules

- Preserve backward compatibility for legacy CLI flags unless the task explicitly changes the public interface.
- Do not change scientific behavior incidentally. Coordinate conventions, TSS/TES definitions, strand handling, gene-body scaling, binning, counting, normalization, and missing-value handling require explicit scope and tests.
- Keep scripts single-purpose and name new analysis scripts with the `NN_function` pattern.
- Keep functions focused and preferably under 50 lines.
- Write project documentation, analysis reports, change notes, and logs in Chinese unless the user explicitly requests another language.
- Write code comments and docstrings in English. Use English for identifiers and command-line output.
- Add or update a task log under `log/` after analysis or implementation work.
- Run the narrowest relevant contract, Java, R, smoke, or integration tests before declaring success.
- Inspect the worktree before staging. Preserve unrelated user changes and commit only the files belonging to the completed task.
