build_ngsviz_theme <- function() {
  create_theme(
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
}

build_static_config <- function() {
  list(
    ngsViz_log = "img/NGSViz_log.png",
    help_text = here("README.md"),
    split_color_panel = c(
      "NPG" = "npg",
      "AAAS" = "aaas",
      "NEJM" = "nejm",
      "Lancet" = "lancet",
      "JAMA" = "jama"
    ),
    sample_color_panel = c(
      "Pastel1" = "Pastel1",
      "Pastel2" = "Pastel2",
      "Paired" = "Paired",
      "Dark2" = "Dark2",
      "Accent" = "Accent",
      "Set1" = "Set1",
      "Set2" = "Set2",
      "Set3" = "Set3"
    ),
    file_type_list = c("tiff", "png", "pdf", "jpeg")
  )
}

build_runtime_config <- function(json_file) {
  java_res <- getToolJsonPara(json_file)
  list(
    java_path = java_res[1],
    jar_path = java_res[3],
    db_file = java_res[4]
  )
}

build_app_context <- function() {
  tool_dir <- here()
  print(sprintf("The root path of ngsViz is : %s", tool_dir))

  json_file <- here("NGSViz_setting.json")
  runtime_config <- build_runtime_config(json_file)

  # Enable asynchronous execution for long-running tasks.
  plan(multisession)

  c(
    list(
      tool_dir = tool_dir,
      json_file = json_file,
      app_theme = build_ngsviz_theme(),
      genome_result = getGenomeName(runtime_config$db_file)
    ),
    build_static_config(),
    runtime_config
  )
}
