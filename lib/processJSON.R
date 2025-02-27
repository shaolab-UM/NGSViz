
# process the calculationMode result json
getCalcuJsonRes <- function(json_file_path){
  json_data <- fromJSON(json_file_path)
  file_names <- basename(json_data$OutputFile$heatmapDataFile) 
  df <- data.frame(file_path = file_names, stringsAsFactors = FALSE)
  sample_df <- df %>%
    separate(file_path, into = c("sampleName", "tittle", "suffix"), sep = "__")
  uniq_sample <- sample_df$sampleName[!duplicated(sample_df)]
  plot_paras <- list("region_labels" = json_data$coverage_calculator$region_labels,
                     "flank_size" = json_data$coverage_calculator$flank_size,
                     "middle_point" = json_data$plot_parameters$middle_datapoints,
                     "flank_point" = json_data$plot_parameters$flanking_region_datapoints
  )
  res <- list(
    "uniq_sample" = uniq_sample,
    "sample_df" = sample_df,
    "plot_paras" = plot_paras
  )
  return(res)
}

# generate the java running environment
getToolJsonPara <- function(json_file){
  # Read and parse the JSON file
  json_data <- fromJSON(json_file)
  # Access specific fields
  version <- json_data$versionNum$version
  tool_path <- json_data$toolParas$tool_path
  db_path <- json_data$toolParas$db_path
  tool_name <- json_data$toolParas$tool_name
  java_path <- json_data$toolParas$java_path
  jar_path <- file.path(tool_path, tool_name)
  args <- c("-jar", jar_path)
  res <- c(java_path, args)
  return(res)
}

# getJavaParameter <- function(paras){
#   cmd_text <- sprintf("-G %s -R %s -I %s -O %s -X %s -T %s -A %s -B %s -F %d -N %d -RB %d -S %d -M %s -CZ %d -MQ %d -FL %d -SS %s -P %d -D %s",
#                       paras$dashG, paras$dashR,
#                       paras$dashI, paras$dashO, paras$dashX, paras$dashT,
#                       paras$dashA, paras$dashB, paras$dashF, paras$dashN,
#                       paras$dashRB, paras$dashS, paras$dashM, paras$dashCZ,
#                       paras$dashMQ, paras$dashFL, paras$dashSS, paras$dashP,
#                       paras$dashD)
#   print(paste0("Input the java parameters is: ", cmd_text))
#   return(cmd_text)
# }
# generate the java parameter args
generateJavaCommand <- function(paras){
  args <- c("-G", paras$dashG, "-R", paras$dashR, "-I",paras$dashI,
            "-O", paras$dashO, "-X", paras$dashX, "-T", paras$dashT,
            "-A", paras$dashA, "-B", paras$dashB, "-F", paras$dashF,
            "-N", paras$dashN, "-RB", paras$dashRB, "-S", paras$dashS,
            "-M", paras$dashM, "-CZ", paras$dashCZ, "-MQ", paras$dashMQ,
            "-FL", paras$dashFL, "-SS", paras$dashSS, "-P", paras$dashP,
            "-D", paras$dashD)
  # args <- c("-G", paras$dashG, "-R", paras$dashR, "-I",paras$dashI, 
  #           "-O", paras$dashO)
  #print(paste0("Input the java parameters is: ", cmd_text))
  return(args)
}