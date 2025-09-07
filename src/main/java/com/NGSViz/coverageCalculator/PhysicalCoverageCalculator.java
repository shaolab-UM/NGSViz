package com.NGSViz.coverageCalculator;

import com.NGSViz.ReadBam.ReadBam;
import htsjdk.samtools.*;
import htsjdk.samtools.util.Interval;
import java.util.*;
import com.NGSViz.ReadBam.ReadRecordProcess;

/**
 * @author Benchen Ye
 * @create 2024-09--22:25
 * @function calculate the physical coverage for one query region
 */
public class PhysicalCoverageCalculator {



    public static int[] calculatePhysicalCoverage2(SamReader bam_reader,
                                                   Interval interval_range,
                                                   String query_strand)
    {
        String chr_name = interval_range.getContig();
        int query_range_start = interval_range.getStart();
        int query_range_end = interval_range.getEnd();
        // gene range (length in the genome)
        int range_len = query_range_end - query_range_start + 1;

        // *** Use try-with-resources to ensure the iterator is closed ***
        try (SAMRecordIterator iterator = bam_reader.queryContained(chr_name, query_range_start, query_range_end)) {
            // create a list (length = rangeLen, value = 0)
            int[] coverage = new int[range_len];
            while (iterator.hasNext()) {
                SAMRecord bam_record = iterator.next();
                // filter the bam alignment for each read, true filter, false not filter
                boolean filterRes = ReadRecordProcess.filterOneReadAlign(bam_record, query_strand);
                if (filterRes) continue;

                //boolean paired_lab = ReadRecordProcess.checkPairedRecord(bam_record); // unused variable?
                boolean isPairedEnd = bam_record.getReadPairedFlag();
                if (bam_record.getReadUnmappedFlag()) continue;
                // bam filter
                // get the alignment read information
                List<Integer> read_align_info;
                if(isPairedEnd){
                    //if (!bam_record.getFirstOfPairFlag()) continue;
                    if (!bam_record.getProperPairFlag()) continue;
                    read_align_info = ReadRecordProcess.getPairedReadAlignmentInfo(bam_record);
                } else {
                    read_align_info = ReadRecordProcess.getReadAlignmentInfo(bam_record, interval_range);
                }

                if (read_align_info.get(0) != 0) {
                    //System.out.println("--- Processing a read: " + read_align_info);
                    int align_pos_start = read_align_info.get(0);
                    int align_width = read_align_info.get(1);
                    // int align_pos_end = align_pos_start + align_width -1; // Unused after calculation logic

                    // The following block of logic for align_width adjustment based on interval_range
                    // is quite complex and might be simplified. Ensure it's correct for your needs.
                    // It looks like it tries to clip the read to the query range.
                    // A simpler way often involves calculating intersection.
                    int current_align_end = align_pos_start + align_width ; // End exclusive for easier length calc

                    // Calculate overlap with query range [query_range_start, query_range_end]
                    int overlap_start = Math.max(align_pos_start, query_range_start);
                    int overlap_end = Math.min(current_align_end, query_range_end ); // +1 because query_range_end is inclusive

                    int effective_width = overlap_end - overlap_start;

                    if (effective_width > 0) {
                        int pos_start_in_coverage_array = overlap_start - query_range_start;
                        coverage = physicalCoverageCalculate(coverage, pos_start_in_coverage_array, effective_width);
                    }
                }
            }
            return coverage;
        } // The iterator automatically closes here.
        catch (Exception e) {
            // Handle or rethrow exceptions if the iterator fails to acquire
            System.err.println("Error creating iterator for " + chr_name + ":" + query_range_start + "-" + query_range_end + ": " + e.getMessage());
            throw new RuntimeException("Failed to process interval: " + e.getMessage(), e);
        }
    }

    // calculate the physical coverage for a reads in query range using primitive array
    public static int[] physicalCoverageCalculate(int[] physical_coverage,
                                                          int align_start_pos,
                                                          int read_width) {
        if (align_start_pos < 0 || align_start_pos + read_width > physical_coverage.length) {
            throw new IllegalArgumentException("Invalid range: align_start_pos=" + align_start_pos +
                    ", read_width=" + read_width + ", array length=" + physical_coverage.length);
        }

        // Direct array access - no boxing/unboxing overhead
        for (int i = align_start_pos; i < align_start_pos + read_width; i++) {
            physical_coverage[i]++;
        }
        return physical_coverage;
    }
}
