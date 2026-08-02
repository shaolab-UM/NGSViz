package com.NGSViz.cli;

import com.NGSViz.compute.ComputeRequest;
import com.NGSViz.configSet.DataPointNum;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.configSet.SetIntervalType;
import org.json.JSONObject;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Explicitly maps a validated request to the existing computation attributes. */
final class ComputeParameterMapper {
    void map(ComputeRequest request, JSONObject validationContext) throws Exception {
        resetCollections();
        mapIdentity(request, validationContext);
        mapAnalysis(request);
        mapExecution(request);
        loadDatabaseMetadata(request);
        InputParameterAttributes.interval_type = SetIntervalType.setIntervalTypeValue(
                InputParameterAttributes.region_labels
        );
        if (request.centerMode()) InputParameterAttributes.interval_type = "point_interval";
        DataPointNum.getDataPointNum();
    }

    private void resetCollections() {
        InputParameterAttributes.bam_list = new ArrayList<>();
        InputParameterAttributes.region_labels = new ArrayList<>();
        InputParameterAttributes.gene_list = new ArrayList<>();
        InputParameterAttributes.analysis_title = new ArrayList<>();
        InputParameterAttributes.sample_list = new ArrayList<>();
        InputParameterAttributes.group_list = new ArrayList<>();
        InputParameterAttributes.middle_points = 0;
        InputParameterAttributes.flank_points = 0;
    }

    private void mapIdentity(ComputeRequest request, JSONObject context) {
        Path output = RunComputeCommand.outputDirectory(request);
        InputParameterAttributes.file_type = "bam";
        InputParameterAttributes.genome = request.genome();
        InputParameterAttributes.region_type = request.region();
        InputParameterAttributes.input_file = bamValue(request);
        InputParameterAttributes.output_path = output.toString();
        InputParameterAttributes.title = request.title();
        InputParameterAttributes.sys_config_path = request.systemConfig().orElse("");
        InputParameterAttributes.db_path = context.getJSONObject("system")
                .getString("database_path");
        InputParameterAttributes.tbl_name = context.getString("coordinate_table");
        InputParameterAttributes.refname = InputParameterAttributes.tbl_name;
    }

    private void mapAnalysis(ComputeRequest request) {
        InputParameterAttributes.DB_type = request.database();
        InputParameterAttributes.analysis_type = request.analysisType();
        InputParameterAttributes.biotype = request.biotype();
        InputParameterAttributes.flank_region = request.flankRegion();
        InputParameterAttributes.flank_factor = request.flankFactor();
        InputParameterAttributes.num_datapoints = request.numDatapoints();
        InputParameterAttributes.scale_ratio = request.scaleRatio().orElse(null);
        InputParameterAttributes.genes = request.geneSubset();
        InputParameterAttributes.bedDB_path = request.customBed().orElse(null);
    }

    private void mapExecution(ComputeRequest request) {
        InputParameterAttributes.min_mapq = request.mappingQuality();
        InputParameterAttributes.frag_len = request.fragmentLength();
        InputParameterAttributes.buf_size = request.fragmentLength();
        InputParameterAttributes.core_num = request.cores();
        InputParameterAttributes.BATCH_SIZE = request.batchSize();
        InputParameterAttributes.bin_method = request.binMethod();
        InputParameterAttributes.strand_spec = request.strandSpecific();
        InputParameterAttributes.CenterMode = request.centerMode();
        InputParameterAttributes.new_forder = false;
        InputParameterAttributes.bam_list.add(bamValue(request));
        InputParameterAttributes.gene_list.add(request.geneSubset());
        InputParameterAttributes.analysis_title.add(request.title());
        InputParameterAttributes.sample_list.add(request.sampleName());
        InputParameterAttributes.group_list.add(request.groupName());
    }

    private void loadDatabaseMetadata(ComputeRequest request) throws Exception {
        String url = "jdbc:sqlite:file:" + Path.of(InputParameterAttributes.db_path)
                .toAbsolutePath() + "?mode=ro";
        String sql = "SELECT PointLab, Species FROM defaultTbl WHERE Genome=? AND DB=? "
                + "AND Region=? AND AnalysisType=? AND Biotype=? LIMIT 1";
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindRequest(statement, request);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalStateException("Validated database row is missing.");
                InputParameterAttributes.region_labels.addAll(
                        List.of(result.getString("PointLab").split("-"))
                );
                InputParameterAttributes.species = result.getString("Species");
            }
        }
    }

    private void bindRequest(PreparedStatement statement, ComputeRequest request)
            throws Exception {
        statement.setString(1, request.genome());
        statement.setString(2, request.database());
        statement.setString(3, request.region());
        statement.setString(4, request.analysisType());
        statement.setString(5, request.biotype());
    }

    private String bamValue(ComputeRequest request) {
        return request.signalBam() + request.controlBam().map(value -> ":" + value).orElse("");
    }
}
