package com.NGSViz.sqldbOperate.exonMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--21:01
 * @fucntion integrate exon coverage
 */
import java.util.Arrays;
import java.util.List;

public class CoverageExonSubset {
    public static int[] getCoverageExonSubset(int[] physical_coverage, String tid_name,
                                              int query_range_start, int query_range_end) {
        System.out.println("---------");
        System.out.println("Processing exon coverage, its id is : " + tid_name);

        // Calculate the total length of the query range
        int range_len = query_range_end - query_range_start + 1;

        // Retrieve the start and end coordinates of all exons for this transcript
        List<Integer> exon_start_list = ExonModelData.getExonStartList(tid_name);
        List<Integer> exon_end_list = ExonModelData.getExonEndList(tid_name);
        int exon_list_len = exon_start_list.size();

        // Allocate a buffer array with the maximum possible length (the entire query range)
        int[] exon_coverage_buffer = new int[range_len];

        // Record the current number of extracted exon bases, which will be the actual length of the returned array
        int currentIndex = 0;

        for (int i = 0; i < exon_list_len; i++) {
            // Convert absolute genomic coordinates to relative coordinates based on the query range
            int exon_start = exon_start_list.get(i) - query_range_start;
            int exon_end = exon_end_list.get(i) - query_range_start;

            // Handle boundary logic for the first and last exons (kept original logic, likely for flanking sequences/buffer)
            if (i == 0) {
                exon_start = 0;
            }
            if (i == exon_list_len - 1) {
                exon_end = range_len - 1;
            }

            // Fix 1: Change j < exon_end to j <= exon_end
            // Ensure the last base of the 1-based closed interval [exon_start, exon_end] is correctly extracted to avoid data loss
            for (int j = exon_start; j <= exon_end; j++) {
                // Add a boundary safety check to prevent OutOfBounds exceptions if the physical_coverage length mismatches range_len
                if (j >= 0 && j < physical_coverage.length) {
                    exon_coverage_buffer[currentIndex++] = physical_coverage[j];
                }
            }
        }

        // Removed the meaningless ArrayList logic from the original code that was never returned
        // ArrayList<Integer> exon_coverage = new ArrayList<>();
        // exon_coverage.addAll(exon_start_list);

        // Fix 2: Truncate the array based on the actual number of extracted bases (currentIndex) and return it
        // This completely eliminates the trailing zero-padding pollution caused by the fixed-length array in the original code
        return Arrays.copyOf(exon_coverage_buffer, currentIndex);
    }
}
