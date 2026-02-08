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
        this.bam_obj = bam_obj;
        this.geneList_batches = geneList_batches;
        this.record_name_list = record_name_list;
    }

    static class CoverageRow {
        final int index;
        final String geneName;
        final double[] coverage;
        final int readCount;

        CoverageRow(int index, String geneName, double[] coverage, int readCount) {
            this.index = index;
            this.geneName = geneName;
            this.coverage = coverage;
            this.readCount = readCount;
        }
    }

    static class BatchResult {
        final List<CoverageRow> rows;

        public BatchResult(int capacity) {
            this.rows = new ArrayList<>(capacity);
        }
    }

    public Map<String, Object> buildCoverageMatrix(Double scale_ratio) throws IOException {
        SparseMatrix coverage_scaled_matrix = new SparseMatrix(record_name_list.size(), num_datapoints + 1);
        String[] final_record_names = new String[record_name_list.size()];
        int[] final_read_counts = new int[record_name_list.size()];

        long library_size = bam_obj.getLibrarySize();
        Set<String> bam_chromosomes_list = bam_obj.getBamChromosomesList();

        System.out.println("--- Starting coverage calculation group by chromosomes ----");
        ExecutorService customThreadPool = Executors.newFixedThreadPool(core_num);
        // get parallel result
        CompletionService<BatchResult> completionService = new ExecutorCompletionService<>(customThreadPool);
        AtomicInteger taskCount = new AtomicInteger(0);
        // [CHANGED] Reuse one SamReader per worker thread instead of opening per batch task.
        List<SamReader> readersToClose = Collections.synchronizedList(new ArrayList<>());
        ThreadLocal<SamReader> threadLocalReader = ThreadLocal.withInitial(() -> {
            try {
                SamReader reader = SamReaderFactory.makeDefault()
                        .validationStringency(ValidationStringency.SILENT)
                        .open(bam_obj.getBamFile());
                readersToClose.add(reader);
                return reader;
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize thread-local SamReader: " + e.getMessage(), e);
            }
        });

        try {
            geneList_batches.forEach((batch_num, transcriptList) -> {
                Callable<BatchResult> chromosomeTask = () -> {
                    List<Transcript> sortedTranscripts = new ArrayList<>(transcriptList);
                    // sort transcript based on chr and startPos
                    sortedTranscripts.sort(Comparator
                            .comparing(Transcript::getChrLab)
                            .thenComparingInt(Transcript::getStartPos));

                    BatchResult result = new BatchResult(sortedTranscripts.size());
                    Map<String, String> chrCache = new HashMap<>();
                    Set<String> warnedMissing = new HashSet<>();

                    try {
                        // [CHANGED] Reader is reused across tasks executed by the same worker thread.
                        SamReader reader = threadLocalReader.get();

                        // [CHANGED] Build per-record contexts first, grouped by resolved BAM chromosome.
                        Map<String, List<ProcessEachQueryCoorRecord.RecordContext>> contextsByChromosome = new LinkedHashMap<>();
                        for (Transcript gene_coord : sortedTranscripts) {
                            String chr_name = gene_coord.getChrLab();
                            String no_chr_name = gene_coord.getNoChrLab();
                            String chrCacheKey = chr_name + "|" + no_chr_name;
                            String actualChrName;

                            if (chrCache.containsKey(chrCacheKey)) {
                                actualChrName = chrCache.get(chrCacheKey);
                            } else {
                                actualChrName = ReadBam.checkChromosomeInBam(bam_chromosomes_list, chr_name, no_chr_name);
                                chrCache.put(chrCacheKey, actualChrName);
                            }

                            if (actualChrName == null) {
                                if (warnedMissing.add(chrCacheKey)) {
                                    System.err.println("Warning: Chromosome '" + chr_name + "' not found in BAM. Skipping associated records.");
                                }
                                continue;
                            }

                            ProcessEachQueryCoorRecord.RecordContext ctx =
                                    ProcessEachQueryCoorRecord.buildRecordContext(gene_coord, actualChrName);
                            contextsByChromosome.computeIfAbsent(actualChrName, k -> new ArrayList<>()).add(ctx);
                        }

                        // [CHANGED] One BAM scan per chromosome-group in this chunk.
                        for (Map.Entry<String, List<ProcessEachQueryCoorRecord.RecordContext>> entry : contextsByChromosome.entrySet()) {
                            List<ProcessEachQueryCoorRecord.RecordContext> contexts = entry.getValue();
                            PhysicalCoverageCalculator.BatchCoverageResult batchCoverageResult =
                                    PhysicalCoverageCalculator.calculatePhysicalCoverageBatch(reader, contexts);
                            Map<Integer, int[]> coverageByRecord = batchCoverageResult.getCoverageByRecord();
                            Map<Integer, Integer> readCountByRecord = batchCoverageResult.getReadCountByRecord();

                            for (ProcessEachQueryCoorRecord.RecordContext ctx : contexts) {
                                int[] physicalCoverage = coverageByRecord.get(ctx.transcript.getIndex());
                                if (physicalCoverage == null) {
                                    int len = ctx.intervalRange.getEnd() - ctx.intervalRange.getStart() + 1;
                                    physicalCoverage = new int[len];
                                }

                                EachCoverageResult coverageScaled =
                                        ProcessEachQueryCoorRecord.processRecordFromPhysicalCoverage(
                                                ctx.transcript,
                                                physicalCoverage,
                                                ctx
                                        );
                                coverageScaled.bamSizeNormalization(library_size, scale_ratio);

                                int index = ctx.transcript.getIndex();
                                String geneName = ctx.transcript.getRecordName();
                                int readCount = readCountByRecord.getOrDefault(index, 0);
                                result.rows.add(new CoverageRow(index, geneName, coverageScaled.getScaledCoverage(), readCount));
                            }
                        }

                    } catch (Exception e) {
                        System.err.println("Error processing batch " + batch_num + ": " + e.getMessage());
                        throw new RuntimeException("Processing failed for batch " + batch_num + ": " + e.getMessage(), e);
                    }
                    return result;
                };

                completionService.submit(chromosomeTask);
                taskCount.incrementAndGet();
            });

            System.out.println("--- Consolidating results ----");
            for (int i = 0; i < taskCount.get(); i++) {
                try {
                    BatchResult chrResult = completionService.take().get();
                    for (CoverageRow row : chrResult.rows) {
                        coverage_scaled_matrix.setRow(row.index, row.coverage);
                        final_record_names[row.index] = row.geneName;
                        final_read_counts[row.index] = row.readCount;
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
            } finally {
                // [CHANGED] Close all thread-local readers created during pool execution.
                synchronized (readersToClose) {
                    for (SamReader reader : readersToClose) {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                System.err.println("Failed to close SamReader: " + e.getMessage());
                            }
                        }
                    }
                    readersToClose.clear();
                }
            }
        }

        Map<String, Object> coverage_map = new HashMap<>();
        coverage_map.put("record_names", Arrays.asList(final_record_names));
        coverage_map.put("cov_mat", coverage_scaled_matrix);
        coverage_map.put("read_counts", final_read_counts);
        return coverage_map;
    }
}
