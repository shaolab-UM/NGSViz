package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-10--20:55
 * @function save the matrix data into the csv file with/without rownames
 */
public class Matrix2CSV {
    // test
    public static void main(String[] args) {
        double[][] matrix = {
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0},
                {7.0, 8.0, 9.0}
        };

        List<String> rowNames = Arrays.asList("Row1", "Row2", "Row3");
        String filePath = "/Users/bencheye/myProj/ngsPlot/Output/matrix_with_row_names.csv";
        saveMatrix2CSV(matrix, rowNames, filePath);
    }
    // save the coverage marix data with the rownames (heatmap)
    public static void saveMatrix2CSV(double[][] matrix_data, List<String> row_names, String file_path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path))) {
            for (int i = 0; i < matrix_data.length; i++) {
                // write the row name
                writer.write(row_names.get(i) + ",");
                for (int j = 0; j < matrix_data[i].length; j++) {
                    writer.write(Double.toString(matrix_data[i][j]));
                    if (j < matrix_data[i].length - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }
            System.out.println("Coverage matrix with row names saved to " + file_path);
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
