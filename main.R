library(shiny)
library(bs4Dash)
library(dplyr)
library(readr)
library(plotly)
library(leaflet)
library(DT)
library(fresh)
library(future)
library(promises)
library(RSQLite)
library(processx)


plot_colour <- "#8965CD"

theme <- create_theme(
  bs4dash_color(
    lime = "#52A1A5",
    olive = "#4A9094",
    purple = "#8965CD"
  ),
  bs4dash_status(
    primary = "#E1EDED",
    info = "#E4E4E4"
  )
)


# Load and wrangle data ---------------------------------------------------

getGenomeName <- function(db_file){
  con <- RSQLite::dbConnect(RSQLite::SQLite(), dbname = db_file)
  query_key <- "SELECT DISTINCT Genome FROM defaultTbl"
  results <- dbGetQuery(con, query_key)
  dbDisconnect(con)
  return(results)
}

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

getToolJsonPara <- function(json_file){
  # Read and parse the JSON file
  json_data <- fromJSON(json_file)
  # Access specific fields
  version <- json_data$versionNum$version
  tool_path <- json_data$toolParas$tool_path
  db_path <- json_data$toolParas$db_path
  tool_name <- json_data$toolParas$tool_name
  java_path <- json_data$toolParas$java_path
  jar_path <- file.path(tool_path, tool_name)
  args <- c("-jar", jar_path)
  res <- c(java_path, args)
  return(res)
}

getJavaParameter <- function(paras){
  cmd_text <- sprintf("-G %s -R %s -I %s -O %s -X %s -T %s -A %s -B %s -F %d -N %d -RB %d -S %d -M %s -CZ %d -MQ %d -FL %d -SS %s -P %d -D %s",
                      paras$dashG, paras$dashR,
                      paras$dashI, paras$dashO, paras$dashX, paras$dashT,
                      paras$dashA, paras$dashB, paras$dashF, paras$dashN,
                      paras$dashRB, paras$dashS, paras$dashM, paras$dashCZ,
                      paras$dashMQ, paras$dashFL, paras$dashSS, paras$dashP,
                      paras$dashD)
  print(paste0("Input the java parameters is: ", cmd_text))
  return(cmd_text)
}
generateJavaCommand <- function(paras){
  args <- c("-G", paras$dashG, "-R", paras$dashR, "-I",paras$dashI,
            "-O", paras$dashO, "-X", paras$dashX, "-T", paras$dashT,
            "-A", paras$dashA, "-B", paras$dashB, "-F", paras$dashF,
            "-N", paras$dashN, "-RB", paras$dashRB, "-S", paras$dashS,
            "-M", paras$dashM, "-CZ", paras$dashCZ, "-MQ", paras$dashMQ,
            "-FL", paras$dashFL, "-SS", paras$dashSS, "-P", paras$dashP,
            "-D", paras$dashD)
  # args <- c("-G", paras$dashG, "-R", paras$dashR, "-I",paras$dashI, 
  #           "-O", paras$dashO)
  #print(paste0("Input the java parameters is: ", cmd_text))
  return(args)
}

# Input parameters --------------------------------------------------------
json_file <- "NGSViz_setting.json"
db_file <- '/Users/bencheye/myProj/ngsPlot/NGSViz/database/genomeCoordinate.db'
log_name <- "NGSViz_java_running.log"

# run java ----------------------------------------------------------------
java_res <- getToolJsonPara(json_file)
cmd <- java_res[1]
args <- c(java_res[2], java_res[3])
# temp file
# log_file <- tempfile(fileext = ".txt")
# result_file <- tempfile(fileext = ".txt")brary(shiny)
plan(multisession)  # Enable asynchronous execution
# Temporary file path
# log_file <- tempfile(fileext = ".txt")
# result_file <- tempfile(fileext = ".txt")

# get the genome info from db
result <- getGenomeName(db_file)

# User Interface ----------------------------------------------------------

ui <- dashboardPage(
  title = "NGSViz",
  freshTheme = theme,
  dark = TRUE,
  help = NULL,
  fullscreen = TRUE,
  scrollToTop = TRUE,
  
  # Header ----
  header = dashboardHeader(
     status = "lime",
    title = dashboardBrand(
      title = "NGSViz",
      color = "olive",
      image = "NGSViz_log.png"
    ),
    controlbarIcon = NULL,
    fixed = TRUE,
    rightUi = NULL
  ),
  
  # Sidebar ----
  sidebar = dashboardSidebar(
    sidebarMenu(
      id = "sidebarMenuid",
      menuItem("Home", tabName = "home", icon = icon("home")),
      menuItem("CalculationMode", tabName = "CalculationMode", icon = icon("calculator")),
      menuItem(
        "VisualizationMode", 
        tabName = "VisualizationMode", 
        icon = icon("chart-line"),
        startExpanded = TRUE,
        menuSubItem("CoveragePlot", tabName = "CoveragePlot", icon = icon("chart-line")),
        menuSubItem("PCA", tabName = "PCA", icon = icon("chart-line"))
      ),
      menuItem("Help", tabName = "Help", icon = icon("question-circle")),
      menuItem("AboutUs", tabName = "AboutUs", icon = icon("info-circle"))
    )
  ),
  
  # Footer ----
  footer = dashboardFooter(
    left = "FHS | University of Macau",
    right = "Copyright: Benchen Ye"
  ),
  
  # Body ----
  body = dashboardBody(
    tabItems(
      # home --------------------------------------------------------------------
      tabItem(
        tabName = "home",
        jumbotron(
          title = "Welcome to use NGSViz!",
          status = "info",
          lead = "Calculate and visualize coverage of NGS data",
          # href = "",
        )
      ),

      # CalculationMode ---------------------------------------------------------
      tabItem(
        tabName = "CalculationMode",

        # required parameters -----------------------------------------------------
        h3("Required parameters"),
        fluidRow(
          column(6, selectInput("dashG", "choose a Genome type", 
                                choices = result$Genome)),
          column(6, textInput("dashI", "choose the input a bam/txt file!:", value = "")),
        ),
        fluidRow(
          column(6, selectInput("dashR", "choose a region type", 
                             choices = c("tss", "tes", "genebody"))),
          column(6,textInput("dashO", "Type Output directory:", value = "."))
        ),
        

        # run java ----------------------------------------------------------------
        hr(),
        h3("Run java script"),
        fluidRow(
          column(6, actionButton("run_java", "Start analysis"))
          #column(6, actionButton("stop_java", "Stop script"))
        ),
        hr(),
        # h3("Input parameter: "),
        # verbatimTextOutput("result_para"),
        h3("status"),
        verbatimTextOutput("status"),
        
        
        # optional parameters -----------------------------------------------------
        hr(),
        h3("Optional parameters"),
        fluidRow(
          column(3, selectInput("dashA", "choose a analysis type", 
                                choices = c("transcript", "exon"))),
          column(3, uiOutput("dynamic_ui")), # dashB
          column(6, numericInput("dashF", "input the flanking region size:", 
                                 value = 2000, min = 1, max = 3000))
        ),
        fluidRow(
          column(3, selectInput("dashM", "choose the scale method", 
                                choices = c("bin", "spline"))),
          column(3, textInput("dashT", "Input title of plot:", value = "AverageCoverage")),
          column(6, textInput("dashX", "Input a gene list file:", value = "all"))
        ),
        fluidRow(
          column(3, numericInput("dashMQ", "MAPQ", 
                                 value = 20, min = 0, max = 100)),
          column(3, numericInput("dashP", "number of core", 
                                 value = 1, min = 1, max = 100)),
          column(6, numericInput("dashFL", "Fragment length", 
                                 value = 150, min = 50, max = 1000))
        ),
        fluidRow(
          column(3, numericInput("dashN", "Flanking size factor", 
                                 value = 0, min = 0, max = 1)),
          column(3, numericInput("dashRB", "both ends trimmed (%)", 
                                 value = 0, min = 0, max = 1)),
          column(6, selectInput("dashSS", "choose the read strand in bam file!",
                                choices = c("both", "+", "-")))
        ),
        fluidRow(
          column(3, numericInput("dashS", "random sampling rate", 
                                 value = 0, min = 0, max = 1)),
          column(3, numericInput("dashCZ", "Gene chunk size", 
                                 value = 100, min = 0, max = 1000)),
          column(6, selectInput("dashD", "Reference sequence database", 
                                choices = c("RefSeq")))
        )

      ) # CalculationMode end
      
    )
  )
)


# Server ------------------------------------------------------------------

server <- function(input, output) {
  cat("Processing begins...\n")
# dynamically generate options for dashB based on the value of das --------
  output$dynamic_ui <- renderUI({
    result <- getBiotype(db_file, input$dashG)
    selectInput("dashB", "Choose a gene element type:", 
                choices = result)
  })
  
  # Display the selected value
  output$selected_value <- renderText({
    paste("You selected:", input$dashB)
  })

# Run Java ----------------------------------------------------------------
  rv <- reactiveValues(
    process = NULL,
    running = FALSE,
    start_time = NULL
  )
  observeEvent(input$run_java, {
    req(!rv$running)
    
    para_list <- list(dashG=input$dashG, dashR=input$dashR,
      dashI=input$dashI, dashO=input$dashO, dashX=input$dashX, dashT=input$dashT,
      dashA=input$dashA, dashB=input$dashB, dashF=input$dashF, dashN=input$dashN,
      dashRB=input$dashRB, dashS=input$dashS, dashM=input$dashM, dashCZ=input$dashCZ,
      dashMQ=input$dashMQ, dashFL=input$dashFL, dashSS=input$dashSS, dashP=input$dashP,
      dashD=input$dashD
                      )
    
    cat("check input parameters!\n")
    if (is.null(input$dashI) || input$dashI == "") {
      showNotification("Please input the path of bam/txt file!", type = "error")
      return()
    }else{
      if(!file.exists(input$dashI)){
        showNotification("Please input the path of output directory!", type = "error")
      }
    }
    
    if (is.null(input$dashO) || input$dashO == "") {
      showNotification("You must enter the value of dashO!", type = "error")
      return()
    }
    
    commond_args <- generateJavaCommand(para_list)
    # # Display input parameters
    # output$result_para <- renderText({
    #   paste0("The input parameter is :\n")
    #   paste(getJavaParameter(para_list))
    # })
    
    # build the command
    cat("Prepare to run java script!\n")
    args <- c(args, commond_args)
    
    # Update status to running
    # output$status <- renderText({
    #   "The program is running...."
    # })
    # log file
    log_file <- file.path(input$dashO, log_name)
    # Clear old files
    if(file.exists(log_file)) file.remove(log_file)
    tail_args <- c(">", log_file, "2>&1", "&& echo 'PROCESS_COMPLETED' ")
    args <- c(args, tail_args)
    # Run the jar asynchronously
    rv$running <- TRUE
    rv$start_time <- Sys.time()
    
    future({
      # system2(cmd, args) #, stdout = TRUE, stderr = TRUE
      system2(cmd, args)
    }) %...>% 
      {
        rv$running <- FALSE
      } %...!% 
      {
        rv$running <- FALSE
        showNotification("Execution error!", type = "error")
      }
    
    

  }) # end for observeEvent
  
  
  # # stop 
  # observeEvent(input$stop_java, {
  #   if(rv$running && !is.null(rv$process)){
  #     tools::pskill(rv$process$get_pid())
  #     rv$running <- FALSE
  #   }
  # })
  
  # Display status information
  output$status <- renderText({
    if(rv$running){
      elapsed <- difftime(Sys.time(), rv$start_time, units = "secs")
      paste("Running... Time elapsed:", round(elapsed), "s")
    } else {
      "Ready state"
    }
  })
  
  # # Display the final result
  # output$result <- renderText({
  #   req(file.exists(result_file))
  #   readLines(result_file)
  # })
    
  
}

  

shinyApp(ui, server)