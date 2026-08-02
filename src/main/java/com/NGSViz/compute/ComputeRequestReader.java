package com.NGSViz.compute;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Reads a strict JSON object directly into a ComputeRequest. */
public final class ComputeRequestReader {
    private static final Set<String> FIELDS = Set.of(
            "schema_version", "sample_id", "sample_name", "group_name", "title",
            "genome", "region", "signal_bam", "control_bam", "output_dir",
            "system_config", "database", "analysis_type", "biotype", "flank_region",
            "flank_factor", "num_datapoints", "scale_ratio", "mapping_quality",
            "fragment_length", "cores", "batch_size", "bin_method", "strand_specific",
            "center_mode", "custom_bed", "gene_subset", "create_result_folder"
    );
    private static final Set<String> REQUIRED = Set.of(
            "sample_id", "sample_name", "group_name", "title", "genome", "region",
            "signal_bam", "output_dir"
    );

    public ComputeRequest read(Path requestFile) throws ComputeRequestFormatException {
        JSONObject json = readJson(requestFile);
        List<ValidationIssue> issues = validateShape(json);
        ComputeRequest request = new ComputeRequest();
        if (issues.isEmpty()) {
            populate(request, json, issues);
        }
        if (!issues.isEmpty()) {
            throw new ComputeRequestFormatException(issues);
        }
        return request;
    }

    private JSONObject readJson(Path path) throws ComputeRequestFormatException {
        try {
            return new JSONObject(new String(
                    Files.readAllBytes(path), StandardCharsets.UTF_8
            ));
        } catch (IOException | JSONException error) {
            throw new ComputeRequestFormatException(List.of(new ValidationIssue(
                    "REQUEST_JSON_INVALID", null, "Cannot read a valid JSON request: "
                    + error.getMessage()
            )));
        }
    }

    private List<ValidationIssue> validateShape(JSONObject json) {
        List<ValidationIssue> issues = new ArrayList<>();
        for (String key : json.keySet()) {
            if (!FIELDS.contains(key)) {
                issues.add(issue("REQUEST_UNKNOWN_FIELD", key, "Unknown field: " + key + "."));
            }
        }
        for (String key : REQUIRED) {
            if (!json.has(key) || json.isNull(key)) {
                issues.add(issue("REQUEST_REQUIRED_FIELD", key, "Required field is missing: " + key + "."));
            }
        }
        return issues;
    }

    private void populate(ComputeRequest request, JSONObject json,
                          List<ValidationIssue> issues) {
        populateIdentity(request, json, issues);
        populateAnalysis(request, json, issues);
        populateExecution(request, json, issues);
    }

    private void populateIdentity(ComputeRequest request, JSONObject json,
                                  List<ValidationIssue> issues) {
        request.schemaVersion = stringValue(json, "schema_version", "1.3", false, issues);
        request.sampleId = stringValue(json, "sample_id", null, true, issues);
        request.sampleName = stringValue(json, "sample_name", null, true, issues);
        request.groupName = stringValue(json, "group_name", null, true, issues);
        request.title = stringValue(json, "title", null, true, issues);
        request.genome = stringValue(json, "genome", null, true, issues);
        request.region = stringValue(json, "region", null, true, issues);
        if (request.region != null) request.region = request.region.toLowerCase();
        request.signalBam = stringValue(json, "signal_bam", null, true, issues);
        request.controlBam = stringValue(json, "control_bam", null, false, issues);
        request.outputDir = stringValue(json, "output_dir", null, true, issues);
        request.systemConfig = stringValue(json, "system_config", null, false, issues);
    }

    private void populateAnalysis(ComputeRequest request, JSONObject json,
                                  List<ValidationIssue> issues) {
        request.database = stringValue(json, "database", "RefSeq", false, issues);
        request.analysisType = stringValue(json, "analysis_type", "transcript", false, issues);
        request.biotype = stringValue(json, "biotype", "protein_coding", false, issues);
        Integer flank = integerValue(json, "flank_region", null, issues);
        request.flankRegion = flank == null ? defaultFlank(request.region) : flank;
        request.flankFactor = numberValue(json, "flank_factor", 0.0, issues);
        request.numDatapoints = integerValue(json, "num_datapoints", 100, issues);
        request.scaleRatio = nullableNumber(json, "scale_ratio", issues);
    }

    private void populateExecution(ComputeRequest request, JSONObject json,
                                   List<ValidationIssue> issues) {
        request.mappingQuality = integerValue(json, "mapping_quality", 20, issues);
        request.fragmentLength = integerValue(json, "fragment_length", 150, issues);
        request.cores = integerValue(json, "cores", 1, issues);
        request.batchSize = integerValue(json, "batch_size", 500, issues);
        request.binMethod = stringValue(json, "bin_method", "mean", false, issues);
        request.strandSpecific = stringValue(json, "strand_specific", "both", false, issues);
        request.centerMode = booleanValue(json, "center_mode", false, issues);
        request.customBed = stringValue(json, "custom_bed", null, false, issues);
        request.geneSubset = stringValue(json, "gene_subset", "all", false, issues);
        request.createResultFolder = booleanValue(
                json, "create_result_folder", false, issues
        );
    }

    private String stringValue(JSONObject json, String key, String defaultValue,
                               boolean nonEmpty, List<ValidationIssue> issues) {
        if (!json.has(key) || json.isNull(key)) return defaultValue;
        Object value = json.get(key);
        if (!(value instanceof String) || (nonEmpty && ((String) value).trim().isEmpty())) {
            issues.add(typeIssue(key, nonEmpty ? "a non-empty string" : "a string"));
            return defaultValue;
        }
        return (String) value;
    }

    private Integer integerValue(JSONObject json, String key, Integer defaultValue,
                                 List<ValidationIssue> issues) {
        if (!json.has(key) || json.isNull(key)) return defaultValue;
        Object value = json.get(key);
        if (!(value instanceof Integer) && !(value instanceof Long)) {
            issues.add(typeIssue(key, "an integer"));
            return defaultValue == null ? 0 : defaultValue;
        }
        long parsed = ((Number) value).longValue();
        if (parsed < Integer.MIN_VALUE || parsed > Integer.MAX_VALUE) {
            issues.add(typeIssue(key, "a 32-bit integer"));
            return defaultValue == null ? 0 : defaultValue;
        }
        return (int) parsed;
    }

    private double numberValue(JSONObject json, String key, double defaultValue,
                               List<ValidationIssue> issues) {
        Double value = nullableNumber(json, key, issues);
        return value == null ? defaultValue : value;
    }

    private Double nullableNumber(JSONObject json, String key,
                                  List<ValidationIssue> issues) {
        if (!json.has(key) || json.isNull(key)) return null;
        Object value = json.get(key);
        if (!(value instanceof Number)) {
            issues.add(typeIssue(key, "a number"));
            return null;
        }
        return ((Number) value).doubleValue();
    }

    private boolean booleanValue(JSONObject json, String key, boolean defaultValue,
                                 List<ValidationIssue> issues) {
        if (!json.has(key) || json.isNull(key)) return defaultValue;
        Object value = json.get(key);
        if (!(value instanceof Boolean)) {
            issues.add(typeIssue(key, "a boolean"));
            return defaultValue;
        }
        return (Boolean) value;
    }

    private int defaultFlank(String region) {
        if ("enhancer".equals(region)) return 1500;
        if ("dhs".equals(region) || "bed".equals(region)) return 1000;
        return 2000;
    }

    private ValidationIssue typeIssue(String field, String expected) {
        return issue("REQUEST_FIELD_TYPE_INVALID", field,
                "Field " + field + " must be " + expected + ".");
    }

    private ValidationIssue issue(String code, String field, String message) {
        return new ValidationIssue(code, field, message);
    }
}
