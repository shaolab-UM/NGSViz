
library(dplyr)
library(circlize)
library(EnrichedHeatmap)

# point mode
matri2normalizedMatrix <- function(mat){
  attr(mat, "upstream_index") <- 1:50 
  attr(mat, "downstream_index") <- 51:101 
  attr(mat, "target_index") <- integer(0)
  attr(mat, "extend") <- c(2000, 2000)
  #attr(mat, "smooth") <- F
  #attr(mat, "signal_name") <- "H3K4me2"
  #attr(mat, "target_name") <- "TSS"
  attr(mat, "target_is_single_point") <- T
  #attr(mat, "backgroud") <- 0
  attr(mat, "signal_is_categorical") <- F
  #attr(mat, "dimnames") <- list()
  class(mat) = c("normalizedMatrix", "matrix")
  return(mat)
}

# point mode
matri2normalizedMatrix <- function(mat){
  attr(mat, "upstream_index") <- 1:40
  attr(mat, "downstream_index") <- 62:101 
  attr(mat, "target_index") <- 41:61
  attr(mat, "extend") <- c(2000, 2000)
  #attr(mat, "smooth") <- F
  #attr(mat, "signal_name") <- "H3K4me2"
  #attr(mat, "target_name") <- "TSS"
  attr(mat, "target_is_single_point") <- F
  #attr(mat, "backgroud") <- 0
  attr(mat, "signal_is_categorical") <- F
  #attr(mat, "dimnames") <- list()
  class(mat) = c("normalizedMatrix", "matrix")
  return(mat)
}

# point mode
getAxisName <- function(){
  n1 = length(upstream_index)
  n2 = length(target_index)
  n3 = length(downstream_index)
  n = n1 + n2 + n3
  extend = attr(data_sub, "extend")
  axis_name = c(paste0("-", extend[1]), "start", "end", 
                extend[2])
  axis_name <- c("TSS")
  return(axis_name)
}
# broad mode 
getAxisName <- function(){
  n1 = length(upstream_index)
  n2 = length(target_index)
  n3 = length(downstream_index)
  n = n1 + n2 + n3
  extend = attr(data_sub, "extend")
  axis_name = c(paste0("-", extend[1]), "start", "end", 
                extend[2])
  axis_name <- c("TSS", "TES")
  return(axis_name)
}

# start analysis ----------------------------------------------------------
#file_path <- "/Users/bencheye/myProj/ngsPlot/NGSViz/hesc.H3k27me3.1M__AverageCoverage__coverage_matrix_heatmap.csv"
file_path <- "/Users/bencheye/Downloads/sg1/hm1.txt"
data <- read.csv(file_path, row.names = 2, sep = '\t') #%>% as.matrix()
data <- data[ ,-c(1:3)] %>% as.matrix()
#data <- read.csv(file_path, row.names = 1) #%>% as.matrix()
#data_sub <- data[1:2000, ]
# data <- matri2normalizedMatrix(data)
bg_col <- c("#Cb648b", "#679cd2")

# plot_paras
# class(data)
data <- matri2normalizedMatrix(data)
class(data) = c("normalizedMatrix", "matrix")
col_fun = colorRamp2(quantile(data, c(0, 0.99)), c("grey95", bg_col[1]))
# need to adjust
axis_name <- c("-2000", "TSS", "TES", "2000")
ht <-
  EnrichedHeatmap(data, col = col_fun, 
                  #name = sample[x],
                  column_title = "title",
                  axis_name = axis_name,
                  column_title_gp = gpar(fontsize = 10,
                                         fill = bg_col[1]),
                  # top_annotation = HeatmapAnnotation(
                  #   enriched = anno_enriched(
                  #     ylim = c(0, 30))),
                  use_raster = F,
                  # show_heatmap_legend = lgd[x],
                  show_heatmap_legend = T,
                  heatmap_legend_param = list(title = "Legend Title")
  )
ht
