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
        try (BufferedReader br = new BufferedReader(new FileReader(bedFilePath))) {
            String line;

            System.out.println("=== batch size" + BATCH_SIZE + " ===");
            int batchIndex = 0;
            List<Transcript> currentBatch = new ArrayList<>(BATCH_SIZE);

            while ((line = br.readLine()) != null) {
                // Skip comment lines
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                String[] records = line.split("\t");
                String chr_name = records[0];

                int start_pos = Integer.parseInt(records[1]);
                int end_pos = Integer.parseInt(records[2]);
                String strand = records[3];
                String gene_name = records[4];
                String transcript_id = records[5];
                int width = end_pos - start_pos + 1;

                String nochr_name = chr_name.replace("chr", "");
                String record_name = gene_name + ":" + transcript_id;

                // Thread-safe method using concurrent collections (no synchronization needed)
                if (!record_name_list.contains(record_name)){
                    record_name_list.add(record_name);
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


}
