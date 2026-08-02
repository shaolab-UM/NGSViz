#!/usr/bin/env Rscript

script_arg <- grep("^--file=", commandArgs(trailingOnly = FALSE), value = TRUE)
script_file <- normalizePath(sub("^--file=", "", script_arg[1]))
input_file <- file.path(
  dirname(script_file),
  "result",
  "K562_H3K4me3-ENCFF752MYF_5p_coverage_matrix_heatmap.csv"
)
output_dir <- file.path(dirname(dirname(input_file)), "summary")
dir.create(output_dir, recursive = TRUE, showWarnings = FALSE)

coverage <- read.csv(input_file, row.names = 1, check.names = FALSE)
profile <- colMeans(coverage)
positions_bp <- seq(-2000, 2000, length.out = length(profile))
peak_index <- which.max(profile)
zero_rows <- rowSums(abs(as.matrix(coverage))) == 0

summary_table <- data.frame(
  metric = c(
    "record_count", "coverage_columns", "peak_position_bp",
    "peak_mean_coverage", "tss_mean_coverage", "zero_coverage_records",
    "zero_coverage_fraction"
  ),
  value = c(
    nrow(coverage), ncol(coverage), positions_bp[peak_index],
    profile[peak_index], profile[positions_bp == 0], sum(zero_rows), mean(zero_rows)
  )
)

write.csv(
  summary_table,
  file.path(output_dir, "tss_profile_summary.csv"),
  row.names = FALSE
)
