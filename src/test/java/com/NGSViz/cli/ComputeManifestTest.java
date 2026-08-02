package com.NGSViz.cli;

import com.NGSViz.compute.ComputeRequest;
import com.NGSViz.compute.ComputeRequestReader;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComputeManifestTest {
    @TempDir
    Path tempDir;

    @Test
    void completedManifestContainsRequiredSingleSampleFieldsWithoutVisualizationState()
            throws Exception {
        ComputeRequest request = request();
        ComputeOutputs outputs = new ComputeOutputs(
                List.of("/output/signal_coverage_matrix_heatmap.csv"),
                List.of("/output/signal_gene_read_count.csv"),
                "/output/Test_NGSViz_plotSetting.json"
        );
        JSONObject manifest = ComputeManifest.fromRun(
                request, "completed", Instant.parse("2026-08-02T10:00:00Z"),
                Instant.parse("2026-08-02T10:00:01Z"), outputs,
                "/output/NGSViz_compute.log", List.of(), List.of()
        ).toJson();

        assertTrue(manifest.keySet().containsAll(List.of(
                "schema_version", "status", "ngsviz_version", "started_at",
                "finished_at", "duration_ms", "resolved_parameters", "sample_id",
                "sample_name", "group_name", "outputs", "log_file", "warnings", "errors"
        )));
        assertEquals("sample-a", manifest.getString("sample_id"));
        assertEquals("Sample A", manifest.getString("sample_name"));
        assertEquals("Group A", manifest.getString("group_name"));
        assertEquals(1, manifest.getJSONObject("outputs")
                .getJSONArray("coverage_matrices").length());
        assertFalse(manifest.has("visualization_status"));
        assertFalse(manifest.getJSONObject("resolved_parameters").has("samples"));
    }

    private ComputeRequest request() throws Exception {
        JSONObject json = new JSONObject()
                .put("sample_id", "sample-a")
                .put("sample_name", "Sample A")
                .put("group_name", "Group A")
                .put("title", "Test")
                .put("genome", "hg38")
                .put("region", "tss")
                .put("signal_bam", "/data/signal.bam")
                .put("output_dir", "/output");
        Path path = tempDir.resolve("request.json");
        Files.writeString(path, json.toString());
        return new ComputeRequestReader().read(path);
    }
}
