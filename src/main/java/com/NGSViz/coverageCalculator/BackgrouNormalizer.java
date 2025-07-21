package com.NGSViz.coverageCalculator;

import com.NGSViz.utils.SparseMatrix;

public class BackgrouNormalizer {
    // Correct the input or IgG.
    public static SparseMatrix backgroundNormalize(SparseMatrix signal_mat, SparseMatrix input_mat) {
        int[] dims = signal_mat.getDimensions(); // [rows, cols]
        int row_num = dims[0];
        int col_num = dims[1];
        // Prevent the small coefficient of 0
        double epsilon = 1e-6;

        for (int i = 0; i < row_num; i++) {
            for (int j = 0; j < col_num; j++) {
                // log2(signal/input)
                double norm_value = Math.log((signal_mat.get(i, j) + epsilon) / (input_mat.get(i, j) + epsilon))/ Math.log(2);
                signal_mat.set(i, j, norm_value);
            }
        }
        return signal_mat;
    }
}

