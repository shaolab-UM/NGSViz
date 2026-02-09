build_home_tab <- function() {
  tabItem(
    tabName = "home",
    jumbotron(
      title = "Welcome to use NGSViz!",
      status = "info",
      btn_name = NULL,
      lead = "Exploring Signal Enrichment of Functional Elements in Genomic Regions of Interest",
      href = "https://github.com/shaolab-UM/NGSViz",
      p(
        "NGSViz is an analysis and visualization tool for high-throughput sequencing (NGS) data,",
        "supporting input files in BAM format, integrating an efficient Java computing engine,",
        "flexible R visualization scripts, and an interactive R Shiny interface,",
        "providing a variety of commonly used built-in genomic functional element databases,",
        "suitable for various scenarios such as genomic coverage and enrichment analysis."
      )
    )
  )
}

build_help_tab <- function() {
  tabItem(
    tabName = "Help",
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
  )
}

build_calculation_tab <- function(app_context) {
  tabItem(
    tabName = "CalculationMode",
    box(
      title = " | Required parameters",
      width = 12,
      solidHeader = TRUE,
      status = "primary",
      icon = icon("tools"),
      fluidRow(
        column(4, selectInput("dashG", "Choose a reference genome", choices = app_context$genome_result$Genome)),
        column(4, selectInput("dashR", "Choose a region type", choices = c("tss", "tes", "genebody", "exon"))),
        column(4, textInput("dashT", "Type the analysis title:", value = ""))
      ),
      fluidRow(
        column(6, textInput("dashI", "Path to a bam file:", value = "")),
        column(6, textInput("dashO", "Path to output directory:", value = ""))
      )
    ),
    box(
      title = " | Optional parameters",
      width = 12,
      solidHeader = TRUE,
      status = "primary",
      icon = icon("wrench"),
      collapsible = TRUE,
      collapsed = FALSE,
      fluidRow(
        column(4, numericInput("dashBS", "Batch size for genes parallel", value = 500, min = 100, max = 2000)),
        column(4, uiOutput("dynamic_ui")),
        column(4, numericInput("dashF", "Input the flanking region size:", value = 2000, min = 1, max = 3000))
      ),
      fluidRow(
        column(4, numericInput("dashN", "Flanking size factor", value = 0, min = 0, max = 1)),
        column(4, numericInput("dashS", "scale ratio (spike-in calibration)", value = 1, min = 0, max = 1)),
        column(4, numericInput("dashDP", "Number of datapoints", value = 100, min = 0, max = 1000))
      ),
      fluidRow(
        column(4, numericInput("dashMQ", "MAPQ", value = 20, min = 0, max = 100)),
        column(4, numericInput("dashP", "Number of CPU cores to use", value = 1, min = 1, max = 100)),
        column(4, numericInput("dashFL", "Fragment length", value = 150, min = 50, max = 1000))
      ),
      fluidRow(
        column(4, textInput("dashCP", "Location of user-defined system config", value = "-")),
        column(4, textInput("dashDB", "Path to new database be merged into tool's DB:", value = "-")),
        column(4, textInput("dashBD", "Path to custom-built reference database:", value = "-"))
      )
    ),
    box(
      title = " | Run CalculationMode jar script",
      width = 12,
      solidHeader = TRUE,
      status = "primary",
      icon = icon("play"),
      fluidRow(
        column(4, actionButton("run_java", "Start analysis")),
        column(4, textOutput("statusText"))
      )
    )
  )
}

build_single_coverage_tab <- function(app_context) {
  tabItem(
    tabName = "SingleCoveragePlot",
    box(
      title = " | Parse Paras",
      width = 12,
      solidHeader = TRUE,
      status = "primary",
      icon = icon("cog"),
      fluidRow(
        column(4, h5(" Start Parsing"), actionButton("parse_covPlot", "Parsing parameters")),
        column(4, textInput("outputdir", " Type JSON config path", value = ""))
      )
    ),
    box(
      title = " | Default Parameters for Plot",
      width = 12,
      solidHeader = TRUE,
      status = "primary",
      icon = icon("cog"),
      hr(),
      h5("Output Plot Parameters"),
      hr(),
      fluidRow(
        column(4, numericInput("high", "High for output plot:", value = 12)),
        column(4, numericInput("width", "Width for output plot:", value = 10)),
        column(4, selectInput("file_type", "File type for output plot:", choices = app_context$file_type_list, selected = "tiff"))
      ),
      fluidRow(
        column(4, numericInput("dpi", "Output dpi:", value = 300)),
        column(4, textInput("top_bottom_ratio", "Input the top_bottom_ratio:", value = "1:3")),
        column(4, selectInput("SEM", "Whether show SEM:", choices = c("TRUE", "FALSE"), selected = "TRUE"))
      ),
      fluidRow(
        column(4, selectInput("smooth", "Whether smooth the plot:", choices = c("FALSE", "TRUE"), selected = "FALSE"))
      ),
      hr(),
      h5("Gene Split Parameters"),
      hr(),
      fluidRow(
        column(4, selectInput("dist_m", "dist_m:", choices = c("euclidean", "manhattan", "maximum"), selected = "euclidean")),
        column(4, numericInput("cluster_num", "Number of cluster :", value = 1)),
        column(4, selectInput("hc_m", "hc_m:", choices = c("ward.D", "median", "average"), selected = "ward.D"))
      ),
      hr(),
      h5("Theme Parameters"),
      hr(),
      fluidRow(
        column(4, numericInput("line_size", "Average plot line size:", value = 0.8)),
        column(4, numericInput("dash_size", "Dash size:", value = 0.6)),
        column(4, textInput("dash_color", "Dash color:", value = "black"))
      ),
      fluidRow(
        column(4, numericInput("title_size", "Title size :", value = 11)),
        column(4, textInput("title_color", "Title color:", value = "black")),
        column(4, numericInput("title_hjust", "title_hjust:", value = 0.5))
      ),
      fluidRow(
        column(4, numericInput("axis_text_x_size", "Axis_text_x_size:", value = 10)),
        column(4, numericInput("axis_text_y_size", "Axis_text_y_size:", value = 10)),
        column(4, numericInput("legend_text_size", "legend_text_size:", value = 10))
      ),
      fluidRow(
        column(4, numericInput("legend_size", "legend_size:", value = 10)),
        column(4, numericInput("border_size", "border_size:", value = 1.2)),
        column(4, selectInput("text_face", "text_face:", choices = c("bold", "plain"), selected = "bold"))
      ),
      fluidRow(
        column(4, numericInput("ystrip_text_size", "ystrip_text_size:", value = 10))
      ),
      fluidRow(
        column(
          4,
          selectInput(
            "sample_color_panel",
            "Select a color palette for sample:",
            choices = app_context$sample_color_panel,
            selected = "Pastel1"
          )
        ),
        column(4, uiOutput("sample_color_checkboxes")),
        column(4, uiOutput("selected_sample_color_display"))
      ),
      fluidRow(
        column(
          4,
          selectInput(
            "split_color_panel",
            "Select a color palette for genes split:",
            choices = app_context$split_color_panel,
            selected = "npg"
          )
        ),
        column(4, uiOutput("split_color_checkboxes")),
        column(4, uiOutput("selected_split_color_display"))
      )
    ),
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
  )
}

build_multi_sample_tab <- function() {
  tabItem(
    tabName = "MultiSamplePlot",
    box(
      title = " | Parse Paras",
      width = 12,
      solidHeader = TRUE,
      status = "primary",
      icon = icon("cog"),
      fluidRow(
        column(4, h5(" Start Parsing to Plot"), actionButton("parse_multiSamplePlot", "Parsing Json config files")),
        column(4, textInput("json_files_path", " Type direction of JSON config path", value = ""))
      )
    ),
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
  )
}

build_qc_tab <- function() {
  tabItem(
    tabName = "QCPlot",
    box(
      title = " | Parse Paras",
      width = 12,
      solidHeader = TRUE,
      status = "primary",
      icon = icon("cog"),
      fluidRow(
        column(4, h5(" Start Parsing to Plot"), actionButton("parse_QCPlot", "Parsing Json config files")),
        column(4, textInput("qc_json_path", " Type direction of JSON config path", value = ""))
      )
    ),
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
  )
}

build_ngsviz_ui <- function(app_context) {
  dashboardPage(
    title = "NGSViz",
    freshTheme = app_context$app_theme,
    dark = TRUE,
    help = FALSE,
    fullscreen = TRUE,
    scrollToTop = TRUE,
    header = dashboardHeader(
      status = "lime",
      title = dashboardBrand(
        title = "NGSViz",
        color = "olive",
        image = app_context$ngsViz_log
      ),
      controlbarIcon = NULL,
      fixed = TRUE,
      rightUi = NULL,
      h4("    NGSViz | Coverage Calculation & Visualization")
    ),
    sidebar = dashboardSidebar(
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
    footer = dashboardFooter(
      left = "FHS | University of Macau",
      right = "NGSViz"
    ),
    body = dashboardBody(
      tabItems(
        build_home_tab(),
        aboutUS(),
        build_help_tab(),
        build_calculation_tab(app_context),
        build_single_coverage_tab(app_context),
        build_multi_sample_tab(),
        build_qc_tab()
      )
    )
  )
}
