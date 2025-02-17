# build species reference genome based on the gtf file
check_and_install_package <- function(package_name) {
  if (!requireNamespace(package_name, quietly = TRUE)) {
    if (!require("BiocManager", quietly = TRUE)) {
      install.packages("BiocManager")
    }
    BiocManager::install(package_name)
  }
}

extractGTFName <- function(gtf_file){
  gtf_name <- basename(gtf_file)
  split_result <- strsplit(gtf_name, split = ".", fixed = TRUE)
  split_result <- unlist(split_result)[1:3]
  names(split_result) <- c('Species', 'Genome', 'Gversion')
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

buildDefaultTbl <- function(gtf_name_list, DB_name){
  genome <- rep(gtf_name_list["Genome"], 4)
  species <- rep(gtf_name_list["Species"], 4)
  version <- rep(gtf_name_list["Gversion"], 4)
  DB <- rep(DB_name, 4)
  region <- c("exon", "genebody", "TSS", "TES")
  analysis_type <- rep("chipseq", 4)
  biotype <- rep("protein_coding", 4)
  point_lab <- c("Acceptor-Donor", "TSS-TES", "TSS", "TES")
  flank_size <- c(500, 2000, 2000, 2000)
  name_prefix <- paste(gtf_name_list[1], DB_name, gtf_name_list[2], 
                       gtf_name_list[3], sep = '_')
  coord_tbl_name <- rep(name_prefix, 4)
  default_tbl <- data.frame(
    "Species" = species,
    "CoordinateTblName" = coord_tbl_name,
    "DefaultDB" = DB,
    "Genome" = genome,
    "Version" = version,
    "Region" = region,
    "AnalysisType" = analysis_type,
    "Biotype" = biotype,
    "PointLab" = point_lab,
    "FlankSize" = flank_size
  )
  return(default_tbl)
}

constructDB <- function(gtf_name_list, DB_name, default_tbl, gtf_df_filter){
  library(RSQLite)
  # Create SQLite database connection
  name_prefix <- paste(gtf_name_list[1], DB_name, gtf_name_list[2], 
                       gtf_name_list[3], sep = '_')
  db_file <- paste0("DB/ngsplot2_", name_prefix, ".db")
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
}

buildDBMain <- function(gtf_file, DB_name){
  gtf_name_list <- extractGTFName(gtf_file)
  gtf_data <- import(gtf_file, format="gtf")
  gtf_df <- as.data.frame(gtf_data)
  # filter item
  save_items <- c("protein_coding", "lncRNA", "snRNA", "scaRNA", "snoRNA", "sRNA",
                  "scRNA", "miRNA", "misc_RNA")
  gtf_df_filter <- gtf_df[(gtf_df$transcript_biotype %in% save_items), ]
  save_column <- c("seqnames", "start", "end", "width", "strand", "type", 
                   "gene_biotype", "gene_id", "gene_name", "transcript_id")
  gtf_df_filter <- gtf_df_filter[ ,save_column]
  save_items <- c("transcript", "exon") # , "gene"
  gtf_df_filter <- gtf_df_filter[(gtf_df_filter$type %in% save_items), ]
  # adjust seqnames
  gtf_df_filter <- adjustSeqNames(gtf_df_filter)
  colnames(gtf_df_filter)[c(1,7:10)] <- c("chrom", "biotype", "gid", "gname", "tid")
  # default tabel
  default_tbl <- buildDefaultTbl(gtf_name_list, DB_name)
  # constructDB
  constructDB(gtf_name_list, DB_name, default_tbl, gtf_df_filter)
}

check_and_install_package('rtracklayer')
check_and_install_package('RSQLite')
library(RSQLite)
library(rtracklayer)
proj_path <- '/Users/bencheye/myProj/ngsPlot/DB'
setwd(proj_path)
#gtf_file <- 'Mus_musculus.GRCm38.102.chr.gtf'
gtf_file <- 'gtf/Homo_sapiens.GRCh38.113.chr.gtf'
DB_name <- 'ensembl'
buildDBMain(gtf_file, DB_name)

gtf_file <- 'gtf/Mus_musculus.GRCm38.102.chr.gtf'
DB_name <- 'ensembl'
buildDBMain(gtf_file, DB_name)

gtf_file <- "gtf/Rattus_norvegicus.mRatBN7.2.113.gtf"
DB_name <- 'ensembl'
buildDBMain(gtf_file, DB_name)



# db_info
db_file <- 'DB/ngsplot2_Rattus_norvegicus_ensembl_mRatBN7_2.db'
db_file <- 'DB/ngsplot2_Mus_musculus_ensembl_GRCm38_102.db'
db_file <- 'DB/NGSVir_hg19_RefSeq.db'
con <- RSQLite::dbConnect(RSQLite::SQLite(), dbname = db_file)
# get the tables of DB
dbListTables(con)
# Close database connection
query_key <- "SELECT * FROM Rattus_norvegicus_ensembl_mRatBN7_2"
query_key <- "SELECT * FROM Mus_musculus_ensembl_GRCm38_102"
query_key <- "SELECT * FROM hg19_RefSeq"
results <- dbGetQuery(con, query_key)
#RSQLite::dbDisconnect(con)
query_key <- "SELECT * FROM defaultTbl"
results1 <- dbGetQuery(con, query_key)
