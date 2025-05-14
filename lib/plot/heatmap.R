
# 顶部注释
ha = HeatmapAnnotation(foo = anno_empty(border = TRUE, height = unit(3, "cm")))
ht = Heatmap(matrix(rnorm(100), nrow = 10), name = "mat", top_annotation = ha)
ht = draw(ht)
co = column_order(ht)
value = runif(10)
decorate_annotation("foo", {
  # value on x-axis is always 1:ncol(mat)
  x = 1:10
  # while values on y-axis is the value after column reordering
  value = value[co]
  pushViewport(viewport(xscale = c(0.5, 10.5), yscale = c(0, 1)))
  grid.lines(c(0.5, 10.5), c(0.5, 0.5), gp = gpar(lty = 2),
             default.units = "native")
  grid.points(x, value, pch = 16, size = unit(2, "mm"),
              gp = gpar(col = ifelse(value > 0.5, "red", "blue")), default.units = "native")
  grid.yaxis(at = c(0, 0.5, 1))
  popViewport()
})

# 添加行注释
# left_annotation = rowAnnotation(foo = anno_block(gp = gpar(fill = 2:4),
#                                                  labels = c("group1", "group2", "group3"), 
#                                                  labels_gp = gpar(col = "white", fontsize = 10))),

# 添加虚线
decorate_heatmap_body("ht1", {
  grid.text("outlier", 1.5/10, 2.5/4, default.units = "npc")
  grid.lines(c(0.5, 0.5), c(0, 1), gp = gpar(lty = 2, lwd = 2))
}, slice = 2)

random_text = function(n) {
  sapply(1:n, function(i) {
    paste0(sample(letters, sample(4:10, 1)), collapse = "")
  })
}
text_list = list(
  text1 = random_text(4),
  text2 = random_text(4),
  text3 = random_text(4),
  text4 = random_text(4)
)
# note how we set the width of this empty annotation
ha = rowAnnotation(foo = anno_empty(border = FALSE, 
                                    width = max_text_width(unlist(text_list)) + unit(4, "mm")))
Heatmap(matrix(rnorm(1000), nrow = 100), name = "mat", row_km = 4, right_annotation = ha)
for(i in 1:4) {
  decorate_annotation("foo", slice = i, {
    grid.rect(x = 0, width = unit(2, "mm"), gp = gpar(fill = i, col = NA), just = "left")
    grid.text(paste(text_list[[i]], collapse = "\n"), x = unit(4, "mm"), just = "left")
  })
}

# lgd = list(col_fun = col_fun, title = "",at = c(-20,0,20),direction = "horizontal",
#            title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)
# r_split <- factor(rep(c('A','B'),c(sum(1777,2147),sum(13503,4317))))
# 
# mat_df <- read.table("hm1.txt", row.names = 1, header = T)[ ,-c(1:3)]
# heatmap_df <- mat_df
# hc_df <- data.frame()
# cluster_num <- 3
# for (i in 1:nrow(heatmap_df)) {
#   hc_df[i,1] <- mean(heatmap_df[i,21])
# }
# #colnames(hc_df) <- c('LSD2')
# row.names(hc_df) <- row.names(heatmap_df)

processRowOrder <- function(hc_df, 
                            cluster_num, 
                            hc_m = "ward.D", 
                            dist_m = "euclidean"){
  hc_df <- getHCluster(hc_df, cluster_num, hc_m, dist_m)
  # order
  sorted_df <- hc_df %>%
    group_by(cat) %>%
    summarise(mean_value = mean(value)) %>%
    arrange(desc(mean_value))
  # 
  hc_df$cat <- factor(hc_df$cat,levels = sorted_df$cat)
  r_order <- hc_df$gene
  count_num <- table(hc_df$cat)
  r_split <- factor(rep(names(count_num),count_num), levels = sorted_df$cat)
}

getHCluster <- function(hc_df, cluster_num, hc_m = "ward.D", dist_m = "euclidean"){
  hc <- hclust(dist(hc_df, method = dist_m), method = hc_m)
  # Visualize clustering results
  #plot(hc, label = FALSE)
  #rect.hclust(hc, k = 3, border = "red")
  # Cut the clustering tree and store the results
  hc_result <- cutree(hc, k = cluster_num)
  hc_df$gene <- rownames(hc_df)
  hc_df$hc <- hc_result
  table(hc_df$hc)
  colnames(hc_df)[1] <- "value"
  # Set clustering factor and classification
  hc_df$cat <- NA
  for (i in 1:cluster_num) {
    hc_df[hc_df$hc == i, 'cat'] <- paste0("C", as.character(i))
  }
  hc_df <- hc_df[order(hc_df$cat),]
  return(hc_df)
}

column_breaks <- c(1,51,101)
labels_items <- c("+2kb", "TSS", "-2kb")
heatmap_mat <- as.matrix(heatmap_df[r_order, ])

pa = cluster::pam(mat, k = 3)
Heatmap(mat, name = "mat", row_split = paste0("pam", pa$clustering))
column_title_gp = gpar(fill = c("red", "blue", "green"), font = 1:3)

flood.q=.02
flood.pts <- quantile(c(data_log, recursive=T), c(flood.q, 1-flood.q))

col_fun = colorRamp2(flood.pts, c("#2166AC","#F00000"))
#col_fun <- colorRampPalette(c("blue", "white", "red"))(100)
Heatmap(data_log,
              col = col_fun,
              #row_split = r_split,
              width = unit(3, "cm"), 
              height = unit(10, "cm"),
              cluster_rows = FALSE, 
              cluster_columns = FALSE, 
              use_raster = TRUE, 
              #raster_quality = 25,
              show_column_dend = FALSE, 
              show_row_dend = FALSE, 
              raster_device = "agg_png",
              show_row_names = FALSE,
              show_column_names = FALSE,
              #column_split = cut(seq_len(ncol(heatmap_mat)), breaks = column_breaks, labels = labels_items),
              border = TRUE
)
ht
data_log <- log2(heatmap_mat + 1)
data_log <- scale(heatmap_mat)

BiocManager::install('preprocessCore')
library(preprocessCore)
data_log <- normalize.quantiles(as.matrix(heatmap_mat))
data_log <- pmin(pmax(data_log, lower_bound), upper_bound)
col_upper_quartiles <- apply(data_log, 2, function(x) quantile(x, probs=0.75))
max(col_upper_quartiles)
