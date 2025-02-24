package com.NGSViz.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-10--20:55
 * @function save the matrix data into the csv file with/without rownames
 */
public class Matrix2CSV {

    public static int getNumColumns(double[][] matrix) {
        if (matrix.length > 0) {
            return matrix[0].length;
        } else {
            return 0;
        }
    }

    public static void saveMatrix2CSV(String file_path, double[][] matrix_data, List<String> row_names) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path))) {
            // Write column names
                writer.write(",");
                int col_num = getNumColumns(matrix_data);
                for (int j = 0; j < col_num; j++) {
                    writer.write("X" + j);
                    if (j < col_num - 1) {
                    writer.write(",");
                    }
                }
                writer.newLine();
            // Write matrix data with row names
            for (int i = 0; i < matrix_data.length; i++) {
                if (row_names != null) {
                    writer.write(row_names.get(i) + ",");
                }
                for (int j = 0; j < matrix_data[i].length; j++) {
                    writer.write(Double.toString(matrix_data[i][j]));
                    if (j < matrix_data[i].length - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }
            System.out.println("Coverage matrix with row and column names saved to " + file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveMatrix2CSV(String file_path, double[] matrix_data, String col_names) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path))) {
            // Write column names
            writer.write(col_names);
            writer.newLine();
            // Write matrix data with row names
            for (double value : matrix_data) {
                writer.write(Double.toString(value));
                writer.newLine();
            }

            System.out.println("Coverage matrix with row and column names saved to " + file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // save the average coverage data without the rownames
    public static void saveMatrix2CSV(double[] matrix_data, String file_path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path))) {
            for (double value : matrix_data) {
                writer.write(Double.toString(value));
                writer.newLine();
            }
            System.out.println("Average coverage data saved to " + file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
