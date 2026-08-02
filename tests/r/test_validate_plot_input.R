write_plot_fixture <- function(
    directory, sample_name = "sample-1", values = c(0, 1, 2),
    coverage_name = "coverage.csv", setting_name = "sample_NGSViz_plotSetting.json",
    middle_datapoints = 1L, flanking_region_datapoints = 1L,
    include_coverage = TRUE) {
  coverage_path <- file.path(directory, coverage_name)
  if (include_coverage) {
    header <- paste(c("gene", paste0("X", seq_along(values) - 1L)), collapse = ",")
    row <- paste(c("gene-1", as.character(values)), collapse = ",")
    writeLines(c(header, row), coverage_path)
  }

  setting <- list(
    versionParameters = list(version = "1.0"),
    output_paras = list(
      top_bottom_ratio = c(1, 3), high = 12, file_type = "tiff",
      width = 12, output_path = directory, dpi = 300
    ),
    OutputFile = list(list(
      heatmapDataFile = coverage_path,
      readCountFile = file.path(directory, "read-count.csv")
    )),
    split_paras = list(
      hc_m = "ward.D", gene_list_file = "", split_mode = "cluster",
      cluster_num = 1L, sub_gene_list = "all", dist_m = "euclidean"
    ),
    plot_parameters = list(
      flank_size = 1000, CenterMode = FALSE, num_datapoints = length(values) - 1L,
      region_labels = "TSS", SEM = TRUE, plot_title = sample_name,
      middle_datapoints = middle_datapoints,
      flanking_region_datapoints = flanking_region_datapoints,
      sample_list = sample_name, interval_type = "point_interval", smooth = TRUE
    ),
    theme_paras = list(
      title_size = 11, legend_size = 10, title_color = "black",
      dash_color = "black", axis_text_x_size = 10, text_family = "sans",
      axis_text_y_size = 10, dash_size = 0.6, sample_color = "#66C2A5",
      text_face = "bold", split_color = "#BC3C29FF", title_hjust = 0.5,
      border_size = 1.2, legend_text_size = 10, line_size = 0.8,
      ystrip_text_size = 8
    ),
    run_metadata = list(
      sample_id = sample_name,
      sample_name = sample_name,
      group_name = "group-1"
    )
  )
  jsonlite::write_json(
    setting, file.path(directory, setting_name), auto_unbox = TRUE, pretty = TRUE
  )
  invisible(file.path(directory, setting_name))
}

with_fixture <- function(code) {
  directory <- tempfile("ngsviz-r-test-")
  dir.create(directory)
  on.exit(unlink(directory, recursive = TRUE), add = TRUE)
  force(code)(directory)
}

has_issue <- function(result, code, collection = "errors") {
  any(vapply(result[[collection]], function(issue) identical(issue$code, code), logical(1)))
}

test_validate_plot_input <- function(project_root) {
  source(file.path(project_root, "lib", "cli", "cli_response.R"))
  source(file.path(project_root, "lib", "cli", "validate_plot_input.R"))

  with_fixture(function(directory) {
    write_plot_fixture(directory)
    result <- validate_plot_input(directory)
    stopifnot(result$valid, identical(result$plot_setting_count, 1L))
  })

  with_fixture(function(directory) {
    result <- validate_plot_input(directory)
    stopifnot(!result$valid, has_issue(result, "PLOT_SETTING_NOT_FOUND"))
  })

  with_fixture(function(directory) {
    write_plot_fixture(directory, include_coverage = FALSE)
    result <- validate_plot_input(directory)
    stopifnot(!result$valid, identical(result$exit_code, 4L))
    stopifnot(has_issue(result, "COVERAGE_FILE_MISSING"))
  })

  for (non_finite in c("NaN", "Inf")) {
    with_fixture(function(directory) {
      write_plot_fixture(directory, values = c("0", non_finite, "2"))
      result <- validate_plot_input(directory)
      stopifnot(!result$valid, has_issue(result, "COVERAGE_NON_FINITE"))
    })
  }

  with_fixture(function(directory) {
    write_plot_fixture(directory, values = c(0, 0, 0))
    result <- validate_plot_input(directory)
    stopifnot(result$valid, has_issue(result, "COVERAGE_ALL_ZERO", "warnings"))
  })

  with_fixture(function(directory) {
    write_plot_fixture(
      directory, values = c(0, 1, 2, 3),
      middle_datapoints = 1L, flanking_region_datapoints = 1L
    )
    result <- validate_plot_input(directory)
    stopifnot(!result$valid, has_issue(result, "COVERAGE_COLUMN_COUNT_MISMATCH"))
  })

  with_fixture(function(directory) {
    write_plot_fixture(directory, sample_name = "sample-1")
    write_plot_fixture(
      directory, sample_name = "sample-2", coverage_name = "coverage-2.csv",
      setting_name = "sample-2_NGSViz_plotSetting.json"
    )
    result <- validate_plot_input(directory)
    stopifnot(result$valid, identical(result$plot_setting_count, 2L))
  })

  with_fixture(function(directory) {
    write_plot_fixture(directory, sample_name = "duplicate")
    write_plot_fixture(
      directory, sample_name = "duplicate", coverage_name = "coverage-2.csv",
      setting_name = "sample-2_NGSViz_plotSetting.json"
    )
    result <- validate_plot_input(directory)
    stopifnot(!result$valid, has_issue(result, "SAMPLE_NAME_DUPLICATE"))
  })

  with_fixture(function(directory) {
    write_plot_fixture(directory)
    path <- file.path(directory, "sample_NGSViz_plotSetting.json")
    setting <- jsonlite::read_json(path, simplifyVector = FALSE)
    setting$OutputFile <- NULL
    jsonlite::write_json(setting, path, auto_unbox = TRUE)
    result <- validate_plot_input(directory)
    stopifnot(!result$valid, has_issue(result, "PLOT_SETTING_FIELD_MISSING"))
  })
}
