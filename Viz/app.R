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
plan(multisession)

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


# run java ----------------------------------------------------------------
# temp file
log_file <- tempfile(fileext = ".txt")
result_file <- tempfile(fileext = ".txt")brary(shiny)
plan(multisession)  # Enable asynchronous execution
# Temporary file path
log_file <- tempfile(fileext = ".txt")
result_file <- tempfile(fileext = ".txt")

db_file <- '/Users/bencheye/myProj/ngsPlot/NGSViz/database/genomeCoordinate.db'
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
        ),
      ),

      # CalculationMode ---------------------------------------------------------
      tabItem(
        tabName = "CalculationMode",

        # required parameters -----------------------------------------------------
        h3("Required parameters"),
        fluidRow(
          column(6, selectInput("dashG", "choose a Genome type", 
                                choices = result$Genome),),
          column(6, fileInput("dashI", "choose the input a bam/txt file!"),),
        ),
        fluidRow(
          column(6, selectInput("dashR", "choose a region type", 
                             choices = c("tss", "tes", "genebody")),),
          column(6,textInput("dashO", "Type Output directory:", value = ""),)
        ),
        

        # run java ----------------------------------------------------------------
        
        sidebarPanel(
          textInput("java_path", "Java路径", value = "java"),
          textInput("jar_path", "JAR文件路径", value = "your_program.jar"),
          actionButton("run", "运行脚本"),
          actionButton("stop", "停止脚本"),
          hr(),
          verbatimTextOutput("status")
        ),
        mainPanel(
          h4("实时日志"),
          verbatimTextOutput("log"),
          h4("最终结果"),
          verbatimTextOutput("results")
        ),
        
        
        # optional parameters -----------------------------------------------------
        h3("Optional parameters"),
        fluidRow(
          column(3, selectInput("dashA", "choose a analysis type", 
                                choices = c("transcript", "exon")),),
          column(3, uiOutput("dynamic_ui")), # dashB
          column(6, numericInput("dashF", "input the flanking region size:", 
                                 value = 2000, min = 1, max = 3000),),
        ),
        fluidRow(
          column(3, selectInput("dashM", "choose the scale method", 
                                choices = c("bin", "spline")),),
          column(3, textInput("dashT", "Input title of plot:", value = "Average Coverage"),),
          column(6, textInput("dashX", "Input a gene list file:", value = "all"),),
        ),
        fluidRow(
          column(3, numericInput("dashMQ", "MAPQ", 
                                 value = 20, min = 0, max = 100),),
          column(3, numericInput("dashP", "number of core", 
                                 value = 1, min = 1, max = 100),),
          column(6, numericInput("dashFL", "Fragment length", 
                                 value = 150, min = 50, max = 1000),),
        ),
        fluidRow(
          column(3, numericInput("dashN", "Flanking size factor", 
                                 value = 0, min = 0, max = 1),),
          column(3, numericInput("dashRB", "both ends trimmed (%)", 
                                 value = 0, min = 0, max = 1),),
          column(6, selectInput("dashSS", "choose the read strand in bam file!",
                                choices = c("both", "+", "-")),),
        ),
        fluidRow(
          column(3, numericInput("dashS", "random sampling rate", 
                                 value = 0, min = 0, max = 1),),
          column(3, numericInput("dashCZ", "Gene chunk size", 
                                 value = 100, min = 0, max = 1000),),
          column(6, selectInput("dashD", "Reference sequence database", 
                                choices = c("RefSeq")),),
        ),

      ) # CalculationMode end
      
    )
  )
)


# Server ------------------------------------------------------------------

server <- function(input, output) {

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
  #  reactive values
  rv <- reactiveValues(
    process = NULL,
    running = FALSE,
    start_time = NULL
  )
  # Real-time log monitoring
  output$log <- renderText({
    invalidateLater(1000)  # Refresh every second
    if(file.exists(log_file)) readLines(log_file) else "Waiting log..."
  })
  # Start Java process
  observeEvent(input$run, {
    req(!rv$running)
    # Clear old files
    if(file.exists(log_file)) file.remove(log_file)
    if(file.exists(result_file)) file.remove(result_file)
    # Build command
    cmd <- paste(
      input$java_path,
      "-jar",
      shQuote(input$jar_path),
      ">", log_file,
      "2>&1",  # Capture standard error and standard output
      "&& echo 'PROCESS_COMPLETED' >", result_file
    )
    # 异步执行
    rv$running <- TRUE
    rv$start_time <- Sys.time()
    future({
      system(cmd, intern = FALSE, wait = TRUE)
    }) %...>% 
      {
        rv$running <- FALSE
      } %...!% 
      {
        rv$running <- FALSE
        showNotification("Execution error!", type = "error")
      }
  })
  # Stop process
  observeEvent(input$stop, {
    if(rv$running && !is.null(rv$process)){
      tools::pskill(rv$process$get_pid())
      rv$running <- FALSE
    }
  })
  # Display status information
  output$status <- renderText({
    if(rv$running){
      elapsed <- difftime(Sys.time(), rv$start_time, units = "secs")
      paste("Running... Time elapsed:", round(elapsed), "s")
    } else {
      "Ready state"
    }
  })
  # Display the final result
  output$results <- renderText({
    req(file.exists(result_file))
    readLines(result_file)
  })
  
}

  

shinyApp(ui, server)