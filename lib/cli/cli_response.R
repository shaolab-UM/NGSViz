new_cli_issue <- function(code, file = NULL, field = NULL, message) {
  list(code = code, file = file, field = field, message = message)
}

build_cli_response <- function(command, status, exit_code, message, data = NULL,
                               warnings = list(), errors = list()) {
  list(
    schema_version = "1.0",
    command = command,
    status = status,
    exit_code = as.integer(exit_code),
    message = message,
    data = data,
    warnings = unname(warnings),
    errors = unname(errors)
  )
}

build_validation_response <- function(command, input_dir, environment_result,
                                      validation_result = NULL) {
  input_path <- normalizePath(input_dir, mustWork = FALSE)
  if (!environment_result$valid) {
    data <- list(
      input_dir = input_path,
      r_version = environment_result$r_version,
      missing_packages = unname(environment_result$missing_packages)
    )
    return(build_cli_response(
      command, "error", 3L, "Visualization environment validation failed.",
      data, errors = environment_result$errors
    ))
  }
  data <- list(
    input_dir = input_path,
    plot_setting_count = validation_result$plot_setting_count,
    manifest = NULL,
    r_version = environment_result$r_version
  )
  if (!validation_result$valid) {
    return(build_cli_response(
      command, "error", validation_result$exit_code,
      "Visualization input validation failed.", data,
      validation_result$warnings, validation_result$errors
    ))
  }
  build_cli_response(
    command, "success", 0L, "Visualization inputs are valid.", data,
    validation_result$warnings
  )
}

emit_cli_response <- function(response) {
  cat(jsonlite::toJSON(
    response, auto_unbox = TRUE, null = "null", na = "null", digits = NA
  ))
  cat("\n")
}
