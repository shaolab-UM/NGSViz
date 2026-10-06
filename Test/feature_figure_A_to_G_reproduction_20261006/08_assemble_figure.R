suppressPackageStartupMessages(library(magick))
root <- normalizePath("Test/feature_figure_A_to_G_reproduction_20261006")
out <- file.path(root, "figure")
dir.create(out, showWarnings = FALSE)
vis <- file.path(root, "visualization")
paths <- c(file.path(vis, "A/coverage_heatmap.tiff"),
           file.path(vis, "B/coverage_heatmap.tiff"),
           file.path(vis, "C/coverage_heatmap.tiff"),
           file.path(vis, "D/coverage_heatmap.tiff"),
           file.path(vis, "EFG/merge_average_plot.tiff"),
           file.path(vis, "EFG/correlation_plot.tiff"),
           file.path(vis, "EFG/PCA_plot.tiff"))
stopifnot(all(file.exists(paths)))
layout <- data.frame(x = c(20,1220,2420,20,20,1220,2420),
                     y = c(60,60,60,1510,3000,3000,3000),
                     width = c(1160,1160,1160,3560,1160,1160,1160),
                     height = c(1392,1392,1392,1424,980,980,980))
canvas <- image_blank(3600, 4000, "white")
for (i in seq_along(paths)) {
  box <- sprintf("%dx%d", layout$width[i], layout$height[i])
  panel <- image_extent(image_resize(image_read(paths[i]), box), box,
                        gravity = "center", color = "white")
  canvas <- image_composite(canvas, panel, offset = sprintf("+%d+%d", layout$x[i], layout$y[i]))
  canvas <- image_annotate(canvas, LETTERS[i], size = 52, font = "Arial", weight = 700,
                           location = sprintf("+%d+%d", layout$x[i], layout$y[i]-50), color = "black")
}
image_write(canvas, file.path(out, "feature_figure_A_to_G.tiff"), density = "300x300", compression = "lzw")
image_write(canvas, file.path(out, "feature_figure_A_to_G.png"), density = "300x300")
image_write(canvas, file.path(out, "feature_figure_A_to_G.pdf"), density = "300x300")
