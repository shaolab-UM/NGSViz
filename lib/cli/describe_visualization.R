visualization_description <- function() {
  list(
    version = "1.3",
    commands = c("describe", "validate-plot", "run-plot"),
    inputs = list(
      describe = list(format = "json"),
      validate_plot = list(input = "directory", format = "json"),
      run_plot = list(input = "directory", format = "json")
    ),
    exit_codes = list(
      success = 0L,
      usage_error = 2L,
      environment_error = 3L,
      input_error = 4L,
      execution_error = 5L,
      output_error = 6L
    )
  )
}

describe_visualization <- function() {
  build_cli_response(
    "describe", "success", 0L, "Visualization CLI contract described.",
    visualization_description()
  )
}
