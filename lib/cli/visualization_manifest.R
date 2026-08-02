format_visualization_time <- function(value) {
  format(value, "%Y-%m-%dT%H:%M:%OS3Z", tz = "UTC")
}

visualization_input_records <- function(plot_setting_files) {
  settings <- lapply(plot_setting_files, function(path) {
    parsed <- jsonlite::read_json(path, simplifyVector = FALSE)
    list(
      path = normalizePath(path, mustWork = FALSE),
      title = parsed$plot_parameters$plot_title
    )
  })
  coverage <- lapply(plot_setting_files, function(path) {
    parsed <- jsonlite::read_json(path, simplifyVector = FALSE)
    coverage_path <- parsed$OutputFile[[1L]]$heatmapDataFile
    list(path = normalizePath(coverage_path, mustWork = FALSE))
  })
  list(plot_settings = settings, coverage_files = coverage)
}

build_visualization_manifest <- function(
    input_dir, outputs, started_at, finished_at, status = "completed",
    input_plot_settings = list(), input_coverage_files = list(),
    log_file = file.path(input_dir, "NGSViz_visualization.log"),
    warnings = list(), errors = list()) {
  duration <- max(0, as.numeric(difftime(
    finished_at, started_at, units = "secs"
  )) * 1000)
  list(
    schema_version = "1.0",
    status = status,
    r_version = R.version.string,
    started_at = format_visualization_time(started_at),
    finished_at = format_visualization_time(finished_at),
    duration_ms = as.integer(round(duration)),
    input_plot_settings = unname(input_plot_settings),
    input_coverage_files = unname(input_coverage_files),
    output_images = unname(outputs),
    log_file = normalizePath(log_file, mustWork = FALSE),
    warnings = unname(warnings),
    errors = unname(errors)
  )
}

write_visualization_manifest <- function(manifest, path) {
  temporary_path <- tempfile(
    pattern = ".visualization_manifest-", tmpdir = dirname(path), fileext = ".json"
  )
  on.exit(unlink(temporary_path), add = TRUE)
  jsonlite::write_json(
    manifest, temporary_path, auto_unbox = TRUE, null = "null", na = "null",
    pretty = TRUE, digits = NA
  )
  if (!file.rename(temporary_path, path)) {
    stop("Unable to atomically write visualization manifest.")
  }
  invisible(normalizePath(path, mustWork = FALSE))
}
