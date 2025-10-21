
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


packages <- c(
  "shiny", "bs4Dash", "dplyr", "readr", "plotly", "leaflet", "DT", 
  "fresh", "future", "promises", "RSQLite", "processx", "jsonlite", 
  "ggplot2", "markdown", "tidyr", "ggsci", "shinyFiles", "circlize",
  "here", "corrplot", "ggrepel", "reshape2", "aplot",
  "ggh4x", "tidyverse", "rtracklayer", "colorspace", "zoo", "corrplot", 
  "RColorBrewer", "purrr", "data.table", "RcppRoll", "ggrepel", "cowplot",
  "FactoMineR"
)

# # Load all packages
# lapply(packages, library, character.only = TRUE)
checkRequiredPackages(packages)

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

# Load data ---------------------------------------------------

# Input parameters --------------------------------------------------------
tool_dir <- here()
print(sprintf("The root path of ngsViz is : %s", tool_dir))
json_file <- here("NGSViz_setting.json")
ngsViz_log <- "img/NGSViz_log.png"
help_text <- here("README.md")
split_color_panel <- c(
  "NPG" = "npg", 
  "AAAS" = "aaas", 
  "NEJM" = "nejm",
  "Lancet" = "lancet", 
  "JAMA" = "jama"
)
sample_color_panel <- c(
  "Pastel1" = "Pastel1",
  "Pastel2" = "Pastel2",
  "Paired" = "Paired",
  "Dark2" = "Dark2",
  "Accent" = "Accent",
  "Set1" = "Set1", 
  "Set2" = "Set2",
  "Set3" = "Set3"
)
file_type_list <- c("tiff", "png", "pdf", "jpeg")

# load Rscript ------------------------------------------------------------
source(here("www/aboutUS.R"))
source(here("lib/processDB.R"))
source(here("lib/processJSON.R"))
source(here("lib/coverageHeatmap.R"))
source(here("lib/qualityControlViz.R"))
source(here("lib/processCalculateParameters.R"))

# run java ----------------------------------------------------------------
java_res <- getToolJsonPara(json_file)

java_path <- java_res[1]
jar_path <- java_res[3]
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
  help = FALSE,
  fullscreen = TRUE,
  scrollToTop = TRUE,
  
  # Header ----
  header = dashboardHeader(
    status = "lime",
    title = dashboardBrand(
      title = "NGSViz",
      color = "olive",
      image = ngsViz_log
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
        menuSubItem("SingleCoveragePlot", tabName = "SingleCoveragePlot", icon = icon("chart-line")),
        menuSubItem("MultiSamplePlot", tabName = "MultiSamplePlot", icon = icon("chart-line")),
        menuSubItem("QCPlot", tabName = "QCPlot", icon = icon("chart-line"))
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
          btn_name = NULL,
          lead = "Exploring Signal Enrichment of Functional Elements in Genomic Regions of Interest",
          href = "https://github.com/shaolab-UM/NGSViz",
          p(
            "NGSViz is an analysis and visualization tool for high-throughput sequencing (NGS) data,
            supporting input files in BAM format, integrating an efficient Java computing engine,
            flexible R visualization scripts, and an interactive R Shiny interface, 
            providing a variety of commonly used built-in genomic functional element databases,
            suitable for various scenarios such as genomic coverage and enrichment analysis."
          )
        )
      ),
      
      # aboutUS -----------------------------------------------------------------
      aboutUS(),
      
      # help ------------------------------------------------------------------
      tabItem(
        tabName = "Help",
        # parse paras ----------------------------------------------------------
        box(
          title = " | Help",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("cog"),
          fluidRow(
            mainPanel(
              htmlOutput("markdown_content")
            )
          )
        )
      ), # end for help
      
      
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
            column(4, selectInput("dashG", "Choose a reference genome",
                                  choices = result$Genome)),
            column(4, selectInput("dashR", "Choose a region type", 
                                  choices = c("tss", "tes", "genebody", "exon"))),
            column(4, textInput("dashT", "Type the analysis title:", value = "")),
          ),
          fluidRow(
            column(6, textInput("dashI", "Path to a bam file:", value = "")),
            column(6,textInput("dashO", "Path to output directory:", value = ""))
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
          collapsed = FALSE,
          fluidRow(
            column(4, numericInput("dashBS", "Batch size for genes parallel",
                                   value = 500, min = 100, max = 2000)),
            column(4, uiOutput("dynamic_ui")), # dashB
            
            column(4, numericInput("dashF", "Input the flanking region size:", 
                                   value = 2000, min = 1, max = 3000))
          ),
          fluidRow(
            column(4, numericInput("dashN", "Flanking size factor", 
                                   value = 0, min = 0, max = 1)),
            column(4, numericInput("dashS", "scale ratio (spike-in calibration)", 
                                   value = 1, min = 0, max = 1)),
            column(4, numericInput("dashDP", "Number of datapoints",
                                   value = 100, min = 0, max = 1000))
          ),
          fluidRow(
            column(4, numericInput("dashMQ", "MAPQ", 
                                   value = 20, min = 0, max = 100)),
            column(4, numericInput("dashP", "Number of CPU cores to use", 
                                   value = 1, min = 1, max = 100)),
            column(4, numericInput("dashFL", "Fragment length", 
                                   value = 150, min = 50, max = 1000))
          ),
          fluidRow(
            column(4, textInput("dashCP", "Location of user-defined system config",
                                value = "-")),
            column(4, textInput("dashDB", "Path to new database be merged into tool's DB:", value = "-")),
            column(4, textInput("dashBD", "Path to custom-built reference database:", value = "-"))
          )
        ), # end for box
        
        # run java ----------------------------------------------------------------
        box(
          title = " | Run CalculationMode jar script",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("play"),
          fluidRow(
            column(4, actionButton("run_java", "Start analysis")),
            column(4,textOutput("statusText"))
          )
        )# end for box
        
      ), # CalculationMode end
      
      # SingleCoveragePlot --------------------------------------------------------------------
      tabItem(
        tabName = "SingleCoveragePlot",
        # parse paras ----------------------------------------------------------
        box(
          title = " | Parse Paras",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("cog"),
          fluidRow(
            column(4, h5(" Start Parsing"),
                   actionButton("parse_covPlot", "Parsing parameters")),
            column(4, textInput("outputdir", " Type JSON config path", value = ""))
          )
        ),
        # adjust plot paras ----------------------------------------------------
        box(
          title = " | Default Parameters for Plot",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("cog"),
          # output
          hr(), # Add horizontal separator
          h5("Output Plot Parameters"),
          hr(), # Add horizontal separator
          fluidRow(
            column(4, numericInput("high", "High for output plot:",
                                   value = 12)),
            column(4, numericInput("width", "Width for output plot:",
                                   value = 10)),
            column(4, selectInput("file_type", "File type for output plot:",
                                  choices = file_type_list,
                                  selected = "tiff"))
          ),
          fluidRow(
            column(4, numericInput("dpi", "Output dpi:",
                                   value = 300)),
            column(4, textInput("top_bottom_ratio", "Input the top_bottom_ratio:",
                                value = "1:3")),
            column(4, textInput("SEM", "Whether show SEM:",
                                value = TRUE))
          ),
          fluidRow(
            column(4, textInput("smooth", "Whether smooth the plot:",
                                   value = FALSE))
          ),
          
          # split
          hr(), # Add horizontal separator
          h5("Gene Split Parameters"),
          hr(), # Add horizontal separator
          fluidRow(
            column(4, selectInput("dist_m", "dist_m:",
                                  choices = c("euclidean", "manhattan", "maximum"),
                                  selected = "euclidean")),
            column(4, numericInput("cluster_num", "Number of cluster :",
                                   value = 1)),
            column(4, selectInput("hc_m", "hc_m:",
                                  choices = c("ward.D", "median", "average"),
                                  selected = "ward.D"))
          ),
          
          hr(), # Add horizontal separator
          h5("Theme Parameters"),
          hr(), # Add horizontal separator
          # 
          fluidRow(
            column(4, numericInput("line_size", "Average plot line size:",
                                   value = 0.8)),
            column(4, numericInput("dash_size", "Dash size:",
                                   value = 0.6)),
            column(4, textInput("dash_color", "Dash color:",
                                value = "black"))
          ),
          
          fluidRow(
            column(4, numericInput("title_size", "Title size :",
                                   value = 11)),
            column(4, textInput("title_color", "Title color:",
                                value = "black")),
            column(4, numericInput("title_hjust", "title_hjust:",
                                   value = 0.5))
          ),
          
          fluidRow(
            column(4, numericInput("axis_text_x_size", "Axis_text_x_size:",
                                   value = 10)),
            column(4, numericInput("axis_text_y_size", "Axis_text_y_size:",
                                   value = 10)),
            column(4, numericInput("legend_text_size", "legend_text_size:",
                                   value = 10))
          ),
          
          fluidRow(
            column(4, numericInput("legend_size", "legend_size:",
                                   value = 10)),
            column(4, numericInput("border_size", "border_size:",
                                   value = 1.2)),
            column(4, selectInput("text_face", "text_face:",
                                  choices = c("bold", "plain"),
                                  selected = "bold"))
          ),
          
          # choose the sample color
          fluidRow(
            column(4, selectInput("sample_color_panel",
                                  "Select a color palette for sample:",
                                  choices = sample_color_panel,
                                  selected = "Pastel1")),
            column(4, uiOutput("sample_color_checkboxes")),
            column(4, uiOutput("selected_sample_color_display"))
          ),
          # # choose the sample color
          fluidRow(
            column(4, selectInput("split_color_panel",
                                  "Select a color palette for genes split:",
                                  choices = split_color_panel,
                                  selected = "Pastel1")),
            column(4, uiOutput("split_color_checkboxes")),
            column(4, uiOutput("selected_split_color_display"))
          )
        ), # end for box
        
        
        # plot  ----------------------------------------------------
        fluidRow(
          box(
            title = " | Start to Plot",
            width = 4,
            solidHeader = TRUE,
            status = "primary",
            icon = icon("chart-line"),
            actionButton("run_covPlot", "Run plot")
          ),
          box(
            title = " | Show Plot Result",
            width = 8,
            solidHeader = TRUE,
            status = "primary",
            icon = icon("chart-line"),
            plotOutput("covagePlot")
          )
        )
      ), # end for SingleCoveragePlot
      

# start MultiSamplePlot ---------------------------------------------------
      
      tabItem(
        tabName = "MultiSamplePlot",
        # parse paras ----------------------------------------------------------
        box(
          title = " | Parse Paras",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("cog"),
          fluidRow(
            column(4, h5(" Start Parsing to Plot"),
                   actionButton("parse_multiSamplePlot", "Parsing Json config files")),
            column(4, textInput("json_files_path", " Type direction of JSON config path", value = ""))
          )
        ),
        
        # plot  ----------------------------------------------------
        fluidRow(
          box(
            title = " | Multi-Sample Average Coverage Plot",
            width = 12,
            solidHeader = TRUE,
            status = "primary",
            icon = icon("chart-line"),
            plotOutput("multiSampleAveCovPlot")
          )
        ),
        fluidRow(
          box(
            title = " | Multi-Sample Integrate Coverage Plot",
            width = 12,
            solidHeader = TRUE,
            status = "primary",
            icon = icon("chart-line"),
            plotOutput("multiSampleIntegratedCovPlot")
          )
        )
        
      ), # end for multiSamplePlot

# QCPlot ------------------------------------------------------------------
tabItem(
  tabName = "QCPlot",
  # parse paras ----------------------------------------------------------
  box(
    title = " | Parse Paras",
    width = 12,
    solidHeader = TRUE,
    status = "primary",
    icon = icon("cog"),
    fluidRow(
      column(4, h5(" Start Parsing to Plot"),
             actionButton("parse_QCPlot", "Parsing Json config files")),
      column(4, textInput("qc_json_path", " Type direction of JSON config path", value = ""))
    )
  ),
  
  # plot  ----------------------------------------------------
  fluidRow(
    box(
      title = " | Coverage PCA Plot",
      width = 12,
      solidHeader = TRUE,
      status = "primary",
      icon = icon("chart-line"),
      plotOutput("pcaPlot")
    )
  ),
  fluidRow(
    box(
      title = " | Coverage Correlation Plot",
      width = 12,
      solidHeader = TRUE,
      status = "primary",
      icon = icon("chart-line"),
      plotOutput("corrPlot")
    )
  )
  
) # end for QCPlot
      
    ) # tabItems
  ) # body
) # UI


# Server ------------------------------------------------------------------

server <- function(input, output, session) {
  
  # # initial state of run java script
  run_status <- reactiveVal("Ready to run")
  
  # # Rendering status text
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
  
  # Run Java ----------------------------------------------------------------
  observeEvent(input$run_java, {
    # Retrieve the value of the dashB parameter currently selected by the user.
    selected_dashB <- input$dashB
    req(selected_dashB)
    print("Get the parameter")
    print(sprintf("Select the dasb is %s", selected_dashB))
    # Step 1: Button is clicked, update status to "Running"
    run_status("Running...")
    # Disable button, prevent repeated clicks
    para_list <- list(
      G=input$dashG, R=input$dashR, I=input$dashI,
      O=input$dashO, "T"=input$dashT, 
      DB=input$dashDB, CP=input$dashCP, # X=input$dashX, 
      BS=input$dashBS, B=selected_dashB, "F"=input$dashF, 
      N=input$dashN, DP=input$dashDP, S=input$dashS,
      MQ=input$dashMQ, BD=input$dashBD, # M=input$dashM,
      FL=input$dashFL,  P=input$dashP # SS=input$dashSS,
    )
    
    excution_res <- executeNgsvizJar(java_path, jar_path, para_list)
    print(excution_res)
    run_status("Operation completed")
    print("Finish the analysis!")
    
  }) # end for observeEvent Java run
  
  
  # Run plot Coverage -------------------------------------------------------
  
  # Parse configuration file ------------------------------------------------
  observeEvent(input$parse_covPlot, {
    cat("Process the json parameters!\n")
    # check
    if(is.null(input$outputdir) || input$outputdir == ""){
      showNotification("Please input the path of calculation result!", type = "error")
      return()
    }else{
      if (!file.exists(input$outputdir)) {
        showNotification("The folder does not exist!", type = "error")
        return()
      }
    }
    
    #plot_json <- file.path(input$outputdir, input$plotJson)
    plot_json <- input$outputdir
    if(!file.exists(plot_json)){
      showNotification("The json file of plot json parameters not exist!", 
                       type = "error")
      return()
    }
    
    print(sprintf("Starting process the plot parameters: %s", plot_json))
    # get calculationMode result json
    plot_paras <- getPlotJsonParas(plot_json)
    print("Finish processing the plot parameters.")
    # plot_paras <- getPlotJsonParas(plot_para_file)
    uniq_sample <- plot_paras$sample_list
    shared_values$sample_df <- uniq_sample

    # # merge the two paras list
    shared_values$plot_paras <- plot_paras
    output$dynamic_sample <- renderUI({
      selectInput("samples", "Choose the samples", 
                  choices = uniq_sample, multiple = TRUE)
    })
    
  }) # end for observeEvent parse_covPlot
  
  
  # plot coverage -----------------------------------------------------------
  
  # choose split color --------------------------------------------------------
  split_reactive_colors <- reactive({
    color_num <- 6
    # Ensure that input$split_color_panel is not empty
    req(input$split_color_panel) 
    panel_name <- input$split_color_panel
    # Retrieve the ggsci palette function and determine its maximum number of colors
    palette_func_name <- paste0("pal_", panel_name)
    if (!exists(palette_func_name, mode = "function", where = asNamespace("ggsci"))) {
      stop(paste0("ggsci Palette function not found in package. ", palette_func_name))
    }
    # Retrieve the maximum number of colors in the ggsci palette
    max_colors_ggsci <- switch(panel_name,
                               "npg" = 10,
                               "aaas" = 10,
                               "nejm" = 8,
                               "lancet" = 9,
                               "jama" = 7,
                               6)
    n_to_display <- min(color_num, max_colors_ggsci)
    colors <- get(palette_func_name, asNamespace("ggsci"))(alpha = 1)(n_to_display)
    # From RColorBrewer get colors
    # colors <- RColorBrewer::brewer.pal(n_to_display, panel_name)
    # Use color values as names to display color values in checkboxGroupInput
    names(colors) <- colors
    return(colors)
  })
  # output$split_color_checkboxes The generation will be based on the result of split_reactive_colors().
  output$split_color_checkboxes <- renderUI({
    split_colors_to_display <- split_reactive_colors()
    # Create a multi-select checkbox group
    checkboxGroupInput(
      "selected_split_colors",
      "Select the color to be used: ",
      choices = split_colors_to_display,
      selected = unname(split_colors_to_display)[1:3]
    )
  })
  
  # Render the display of selected colors as small color blocks
  output$selected_split_color_display <- renderUI({
    req(input$selected_split_colors) # Ensure that input$selected_sample_colors is not empty
    selected_vals <- input$selected_split_colors
    if (length(selected_vals) == 0) {
      return(p("Please select the color on the left."))
    }
    # Create a small square for each selected color.
    div_tags <- lapply(selected_vals, function(color_code) {
      tags$div(
        style = paste0("display: inline-block; width: 50px; height: 50px; background-color: ", color_code, "; border: 1px solid #ccc; margin: 5px; vertical-align: middle;"),
        title = color_code # Mouse hover to display color code
      )
    })
    # Merge all color blocks into one tagList
    do.call(tagList, div_tags)
  })
  
  # choose sample color --------------------------------------------------------
  # When the input$sample_color_panel changes, this expression will be recalculated.
  sample_reactive_colors <- reactive({
    color_num <- 6
    # Ensure that input$sample_color_panel is not empty
    req(input$sample_color_panel) 
    panel_name <- input$sample_color_panel
    # Get the maximum number of colors on the selected panel
    pal_info <- brewer.pal.info[panel_name, ]
    n_colors_available <- pal_info$maxN
    n_to_display <- min(color_num, n_colors_available)
    # From RColorBrewer get colors
    colors <- RColorBrewer::brewer.pal(n_to_display, panel_name)
    # Use color values as names to display color values in checkboxGroupInput
    names(colors) <- colors
    return(colors)
  })
  # output$sample_color_checkboxes The generation will be based on the result of sample_reactive_colors().
  
  output$sample_color_checkboxes <- renderUI({
    sample_colors_to_display <- sample_reactive_colors()
    # Create a multi-select checkbox group
    checkboxGroupInput(
      "selected_sample_colors",
      "Select the color to be used: ",
      choices = sample_colors_to_display,
      selected = unname(sample_colors_to_display)[1]
    )
  })
  
  # Render the display of selected colors as small color blocks
  output$selected_sample_color_display <- renderUI({
    req(input$selected_sample_colors) # Ensure that input$selected_sample_colors is not empty
    selected_vals <- input$selected_sample_colors
    if (length(selected_vals) == 0) {
      return(p("Please select the color on the left."))
    }
    # Create a small square for each selected color.
    div_tags_sample <- lapply(selected_vals, function(color_code) {
      tags$div(
        style = paste0("display: inline-block; width: 50px; height: 50px; background-color: ", color_code, "; border: 1px solid #ccc; margin: 5px; vertical-align: middle;"),
        title = color_code # Mouse hover to display color code
      )
    })
    # Merge all color blocks into one tagList
    do.call(tagList, div_tags_sample)
  })
  
  
  # plot result --------------------------------------------------------
  observeEvent(input$run_covPlot, { 
    cat("Average plot!\n")
    # parameters
    plot_paras <- shared_values$plot_paras
    
    # update the plot parameters ----------------------------------------------
    plot_paras[["high"]] <- input$high
    plot_paras[["width"]] <- input$width
    plot_paras[["dpi"]] <- input$dpi
    plot_paras[["file_type"]] <- input$file_type
    plot_paras[["SEM"]] <- input$SEM
    plot_paras[["smooth"]] <- input$file_type
    tb_ratio <- strsplit(input$top_bottom_ratio, ":")[[1]] %>% as.numeric()
    # print(sprintf("The tb ratio is the %s,", tb_ratio))
    plot_paras[["top_bottom_ratio"]] <- tb_ratio
    # split_paras
    plot_paras[["hc_m"]] <- input$hc_m
    plot_paras[["dist_m"]] <- input$dist_m
    plot_paras[["cluster_num"]] <- input$cluster_num
    # plot_paras[["split_mode"]] <- input$split_mode
    # plot_paras[["sub_gene_list"]] <- input$sub_gene_list
    # plot_paras[["gene_list_file"]] <- input$gene_list_file
    # theme_paras
    plot_paras[["title_size"]] <- input$title_size
    plot_paras[["legend_size"]] <- input$legend_size
    plot_paras[["title_color"]] <- input$title_color
    plot_paras[["dash_color"]] <- input$dash_color
    plot_paras[["axis_text_x_size"]] <- input$axis_text_x_size
    plot_paras[["axis_text_y_size"]] <- input$axis_text_y_size
    plot_paras[["dash_size"]] <- input$dash_size
    plot_paras[["text_face"]] <- input$text_face
    plot_paras[["title_hjust"]] <- input$title_hjust
    plot_paras[["border_size"]] <- input$border_size
    plot_paras[["legend_text_size"]] <- input$legend_text_size
    plot_paras[["line_size"]] <- input$line_size
    plot_paras[["ystrip_text_size"]] <- input$ystrip_text_size
    
    # Ensure that a color is selected
    req(input$selected_sample_colors) 
    plot_paras[["sample_color"]] <- input$selected_sample_colors
    print("The color selected by the user has been updated:")
    print(plot_paras[["sample_color"]])
    req(input$selected_split_colors) 
    plot_paras[["split_color"]] <- input$selected_split_colors
    print("The color selected by the user has been updated:")
    print(plot_paras[["split_color"]])
    
    print(plot_paras)
    # Parse configuration file
    output$covagePlot <- renderPlot({
      
      p <- plotOneIntegratePlot(plot_paras)
      p
      
    }) # end for covagePlot
    
  })  # end for observeEvent run_Covplot

# multisamplePlot ---------------------------------------------------------

# multiSampleAverageCoveragePlot ------------------------------------------
  observeEvent(input$parse_multiSamplePlot, { 
    cat("Multi sample average coverage plot!\n")
    
    cat("Process the json parameters!\n")
    # check
    if(is.null(input$json_files_path) || input$json_files_path == ""){
      showNotification("Please input the path of json config dir path!", type = "error")
      return()
    }else{
      if (!dir.exists(input$json_files_path)) {
        showNotification("The folder does not exist!", type = "error")
        return()
      }
    }

    json_path <- input$json_files_path
    cat("Start to plot the multi sample average coverage plot!\n")
    # Parse configuration file
    output$multiSampleAveCovPlot <- renderPlot({
      p2 <- mergeAveragePlot(json_path)
      p2
    }) 
    cat("Start to plot the multi sample integrated coverage plot!\n")
    output$multiSampleIntegratedCovPlot <- renderPlot({
      p3 <- mergeMultiVisResult(json_path)
      p3
    }) 
  }) 

# QCPlot ------------------------------------------------------------------
  observeEvent(input$parse_QCPlot, { 
    cat("QC plot!\n")
    cat("Process the json parameters!\n")
    # check
    if(is.null(input$qc_json_path) || input$qc_json_path == ""){
      showNotification("Please input the path of json config dir path!", type = "error")
      return()
    }else{
      if (!dir.exists(input$qc_json_path)) {
        showNotification("The folder does not exist!", type = "error")
        return()
      }
    }
    
    json_path <- input$qc_json_path
    heatmapMat_rowMerge <- propressData(json_path)
    cat("Start to plot the multi sample average coverage plot!\n")
    # Parse configuration file
    output$pcaPlot <- renderPlot({
      p4 <- runPCA(heatmapMat_rowMerge, json_path)
      p4
    }) 
    cat("Start to plot the multi sample integrated coverage plot!\n")
    output$corrPlot <- renderPlot({
      corPlot(json_path, heatmapMat_rowMerge)
    }) 
  }) 

# Help --------------------------------------------------------------------
  output$markdown_content <- renderUI({
    md_file <- help_text
    # Convert Markdown to HTML
    html_content <- markdownToHTML(file = md_file, fragment.only = TRUE)
    HTML(html_content)
  })
}

shinyApp(ui, server)