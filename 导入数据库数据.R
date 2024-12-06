
library(RSQLite)
library(readr)

# fucntion
getFileFunctionName <- function(load_name){
  file_name <- basename(load_name)
  str_split <- strsplit(file_name, '[.]')
  genome <- str_split[[1]][1]
  db_name <- str_split[[1]][2]
  return(c(genome, db_name))
}
integrateExonData <- function(load_name, genome, db_name){
  exon_df <- data.frame()
  for(i in 1:length(exonmodel_list)){
    list_name <- names(exonmodel_list)[i]
    part_df <- as.data.frame(exonmodel_list[[i]])
    colnames(part_df) <- c("start", 'end', 'width')
    part_df['tid'] <- list_name
    part_df['genome'] <- genome
    part_df['db'] <- db_name
    part_df <- part_df[ ,c(4,5,6,1,2,3)]
    if(i == 1){
      exon_df <- part_df
    }else{
      exon_df <- rbind(exon_df, part_df)
    }
  }
  return(exon_df)
}
processExonModel <- function(load_name, exonmodel_list){
  str_res <- getFileFunctionName(load_name)
  genome <- str_res[1]
  db_name <- str_res[2]
  exon_df <- integrateExonData(exonmodel_list, genome, db_name)
  return(exon_df)
}

# 替换为你的 CSV 文件路径
proj_path <- "/Users/bencheye/myProj/ngsPlot/Data"
setwd(proj_path)
coord_file <- 'hg19.ensembl.genebody.protein_coding.csv'
dbfile_file <- 'dbfile.tbl.csv'
defaultTbl_file <- 'default_tbl.csv'
coord_df <- read.csv(coord_file)
dbfile <- read.csv(dbfile_file, check.names = F)
colnames(dbfile) <- gsub("\\.", "", colnames(dbfile))
defaultTbl_df <- read.csv(defaultTbl_file)

# remove the ".Rdata"

# 创建 SQLite 数据库连接
# 替换为你想要的数据库文件路径
db_file <- "/Users/bencheye/myProj/ngsPlot/NGSPlot2/coordinate_db.db"
con <- dbConnect(RSQLite::SQLite(), dbname = db_file)
# 将数据写入数据库
# 替换为你想要的表名

#-----------------------------------------
# test database just only have part of data

# exonmodel
load_name <- "/Users/bencheye/myProj/ngsPlot/ngsplot-develop/database/hg19/hg19.ensembl.exonmodel.RData"
load(load_name)

## test-----------------------
exonmodel_list <- exonmodel[1:10]
rm(exonmodel)
# test-------------------------
exon_df <- processExonModel(load_name, exonmodel_list)


species_name <- dbfile$species[1]
dbWriteTable(con, species_name, coord_df, 
             overwrite = TRUE, row.names = FALSE)
#dbWriteTable(con, "elementgenomecoordinate", coord_df, 
 #            overwrite = TRUE, row.names = FALSE)
dbWriteTable(con, "refmappinginfotab", dbfile, 
             overwrite = TRUE, row.names = FALSE)
dbWriteTable(con, "defaulttbl", defaultTbl_df, 
             overwrite = TRUE, row.names = FALSE)
table_name <- paste0(species_name, "exonModel")
dbWriteTable(con, table_name, exon_df, 
             overwrite = TRUE, row.names = FALSE)
# 关闭数据库连接
dbDisconnect(con)

tables <- dbListTables(con)
print(tables)
query <- "SELECT * FROM defaulttbl"
query <- "SELECT Genome FROM defaulttbl WHERE Genome = 'hg19' AND Region = 'tss'"
query_key <- "SELECT FI1 FROM refmappinginfotab WHERE Genome = 'hg19' AND Region = 'cgi' AND DB = 'ensembl' "
query_key <- "SELECT tid,start,end,width FROM humanexonModel WHERE Genome = 'hg19' AND db = 'ensembl'"
results <- dbGetQuery(con, query_key)
print(results)

library(Rsamtools)
fl <- system.file("extdata", "ex1.bam", package="Rsamtools")
