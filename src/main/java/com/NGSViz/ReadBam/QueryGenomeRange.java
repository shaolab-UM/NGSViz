package com.NGSViz.ReadBam;

import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.sqldbOperate.exonMode.ExonModelData;
import htsjdk.samtools.QueryInterval;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.util.Interval;

import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-10--18:03
 * @function build the interval region range for query bam read
 */
public class QueryGenomeRange {
    private static int buf_size = InputParameterAttributes.buf_size;
    private static int flanking_size = InputParameterAttributes.flank_region;
    private static int flank_size;
    private static Interval interval_range;
    private static String region_plot = InputParameterAttributes.region_type;
    private static double flank_factor = InputParameterAttributes.flank_factor;
    private static String interval_type = InputParameterAttributes.interval_type;

    // calculate the range coordinate of point interval of query gene
    public static Interval getQueryBamGranges(String chr_name, int middle_point){
        Interval interval_range;
        int query_start = middle_point - flanking_size - buf_size;
        // htsjdk Interval uses 1-based closed coordinates; minimum valid start is 1.
        if(query_start < 1){
            query_start = 1;
        }
        int query_end = middle_point + flanking_size + buf_size;
        interval_range = new Interval(chr_name, query_start, query_end);
        //System.out.println("The point interval range is: " + interval_range);
        return interval_range;
    }

    // Merge the overlapping areas of intervals and convert them into queryInterval for easy querying.
    public static QueryInterval[] mergeIntervals(List<Interval> intervals, SAMFileHeader header) {
        QueryInterval[] qis = intervals.stream()
                .map(iv -> new QueryInterval(
                        header.getSequenceIndex(iv.getContig()),
                        iv.getStart(),
                        iv.getEnd()))
                .toArray(QueryInterval[]::new);
        return QueryInterval.optimizeIntervals(qis);
        //return qis;
    }

    // calculate the range coordinate of broad interval of query gene for epigenetic
    public static Interval getQueryBamGranges(String chr_name,
                                               int start_pos,
                                               int end_pos,
                                               int flank_size)
    {
        Interval interval_range;
        int query_start = start_pos - flank_size - buf_size;
        // htsjdk Interval uses 1-based closed coordinates; minimum valid start is 1.
        if(query_start < 1){
            query_start = 1;
        }
        int query_end = end_pos + flank_size + buf_size;
        interval_range = new Interval(chr_name, query_start, query_end);
        //System.out.println("The large interval range is: " + interval_range);
        return interval_range;
    }
    /*
     * start to process the query DB results to generate the interval range.
     * chr_name need to coordinate with the chr format in bam
     * */
    public static Interval generateInterval(String record_name,
                                     String query_strand,
                                     String chr_name,
                                     int start_pos,
                                     int end_pos){
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
            start_pos = start_pos;
            end_pos = end_pos;
            interval_range = QueryGenomeRange.getQueryBamGranges(chr_name,
                    start_pos, end_pos, flank_size);
        }
        return interval_range;
    }

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
