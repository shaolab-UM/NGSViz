package com.NGSViz.compute;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComputeRequestReaderTest {
    @TempDir
    Path tempDir;

    @Test
    void readsValidSingleSampleRequestAndResolvesDefaults() throws Exception {
        Path requestFile = writeMinimalRequest();

        ComputeRequest request = new ComputeRequestReader().read(requestFile);

        assertEquals("sample-1", request.sampleId());
        assertEquals("Sample 1", request.sampleName());
        assertEquals("Group A", request.groupName());
        assertEquals("hg38", request.genome());
        assertEquals("tss", request.region());
        assertEquals(2000, request.flankRegion());
        assertEquals(100, request.numDatapoints());
        assertEquals("mean", request.binMethod());
        assertFalse(request.centerMode());
        assertTrue(request.controlBam().isEmpty());
    }

    @Test
    void rejectsSamplesArrayAsRequestFormatError() throws Exception {
        JSONObject json = new JSONObject(Files.readString(writeMinimalRequest()));
        json.put("samples", new JSONArray());
        Path requestFile = tempDir.resolve("samples.json");
        Files.writeString(requestFile, json.toString());

        ComputeRequestFormatException error = assertThrows(
                ComputeRequestFormatException.class,
                () -> new ComputeRequestReader().read(requestFile)
        );

        assertEquals("REQUEST_UNKNOWN_FIELD", error.issues().get(0).code());
        assertEquals("samples", error.issues().get(0).field());
    }

    @Test
    void rejectsUnknownField() throws Exception {
        JSONObject json = new JSONObject(Files.readString(writeMinimalRequest()));
        json.put("unexpected", true);
        Path requestFile = tempDir.resolve("unknown.json");
        Files.writeString(requestFile, json.toString());

        ComputeRequestFormatException error = assertThrows(
                ComputeRequestFormatException.class,
                () -> new ComputeRequestReader().read(requestFile)
        );

        assertEquals("unexpected", error.issues().get(0).field());
    }

    @Test
    void rejectsMissingGenome() throws Exception {
        JSONObject json = new JSONObject(Files.readString(writeMinimalRequest()));
        json.remove("genome");
        Path requestFile = tempDir.resolve("missing-genome.json");
        Files.writeString(requestFile, json.toString());

        ComputeRequestFormatException error = assertThrows(
                ComputeRequestFormatException.class,
                () -> new ComputeRequestReader().read(requestFile)
        );

        assertTrue(error.issues().stream().anyMatch(issue -> "genome".equals(issue.field())));
    }

    private Path writeMinimalRequest() throws Exception {
        JSONObject request = new JSONObject()
                .put("sample_id", "sample-1")
                .put("sample_name", "Sample 1")
                .put("group_name", "Group A")
                .put("title", "Sample_1_TSS")
                .put("genome", "hg38")
                .put("region", "TSS")
                .put("signal_bam", tempDir.resolve("signal.bam").toString())
                .put("output_dir", tempDir.resolve("output").toString());
        Path requestFile = tempDir.resolve("request.json");
        Files.writeString(requestFile, request.toString());
        return requestFile;
    }
}
