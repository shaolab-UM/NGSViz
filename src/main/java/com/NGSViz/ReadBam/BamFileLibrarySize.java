package com.NGSViz.ReadBam;

import com.NGSViz.configSet.InputParameterAttributes;
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
    // spike-in calibration
    private static double scale_ratio = InputParameterAttributes.scale_ratio;

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
    public static double[] bamSizeNormalization(double[] coverage_scaled,
                                                    long library_size)
    {
        double[] result_list = new double[coverage_scaled.length];
        // normalize to RPM
        for (int i = 0; i < coverage_scaled.length; i++) {
            double num = coverage_scaled[i];
            // num / library_size * 1e6
            double result = num / library_size * 1_000_000.0 * scale_ratio;
            result_list[i] = result;
        }
        return result_list;
    }
}
