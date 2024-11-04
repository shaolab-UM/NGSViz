package sqldbOperate;

import ReadBam.QueryGenomeRange;
import configSet.CommonFinalParas;
import coverageCalculator.PhysicalCoverageCalculator;
import coverageCalculator.dataScaler.DataScaler;
import htsjdk.samtools.util.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-10--20:14
 */
public class PintProcessRecordGenomeCoordinateDB {
    private static String region_plot = CommonFinalParas.region_plot;
    private static String bam_file_path = CommonFinalParas.bam_file;
    private static int flanking_size = CommonFinalParas.flanking_size;
    private static int buf_size = CommonFinalParas.buf_size;
    private static String scaler_method = CommonFinalParas.scaler_method;
    private static int num_datapoints = CommonFinalParas.num_datapoints;



    public static List<Double> processRecord(Map<String, Object> record) {
        List<Double> coverage_scaled = new ArrayList<>();
        System.out.println("Processing record: " + record);
        // genome coordinate information
        String chr_name = (String) record.get("chrom");
        int start_pos = (int) record.get("start");
        int end_pos = (int) record.get("end");
        String query_strand = (String) record.get("strand");
        int middle_point = getMiddlePointPos(start_pos, end_pos, query_strand);
        System.out.println("The middle point is " + middle_point);
        // Specify the genome interval range to calculate the coverage
        Interval interval_range = QueryGenomeRange.getQueryBamGranges(chr_name, middle_point);
        // calculate the physical coverage for each query region
        ArrayList<Integer> physical_coverage =
                PhysicalCoverageCalculator.calculatePhysicalCoverage(bam_file_path, interval_range, query_strand);
        //System.out.println("Physical coverage: " + physical_coverage);

        // remove the bufsize

        // scale the coverage into the data point for plot
        DataScaler data_scaler = new DataScaler(physical_coverage);
        coverage_scaled = data_scaler.getCoverageScaled(scaler_method);
        System.out.println(coverage_scaled);
        System.out.println("finishing record: " + record);
        return coverage_scaled;

    }
    // identify the middle point coordinate for the point interval of query gene
    private static int getMiddlePointPos(int start_pos, int end_pos, String strand) {
        int middle_point;
        boolean cond_1 = region_plot.equals("tss") && strand.equals("+");
        boolean cond_2 = region_plot.equals("tes") && strand.equals("-");
        if ( cond_1 || cond_2){
            middle_point = start_pos;
        } else {
            middle_point = end_pos;
        }
        return middle_point;
    }

}
