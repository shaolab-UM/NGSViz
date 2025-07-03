package com.NGSViz.utils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Performance benchmark tool for testing optimization strategies
 * Provides comprehensive benchmarking for different processing methods
 */
public class PerformanceBenchmark {
    
    private static final int WARMUP_ITERATIONS = 3;
    private static final int BENCHMARK_ITERATIONS = 5;
    
    /**
     * Benchmark result class
     */
    public static class BenchmarkResult {
        public final String methodName;
        public final long totalTime;
        public final long averageTime;
        public final long minTime;
        public final long maxTime;
        public final long memoryUsage;
        public final int iterations;
        
        public BenchmarkResult(String methodName, List<Long> times, long memoryUsage) {
            this.methodName = methodName;
            this.iterations = times.size();
            this.totalTime = times.stream().mapToLong(Long::longValue).sum();
            this.averageTime = totalTime / iterations;
            this.minTime = times.stream().mapToLong(Long::longValue).min().orElse(0);
            this.maxTime = times.stream().mapToLong(Long::longValue).max().orElse(0);
            this.memoryUsage = memoryUsage;
        }
        
        @Override
        public String toString() {
            return String.format("%s: avg=%.2fms, min=%.2fms, max=%.2fms, memory=%.2fMB", 
                               methodName, averageTime, minTime, maxTime, memoryUsage / 1024.0 / 1024.0);
        }
    }
    
    /**
     * Benchmark a single method
     */
    public static BenchmarkResult benchmark(String methodName, Runnable task) {
        List<Long> times = new ArrayList<>();
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            task.run();
        }
        
        // Benchmark
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long startTime = System.currentTimeMillis();
            task.run();
            long endTime = System.currentTimeMillis();
            times.add(endTime - startTime);
        }
        
        // Measure memory usage
        long memoryUsage = PerformanceMonitor.getMemoryInfo().heapUsed;
        
        return new BenchmarkResult(methodName, times, memoryUsage);
    }
    
    /**
     * Compare multiple methods
     */
    public static void compareMethods(Map<String, Runnable> methods) {
        System.out.println("=== Performance Benchmark Comparison ===");
        System.out.println("Warmup iterations: " + WARMUP_ITERATIONS);
        System.out.println("Benchmark iterations: " + BENCHMARK_ITERATIONS);
        System.out.println();
        
        List<BenchmarkResult> results = new ArrayList<>();
        
        for (Map.Entry<String, Runnable> entry : methods.entrySet()) {
            System.out.println("Benchmarking: " + entry.getKey());
            BenchmarkResult result = benchmark(entry.getKey(), entry.getValue());
            results.add(result);
            System.out.println("Result: " + result);
            System.out.println();
        }
        
        // Print comparison
        printComparison(results);
    }
    
    /**
     * Print comparison table
     */
    private static void printComparison(List<BenchmarkResult> results) {
        System.out.println("=== Performance Comparison Summary ===");
        System.out.printf("%-20s %-12s %-12s %-12s %-12s\n", 
                         "Method", "Avg (ms)", "Min (ms)", "Max (ms)", "Memory (MB)");
        System.out.println("------------------------------------------------------------");
        
        for (BenchmarkResult result : results) {
            System.out.printf("%-20s %-12.2f %-12.2f %-12.2f %-12.2f\n",
                            result.methodName, result.averageTime, result.minTime, 
                            result.maxTime, result.memoryUsage / 1024.0 / 1024.0);
        }
        
        // Find fastest method
        BenchmarkResult fastest = results.stream()
                .min(Comparator.comparingLong(r -> r.averageTime))
                .orElse(null);
        
        if (fastest != null) {
            System.out.println();
            System.out.println("Fastest method: " + fastest.methodName);
            
            // Calculate speedup
            for (BenchmarkResult result : results) {
                if (!result.methodName.equals(fastest.methodName)) {
                    double speedup = (double) result.averageTime / fastest.averageTime;
                    System.out.printf("Speedup vs %s: %.2fx\n", result.methodName, speedup);
                }
            }
        }
        
        System.out.println("=========================================");
    }
    
    /**
     * Memory usage benchmark
     */
    public static void benchmarkMemoryUsage(String description, Runnable task) {
        System.out.println("=== Memory Usage Benchmark: " + description + " ===");
        
        // Get initial memory
        PerformanceMonitor.MemoryInfo initialMemory = PerformanceMonitor.getMemoryInfo();
        System.out.printf("Initial memory: %.2f MB\n", initialMemory.heapUsed / 1024.0 / 1024.0);
        
        // Run task
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();
        
        // Get final memory
        PerformanceMonitor.MemoryInfo finalMemory = PerformanceMonitor.getMemoryInfo();
        System.out.printf("Final memory: %.2f MB\n", finalMemory.heapUsed / 1024.0 / 1024.0);
        System.out.printf("Memory increase: %.2f MB\n", 
                         (finalMemory.heapUsed - initialMemory.heapUsed) / 1024.0 / 1024.0);
        System.out.printf("Execution time: %d ms\n", endTime - startTime);
        System.out.println("=========================================");
    }
    
    /**
     * Thread pool performance benchmark
     */
    public static void benchmarkThreadPool(int[] threadCounts, Runnable task) {
        System.out.println("=== Thread Pool Performance Benchmark ===");
        
        for (int threadCount : threadCounts) {
            System.out.println("Testing with " + threadCount + " threads:");
            
            BenchmarkResult result = benchmark("ThreadPool-" + threadCount, () -> {
                // Create thread pool with specified count
                java.util.concurrent.ExecutorService executor = 
                    java.util.concurrent.Executors.newFixedThreadPool(threadCount);
                
                try {
                    // Submit task multiple times
                    List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        futures.add(executor.submit(task));
                    }
                    
                    // Wait for completion
                    for (java.util.concurrent.Future<?> future : futures) {
                        future.get();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    executor.shutdown();
                    try {
                        executor.awaitTermination(1, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            
            System.out.println("Result: " + result);
            System.out.println();
        }
    }
    
    /**
     * Example usage
     */
    public static void main(String[] args) {
        // Example benchmark
        Map<String, Runnable> methods = new HashMap<>();
        
        methods.put("Method A", () -> {
            // Simulate some work
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        methods.put("Method B", () -> {
            // Simulate different work
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        compareMethods(methods);
        
        // Memory benchmark example
        benchmarkMemoryUsage("Large Array Creation", () -> {
            double[][] largeArray = new double[1000][1000];
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < 1000; j++) {
                    largeArray[i][j] = Math.random();
                }
            }
        });
    }
} 