package com.NGSViz.sqldbOperate;

import com.NGSViz.configSet.InputParameterAttributes;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class GeneDbProcessor extends DBAtribute{
    public static Set<String> record_name_list = ConcurrentHashMap.newKeySet();
    public static Map<Integer, List<Transcript>> geneList_batches = new HashMap<>();
    private static final int BATCH_SIZE = InputParameterAttributes.BATCH_SIZE;

    public static void processGeneData(String tbl_name,
                                       String biotype,
                                       String type) throws SQLException {

        if (BATCH_SIZE <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }

        System.out.println("=== batch size" + BATCH_SIZE + " ===");

        String query = String.format(
                "SELECT gname, tid, chrom, strand, start, end FROM '%s' " +
                        "WHERE biotype = '%s' AND type = '%s' AND DefaultChoose = 1",
                tbl_name, biotype, type);
        System.out.println("The query keyword is : " + query);
        DatabaseConnectionPool pool = DatabaseConnectionPool.getInstance(DATABASE_URL);

        int batchIndex = 0;
        try (ResultSet rs = pool.executeQuery(query)) {
            List<Transcript> currentBatch = new ArrayList<>(BATCH_SIZE);

            while (rs.next()) {
                String geneName   = rs.getString("gname");
                String transcriptId = rs.getString("tid");
                String chrName    = rs.getString("chrom");
                String strand     = rs.getString("strand");
                int start         = rs.getInt("start");
                int end           = rs.getInt("end");

                String nochrName = chrName.replace("chr", "");
                String recordName = geneName + ":" + transcriptId;

                record_name_list.add(recordName);

                Transcript transcript = new Transcript(
                        recordName, geneName, transcriptId,
                        chrName, nochrName, strand, start, end);

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
        }
    }

    // Methods
    public static Set<String> getRecordName() { return record_name_list; }
    public static Map<Integer, List<Transcript>> getBatchGenes() { return geneList_batches; }
    public void setRecordName(Set<String> bed_record_name_list) {record_name_list = bed_record_name_list;
    }
    public void setBatchGeneList(Map<Integer, List<Transcript>> bed_transcriptBatches) {
        geneList_batches = bed_transcriptBatches;
    }
}

