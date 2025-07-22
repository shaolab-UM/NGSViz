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

public class GeneCoverageProcessorByChromosome {
    private static final int num_datapoints = InputParameterAttributes.num_datapoints;
    private BAMAttribute bam_obj;
    private final Set<String> record_name_list;
    private final Map<String, List<Transcript>> genesByChromosome;
    private final int core_num = InputParameterAttributes.core_num;

    public GeneCoverageProcessorByChromosome(
            BAMAttribute bam_obj,
            Map<String, List<Transcript>> genesByChromosome,
            Set<String> record_name_list
    ) {
        this.bam_obj =  bam_obj;
        this.genesByChromosome = genesByChromosome;
        this.record_name_list = record_name_list;
    }

    // Define an inner class to encapsulate the calculation results of each chromosome task.
    static class ChromosomeResult {
        // Storage of the coverage data for all genes on the chromosome (gene index -> coverage array)
        Map<Integer, double[]> coverageData;
        // Store the names of all genes on the chromosome (gene index -> gene name)
        Map<Integer, String> geneNames;
        public ChromosomeResult() {
            this.coverageData = new HashMap<>();
            this.geneNames = new HashMap<>();
        }
    }

    public Map<String, Object> buildCoverageMatrix() throws IOException {
        // Create result matrix
        SparseMatrix coverage_scaled_matrix = new SparseMatrix(record_name_list.size(), num_datapoints + 1);
        // Initialize the gene name list, used to record the final sequence
        List<String> final_record_names = new ArrayList<>(record_name_list.size());
        for (int i = 0; i < record_name_list.size(); i++) {
            final_record_names.add(null);
        }

        // Get the library size of bam file
        long library_size = bam_obj.getLibrarySize();
        Set<String> bam_chromosomes_list = bam_obj.getBamChromosomesList();

        System.out.println("--- Starting coverage calculation group by chromosomes ----");
        // Create a ForkJoinPool for parallel processing.
        ForkJoinPool customThreadPool = new ForkJoinPool(core_num);
        // Store all the Future objects returned by the chromosome task
        List<Future<ChromosomeResult>> futures = new ArrayList<>();

        try {
            // Submit each chromosome's processing task to the thread pool
            genesByChromosome.forEach((chrName, transcriptList) -> {
                // Each chromosome task opens its own SamReader
                Callable<ChromosomeResult> chromosomeTask = () -> {
                    ChromosomeResult result = new ChromosomeResult();
                    try (SamReader reader = SamReaderFactory.makeDefault()
                            .validationStringency(ValidationStringency.SILENT)
                            .open(bam_obj.getBamFile())) {

                        // Check if the chromosome exists in the BAM file.
                        String actualChrName = ReadBam.checkChromosomeInBam(bam_chromosomes_list, chrName, transcriptList.get(0).getNoChrLab());

                        //If the queried chromosome is not in the chromosomes of the bam file.
                        if (actualChrName == null) {
                            System.err.println("Warning: Chromosome '" + chrName + "' not found in BAM. Skipping associated records.");
                            return result;
                        }

                        // Traverse all genes on the current chromosome
                        for (Transcript gene_coord : transcriptList) {
                            if (gene_coord == null) {
                                System.err.println("Warning: Null gene_coord found in chromosome " + chrName + ". Skipping.");
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
                            // Store the results in the local result Map of the current chromosome task
                            result.coverageData.put(index, CoverageScaled.getScaledCoverage());
                            result.geneNames.put(index, geneName);
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing chromosome " + chrName + ": " + e.getMessage());
                        throw new RuntimeException("Processing failed for chromosome " + chrName + ": " + e.getMessage(), e);
                    }
                    return result; // Return the computational result of the chromosome task
                };
                futures.add(customThreadPool.submit(chromosomeTask)); // Submit the task and collect Future
            });

            // --- Aggregation Phase: Collect and merge all threads' local results ---
            System.out.println("--- Consolidating results ----");
            for (Future<ChromosomeResult> future : futures) {
                try {
                    ChromosomeResult chrResult = future.get(); // Block until the task is completed and the result is obtained.
                    // Merge the local results into the final shared matrix and list.
                    chrResult.coverageData.forEach(coverage_scaled_matrix::setRow); // SparseMatrix.setRow(index, double[])
                    for (Map.Entry<Integer, String> entry : chrResult.geneNames.entrySet()) {
                        ((ArrayList<String>) final_record_names).set(entry.getKey(), entry.getValue());
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
        coverage_map.put("record_names", final_record_names);
        coverage_map.put("cov_mat", coverage_scaled_matrix);
        return coverage_map;
    }
}