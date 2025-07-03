package com.NGSViz.utils;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Memory manager for preventing memory leaks and optimizing memory usage
 * Provides cache management, memory monitoring, and automatic cleanup
 */
public class MemoryManager {
    
    private static final int DEFAULT_CACHE_SIZE_LIMIT = 1000;
    private static final long CLEANUP_INTERVAL = 300000; // 5 minutes
    private static final double MEMORY_THRESHOLD = 0.8; // 80% memory usage threshold
    
    private static final Map<String, CacheEntry> globalCache = new ConcurrentHashMap<>();
    private static final List<WeakReference<Object>> trackedObjects = new ArrayList<>();
    private static final ScheduledExecutorService cleanupExecutor = 
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
    
    private static ScheduledFuture<?> cleanupTask;
    
    static {
        startCleanupTask();
    }
    
    /**
     * Cache entry with timestamp and access count
     */
    private static class CacheEntry {
        public final Object data;
        public final long timestamp;
        public int accessCount;
        public long lastAccess;
        
        public CacheEntry(Object data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.accessCount = 0;
            this.lastAccess = timestamp;
        }
        
        public void access() {
            accessCount++;
            lastAccess = System.currentTimeMillis();
        }
    }
    
    /**
     * LRU Cache implementation
     */
    public static class LRUCache<K, V> {
        private final int maxSize;
        private final Map<K, V> cache;
        private final Queue<K> accessOrder;
        
        public LRUCache(int maxSize) {
            this.maxSize = maxSize;
            this.cache = new ConcurrentHashMap<>();
            this.accessOrder = new LinkedList<>();
        }
        
        public V get(K key) {
            V value = cache.get(key);
            if (value != null) {
                // Update access order
                accessOrder.remove(key);
                accessOrder.offer(key);
            }
            return value;
        }
        
        public void put(K key, V value) {
            if (cache.size() >= maxSize) {
                // Remove least recently used item
                K lruKey = accessOrder.poll();
                if (lruKey != null) {
                    cache.remove(lruKey);
                }
            }
            
            cache.put(key, value);
            accessOrder.remove(key);
            accessOrder.offer(key);
        }
        
        public void clear() {
            cache.clear();
            accessOrder.clear();
        }
        
        public int size() {
            return cache.size();
        }
        
        public boolean isEmpty() {
            return cache.isEmpty();
        }
    }
    
    /**
     * Add object to global cache with automatic cleanup
     */
    public static void cacheObject(String key, Object data) {
        globalCache.put(key, new CacheEntry(data));
        
        // Check if cache size exceeds limit
        if (globalCache.size() > DEFAULT_CACHE_SIZE_LIMIT) {
            cleanupOldEntries();
        }
    }
    
    /**
     * Get object from global cache
     */
    public static Object getCachedObject(String key) {
        CacheEntry entry = globalCache.get(key);
        if (entry != null) {
            entry.access();
            return entry.data;
        }
        return null;
    }
    
    /**
     * Remove object from global cache
     */
    public static void removeCachedObject(String key) {
        globalCache.remove(key);
    }
    
    /**
     * Track object for memory leak detection
     */
    public static void trackObject(Object obj) {
        trackedObjects.add(new WeakReference<>(obj));
    }
    
    /**
     * Clean up old cache entries
     */
    private static void cleanupOldEntries() {
        long currentTime = System.currentTimeMillis();
        long maxAge = 3600000; // 1 hour
        
        globalCache.entrySet().removeIf(entry -> {
            CacheEntry cacheEntry = entry.getValue();
            return (currentTime - cacheEntry.lastAccess) > maxAge;
        });
        
        // Clean up tracked objects
        trackedObjects.removeIf(ref -> ref.get() == null);
    }
    
    /**
     * Start automatic cleanup task
     */
    private static void startCleanupTask() {
        cleanupTask = cleanupExecutor.scheduleAtFixedRate(() -> {
            try {
                cleanupOldEntries();
                checkMemoryUsage();
            } catch (Exception e) {
                System.err.println("Error in memory cleanup task: " + e.getMessage());
            }
        }, CLEANUP_INTERVAL, CLEANUP_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Check memory usage and trigger cleanup if needed
     */
    private static void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / totalMemory;
        
        if (memoryUsage > MEMORY_THRESHOLD) {
            System.out.println("Memory usage high: " + String.format("%.1f", memoryUsage * 100) + "%. Triggering cleanup.");
            
            // Force garbage collection
            System.gc();
            
            // Clear old cache entries
            cleanupOldEntries();
            
            // Clear BAM reader caches if available
            try {
                Class<?> bamReaderClass = Class.forName("com.NGSViz.ReadBam.OptimizedBamReader");
                java.lang.reflect.Method clearMethod = bamReaderClass.getMethod("clearCaches");
                clearMethod.invoke(null);
            } catch (Exception e) {
                // Ignore if method not available
            }
        }
    }
    
    /**
     * Get memory statistics
     */
    public static MemoryStats getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        return new MemoryStats(
            usedMemory,
            totalMemory,
            maxMemory,
            globalCache.size(),
            trackedObjects.size()
        );
    }
    
    /**
     * Print memory statistics
     */
    public static void printMemoryStats() {
        MemoryStats stats = getMemoryStats();
        System.out.println("=== Memory Statistics ===");
        System.out.printf("Used Memory: %.2f MB\n", stats.usedMemory / 1024.0 / 1024.0);
        System.out.printf("Total Memory: %.2f MB\n", stats.totalMemory / 1024.0 / 1024.0);
        System.out.printf("Max Memory: %.2f MB\n", stats.maxMemory / 1024.0 / 1024.0);
        System.out.printf("Memory Usage: %.1f%%\n", (double) stats.usedMemory / stats.totalMemory * 100);
        System.out.printf("Cached Objects: %d\n", stats.cachedObjects);
        System.out.printf("Tracked Objects: %d\n", stats.trackedObjects);
        System.out.println("========================");
    }
    
    /**
     * Force cleanup of all caches
     */
    public static void forceCleanup() {
        System.out.println("Forcing memory cleanup...");
        
        // Clear global cache
        globalCache.clear();
        
        // Clear tracked objects
        trackedObjects.clear();
        
        // Force garbage collection
        System.gc();
        
        System.out.println("Memory cleanup completed");
    }
    
    /**
     * Shutdown memory manager
     */
    public static void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel(false);
        }
        cleanupExecutor.shutdown();
        
        // Final cleanup
        forceCleanup();
        
        System.out.println("Memory manager shutdown complete");
    }
    
    /**
     * Memory statistics class
     */
    public static class MemoryStats {
        public final long usedMemory;
        public final long totalMemory;
        public final long maxMemory;
        public final int cachedObjects;
        public final int trackedObjects;
        
        public MemoryStats(long usedMemory, long totalMemory, long maxMemory, 
                          int cachedObjects, int trackedObjects) {
            this.usedMemory = usedMemory;
            this.totalMemory = totalMemory;
            this.maxMemory = maxMemory;
            this.cachedObjects = cachedObjects;
            this.trackedObjects = trackedObjects;
        }
    }
    
    /**
     * Auto-cleanup resource wrapper
     */
    public static class AutoCleanupResource implements AutoCloseable {
        private final String resourceKey;
        
        public AutoCleanupResource(String resourceKey) {
            this.resourceKey = resourceKey;
        }
        
        @Override
        public void close() {
            removeCachedObject(resourceKey);
        }
    }
} 