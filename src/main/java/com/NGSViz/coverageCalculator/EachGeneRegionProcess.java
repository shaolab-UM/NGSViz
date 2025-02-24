package com.NGSViz.coverageCalculator;

import com.NGSViz.ReadBam.ReadBam;
import com.NGSViz.configSet.InputParameterAttributes;
import htsjdk.samtools.SamReader;
import com.NGSViz.sqldbOperate.ProcessEachQueryCoorRecord;
import com.NGSViz.sqldbOperate.QueryWholeRegionCoordinate;
import com.NGSViz.sqldbOperate.Transcript;
import com.NGSViz.ReadBam.BamFileLibrarySize;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Benchen Ye
 * @create 2025-02--23:06
 */
public class EachGeneRegionProcess {
    private static Map<String, List<Transcript>> gene_map = QueryWholeRegionCoordinate.gene_map;
    private static int num_datapoints = InputParameterAttributes.num_datapoints;
    private static int core_num = InputParameterAttributes.core_num;
    private static final int MAX_THREADS = 10;
    private static final int MAX_OPEN_FILES = 50;
    private static final Semaphore fileSemaphore = new Semaphore(MAX_OPEN_FILES);

    public static HashMap<String, Object> processEachQueryRegion(String bam_file, List<String> query_gene_list, boolean paired_mode){
        // initiation coverage_scaled_matrix (heatmap data)
        int num_query_gene = query_gene_list.size();
        double[][] coverage_scaled_matrix = new double[num_query_gene][num_datapoints+1];
        // get the library size of bam file
        SamReader bam_reader = ReadBam.getBamReader(bam_file);
        long library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
        // get the chromosome list in bam file
        Set<String> bam_chromosomes_list = ReadBam.getChromosomesInBam(bam_file);
        // each gene and its transcript loop
        int gene = 0;
        for (String gene_name : query_gene_list){
            List<Double> transcript_mat = new ArrayList<>(Collections.nCopies(num_datapoints+1, 0.0));
            // add the coverage_scaled to coverage_scaled_matrix
            transcript_mat = processGene(gene_name, bam_file, paired_mode, library_size, bam_chromosomes_list);
            for (int j = 0; j < transcript_mat.size(); j++) {
                coverage_scaled_matrix[gene][j] = transcript_mat.get(j);
            }
            gene++;
        }
        HashMap<String, Object> cal_res_map = new HashMap<>();
        cal_res_map.put("row_names", query_gene_list);
        cal_res_map.put("coverage_matrix", coverage_scaled_matrix);
        return cal_res_map;
    }

    public static HashMap<String, Object> parallelProcessEachGene2(String bam_file, List<String> query_gene_list, boolean paired_mode) {
        // initiation coverage_scaled_matrix (heatmap data)
        int num_query_gene = query_gene_list.size();
        double[][] coverage_scaled_matrix = new double[num_query_gene][num_datapoints+1];
        // get the library size of bam file
        SamReader bam_reader = ReadBam.getBamReader(bam_file);
        long library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
        // get the chromosome list in bam file
        Set<String> bam_chromosomes_list = ReadBam.getChromosomesInBam(bam_file);
        // each gene and its transcript loop
        int gene = 0;

        ExecutorService executor = Executors.newFixedThreadPool(core_num);
        AtomicInteger geneIndex = new AtomicInteger(0);

        for (String gene_name : query_gene_list) {
            int finalGeneIndex = geneIndex.getAndIncrement();
            executor.submit(() -> {
                try {
                    // Obtain file pool license
                    fileSemaphore.acquire();
                    try {
                        List<Double> transcript_mat = processGene(gene_name, bam_file, paired_mode, library_size, bam_chromosomes_list);
                        for (int j = 0; j < transcript_mat.size(); j++) {
                            coverage_scaled_matrix[finalGeneIndex][j] = transcript_mat.get(j);
                        }
                    } finally {
                        // Release file pool license
                        fileSemaphore.release();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(); // Handle interrupt exceptions
        }

        HashMap<String, Object> cal_res_map = new HashMap<>();
        cal_res_map.put("row_names", query_gene_list);
        cal_res_map.put("coverage_matrix", coverage_scaled_matrix);
        return cal_res_map;
    }

    public static HashMap<String, Object> parallelProcessEachGene(String bam_file, List<String> query_gene_list, boolean paired_mode) {
        // get the library size of bam file
        SamReader bam_reader = ReadBam.getBamReader(bam_file);
        long library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
        // get the chromosome list in bam file
        Set<String> bam_chromosomes_list = ReadBam.getChromosomesInBam(bam_file);
        // Create a fixed-size thread pool
        // Use the number of CPU cores as the number of threads
        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        // submit the task
        List<CompletableFuture<Map.Entry<String, List<Double>>>> futures = query_gene_list.stream()
                .map(gene -> CompletableFuture.supplyAsync(() -> processParallelGene(gene, bam_file,
                        paired_mode, library_size, bam_chromosomes_list), executor))
                .collect(Collectors.toList());
        //
        List<Map.Entry<String, List<Double>>> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        // Shutdown the thread pool and wait for all tasks to complete
        executor.shutdown();
        // merge data
        List<String> geneNames = new ArrayList<>();
        List<List<Double>> geneResults = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : results) {
            geneNames.add(entry.getKey());
            geneResults.add(entry.getValue());
        }
        double[][] geneMatrix = new double[geneResults.size()][];
        for (int i = 0; i < geneResults.size(); i++) {
            List<Double> row = geneResults.get(i);
            geneMatrix[i] = new double[row.size()];
            for (int j = 0; j < row.size(); j++) {
                geneMatrix[i][j] = row.get(j);
            }
        }
        // merge data
        HashMap<String, Object> cal_res_map = new HashMap<>();
        cal_res_map.put("row_names", geneNames);
        cal_res_map.put("coverage_matrix", geneMatrix);
        //HashMap<String, Object> cal_res_map = mergeResults(futures);
        return cal_res_map;
    }
    /*
    * process one gene and generate a coverage list of all transcript for this gene
    * sum the result for multi-transcript
    * */
    private static Map.Entry<String, List<Double>> processParallelGene(String gene_name,
                                    String bam_file,
                                    boolean paired_mode,
                                    long library_size,
                                    Set<String> bam_chromosomes_list
    ) {
        System.out.println("> --- Processing query gene name is: " + gene_name);
        List<Transcript> transcript_list = gene_map.get(gene_name);
        List<Double> coverage_scaled = new ArrayList<>();
        List<Double> transcript_mat = new ArrayList<>(Collections.nCopies(num_datapoints+1, 0.0));
        for (Transcript transcript : transcript_list) {
            Map<String, Object> queryDB_record = transcript.getQueryCoord();
            System.out.println("> ---This query region range is: \n" + queryDB_record);
            String chr_name = (String) queryDB_record.get("chrom");
            String nochr_name = (String) queryDB_record.get("nochr_name");
            chr_name = ReadBam.checkChromosomeInBam(bam_chromosomes_list, chr_name, nochr_name);
            if (chr_name != null) {
                // calculate the coverage for each queryDB region
                coverage_scaled = ProcessEachQueryCoorRecord.processRecord(queryDB_record, bam_file, chr_name, paired_mode);
                // normalize to RPM.
                coverage_scaled = BamFileLibrarySize.bamSizeNormalization(coverage_scaled, library_size);
                // add the coverage_scaled to transcript_mat
                for (int t = 0; t < coverage_scaled.size(); t++) {
                    transcript_mat.set(t, transcript_mat.get(t) + coverage_scaled.get(t));
                }
            }
        }
        List<Double> result = new ArrayList<>();
        return new AbstractMap.SimpleEntry<>(gene_name, result);
        //return transcript_mat;
    }

    private static List<Double> processGene(String gene_name,
                                                               String bam_file,
                                                               boolean paired_mode,
                                                               long library_size,
                                                               Set<String> bam_chromosomes_list
    ) {
        System.out.println("> --- Processing query gene name is: " + gene_name);
        List<Transcript> transcript_list = gene_map.get(gene_name);
        List<Double> coverage_scaled = new ArrayList<>();
        List<Double> transcript_mat = new ArrayList<>(Collections.nCopies(num_datapoints+1, 0.0));
        for (Transcript transcript : transcript_list) {
            Map<String, Object> queryDB_record = transcript.getQueryCoord();
            System.out.println("> ---This query region range is: \n" + queryDB_record);
            String chr_name = (String) queryDB_record.get("chrom");
            String nochr_name = (String) queryDB_record.get("nochr_name");
            chr_name = ReadBam.checkChromosomeInBam(bam_chromosomes_list, chr_name, nochr_name);
            if (chr_name != null) {
                // calculate the coverage for each queryDB region
                coverage_scaled = ProcessEachQueryCoorRecord.processRecord(queryDB_record, bam_file, chr_name, paired_mode);
                // normalize to RPM.
                coverage_scaled = BamFileLibrarySize.bamSizeNormalization(coverage_scaled, library_size);
                // add the coverage_scaled to transcript_mat
                for (int t = 0; t < coverage_scaled.size(); t++) {
                    transcript_mat.set(t, transcript_mat.get(t) + coverage_scaled.get(t));
                }
            }
        }
        return transcript_mat;
    }
}
