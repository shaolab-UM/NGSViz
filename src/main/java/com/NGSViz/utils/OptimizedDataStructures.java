package com.NGSViz.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Optimized data structures for better performance
 * Provides efficient alternatives to standard collections
 */
public class OptimizedDataStructures {
    
    /**
     * Optimized coverage array calculator
     * Uses primitive arrays instead of ArrayList for better performance
     */
    public static class OptimizedCoverageCalculator {
        
        /**
         * Calculate coverage using primitive array
         * Much faster than ArrayList<Integer>
         */
        public static int[] calculateCoverage(int[] coverage, int alignStartPos, int readWidth) {
            if (alignStartPos < 0 || alignStartPos + readWidth > coverage.length) {
                throw new IllegalArgumentException("Invalid range: alignStartPos=" + alignStartPos +
                        ", readWidth=" + readWidth + ", array length=" + coverage.length);
            }
            
            // Direct array access - no boxing/unboxing overhead
            for (int i = alignStartPos; i < alignStartPos + readWidth; i++) {
                coverage[i]++;
            }
            return coverage;
        }
        
        /**
         * Initialize coverage array with zeros
         */
        public static int[] createCoverageArray(int length) {
            return new int[length]; // Automatically initialized to zeros
        }
        
        /**
         * Convert ArrayList<Integer> to int[] for better performance
         */
        public static int[] toPrimitiveArray(List<Integer> list) {
            int[] array = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            return array;
        }
        
        /**
         * Convert int[] to List<Integer> when needed
         */
        public static List<Integer> toList(int[] array) {
            List<Integer> list = new ArrayList<>(array.length);
            for (int value : array) {
                list.add(value);
            }
            return list;
        }
    }
    
    /**
     * Optimized CSV writer with buffering
     */
    public static class OptimizedCSVWriter {
        private static final int DEFAULT_BUFFER_SIZE = 8192;
        
        /**
         * Write matrix to CSV with optimized buffering
         */
        public static void writeMatrixOptimized(String filePath, double[][] matrix, List<String> rowNames) {
            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                    new java.io.FileWriter(filePath), DEFAULT_BUFFER_SIZE)) {
                
                StringBuilder buffer = new StringBuilder(DEFAULT_BUFFER_SIZE);
                
                // Write header
                buffer.append(",");
                int colNum = matrix.length > 0 ? matrix[0].length : 0;
                for (int j = 0; j < colNum; j++) {
                    buffer.append("X").append(j);
                    if (j < colNum - 1) {
                        buffer.append(",");
                    }
                }
                buffer.append('\n');
                writer.write(buffer.toString());
                
                // Write data rows
                for (int i = 0; i < matrix.length; i++) {
                    buffer.setLength(0);
                    
                    if (rowNames != null) {
                        buffer.append(rowNames.get(i)).append(",");
                    }
                    
                    for (int j = 0; j < matrix[i].length; j++) {
                        buffer.append(matrix[i][j]);
                        if (j < matrix[i].length - 1) {
                            buffer.append(",");
                        }
                    }
                    buffer.append('\n');
                    
                    writer.write(buffer.toString());
                }
                
                System.out.println("Matrix saved to " + filePath);
                
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        
        /**
         * Write array to CSV with optimized buffering
         */
        public static void writeArrayOptimized(String filePath, double[] array, String header) {
            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                    new java.io.FileWriter(filePath), DEFAULT_BUFFER_SIZE)) {
                
                StringBuilder buffer = new StringBuilder(DEFAULT_BUFFER_SIZE);
                
                // Write header
                buffer.append(header).append('\n');
                writer.write(buffer.toString());
                
                // Write data
                for (double value : array) {
                    buffer.setLength(0);
                    buffer.append(value).append('\n');
                    writer.write(buffer.toString());
                }
                
                System.out.println("Array saved to " + filePath);
                
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Optimized data scaler with pre-computed boundaries
     */
    public static class OptimizedDataScaler {
        
        /**
         * Optimized binning with pre-computed boundaries
         */
        public static double[] binOptimized(int[] coverage, int numPoints) {
            int totalSize = coverage.length;
            int chunkSize = totalSize / numPoints;
            
            // Pre-compute boundaries
            int[] boundaries = new int[numPoints + 1];
            for (int i = 0; i <= numPoints; i++) {
                boundaries[i] = i * chunkSize;
            }
            boundaries[numPoints] = totalSize; // Ensure last boundary is correct
            
            double[] result = new double[numPoints];
            
            for (int i = 0; i < numPoints; i++) {
                int start = boundaries[i];
                int end = boundaries[i + 1];
                
                double sum = 0;
                int count = end - start;
                
                for (int j = start; j < end; j++) {
                    sum += coverage[j];
                }
                
                result[i] = count > 0 ? sum / count : 0;
            }
            
            return result;
        }
        
        /**
         * Optimized binning for double arrays
         */
        public static double[] binOptimized(double[] coverage, int numPoints) {
            int totalSize = coverage.length;
            int chunkSize = totalSize / numPoints;
            
            // Pre-compute boundaries
            int[] boundaries = new int[numPoints + 1];
            for (int i = 0; i <= numPoints; i++) {
                boundaries[i] = i * chunkSize;
            }
            boundaries[numPoints] = totalSize;
            
            double[] result = new double[numPoints];
            
            for (int i = 0; i < numPoints; i++) {
                int start = boundaries[i];
                int end = boundaries[i + 1];
                
                double sum = 0;
                int count = end - start;
                
                for (int j = start; j < end; j++) {
                    sum += coverage[j];
                }
                
                result[i] = count > 0 ? sum / count : 0;
            }
            
            return result;
        }
    }
    
    /**
     * Thread-safe object pool for reducing GC pressure
     */
    public static class ObjectPool<T> {
        private final ThreadLocal<Queue<T>> pool;
        private final java.util.function.Supplier<T> factory;
        private final int maxPoolSize;
        
        public ObjectPool(java.util.function.Supplier<T> factory, int maxPoolSize) {
            this.factory = factory;
            this.maxPoolSize = maxPoolSize;
            this.pool = ThreadLocal.withInitial(LinkedList::new);
        }
        
        public T borrow() {
            Queue<T> threadPool = pool.get();
            T obj = threadPool.poll();
            return obj != null ? obj : factory.get();
        }
        
        public void returnObject(T obj) {
            Queue<T> threadPool = pool.get();
            if (threadPool.size() < maxPoolSize) {
                threadPool.offer(obj);
            }
        }
    }
    
    /**
     * Optimized concurrent collections
     */
    public static class OptimizedConcurrentCollections {
        
        /**
         * Create optimized concurrent set
         */
        public static <T> Set<T> createConcurrentSet() {
            return ConcurrentHashMap.newKeySet();
        }
        
        /**
         * Create optimized concurrent map
         */
        public static <K, V> Map<K, V> createConcurrentMap() {
            return new ConcurrentHashMap<>();
        }
        
        /**
         * Create optimized concurrent list
         */
        public static <T> List<T> createConcurrentList() {
            return Collections.synchronizedList(new ArrayList<>());
        }
    }
    
    /**
     * Memory-efficient string operations
     */
    public static class OptimizedStringOperations {
        
        /**
         * Efficient string concatenation for CSV
         */
        public static String joinCSV(double[] values) {
            if (values.length == 0) return "";
            
            StringBuilder sb = new StringBuilder(values.length * 8); // Estimate 8 chars per double
            sb.append(values[0]);
            
            for (int i = 1; i < values.length; i++) {
                sb.append(',').append(values[i]);
            }
            
            return sb.toString();
        }
        
        /**
         * Efficient string concatenation with custom separator
         */
        public static String join(String separator, Object... values) {
            if (values.length == 0) return "";
            
            StringBuilder sb = new StringBuilder(values.length * 16); // Estimate 16 chars per object
            sb.append(values[0]);
            
            for (int i = 1; i < values.length; i++) {
                sb.append(separator).append(values[i]);
            }
            
            return sb.toString();
        }
    }
} 