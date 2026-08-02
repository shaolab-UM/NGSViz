package com.NGSViz.compute;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Validates BED rows as 0-based half-open intervals without modifying the file. */
public final class BedInspector {
    public List<ValidationIssue> inspect(Path bed) {
        List<ValidationIssue> errors = new ArrayList<>();
        if (!Files.isRegularFile(bed) || !Files.isReadable(bed)) {
            errors.add(issue("BED_UNREADABLE", "custom_bed", "BED is not readable."));
            return errors;
        }
        int dataRows = 0;
        try (BufferedReader reader = Files.newBufferedReader(bed)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.startsWith("#")
                        || line.startsWith("track") || line.startsWith("browser")) continue;
                dataRows++;
                inspectRow(line, lineNumber, errors);
            }
        } catch (IOException error) {
            errors.add(issue("BED_READ_FAILED", "custom_bed", error.getMessage()));
        }
        if (dataRows == 0 && errors.isEmpty()) {
            errors.add(issue("BED_EMPTY", "custom_bed", "BED contains no data rows."));
        }
        return errors;
    }

    private void inspectRow(String line, int lineNumber, List<ValidationIssue> errors) {
        String[] columns = line.split("\t", -1);
        if (columns.length < 3) {
            errors.add(issue("BED_COLUMN_COUNT_INVALID", "custom_bed",
                    "BED line " + lineNumber + " has fewer than three columns."));
            return;
        }
        try {
            long start = Long.parseLong(columns[1]);
            long end = Long.parseLong(columns[2]);
            if (start < 0) errors.add(issue("BED_START_INVALID", "custom_bed",
                    "BED line " + lineNumber + " has a negative start."));
            if (end <= start) errors.add(issue("BED_END_INVALID", "custom_bed",
                    "BED line " + lineNumber + " must have end greater than start."));
        } catch (NumberFormatException error) {
            errors.add(issue("BED_COORDINATE_TYPE_INVALID", "custom_bed",
                    "BED line " + lineNumber + " has non-integer coordinates."));
        }
    }

    private ValidationIssue issue(String code, String field, String message) {
        return new ValidationIssue(code, field, message);
    }
}
