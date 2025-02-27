
# get the genome
getGenomeName <- function(db_file){
  con <- RSQLite::dbConnect(RSQLite::SQLite(), dbname = db_file)
  query_key <- "SELECT DISTINCT Genome FROM defaultTbl"
  results <- dbGetQuery(con, query_key)
  dbDisconnect(con)
  return(results)
}

# get the biotype based on the db
getBiotype <- function(db_file, genome){
  con <- RSQLite::dbConnect(RSQLite::SQLite(), dbname = db_file)
  query_key <- sprintf("SELECT DISTINCT CoordinateTblName FROM defaultTbl WHERE Genome = '%s'", genome)
  result <- dbGetQuery(con, query_key)
  query_key2 <- paste0("SELECT DISTINCT biotype FROM ", result$CoordinateTblName)
  print(result$CoordinateTblName)
  results <- dbGetQuery(con, query_key2)
  dbDisconnect(con)
  result <- results$biotype
  protein_coding <- result[grep("protein_coding", result)]
  non_protein_coding <- result[!grepl("protein_coding", result)]
  sorted_res <- c(protein_coding, non_protein_coding)
  return(sorted_res)
}