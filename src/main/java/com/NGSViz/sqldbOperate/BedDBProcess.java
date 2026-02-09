package com.NGSViz.sqldbOperate;

import com.NGSViz.configSet.InputParameterAttributes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BedDBProcess {
    public static Set<String> record_name_list = ConcurrentHashMap.newKeySet();
    public static Map<Integer, List<Transcript>> geneList_batches = new HashMap<>();
    private static final int BATCH_SIZE = InputParameterAttributes.BATCH_SIZE;
    private String bedFilePath;

    public static void main(String[] args) {
        String bedFilePath = "/Users/benche/myProj/ngsPlot/test.bed";
        //buildBedDB(bedFilePath);
    }
    public BedDBProcess(String bedFilePath) {
        this.bedFilePath = bedFilePath;
    }

    public void buildBedDB(String bedFilePath) {
        record_name_list.clear();
        geneList_batches.clear();
        Transcript.resetIndexCounter();
        try (BufferedReader br = new BufferedReader(new FileReader(bedFilePath))) {
            String line;

            System.out.println("=== batch size" + BATCH_SIZE + " ===");
            int batchIndex = 0;
            List<Transcript> currentBatch = new ArrayList<>(BATCH_SIZE);

            while ((line = br.readLine()) != null) {
                // Skip comment lines
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                if (line.startsWith("track") || line.startsWith("browser")) continue;

                String[] records = line.split("\t");
                if (records.length < 3) {
                    records = line.trim().split("\\s+");
                }
                if (records.length < 3) {
                    throw new IllegalArgumentException("Invalid BED line (expected >= 3 columns): " + line);
                }

                String chr_name = records[0];

                int start_pos;
                int end_pos;
                try {
                    start_pos = Integer.parseInt(records[1]);
                    end_pos = Integer.parseInt(records[2]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid BED coordinates (start/end must be integers): " + line, e);
                }

                String strand = "+";
                String gene_name = null;
                String transcript_id = null;

                // Support both NGSViz custom format and common BED formats.
                // NGSViz custom (documented): chrom, start, end, strand, geneName, transcriptId
                // Common BED6: chrom, start, end, name, score, strand
                if (records.length >= 4 && isStrandToken(records[3])) {
                    // NGSViz: strand is column 4
                    strand = normalizeStrand(records[3]);
                    if (records.length >= 6) {
                        gene_name = records[4];
                        transcript_id = records[5];
                    } else if (records.length >= 5) {
                        gene_name = records[4];
                    }
                } else if (records.length >= 6 && isStrandToken(records[5])) {
                    // BED6: strand is column 6
                    strand = normalizeStrand(records[5]);
                    gene_name = records[3];
                } else if (records.length >= 4) {
                    // BED4/5 without explicit strand: treat column 4 as name
                    gene_name = records[3];
                    if (records.length >= 5) {
                        transcript_id = records[4];
                    }
                }

                if (gene_name == null || gene_name.isEmpty()) {
                    gene_name = chr_name + "_" + start_pos + "_" + end_pos;
                }
                if (transcript_id == null || transcript_id.isEmpty()) {
                    transcript_id = chr_name + "_" + start_pos + "_" + end_pos;
                }

                if (start_pos > end_pos) {
                    throw new IllegalArgumentException("Invalid BED coordinates (start > end): " + line);
                }
                // Keep for compatibility; current pipeline doesn't use it.
                int width = end_pos - start_pos + 1;

                String nochr_name = chr_name.replace("chr", "");
                String record_name = gene_name + ":" + transcript_id;

                // Skip redundant entries to keep Transcript indices aligned with record_name_list.size()
                if (!record_name_list.add(record_name)) {
                    continue;
                }

                Transcript transcript = new Transcript(record_name, gene_name, transcript_id,
                        chr_name, nochr_name, strand, start_pos, end_pos);

                // Join the current batch
                currentBatch.add(transcript);
                // Batch is full, stock is depleted and reset
                if (currentBatch.size() == BATCH_SIZE) {
                    geneList_batches.put(batchIndex++, currentBatch);
                    currentBatch = new ArrayList<>(BATCH_SIZE);
                }
            }
            // Process the remaining records that are less than batchSize
            if (!currentBatch.isEmpty()) {
                geneList_batches.put(batchIndex, currentBatch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("--- BedDB Build Complete!");
    }

    public static Set<String> getRecordName() { return record_name_list; }
    public static Map<Integer, List<Transcript>> getBatchGeneList() { return geneList_batches; }

    private static boolean isStrandToken(String token) {
        return "+".equals(token) || "-".equals(token) || ".".equals(token) || "*".equals(token);
    }

    private static String normalizeStrand(String token) {
        if ("-".equals(token)) {
            return "-";
        }
        // Treat unknown/unspecified strand as '+' for non-strand-specific analysis.
        return "+";
    }

}
