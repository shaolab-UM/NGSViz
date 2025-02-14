package coverageCalculator;

import configSet.InputParameterAttributes;
import sqldbOperate.*;
import ReadBam.ReadBam;
import htsjdk.samtools.SamReader;
import ReadBam.BamFileLibrarySize;

import java.util.*;

/**
 * @author Benchen Ye
 * @create 2024-10--18:28
 */
public class EachQueryRegionProcess {
    private static int num_datapoints = InputParameterAttributes.num_datapoints;
    public static List<Map<String, Object>> query_regions = QueryWholeRegionCoordinateCP.query_coord;
    private static String analysis_type = InputParameterAttributes.analysis_type;
    private static String interval_type = InputParameterAttributes.interval_type;
    private static int num_core = InputParameterAttributes.core_num;

    public static HashMap<String, Object> processEachQueryRegion(String bam_file_path, boolean paired_mode) {
        List<Double> coverage_scaled = new ArrayList<>();
        // get the library size of bam file
        SamReader bam_reader = ReadBam.getBamReader(bam_file_path);
        long library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
        // get all query region in the database
        int num_query_regions = query_regions.size();
        System.out.println("The number of the genes in the query regions is " + num_query_regions);
        // initiation coverage_scaled_matrix (heatmap data)
        double[][] coverage_scaled_matrix = new double[num_query_regions][num_datapoints+1];
        // save the row names of the coverage_scaled_matrix
        List<String> row_names = new ArrayList<>();

        // get the chromosome list in bam file
        Set<String> bam_chromosomes_list = ReadBam.getChromosomesInBam(bam_file_path);

        for (int i = 0; i < num_query_regions; i++) {
            System.out.println("> ---------");
            //System.out.println("Processing the coordinate region for the i-th query gene : " + i);
            Map<String, Object> queryDB_record = query_regions.get(i);

            System.out.println("This query region range is: " + queryDB_record);
            String record_name = queryDB_record.get("record_name").toString();

            row_names.add(record_name);
            System.out.println("The query region name is " + record_name);

            // check whether is the bowtie
            //if(bowtie) {srg = within(srg, mapq[is.na(mapq)] <- 254)}

            // check whether query char name is in bam
            String chr_name = (String) queryDB_record.get("chrom");
            String nochr_name = (String) queryDB_record.get("nochr_name");
            chr_name = ReadBam.checkChromosomeInBam(bam_chromosomes_list, chr_name, nochr_name);

            if (chr_name != null) {
                // calculate the coverage for each queryDB region
                coverage_scaled = ProcessEachQueryCoorRecord.processRecord(queryDB_record, bam_file_path, chr_name, paired_mode);
                // normalize to RPM.
                coverage_scaled = BamFileLibrarySize.bamSizeNormalization(coverage_scaled, library_size);
            }

            // add the coverage_scaled to coverage_scaled_matrix
            for (int j = 0; j < coverage_scaled.size(); j++) {
                coverage_scaled_matrix[i][j] = coverage_scaled.get(j);
            }
        }
        HashMap<String, Object> cal_res_map = new HashMap<>();
        cal_res_map.put("row_names", row_names);
        cal_res_map.put("coverage_matrix", coverage_scaled_matrix);
        return cal_res_map;
    }
}
