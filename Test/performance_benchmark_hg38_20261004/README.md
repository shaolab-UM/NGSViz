# hg38 performance benchmark scripts

Run these scripts inside the existing `ngsplot-work` container. The host directory `/Users/benche/Desktop/myProj/ngsPlot` is mounted at `/home/rstudio/ngsPlot`. This directory contains reproducibility scripts. The completed benchmark results remain in `/home/rstudio/ngsPlot/Test/benchmarks/20261003_container_hg38` (four cores) and `20261004_container_hg38_p1` (one core).

| Tool | One-core entry point | Four-core entry point |
| --- | --- | --- |
| ngsViz v1.3 | `02_ngsViz_1core.sh` | `03_ngsViz_4core.sh` |
| deepTools v3.5.6 | `04_deepTools_1core.sh` | `05_deepTools_4core.sh` |
| ngs.plot v2.63 | `06_ngsplot_1core.sh` | `07_ngsplot_4core.sh` |

Each entry point processes BAM fractions 0.1, 0.25, 0.5, 0.75, and 1.0 with three runs per fraction. Example:

```bash
docker exec ngsplot-work bash /home/rstudio/ngsPlot/NGSViz/Test/performance_benchmark_hg38_20261004/02_ngsViz_1core.sh
```

Replace the final script name to run another configuration. Outputs are written under this directory's `results/p1` or `results/p4`. Existing outputs are never overwritten. Runs are sequential. Each run records its complete command, stdout, stderr, and GNU `time` measurements. The deepTools wrapper additionally records `bamCoverage_time.tsv` and `computeMatrix_time.tsv` in future reruns; the completed benchmark did not record stage-specific times.

| Parameter | ngsViz | deepTools | ngs.plot |
| --- | --- | --- | --- |
| Genome and regions | hg38 RefSeq protein-coding transcripts, gene body; 19,303 records | 19,303 BED6 regions derived from `hg38_ngsViz.bed` | Built-in hg38 Ensembl protein-coding gene bodies |
| Flanks | 2,000 bp on each side | 2,000 bp on each side | Not specified; program default |
| Binning or scaling | 100 data points; mean binning | `bamCoverage --binSize 50`; `computeMatrix scale-regions --regionBodyLength 5000`; default matrix bin size 10 bp | Default spline algorithm |
| Read filtering | MAPQ >=20; fragment length 150 bp; both strands | No explicit MAPQ, duplicate, or fragment filters; program defaults | Default MAPQ >=20, fragment length 150 bp, both strands |
| Normalization | CPM (`scale_ratio=null`) | `bamCoverage --normalizeUsing CPM` | No explicit CPM option; program default |
| Threads | `cores=1/4` | `--numberOfProcessors 1/4` and `-p 1/4` | `-P 1/4` |
| Timed scope | Java `run-compute`; R plotting excluded | BAM-to-bigWig plus coverage matrix; plotting excluded | Coverage calculation plus built-in average-profile and heatmap plotting |

The deepTools matrix command uses `--skipZeros` to exclude regions with all-zero coverage. The deepTools BED6 file is generated from the original `hg38_ngsViz.bed` before the first deepTools run: the 1-based closed start coordinate is decremented and columns are reordered. `00_prepare_ngsViz.R` generates the ngsViz request JSON files, and `01_run.sh` validates each request before compute. `01_run.sh` is the shared driver; `08_deepTools_pipeline.sh` contains the two native deepTools commands.

The tools use the same BAM files, but their annotation collections, binning, filtering, normalization, and plotting scope differ. These measurements describe the specified workflows rather than algorithm speed for identical computations. GNU `time -f %M` reports maximum resident set size in KiB; it is not the sum of concurrent child-process memory.
