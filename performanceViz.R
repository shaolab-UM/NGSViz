
library(stringr)
library(dplyr)

# 设置根目录
root_dir <- "/Users/benche/myProj/ngsPlot/Result/performanceCompare"

# 初始化结果列表
results <- list()

# 遍历工具文件夹
tools <- c("ngsplot", "deeptools", "ngsViz")
threads <- c("p1", "p8") #
runs <- c("run_1", "run_2", "run_3")
samples <- c("0.1", "0.25", "0.5", "0.75", "1.0")

for (tool in tools) {
  for (thread in threads) {
    for (run in runs) {
      for (sample in samples) {
        log_path <- file.path(root_dir, tool, paste0(thread, "_", run), sample, paste0(sample, "_performance_log.txt"))
        if (file.exists(log_path)) {
          lines <- readLines(log_path)
          
          # 找到所有 Run Start 和 Run End 的位置
          start_indices <- grep("=== Run Start:", lines)
          end_indices <- grep("=== Run End ===", lines)
          
          if (length(start_indices) > 0 && length(end_indices) > 0) {
            # 取最后一次运行的块
            start <- tail(start_indices, 1)
            end <- end_indices[end_indices > start][1]
            block <- lines[start:end]
            
            # 提取性能数据
            get_value <- function(pattern) {
              line <- block[str_detect(block, pattern)]
              if (length(line) == 0) return(NA)
              str_trim(str_split_fixed(line, ":", 2)[,2])
            }
            
            elapsed <- get_value("Elapsed Real Time")
            user_cpu <- get_value("User CPU Time")
            system_cpu <- get_value("System CPU Time")
            cpu_util <- get_value("CPU Utilization")
            max_mem <- get_value("Max Resident Memory")
            
            # 添加到结果列表
            results[[length(results) + 1]] <- data.frame(
              Tool = tool,
              Threads = thread,
              Run = run,
              Sample = sample,
              ElapsedTime_s = as.numeric(elapsed),
              UserCPU_s = as.numeric(user_cpu),
              SystemCPU_s = as.numeric(system_cpu),
              CPUUtilization = cpu_util,
              MaxMemory_KB = as.numeric(max_mem),
              stringsAsFactors = FALSE
            )
          }
        }
      }
    }
  }
}

# 合并所有结果并保存为 CSV
final_df <- bind_rows(results)
setwd("/Users/benche/myProj/ngsPlot/Result/")
write.csv(final_df, "performance_summary.csv", row.names = FALSE)

cat("✅ 性能数据已保存为 performance_summary.csv\n")
