suppressPackageStartupMessages(library(jsonlite))
root <- normalizePath("Test/feature_figure_A_to_G_reproduction_20261006")
result <- "/Users/benche/Desktop/myProj/ngsPlot/Result"
base <- file.path(result, "feature/20251010_Result")
old <- "/Users/benche/Desktop/myProj/ngsPlot/Material/plots/figure/feature/RNAseq/RNAseq_NGSViz_plotSetting.json"
inputs <- list(
  A = file.path(base, "H3K4me3_NGSViz_plotSetting.json"),
  B = file.path(base, "H3K36me3/H3K36me3_NGSViz_plotSetting.json"),
  C = file.path(result, "RNAseq_NGSViz_plotSetting.json"),
  D = file.path(base, "merge/all", paste0(c("A549", "H3K4me3", "lung"), "_NGSViz_plotSetting.json")),
  EFG = file.path(base, "merge", paste0(c("A549", "H3K4me3", "lung"), "_NGSViz_plotSetting.json"))
)
ids <- list(A = "ENCFF752MYF", B = "ENCFF975JFV", C = "ENCFF675YXM",
            D = c("ENCFF973TUQ", "ENCFF752MYF", "ENCFF822ITP"),
            EFG = c("ENCFF973TUQ", "ENCFF752MYF", "ENCFF822ITP"))
manifests <- file.path(root, "compute_results", unique(unlist(ids)), "compute_manifest.json")
use_new <- all(file.exists(manifests))
if (use_new) stopifnot(all(vapply(manifests, function(x) read_json(x)$status == "completed", logical(1))))
for (panel in names(inputs)) {
  out <- file.path(root, "visualization", panel)
  dir.create(out, recursive = TRUE, showWarnings = FALSE)
  for (i in seq_along(inputs[[panel]])) {
    style <- read_json(inputs[[panel]][i])
    setting <- if (use_new) {
      manifest <- read_json(file.path(root, "compute_results", ids[[panel]][i], "compute_manifest.json"))
      read_json(manifest$outputs$plot_setting_file)
    } else style
    csv <- sub("/Users/benche/myProj/", "/Users/benche/Desktop/myProj/",
               setting$OutputFile[[1]]$heatmapDataFile, fixed = TRUE)
    stopifnot(file.exists(csv))
    setting$OutputFile[[1]]$heatmapDataFile <- csv
    setting$OutputFile[[1]]$readCountFile <- sub("_coverage_matrix_heatmap.csv", "_gene_read_count.csv", csv, fixed = TRUE)
    setting$versionParameters$version <- "1.3"
    setting$plot_parameters$CenterMode <- FALSE
    setting$theme_paras <- style$theme_paras
    setting$theme_paras$border_color <- "black"
    setting$output_paras <- style$output_paras
    setting$output_paras$output_path <- out
    setting$split_paras <- style$split_paras
    if (panel == "C") {
      style <- read_json(old)
      setting$theme_paras <- style$theme_paras
      setting$theme_paras$border_color <- "black"
      setting$output_paras <- style$output_paras
      setting$output_paras$output_path <- out
      setting$plot_parameters$smooth <- FALSE
    }
    title <- switch(panel, A = "H3K4me3", B = "H3K36me3", C = "RNAseq",
                    D = c("A549", "K562", "lung")[i], EFG = c("A549", "K562", "lung")[i])
    setting$run_metadata <- list(sample_id = title, sample_name = title, group_name = title)
    setting$plot_parameters$sample_list <- list(title)
    setting$plot_parameters$plot_title <- list(title)
    setting$plot_parameters$smooth <- style$plot_parameters$smooth
    write_json(setting, file.path(out, paste0(i, "_", title, "_NGSViz_plotSetting.json")),
               pretty = TRUE, auto_unbox = TRUE)
  }
}
