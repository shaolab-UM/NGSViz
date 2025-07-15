
aboutUS <- function(){
  
  tabItem(
    tabName = "AboutUs",
    fluidRow(
      box(
        title = "| Project Information",
        width = 12,
        solidHeader = TRUE,
        status = "primary",
        icon = icon("project-diagram"),
        tags$p("NGSViz: A tool for coverage Calculation and visualization of deep seqence"),
        tags$p(tags$b("GitHub Repository:"), 
               tags$a("https://github.com/shaolab-UM/NGSViz",
                      href = "https://github.com/shaolab-UM/NGSViz",
                      target = "_blank")),
        tags$p(tags$b("Article Introduction: "), 
               tags$a("...",
                      href = "...",
                      target = "_blankz"))
      ), # box end
      box(
        title = "| Author Information",
        width = 12,
        solidHeader = TRUE,
        status = "primary",
        icon = icon("user"),
        tags$p(tags$b("Author: "), "Ningyi-Shao (University of Macua)"),
        tags$p(tags$b("Email: "), "nshao@um.edu.mo"),
        tags$p(tags$b("Person Website: "), 
               tags$a("https://fhs.um.edu.mo/zh-hant/staff/ningyi-shao/",
                      href = "https://fhs.um.edu.mo/zh-hant/staff/ningyi-shao/",
                      target = "_blank")),       
        hr(),
        tags$p(tags$b("Author: "), "Benchen-Ye (University of Macua)"),
        tags$p(tags$b("Email: "), "yc47650@um.edu.mo"),
        tags$p(tags$b("Person Website: "), 
               tags$a("https://bencheye.github.io/",
                      href = "https://bencheye.github.io/",
                      target = "_blank"))
      ),
      box(
        title = "| Acknowledgments",
        width = 12,
        solidHeader = TRUE,
        status = "primary",
        icon = icon("user"),
        tags$p(tags$b("FHS"), "University of Macua")     
        
      )
    )
  )
  
}