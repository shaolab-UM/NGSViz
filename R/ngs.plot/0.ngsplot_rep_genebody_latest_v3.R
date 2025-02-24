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
library(edgeR)
options(scipen = 2)

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
gene_cat <- c('allgene_FL300bp_L10kb')
norm_cat <- c('CPM')
ylab <- c('Enrich Density')

bam_cat <- c('dedup') #sorted , dedup , dedup.120bps

#setwd(paste("F:/UMProject/mouse_PanNET/Outs/CutRun/03.ngsplot/Norm_by_",norm_cat,'/',bam_cat,'/',gene_cat,'/',sep = ""))
setwd(paste("F:/UMproject/LSD2_NSD3/CutRun_2/03.ngsplot/Norm_by_",norm_cat,'/',bam_cat,'/',gene_cat,'/',sep = ""))
getwd()

#0.load ngsplot heatmap  ----
load("heatmap.RData")
sample_id <- ctg.tbl$title

length((rownames(enrichList[[1]]) == rownames(enrichList[[2]])) == TRUE)

for (i in 1:length(sample_id)) {
  names(enrichList)[i] <- sample_id[i]
}

raw_enrichList <- enrichList

##0.1 perform normalization----
SampleSheet <- readr::read_csv("F:/UMproject/LSD2_NSD3/CutRun_2/logs/SampleSheet.csv")

intersect(SampleSheet$Sample_ID,sample_id)

count_enrichList <- raw_enrichList

for (i in 1:nrow(SampleSheet)) {
  sa <- SampleSheet$Sample_ID[i]
  count_enrichList[[sa]] <- round(raw_enrichList[[sa]]*SampleSheet$total_reads[i]/1000000,digits = 0)
}

for (i in 1:length(count_enrichList)) {
  count_enrichList[[i]][count_enrichList[[i]] < 1 ] <- 1
}

#Spike norm----
adj_enrichList <- count_enrichList
for (i in 1:nrow(SampleSheet)) {
  sa <- SampleSheet$Sample_ID[i]
  adj_enrichList[[sa]] <- count_enrichList[[sa]]/SampleSheet$spikein_reads[i]*10000
  #adj_enrichList[[sa]] <- (count_enrichList[[sa]]/SampleSheet$total_reads[i]*1000000)*(10000/SampleSheet$spikein_reads[i])
}

#spkie & IgG norm-----
IgG_spike_enrichList <- adj_enrichList
names(adj_enrichList)
for (i in grep('sgControl',names(adj_enrichList))) {
  #IgG_spike_enrichList[[i]] <- round(log2((adj_enrichList[[i]]+1e-6)/(adj_enrichList[['sgControl_IgG_R2']]+1e-6)),digits = 3)
  IgG_spike_enrichList[[i]] <- round(log2(adj_enrichList[[i]]/adj_enrichList[['sgControl_NC_R']]),digits = 3)
}

for (i in grep('sgLSD2',names(adj_enrichList))) {
  #IgG_spike_enrichList[[i]] <- round(log2((adj_enrichList[[i]]+1e-6)/(adj_enrichList[['sgControl_IgG_R2']]+1e-6)),digits = 3)
  IgG_spike_enrichList[[i]] <- round(log2(adj_enrichList[[i]]/adj_enrichList[['sgLSD2_NC_R']]),digits = 3)
}

#IgG nor----
IgG_enrichList <- count_enrichList
for (i in grep('sgControl',names(count_enrichList))) {
  IgG_enrichList[[i]] <- log2((cpm(count_enrichList[[i]]))/(cpm(count_enrichList[['sgControl_NC_R']])))
}


for (i in grep('sgLSD2',names(count_enrichList))) {
  IgG_enrichList[[i]] <- log2((cpm(count_enrichList[[i]]))/(cpm(count_enrichList[['sgLSD2_NC_R']])))
}


#1. plot RNA-seq genes----
ngs_all_gene_id <- rownames(enrichList[[1]])
ngs_all_gene_info <- data.frame(gene_id = ngs_all_gene_id)
ngs_all_gene_info$symbol <- str_split_fixed(ngs_all_gene_info$gene_id,pattern = ':',2)[,1]

library(readr)
DESeq2_DEG <- read_csv("F:/UMproject/LSD2_NSD3/RNAseq/05.DESeq2/DESeq2_DEG.csv")

m_df<- msigdbr::msigdbr(species = "Homo sapiens", category = "H")

myc_target <- m_df[m_df$gs_name == "HALLMARK_MYC_TARGETS_V1",]$gene_symbol

RNA_list <- list(uDEGs = setdiff(DESeq2_DEG[DESeq2_DEG$change == 'UP',]$gene,myc_target),
                 dDEGs = setdiff(DESeq2_DEG[DESeq2_DEG$change == 'DOWN',]$gene,myc_target),
                 zmyc_target = myc_target)

RNA_list <- list(uDEGs = DESeq2_DEG[DESeq2_DEG$change == 'UP',]$gene, dDEGs = DESeq2_DEG[DESeq2_DEG$change == 'DOWN',]$gene)

#RNA_list <- list(myc_target = myc_gene)
gene_list <- RNA_list

#geneset to plot
names(gene_list)

ngs_all_gene_info$Cat <- 'background'
for (j in 1:length(gene_list)) {
  for (i in 1:length(gene_list[[j]])) {
    ngs_all_gene_info[which(ngs_all_gene_info$symbol == gene_list[[j]][i]),'Cat'] <- names(gene_list)[j]
  }
}

gene_cat <- unique(ngs_all_gene_info$Cat)
gene_cat <- gene_cat[order(gene_cat)]

gene_class <- list()
for (i in 1:length(gene_cat)) {
  gene_class[[i]] <- ngs_all_gene_info[ngs_all_gene_info$Cat == gene_cat[i],]$gene_id
  names(gene_class)[i] <- gene_cat[i]
}

gene_cat

##1.1 ALL genes Avg signal sgCtrl vs sgLSD2-----
group_list <- list(NSD3 = c(sample_id[grep('_NSD3_',sample_id)],sample_id[grep('NC',sample_id)]),
                   H3K4me2 = c(sample_id[grep('H3K4me2',sample_id)],sample_id[grep('NC',sample_id)]))

gene_cat

#choose normalization 
enrichList <- raw_enrichList
enrichList <- adj_enrichList
enrichList <- IgG_spike_enrichList
enrichList <- IgG_enrichList

plot_list <- list()
for (z in 1:length(group_list)) {
  sample <- group_list[[z]]
  Marker_mat <- list()
  for (j in 1:length(sample)) {
    Marker_mat[[j]] <- enrichList[[sample[j]]]
    names(Marker_mat)[j] <- sample[j]
  }
  
  j=3 #gene cat
  
  enrich_class <- gene_cat[j]
  
  Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
  
  colnames(Marker_avg) <- sample
  rownames(Marker_avg) <- c(1:101)
  
  for (i in 1:length(sample)) {
    Marker_avg[,i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-Marker_avg
  
  for (i in 1:ncol(Marker_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-50,method = 'mean')
  }
  
  Marker_avg <- avg_smooth
  
  Marker_data<-list()
  for (i in 1:ncol(Marker_avg)) {
    Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
    Marker_data[[i]]$group<-colnames(Marker_avg)[i]
  }
  
  data <- Marker_data[[1]]
  for (i in 2:length(Marker_data)) {
    data <- rbind(data,Marker_data[[i]])
  }
  
  data$group <- factor(data$group,levels = sample)
  max(data$y)
  min(data$y)
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(Marker_data)
  color <-getPalette(colourCount_1)
  color <- c("#FFC0CB","#FF0000","#8B0000",
             "#9ACD32",'#00FF00',"#006400",
             '#0000CC','#3333FF',
             '#202020','#808080')
  
  plot_list[[z]] <- ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,21,81,100))+
    scale_y_continuous(name = ylab,limits = c(min(data$y),max(data$y),breaks = seq(min(data$y),max(data$y),0.1)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = unique(str_split_fixed(sample,'_',3)[,2]))+
    theme(plot.title = element_text(size = 11, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          legend.position = c(0.7,1),
          #legend.position = 'none',
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(21,81), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
}

wrap_plots(plot_list,nrow = 1,ncol = 2)

##1.2 combined sample----
enrichList <- raw_enrichList
enrichList <- adj_enrichList
enrichList <- IgG_spike_enrichList

group_list <- list(NSD3 = c(sample_id[grep('_NSD3_',sample_id)]),
                   H3K4me2 = c(sample_id[grep('H3K4me2',sample_id)]))

plot_list <- list()
z=1
for (z in 1:length(group_list)) {
  sample <- group_list[[z]]
  
  combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
  
  Marker_mat <- list()
  
  if (length(sample) > 2) {
    Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]]+enrichList[[sample[3]]])/3
    Marker_mat[[2]] <- (enrichList[[sample[4]]]+enrichList[[sample[5]]]+enrichList[[sample[6]]])/3
  } else {
    Marker_mat[[1]] <- enrichList[[sample[1]]]
    Marker_mat[[2]] <- enrichList[[sample[2]]]
  }
  
  if (length(grep('NC',sample)) > 0 ) {
    Marker_mat[[3]] <- enrichList[[sample[7]]]
    Marker_mat[[4]] <- enrichList[[sample[8]]]
  } 
  
  names(Marker_mat) <- combined_sample
  
  j=1 #gene cat
  #for (j in 1:length(gene_cat)) {
  
  enrich_class <- gene_cat[j]
  
  Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
  
  colnames(Marker_avg) <- combined_sample
  rownames(Marker_avg) <- c(1:101)
  
  for (i in 1:length(combined_sample)) {
    Marker_avg[,i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-Marker_avg
  
  for (i in 1:ncol(Marker_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-50,method = 'mean')
  }
  
  Marker_avg <- avg_smooth
  
  Marker_data<-list()
  for (i in 1:ncol(Marker_avg)) {
    Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
    Marker_data[[i]]$group<-colnames(Marker_avg)[i]
  }
  
  data <- Marker_data[[1]]
  for (i in 2:length(Marker_data)) {
    data <- rbind(data,Marker_data[[i]])
  }
  
  data$group <- factor(data$group,levels = combined_sample)
  max(data$y)
  min(data$y)
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(Marker_data)
  color <-getPalette(colourCount_1)
  color <- c("#990000", "#009900",'#0000CC','#3333FF')
  
  plot_list[[z]] <- ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(min(data$y),max(data$y),breaks = seq(min(data$y),max(data$y),0.1)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = unique(str_split_fixed(sample,'_',3)[,2]))+
    theme(plot.title = element_text(size = 11, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          legend.position = c(0.6,1),
          #legend.position = 'none',
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
}

wrap_plots(plot_list,nrow = 1,ncol = 2)

#2.DEGs split by signals----
group_list <- list(NSD3 = c(sample_id[grep('_NSD3_',sample_id)]),
                   H3K4me2 = c(sample_id[grep('H3K4me2',sample_id)]))

group_list <- list(NSD3 = c(sample_id[grep('_NSD3_',sample_id)],sample_id[grep('NC',sample_id)]),
                   H3K4me2 = c(sample_id[grep('H3K4me2',sample_id)],sample_id[grep('NC',sample_id)]))

enrichList <- raw_enrichList

enrichList <- adj_enrichList

enrichList <- IgG_spike_enrichList

enrichList <- IgG_enrichList

names(group_list)
y_max <- list()
y_min <- list()

for (z in 1:length(group_list)) {
  sample <- group_list[[z]]
  
  t <- str_split_fixed(sample,'_',3)[1,2]
  
  combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
  Marker_mat <- list()
  if (length(sample) > 2) {
    Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]]+enrichList[[sample[3]]])/3
    Marker_mat[[2]] <- (enrichList[[sample[4]]]+enrichList[[sample[5]]]+enrichList[[sample[6]]])/3
  } else {
    Marker_mat[[1]] <- enrichList[[sample[1]]]
    Marker_mat[[2]] <- enrichList[[sample[2]]]
  }
  
  if (length(grep('NC',sample)) > 0 ) {
    Marker_mat[[3]] <- enrichList[[sample[7]]]
    Marker_mat[[4]] <- enrichList[[sample[8]]]
  } 
  
  
  dat_max <- data.frame(gene_cat = gene_cat,
                        y_max = NA,
                        y_min = NA) 
  
  for (j in 1:length(gene_cat)) {
    
    enrich_class <- gene_cat[j]
    Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
    
    colnames(Marker_avg) <-combined_sample
    rownames(Marker_avg) <- c(1:101)
    
    for (i in 1:length(combined_sample)) {
      Marker_avg[,i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
    }
    
    avg_smooth <- Marker_avg
    
    for (i in 1:ncol(Marker_avg)) {
      avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-10,method = 'mean')
    }
    
    Marker_avg <- avg_smooth
    
    Marker_data<-list()
    for (i in 1:ncol(Marker_avg)) {
      Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
      Marker_data[[i]]$group<-colnames(Marker_avg)[i]
    }
    
    data <- Marker_data[[1]]
    for (i in 2:length(Marker_data)) {
      data <- rbind(data,Marker_data[[i]])
    }
    
    data$group <- factor(data$group,levels = combined_sample)
    max(data$y)
    min(data$y)
    
    dat_max$y_max[j] <- max(data$y)
    dat_max$y_min[j] <- min(data$y)
  }
  y_max[[z]] <- round(max(dat_max$y_max),digits=3)
  
  if (min(dat_max$y_min) < 0 ) {
    y_min[[z]] <- round(min(dat_max$y_min),digits=3)
  } else {
    y_min[[z]] <- round(min(dat_max$y_min),digits=3)
  }
  
  names(y_max)[z] <- t
  names(y_min)[z] <- t
}

  ##2.2 NSD3 signals----
{
  plot_list <- list()
  z=1
  sample <- group_list[[z]]
  t <- unique(str_split_fixed(sample,'_',3)[,2])
  combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
  Marker_mat <- list()
  if (length(sample) > 2) {
    Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]]+enrichList[[sample[3]]])/3
    Marker_mat[[2]] <- (enrichList[[sample[4]]]+enrichList[[sample[5]]]+enrichList[[sample[6]]])/3
  } else {
    Marker_mat[[1]] <- enrichList[[sample[1]]]
    Marker_mat[[2]] <- enrichList[[sample[2]]]
  }
  if (length(grep('NC',sample)) > 0 ) {
    Marker_mat[[3]] <- enrichList[[sample[7]]]
    Marker_mat[[4]] <- enrichList[[sample[8]]]
  } 
  
  names(Marker_mat) <- combined_sample
  
  for (j in 1:length(gene_cat)) {
    
    enrich_class <- gene_cat[j]
    Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
    
    colnames(Marker_avg) <-combined_sample
    rownames(Marker_avg) <- c(1:101)
    
    for (i in 1:length(combined_sample)) {
      Marker_avg[,i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
    }
    
    avg_smooth<-Marker_avg
    
    for (i in 1:ncol(Marker_avg)) {
      avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-2,method = 'mean')
    }
    
    Marker_avg <- avg_smooth
    
    Marker_data<-list()
    for (i in 1:ncol(Marker_avg)) {
      Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
      Marker_data[[i]]$group<-colnames(Marker_avg)[i]
    }
    
    data <- Marker_data[[1]]
    for (i in 2:length(Marker_data)) {
      data <- rbind(data,Marker_data[[i]])
    }
    
    data$group <- factor(data$group,levels = combined_sample)
    max(data$y)
    min(data$y)
    
    getPalette = colorRampPalette(brewer.pal(8, "Set1"))
    colourCount_1 = length(Marker_data)
    color <-getPalette(colourCount_1)
    color <- c("#990000", "#009900",'#0000CC','#3333FF')
    
    plot_list[[j]] <- ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
      geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
      theme_bw() + 
      theme(panel.border = element_blank(),
            panel.grid = element_blank())+
      theme(axis.line = element_line(size=1, colour = "black"))+
      scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
      scale_y_continuous(name = ylab,limits = c(y_min[[z]],y_max[[z]]),breaks = seq(y_min[[z]],y_max[[z]],round((y_max[[z]]-y_min[[z]])/5,digits = 4)))+    
      #scale_y_continuous(name = ylab,limits = c(-1,0.5,breaks = seq(-1,0.5,0.2)))+
      theme(axis.text.x = element_text(size = 10, color = "black"))+
      theme(axis.text.y = element_text(size = 10, color = "black"))+
      theme(axis.title.y = element_text(size = 10, color = "black"))+
      labs(title = t)+
      theme(plot.title = element_text(size = 11, color = "black",hjust = 0.5))+
      theme(legend.text = element_text(size = 10, color = "black"))+
      theme(legend.title = element_text(size = 10, color = "black"))+
      theme(panel.grid=element_blank(),
            #legend.position = c(0.7,1),
            legend.position = 'none',
            legend.justification = c(0,1),
            legend.spacing.y = unit(0.1,'cm'),
            legend.key.height = unit(0.1,'cm'))+
      theme(legend.text = element_text(size = 8, color = "black"))+
      theme(legend.title=element_blank())+
      geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
      theme(plot.margin = margin(0, 0, 0, 0, "cm"))
  }
  
  p1 <- wrap_plots(plot_list,ncol = 1)
  
  ##2.3 H3K4me2 signals----
  plot_list <- list()
  z=2
  sample <- group_list[[z]]
  t <- unique(str_split_fixed(sample,'_',3)[,2])
  combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
  Marker_mat <- list()
  if (length(sample) > 2) {
    Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]]+enrichList[[sample[3]]])/3
    Marker_mat[[2]] <- (enrichList[[sample[4]]]+enrichList[[sample[5]]]+enrichList[[sample[6]]])/3
  } else {
    Marker_mat[[1]] <- enrichList[[sample[1]]]
    Marker_mat[[2]] <- enrichList[[sample[2]]]
  }
  
  if (length(grep('NC',sample)) > 0 ) {
    Marker_mat[[3]] <- enrichList[[sample[7]]]
    Marker_mat[[4]] <- enrichList[[sample[8]]]
  } 
  
  names(Marker_mat) <- combined_sample
  
  for (j in 1:length(gene_cat)) {
    
    enrich_class <- gene_cat[j]
    Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
    
    colnames(Marker_avg) <-combined_sample
    rownames(Marker_avg) <- c(1:101)
    
    for (i in 1:length(combined_sample)) {
      Marker_avg[,i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
    }
    
    avg_smooth<-Marker_avg
    
    for (i in 1:ncol(Marker_avg)) {
      avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-2,method = 'mean')
    }
    
    Marker_avg <- avg_smooth
    
    Marker_data<-list()
    for (i in 1:ncol(Marker_avg)) {
      Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
      Marker_data[[i]]$group<-colnames(Marker_avg)[i]
    }
    
    data <- Marker_data[[1]]
    for (i in 2:length(Marker_data)) {
      data <- rbind(data,Marker_data[[i]])
    }
    
    data$group <- factor(data$group,levels = combined_sample)
    max(data$y)
    min(data$y)
    
    getPalette = colorRampPalette(brewer.pal(8, "Set1"))
    colourCount_1 = length(Marker_data)
    color <-getPalette(colourCount_1)
    color <- c("#990000", "#009900",'#0000CC','#3333FF')
    
    plot_list[[j]] <- ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
      geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
      theme_bw() + 
      theme(panel.border = element_blank(),
            panel.grid = element_blank())+
      theme(axis.line = element_line(size=1, colour = "black"))+
      scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
      scale_y_continuous(name = ylab,limits = c(y_min[[z]],y_max[[z]]),breaks = seq(y_min[[z]],y_max[[z]],round((y_max[[z]]-y_min[[z]])/5,digits = 2)))+    #scale_y_continuous(name = ylab,limits = c(-1,0.5,breaks = seq(-1,0.5,0.2)))+
      theme(axis.text.x = element_text(size = 10, color = "black"))+
      theme(axis.text.y = element_text(size = 10, color = "black"))+
      theme(axis.title.y = element_text(size = 10, color = "black"))+
      labs(title = t)+
      theme(plot.title = element_text(size = 11, color = "black",hjust = 0.5))+
      theme(legend.text = element_text(size = 10, color = "black"))+
      theme(legend.title = element_text(size = 10, color = "black"))+
      theme(panel.grid=element_blank(),
            #legend.position = c(0.7,1),
            legend.position = 'none',
            legend.justification = c(0,1),
            legend.spacing.y = unit(0.1,'cm'),
            legend.key.height = unit(0.1,'cm'))+
      theme(legend.text = element_text(size = 8, color = "black"))+
      theme(legend.title=element_blank())+
      geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
      theme(plot.margin = margin(0, 0, 0, 0, "cm"))
  }
  
  p2 <- wrap_plots(plot_list,ncol = 1)
}  
  
p1|p2


#3. extract combined matrix for clustering-----
enrichList <- IgG_spike_enrichList

sgC_mat <- list()
for (z in 1:length(group_list)) {
  sample <- group_list[[z]]
  t <- unique(str_split_fixed(sample,'_',3)[,2])
  combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
  Marker_mat <- list()
  if (length(sample) > 2) {
    Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]]+enrichList[[sample[3]]])/3
    Marker_mat[[2]] <- (enrichList[[sample[4]]]+enrichList[[sample[5]]]+enrichList[[sample[6]]])/3
  } else {
    Marker_mat[[1]] <- enrichList[[sample[1]]]
    Marker_mat[[2]] <- enrichList[[sample[2]]]
  }
  
  sgC_mat[[z]] <- Marker_mat[[1]]
  names(sgC_mat)[z] <- combined_sample[1]
}

sgN_mat <- list()
for (z in 1:length(group_list)) {
  sample <- group_list[[z]]
  t <- unique(str_split_fixed(sample,'_',3)[,2])
  combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
  Marker_mat <- list()
  if (length(sample) > 2) {
    Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]])/2
    Marker_mat[[2]] <- (enrichList[[sample[3]]]+enrichList[[sample[4]]])/2
  } else {
    Marker_mat[[1]] <- enrichList[[sample[1]]]
    Marker_mat[[2]] <- enrichList[[sample[2]]]
  }
  
  sgN_mat[[z]] <- Marker_mat[[2]]
  names(sgN_mat)[z] <- combined_sample[2]
}


#LSD2 cat heirachical----
data <- sgC_mat[['sgControl_LSD2']]
Y_LSD2<-data.frame()
for (i in 1:nrow(data)) {
  Y_LSD2[i,1]<-mean(data[i,21])
}

length(row.names(data))
length(unique(row.names(data)))

colnames(Y_LSD2) <- c('LSD2')
row.names(Y_LSD2)<-row.names(data)

hc=hclust(dist(Y_LSD2,method = 'euclidean'),method = 'ward.D')

#fviz_nbclust(Y_LSD2, FUN = hcut, method = "wss",k.max = 10)
#fviz_nbclust(Y_LSD2, FUN = hcut, method = "silhouette",k.max = 10)

plot(hc,label=FALSE)
rect.hclust(hc, k=4, border="red")

hc_result=cutree(hc,k = 4)

Y_LSD2$gene <- rownames(Y_LSD2)
Y_LSD2$hc <- hc_result
table(Y_LSD2$hc)

mean(Y_LSD2[Y_LSD2$hc==1,]$LSD2)
mean(Y_LSD2[Y_LSD2$hc==2,]$LSD2)
mean(Y_LSD2[Y_LSD2$hc==3,]$LSD2)
mean(Y_LSD2[Y_LSD2$hc==4,]$LSD2)

Y_LSD2$hc <- factor(Y_LSD2$hc,levels = c(2,1,3,4))

Y_LSD2$cat <- NA
Y_LSD2[Y_LSD2$hc == 2,'cat'] <- 'A'
Y_LSD2[Y_LSD2$hc == 1,'cat'] <- 'B'
Y_LSD2[Y_LSD2$hc == 3,'cat'] <- 'C'
Y_LSD2[Y_LSD2$hc == 4,'cat'] <- 'D'

Y_LSD2 <- Y_LSD2[order(Y_LSD2$cat),]
r_order <- Y_LSD2$gene

LSD2_class_A <- Y_LSD2[Y_LSD2$cat == 'A','gene']
LSD2_class_B <- Y_LSD2[Y_LSD2$cat == 'B','gene']
LSD2_class_C <- Y_LSD2[Y_LSD2$cat == 'C','gene']
LSD2_class_D <- Y_LSD2[Y_LSD2$cat == 'D','gene']

LSD2_class_list <- list(LSD2_class_A=LSD2_class_A,
                        LSD2_class_B=LSD2_class_B,
                        LSD2_class_C=LSD2_class_C,
                        LSD2_class_D=LSD2_class_D)

#NSD3 CAT HEIRACHICAL----
data3 <- sgC_mat[['sgControl_NSD3']]
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

Y_NSD3$hc <- factor(Y_NSD3$hc,levels = c(3,4,1,2))

Y_NSD3$cat <- NA
Y_NSD3[Y_NSD3$hc == 3,'cat'] <- 'A'
Y_NSD3[Y_NSD3$hc == 4,'cat'] <- 'B'
Y_NSD3[Y_NSD3$hc == 1,'cat'] <- 'C'
Y_NSD3[Y_NSD3$hc == 2,'cat'] <- 'D'

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

gom.obj <- newGOM(NSD3_class_list,LSD2_class_list,genome.size = 21744)

drawHeatmap(gom.obj)

NSD3_list <- list(NSD3_enrich = c(NSD3_class_list[[1]],NSD3_class_list[[2]]),
                  NSD3_non_enrich = c(NSD3_class_list[[3]],NSD3_class_list[[4]]))

LSD2_list <- list(LSD2_enrich = c(LSD2_class_list[[1]],LSD2_class_list[[2]]),
                  LSD2_non_enrich = c(LSD2_class_list[[3]],LSD2_class_list[[4]]))

gom.obj <- newGOM(NSD3_list,LSD2_list,genome.size = 21744)
drawHeatmap(gom.obj)

#NSD3 CALSS HEATMAP----
brewer.pal.info
display.brewer.pal(9,"RdBu")
coul <- colorRampPalette(brewer.pal(9, "RdBu"))(9)
coul
col_fun = colorRamp2(c(-20,0,20), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-20,0,20),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

table(Y_NSD3$cat)
r_order <- Y_NSD3$gene
#r_split <- factor(rep(c('A','B','C','D'),c(1777,2147,13503,4317)))
r_split <- factor(rep(c('A','B'),c(sum(1777,2147),sum(13503,4317))))
table(r_split)

ht1<-Heatmap(data[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE)

col_fun = colorRamp2(c(-10,0,10), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-10,0,10),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht2<-Heatmap(data3[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)



ht_list<-ht2+ht1

pdf('00.heatmap_NSD3_CLASS.pdf',height = 10,width = 25)
draw(ht_list,heatmap_legend_side = "top",ht_gap = unit(0.4, "cm"))
dev.off()

#LSD2 CLASS HEATMAP----
brewer.pal.info
display.brewer.pal(9,"RdBu")
coul <- colorRampPalette(brewer.pal(9, "RdBu"))(9)
coul
col_fun = colorRamp2(c(-20,0,20), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-20,0,20),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

table(Y_LSD2$cat)

r_order <- Y_LSD2$gene

#r_split <- factor(rep(c('A','B','C','D'),c(4027,14049,1646,2022)))
r_split <- factor(rep(c('A','B'),c(sum(4027),sum(14049,1646,2022))))
table(r_split)

ht1<-Heatmap(data[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE)

col_fun = colorRamp2(c(-10,0,10), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-10,0,10),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht2<-Heatmap(data3[r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 25,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

ht_list<-ht1+ht2

pdf('00.heatmap_LSD2_CLASS.pdf',height = 10,width = 25)
draw(ht_list,heatmap_legend_side = "top",ht_gap = unit(0.4, "cm"))
dev.off()

#4.dDEGs Enrichheamp plot-----
M_enrichList <- c(sgC_mat,sgN_mat)

for (i in 1:length(M_enrichList)) {
  M_enrichList[[i]] <- M_enrichList[[i]][gene_class[["dDEGs"]],]
}

#LSD2 cat heirachical----
data <- M_enrichList[['sgControl_LSD2']]
Y_LSD2<-data.frame()
for (i in 1:nrow(data)) {
  Y_LSD2[i,1]<-mean(data[i,21])
}

length(row.names(data))
length(unique(row.names(data)))

colnames(Y_LSD2) <- c('LSD2')
row.names(Y_LSD2)<-row.names(data)

hc=hclust(dist(Y_LSD2,method = 'euclidean'),method = 'ward.D')

#fviz_nbclust(Y_LSD2, FUN = hcut, method = "wss",k.max = 10)
#fviz_nbclust(Y_LSD2, FUN = hcut, method = "silhouette",k.max = 10)

plot(hc,label=FALSE)
rect.hclust(hc, k=4, border="red")

hc_result=cutree(hc,k = 4)

Y_LSD2$gene <- rownames(Y_LSD2)
Y_LSD2$hc <- hc_result
table(Y_LSD2$hc)

mean(Y_LSD2[Y_LSD2$hc==1,]$LSD2)
mean(Y_LSD2[Y_LSD2$hc==2,]$LSD2)
mean(Y_LSD2[Y_LSD2$hc==3,]$LSD2)
mean(Y_LSD2[Y_LSD2$hc==4,]$LSD2)

Y_LSD2$hc <- factor(Y_LSD2$hc,levels = c(3,2,1,4))

Y_LSD2$cat <- NA
Y_LSD2[Y_LSD2$hc == 3,'cat'] <- 'A'
Y_LSD2[Y_LSD2$hc == 2,'cat'] <- 'B'
Y_LSD2[Y_LSD2$hc == 1,'cat'] <- 'C'
Y_LSD2[Y_LSD2$hc == 4,'cat'] <- 'D'

Y_LSD2 <- Y_LSD2[order(Y_LSD2$cat),]
r_order <- Y_LSD2$gene

LSD2_class_A <- Y_LSD2[Y_LSD2$cat == 'A','gene']
LSD2_class_B <- Y_LSD2[Y_LSD2$cat == 'B','gene']
LSD2_class_C <- Y_LSD2[Y_LSD2$cat == 'C','gene']
LSD2_class_D <- Y_LSD2[Y_LSD2$cat == 'D','gene']

LSD2_class_list <- list(LSD2_class_A=LSD2_class_A,
                        LSD2_class_B=LSD2_class_B,
                        LSD2_class_C=LSD2_class_C,
                        LSD2_class_D=LSD2_class_D)


LSD2_list <- list(LSD2_highly_enrich = c(LSD2_class_list[[1]]),
                  LSD2_median_enrich = c(LSD2_class_list[[2]],LSD2_class_list[[3]]),
                  LSD2_non_enrich = c(LSD2_class_list[[4]]))

#LSD2 CLASS HEATMAP----

table(Y_LSD2$cat)
r_order <- Y_LSD2$gene

#r_split <- factor(rep(c('A','B','C','D'),c(4027,14049,1646,2022)))
r_split <- factor(rep(c('A','B','C'),c(sum(278),sum(292,426),sum(42))))
table(r_split)

col_fun = colorRamp2(c(-10,0,10), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-10,0,10),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht1<-Heatmap(M_enrichList[['sgControl_LSD2']][r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 15,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE)

ht2<-Heatmap(M_enrichList[['sgLSD2_LSD2']][r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 15,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

col_fun = colorRamp2(c(-5,0,5), c("#2166AC","#F7FBFF","#F00000"))
lgd = list(col_fun = col_fun, title = "",at = c(-5,0,5),direction = "horizontal",
           title_position = "topcenter",legend_width = unit(3, "cm"),border=TRUE)

ht3<-Heatmap(M_enrichList[['sgControl_NSD3']][r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 15,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)

ht4<-Heatmap(M_enrichList[['sgLSD2_NSD3']][r_order,],
             col = col_fun,row_split = r_split,
             width = unit(3, "cm"), height = unit(10, "cm"),
             cluster_rows = FALSE, cluster_columns = FALSE,use_raster = TRUE,raster_quality = 15,
             show_column_dend=FALSE,show_row_dend = FALSE,show_row_names = FALSE,
             heatmap_legend_param = lgd,border = TRUE
)


ht_list<-ht1+ht2+ht3+ht4

pdf('00.ddegheatmap_LSD2_CLASS.pdf',height = 7,width = 20)
draw(ht_list,heatmap_legend_side = "top",ht_gap = unit(0.4, "cm"))
dev.off()

#2.DEGs split by signals----
enrichList <- IgG_spike_enrichList
#enrichList <- adj_enrichList
LSD2_list <- list(LSD2_highly_enrich = c(LSD2_class_list[[1]]),
                  LSD2_median_enrich = c(LSD2_class_list[[2]],LSD2_class_list[[3]]),
                  LSD2_non_enrich = c(LSD2_class_list[[4]]))

gene_class <- LSD2_list
gene_cat <- names(gene_class)

names(group_list)
y_max <- list()
y_min <- list()
z=1
for (z in 1:length(group_list)) {
  sample <- group_list[[z]]
  t <- unique(str_split_fixed(sample,'_',3)[,2])
  combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
  Marker_mat <- list()
  if (length(sample) > 2) {
    Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]])/2
    Marker_mat[[2]] <- (enrichList[[sample[3]]]+enrichList[[sample[4]]])/2
  } else {
    Marker_mat[[1]] <- enrichList[[sample[1]]]
    Marker_mat[[2]] <- enrichList[[sample[2]]]
  }
  
  
  dat_max <- data.frame(gene_cat = gene_cat,
                        y_max = NA,
                        y_min = NA) 
  
  for (j in 1:length(gene_cat)) {
    
    enrich_class <- gene_cat[j]
    Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
    
    colnames(Marker_avg) <-combined_sample
    rownames(Marker_avg) <- c(1:101)
    
    for (i in 1:length(combined_sample)) {
      Marker_avg[,i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
    }
    
    avg_smooth <- Marker_avg
    
    for (i in 1:ncol(Marker_avg)) {
      avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-10,method = 'mean')
    }
    
    Marker_avg <- avg_smooth
    
    Marker_data<-list()
    for (i in 1:ncol(Marker_avg)) {
      Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
      Marker_data[[i]]$group<-colnames(Marker_avg)[i]
    }
    
    data <- Marker_data[[1]]
    for (i in 2:length(Marker_data)) {
      data <- rbind(data,Marker_data[[i]])
    }
    
    data$group <- factor(data$group,levels = combined_sample)
    max(data$y)
    min(data$y)
    
    dat_max$y_max[j] <- max(data$y)
    dat_max$y_min[j] <- min(data$y)
  }
  
  y_max[[z]] <- round(max(dat_max$y_max),digits=0) + 0.5
  
  if (min(dat_max$y_min) < 0 ) {
    y_min[[z]] <- round(min(dat_max$y_min),digits=1)
  } else {
    y_min[[z]] <- floor(min(dat_max$y_min))
  }
  
  names(y_max)[z] <- t
  names(y_min)[z] <- t
}

y_max[['LSD2']] <- y_max[['LSD2']] + 0.5

#2.DEGs split by signals----
enrichList <- IgG_spike_enrichList
#enrichList <- adj_enrichList
LSD2_list <- list(LSD2_highly_enrich = c(LSD2_class_list[[1]]),
                  LSD2_median_enrich = c(LSD2_class_list[[2]],LSD2_class_list[[3]]),
                  LSD2_non_enrich = c(LSD2_class_list[[4]]))

DEG_cat <- names(LSD2_list)

names(group_list)
y_max <- list()
y_min <- list()
z=1
for (z in 1:length(group_list)) {
  sample <- group_list[[z]]
  t <- unique(str_split_fixed(sample,'_',3)[,2])
  combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
  Marker_mat <- list()
  if (length(sample) > 2) {
    Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]]+enrichList[[sample[3]]])/3
    Marker_mat[[2]] <- (enrichList[[sample[4]]]+enrichList[[sample[5]]]+enrichList[[sample[6]]])/3
  } else {
    Marker_mat[[1]] <- enrichList[[sample[1]]]
    Marker_mat[[2]] <- enrichList[[sample[2]]]
  }
  
  
  dat_max <- data.frame(DEG_cat = DEG_cat,
                        y_max = NA,
                        y_min = NA) 
  
  for (j in 1:length(DEG_cat)) {
    
    enrich_class <- DEG_cat[j]
    Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
    
    colnames(Marker_avg) <-combined_sample
    rownames(Marker_avg) <- c(1:101)
    
    for (i in 1:length(combined_sample)) {
      Marker_avg[,i] <- colMeans(Marker_mat[[i]][LSD2_list[[enrich_class]],])
    }
    
    avg_smooth <- Marker_avg
    
    for (i in 1:ncol(Marker_avg)) {
      avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-10,method = 'mean')
    }
    
    Marker_avg <- avg_smooth
    
    Marker_data<-list()
    for (i in 1:ncol(Marker_avg)) {
      Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
      Marker_data[[i]]$group<-colnames(Marker_avg)[i]
    }
    
    data <- Marker_data[[1]]
    for (i in 2:length(Marker_data)) {
      data <- rbind(data,Marker_data[[i]])
    }
    
    data$group <- factor(data$group,levels = combined_sample)
    max(data$y)
    min(data$y)
    
    dat_max$y_max[j] <- max(data$y)
    dat_max$y_min[j] <- min(data$y)
  }
  
  y_max[[z]] <- round(max(dat_max$y_max),digits=0) + 0.5
  
  if (min(dat_max$y_min) < 0 ) {
    y_min[[z]] <- round(min(dat_max$y_min),digits=1)
  } else {
    y_min[[z]] <- floor(min(dat_max$y_min))
  }
  
  names(y_max)[z] <- t
  names(y_min)[z] <- t
}

y_max[['LSD2']] <- y_max[['LSD2']] + 0.5

##2.1 LSD2 signals----
plot_list <- list()
z=1
sample <- group_list[[z]]
t <- unique(str_split_fixed(sample,'_',3)[,2])
combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
Marker_mat <- list()
if (length(sample) > 2) {
  Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]]+enrichList[[sample[3]]])/3
  Marker_mat[[2]] <- (enrichList[[sample[4]]]+enrichList[[sample[5]]]+enrichList[[sample[6]]])/3
} else {
  Marker_mat[[1]] <- enrichList[[sample[1]]]
  Marker_mat[[2]] <- enrichList[[sample[2]]]
}
#Marker_mat[[3]] <- (enrichList[[sample[7]]]+enrichList[[sample[8]]])/2
#Marker_mat[[4]] <- (enrichList[[sample[9]]]+enrichList[[sample[10]]])/2
names(Marker_mat) <- combined_sample

for (j in 1:length(gene_cat)) {
  
  enrich_class <- gene_cat[j]
  Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
  
  colnames(Marker_avg) <-combined_sample
  rownames(Marker_avg) <- c(1:101)
  
  for (i in 1:length(combined_sample)) {
    Marker_avg[,i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
  }
  
  #avg_smooth<-Marker_avg
  
  #for (i in 1:ncol(Marker_avg)) {
  # avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-100,method = 'mean')
  #}
  
  #Marker_avg <- avg_smooth
  
  Marker_data<-list()
  for (i in 1:ncol(Marker_avg)) {
    Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
    Marker_data[[i]]$group<-colnames(Marker_avg)[i]
  }
  
  data <- Marker_data[[1]]
  for (i in 2:length(Marker_data)) {
    data <- rbind(data,Marker_data[[i]])
  }
  
  data$group <- factor(data$group,levels = combined_sample)
  max(data$y)
  min(data$y)
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(Marker_data)
  color <-getPalette(colourCount_1)
  color <- c("#990000", "#009900","black","darkgray")
  
  plot_list[[j]] <- ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(y_min[[z]],y_max[[z]]),breaks = seq(y_min[[z]],y_max[[z]],round((y_max[[z]]-y_min[[z]])/5,digits = 0)))+
    #scale_y_continuous(name = ylab,limits = c(-1,0.5,breaks = seq(-1,0.5,0.2)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = t)+
    theme(plot.title = element_text(size = 11, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          #legend.position = c(0.7,1),
          legend.position = 'none',
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
}

p1 <- wrap_plots(plot_list,ncol = 1)

##2.2 NSD3 signals----
plot_list <- list()
z=2
sample <- group_list[[z]]
t <- unique(str_split_fixed(sample,'_',3)[,2])
combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
Marker_mat <- list()
if (length(sample) > 2) {
  Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]]+enrichList[[sample[3]]])/3
  Marker_mat[[2]] <- (enrichList[[sample[4]]]+enrichList[[sample[5]]]+enrichList[[sample[6]]])/3
} else {
  Marker_mat[[1]] <- enrichList[[sample[1]]]
  Marker_mat[[2]] <- enrichList[[sample[2]]]
}
#Marker_mat[[3]] <- (enrichList[[sample[7]]]+enrichList[[sample[8]]])/2
#Marker_mat[[4]] <- (enrichList[[sample[9]]]+enrichList[[sample[10]]])/2
names(Marker_mat) <- combined_sample

for (j in 1:length(gene_cat)) {
  
  enrich_class <- gene_cat[j]
  Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
  
  colnames(Marker_avg) <-combined_sample
  rownames(Marker_avg) <- c(1:101)
  
  for (i in 1:length(combined_sample)) {
    Marker_avg[,i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-Marker_avg
  
  for (i in 1:ncol(Marker_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-10,method = 'mean')
  }
  
  Marker_avg <- avg_smooth
  
  Marker_data<-list()
  for (i in 1:ncol(Marker_avg)) {
    Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
    Marker_data[[i]]$group<-colnames(Marker_avg)[i]
  }
  
  data <- Marker_data[[1]]
  for (i in 2:length(Marker_data)) {
    data <- rbind(data,Marker_data[[i]])
  }
  
  data$group <- factor(data$group,levels = combined_sample)
  max(data$y)
  min(data$y)
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(Marker_data)
  color <-getPalette(colourCount_1)
  color <- c("#990000", "#009900","black","darkgray")
  
  plot_list[[j]] <- ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(y_min[[z]],y_max[[z]]),breaks = seq(y_min[[z]],y_max[[z]],round((y_max[[z]]-y_min[[z]])/5,digits = 0)))+    
    #scale_y_continuous(name = ylab,limits = c(-1,0.5,breaks = seq(-1,0.5,0.2)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = t)+
    theme(plot.title = element_text(size = 11, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          #legend.position = c(0.7,1),
          legend.position = 'none',
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
}

p2 <- wrap_plots(plot_list,ncol = 1)

##2.3 H3K4me2 signals----
plot_list <- list()
z=4
sample <- group_list[[z]]
t <- unique(str_split_fixed(sample,'_',3)[,2])
combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
Marker_mat <- list()
if (length(sample) > 2) {
  Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]]+enrichList[[sample[3]]])/3
  Marker_mat[[2]] <- (enrichList[[sample[4]]]+enrichList[[sample[5]]]+enrichList[[sample[6]]])/3
} else {
  Marker_mat[[1]] <- enrichList[[sample[1]]]
  Marker_mat[[2]] <- enrichList[[sample[2]]]
}
#Marker_mat[[3]] <- (enrichList[[sample[7]]]+enrichList[[sample[8]]])/2
#Marker_mat[[4]] <- (enrichList[[sample[9]]]+enrichList[[sample[10]]])/2
names(Marker_mat) <- combined_sample

for (j in 1:length(gene_cat)) {
  
  enrich_class <- gene_cat[j]
  Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
  
  colnames(Marker_avg) <-combined_sample
  rownames(Marker_avg) <- c(1:101)
  
  for (i in 1:length(combined_sample)) {
    Marker_avg[,i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-Marker_avg
  
  for (i in 1:ncol(Marker_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-2,method = 'mean')
  }
  
  Marker_avg <- avg_smooth
  
  Marker_data<-list()
  for (i in 1:ncol(Marker_avg)) {
    Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
    Marker_data[[i]]$group<-colnames(Marker_avg)[i]
  }
  
  data <- Marker_data[[1]]
  for (i in 2:length(Marker_data)) {
    data <- rbind(data,Marker_data[[i]])
  }
  
  data$group <- factor(data$group,levels = combined_sample)
  max(data$y)
  min(data$y)
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(Marker_data)
  color <-getPalette(colourCount_1)
  color <- c("#990000", "#009900","black","darkgray")
  
  plot_list[[j]] <- ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(y_min[[z]],y_max[[z]]),breaks = seq(y_min[[z]],y_max[[z]],round((y_max[[z]]-y_min[[z]])/5,digits = 0)))+    #scale_y_continuous(name = ylab,limits = c(-1,0.5,breaks = seq(-1,0.5,0.2)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = t)+
    theme(plot.title = element_text(size = 11, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          #legend.position = c(0.7,1),
          legend.position = 'none',
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
}

p3 <- wrap_plots(plot_list,ncol = 1)

##2.4 H3K36me2 signals----
plot_list <- list()
z=4
sample <- group_list[[z]]
t <- unique(str_split_fixed(sample,'_',3)[,2])
combined_sample <- unique(paste(str_split_fixed(sample,'_',3)[,1],str_split_fixed(sample,'_',3)[,2],sep = "_"))
Marker_mat <- list()
if (length(sample) > 2) {
  Marker_mat[[1]] <- (enrichList[[sample[1]]]+enrichList[[sample[2]]]+enrichList[[sample[3]]])/3
  Marker_mat[[2]] <- (enrichList[[sample[4]]]+enrichList[[sample[5]]]+enrichList[[sample[6]]])/3
} else {
  Marker_mat[[1]] <- enrichList[[sample[1]]]
  Marker_mat[[2]] <- enrichList[[sample[2]]]
}
#Marker_mat[[3]] <- (enrichList[[sample[7]]]+enrichList[[sample[8]]])/2
#Marker_mat[[4]] <- (enrichList[[sample[9]]]+enrichList[[sample[10]]])/2
names(Marker_mat) <- combined_sample

for (j in 1:length(gene_cat)) {
  
  enrich_class <- gene_cat[j]
  Marker_avg <- matrix(ncol = length(Marker_mat),nrow = 101)
  
  colnames(Marker_avg) <-combined_sample
  rownames(Marker_avg) <- c(1:101)
  
  for (i in 1:length(combined_sample)) {
    Marker_avg[,i] <- colMeans(Marker_mat[[i]][gene_class[[enrich_class]],])
  }
  
  avg_smooth<-Marker_avg
  
  for (i in 1:ncol(Marker_avg)) {
    avg_smooth[,i]<-smoothvec(as.vector(Marker_avg[,i]),1e-2,method = 'mean')
  }
  
  Marker_avg <- avg_smooth
  
  Marker_data<-list()
  for (i in 1:ncol(Marker_avg)) {
    Marker_data[[i]]<-data.frame(x=c(1:101),y=Marker_avg[,i])
    Marker_data[[i]]$group<-colnames(Marker_avg)[i]
  }
  
  data <- Marker_data[[1]]
  for (i in 2:length(Marker_data)) {
    data <- rbind(data,Marker_data[[i]])
  }
  
  data$group <- factor(data$group,levels = combined_sample)
  max(data$y)
  min(data$y)
  
  getPalette = colorRampPalette(brewer.pal(8, "Set1"))
  colourCount_1 = length(Marker_data)
  color <-getPalette(colourCount_1)
  color <- c("#990000", "#009900","black","darkgray")
  
  plot_list[[j]] <- ggplot(data, mapping=aes(x=x, y=y,group=group)) + 
    geom_line(aes(color=group),size=1.2)+scale_color_manual(values=color)+
    theme_bw() + 
    theme(panel.border = element_blank(),
          panel.grid = element_blank())+
    theme(axis.line = element_line(size=1, colour = "black"))+
    scale_x_continuous(name = "",labels = c('-3kb','TSS','TES','+3kb'),breaks = c(0,20,80,100))+
    scale_y_continuous(name = ylab,limits = c(y_min[[z]],y_max[[z]]),breaks = seq(y_min[[z]],y_max[[z]],round((y_max[[z]]-y_min[[z]])/5,digits = 0)))+   
    #scale_y_continuous(name = ylab,limits = c(-1,0.5,breaks = seq(-1,0.5,0.2)))+
    theme(axis.text.x = element_text(size = 10, color = "black"))+
    theme(axis.text.y = element_text(size = 10, color = "black"))+
    theme(axis.title.y = element_text(size = 10, color = "black"))+
    labs(title = t)+
    theme(plot.title = element_text(size = 11, color = "black",hjust = 0.5))+
    theme(legend.text = element_text(size = 10, color = "black"))+
    theme(legend.title = element_text(size = 10, color = "black"))+
    theme(panel.grid=element_blank(),
          #legend.position = c(0.7,1),
          legend.position = 'none',
          legend.justification = c(0,1),
          legend.spacing.y = unit(0.1,'cm'),
          legend.key.height = unit(0.1,'cm'))+
    theme(legend.text = element_text(size = 8, color = "black"))+
    theme(legend.title=element_blank())+
    geom_vline(xintercept=c(20,80), linetype="dashed",size=0.8)+
    theme(plot.margin = margin(0, 0, 0, 0, "cm"))
}

wrap_plots(plot_list,ncol = 1)
p4 <- wrap_plots(plot_list,ncol = 1)

p1|p2|p3|p4
