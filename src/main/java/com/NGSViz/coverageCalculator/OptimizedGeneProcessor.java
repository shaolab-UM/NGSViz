package com.NGSViz.coverageCalculator;

import com.NGSViz.ReadBam.ReadBam;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.utils.SparseMatrix;
import htsjdk.samtools.SamReader;
import com.NGSViz.sqldbOperate.ProcessEachQueryCoorRecord;
import com.NGSViz.sqldbOperate.QueryWholeRegionCoordinate;
import com.NGSViz.sqldbOperate.Transcript;
import com.NGSViz.ReadBam.BamFileLibrarySize;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Optimized gene processor
 * Contains SamReader object pool, memory management and batch processing optimization
 */
public class OptimizedGeneProcessor {
    private static Map<String, List<Transcript>> gene_map = QueryWholeRegionCoordinate.gene_map;
    private static int num_datapoints = InputParameterAttributes.num_datapoints;
    private static int core_num = InputParameterAttributes.core_num;
    private static final int BATCH_SIZE = 100; // Batch size
    private static final int MAX_MEMORY_THRESHOLD = 80; // Memory usage threshold percentage
    private static final Semaphore fileSemaphore = new Semaphore(core_num * 2);

    /**
     * Optimized parallel gene processing method
     */
    public static HashMap<String, Object> optimizedParallelProcess(String bam_file, 
                                                                  List<String> query_gene_list, 
                                                                  boolean paired_mode) {
        // Create SamReader object pool
        SamReader[] readerPool = createReaderPool(bam_file);
        try {
            // Get library size and chromosome list (only once)
            long library_size = BamFileLibrarySize.getLibrarySize(readerPool[0]);
            Set<String> bam_chromosomes_list = ReadBam.getChromosomesInBam(bam_file);
            
            // Create optimized thread pool
            ThreadPoolExecutor executor = createOptimizedThreadPool();
            
            // Initialize result matrix
            int num_query_gene = query_gene_list.size();
            //double[][] coverage_scaled_matrix = new double[num_query_gene][num_datapoints+1];
            SparseMatrix coverage_scaled_matrix = new SparseMatrix(num_query_gene, num_datapoints+1);
            
            // Batch process gene list
            List<List<String>> batches = splitIntoBatches(query_gene_list, BATCH_SIZE);
            
            // Use CompletableFuture for asynchronous processing
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
                List<String> batch = batches.get(batchIndex);
                int startIndex = batchIndex * BATCH_SIZE;
                
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    processBatch(batch, bam_file, paired_mode, library_size, 
                               bam_chromosomes_list, coverage_scaled_matrix, 
                               startIndex, readerPool);
                }, executor);
                
                futures.add(future);
                
                // Memory monitoring and garbage collection
                monitorMemoryUsage();
            }
            
            // Wait for all tasks to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // Shutdown thread pool
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
            
            HashMap<String, Object> cal_res_map = new HashMap<>();
            cal_res_map.put("row_names", query_gene_list);
            cal_res_map.put("coverage_matrix", coverage_scaled_matrix);
            
            return cal_res_map;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred during processing", e);
        } finally {
            // Close all readers
            closeReaderPool(readerPool);
        }
    }
    
    /**
     * Create SamReader object pool
     */
    private static SamReader[] createReaderPool(String bam_file) {
        SamReader[] readerPool = new SamReader[core_num];
        for (int i = 0; i < core_num; i++) {
            readerPool[i] = ReadBam.getBamReader(bam_file);
        }
        return readerPool;
    }
    
    /**
     * Close SamReader object pool
     */
    private static void closeReaderPool(SamReader[] readerPool) {
        for (SamReader reader : readerPool) {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Create optimized thread pool
     */
    private static ThreadPoolExecutor createOptimizedThreadPool() {
        return new ThreadPoolExecutor(
            core_num,                    // Core thread count
            core_num * 2,               // Maximum thread count
            60L, TimeUnit.SECONDS,      // Thread keep-alive time
            new LinkedBlockingQueue<>(1000), // Work queue
            new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );
    }
    
    /**
     * Split gene list into batches
     */
    private static List<List<String>> splitIntoBatches(List<String> geneList, int batchSize) {
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < geneList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, geneList.size());
            batches.add(geneList.subList(i, end));
        }
        return batches;
    }
    
    /**
     * Process a batch of genes
     */
    private static void processBatch(List<String> batch, String bam_file, boolean paired_mode,
                                   long library_size, Set<String> bam_chromosomes_list,
                                     SparseMatrix coverage_scaled_matrix, int startIndex,
                                   SamReader[] readerPool) {
        try {
            fileSemaphore.acquire();
            try {
                // Get the reader corresponding to current thread
                int threadId = (int) (Thread.currentThread().getId() % core_num);
                SamReader reader = readerPool[threadId];
                
                for (int i = 0; i < batch.size(); i++) {
                    String gene_name = batch.get(i);
                    int matrixIndex = startIndex + i;
                    
                    double[] transcript_mat = processGeneOptimized(gene_name, reader, 
                                                                 paired_mode, library_size, 
                                                                 bam_chromosomes_list);
                    
                    // Copy results to shared matrix
                    //System.arraycopy(transcript_mat, 0, coverage_scaled_matrix[matrixIndex], 0, transcript_mat.length);
                    coverage_scaled_matrix.setRow(matrixIndex, transcript_mat);
                }
            } finally {
                fileSemaphore.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Optimized single gene processing method
     */
    private static double[] processGeneOptimized(String gene_name, SamReader reader,
                                               boolean paired_mode, long library_size,
                                               Set<String> bam_chromosomes_list) {
        System.out.println("> --- Processing query gene name is: " + gene_name);
        List<Transcript> transcript_list = gene_map.get(gene_name);
        double[] transcript_mat = new double[num_datapoints + 1];
        
        if (transcript_list == null) {
            return transcript_mat;
        }
        
        for (Transcript transcript : transcript_list) {
            Map<String, Object> queryDB_record = transcript.getQueryCoord();
            String chr_name = (String) queryDB_record.get("chrom");
            String nochr_name = (String) queryDB_record.get("nochr_name");
            chr_name = ReadBam.checkChromosomeInBam(bam_chromosomes_list, chr_name, nochr_name);
            
            if (chr_name != null) {
                // Calculate coverage - use method that accepts SamReader
                double[] coverage_scaled = ProcessEachQueryCoorRecord.processRecord2(
                    transcript, reader, chr_name, paired_mode).getScaledCoverage();
                
                // Normalize to RPM
                coverage_scaled = BamFileLibrarySize.bamSizeNormalization(coverage_scaled, library_size);
                
                // Accumulate to result matrix
                for (int t = 0; t < coverage_scaled.length; t++) {
                    transcript_mat[t] += coverage_scaled[t];
                }
            }
        }
        
        return transcript_mat;
    }
    
    /**
     * Monitor memory usage
     */
    private static void monitorMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
        
        if (memoryUsagePercent > MAX_MEMORY_THRESHOLD) {
            System.out.println("Memory usage too high: " + String.format("%.2f", memoryUsagePercent) + "%, triggering garbage collection");
            System.gc();
        }
    }
} 