package com.NGSViz.coverageCalculator;

import com.NGSViz.utils.SparseMatrix;

import java.util.Arrays;

/**
 * @author Benchen Ye
 * @create 2024-10--20:38
 */
public class AverageCoverage {

    public static double[] calculateAverageCoverage(SparseMatrix coverage_scaled_matrix, double trim_ratio) {
        int[] dims = coverage_scaled_matrix.getDimensions(); // [rows, cols]
        int rows = dims[0];
        int cols = dims[1];
        double[] trimmed_means = new double[cols];

        for (int col = 0; col < cols; col++) {
            double[] column_data = new double[rows];
            for (int row = 0; row < rows; row++) {
                column_data[row] = coverage_scaled_matrix.get(row, col);
            }
            trimmed_means[col] = trimmedMean(column_data, trim_ratio);
        }

        return trimmed_means;
    }

    public static double trimmedMean(double[] data, double trim_ratio) {
        Arrays.sort(data);
        int trim_count = (int) (data.length * trim_ratio);
        double sum = 0.0;
        for (int i = trim_count; i < data.length - trim_count; i++) {
            sum += data[i];
        }
        return sum / (data.length - 2 * trim_count);
    }
    public static double[] calculateSEM(SparseMatrix coverage_scaled_matrix) {
        int[] dims = coverage_scaled_matrix.getDimensions(); // [rows, cols]
        int rows = dims[0];
        int cols = dims[1];
        double[] sems = new double[cols];
        for (int col = 0; col < cols; col++) {
            double sum = 0.0;
            double sumSq = 0.0;
            for (int row = 0; row < rows; row++) {
                double value = coverage_scaled_matrix.get(row, col);
                sum += value;
                sumSq += value * value;
            }
            double mean = sum / rows;
            double variance = (sumSq / rows) - (mean * mean);
            double stdDev = Math.sqrt(variance);
            sems[col] = stdDev / Math.sqrt(rows);
        }
        return sems;
    }

}
