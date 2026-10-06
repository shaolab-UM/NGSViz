suppressPackageStartupMessages(library(jsonlite))
root <- normalizePath("Test/feature_figure_A_to_G_reproduction_20261006")
out <- file.path(root, "compute_requests")
dir.create(out, showWarnings = FALSE)
data <- "/Users/benche/Desktop/myProj/ngsPlot/Test/Data/featurePlot"
config <- "/Users/benche/Desktop/myProj/ngsPlot/NGSViz/NGSViz_setting.json"
mouse_config <- file.path(data, "feature_fig3A-C/NGSViz_setting.json")
samples <- data.frame(
  id = c("ENCFF752MYF", "ENCFF975JFV", "ENCFF675YXM", "ENCFF973TUQ", "ENCFF822ITP"),
  name = c("K562_H3K4me3", "K562_H3K36me3", "RNAseq", "A549_H3K4me3", "lung_H3K4me3"),
  group = c("K562", "K562", "RNAseq", "A549", "lung"),
  region = c("tss", "genebody", "exon", "tss", "tss"),
  bam = c("feature_fig3A-C/K562-H3K4me3-ENCFF752MYF.bam",
          "feature_fig3A-C/K562-H3K36me3-ENCFF975JFV.bam",
          "feature_fig3A-C/ENCFF675YXM.bam",
          "feature_fig3D/A549-H3K4me3-ENCFF973TUQ.bam",
          "feature_fig3D/lung-H3K4me3-ENCFF822ITP.bam")
)
for (i in seq_len(nrow(samples))) {
  mouse <- samples$id[i] == "ENCFF675YXM"
  request <- list(
    schema_version = "1.3", sample_id = samples$id[i], sample_name = samples$name[i],
    group_name = samples$group[i], title = samples$name[i],
    genome = if (mouse) "mm10" else "hg38", region = samples$region[i],
    signal_bam = file.path(data, samples$bam[i]),
    output_dir = file.path(root, "compute_results", samples$id[i]),
    system_config = if (mouse) mouse_config else config,
    database = "RefSeq", analysis_type = if (mouse) "exon" else "transcript",
    biotype = "protein_coding", flank_region = 2000L, flank_factor = 0,
    num_datapoints = 100L, mapping_quality = 20L, fragment_length = 150L,
    cores = if (mouse) 4L else 1L, batch_size = 500L, bin_method = "mean",
    strand_specific = "both", center_mode = FALSE, gene_subset = "all",
    create_result_folder = FALSE
  )
  stopifnot(file.exists(request$signal_bam), file.exists(paste0(request$signal_bam, ".bai")))
  write_json(request, file.path(out, sprintf("%02d_%s.json", i, samples$id[i])), pretty = TRUE, auto_unbox = TRUE)
}
