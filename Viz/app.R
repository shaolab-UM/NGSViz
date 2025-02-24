library(shiny)
library(bs4Dash)
library(dplyr)
library(readr)
library(plotly)
library(leaflet)
library(DT)
library(fresh)

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
        h3("Required parameters"),
        fluidRow(
          column(6,
                 selectInput("select1", "choose a Genome type", 
                             choices = result$Genome),
                 fileInput("file1", "choose the input file!")
          ),
          column(6,
                 selectInput("select2", "choose a region type", 
                             choices = c("tss", "tes", "genebody")),
                 textInput("text2", "Input Output directory:", value = "")
          )
        )
      )
    )
  )
)


# Server ------------------------------------------------------------------

server <- function(input, output) {
    
  
}

shinyApp(ui, server)