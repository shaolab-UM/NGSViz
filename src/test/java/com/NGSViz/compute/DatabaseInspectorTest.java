package com.NGSViz.compute;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseInspectorTest {
    @TempDir
    Path tempDir;

    @Test
    void validatesIntegrityCombinationCoordinateTableAndChromosomes() throws Exception {
        Path database = ComputeTestFixtures.writeDatabase(
                tempDir.resolve("coordinates.db"), "hg38_RefSeq"
        );

        DatabaseInspection inspection = new DatabaseInspector().inspect(
                database, "hg38", "RefSeq", "tss", "transcript", "protein_coding"
        );

        assertTrue(inspection.errors().isEmpty());
        assertEquals("hg38_RefSeq", inspection.coordinateTable());
        assertEquals("chr1", inspection.chromosomes().iterator().next());
        assertTrue(inspection.supportedValues().get("genomes").contains("hg38"));
    }

    @Test
    void rejectsInvalidGenomeDatabaseRegionCombination() throws Exception {
        Path database = ComputeTestFixtures.writeDatabase(
                tempDir.resolve("coordinates.db"), "hg38_RefSeq"
        );

        DatabaseInspection inspection = new DatabaseInspector().inspect(
                database, "hg19", "RefSeq", "tss", "transcript", "protein_coding"
        );

        assertTrue(inspection.errors().stream()
                .anyMatch(issue -> "DATABASE_COMBINATION_NOT_FOUND".equals(issue.code())));
    }

    @Test
    void rejectsMissingCoordinateTable() throws Exception {
        Path database = ComputeTestFixtures.writeDatabase(
                tempDir.resolve("coordinates.db"), "missing_table"
        );

        DatabaseInspection inspection = new DatabaseInspector().inspect(
                database, "hg38", "RefSeq", "tss", "transcript", "protein_coding"
        );

        assertFalse(inspection.errors().isEmpty());
        assertTrue(inspection.errors().stream()
                .anyMatch(issue -> "DATABASE_COORDINATE_TABLE_MISSING".equals(issue.code())));
    }
}
