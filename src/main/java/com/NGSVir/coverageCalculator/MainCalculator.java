package com.NGSVir.coverageCalculator;

import com.NGSVir.ReadBam.ReadBam;
import com.NGSVir.configSet.InputParameterAttributes;
import htsjdk.samtools.SamReader;
import com.NGSVir.sqldbOperate.QueryWholeRegionCoordinate;
import com.NGSVir.utils.CalculateLog2Matrix;
import com.NGSVir.utils.DirectoryChecker;
import com.NGSVir.utils.Matrix2CSV;
import com.NGSVir.ReadBam.BamFileLibrarySize;
import com.NGSVir.utils.ReadTextFile;
import java.io.File;
import java.util.*;


/**
 * @author Benchen Ye
 * @create 2024-11--14:47
 */
public class MainCalculator extends InputParameterAttributes {
    private static String bam_file;
    private static String gene_subset;
    private static String title_subset;
    private static double trim_ratio = InputParameterAttributes.robust;
    private static String heatmap_file_name = "coverage_matrix_heatmap.csv";
    private static String average_cov_name = "average_coverage_matrix.csv";
    private static String sem_name = "sem.csv";
    private static boolean paired_mode = false;
    private static String paired_bam = null;
    private static List<String> query_gene_list;
    private static List<String> DB_gene_list = QueryWholeRegionCoordinate.DB_gene_list;
    private static String cleaned_path = DirectoryChecker.removeTrailingSlash(output_path);


    public static void mainCalculator(){
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
            // get the subset of gene list
            if (gene_subset.equals("all")){
                System.out.println("--- Run all DB gene list!");
                query_gene_list = DB_gene_list;
            } else {
                System.out.println("--- Run subset gene list!");
                System.out.println("subset gene list name is: " + gene_subset);
                query_gene_list = ReadTextFile.readGeneList(gene_subset);
            }
            title_subset = analysis_title.get(i);
            System.out.println("The title subset will be analysing: " + title_subset);
            System.out.println("--- Start coverage matrix calculation! ---");

            // coverage_scaled_matrix is data matrix for heatmap
            HashMap<String, Object> mat_res = EachGeneRegionProcess.processEachQueryRegion(bam_file, query_gene_list, paired_mode);
            //HashMap<String, Object> mat_res = EachGeneRegionProcess.parallelProcessEachGene(bam_file, query_gene_list, paired_mode);
            List<String> row_names = (List<String>) mat_res.get("row_names");
            double[][] coverage_scaled_matrix = (double[][]) mat_res.get("coverage_matrix");

            if(paired_mode){
                // calculate background
                System.out.println("Paired mode: to calculate background.");
                // bkg_coverage_scaled_matrix
                HashMap<String, Object> bkg_mat_res = EachGeneRegionProcess.processEachQueryRegion(bam_file, query_gene_list, paired_mode);
                double[][] bkg_coverage_scaled_matrix = (double[][]) bkg_mat_res.get("coverage_matrix");
                // libsize
                // get the library size of bam file
                SamReader bam_reader = ReadBam.getBamReader(bam_file);
                double library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
                double result_pseudo_rpm = (1e6 / library_size);
                // get the library size of paired bam file
                SamReader paired_bam_reader = ReadBam.getBamReader(paired_bam);
                double paired_library_size = BamFileLibrarySize.getLibrarySize(paired_bam_reader);
                double bkg_pseudo_rpm = (1e6 / paired_library_size);
                coverage_scaled_matrix = CalculateLog2Matrix.calculateLog2Matrix(coverage_scaled_matrix,
                        bkg_coverage_scaled_matrix, result_pseudo_rpm, bkg_pseudo_rpm);
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

            String heatmap_data_path = cleaned_path + "/" + bam_name + "_" + heatmap_file_name;
            String average_coverage_path = cleaned_path + "/" + bam_name + "_" + average_cov_name;
            String sem_path = cleaned_path + "/" + bam_name + "_" + sem_name;
            // output the result
            Matrix2CSV.saveMatrix2CSV(heatmap_data_path, coverage_scaled_matrix, row_names);
            Matrix2CSV.saveMatrix2CSV(average_coverage_path, average_coverage_matrix, title_subset);
            Matrix2CSV.saveMatrix2CSV(sem_path, confi_matrix, title_subset);
        }
    }
}
