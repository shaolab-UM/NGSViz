package com.NGSViz.coverageCalculator;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DataScalerTest {
    @Test
    void distributesRemainderAcrossBinsInsteadOfAddingItToFinalBin() {
        int[] coverage = IntStream.range(0, 4_001).toArray();

        double[] scaled = DataScaler.bin(coverage, 101, "mean");

        assertEquals(19.0, scaled[0]);
        assertEquals(3_980.5, scaled[100]);
    }

    @Test
    void distributesValuesWhenThereAreMoreBinsThanPositions() {
        double[] scaled = DataScaler.bin(new int[]{10, 20, 30}, 5, "mean");

        assertArrayEquals(new double[]{0.0, 10.0, 0.0, 20.0, 30.0}, scaled);
    }
}
