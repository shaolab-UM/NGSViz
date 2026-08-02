package com.NGSViz.compute;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BamInspectorTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsReadableBamWithHeaderAndIndex() {
        Path bam = ComputeTestFixtures.writeBam(
                tempDir.resolve("signal.bam"), "chr1", true
        );

        BamInspection inspection = new BamInspector().inspect(bam, "signal_bam");

        assertTrue(inspection.errors().isEmpty());
        assertTrue(inspection.references().contains("chr1"));
    }

    @Test
    void rejectsBamWithoutIndex() {
        Path bam = ComputeTestFixtures.writeBam(
                tempDir.resolve("signal.bam"), "chr1", false
        );

        BamInspection inspection = new BamInspector().inspect(bam, "signal_bam");

        assertTrue(inspection.errors().stream()
                .anyMatch(issue -> "BAM_INDEX_MISSING".equals(issue.code())));
    }

    @Test
    void detectsSignalControlReferenceNamingMismatch() {
        Path signal = ComputeTestFixtures.writeBam(
                tempDir.resolve("signal.bam"), "chr1", true
        );
        Path control = ComputeTestFixtures.writeBam(
                tempDir.resolve("control.bam"), "1", true
        );
        BamInspection signalInspection = new BamInspector().inspect(signal, "signal_bam");
        BamInspection controlInspection = new BamInspector().inspect(control, "control_bam");

        assertTrue(BamInspector.referenceNamingMismatch(
                signalInspection.references(), controlInspection.references()
        ));
    }
}
