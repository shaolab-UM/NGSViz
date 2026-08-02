test_cli_parser <- function(project_root) {
  source(file.path(project_root, "lib", "cli", "cli_response.R"))
  source(file.path(project_root, "lib", "cli", "cli_parser.R"))

  described <- parse_cli_args(c("describe", "--format", "json"))
  stopifnot(
    identical(described$mode, "native"),
    identical(described$command, "describe"),
    identical(described$format, "json")
  )

  validated <- parse_cli_args(c(
    "validate-plot", "--input", "/tmp/input", "--format", "json"
  ))
  stopifnot(
    identical(validated$command, "validate-plot"),
    identical(validated$input_dir, "/tmp/input")
  )

  legacy <- parse_cli_args("/tmp/legacy-input")
  stopifnot(
    identical(legacy$mode, "legacy"),
    identical(legacy$command, "run-plot"),
    identical(legacy$input_dir, "/tmp/legacy-input")
  )

  unsupported <- parse_cli_args(c(
    "run-plot", "--input", "/tmp/input", "--format", "json"
  ))
  stopifnot(identical(unsupported$command, "run-plot"))

  invalid <- tryCatch(
    parse_cli_args(c("validate-plot", "--format", "json")),
    cli_argument_error = identity
  )
  stopifnot(inherits(invalid, "cli_argument_error"))

  missing_package <- build_validation_response(
    command = "validate-plot",
    input_dir = "/tmp/input",
    environment_result = list(
      valid = FALSE,
      r_version = R.version.string,
      missing_packages = "definitely-not-installed",
      errors = list(new_cli_issue(
        "R_PACKAGE_MISSING",
        field = "packages",
        message = "Required R package is not installed: definitely-not-installed."
      ))
    ),
    validation_result = NULL
  )
  stopifnot(
    identical(missing_package$status, "error"),
    identical(missing_package$exit_code, 3L),
    identical(missing_package$errors[[1]]$code, "R_PACKAGE_MISSING")
  )
}
