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
                                                               String query_strand,
                                                               boolean paired_mode)
    {
        String chr_name = interval_range.getContig();
        int query_range_start = interval_range.getStart();
        int query_range_end = interval_range.getEnd();
        // gene range (length in the genome)
        int range_len = query_range_end - query_range_start + 1;
        SAMRecordIterator iterator = bam_reader.queryContained(chr_name, query_range_start, query_range_end);
        // create a list (length = rangeLen, value = 0)
        int[] coverage = new int[range_len];
        while (iterator.hasNext()) {
            SAMRecord bam_record = iterator.next();
            // filter the bam alignment for each read, true filter, false not filter
            boolean filterRes = ReadRecordProcess.filterOneReadAlign(bam_record, query_strand);
            boolean paired_lab = ReadRecordProcess.checkPairedRecord(bam_record);
            boolean isPairedEnd = bam_record.getReadPairedFlag();

            if (isPairedEnd) {
                boolean paired_filter = ReadRecordProcess.checkPairedRecordQuality(bam_record);
                filterRes = filterRes & !paired_filter;
            }

            if (bam_record.getReadUnmappedFlag()) continue;
            if (filterRes) continue;

            // bam filter
            // get the alignment read information
            List<Integer> read_align_info;
            if(isPairedEnd){
                if (!bam_record.getFirstOfPairFlag()) continue;
                if (!bam_record.getProperPairFlag()) continue;
                read_align_info = ReadRecordProcess.getPairedReadAlignmentInfo(bam_record);
            } else {
                read_align_info = ReadRecordProcess.getReadAlignmentInfo(bam_record, interval_range);
            }

            if (read_align_info.get(0) != 0) {
                //System.out.println("--- Processing a read: " + read_align_info);
                int align_pos_start = read_align_info.get(0);
                int align_width = read_align_info.get(1);
                int align_pos_end = align_pos_start + align_width -1;
                // read start position in the search range
                // index start from zero, so no add 1

                int pos_start_difference = align_pos_start - query_range_start;
                if(pos_start_difference<0){
                    align_width = align_width + pos_start_difference;
                    pos_start_difference = 0;
                }
                if(align_pos_start >= query_range_start){
                    if(align_pos_start >= query_range_end){
                        align_width = 0;
                    }else if(align_pos_end >= query_range_end){
                        align_width = query_range_end - align_pos_start + 1;
                    }else if(align_pos_end < query_range_end){
                        align_width = align_width;
                    }
                }else{
                    if(align_pos_end <= query_range_start){
                        align_width = 0;
                    }else{
                        align_width = align_pos_end - query_range_start + 1;
                    }
                }
                //System.out.println("align start is : " + pos_start_difference);
                coverage = physicalCoverageCalculate(coverage, pos_start_difference, align_width);
            }

        }
        return coverage;
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
