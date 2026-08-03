#!/usr/bin/env Rscript

# Install the Bioconductor package that has no osx-arm64 Conda build.
bioconductor_version <- "3.20"
expected_versions <- c(EnrichedHeatmap = "1.36.0")

if (!requireNamespace("BiocManager", quietly = TRUE)) {
  stop("BiocManager is required before installing post-Conda dependencies.")
}

missing_packages <- names(expected_versions)[!vapply(
  names(expected_versions), requireNamespace, logical(1), quietly = TRUE
)]
if (length(missing_packages) > 0L) {
  BiocManager::install(
    missing_packages,
    version = bioconductor_version,
    ask = FALSE,
    update = FALSE
  )
}

for (package in names(expected_versions)) {
  installed_version <- as.character(packageVersion(package))
  expected_version <- expected_versions[[package]]
  if (!identical(installed_version, expected_version)) {
    stop(sprintf(
      "Unexpected %s version: expected %s, found %s.",
      package, expected_version, installed_version
    ))
  }
  message(sprintf("Verified %s %s.", package, installed_version))
}
