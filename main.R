

# 定义需要检查的包列表
packages <- c(
  "shiny", "bs4Dash", "dplyr", "readr", "plotly", "leaflet", "DT", 
  "fresh", "future", "promises", "RSQLite", "processx", "jsonlite", 
  "ggplot2", "markdown", "tidyr", "ggsci", "shinyFiles"
)

# Check and install missing packages
for (pkg in packages) {
  if (!requireNamespace(pkg, quietly = TRUE)) {
    install.packages(pkg, dependencies = TRUE)
    cat(paste("Installed packages:", pkg, "\n"))
  } 
}

# Load all packages
lapply(packages, library, character.only = TRUE)


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


# load Rscript ------------------------------------------------------------
source("lib/plot/averagePlot.R")
source("lib/processDB.R")
source("lib/processJSON.R")
source("lib/pages/aboutUS.R")
source("lib/plot/mainPlot.R")
source("lib/plot/plotThemeSet.R")
source("lib/processCalculateParameters.R")

# Load data ---------------------------------------------------

# Input parameters --------------------------------------------------------
json_file <- "NGSViz_setting.json"
# db_file <- '/Users/bencheye/myProj/ngsPlot/NGSViz/database/genomeCoordinate.db'
log_name <- "NGSViz_java_running.log"
color_panel_list <- c("NPG" = "npg", "AAAS" = "aaas", "NEJM" = "nejm",
                      "Lancet" = "lancet", "JAMA" = "jama")

# run java ----------------------------------------------------------------
java_res <- getToolJsonPara(json_file)
cmd <- java_res[1]
args <- c(java_res[2], java_res[3])
db_file <- java_res[4]
# Enable asynchronous execution
plan(multisession)  

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
    rightUi = NULL,
    h4("    NGSViz | Coverage Calculation & Visualization")
  ),
  
  # Sidebar ----
  sidebar = dashboardSidebar(
    # Default collapse sidebar
    collapsed = FALSE,
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
    right = "NGSViz"
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

      # aboutUS -----------------------------------------------------------------
      aboutUS(),

      # helpMe ------------------------------------------------------------------
      
      
      
      # CalculationMode ---------------------------------------------------------
      tabItem(
        tabName = "CalculationMode",

        # required parameters -----------------------------------------------------
        # Required parameters
        box(
          title = " | Required parameters",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("tools"),
          fluidRow(
            column(6, selectInput("dashG", "Choose a Genome type",
                                  choices = result$Genome)),
            column(6, textInput("dashI", "Input a bam/txt file!:", value = "")),
          ),
          fluidRow(
            column(6, selectInput("dashR", "Choose a region type", 
                                  choices = c("tss", "tes", "genebody"))),
            column(6,textInput("dashO", "Type Output directory:", value = "."))
          )
        ),
        
        # run java ----------------------------------------------------------------
        box(
          title = " | Run CalculationMode jar script",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("play"),
          fluidRow(
            column(4, actionButton("run_java", "Start analysis")),
            #column(6, actionButton("stop_java", "Stop script"))
            # column(4,textOutput("static_text")),
            column(4,textOutput("statusText"))
            # column(4,verbatimTextOutput("status"))
          )
        ),
        
        
        # optional parameters -----------------------------------------------------
        box(
          title = " | Optional parameters",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("wrench"),
          collapsible = TRUE,
          collapsed = TRUE,
          fluidRow(
            column(3, selectInput("dashA", "Choose a analysis type", 
                                  choices = c("transcript", "exon"))),
            column(3, uiOutput("dynamic_ui")), # dashB
            column(6, numericInput("dashF", "Input the flanking region size:", 
                                   value = 2000, min = 1, max = 3000))
          ),
          fluidRow(
            column(3, selectInput("dashM", "Choose the scale method", 
                                  choices = c("bin", "spline"))),
            column(3, textInput("dashT", "Input title of plot:", value = "AverageCoverage")),
            column(6, textInput("dashX", "Group: gene list file:", value = "all"))
          ),
          fluidRow(
            column(3, numericInput("dashMQ", "MAPQ", 
                                   value = 20, min = 0, max = 100)),
            column(3, numericInput("dashP", "Number of core", 
                                   value = 1, min = 1, max = 100)),
            column(6, numericInput("dashFL", "Fragment length", 
                                   value = 150, min = 50, max = 1000))
          ),
          fluidRow(
            column(3, numericInput("dashN", "Flanking size factor", 
                                   value = 0, min = 0, max = 1)),
            column(3, numericInput("dashRB", "Both ends trimmed (%)", 
                                   value = 0, min = 0, max = 1)),
            column(6, selectInput("dashSS", "Choose the read strand in bam file!",
                                  choices = c("both", "+", "-")))
          ),
          fluidRow(
            column(3, numericInput("dashS", "Random sampling rate", 
                                   value = 0, min = 0, max = 1)),
            column(3, numericInput("dashCZ", "Gene chunk size", 
                                   value = 100, min = 0, max = 1000)),
            column(6, selectInput("dashD", "Reference sequence database", 
                                  choices = c("RefSeq")))
          )
        ) # end for box
      ), # CalculationMode end
      
      # CoveragePlot --------------------------------------------------------------------
      tabItem(
        tabName = "CoveragePlot",
        # parse paras -------------------------------------------------------------
        box(
          title = " | Parse Paras",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("cog"),
          fluidRow(
            column(2, h5(" Start Parsing"),
                   actionButton("parse_covPlot", "Parsing parameters")),
            column(3, textInput("outputdir", " Type calculation result path", value = "")),
            column(3, textInput("calcuJson", "CalculationMode JSON", 
                                value = "NGSVir_running_config.json")),
            column(3, textInput("plotJson", "Plot JSON", 
                                value = "NGSViz_plotSetting.json"))
          )
          # fluidRow(column(4,actionButton("parse_covPlot", "Parsing parameters")))
        ),
        
          fluidRow(
            column(3,
                   box(
                     title = " | Run Plot",
                     width = 12,
                     solidHeader = TRUE,
                     status = "primary",
                     icon = icon("cog"),
                     actionButton("run_covPlot", "Start to plot"),
                     uiOutput("dynamic_sample"),
                     # choose the color
                     selectizeInput("palette_choice", "Choose color panel:", 
                                    choices = color_panel_list),
                     selectizeInput("selected_colors", "choose the colors:", 
                                    choices = NULL, multiple = TRUE,
                                    options = list(render = I('{
                     item: function(item, escape) {
                       return "<div style=\'background-color:" + escape(item.value) + "; color: white; padding: 5px; border-radius: 3px;\'>" + escape(item.label) + " </div>";
                     },
                     option: function(item, escape) {
                       return "<div style=\'background-color:" + escape(item.value) + "; color: white; padding: 5px; border-radius: 3px;\'>" + escape(item.label) + " </div>";
                     }
                   }')))
                   ) # end for box
            ),
            column(9, 
                     box(
                       title = " | Average Plot Result",
                       width = 12,
                       solidHeader = TRUE,
                       status = "primary",
                       icon = icon("chart-line"),
                       plotOutput("covagePlot")
                     )
            ),
            
            
          ) # end for fluidRow
      ) # end for tabItem
      
    ) # tabItems
  ) # body
) # UI


# Server ------------------------------------------------------------------

server <- function(input, output, session) {
  
  # initial state of run java script
  run_status <- reactiveVal("Ready to run")
  # Rendering status text
  output$statusText <- renderText({
    # When the run_status changes, this output will automatically update.
    run_status() 
  })
  
  # shared value
  shared_values <- reactiveValues(plot_paras = NULL, sample_df = NULL)

  cat("Processing begins...\n")
# dynamically generate options for dashB based on the value of das --------
  output$dynamic_ui <- renderUI({
    result <- getBiotype(db_file, input$dashG)
    selectInput("dashB", "Choose a gene element type:", 
                choices = result)
  })
  
  # output$static_text <- renderText({
  #   "\nRunning status: "
  # })
  
  # # Display the selected value
  # output$selected_value <- renderText({
  #   paste("You selected:", input$dashB)
  # })

# Run Java ----------------------------------------------------------------
  rv <- reactiveValues(
    process = NULL,
    running = FALSE,
    start_time = NULL
  )
  observeEvent(input$run_java, {
    # Step 1: Button is clicked, update status to "Running"
    run_status("Running...")
    # Disable button, prevent repeated clicks
    # disable("run_java")
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
      cat("Start running java script to calculate the coverage!\n")
      system2(cmd, args) #, stdout = TRUE, stderr = TRUE
      print(cmd)
      print(args)
    }) %...>% 
      {
        rv$running <- FALSE
      } %...!% 
      {
        rv$running <- FALSE
        showNotification("Execution error!", type = "error")
      }
    
    run_status("Operation completed")
    print("Finish the analysis!")

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

# Run plot Coverage -------------------------------------------------------

# Parse configuration file ------------------------------------------------
  observeEvent(input$parse_covPlot, {
    cat("Process the json parameters!\n")
    # check
    if(is.null(input$outputdir) || input$outputdir == ""){
      showNotification("Please input the path of calculation result!", type = "error")
      return()
    }else{
      if (!dir.exists(input$outputdir)) {
        showNotification("The folder does not exist!", type = "error")
        return()
      }
    }
    #shared_values$outputdir <- input$outputdir
    calcu_json <- file.path(input$outputdir, input$calcuJson)
    if(!file.exists(calcu_json)){
      showNotification("The json file of calculation parameters not exist!", type = "error")
      return()
    }
    
    # get calculationMode result json
    calcu_res <- getCalcuJsonRes(calcu_json)
    plot_setting <- getPlotJsonParas(input$plotJson)
    uniq_sample <- calcu_res[["uniq_sample"]]
    shared_values$sample_df <- calcu_res[["sample_df"]]
    plot_paras <- calcu_res[["plot_paras"]]
    plot_paras[["out_dir"]] <- input$outputdir
    plot_paras[["uniq_sample"]] <- uniq_sample
    # merge the two paras list
    plot_paras <- c(plot_paras, plot_setting)
    shared_values$plot_paras <- plot_paras
    output$dynamic_sample <- renderUI({
      selectInput("samples", "Choose the samples", 
                  choices = uniq_sample, multiple = TRUE)
    })
    
  }) # end for observeEvent parse_covPlot
  
  

# plot coverage -----------------------------------------------------------
  observe({
    palette_func <- switch(input$palette_choice,
                           "npg" = pal_npg,
                           "aaas" = pal_aaas,
                           "nejm" = pal_nejm,
                           "lancet" = pal_lancet,
                           "jama" = pal_jama)
    
    updateSelectizeInput(session, "selected_colors", 
                         choices = palette_func()(9), 
                         server = TRUE)
  })
  
  observeEvent(input$run_covPlot, { 
    cat("Average plot!\n")
    # parameters
    plot_paras <- shared_values$plot_paras
    sample_df <- shared_values$sample_df
    selected_samples <- input$samples
    
    plot_paras[["selected_colors"]] <- input$selected_colors
    
    if (is.null(selected_samples)) {
      shinyalert("No samples selected", type = "error")
      return()
    } 
    print(plot_paras)
    # Parse configuration file
    output$covagePlot <- renderPlot({
      
      p <- mainPlot(sample_df, plot_paras)
      p
      
      #plot(1:input$param1, 1:input$param2, main = "")
      
    }) # end for covagePlot
    
    })  # end for observeEvent run_Covplot
  
  
  
}



shinyApp(ui, server)