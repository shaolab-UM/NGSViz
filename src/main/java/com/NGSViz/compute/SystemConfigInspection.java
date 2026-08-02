package com.NGSViz.compute;

import org.json.JSONObject;

import java.nio.file.Path;
import java.util.List;

/** Resolved system configuration and runtime JAR metadata. */
final class SystemConfigInspection {
    private final Path configPath;
    private final Path databasePath;
    private final JSONObject details;
    private final List<ValidationIssue> errors;
    private final List<ValidationIssue> warnings;

    SystemConfigInspection(Path configPath, Path databasePath, JSONObject details,
                           List<ValidationIssue> errors, List<ValidationIssue> warnings) {
        this.configPath = configPath;
        this.databasePath = databasePath;
        this.details = details;
        this.errors = List.copyOf(errors);
        this.warnings = List.copyOf(warnings);
    }

    Path configPath() { return configPath; }
    Path databasePath() { return databasePath; }
    JSONObject details() { return details; }
    List<ValidationIssue> errors() { return errors; }
    List<ValidationIssue> warnings() { return warnings; }
}
