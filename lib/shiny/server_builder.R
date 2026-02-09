validate_path_input <- function(path_value, empty_message, missing_message, path_type = c("file", "dir")) {
  path_type <- match.arg(path_type)

  if (is.null(path_value) || !nzchar(trimws(path_value))) {
    showNotification(empty_message, type = "error")
    return(FALSE)
  }

  path_exists <- if (path_type == "file") file.exists(path_value) else dir.exists(path_value)
  if (!path_exists) {
    showNotification(missing_message, type = "error")
    return(FALSE)
  }

  TRUE
}

parse_ratio_input <- function(ratio_text) {
  ratio_parts <- strsplit(ratio_text, ":", fixed = TRUE)[[1]]
  ratio_values <- suppressWarnings(as.numeric(ratio_parts))

  if (length(ratio_values) != 2 || anyNA(ratio_values)) {
    return(NULL)
  }

  ratio_values
}

parse_logical_text <- function(value, field_name) {
  if (is.logical(value) && length(value) == 1 && !is.na(value)) {
    return(value)
  }

  normalized <- tolower(trimws(as.character(value)))
  if (normalized %in% c("true", "t", "1", "yes", "y")) {
    return(TRUE)
  }
  if (normalized %in% c("false", "f", "0", "no", "n")) {
    return(FALSE)
  }

  stop(sprintf("`%s` must be TRUE or FALSE.", field_name))
}

build_color_blocks <- function(selected_vals) {
  div_tags <- lapply(selected_vals, function(color_code) {
    tags$div(
      style = paste0(
        "display: inline-block; width: 50px; height: 50px; ",
        "background-color: ", color_code,
        "; border: 1px solid #ccc; margin: 5px; vertical-align: middle;"
      ),
      title = color_code
    )
  })

  do.call(tagList, div_tags)
}

get_split_palette <- function(panel_name, color_num = 6) {
  palette_func_name <- paste0("pal_", panel_name)
  if (!exists(palette_func_name, mode = "function", where = asNamespace("ggsci"))) {
    stop(sprintf("ggsci palette function not found: %s", palette_func_name))
  }

  max_colors <- c(npg = 10, aaas = 10, nejm = 8, lancet = 9, jama = 7)
  palette_max <- unname(max_colors[[panel_name]])
  if (is.null(palette_max)) {
    palette_max <- color_num
  }
  n_to_display <- min(color_num, palette_max)

  colors <- get(palette_func_name, envir = asNamespace("ggsci"))(alpha = 1)(n_to_display)
  names(colors) <- colors
  colors
}

get_sample_palette <- function(panel_name, color_num = 6) {
  pal_info <- brewer.pal.info[panel_name, , drop = FALSE]
  if (nrow(pal_info) == 0) {
    stop(sprintf("RColorBrewer palette not found: %s", panel_name))
  }

  n_to_display <- min(color_num, pal_info$maxN[[1]])
  colors <- RColorBrewer::brewer.pal(n_to_display, panel_name)
  names(colors) <- colors
  colors
}

register_palette_selector <- function(
    input,
    output,
    palette_input_id,
    checkbox_output_id,
    selected_input_id,
    display_output_id,
    palette_getter,
    default_selected_count = 1
) {
  palette_colors <- reactive({
    panel_name <- input[[palette_input_id]]
    req(panel_name)
    palette_getter(panel_name)
  })

  output[[checkbox_output_id]] <- renderUI({
    colors_to_display <- palette_colors()
    checkboxGroupInput(
      selected_input_id,
      "Select the color to be used: ",
      choices = colors_to_display,
      selected = head(unname(colors_to_display), default_selected_count)
    )
  })

  output[[display_output_id]] <- renderUI({
    selected_vals <- input[[selected_input_id]]
    if (is.null(selected_vals) || length(selected_vals) == 0) {
      return(p("Please select the color on the left."))
    }

    build_color_blocks(selected_vals)
  })
}

update_plot_parameters <- function(plot_paras, input) {
  plot_paras[["high"]] <- input$high
  plot_paras[["width"]] <- input$width
  plot_paras[["dpi"]] <- input$dpi
  plot_paras[["file_type"]] <- input$file_type
  plot_paras[["SEM"]] <- parse_logical_text(input$SEM, "SEM")
  plot_paras[["smooth"]] <- parse_logical_text(input$smooth, "smooth")

  top_bottom_ratio <- parse_ratio_input(input$top_bottom_ratio)
  if (is.null(top_bottom_ratio)) {
    stop("`top_bottom_ratio` must use `a:b` format, for example `1:3`.")
  }
  plot_paras[["top_bottom_ratio"]] <- top_bottom_ratio

  plot_paras[["hc_m"]] <- input$hc_m
  plot_paras[["dist_m"]] <- input$dist_m
  plot_paras[["cluster_num"]] <- input$cluster_num

  plot_paras[["title_size"]] <- input$title_size
  plot_paras[["legend_size"]] <- input$legend_size
  plot_paras[["title_color"]] <- input$title_color
  plot_paras[["dash_color"]] <- input$dash_color
  plot_paras[["axis_text_x_size"]] <- input$axis_text_x_size
  plot_paras[["axis_text_y_size"]] <- input$axis_text_y_size
  plot_paras[["dash_size"]] <- input$dash_size
  plot_paras[["text_face"]] <- input$text_face
  plot_paras[["title_hjust"]] <- input$title_hjust
  plot_paras[["border_size"]] <- input$border_size
  plot_paras[["legend_text_size"]] <- input$legend_text_size
  plot_paras[["line_size"]] <- input$line_size
  plot_paras[["ystrip_text_size"]] <- input$ystrip_text_size

  plot_paras
}

register_calculation_server <- function(input, output, app_context, run_status) {
  output$dynamic_ui <- renderUI({
    biotype_result <- getBiotype(app_context$db_file, input$dashG)
    selectInput("dashB", "Choose a gene element type:", choices = biotype_result)
  })

  observeEvent(input$run_java, {
    selected_dashB <- input$dashB
    req(selected_dashB)

    run_status("Running...")

    para_list <- list(
      G = input$dashG,
      R = input$dashR,
      I = input$dashI,
      O = input$dashO,
      T = input$dashT,
      DB = input$dashDB,
      CP = input$dashCP,
      BS = input$dashBS,
      B = selected_dashB,
      F = input$dashF,
      N = input$dashN,
      DP = input$dashDP,
      S = input$dashS,
      MQ = input$dashMQ,
      BD = input$dashBD,
      FL = input$dashFL,
      P = input$dashP
    )

    tryCatch(
      {
        execution_res <- executeNgsvizJar(app_context$java_path, app_context$jar_path, para_list)
        print(execution_res)
        run_status("Operation completed")
      },
      error = function(e) {
        run_status("Operation failed")
        showNotification(e$message, type = "error")
      }
    )
  }, ignoreInit = TRUE)
}

register_single_coverage_server <- function(input, output, shared_values) {
  register_palette_selector(
    input = input,
    output = output,
    palette_input_id = "split_color_panel",
    checkbox_output_id = "split_color_checkboxes",
    selected_input_id = "selected_split_colors",
    display_output_id = "selected_split_color_display",
    palette_getter = get_split_palette,
    default_selected_count = 3
  )

  register_palette_selector(
    input = input,
    output = output,
    palette_input_id = "sample_color_panel",
    checkbox_output_id = "sample_color_checkboxes",
    selected_input_id = "selected_sample_colors",
    display_output_id = "selected_sample_color_display",
    palette_getter = get_sample_palette,
    default_selected_count = 1
  )

  observeEvent(input$parse_covPlot, {
    if (!validate_path_input(
      input$outputdir,
      empty_message = "Please input the path of calculation result!",
      missing_message = "The json file of plot json parameters not exist!",
      path_type = "file"
    )) {
      return()
    }

    plot_json <- input$outputdir
    print(sprintf("Starting process the plot parameters: %s", plot_json))

    plot_paras <- getPlotJsonParas(plot_json)
    shared_values$plot_paras <- plot_paras
    shared_values$sample_df <- plot_paras$sample_list

    print("Finish processing the plot parameters.")
  }, ignoreInit = TRUE)

  observeEvent(input$run_covPlot, {
    if (is.null(shared_values$plot_paras)) {
      showNotification("Please parse plot parameters first.", type = "error")
      return()
    }

    req(input$selected_sample_colors)
    req(input$selected_split_colors)

    tryCatch(
      {
        plot_paras <- update_plot_parameters(shared_values$plot_paras, input)
        plot_paras[["sample_color"]] <- input$selected_sample_colors
        plot_paras[["split_color"]] <- input$selected_split_colors

        output$covagePlot <- renderPlot({
          plotOneIntegratePlot(plot_paras)
        })
      },
      error = function(e) {
        showNotification(e$message, type = "error")
      }
    )
  }, ignoreInit = TRUE)
}

register_multi_sample_server <- function(input, output) {
  observeEvent(input$parse_multiSamplePlot, {
    if (!validate_path_input(
      input$json_files_path,
      empty_message = "Please input the path of json config dir path!",
      missing_message = "The folder does not exist!",
      path_type = "dir"
    )) {
      return()
    }

    json_path <- input$json_files_path

    output$multiSampleAveCovPlot <- renderPlot({
      mergeAveragePlot(json_path)
    })

    output$multiSampleIntegratedCovPlot <- renderPlot({
      mergeMultiVisResult(json_path)
    })
  }, ignoreInit = TRUE)
}

register_qc_server <- function(input, output) {
  observeEvent(input$parse_QCPlot, {
    if (!validate_path_input(
      input$qc_json_path,
      empty_message = "Please input the path of json config dir path!",
      missing_message = "The folder does not exist!",
      path_type = "dir"
    )) {
      return()
    }

    json_path <- input$qc_json_path
    heatmapMat_rowMerge <- propressData(json_path)

    output$pcaPlot <- renderPlot({
      runPCA(heatmapMat_rowMerge, json_path)
    })

    output$corrPlot <- renderPlot({
      corPlot(json_path, heatmapMat_rowMerge)
    })
  }, ignoreInit = TRUE)
}

register_help_server <- function(output, help_text) {
  output$markdown_content <- renderUI({
    html_content <- markdownToHTML(file = help_text, fragment.only = TRUE)
    HTML(html_content)
  })
}

build_ngsviz_server <- function(app_context) {
  function(input, output, session) {
    run_status <- reactiveVal("Ready to run")
    shared_values <- reactiveValues(plot_paras = NULL, sample_df = NULL)

    output$statusText <- renderText({
      run_status()
    })

    register_calculation_server(input, output, app_context, run_status)
    register_single_coverage_server(input, output, shared_values)
    register_multi_sample_server(input, output)
    register_qc_server(input, output)
    register_help_server(output, app_context$help_text)
  }
}
