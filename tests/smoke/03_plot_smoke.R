#!/usr/bin/env Rscript

# Run a real plot in an isolated temporary directory.
args <- commandArgs(trailingOnly = TRUE)
if (length(args) != 1L) stop("Usage: 03_plot_smoke.R <project-root>")
project_root <- normalizePath(args[[1L]], mustWork = TRUE)
fixture_dir <- file.path(
  project_root, "analysis", "20260802_k562_h3k4me3_tss", "result"
)
work_dir <- tempfile("ngsviz-plot-smoke-")
dir.create(work_dir)
on.exit(unlink(work_dir, recursive = TRUE), add = TRUE)

setting_name <- "K562_H3K4me3_ENCFF752MYF_5p_TSS_NGSViz_plotSetting.json"
coverage_name <- "K562_H3K4me3-ENCFF752MYF_5p_coverage_matrix_heatmap.csv"
count_name <- "K562_H3K4me3-ENCFF752MYF_5p_gene_read_count.csv"
file.copy(file.path(fixture_dir, coverage_name), work_dir, overwrite = FALSE)
file.copy(file.path(fixture_dir, count_name), work_dir, overwrite = FALSE)

setting <- jsonlite::read_json(file.path(fixture_dir, setting_name), simplifyVector = FALSE)
setting$output_paras$output_path <- work_dir
setting$OutputFile[[1L]]$heatmapDataFile <- file.path(work_dir, coverage_name)
setting$OutputFile[[1L]]$readCountFile <- file.path(work_dir, count_name)
setting_path <- file.path(work_dir, setting_name)
jsonlite::write_json(setting, setting_path, auto_unbox = TRUE, pretty = TRUE)

output <- system2(
  file.path(R.home("bin"), "Rscript"),
  c(
    file.path(project_root, "lib", "ngsVizPlotMain.R"),
    "run-plot", "--input", work_dir, "--format", "json"
  ),
  stdout = TRUE,
  stderr = TRUE
)
status <- attr(output, "status")
if (!is.null(status) && status != 0L) stop(paste(output, collapse = "\n"))

expected <- file.path(work_dir, c(
  "merge_average_plot.tiff", "coverage_heatmap.tiff", "visualization_manifest.json"
))
if (!all(file.exists(expected)) || any(file.info(expected)$size <= 0L)) {
  stop("Plot smoke outputs are missing or empty.")
}
manifest <- jsonlite::read_json(expected[[3L]], simplifyVector = TRUE)
if (!identical(manifest$status, "completed")) stop("Plot manifest is not completed.")
cat("Plot smoke passed.\n")
