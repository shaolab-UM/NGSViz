import configSet.InputParameterAttributes;

import java.util.ArrayList;


/**
 * @author Benchen Ye
 * @create 2024-11--11:35
 */
public class test extends InputParameterAttributes {
    public static void main(String[] args) {
        /*
        int align_pos_start = readAlignInfo.get(0);
        int qwidth = readAlignInfo.get(1);
        // read start position in the search range
        // index start from zero, so no add 1
        align_pos_start = align_pos_start - query_range_start;
        // calculate the physical coverage
        coverage = physicalCoverageCalculate(coverage, align_pos_start, qwidth);

         */

    }
    public static ArrayList<Integer> physicalCoverageCalculate(ArrayList<Integer> physical_coverage,
                                                               int align_start_pos,
                                                               int read_width) {
        // the coverage value of the region covered by read coverage will add 1
        for (int i = align_start_pos; i < align_start_pos + read_width; i++) {
            // add 1 for the value of i
            physical_coverage.set(i, physical_coverage.get(i) + 1);
        }
        return physical_coverage;
    }
}
