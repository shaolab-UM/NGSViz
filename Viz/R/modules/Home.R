# modules/about.R
homeUI <- function() {
    tabItem(
        tabName = "home",
        
        jumbotron(
          title = "Welcome to use NGSViz!",
          status = "info",
          lead = "Calculate and visualize coverage of NGS data",
          # href = "",
        ),
      )
}

aboutServer <- function(id) {
    moduleServer(id, function(input, output, session) {
        
    })
}