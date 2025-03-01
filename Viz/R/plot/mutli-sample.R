
# 作用：使用 ggplot2 对不同分组的基因信号进行可视化。
#逻辑：
#根据分组信息计算每个样本的平均信号。
#将信号数据转换为长格式，方便绘图。
#使用 geom_line 绘制信号曲线，通过颜色区分不同样本。
#添加垂直虚线标记转录起始位点（TSS）和转录终止位点（TES）

library(ggplot2)
library(ggsci)

!ggsci 
# load data ---------------------------------------------------------------
source("lib/plot/plotThemeSet.R")
source("lib/processJSON.R")
plot_para_file <- "NGSViz_plotSetting.json"

plot_paras <- getPlotJsonParas(plot_para_file)
custom_theme <- setPlotTheme(plot_paras)
legend_theme <- setPlotLegend(plot_paras)

# 用于存储生成的多个图形，便于后续批量绘制或保存
plot_list <- list()
# group_list: multi-sample such as nsd3 H3K4me2
mat_df <- read.csv("~/myProj/ngsPlot/Output/Result/hesc.H3k27me3.1M_coverage_matrix_heatmap.csv", row.names = 1)
cov_df <- read.csv("~/myProj/ngsPlot/Output/Result/hesc.H3k27me3.1M_average_coverage_matrix.csv")
cov_df <- as.data.frame(cov_df)
cov_df['Position'] <- 1:dim(cov_df)[1]
colnames(cov_df)[1] <- "AverageCoverage"
cov_df["group"] <- "3"

colourCount_1 = length(Marker_data)
color <-getPalette(colourCount_1)
color <- c("#FFC0CB","#FF0000","#8B0000",
           "#9ACD32",'#00FF00',"#006400",
           '#0000CC','#3333FF')


selected_colors <- pal_aaas("default")(9)

#scale_y_continuous(name = ylab, limits = c(min(cov_df$AverageCoverage), max(cov_df$AverageCoverage)),
#breaks = seq(min(cov_df$AverageCoverage), max(cov_df$AverageCoverage), 0.1)) + 

ggplot(cov_df, aes(x = Position, y = AverageCoverage, group = group)) +
  geom_line(aes(color = group), size = plot_paras[["line_size"]]) + 
  #scale_color_manual(values = color) + 
  #scale_color_npg() +
  scale_color_manual(values = selected_colors)+
  #scale_fill_aaas() +
  scale_x_continuous(name = "", labels = c('-3kb', 'TSS', 'TES', '+3kb'), breaks = c(0, 20, 80, 101)) + 
  geom_vline(xintercept = c(20, 80), linetype = "dashed",
             size = plot_paras[["dash_size"]], color = plot_paras[["dash_color"]]) + 
  labs(title = "Coverage Line Plot") +
  custom_theme + legend_theme

plot_list[[z]] <- ggplot(data, mapping = aes(x = x, y = y, group = group)) + 
  geom_line(aes(color = group), size = 1.2) + 
  scale_color_manual(values = color) + 
  scale_x_continuous(name = "", labels = c('-3kb', 'TSS', 'TES', '+3kb'), breaks = c(0, 20, 80, 100)) + 
  scale_y_continuous(name = ylab, limits = c(min(data$y), max(data$y)), breaks = seq(min(data$y), max(data$y), 0.1)) + 
  labs(title = unique(str_split_fixed(sample, '_', 3)[, 2])) + 
  geom_vline(xintercept = c(20, 80), linetype = "dashed", size = 0.8) + 
  custom_theme
  
 