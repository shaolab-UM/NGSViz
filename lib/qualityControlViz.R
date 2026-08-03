#!/usr/bin/env Rscript
#
# Program: analyzeCoverageMatrix.r
# Purpose: Analyze two coverage matrices to generate corrgram, PCA, and hierarchical clustering.
#
# Created: July 2025
#

# build species reference genome based on the gtf file
checkRequiredPackages <- function(required_packages) {
  # Loop through each package to check and load
  for (pkg in required_packages) {
    # Check if the package is installed
    if (!requireNamespace(pkg, quietly = TRUE)) {
      message(paste0("Package '", pkg, "' is not installed, attempting to install..."))
      # Attempt to install the package
      tryCatch({
        install.packages(pkg, dependencies = TRUE)
        message(paste0("Package '", pkg, "' installed successfully."))
      }, error = function(e) {
        # If installation fails, print error message and stop the script
        stop(paste0("Error: Unable to install package '", pkg, "'. Please install this package manually and then run the script.\n",
                    "Error message: ", e$message))
      })
    }
    
    # Attempt to load the package
    # Use suppressPackageStartupMessages to avoid printing package startup messages
    # Use tryCatch to capture potential errors during loading
    tryCatch({
      suppressPackageStartupMessages(library(pkg, character.only = TRUE))
      message(paste0("Package '", pkg, "' loaded successfully."))
    }, error = function(e) {
      # If loading fails, print error message and stop the script
      stop(paste0("Error: Unable to load package '", pkg, "'. Please check if the installation is complete or if there are compatibility issues.\n",
                  "Error message: ", e$message))
    })
  }
  
  message("\nAll required packages have been installed and loaded.")
}

# Define row statistic functions
getRowMeans <- function(x, mean.trim=0, na.rm=TRUE){
  row.mean.x <- apply(x, 1, mean, trim=mean.trim, na.rm=na.rm)
  names(row.mean.x) <- rownames(x)
  return(row.mean.x)
}

getRowMax <- function(x){  
  row.max.x <- apply(x, 1, max, na.rm=TRUE)
  names(row.max.x) <- rownames(x)
  return(row.max.x)
}

readHeatmapMat <- function(plot_paras){
  heatmap_files <- plot_paras$heatmap_mat_path
  out_path <- plot_paras$output_path
  if (!file.exists(heatmap_files)) {
    error_message <- paste0("Error: Specified file path '", 
                            heatmap_files, 
                            "' not exist. The script will terminate.")
    stop(error_message)
  }else{
    data_mat <- read.csv(heatmap_files, row.names = 1) %>% as.matrix()
  }
  return(data_mat)
}

propressData <- function(json_path, row_merge_method = "mean"){
  mean.trim = 0
  # get the json file name list
  plot_para_list <- getJsonFiles(json_path)
  heatmapMat_list <- list()
  sample_names <- c()
  for(i in 1:length(plot_para_list)){
    plot_paras_name <- plot_para_list[[i]]
    plot_paras_path <- file.path(json_path, plot_paras_name)
    plot_paras <- getPlotJsonParas(plot_paras_path)
    heatmap_mat <- readHeatmapMat(plot_paras)
    title <- plot_paras$plot_title
    heatmapMat_list[[title]] <- heatmap_mat
    sample_names <- c(sample_names, title)
  }
  # Calculate values for correlation
  switch(row_merge_method,
         mean={
           value.list <- lapply(heatmapMat_list, getRowMeans, mean.trim=mean.trim)
         },
         max={
           value.list <- lapply(heatmapMat_list, getRowMax)
         },
         window={
           value.list <- lapply(heatmapMat_list, getRowWindow, window.borders=window.borders)
         }
  )
  # Prepare matrix for correlation
  heatmapMat_rowMerge <- do.call(cbind, value.list)
  # filter out of the 0
  heatmapMat_rowMerge <- heatmapMat_rowMerge[rowSums(heatmapMat_rowMerge != 0) > 0, ]
  return(heatmapMat_rowMerge)
}

# correlation -------------------------------------------------------------
corPlot <- function(json_path, heatmapMat_rowMerge, width = 600, height=600, dpi = 80){
  cor_matrix <- cor(heatmapMat_rowMerge)
  file_name <- sprintf("%s/correlation_plot.tiff", json_path)
  # plot
  tiff(file_name, width = width, height = height, res = dpi)
  corrplot(cor_matrix,
           method = "circle",
           type = "upper",
           diag = T,
           tl.col = "black",
           addgrid.col = "black",
           tl.cex = 1.2,           
           number.cex = 1.2, 
           rect.lwd = 3,
           tl.srt = 45,
           addCoef.col = "grey",
           addCoefasPercent = TRUE,  
           mar = c(0, 0, 0, 0)
  )    
  dev.off()
  corrplot(cor_matrix,
           method = "circle",
           type = "upper",
           diag = T,
           tl.col = "black",
           addgrid.col = "black",
           tl.cex = 1.2,           
           number.cex = 1.2, 
           rect.lwd = 3,
           tl.srt = 45,
           addCoef.col = "grey",
           addCoefasPercent = TRUE,  
           mar = c(0, 0, 0, 0)
  )
}

# PCA ---------------------------------------------------------------------
getPCAVarianceText <- function(pca_eig, num){
  pca_variance <- pca_eig[num]
  pc_text <- sprintf("PC%d (Variance: %g %s)", num, pca_variance, "%")
  return(pc_text)
}

preparePcaPlotData <- function(coordinates, eigenvalues, dimensions = 2L) {
  coordinate_matrix <- as.matrix(coordinates)
  available <- min(dimensions, ncol(coordinate_matrix))
  plot_data <- data.frame(coordinate_matrix[, seq_len(available), drop = FALSE])
  if (available < dimensions) {
    for (index in seq.int(available + 1L, dimensions)) {
      plot_data[[index]] <- 0
    }
  }
  colnames(plot_data) <- paste0("PC", seq_len(dimensions))
  variance <- round(as.numeric(eigenvalues), 2)
  length(variance) <- dimensions
  variance[is.na(variance)] <- 0
  list(data = plot_data, variance = variance)
}

runPCA <- function(heatmapMat_rowMerge, json_path){
  plot_para_list <- getJsonFiles(json_path)
  plot_paras_name <- plot_para_list[[1]]
  plot_paras_path <- file.path(json_path, plot_paras_name)
  plot_paras <- getPlotJsonParas(plot_paras_path)
  
  gene <- t(heatmapMat_rowMerge)
  pca_num <- max(1L, min(2L, nrow(gene) - 1L, ncol(gene)))
  gene.pca <- FactoMineR::PCA(gene, scale.unit = TRUE, graph = FALSE, ncp=pca_num)
  prepared <- preparePcaPlotData(gene.pca$ind$coord, gene.pca$eig[, 2])
  pca_data <- prepared$data
  pca_eig <- prepared$variance
  # plot label
  pc1_text <- getPCAVarianceText(pca_eig, 1)
  pc2_text <- getPCAVarianceText(pca_eig, 2)
  # plot
  pca_plot <- ggplot(pca_data, aes(x = PC1, y = PC2)) +
    geom_point(size = 2) +
    labs(title = sprintf(""),
         x = pc1_text,
         y = pc2_text) +
    geom_text_repel(aes(label = rownames(pca_data)), size = 3) +
    myTheme(plot_paras)
  png_file <- sprintf("%s/PCA_plot.%s", json_path, plot_paras$file_type)
  ggsave(png_file, plot = pca_plot, dpi = plot_paras$dpi, 
         width = plot_paras$width, height = plot_paras$high, units = "cm")
  return(pca_plot)
}

# usage -------------------------------------------------------------------
packages <- c(
  "corrplot", "ggrepel", "FactoMineR"
)

# Load packages only for direct legacy sourcing. Native CLI validates first.
if (!isTRUE(getOption("ngsviz.skip_dependency_loading"))) {
  checkRequiredPackages(packages)
}

# json_path <- "/Users/benche/myProj/ngsPlot/Result/feature/mergeH3K4me3"
# heatmapMat_rowMerge <- propressData(json_path)
# if(length(plot_para_list)>1){
#   corPlot(json_path, heatmapMat_rowMerge)
#   runPCA(heatmapMat_rowMerge, json_path)
# }
