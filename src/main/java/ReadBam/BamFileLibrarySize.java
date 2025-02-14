package ReadBam;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--10:44
 * @function get the library size of bam file which pass the quality control
 */
public class BamFileLibrarySize {

    public static long getLibrarySize(SamReader bam_reader) {
        long librarySize = 0;
        // Set filter conditions to only count read segments that pass quality control, are non-repetitive, and have been mapped.
        boolean countOnlyQualifiedReads = true;
        for (SAMRecord samRecord : bam_reader) {
            if (!countOnlyQualifiedReads ||
                    (samRecord.getMappingQuality() >= 0 &&
                            !samRecord.getDuplicateReadFlag() &&
                            samRecord.getReadUnmappedFlag() == false)) {
                librarySize++;
            }
        }
        System.out.println("The library size of this bam file is : " + librarySize);
        return librarySize;
    }
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
