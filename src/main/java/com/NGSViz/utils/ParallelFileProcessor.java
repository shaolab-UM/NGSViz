package com.NGSViz.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Parallel file processor for optimized I/O operations
 * Provides efficient file reading, writing, and processing capabilities
 */
public class ParallelFileProcessor {
    
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    
    /**
     * Parallel file reader with chunked processing
     */
    public static class ParallelFileReader {
        
        /**
         * Read large file in parallel chunks
         */
        public static List<String> readFileParallel(String filePath, int chunkSize) throws IOException {
            List<String> lines = new ArrayList<>();
            Path path = Paths.get(filePath);
            
            // Read all lines first
            List<String> allLines = Files.readAllLines(path);
            
            // Process in parallel chunks
            ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
            List<Future<List<String>>> futures = new ArrayList<>();
            
            for (int i = 0; i < allLines.size(); i += chunkSize) {
                int end = Math.min(i + chunkSize, allLines.size());
                List<String> chunk = allLines.subList(i, end);
                
                Future<List<String>> future = executor.submit(() -> processChunk(chunk));
                futures.add(future);
            }
            
            // Collect results
            for (Future<List<String>> future : futures) {
                try {
                    lines.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new IOException("Error processing file chunks", e);
                }
            }
            
            executor.shutdown();
            return lines;
        }
        
        /**
         * Process a chunk of lines
         */
        private static List<String> processChunk(List<String> chunk) {
            return chunk.stream()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.toList());
        }
        
        /**
         * Read file with buffered reader for better performance
         */
        public static List<String> readFileBuffered(String filePath) throws IOException {
            List<String> lines = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(
                    new FileReader(filePath), DEFAULT_BUFFER_SIZE)) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            
            return lines;
        }
    }
    
    /**
     * Parallel file writer with batch processing
     */
    public static class ParallelFileWriter {
        
        /**
         * Write large dataset to file in parallel batches
         */
        public static void writeMatrixParallel(String filePath, double[][] matrix, 
                                             List<String> rowNames, int batchSize) throws IOException {
            
            ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
            List<Future<Void>> futures = new ArrayList<>();
            
            // Write header first
            writeHeader(filePath, matrix[0].length);
            
            // Process matrix in batches
            for (int i = 0; i < matrix.length; i += batchSize) {
                int end = Math.min(i + batchSize, matrix.length);
                int startIndex = i;
                
                Future<Void> future = executor.submit(() -> {
                    writeMatrixBatch(filePath, matrix, rowNames, startIndex, end);
                    return null;
                });
                futures.add(future);
            }
            
            // Wait for completion
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IOException("Error writing matrix batches", e);
                }
            }
            
            executor.shutdown();
        }
        
        /**
         * Write header to file
         */
        private static void writeHeader(String filePath, int colCount) throws IOException {
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(filePath), DEFAULT_BUFFER_SIZE)) {
                
                StringBuilder header = new StringBuilder(",");
                for (int j = 0; j < colCount; j++) {
                    header.append("X").append(j);
                    if (j < colCount - 1) {
                        header.append(",");
                    }
                }
                header.append('\n');
                writer.write(header.toString());
            }
        }
        
        /**
         * Write a batch of matrix rows
         */
        private static void writeMatrixBatch(String filePath, double[][] matrix, 
                                           List<String> rowNames, int start, int end) throws IOException {
            
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(filePath, true), DEFAULT_BUFFER_SIZE)) {
                
                StringBuilder buffer = new StringBuilder(DEFAULT_BUFFER_SIZE);
                
                for (int i = start; i < end; i++) {
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
            }
        }
    }
    
    /**
     * File I/O performance monitor
     */
    public static class FileIOMonitor {
        
        /**
         * Monitor file reading performance
         */
        public static void monitorReadPerformance(String filePath, Runnable readTask) {
            PerformanceMonitor.PerformanceTimer timer = 
                new PerformanceMonitor.PerformanceTimer("File Read: " + filePath);
            
            readTask.run();
            
            timer.stop();
            
            // Get file size for throughput calculation
            try {
                long fileSize = Files.size(Paths.get(filePath));
                long duration = timer.getElapsedTime();
                double throughput = fileSize / 1024.0 / 1024.0 / (duration / 1000.0); // MB/s
                
                System.out.printf("File read throughput: %.2f MB/s\n", throughput);
            } catch (IOException e) {
                System.err.println("Could not calculate file size: " + e.getMessage());
            }
        }
        
        /**
         * Monitor file writing performance
         */
        public static void monitorWritePerformance(String filePath, Runnable writeTask) {
            PerformanceMonitor.PerformanceTimer timer = 
                new PerformanceMonitor.PerformanceTimer("File Write: " + filePath);
            
            writeTask.run();
            
            timer.stop();
        }
    }
    
    /**
     * Batch file processor for multiple files
     */
    public static class BatchFileProcessor {
        
        /**
         * Process multiple files in parallel
         */
        public static <T> List<T> processFilesParallel(List<String> filePaths, 
                                                      java.util.function.Function<String, T> processor) {
            
            ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
            List<Future<T>> futures = new ArrayList<>();
            
            for (String filePath : filePaths) {
                Future<T> future = executor.submit(() -> processor.apply(filePath));
                futures.add(future);
            }
            
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                try {
                    results.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error processing file: " + e.getMessage());
                }
            }
            
            executor.shutdown();
            return results;
        }
        
        /**
         * Copy multiple files in parallel
         */
        public static void copyFilesParallel(List<String> sourcePaths, List<String> targetPaths) {
            if (sourcePaths.size() != targetPaths.size()) {
                throw new IllegalArgumentException("Source and target path lists must have same size");
            }
            
            ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
            List<Future<Void>> futures = new ArrayList<>();
            
            for (int i = 0; i < sourcePaths.size(); i++) {
                String source = sourcePaths.get(i);
                String target = targetPaths.get(i);
                
                Future<Void> future = executor.submit(() -> {
                    try {
                        Files.copy(Paths.get(source), Paths.get(target), 
                                 StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println("Error copying " + source + " to " + target + ": " + e.getMessage());
                    }
                    return null;
                });
                futures.add(future);
            }
            
            // Wait for completion
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error in file copy operation: " + e.getMessage());
                }
            }
            
            executor.shutdown();
        }
    }
} 