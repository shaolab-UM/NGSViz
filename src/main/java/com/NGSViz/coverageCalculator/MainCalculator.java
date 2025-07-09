package com.NGSViz.coverageCalculator;

import com.NGSViz.configSet.GenerateJsonConfig;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.sqldbOperate.ParallelGeneDbProcessor;
import com.NGSViz.sqldbOperate.Transcript;
import com.NGSViz.sqldbOperate.QueryWholeRegionCoordinate;
import com.NGSViz.utils.DirectoryChecker;
import com.NGSViz.utils.Matrix2CSV;
import com.NGSViz.utils.ReadTextFile;
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
    private static String gene_subset;
    private static String title_subset;
    private static String sample_subset;
    private static String group_subset;
    private static double trim_ratio = InputParameterAttributes.robust;
    private static String heatmap_file_name = "coverage_matrix_heatmap.csv";
    private static String average_cov_name = "average_coverage_matrix.csv";
    private static String sem_name = "sem.csv";
    private static boolean paired_mode = false;
    private static String paired_bam = null;
    private static List<String> query_gene_list;
    private static List<String> DB_gene_list = QueryWholeRegionCoordinate.DB_gene_list;
    private static String cleaned_path = DirectoryChecker.removeTrailingSlash(output_path);
    private static JSONObject config_obj = GenerateJsonConfig.transformParas2JsonConfig();
    private static double[][] coverage_scaled_matrix;
    private static Map<String, Transcript> DB_GENES;
    private static Set<String> record_name_list;
    private static List<String> row_names;


    public static void mainCalculator() throws IOException {
        JSONArray jsonArray = new JSONArray();
        for (int i=0; i<bam_list.size(); i++) {
            String[] bam_file_list = bam_list.get(i).split(":", 2);
            bam_file = bam_file_list[0];
            // Is the paired mode available?
            if (bam_file_list.length == 2) {
                paired_mode = true;
                paired_bam = bam_file_list[1];
                System.out.println("--- paired pattern is available, the pair-end bam file is : " + bam_file_list[1]);
            }
            System.out.println("--- The bam file will be analysing: " + bam_file);
            gene_subset = gene_list.get(i);
            System.out.println("The gene subset will be analysing: " + gene_subset);

            title_subset = analysis_title.get(i);
            group_subset = group_list.get(i);
            sample_subset = sample_list.get(i);

            System.out.println("The title subset will be analysing: " + title_subset);
            System.out.println("--- Start coverage matrix calculation! ---");

            if (analysis_type.equals("exon")){
                // get the whole coordinate
                QueryWholeRegionCoordinate.queryGenomeCoorDatabaseRecord(tbl_name, biotype, analysis_type);
                List<Double> width_list = QueryWholeRegionCoordinate.width_list;
                System.out.println("The gene subset will be analysing: " + gene_subset);
                // get the subset of gene list
                if (gene_subset.equals("all")){
                    System.out.println("--- Run all DB gene list!");
                    query_gene_list = DB_gene_list;
                } else {
                    System.out.println("--- Run subset gene list!");
                    System.out.println("subset gene list name is: " + gene_subset);
                    query_gene_list = ReadTextFile.readGeneList2(gene_subset);
                }
                
                // Use optimized processor instead of old method
                HashMap<String, Object> mat_res = OptimizedGeneProcessor.optimizedParallelProcess(bam_file, query_gene_list, paired_mode);
                List<String> row_names = (List<String>) mat_res.get("row_names");
                coverage_scaled_matrix = (double[][]) mat_res.get("coverage_matrix");
            }else{
                // get the subset of gene list
                ParallelGeneDbProcessor GenesBInfo = new ParallelGeneDbProcessor(core_num);
                try {
                    // By invoking a non-static method through an instance
                    GenesBInfo.processGeneData(tbl_name, biotype, analysis_type);
                } catch (SQLException e) {
                    System.err.println("Database error: " + e.getMessage());
                } catch (InterruptedException e) {
                    System.err.println("Thread interruption error: " + e.getMessage());
                }
                if (gene_subset.equals("all")){
                    System.out.println("--- Run all DB gene list!");
                    record_name_list = GenesBInfo.getRecordName();
                    DB_GENES = GenesBInfo.getGeneMap();
                } else {
                    System.out.println("--- Run subset gene list!");
                    System.out.println("subset gene list name is: " + gene_subset);
                    Set<String> query_gene_list = ReadTextFile.readGeneList(gene_subset);
                    ParallelGeneDbProcessor newGenesBInfo = new ParallelGeneDbProcessor(core_num);
                    ParallelGeneDbProcessor subDBGeneInfo = newGenesBInfo.subsetGeneList(newGenesBInfo, query_gene_list, GenesBInfo);
                    record_name_list = subDBGeneInfo.getRecordName();
                    DB_GENES = subDBGeneInfo.getGeneMap();
                }
                // Use optimized processor instead of old method
                GeneCoverageProcessor genesProcess = new GeneCoverageProcessor(bam_file, DB_GENES, record_name_list, paired_mode);
                Map<String, Object> coverage_map = genesProcess.buildCoverageMatrix();
                row_names = (List<String>) coverage_map.get("record_names");
                coverage_scaled_matrix = (double[][]) coverage_map.get("cov_mat");
            }

            // Calculate standard errors (SEM) for matrix if needed. Shut off SEM in single gene case.
            double[] confi_matrix = AverageCoverage.calculateAverageCoverage(coverage_scaled_matrix, trim_ratio);
            // output the result
            System.out.println("Coverage Scaled Matrix finish!");
            System.out.println("The number of row is: " + coverage_scaled_matrix.length);
            System.out.println("The number of column is: " + coverage_scaled_matrix[0].length);

            // Return avg. profile.
            double[] average_coverage_matrix = AverageCoverage.calculateAverageCoverage(coverage_scaled_matrix, trim_ratio);
            // save the data
            // Book-keep this matrix for heatmap.
            File bam_path = new File(bam_file);
            String bam_name = bam_path.getName();
            bam_name = bam_name.substring(0, bam_name.lastIndexOf('.'));

            //
            JSONObject OutFileList = new JSONObject();
            String heatmap_data_path = cleaned_path + "/" + bam_name + "__" + group_subset + "__" + heatmap_file_name;
            String average_coverage_path = cleaned_path + "/" + bam_name + "__" + group_subset + "__" + average_cov_name;
            String sem_path = cleaned_path + "/" + bam_name + "__" + group_subset + "__" + sem_name;
            OutFileList.put("heatmapDataFile", heatmap_data_path);
            OutFileList.put("avgPlotDataFile", average_coverage_path);
            OutFileList.put("semDataFile", sem_path);
            jsonArray.put(OutFileList);

            // output the result
            Matrix2CSV.saveMatrix2CSV(heatmap_data_path, coverage_scaled_matrix, row_names);
            Matrix2CSV.saveMatrix2CSV(average_coverage_path, average_coverage_matrix, title_subset);
            Matrix2CSV.saveMatrix2CSV(sem_path, confi_matrix, title_subset);
        }
        config_obj.put("OutputFile", jsonArray);
        String plot_json_name = "NGSViz_plotSetting.json";
        GenerateJsonConfig.generateJsonConfig2(output_path, plot_json_name, config_obj);
    }
}
