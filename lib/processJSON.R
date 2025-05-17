
# process the calculationMode result json
getCalcuJsonRes <- function(json_file_path){
  json_data <- jsonlite::fromJSON(json_file_path)
  # file_names <- basename(json_data$OutputFile$heatmapDataFile) 
  # df <- data.frame(file_path = file_names, stringsAsFactors = FALSE)
  # sample_df <- df %>%
  #   separate(file_path, into = c("sampleName", "group", "suffix"), sep = "__")
  # uniq_sample <- sample_df$sampleName[!duplicated(sample_df)]
  plot_paras <- list(
                     "middle_point" = json_data$plot_parameters$middle_datapoints,
                     "flank_point" = json_data$plot_parameters$flanking_region_datapoints,
                     "sample_list" = json_data$plot_parameters$sample_list,
                     "plot_title" = json_data$plot_parameters$plot_title,
                     "num_datapoints"=json_data$plot_parameters$num_datapoints+1,
                     "sub_gene_list"=json_data$basis_parameters$gene_list,
                     "interval_type"=json_data$coverage_calculator$interval_type,
                     "region_labels" = json_data$coverage_calculator$region_labels,
                     "flank_size" = json_data$coverage_calculator$flank_size,
                     "output_name"=json_data$mandatory_parameters$output_name
                  )
  # res <- list(
  #   "uniq_sample" = uniq_sample,
  #   "sample_df" = sample_df,
  #   "plot_paras" = plot_paras
  # )
  return(plot_paras)
}

# generate the java running environment
getToolJsonPara <- function(json_file){
  # Read and parse the JSON file
  json_data <- jsonlite::fromJSON(json_file)
  # Access specific fields
  version <- json_data$versionNum$version
  tool_path <- json_data$toolParas$tool_path
  db_path <- json_data$toolParas$db_path
  tool_name <- json_data$toolParas$tool_name
  java_path <- json_data$toolParas$java_path
  jar_path <- file.path(tool_path, tool_name)
  if (!file.exists(jar_path)) {
    stop("Error: The path of NGSViz java excution file is not correct: \n", jar_path)
  }
  if (!file.exists(db_path)) {
    stop("Error: The database path of NGSViz is not correct: \n", db_path)
  }
  args <- c("-jar", jar_path)
  res <- c(java_path, args, db_path)
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
# generateJavaCommand <- function(paras){
#   args <- c("-G", paras$dashG, "-R", paras$dashR, "-I",paras$dashI,
#             "-O", paras$dashO, "-X", paras$dashX, "-T", paras$dashT,
#             "-A", paras$dashA, "-B", paras$dashB, "-F", paras$dashF,
#             "-N", paras$dashN, "-RB", paras$dashRB, "-S", paras$dashS,
#             "-M", paras$dashM, "-CZ", paras$dashCZ, "-MQ", paras$dashMQ,
#             "-FL", paras$dashFL, "-SS", paras$dashSS, "-P", paras$dashP,
#             "-D", paras$dashD)
#   # args <- c("-G", paras$dashG, "-R", paras$dashR, "-I",paras$dashI, 
#   #           "-O", paras$dashO)
#   #print(paste0("Input the java parameters is: ", cmd_text))
#   return(args)
# }

# Parse drawing JSON parameters and return
getPlotJsonParas <- function(plot_para_file){
  parsed_data <- jsonlite::fromJSON(plot_para_file)
  # file_names <- basename(parsed_data$OutputFile$heatmapDataFile) 
  # df <- data.frame(file_path = file_names, stringsAsFactors = FALSE)
  # sample_df <- df %>%
  #   separate(file_path, into = c("sampleName", "group", "suffix"), sep = "__")
  # uniq_sample <- sample_df$sampleName[!duplicated(sample_df)]
  # build the plot_paras list
  plot_paras <- list(
    # input data path
    heatmap_mat_path = parsed_data$OutputFile$heatmapDataFile,
    # split_paras
    cluster_num = parsed_data$split_paras$cluster_num,
    hc_m = parsed_data$split_paras$hc_m,
    dist_m = parsed_data$split_paras$dist_m,
    sub_gene_list = parsed_data$split_paras$sub_gene_list,
    sub_gene_list_file = parsed_data$split_paras$gene_list_file,
    split_mode = parsed_data$split_paras$split_mode,
    
    # plot_parameters
    flank_size = parsed_data$plot_parameters$flank_size,
    num_datapoints = parsed_data$plot_parameters$num_datapoints,
    region_labels = parsed_data$plot_parameters$region_labels,
    plot_title = parsed_data$plot_parameters$plot_title,
    middle_datapoints = parsed_data$plot_parameters$middle_datapoints,
    flanking_region_datapoints = parsed_data$plot_parameters$flanking_region_datapoints,
    sample_list = parsed_data$plot_parameters$sample_list,
    interval_type = parsed_data$plot_parameters$interval_type,
    
    # output_paras
    output_path = parsed_data$output_paras$output_path,
    high = parsed_data$output_paras$high,
    width = parsed_data$output_paras$width,
    dpi = parsed_data$output_paras$dpi,
    top_bottom_ratio = parsed_data$output_paras$top_bottom_ratio,
    file_type = parsed_data$output_paras$file_type,
    
    # theme_paras
    text_family = parsed_data$theme_paras$text_family,
    text_face = parsed_data$theme_paras$text_face,
    title_hjust = parsed_data$theme_paras$title_hjust,
    # size
    title_size = parsed_data$theme_paras$title_size,
    border_size = parsed_data$theme_paras$border_size,
    axis_text_x_size = parsed_data$theme_paras$axis_text_x_size,
    axis_text_y_size = parsed_data$theme_paras$axis_text_y_size,
    legend_text_size = parsed_data$theme_paras$legend_text_size,
    ystrip_text_size = parsed_data$theme_paras$ystrip_text_size,
    legend_size = parsed_data$theme_paras$legend_size,
    line_size = parsed_data$theme_paras$line_size,
    dash_color = parsed_data$theme_paras$dash_color,
    # color
    title_color = parsed_data$theme_paras$title_color,
    border_color = parsed_data$theme_paras$border_color,
    sample_color = parsed_data$theme_paras$sample_color,
    split_color = parsed_data$theme_paras$split_color
  )
  plot_paras$sample_color <- plot_paras$sample_color[1:length(plot_paras$sample_list)]
  if(plot_paras$sub_gene_list[1] == "all"){
    plot_paras$split_mode <- "cluster"
  }else{
    plot_paras$split_mode <- "sub_gene_list"
  }
  return(plot_paras)
}
