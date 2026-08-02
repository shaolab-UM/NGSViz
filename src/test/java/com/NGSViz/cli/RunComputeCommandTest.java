package com.NGSViz.cli;

import com.NGSViz.compute.ComputeRequest;
import com.NGSViz.compute.ComputeRequestReader;
import com.NGSViz.compute.ComputeValidationResult;
import com.NGSViz.compute.ValidationIssue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RunComputeCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void validationFailureDoesNotCreateOutputOrInvokeCalculator() throws Exception {
        ComputeRequest request = request(tempDir.resolve("not-created"), "sample-a");
        AtomicInteger calls = new AtomicInteger();
        RunComputeCommand command = command(
                ignored -> invalidValidation(request), (ignored, context) -> { },
                ignored -> calls.incrementAndGet()
        );

        CliResponse response = command.execute(request);

        assertEquals(4, response.exitCode());
        assertEquals(0, calls.get());
        assertFalse(Files.exists(tempDir.resolve("not-created")));
    }

    @Test
    void calculatorExceptionReturnsExitCodeFiveAndWritesFailedManifest() throws Exception {
        Path output = tempDir.resolve("execution-error");
        ComputeRequest request = request(output, "sample-a");
        RunComputeCommand command = command(
                ignored -> validValidation(request), (ignored, context) -> { },
                ignored -> { throw new IOException("calculator failed"); }
        );

        CliResponse response = command.execute(request);

        assertEquals(5, response.exitCode());
        JSONObject manifest = readJson(output.resolve("compute_manifest.json"));
        assertEquals("failed", manifest.getString("status"));
        assertFalse(manifest.getJSONArray("errors").isEmpty());
    }

    @Test
    void missingProductsReturnExitCodeSix() throws Exception {
        Path output = tempDir.resolve("missing-products");
        ComputeRequest request = request(output, "sample-a");
        RunComputeCommand command = command(
                ignored -> validValidation(request), (ignored, context) -> { }, ignored -> { }
        );

        CliResponse response = command.execute(request);

        assertEquals(6, response.exitCode());
        assertEquals("failed", readJson(output.resolve("compute_manifest.json"))
                .getString("status"));
    }

    @Test
    void calculatorLogsDoNotLeakToCommandStdout() throws Exception {
        Path output = tempDir.resolve("logged-run");
        ComputeRequest request = request(output, "sample-a");
        RunComputeCommand command = command(
                ignored -> validValidation(request), (ignored, context) -> { },
                value -> {
                    System.out.println("calculator stdout");
                    System.err.println("calculator stderr");
                    writeProducts(value);
                }
        );
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        PrintStream capturedOut = new PrintStream(captured, true, StandardCharsets.UTF_8);
        System.setOut(capturedOut);

        CliResponse response;
        try {
            response = command.execute(request);
            assertSame(capturedOut, System.out);
            assertSame(originalErr, System.err);
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        assertEquals(0, response.exitCode());
        assertEquals("", captured.toString(StandardCharsets.UTF_8));
        String log = Files.readString(output.resolve("NGSViz_compute.log"));
        assertTrue(log.contains("calculator stdout"));
        assertTrue(log.contains("calculator stderr"));
        JSONObject metadata = readJson(output.resolve("Test plot_NGSViz_plotSetting.json"))
                .getJSONObject("run_metadata");
        assertEquals("Sample A", metadata.getString("sample_name"));
        assertEquals("Group A", metadata.getString("group_name"));
        List<Path> manifests;
        try (java.util.stream.Stream<Path> paths = Files.walk(output)) {
            manifests = paths.filter(path -> "compute_manifest.json".equals(
                    path.getFileName().toString())).collect(java.util.stream.Collectors.toList());
        }
        assertEquals(1, manifests.size());
        JSONObject manifest = readJson(manifests.get(0));
        assertEquals("sample-a", manifest.getString("sample_id"));
        assertFalse(manifest.toString().contains("visualization_status"));
    }

    @Test
    void jsonAndLegacyRequestsUseTheSameCalculatorInvocationPoint() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        RunComputeCommand.ComputeExecutor executor = request -> {
            calls.incrementAndGet();
            writeProducts(request);
        };
        ComputeRequest jsonRequest = request(tempDir.resolve("json"), "signal");
        ComputeRequest legacyRequest = new LegacyComputeRequestAdapter().fromArgs(new String[]{
                "-G", "hg38", "-R", "tss", "-I", "/data/signal.bam",
                "-O", tempDir.resolve("legacy").toString(), "-T", "Test plot",
                "-CP", "/config/settings.json"
        });
        RunComputeCommand jsonCommand = command(
                ignored -> validValidation(jsonRequest), (ignored, context) -> { }, executor
        );
        RunComputeCommand legacyCommand = command(
                ignored -> validValidation(legacyRequest), (ignored, context) -> { }, executor
        );

        assertEquals(0, jsonCommand.execute(jsonRequest).exitCode());
        assertEquals(0, legacyCommand.execute(legacyRequest).exitCode());
        assertEquals(2, calls.get());
    }

    private RunComputeCommand command(
            RunComputeCommand.RequestValidator validator,
            RunComputeCommand.RequestMapper mapper,
            RunComputeCommand.ComputeExecutor executor) {
        return new RunComputeCommand(
                validator, mapper, executor,
                Clock.fixed(Instant.parse("2026-08-02T10:00:00Z"), ZoneOffset.UTC)
        );
    }

    private ComputeValidationResult validValidation(ComputeRequest request) {
        return new ComputeValidationResult(
                0, new JSONObject().put("resolved_parameters", request.toJson()),
                List.of(), List.of()
        );
    }

    private ComputeValidationResult invalidValidation(ComputeRequest request) {
        return new ComputeValidationResult(
                4, new JSONObject().put("resolved_parameters", request.toJson()), List.of(),
                List.of(new ValidationIssue("BAM_INVALID", "signal_bam", "Invalid BAM."))
        );
    }

    private ComputeRequest request(Path output, String sampleId) throws Exception {
        JSONObject json = new JSONObject()
                .put("schema_version", "1.3")
                .put("sample_id", sampleId)
                .put("sample_name", "Sample A")
                .put("group_name", "Group A")
                .put("title", "Test plot")
                .put("genome", "hg38")
                .put("region", "tss")
                .put("signal_bam", "/data/signal.bam")
                .put("output_dir", output.toString())
                .put("system_config", "/config/settings.json");
        Path path = tempDir.resolve(sampleId + "-request.json");
        Files.writeString(path, json.toString());
        return new ComputeRequestReader().read(path);
    }

    private void writeProducts(ComputeRequest request) throws IOException {
        Path output = Path.of(request.outputDir());
        Files.createDirectories(output);
        Path coverage = output.resolve("signal_coverage_matrix_heatmap.csv");
        Path counts = output.resolve("signal_gene_read_count.csv");
        Path setting = output.resolve(request.title() + "_NGSViz_plotSetting.json");
        Files.writeString(coverage, "gene,value\nA,1\n");
        Files.writeString(counts, "gene,count\nA,1\n");
        JSONObject entry = new JSONObject()
                .put("heatmapDataFile", coverage.toString())
                .put("readCountFile", counts.toString());
        Files.writeString(setting, new JSONObject()
                .put("OutputFile", new JSONArray().put(entry)).toString());
    }

    private JSONObject readJson(Path path) throws IOException {
        return new JSONObject(Files.readString(path));
    }
}
