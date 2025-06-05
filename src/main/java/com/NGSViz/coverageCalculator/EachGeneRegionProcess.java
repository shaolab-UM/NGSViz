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

    public static HashMap<String, Object> parallelProcessEachGene(String bam_file, List<String> query_gene_list, boolean paired_mode) {
        // initiation coverage_scaled_matrix (heatmap data)
        int num_query_gene = query_gene_list.size();
        double[][] coverage_scaled_matrix = new double[num_query_gene][num_datapoints+1];
        // get the library size of bam file
        SamReader bam_reader = ReadBam.getBamReader(bam_file);
        long library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
        ReadBam.closeBamReader (bam_reader);
        // get the chromosome list in bam file
        Set<String> bam_chromosomes_list = ReadBam.getChromosomesInBam(bam_file);
        // each gene and its transcript loop
        // Create a fixed-size thread pool
        ExecutorService executor = Executors.newFixedThreadPool(core_num);
        // Use AtomicInteger to atomically generate the index for each gene.
        // This ensures that each gene can obtain a unique and correct index, avoiding race conditions.
        AtomicInteger geneIndex = new AtomicInteger(0);

        for (String gene_name : query_gene_list) {
            // To obtain and increment a unique index for the current gene
            int finalGeneIndex = geneIndex.getAndIncrement();
            // Encapsulate the processing logic of each gene into a Runnable task and submit it to the thread pool.
            executor.submit(() -> {
                try {
                    // Obtain file pool license
                    fileSemaphore.acquire();
                    try {
                        double[] transcript_mat = processGene(gene_name, bam_file, paired_mode, library_size, bam_chromosomes_list);
                        // Copy the results to the shared matrix
                        System.arraycopy(transcript_mat, 0, coverage_scaled_matrix[finalGeneIndex], 0, transcript_mat.length);
                    } finally {
                        // Release file pool license
                        fileSemaphore.release();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        // Stop accepting new tasks, but will continue to execute tasks that have already been submitted.
        executor.shutdown();
        try {
            // Ensured that all genes have been processed before returning the results.
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(); // Handle interrupt exceptions
        }

        HashMap<String, Object> cal_res_map = new HashMap<>();
        cal_res_map.put("row_names", query_gene_list);
        cal_res_map.put("coverage_matrix", coverage_scaled_matrix);
        return cal_res_map;
    }

    private static double[] processGene(String gene_name,
                                            String bam_file,
                                            boolean paired_mode,
                                            long library_size,
                                            Set<String> bam_chromosomes_list
    ) {
        System.out.println("> --- Processing query gene name is: " + gene_name);
        List<Transcript> transcript_list = gene_map.get(gene_name);
        double[] coverage_scaled;
        double[] transcript_mat = new double[num_datapoints + 1];
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
                for (int t = 0; t < coverage_scaled.length; t++) {
                    transcript_mat[t] = transcript_mat[t] + coverage_scaled[t];
                }
            }
        }
        return transcript_mat;
    }
}
