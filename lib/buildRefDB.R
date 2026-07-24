#!/usr/bin/env Rscript

# Define the usage message for the script
usage_message <- function() {
  cat("
Usage: Rscript your_script_name.R <gtf_file> <DB_name> <species> <annotatedNCRNA>

Parameter descriptions:
  <gtf_file>: Specifies the path to the GTF file.
  <DB_name>: The name of the database.
  <species>: The name of the species.
  <annotatedNCRNA>: Indicates whether to refine annotations for ncRNA.

Example:
  Rscript your_script_name.R hg38.ncbiRefSeq.gtf Homo_sapiens RefSeq FALSE
")
}


# build species reference genome based on the gtf file
checkRequiredPackages <- function(required_packages) {
  # Loop through each package to check and load
  for (pkg in required_packages) {
    # Check if the package is installed
    if (!requireNamespace(pkg, quietly = TRUE)) {
      message(paste0("Package '", pkg, "' is not installed, attempting to install..."))
      # Attempt to install the package
      tryCatch({
        install.packages(pkg, dependencies = TRUE)
        message(paste0("Package '", pkg, "' installed successfully."))
      }, error = function(e) {
        # If installation fails, print error message and stop the script
        stop(paste0("Error: Unable to install package '", pkg, "'. Please install this package manually and then run the script.\n",
                    "Error message: ", e$message))
      })
    }
    
    # Attempt to load the package
    # Use suppressPackageStartupMessages to avoid printing package startup messages
    # Use tryCatch to capture potential errors during loading
    tryCatch({
      suppressPackageStartupMessages(library(pkg, character.only = TRUE))
      message(paste0("Package '", pkg, "' loaded successfully."))
    }, error = function(e) {
      # If loading fails, print error message and stop the script
      stop(paste0("Error: Unable to load package '", pkg, "'. Please check if the installation is complete or if there are compatibility issues.\n",
                  "Error message: ", e$message))
    })
  }
  
  message("\nAll required packages have been installed and loaded.")
}

extractGTFName <- function(gtf_file){
  gtf_name <- basename(gtf_file)
  split_result <- strsplit(gtf_name, split = ".", fixed = TRUE)
  split_result <- unlist(split_result)[1:2]
  names(split_result) <- c('Genome', 'DB')
  return(split_result)
}

adjustSeqNames <- function(gtf_df){
  gtf_df$seqnames <- as.character(gtf_df$seqnames)
  mt_index <- which(gtf_df$seqnames %in% "MT")
  seqnames_list <- gtf_df$seqnames[!duplicated(gtf_df$seqnames)]
  print("Remove the mt gene information")
  if(length(mt_index)>0){
    gtf_df <- gtf_df[-mt_index, ]
  }
  chr_lab <- substr(gtf_df$seqnames[1], 1, 3)
  if(chr_lab != 'chr'){
    gtf_df$seqnames <- paste0("chr", gtf_df$seqnames)
    print("Add the 'chr' label")
  }
  return(gtf_df)
}

buildDefaultTbl <- function(gtf_name_list, DB_name, species){
  genome <- rep(gtf_name_list["Genome"], 4)
  species <- rep(species, 4)
  # version <- rep(gtf_name_list["Gversion"], 4)
  DB <- rep(DB_name, 4)
  region <- c("exon", "genebody", "tss", "tes")
  analysis_type <- c("exon", "transcript", "transcript", "transcript")
  biotype <- rep("protein_coding", 4)
  point_lab <- c("TSS-TES", "TSS-TES", "TSS", "TES")
  flank_size <- c(500, 2000, 2000, 2000)
  name_prefix <- paste(gtf_name_list[1], DB_name, sep = '_')
  coord_tbl_name <- rep(name_prefix, 4)
  default_tbl <- data.frame(
    "Species" = species,
    "CoordinateTblName" = coord_tbl_name,
    "DB" = DB,
    "Genome" = genome,
    #"Version" = version,
    "Region" = region,
    "AnalysisType" = analysis_type,
    "Biotype" = biotype,
    "PointLab" = point_lab,
    "FlankSize" = flank_size
  )
  return(default_tbl)
}

constructDB <- function(gtf_name_list, DB_name, default_tbl, gtf_df_filter, species){
  # Create SQLite database connection
  name_prefix <- paste(gtf_name_list[1], DB_name, sep = '_')
  db_path <- paste0("DB/", species)
  if (!dir.exists(db_path)) {
    dir.create(db_path, recursive = TRUE)
    cat("create dir:", db_path, "\n")
  } 
  db_file <- paste0(db_path, "/NGSViz_", name_prefix, ".db")
  folder_path <- "DB"
  if (!dir.exists(folder_path)) {
    dir.create(folder_path)
    print(paste(folder_path, "created successfully."))
  } 
  con <- RSQLite::dbConnect(RSQLite::SQLite(), dbname = db_file)
  RSQLite::dbWriteTable(con, "defaultTbl", default_tbl, 
                        overwrite = TRUE, row.names = FALSE)
  RSQLite::dbWriteTable(con, name_prefix, gtf_df_filter, 
                        overwrite = TRUE, row.names = FALSE)
  # Close database connection
  RSQLite::dbDisconnect(con)
  message(sprintf("The DB has been constructed in: %s", db_file))
}

gtfProcess <- function(gtf_file, DB_name){
  gtf_name_list <- extractGTFName(gtf_file)
  gtf_data <- import(gtf_file, format="gtf")
  gtf_df <- as.data.frame(gtf_data)
  # filter seqnames and other data
  gtf_df <- gtf_df %>% filter(!grepl("chrMT|_", seqnames))
  gtf_df <- gtf_df %>% filter(grepl("^NR|^NM", transcript_id))
  gtf_df <- gtf_df %>% filter(!grepl("^LOC", gene_name))
  gtf_df$transcript_id <- sub("\\.[0-9]+$", "", gtf_df$transcript_id)
  gtf_df$exon_id <- sub("\\.[0-9]+$", "", gtf_df$exon_id)

  gtf_df['gene_biotype'] <- "protein_coding"
  nr_rows <- which(grepl("^NR", gtf_df$transcript_id))
  gtf_df$gene_biotype[nr_rows] <- "ncRNA"
  # filter item
  save_column <- c("seqnames", "start", "end", "width", "strand", "type", 
                   "gene_biotype", "gene_id", "gene_name", "transcript_id")
  gtf_df_filter <- gtf_df[ ,save_column]
  save_items <- c("transcript", "exon") # , "gene"
  gtf_df_filter <- gtf_df_filter[(gtf_df_filter$type %in% save_items), ]
  # adjust seqnames
  gtf_df_filter <- adjustSeqNames(gtf_df_filter)
  colnames(gtf_df_filter)[c(1,7:10)] <- c("chrom", "biotype", "gid", "gname", "tid")
  gtf_df_filter <- gtf_df_filter[ ,-8]
  return(gtf_df_filter)
}

getMapInfo <- function(ucsc_df, dataset = "hsapiens_gene_ensembl"){
  # Connect to the Ensembl database
  ensembl <- useMart("ensembl", dataset = dataset)
  nr_item <- ucsc_df %>% filter(grepl("^NR", tid))
  gene_names <- nr_item$tid[!duplicated(nr_item$tid)]
  # Obtain genetic type information
  gene_info <- getBM(attributes = c('refseq_ncrna', 'gene_biotype'),
                   filters = 'refseq_ncrna',
                   values = gene_names,  
                   mart = ensembl)
  return(gene_info)
}

convert_format <- function(input_string) {
  first_letter <- tolower(substr(input_string, 1, 1))
  remaining_part <- strsplit(input_string, "_")[[1]][2]
  new_string <- paste0(first_letter, remaining_part, "_gene_ensembl")
  return(new_string)
}

buildDBMain <- function(gtf_file, DB_name, species, annotatedNCRNA = T){
  gtf_name_list <- extractGTFName(gtf_file)
  dataset <- convert_format(species)
  message("Parsing the GTF file...")
  ucsc_df <- gtfProcess(gtf_file, DB_name)
  if(annotatedNCRNA){
    # annotated the ncRNA with biomaRt
    message("Annotating ncRNA...")
    gene_info <- getMapInfo(ucsc_df, dataset)
    if(!is.null(gene_info)){
      gene_info$gene_biotype[gene_info$gene_biotype == "protein_coding"] <- "ncRNA"
      for (i in 1:dim(gene_info)[1]) {
        ucsc_df$biotype[ucsc_df$tid == gene_info$refseq_ncrna[i]] <- gene_info$gene_biotype[i]
      }
    }
  }
  
  # default tabel
  default_tbl <- buildDefaultTbl(gtf_name_list, DB_name, species)
  # constructDB
  constructDB(gtf_name_list, DB_name, default_tbl, ucsc_df, species)
  
}


mainDB <- function(args){
  required_arg_count <- 4
  if (length(args) == 0) {
    # If no arguments are provided
    message("No command line arguments detected.")
    usage_message()
    quit(save = "no", status = 1) # Exit script with non-zero status code, indicating an error
  } else if (length(args) != required_arg_count) {
    # If the number of arguments does not match
    message(paste0("Incorrect number of arguments. ", required_arg_count, 
                   " arguments are required, but only ", length(args), " were provided."))
    usage_message()
    quit(save = "no", status = 1) # Exit script
  } else {
    message("Command line arguments successfully received.")
  }  
  required_packages <- c("RSQLite", "dplyr", "rtracklayer", "biomaRt")
  checkRequiredPackages(required_packages) 
  # get the parameters values
  gtf_file <- args[1]
  DB_name <- args[2]
  species <- args[3]
  annotatedNCRNA <- toupper(args[4]) == "TRUE"
  buildDBMain(gtf_file, DB_name, species, annotatedNCRNA)
}

# Usage -------------------------------------------------------------------

# --- 1. Check command line arguments ---
args <- commandArgs(trailingOnly = TRUE)
mainDB(args)
