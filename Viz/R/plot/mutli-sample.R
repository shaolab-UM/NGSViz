
# 作用：使用 ggplot2 对不同分组的基因信号进行可视化。
#逻辑：
#根据分组信息计算每个样本的平均信号。
#将信号数据转换为长格式，方便绘图。
#使用 geom_line 绘制信号曲线，通过颜色区分不同样本。
#添加垂直虚线标记转录起始位点（TSS）和转录终止位点（TES）。

# 用于存储生成的多个图形，便于后续批量绘制或保存
plot_list <- list()
# group_list: multi-sample such as nsd3 H3K4me2
mat_df <- read.csv("~/myProj/ngsPlot/Output/Result/hesc.H3k27me3.1M_coverage_matrix_heatmap.csv", row.names = 1)
cov_df <- read.csv("~/myProj/ngsPlot/Output/Result/hesc.H3k27me3.1M_average_coverage_matrix.csv")
cov_df <- as.data.frame(cov_df)
cov_df['Position'] <- 1:dim(cov_df)[1]
colnames(cov_df)[1] <- "AverageCoverage"
cov_df["group"] <- "3"
cov_df$group[1:100] <- "1"
cov_df$group[101:1000] <- "2"

colourCount_1 = length(Marker_data)
color <-getPalette(colourCount_1)
color <- c("#FFC0CB","#FF0000","#8B0000",
           "#9ACD32",'#00FF00',"#006400",
           '#0000CC','#3333FF')

library(ggplot2)
# Create a custom theme
custom_theme <- theme_bw() +
  theme(panel.border = element_blank(),
        panel.grid = element_blank(),
        axis.line = element_line(size = 1, colour = "black"),
        axis.text.x = element_text(size = 10, color = "black"),
        axis.text.y = element_text(size = 10, color = "black"),
        axis.title.y = element_text(size = 10, color = "black"),
        plot.title = element_text(size = 11, color = "black", hjust = 0.5),
        legend.text = element_text(size = 10, color = "black"),
        legend.title = element_text(size = 10, color = "black"),
        legend.position = c(0.7, 1),
        legend.justification = c(0, 1),
        legend.spacing.y = unit(0.1, 'cm'),
        legend.key.height = unit(0.1, 'cm'),
        legend.text = element_text(size = 8, color = "black"),
        legend.title = element_blank(),
        plot.margin = margin(0, 0, 0, 0, "cm")
        )
custom_theme <- theme_bw() +
  theme(
    panel.border = element_blank(),  # 设置面板边框样式，移除边框
    panel.grid = element_blank(),    # 设置面板网格线样式，移除所有网格线
    axis.line = element_line(size = 1, colour = "black"),  # 设置坐标轴线样式，线条宽度为1，黑色
    axis.text.x = element_text(size = 10, color = "black"),  # 设置x轴文本样式，字体大小为10，黑色
    axis.text.y = element_text(size = 10, color = "black"),  # 设置y轴文本样式，字体大小为10，黑色
    axis.title.y = element_text(size = 10, color = "black"),  # 设置y轴标题样式，字体大小为10，黑色
    plot.title = element_text(size = 11, color = "black", hjust = 0.5),  # 设置图标题样式，字体大小为11，居中对齐
    #legend.text = element_text(size = 10, color = "black"),  # 设置图例文本样式，字体大小为10，黑色
    #legend.title = element_text(size = 10, color = "black"),  # 设置图例标题样式，字体大小为10，黑色
    legend.position = c(0.7, 1),  # 设置图例位置，距离绘图区域的左下角分别为0.7和1（归一化后的坐标）
    legend.justification = c(0, 1),  # 设置图例的对齐方式，左上角对齐
    legend.spacing.y = unit(0.1, 'cm'),  # 设置图例项目的垂直间距，0.1厘米
    legend.key.height = unit(0.1, 'cm'),  # 设置图例键高的高度，0.1厘米
    legend.text = element_text(size = 8, color = "black"),  # 重复设置图例文本样式，字体大小改为8，黑色
    legend.title = element_blank(),  # 移除图例标题
    plot.margin = margin(0, 0, 0, 0, "cm")  # 设置绘图区域的边距为0，单位是厘米
  )

ggplot(cov_df, aes(x = Position, y = AverageCoverage, group = group)) +
  geom_line(aes(color = group), size = 1.2) + 
  scale_color_manual(values = color) + 
  scale_x_continuous(name = "", labels = c('-3kb', 'TSS', 'TES', '+3kb'), breaks = c(0, 20, 80, 100)) + 
  #scale_y_continuous(name = ylab, limits = c(min(cov_df$AverageCoverage), max(cov_df$AverageCoverage)),
                     #breaks = seq(min(cov_df$AverageCoverage), max(cov_df$AverageCoverage), 0.1)) + 
  geom_vline(xintercept = c(20, 80), linetype = "dashed", size = 0.8) + 
  labs(title = "Coverage Line Plot") +
  custom_theme

plot_list[[z]] <- ggplot(data, mapping = aes(x = x, y = y, group = group)) + 
  geom_line(aes(color = group), size = 1.2) + 
  scale_color_manual(values = color) + 
  scale_x_continuous(name = "", labels = c('-3kb', 'TSS', 'TES', '+3kb'), breaks = c(0, 20, 80, 100)) + 
  scale_y_continuous(name = ylab, limits = c(min(data$y), max(data$y)), breaks = seq(min(data$y), max(data$y), 0.1)) + 
  labs(title = unique(str_split_fixed(sample, '_', 3)[, 2])) + 
  geom_vline(xintercept = c(20, 80), linetype = "dashed", size = 0.8) + 
  custom_theme

for (z in 1:length(group_list)) {
  # sample：当前分组的样本列表。
  # Marker_mat：用于存储当前分组中不同样本组合的信号矩阵。
  sample <- group_list[[z]]
  Marker_mat <- list()
  if (length(sample) > 2) {
    Marker_mat[[1]] <- (enrichList[[sample[1]]] + enrichList[[sample[2]]] + enrichList[[sample[3]]]) / 3
    Marker_mat[[2]] <- (enrichList[[sample[4]]] + enrichList[[sample[5]]] + enrichList[[sample[6]]]) / 3
  } else {
    Marker_mat[[1]] <- enrichList[[sample[1]]]
    Marker_mat[[2]] <- enrichList[[sample[2]]]
  }
  
  Marker_avg <- matrix(ncol = length(Marker_mat), nrow = 101)
  colnames(Marker_avg) <- combined_sample
  rownames(Marker_avg) <- c(1:101)
  
  for (i in 1:length(combined_sample)) {
    Marker_avg[, i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
  }
  
  Marker_data <- list()
  for (i in 1:ncol(Marker_avg)) {
    Marker_data[[i]] <- data.frame(x = c(1:101), y = Marker_avg[, i])
    Marker_data[[i]]$group <- colnames(Marker_avg)[i]
  }
  
  data <- Marker_data[[1]]
  for (i in 2:length(Marker_data)) {
    data <- rbind(data, Marker_data[[i]])
  }
  
  plot_list[[z]] <- ggplot(data, mapping = aes(x = x, y = y, group = group)) + 
    geom_line(aes(color = group), size = 1.2) + scale_color_manual(values = color) + 
    theme_bw() + 
    theme(panel.border = element_blank(), panel.grid = element_blank()) + 
    theme(axis.line = element_line(size = 1, colour = "black")) + 
    scale_x_continuous(name = "", labels = c('-3kb', 'TSS', 'TES', '+3kb'), breaks = c(0, 20, 80, 100)) + 
    scale_y_continuous(name = ylab, limits = c(min(data$y), max(data$y)), breaks = seq(min(data$y), max(data$y), 0.1)) + 
    theme(axis.text.x = element_text(size = 10, color = "black")) + 
    theme(axis.text.y = element_text(size = 10, color = "black")) + 
    theme(axis.title.y = element_text(size = 10, color = "black")) + 
    labs(title = unique(str_split_fixed(sample, '_', 3)[, 2])) + 
    theme(plot.title = element_text(size = 11, color = "black", hjust = 0.5)) + 
    theme(legend.text = element_text(size = 10, color = "black")) + 
    theme(legend.title = element_text(size = 10, color = "black")) + 
    theme(panel.grid = element_blank(), legend.position = c(0.7, 1), legend.justification = c(0, 1), legend.spacing.y = unit(0.1, 'cm'), legend.key.height = unit(0.1, 'cm')) + 
    theme(legend.text = element_text(size = 8, color = "black")) + 
    theme(legend.title = element_blank()) + 
    geom_vline(xintercept = c(20, 80), linetype = "dashed", size = 0.8) + 
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
}