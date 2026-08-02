package com.NGSViz.compute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Detects existing ngsViz computation products without creating output paths. */
final class OutputInspector {
    List<ValidationIssue> inspect(ComputeRequest request) {
        List<ValidationIssue> errors = new ArrayList<>();
        Path output = Paths.get(request.outputDir());
        if (request.createResultFolder()) output = output.resolve(request.title());
        if (!Files.exists(output)) return errors;
        if (!Files.isDirectory(output)) {
            errors.add(issue("OUTPUT_PATH_INVALID", "output_dir",
                    "Output path exists and is not a directory."));
            return errors;
        }
        try (Stream<Path> children = Files.list(output)) {
            children.filter(path -> isNgsvizProduct(path.getFileName().toString()))
                    .forEach(path -> errors.add(issue("OUTPUT_PRODUCT_EXISTS", "output_dir",
                            "Existing ngsViz product would be overwritten: " + path + ".")));
        } catch (IOException error) {
            errors.add(issue("OUTPUT_DIRECTORY_UNREADABLE", "output_dir", error.getMessage()));
        }
        return errors;
    }

    private boolean isNgsvizProduct(String name) {
        return "compute_manifest.json".equals(name)
                || "NGSViz_compute.log".equals(name)
                || name.endsWith("_coverage_matrix_heatmap.csv")
                || name.endsWith("_gene_read_count.csv")
                || name.endsWith("_NGSViz_plotSetting.json");
    }

    private ValidationIssue issue(String code, String field, String message) {
        return new ValidationIssue(code, field, message);
    }
}
