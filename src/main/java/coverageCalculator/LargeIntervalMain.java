package coverageCalculator;

import configSet.InputParameterAttributes;
import sqldbOperate.QueryWholeRegionCoordinate;
import sqldbOperate.RNAseqProcessRecordGenomeCoordinateDB;
import utils.CurrentTime;
import ReadBam.ReadBam;
import htsjdk.samtools.SamReader;
import ReadBam.BamFileLibrarySize;
import sqldbOperate.LargeChIPProcessRecordGenomeCoordinateDB;
import utils.Matrix2CSV;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-10--18:28
 */
public class LargeIntervalMain {
    private static int num_datapoints = InputParameterAttributes.num_datapoints;
    public static List<Map<String, Object>> query_regions = QueryWholeRegionCoordinate.query_coord;
    private static String analysis_type = InputParameterAttributes.analysis_type;
    private static int num_core = InputParameterAttributes.core_num;

    public static double[][] largeIntervalMain(String bam_file_path) {
        List<Double> coverage_scaled = new ArrayList<>();
        // get the library size of bam file
        SamReader bam_reader = ReadBam.getBamReader(bam_file_path);
        long library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
        // get all query region in the database
        //List<Map<String, Object>> query_regions = SQLiteQuery.queryGenomeCoorDatabaseRecord(query_refname);
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
                if(analysis_type.equals("rnaseq")){
                    coverage_scaled = RNAseqProcessRecordGenomeCoordinateDB.processRecord(record, bam_file_path, chr_name);
                }else{
                    coverage_scaled = LargeChIPProcessRecordGenomeCoordinateDB.processRecord(record, bam_file_path, chr_name);
                }
                // normalize to RPM.
                coverage_scaled = BamFileLibrarySize.bamSizeNormalization(coverage_scaled, library_size);
            }
            // List<Double> coverage_scaled = PintProcessRecordGenomeCoordinateDB.processRecord(record);
            // add the coverage_scaled to coverage_scaled_matrix
            for (int j = 0; j < coverage_scaled.size(); j++) {
                coverage_scaled_matrix[i][j] = coverage_scaled.get(j);
            }
        }
        return coverage_scaled_matrix;
    }
}
