package com.NGSViz.utils;

/**
 * Example demonstrating the use of SparseMatrix for memory optimization
 */
public class SparseMatrixExample {
    
    public static void main(String[] args) {
        // Example 1: Basic sparse matrix usage
        demonstrateBasicUsage();
        
        // Example 2: Memory comparison
        demonstrateMemoryComparison();
        
        // Example 3: Performance comparison
        demonstratePerformanceComparison();
    }
    
    /**
     * Demonstrate basic sparse matrix operations
     */
    private static void demonstrateBasicUsage() {
        System.out.println("=== Basic Sparse Matrix Usage ===");
        
        // Create a 1000x1000 sparse matrix
        SparseMatrix sparse = new SparseMatrix(1000, 1000);
        
        // Set some values (only 5% of the matrix will have non-zero values)
        for (int i = 0; i < 1000; i += 20) {
            for (int j = 0; j < 1000; j += 20) {
                sparse.set(i, j, Math.random() * 10.0);
            }
        }
        
        System.out.println("Sparse matrix created: " + sparse);
        System.out.println("Memory usage: " + sparse.getMemoryUsage() / 1024.0 / 1024.0 + " MB");
        System.out.println("Sparsity ratio: " + String.format("%.2f", sparse.getSparsityRatio()) + "%");
        
        // Convert to dense matrix
        double[][] dense = sparse.toDenseMatrix();
        System.out.println("Converted to dense matrix: " + dense.length + "x" + dense[0].length);
        System.out.println();
    }
    
    /**
     * Demonstrate memory usage comparison
     */
    private static void demonstrateMemoryComparison() {
        System.out.println("=== Memory Usage Comparison ===");
        
        int rows = 10000;
        int cols = 100;
        
        // Dense matrix memory usage
        long denseMemory = rows * cols * 8; // 8 bytes per double
        
        // Sparse matrix with different sparsity levels
        double[] sparsityLevels = {0.01, 0.05, 0.10, 0.20, 0.50}; // 1%, 5%, 10%, 20%, 50%
        
        System.out.printf("Matrix size: %dx%d\n", rows, cols);
        System.out.printf("Dense matrix memory: %.2f MB\n", denseMemory / 1024.0 / 1024.0);
        System.out.println();
        
        for (double sparsity : sparsityLevels) {
            int nonZeroElements = (int) (rows * cols * sparsity);
            long sparseMemory = nonZeroElements * 16; // Rough estimate for sparse storage
            
            double savings = (double) (denseMemory - sparseMemory) / denseMemory * 100;
            
            System.out.printf("Sparsity %.1f%%: %d non-zero elements, %.2f MB, %.1f%% savings\n",
                            sparsity * 100, nonZeroElements, 
                            sparseMemory / 1024.0 / 1024.0, savings);
        }
        System.out.println();
    }
    
    /**
     * Demonstrate performance comparison
     */
    private static void demonstratePerformanceComparison() {
        System.out.println("=== Performance Comparison ===");
        
        int size = 1000;
        
        // Create sparse matrix
        SparseMatrix sparse = new SparseMatrix(size, size);
        
        // Fill with some data
        for (int i = 0; i < size; i += 10) {
            for (int j = 0; j < size; j += 10) {
                sparse.set(i, j, Math.random());
            }
        }
        
        // Measure sparse matrix operations
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double value = sparse.get(i, j);
            }
        }
        long sparseTime = System.currentTimeMillis() - startTime;
        
        // Convert to dense and measure
        double[][] dense = sparse.toDenseMatrix();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double value = dense[i][j];
            }
        }
        long denseTime = System.currentTimeMillis() - startTime;
        
        System.out.printf("Sparse matrix access time: %d ms\n", sparseTime);
        System.out.printf("Dense matrix access time: %d ms\n", denseTime);
        System.out.printf("Performance ratio: %.2fx\n", (double) denseTime / sparseTime);
        System.out.println();
    }
    
    /**
     * Example of using sparse matrix in gene processing context
     */
    public static void demonstrateGeneProcessingExample() {
        System.out.println("=== Gene Processing Example ===");
        
        // Simulate gene coverage data
        int numGenes = 5000;
        int numDataPoints = 100;
        
        SparseMatrix geneCoverage = new SparseMatrix(numGenes, numDataPoints);
        
        // Simulate sparse gene coverage data (most genes have coverage only in specific regions)
        for (int gene = 0; gene < numGenes; gene++) {
            // Each gene has coverage in only 10-30% of data points
            int coveragePoints = 10 + (int) (Math.random() * 20);
            
            for (int point = 0; point < coveragePoints; point++) {
                int dataPoint = (int) (Math.random() * numDataPoints);
                double coverage = Math.random() * 100.0;
                geneCoverage.set(gene, dataPoint, coverage);
            }
        }
        
        System.out.println("Gene coverage matrix: " + geneCoverage);
        System.out.printf("Memory usage: %.2f MB\n", geneCoverage.getMemoryUsage() / 1024.0 / 1024.0);
        System.out.printf("Sparsity: %.2f%%\n", geneCoverage.getSparsityRatio());
        
        // Calculate memory savings
        long denseMemory = numGenes * numDataPoints * 8;
        long sparseMemory = geneCoverage.getMemoryUsage();
        double savings = (double) (denseMemory - sparseMemory) / denseMemory * 100;
        
        System.out.printf("Memory savings: %.1f%%\n", savings);
        System.out.println();
    }
} 