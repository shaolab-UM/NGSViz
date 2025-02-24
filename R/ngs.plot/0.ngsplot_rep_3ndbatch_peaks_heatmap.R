library(patchwork)
library(circlize)
library(ggplot2)
library(ggpubr)
library(RColorBrewer)
library(ggpubr)
library(gmodels)
library(ggpubr)
library(ggplot2)
library(ggthemes)
library(FactoMineR)
library(ggrepel)
library(factoextra)
library(ComplexHeatmap)
library(GeneOverlap)
library(stringr)
library(EnrichedHeatmap)
library(sampling)
library(dplyr)

set.seed(123)
gene_cat <- c('Enhancer_FL150bp_L100kb')
norm_cat <- c('CPM')

if (norm_cat == 'CPM') {
  ylab <- c('CPM')
} else {
  ylab <- c('log2(vs.IgG)')
}
bam_cat <- c('dedup') #sorted , dedup , dedup.120bps

#setwd(paste("F:/UMProject/mouse_PanNET/Outs/CutRun/03.ngsplot/Norm_by_",norm_cat,'/',bam_cat,'/',gene_cat,'/',sep = ""))
setwd(paste("F:/UMproject/LSD2_NSD3/CutRun_2/03.ngsplot/Norm_by_",norm_cat,'/',bam_cat,'/',gene_cat,'/',sep = ""))
getwd()
load("heatmap.RData")
sample_id <- ctg.tbl$title

length((rownames(enrichList[[1]]) == rownames(enrichList[[2]])) == TRUE)

for (i in 1:length(sample_id)) {
  names(enrichList)[i] <- sample_id[i]
}
names(enrichList)
length((rownames(enrichList[[1]]) == rownames(enrichList[[2]])) == TRUE)

for (i in 1:length(sample_id)) {
  names(enrichList)[i] <- sample_id[i]
}
raw_enrichList <- enrichList
sample_id

enrichList <- raw_enrichList
sgControl_LSD2 <- list()
for (i in c('sgControl_LSD2_R1','sgControl_LSD2_R2','sgControl_LSD2_R3')) {
  sgControl_LSD2[[i]] <- enrichList[[i]]
}

sgControl_NSD3 <- list()
for (i in c('sgControl_NSD3_R1','sgControl_NSD3_R2','sgControl_NSD3_R3')) {
  sgControl_NSD3[[i]] <- enrichList[[i]]
}

r_sum <- data.frame(LSD2=(rowSums( sgControl_LSD2[[1]][,41:61]) + rowSums( sgControl_LSD2[[2]][,41:61]) + rowSums( sgControl_LSD2[[3]][,41:61]))/3,
                    NSD3=(rowSums( sgControl_NSD3[[1]][,41:61]) + rowSums( sgControl_NSD3[[2]][,41:61]) + rowSums( sgControl_NSD3[[3]][,41:61]))/3)

r_sum$delta <- r_sum$LSD2-r_sum$NSD3

partition <-  paste0("cluster", kmeans(r_sum, centers = 4)$cluster)

table(partition)

r_sum$row_id <- rownames(r_sum)
r_sum$partition <- partition

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$delta))
}

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$LSD2))
}

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$NSD3))
}

r_sum$peak_cat <- str_split_fixed(r_sum$row_id,"_peaks",2)[,1]


r_sum <- r_sum[order(-r_sum$LSD2),]
r_order <- r_sum$row_id

cor.test(r_sum$LSD2,r_sum$NSD3)

p=r_sum%>%ggplot(aes(LSD2,NSD3))+
  geom_point(size=2,alpha=0.6,color="#6baed6")+
  labs(x= 'LSD2 signals', y= 'NSD3 signals') + 
  geom_smooth(method = "lm", formula = y~x, color = "#756bb1", fill = "#cbc9e2")+
  xlim(0,6) + ylim(0,6)+
  theme_bw()+
  theme( panel.grid.major = element_blank(),panel.grid.minor = element_blank())

p + stat_cor()

#raw_signals----
enrichList <- raw_enrichList

mat_list <- list()
for (i in 1:length(names(enrichList))) {
  mat_list[[i]] <- enrichList[[i]]
  attr(mat_list[[i]], "upstream_index") = 1:40
  attr(mat_list[[i]], "target_index") = 41:61
  attr(mat_list[[i]], "downstream_index") = 60:101
  attr(mat_list[[i]], "extend") = c(100000,100000)  
  class(mat_list[[i]]) = c("normalizedMatrix", "matrix")
  attr(mat_list[[i]], "signal_name") = names(enrichList)[i]
  attr(mat_list[[i]], "target_name") = "Peak"
  attr(mat_list[[i]], "target_is_single_point") = TRUE
  names(mat_list)[i] <- names(enrichList)[i]
}

mat_list[[3]]

col_fun1 = colorRamp2(c(0,2), c("lightblue", "red"))
col_fun2 = colorRamp2(c(0,0.5), c("lightblue", "red"))
col_fun3 = colorRamp2(c(0,0.15), c("lightblue", "red"))
axis_name = c('-100kb','5End','3End','100kb')
r_sum <- r_sum[order(-r_sum$LSD2),]
r_order <- r_sum$row_id

##Partition heatmap-----
partition <- r_sum$partition
names(mat_list)

ht_list = Heatmap(partition, col = structure(1:length(unique(partition)), names = unique(partition)[order(unique(partition))]), name = "cluster",
                  show_row_names = FALSE, width = unit(3, "mm")) +
  #LSD2
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R1']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R2']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R3']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R1']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R2']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R3']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R3') +
  
  #NSD3
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R1']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R2']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R3']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R1']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R3']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R3') +
  
  #H3K4me2
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R1']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R2']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R3']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R1']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R2']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R3']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R3') + 
  #H3K36me2
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R1']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R2']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R3']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R1']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R3']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R3') + 
  
  #IgG
  EnrichedHeatmap(mat_list[['IgG_M_1st']][r_order,], col = col_fun3, 
                  name = 'IgG_M_1st',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_1st') + 
  
  EnrichedHeatmap(mat_list[['IgG_M_2nd']][r_order,], col = col_fun3, 
                  name = 'IgG_M_2nd',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_2nd') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun3, 
                  name = 'NC_M_3rd',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC_M_3rd')
  
lgd = Legend(at = unique(partition)[order(unique(partition))], title = "Clusters", 
             type = "lines", legend_gp = gpar(col = 1:length(unique(partition))))

pdf('02.heatmap_partition.pdf',height = 10,width = 50)
draw(ht_list, split = partition, annotation_legend_list = list(lgd),heatmap_legend_side = "bottom",ht_gap = unit(0.8, "cm"))
dev.off()

##binary cluster heatmapp----
r_sum$cluster <- 'Co_enrich'
r_sum[r_sum$partition == 'cluster1',]$cluster <- 'False_Positive_Peaks' 
cluster <- r_sum$cluster
table(r_sum$cluster,r_sum$partition)

ht_list = Heatmap(cluster, col = structure(1:length(unique(cluster)), names = c('Co_enrich','False_Positive_Peaks')), name = "cluster",
                  show_row_names = FALSE, width = unit(3, "mm")) +
  EnrichedHeatmap(mat_list[[3]][r_order,], col = col_fun1, name = names(mat_list)[3],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:2,lwd = 3),
                                                                           ylim = c(0,2),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = names(mat_list)[3]) + 
  EnrichedHeatmap(mat_list[[7]][r_order,], col = col_fun1, name = names(mat_list)[7],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:2,lwd = 3),
                                                                           ylim = c(0,2),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = names(mat_list)[7]) + 
  EnrichedHeatmap(mat_list[[4]][r_order,], col = col_fun1, name = names(mat_list)[4],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:2,lwd = 3),
                                                                           ylim = c(0,0.4),
                                                                           #ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[4]) + 
  EnrichedHeatmap(mat_list[[8]][r_order,], col = col_fun1, name = names(mat_list)[8],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:2,lwd = 3),
                                                                           ylim = c(0,0.4),
                                                                           #ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[8]) + 
  EnrichedHeatmap(mat_list[[2]][r_order,], col = col_fun2, name = names(mat_list)[2],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:2,lwd = 3),
                                                                           ylim = c(0,3.5),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[2]) + 
  EnrichedHeatmap(mat_list[[6]][r_order,], col = col_fun2, name = names(mat_list)[6],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:2,lwd = 3),
                                                                           ylim = c(0,3.5),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[6]) + 
  EnrichedHeatmap(mat_list[[1]][r_order,], col = col_fun3, name = names(mat_list)[1],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:2,lwd = 3),
                                                                           ylim = c(0,0.4),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[1]) + 
  EnrichedHeatmap(mat_list[[5]][r_order,], col = col_fun3, name = names(mat_list)[5],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:2,lwd = 3),
                                                                           ylim = c(0,0.4),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[5]) + 
  EnrichedHeatmap(mat_list[[9]][r_order,], col = col_fun1, name = names(mat_list)[9],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:2,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[9]) 

lgd = Legend(at = unique(cluster)[order(unique(cluster))], title = "Clusters", 
             type = "lines", legend_gp = gpar(col = 1:length(unique(cluster))))

pdf('03.heatmap_enrich_cat.pdf',height = 13,width = 23)
draw(ht_list, split = cluster, annotation_legend_list = list(lgd),heatmap_legend_side = "bottom",ht_gap = unit(0.8, "cm"))
dev.off()

##Peak 3cat heatmap----
peak_cat <- r_sum$peak_cat

ht_list = Heatmap(peak_cat, col = structure(1:length(unique(peak_cat)), names = unique(peak_cat)[order(unique(peak_cat))]), name = "cluster",
                  show_row_names = FALSE, width = unit(3, "mm")) +
  EnrichedHeatmap(mat_list[[3]][r_order,], col = col_fun1, name = names(mat_list)[3],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:3,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = names(mat_list)[3]) + 
  EnrichedHeatmap(mat_list[[7]][r_order,], col = col_fun1, name = names(mat_list)[7],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:3,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = names(mat_list)[7]) + 
  EnrichedHeatmap(mat_list[[4]][r_order,], col = col_fun1, name = names(mat_list)[4],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:3,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[4]) + 
  EnrichedHeatmap(mat_list[[8]][r_order,], col = col_fun1, name = names(mat_list)[8],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:3,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[8]) + 
  EnrichedHeatmap(mat_list[[2]][r_order,], col = col_fun2, name = names(mat_list)[2],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:3,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[2]) + 
  EnrichedHeatmap(mat_list[[6]][r_order,], col = col_fun2, name = names(mat_list)[6],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:3,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[6]) + 
  EnrichedHeatmap(mat_list[[1]][r_order,], col = col_fun1, name = names(mat_list)[1],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:3,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[1]) + 
  EnrichedHeatmap(mat_list[[5]][r_order,], col = col_fun1, name = names(mat_list)[5],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:3,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[5]) + 
  EnrichedHeatmap(mat_list[[9]][r_order,], col = col_fun1, name = names(mat_list)[9],use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:3,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )),
                  axis_name = axis_name,
                  column_title = names(mat_list)[9]) 

lgd = Legend(at = unique(peak_cat)[order(unique(peak_cat))], title = "Clusters", 
             type = "lines", legend_gp = gpar(col = 1:length(unique(peak_cat))))

pdf('01.heatmap_peak_cat.pdf',height = 13,width = 23)
draw(ht_list, split = peak_cat, annotation_legend_list = list(lgd),heatmap_legend_side = "bottom",ht_gap = unit(0.8, "cm"))
dev.off()

#spikein_signals----
SampleSheet <- read.table("F:/UMproject/LSD2_NSD3/CutRun_2/03.ngsplot/spikein_reads_M.tsv",header = T)
SampleSheet

adj_enrichList1 <- raw_enrichList
for (i in 1:nrow(SampleSheet)) {
  sa <- SampleSheet$Sample_ID[i]
  adj_enrichList1[[sa]] <- raw_enrichList[[sa]]*SampleSheet$scale_factor[i]
  #adj_enrichList[[sa]] <- (raw_enrichList[[sa]]*SampleSheet$ngs_reads[i])/SampleSheet$Spikein_reads[i]/100
}

adj_enrichList2 <- raw_enrichList
for (i in 1:nrow(SampleSheet)) {
  sa <- SampleSheet$Sample_ID[i]
  #adj_enrichList2[[sa]] <- raw_enrichList[[sa]]*SampleSheet$scale_factor[i]
  adj_enrichList2[[sa]] <- (raw_enrichList[[sa]]*SampleSheet$ngs_reads[i])/SampleSheet$Spikein_reads[i]/10000
}


enrichList <- adj_enrichList2
sgControl_LSD2 <- list()
for (i in c('sgControl_LSD2_R1','sgControl_LSD2_R2','sgControl_LSD2_R3')) {
  sgControl_LSD2[[i]] <- enrichList[[i]]
}

sgControl_NSD3 <- list()
for (i in c('sgControl_NSD3_R1','sgControl_NSD3_R2','sgControl_NSD3_R3')) {
  sgControl_NSD3[[i]] <- enrichList[[i]]
}

r_sum <- data.frame(LSD2=(rowSums( sgControl_LSD2[[1]][,41:61]) + rowSums( sgControl_LSD2[[2]][,41:61]) + rowSums( sgControl_LSD2[[3]][,41:61]))/3,
                    NSD3=(rowSums( sgControl_NSD3[[1]][,41:61]) + rowSums( sgControl_NSD3[[2]][,41:61]) + rowSums( sgControl_NSD3[[3]][,41:61]))/3)

#r_sum$delta <- r_sum$LSD2-r_sum$NSD3

partition <-  paste0("cluster", kmeans(r_sum, centers = 3)$cluster)

table(partition)

r_sum$row_id <- rownames(r_sum)
r_sum$partition <- partition

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$delta))
}

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$LSD2))
}

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$NSD3))
}

r_sum$peak_cat <- str_split_fixed(r_sum$row_id,"_peaks",2)[,1]


r_sum <- r_sum[order(-r_sum$LSD2),]
r_order <- r_sum$row_id

cor.test(r_sum$LSD2,r_sum$NSD3)

enrichList <- adj_enrichList2

mat_list <- list()
for (i in 1:length(names(enrichList))) {
  mat_list[[i]] <- enrichList[[i]]
  attr(mat_list[[i]], "upstream_index") = 1:40
  attr(mat_list[[i]], "target_index") = 41:61
  attr(mat_list[[i]], "downstream_index") = 60:101
  attr(mat_list[[i]], "extend") = c(100000,100000)  
  class(mat_list[[i]]) = c("normalizedMatrix", "matrix")
  attr(mat_list[[i]], "signal_name") = names(enrichList)[i]
  attr(mat_list[[i]], "target_name") = "Peak"
  attr(mat_list[[i]], "target_is_single_point") = TRUE
  names(mat_list)[i] <- names(enrichList)[i]
}

mat_list[[3]]

col_fun1 = colorRamp2(c(0,1), c("lightblue", "red"))
col_fun2 = colorRamp2(c(0,2), c("lightblue", "red"))
col_fun3 = colorRamp2(c(0,8), c("lightblue", "red"))
col_fun4 = colorRamp2(c(0,18), c("lightblue", "red"))

axis_name = c('-100kb','5End','3End','100kb')
r_sum <- r_sum[order(-r_sum$LSD2),]
r_order <- r_sum$row_id

##Partition heatmap-----
partition <- r_sum$partition
names(mat_list)

ht_list = Heatmap(partition, col = structure(1:length(unique(partition)), names = unique(partition)[order(unique(partition))]), name = "cluster",
                  show_row_names = FALSE, width = unit(3, "mm")) +
  #LSD2
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R1']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R2']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R3']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R1']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R2']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R3']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R3') +
  
  #NSD3
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R1']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R2']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R3']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R1']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R3']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R3') +
  
  #H3K4me2
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R1']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R2']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R3']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R1']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R2']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R3']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R3') + 
  #H3K36me2
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R1']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R2']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R3']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R1']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R3']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R3') + 
  
  #IgG
  EnrichedHeatmap(mat_list[['IgG_M_1st']][r_order,], col = col_fun3, 
                  name = 'IgG_M_1st',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_1st') + 
  
  EnrichedHeatmap(mat_list[['IgG_M_2nd']][r_order,], col = col_fun3, 
                  name = 'IgG_M_2nd',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_2nd') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun3, 
                  name = 'NC_M_3rd',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC_M_3rd')

lgd = Legend(at = unique(partition)[order(unique(partition))], title = "Clusters", 
             type = "lines", legend_gp = gpar(col = 1:length(unique(partition))))

pdf('02.heatmap_partition_spike.pdf',height = 10,width = 50)
draw(ht_list, split = partition, annotation_legend_list = list(lgd),heatmap_legend_side = "bottom",ht_gap = unit(0.8, "cm"))
dev.off()

##binary cluster heatmapp----
r_sum$cluster <- 'Co_enrich'
r_sum[r_sum$partition == 'cluster1',]$cluster <- 'False_Positive_Peaks' 
cluster <- r_sum$cluster
table(r_sum$cluster,r_sum$partition)

col_fun1 = colorRamp2(c(0,1), c("lightblue", "red"))
col_fun2 = colorRamp2(c(0,2), c("lightblue", "red"))
col_fun3 = colorRamp2(c(0,8), c("lightblue", "red"))
col_fun4 = colorRamp2(c(0,18), c("lightblue", "red"))

ht_list = Heatmap(cluster, col = structure(1:length(unique(cluster)), names = c('Co_enrich','False_Positive_Peaks')), name = "cluster",
                  show_row_names = FALSE, width = unit(3, "mm")) +
  #LSD2
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R1']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R2']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R3']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R1']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R2']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R3']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R3') +
  
  #NSD3
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R1']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R2']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R3']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R1']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R3']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R3') +
  
  #H3K4me2
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R1']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R2']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R3']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R1']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R2']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R3']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R3') + 
  #H3K36me2
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R1']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R2']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R3']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R1']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R3']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R3') + 
  
  #IgG
  EnrichedHeatmap(mat_list[['IgG_M_1st']][r_order,], col = col_fun3, 
                  name = 'IgG_M_1st',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_1st') + 
  
  EnrichedHeatmap(mat_list[['IgG_M_2nd']][r_order,], col = col_fun3, 
                  name = 'IgG_M_2nd',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_2nd') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun3, 
                  name = 'NC_M_3rd',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC_M_3rd')

lgd = Legend(at = unique(partition)[order(unique(partition))], title = "Clusters", 
             type = "lines", legend_gp = gpar(col = 1:length(unique(partition))))

pdf('04.heatmap_enrich_cat_spike.pdf',height = 13,width = 23)
draw(ht_list, split = cluster, annotation_legend_list = list(lgd),heatmap_legend_side = "bottom",ht_gap = unit(0.8, "cm"))
dev.off()

##Peak 3cat heatmap----
peak_cat <- r_sum$peak_cat

ht_list = Heatmap(peak_cat, col = structure(1:length(unique(peak_cat)), names = unique(peak_cat)[order(unique(peak_cat))]), name = "cluster",
                  show_row_names = FALSE, width = unit(3, "mm")) +
  #LSD2
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R1']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R2']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_LSD2_R3']][r_order,], col = col_fun2, 
                  name = 'sgControl_LSD2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R1']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R2']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2_R3']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_LSD2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2_R3') +
  
  #NSD3
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R1']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R2']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_NSD3_R3']][r_order,], col = col_fun3, 
                  name = 'sgControl_NSD3_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R1']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3_R3']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_NSD3_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3_R3') +
  
  #H3K4me2
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R1']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R2']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2_R3']][r_order,], col = col_fun1, 
                  name = 'sgControl_H3K4me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R1']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R2']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2_R3']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_H3K4me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2_R3') + 
  #H3K36me2
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R1']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R1') + 
  
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R2']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2_R3']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K36me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2_R3') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R1']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R1') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R2') +
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2_R3']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K36me2_R3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2_R3') + 
  
  #IgG
  EnrichedHeatmap(mat_list[['IgG_M_1st']][r_order,], col = col_fun3, 
                  name = 'IgG_M_1st',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_1st') + 
  
  EnrichedHeatmap(mat_list[['IgG_M_2nd']][r_order,], col = col_fun3, 
                  name = 'IgG_M_2nd',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_2nd') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun3, 
                  name = 'NC_M_3rd',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           #ylim = c(0,4.5),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC_M_3rd')

lgd = Legend(at = unique(partition)[order(unique(partition))], title = "Clusters", 
             type = "lines", legend_gp = gpar(col = 1:length(unique(partition))))

pdf('01.heatmap_peak_cat_Spike.pdf',height = 13,width = 23)
draw(ht_list, split = peak_cat, annotation_legend_list = list(lgd),heatmap_legend_side = "bottom",ht_gap = unit(0.8, "cm"))
dev.off()

rm(ht_list)

#save.image('kmeans_heatmap.Rdata')
#saveRDS(r_sum,file = 'r_sum.rds')

#Merge combined signals1-----
enrichList <- adj_enrichList2

sgControl_LSD2 <- (enrichList[['sgControl_LSD2_R1']] + enrichList[['sgControl_LSD2_R2']] + enrichList[['sgControl_LSD2_R3']])/3
sgLSD2_LSD2 <- (enrichList[['sgLSD2_LSD2_R1']] + enrichList[['sgLSD2_LSD2_R2']] + enrichList[['sgLSD2_LSD2_R3']])/3

sgControl_NSD3 <- (enrichList[['sgControl_NSD3_R1']] + enrichList[['sgControl_NSD3_R2']] + enrichList[['sgControl_NSD3_R3']])/3
sgLSD2_NSD3 <- (enrichList[['sgLSD2_NSD3_R1']] + enrichList[['sgLSD2_NSD3_R2']] + enrichList[['sgLSD2_NSD3_R3']])/3

sgControl_H3K4me2 <- (enrichList[['sgControl_H3K4me2_R1']] + enrichList[['sgControl_H3K4me2_R2']] + enrichList[['sgControl_H3K4me2_R3']])/3
sgLSD2_H3K4me2 <- (enrichList[['sgLSD2_H3K4me2_R1']] + enrichList[['sgLSD2_H3K4me2_R2']] + enrichList[['sgLSD2_H3K4me2_R3']])/3

sgControl_H3K36me2 <- (enrichList[['sgControl_H3K36me2_R1']] + enrichList[['sgControl_H3K36me2_R2']] + enrichList[['sgControl_H3K36me2_R3']])/3
sgLSD2_H3K36me2 <- (enrichList[['sgLSD2_H3K36me2_R1']] + enrichList[['sgLSD2_H3K36me2_R2']] + enrichList[['sgLSD2_H3K36me2_R3']])/3


r_sum <- data.frame(LSD2=rowSums(sgControl_LSD2[,41:61]),
                    NSD3=rowSums(sgControl_NSD3[,41:61]))

r_sum$delta <- r_sum$LSD2-r_sum$NSD3

partition <-  paste0("cluster", kmeans(r_sum, centers = 5)$cluster)

table(partition)

r_sum$row_id <- rownames(r_sum)
r_sum$partition <- partition

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$delta))
}

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$LSD2))
}

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$NSD3))
}

r_sum$peak_cat <- str_split_fixed(r_sum$row_id,"_peaks",2)[,1]


r_sum <- r_sum[order(-r_sum$LSD2),]
r_order <- r_sum$row_id

cor.test(r_sum$LSD2,r_sum$NSD3)

names(adj_enrichList2)

enrichList <- list(sgControl_LSD2=sgControl_LSD2,
                   sgLSD2_LSD2=sgLSD2_LSD2,
                   sgControl_NSD3=sgControl_NSD3,
                   sgLSD2_NSD3=sgLSD2_NSD3,
                   sgControl_H3K4me2=sgControl_H3K4me2,
                   sgLSD2_H3K4me2=sgLSD2_H3K4me2,
                   sgControl_H3K36me2=sgControl_H3K36me2,
                   sgLSD2_H3K36me2=sgLSD2_H3K36me2,
                   IgG_M_1st=adj_enrichList2[["IgG_M_1st"]],
                   IgG_M_2nd=adj_enrichList2[["IgG_M_2nd"]],
                   NC_M_3rd=adj_enrichList2[["NC_M_3rd"]])

mat_list <- list()
for (i in 1:length(names(enrichList))) {
  mat_list[[i]] <- enrichList[[i]]
  attr(mat_list[[i]], "upstream_index") = 1:40
  attr(mat_list[[i]], "target_index") = 41:61
  attr(mat_list[[i]], "downstream_index") = 60:101
  attr(mat_list[[i]], "extend") = c(100000,100000)  
  class(mat_list[[i]]) = c("normalizedMatrix", "matrix")
  attr(mat_list[[i]], "signal_name") = names(enrichList)[i]
  attr(mat_list[[i]], "target_name") = "Peak"
  attr(mat_list[[i]], "target_is_single_point") = TRUE
  names(mat_list)[i] <- names(enrichList)[i]
}

mat_list[[3]]

col_fun1 = colorRamp2(c(0,0.3), c("lightblue", "red"))
col_fun2 = colorRamp2(c(0,0.15), c("lightblue", "red"))
col_fun3 = colorRamp2(c(0,1), c("lightblue", "red"))
col_fun4 = colorRamp2(c(0,0.1), c("lightblue", "red"))

axis_name = c('-100kb','5End','3End','100kb')
r_sum <- r_sum[order(-r_sum$LSD2),]
r_order <- r_sum$row_id


##Partition heatmap-----
partition <- r_sum$partition
names(mat_list)

ht_list = Heatmap(partition, col = structure(1:length(unique(partition)), names = unique(partition)[order(unique(partition))]), name = "cluster",
                  show_row_names = FALSE, width = unit(3, "mm")) +
  #LSD2
  EnrichedHeatmap(mat_list[['sgControl_LSD2']][r_order,], col = col_fun1, 
                  name = 'sgControl_LSD2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_LSD2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2') +
  
  EnrichedHeatmap(mat_list[['IgG_M_1st']][r_order,], col = col_fun1, 
                  name = 'IgG_M_1st',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_1st') + 
  #NSD3
  EnrichedHeatmap(mat_list[['sgControl_NSD3']][r_order,], col = col_fun2, 
                  name = 'sgControl_NSD3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.18),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_NSD3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.18),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun2, 
                  name = 'NC_M1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.18),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC_M1') +
  
  #H3K4me2
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K4me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,1.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K4me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,1.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun2, 
                  name = 'NC_M2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,1.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC_M2') +
  
  #H3K36me2
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2']][r_order,], col = col_fun4, 
                  name = 'sgControl_H3K36me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.12),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2']][r_order,], col = col_fun4, 
                  name = 'sgLSD2_H3K36me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.12),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2') +
  
  EnrichedHeatmap(mat_list[['IgG_M_2nd']][r_order,], col = col_fun4, 
                  name = 'IgG_M_2nd',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.12),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_2nd')
  

lgd = Legend(at = unique(partition)[order(unique(partition))], title = "Clusters", 
             type = "lines", legend_gp = gpar(col = 1:length(unique(partition))))

pdf('02.heatmap_partition_spike_M1.pdf',height = 10,width = 25)
draw(ht_list, split = partition, annotation_legend_list = list(lgd),heatmap_legend_side = "bottom",ht_gap = unit(0.8, "cm"))
dev.off()

#Merge combined signals2-----
enrichList <- adj_enrichList2

sgControl_LSD2 <- (enrichList[['sgControl_LSD2_R1']] + enrichList[['sgControl_LSD2_R2']] + enrichList[['sgControl_LSD2_R3']])/3
sgLSD2_LSD2 <- (enrichList[['sgLSD2_LSD2_R1']] + enrichList[['sgLSD2_LSD2_R2']] + enrichList[['sgLSD2_LSD2_R3']])/3

sgControl_NSD3 <- (enrichList[['sgControl_NSD3_R1']] + enrichList[['sgControl_NSD3_R3']])/2
sgLSD2_NSD3 <- (enrichList[['sgLSD2_NSD3_R2']] + enrichList[['sgLSD2_NSD3_R3']])/2

sgControl_H3K4me2 <- (enrichList[['sgControl_H3K4me2_R1']] + enrichList[['sgControl_H3K4me2_R2']] + enrichList[['sgControl_H3K4me2_R3']])/3
sgLSD2_H3K4me2 <- (enrichList[['sgLSD2_H3K4me2_R1']] + enrichList[['sgLSD2_H3K4me2_R2']] + enrichList[['sgLSD2_H3K4me2_R3']])/3

sgControl_H3K36me2 <- (enrichList[['sgControl_H3K36me2_R1']] + enrichList[['sgControl_H3K36me2_R2']])/2
sgLSD2_H3K36me2 <- (enrichList[['sgLSD2_H3K36me2_R2']] + enrichList[['sgLSD2_H3K36me2_R3']])/2


r_sum <- data.frame(LSD2=rowSums(sgControl_LSD2[,41:61]),
                    NSD3=rowSums(sgControl_NSD3[,41:61]))

#r_sum$co_enrich <- r_sum$LSD2*r_sum$NSD3
#r_sum$delta <- r_sum$LSD2-r_sum$NSD3

partition <-  paste0("cluster", kmeans(r_sum$LSD2, centers = 3)$cluster)

table(partition)

r_sum$row_id <- rownames(r_sum)
r_sum$partition <- partition

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$delta))
}

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$co_enrich))
}

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$LSD2))
}

for (i in 1:length(unique(partition))) {
  print(mean(r_sum[r_sum$partition == paste0('cluster',i),]$NSD3))
}

r_sum$peak_cat <- str_split_fixed(r_sum$row_id,"_peaks",2)[,1]


r_sum <- r_sum[order(-r_sum$LSD2),]
r_order <- r_sum$row_id

cor.test(r_sum$LSD2,r_sum$NSD3)

names(adj_enrichList2)

enrichList <- list(sgControl_LSD2=sgControl_LSD2,
                   sgLSD2_LSD2=sgLSD2_LSD2,
                   sgControl_NSD3=sgControl_NSD3,
                   sgLSD2_NSD3=sgLSD2_NSD3,
                   sgControl_H3K4me2=sgControl_H3K4me2,
                   sgLSD2_H3K4me2=sgLSD2_H3K4me2,
                   sgControl_H3K36me2=sgControl_H3K36me2,
                   sgLSD2_H3K36me2=sgLSD2_H3K36me2,
                   IgG_M_1st=adj_enrichList2[["IgG_M_1st"]],
                   IgG_M_2nd=adj_enrichList2[["IgG_M_2nd"]],
                   NC_M_3rd=adj_enrichList2[["NC_M_3rd"]])

mat_list <- list()
for (i in 1:length(names(enrichList))) {
  mat_list[[i]] <- enrichList[[i]]
  attr(mat_list[[i]], "upstream_index") = 1:40
  attr(mat_list[[i]], "target_index") = 41:61
  attr(mat_list[[i]], "downstream_index") = 60:101
  attr(mat_list[[i]], "extend") = c(100000,100000)  
  class(mat_list[[i]]) = c("normalizedMatrix", "matrix")
  attr(mat_list[[i]], "signal_name") = names(enrichList)[i]
  attr(mat_list[[i]], "target_name") = "Peak"
  attr(mat_list[[i]], "target_is_single_point") = FALSE
  names(mat_list)[i] <- names(enrichList)[i]
}

mat_list[[3]]

col_fun1 = colorRamp2(c(0,0.3), c("lightblue", "red"))
col_fun2 = colorRamp2(c(0,0.3), c("lightblue", "red"))
col_fun3 = colorRamp2(c(0,0.3), c("lightblue", "red"))
col_fun4 = colorRamp2(c(0,0.08), c("lightblue", "red"))

axis_name = c('-100kb','5End','3End','100kb')

##Partition heatmap-----
r_sum <- r_sum[order(-r_sum$LSD2),]
colnames(r_sum)
table(r_sum$partition)

r_sum_sub <- r_sum %>%
  group_by(partition) %>%
  sample_frac(size = 0.2)

table(r_sum_sub$partition)

r_order <- r_sum_sub$row_id
partition <- r_sum_sub$partition
names(mat_list)

ht_list = Heatmap(partition, col = structure(1:length(unique(partition)), names = unique(partition)[order(unique(partition))]), name = "cluster",
                  show_row_names = FALSE, width = unit(3, "mm")) +
  #LSD2
  EnrichedHeatmap(mat_list[['sgControl_LSD2']][r_order,], col = col_fun1, 
                  name = 'sgControl_LSD2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_LSD2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2') +
  
  EnrichedHeatmap(mat_list[['IgG_M_1st']][r_order,], col = col_fun1, 
                  name = 'IgG_M_1st',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_1st') + 
  #NSD3
  EnrichedHeatmap(mat_list[['sgControl_NSD3']][r_order,], col = col_fun2, 
                  name = 'sgControl_NSD3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_NSD3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun2, 
                  name = 'NC1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC1') +
  
  #H3K4me2
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K4me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,1.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K4me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,1.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun3, 
                  name = 'NC2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,1.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC2') +
  
  #H3K36me2
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2']][r_order,], col = col_fun4, 
                  name = 'sgControl_H3K36me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.06),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2']][r_order,], col = col_fun4, 
                  name = 'sgLSD2_H3K36me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.06),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun3, 
                  name = 'NC3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.06),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC3')

lgd = Legend(at = unique(partition)[order(unique(partition))], title = "Clusters", 
             type = "lines", legend_gp = gpar(col = 1:length(unique(partition))))

pdf('02.heatmap_partition_spike_M3.pdf',height = 10,width = 30)
draw(ht_list, split = partition, annotation_legend_list = list(lgd),heatmap_legend_side = "bottom",ht_gap = unit(0.8, "cm"))
dev.off()

##cat heatmap-----
unique(r_sum$partition)
r_sum$cluster <- 'Non_enrich'
#r_sum[r_sum$partition == 'cluster1',]$cluster <- 'LSD2_enrich' 
r_sum[r_sum$partition == 'cluster3',]$cluster <- 'Co_enrich' 
#r_sum[r_sum$partition == 'cluster4',]$cluster <- 'NSD3_enrich' 
r_sum$cluster <- factor(r_sum$cluster,levels = c('Co_enrich','Non_enrich'))

r_sum <- r_sum[order(-r_sum$LSD2),]

r_sum_sub <- r_sum %>%
  group_by(partition) %>%
  sample_frac(size = 0.2)

r_order <- r_sum_sub$row_id
cluster <- r_sum_sub$cluster
ht_list = Heatmap(cluster, col = structure(1:length(unique(cluster)), names = c('Co_enrich','Non_enrich')), name = "cluster",
                  show_row_names = FALSE, width = unit(3, "mm")) +
  #LSD2
  EnrichedHeatmap(mat_list[['sgControl_LSD2']][r_order,], col = col_fun1, 
                  name = 'sgControl_LSD2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_LSD2') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_LSD2']][r_order,], col = col_fun1, 
                  name = 'sgLSD2_LSD2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_LSD2') +
  
  EnrichedHeatmap(mat_list[['IgG_M_1st']][r_order,], col = col_fun1, 
                  name = 'IgG_M_1st',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.35),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'IgG_M_1st') + 
  #NSD3
  EnrichedHeatmap(mat_list[['sgControl_NSD3']][r_order,], col = col_fun2, 
                  name = 'sgControl_NSD3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_NSD3') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_NSD3']][r_order,], col = col_fun2, 
                  name = 'sgLSD2_NSD3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_NSD3') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun2, 
                  name = 'NC1',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC1') +
  
  #H3K4me2
  EnrichedHeatmap(mat_list[['sgControl_H3K4me2']][r_order,], col = col_fun3, 
                  name = 'sgControl_H3K4me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,1.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K4me2') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K4me2']][r_order,], col = col_fun3, 
                  name = 'sgLSD2_H3K4me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,1.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K4me2') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun3, 
                  name = 'NC2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,1.3),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC2') +
  
  #H3K36me2
  EnrichedHeatmap(mat_list[['sgControl_H3K36me2']][r_order,], col = col_fun4, 
                  name = 'sgControl_H3K36me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.06),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgControl_H3K36me2') + 
  
  EnrichedHeatmap(mat_list[['sgLSD2_H3K36me2']][r_order,], col = col_fun4, 
                  name = 'sgLSD2_H3K36me2',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.06),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'sgLSD2_H3K36me2') +
  
  EnrichedHeatmap(mat_list[['NC_M_3rd']][r_order,], col = col_fun3, 
                  name = 'NC3',use_raster = TRUE,raster_quality = 25,
                  top_annotation = HeatmapAnnotation(lines = anno_enriched(gp = gpar(col = 1:9,lwd = 3),
                                                                           ylim = c(0,0.06),
                                                                           height = unit(4, "cm")
                  )), 
                  axis_name = axis_name,
                  column_title = 'NC3')


lgd = Legend(at = c("Co_enrich","Non_enrich"), title = "Clusters", 
             type = "lines", legend_gp = gpar(col = 1:length(unique(cluster))))

pdf('02.heatmap_cluster_spike_M4.pdf',height = 10,width = 30)
draw(ht_list, split = cluster, annotation_legend_list = list(lgd),heatmap_legend_side = "bottom",ht_gap = unit(0.8, "cm"))
dev.off()

Peaks_loc <- read.table("F:/UMproject/LSD2_NSD3/CutRun_2/03.ngsplot/Total_peaks.bed")
r_sum$peak_id <- str_split_fixed(r_sum$row_id,":",2)[,1]
nrow(Peaks_loc[Peaks_loc$V4 %in% r_sum[r_sum$cluster == 'Co_enrich',]$peak_id,])

Co_enrich <- Peaks_loc[Peaks_loc$V4 %in% r_sum[r_sum$cluster == 'Co_enrich',]$peak_id,]
rownames(Co_enrich) <- 1:nrow(Co_enrich)
Co_enrich$V4 <- paste('Co_enrich_',rownames(Co_enrich),sep = "")

Non_enrich <- Peaks_loc[Peaks_loc$V4 %in% r_sum[r_sum$cluster == 'Non_enrich',]$peak_id,]
rownames(Non_enrich) <- 1:nrow(Non_enrich)
Non_enrich$V4 <- paste('Non_enrich_',rownames(Non_enrich),sep = "")

LSD2_enrich <- Peaks_loc[Peaks_loc$V4 %in% r_sum[r_sum$cluster == 'LSD2_enrich',]$peak_id,]
rownames(LSD2_enrich) <- 1:nrow(LSD2_enrich)
LSD2_enrich$V4 <- paste('LSD2_enrich_',rownames(LSD2_enrich),sep = "")

NSD3_enrich <- Peaks_loc[Peaks_loc$V4 %in% r_sum[r_sum$cluster == 'NSD3_enrich',]$peak_id,]
rownames(NSD3_enrich) <- 1:nrow(NSD3_enrich)
NSD3_enrich$V4 <- paste('NSD3_enrich_',rownames(NSD3_enrich),sep = "")

cluster_bed <- rbind(Co_enrich,LSD2_enrich,NSD3_enrich,Non_enrich)

write.table(cluster_bed,file = "bed/cluster_peaks.bed",quote = F,row.names = F,col.names = F,sep = '\t')

write.table(Co_enrich,file = "bed/Co_enrich.bed",quote = F,row.names = F,col.names = F,sep = '\t')
write.table(LSD2_enrich,file = "bed/LSD2_enrich.bed",quote = F,row.names = F,col.names = F,sep = '\t')
write.table(NSD3_enrich,file = "bed/NSD3_enrich.bed",quote = F,row.names = F,col.names = F,sep = '\t')
write.table(Non_enrich,file = "bed/Non_enrich.bed",quote = F,row.names = F,col.names = F,sep = '\t')

#saveRDS(r_sum,file = "r_sum.rds")
save.image('kmeans_heatmap.Rdata')


nrow(r_sum)
table(r_sum$partition)
