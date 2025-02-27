

avgCovPlot <- function(sample_df, plot_paras){
  avg_cov_name <- paste(sample_df$sampleName[1],
                        sample_df$tittle[1],
                        "average_coverage_matrix.csv", sep = "__")
  avg_cov_file <- file.path(plot_paras[["out_dir"]], avg_cov_name)
  cov_df <- read.csv(avg_cov_file)
  cov_df <- as.data.frame(cov_df)
  cov_df['Position'] <- 1:dim(cov_df)[1]
  #
  p <- ggplot(cov_df, aes(x = Position, y = AverageCoverage)) +
    geom_line() +
    labs(title = "Coverage Line Plot", x = "Position", y = "AverageCoverage") +
    theme_minimal()
  return(p)
}