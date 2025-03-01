
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

