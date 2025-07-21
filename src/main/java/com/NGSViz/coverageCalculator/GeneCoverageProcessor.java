package com.NGSViz.coverageCalculator;

import com.NGSViz.ReadBam.BAMAttribute;
import com.NGSViz.ReadBam.BamFileLibrarySize;
import com.NGSViz.ReadBam.ReadBam;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.sqldbOperate.ProcessEachQueryCoorRecord;
import com.NGSViz.sqldbOperate.Transcript;
import com.NGSViz.utils.SparseMatrix;
import htsjdk.samtools.*;
import htsjdk.samtools.util.Interval;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.*;
import java.util.concurrent.*;



public class GeneCoverageProcessor {
    private static final int num_datapoints = InputParameterAttributes.num_datapoints;
    private BAMAttribute bam_obj;
    private final Set<String> record_name_list;
    private final Map<String, Transcript> DB_GENES;
    private final boolean paired_mode;
    private final int core_num = InputParameterAttributes.core_num;

    public GeneCoverageProcessor(BAMAttribute bam_obj,
                                 Map<String, Transcript> DB_GENES,
                                 Set<String> record_name_list,
                                 boolean paired_mode) {
        this.bam_obj = bam_obj;
        this.paired_mode = paired_mode;
        this.DB_GENES = DB_GENES;
        this.record_name_list = record_name_list;
    }

    // Calculate the coverage matrix
    public Map<String, Object> buildCoverageMatrix() throws IOException {
        // Create result matrix
        SparseMatrix coverage_scaled_matrix = new SparseMatrix(record_name_list.size(), num_datapoints+1);
        // Initialize gene name list
        List<String> record_names = new CopyOnWriteArrayList<>(Collections.nCopies(record_name_list.size(), null));
        // Get the library size of bam file
        //BAMAttribute bamObj = new BAMAttribute(bam_file_path);
        long library_size = bam_obj.getLibrarySize();
        Set<String> bam_chromosomes_list = bam_obj.getBamChromosomesList();
        // Create a parallel stream with custom thread pool
        System.out.println("--- Starting coverage calculation ----");
        ForkJoinPool customThreadPool = new ForkJoinPool(core_num);
        try {
            customThreadPool.submit(() ->
                    record_name_list.parallelStream().forEach(query_record_name -> {
                        SamReaderFactory factory = SamReaderFactory.makeDefault();
                        try (SamReader reader = factory.open(bam_obj.getBamFile());) {
                            Transcript gene_coord = DB_GENES.get(query_record_name);
                            if (gene_coord == null) {
                                System.err.println("Warning: No coordinates found for record: " + query_record_name);
                                return;
                            }

                            String chr_name = gene_coord.getChrLab();
                            String nochr_name = gene_coord.getNoChrLab();
                            chr_name = ReadBam.checkChromosomeInBam(bam_chromosomes_list, chr_name, nochr_name);
                            if (chr_name == null) {
                                System.err.println("Warning: Chromosome not found in BAM for record: " + query_record_name);
                                return;
                            }

                            // Calculate the coverage for each queryDB region
                            EachCoverageResult CoverageScaled = ProcessEachQueryCoorRecord.processRecord2(
                                    gene_coord, reader, chr_name, paired_mode);
                            // Normalize to RPM
                            CoverageScaled.bamSizeNormalization(library_size);
                            // Obtain gene index
                            int index = gene_coord.getIndex();
                            coverage_scaled_matrix.setRow(index, CoverageScaled.getScaledCoverage());
                            // Write gene name
                            record_names.set(index, query_record_name);
                        } catch (Exception e) {
                            throw new RuntimeException("Processing failed for " + query_record_name + ": " + e.getMessage());
                        }
                    })
            ).get(360, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Parallel processing failed: " + e.getMessage());
        } finally {
            customThreadPool.shutdown();
        }
        Map<String, Object> coverage_map = new HashMap<>();
        coverage_map.put("record_names", record_names);
        coverage_map.put("cov_mat", coverage_scaled_matrix);
        return coverage_map;
    }
}