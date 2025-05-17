
library(dplyr)
library(circlize)
library(EnrichedHeatmap)

# transform matrix to the normalizedMatrix, to meet the enrichHeatmap plot
matri2normalizedMatrix <- function(mat, running_paras){
  interval_type <- running_paras$plot_paras$interval_type
  num_middle_point <- running_paras$plot_paras$middle_point
  num_flank_point <- running_paras$plot_paras$flank_point
  flank_size <- running_paras$plot_paras$flank_size
  num_datapoints <- running_paras$plot_paras$num_datapoints
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
  #attr(mat, "signal_name") <- running_paras$plot_paras$sample_list
  #attr(mat, "target_name") <- "TSS"
  #attr(mat, "backgroud") <- 0
  attr(mat, "signal_is_categorical") <- F
  #attr(mat, "dimnames") <- list()
  class(mat) = c("normalizedMatrix", "matrix")
  return(mat)
}

# to generate the axis label
getAxisName <- function(data_mat, running_paras){
  extend = attr(data_mat, "extend")
  region_labels <- running_paras$plot_paras$region_labels
  extend = attr(data_mat, "extend")
  axis_name = c(paste0("-", extend[1]), region_labels, extend[2])
  return(axis_name)
}

# plot heatmap
# bg_col <- c("#Cb648b", "#679cd2")

plotEnrichedHeatmap <- function(file_path, running_paras, bg_col, sample_name = ""){
  data_mat <- read.csv(file_path, sep = ',', row.names = 1) %>% as.matrix()
  # plot_paras
  data_mat <- matri2normalizedMatrix(data_mat, running_paras)
  col_fun = colorRamp2(quantile(data_mat, c(0, 0.99)), c("grey95", "#Cb648b"))
  axis_name <- getAxisName(data_mat, running_paras)
  ht <-
    EnrichedHeatmap(data_mat, col = col_fun, 
                    name = sample_name,
                    column_title = sample_name,
                    axis_name = axis_name,
                    column_title_gp = gpar(fontsize = 10, fill = bg_col),
                    # top_annotation = HeatmapAnnotation(
                    #   enriched = anno_enriched(ylim = c(0, 30))),
                    use_raster = F,
                    # show_heatmap_legend = lgd[x],
                    show_heatmap_legend = T,
                    heatmap_legend_param = list(title = "")
    )
  return(ht)
}


# start analysis ----------------------------------------------------------
#file_path <- "/Users/bencheye/myProj/ngsPlot/NGSViz/hesc.H3k27me3.1M__AverageCoverage__coverage_matrix_heatmap.csv"
# file_path <- "/Users/bencheye/myProj/ngsPlot/Output/k4.test/hm1.csv"
# file_path <- "/Users/bencheye/myProj/ngsPlot/Output/hesc.H3k4me3.1M__GroupName__coverage_matrix_heatmap.csv"
# data <- read.csv(file_path, sep = '\t', row.names = 1) #%>% as.matrix()
# #data <- read.csv(file_path, row.names = 1) #%>% as.matrix()
# data <- data[1:2000, ]
# data <- matri2normalizedMatrix(data)
# bg_col <- c("#Cb648b", "#679cd2")

transform2EnrichedHeatmap <- function(running_paras){
  sample_df <- running_paras$sample_df
  out_path <- running_paras$plot_paras$output_name
  bg_col <- c("#Cb648b", "#679cd2")
  ht_list <- list()
  for (i in 1:nrow(sample_df)) {
    sample_name <- running_paras$plot_paras$sample_list[i]
    file_path <- sprintf("%s/%s__%s__%s", out_path, sample_df[i, 1],
                               sample_df[i, 2], sample_df[i, 3])
    ht <- plotEnrichedHeatmap(file_path, running_paras, bg_col[i], sample_name)
    ht_list[[i]] <- ht
  }
  out_name <- sprintf("%s/heatmap.pdf", out_path)
  # merge plot list
  pdf(out_name, width = 15, height = 6, onefile = F)
  draw(Reduce("+", ht_list), ht_gap = unit(0.8, "cm"),
       heatmap_legend_side = "right")
  dev.off()
}

transform2EnrichedHeatmap(running_paras)
