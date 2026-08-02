package com.NGSViz.cli;

import com.NGSViz.compute.ComputeRequest;
import com.NGSViz.compute.ComputeRequestReader;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyComputeRequestAdapterTest {
    @TempDir
    Path tempDir;

    @Test
    void legacyFlagsAndEquivalentJsonResolveToIdenticalParameters() throws Exception {
        String bam = "/data/signal.bam";
        String output = tempDir.resolve("output").toString();
        ComputeRequest legacy = new LegacyComputeRequestAdapter().fromArgs(new String[]{
                "-G", "hg38", "-R", "TSS", "-I", bam, "-O", output,
                "-T", "Test", "-CP", "-", "-D", "RefSeq", "-A", "transcript",
                "-B", "protein_coding", "-F", "2000", "-N", "0.0",
                "-DP", "100", "-MQ", "20", "-FL", "150", "-P", "1",
                "-BS", "500", "-BM", "mean", "-SS", "both", "-CM", "false",
                "-NF", "false", "-X", "all"
        });
        JSONObject json = new JSONObject()
                .put("schema_version", "1.3")
                .put("sample_id", "signal")
                .put("sample_name", "SampleName")
                .put("group_name", "GroupName")
                .put("title", "Test")
                .put("genome", "hg38")
                .put("region", "tss")
                .put("signal_bam", bam)
                .put("control_bam", JSONObject.NULL)
                .put("output_dir", output)
                .put("system_config", JSONObject.NULL)
                .put("database", "RefSeq")
                .put("analysis_type", "transcript")
                .put("biotype", "protein_coding")
                .put("flank_region", 2000)
                .put("flank_factor", 0.0)
                .put("num_datapoints", 100)
                .put("scale_ratio", JSONObject.NULL)
                .put("mapping_quality", 20)
                .put("fragment_length", 150)
                .put("cores", 1)
                .put("batch_size", 500)
                .put("bin_method", "mean")
                .put("strand_specific", "both")
                .put("center_mode", false)
                .put("custom_bed", JSONObject.NULL)
                .put("gene_subset", "all")
                .put("create_result_folder", false);
        Path requestPath = tempDir.resolve("request.json");
        Files.writeString(requestPath, json.toString());
        ComputeRequest request = new ComputeRequestReader().read(requestPath);

        assertEquals(request.toJson().toMap(), legacy.toJson().toMap());
        assertEquals("signal", legacy.sampleId());
        assertEquals("SampleName", legacy.sampleName());
        assertEquals("GroupName", legacy.groupName());
    }

    @Test
    void omittedOptionalFlagsKeepLegacyDefaults() throws Exception {
        ComputeRequest request = new LegacyComputeRequestAdapter().fromArgs(new String[]{
                "-G", "hg38", "-R", "enhancer", "-I", "/data/signal.bam",
                "-O", tempDir.resolve("output").toString(), "-T", "Test"
        });

        assertEquals(1500, request.flankRegion());
        assertEquals(100, request.numDatapoints());
        assertEquals(20, request.mappingQuality());
        assertEquals(150, request.fragmentLength());
        assertEquals(1, request.cores());
        assertEquals(500, request.batchSize());
        assertEquals("mean", request.binMethod());
        assertEquals("both", request.strandSpecific());
        assertEquals("all", request.geneSubset());
    }
}
