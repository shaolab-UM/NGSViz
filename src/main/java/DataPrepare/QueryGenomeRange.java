package DataPrepare;

import configSet.CommonFinalParas;
import htsjdk.samtools.util.Interval;

/**
 * @author Benchen Ye
 * @create 2024-10--18:03
 */
public class QueryGenomeRange {
    private static int buf_size = CommonFinalParas.buf_size;
    private static int flanking_size = CommonFinalParas.flanking_size;

    // calculate the range coordinate of point interval of query gene
    public static Interval getQueryBamGranges(String chr_name, int middle_point){
        Interval interval_range;
        int query_start = middle_point - flanking_size - buf_size;
        int query_end = middle_point + flanking_size + buf_size;
        interval_range = new Interval(chr_name, query_start, query_end);
        System.out.println("The point interval range is: " + interval_range);
        return interval_range;
    }
    public static Interval getQueryBamGranges(String chr_name,
                                               int start_pos,
                                               int end_pos,
                                               int large_flanking_size)
    {
        Interval interval_range;
        int query_start = start_pos - large_flanking_size - buf_size;
        int query_end = end_pos + large_flanking_size + buf_size;
        interval_range = new Interval(chr_name, query_start, query_end);
        System.out.println("The large interval range is: " + interval_range);
        return interval_range;
    }
}
