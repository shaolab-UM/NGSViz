package com.NGSViz.compute;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/** Aggregated read-only compute validation result. */
public final class ComputeValidationResult {
    private final int exitCode;
    private final JSONObject context;
    private final List<ValidationIssue> warnings;
    private final List<ValidationIssue> errors;

    public ComputeValidationResult(int exitCode, JSONObject context,
                                   List<ValidationIssue> warnings,
                                   List<ValidationIssue> errors) {
        this.exitCode = exitCode;
        this.context = context;
        this.warnings = List.copyOf(warnings);
        this.errors = List.copyOf(errors);
    }

    public boolean isValid() { return errors.isEmpty(); }
    public int exitCode() { return exitCode; }
    public JSONObject context() { return context; }
    public List<ValidationIssue> warnings() { return warnings; }
    public List<ValidationIssue> errors() { return errors; }

    public JSONArray warningsJson() { return issuesJson(warnings); }
    public JSONArray errorsJson() { return issuesJson(errors); }

    private JSONArray issuesJson(List<ValidationIssue> issues) {
        JSONArray json = new JSONArray();
        for (ValidationIssue issue : issues) json.put(issue.toJson());
        return json;
    }
}
