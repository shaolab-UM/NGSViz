visualization_function_names <- function() {
  c("mergeAveragePlot", "mergeMultiVisResult", "propressData", "corPlot", "runPCA")
}

load_visualization_functions <- function() {
  target <- environment(run_plot)
  functions_ready <- vapply(
    visualization_function_names(), exists, logical(1), envir = target,
    mode = "function", inherits = TRUE
  )
  if (all(functions_ready)) return(invisible(NULL))
  library_dir <- getOption("ngsviz.library_dir")
  if (is.null(library_dir)) stop("NGSViz R library directory is not configured.")
  previous_skip <- getOption("ngsviz.skip_dependency_loading")
  options(ngsviz.skip_dependency_loading = TRUE)
  on.exit(options(ngsviz.skip_dependency_loading = previous_skip), add = TRUE)
  source(file.path(library_dir, "processJSON.R"), local = target)
  source(file.path(library_dir, "coverageHeatmap.R"), local = target)
  source(file.path(library_dir, "qualityControlViz.R"), local = target)
  dependencies <- getOption("ngsviz.visualization_packages", character())
  for (package in dependencies) {
    suppressPackageStartupMessages(library(package, character.only = TRUE))
  }
  invisible(NULL)
}

expected_visualization_outputs <- function(input_dir, setting_files) {
  setting <- jsonlite::read_json(setting_files[[1L]], simplifyVector = FALSE)
  file_type <- setting$output_paras$file_type
  outputs <- list(
    list(kind = "average_profile", path = file.path(
      input_dir, paste0("merge_average_plot.", file_type)
    )),
    list(kind = "coverage_heatmap", path = file.path(
      input_dir, paste0("coverage_heatmap.", file_type)
    ))
  )
  if (length(setting_files) > 1L) {
    outputs <- c(outputs, list(
      list(kind = "correlation", path = file.path(input_dir, "correlation_plot.tiff")),
      list(kind = "pca", path = file.path(input_dir, paste0("PCA_plot.", file_type)))
    ))
  }
  outputs
}

snapshot_output <- function(path) {
  info <- file.info(path)
  if (!file.exists(path) || isTRUE(info$isdir)) {
    return(list(exists = FALSE, size = NA_real_, mtime = as.POSIXct(NA)))
  }
  list(exists = TRUE, size = info$size, mtime = info$mtime)
}

inspect_visualization_outputs <- function(outputs, before) {
  Map(function(output, previous) {
    current <- snapshot_output(output$path)
    changed <- current$exists && (
      !previous$exists || !identical(current$size, previous$size) ||
        isTRUE(current$mtime > previous$mtime)
    )
    status <- if (!current$exists) {
      "missing"
    } else if (current$size <= 0) {
      "empty"
    } else if (changed) {
      "created"
    } else {
      "failed"
    }
    list(
      kind = output$kind,
      path = normalizePath(output$path, mustWork = FALSE),
      status = status,
      size_bytes = if (current$exists) as.integer(current$size) else NULL
    )
  }, outputs, before)
}

write_run_manifest <- function(
    input_dir, outputs, started_at, status, inputs, log_path, warnings, errors) {
  finished_at <- Sys.time()
  manifest <- build_visualization_manifest(
    input_dir, outputs, started_at, finished_at, status,
    inputs$plot_settings, inputs$coverage_files, log_path, warnings, errors
  )
  manifest_path <- file.path(input_dir, "visualization_manifest.json")
  write_visualization_manifest(manifest, manifest_path)
  normalizePath(manifest_path, mustWork = FALSE)
}

run_plot <- function(input_dir) {
  started_at <- Sys.time()
  input_path <- normalizePath(input_dir, mustWork = FALSE)
  log_path <- file.path(input_path, "NGSViz_visualization.log")
  if (!dir.exists(input_path) || file.access(input_path, 2L) != 0L) {
    issue <- new_cli_issue(
      "INPUT_DIRECTORY_NOT_WRITABLE", input_path, "input_dir",
      "Input directory does not exist or is not writable."
    )
    return(build_cli_response(
      "run-plot", "error", 2L, "Visualization input validation failed.",
      list(input_dir = input_path, manifest = NULL), errors = list(issue)
    ))
  }

  log_connection <- file(log_path, open = "at")
  sink(log_connection, type = "output")
  sink(log_connection, type = "message")
  on.exit({
    sink(type = "message")
    sink(type = "output")
    close(log_connection)
  }, add = TRUE)
  message("Starting NGSViz visualization run.")

  validation <- validate_plot_input(input_path)
  empty_inputs <- list(plot_settings = list(), coverage_files = list())
  if (!validation$valid) {
    manifest_path <- write_run_manifest(
      input_path, list(), started_at, "failed", empty_inputs, log_path,
      validation$warnings, validation$errors
    )
    return(build_cli_response(
      "run-plot", "error", validation$exit_code,
      "Visualization input validation failed.",
      list(
        input_dir = input_path,
        plot_setting_count = validation$plot_setting_count,
        manifest = manifest_path
      ), validation$warnings, validation$errors
    ))
  }

  inputs <- visualization_input_records(validation$plot_setting_files)
  expected <- expected_visualization_outputs(input_path, validation$plot_setting_files)
  before <- lapply(expected, function(output) snapshot_output(output$path))
  plot_warnings <- list()
  plot_error <- tryCatch(withCallingHandlers({
      load_visualization_functions()
      validated_files <- basename(validation$plot_setting_files)
      assign("getJsonFiles", function(path) as.list(validated_files),
             envir = environment(run_plot))
      mergeAveragePlot(input_path)
      mergeMultiVisResult(input_path)
      merged_matrix <- propressData(input_path)
      if (validation$plot_setting_count > 1L) {
        corPlot(input_path, merged_matrix)
        runPCA(merged_matrix, input_path)
      }
      NULL
    }, warning = function(warning) {
      issue <- new_cli_issue(
        "PLOT_WARNING", NULL, NULL, conditionMessage(warning)
      )
      plot_warnings <<- c(plot_warnings, list(issue))
      message("Plot warning: ", conditionMessage(warning))
      invokeRestart("muffleWarning")
    }), error = identity)
  outputs <- inspect_visualization_outputs(expected, before)
  run_warnings <- c(validation$warnings, plot_warnings)

  if (inherits(plot_error, "error")) {
    issue <- new_cli_issue(
      "PLOT_EXECUTION_FAILED", NULL, NULL,
      paste0("Visualization plotting failed: ", conditionMessage(plot_error))
    )
    errors <- c(validation$errors, list(issue))
    manifest_path <- write_run_manifest(
      input_path, outputs, started_at, "failed", inputs, log_path,
      run_warnings, errors
    )
    return(build_cli_response(
      "run-plot", "error", 5L, "Visualization plotting failed.",
      list(input_dir = input_path, manifest = manifest_path),
      run_warnings, errors
    ))
  }

  invalid_outputs <- Filter(function(output) output$status != "created", outputs)
  if (length(invalid_outputs) > 0L) {
    errors <- lapply(invalid_outputs, function(output) new_cli_issue(
      "VISUALIZATION_OUTPUT_INVALID", output$path, NULL,
      paste0("Expected visualization image is ", output$status, ".")
    ))
    manifest_path <- write_run_manifest(
      input_path, outputs, started_at, "failed", inputs, log_path,
      run_warnings, errors
    )
    return(build_cli_response(
      "run-plot", "error", 6L, "Visualization output validation failed.",
      list(input_dir = input_path, manifest = manifest_path),
      run_warnings, errors
    ))
  }

  manifest_path <- write_run_manifest(
    input_path, outputs, started_at, "completed", inputs, log_path,
    run_warnings, list()
  )
  message("NGSViz visualization run completed.")
  build_cli_response(
    "run-plot", "success", 0L, "Visualization completed.",
    list(
      input_dir = input_path,
      plot_setting_count = validation$plot_setting_count,
      manifest = manifest_path
    ), run_warnings
  )
}
