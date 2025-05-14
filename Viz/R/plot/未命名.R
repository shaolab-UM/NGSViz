# 没看懂

# NSD3_class_list <- list(NSD3_class_A=NSD3_class_A,
#                         NSD3_class_B=NSD3_class_B,
#                         NSD3_class_C=NSD3_class_C,
#                         NSD3_class_D=NSD3_class_D)
# 
# gom.obj <- newGOM(NSD3_class_list,LSD2_class_list,genome.size = 21744)
# 
# drawHeatmap(gom.obj)
# 
# NSD3_list <- list(NSD3_enrich = c(NSD3_class_list[[1]],NSD3_class_list[[2]]),
#                   NSD3_non_enrich = c(NSD3_class_list[[3]],NSD3_class_list[[4]]))
# 
# LSD2_list <- list(LSD2_enrich = c(LSD2_class_list[[1]],LSD2_class_list[[2]]),
#                   LSD2_non_enrich = c(LSD2_class_list[[3]],LSD2_class_list[[4]]))
# 
# gom.obj <- newGOM(NSD3_list,LSD2_list,genome.size = 21744)
# drawHeatmap(gom.obj)

col_fun = colorRamp2(c(-20,0,20), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-20,0,20),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)
r_split <- factor(rep(c('A','B'),c(sum(1777,2147),sum(13503,4317))))

mat_df <- read.table("hm1.txt", row.names = 1, header = T)[ ,-c(1:3)]
heatmap_df <- mat_df
hc_df <- data.frame()
cluster_num <- 3
for (i in 1:nrow(heatmap_df)) {
  hc_df[i,1] <- mean(heatmap_df[i,21])
}
#colnames(hc_df) <- c('LSD2')
row.names(hc_df) <- row.names(heatmap_df)





getClusterOrder <- function()
# order
#  change name
#hc_df$hc <- factor(hc_df$hc, levels = c(2, 1, 3, 4))

hclust()


hc = hclust(dist(Y_LSD2, method = 'euclidean'), method = 'ward.D')
#作用：对数据进行分层聚类（Hierarchical Clustering），以识别基因信号的相似模式。
#逻辑：
#使用欧几里得距离（dist）衡量样本之间的相似性。
#采用 Ward's 方法（ward.D）构建聚类树。
#通过 cutree 函数将聚类树划分为指定数量的簇。

library(ComplexHeatmap)
# r_order：用于对数据进行排序的行顺序。r_order 可能是通过层次聚类或其他方法获得的行顺序。
# col = col_fun 指定了热图的颜色映射方式 colorRamp2 或 brewer.pal
# row_split = r_split 行分割：控制热图中行的分割方式
# width = unit(3, "cm") 宽度：指定热图的宽度
# height = unit(10, "cm") 高度：指定热图的高度。
# cluster_rows = FALSE 行聚类：决定是否对热图的行进行聚类。
# cluster_columns = FALSE
# use_raster = TRUE 数据量较大时，启用栅格化可以显著提高绘图性能，并减少内存占用
# raster_quality = 25 栅格质量：设置栅格化的质量(1-30)，默认为 15。较大的值表示更高的质量，但会增加内存消耗
# show_column_dend = FALSE 显示列聚类树
# show_row_dend = FALSE
# show_row_names = FALSE 显示行名称
# heatmap_legend_param = lgd 热图图例
# border = TRUE 边框：设置热图边框是否可见。
library(circlize)
install.packages('magick')
library(magick)
max_value <- max(heatmap_df)
col_fun <- colorRamp2(c(0, 1.1), c("#2166AC", "#F00000"))
col_fun = colorRamp2(c(0,10), c("#F7FBFF","#F00000"))

ht <- draw(ht)


mat = matrix(rnorm(100), 10)
Heatmap(as.matrix(mat_df[1:1000, ]))
#作用：使用 ComplexHeatmap 包绘制基因信号的热图。
#逻辑：
#根据聚类结果对基因进行排序。
#使用颜色映射（col_fun）将信号值转换为颜色。
#通过 row_split 参数将热图按簇分割，方便观察。