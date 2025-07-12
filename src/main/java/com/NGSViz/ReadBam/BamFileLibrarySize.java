package com.NGSViz.ReadBam;

import com.NGSViz.configSet.InputParameterAttributes;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;

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

}
