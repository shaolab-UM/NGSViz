package coverageCalculator;

import configSet.CommonFinalParas;
import htsjdk.samtools.util.Interval;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Benchen Ye
 * @create 2024-09--17:12
 */


// attribute: intervalRange, refer to a genome region to query the coverage based on the bam file
public class GetGeneAlignmentInfo{
    private int flank_size = CommonFinalParas.flanking_size;
    // class attributes
    private Interval intervalRange;
    // construct function
    public GetGeneAlignmentInfo(Interval intervalRange){
        this.intervalRange = intervalRange;
    }

    // remove bufsize
    public ArrayList<Integer>  removeBufsize(ArrayList<Integer> physical_coverage){
        // calculate the total length of query range
        int total_query_length = physical_coverage.size();
        int mid_width = total_query_length - 2*flank_size;
        ArrayList<Integer> left_coverage = new ArrayList<>(physical_coverage.subList(0, flank_size));
        ArrayList<Integer> right_coverage = new ArrayList<>(physical_coverage.subList(flank_size+mid_width, total_query_length));
        ArrayList<Integer> mid_coverage = new ArrayList<>(physical_coverage.subList(flank_size, flank_size+mid_width));

        ArrayList<Integer> x = new ArrayList<>(Collections.nCopies(1, 0));;
        return x;
    }
}






