#!/usr/bin/env Rscript

# Get the absolute path of the current script
getScriptPath <- function() {
  cmd_args <- commandArgs(trailingOnly = FALSE)
  # Find the --file= argument, which is passed when running with Rscript
  file_arg <- grep("--file=", cmd_args, value = TRUE)
  if (length(file_arg) > 0) {
    # Extract the file path and convert to absolute path
    script_path <- normalizePath(sub("--file=", "", file_arg))
  } else {
    # If running interactively in R, try using sys.frame
    script_path <- normalizePath(sys.frame(1)$ofile)
  }
  return(script_path)
}


getJsonFiles <- function(path) {
  json_files <- list.files(path, pattern = "\\.json$", full.names = FALSE)
  return(as.list(json_files))
}

Main <- function(args){
  # Get the directory of the script
  script_dir <- dirname(getScriptPath())
  # Print the script path and directory
  message(paste0("Script directory:", script_dir, "\n"))
  message("Load the library")
  # Source scripts from the same directory as the current script
  source(file.path(script_dir, "processJSON.R"))
  source(file.path(script_dir, "coverageHeatmap.R"))
  source(file.path(script_dir, "qualityControlViz.R"))
  
  # Parse parameters
  if (length(args) != 1) {
    cat("Usage: Rscript your_script.R <json_path>\n")
    cat("<json_path> : Path of the JSON folder!\n")
    quit(status = 1)
  }
  json_path <- args[1]
  
  
  # Check if json_path is provided
  if (is.null(json_path)) {
    cat("Error: JSON path is required!\n")
    cat("Usage: Rscript your_script.R <json_path>\n")
    cat("<json_path> : Path of the JSON folder!\n")
    quit(status = 1)
  }
  
  if (!dir.exists(json_path)) {
    stop(paste("Error: folder of Json not exist ->", json_path))
  }
  message(paste0("Input the json path is:", json_path, "\n"))
  mergeAveragePlot(json_path) # , title_name
  mergeMultiVisResult(json_path)
  heatmapMat_rowMerge <- propressData(json_path) #  , row_merge_method
  plot_para_list <- getJsonFiles(json_path)
  if(length(plot_para_list)>1){
    corPlot(json_path, heatmapMat_rowMerge) 
    runPCA(heatmapMat_rowMerge, json_path)
  }
  
}


# usage -------------------------------------------------------------------
# json_path <- "/Users/benche/myProj/ngsPlot/Output/Test/H3K4"
args <- commandArgs(trailingOnly = TRUE)
Main(args)


