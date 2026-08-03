#!/usr/bin/env Rscript

# Build and inspect a minimal reference database in a temporary directory.
args <- commandArgs(trailingOnly = TRUE)
if (length(args) != 1L) stop("Usage: 04_build_db_smoke.R <project-root>")
project_root <- normalizePath(args[[1L]], mustWork = TRUE)
work_dir <- tempfile("ngsviz-db-smoke-")
dir.create(work_dir)
on.exit(unlink(work_dir, recursive = TRUE), add = TRUE)

gtf_path <- file.path(work_dir, "smoke.RefSeq.gtf")
attributes <- paste0(
  'gene_id "GENE1"; transcript_id "NM_000001.1"; ',
  'gene_name "GENE1"; exon_id "EXON1.1";'
)
writeLines(c(
  paste("chr1", "RefSeq", "transcript", 101, 300, ".", "+", ".", attributes, sep = "\t"),
  paste("chr1", "RefSeq", "exon", 101, 200, ".", "+", ".", attributes, sep = "\t")
), gtf_path)

previous_dir <- setwd(work_dir)
on.exit(setwd(previous_dir), add = TRUE)
output <- system2(
  file.path(R.home("bin"), "Rscript"),
  c(
    file.path(project_root, "lib", "buildRefDB.R"), gtf_path,
    "RefSeq", "Homo_sapiens", "FALSE"
  ),
  stdout = TRUE,
  stderr = TRUE
)
status <- attr(output, "status")
if (!is.null(status) && status != 0L) stop(paste(output, collapse = "\n"))

database <- file.path(
  work_dir, "DB", "Homo_sapiens", "NGSViz_smoke_RefSeq.db"
)
if (!file.exists(database) || file.info(database)$size <= 0L) {
  stop("Reference database was not created.")
}
connection <- RSQLite::dbConnect(RSQLite::SQLite(), database)
on.exit(RSQLite::dbDisconnect(connection), add = TRUE)
default_rows <- RSQLite::dbGetQuery(connection, "SELECT COUNT(*) AS n FROM defaultTbl")$n
coordinate_rows <- RSQLite::dbGetQuery(connection, "SELECT COUNT(*) AS n FROM smoke_RefSeq")$n
if (default_rows != 4L || coordinate_rows < 1L) {
  stop("Reference database tables are incomplete.")
}
cat("Reference DB smoke passed.\n")
