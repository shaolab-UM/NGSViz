package com.NGSViz.sqldbOperate.exonMode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CoverageExonSubsetTest {
    @AfterEach
    void clearExonCache() {
        ExonModelData.clear();
    }

    @Test
    void keepsFlanksAndSkipsIntronsBetweenExons() {
        ExonModelData.exon_matrix.put("TX1", new OneEnstAllExonCoorClass(
                "TX1", 5, 11, 4,
                Arrays.asList(5, 10), Arrays.asList(6, 11), Arrays.asList(2, 2)));

        int[] coverage = IntStream.rangeClosed(1, 20).toArray();
        int[] result = CoverageExonSubset.getCoverageExonSubset(coverage, "TX1", 1, 20);

        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, result);
    }
}
