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

smoothvec <- function(v, radius, method=c('mean', 'median')){
  stopifnot(is.vector(v))
  stopifnot(length(v) > 0)
  stopifnot(radius > 0 && radius < 1)
  
  halfwin <- ceiling(length(v) * radius)
  s <- rep(NA, length(v))
  
  for(i in 1:length(v)){
    winpos <- (i - halfwin) : (i + halfwin)
    winpos <- winpos[winpos > 0 & winpos <= length(v)]
    if(method == 'mean'){
      s[i] <- mean(v[winpos])
    }else if(method == 'median'){
      s[i] <- median(v[winpos])
    }
  }
  s
}

set.seed(123)
gene_cat <- c('allgene_FL300bp_L3000bp')
norm_cat <- c('CPM')
ylab <- c('Log2FC')

bam_cat <- c('rmdup_all') #sorted , dedup , dedup.120bps

#setwd(paste("F:/UMProject/mouse_PanNET/Outs/CutRun/03.ngsplot/Norm_by_",norm_cat,'/',bam_cat,'/',gene_cat,'/',sep = ""))
setwd(paste("F:/UMProject/NSD3/mouse_PanNET/Outs/CnR/ngsplot/Norm_by_",norm_cat,'/',bam_cat,'/',gene_cat,'/',sep = ""))
getwd()
load("heatmap.RData")
sample_id <- ctg.tbl$title

length((rownames(enrichList[[1]]) == rownames(enrichList[[2]])) == TRUE)

for (i in 1:length(sample_id)) {
  names(enrichList)[i] <- sample_id[i]
}

{
  sample <- c('sgControl','sgNSD3')
  IgG_sample <- c("sgControl_IgG_R2","sgControl_IgG_R2",
                  "sgControl_IgG_R2","sgControl_IgG_R2")
  
  #H2AZ norm heatmap ----
  sample <- c('sgControl','sgNSD3')
  H2AZ_sample <- c("sgControl_H2AZ_R1","sgControl_H2AZ_R2",
                   "sgNSD3_H2AZ_R1","sgNSD3_H2AZ_R2")
  H2AZ_mat <- list()
  
  
  H2AZ_mat[[1]] <- ((enrichList[[H2AZ_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[H2AZ_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2
  H2AZ_mat[[2]] <- ((enrichList[[H2AZ_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[H2AZ_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2
  names(H2AZ_mat) <- sample 
  
  #NSD3 norm heatmap ----
  sample <- c('sgControl','sgNSD3')
  NSD3_sample <- c("sgControl_NSD3_R1","sgControl_NSD3_R2",
                   "sgNSD3_NSD3_R1","sgNSD3_NSD3_R2")
  NSD3_mat <- list()
  
  NSD3_mat[[1]] <- ((enrichList[[NSD3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[NSD3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2
  NSD3_mat[[2]] <- ((enrichList[[NSD3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[NSD3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2
  names(NSD3_mat) <- sample
  
  #H3K36me2 norm heatmap ---- 
  sample <- c('sgControl','sgNSD3')
  H3K36me2_mat <- list()
  
  H3K36me2_sample <- c("sgControl_H3K36me2_R1","sgControl_H3K36me2_R2",
                       "sgNSD3_H3K36me2_R1","sgNSD3_H3K36me2_R2")
  
  H3K36me2_mat[[1]] <- ((enrichList[[H3K36me2_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[H3K36me2_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2
  H3K36me2_mat[[2]] <- ((enrichList[[H3K36me2_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[H3K36me2_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2
  names(H3K36me2_mat) <- sample  
  
  #H3K36me3 norm heatmap ---- 
  sample <- c('sgControl','sgNSD3')
  H3K36me3_mat <- list()
  
  H3K36me3_sample <- c("sgControl_H3K36me3_R2","sgControl_H3K36me3_R2",
                       "sgNSD3_H3K36me3_R1","sgNSD3_H3K36me3_R1")
  
  H3K36me3_mat[[1]] <- ((enrichList[[H3K36me3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[H3K36me3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2
  H3K36me3_mat[[2]] <- ((enrichList[[H3K36me3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[H3K36me3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2
  names(H3K36me3_mat) <- sample  
  
  data <-log2(H2AZ_mat[[1]])
  data2<-log2(H2AZ_mat[[2]])
  data3<-log2(NSD3_mat[[1]])
  data4<-log2(NSD3_mat[[2]])
  data5<-log2(H3K36me2_mat[[1]])
  data6<-log2(H3K36me2_mat[[2]])
  data7<-log2(H3K36me3_mat[[1]])
  data8<-log2(H3K36me3_mat[[2]])
}

#H2AZ cat heirachical----
Y_H2AZ<-data.frame()
for (i in 1:nrow(data)) {
  Y_H2AZ[i,1]<-mean(data[i,21])
}

length(row.names(data))
length(unique(row.names(data)))

colnames(Y_H2AZ) <- c('H2AZ')
row.names(Y_H2AZ)<-row.names(data)

hc=hclust(dist(Y_H2AZ,method = 'euclidean'),method = 'ward.D')

#fviz_nbclust(Y_H2AZ, FUN = hcut, method = "wss",k.max = 10)
#fviz_nbclust(Y_H2AZ, FUN = hcut, method = "silhouette",k.max = 10)


plot(hc,label=FALSE)
rect.hclust(hc, k=4, border="red")

hc_result=cutree(hc,k = 4)

Y_H2AZ$gene <- rownames(Y_H2AZ)
Y_H2AZ$hc <- hc_result
table(Y_H2AZ$hc)

mean(Y_H2AZ[Y_H2AZ$hc==1,]$H2AZ)
mean(Y_H2AZ[Y_H2AZ$hc==2,]$H2AZ)
mean(Y_H2AZ[Y_H2AZ$hc==3,]$H2AZ)
mean(Y_H2AZ[Y_H2AZ$hc==4,]$H2AZ)

Y_H2AZ$hc <- factor(Y_H2AZ$hc,levels = c(2,1,4,3))

Y_H2AZ$cat <- NA
Y_H2AZ[Y_H2AZ$hc == 2,'cat'] <- 'A'
Y_H2AZ[Y_H2AZ$hc == 1,'cat'] <- 'B'
Y_H2AZ[Y_H2AZ$hc == 4,'cat'] <- 'C'
Y_H2AZ[Y_H2AZ$hc == 3,'cat'] <- 'D'

Y_H2AZ <- Y_H2AZ[order(Y_H2AZ$cat),]
r_order <- Y_H2AZ$gene

H2AZ_class_A <- Y_H2AZ[Y_H2AZ$cat == 'A','gene']
H2AZ_class_B <- Y_H2AZ[Y_H2AZ$cat == 'B','gene']
H2AZ_class_C <- Y_H2AZ[Y_H2AZ$cat == 'C','gene']
H2AZ_class_D <- Y_H2AZ[Y_H2AZ$cat == 'D','gene']

H2AZ_class_list <- list(H2AZ_class_A=H2AZ_class_A,
                        H2AZ_class_B=H2AZ_class_B,
                        H2AZ_class_C=H2AZ_class_C,
                        H2AZ_class_D=H2AZ_class_D)

#NSD3 CAT HEIRACHICAL----
Y_NSD3<-data.frame()
for (i in 1:nrow(data3)) {
  Y_NSD3[i,1]<-mean(data3[i,21])
}

colnames(Y_NSD3) <- c('NSD3')
row.names(Y_NSD3)<-row.names(data3)

hc=hclust(dist(Y_NSD3,method = 'euclidean'),method = 'ward.D')

#fviz_nbclust(Y_NSD3, FUN = hcut, method = "wss",k.max = 10)
#fviz_nbclust(Y_NSD3, FUN = hcut, method = "silhouette",k.max = 10)

plot(hc,label=FALSE)
rect.hclust(hc, k=4, border="red")

hc_result=cutree(hc,k = 4)

Y_NSD3$gene <- rownames(Y_NSD3)
Y_NSD3$hc <- hc_result
table(Y_NSD3$hc)

mean(Y_NSD3[Y_NSD3$hc==1,]$NSD3)
mean(Y_NSD3[Y_NSD3$hc==2,]$NSD3)
mean(Y_NSD3[Y_NSD3$hc==3,]$NSD3)
mean(Y_NSD3[Y_NSD3$hc==4,]$NSD3)

Y_NSD3$hc <- factor(Y_NSD3$hc,levels = c(4,2,1,3))

Y_NSD3$cat <- NA
Y_NSD3[Y_NSD3$hc == 4,'cat'] <- 'A'
Y_NSD3[Y_NSD3$hc == 2,'cat'] <- 'B'
Y_NSD3[Y_NSD3$hc == 1,'cat'] <- 'C'
Y_NSD3[Y_NSD3$hc == 3,'cat'] <- 'D'

Y_NSD3 <- Y_NSD3[order(Y_NSD3$cat),]
r_order <- Y_NSD3$gene

NSD3_class_A <- Y_NSD3[Y_NSD3$cat == 'A','gene']
NSD3_class_B <- Y_NSD3[Y_NSD3$cat == 'B','gene']
NSD3_class_C <- Y_NSD3[Y_NSD3$cat == 'C','gene']
NSD3_class_D <- Y_NSD3[Y_NSD3$cat == 'D','gene']

NSD3_class_list <- list(NSD3_class_A=NSD3_class_A,
                        NSD3_class_B=NSD3_class_B,
                        NSD3_class_C=NSD3_class_C,
                        NSD3_class_D=NSD3_class_D)

gom.obj <- newGOM(NSD3_class_list,H2AZ_class_list,genome.size = 21744)


pdf('0.GeneOverlap.pdf',width = 8,height = 8)
drawHeatmap(gom.obj)
dev.off()

NSD3_list <- list(NSD3_enrich = c(NSD3_class_list[[1]],NSD3_class_list[[2]]),
                  NSD3_non_enrich = c(NSD3_class_list[[3]],NSD3_class_list[[4]]))

H2AZ_list <- list(H2AZ_enrich = c(H2AZ_class_list[[1]],H2AZ_class_list[[2]],H2AZ_class_list[[3]]),
                  H2AZ_non_enrich = c(H2AZ_class_list[[4]]))

gom.obj <- newGOM(NSD3_list,H2AZ_list,genome.size = 21744)
drawHeatmap(gom.obj)

venn_list <- list(NSD3_enrich = NSD3_list[[1]],
                  H2AZ_enrich = H2AZ_list[[1]])

length(intersect(NSD3_list[[1]],H2AZ_list[[1]]))


#save.image(file = "Hclust_heatmap.Rdata")

#NSD3 CALSS HEATMAP----
brewer.pal.info
display.brewer.pal(9,"RdBu")
coul <- colorRampPalette(brewer.pal(9, "RdBu"))(9)
coul
col_fun = colorRamp2(c(-1,0,1), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-1,0,1),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

table(Y_NSD3$cat)
r_order <- Y_NSD3$gene
r_split <- factor(rep(c('A','B','C','D'),c(5335,4577,9298,2534)))
r_split <- factor(rep(c('A','B'),c(sum(5335,4577),sum(9298,2534))))
table(r_split)

ht1<-Heatmap(data[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE)


ht2<-Heatmap(data2[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

col_fun = colorRamp2(c(-0.5,0,0.5), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-0.5,0,0.5),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht3<-Heatmap(data3[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)


ht4<-Heatmap(data4[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

col_fun = colorRamp2(c(-0.8,0,0.8), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-0.8,0,0.8),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht5<-Heatmap(data5[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)


ht6<-Heatmap(data6[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

ht_list<-ht3+ht1

pdf('heatmap_NSD3_CLASS.pdf',height = 10,width = 25)
draw(ht_list,heatmap_legend_side = "top",ht_gap = unit(0.4, "cm"))
dev.off()

#H2AZ CLASS HEATMAP----
brewer.pal.info
display.brewer.pal(9,"RdBu")
coul <- colorRampPalette(brewer.pal(9, "RdBu"))(9)
coul
col_fun = colorRamp2(c(-1,0,1), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-1,0,1),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

table(Y_H2AZ$cat)

r_order <- Y_H2AZ$gene


r_split <- factor(rep(c('A','B','C','D'),c(3373,3170,5689,9512)))
r_split <- factor(rep(c('A','B'),c(sum(3373,3170,5689),sum(9512))))
table(r_split)

ht1<-Heatmap(data[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE)


ht2<-Heatmap(data2[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

col_fun = colorRamp2(c(-0.5,0,0.5), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-0.5,0,0.5),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht3<-Heatmap(data3[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)


ht4<-Heatmap(data4[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

col_fun = colorRamp2(c(-0.8,0,0.8), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-0.8,0,0.8),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht5<-Heatmap(data5[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)


ht6<-Heatmap(data6[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

ht_list<-ht1+ht3

pdf('heatmap_H2AZ_CLASS.pdf',height = 10,width = 25)
draw(ht_list,heatmap_legend_side = "top",ht_gap = unit(0.4, "cm"))
dev.off()

#NSD3/H2AZ CLASS HEATMAP----
brewer.pal.info
display.brewer.pal(9,"RdBu")
coul <- colorRampPalette(brewer.pal(9, "RdBu"))(9)
coul
col_fun = colorRamp2(c(-1,0,1), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-1,0,1),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

table(Y_H2AZ$cat)
table(Y_NSD3$cat)

gene_class <- list(NSD3_H2AZ_co_enrich = gom.obj@go.nested.list[["H2AZ_enrich"]][["NSD3_enrich"]]@intersection,
                   NSD3_only = gom.obj@go.nested.list[["H2AZ_non_enrich"]][["NSD3_enrich"]]@intersection,
                   H2AZ_only = gom.obj@go.nested.list[["H2AZ_enrich"]][["NSD3_non_enrich"]]@intersection,
                   NSD3_H2AZ_non_enrich = gom.obj@go.nested.list[["H2AZ_non_enrich"]][["NSD3_non_enrich"]]@intersection)

Y <- cbind(Y_H2AZ[row.names(Y_H2AZ),c('H2AZ','cat')],Y_NSD3[row.names(Y_H2AZ),c('NSD3','cat')])

colnames(Y) <- c('H2AZ','cat1','NSD3','cat2')

Y$Gene_class <- NA
Y[gene_class[[1]],]$Gene_class <- c("NSD3_H2AZ_co_enrich")
Y[gene_class[[2]],]$Gene_class <- c("NSD3_only")
Y[gene_class[[3]],]$Gene_class <- c("H2AZ_only")
Y[gene_class[[4]],]$Gene_class <- c("NSD3_H2AZ_non_enrich")
Y$Gene_class <- factor(Y$Gene_class,levels = c("NSD3_H2AZ_co_enrich","NSD3_only",
                                               "H2AZ_only","NSD3_H2AZ_non_enrich"))
Y <- Y[order(Y$Gene_class),]
table(Y$Gene_class)
r_order <- rownames(Y)

r_split <- factor(rep(c('A','B','C','D'),
                      c(6131,944,5489,9180)),
                  levels = c('A','B','C','D'))

table(r_split)

ht1<-Heatmap(data[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE)


ht2<-Heatmap(data2[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

col_fun = colorRamp2(c(-0.5,0,0.5), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-0.5,0,0.5),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht3<-Heatmap(data3[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)


ht4<-Heatmap(data4[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

col_fun = colorRamp2(c(-0.8,0,0.8), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-0.8,0,0.8),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht5<-Heatmap(data5[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)


ht6<-Heatmap(data6[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

ht_list<-ht3+ht1

pdf('heatmap_NSD3_H2AZ_CLASS.pdf',height = 10,width = 25)
draw(ht_list,heatmap_legend_side = "top",ht_gap = unit(0.4, "cm"))
dev.off()

#H3K36me2 CLASS HEATMAP----
brewer.pal.info
display.brewer.pal(9,"RdBu")
coul <- colorRampPalette(brewer.pal(9, "RdBu"))(9)
coul
col_fun = colorRamp2(c(-1,0,1), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-1,0,1),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

table(Y_H3K36me2$cat)

r_order <- Y_H3K36me2$gene

r_split <- factor(rep(c('A','B','C','D'),c(5004,6021,7560,3159)))
table(r_split)

ht1<-Heatmap(data[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE)


ht2<-Heatmap(data2[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

col_fun = colorRamp2(c(-0.5,0,0.5), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-0.5,0,0.5),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht3<-Heatmap(data3[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)


ht4<-Heatmap(data4[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

col_fun = colorRamp2(c(-0.8,0,0.8), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-0.8,0,0.8),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht5<-Heatmap(data5[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)


ht6<-Heatmap(data6[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

ht_list<-ht3+ht4+ht1+ht2+ht5+ht6

pdf('heatmap_H3K36me2_CLASS.pdf',height = 10,width = 25)
draw(ht_list,heatmap_legend_side = "top",ht_gap = unit(0.4, "cm"))
dev.off()

#RNA-seq genes----
ngs_all_gene_id <- rownames(enrichList[[1]])
RNA_symbol_list <- readRDS("F:/UMProject/NSD3/mouse_PanNET/Outs/CnR/ngsplot/RNA_symbol_list.rds")

ngs_all_gene_info <- data.frame(gene_id = ngs_all_gene_id)
ngs_all_gene_info$symbol <- str_split_fixed(ngs_all_gene_info$gene_id,pattern = ':',2)[,1]

names(RNA_symbol_list)
ngs_all_gene_info$Cat <- 'ngs_gene'

for (i in 1:length(RNA_symbol_list[['all_gene']])) {
  ngs_all_gene_info[which(ngs_all_gene_info$symbol == RNA_symbol_list[['all_gene']][i]),'Cat'] <- 'RNA_gene'
}

for (i in 1:length(RNA_symbol_list[['dDEG']])) {
  ngs_all_gene_info[which(ngs_all_gene_info$symbol == RNA_symbol_list[['dDEG']][i]),'Cat'] <- 'dDEG'
}


for (i in 1:length(RNA_symbol_list[['uDEG']])) {
  ngs_all_gene_info[which(ngs_all_gene_info$symbol == RNA_symbol_list[['uDEG']][i]),'Cat'] <- 'uDEG'
}

names(table(ngs_all_gene_info$Cat))
table(ngs_all_gene_info$Cat)

gene_class <- list()
for (i in 1:length(names(table(ngs_all_gene_info$Cat)))) {
  gene_class[[i]] <- ngs_all_gene_info[ngs_all_gene_info$Cat == names(table(ngs_all_gene_info$Cat))[i],]$gene_id
  names(gene_class)[i] <- names(table(ngs_all_gene_info$Cat))[i]
}

gene_class[['RNA_gene_uDEG']] <- c(gene_class[['RNA_gene']],gene_class[['uDEG']])

gene_class[['RNA_gene_ngs_gene']] <- c(gene_class[['RNA_gene']],gene_class[['ngs_gene']])

gene_class[['all_gene']] <- c(gene_class[['RNA_gene']],gene_class[['ngs_gene']],gene_class[['uDEG']])

#genes to plot----
enrich_class <- c('RNA_gene_uDEG')

enrich_class <- c('dDEG')

enrich_class <- c('RNA_gene')

enrich_class <- c('uDEG')


gene_class[[enrich_class]]

#H2AZ norm average ----
IgG_sample <- c("sgControl_IgG","sgControl_IgG",
                "sgControl_IgG","sgControl_IgG")

{
  sample <- c('sgControl','sgNSD3')
  H2AZ_sample <- c("sgControl_H2AZ_R1","sgControl_H2AZ_R2",
                   "sgNSD3_H2AZ_R1","sgNSD3_H2AZ_R2")
  
  H2AZ_mat <- list()
  
  
  H2AZ_mat[[1]] <- log2(((enrichList[[H2AZ_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[H2AZ_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2)
  H2AZ_mat[[2]] <- log2(((enrichList[[H2AZ_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[H2AZ_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2)
  names(H2AZ_mat) <- sample 
  
  H2AZ_avg <- matrix(ncol = 2,nrow = 101)
  colnames(H2AZ_avg) <- sample
  rownames(H2AZ_avg) <- c(1:101)
  for (i in 1:length(sample)) {
    H2AZ_avg[,i] <- colMeans(H2AZ_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-H2AZ_avg
  
  for (i in 1:ncol(H2AZ_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(H2AZ_avg[,i]),1e-50,method = 'mean')
  }
  
  H2AZ_avg <- avg_smooth
  
  H2AZ_data<-list()
  for (i in 1:ncol(H2AZ_avg)) {
    H2AZ_data[[i]]<-data.frame(x=c(1:101),y=H2AZ_avg[,i])
    H2AZ_data[[i]]$group<-colnames(H2AZ_avg)[i]
  }
  
  data <- H2AZ_data[[1]]
  for (i in 2:length(H2AZ_data)) {
    data <- rbind(data,H2AZ_data[[i]])
  }
  
  data$group <- factor(data$group,levels = sample)
  max(data$y)
  min(data$y)
  
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(H2AZ_data)
  color <-getPalette(colourCount_1)
  color <- c("#006838", "#662D91")
  all_p1<-ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(-0.05,0.6,breaks = seq(-0.05,0.6,0.2)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = 'H2AZ',y = "CPM")+
    theme(plot.title = element_text(size = 15, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          legend.position = c(0.7,1),
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
  
  
  #NSD3 norm average ----
  sample <- c('sgControl','sgNSD3')
  NSD3_sample <- c("sgControl_NSD3_R1","sgControl_NSD3_R2",
                   "sgNSD3_NSD3_R1","sgNSD3_NSD3_R2")
  NSD3_mat <- list()
  
  NSD3_mat[[1]] <- log2(((enrichList[[NSD3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[NSD3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2)
  NSD3_mat[[2]] <- log2(((enrichList[[NSD3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[NSD3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2)
  names(NSD3_mat) <- sample
  
  
  NSD3_avg <- matrix(ncol = 2,nrow = 101)
  colnames(NSD3_avg) <- sample
  rownames(NSD3_avg) <- c(1:101)
  for (i in 1:length(sample)) {
    NSD3_avg[,i] <- colMeans(NSD3_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-NSD3_avg
  
  for (i in 1:ncol(NSD3_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(NSD3_avg[,i]),1e-50,method = 'mean')
  }
  
  NSD3_avg <- avg_smooth
  
  NSD3_data<-list()
  for (i in 1:ncol(NSD3_avg)) {
    NSD3_data[[i]]<-data.frame(x=c(1:101),y=NSD3_avg[,i])
    NSD3_data[[i]]$group<-colnames(NSD3_avg)[i]
  }
  
  data <- NSD3_data[[1]]
  for (i in 2:length(NSD3_data)) {
    data <- rbind(data,NSD3_data[[i]])
  }
  
  data$group <- factor(data$group,levels = sample)
  max(data$y)
  min(data$y)
  
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(NSD3_data)
  color <-getPalette(colourCount_1)
  color <- c("#006838", "#662D91")
  all_p2<-ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(-0.02,0.25,breaks = seq(-0.02,0.25,0.05)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = 'NSD3',y = "CPM")+
    theme(plot.title = element_text(size = 15, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          legend.position = c(0.7,1),
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
  
  
  
  #H3K36me2 norm average ----
  sample <- c('sgControl','sgNSD3')
  H3K36me2_sample <- c("sgControl_H3K36me2_R1","sgControl_H3K36me2_R2",
                       "sgNSD3_H3K36me2_R1","sgNSD3_H3K36me2_R2")
  H3K36me2_mat <- list()
  
  H3K36me2_mat[[1]] <- log2(((enrichList[[H3K36me2_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[H3K36me2_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2)
  H3K36me2_mat[[2]] <- log2(((enrichList[[H3K36me2_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[H3K36me2_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2)
  names(H3K36me2_mat) <- sample  
  
  H3K36me2_avg <- matrix(ncol = 2,nrow = 101)
  colnames(H3K36me2_avg) <- sample
  rownames(H3K36me2_avg) <- c(1:101)
  for (i in 1:length(sample)) {
    H3K36me2_avg[,i] <- colMeans(H3K36me2_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-H3K36me2_avg
  
  for (i in 1:ncol(H3K36me2_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(H3K36me2_avg[,i]),1E-50,method = 'mean')
  }
  
  H3K36me2_avg <- avg_smooth
  
  H3K36me2_data<-list()
  for (i in 1:ncol(H3K36me2_avg)) {
    H3K36me2_data[[i]]<-data.frame(x=c(1:101),y=H3K36me2_avg[,i])
    H3K36me2_data[[i]]$group<-colnames(H3K36me2_avg)[i]
  }
  
  data <- H3K36me2_data[[1]]
  for (i in 2:length(H3K36me2_data)) {
    data <- rbind(data,H3K36me2_data[[i]])
  }
  
  data$group <- factor(data$group,levels = sample)
  max(data$y)
  min(data$y)
  
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(H3K36me2_data)
  color <-getPalette(colourCount_1)
  color <- c("#006838", "#662D91")
  all_p3<-ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(-0.1,0.18,breaks = seq(-0.1,0.18,0.01)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = 'H3K36me2',y = "CPM")+
    theme(plot.title = element_text(size = 15, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          legend.position = c(0.7,1),
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
  
  
  
  #H3K27me3 norm average ----
  sample <- c('sgControl','sgNSD3')
  H3K27me3_sample <- c("sgControl_H3K27me3_R1","sgControl_H3K27me3_R2",
                       "sgNSD3_H3K27me3_R1","sgNSD3_H3K27me3_R2")
  H3K27me3_mat <- list()
  
  
  H3K27me3_mat[[1]] <- log2(((enrichList[[H3K27me3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[H3K27me3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2)
  H3K27me3_mat[[2]] <- log2(((enrichList[[H3K27me3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[H3K27me3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2)
  names(H3K27me3_mat) <- sample 
  
  H3K27me3_avg <- matrix(ncol = 2,nrow = 101)
  colnames(H3K27me3_avg) <- sample
  rownames(H3K27me3_avg) <- c(1:101)
  for (i in 1:length(sample)) {
    H3K27me3_avg[,i] <- colMeans(H3K27me3_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-H3K27me3_avg
  
  for (i in 1:ncol(H3K27me3_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(H3K27me3_avg[,i]),1e-50,method = 'mean')
  }
  
  H3K27me3_avg <- avg_smooth
  
  H3K27me3_data<-list()
  for (i in 1:ncol(H3K27me3_avg)) {
    H3K27me3_data[[i]]<-data.frame(x=c(1:101),y=H3K27me3_avg[,i])
    H3K27me3_data[[i]]$group<-colnames(H3K27me3_avg)[i]
  }
  
  data <- H3K27me3_data[[1]]
  for (i in 2:length(H3K27me3_data)) {
    data <- rbind(data,H3K27me3_data[[i]])
  }
  
  data$group <- factor(data$group,levels = sample)
  max(data$y)
  min(data$y)
  
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(H3K27me3_data)
  color <-getPalette(colourCount_1)
  color <- c("#006838", "#662D91")
  all_p4<-ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(-0.15,0.08,breaks = seq(-0.15,0.08,0.1)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = 'H3K27me3',y = "CPM")+
    theme(plot.title = element_text(size = 15, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          legend.position = c(0.7,1),
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
  
  
  
  #H3K4me3 norm average ----
  sample <- c('sgControl','sgNSD3')
  H3K4me3_sample <- c("sgControl_H3K4me3_R1","sgControl_H3K4me3_R2",
                      "sgNSD3_H3K4me3_R1","sgNSD3_H3K4me3_R2")
  H3K4me3_mat <- list()
  
  H3K4me3_mat[[1]] <- log2(((enrichList[[H3K4me3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[H3K4me3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2)
  H3K4me3_mat[[2]] <- log2(((enrichList[[H3K4me3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[H3K4me3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2)
  names(H3K4me3_mat) <- sample 
  
  H3K4me3_avg <- matrix(ncol = 2,nrow = 101)
  colnames(H3K4me3_avg) <- sample
  rownames(H3K4me3_avg) <- c(1:101)
  for (i in 1:length(sample)) {
    H3K4me3_avg[,i] <- colMeans(H3K4me3_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-H3K4me3_avg
  
  for (i in 1:ncol(H3K4me3_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(H3K4me3_avg[,i]),1e-50,method = 'mean')
  }
  
  H3K4me3_avg <- avg_smooth
  
  H3K4me3_data<-list()
  for (i in 1:ncol(H3K4me3_avg)) {
    H3K4me3_data[[i]]<-data.frame(x=c(1:101),y=H3K4me3_avg[,i])
    H3K4me3_data[[i]]$group<-colnames(H3K4me3_avg)[i]
  }
  
  data <- H3K4me3_data[[1]]
  for (i in 2:length(H3K4me3_data)) {
    data <- rbind(data,H3K4me3_data[[i]])
  }
  
  data$group <- factor(data$group,levels = sample)
  max(data$y)
  min(data$y)
  
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(H3K4me3_data)
  color <-getPalette(colourCount_1)
  color <- c("#006838", "#662D91")
  all_p5<-ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(-0.05,1,breaks = seq(-0.05,1,0.1)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = 'H3K4me3',y = "CPM")+
    theme(plot.title = element_text(size = 15, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          legend.position = c(0.7,1),
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
  
  
  
  #H3K36me3 norm average ----
  sample <- c('sgControl','sgNSD3')
  H3K36me3_sample <- c("sgControl_H3K36me3_R2","sgControl_H3K36me3_R2",
                       "sgNSD3_H3K36me3_R1","sgNSD3_H3K36me3_R1")
  H3K36me3_mat <- list()
  
  H3K36me3_mat[[1]] <- log2(((enrichList[[H3K36me3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[H3K36me3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2)
  H3K36me3_mat[[2]] <- log2(((enrichList[[H3K36me3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[H3K36me3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2)
  names(H3K4me3_mat) <- sample 
  
  
  H3K36me3_avg <- matrix(ncol = 2,nrow = 101)
  colnames(H3K36me3_avg) <- sample
  rownames(H3K36me3_avg) <- c(1:101)
  for (i in 1:length(sample)) {
    H3K36me3_avg[,i] <- colMeans(H3K36me3_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-H3K36me3_avg
  
  for (i in 1:ncol(H3K36me3_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(H3K36me3_avg[,i]),1e-50,method = 'mean')
  }
  
  H3K36me3_avg <- avg_smooth
  
  H3K36me3_data<-list()
  for (i in 1:ncol(H3K36me3_avg)) {
    H3K36me3_data[[i]]<-data.frame(x=c(1:101),y=H3K36me3_avg[,i])
    H3K36me3_data[[i]]$group<-colnames(H3K36me3_avg)[i]
  }
  
  data <- H3K36me3_data[[1]]
  for (i in 2:length(H3K36me3_data)) {
    data <- rbind(data,H3K36me3_data[[i]])
  }
  
  data$group <- factor(data$group,levels = sample)
  max(data$y)
  min(data$y)
  
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(H3K36me3_data)
  color <-getPalette(colourCount_1)
  color <- c("#006838", "#662D91")
  
  all_p6<-ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(-0.1,0.18,breaks = seq(-0.1,0.18,0.1)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = 'H3K36me3',y = "CPM")+
    theme(plot.title = element_text(size = 15, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          legend.position = c(0.7,1),
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
  
(all_p1|all_p2|all_p3)/(all_p4|all_p5|all_p6)
}


dev.off()
pdf(paste('00.Average_ADJUST_',gene_cat,"_",enrich_class,'_ALL.pdf',sep = ""),width = 11,height = 6)
(all_p1|all_p2|all_p3)/(all_p4|all_p5|all_p6)
dev.off()

