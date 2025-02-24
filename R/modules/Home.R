# modules/about.R
homeUI <- function() {
    tabItem(
        tabName = "home",
        
        jumbotron(
          title = "Welcome to use NGSViz!",
          status = "info",
          lead = "Calculate and visualize coverage of NGS data",
          # href = "",
          # btnName = "Download",
          # "Data available from the City of Melbourne Open Data Portal"
        ),
        
        # fluidRow(
        #   userBox(
        #     collapsible = FALSE,
        #     title = userDescription(
        #       title = "Ashleigh Latter",
        #       subtitle = "Developer",
        #       image = "",
        #       type = 1
        #     ),
        #     status = "purple",
        #     "Super impressive bio."
        #   ),
        #   
        #   box(
        #     title = "My favourite quote",
        #     width = 6,
        #     collapsible = FALSE,
        #     blockQuote("Just because you're trash, doesn't mean you can't do great things. It's called garbage can, not garbage cannot.", color = "purple")
        #   )
        #   
        # )
        
      )
}

aboutServer <- function(id) {
    moduleServer(id, function(input, output, session) {
        
    })
}