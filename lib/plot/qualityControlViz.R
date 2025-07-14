#!/usr/bin/env Rscript
#
# Program: analyzeCoverageMatrix.r
# Purpose: Analyze two coverage matrices to generate corrgram, PCA, and hierarchical clustering.
#
# Created: July 2025
#


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
  tiff(file_name, width = width, height = height, res = dpi)
  corrplot(cor_matrix, 
           method = "circle", 
           type = "upper",
           diag = FALSE, 
           tl.col = "black", 
           tl.cex = 1.2,
           number.cex = 0.8,
           tl.srt = 45, # Set the label rotation angle to 45 degrees
           addCoef.col = "grey"
  )
  dev.off()
}

# PCA ---------------------------------------------------------------------
getPCAVarianceText <- function(pca_eig, num){
  pca_variance <- pca_eig[num]
  pc_text <- sprintf("PC%d (Variance: %g %s)", num, pca_variance, "%")
  return(pc_text)
}

runPCA <- function(heatmapMat_rowMerge, plot_paras){
  gene <- t(heatmapMat_rowMerge)
  pca_num <- 3
  gene.pca <- FactoMineR::PCA(gene, scale.unit = TRUE, graph = FALSE, ncp=pca_num)
  pca_data <- data.frame(gene.pca$ind$coord)
  colnames(pca_data) <- paste("PC", 1:pca_num, sep = "")
  pca_eig <- round(gene.pca$eig[ ,2],2)
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
}

# usage -------------------------------------------------------------------
# library(ggplot2)
# library(reshape2)
# library(corrplot)
# library(ggrepel)
# json_path <- "/Users/benche/myProj/ngsPlot/Output/Test/mutliAveragePlot"
# row_merge_method <- "mean"
# 
# 
# heatmapMat_rowMerge <- propressData(json_path, row_merge_method)
# corPlot(json_path, heatmapMat_rowMerge) #, 600, 600, 80
# runPCA(heatmapMat_rowMerge, plot_paras)