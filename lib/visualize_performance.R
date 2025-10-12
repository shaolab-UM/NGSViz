
library(ggplot2)
library(dplyr)
library(readr)

# Load the data
df <- read_csv("performance_summary.csv")

# Convert Sample to numeric
df$Sample <- as.numeric(df$Sample)

# Separate single-threaded and multi-threaded data
df_single <- df %>% filter(Threads == "p1")
df_multi <- df %>% filter(Threads == "p8")

# Function to plot and save
plot_metric <- function(data, metric, filename) {
  p <- ggplot(data, aes(x = Sample, y = .data[[metric]], color = Tool)) +
    geom_point() +
    geom_line() +
    facet_wrap(~Tool, scales = "free_y") +
    labs(title = paste(metric, "vs Sample Size"), x = "Sample Size", y = metric) +
    theme_minimal()
  ggsave(filename, plot = p, width = 8, height = 5)
}

# Plot for single-threaded
plot_metric(df_single, "ElapsedTime_s", "ElapsedTime_single.png")
plot_metric(df_single, "MaxMemory_KB", "MaxMemory_single.png")
plot_metric(df_single, "CPUUtilization", "CPUUtilization_single.png")

# Plot for multi-threaded
plot_metric(df_multi, "ElapsedTime_s", "ElapsedTime_multi.png")
plot_metric(df_multi, "MaxMemory_KB", "MaxMemory_multi.png")
plot_metric(df_multi, "CPUUtilization", "CPUUtilization_multi.png")
