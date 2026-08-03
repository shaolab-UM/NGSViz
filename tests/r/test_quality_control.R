test_quality_control <- function(project_root) {
  previous_skip <- getOption("ngsviz.skip_dependency_loading")
  options(ngsviz.skip_dependency_loading = TRUE)
  on.exit(options(ngsviz.skip_dependency_loading = previous_skip), add = TRUE)
  source(file.path(project_root, "lib", "qualityControlViz.R"), local = TRUE)

  coordinates <- matrix(c(-1, 1), ncol = 1L)
  rownames(coordinates) <- c("sample-1", "sample-2")
  prepared <- preparePcaPlotData(coordinates, 100)

  stopifnot(
    identical(colnames(prepared$data), c("PC1", "PC2")),
    identical(prepared$data$PC1, c(-1, 1)),
    identical(prepared$data$PC2, c(0, 0)),
    identical(prepared$variance, c(100, 0))
  )
}
