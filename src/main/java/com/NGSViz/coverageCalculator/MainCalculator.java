package com.NGSViz.coverageCalculator;

import com.NGSViz.ReadBam.BAMAttribute;
import com.NGSViz.configSet.GenerateJsonConfig;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.sqldbOperate.BedDBProcess;
import com.NGSViz.sqldbOperate.GeneDbProcessor;
import com.NGSViz.sqldbOperate.Transcript;
import com.NGSViz.utils.DirectoryChecker;
import com.NGSViz.utils.Matrix2CSV;
import com.NGSViz.utils.SparseMatrix;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Benchen Ye
 * @create 2024-11--14:47
 */
public class MainCalculator extends InputParameterAttributes {
    private static String bam_file;
    private static String bam_file_bkg;
    private static String gene_subset;
    private static String title_subset;
    private static String sample_subset;
    private static String group_subset;
    private static String heatmap_file_name = "coverage_matrix_heatmap.csv";
    private static String read_count_file_name = "gene_read_count.csv";
    private static boolean input_mode = false;
    private static String cleaned_path = DirectoryChecker.removeTrailingSlash(output_path);
    private static JSONObject config_obj = GenerateJsonConfig.transformParas2JsonConfig();
    private static SparseMatrix coverage_scaled_matrix;
    private static SparseMatrix coverage_scaled_matrix_bkg;
    private static Map<Integer, List<Transcript>> geneList_batches;
    private static Set<String> record_name_list;
    private static List<String> row_names;
    private static int[] read_counts;
    private static Map<String, List<Transcript>> exon_gene_map;


    public static void mainCalculator() throws IOException {
        JSONArray jsonArray = new JSONArray();
        for (int i=0; i<bam_list.size(); i++) {
            String[] bam_file_list = bam_list.get(i).split(":", 2);
            bam_file = bam_file_list[0];
            BAMAttribute bamObj = new BAMAttribute(bam_file);
            System.out.println("--- The bam file will be analysing: " + bam_file);
            // Is the input mode available? use input bam to adjust the signal
            if (bam_file_list.length == 2) {
                input_mode = true;
                bam_file_bkg = bam_file_list[1];
                System.out.println("--- Input pattern is available, the Input bam file is : " + bam_file_list[1]);
            }

            gene_subset = gene_list.get(i);
            System.out.println("The gene subset will be analysing: " + gene_subset);
            title_subset = analysis_title.get(i);
            group_subset = group_list.get(i);
            sample_subset = sample_list.get(i);

            System.out.println("The title subset will be analysing: " + title_subset);
            System.out.println("----------------------------------------");
            System.out.println("--- Start coverage matrix calculation! ---");

            if(bedDB_path == null || bedDB_path.equals("-")) { //
                // get genelist from build in DB
                try {
                    // By invoking a non-static method through an instance
                    GeneDbProcessor.processGeneData(tbl_name, biotype, analysis_type);
                } catch (SQLException e) {
                    System.err.println("Database error: " + e.getMessage());
                }
                record_name_list = GeneDbProcessor.getRecordName();
                geneList_batches = GeneDbProcessor.getBatchGenes();
            } else {
                BedDBProcess BedDB = new BedDBProcess(bedDB_path);
                BedDB.buildBedDB(bedDB_path);
                //genesByChromosome = BedDB.getGeneListByChromosome();
                record_name_list = BedDB.getRecordName();
                geneList_batches = BedDB.getBatchGeneList();
            }

            // Use optimized processor instead of old method
            GeneCoverageProcessor genesProcess = new GeneCoverageProcessor(bamObj, geneList_batches, record_name_list);
            Map<String, Object> coverage_map = genesProcess.buildCoverageMatrix(scale_ratio);
            row_names = (List<String>) coverage_map.get("record_names");
            coverage_scaled_matrix = (SparseMatrix) coverage_map.get("cov_mat");
            read_counts = (int[]) coverage_map.get("read_counts");

            if(input_mode){
                // calculate background
                System.out.println("Input mode: to calculate background.");
                // bkg_coverage_scaled_matrix
                BAMAttribute bamObj_bkg = new BAMAttribute(bam_file_bkg);
                GeneCoverageProcessor genesProcess_bkg = new GeneCoverageProcessor(bamObj_bkg, geneList_batches, record_name_list);
                // bkg_coverage_scaled_matrix - FIXED: use paired_bam instead of bam_file
                Double input_scale_ratio = null;
                Map<String, Object> coverage_map_bkg = genesProcess_bkg.buildCoverageMatrix(input_scale_ratio);
                coverage_scaled_matrix_bkg = (SparseMatrix) coverage_map_bkg.get("cov_mat");
                // adjust the coverage matrix based on the input / IgG
                coverage_scaled_matrix = BackgrouNormalizer.backgroundNormalize(coverage_scaled_matrix,
                        coverage_scaled_matrix_bkg);
            }
            System.out.println("Coverage Scaled Matrix finish!");
            System.out.println("----------------------------------------");
            // save the data
            // Book-keep this matrix for heatmap.
            File bam_path = new File(bam_file);
            String bam_name = bam_path.getName();
            bam_name = bam_name.substring(0, bam_name.lastIndexOf('.'));
            //
            JSONObject OutFileList = new JSONObject();
            String heatmap_data_path = cleaned_path + "/" + bam_name + "_" + heatmap_file_name;
            String read_count_data_path = cleaned_path + "/" + bam_name + "_" + read_count_file_name;
            OutFileList.put("heatmapDataFile", heatmap_data_path);
            OutFileList.put("readCountFile", read_count_data_path);
            jsonArray.put(OutFileList);

            // output the result
            Matrix2CSV.saveMatrix2CSV(heatmap_data_path, coverage_scaled_matrix, row_names);
            Matrix2CSV.saveGeneReadCount2CSV(read_count_data_path, row_names, read_counts);
        }
        config_obj.put("OutputFile", jsonArray);
        String plot_json_name = title + "_NGSViz_plotSetting.json";
        GenerateJsonConfig.generateJsonConfig2(output_path, plot_json_name, config_obj);
    }
}
