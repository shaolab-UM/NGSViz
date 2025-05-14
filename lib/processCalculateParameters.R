

execute_ngsviz_jar <- function(java_path, jar_path, params_list, output_dir, log_filename) {
  # Ensure the JAR file exists
  if (!file.exists(jar_path)) {
    stop("Error: NGSViz JAR file not found at ", jar_path)
  }
  
  # Ensure the output directory exists, create if not
  if (!dir.exists(output_dir)) {
    dir.create(output_dir, recursive = TRUE)
  }
  
  # Construct the base command arguments for java -jar
  cmd_args <- c("-jar", shQuote(jar_path)) # Use shQuote to handle spaces in path
  
  # Add parameters from the list
  for (param_name in names(params_list)) {
    param_value <- params_list[[param_name]]
    
    # Add the flag (e.g., "-G")
    cmd_args <- c(cmd_args, paste0("-", param_name))
    
    # Add the value if it's not a boolean flag indicator (like TRUE)
    # Assuming TRUE means the flag is present but has no value following it
    if (!isTRUE(param_value)) {
      cmd_args <- c(cmd_args, shQuote(as.character(param_value))) # Use shQuote for values too
    }
  }
  
  # Construct the full command string including redirection and completion marker
  # We use bash -c to execute the complex shell command string
  log_path <- file.path(output_dir, log_filename)
  # Ensure log_path is also quoted
  command_string <- paste(
    java_path, paste(cmd_args, collapse = " "),
    ">", shQuote(log_path)
  )
  
  # Print the command string for debugging (optional)
  message("Executing command: ", command_string)
  
  # Execute the command using system2 with shell = TRUE
  # We capture stdout and stderr to see the output/errors in R
  status <- system(command_string)
  print(paste("Command exit status code: ", status))
  return(status)
}