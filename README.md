# ngsViz

ngsViz is a local analysis and visualization toolkit for BAM-based next-generation sequencing data. It calculates coverage and enrichment over genomic features, writes reusable result contracts, and renders static or interactive visualizations.

The project provides three supported user interfaces:

1. A legacy-compatible Java command-line interface for direct analysis.
2. An R Shiny graphical interface for interactive computation and visualization.
3. An AI-agent workflow built on structured JSON requests, machine-readable responses, manifests, repository-scoped skills, and explicit execution rules.

## Table of contents

- [Features](#features)
- [Architecture](#architecture)
- [Installation](#installation)
- [Configuration and reference databases](#configuration-and-reference-databases)
- [Quick start](#quick-start)
- [Command-line interface](#command-line-interface)
- [R Shiny graphical interface](#r-shiny-graphical-interface)
- [AI agent usage](#ai-agent-usage)
- [Output interpretation](#output-interpretation)
- [Troubleshooting](#troubleshooting)
- [Version history](#version-history)
- [Contributing](#contributing)
- [License](#license)

## Features

- BAM/BAI input with reference compatibility checks.
- Coverage and read-count calculation over TSS, TES, gene body, exon, and supported custom intervals.
- Signal-only analysis or signal/control normalization.
- Configurable mapping quality, fragment length, strand mode, binning, scaling, flanking regions, and data-point resolution.
- Static average profiles and coverage heatmaps.
- Multi-sample correlation and PCA plots.
- Interactive R Shiny panels for calculation, single-sample plotting, multi-sample plotting, and quality control.
- SQLite reference-coordinate databases with pre-built and custom-build workflows.
- Backward-compatible Java short flags for users and structured native CLIs for automation.
- Machine-readable validation, stable exit codes, and separate compute and visualization manifests.
- Repository-scoped Codex skills for environment setup, reference database resolution, safe validation, sequential multi-sample execution, and deferred visualization.

## Architecture

```text
BAM + BAI + reference SQLite database
                 |
                 v
        Java compute engine
                 |
                 +-- coverage matrix CSV
                 +-- read-count CSV
                 +-- *_NGSViz_plotSetting.json
                 `-- compute_manifest.json       JSON mode only
                              |
                              v
                    R visualization CLI
                              |
                              +-- average profile
                              +-- coverage heatmap
                              +-- correlation and PCA when sample count > 1
                              `-- visualization_manifest.json
```

Java and R are independent native programs. Java never starts R, and R never computes coverage. The plot-setting JSON file is the supported contract between the two stages.

## Installation

### Requirements

| Component | Requirement | Purpose |
|---|---|---|
| Java | 17 or later | Compute engine and validation CLI |
| R | 4.x; the locked environment currently targets R 4.4 | Visualization CLI and R Shiny GUI |
| Maven | Required only when building the JAR from source | Java build and tests |
| Conda | Recommended | Reproducible Java and R environment |
| SAMtools | Optional | Create a missing BAM index |

### Clone the repository

```bash
git clone https://github.com/shaolab-UM/NGSViz.git
cd NGSViz
```

### Create the complete Conda environment

Use the lock file matching the current platform:

```bash
conda create -n ngsViz --file conda-lock/linux-64.lock
conda create -n ngsViz --file conda-lock/osx-64.lock
conda create -n ngsViz --file conda-lock/osx-arm64.lock
```

Run only one of these commands. Then install the pinned post-dependency required by the locked R environment:

```bash
conda run -n ngsViz Rscript lib/install_r_post_dependencies.R
conda activate ngsViz
```

`environment.yml` records the shared direct dependencies. The files under `conda-lock/` record exact builds and are preferred for installation.

### Build the Java application

The repository includes a runnable release JAR. To rebuild it from source:

```bash
mvn clean package
mvn test
```

The current Maven build writes `target/NGSViz-1.0.jar`; the native application version reported by `-V` and `describe` is `1.3`. Do not infer the application version from the historical JAR basename. Use the JAR selected in `NGSViz_setting.json` or verify a candidate with:

```bash
java -jar NGSViz-1.0.jar -V
java -jar NGSViz-1.0.jar describe --format json
```

## Configuration and reference databases

### System configuration

`NGSViz_setting.json` tells the GUI and helper tools where to find Java, the JAR, the project, and the SQLite reference database.

```json
{
  "versionNum": {
    "version": "1.0"
  },
  "toolParas": {
    "tool_path": "/absolute/path/to/NGSViz",
    "db_path": "/absolute/path/to/NGSViz/database/genomeCoordinate.db",
    "tool_name": "NGSViz-1.0.jar",
    "java_path": "/usr/bin/java"
  }
}
```

Use absolute paths for portable agent and GUI workflows. A direct Java command may select another configuration with `-CP /absolute/path/to/NGSViz_setting.json`. JSON-mode requests use the `system_config` field.

### BAM and chromosome naming

- Every BAM must have a readable BAI. Create one with `samtools index sample.bam` when necessary.
- The selected genome assembly must exactly match the assembly used for alignment.
- UCSC-style chromosome names such as `chr1` are native.
- BAM files using Ensembl-style names such as `1` are detected when they are otherwise compatible with the installed database.
- NCBI accession names such as `NC_000001.11` are not converted automatically.

An installed genome only proves that ngsViz can query it. It does not prove that the genome matches a BAM.

### Pre-built reference databases

The complete catalog is also available in the [shared Google Drive folder](https://drive.google.com/drive/folders/1h3tORc5PiZ_TTbIH9cH03O1wyqjhNMDz?usp=sharing).

| Organism | Scientific name | Genome version | Annotation source |
|---|---|---|---|
| Human | Homo_sapiens | [hg38](https://drive.google.com/uc?export=download&id=1XprZE54eZ14FBGrvIajy91_O9ZOfwJSW) | UCSC RefSeq |
| Human | Homo_sapiens | [hg19](https://drive.google.com/uc?export=download&id=1XTp7i780krY87Uc1JDtVrz-Y5oimuitZ) | UCSC RefSeq |
| Mouse | Mus_musculus | [mm39](https://drive.google.com/uc?export=download&id=1lsbnW18O_MsQrGr2mRq-_cBrf2sHeGBw) | UCSC RefSeq |
| Mouse | Mus_musculus | [mm10](https://drive.google.com/uc?export=download&id=1fzTrVOWF2V-VPDODo6AS30kzAPNgHyt_) | UCSC RefSeq |
| Mouse | Mus_musculus | [mm9](https://drive.google.com/uc?export=download&id=1vjIk74g29LN7a7P8nwVyJMwtRhXR-b4r) | UCSC RefSeq |
| Zebrafish | Danio_rerio | [danRer11](https://drive.google.com/uc?export=download&id=1M3sxoVc2pAqAisTjVUsjnDaKBKYJHUUI) | UCSC RefSeq |
| Zebrafish | Danio_rerio | [danRer10](https://drive.google.com/uc?export=download&id=1gWaMnpPAMKYN4kzvaaw-oW3w1dnKy4_0) | UCSC RefSeq |
| Zebrafish | Danio_rerio | [danRer7](https://drive.google.com/uc?export=download&id=12MDJu47MGpoHqqM0o5w2VRouc75Y42_0) | UCSC RefSeq |
| Fruit fly | Drosophila_melanogaster | [Dm6](https://drive.google.com/uc?export=download&id=1k5tiDT6bwRj1DE6uUsLKB4K1UbtcGOH4) | UCSC RefSeq |
| Fruit fly | Drosophila_melanogaster | [dm3](https://drive.google.com/uc?export=download&id=1Y1V5lNyegP3DZYLUKP-GwijrbxHdOxOb) | UCSC RefSeq |
| Cat | Felis_catus | [felCat9](https://drive.google.com/uc?export=download&id=1mTiasy8GgfTcaRPZ6EP5SqiWoOCDKSpT) | UCSC RefSeq |
| Cat | Felis_catus | [felCat8](https://drive.google.com/uc?export=download&id=12gMeK7iAM6KP5S1LRjlzi7gMRMy6B56h) | UCSC RefSeq |
| Cat | Felis_catus | [felCat5](https://drive.google.com/uc?export=download&id=1TZrdhjFFvFfZVlnHiCDPrNrpPrOjGUMS) | UCSC RefSeq |
| Dog | Canis_familiaris | [canFam6](https://drive.google.com/uc?export=download&id=1JbB6udpnax4ZHjwUuaKZwRq0NNnWVRoG) | UCSC RefSeq |

A downloaded database may be selected directly through `db_path` or merged into the configured writable database:

```bash
java -jar NGSViz-1.0.jar \
  -CP NGSViz_setting.json \
  -DB /absolute/path/to/NGSViz_hg38_RefSeq.db
```

Back up the primary database and verify the target genome after merging. The AI installation skill performs both checks automatically.

### Build a reference database from GTF

Use a UCSC RefSeq GTF whose filename begins with the confirmed assembly, for example `hg38.ncbiRefSeq.gtf`:

```bash
Rscript lib/buildRefDB.R \
  /absolute/path/to/hg38.ncbiRefSeq.gtf \
  RefSeq Homo_sapiens FALSE
```

| Argument | Meaning |
|---|---|
| `gtf_file` | UCSC RefSeq GTF matching the BAM assembly |
| `DB_name` | Annotation database name, such as `RefSeq` |
| `species` | Scientific name, such as `Homo_sapiens` |
| `annotatedNCRNA` | `TRUE` to refine ncRNA annotation with `biomaRt`; otherwise `FALSE` |

The builder expects transcript/exon records with `gene_name` and `transcript_id` and currently retains RefSeq `NM` and `NR` transcripts. Convert incompatible chromosome names before building; do not use mixed or NCBI accession-style names.

### Custom interval BED

Use `-BD` in the legacy CLI or `custom_bed` in a JSON request. The file must be tab-delimited, have no header, and use 0-based half-open coordinates.

| Column | Required | Meaning |
|---:|---:|---|
| 1 | Yes | Chromosome |
| 2 | Yes | Start |
| 3 | Yes | End |
| 4 | No | Strand, `+` or `-` |
| 5 | No | Gene or element name |
| 6 | No | Transcript or stable element ID |

When optional columns are absent, ngsViz assumes the `+` strand and generates stable IDs from coordinates.

## Quick start

The repository includes a BAM/BAI pair, an installed reference database, and example visualization results.

### 1. Check the native interfaces

```bash
java -jar NGSViz-1.0.jar -V
java -jar NGSViz-1.0.jar describe --format json
Rscript lib/ngsVizPlotMain.R describe
```

### 2. Run a small legacy-CLI computation

```bash
mkdir -p analysis/readme_quick_start

java -jar NGSViz-1.0.jar \
  -CP NGSViz_setting.json \
  -G hg38 \
  -R tss \
  -I Samples/K562_H3K4me3-ENCFF752MYF_5p.bam \
  -O analysis/readme_quick_start \
  -T K562_H3K4me3_TSS \
  -B protein_coding \
  -P 1
```

### 3. Validate and render a bundled result in a separate workspace

```bash
mkdir -p analysis/readme_visualization
cp Samples/Result/H3K4me3_point/H3K4me3_NGSViz_plotSetting.json \
  analysis/readme_visualization/

Rscript lib/ngsVizPlotMain.R validate-plot \
  --input-dir analysis/readme_visualization

Rscript lib/ngsVizPlotMain.R run-plot \
  --input-dir analysis/readme_visualization
```

The visualization command writes `merge_average_plot.tiff`, `coverage_heatmap.tiff`, and `visualization_manifest.json` in the input directory.

## Command-line interface

### Direct Java computation

The user-facing compute entry point is:

```bash
java -jar NGSViz-1.0.jar -G <genome> -R <region> -I <input> -O <output> -T <title> [options]
```

Run `java -jar NGSViz-1.0.jar -H` for the native help and `-V` for the application version.

#### Parameters

| Flag | Required | Default | Description |
|---|---:|---|---|
| `-G` | Yes | None | Reference genome installed in the selected database |
| `-R` | Yes | None | Region type, such as `tss`, `tes`, `genebody`, or `exon` |
| `-I` | Yes | None | Signal BAM, `signal.bam:control.bam`, or legacy TXT/CSV sample sheet |
| `-O` | Yes | None | Output directory |
| `-T` | Yes | None | Analysis and plot title |
| `-CP` | No | JAR-adjacent config | Alternate `NGSViz_setting.json` |
| `-D` | No | `RefSeq` | Reference annotation database type |
| `-A` | No | `transcript` | Analysis type: `transcript` or `exon` |
| `-B` | No | `protein_coding` | Annotation biotype |
| `-F` | No | Region-dependent | Flank size in base pairs |
| `-N` | No | `0.0` | Gene-body flank scaling factor in `[0,1]` |
| `-DP` | No | `100` | Number of data points; minimum `100` |
| `-MQ` | No | `20` | Minimum mapping quality |
| `-FL` | No | `150` | Single-end fragment length |
| `-P` | No | `1` | CPU cores |
| `-BS` | No | `500` | Gene batch size |
| `-BM` | No | `mean` | Bin summary: `mean`, `median`, or `max` |
| `-SS` | No | `both` | Strand mode: `both`, `same`, or `opposite` |
| `-S` | No | CPM | Signal scale ratio; when absent, retain CPM normalization |
| `-BD` | No | None | Custom BED interval file |
| `-X` | No | `all` | Gene subset or gene-list path |
| `-NF` | No | `false` | Create a title-named directory under `-O` |
| `-CM` | No | `false` | Align selected intervals by their center |
| `-DB` | Administrative | None | Merge a reference database and exit |
| `-H` | Informational | None | Print help and exit |
| `-V` | Informational | None | Print version and exit |
| `-J` | Deprecated | None | Retained parser placeholder; it does not load AI configuration |

The default flank size is `2000` for `tss`, `tes`, `genebody`, `exon`, and `cgi`; `1500` for `enhancer`; and `1000` for `dhs` and `bed` when those regions are available.

#### Extended compute example

```bash
java -jar NGSViz-1.0.jar \
  -CP NGSViz_setting.json \
  -G hg38 \
  -R genebody \
  -I Samples/K562_H3K36me3-ENCFF975JFV_5P.bam \
  -O analysis/readme_extended_compute \
  -T K562_H3K36me3_gene_body \
  -BM mean \
  -MQ 20 \
  -FL 150 \
  -P 1
```

One direct computation writes a coverage matrix CSV, a read-count CSV, and a `*_NGSViz_plotSetting.json`. The legacy flag entry point does not provide the same machine-oriented request/response guarantees as JSON mode.

### R visualization CLI

The native R CLI accepts an existing directory, not an individual JSON or CSV file:

```bash
mkdir -p analysis/readme_broad_visualization
cp Samples/Result/K36me3_broad/H3K36me3_NGSViz_plotSetting.json \
  analysis/readme_broad_visualization/

Rscript lib/ngsVizPlotMain.R describe
Rscript lib/ngsVizPlotMain.R validate-plot --input-dir analysis/readme_broad_visualization
Rscript lib/ngsVizPlotMain.R run-plot --input-dir analysis/readme_broad_visualization
```

The legacy positional form remains supported:

```bash
Rscript lib/ngsVizPlotMain.R analysis/readme_broad_visualization
```

Only top-level files matching `*_NGSViz_plotSetting.json` are discovered. One plot-setting file produces an average profile and heatmap. More than one compatible file also produces correlation and PCA plots. All plot-setting files in one run must agree on matrix shape, row order, interval settings, labels, and output type.

## R Shiny graphical interface

### Start the application

Run the application from the repository root so `here()` resolves project files correctly:

```bash
conda activate ngsViz
R -e "shiny::runApp('ngsViz-Shiny.R', host='127.0.0.1', port=3838)"
```

Open `http://127.0.0.1:3838` in a browser. To expose the app on a trusted network, change the host to `0.0.0.0` and apply appropriate network access controls.

`ngsViz-Shiny.R` returns a `shinyApp` object; it is not a separate command-line launcher.

### Interface panels

| Panel | Purpose |
|---|---|
| Home | Project overview |
| CalculationMode | Select genome and region, provide BAM/output paths, adjust compute parameters, and run the Java engine |
| SingleCoveragePlot | Parse one plot-setting JSON, adjust plot/theme options, and render a single-sample profile and heatmap |
| MultiSamplePlot | Parse a directory of plot-setting JSON files and render combined average and integrated coverage views |
| QCPlot | Render coverage correlation and PCA views |
| Help | Display this README inside the application |
| AboutUs | Project and team information |

The repository currently includes the ngsViz logo but no maintained GUI screenshot. A screenshot is therefore not embedded here; the panel descriptions above follow the current UI source.

The GUI is intended for interactive use. AI agents must use the native Java and R CLIs and must not automate the GUI.

## AI agent usage

### Why AI agents use a different interface

Legacy flags are convenient for a person typing one command, but an agent needs stronger contracts:

- Typed JSON fields instead of shell-assembled scientific parameters.
- Read-only validation before state-changing execution.
- Stable JSON response envelopes and exit codes.
- Explicit artifact acceptance and manifests.
- A single-sample Java boundary that prevents ambiguous batch behavior.
- Separate compute and visualization confirmations.
- Deterministic, sequential orchestration for multiple samples.

The AI mode does not add a new analysis engine, Python runner, MCP service, Web API, or combined Java/R process. It orchestrates the same native Java computation and R plotting logic used by other interfaces.

### Skills and rules

The `skill` is a repository-scoped Codex skill: a Markdown instruction package with YAML front matter, stored under `.agents/skills/`. It is not a Claude plugin and is not part of the ngsViz runtime.

| Path | Responsibility |
|---|---|
| `.agents/skills/ngsviz-agent/SKILL.md` | Workflow router and execution boundaries |
| `.agents/skills/ngsviz-setup/SKILL.md` | Java, JAR, configuration, and SQLite preflight |
| `.agents/skills/ngsviz-install-db/SKILL.md` | Pre-built database discovery, verification, and installation |
| `.agents/skills/ngsviz-build-db/SKILL.md` | UCSC RefSeq GTF validation and custom database build |
| `AGENTS.md` | Repository-wide agent rules and authoritative document routing |
| `agent/ai-friendly/*.md` | Native Java, native R, architecture, orchestration, and confirmation contracts |

The accompanying rules are operational safety and validation contracts, not a prompt template or a naming convention alone. They define parameter ownership, required confirmations, input schemas, output acceptance, process ordering, failure behavior, and prohibited cross-stage actions.

### Environment preparation

1. Start the agent with the repository as its working directory.
2. Ensure the agent can read `AGENTS.md` and `.agents/skills/`.
3. Ask it to use `ngsviz-agent`; compatible Codex environments discover repository-scoped skills from `.agents/skills`.
4. For compute work, the router invokes `ngsviz-setup` before creating a request. Visualization-only work skips Java preflight.
5. If another agent framework does not discover Codex skills automatically, explicitly instruct it to read `AGENTS.md`, `.agents/skills/ngsviz-agent/SKILL.md`, and the linked contracts before acting. The rules remain usable, but automatic skill discovery is framework-specific.

A read-only environment audit can be run directly:

```bash
python3 .agents/skills/ngsviz-setup/scripts/check_ngsviz_environment.py \
  --tool-path "$PWD"
```

The audit must report `status=ready` before an agent creates or validates a compute request.

### Native AI-friendly commands

```bash
java -jar NGSViz-1.0.jar describe --format json
java -jar NGSViz-1.0.jar validate-compute --request /absolute/path/to/compute_request.json
java -jar NGSViz-1.0.jar run-compute --request /absolute/path/to/compute_request.json

Rscript lib/ngsVizPlotMain.R describe
Rscript lib/ngsVizPlotMain.R validate-plot --input-dir /absolute/path/to/visualization_workspace
Rscript lib/ngsVizPlotMain.R run-plot --input-dir /absolute/path/to/visualization_workspace
```

JSON mode and legacy flags are mutually exclusive. An agent must never append `-G`, `-R`, or other legacy compute flags to `validate-compute` or `run-compute`.

### Input contract

A Java `compute_request.json` describes exactly one signal sample and zero or one control BAM. It must never contain `samples`.

The repository includes a complete executable example at `agent/ai-friendly/examples/compute_request.single.json`. Its important groups are:

| Group | Fields |
|---|---|
| Identity | `sample_id`, `sample_name`, `group_name`, `title` |
| Scientific scope | `genome`, `region`, `database`, `analysis_type`, `biotype` |
| Inputs and output | `signal_bam`, `control_bam`, `output_dir`, `system_config`, `custom_bed` |
| Computation | `flank_region`, `num_datapoints`, `mapping_quality`, `fragment_length`, `cores`, `bin_method`, `strand_specific`, and related options |

See `agent/ai-friendly/01_java_compute_cli_contract.md` for the authoritative field table and defaults. Unknown fields, wrong JSON types, missing required fields, and a top-level `samples` field fail validation.

For multiple samples, the agent may read `analysis_plan.json`, whose `samples[]` order defines the queue. The plan is an orchestration input only: Java and R never read it. The schema and complete example are:

- `agent/ai-friendly/schemas/analysis_plan.schema.json`
- `agent/ai-friendly/examples/analysis_plan.multi_sample.json`

The agent expands the plan into one independently valid Java request per sample.

For a multi-sample plan, it validates every expanded request before starting computation, displays one fixed queue, requests one batch confirmation, and runs Java strictly in `samples[]` order. After each process exits, it verifies that sample's `compute_manifest.json` and updates `analysis_index.json`. The default policy is `stop_on_error`; an explicitly selected `continue_on_error` policy still runs one process at a time.

### Output contract and parsing

Every native AI-friendly command ends stdout with one JSON response envelope:

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

An agent must:

1. Check the process exit code and require it to match `exit_code`.
2. Require `status` to be `success` before continuing.
3. Read resolved values from `data`, not from incidental stderr diagnostics.
4. Report every warning and error without silently changing scientific parameters.
5. After execution, open the manifest path returned in `data` and verify its top-level status and accepted artifacts.

| Artifact | Owner | Meaning |
|---|---|---|
| `compute_request.json` | Agent | One single-sample Java request |
| `analysis_plan.json` | Agent | Ordered multi-sample orchestration input |
| `compute_manifest.json` | Java | One sample's compute status, resolved parameters, CSV files, plot-setting file, and log |
| `analysis_index.json` | Agent | Ordered index of requests and compute manifests; not proof of scientific success |
| `visualization_manifest.json` | R | One visualization run's inputs, figures, validation state, and errors |

Do not merge compute and visualization state into one manifest.

### Example 1: validate and run one sample

User request to the agent:

> Use the ngsviz-agent skill. Validate `agent/ai-friendly/examples/compute_request.single.json`, show me the resolved parameters and exact run command, and wait for my confirmation before computing. Do not plot.

The agent performs the environment preflight and then runs:

```bash
java -jar NGSViz-1.0.jar validate-compute \
  --request "$PWD/agent/ai-friendly/examples/compute_request.single.json"
```

A successful validation returns `status: success`, `exit_code: 0`, supported values, and `data.resolved_parameters`. The agent shows these values and this exact pending command:

```bash
java -jar NGSViz-1.0.jar run-compute \
  --request "$PWD/agent/ai-friendly/examples/compute_request.single.json"
```

Only after explicit confirmation does the agent run it, wait for the Java process, and inspect:

```text
agent/ai-friendly/examples/validate_compute_output/compute_manifest.json
```

It reports the manifest and stops. It does not invoke R because the request was compute-only.

### Example 2: compute followed by separately confirmed visualization

User request to the agent:

> Use the ngsviz-agent skill to run `agent/ai-friendly/examples/compute_request.single.json`. Validate first and wait for compute confirmation. If computation succeeds, ask me separately whether to visualize the result in `agent_output/readme_full_workflow/visualization`.

The agent performs these steps:

1. Preflight Java, the configured database, and the confirmed `hg38` assembly.
2. Validate the single-sample request.
3. Show the resolved scientific parameters and exact compute command.
4. Wait for explicit compute confirmation.
5. Run Java, wait for it to exit, and require a completed compute manifest.
6. Ask whether to visualize now or later.
7. If the user chooses now, create a separate workspace and copy only the plot-setting JSON into it.
8. Validate the R input, show the exact plot command, and wait for a second confirmation.

The resulting native command sequence is:

```bash
java -jar NGSViz-1.0.jar validate-compute \
  --request "$PWD/agent/ai-friendly/examples/compute_request.single.json"
java -jar NGSViz-1.0.jar run-compute \
  --request "$PWD/agent/ai-friendly/examples/compute_request.single.json"
```

The second command is run only after the first command succeeds and the user confirms it. If Java fails or its manifest is missing or failed, the workflow stops without invoking R.

After successful computation, the agent asks whether to plot now or later. If the user chooses now, it creates the workspace without modifying the compute directory:

```bash
mkdir -p "$PWD/agent_output/readme_full_workflow/visualization"
cp "$PWD/agent/ai-friendly/examples/validate_compute_output/K562_H3K4me3_TSS_NGSViz_plotSetting.json" \
  "$PWD/agent_output/readme_full_workflow/visualization/"

Rscript lib/ngsVizPlotMain.R validate-plot \
  --input-dir "$PWD/agent_output/readme_full_workflow/visualization"
```

After a second, independent confirmation:

```bash
Rscript lib/ngsVizPlotMain.R run-plot \
  --input-dir "$PWD/agent_output/readme_full_workflow/visualization"
```

The agent reports `validate_compute_output/compute_manifest.json` separately from `visualization/visualization_manifest.json`.

### Boundaries and limitations

- Never guess species, genome assembly, region, strand mode, fragment length, control, normalization, or another scientific parameter.
- Confirm that the selected genome matches the BAM alignment assembly.
- Run validation before every compute or plot execution.
- Show the validation result, exact command, inputs, outputs, and scope before requesting confirmation.
- A compute confirmation does not authorize plotting; full workflows require a second confirmation.
- Java processes one signal sample and at most one control per JSON request.
- Multi-sample Java computation is strictly sequential, even with `continue_on_error`.
- Never plot automatically after compute-only work, recompute during visualization-only work, or invoke R after a Java failure.
- Never overwrite, move, or rewrite raw BAMs, coverage CSV files, plot-setting JSON files, or completed computation results.
- Never write agent judgments into raw result files.
- Do not use YAML, `-J`, legacy compute flags, Python intermediates, MCP, or a Web API as the AI parameter interface.
- Do not treat `analysis_index.json` as a replacement for a native manifest.

The authoritative rules are in `agent/ai-friendly/` and `.agents/skills/ngsviz-agent/SKILL.md`.

## Output interpretation

### Coverage matrix

- Rows represent genes or queried genomic elements.
- The first column contains stable row identifiers.
- Remaining columns are numeric coverage positions.
- Values inside each bin are summarized with `mean`, `median`, or `max`.
- Without a scale ratio, coverage retains CPM normalization.
- With `scale_ratio`, the existing behavior applies `signal * scale_ratio`.
- With a control BAM, the existing background-normalization logic is applied.

For single-end data, `fragment_length` controls read-aware extension used to better represent the estimated DNA fragment. Select it from library characteristics; do not infer it from a filename.

### Compute artifacts

| Artifact | Description |
|---|---|
| `*_coverage_matrix_heatmap.csv` | Coverage matrix used by plotting |
| `*_gene_read_count.csv` | Per-feature read counts |
| `*_NGSViz_plotSetting.json` | Java-to-R plotting contract |
| `NGSViz_compute.log` | Compute diagnostics in JSON mode |
| `compute_manifest.json` | Accepted Java artifacts and resolved request in JSON mode |

### Visualization artifacts

| Sample count | Output |
|---:|---|
| Any | `merge_average_plot.<file_type>` |
| Any | `coverage_heatmap.<file_type>` |
| More than one | `correlation_plot.tiff` |
| More than one | `PCA_plot.<file_type>` |
| Any native run | `visualization_manifest.json` |

Supported native CLI figure types are `tiff`, `png`, and `pdf`.

## Troubleshooting

### The JAR name and reported version differ

The current Maven `finalName` is historical. Use `NGSViz_setting.json` and verify the selected file with `-V` or `describe --format json`. Do not rename files solely to make the basename match version 1.3.

### The system configuration cannot be found

Without `-CP`, Java looks for `NGSViz_setting.json` beside the running JAR. Supply an explicit absolute configuration path or place a valid file beside the JAR.

### BAM validation fails

- Confirm the BAM exists and is readable.
- Confirm `sample.bam.bai` or another usable BAI is present.
- Run `samtools quickcheck sample.bam` and `samtools index sample.bam` when appropriate.
- Confirm the genome assembly and chromosome naming match the selected database.

### The genome or region is unavailable

Inspect `defaultTbl` in the configured SQLite database. Install a matching pre-built database or build one from a compatible UCSC RefSeq GTF. Do not substitute a similar assembly.

### `validate-plot` reports no candidates

Place at least one regular `*_NGSViz_plotSetting.json` file directly in `input_dir`. The R CLI does not search recursively and does not infer inputs from `compute_manifest.json`.

### Multi-sample plotting fails

All plot-setting files must describe compatible matrices and plotting parameters. Row identifiers, row order, coverage-column count, interval type, labels, flank settings, and output file type must agree.

### R reports missing packages

Recreate the environment from the platform lock file and run:

```bash
conda run -n ngsViz Rscript lib/install_r_post_dependencies.R
```

Validation never installs packages automatically.

### Exit codes for native AI-friendly commands

| Code | Meaning |
|---:|---|
| `0` | Success |
| `2` | CLI, request, or schema error |
| `3` | Missing environment or dependency |
| `4` | Invalid compute or visualization input |
| `5` | Execution failure |
| `6` | Output artifact acceptance failure |

## Version history

The repository does not currently maintain a standalone `CHANGELOG.md`. Relevant milestones visible in the project history are:

- **1.3**: Native `describe`, `validate-compute`, and `run-compute` Java commands; native R validation and manifest workflow; machine-readable responses; repository-scoped Codex skills; sequential multi-sample orchestration rules; independent Java/R confirmations.
- **1.2**: Binning and scaling documentation updates, container-era distribution work, and compatibility fixes.
- **1.0**: Original Java computation, R visualization, and R Shiny workflow.

This README removes obsolete syntax such as `Rscript lib/ngsVizPlotMain.R -O ...`, replaces the non-rendering `[TOC]` marker, and moves low-level schemas and exit behavior to the authoritative AI-friendly contracts.

## Contributing

1. Create a focused branch.
2. Preserve legacy CLI compatibility unless the change explicitly updates the public contract.
3. Do not change scientific behavior incidentally.
4. Add or update focused Java, R, contract, smoke, or integration tests.
5. Update the relevant contract and README when a public interface changes.
6. Add a task log under `log/`.
7. Open a pull request against the repository.

Useful checks include:

```bash
mvn test
Rscript tests/r/run_r_tests.R
bash tests/contracts/02_check_module_independence.sh
```

## License

ngsViz is distributed under the GNU General Public License, version 3. See [LICENSE](LICENSE).
