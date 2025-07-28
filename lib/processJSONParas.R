# Create a custom theme
setPlotTheme <- function(plot_paras){
  custom_theme <- theme_bw() +
    theme(
      # size and color of panel.border
      panel.border = element_rect(size = plot_paras[["border_size"]],
                                  colour = plot_paras[["border_color"]]),
      panel.grid = element_blank(),
      #axis.line = element_line(size = 1, colour = "black"),
      axis.text.x = element_text(size = plot_paras[["axis_text_x_size"]],
                                 color = plot_paras[["axis_text_x_color"]]),
      axis.text.y = element_text(size = plot_paras[["axis_text_y_size"]],
                                 color = plot_paras[["axis_text_y_color"]]),
      axis.title.y = element_blank(),
      plot.title = element_text(size = plot_paras[["title_size"]], 
                                color = plot_paras[["title_color"]], 
                                hjust = plot_paras[["title_hjust"]]),
      plot.margin = margin(0, 0, 0, 0, "cm")
    )
  return(custom_theme)
}

setPlotLegend <- function(plot_paras){
  legend_theme <- theme(
    legend.text = element_text(size = plot_paras[["legend_text_size"]],
                               color = plot_paras[["legend_text_color"]]),
    legend.title = element_blank()
  )
  return(legend_theme)
}

