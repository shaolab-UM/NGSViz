package coverageCalculator;

import DataPrepare.CurrentTime;
import ReadBam.ReadBam;
import configSet.CommonFinalParas;
import htsjdk.samtools.SamReader;
import ReadBam.BamFileLibrarySize;
import ReadBam.BamSizeNormalization;
import sqldbOperate.LargeChIPProcessRecordGenomeCoordinateDB;
import sqldbOperate.SQLiteQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-10--18:28
 */
public class LargeIntervalMain {
    private static int num_datapoints = CommonFinalParas.num_datapoints;
    private static String bam_file_path = CommonFinalParas.bam_file;
    private static int num_core = CommonFinalParas.cores_num;
    private static int trim_ratio = CommonFinalParas.robust;
    private static String current_time = CurrentTime.getCurrentTime();
    private static String heatmap_data_path = "/Users/bencheye/myProj/ngsPlot/Output/coverage_matrix_heatmap.csv";
    private static String average_coverage_path = "/Users/bencheye/myProj/ngsPlot/Output/average_coverage_matrix.csv";


    public static void main(String[] args) {
        String query_refname = "hg19.ensembl.genebody.protein_coding";
        List<Double> coverage_scaled = new ArrayList<>();
        // get the library size of bam file
        SamReader bam_reader = ReadBam.getBamReader(bam_file_path);
        long library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
        // get all query region in the database
        List<Map<String, Object>> query_regions = SQLiteQuery.queryGenomeCoorDatabaseRecord(query_refname);
        int num_query_regions = query_regions.size();
        System.out.println("The number of the genes in the query regions is " + num_query_regions);
        // initiation coverage_scaled_matrix (heatmap data)
        double[][] coverage_scaled_matrix = new double[num_query_regions][num_datapoints+1];
        // save the row names of the coverage_scaled_matrix
        List<String> row_names = new ArrayList<>();
        for (int i = 0; i < num_query_regions; i++) {
            Map<String, Object> record = query_regions.get(i);
            String record_name = record.get("record_name").toString();
            String chr_name = (String) record.get("chrom");
            row_names.add(record_name);
            System.out.println("The query region name is " + record_name);
            // check whether is the bowtie
            //if(bowtie) {srg = within(srg, mapq[is.na(mapq)] <- 254)}

            boolean chromosome_in_bam = ReadBam.checkChromosomeInBam(bam_file_path, chr_name);
            if (chromosome_in_bam) {
                coverage_scaled = LargeChIPProcessRecordGenomeCoordinateDB.processRecord(record);
                // normalize to RPM.
                coverage_scaled = BamSizeNormalization.bamSizeNormalization(coverage_scaled, library_size);
            } else {
                System.out.println(chr_name + " isn't in the bam");
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
