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
gene_cat <- c('allgene_500bp')
norm_cat <- c('CPM')
ylab <- c('Log2FC')


bam_cat <- c('rmdup') #sorted , dedup , dedup.120bps

#setwd(paste("F:/UMProject/mouse_PanNET/Outs/CutRun/03.ngsplot/Norm_by_",norm_cat,'/',bam_cat,'/',gene_cat,'/',sep = ""))
setwd(paste("F:/UMProject/mouse_PanNET/Outs/CnR/ngsplot/Norm_by_",norm_cat,'/',bam_cat,'/',gene_cat,'/',sep = ""))
getwd()
load("heatmap.RData")
sample_id <- ctg.tbl$title

length((rownames(enrichList[[1]]) == rownames(enrichList[[2]])) == TRUE)

for (i in 1:length(sample_id)) {
  names(enrichList)[i] <- sample_id[i]
}

sample <- c('sgControl','sgNSD3')
IgG_sample <- c("sgControl_IgG_R2","sgControl_IgG_R2",
                "sgControl_IgG_R2","sgControl_IgG_R2")

#H2AZ norm heatmap ----
sample <- c('sgControl','sgNSD3')
H2AZ_sample <- c("sgControl_H2AZ_R1","sgControl_H2AZ_R2",
                 "sgNSD3_H2AZ_R1","sgNSD3_H2AZ_R2")
H2AZ_mat <- list()

H2AZ_mat[[1]] <- (log2((enrichList[[H2AZ_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1)) + log2((enrichList[[H2AZ_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1)))/2
H2AZ_mat[[2]] <- (log2((enrichList[[H2AZ_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1)) + log2((enrichList[[H2AZ_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1)))/2
names(H2AZ_mat) <- sample 

#NSD3 norm heatmap ----
sample <- c('sgControl','sgNSD3')
NSD3_sample <- c("sgControl_NSD3_R1","sgControl_NSD3_R2",
                 "sgNSD3_NSD3_R1","sgNSD3_NSD3_R2")
NSD3_mat <- list()

NSD3_mat[[1]] <- (log2((enrichList[[NSD3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1)) + log2((enrichList[[NSD3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1)))/2
NSD3_mat[[2]] <- (log2((enrichList[[NSD3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1)) + log2((enrichList[[NSD3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1)))/2
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

H3K36me3_mat[[1]] <- (log2((enrichList[[H3K36me3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1)) + log2((enrichList[[H3K36me3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1)))/2
H3K36me3_mat[[2]] <- (log2((enrichList[[H3K36me3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1)) + log2((enrichList[[H3K36me3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1)))/2
names(H3K36me3_mat) <- sample  

data <-H2AZ_mat[[1]]
data2<-H2AZ_mat[[2]]
data3<-NSD3_mat[[1]]
data4<-NSD3_mat[[2]]
data5<-H3K36me2_mat[[1]]
data6<-H3K36me2_mat[[2]]
data7<-H3K36me3_mat[[1]]
data8<-H3K36me3_mat[[2]]

#H2AZ cat heirachical----
Y_H2AZ<-data.frame()
for (i in 1:nrow(data)) {
  Y_H2AZ[i,1]<-mean(data[i,20:22])
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

Y_H2AZ$hc <- factor(Y_H2AZ$hc,levels = c(2,3,1,4))

Y_H2AZ$cat <- NA
Y_H2AZ[Y_H2AZ$hc == 2,'cat'] <- 'A'
Y_H2AZ[Y_H2AZ$hc == 3,'cat'] <- 'B'
Y_H2AZ[Y_H2AZ$hc == 1,'cat'] <- 'C'
Y_H2AZ[Y_H2AZ$hc == 4,'cat'] <- 'D'

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
  Y_NSD3[i,1]<-mean(data3[i,20:22])
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

Y_NSD3$hc <- factor(Y_NSD3$hc,levels = c(3,1,2,4))

Y_NSD3$cat <- NA
Y_NSD3[Y_NSD3$hc == 3,'cat'] <- 'A'
Y_NSD3[Y_NSD3$hc == 1,'cat'] <- 'B'
Y_NSD3[Y_NSD3$hc == 2,'cat'] <- 'C'
Y_NSD3[Y_NSD3$hc == 4,'cat'] <- 'D'

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


venn::venn(venn_list,zcolor = "style",plotsize = 20, 
           ggplot = TRUE, borders = TRUE,box = FALSE)

#H3K36me2 CAT HIERACHICAL----
Y_H3K36me2<-data.frame()
for (i in 1:nrow(data3)) {
  Y_H3K36me2[i,1]<-mean(data5[i,20:22])
}

colnames(Y_H3K36me2) <- c('H3K36me2')
row.names(Y_H3K36me2)<-row.names(data5)

hc=hclust(dist(Y_H3K36me2,method = 'euclidean'),method = 'ward.D')

fviz_nbclust(Y_H3K36me2, FUN = hcut, method = "wss",k.max = 10)
fviz_nbclust(Y_H3K36me2, FUN = hcut, method = "silhouette",k.max = 10)


plot(hc,label=FALSE)
rect.hclust(hc, k=4, border="red")

hc_result=cutree(hc,k = 4)

Y_H3K36me2$gene <- rownames(Y_H3K36me2)
Y_H3K36me2$hc <- hc_result
table(Y_H3K36me2$hc)

mean(Y_H3K36me2[Y_H3K36me2$hc==1,]$H3K36me2)
mean(Y_H3K36me2[Y_H3K36me2$hc==2,]$H3K36me2)
mean(Y_H3K36me2[Y_H3K36me2$hc==3,]$H3K36me2)
mean(Y_H3K36me2[Y_H3K36me2$hc==4,]$H3K36me2)

Y_H3K36me2$hc <- factor(Y_H3K36me2$hc,levels = c(2,4,3,1))

Y_H3K36me2$cat <- NA
Y_H3K36me2[Y_H3K36me2$hc == 2,'cat'] <- 'A'
Y_H3K36me2[Y_H3K36me2$hc == 4,'cat'] <- 'B'
Y_H3K36me2[Y_H3K36me2$hc == 3,'cat'] <- 'C'
Y_H3K36me2[Y_H3K36me2$hc == 1,'cat'] <- 'D'

Y_H3K36me2 <- Y_H3K36me2[order(Y_H3K36me2$cat),]
r_order <- Y_H3K36me2$gene

H3K36me2_class_A <- Y_H3K36me2[Y_H3K36me2$cat == 'A','gene']
H3K36me2_class_B <- Y_H3K36me2[Y_H3K36me2$cat == 'B','gene']
H3K36me2_class_C <- Y_H3K36me2[Y_H3K36me2$cat == 'C','gene']
H3K36me2_class_D <- Y_H3K36me2[Y_H3K36me2$cat == 'D','gene']

H3K36me2_class_list <- list(H3K36me2_class_A=H3K36me2_class_A,
                            H3K36me2_class_B=H3K36me2_class_B,
                            H3K36me2_class_C=H3K36me2_class_C,
                            H3K36me2_class_D=H3K36me2_class_D)


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
r_split <- factor(rep(c('A','B','C','D'),c(3480,3595,12390,2279)))
r_split <- factor(rep(c('A','B'),c(7075,14669)))
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

r_split <- factor(rep(c('A','B','C','D'),c(1611,4022,5987,10124)))
r_split <- factor(rep(c('A','B'),c(11620,10124)))
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



#H2AZ norm average ----
table(y$cat)
table(r_split)

H2AZ_list[[3]] <- c(H2AZ_list[[1]],H2AZ_list[[2]])
names(H2AZ_list)[3] <- 'All_gene'
gene_class <- H2AZ_list
enrich_class <- c('All_gene')
gene_class[[enrich_class]]


{
  sample <- c('sgControl','sgNSD3')
  H2AZ_sample <- c("sgControl_H2AZ_R1","sgControl_H2AZ_R2",
                   "sgNSD3_H2AZ_R1","sgNSD3_H2AZ_R2")
  H2AZ_mat <- list()
  
  
  H2AZ_mat[[1]] <- (log2((enrichList[[H2AZ_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1)) + log2((enrichList[[H2AZ_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1)))/2
  H2AZ_mat[[2]] <- (log2((enrichList[[H2AZ_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1)) + log2((enrichList[[H2AZ_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1)))/2
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
    scale_y_continuous(name = ylab,limits = c(-0.05,0.8,breaks = seq(-0.05,0.8,0.2)))+
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
  
  all_p1
  
  #NSD3 norm average ----
  sample <- c('sgControl','sgNSD3')
  NSD3_sample <- c("sgControl_NSD3_R1","sgControl_NSD3_R2",
                   "sgNSD3_NSD3_R1","sgNSD3_NSD3_R2")
  NSD3_mat <- list()
  
  NSD3_mat[[1]] <- (log2((enrichList[[NSD3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1)) + log2((enrichList[[NSD3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1)))/2
  NSD3_mat[[2]] <- (log2((enrichList[[NSD3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1)) + log2((enrichList[[NSD3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1)))/2
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
    scale_y_continuous(name = ylab,limits = c(-0.05,0.2,breaks = seq(-0.05,0.2,0.1)))+
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
  
  all_p2
  
  
  #H3K36me2 norm average ----
  sample <- c('sgControl','sgNSD3')
  H3K36me2_sample <- c("sgControl_H3K36me2_R1","sgControl_H3K36me2_R2",
                       "sgNSD3_H3K36me2_R1","sgNSD3_H3K36me2_R2")
  H3K36me2_mat <- list()
  
  
  H3K36me2_mat[[1]] <- ((enrichList[[H3K36me2_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1) + (enrichList[[H3K36me2_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1))/2
  H3K36me2_mat[[2]] <- ((enrichList[[H3K36me2_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1) + (enrichList[[H3K36me2_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1))/2
  names(H3K36me2_mat) <- sample  
  
  H3K36me2_avg <- matrix(ncol = 2,nrow = 101)
  colnames(H3K36me2_avg) <- sample
  rownames(H3K36me2_avg) <- c(1:101)
  for (i in 1:length(sample)) {
    H3K36me2_avg[,i] <- colMeans(H3K36me2_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-H3K36me2_avg
  
  for (i in 1:ncol(H3K36me2_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(H3K36me2_avg[,i]),1e-50,method = 'mean')
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
    scale_y_continuous(name = ylab,limits = c(1,1.3,breaks = seq(1,1.3,0.1)))+
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
  
  all_p3
  
  
  #H3K27me3 norm average ----
  sample <- c('sgControl','sgNSD3')
  H3K27me3_sample <- c("sgControl_H3K27me3_R1","sgControl_H3K27me3_R2",
                       "sgNSD3_H3K27me3_R1","sgNSD3_H3K27me3_R2")
  H3K27me3_mat <- list()
  
  
  H3K27me3_mat[[1]] <- (log2((enrichList[[H3K27me3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1)) + log2((enrichList[[H3K27me3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1)))/2
  H3K27me3_mat[[2]] <- (log2((enrichList[[H3K27me3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1)) + log2((enrichList[[H3K27me3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1)))/2
  names(H3K27me3_mat) <- sample 
  
  H3K27me3_avg <- matrix(ncol = 2,nrow = 101)
  colnames(H3K27me3_avg) <- sample
  rownames(H3K27me3_avg) <- c(1:101)
  for (i in 1:length(sample)) {
    H3K27me3_avg[,i] <- colMeans(H3K27me3_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-H3K27me3_avg
  
  for (i in 1:ncol(H3K27me3_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(H3K27me3_avg[,i]),1e-2,method = 'mean')
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
    scale_y_continuous(name = ylab,limits = c(-0.05,0.1,breaks = seq(-0.05,0.1,0.05)))+
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
  
  all_p4
  
  
  #H3K4me3 norm average ----
  sample <- c('sgControl','sgNSD3')
  H3K4me3_sample <- c("sgControl_H3K4me3_R1","sgControl_H3K4me3_R2",
                      "sgNSD3_H3K4me3_R1","sgNSD3_H3K4me3_R2")
  H3K4me3_mat <- list()
  
  H3K4me3_mat[[1]] <- (log2((enrichList[[H3K4me3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1)) + log2((enrichList[[H3K4me3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1)))/2
  H3K4me3_mat[[2]] <- (log2((enrichList[[H3K4me3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1)) + log2((enrichList[[H3K4me3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1)))/2
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
    scale_y_continuous(name = ylab,limits = c(0,2.6,breaks = seq(0,2.6,0.1)))+
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
  
  all_p5
  
  
  #H3K36me3 norm average ----
  sample <- c('sgControl','sgNSD3')
  H3K36me3_sample <- c("sgControl_H3K36me3_R2","sgControl_H3K36me3_R2",
                       "sgNSD3_H3K36me3_R1","sgNSD3_H3K36me3_R1")
  H3K36me3_mat <- list()
  
  H3K36me3_mat[[1]] <- (log2((enrichList[[H3K36me3_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1)) + log2((enrichList[[H3K36me3_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1)))/2
  H3K36me3_mat[[2]] <- (log2((enrichList[[H3K36me3_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1)) + log2((enrichList[[H3K36me3_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1)))/2
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
    scale_y_continuous(name = ylab,limits = c(0.1,0.45,breaks = seq(0.1,0.45,0.1)))+
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
  
  all_p6
}

dev.off()
pdf(paste('00.Average_ADJUST_',gene_cat,"_",enrich_class,'_ALL.pdf',sep = ""),width = 11,height = 6)
(all_p1|all_p2|all_p3)/(all_p4|all_p5|all_p6)
dev.off()

#DEG----
library(tidyverse)
library(readr)

DESeq2_DEG <- read_csv("F:/UMProject/mouse_PanNET/Outs/RNAseq2/05.DESeq2/DESeq2_DEG.csv")

class_1_gene <- str_split_fixed(class_A,pattern = ':',n = 2)[,1]
class_2_gene <- str_split_fixed(class_B,pattern = ':',n = 2)[,1]
class_3_gene <- str_split_fixed(class_C,pattern = ':',n = 2)[,1]
class_4_gene <- str_split_fixed(class_D,pattern = ':',n = 2)[,1]
class_5_gene <- str_split_fixed(class_E,pattern = ':',n = 2)[,1]

DESeq2_DEG <- data.frame(DESeq2_DEG)
rownames(DESeq2_DEG) <- DESeq2_DEG$gene

DESeq2_DEG$group <- NA
DESeq2_DEG[class_1_gene,'group'] <- 'A'
DESeq2_DEG[class_2_gene,'group'] <- 'B'
DESeq2_DEG[class_3_gene,'group'] <- 'C'
DESeq2_DEG[class_4_gene,'group'] <- 'D'
DESeq2_DEG[class_5_gene,'group'] <- 'E'


dDEG <- subset(DESeq2_DEG,DESeq2_DEG$change=='DOWN')
dDEG <- na.omit(dDEG)
dDEG$group <- factor(dDEG$group,levels = c('A','B'))

mean(dDEG[dDEG$group=='A',]$log2FoldChange)
mean(dDEG[dDEG$group=='B',]$log2FoldChange)
mean(dDEG[dDEG$group=='C',]$log2FoldChange)
mean(dDEG[dDEG$group=='D',]$log2FoldChange)
mean(dDEG[dDEG$group=='E',]$log2FoldChange)

median(dDEG[dDEG$group=='A',]$padj)
median(dDEG[dDEG$group=='B',]$padj)
median(dDEG[dDEG$group=='C',]$padj)
median(dDEG[dDEG$group=='D',]$padj)
median(dDEG[dDEG$group=='E',]$padj)

ggplot(dDEG, aes(x=group, y=abs(log2FoldChange), color=group)) +
  geom_boxplot(width=0.1,outlier.size=0) + theme_minimal()+
  stat_compare_means(method = "anova", label.y = 5)+ 
  stat_compare_means(comparisons = list( c("A", "B"), c("A", "C")),
                     label.y = c(2, 3, 4)) + ylim(0,6)

ggplot(dDEG, aes(x=group, y=-log10(padj), color=group)) + 
  geom_boxplot(width=0.1,outlier.size=0) + theme_minimal() +
  stat_compare_means(method = "anova", label.y = 50)+ 
  stat_compare_means(comparisons = list( c("A", "B"), c("A", "C")),
                     label.y = c(10, 20, 30)) + ylim(0,50)

write.table(dDEG[dDEG$group=='A',]$gene,file = 'class_A.genes.txt',row.names = FALSE,col.names = FALSE,quote = FALSE,sep = "\t")
write.table(dDEG[dDEG$group=='B',]$gene,file = 'class_B.genes.txt',row.names = FALSE,col.names = FALSE,quote = FALSE,sep = "\t")
write.table(dDEG[dDEG$group=='C',]$gene,file = 'class_C.genes.txt',row.names = FALSE,col.names = FALSE,quote = FALSE,sep = "\t")
write.table(dDEG[dDEG$group=='D',]$gene,file = 'class_D.genes.txt',row.names = FALSE,col.names = FALSE,quote = FALSE,sep = "\t")
write.table(dDEG[dDEG$group=='E',]$gene,file = 'class_E.genes.txt',row.names = FALSE,col.names = FALSE,quote = FALSE,sep = "\t")

#Function enrichment----

library(msigdbr)
library(clusterProfiler)

gene <- dDEG[dDEG$group=='A',]$gene
gene <- dDEG[dDEG$group=='B',]$gene

###5.3.1GO term----
# M24260 GOBP_NEUROENDOCRINE_CELL_DIFFERENTIATION
m_df<- msigdbr(species = "Mus musculus", subcategory = 'GO:BP')
m_df$gs_id_name <- paste(m_df$gs_id,'-',m_df$gs_name,sep = "")

GOBP_sets<- m_df %>% split(x = .$gene_symbol, f = .$gs_id_name)
GOBP_sets<- m_df [,c('gs_id_name','gene_symbol')]

enrich_GOBP <- enricher(gene, TERM2GENE=GOBP_sets)


###5.3.2 CGP chemical and genetic perturbations pathways term ----
# M1862(ZHOU_PANCREATIC_ENDOCRINE_PROGENITOR) 
# M1860 (ZHOU_PANCREATIC_EXOCRINE_PROGENITOR)
m_df<- msigdbr(species = "Mus musculus", subcategory = 'CGP')
m_df$gs_id_name <- paste(m_df$gs_id,'-',m_df$gs_name,sep = "")

CGP_sets<- m_df %>% split(x = .$gene_symbol, f = .$gs_id_name)
CGP_sets<- m_df [,c('gs_id_name','gene_symbol')]

enrich_CGP <- enricher(gene, TERM2GENE=CGP_sets)

###5.3.2 hpo Human Phenotype Ontology pathways term ----
# M38778(HP_NEUROENDOCRINE_NEOPLASM) 
# M38373(HP_PANCREATIC_ENDOCRINE_TUMOR)
m_df <- msigdbr(species = "Mus musculus", subcategory = 'HPO')
m_df$gs_id_name <- paste(m_df$gs_id,'-',m_df$gs_name,sep = "")

HPO_sets<- m_df %>% split(x = .$gene_symbol, f = .$gs_id_name)
HPO_sets<- m_df [,c('gs_id_name','gene_symbol')]

enrich_HPO <- enricher(gene, TERM2GENE=HPO_sets)

###5.3.3 C8 pathways term ----
# M41658 TRAVAGLINI_LUNG_NEUROENDOCRINE_CELL
m_df <- msigdbr(species = "Mus musculus", category = 'C8')
m_df$gs_id_name <- paste(m_df$gs_id,'-',m_df$gs_name,sep = "")

C8_sets<- m_df %>% split(x = .$gene_symbol, f = .$gs_id_name)
C8_sets<- m_df [,c('gs_id_name','gene_symbol')]

enrich_C8 <- enricher(gene, TERM2GENE=C8_sets)

###5.3.4 C6 pathways term ----
m_df <- msigdbr(species = "Mus musculus", category = 'C6')
m_df$gs_id_name <- paste(m_df$gs_id,'-',m_df$gs_name,sep = "")

C6_sets<- m_df %>% split(x = .$gene_symbol, f = .$gs_id_name)
C6_sets<- m_df [,c('gs_id_name','gene_symbol')]

enrich_C6 <- enricher(gene, TERM2GENE=C6_sets)

enrich_tab <- rbind(enrich_GOBP@result,enrich_CGP@result,
                    enrich_HPO@result,enrich_C6@result,
                    enrich_C8@result)

candidate_tab <- enrich_tab[grep(pattern = "M12739-|M1862-|M1860-|M38778-|M38373-|M41658-",enrich_tab$Description),]

write.csv(candidate_tab,file = '0.B_candidate_term_enrich.csv')

library(enrichplot)
barplot(enrich_C6, showCategory=10) 
barplot(enrich_C8, showCategory=10) 
barplot(enrich_HPO, showCategory=5) 
barplot(enrich_CGP, showCategory=10) 
barplot(enrich_GOBP, showCategory=10) 


save.image(file = 'HC_dDEG.Rdata')
saveRDS(gene_class,file = 'gene_class.rds')

#raw expr----
load("F:/UMProject/mouse_PanNET/Outs/RNAseq2/05.DESeq2/RNAseq_DESeq2.Rdata")

dDEG_CPM_expr <- CPM_expr[c(class_1_gene,class_2_gene),]

pheatmap(dDEG_CPM_expr,cluster_cols = F,cluster_rows = F)

sgControl_dat <- data.frame(rowMeans(dDEG_CPM_expr[,1:3]))
sgNSD3_dat <- data.frame(rowMeans(dDEG_CPM_expr[,4:6]))

colnames(sgControl_dat) <- 'Expression_level'
colnames(sgNSD3_dat) <- 'Expression_level'

sgControl_dat$cat <- NA
sgControl_dat[class_1_gene,'cat'] <- 'A'
sgControl_dat[class_2_gene,'cat'] <- 'B'

sgControl_dat$group <- 'sgControl'
sgControl_dat$cat <- factor(sgControl_dat$cat,levels = c('A','B'))

sgNSD3_dat$cat <- NA
sgNSD3_dat[class_1_gene,'cat'] <- 'A'
sgNSD3_dat[class_2_gene,'cat'] <- 'B'

sgNSD3_dat$group <- 'sgNSD3'

expr_dat <- rbind(sgControl_dat,sgNSD3_dat)

expr_dat$cat <- factor(expr_dat$cat,levels = c('A','B'))


p1 <- ggviolin(expr_dat, 
               x = "cat", 
               y = "Expression_level", 
               fill = "group",
               palette = c("#006838", "#662D91"),
               add = "mean_sd", error.plot = "crossbar") + 
  scale_x_discrete(breaks = c('A','B'),
                   labels = c('A','B')) + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1))


ggviolin(expr_dat, 
         x = "cat", 
         y = "Expression_level", 
         fill = "group",
         palette = c("#006838", "#662D91"),
         add = "mean_sd", error.plot = "crossbar") + 
  scale_x_discrete(breaks = c('A','B'),
                   labels = c('A','B')) + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1))+ 
  stat_compare_means(method = "anova", label.y = 10)+ 
  stat_compare_means(comparisons = list( c("A", "B")),
                     label.y = c(8))


#H3K36me2 TSS density----

sgControl_H3K36me2_dat <- data.frame(log2(rowMeans(H3K36me2_mat[[1]][,c(20:22)])))
sgNSD3_H3K36me2_dat <- data.frame(log2(rowMeans(H3K36me2_mat[[2]][,c(20,22)])))

colnames(sgControl_H3K36me2_dat) <- 'H3K36me2_normalized_densities'
colnames(sgNSD3_H3K36me2_dat) <- 'H3K36me2_normalized_densities'


sgControl_H3K36me2_dat$cat <- NA
sgControl_H3K36me2_dat[class_A,'cat'] <- 'A'
sgControl_H3K36me2_dat[class_B,'cat'] <- 'B'
sgControl_H3K36me2_dat$group <- 'sgControl'
sgControl_H3K36me2_dat$cat <- factor(sgControl_H3K36me2_dat$cat,levels = c('A','B'))
mean(sgControl_H3K36me2_dat[sgControl_H3K36me2_dat$cat=='A',]$H3K36me2_normalized_densities)
mean(sgControl_H3K36me2_dat[sgControl_H3K36me2_dat$cat=='B',]$H3K36me2_normalized_densities)

sgNSD3_H3K36me2_dat$cat <- NA
sgNSD3_H3K36me2_dat[class_A,'cat'] <- 'A'
sgNSD3_H3K36me2_dat[class_B,'cat'] <- 'B'
sgNSD3_H3K36me2_dat$group <- 'sgNSD3'
sgNSD3_H3K36me2_dat$cat <- factor(sgNSD3_H3K36me2_dat$cat,levels = c('A','B'))

mean(sgNSD3_H3K36me2_dat[sgNSD3_H3K36me2_dat$cat=='A',]$H3K36me2_normalized_densities)
mean(sgNSD3_H3K36me2_dat[sgNSD3_H3K36me2_dat$cat=='B',]$H3K36me2_normalized_densities)

H3K36me2_dat <- rbind(sgControl_H3K36me2_dat,sgNSD3_H3K36me2_dat)

H3K36me2_dat$cat <- factor(H3K36me2_dat$cat,levels = c('A','B'))


p2 <- ggviolin(H3K36me2_dat, 
               x = "cat", 
               y = "H3K36me2_normalized_densities", 
               fill = "group",width = 0.7,
               palette = c("#006838", "#662D91"),
               add = "mean_sd", error.plot = "crossbar",trim = FALSE) +
  scale_x_discrete(breaks = c('A','B'),
                   labels = c('A','B')) + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1)) + 
  scale_y_continuous(limits = c(-0.2,0.7),breaks = seq(-0.2,0.7,0.1))

p2

#H3K27me3 TSS density----

sgControl_H3K27me3_dat <- data.frame(log2(rowMeans(H3K27me3_mat[[1]][,20:22])))
sgNSD3_H3K27me3_dat <- data.frame(log2(rowMeans(H3K27me3_mat[[2]][,20:22])))

colnames(sgControl_H3K27me3_dat) <- 'H3K27me3_normalized_densities'
colnames(sgNSD3_H3K27me3_dat) <- 'H3K27me3_normalized_densities'

sgControl_H3K27me3_dat$cat <- NA
sgControl_H3K27me3_dat[class_A,'cat'] <- 'A'
sgControl_H3K27me3_dat[class_B,'cat'] <- 'B'

sgControl_H3K27me3_dat$group <- 'sgControl'

sgNSD3_H3K27me3_dat$cat <- NA
sgNSD3_H3K27me3_dat[class_A,'cat'] <- 'A'
sgNSD3_H3K27me3_dat[class_B,'cat'] <- 'B'

sgNSD3_H3K27me3_dat$group <- 'sgNSD3'

H3K27me3_dat <- rbind(sgControl_H3K27me3_dat,sgNSD3_H3K27me3_dat)

H3K27me3_dat$cat <- factor(H3K27me3_dat$cat,levels = c('A','B'))


p3 <- ggviolin(H3K27me3_dat, 
               x = "cat", 
               y = "H3K27me3_normalized_densities", 
               fill = "group",width = 0.7,
               palette = c("#006838", "#662D91"),
               add = "mean_sd", error.plot = "crossbar",trim = FALSE) + 
  scale_x_discrete(breaks = c('A','B'),
                   labels = c('A','B')) + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1)) +
  scale_y_continuous(limits = c(-0.2,0.7),breaks = seq(-0.2,0.7,0.1))


p1|p2|p3

#delta expr----
p1 <- ggviolin(dDEG, 
               x = "group", 
               y = "log2FoldChange", 
               fill = "group",
               palette = c("#006838", "#662D91"),
               add = "mean_sd", error.plot = "crossbar") + 
  scale_x_discrete(breaks = c('A','B'),
                   labels = c('A','B')) + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1))


#H3K36me2 TSS density----

sgControl_H3K36me2_dat <- data.frame(log2(rowMeans(H3K36me2_mat[[1]][,c(20:22)])))
sgNSD3_H3K36me2_dat <- data.frame(log2(rowMeans(H3K36me2_mat[[2]][,c(20,22)])))

colnames(sgControl_H3K36me2_dat) <- 'H3K36me2_normalized_densities'
colnames(sgNSD3_H3K36me2_dat) <- 'H3K36me2_normalized_densities'

H3K36me2_dat <- sgControl_H3K36me2_dat-sgNSD3_H3K36me2_dat

H3K36me2_dat$cat <- NA
H3K36me2_dat[class_A,'cat'] <- 'A'
H3K36me2_dat[class_B,'cat'] <- 'B'
H3K36me2_dat$cat <- factor(H3K36me2_dat$cat,levels = c('A','B'))


p2 <- ggviolin(H3K36me2_dat, 
               x = "cat", 
               y = "H3K36me2_normalized_densities", 
               fill = "cat",width = 0.7,
               palette = c("#006838", "#662D91"),
               add = "mean_sd", error.plot = "crossbar",trim = FALSE) +
  scale_x_discrete(breaks = c('A','B'),
                   labels = c('A','B')) + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1)) + 
  scale_y_continuous(name = 'sgControl - sgNSD3',limits = c(-0.5,0.6),breaks = seq(-0.5,0.8,0.2))

p2

#H3K27me3 TSS density----

sgControl_H3K27me3_dat <- data.frame(log2(rowMeans(H3K27me3_mat[[1]][,20:22])))
sgNSD3_H3K27me3_dat <- data.frame(log2(rowMeans(H3K27me3_mat[[2]][,20:22])))

colnames(sgControl_H3K27me3_dat) <- 'H3K27me3_normalized_densities'
colnames(sgNSD3_H3K27me3_dat) <- 'H3K27me3_normalized_densities'

H3K27me3_dat <- sgControl_H3K27me3_dat-sgNSD3_H3K27me3_dat
H3K27me3_dat$cat <- NA
H3K27me3_dat[class_A,'cat'] <- 'A'
H3K27me3_dat[class_B,'cat'] <- 'B'

H3K27me3_dat$cat <- factor(H3K27me3_dat$cat,levels = c('A','B'))



p3 <- ggviolin(H3K27me3_dat, 
               x = "cat", 
               y = "H3K27me3_normalized_densities", 
               fill = "cat",width = 0.7,
               palette = c("#006838", "#662D91"),
               add = "mean_sd", error.plot = "crossbar",trim = FALSE) + 
  scale_x_discrete(breaks = c('A','B'),
                   labels = c('A','B')) + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1)) +
  scale_y_continuous(name = 'sgControl - sgNSD3',limits = c(-0.5,0.5),breaks = seq(-0.5,0.5,0.2))


p1|p2|p3

#LINEAR REGRESSION ----
library(stringr)
data_H2AZ <- H2AZ_mat[[1]]
data_NSD3 <- NSD3_mat[[1]]
data_H3K36me2 <- H3K36me2_mat[[1]]
data_H3K36me3 <- H3K36me3_mat[[1]]

data2_H2AZ <- H2AZ_mat[[2]]
data2_NSD3 <- NSD3_mat[[2]]
data2_H3K36me2 <- H3K36me2_mat[[2]]
data2_H3K36me3 <- H3K36me3_mat[[2]]

load("F:/UMProject/mouse_PanNET/Outs/RNAseq2/05.DESeq2/RNAseq_DESeq2.Rdata")
class_gene <- str_split_fixed(rownames(data_H2AZ),pattern = ':',n = 2)[,1]
dDEG_CPM_expr <- CPM_expr[intersect(class_gene,rownames(CPM_expr)),]

rownames(data_H2AZ) <- class_gene
rownames(data_NSD3) <- class_gene
rownames(data_H3K36me2) <- class_gene
rownames(data_H3K36me3) <- class_gene

rownames(data2_H2AZ) <- class_gene
rownames(data2_NSD3) <- class_gene
rownames(data2_H3K36me2) <- class_gene
rownames(data2_H3K36me3) <- class_gene

data_H2AZ <- data_H2AZ[intersect(class_gene,rownames(CPM_expr)),]
data_NSD3 <- data_NSD3[intersect(class_gene,rownames(CPM_expr)),]
data_H3K36me2 <- data_H3K36me2[intersect(class_gene,rownames(CPM_expr)),]
data_H3K36me3 <- data_H3K36me3[intersect(class_gene,rownames(CPM_expr)),]

data2_H2AZ <- data2_H2AZ[intersect(class_gene,rownames(CPM_expr)),]
data2_NSD3 <- data2_NSD3[intersect(class_gene,rownames(CPM_expr)),]
data2_H3K36me2 <- data2_H3K36me2[intersect(class_gene,rownames(CPM_expr)),]
data2_H3K36me3 <- data2_H3K36me3[intersect(class_gene,rownames(CPM_expr)),]

L_data1 <- data.frame()
for (i in 1:nrow(data_NSD3)) {
  L_data1 [i,1] <- mean(data_NSD3[i,20:22])
  L_data1 [i,2] <- mean(data_H2AZ[i,20:22])
  L_data1 [i,3] <- mean(data_H3K36me2[i,20:22])
  L_data1 [i,4] <- mean(data_H3K36me3[i,20:22])
  L_data1 [i,5] <- mean(dDEG_CPM_expr[i,1:3])
}

colnames(L_data1) <- c('NSD3','H2AZ','H3K36me2','H3K36me3','RNA_Expr')

L_data2 <- data.frame()
for (i in 1:nrow(data_NSD3)) {
  L_data2 [i,1] <- mean(data2_NSD3[i,20:22])
  L_data2 [i,2] <- mean(data2_H2AZ[i,20:22])
  L_data2 [i,3] <- mean(data2_H3K36me2[i,20:22])
  L_data2 [i,4] <- mean(data2_H3K36me3[i,20:22])
  L_data2 [i,5] <- mean(dDEG_CPM_expr[i,4:6])
}

colnames(L_data2) <- c('NSD3','H2AZ','H3K36me2','H3K36me3','RNA_Expr')

L_data <-rbind(L_data1,L_data2)
#L_data <- data.frame(scale(L_data))

model1 <- lm(H3K36me3 ~  NSD3 + H2AZ ,data = L_data)

model2 <- lm(H3K36me2 ~  NSD3 + H2AZ + NSD3:H2AZ ,data = L_data)
model3 <- lm(H3K36me3 ~  NSD3 + H2AZ + NSD3:H2AZ ,data = L_data)
model4 <- lm(RNA_Expr ~  NSD3 + H2AZ + NSD3:H2AZ ,data = L_data)

summary(model1)
summary(model2)
summary(model3)
summary(model4)

anova(model1,model2)

#AREA UNDER CURVE -----
library(zoo)


gene_class <- list(NSD3_H2AZ_co_enrich = gom.obj@go.nested.list[["H2AZ_enrich"]][["NSD3_enrich"]]@intersection,
                   NSD3_only = gom.obj@go.nested.list[["H2AZ_non_enrich"]][["NSD3_enrich"]]@intersection,
                   H2AZ_only = gom.obj@go.nested.list[["H2AZ_enrich"]][["NSD3_non_enrich"]]@intersection,
                   NSD3_H2AZ_non_enrich = gom.obj@go.nested.list[["H2AZ_non_enrich"]][["NSD3_non_enrich"]]@intersection)


names(gene_class)
#H2AZ_enrich
enrich_class <- c('NSD3_H2AZ_non_enrich')

gene_class[[enrich_class]]
sample <- c('sgControl','sgNSD3')
H3K36me2_sample <- c("sgControl_H3K36me2_R1","sgControl_H3K36me2_R2",
                     "sgNSD3_H3K36me2_R1","sgNSD3_H3K36me2_R2")
H3K36me2_mat <- list()

H3K36me2_mat[[1]] <- (log2((enrichList[[H3K36me2_sample[1]]]+1)/(enrichList[[IgG_sample[1]]]+1)) + (log2(enrichList[[H3K36me2_sample[2]]]+1)/(enrichList[[IgG_sample[2]]]+1)))/2
H3K36me2_mat[[2]] <- (log2((enrichList[[H3K36me2_sample[3]]]+1)/(enrichList[[IgG_sample[3]]]+1)) + (log2(enrichList[[H3K36me2_sample[4]]]+1)/(enrichList[[IgG_sample[4]]]+1)))/2
names(H3K36me2_mat) <- sample 
H3K36me2_mat[[1]] <- H3K36me2_mat[[1]][gene_class[[enrich_class]],]
H3K36me2_mat[[2]] <- H3K36me2_mat[[2]][gene_class[[enrich_class]],]

X_REGION <- c(1:40)

AUC_dat <- data.frame(gene = gene_class[[enrich_class]], sgControl = NA, sgNSD3 = NA)
for (i in 1:nrow(AUC_dat)) {
  AUC_dat$sgControl[i] <- sum(diff(X_REGION)*rollmean(H3K36me2_mat[[1]][i,X_REGION],2))
  AUC_dat$sgNSD3[i] <- sum(diff(X_REGION)*rollmean(H3K36me2_mat[[2]][i,X_REGION],2))
}

AUC_dat$delta <- AUC_dat$sgControl-AUC_dat$sgNSD3
mean(AUC_dat$delta)
AUC_dat_A <- AUC_dat
mean(AUC_dat_A$sgControl)
mean(AUC_dat_A$sgNSD3)

AUC_dat_A$symbol <- str_split_fixed(AUC_dat_A$gene,pattern = ':',2)[,1]

AUC_dat_A$RNA_seq_Log2FC <- NA
for (i in 1:nrow(DESeq2_DEG)) {
  AUC_dat_A[which(AUC_dat_A$symbol == DESeq2_DEG$gene[i]),'RNA_seq_Log2FC'] <- DESeq2_DEG$log2FoldChange[i]
  AUC_dat_A[which(AUC_dat_A$symbol == DESeq2_DEG$gene[i]),'RNA_seq_adj_P'] <- DESeq2_DEG$padj[i]
}


candidate_dat <- na.omit(AUC_dat_A)

candidate_dat <- subset(candidate_dat, candidate_dat$RNA_seq_adj_P > 0.05 | abs(candidate_dat$RNA_seq_Log2FC) < 1)

#write.csv(AUC_dat_A,file = 'NSD3_H2AZ_non_enrich_list.csv')
write.csv(candidate_dat,file = 'control_candidate_list.csv')




