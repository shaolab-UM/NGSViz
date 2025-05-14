
mainPlot <- function(sample_df, plot_paras){
  avg_cov_name <- paste(sample_df$sampleName[1],
                        sample_df$group[1],
                        "average_coverage_matrix.csv", sep = "__")
  print(avg_cov_name)
  avg_cov_file <- file.path(plot_paras[["out_dir"]], avg_cov_name)
  cov_df <- read.csv(avg_cov_file)
  cov_df <- as.data.frame(cov_df)
  cov_df['Position'] <- 1:dim(cov_df)[1]
  cov_df['group'] <- "group"
  p <- avgCovPlot(cov_df,plot_paras)
  return(p)
}

# ylab <- c('Enrich Density')

# ##0.1 perform normalization----
# SampleSheet <- readr::read_csv("F:/UMproject/LSD2_NSD3/CutRun_2/logs/SampleSheet.csv")
# 
# intersect(SampleSheet$Sample_ID,sample_id)
# 
# count_enrichList <- raw_enrichList
# 
# for (i in 1:nrow(SampleSheet)) {
#   sa <- SampleSheet$Sample_ID[i]
#   count_enrichList[[sa]] <- round(raw_enrichList[[sa]]*SampleSheet$total_reads[i]/1000000,digits = 0)
# }
# 
# for (i in 1:length(count_enrichList)) {
#   count_enrichList[[i]][count_enrichList[[i]] < 1 ] <- 1
# }
# 
# #Spike norm----
# adj_enrichList <- count_enrichList
# for (i in 1:nrow(SampleSheet)) {
#   sa <- SampleSheet$Sample_ID[i]
#   adj_enrichList[[sa]] <- count_enrichList[[sa]]/SampleSheet$spikein_reads[i]*10000
#   #adj_enrichList[[sa]] <- (count_enrichList[[sa]]/SampleSheet$total_reads[i]*1000000)*(10000/SampleSheet$spikein_reads[i])
# }

# #spkie & IgG norm-----
# IgG_spike_enrichList <- adj_enrichList
# names(adj_enrichList)
# for (i in grep('sgControl',names(adj_enrichList))) {
#   #IgG_spike_enrichList[[i]] <- round(log2((adj_enrichList[[i]]+1e-6)/(adj_enrichList[['sgControl_IgG_R2']]+1e-6)),digits = 3)
#   IgG_spike_enrichList[[i]] <- round(log2(adj_enrichList[[i]]/adj_enrichList[['sgControl_NC_R']]),digits = 3)
# }
# 
# for (i in grep('sgLSD2',names(adj_enrichList))) {
#   #IgG_spike_enrichList[[i]] <- round(log2((adj_enrichList[[i]]+1e-6)/(adj_enrichList[['sgControl_IgG_R2']]+1e-6)),digits = 3)
#   IgG_spike_enrichList[[i]] <- round(log2(adj_enrichList[[i]]/adj_enrichList[['sgLSD2_NC_R']]),digits = 3)
# }
# 
# #IgG nor----
# IgG_enrichList <- count_enrichList
# for (i in grep('sgControl',names(count_enrichList))) {
#   IgG_enrichList[[i]] <- log2((cpm(count_enrichList[[i]]))/(cpm(count_enrichList[['sgControl_NC_R']])))
# }
# 
# 
# for (i in grep('sgLSD2',names(count_enrichList))) {
#   IgG_enrichList[[i]] <- log2((cpm(count_enrichList[[i]]))/(cpm(count_enrichList[['sgLSD2_NC_R']])))
# }