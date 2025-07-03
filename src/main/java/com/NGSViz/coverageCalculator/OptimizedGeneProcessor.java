package com.NGSViz.coverageCalculator;

import com.NGSViz.ReadBam.ReadBam;
import com.NGSViz.configSet.InputParameterAttributes;
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
 * 优化版本的基因处理器
 * 包含SamReader对象池、内存管理和批处理优化
 */
public class OptimizedGeneProcessor {
    private static Map<String, List<Transcript>> gene_map = QueryWholeRegionCoordinate.gene_map;
    private static int num_datapoints = InputParameterAttributes.num_datapoints;
    private static int core_num = InputParameterAttributes.core_num;
    private static final int BATCH_SIZE = 100; // 批处理大小
    private static final int MAX_MEMORY_THRESHOLD = 80; // 内存使用阈值百分比
    private static final Semaphore fileSemaphore = new Semaphore(core_num * 2);

    /**
     * 优化的并行基因处理方法
     */
    public static HashMap<String, Object> optimizedParallelProcess(String bam_file, 
                                                                  List<String> query_gene_list, 
                                                                  boolean paired_mode) {
        // 创建SamReader对象池
        SamReader[] readerPool = createReaderPool(bam_file);
        
        try {
            // 获取库大小和染色体列表（只获取一次）
            long library_size = BamFileLibrarySize.getLibrarySize(readerPool[0]);
            Set<String> bam_chromosomes_list = ReadBam.getChromosomesInBam(bam_file);
            
            // 创建优化的线程池
            ThreadPoolExecutor executor = createOptimizedThreadPool();
            
            // 初始化结果矩阵
            int num_query_gene = query_gene_list.size();
            double[][] coverage_scaled_matrix = new double[num_query_gene][num_datapoints+1];
            
            // 批处理基因列表
            List<List<String>> batches = splitIntoBatches(query_gene_list, BATCH_SIZE);
            
            // 使用CompletableFuture进行异步处理
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
                
                // 内存监控和垃圾回收
                monitorMemoryUsage();
            }
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 关闭线程池
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
            
            HashMap<String, Object> cal_res_map = new HashMap<>();
            cal_res_map.put("row_names", query_gene_list);
            cal_res_map.put("coverage_matrix", coverage_scaled_matrix);
            
            return cal_res_map;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("处理过程中发生错误", e);
        } finally {
            // 关闭所有reader
            closeReaderPool(readerPool);
        }
    }
    
    /**
     * 创建SamReader对象池
     */
    private static SamReader[] createReaderPool(String bam_file) {
        SamReader[] readerPool = new SamReader[core_num];
        for (int i = 0; i < core_num; i++) {
            readerPool[i] = ReadBam.getBamReader(bam_file);
        }
        return readerPool;
    }
    
    /**
     * 关闭SamReader对象池
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
     * 创建优化的线程池
     */
    private static ThreadPoolExecutor createOptimizedThreadPool() {
        return new ThreadPoolExecutor(
            core_num,                    // 核心线程数
            core_num * 2,               // 最大线程数
            60L, TimeUnit.SECONDS,      // 空闲线程存活时间
            new LinkedBlockingQueue<>(1000), // 工作队列
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
    }
    
    /**
     * 将基因列表分割成批次
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
     * 处理一批基因
     */
    private static void processBatch(List<String> batch, String bam_file, boolean paired_mode,
                                   long library_size, Set<String> bam_chromosomes_list,
                                   double[][] coverage_scaled_matrix, int startIndex,
                                   SamReader[] readerPool) {
        try {
            fileSemaphore.acquire();
            try {
                // 获取当前线程对应的reader
                int threadId = (int) (Thread.currentThread().getId() % core_num);
                SamReader reader = readerPool[threadId];
                
                for (int i = 0; i < batch.size(); i++) {
                    String gene_name = batch.get(i);
                    int matrixIndex = startIndex + i;
                    
                    double[] transcript_mat = processGeneOptimized(gene_name, reader, 
                                                                 paired_mode, library_size, 
                                                                 bam_chromosomes_list);
                    
                    // 复制结果到共享矩阵
                    System.arraycopy(transcript_mat, 0, coverage_scaled_matrix[matrixIndex], 0, transcript_mat.length);
                }
            } finally {
                fileSemaphore.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 优化的单个基因处理方法
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
                // 计算覆盖度 - 使用接受SamReader的方法
                double[] coverage_scaled = ProcessEachQueryCoorRecord.processRecord2(
                    transcript, reader, chr_name, paired_mode).getScaledCoverage();
                
                // 标准化到RPM
                coverage_scaled = BamFileLibrarySize.bamSizeNormalization(coverage_scaled, library_size);
                
                // 累加到结果矩阵
                for (int t = 0; t < coverage_scaled.length; t++) {
                    transcript_mat[t] += coverage_scaled[t];
                }
            }
        }
        
        return transcript_mat;
    }
    
    /**
     * 监控内存使用情况
     */
    private static void monitorMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
        
        if (memoryUsagePercent > MAX_MEMORY_THRESHOLD) {
            System.out.println("内存使用率过高: " + String.format("%.2f", memoryUsagePercent) + "%，触发垃圾回收");
            System.gc();
        }
    }
} 