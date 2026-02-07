package com.NGSViz.coverageCalculator;

import com.NGSViz.ReadBam.BAMAttribute;
import com.NGSViz.ReadBam.ReadBam;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.sqldbOperate.ProcessEachQueryCoorRecord;
import com.NGSViz.sqldbOperate.Transcript;
import com.NGSViz.utils.SparseMatrix;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GeneCoverageProcessor {
    private static final int num_datapoints = InputParameterAttributes.num_datapoints;
    private BAMAttribute bam_obj;
    private final Set<String> record_name_list;
    private final Map<Integer, List<Transcript>> geneList_batches;
    private final int core_num = InputParameterAttributes.core_num;

    public GeneCoverageProcessor(
            BAMAttribute bam_obj,
            Map<Integer, List<Transcript>> geneList_batches,
            Set<String> record_name_list
    ) {
        this.bam_obj =  bam_obj;
        this.geneList_batches = geneList_batches;
        this.record_name_list = record_name_list;
    }

    // Define an inner class to encapsulate the calculation results of each chromosome task.
    static class CoverageRow {
        final int index;
        final String geneName;
        final double[] coverage;

        CoverageRow(int index, String geneName, double[] coverage) {
            this.index = index;
            this.geneName = geneName;
            this.coverage = coverage;
        }
    }

    static class BatchResult {
        final List<CoverageRow> rows;

        public BatchResult(int capacity) {
            this.rows = new ArrayList<>(capacity);
        }
    }

    public Map<String, Object> buildCoverageMatrix() throws IOException {
        // Create result matrix
        SparseMatrix coverage_scaled_matrix = new SparseMatrix(record_name_list.size(), num_datapoints + 1);
        String[] final_record_names = new String[record_name_list.size()];

        // Get the library size of bam file
        long library_size = bam_obj.getLibrarySize();
        Set<String> bam_chromosomes_list = bam_obj.getBamChromosomesList();

        System.out.println("--- Starting coverage calculation group by chromosomes ----");
        // Create a ForkJoinPool for parallel processing.
        ExecutorService customThreadPool = Executors.newFixedThreadPool(core_num);
        CompletionService<BatchResult> completionService = new ExecutorCompletionService<>(customThreadPool);
        AtomicInteger taskCount = new AtomicInteger(0);

        try {
            // Submit each chromosome's processing task to the thread pool
            geneList_batches.forEach((batch_num, transcriptList) -> {
                // Each chromosome task opens its own SamReader
                Callable<BatchResult> chromosomeTask = () -> {
                    List<Transcript> sortedTranscripts = new ArrayList<>(transcriptList);
                    sortedTranscripts.sort(Comparator
                            .comparing(Transcript::getChrLab)
                            .thenComparingInt(Transcript::getStartPos));

                    BatchResult result = new BatchResult(sortedTranscripts.size());
                    Map<String, String> chrCache = new HashMap<>();
                    Set<String> warnedMissing = new HashSet<>();
                    try (SamReader reader = SamReaderFactory.makeDefault()
                            .validationStringency(ValidationStringency.SILENT)
                            .open(bam_obj.getBamFile())) {
                        // Traverse all genes on the current batch
                        for (Transcript gene_coord : sortedTranscripts) {
                            String chr_name = gene_coord.getChrLab();
                            String no_chr_name = gene_coord.getNoChrLab();
                            // Check if the chromosome exists in the BAM file.
                            String chrCacheKey = chr_name + "|" + no_chr_name;
                            String actualChrName;
                            if (chrCache.containsKey(chrCacheKey)) {
                                actualChrName = chrCache.get(chrCacheKey);
                            } else {
                                actualChrName = ReadBam.checkChromosomeInBam(bam_chromosomes_list, chr_name, no_chr_name);
                                chrCache.put(chrCacheKey, actualChrName);
                            }
                            //If the queried chromosome is not in the chromosomes of the bam file.
                            if (actualChrName == null) {
                                if (warnedMissing.add(chrCacheKey)) {
                                    System.err.println("Warning: Chromosome '" + chr_name + "' not found in BAM. Skipping associated records.");
                                }
                                continue;
                            }
                            // Invoke the method for calculating the physical coverage.
                            EachCoverageResult CoverageScaled = ProcessEachQueryCoorRecord.processRecord2(
                                    gene_coord, reader, actualChrName);
                            // Normalize to RPM
                            CoverageScaled.bamSizeNormalization(library_size);
                            // Obtain gene index and name
                            int index = gene_coord.getIndex();
                            String geneName = gene_coord.getRecordName();
                            // Store the results in the local result list of the current batch task
                            result.rows.add(new CoverageRow(index, geneName, CoverageScaled.getScaledCoverage()));
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing batch " + batch_num + ": " + e.getMessage());
                        throw new RuntimeException("Processing failed for batch " + batch_num + ": " + e.getMessage(), e);
                    }
                    return result; // Return the computational result of the chromosome task
                };
                completionService.submit(chromosomeTask); // Submit the task and collect Future
                taskCount.incrementAndGet();
            });

            // --- Aggregation Phase: Collect and merge all threads' local results ---
            System.out.println("--- Consolidating results ----");
            for (int i = 0; i < taskCount.get(); i++) {
                try {
                    BatchResult chrResult = completionService.take().get(); // Block until the task is completed and the result is obtained.
                    // Merge the local results into the final shared matrix and list.
                    for (CoverageRow row : chrResult.rows) {
                        coverage_scaled_matrix.setRow(row.index, row.coverage);
                        final_record_names[row.index] = row.geneName;
                    }

                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Failed to retrieve chromosome task result: " + e.getMessage());
                    throw new RuntimeException("Error during result consolidation: " + e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Parallel processing failed: " + e.getMessage(), e);
        } finally {
            customThreadPool.shutdown();
            try {
                if (!customThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate in time. Forcing shutdown.");
                    customThreadPool.shutdownNow();
                }
            } catch (InterruptedException ie) {
                customThreadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        Map<String, Object> coverage_map = new HashMap<>();
        coverage_map.put("record_names", Arrays.asList(final_record_names));
        coverage_map.put("cov_mat", coverage_scaled_matrix);
        return coverage_map;
    }
}