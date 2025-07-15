#!/usr/bin/env Rscript

args <- commandArgs(trailingOnly = TRUE)

library(here)

source(here("lib", "processJSON.R"))
source(here("lib", "coverageHeatmap.R"))
source(here("lib", "qualityControlViz.R"))

parse_args <- function(args) {
  if (length(args) != 1) {
    cat("Usage: Rscript your_script.R <json_path>\n")
    cat("<json_path> : Path of the JSON folder!\n")
    quit(status = 1)
  }
  
  list(json_path = args[1])
}

getJsonFiles <- function(path) {
  json_files <- list.files(path, pattern = "\\.json$", full.names = FALSE)
  return(as.list(json_files))
}

Main <- function(args){
  # Parse parameters
  params <- parse_args(args)
  
  # Check if json_path is provided
  if (is.null(params$json_path)) {
    cat("Error: JSON path is required!\n")
    cat("Usage: Rscript your_script.R <json_path>\n")
    cat("<json_path> : Path of the JSON folder!\n")
    quit(status = 1)
  }
  
  if (!dir.exists(params$json_path)) {
    stop(paste("Error: folder of Json not exist ->", params$json_path))
  }

  cat("JSON Path:", params$json_path, "\n")
  mergeAveragePlot(json_path) # , title_name
  mergeMultiVisResult(json_path)
  heatmapMat_rowMerge <- propressData(json_path) #  , row_merge_method
  plot_para_list <- getJsonFiles(json_path)
  if(length(plot_para_list)>1){
    corPlot(json_path, heatmapMat_rowMerge) 
    runPCA(heatmapMat_rowMerge, json_path)
  }
  
}

Main(args)

# usage -------------------------------------------------------------------
# json_path <- "/Users/benche/myProj/ngsPlot/Output/Test/H3K4"

