

avgCovPlot <- function(cov_df,plot_paras){
  custom_theme <- setPlotTheme(plot_paras)
  legend_theme <- setPlotLegend(plot_paras)
  # lab
  lab_item <- plot_paras[["region_labels"]]
  print(lab_item)
  flank_size <- as.character(floor(plot_paras[["flank_size"]]/1000))
  str(flank_size)
  up_item <- paste0("-", flank_size, "kb")
  down_item <- paste0("+", flank_size, "kb")
  labels_items <- c(up_item, toupper(lab_item), down_item)
  print(labels_items)
  # middle points
  flank_point <- plot_paras[["flank_point"]]
  str(flank_point)
  middle_point <- plot_paras[["middle_point"]]
  if(length(lab_item) == 1){
    middle_loc <- middle_point + flank_point
  }else{
    middle_loc <- c((0+flank_point), (middle_point + flank_point))
  }
  breaks_loc <- c(0, middle_loc, 101)
  #
  p <- ggplot(cov_df, aes(x = Position, y = AverageCoverage, group = group)) +
    geom_line(aes(color = group), size = plot_paras[["line_size"]]) + 
    scale_fill_manual(values = plot_paras[["selected_colors"]]) +
    scale_x_continuous(name = "", labels = labels_items, breaks = breaks_loc) + 
    # scale_y_continuous(name = ylab,limits = c(y_min[[z]],y_max[[z]]),
    #                    breaks = seq(y_min[[z]],y_max[[z]],
    #                                 round((y_max[[z]]-y_min[[z]])/5,digits = 0)))+ 
    geom_vline(xintercept = middle_loc, linetype = "dashed",
               size = plot_paras[["dash_size"]], color = plot_paras[["dash_color"]]) + 
    labs(title = plot_paras[["plot_title"]]) +
    custom_theme + legend_theme
  return(p)
}

