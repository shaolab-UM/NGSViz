source_run_plot_test_environment <- function(project_root) {
  environment <- new.env(parent = globalenv())
  source(file.path(project_root, "lib", "cli", "cli_response.R"), local = environment)
  source(file.path(project_root, "lib", "cli", "visualization_manifest.R"), local = environment)
  source(file.path(project_root, "lib", "cli", "run_plot.R"), local = environment)
  environment
}

mock_validation <- function(setting_path, valid = TRUE, exit_code = 0L) {
  list(
    valid = valid,
    exit_code = exit_code,
    plot_setting_count = if (valid) 1L else 0L,
    plot_setting_files = if (valid) setting_path else character(),
    warnings = list(),
    errors = if (valid) list() else list(list(
      code = "TEST_VALIDATION_FAILED", file = NULL, field = NULL,
      message = "Validation failed for test."
    ))
  )
}

install_plot_mocks <- function(environment, directory, behavior = "success") {
  environment$mergeAveragePlot <- function(input_dir) {
    if (behavior == "error") stop("mock plotting failure")
    if (behavior == "success") writeLines("average", file.path(
      input_dir, "merge_average_plot.tiff"
    ))
  }
  environment$mergeMultiVisResult <- function(input_dir) {
    if (behavior == "success") writeLines("heatmap", file.path(
      input_dir, "coverage_heatmap.tiff"
    ))
  }
  environment$propressData <- function(input_dir) matrix(1, nrow = 1L)
  environment$corPlot <- function(input_dir, merged_matrix) invisible(NULL)
  environment$runPCA <- function(merged_matrix, input_dir) invisible(NULL)
}

test_run_plot <- function(project_root) {
  with_fixture(function(directory) {
    environment <- source_run_plot_test_environment(project_root)
    calls <- 0L
    environment$validate_plot_input <- function(input_dir) {
      mock_validation(character(), valid = FALSE, exit_code = 4L)
    }
    for (name in c(
      "mergeAveragePlot", "mergeMultiVisResult", "propressData", "corPlot", "runPCA"
    )) {
      environment[[name]] <- function(...) {
        calls <<- calls + 1L
        stop("plot function must not be called")
      }
    }
    result <- environment$run_plot(directory)
    stopifnot(identical(result$exit_code, 4L), identical(calls, 0L))
  })

  with_fixture(function(directory) {
    setting_path <- write_plot_fixture(directory)
    environment <- source_run_plot_test_environment(project_root)
    environment$validate_plot_input <- function(input_dir) mock_validation(setting_path)
    install_plot_mocks(environment, directory, behavior = "error")
    result <- environment$run_plot(directory)
    stopifnot(identical(result$exit_code, 5L))
  })

  with_fixture(function(directory) {
    setting_path <- write_plot_fixture(directory)
    environment <- source_run_plot_test_environment(project_root)
    environment$validate_plot_input <- function(input_dir) mock_validation(setting_path)
    install_plot_mocks(environment, directory, behavior = "missing")
    result <- environment$run_plot(directory)
    stopifnot(identical(result$exit_code, 6L))
  })
}
