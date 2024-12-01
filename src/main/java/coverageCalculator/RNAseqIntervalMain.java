package coverageCalculator;

import configSet.InputParameterAttributes;
import sqldbOperate.QueryWholeRegionCoordinate;
import sqldbOperate.RNAseqProcessRecordGenomeCoordinateDB;
import utils.CurrentTime;
import ReadBam.ReadBam;
import htsjdk.samtools.SamReader;
import ReadBam.BamFileLibrarySize;
import utils.Matrix2CSV;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * @author Benchen Ye
 * @create 2024-11--17:37
 */
public class RNAseqIntervalMain {
    private static int num_datapoints = InputParameterAttributes.num_datapoints;
    public static List<Map<String, Object>> query_regions = QueryWholeRegionCoordinate.query_coord;
    private static int num_core = InputParameterAttributes.core_num;
    private static double trim_ratio = InputParameterAttributes.robust;
    private static String current_time = CurrentTime.getCurrentTime();
    private static String heatmap_data_path = "/Users/bencheye/myProj/ngsPlot/Output/coverage_matrix_heatmap.csv";
    private static String average_coverage_path = "/Users/bencheye/myProj/ngsPlot/Output/average_coverage_matrix.csv";

    public static void rnaseqIntervalMain(String bam_file_path){
        List<Double> coverage_scaled = new ArrayList<>();
        // get the library size of bam file
        SamReader bam_reader = ReadBam.getBamReader(bam_file_path);
        long library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
        // get all query region in the database
        int num_query_regions = query_regions.size();
        System.out.println("---------");
        System.out.println("The number of the genes in the query regions is " + num_query_regions);
        System.out.println("---------");
        // initiation coverage_scaled_matrix (heatmap data)
        double[][] coverage_scaled_matrix = new double[num_query_regions][num_datapoints+1];
        // save the row names of the coverage_scaled_matrix
        List<String> row_names = new ArrayList<>();
        for (int i = 0; i < num_query_regions; i++) {
            System.out.println("---------");
            System.out.println("i is :" + i);
            System.out.println("---------");
            Map<String, Object> record = query_regions.get(i);
            String record_name = record.get("record_name").toString();
            String chr_name = (String) record.get("chrom");
            String nochr_name = (String) record.get("nochr_name");
            row_names.add(record_name);
            System.out.println("The query region name is " + record_name);
            // check whether is the bowtie
            //if(bowtie) {srg = within(srg, mapq[is.na(mapq)] <- 254)}

            chr_name = ReadBam.checkChromosomeInBam(bam_file_path, chr_name, nochr_name);
            if (chr_name != null) {
                coverage_scaled = RNAseqProcessRecordGenomeCoordinateDB.processRecord(record, bam_file_path, chr_name);
                // normalize to RPM.
                coverage_scaled = BamFileLibrarySize.bamSizeNormalization(coverage_scaled, library_size);
            }
            // List<Double> coverage_scaled = PintProcessRecordGenomeCoordinateDB.processRecord(record);
            // add the coverage_scaled to coverage_scaled_matrix
            for (int j = 0; j < coverage_scaled.size(); j++) {
                coverage_scaled_matrix[i][j] = coverage_scaled.get(j);
            }
        }

        // whether is the pair-end bam

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
        Matrix2CSV.saveMatrix2CSV(coverage_scaled_matrix, row_names, heatmap_data_path);
        Matrix2CSV.saveMatrix2CSV(average_coverage_matrix, average_coverage_path);
    }
}
