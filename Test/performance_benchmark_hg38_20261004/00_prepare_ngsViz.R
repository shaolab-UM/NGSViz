library(jsonlite)
args <- commandArgs(TRUE)
root <- normalizePath(args[1])
cores <- as.integer(args[2])
project <- normalizePath(file.path(root, "../../.."))
dir.create(file.path(root, "config"), showWarnings = FALSE)
dir.create(file.path(root, "requests", paste0("p", cores)), recursive = TRUE, showWarnings = FALSE)
config <- list(versionNum = list(version = "1.3"), toolParas = list(
  tool_path = file.path(project, "NGSViz"),
  db_path = file.path(project, "NGSViz/database/genomeCoordinate.db"),
  tool_name = "NGSViz-1.3.jar",
  java_path = "/usr/lib/jvm/java-17-openjdk-amd64/bin/java"))
config_path <- file.path(root, "config/NGSViz_setting.json")
if (!file.exists(config_path)) write_json(config, config_path, auto_unbox = TRUE, pretty = TRUE)
for (run in 1:3) for (fraction in c("0.1", "0.25", "0.5", "0.75", "1.0")) {
  id <- paste0("p", cores, "_run_", run, "_", fraction)
  path <- file.path(root, "requests", paste0("p", cores), paste0(id, ".json"))
  if (file.exists(path)) next
  request <- list(schema_version = "1.3", sample_id = id, sample_name = id,
    group_name = id, title = paste0("GSE208913_H3K27me3_", id), genome = "hg38",
    region = "genebody", signal_bam = file.path(project, "Test/Data",
      paste0("GSE208913_H3K27me3_", fraction, ".sorted.bam")), control_bam = NULL,
    output_dir = file.path(root, "results", paste0("p", cores), "ngsViz",
      paste0("run_", run), fraction), system_config = config_path, database = "RefSeq",
    analysis_type = "transcript", biotype = "protein_coding", flank_region = 2000L,
    flank_factor = 0, num_datapoints = 100L, scale_ratio = NULL, mapping_quality = 20L,
    fragment_length = 150L, cores = cores, batch_size = 500L, bin_method = "mean",
    strand_specific = "both", center_mode = FALSE, custom_bed = NULL,
    gene_subset = "all", create_result_folder = FALSE)
  write_json(request, path, auto_unbox = TRUE, pretty = TRUE, null = "null")
}
