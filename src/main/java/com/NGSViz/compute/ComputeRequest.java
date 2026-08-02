package com.NGSViz.compute;

import org.json.JSONObject;

import java.util.Optional;

/** Fully resolved native single-sample computation request. */
public final class ComputeRequest {
    String schemaVersion;
    String sampleId;
    String sampleName;
    String groupName;
    String title;
    String genome;
    String region;
    String signalBam;
    String controlBam;
    String outputDir;
    String systemConfig;
    String database;
    String analysisType;
    String biotype;
    int flankRegion;
    double flankFactor;
    int numDatapoints;
    Double scaleRatio;
    int mappingQuality;
    int fragmentLength;
    int cores;
    int batchSize;
    String binMethod;
    String strandSpecific;
    boolean centerMode;
    String customBed;
    String geneSubset;
    boolean createResultFolder;

    ComputeRequest() {
    }

    public String schemaVersion() { return schemaVersion; }
    public String sampleId() { return sampleId; }
    public String sampleName() { return sampleName; }
    public String groupName() { return groupName; }
    public String title() { return title; }
    public String genome() { return genome; }
    public String region() { return region; }
    public String signalBam() { return signalBam; }
    public Optional<String> controlBam() { return Optional.ofNullable(controlBam); }
    public String outputDir() { return outputDir; }
    public Optional<String> systemConfig() { return Optional.ofNullable(systemConfig); }
    public String database() { return database; }
    public String analysisType() { return analysisType; }
    public String biotype() { return biotype; }
    public int flankRegion() { return flankRegion; }
    public double flankFactor() { return flankFactor; }
    public int numDatapoints() { return numDatapoints; }
    public Optional<Double> scaleRatio() { return Optional.ofNullable(scaleRatio); }
    public int mappingQuality() { return mappingQuality; }
    public int fragmentLength() { return fragmentLength; }
    public int cores() { return cores; }
    public int batchSize() { return batchSize; }
    public String binMethod() { return binMethod; }
    public String strandSpecific() { return strandSpecific; }
    public boolean centerMode() { return centerMode; }
    public Optional<String> customBed() { return Optional.ofNullable(customBed); }
    public String geneSubset() { return geneSubset; }
    public boolean createResultFolder() { return createResultFolder; }

    public JSONObject toJson() {
        JSONObject json = identityJson();
        addAnalysisParameters(json);
        addExecutionParameters(json);
        return json;
    }

    private JSONObject identityJson() {
        return new JSONObject()
                .put("schema_version", schemaVersion).put("sample_id", sampleId)
                .put("sample_name", sampleName).put("group_name", groupName)
                .put("title", title).put("genome", genome).put("region", region)
                .put("signal_bam", signalBam).put("control_bam", nullable(controlBam))
                .put("output_dir", outputDir).put("system_config", nullable(systemConfig));
    }

    private void addAnalysisParameters(JSONObject json) {
        json.put("database", database).put("analysis_type", analysisType)
                .put("biotype", biotype).put("flank_region", flankRegion)
                .put("flank_factor", flankFactor).put("num_datapoints", numDatapoints)
                .put("scale_ratio", nullable(scaleRatio));
    }

    private void addExecutionParameters(JSONObject json) {
        json.put("mapping_quality", mappingQuality).put("fragment_length", fragmentLength)
                .put("cores", cores).put("batch_size", batchSize)
                .put("bin_method", binMethod).put("strand_specific", strandSpecific)
                .put("center_mode", centerMode).put("custom_bed", nullable(customBed))
                .put("gene_subset", geneSubset)
                .put("create_result_folder", createResultFolder);
    }

    private Object nullable(Object value) {
        return value == null ? JSONObject.NULL : value;
    }
}
