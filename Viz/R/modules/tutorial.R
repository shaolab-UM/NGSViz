# modules/tutorial.R
tutorialUI <- function(id) {
    ns <- NS(id)
    fluidPage(
        titlePanel("Tutorial"),
        tabsetPanel(
            tabPanel("Text tutorial", HTML(paste(readLines("tutorial_pdf/tutorial.html"), collapse = "<br/>"))),
            tabPanel("Video tutorial", includeHTML("tutorial_pdf/tutorial_video.html"))
        )
    )
}

tutorialServer <- function(id) {
    moduleServer(id, function(input, output, session) {
        # No server-side logic is required.
    })
}