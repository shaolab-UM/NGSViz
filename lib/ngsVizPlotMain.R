#!/usr/bin/env Rscript

# Get the absolute path of the current script
getScriptPath <- function() {
  cmd_args <- commandArgs(trailingOnly = FALSE)
  # Find the --file= argument, which is passed when running with Rscript
  file_arg <- grep("--file=", cmd_args, value = TRUE)
  if (length(file_arg) > 0) {
    # Extract the file path and convert to absolute path
    script_path <- normalizePath(sub("--file=", "", file_arg))
  } else {
    # If running interactively in R, try using sys.frame
    script_path <- normalizePath(sys.frame(1)$ofile)
  }
  return(script_path)
}


# usage -------------------------------------------------------------------
# json_path <- "/Users/benche/myProj/ngsPlot/Output/Test/H3K4"
args <- commandArgs(trailingOnly = TRUE)

dispatchNativeCli <- function(args) {
  script_dir <- dirname(getScriptPath())
  cli_dir <- file.path(script_dir, "cli")
  source(file.path(cli_dir, "cli_response.R"))
  source(file.path(cli_dir, "cli_parser.R"))
  source(file.path(cli_dir, "describe_visualization.R"))
  source(file.path(cli_dir, "check_visualization_environment.R"))
  source(file.path(cli_dir, "validate_plot_input.R"))
  source(file.path(cli_dir, "visualization_manifest.R"))
  source(file.path(cli_dir, "run_plot.R"))
  options(ngsviz.library_dir = script_dir)

  parsed <- tryCatch(parse_cli_args(args), cli_argument_error = identity)
  if (inherits(parsed, "cli_argument_error")) {
    response <- build_cli_response(
      parsed$command, "error", 2L, parsed$message, errors = list(new_cli_issue(
        "CLI_ARGUMENT_ERROR", field = NULL, message = parsed$message
      ))
    )
  } else if (parsed$command == "describe") {
    response <- describe_visualization()
  } else if (parsed$command == "run-plot") {
    environment <- check_visualization_environment()
    options(ngsviz.visualization_packages = environment$required_packages)
    response <- if (environment$valid) {
      run_plot(parsed$input_dir)
    } else {
      build_validation_response("run-plot", parsed$input_dir, environment)
    }
  } else {
    environment <- check_visualization_environment()
    validation <- if (environment$valid) validate_plot_input(parsed$input_dir) else NULL
    response <- build_validation_response(
      parsed$command, parsed$input_dir, environment, validation
    )
  }
  emit_cli_response(response)
  quit(status = response$exit_code)
}

dispatchNativeCli(args)
