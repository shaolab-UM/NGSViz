package sqldbOperate;

import ReadBam.QueryGenomeRange;
import configSet.CommonFinalParas;
import configSet.InputParameterAttributes;
import coverageCalculator.PhysicalCoverageCalculator;
import coverageCalculator.TrimBuffer;
import coverageCalculator.dataScaler.DataScaler;
import htsjdk.samtools.util.Interval;
import sqldbOperate.exonMode.CoverageExonSubset;
import sqldbOperate.exonMode.ExonModelData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-10--16:16
 * @function calculate the scaled coverage of large ChIP for each query region (record)
 */
public class ProcessEachQueryCoorRecord {
        private static String region_plot = InputParameterAttributes.region_plot;
        //private static String bam_file_path = CommonFinalParas.bam_file;
        private static int flanking_size = InputParameterAttributes.flank_region;
        private static int buf_size = InputParameterAttributes.buf_size;
        private static String scaler_method = InputParameterAttributes.scaler_method;
        private static double flank_factor = InputParameterAttributes.flank_factor;
        private static String analysis_type = InputParameterAttributes.analysis_type;
        private static String interval_type = InputParameterAttributes.interval_type;
        private static int flank_size;
        private static int start_pos;
        private static int end_pos;
        private static Interval interval_range;


        public static List<Double> processRecord(Map<String, Object> record, String bam_file_path, String chr_name) {
            System.out.println("Processing record: " + record);
            // genome coordinate information
            String query_strand = (String) record.get("strand");
            String record_name = (String) record.get("record_name");
            String[] parts = record_name.split(":");
            String tid_name = parts.length > 1 ? parts[1] : "";

            // get the number of middle point and flank_point
            //int num_middle_point = InputParameterAttributes.middle_points;
            //int num_flank_point = InputParameterAttributes.flank_points;

            // config code need to change when flank_factor exist
            if (flank_factor > 0){
                if(analysis_type.equals("rnaseq")){
                    // RNA-seq specific
                    double calculate_res = ExonModelData.getEnstExonWidth(tid_name)*flank_factor;
                    flank_size = (int) calculate_res;
                } else {
                    double calculate_res = (end_pos - start_pos + 1)*flank_factor;
                    flank_size = (int) calculate_res;
                }
            } else {
                flank_size = flanking_size;
            }
            // process the start and end position
            if(analysis_type.equals("rnaseq")){
                // RNA-seq
                start_pos = ExonModelData.getEnstStart(tid_name);
                end_pos = ExonModelData.getEnstEnd(tid_name);
            } else {
                // broad interval
                start_pos = (int) record.get("start");
                end_pos = (int) record.get("end");
            }
            // Specify the genome interval range to calculate the coverage
            if(interval_type.equals("point_interval")){
                // point interval
                int middle_point = getMiddlePointPos(start_pos, end_pos, query_strand);
                System.out.println("The middle point is " + middle_point);
                // Specify the genome interval range to calculate the coverage
                interval_range = QueryGenomeRange.getQueryBamGranges(chr_name, middle_point);
            } else {
                // large interval
                interval_range = QueryGenomeRange.getQueryBamGranges(chr_name,
                        start_pos, end_pos, flank_size);
            }

            //System.out.println("interval_range: " + interval_range);

            // calculate the physical coverage for each query region
            ArrayList<Integer> physical_coverage =
                    PhysicalCoverageCalculator.calculatePhysicalCoverage(bam_file_path, interval_range, query_strand);

            // Filter transcripts whose chromosomes do not match bam file.

            if(analysis_type.equals("rnaseq")){
                int query_range_start = interval_range.getStart();
                int query_range_end = interval_range.getEnd();
                // process exon coverage
                physical_coverage = CoverageExonSubset.getCoverageExonSubset(physical_coverage, tid_name,
                        query_range_start, query_range_end);
            }

            // remove the buffers from the coverage
            physical_coverage = TrimBuffer.trimBuffer(physical_coverage, buf_size);
            // scale the coverage into the data point for plot
            DataScaler data_scaler = new DataScaler(physical_coverage);
            List<Double> coverage_scaled = data_scaler.getCoverageScaled(scaler_method);
            // if strand is `-` , reverse the coverage
            if(query_strand.equals("-")){
                System.out.println("query strand: " + query_strand);
                System.out.println("reverse the scaled coverage. " );
                Collections.reverse(coverage_scaled);
            }
            // (done in the spline method) Floor negative values which are caused by spline.


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
