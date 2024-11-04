package ReadBam;

import htsjdk.samtools.SamReader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--11:38
 * @fucntion Calculate library size from bam files for normalization.
 */
public class BamSizeNormalization {
    public static List<Double> bamSizeNormalization(List<Double> integer_list,
                                                    long library_size)
    {
        List<Double> result_list = new ArrayList<>();
        // normalize to RPM
        for (Double num : integer_list) {
            double result = (double) num / library_size * 1e6;
            result_list.add(result);
        }
        return result_list;
    }
}
