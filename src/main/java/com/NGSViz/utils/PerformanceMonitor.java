package com.NGSViz.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Performance monitoring utility class
 * Used for monitoring memory usage, thread status and performance metrics
 */
public class PerformanceMonitor {
    
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    /**
     * Get current memory usage information
     */
    public static MemoryInfo getMemoryInfo() {
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
        long nonHeapMax = memoryBean.getNonHeapMemoryUsage().getMax();
        
        return new MemoryInfo(heapUsed, heapMax, nonHeapUsed, nonHeapMax);
    }


    
    /**
     * Memory information class
     */
    public static class MemoryInfo {
        public final long heapUsed;
        public final long heapMax;
        public final long nonHeapUsed;
        public final long nonHeapMax;
        
        public MemoryInfo(long heapUsed, long heapMax, long nonHeapUsed, long nonHeapMax) {
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.nonHeapUsed = nonHeapUsed;
            this.nonHeapMax = nonHeapMax;
        }
    }

    
    /**
     * Performance timer
     */
    public static class PerformanceTimer {
        private final long startTime;
        private final String operationName;
        
        public PerformanceTimer(String operationName) {
            this.operationName = operationName;
            this.startTime = System.currentTimeMillis();
        }
        
        public void stop() {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.printf("Operation '%s' took: %d ms\n", operationName, duration);
        }
        
        public long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }
    }
} 