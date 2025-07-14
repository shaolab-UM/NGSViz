#!/usr/bin/env Rscript

args <- commandArgs(trailingOnly = TRUE)

parse_args <- function(args) {
  title_name <- NULL
  json_path <- NULL
  i <- 1
  while (i <= length(args)) {
    if (args[i] == "-t" && i + 1 <= length(args)) {
      title_name <- args[i + 1]
      i <- i + 2
    } else if (args[i] == "-d" && i + 1 <= length(args)) {
      json_path <- args[i + 1]
      i <- i + 2
    } else {
      i <- i + 1
    }
  }
  
  list(title_name = title_name, json_path = json_path)
}

# Parse parameters
params <- parse_args(args)

# Check if the parameter exists
if (is.null(params$title_name) || is.null(params$json_path)) {
  cat("Usage: Rscript your_script.R -t <title_name> -d <json_path>\n")
  cat("-t <title_name> : average coverage plot title!\n")
  cat("-d <json_path> : Path of the JSON folder!\n")
  quit(status = 1)
}


if (!dir.exists(params$json_path)) {
  stop(paste("Error: folder of Json not exist ->", params$json_path))
}

cat("Title Name:", params$title_name, "\n")
cat("JSON Path:", params$json_path, "\n")


library(here)

source(here("lib", "processJSON.R"))
source(here("lib/plot", "coverageHeatmap.R"))
source(here("lib/plot", "qualityControlViz.R"))


mergeAveragePlot(json_path, title_name)
mergeMultiVisResult(json_path)
heatmapMat_rowMerge <- propressData(json_path, row_merge_method)
corPlot(json_path, heatmapMat_rowMerge) 
runPCA(heatmapMat_rowMerge, plot_paras)