

# 定义需要检查的包列表
packages <- c(
  "shiny", "bs4Dash", "dplyr", "readr", "plotly", "leaflet", "DT", 
  "fresh", "future", "promises", "RSQLite", "processx", "jsonlite", 
  "ggplot2", "markdown", "tidyr", "ggsci", "shinyFiles", "circlize",
  "EnrichedHeatmap"
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
# source("lib/plot/averagePlot.R")
source("lib/processDB.R")
source("lib/pages/aboutUS.R")
source("lib/processJSON.R")
source("lib/plot/coverageHeatmap.R")
# source("lib/plot/mainPlot.R")
# source("lib/plot/plotThemeSet.R")
# source("lib/processCalculateParameters.R")

# Load data ---------------------------------------------------

# Input parameters --------------------------------------------------------
json_file <- "NGSViz_setting.json"
# db_file <- '/Users/bencheye/myProj/ngsPlot/NGSViz/database/genomeCoordinate.db'
color_panel_list <- c("NPG" = "npg", "AAAS" = "aaas", "NEJM" = "nejm",
                      "Lancet" = "lancet", "JAMA" = "jama")
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

# run java ----------------------------------------------------------------
java_res <- getToolJsonPara(json_file)
# cmd <- java_res[1]
# args <- c(java_res[2], java_res[3])
# db_file <- java_res[4]

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
            column(6, textInput("dashCP", "System configuration path", 
                                value = "NGSViz_setting.json"))
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
            #column(6, actionButton("stop_java", "Stop script"))
            # column(4,textOutput("static_text")),
            column(4,textOutput("statusText"))
            # column(4,verbatimTextOutput("status"))
          )
        )# end for box
        
      ), # CalculationMode end
      
      
      
      # CoveragePlot --------------------------------------------------------------------
      tabItem(
        tabName = "CoveragePlot",
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
            column(4, textInput("outputdir", " Type calculation result path", value = "")),
            # column(3, textInput("calcuJson", "CalculationMode JSON", 
            #                     value = "NGSVir_running_config.json")),
            column(4, textInput("plotJson", "Plot JSON", 
                                value = "NGSViz_plotSetting.json"))
          )
        ),
        # adjust plot paras ----------------------------------------------------
        box(
          title = " | Run Plot",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("cog"),
          fluidRow(
            column(4, h5("Average Plot"), actionButton("run_covPlot", "Start to plot")),
            column(4, selectizeInput("split_palette", "Choose the color panel for split:", 
                                     choices = color_panel_list)),
            column(4, selectizeInput("split_color", "choose the color for split :", 
                                     choices = NULL, multiple = TRUE,
                                     options = list(render = I('{
             item: function(item, escape) {
               return "<div style=\'background-color:" + escape(item.value) + "; color: white; padding: 5px; border-radius: 3px;\'>" + escape(item.label) + " </div>";
             },
             option: function(item, escape) {
               return "<div style=\'background-color:" + escape(item.value) + "; color: white; padding: 5px; border-radius: 3px;\'>" + escape(item.label) + " </div>";
             }
           }')))
                   )
          ),
          
          fluidRow(
            # column(4, textInput("plotJson", "Plot JSON", value = "NGSViz_plotSetting.json")),
            column(4, selectInput("color_panel_choice",
                                  "Select a color palette for sample:",
                                  choices = sample_color_panel,
                                  selected = "Pastel1")),
            # column(6, selectizeInput("sample_palette", "Choose the color panel for samples:",
            #                          choices = sample_color_panel)),
            column(4, uiOutput("color_checkboxes")),
            column(4, uiOutput("selected_color_display"))
           #  column(6, selectizeInput("sample_color", "choose the color for sample :",
           #                           choices = NULL, multiple = TRUE,
           #                           options = list(render = I('{
           #   item: function(item, escape) {
           #     return "<div style=\'background-color:" + escape(item.value) + "; color: white; padding: 5px; border-radius: 3px;\'>" + escape(item.label) + " </div>";
           #   },
           #   option: function(item, escape) {
           #     return "<div style=\'background-color:" + escape(item.value) + "; color: white; padding: 5px; border-radius: 3px;\'>" + escape(item.label) + " </div>";
           #   }
           # }')))
           #  )
          )
        ), # end for box
         
         
        # plot  ----------------------------------------------------
        box(
          title = " | Plot Result",
          width = 12,
          solidHeader = TRUE,
          status = "primary",
          icon = icon("chart-line"),
          plotOutput("covagePlot")
          # fluidRow(column(4,actionButton("parse_covPlot", "Parsing parameters")))
        ),
      ) # end for tabItem
      
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
    # disable("run_java")
    # req(!rv$running)
    para_list <- list(
      G=input$dashG, R=input$dashR, I=input$dashI,
      O=input$dashO, X=input$dashX, "T"=input$dashT,
      A=input$dashA, B=selected_dashB, "F"=input$dashF, 
      N=input$dashN, RB=input$dashRB, S=input$dashS,
      M=input$dashM, CZ=input$dashCZ, MQ=input$dashMQ, 
      FL=input$dashFL, SS=input$dashSS, P=input$dashP,
      CP=input$dashCP
                      )

    excution_res <- execute_ngsviz_jar(java_path, jar_path, para_list)
    
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
      if (!dir.exists(input$outputdir)) {
        showNotification("The folder does not exist!", type = "error")
        return()
      }
    }
    
    plot_json <- file.path(input$outputdir, input$plotJson)
    if(!file.exists(plot_json)){
      showNotification("The json file of plot json parameters not exist!", 
                       type = "error")
      return()
    }
    
    print(sprintf("Starting process the plot parameters: %s", plot_json))
    # get calculationMode result json
    # calcu_res <- getCalcuJsonRes(calcu_json)
    # plot_paras <- getPlotJsonParas(input$plotJson)
    plot_paras <- getPlotJsonParas(plot_json)
    print("Finish processing the plot parameters.")
    # plot_paras <- getPlotJsonParas(plot_para_file)
    uniq_sample <- plot_paras$sample_list
    shared_values$sample_df <- uniq_sample
    # plot_paras <- calcu_res[["plot_paras"]]
    # plot_paras[["out_dir"]] <- input$outputdir
    # plot_paras[["uniq_sample"]] <- uniq_sample
    # # merge the two paras list
    # plot_paras <- c(plot_paras, plot_setting)
    shared_values$plot_paras <- plot_paras
    output$dynamic_sample <- renderUI({
      selectInput("samples", "Choose the samples", 
                  choices = uniq_sample, multiple = TRUE)
    })
    
  }) # end for observeEvent parse_covPlot
  
  

# plot coverage -----------------------------------------------------------
  
  # When the input$color_panel_choice changes, this expression will be recalculated.
  reactive_colors <- reactive({
    color_num <- 6
    # Ensure that input$color_panel_choice is not empty
    req(input$color_panel_choice) 
    panel_name <- input$color_panel_choice
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
  # output$color_checkboxes The generation will be based on the result of reactive_colors().
  output$color_checkboxes <- renderUI({
    colors_to_display <- reactive_colors()
    # Create a multi-select checkbox group
    checkboxGroupInput(
      "selected_colors",
      "Select the color to be used: ",
      choices = colors_to_display,
      selected = unname(colors_to_display)  
    )
  })
  
  # Render the display of selected colors as small color blocks
  output$selected_color_display <- renderUI({
    req(input$selected_colors) # Ensure that input$selected_colors is not empty
    selected_vals <- input$selected_colors
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
  
  observe({
    palette_func <- switch(input$split_palette,
                           "npg" = pal_npg,
                           "aaas" = pal_aaas,
                           "nejm" = pal_nejm,
                           "lancet" = pal_lancet,
                           "jama" = pal_jama)
    
    updateSelectizeInput(session, "split_color", 
                         choices = palette_func()(9), 
                         server = TRUE)
  })

  observeEvent(input$run_covPlot, { 
    cat("Average plot!\n")
    # parameters
    plot_paras <- shared_values$plot_paras
    # sample_df <- shared_values$sample_df
    # selected_samples <- input$samples
    
    # Ensure that a color is selected
    req(input$selected_colors) 
    plot_paras[["sample_color"]] <- input$selected_colors
    print("The color selected by the user has been updated:")
    print(plot_paras[["sample_color"]])
    
    
    # plot_paras[["sample_color"]] <- input$sample_color
    # plot_paras[["split_color"]] <- input$split_color
    # if (is.null(selected_samples)) {
    #   shinyalert("No samples selected", type = "error")
    #   return()
    # } 
    print(plot_paras)
    # Parse configuration file
    output$covagePlot <- renderPlot({
      
      p <- plotMain(plot_paras)
      p
      
      #plot(1:input$param1, 1:input$param2, main = "")
      
    }) # end for covagePlot
    
    })  # end for observeEvent run_Covplot
  
  
  
}



shinyApp(ui, server)