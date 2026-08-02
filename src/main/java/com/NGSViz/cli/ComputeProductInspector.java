package com.NGSViz.cli;

import com.NGSViz.compute.ValidationIssue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Validates Java computation products declared by the plot-setting JSON. */
final class ComputeProductInspector {
    ComputeOutputs inspect(Path outputDir, String title) throws ProductValidationException {
        Path setting = outputDir.resolve(title + "_NGSViz_plotSetting.json");
        requireNonEmpty(setting, "plot-setting JSON");
        List<String> coverage = new ArrayList<>();
        List<String> counts = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(new String(
                    Files.readAllBytes(setting), StandardCharsets.UTF_8
            ));
            JSONArray files = json.getJSONArray("OutputFile");
            for (int index = 0; index < files.length(); index++) {
                JSONObject entry = files.getJSONObject(index);
                Path coveragePath = Path.of(entry.getString("heatmapDataFile"));
                Path countPath = Path.of(entry.getString("readCountFile"));
                requireNonEmpty(coveragePath, "coverage CSV");
                requireNonEmpty(countPath, "read-count CSV");
                coverage.add(coveragePath.toAbsolutePath().normalize().toString());
                counts.add(countPath.toAbsolutePath().normalize().toString());
            }
        } catch (ProductValidationException error) {
            throw error;
        } catch (Exception error) {
            throw failure("Plot-setting JSON is invalid: " + error.getMessage());
        }
        if (coverage.isEmpty() || counts.isEmpty()) {
            throw failure("Plot-setting JSON declares no computation products.");
        }
        return new ComputeOutputs(
                coverage, counts, setting.toAbsolutePath().normalize().toString()
        );
    }

    private void requireNonEmpty(Path path, String label) throws ProductValidationException {
        try {
            if (!Files.isRegularFile(path) || Files.size(path) == 0) {
                throw failure("Required " + label + " is missing or empty: " + path + ".");
            }
        } catch (IOException error) {
            throw failure("Cannot inspect " + label + ": " + path + ".");
        }
    }

    private ProductValidationException failure(String message) {
        return new ProductValidationException(new ValidationIssue(
                "COMPUTE_OUTPUT_INVALID", "outputs", message
        ));
    }

    static final class ProductValidationException extends Exception {
        private final ValidationIssue issue;

        ProductValidationException(ValidationIssue issue) {
            super(issue.message());
            this.issue = issue;
        }

        ValidationIssue issue() { return issue; }
    }
}
