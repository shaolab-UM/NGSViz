package ReadBam;


import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.File;
import java.io.IOException;

/**
 * @author Benchen Ye
 * @create 2024-11--19:49
 */
/*
public class CheckBamSort {
    private static String bam_file_path = CommonFinalParas.bam_file;
    public static void main(String[] args) {
        try {
            SamReaderFactory readerFactory = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT);
            SamReader samReader = readerFactory.open(new File(bam_file_path));
            SAMFileHeader fileHeader = samReader.getFileHeader();
            if (fileHeader.getSortOrder() == SAMFileHeader.SortOrder.coordinate) {
                System.out.println("The BAM file has been sorted according to coordinates.");
            } else if (fileHeader.getSortOrder() == SAMFileHeader.SortOrder.queryname) {
                System.out.println("The BAM file has been sorted according to query names.");
            } else if (fileHeader.getSortOrder() == SAMFileHeader.SortOrder.unsorted) {
                System.out.println("The BAM file is not sorted.");
            } else {
                System.out.println("The BAM file is in other sorting methods or the sorting information cannot be recognized.");
            }
            samReader.close();
        } catch (IOException e) {
            System.err.println("Error occurred while reading BAM file: " + e.getMessage());
        }
    }
}
*/
