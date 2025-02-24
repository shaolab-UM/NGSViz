# modules/about.R
aboutUI <- function(id) {
    ns <- NS(id)
    fluidPage(
        titlePanel("About US"),
        mainPanel(
            fluidRow(
                column(6, includeMarkdown("README.md")),
                column(6, img(src = "www/team_photo.jpg", height = 400))
            )
        )
    )
}

aboutServer <- function(id) {
    moduleServer(id, function(input, output, session) {
        
    })
}