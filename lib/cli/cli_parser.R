cli_argument_error <- function(command, message) {
  condition <- structure(
    list(message = message, call = NULL, command = command),
    class = c("cli_argument_error", "error", "condition")
  )
  stop(condition)
}

parse_native_options <- function(args, command) {
  expected <- if (command == "describe") {
    command
  } else {
    c(command, "--input-dir", NA_character_)
  }
  valid <- length(args) == length(expected)
  fixed <- !is.na(expected)
  if (valid) valid <- identical(args[fixed], expected[fixed])
  if (!valid || (command != "describe" && !nzchar(args[3]))) {
    usage <- if (command == "describe") {
      "describe does not accept arguments."
    } else {
      paste0(command, " requires --input-dir <directory>.")
    }
    cli_argument_error(command, usage)
  }
  list(
    mode = "native", command = command,
    input_dir = if (command == "describe") NULL else args[3]
  )
}

parse_cli_args <- function(args) {
  commands <- c("describe", "validate-plot", "run-plot")
  if (length(args) == 1L && !(args[1] %in% commands)) {
    return(list(mode = "legacy", command = "run-plot", input_dir = args[1]))
  }
  if (length(args) == 0L) cli_argument_error("", "A command is required.")
  command <- args[1]
  if (!(command %in% commands)) {
    cli_argument_error(command, paste0("Unknown command: ", command, "."))
  }
  parse_native_options(args, command)
}
