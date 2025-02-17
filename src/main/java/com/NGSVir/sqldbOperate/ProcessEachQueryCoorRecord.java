package com.NGSVir.sqldbOperate;

import com.NGSVir.ReadBam.QueryGenomeRange;
import com.NGSVir.configSet.InputParameterAttributes;
import com.NGSVir.coverageCalculator.PhysicalCoverageCalculator;
import com.NGSVir.coverageCalculator.TrimBuffer;
import com.NGSVir.coverageCalculator.DataScaler;
import htsjdk.samtools.util.Interval;
import com.NGSVir.sqldbOperate.exonMode.CoverageExonSubset;
import com.NGSVir.sqldbOperate.exonMode.ExonModelData;
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
        private static String region_plot = InputParameterAttributes.region_type;
        private static int flanking_size = InputParameterAttributes.flank_region;
        private static int buf_size = InputParameterAttributes.buf_size;
        private static String scaler_method = InputParameterAttributes.scaler_method;
        private static double flank_factor = InputParameterAttributes.flank_factor;
        private static String analysis_type = InputParameterAttributes.analysis_type;
        private static String interval_type = InputParameterAttributes.interval_type;
        private static int flank_size;
        private static Interval interval_range;


        public static List<Double> processRecord(Map<String, Object> queryDB_record, String bam_file_path,
                                                 String chr_name, boolean paired_mode) {
            System.out.println("--- Start processing queryDB region record! ---");
            // genome coordinate information
            String query_strand = (String) queryDB_record.get("strand");
            String record_name = (String) queryDB_record.get("record_name");
            int start_pos = (int)queryDB_record.get("start");
            int end_pos = (int)queryDB_record.get("end");
            // get the exon tid name
            String[] parts = record_name.split(":");
            String tid_name = parts.length > 1 ? parts[1] : "";

            // config code need to change when flank_factor exist
            if (flank_factor > 0){
                System.out.println("Processing the flank size with flanking factor: " + flank_factor);
                if(analysis_type.equals("exon")){
                    // exon specific
                    double calculate_res = ExonModelData.getEnstExonWidth(tid_name)*flank_factor;
                    flank_size = (int) calculate_res;
                } else {
                    double calculate_res = (end_pos - start_pos + 1)*flank_factor;
                    flank_size = (int) calculate_res;
                }
            } else {
                flank_size = flanking_size;
            }
            System.out.println("flank_size is : " + flank_size);
            // Specify the genome interval range to calculate the coverage
            // process the start and end position
            if(interval_type.equals("point_interval")){
                // point interval
                System.out.println("--- Performing point interval analysis mode for chipseq! ---");
                int middle_point = getMiddlePointPos(start_pos, end_pos, query_strand);
                System.out.println("The middle point is " + middle_point);
                // Specify the genome interval range to calculate the coverage
                interval_range = QueryGenomeRange.getQueryBamGranges(chr_name, middle_point);
                System.out.println("--- The interval range is: " + interval_range);

            } else {
                if(analysis_type.equals("exon")){
                    // RNA-seq
                    System.out.println("--- Performing exon mode! ---");
                    start_pos = ExonModelData.getEnstStart(tid_name);
                    end_pos = ExonModelData.getEnstEnd(tid_name);
                } else {
                    // broad interval
                    System.out.println("--- Performing broad interval analysis mode for chipseq! ---");
                    start_pos = (int) queryDB_record.get("start");
                    end_pos = (int) queryDB_record.get("end");
                }
                interval_range = QueryGenomeRange.getQueryBamGranges(chr_name,
                        start_pos, end_pos, flank_size);
            }
            // calculate the physical coverage for each query region
            System.out.println("--- Starting calculate physical coverage for interval: " + interval_range);
            ArrayList<Integer> physical_coverage =
                    PhysicalCoverageCalculator.calculatePhysicalCoverage(bam_file_path, interval_range,
                            query_strand, paired_mode);

            // Filter transcripts whose chromosomes do not match bam file.
            if(analysis_type.equals("exon")){
                int query_range_start = interval_range.getStart();
                int query_range_end = interval_range.getEnd();
                // integrate exon coverage
                physical_coverage = CoverageExonSubset.getCoverageExonSubset(physical_coverage, tid_name,
                        query_range_start, query_range_end);
            }

            // remove the buffers from the coverage
            physical_coverage = TrimBuffer.trimBuffer(physical_coverage, buf_size);
            // scale the coverage into the data point for plot
            List<Double> coverage_scaled = DataScaler.getCoverageScaled(physical_coverage, scaler_method, flank_size);
            // if strand is `-` , reverse the coverage
            if(query_strand.equals("-")){
                System.out.println("query strand: " + query_strand);
                System.out.println("reverse the scaled coverage. " );
                Collections.reverse(coverage_scaled);
            }
            // (done in the spline method) Floor negative values which are caused by spline.
            System.out.println(coverage_scaled);
            System.out.println("finishing record: " + queryDB_record);
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
