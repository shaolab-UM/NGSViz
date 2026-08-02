test_visualization_manifest <- function(project_root) {
  with_fixture(function(directory) {
    setting_path <- write_plot_fixture(directory)
    environment <- source_run_plot_test_environment(project_root)
    environment$validate_plot_input <- function(input_dir) mock_validation(setting_path)
    install_plot_mocks(environment, directory, behavior = "success")

    result <- environment$run_plot(directory)
    manifest_path <- file.path(directory, "visualization_manifest.json")
    manifest <- jsonlite::read_json(manifest_path, simplifyVector = FALSE)
    required <- c(
      "schema_version", "status", "r_version", "started_at", "finished_at",
      "duration_ms", "input_plot_settings", "input_coverage_files",
      "output_images", "log_file", "warnings", "errors"
    )
    stopifnot(
      identical(result$exit_code, 0L),
      all(required %in% names(manifest)),
      file.exists(manifest$log_file),
      length(manifest$output_images) == 2L,
      all(vapply(manifest$output_images, function(output) {
        identical(output$status, "created") && output$size_bytes > 0L
      }, logical(1)))
    )
    forbidden <- c("bam", "database", "compute_status")
    all_names <- unique(unlist(lapply(manifest, names), use.names = FALSE))
    stopifnot(!any(tolower(all_names) %in% forbidden))
  })
}
