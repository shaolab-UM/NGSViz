package coverageCalculator;

import java.util.Arrays;

/**
 * @author Benchen Ye
 * @create 2024-10--20:38
 */
public class AverageCoverage {

    public static double[] calculateAverageCoverage(double[][] coverage_scaled_matrix, double trim_ratio) {
        int rows = coverage_scaled_matrix.length;
        int cols = coverage_scaled_matrix[0].length;
        double[] trimmed_means = new double[cols];

        for (int col = 0; col < cols; col++) {
            double[] column_data = new double[rows];
            for (int row = 0; row < rows; row++) {
                column_data[row] = coverage_scaled_matrix[row][col];
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
}
