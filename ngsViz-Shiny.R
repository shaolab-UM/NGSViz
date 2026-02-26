checkRequiredPackages <- function(required_packages) {
  for (pkg in unique(required_packages)) {
    if (!requireNamespace(pkg, quietly = TRUE)) {
      message(paste0("Package '", pkg, "' is not installed, attempting to install..."))
      tryCatch(
        {
          install.packages(pkg, dependencies = TRUE)
          #pak::pkg_install(pkg)
          message(paste0("Package '", pkg, "' installed successfully."))
        },
        error = function(e) {
          stop(
            paste0(
              "Error: Unable to install package '", pkg,
              "'. Please install this package manually and then run the script.\n",
              "Error message: ", e$message
            )
          )
        }
      )
    }

    tryCatch(
      {
        suppressPackageStartupMessages(library(pkg, character.only = TRUE))
        message(paste0("Package '", pkg, "' loaded successfully."))
      },
      error = function(e) {
        stop(
          paste0(
            "Error: Unable to load package '", pkg,
            "'. Please check if the installation is complete or if there are compatibility issues.\n",
            "Error message: ", e$message
          )
        )
      }
    )
  }

  message("\nAll required packages have been installed and loaded.")
}

packages <- c(
  "fresh", "shinyFiles", "plotly", "ggrepel", "tidyverse", 
  "FactoMineR", "aplot","ggh4x", "circlize",
  "future", "RSQLite", "markdown", "ggsci", "here", "corrplot", "reshape2",  "zoo","RcppRoll",
  "dplyr", "readr",   "DT",
  "promises", "processx", "jsonlite",
  "ggplot2", "tidyr",
  "colorspace",
  "RColorBrewer", "purrr", "data.table",  "cowplot",
  "shiny", "bs4Dash","leaflet"
)

checkRequiredPackages(packages)

# Load shared scripts -----------------------------------------------------
source(here("www/aboutUS.R"))
source(here("lib/processDB.R"))
source(here("lib/processJSON.R"))
source(here("lib/coverageHeatmap.R"))
source(here("lib/qualityControlViz.R"))
source(here("lib/processCalculateParameters.R"))

# Load Shiny modules ------------------------------------------------------
source(here("lib/shiny/app_config.R"))
source(here("lib/shiny/ui_builder.R"))
source(here("lib/shiny/server_builder.R"))

# Build app ---------------------------------------------------------------
app_context <- build_app_context()
ui <- build_ngsviz_ui(app_context)
server <- build_ngsviz_server(app_context)

# Return a Shiny app object so `shiny::runApp('ngsViz-Shiny.R')` works.
shinyApp(ui = ui, server = server)
