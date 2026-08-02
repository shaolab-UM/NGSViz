package com.NGSViz.cli;

import com.NGSViz.CliOptions;
import com.NGSViz.compute.ComputeRequest;
import com.NGSViz.compute.ValidationIssue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Single-sample native computation manifest. */
final class ComputeManifest {
    private static final String SCHEMA_VERSION = "1.0";

    private final JSONObject json;

    private ComputeManifest(JSONObject json) {
        this.json = json;
    }

    static ComputeManifest fromRun(
            ComputeRequest request, String status, Instant startedAt, Instant finishedAt,
            ComputeOutputs outputs, String logFile, List<ValidationIssue> warnings,
            List<ValidationIssue> errors) {
        JSONObject json = new JSONObject()
                .put("schema_version", SCHEMA_VERSION)
                .put("status", status)
                .put("ngsviz_version", CliOptions.VERSION)
                .put("started_at", startedAt.toString())
                .put("finished_at", finishedAt.toString())
                .put("duration_ms", Math.max(0, Duration.between(startedAt, finishedAt).toMillis()))
                .put("resolved_parameters", request.toJson())
                .put("sample_id", request.sampleId())
                .put("sample_name", request.sampleName())
                .put("group_name", request.groupName())
                .put("outputs", outputs.toJson())
                .put("log_file", logFile == null ? JSONObject.NULL : logFile)
                .put("warnings", issuesJson(warnings))
                .put("errors", issuesJson(errors));
        return new ComputeManifest(json);
    }

    JSONObject toJson() {
        return new JSONObject(json.toString());
    }

    private static JSONArray issuesJson(List<ValidationIssue> issues) {
        JSONArray json = new JSONArray();
        for (ValidationIssue issue : issues) json.put(issue.toJson());
        return json;
    }
}
