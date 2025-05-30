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
  point_lab <- c("Acceptor-Donor", "TSS-TES", "TSS", "TES")
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
  library(RSQLite)
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
  gtf_df$exon_id <- sub("\\.[0-9]+$", "", gtf_df$exon_id)
  gtf_df['gene_biotype'] <- "protein_coding"
  nr_rows <- which(grepl("^NR", gtf_df$transcript_id))
  gtf_df$gene_biotype[nr_rows] <- "ncRNA"
  # filter item
  #save_items <- c("protein_coding", "lncRNA", "snRNA", "scaRNA", "snoRNA", "sRNA",
                 # "scRNA", "miRNA", "misc_RNA", "pseudogene")
  #gtf_df_filter <- gtf_df[(gtf_df$transcript_biotype %in% save_items), ]
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
  # default tabel
  #default_tbl <- buildDefaultTbl(gtf_name_list, DB_name)
  # constructDB
  #constructDB(gtf_name_list, DB_name, default_tbl, gtf_df_filter)
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

readRefSeqFuncElems <- function(file_name){
  anno_df <- read.csv(file_name, sep = '\t', header = F)
  items <- c("enhancer", "CpG_island", "silencer", "DNase_I_hypersensitive_site",
             "promoter", "transcriptional_cis_regulatory_region", "insulator")
  anno_df <- anno_df[anno_df$V4 %in% items, c(1:4,6)]
  colnames(anno_df) <- c("chrom", "start", "end", "biotype", "strand")
  anno_df$biotype[anno_df$biotype == "DNase_I_hypersensitive_site"] <- "DHS"
  anno_df$biotype[anno_df$biotype == "transcriptional_cis_regulatory_region"] <- "TCRR"
  anno_df['width'] <- anno_df$end - anno_df$start
  anno_df['type'] <- "FuncElems"
  anno_df <- anno_df[ ,c(1:3,6,5,7,4)]
  anno_df["tid"] <- anno_df["gname"]<- "-"
  anno_df <- anno_df %>% filter(!grepl("_", chrom))
  return(anno_df)
}

convert_format <- function(input_string) {
  first_letter <- tolower(substr(input_string, 1, 1))
  remaining_part <- strsplit(input_string, "_")[[1]][2]
  new_string <- paste0(first_letter, remaining_part, "_gene_ensembl")
  return(new_string)
  return(new_string)
}

buildDBMain <- function(gtf_file, DB_name, species, file_name=NULL){
  gtf_name_list <- extractGTFName(gtf_file)
  dataset <- convert_format(species)
  ucsc_df <- gtfProcess(gtf_file, DB_name)
  gene_info <- getMapInfo(ucsc_df, dataset)
  gene_info$gene_biotype[gene_info$gene_biotype == "protein_coding"] <- "ncRNA"
  for (i in 1:dim(gene_info)[1]) {
    ucsc_df$biotype[ucsc_df$tid == gene_info$refseq_ncrna[i]] <- gene_info$gene_biotype[i]
  }
  if(!is.null(file_name)){
    anno_df <- readRefSeqFuncElems(file_name)
    ucsc_df <- rbind(ucsc_df, anno_df)
  }
  # default tabel
  default_tbl <- buildDefaultTbl(gtf_name_list, DB_name, species)
  # constructDB
  constructDB(gtf_name_list, DB_name, default_tbl, ucsc_df, species)
}

addDefaultTid <- function(gtf_file, DB_name){
  gtf_name_list <- extractGTFName(gtf_file)
  species <- strsplit(gtf_file, "/")[[1]][2]
  genome_version <- gtf_name_list[1]
  db_path <- sprintf("DB/%s/NGSViz_%s_RefSeq.db", species, genome_version)
  con <- dbConnect(RSQLite::SQLite(), dbname = db_path)
  query_key <- sprintf("SELECT * FROM %s_RefSeq", genome_version)
  print(sprintf("Query key is : %s", query_key))
  tab_res <- dbGetQuery(con, query_key)
  tab_res["DefaultChoose"] <- 0
  result_df <- tab_res %>%
    filter(type == "transcript") %>%
    group_by(gname) %>%
    slice_max(n = 1, order_by = width, with_ties = FALSE) %>%
    ungroup()
  tab_res$DefaultChoose[tab_res$type == "transcript" & (tab_res$tid %in% result_df$tid)] <- 1
  # default tabel
  default_tbl <- buildDefaultTbl(gtf_name_list, DB_name, species)
  name_prefix <- paste(gtf_name_list[1], DB_name, sep = '_')
  # constructDB
  RSQLite::dbWriteTable(con, name_prefix, tab_res, 
                        overwrite = TRUE, row.names = FALSE)
  # Close database connection
  RSQLite::dbDisconnect(con)
}



check_and_install_package('rtracklayer')
check_and_install_package('RSQLite')
library(RSQLite)
library(rtracklayer)
library(dplyr)
library(biomaRt)
#proj_path <- '/Users/bencheye/myProj/ngsPlot/DB'
proj_path <- "/Users/benche/myProj/ngsPlot/DB"
setwd(proj_path)
DB_name <- 'RefSeq'

# human
species <- "Homo_sapiens"
# hg38
gtf_file <- "gtf/Homo_sapiens/hg38/hg38.ncbiRefSeq.gtf"
file_name <- "gtf/Homo_sapiens/hg38/refseq_element.bed"
buildDBMain(gtf_file, DB_name, species, file_name=file_name)
addDefaultTid(gtf_file, DB_name)
# t2t
gtf_file <- "gtf/Homo_sapiens/t2ths1/hs1.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)
# hg19
gtf_file <- "gtf/Homo_sapiens/hg19/hg19.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)


# Mus_musculus
species <- "Mus_musculus"
# mm39
gtf_file <- "gtf/Mus_musculus/mm39.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species, file_name=file_name)

# mm10 -ucsc error
gtf_file <- "gtf/Mus_musculus/mm10/mm10.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
file_name <- "gtf/Mus_musculus/mm10/RefSeq_Func_Elems.txt"
buildDBMain(gtf_file, DB_name, species)
# mm9
gtf_file <- "gtf/Mus_musculus/mm9.refGene.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)

# Papio
species <- "Papio"
# mm39
gtf_file <- "gtf/Papio/mm39.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species, file_name=file_name)

# Danio_rerio
species <- "Danio_rerio"
# danRer11
gtf_file <- "gtf/Danio_rerio/danRer11.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)
gtf_file <- "gtf/Danio_rerio/danRer10.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)
gtf_file <- "gtf/Danio_rerio/danRer7.refGene.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)

# Drosophila_melanogaster
species <- "Drosophila_melanogaster"
# danRer11
gtf_file <- "gtf/Drosophila_melanogaster/dm6.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)
gtf_file <- "gtf/Drosophila_melanogaster/dm3.refGene.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)

# Felis_catus
species <- "Felis_catus"
# danRer11
gtf_file <- "gtf/Felis_catus/felCat9.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)
gtf_file <- "gtf/Felis_catus/felCat8.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)
gtf_file <- "gtf/Felis_catus/felCat5.refGene.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)

# Canis_familiaris
species <- "Canis_familiaris"
# danRer11
gtf_file <- "gtf/Canis_familiaris/canFam6.ncbiRefSeq.gtf"
addDefaultTid(gtf_file, DB_name)
buildDBMain(gtf_file, DB_name, species)



# db_info
db_file <- 'DB/ngsplot2_Rattus_norvegicus_ensembl_mRatBN7_2.db'
db_file <- 'DB/ngsplot2_Mus_musculus_ensembl_GRCm38_102.db'
db_file <- 'DB/Homo_sapiens/NGSViz_hg19_RefSeq.db'
con <- RSQLite::dbConnect(RSQLite::SQLite(), dbname = db_file)
# get the tables of DB
dbListTables(con)
# Close database connection
query_key <- "SELECT * FROM Rattus_norvegicus_ensembl_mRatBN7_2"
query_key <- "SELECT * FROM defaultTbl"
query_key <- "SELECT * FROM hg19_RefSeq"
results <- dbGetQuery(con, query_key)
#RSQLite::dbDisconnect(con)
query_key <- "SELECT * FROM defaultTbl"
results1 <- dbGetQuery(con, query_key)
