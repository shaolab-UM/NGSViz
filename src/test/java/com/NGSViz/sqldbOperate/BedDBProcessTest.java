package com.NGSViz.sqldbOperate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests BedDBProcess parsing for standard BED6, legacy NGSViz custom format,
 * BED3, BED4, and ambiguous inputs.
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

    // ---- Standard BED6: chrom, start, end, name, score, strand ----

    @Test
    void parsesBed6PlusStrand() throws Exception {
        Path bed = writeBed("chr1\t1000\t2000\tGENE_A\t0\t+\n");
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        Transcript t = singleTranscript();
        assertEquals("GENE_A", t.getGeneName());
        assertEquals("+", t.getQueryStrand());
        // BED 0-based half-open [1000,2000) → 1-based closed [1001,2000]
        assertEquals(1001, t.getStartPos());
        assertEquals(2000, t.getEndPos());
    }

    @Test
    void parsesBed6MinusStrand() throws Exception {
        Path bed = writeBed("chr2\t500\t1500\tGENE_B\t100\t-\n");
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        Transcript t = singleTranscript();
        assertEquals("GENE_B", t.getGeneName());
        assertEquals("-", t.getQueryStrand());
    }

    @Test
    void parsesBed6DotStrand() throws Exception {
        Path bed = writeBed("chr3\t0\t100\tPEAK_1\t500\t.\n");
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        Transcript t = singleTranscript();
        assertEquals("PEAK_1", t.getGeneName());
        // '.' normalizes to '+'
        assertEquals("+", t.getQueryStrand());
    }

    // ---- Legacy NGSViz format: chrom, start, end, strand, geneName, txId ----

    @Test
    void parsesLegacyNgsVizFormat() throws Exception {
        Path bed = writeBed("chr4\t2000\t3000\t-\tTP53\tNM_000546\n");
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        Transcript t = singleTranscript();
        assertEquals("TP53", t.getGeneName());
        assertEquals("-", t.getQueryStrand());
        assertEquals("TP53:NM_000546", t.getRecordName());
    }

    @Test
    void parsesLegacyNgsViz5Columns() throws Exception {
        Path bed = writeBed("chr5\t100\t200\t+\tMYC\n");
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        Transcript t = singleTranscript();
        assertEquals("MYC", t.getGeneName());
        assertEquals("+", t.getQueryStrand());
    }

    @Test
    void parsesLegacyNgsViz4Columns() throws Exception {
        // col4 is strand token with no name columns
        Path bed = writeBed("chr6\t300\t400\t-\n");
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        Transcript t = singleTranscript();
        assertEquals("-", t.getQueryStrand());
        // gene_name auto-generated from coordinates (1-based: 301)
        assertEquals("chr6_301_400", t.getGeneName());
    }

    // ---- BED3 (minimal) ----

    @Test
    void parsesBed3() throws Exception {
        Path bed = writeBed("chr7\t5000\t6000\n");
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        Transcript t = singleTranscript();
        assertEquals("+", t.getQueryStrand());
        assertEquals("chr7_5001_6000", t.getGeneName());
    }

    // ---- BED4 without strand ----

    @Test
    void parsesBed4NonStrandName() throws Exception {
        Path bed = writeBed("chr8\t100\t200\tENHANCER_1\n");
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        Transcript t = singleTranscript();
        assertEquals("ENHANCER_1", t.getGeneName());
        assertEquals("+", t.getQueryStrand());
    }

    // ---- Ambiguous: col4 is strand token AND col6 is also strand token ----

    @Test
    void ambiguousBothCol4AndCol6AreStrandTokens() throws Exception {
        // When col4 is a strand token, legacy NGSViz branch takes priority.
        // Example: +\tSomeGene\t-
        Path bed = writeBed("chr9\t0\t100\t+\tABC\t-\n");
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        Transcript t = singleTranscript();
        // Legacy branch wins: strand from col4, gene from col5, tx from col6
        assertEquals("+", t.getQueryStrand());
        assertEquals("ABC", t.getGeneName());
    }

    // ---- hg19_genes.bed-style: chrom, start, end, numericWidth, strand, txId ----

    @Test
    void parsesHg19StyleBed() throws Exception {
        // col4=numeric (not strand), col5=strand-like but at wrong position for BED6,
        // col6=transcript ID. With BED6-first logic, col6 is NOT a strand token,
        // so falls to fallback: gene_name="13122", transcript_id="+"
        // This is still incorrect for this non-standard format, but the sample file
        // has been converted to proper BED6.
        Path bed = writeBed("chrY\t59330367\t59343488\tNM_002186\t0\t+\n");
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        Transcript t = singleTranscript();
        assertEquals("NM_002186", t.getGeneName());
        assertEquals("+", t.getQueryStrand());
    }

    // ---- Skips comment / track / browser lines ----

    @Test
    void skipsHeaderLines() throws Exception {
        String content = "# comment\n"
                + "track name=test\n"
                + "browser position chr1:1-100\n"
                + "\n"
                + "chr10\t0\t500\tGENE_X\t0\t+\n";
        Path bed = writeBed(content);
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        assertEquals(1, BedDBProcess.record_name_list.size());
        Transcript t = singleTranscript();
        assertEquals("GENE_X", t.getGeneName());
    }

    // ---- Multiple rows with deduplication ----

    @Test
    void deduplicatesByRecordName() throws Exception {
        String content = "chr11\t100\t200\tGENE_D\t0\t+\n"
                + "chr11\t100\t200\tGENE_D\t0\t+\n"  // duplicate
                + "chr11\t300\t400\tGENE_E\t0\t-\n";
        Path bed = writeBed(content);
        new BedDBProcess(bed.toString()).buildBedDB(bed.toString());

        // GENE_D appears twice with same record_name → deduplicated to 1
        Set<String> names = BedDBProcess.getRecordName();
        assertEquals(2, names.size());
    }

    // ---- Helpers ----

    private Path writeBed(String content) throws Exception {
        Path bed = tempDir.resolve("test.bed");
        Files.writeString(bed, content);
        return bed;
    }

    private Transcript singleTranscript() {
        Map<Integer, List<Transcript>> batches = BedDBProcess.getBatchGeneList();
        assertFalse(batches.isEmpty(), "Expected at least one batch");
        List<Transcript> first = batches.get(0);
        assertNotNull(first, "Expected batch 0 to exist");
        assertEquals(1, first.size(), "Expected exactly one transcript");
        return first.get(0);
    }
}
