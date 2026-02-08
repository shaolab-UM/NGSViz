package com.NGSViz.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Benchen Ye
 * @create 2024-10--20:55
 * @function save the matrix data into the csv file with/without rownames
 */
public class Matrix2CSV {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int STRING_BUILDER_INITIAL_CAPACITY = 4096;

    // OPTIMIZED: Save array to CSV with buffered StringBuilder
    public static void saveMatrix2CSVOptimized(String file_path, double[] matrix_data, String col_names) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path), DEFAULT_BUFFER_SIZE)) {
            StringBuilder buffer = new StringBuilder(STRING_BUILDER_INITIAL_CAPACITY);
            
            // Write column names
            buffer.append(col_names).append('\n');
            writer.write(buffer.toString());
            
            // Write matrix data with row names
            for (double value : matrix_data) {
                buffer.setLength(0); // Reset buffer for reuse
                buffer.append(value).append('\n');
                writer.write(buffer.toString());
            }

            System.out.println("Coverage matrix with row and column names saved to " + file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveMatrix2CSVOptimized(String file_path, SparseMatrix matrix_data, List<String> row_names) {
        int[] dims = matrix_data.getDimensions(); // [rows, cols]
        int row_num = dims[0];
        int col_num = dims[1];
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path), DEFAULT_BUFFER_SIZE)) {
            StringBuilder buffer = new StringBuilder(STRING_BUILDER_INITIAL_CAPACITY);

            // Write column names
            buffer.append(",");
            //int col_num = getNumColumns(matrix_data);
            for (int j = 0; j < col_num; j++) {
                buffer.append("X").append(j);
                if (j < col_num - 1) {
                    buffer.append(",");
                }
            }
            buffer.append('\n');
            writer.write(buffer.toString());

            // Write matrix data with row names
            for (int i = 0; i < row_num; i++) {
                buffer.setLength(0); // Reset buffer for reuse

                if (row_names != null) {
                    buffer.append(row_names.get(i)).append(",");
                }

                for (int j = 0; j < col_num; j++) {
                    buffer.append(matrix_data.get(i, j));
                    if (j < col_num - 1) {
                        buffer.append(",");
                    }
                }
                buffer.append('\n');

                writer.write(buffer.toString());
            }
            System.out.println("Coverage matrix with row and column names saved to " + file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int getNumColumns(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            return 0;
        }
        return matrix[0].length;
    }
    public static void saveMatrix2CSVOptimized(String file_path, double[][] matrix_data, List<String> row_names) {
        //int[] dims = matrix_data.getDimensions(); // [rows, cols]
        //int row_num = matrix_data.length;
        //int col_num = (row_num > 0) ? matrix_data[0].length : 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path), DEFAULT_BUFFER_SIZE)) {
            StringBuilder buffer = new StringBuilder(STRING_BUILDER_INITIAL_CAPACITY);

            // Write column names
            buffer.append(",");
            int col_num = getNumColumns(matrix_data);
            //int col_num = getNumColumns(matrix_data);
            for (int j = 0; j < col_num; j++) {
                buffer.append("X").append(j);
                if (j < col_num - 1) {
                    buffer.append(",");
                }
            }
            buffer.append('\n');
            writer.write(buffer.toString());

            // Write matrix data with row names
            for (int i = 0; i < matrix_data.length; i++) {
                buffer.setLength(0); // Reset buffer for reuse

                if (row_names != null) {
                    buffer.append(row_names.get(i)).append(",");
                }

                for (int j = 0; j < matrix_data[i].length; j++) {
                    buffer.append(matrix_data[i][j]);
                    if (j < matrix_data[i].length - 1) {
                        buffer.append(",");
                    }
                }
                buffer.append('\n');

                writer.write(buffer.toString());
            }
            System.out.println("Coverage matrix with row and column names saved to " + file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Legacy methods for backward compatibility
    public static void saveMatrix2CSV(String file_path, SparseMatrix matrix_data, List<String> row_names) {
        saveMatrix2CSVOptimized(file_path, matrix_data, row_names);
    }
    public static void saveMatrix2CSV(String file_path, double[][] matrix_data, List<String> row_names) {
        saveMatrix2CSVOptimized(file_path, matrix_data, row_names);
    }

    public static void saveMatrix2CSV(String file_path, double[] matrix_data, String col_names) {
        saveMatrix2CSVOptimized(file_path, matrix_data, col_names);
    }

    // [CHANGED] Export gene-level read counts as a two-column matrix.
    public static void saveGeneReadCount2CSV(String file_path, List<String> gene_names, int[] read_counts) {
        if (gene_names == null || read_counts == null) {
            throw new IllegalArgumentException("gene_names and read_counts cannot be null");
        }
        if (gene_names.size() != read_counts.length) {
            throw new IllegalArgumentException("gene_names and read_counts must have the same length");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path), DEFAULT_BUFFER_SIZE)) {
            StringBuilder buffer = new StringBuilder(STRING_BUILDER_INITIAL_CAPACITY);
            buffer.append("gene_name,read_count").append('\n');
            writer.write(buffer.toString());

            for (int i = 0; i < read_counts.length; i++) {
                buffer.setLength(0);
                String geneName = gene_names.get(i);
                if (geneName == null) {
                    geneName = "";
                }
                buffer.append(geneName)
                        .append(',')
                        .append(read_counts[i])
                        .append('\n');
                writer.write(buffer.toString());
            }
            System.out.println("Gene read-count matrix saved to " + file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
