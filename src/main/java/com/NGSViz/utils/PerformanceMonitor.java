package com.NGSViz.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 性能监控工具类
 * 用于监控内存使用、线程状态和性能指标
 */
public class PerformanceMonitor {
    
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    /**
     * 获取当前内存使用情况
     */
    public static MemoryInfo getMemoryInfo() {
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
        long nonHeapMax = memoryBean.getNonHeapMemoryUsage().getMax();
        
        return new MemoryInfo(heapUsed, heapMax, nonHeapUsed, nonHeapMax);
    }
    
    /**
     * 检查内存使用是否超过阈值
     */
    public static boolean isMemoryUsageHigh(double thresholdPercent) {
        MemoryInfo info = getMemoryInfo();
        double heapUsagePercent = (double) info.heapUsed / info.heapMax * 100;
        return heapUsagePercent > thresholdPercent;
    }
    
    /**
     * 获取线程池状态信息
     */
    public static ThreadPoolInfo getThreadPoolInfo(ThreadPoolExecutor executor) {
        return new ThreadPoolInfo(
            executor.getCorePoolSize(),
            executor.getMaximumPoolSize(),
            executor.getPoolSize(),
            executor.getActiveCount(),
            executor.getQueue().size(),
            executor.getCompletedTaskCount()
        );
    }
    
    /**
     * 打印性能监控信息
     */
    public static void printPerformanceInfo(ThreadPoolExecutor executor) {
        MemoryInfo memoryInfo = getMemoryInfo();
        ThreadPoolInfo threadInfo = getThreadPoolInfo(executor);
        
        System.out.println("=== 性能监控信息 ===");
        System.out.printf("堆内存使用: %.2f MB / %.2f MB (%.1f%%)\n", 
            memoryInfo.heapUsed / 1024.0 / 1024.0,
            memoryInfo.heapMax / 1024.0 / 1024.0,
            (double) memoryInfo.heapUsed / memoryInfo.heapMax * 100);
        
        System.out.printf("非堆内存使用: %.2f MB / %.2f MB\n",
            memoryInfo.nonHeapUsed / 1024.0 / 1024.0,
            memoryInfo.nonHeapMax / 1024.0 / 1024.0);
        
        System.out.printf("线程池状态: 活跃=%d, 池大小=%d, 队列=%d, 已完成=%d\n",
            threadInfo.activeCount,
            threadInfo.poolSize,
            threadInfo.queueSize,
            threadInfo.completedTaskCount);
        
        System.out.println("==================");
    }
    
    /**
     * 内存信息类
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
     * 线程池信息类
     */
    public static class ThreadPoolInfo {
        public final int corePoolSize;
        public final int maximumPoolSize;
        public final int poolSize;
        public final int activeCount;
        public final int queueSize;
        public final long completedTaskCount;
        
        public ThreadPoolInfo(int corePoolSize, int maximumPoolSize, int poolSize, 
                            int activeCount, int queueSize, long completedTaskCount) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.poolSize = poolSize;
            this.activeCount = activeCount;
            this.queueSize = queueSize;
            this.completedTaskCount = completedTaskCount;
        }
    }
    
    /**
     * 性能计时器
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
            System.out.printf("操作 '%s' 耗时: %d ms\n", operationName, duration);
        }
        
        public long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }
    }
} 