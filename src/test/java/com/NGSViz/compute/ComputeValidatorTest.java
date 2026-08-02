package com.NGSViz.compute;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComputeValidatorTest {
    @TempDir
    Path tempDir;

    private Path config;
    private Path signalBam;
    private JSONObject requestJson;

    @BeforeEach
    void setUp() throws Exception {
        Path database = ComputeTestFixtures.writeDatabase(
                tempDir.resolve("coordinates.db"), "hg38_RefSeq"
        );
        config = ComputeTestFixtures.writeSystemConfig(
                tempDir.resolve("NGSViz_setting.json"), database
        );
        signalBam = ComputeTestFixtures.writeBam(
                tempDir.resolve("signal.bam"), "chr1", true
        );
        requestJson = ComputeTestFixtures.validRequest(tempDir, config, signalBam);
    }

    @Test
    void acceptsValidRequestWithoutCreatingOutputDirectory() throws Exception {
        ComputeRequest request = readRequest(requestJson);

        ComputeValidationResult result = new ComputeValidator().validate(request);

        assertTrue(result.isValid());
        assertEquals(0, result.exitCode());
        assertEquals("hg38_RefSeq", result.context().getString("coordinate_table"));
        assertTrue(result.context().getJSONObject("supported_values")
                .getJSONArray("genomes").toList().contains("hg38"));
        assertFalse(Path.of(request.outputDir()).toFile().exists());
    }

    @Test
    void rejectsNumDatapointsBelowOneHundred() throws Exception {
        requestJson.put("num_datapoints", 99);

        ComputeValidationResult result = new ComputeValidator().validate(readRequest(requestJson));

        assertEquals(4, result.exitCode());
        assertTrue(result.errors().stream()
                .anyMatch(issue -> "PARAMETER_OUT_OF_RANGE".equals(issue.code())
                        && "num_datapoints".equals(issue.field())));
    }

    @Test
    void rejectsSignalAndControlReferenceNamingMismatch() throws Exception {
        Path control = ComputeTestFixtures.writeBam(
                tempDir.resolve("control.bam"), "1", true
        );
        requestJson.put("control_bam", control.toString());

        ComputeValidationResult result = new ComputeValidator().validate(readRequest(requestJson));

        assertTrue(result.errors().stream()
                .anyMatch(issue -> "BAM_REFERENCE_STYLE_MISMATCH".equals(issue.code())));
    }

    @Test
    void rejectsControlWithoutDatabaseChromosomeIntersection() throws Exception {
        Path control = ComputeTestFixtures.writeBam(
                tempDir.resolve("control.bam"), "chrUn", true
        );
        requestJson.put("control_bam", control.toString());

        ComputeValidationResult result = new ComputeValidator().validate(readRequest(requestJson));

        assertTrue(result.errors().stream()
                .anyMatch(issue -> "BAM_DATABASE_REFERENCE_MISMATCH".equals(issue.code())
                        && "control_bam".equals(issue.field())));
    }

    @Test
    void rejectsExistingNgsvizOutputWithoutChangingIt() throws Exception {
        Path output = Path.of(requestJson.getString("output_dir"));
        Files.createDirectories(output);
        Path existing = output.resolve("Sample_1_TSS_NGSViz_plotSetting.json");
        Files.writeString(existing, "sentinel");

        ComputeValidationResult result = new ComputeValidator().validate(readRequest(requestJson));

        assertTrue(result.errors().stream()
                .anyMatch(issue -> "OUTPUT_PRODUCT_EXISTS".equals(issue.code())));
        assertEquals("sentinel", Files.readString(existing));
    }

    private ComputeRequest readRequest(JSONObject json) throws Exception {
        Path request = tempDir.resolve("request-" + System.nanoTime() + ".json");
        Files.writeString(request, json.toString());
        return new ComputeRequestReader().read(request);
    }
}
