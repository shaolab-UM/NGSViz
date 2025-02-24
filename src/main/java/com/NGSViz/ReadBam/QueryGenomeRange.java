package com.NGSViz.ReadBam;

import com.NGSViz.configSet.InputParameterAttributes;
import htsjdk.samtools.util.Interval;

/**
 * @author Benchen Ye
 * @create 2024-10--18:03
 * @function build the interval region range for query bam read
 */
public class QueryGenomeRange {
    private static int buf_size = InputParameterAttributes.buf_size;
    private static int flanking_size = InputParameterAttributes.flank_region;

    // calculate the range coordinate of point interval of query gene
    public static Interval getQueryBamGranges(String chr_name, int middle_point){
        Interval interval_range;
        int query_start = middle_point - flanking_size - buf_size;
        if(query_start < 0){
            query_start = 0;
        }
        int query_end = middle_point + flanking_size + buf_size;
        interval_range = new Interval(chr_name, query_start, query_end);
        //System.out.println("The point interval range is: " + interval_range);
        return interval_range;
    }
    // calculate the range coordinate of broad interval of query gene for epigenetic
    public static Interval getQueryBamGranges(String chr_name,
                                               int start_pos,
                                               int end_pos,
                                               int flank_size)
    {
        Interval interval_range;
        int query_start = start_pos - flank_size - buf_size;
        if(query_start < 0){
            query_start = 0;
        }
        int query_end = end_pos + flank_size + buf_size;
        interval_range = new Interval(chr_name, query_start, query_end);
        //System.out.println("The large interval range is: " + interval_range);
        return interval_range;
    }
}
