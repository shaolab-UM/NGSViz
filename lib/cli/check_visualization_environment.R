visualization_packages <- function() {
  c(
    "jsonlite", "ggh4x", "tidyverse", "colorspace", "circlize",
    "RColorBrewer", "purrr", "data.table", "RcppRoll", "zoo", "aplot",
    "cowplot", "corrplot", "ggrepel", "FactoMineR", "scales"
  )
}

check_visualization_environment <- function(
    required_packages = visualization_packages(), minimum_r_version = "4.1.0") {
  errors <- list()
  if (getRversion() < minimum_r_version) {
    errors[[length(errors) + 1L]] <- new_cli_issue(
      "R_VERSION_UNSUPPORTED", field = "r_version",
      message = paste0("R ", minimum_r_version, " or newer is required.")
    )
  }
  installed <- vapply(
    required_packages, requireNamespace, logical(1), quietly = TRUE,
    USE.NAMES = TRUE
  )
  missing_packages <- names(installed)[!installed]
  for (package in missing_packages) {
    errors[[length(errors) + 1L]] <- new_cli_issue(
      "R_PACKAGE_MISSING", field = "packages",
      message = paste0("Required R package is not installed: ", package, ".")
    )
  }
  list(
    valid = length(errors) == 0L,
    r_version = R.version.string,
    minimum_r_version = minimum_r_version,
    required_packages = required_packages,
    missing_packages = missing_packages,
    errors = errors
  )
}
