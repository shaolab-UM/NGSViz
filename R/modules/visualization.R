# modules/visualization.R
visualizationUI <- function(num_sightings, unique_birds, unique_locations) {
    tabItem(
        tabName = "dashboard",
        ## Info boxes ----
        fluidRow(
          
          column(
            width = 4,
            infoBox(
              width = 12,
              title = "Total Bird Sightings",
              value = num_sightings,
              icon = icon("list"),
              color = "primary"
            )
          ),
          
          column(
            width = 4,
            infoBox(
              width = 12,
              value = unique_birds,
              title = "Species Identified",
              icon = icon("dove"),
              color = "primary"
            )
          ),
          
          column(
            width = 4,
            infoBox(
              width = 12,
              value = unique_locations,
              title = "Total Sites",
              icon = icon("location-dot"),
              color = "primary"
            )
          )
          
        ),
        
        ## Sortable boxes ----
        fluidRow(
          sortable(
            width = 6,
            
            box(
              title = "Unique Birds Found at each Location", 
              width = 12, 
              status = "olive",
              collapsible = FALSE, 
              ribbon(
                text = "NEW",
                color = "olive"
              ),
              
              plotlyOutput("plot_unique_birds_site")
            ),
            
            box(
              title = "Birds Sighted Per Day",
              width = 12, 
              closable = TRUE, 
              status = "olive",
              
              plotlyOutput("plot_unique_birds_by_day")
            )
            
          ),
          
          sortable(
            width = 6,
            
            box(
              title = "Bird Sightings by Location",
              width = 12,  
              status = "olive",
              collapsible = FALSE,
              maximizable = TRUE,
              
              leafletOutput("plot_sightings_by_location")
              
            ),
            
            box(
              title = "Total Sightings For Each Bird",
              width = 12, 
              status = "olive",
              collapsible = FALSE, 
              label = boxLabel(
                text = "Label", 
                status = "primary", 
                tooltip = "I'm a label!"),
              
              sidebar = boxSidebar(
                id = "boxsidebarid",
                numericInput(
                  inputId = "show_top_n",
                  label = "Show Top N",
                  value = 6,
                  min = 1,
                  max = 50,
                  width = "97%"
                )
              ),
              plotlyOutput("plot_bird_totals_per_day")
            )
          )
        ),
        ## Tab box ----
        tabBox(
          title = "Data",
          width = 12,
          type = "tabs",
          status = "olive",
          solidHeader = TRUE,
          
          tabPanel(
            "Site Locations",
            DTOutput("table_sites")
          ),
          tabPanel(
            "Birds",
            DTOutput("table_birds")
          )
          
        )
      )
}