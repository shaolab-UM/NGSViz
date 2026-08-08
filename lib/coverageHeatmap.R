
# theme -------------------------------------------------------------------
myTheme <- function(plot_paras){
  face_item <- plot_paras$text_face 
  # theme
  custom_theme <- theme_bw() +
    theme(
      text = element_text(family = plot_paras$text_family, colour="black"),
      # size and color of panel.border
      panel.border = element_rect(linewidth = plot_paras$border_size, 
                                  colour = "black"),
      panel.grid = element_blank(),
      axis.text.x = element_text(size = plot_paras$axis_text_x_size, face = face_item),
      axis.text.y = element_text(size = plot_paras$axis_text_y_size, face = face_item),
      axis.title=element_text(size = plot_paras$title_size, face = face_item),
      #axis.title.x = element_blank(), 
      legend.title=element_text(size = 10, face = face_item), 
      legend.text = element_text(size = 10, face = face_item),
      legend.key=element_rect(colour = "white"),
      plot.title = element_text(size = plot_paras$title_size, hjust = 0.5, face = face_item),
      # face
      axis.ticks.x = element_blank(),
      # Remove faceted title background
      strip.background = element_blank(), 
      strip.text.x = element_text(size = 12, face = face_item)
    )
  return(custom_theme)
}

heatmapTheme <- function(){
  custom_theme <- theme(
    # Adjust the margin of the graph
    plot.margin = margin(t = 0, r = .2, b = .2, l = .2, unit = "cm"),
    # Remove Y-axis scale text
    axis.text.y = element_blank(),
    axis.ticks.y = element_blank(), # Remove the Y-axis scale lines
    # Place the faceted titles externally
    strip.placement = "outside", 
    # Adjust the spacing between horizontal facets
    panel.spacing.x = unit(0.6, "cm"), 
  )
  return(custom_theme)
}

# build species reference genome based on the gtf file
checkRequiredPackages <- function(required_packages) {
  if (!requireNamespace("pak", quietly = TRUE)) {
    install.packages(
      "pak",
      repos = sprintf(
        "https://r-lib.github.io/p/pak/stable/%s/%s/%s",
        .Platform$pkgType, R.Version()$os, R.Version()$arch
      )
    )
  }
  # Loop through each package to check and load
  for (pkg in required_packages) {
    # Check if the package is installed
    if (!requireNamespace(pkg, quietly = TRUE)) {
      message(paste0("Package '", pkg, "' is not installed, attempting to install..."))
      # Attempt to install the package
      tryCatch({
        #install.packages(pkg, dependencies = TRUE)
        pak::pkg_install(pkg)
        message(paste0("Package '", pkg, "' installed successfully."))
      }, error = function(e) {
        # If installation fails, print error message and stop the script
        stop(paste0("Error: Unable to install package '", pkg, "'. Please install this package manually and then run the script.\n",
                    "Error message: ", e$message))
      })
    }
    
    # Attempt to load the package
    # Use suppressPackageStartupMessages to avoid printing package startup messages
    # Use tryCatch to capture potential errors during loading
    tryCatch({
      suppressPackageStartupMessages(library(pkg, character.only = TRUE))
      message(paste0("Package '", pkg, "' loaded successfully."))
    }, error = function(e) {
      # If loading fails, print error message and stop the script
      stop(paste0("Error: Unable to load package '", pkg, "'. Please check if the installation is complete or if there are compatibility issues.\n",
                  "Error message: ", e$message))
    })
  }
  
  message("\nAll required packages have been installed and loaded.")
}

# process the row order of each gene including split situation ------------
processRowOrder <- function(df_long){
  # 1. Calculate mean_coverage for each Gene within each Split
  df_mean_per_gene_in_split <- df_long %>%
    group_by(Split, Gene) %>%
    mutate(mean_coverage_row = mean(Coverage))
  
  # 2. Calculate the sum of these mean_coverages for each Split
  #    and then sort according to the specified criteria.
  sorted_df <- df_mean_per_gene_in_split %>%
    group_by(Split) %>% # Group by Split to calculate sum within each Split
    mutate(mean_coverage_Split = mean(mean_coverage_row)) %>%
    ungroup() %>% # Ungroup before arranging
    arrange(
      # Sort by sum of means (descending)
      desc(mean_coverage_Split), 
      # Then by individual gene mean_coverage_row (ascending)
      Split, 
      # Then by Split name (ascending, for tie-breaking sums)
      mean_coverage_row                      
    )
  # Retrieve the order of Gene in sorted_df
  gene_order <- unique(sorted_df$Gene) 
  sorted_df$Gene <- factor(sorted_df$Gene, levels = gene_order)
  # adjust the split order
  Split_order <- unique(sorted_df$Split) 
  sorted_df$Split <- factor(sorted_df$Split, levels = Split_order)
  return(sorted_df)
}

# to generate the axis label
getXAxisCoor <- function(plot_paras){
  interval_type <- plot_paras$interval_type
  num_middle_point <- plot_paras$middle_datapoints
  num_flank_point <- plot_paras$flanking_region_datapoints
  flank_size <- plot_paras$flank_size
  num_datapoints <- plot_paras$num_datapoints+1
  region_labels <- plot_paras$region_labels
  # get x-axis label
  axis_name <- c(paste0("-", flank_size), region_labels, flank_size)
  # get x-axis position
  if(interval_type == "broad_interval"){
    axis_pos <- c(1, (num_flank_point+1), 
                  (num_datapoints-num_flank_point), num_datapoints)
  }else{
    # point mode
    axis_pos <- c(1, (num_flank_point+1), num_datapoints)
  }
  names(axis_pos) <- axis_name
  return(axis_pos)
}

matrix2LongDf <- function(data_mat){
  #
  df_wide <- as.data.frame(data_mat)
  # Step 2: Move the row names (gene names) of the data frame to a new column
  df_wide <- df_wide |> rownames_to_column("Gene")
  # Step 3: Use pivot_longer to convert the position column to long format
  df_long <- df_wide |>
    pivot_longer(
      cols = -Gene,           
      names_to = "Position", 
      values_to = "Coverage"    
    )
  df_long["XAsis"] <- sub("X", "", df_long$Position) |> as.numeric()
  if(min(df_long$XAsis, na.rm = TRUE) == 0){
    df_long$XAsis <- df_long$XAsis+1
  }
  return(df_long)
}

# getSubGenelist <- function(data_mat, plot_paras){
#   gene_list <- read.csv(plot_paras$gene_list_file)
# }
# split -------------------------------------------------------------------
getSplitLabel <- function(data_mat, plot_paras){
  hc_m <- plot_paras$hc_m
  dist_m <- plot_paras$dist_m
  cluster_num <- plot_paras$cluster_num
  # for multiple subgeneset, i.e up/down-regulated genes
  if(plot_paras$split_mode == "cluster"){
     df_long <- setHCluster(data_mat, cluster_num, hc_m, dist_m)
  }else{
    # sub gene list
    # # transform the matrix into the long data
    # df_long <- matrix2LongDf(data_mat)
  }
  # order the row of the heatmap based on the mean coverage
  sorted_df <- processRowOrder(df_long)
  return(sorted_df)
}

setHCluster <- function(data_mat, cluster_num, hc_m, dist_m){
  # transform the matrix into the long data
  df_long <- matrix2LongDf(data_mat)
  df_long["Split"] <- "C1"
  if(cluster_num != 1){
    hc <- hclust(dist(data_mat, method = dist_m), method = hc_m)
    hc_result <- cutree(hc, k = cluster_num)
    
    
    # give the cluster label
    for (i in 1:cluster_num) {
      choose_genes <- names(hc_result)[hc_result == i]
      df_long$Split[df_long$Gene %in% choose_genes] <- paste0("C", as.character(i))
    }
  }
  return(df_long)
}


# heatmap -----------------------------------------------------------------

coverageHeatmap <- function(sorted_df, plot_paras, color_limits = NULL){
  bg_col <- plot_paras$sample_color
  axis_pos <- getXAxisCoor(plot_paras)
  axis_name <- names(axis_pos)
  line_pos <- axis_pos[-c(1, length(axis_pos))]
  split_num <- length(unique(sorted_df$Split))
  
  if(is.null(color_limits)){
    lower_limit <- quantile(sorted_df$Coverage, 0.01, na.rm = TRUE)
    upper_limit <- quantile(sorted_df$Coverage, 0.95, na.rm = TRUE) 
    
    # If the lower limit is less than 0 and you want 0 to be white background, you can force the lower limit to be symmetric or set it to 0.
    #lower_limit <- min(lower_limit, 0) 
    
    color_limits <- c(lower_limit, upper_limit)
  }
  
  if (abs(color_limits[2] - color_limits[1]) < 1e-9) { 
    color_limits[2] <- color_limits[2] + 1e-6 
  }
  
  pbom <- ggplot(sorted_df) +
    geom_raster(aes(x = XAsis, y = Gene, fill = Coverage)) + 
    
    # Using gradient2, force 0 to be white
    scale_fill_gradient2(
      low = "grey90",           # Negative value color (or set to white to ignore negative values)
      mid = "white",            # Forced background (0) is white.
      high = colorspace::darken(bg_col, 0.3), # Make the highest signal color deeper than the original.
      midpoint = 0,             # Force white at position 0
      limits = color_limits,
      oob = scales::squish      # Values exceeding the limits will be compressed to extreme colors to prevent extreme values from overwhelming the tonal scale.
    ) +
    
    myTheme(plot_paras) +
    xlab("") + ylab("") +
    scale_x_continuous(breaks = axis_pos, labels = axis_name) +
    geom_vline(xintercept = line_pos, linetype = "dashed",
               color = plot_paras$dash_color, linewidth = plot_paras$dash_size) +
    coord_cartesian(expand = 0) + 
    theme(axis.title.x = element_blank()) +
    heatmapTheme()
  
  if (split_num > 1) {
    mycol <- plot_paras$split_color[1:split_num]
    pbom <- pbom +
      facet_grid2(Split ~ ., switch = "y", scales = "free_y", space = "free_y",
                  strip = strip_themed(background_y = elem_list_rect(fill = mycol),
                                       text_y = element_text(
                                         size = plot_paras$ystrip_text_size, 
                                         face = plot_paras$text_face,
                                         margin = margin(t = 2, r = 2, b = 2, l = 2, unit = "pt") 
                                       )
                  )
      ) +
      theme(panel.spacing.y = unit(0.1, "lines"), strip.placement = "outside")
  }
  return(pbom)
}



averageCovPlot <- function(sorted_df, plot_paras, ylim = NULL){
  plot_title <- plot_paras$plot_title
  mat_df <- sorted_df %>%
    group_by(Split, XAsis) %>%
    summarise(
      Density = mean(Coverage, na.rm = TRUE),
      SEM = sd(Coverage, na.rm = TRUE) / sqrt(n()),
      .groups = 'drop'
    )
  axis_pos <- getXAxisCoor(plot_paras)
  line_pos <- axis_pos[-c(1, length(axis_pos))]
  print(line_pos)
  split_num <- length(unique(sorted_df$Split))
  
  if (is.null(ylim)) {
    ylim <- c(0, max(mat_df$Density, na.rm = TRUE) * 1.1)
  }
  
  ptop <- ggplot(mat_df) +
    geom_line(aes(x = XAsis, y = Density, color = Split), linewidth = plot_paras$line_size) + 
    scale_color_manual(values = plot_paras$split_color[1:split_num]) +
    xlab("") + ylab("") +
    myTheme(plot_paras) + 
    theme(
      axis.text.x = element_blank(), 
      axis.title.x = element_blank(),
      plot.margin = margin(t = 0.2, r = .2, b = 0, l = .2, unit = "cm") 
    ) +
    geom_vline(xintercept = line_pos, linetype = "dashed", 
               color = plot_paras$dash_color, linewidth = plot_paras$dash_size) +
    labs(title = plot_title) + 
    coord_cartesian(ylim = ylim, expand = FALSE)
  
  if(split_num == 1){
    ptop <- ptop + theme(legend.position = "none")
  }
  if(plot_paras$SEM){
    ptop <- ptop + geom_ribbon(aes(x = XAsis, ymin = Density - SEM, ymax = Density + SEM, fill = Split), alpha = 0.3)
  }
  return(ptop)
}

mergeMultiVisResult <- function(json_path, out_path = ""){
  plot_para_list <- getJsonFiles(json_path)
  
  # ---- 1. Get global Coverage and maximum/minimum values for the Y-axis ----
  all_df_list <- list()
  for(i in 1:length(plot_para_list)){
    plot_paras_path <- file.path(json_path, plot_para_list[[i]])
    plot_paras <- getPlotJsonParas(plot_paras_path)
    all_df_list[[i]] <- preparePlotData(plot_paras)
  }
  
  # Unify the color range for all heatmaps
  global_all_coverage <- unlist(lapply(all_df_list, function(df) df$Coverage))
  global_lower <- quantile(global_all_coverage, 0, na.rm = TRUE)
  global_upper <- quantile(global_all_coverage, 0.95, na.rm = TRUE)
  if (abs(global_upper - global_lower) < 1e-9) global_upper <- global_upper + 1e-6
  
  # Unify the Y-axis limits for the top curve plots
  max_densities <- sapply(all_df_list, function(df) {
    df %>% group_by(XAsis) %>% summarise(m = mean(Coverage, na.rm=TRUE)) %>% pull(m) %>% max(na.rm=TRUE)
  })
  global_ylim <- c(0, max(max_densities) * 1.1)
  
  # ---- 2. Loop through and generate plots ----
  ht_list <- list()
  for(i in 1:length(plot_para_list)){
    plot_paras_path <- file.path(json_path, plot_para_list[[i]])
    plot_paras <- getPlotJsonParas(plot_paras_path)
    sorted_df <- all_df_list[[i]]
    
    pbom <- coverageHeatmap(sorted_df, plot_paras, color_limits = c(global_lower, global_upper))
    ptop <- averageCovPlot(sorted_df, plot_paras, ylim = global_ylim)
    
    p_merge <- aplot::plot_list(gglist = list(ptop, pbom), ncol = 1, heights = c(1, 3))
    ht_list[[i]] <- p_merge
  }
  
  # ---- 3. Combine plots horizontally ----
  all_plots <- cowplot::plot_grid(plotlist = ht_list, nrow = 1, align = "h", axis = "tb")
  
  if(out_path == ""){ out_path <- json_path }
  file_name <- sprintf("%s/coverage_heatmap.%s", out_path, plot_paras$file_type)
  item_num <- length(plot_para_list)
  ggsave(file_name, plot = all_plots, dpi = plot_paras$dpi, 
         width = plot_paras$width * item_num, height = plot_paras$high, units = "cm")
  
  return(all_plots)
}


# "moving_average", "gaussian", "loess"
smooth_moving_average_pad <- function(x, window_size = 5) {
  pad_size <- floor(window_size / 2)
  x_padded <- c(rep(x[1], pad_size), x, rep(x[length(x)], pad_size))
  smoothed <- zoo::rollmean(x_padded, k = window_size, align = "center")
  return(smoothed)
}

smooth <- function(data_mat, window_size = 5) {
  smooth_mat <- t(apply(data_mat, 1, function(x) {
    smooth_moving_average_pad(x, window_size = 5)
  }))
  return(smooth_mat)
}

preparePlotData <- function(plot_paras){
  heatmap_files <- plot_paras$heatmap_mat_path
  out_path <- plot_paras$output_path
  if (!file.exists(heatmap_files)) {
    error_message <- paste0("Error: Specified file path '", 
                            heatmap_files, 
                            "' not exist. The script will terminate.")
    stop(error_message)
  }else{
    data_mat <- read.csv(heatmap_files, row.names = 1) %>% as.matrix()
  }
  
  # filter the data_mat based on the subset gene list
  # subset gene list without header
  gene_file <- plot_paras$sub_gene_list_file
  if (!is.null(gene_file) && !is.na(gene_file) && 
      nchar(trimws(gene_file)) > 0 && file.exists(gene_file)) {
    # Read gene list file (assuming one gene per line)
    sub_gene_list <- read.table(gene_file, header = FALSE, stringsAsFactors = FALSE)
    target_genes <- sub_gene_list[[1]]
    # Format GeneName (stripping transcript version if needed)
    gene_names_in_mat <- sub(":.*$", "", rownames(data_mat))
    # Filter sorted_df based on the gene list
    keep_idx <- gene_names_in_mat %in% target_genes
    data_mat <- data_mat[keep_idx, , drop = FALSE]
  }
  
  # smooth
  if(plot_paras$smooth){
    data_mat <- smooth(data_mat)
  }
  
  # order the row of the heatmap based on the mean coverage
  sorted_df <- getSplitLabel(data_mat, plot_paras)
  return(sorted_df)
}



# Retrieve all JSON filenames under the specified path
getJsonFiles <- function(path) {
  json_files <- list.files(path, pattern = "\\.json$", full.names = FALSE)
  return(as.list(json_files))
}

# Multiple results are plotted on one graph.
averageCovPlotMultiMerge <- function(merge_df, plot_paras, title_name = ""){
  # calculate the average coverage for each point
  axis_pos <- getXAxisCoor(plot_paras)
  axis_name <- names(axis_pos)
  # positon of vertical reference line
  line_pos <- axis_pos[-c(1, length(axis_pos))]
  print(line_pos)
  group_num <- length(unique(merge_df$Group))
  # plot the average plot
  ptop <- ggplot(merge_df) +
    geom_line(aes(x = XAsis, y = Density, color = Group, group = Group), linewidth = plot_paras$line_size) + 
    # Specified color
    # scale_color_brewer(palette = "Set1") + 
    scale_color_manual(values = plot_paras$split_color) +
    coord_cartesian(ylim = c(0, max(merge_df$Density)*1.1))+
    xlab("") + ylab("") +
    # add the axis-x label
    scale_x_continuous(breaks = axis_pos, labels = axis_name) +
    myTheme(plot_paras) + 
    theme(
      # Adjust the margin of the graph, setting the bottom margin to a negative value helps with alignment.
      plot.margin = margin(t = 0.2, r = .2, b = 0, l = .2, unit = "cm"),
      axis.title.x = element_blank()
    ) +
    geom_vline(xintercept = line_pos, linetype = "dashed", 
               color = plot_paras$dash_color, linewidth = plot_paras$dash_size) +
    # By sample dimension, because only one sample is selected here, so there is only one dimension.
    # facet_wrap(~sample, nrow = 1) + 
    labs(title = title_name) + 
    # Remove the gap between the heatmap edge and the coordinate axis.
    coord_cartesian(expand = 1) 
  if(group_num == 1){
    ptop <- ptop + theme(legend.position = "none")
  }
  # if(plot_paras$SEM){
  #   # Add error lines
  #   ptop <- ptop + geom_ribbon(aes(x = XAsis, ymin = Density - SEM, ymax = Density + SEM), alpha = 0.3)
  # }
  return(ptop)
}



mergeAveragePlot <- function(json_path, subgenes_list = NULL, title_name = ""){
  # get the json file name list
  plot_para_list <- getJsonFiles(json_path)
  merge_df <- data.frame()
  for(i in 1:length(plot_para_list)){
    plot_paras_name <- plot_para_list[[i]]
    plot_paras_path <- file.path(json_path, plot_paras_name)
    plot_paras <- getPlotJsonParas(plot_paras_path)
    sorted_df <- preparePlotData(plot_paras)
    # update subset
    sorted_df$GeneName <- ifelse(
      grepl(":", sorted_df$Gene),
      sub(":.*$", "", sorted_df$Gene),
      sorted_df$Gene
    )
    
    if(length(subgenes_list) > 0){
      sorted_df <- sorted_df[sorted_df$GeneName %in% subgenes_list, ]
    }
    
    mat_df <- sorted_df %>%
      group_by(Split, XAsis) %>%
      summarise(
        Density = mean(Coverage, na.rm = TRUE),
        SEM = sd(Coverage, na.rm = TRUE) / sqrt(n()),
        .groups = 'drop'
      )
    mat_df["Group"] <- plot_paras$plot_title
    if(nrow(merge_df)==0){
      merge_df <- mat_df
    }else{
      merge_df <- rbind(merge_df, mat_df)
    }
  }
  ave_plot <- averageCovPlotMultiMerge(merge_df, plot_paras, title_name)
  file_name <- sprintf("%s/merge_average_plot.%s", json_path, plot_paras$file_type)
  ggsave(file_name, plot = ave_plot, dpi = plot_paras$dpi, 
         width = plot_paras$width, height = plot_paras$high*0.7, units = "cm")
  return(ave_plot)
}

coeragePlotMian <- function(json_path, title_name){
  mergeAveragePlot(json_path)
  mergeMultiVisResult(json_path)
}

plotOneIntegratePlot <- function(plot_paras){
  heatmap_files <- plot_paras$heatmap_mat_path
  out_path <- plot_paras$output_path
  ht_list <- list()
  for (i in 1:length(heatmap_files)) {
    if (!file.exists(heatmap_files[i])) {
      error_message <- paste0("Error: Specified file path '", 
                              heatmap_files[i], 
                              "' not exist. The script will terminate.")
      stop(error_message)
    }else{
      data_mat <- read.csv(heatmap_files[i], row.names = 1) %>% as.matrix()
    }
    
    # smooth
    data_mat <- smooth(data_mat, window_size = 5)
    
    # order the row of the heatmap based on the mean coverage
    sorted_df <- getSplitLabel(data_mat, plot_paras)
    
    # plot
    bg_col <- plot_paras$sample_color[i]
    pbom <- coverageHeatmap(sorted_df, plot_paras)
    ptop <- averageCovPlot(sorted_df, plot_paras)
    pmer <- aplot::plot_list(gglist = list(ptop, pbom), ncol = 1, heights = c(1, 3))
    ht_list[[i]] <- pmer
  }
  all_plots <- cowplot::plot_grid(plotlist = ht_list, nrow = 1)
  file_name <- sprintf("%s/coverage_heatmap.%s", out_path, plot_paras$file_type)
  ggsave(file_name, plot = all_plots, dpi = plot_paras$dpi, 
         width = plot_paras$width, height = plot_paras$high, units = "cm")
  return(all_plots)
}
# usage -------------------------------------------------------------------
packages <- c(
  "ggh4x", "tidyverse", "colorspace", "circlize",
  "RColorBrewer", "purrr", "data.table", "RcppRoll", "zoo"
)

# Load packages only for direct legacy sourcing. Native CLI validates first.
if (!isTRUE(getOption("ngsviz.skip_dependency_loading"))) {
  checkRequiredPackages(packages)
}


