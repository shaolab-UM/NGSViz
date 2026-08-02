package com.NGSViz.compute;

import com.NGSViz.cli.CliExitCode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Runs all computation checks without creating files or invoking computation. */
public final class ComputeValidator {
    public ComputeValidationResult validate(ComputeRequest request) {
        List<ValidationIssue> errors = new ArrayList<>();
        List<ValidationIssue> warnings = new ArrayList<>();
        JSONObject context = new JSONObject().put("resolved_parameters", request.toJson());
        validateParameters(request, errors);
        SystemConfigInspection config = new SystemConfigInspector().inspect(request);
        errors.addAll(config.errors());
        warnings.addAll(config.warnings());
        context.put("system", config.details());
        if (config.errors().isEmpty()) {
            inspectInputs(request, config.databasePath(), context, errors);
        } else {
            context.put("supported_values", emptySupportedValues());
        }
        errors.addAll(new OutputInspector().inspect(request));
        int exitCode = errors.isEmpty() ? CliExitCode.SUCCESS.code()
                : config.errors().isEmpty() ? CliExitCode.INPUT_ERROR.code()
                : CliExitCode.ENVIRONMENT_ERROR.code();
        return new ComputeValidationResult(exitCode, context, warnings, errors);
    }

    private void inspectInputs(ComputeRequest request, Path database, JSONObject context,
                               List<ValidationIssue> errors) {
        DatabaseInspection db = new DatabaseInspector().inspect(
                database, request.genome(), request.database(), request.region(),
                request.analysisType(), request.biotype()
        );
        errors.addAll(db.errors());
        context.put("coordinate_table", db.coordinateTable() == null
                ? JSONObject.NULL : db.coordinateTable());
        context.put("supported_values", supportedValuesJson(db.supportedValues()));
        BamInspection signal = new BamInspector().inspect(
                Paths.get(request.signalBam()), "signal_bam"
        );
        errors.addAll(signal.errors());
        BamInspection control = inspectControl(request, errors);
        compareReferences(signal, control, db, errors);
        request.customBed().ifPresent(path -> errors.addAll(
                new BedInspector().inspect(Paths.get(path))
        ));
    }

    private BamInspection inspectControl(ComputeRequest request,
                                         List<ValidationIssue> errors) {
        if (request.controlBam().isEmpty()) {
            return new BamInspection(List.of(), Set.of());
        }
        BamInspection control = new BamInspector().inspect(
                Paths.get(request.controlBam().get()), "control_bam"
        );
        errors.addAll(control.errors());
        return control;
    }

    private void compareReferences(BamInspection signal, BamInspection control,
                                   DatabaseInspection database,
                                   List<ValidationIssue> errors) {
        if (!control.references().isEmpty() && BamInspector.referenceNamingMismatch(
                signal.references(), control.references())) {
            errors.add(issue("BAM_REFERENCE_STYLE_MISMATCH", "control_bam",
                    "Signal and control BAM reference naming styles differ."));
        }
        Set<String> databaseReferences = BamInspector.normalizedReferences(
                database.chromosomes()
        );
        checkDatabaseIntersection(
                signal.references(), databaseReferences, "signal_bam", errors
        );
        if (!control.references().isEmpty()) {
            checkDatabaseIntersection(
                    control.references(), databaseReferences, "control_bam", errors
            );
        }
    }

    private void checkDatabaseIntersection(Set<String> references,
                                           Set<String> databaseReferences, String field,
                                           List<ValidationIssue> errors) {
        Set<String> bamReferences = BamInspector.normalizedReferences(references);
        Set<String> intersection = new HashSet<>(bamReferences);
        intersection.retainAll(databaseReferences);
        if (!bamReferences.isEmpty() && !databaseReferences.isEmpty() && intersection.isEmpty()) {
            errors.add(issue("BAM_DATABASE_REFERENCE_MISMATCH", field,
                    "BAM references do not intersect database chromosomes."));
        }
    }

    private void validateParameters(ComputeRequest request, List<ValidationIssue> errors) {
        checkEquals(request.schemaVersion(), "1.3", "schema_version", errors);
        checkMinimum(request.flankRegion(), 0, "flank_region", errors);
        checkRange(request.flankFactor(), 0.0, 1.0, "flank_factor", errors);
        checkMinimum(request.numDatapoints(), 100, "num_datapoints", errors);
        request.scaleRatio().ifPresent(value -> checkFinite(value, "scale_ratio", errors));
        checkMinimum(request.mappingQuality(), 0, "mapping_quality", errors);
        checkMinimum(request.fragmentLength(), 0, "fragment_length", errors);
        checkMinimum(request.cores(), 1, "cores", errors);
        checkMinimum(request.batchSize(), 1, "batch_size", errors);
        checkMember(request.analysisType(), Set.of("transcript", "exon"),
                "analysis_type", errors);
        checkMember(request.binMethod(), Set.of("mean", "median", "max"),
                "bin_method", errors);
        checkMember(request.strandSpecific(), Set.of("both", "same", "opposite"),
                "strand_specific", errors);
    }

    private void checkEquals(String value, String expected, String field,
                             List<ValidationIssue> errors) {
        if (!expected.equals(value)) errors.add(issue("PARAMETER_VALUE_INVALID", field,
                field + " must equal " + expected + "."));
    }

    private void checkMinimum(int value, int minimum, String field,
                              List<ValidationIssue> errors) {
        if (value < minimum) errors.add(issue("PARAMETER_OUT_OF_RANGE", field,
                field + " must be at least " + minimum + "."));
    }

    private void checkRange(double value, double minimum, double maximum, String field,
                            List<ValidationIssue> errors) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            errors.add(issue("PARAMETER_OUT_OF_RANGE", field,
                    field + " must be between " + minimum + " and " + maximum + "."));
        }
    }

    private void checkFinite(double value, String field, List<ValidationIssue> errors) {
        if (!Double.isFinite(value)) errors.add(issue("PARAMETER_VALUE_INVALID", field,
                field + " must be finite."));
    }

    private void checkMember(String value, Set<String> values, String field,
                             List<ValidationIssue> errors) {
        if (!values.contains(value)) errors.add(issue("PARAMETER_VALUE_INVALID", field,
                field + " has an unsupported value."));
    }

    private JSONObject supportedValuesJson(Map<String, List<String>> values) {
        JSONObject json = new JSONObject();
        values.forEach((key, value) -> json.put(key, new JSONArray(value)));
        json.put("bin_methods", new JSONArray(List.of("mean", "median", "max")));
        json.put("strand_specific", new JSONArray(List.of("both", "same", "opposite")));
        return json;
    }

    private JSONObject emptySupportedValues() {
        return supportedValuesJson(Map.of(
                "genomes", List.of(), "databases", List.of(), "regions", List.of(),
                "analysis_types", List.of(), "biotypes", List.of()
        ));
    }

    private ValidationIssue issue(String code, String field, String message) {
        return new ValidationIssue(code, field, message);
    }
}
