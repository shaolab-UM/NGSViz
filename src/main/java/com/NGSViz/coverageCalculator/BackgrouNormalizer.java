package com.NGSViz.coverageCalculator;

import com.NGSViz.utils.SparseMatrix;

public class BackgrouNormalizer {
    public static SparseMatrix backgroundNormalize(
            SparseMatrix signalMat,
            SparseMatrix inputMat,
            long signalLibrarySize,
            long inputLibrarySize,
            Double signalScaleRatio
    ) {
        if (signalLibrarySize <= 0 || inputLibrarySize <= 0) {
            throw new IllegalArgumentException("Library size must be greater than zero.");
        }

        int[] signalDims = signalMat.getDimensions();
        int[] inputDims = inputMat.getDimensions();
        if (signalDims[0] != inputDims[0] || signalDims[1] != inputDims[1]) {
            throw new IllegalArgumentException("Signal and input matrices must have identical dimensions.");
        }

        double signalPseudocount = signalScaleRatio == null
                ? 1_000_000.0 / signalLibrarySize
                : signalScaleRatio;
        double inputPseudocount = 1_000_000.0 / inputLibrarySize;
        double log2 = Math.log(2.0);

        for (int row = 0; row < signalDims[0]; row++) {
            for (int col = 0; col < signalDims[1]; col++) {
                double ratio = (signalMat.get(row, col) + signalPseudocount)
                        / (inputMat.get(row, col) + inputPseudocount);
                signalMat.set(row, col, Math.log(ratio) / log2);
            }
        }

        return signalMat;
    }
}
