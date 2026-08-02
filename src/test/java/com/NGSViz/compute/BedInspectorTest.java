package com.NGSViz.compute;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BedInspectorTest {
    @TempDir
    Path tempDir;

    @Test
    void rejectsRowsWithFewerThanThreeColumns() throws Exception {
        Path bed = tempDir.resolve("invalid-columns.bed");
        Files.writeString(bed, "chr1\t10\n");

        assertTrue(new BedInspector().inspect(bed).stream()
                .anyMatch(issue -> "BED_COLUMN_COUNT_INVALID".equals(issue.code())));
    }

    @Test
    void rejectsNegativeStartAndNonIncreasingEnd() throws Exception {
        Path bed = tempDir.resolve("invalid-coordinates.bed");
        Files.writeString(bed, "chr1\t-1\t10\nchr1\t20\t20\n");

        List<ValidationIssue> errors = new BedInspector().inspect(bed);

        assertTrue(errors.stream().anyMatch(issue -> "BED_START_INVALID".equals(issue.code())));
        assertTrue(errors.stream().anyMatch(issue -> "BED_END_INVALID".equals(issue.code())));
    }
}
