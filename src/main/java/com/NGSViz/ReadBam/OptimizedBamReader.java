package com.NGSViz.ReadBam;

import htsjdk.samtools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimized BAM reader with caching and performance improvements
 * Provides efficient BAM file reading with chromosome caching and connection pooling
 */
public class OptimizedBamReader {
    
    private static final Map<String, Set<String>> chromosomeCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> librarySizeCache = new ConcurrentHashMap<>();
    private static final int CACHE_SIZE_LIMIT = 100;
    
    /**
     * Get BAM reader with optimized settings
     */
    public static SamReader getOptimizedBamReader(String bam_file_path) {
        File bam_file = new File(bam_file_path);
        String bam_name = bam_file.getName();
        
        // Check if BAM file exists
        if (!bam_file.exists()) {
            System.err.println("Error: The BAM file " + bam_file_path + " does not exist!");
            System.exit(1);
        }
        
        // Create optimized SamReader
        SamReaderFactory factory = SamReaderFactory.makeDefault()
                .validationStringency(ValidationStringency.SILENT); // Reduce validation overhead
        
        System.out.println("Reading optimized BAM file: " + bam_name);
        return factory.open(bam_file);
    }
    
    /**
     * Get chromosomes with caching
     */
    public static Set<String> getChromosomesWithCache(String bam_file_path) {
        return chromosomeCache.computeIfAbsent(bam_file_path, k -> {
            Set<String> chromosomes = ReadBam.getChromosomesInBam(bam_file_path);
            
            // Limit cache size
            if (chromosomeCache.size() > CACHE_SIZE_LIMIT) {
                // Remove oldest entries (simple LRU-like behavior)
                String oldestKey = chromosomeCache.keySet().iterator().next();
                chromosomeCache.remove(oldestKey);
            }
            
            return chromosomes;
        });
    }
    
    /**
     * Get library size with caching
     */
    public static long getLibrarySizeWithCache(String bam_file_path) {
        return librarySizeCache.computeIfAbsent(bam_file_path, k -> {
            try (SamReader reader = getOptimizedBamReader(bam_file_path)) {
                long size = BamFileLibrarySize.getLibrarySize(reader);
                
                // Limit cache size
                if (librarySizeCache.size() > CACHE_SIZE_LIMIT) {
                    String oldestKey = librarySizeCache.keySet().iterator().next();
                    librarySizeCache.remove(oldestKey);
                }
                
                return size;
            } catch (Exception e) {
                e.printStackTrace();
                return 0L;
            }
        });
    }
    
    /**
     * Batch process multiple BAM files efficiently
     */
    public static Map<String, Long> getLibrarySizesBatch(List<String> bam_files) {
        Map<String, Long> results = new HashMap<>();
        
        for (String bam_file : bam_files) {
            results.put(bam_file, getLibrarySizeWithCache(bam_file));
        }
        
        return results;
    }
    
    /**
     * Clear all caches (useful for memory management)
     */
    public static void clearCaches() {
        chromosomeCache.clear();
        librarySizeCache.clear();
        System.gc(); // Suggest garbage collection
    }
    
    /**
     * Get cache statistics
     */
    public static void printCacheStats() {
        System.out.println("=== BAM Reader Cache Statistics ===");
        System.out.println("Chromosome cache size: " + chromosomeCache.size());
        System.out.println("Library size cache size: " + librarySizeCache.size());
        System.out.println("================================");
    }
    
    /**
     * Optimized chromosome checking with caching
     */
    public static String checkChromosomeWithCache(String bam_file_path, String chromosome_2_check, String nochrName_2_check) {
        Set<String> chromosomes = getChromosomesWithCache(bam_file_path);
        
        if (chromosomes.contains(chromosome_2_check)) {
            return chromosome_2_check;
        } else if (chromosomes.contains(nochrName_2_check)) {
            return nochrName_2_check;
        } else {
            System.out.println(chromosome_2_check + " is not in the set.");
            return null;
        }
    }
} 