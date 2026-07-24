package com.NGSViz.sqldbOperate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BedDBProcess BED coordinate conversion.
 *
 * BED format uses 0-based half-open coordinates [start, end).
 * Internal Transcript storage must use 1-based closed coordinates [start, end]
 * to match GTF/GFF convention and htsjdk Interval expectations.
 */
class BedDBProcessTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void resetState() {
        BedDBProcess.record_name_list.clear();
        BedDBProcess.geneList_batches.clear();
        Transcript.resetIndexCounter();
    }

    @Test
    void testBedZeroBasedCoordinatesAreConvertedToOneBased() throws IOException {
        // BED line: chrom start=0 end=100  -> represents bases 0..99 (100 bases)
        // Expected 1-based: start=1 end=100 -> represents bases 1..100 (100 bases)
        Path bedFile = tempDir.resolve("test.bed");
        Files.write(bedFile, List.of("chr1\t0\t100\t+\tGENE1\tTX1"));

        BedDBProcess processor = new BedDBProcess(bedFile.toString());
        processor.buildBedDB(bedFile.toString());

        Map<Integer, List<Transcript>> batches = BedDBProcess.getBatchGeneList();
        assertFalse(batches.isEmpty(), "Expected at least one batch");

        Transcript tx = batches.get(0).get(0);
        assertEquals(1, tx.getStartPos(),
                "BED 0-based start (0) must be converted to 1-based (1)");
        assertEquals(100, tx.getEndPos(),
                "BED half-open end (100) equals 1-based inclusive end (100)");
    }

    @Test
    void testBedCoordinatesAtNonZeroStart() throws IOException {
        // BED: chr1 1000 2000 -> 0-based [1000, 2000) = bases 1000..1999
        // 1-based: [1001, 2000] = bases 1001..2000
        Path bedFile = tempDir.resolve("test.bed");
        Files.write(bedFile, List.of("chr1\t1000\t2000\t+\tGENE1\tTX1"));

        BedDBProcess processor = new BedDBProcess(bedFile.toString());
        processor.buildBedDB(bedFile.toString());

        Transcript tx = BedDBProcess.getBatchGeneList().get(0).get(0);
        assertEquals(1001, tx.getStartPos(),
                "BED start 1000 -> 1-based start 1001");
        assertEquals(2000, tx.getEndPos(),
                "BED half-open end 2000 -> 1-based end 2000");
    }

    @Test
    void testBedWidthCalculationAfterConversion() throws IOException {
        // A 1-bp region in BED: [5, 6) -> one base at index 5
        // 1-based: [6, 6] -> one base at position 6
        // Width should be 1, not 2
        Path bedFile = tempDir.resolve("test.bed");
        Files.write(bedFile, List.of("chr1\t5\t6\t+\tGENE1\tTX1"));

        BedDBProcess processor = new BedDBProcess(bedFile.toString());
        processor.buildBedDB(bedFile.toString());

        Transcript tx = BedDBProcess.getBatchGeneList().get(0).get(0);
        int width = tx.getEndPos() - tx.getStartPos() + 1;
        assertEquals(1, width,
                "A single-base BED interval [5,6) must have width 1 after conversion");
    }

    @Test
    void testBedNegativeStrandCoordinates() throws IOException {
        Path bedFile = tempDir.resolve("test.bed");
        Files.write(bedFile, List.of("chr1\t100\t200\t-\tGENE1\tTX1"));

        BedDBProcess processor = new BedDBProcess(bedFile.toString());
        processor.buildBedDB(bedFile.toString());

        Transcript tx = BedDBProcess.getBatchGeneList().get(0).get(0);
        assertEquals(101, tx.getStartPos());
        assertEquals(200, tx.getEndPos());
        assertEquals("-", tx.getQueryStrand());
    }

    @Test
    void testBedMinimalThreeColumns() throws IOException {
        Path bedFile = tempDir.resolve("test.bed");
        Files.write(bedFile, List.of("chr1\t0\t100"));

        BedDBProcess processor = new BedDBProcess(bedFile.toString());
        processor.buildBedDB(bedFile.toString());

        Transcript tx = BedDBProcess.getBatchGeneList().get(0).get(0);
        assertEquals(1, tx.getStartPos());
        assertEquals(100, tx.getEndPos());
    }

    @Test
    void testBedSixColumnFormat() throws IOException {
        // BED6: chrom start end name score strand
        Path bedFile = tempDir.resolve("test.bed");
        Files.write(bedFile, List.of("chr1\t0\t100\tGENE1\t0\t+"));

        BedDBProcess processor = new BedDBProcess(bedFile.toString());
        processor.buildBedDB(bedFile.toString());

        Transcript tx = BedDBProcess.getBatchGeneList().get(0).get(0);
        assertEquals(1, tx.getStartPos());
        assertEquals(100, tx.getEndPos());
        assertEquals("GENE1", tx.getGeneName());
    }
}
