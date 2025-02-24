# modules/calculation.R
calculationUI <- function(id) {
    ns <- NS(id)
    fluidPage(
        titlePanel("Calculation mode"),
        sidebarLayout(
            sidebarPanel(
                fileInput(ns("fileInput"), "Upload data file"),
                numericInput(ns("param"), "Parameter input", value = 100),
                actionButton(ns("runCalculation"), "Run calculation", class = "btn btn-primary")
            ),
            mainPanel(
                verbatimTextOutput(ns("result"))
            )
        )
    )
}

calculationServer <- function(id, javaScriptPath) {
    moduleServer(id, function(input, output, session) {
        observeEvent(input$runCalculation, {
            # Invoke Java to run a script
            system(paste("java -jar", javaScriptPath, input$fileInput$datapath, input$param))
            output_data <- read.csv("output.csv")
            output$result <- renderPrint({
                print(output_data)
            })
        })
    })
}