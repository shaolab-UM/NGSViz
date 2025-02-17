package com.NGSVir.coverageCalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-10--11:22
 * @function trim has error
 */
public class SEMCalculator {
    // calculate quantile value for trim_ratio
    public static double quantile(double[] vector_list, double trim_ratio) {
        double percentile_value;
        Arrays.sort(vector_list);
        double index = trim_ratio * (vector_list.length - 1);
        int lower_index = (int) Math.floor(index);
        int upper_undex = (int) Math.ceil(index);
        if (lower_index == upper_undex) {
            percentile_value = vector_list[lower_index];
        } else {
            double lowerValue = vector_list[lower_index];
            double upperValue = vector_list[upper_undex];
            percentile_value = (lowerValue + upperValue) / 2;
        }
        System.out.println("Percentile value: " + percentile_value);
        return percentile_value;
    }
    // trim the low and high value of the data according to the trim_ratio
    public static List<Double> trim(double[] vector_list, double trim_ratio) {
        double low = quantile(vector_list, trim_ratio);
        double high = quantile(vector_list, 1-trim_ratio);
        return filterData(vector_list, low, high);
    }

    // filter the value of [] data lower than low value and greater than high value
    public static List<Double> filterData(double[] array, double low, double high) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            double value = array[i];
            if (value > low && value < high) {
                result.add(value);
            }
        }
        return result;
    }

    // list<double> to double[]
    public static double[] list2matrix(List<Double> list_data) {
        int item_length = list_data.size();
        double[] res_matrix = new double[item_length];
        for (int i = 0; i < item_length; i++) {
            res_matrix[i] = list_data.get(i);
        }
        return res_matrix;
    }

    // calculate the sem
    public static double calculateSEM(double[] vector_list, double trim_ratio) {
        if (trim_ratio > 0){
            List<Double> trim_res = trim(vector_list, trim_ratio);
            vector_list = list2matrix(trim_res);
        }
        // count the mean
        double sum = 0.0;
        for (double num : vector_list) {
            sum += num;
        }
        double mean = sum / vector_list.length;
        // count the variance
        double varianceSum = 0.0;
        for (double num : vector_list) {
            varianceSum += Math.pow(num - mean, 2);
        }
        double variance = varianceSum / (vector_list.length-1);
        // count the sd
        double standardDeviation = Math.sqrt(variance);
        // standardDeviation
        double sem = standardDeviation / Math.sqrt(vector_list.length);
        if (Double.isNaN(sem)) {
            sem = 0.0;
        }
        System.out.println("Sem: " + sem);
        return sem;
    }
    // get each column from a matrix
    public static double[] getColumnFromMatrix(double[][] matrix, int col_index) {
        // get the row number of matrix for matrix.length
        int num_rows = matrix.length;
        double[] column_list = new double[matrix.length];
        for (int row = 0; row < num_rows; row++) {
            column_list[row] = matrix[row][col_index];
        }
        return column_list;
    }
    // calculate the SEM value for each column from a matrix
    public static double[] calculateSEM2MatrixColumn(double[][] matrix, double trim_ratio) {
        int num_col = matrix[0].length;
        double[] sem_list = new double[num_col];
        for (int col = 0; col < num_col; col++) {
            double[] matrix_column_list = getColumnFromMatrix(matrix, col);
            double sem_value = calculateSEM(matrix_column_list, trim_ratio);
            sem_list[col] = sem_value;
        }
        return sem_list;
    }
}
