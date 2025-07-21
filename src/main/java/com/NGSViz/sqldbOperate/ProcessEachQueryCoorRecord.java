package com.NGSViz.sqldbOperate;

import com.NGSViz.ReadBam.QueryGenomeRange;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.coverageCalculator.EachCoverageResult;
import com.NGSViz.coverageCalculator.PhysicalCoverageCalculator;
import com.NGSViz.coverageCalculator.TrimBuffer;
import com.NGSViz.coverageCalculator.DataScaler;
import htsjdk.samtools.util.Interval;
import com.NGSViz.sqldbOperate.exonMode.CoverageExonSubset;
import com.NGSViz.sqldbOperate.exonMode.ExonModelData;
import htsjdk.samtools.SamReader;

import java.util.Arrays;


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


    public static EachCoverageResult processRecord2(Transcript gene_coord, SamReader bam_reader,
                                         String chr_name, boolean paired_mode) {

        // genome coordinate information
        String query_strand = gene_coord.getQueryStrand();
        String record_name = gene_coord.getRecordName();
        int start_pos = gene_coord.getStartPos();
        int end_pos = gene_coord.getEndPos();
        // get the exon tid name
        String[] parts = record_name.split(":");
        String tid_name = parts.length > 1 ? parts[1] : "";

        // config code need to change when flank_factor exist

        if (flank_factor > 0){
            if(interval_type.equals("exon")){
                // exon specific
                double calculate_res = ExonModelData.getEnstExonWidth(tid_name)*flank_factor;
                flank_size = (int) calculate_res;
            }else {
                double calculate_res = (end_pos - start_pos + 1)*flank_factor;
                flank_size = (int) calculate_res;
            }
        } else {
            flank_size = flanking_size;
        }

        // Specify the genome interval range to calculate the coverage
        // process the start and end position
        if(interval_type.equals("point_interval")){
            // point interval
            int middle_point = getMiddlePointPos(start_pos, end_pos, query_strand);
            // Specify the genome interval range to calculate the coverage
            interval_range = QueryGenomeRange.getQueryBamGranges(chr_name, middle_point);
        } else if(interval_type.equals("exon")){
            System.out.println("----\n");
            // RNA-seq
            System.out.println("--- Performing exon mode! ---");
            start_pos = ExonModelData.getEnstStart(tid_name);
            end_pos = ExonModelData.getEnstEnd(tid_name);
            interval_range = QueryGenomeRange.getQueryBamGranges(chr_name,
                    start_pos, end_pos, flank_size);
        }else{
            start_pos = gene_coord.getStartPos();
            end_pos = gene_coord.getEndPos();
            interval_range = QueryGenomeRange.getQueryBamGranges(chr_name,
                    start_pos, end_pos, flank_size);
        }
        // calculate the physical coverage for each query region
        int[] physical_coverage =
                PhysicalCoverageCalculator.calculatePhysicalCoverage2(bam_reader, interval_range,
                        query_strand, paired_mode);
        // Filter transcripts whose chromosomes do not match bam file.
        if(interval_type.equals("exon")){
            int query_range_start = interval_range.getStart();
            int query_range_end = interval_range.getEnd();
            // integrate exon coverage
            physical_coverage = CoverageExonSubset.getCoverageExonSubset(physical_coverage, tid_name,
                    query_range_start, query_range_end);
        }
        // remove the buffers from the coverage
        physical_coverage = TrimBuffer.trimBuffer2(physical_coverage, buf_size);
        // scale the coverage into the data point for plot
        double[] coverage_scaled = DataScaler.getCoverageScaled(physical_coverage, scaler_method, flank_size);
        // if strand is `-` , reverse the coverage
        if(query_strand.equals("-")){
            reverseDoubleArray(coverage_scaled);
        }
        // Instantiate GeneDbProcessor and call its processing method
        EachCoverageResult record_coverage = new EachCoverageResult(gene_coord, coverage_scaled);
        return record_coverage;
    }

    public static void reverseDoubleArray(double[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        int left = 0;
        int right = array.length - 1;
        while (left < right) {
            double temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
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
