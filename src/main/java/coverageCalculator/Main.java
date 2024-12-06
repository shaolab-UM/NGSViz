package coverageCalculator;

import configSet.CommonFinalParas;
import coverageCalculator.dataScaler.DataScaler;
import htsjdk.samtools.util.Interval;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-09--22:12
 */
public class Main {
    public static void main(String[] args) {
        // Input
        //String workDir = "/Users/bencheye/myProj/ngsPlot/";
        String bam_file_path = CommonFinalParas.bam_file;
        // chromosome start end
        String chr_name = "chrX";
        //int start = 74273115;
        //int end = 74376132;
        int qStart = 72270115;
        int qEnd = 72271115;
        String query_strand = "+";
        String scaler_method = CommonFinalParas.scaler_method;
        boolean paired_mode = false;
        // Specify the genome interval range to calculate the coverage
        Interval interval_range = new Interval(chr_name, qStart, qEnd);
        // calculate the physical coverage for each query region
        ArrayList<Integer> physical_coverage =
                PhysicalCoverageCalculator.calculatePhysicalCoverage(bam_file_path, interval_range, query_strand, paired_mode);
        System.out.println("Physical coverage: " + physical_coverage);
        //List<Double> averages = data_scaler.getCoverageScaled("peakSelection");
        //System.out.println(averages);

        // calculate the physical coverage for each query region
        // remove the bufsize

        // scale the coverage into the data point for plot
        DataScaler data_scaler = new DataScaler(physical_coverage);
        List<Double> coverage_scaled = data_scaler.getCoverageScaled(scaler_method);
        System.out.println(coverage_scaled);
    }
}
