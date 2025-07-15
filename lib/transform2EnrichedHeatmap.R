
library(dplyr)
library(circlize)
library(EnrichedHeatmap)

# transform matrix to the normalizedMatrix, to meet the enrichHeatmap plot
matri2normalizedMatrix <- function(mat, plot_paras){
  interval_type <- plot_paras$interval_type
  num_middle_point <- plot_paras$middle_datapoints
  num_flank_point <- plot_paras$flanking_region_datapoints
  flank_size <- plot_paras$flank_size
  num_datapoints <- plot_paras$num_datapoints
  # axis coordinate
  if(interval_type == "broad_interval"){
    attr(mat, "target_is_single_point") <- FALSE
    attr(mat, "upstream_index") <- 1:num_flank_point
    attr(mat, "downstream_index") <- (num_flank_point+num_middle_point+1):num_datapoints
    attr(mat, "target_index") <- (num_flank_point+1):(num_flank_point+num_middle_point)
  }else if(interval_type == "point_interval"){
    attr(mat, "target_is_single_point") <- TRUE
    attr(mat, "upstream_index") <- 1:num_flank_point 
    attr(mat, "downstream_index") <- (num_flank_point+1):num_datapoints 
    attr(mat, "target_index") <- integer(0)
  }
  # flanking size
  attr(mat, "extend") <- c(flank_size, flank_size)
  
  # could need to be adjusted
  attr(mat, "smooth") <- F
  #attr(mat, "signal_name") <- json_path$plot_paras$sample_list
  #attr(mat, "target_name") <- "TSS"
  #attr(mat, "backgroud") <- 0
  attr(mat, "signal_is_categorical") <- F
  #attr(mat, "dimnames") <- list()
  class(mat) = c("normalizedMatrix", "matrix")
  return(mat)
}

# to generate the axis label
getAxisName <- function(data_mat, plot_paras){
  extend = attr(data_mat, "extend")
  region_labels <- plot_paras$region_labels
  extend = attr(data_mat, "extend")
  axis_name = c(paste0("-", extend[1]), region_labels, extend[2])
  return(axis_name)
}

# plot heatmap

plotEnrichedHeatmap <- function(plot_paras){
  bg_col <- c("#Cb648b", "#679cd2")
  data_mat <- read.csv(plot_paras$heatmap_mat_path, 
                       sep = ',', row.names = 1) %>% as.matrix()
  # plot_paras
  data_mat <- matri2normalizedMatrix(data_mat, plot_paras)
  col_fun = colorRamp2(quantile(data_mat, c(0, 0.99)), c("grey95", "#Cb648b"))
  axis_name <- getAxisName(data_mat, plot_paras)
  ht <-
    EnrichedHeatmap(data_mat, col = col_fun, 
                    name = plot_paras$plot_title,
                    column_title = plot_paras$plot_title,
                    axis_name = axis_name,
                    column_title_gp = gpar(fontsize = 10, fill = bg_col),
                    # top_annotation = HeatmapAnnotation(
                    #   enriched = anno_enriched(ylim = c(0, 30))),
                    use_raster = F,
                    # show_heatmap_legend = lgd[x],
                    show_heatmap_legend = T,
                    heatmap_legend_param = list(title = "")
    )
  out_name <- sprintf("%s/heatmap.png", plot_paras$output_path)
  # merge plot list
  png(out_name, width = 400, height = 400)
  draw(ht)
  dev.off()
  return(ht)
}

plotEnrichedHeatmapMain <- function(json_path){
  plot_paras <- getPlotJsonParas(json_path)
  ht <- plotEnrichedHeatmap(plot_paras)
  ht
}
# start analysis ----------------------------------------------------------

# transform2EnrichedHeatmap <- function(plot_paras){
#   heatmap_files <- plot_paras$heatmap_mat_path
#   data_mat <- read.csv(heatmap_files, row.names = 1) %>% as.data.frame()
#   out_path <- plot_paras$output_path
#   bg_col <- c("#Cb648b", "#679cd2")
#   ht_list <- list()
#   for (i in 1:nrow(data_mat)) {
#     sample_name <- plot_paras$plot_title
#     file_path <- sprintf("%s/%s__%s__%s", out_path, data_mat[i, 1],
#                          data_mat[i, 2], data_mat[i, 3])
#     ht <- plotEnrichedHeatmap(file_path, plot_paras, bg_col[i], sample_name)
#     ht_list[[i]] <- ht
#   }
#   out_name <- sprintf("%s/heatmap.pdf", out_path)
#   # merge plot list
#   pdf(out_name, width = 15, height = 6, onefile = F)
#   draw(Reduce("+", ht_list), ht_gap = unit(0.8, "cm"),
#        heatmap_legend_side = "right")
#   dev.off()
# }




# setwd("/Users/benche/myProj/ngsPlot/NGSViz/")
# source("lib/processJSON.R")
# 
# # title_name <- "title"
# json_path <- "/Users/benche/myProj/ngsPlot/Output/Test/H3K4/H3K4_NGSViz_plotSetting.json"
# plotEnrichedHeatmapMain(json_path)


