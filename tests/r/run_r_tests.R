#!/usr/bin/env Rscript

get_project_root <- function() {
  args <- commandArgs(trailingOnly = FALSE)
  file_arg <- sub("^--file=", "", grep("^--file=", args, value = TRUE))
  normalizePath(file.path(dirname(file_arg), "..", ".."), mustWork = TRUE)
}

project_root <- get_project_root()
source(file.path(project_root, "tests", "r", "test_cli_parser.R"))
source(file.path(project_root, "tests", "r", "test_validate_plot_input.R"))

test_cli_parser(project_root)
test_validate_plot_input(project_root)
cat("All R CLI tests passed.\n")
