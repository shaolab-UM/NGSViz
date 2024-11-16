
library(RSQLite)
library(readr)
# 替换为你的 CSV 文件路径
proj_path <- "/Users/bencheye/myProj/ngsPlot/Data"
setwd(proj_path)
coord_file <- 'hg19.ensembl.genebody.protein_coding.csv'
dbfile_file <- 'dbfile.tbl.csv'
defaultTbl_file <- 'default_tbl.csv'
coord_df <- read_csv(coord_file)
dbfile <- read_csv(dbfile_file)
defaultTbl_df <- read_csv(defaultTbl_file)

# remove the ".Rdata"

# 创建 SQLite 数据库连接
# 替换为你想要的数据库文件路径
db_file <- "/Users/bencheye/myProj/ngsPlot/NGSPlot2/coordinate_db.db"
con <- dbConnect(RSQLite::SQLite(), dbname = db_file)
# 将数据写入数据库
# 替换为你想要的表名
species_name <- dbfile$species[1]
dbWriteTable(con, species_name, coord_df, 
             overwrite = TRUE, row.names = FALSE)
#dbWriteTable(con, "elementgenomecoordinate", coord_df, 
 #            overwrite = TRUE, row.names = FALSE)
dbWriteTable(con, "refmappinginfotab", dbfile, 
             overwrite = TRUE, row.names = FALSE)
dbWriteTable(con, "defaulttbl", defaultTbl_df, 
             overwrite = TRUE, row.names = FALSE)
# 关闭数据库连接
dbDisconnect(con)

tables <- dbListTables(con)
print(tables)
query <- "SELECT * FROM defaulttbl"
query <- "SELECT Genome FROM defaulttbl WHERE Genome = 'hg19' AND Region = 'tss'"
results <- dbGetQuery(con, query)
print(results)



