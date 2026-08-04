package com.NGSViz.coverageCalculator;

import com.NGSViz.utils.SparseMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackgrouNormalizerTest {
    @Test
    void usesLibrarySpecificPseudocountsForCpmNormalization() {
        SparseMatrix signal = new SparseMatrix(1, 1);
        SparseMatrix input = new SparseMatrix(1, 1);
        signal.set(0, 0, 1.0);

        SparseMatrix result = BackgrouNormalizer.backgroundNormalize(
                signal, input, 20_000_000L, 20_000_000L, null
        );

        assertEquals(4.392317422778761, result.get(0, 0), 1e-12);
    }

    @Test
    void usesScaleRatioAsSignalPseudocountForSpikeInNormalization() {
        SparseMatrix signal = new SparseMatrix(1, 1);
        SparseMatrix input = new SparseMatrix(1, 1);

        SparseMatrix result = BackgrouNormalizer.backgroundNormalize(
                signal, input, 20_000_000L, 20_000_000L, 0.2
        );

        assertEquals(2.0, result.get(0, 0), 1e-12);
    }
}
