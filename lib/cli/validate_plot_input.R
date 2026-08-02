plot_setting_schema <- function() {
  list(
    versionParameters = "version",
    output_paras = c(
      "top_bottom_ratio", "high", "file_type", "width", "output_path", "dpi"
    ),
    OutputFile = c("heatmapDataFile", "readCountFile"),
    split_paras = c(
      "hc_m", "gene_list_file", "split_mode", "cluster_num",
      "sub_gene_list", "dist_m"
    ),
    plot_parameters = c(
      "flank_size", "CenterMode", "num_datapoints", "region_labels", "SEM",
      "plot_title", "middle_datapoints", "flanking_region_datapoints",
      "sample_list", "interval_type", "smooth"
    ),
    theme_paras = c(
      "title_size", "legend_size", "title_color", "dash_color",
      "axis_text_x_size", "text_family", "axis_text_y_size", "dash_size",
      "sample_color", "text_face", "split_color", "title_hjust",
      "border_size", "legend_text_size", "line_size", "ystrip_text_size"
    )
  )
}

missing_field_issues <- function(setting, file) {
  issues <- list()
  schema <- plot_setting_schema()
  for (section in names(schema)) {
    value <- setting[[section]]
    if (is.null(value)) {
      issues[[length(issues) + 1L]] <- new_cli_issue(
        "PLOT_SETTING_FIELD_MISSING", file, section,
        paste0("Required plot-setting field is missing: ", section, ".")
      )
      next
    }
    object <- if (section == "OutputFile" && length(value) == 1L) value[[1]] else value
    missing <- setdiff(schema[[section]], names(object))
    for (field in missing) {
      path <- paste(section, field, sep = ".")
      issues[[length(issues) + 1L]] <- new_cli_issue(
        "PLOT_SETTING_FIELD_MISSING", file, path,
        paste0("Required plot-setting field is missing: ", path, ".")
      )
    }
  }
  issues
}

read_plot_setting <- function(path) {
  tryCatch(
    jsonlite::read_json(path, simplifyVector = FALSE),
    error = function(error) structure(
      list(message = conditionMessage(error)), class = "plot_setting_parse_error"
    )
  )
}

inspect_coverage_file <- function(path, expected_columns) {
  errors <- list()
  warnings <- list()
  if (!file.exists(path) || !isTRUE(file.info(path)$isdir == FALSE)) {
    errors[[1L]] <- new_cli_issue(
      "COVERAGE_FILE_MISSING", path, "OutputFile.heatmapDataFile",
      "Coverage CSV does not exist."
    )
    return(list(errors = errors, warnings = warnings, metadata = NULL))
  }
  if (file.info(path)$size <= 0L || file.access(path, 4L) != 0L) {
    errors[[1L]] <- new_cli_issue(
      "COVERAGE_FILE_UNREADABLE", path, "OutputFile.heatmapDataFile",
      "Coverage CSV is empty or unreadable."
    )
    return(list(errors = errors, warnings = warnings, metadata = NULL))
  }
  fields <- tryCatch(count.fields(path, sep = ",", quote = "\""), error = identity)
  if (inherits(fields, "error") || length(fields) < 2L || length(unique(fields)) != 1L) {
    errors[[1L]] <- new_cli_issue(
      "COVERAGE_COLUMN_INCONSISTENT", path, NULL,
      "Coverage CSV rows do not have a consistent column count."
    )
    return(list(errors = errors, warnings = warnings, metadata = NULL))
  }
  inspect_coverage_table(path, expected_columns, errors, warnings)
}

inspect_coverage_table <- function(path, expected_columns, errors, warnings) {
  table <- tryCatch(read.csv(
    path, header = TRUE, check.names = FALSE, stringsAsFactors = FALSE,
    colClasses = "character", na.strings = NULL
  ), error = identity)
  if (inherits(table, "error") || nrow(table) < 1L || ncol(table) < 2L) {
    errors[[length(errors) + 1L]] <- new_cli_issue(
      "COVERAGE_CSV_INVALID", path, NULL,
      "Coverage CSV must contain a header and at least one data row."
    )
    return(list(errors = errors, warnings = warnings, metadata = NULL))
  }
  coverage_columns <- ncol(table) - 1L
  if (coverage_columns != expected_columns) {
    errors[[length(errors) + 1L]] <- new_cli_issue(
      "COVERAGE_COLUMN_COUNT_MISMATCH", path, "plot_parameters",
      paste0("Coverage CSV has ", coverage_columns,
             " numeric columns; expected ", expected_columns, ".")
    )
  }
  ids <- table[[1L]]
  if (any(!nzchar(ids)) || anyDuplicated(ids)) {
    errors[[length(errors) + 1L]] <- new_cli_issue(
      "COVERAGE_ROW_ID_INVALID", path, NULL,
      "Coverage CSV row identifiers must be non-empty and unique."
    )
  }
  values <- suppressWarnings(as.numeric(unlist(table[-1L], use.names = FALSE)))
  if (anyNA(values) || any(!is.finite(values))) {
    errors[[length(errors) + 1L]] <- new_cli_issue(
      "COVERAGE_NON_FINITE", path, NULL,
      "Coverage matrix contains missing, non-numeric, NaN, or infinite values."
    )
  } else if (all(values == 0)) {
    warnings[[1L]] <- new_cli_issue(
      "COVERAGE_ALL_ZERO", path, NULL, "Coverage matrix contains only zero values."
    )
  }
  metadata <- list(row_count = nrow(table), row_ids = ids, column_count = coverage_columns)
  list(errors = errors, warnings = warnings, metadata = metadata)
}

valid_count <- function(value, minimum) {
  is.numeric(value) && length(value) == 1L && is.finite(value) &&
    value >= minimum && value == as.integer(value)
}

validate_setting <- function(path) {
  setting <- read_plot_setting(path)
  if (inherits(setting, "plot_setting_parse_error")) {
    issue <- new_cli_issue(
      "PLOT_SETTING_JSON_INVALID", path, NULL, "Plot-setting JSON is invalid."
    )
    return(list(setting = NULL, errors = list(issue), warnings = list(), metadata = NULL))
  }
  errors <- missing_field_issues(setting, path)
  if (length(errors) > 0L) {
    return(list(setting = setting, errors = errors, warnings = list(), metadata = NULL))
  }
  output <- setting$OutputFile
  if (!is.list(output) || length(output) != 1L || !is.list(output[[1L]])) {
    issue <- new_cli_issue(
      "OUTPUT_FILE_INVALID", path, "OutputFile", "OutputFile must contain one object."
    )
    return(list(setting = setting, errors = list(issue), warnings = list(), metadata = NULL))
  }
  heatmap <- output[[1L]]$heatmapDataFile
  if (!is.character(heatmap) || length(heatmap) != 1L || !nzchar(heatmap)) {
    issue <- new_cli_issue(
      "OUTPUT_FILE_INVALID", path, "OutputFile.heatmapDataFile",
      "OutputFile.heatmapDataFile must be a non-empty string."
    )
    return(list(setting = setting, errors = list(issue), warnings = list(), metadata = NULL))
  }
  counts <- setting$plot_parameters
  if (!valid_count(counts$middle_datapoints, 1L) ||
      !valid_count(counts$flanking_region_datapoints, 0L)) {
    issue <- new_cli_issue(
      "PLOT_SETTING_VALUE_INVALID", path, "plot_parameters",
      "Plot datapoint counts must be non-negative integers with a positive middle count."
    )
    return(list(setting = setting, errors = list(issue), warnings = list(), metadata = NULL))
  }
  expected <- 2L * counts$flanking_region_datapoints + counts$middle_datapoints
  coverage <- inspect_coverage_file(heatmap, expected)
  list(
    setting = setting, errors = coverage$errors,
    warnings = coverage$warnings, metadata = coverage$metadata
  )
}

append_issues <- function(destination, additions) {
  c(destination, additions)
}

multiple_setting_issues <- function(results, paths) {
  issues <- list()
  sample_names <- vapply(results, function(result) {
    value <- result$setting$run_metadata$sample_name
    if (is.character(value) && length(value) == 1L && nzchar(value)) value else NA_character_
  }, character(1))
  for (index in which(is.na(sample_names))) {
    issues[[length(issues) + 1L]] <- new_cli_issue(
      "SAMPLE_NAME_MISSING", paths[index], "run_metadata.sample_name",
      "Multiple plot settings require a non-empty sample_name."
    )
  }
  duplicates <- duplicated(sample_names) | duplicated(sample_names, fromLast = TRUE)
  if (any(duplicates & !is.na(sample_names))) {
    issues[[length(issues) + 1L]] <- new_cli_issue(
      "SAMPLE_NAME_DUPLICATE", NULL, "run_metadata.sample_name",
      "Multiple plot settings require unique sample_name values."
    )
  }
  metadata <- lapply(results, `[[`, "metadata")
  usable <- Filter(Negate(is.null), metadata)
  if (length(usable) > 1L) {
    reference <- usable[[1L]]
    compatible <- vapply(usable[-1L], function(item) {
      identical(item$row_count, reference$row_count) &&
        identical(item$row_ids, reference$row_ids) &&
        identical(item$column_count, reference$column_count)
    }, logical(1))
    if (!all(compatible)) issues[[length(issues) + 1L]] <- new_cli_issue(
      "COVERAGE_MATRICES_INCOMPATIBLE", NULL, NULL,
      "Coverage matrices are not compatible for multi-sample plotting."
    )
  }
  issues
}

validate_plot_input <- function(input_dir) {
  if (!dir.exists(input_dir)) {
    issue <- new_cli_issue(
      "INPUT_DIRECTORY_MISSING", input_dir, "input_dir", "Input directory does not exist."
    )
    return(list(valid = FALSE, exit_code = 2L, plot_setting_count = 0L,
                warnings = list(), errors = list(issue)))
  }
  paths <- list.files(
    input_dir, pattern = "_NGSViz_plotSetting\\.json$", full.names = TRUE
  )
  paths <- sort(paths[file.info(paths)$isdir == FALSE], method = "radix")
  if (length(paths) == 0L) {
    issue <- new_cli_issue(
      "PLOT_SETTING_NOT_FOUND", input_dir, NULL, "No plot-setting JSON file was found."
    )
    return(list(valid = FALSE, exit_code = 2L, plot_setting_count = 0L,
                warnings = list(), errors = list(issue)))
  }
  results <- lapply(paths, validate_setting)
  errors <- unlist(lapply(results, `[[`, "errors"), recursive = FALSE)
  warnings <- unlist(lapply(results, `[[`, "warnings"), recursive = FALSE)
  if (length(paths) > 1L && all(vapply(results, function(x) !is.null(x$setting), logical(1)))) {
    errors <- append_issues(errors, multiple_setting_issues(results, paths))
  }
  schema_codes <- c(
    "PLOT_SETTING_JSON_INVALID", "PLOT_SETTING_FIELD_MISSING",
    "PLOT_SETTING_VALUE_INVALID", "SAMPLE_NAME_MISSING", "SAMPLE_NAME_DUPLICATE"
  )
  codes <- vapply(errors, `[[`, character(1), "code")
  exit_code <- if (any(codes %in% schema_codes)) 2L else if (length(errors)) 4L else 0L
  list(
    valid = length(errors) == 0L, exit_code = exit_code,
    plot_setting_count = as.integer(length(paths)), plot_setting_files = paths,
    warnings = warnings, errors = errors
  )
}
