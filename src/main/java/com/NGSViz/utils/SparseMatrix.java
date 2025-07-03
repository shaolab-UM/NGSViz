package com.NGSViz.utils;

import java.util.*;

/**
 * Sparse matrix implementation for memory optimization
 * Stores only non-zero values to reduce memory usage for large datasets
 */
public class SparseMatrix {
    private final int rows;
    private final int cols;
    private final Map<String, Double> data;
    private final double defaultValue;
    
    /**
     * Constructor for sparse matrix
     * @param rows number of rows
     * @param cols number of columns
     * @param defaultValue default value for empty cells (usually 0.0)
     */
    public SparseMatrix(int rows, int cols, double defaultValue) {
        this.rows = rows;
        this.cols = cols;
        this.defaultValue = defaultValue;
        this.data = new HashMap<>();
    }
    
    /**
     * Constructor with default value 0.0
     */
    public SparseMatrix(int rows, int cols) {
        this(rows, cols, 0.0);
    }
    
    /**
     * Set value at specific position
     * @param row row index
     * @param col column index
     * @param value value to set
     */
    public void set(int row, int col, double value) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Index out of bounds: [" + row + ", " + col + "]");
        }
        
        String key = row + "," + col;
        if (Math.abs(value - defaultValue) < 1e-10) {
            // Remove entry if value equals default (to save memory)
            data.remove(key);
        } else {
            data.put(key, value);
        }
    }
    
    /**
     * Get value at specific position
     * @param row row index
     * @param col column index
     * @return value at position
     */
    public double get(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Index out of bounds: [" + row + ", " + col + "]");
        }
        
        String key = row + "," + col;
        return data.getOrDefault(key, defaultValue);
    }
    
    /**
     * Get entire row as array
     * @param row row index
     * @return row as double array
     */
    public double[] getRow(int row) {
        if (row < 0 || row >= rows) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + row);
        }
        
        double[] rowData = new double[cols];
        for (int col = 0; col < cols; col++) {
            rowData[col] = get(row, col);
        }
        return rowData;
    }
    
    /**
     * Set entire row from array
     * @param row row index
     * @param rowData row data as double array
     */
    public void setRow(int row, double[] rowData) {
        if (row < 0 || row >= rows) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + row);
        }
        if (rowData.length != cols) {
            throw new IllegalArgumentException("Row data length must be " + cols);
        }
        
        for (int col = 0; col < cols; col++) {
            set(row, col, rowData[col]);
        }
    }
    
    /**
     * Convert sparse matrix to dense 2D array
     * @return dense matrix as double[][]
     */
    public double[][] toDenseMatrix() {
        double[][] dense = new double[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                dense[row][col] = get(row, col);
            }
        }
        return dense;
    }
    
    /**
     * Create sparse matrix from dense 2D array
     * @param denseMatrix dense matrix
     * @param defaultValue default value for sparse matrix
     * @return sparse matrix
     */
    public static SparseMatrix fromDenseMatrix(double[][] denseMatrix, double defaultValue) {
        if (denseMatrix == null || denseMatrix.length == 0) {
            throw new IllegalArgumentException("Dense matrix cannot be null or empty");
        }
        
        int rows = denseMatrix.length;
        int cols = denseMatrix[0].length;
        SparseMatrix sparse = new SparseMatrix(rows, cols, defaultValue);
        
        for (int row = 0; row < rows; row++) {
            if (denseMatrix[row].length != cols) {
                throw new IllegalArgumentException("Inconsistent column count in row " + row);
            }
            for (int col = 0; col < cols; col++) {
                sparse.set(row, col, denseMatrix[row][col]);
            }
        }
        
        return sparse;
    }
    
    /**
     * Create sparse matrix from dense 2D array with default value 0.0
     */
    public static SparseMatrix fromDenseMatrix(double[][] denseMatrix) {
        return fromDenseMatrix(denseMatrix, 0.0);
    }
    
    /**
     * Get memory usage estimation
     * @return estimated memory usage in bytes
     */
    public long getMemoryUsage() {
        // Estimate memory usage: HashMap overhead + key-value pairs
        long keyValueSize = data.size() * (8 + 8); // String key + Double value
        long hashMapOverhead = 16 + data.size() * 8; // HashMap internal structure
        return keyValueSize + hashMapOverhead;
    }
    
    /**
     * Get sparsity ratio (percentage of non-zero elements)
     * @return sparsity ratio as percentage
     */
    public double getSparsityRatio() {
        int totalElements = rows * cols;
        int nonZeroElements = data.size();
        return (double) nonZeroElements / totalElements * 100.0;
    }
    
    /**
     * Get number of non-zero elements
     * @return count of non-zero elements
     */
    public int getNonZeroCount() {
        return data.size();
    }
    
    /**
     * Get total number of elements
     * @return total element count
     */
    public int getTotalElements() {
        return rows * cols;
    }
    
    /**
     * Get matrix dimensions
     * @return array with [rows, cols]
     */
    public int[] getDimensions() {
        return new int[]{rows, cols};
    }
    
    /**
     * Get number of rows
     */
    public int getRows() {
        return rows;
    }
    
    /**
     * Get number of columns
     */
    public int getColumns() {
        return cols;
    }
    
    /**
     * Get default value
     */
    public double getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * Clear all data
     */
    public void clear() {
        data.clear();
    }
    
    /**
     * Check if matrix is empty (all values are default)
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("SparseMatrix[%dx%d, %d non-zero elements, %.2f%% sparsity]", 
                           rows, cols, data.size(), getSparsityRatio());
    }
} 