package com.NGSViz.utils;

/**
 * @author Benchen Ye
 * @create 2024-12--16:27
 * @function calculate background
 */
public class CalculateLog2Matrix {
    public static double[][] calculateLog2Matrix(double[][] coverage_scaled_matrix,
                                                 double[][] bkg_coverage_scaled_matrix,
                                                 double result_pseudo_rpm,
                                                 double bkg_pseudo_rpm)
    {
        int rows = coverage_scaled_matrix.length;
        int cols = coverage_scaled_matrix[0].length;
        double[][] result_matrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double numerator = coverage_scaled_matrix[i][j] + result_pseudo_rpm;
                double denominator = bkg_coverage_scaled_matrix[i][j] + bkg_pseudo_rpm;
                result_matrix[i][j] = Math.log(numerator / denominator) / Math.log(2);
            }
        }
        return result_matrix;
    }
}
