args <- commandArgs(trailingOnly = TRUE)
panel <- args[[1]]
stopifnot(panel %in% c("A", "B", "C", "D", "EFG"))
root <- normalizePath("Test/feature_figure_A_to_G_reproduction_20261006")
input <- file.path(root, "visualization", panel)
cli <- "/Users/benche/Desktop/myProj/ngsPlot/NGSViz/lib/ngsVizPlotMain.R"
stopifnot(system2("/usr/local/bin/Rscript", c(cli, "validate-plot", "--input-dir", input)) == 0)
stopifnot(system2("/usr/local/bin/Rscript", c(cli, "run-plot", "--input-dir", input)) == 0)
