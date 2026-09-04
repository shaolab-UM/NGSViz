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
        record_name_list.clear();
        geneList_batches.clear();
        Transcript.resetIndexCounter();

        if (BATCH_SIZE <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }

        System.out.println("=== batch size" + BATCH_SIZE + " ===");

        String order = "exon".equals(type) ? " ORDER BY tid, start" : "";
        String defaultChoice = "exon".equals(type) ? "" : " AND DefaultChoose = 1";
        String query = String.format(
                "SELECT gname, tid, chrom, strand, start, end FROM '%s' " +
                        "WHERE biotype = '%s' AND type = '%s'%s%s",
                tbl_name, biotype, type, defaultChoice, order);
        System.out.println("The query keyword is : " + query);
        DatabaseConnectionPool pool = DatabaseConnectionPool.getInstance(DATABASE_URL);

        int batchIndex = 0;
        try (ResultSet rs = pool.executeQuery(query)) {
            List<Transcript> transcripts = new ArrayList<>();
            String currentRecord = null;
            String currentGene = null;
            String currentTid = null;
            String currentChrom = null;
            String currentStrand = null;
            int currentStart = 0;
            int currentEnd = 0;

            while (rs.next()) {
                String geneName   = rs.getString("gname");
                String transcriptId = rs.getString("tid");
                String chrName    = rs.getString("chrom");
                String strand     = rs.getString("strand");
                int start         = rs.getInt("start");
                int end           = rs.getInt("end");

                String recordName = geneName + ":" + transcriptId;
                if ("exon".equals(type)) {
                    if (!recordName.equals(currentRecord)) {
                        if (currentRecord != null) {
                            transcripts.add(new Transcript(currentRecord, currentGene, currentTid,
                                    currentChrom, currentChrom.replace("chr", ""), currentStrand,
                                    currentStart, currentEnd));
                        }
                        currentRecord = recordName;
                        currentGene = geneName;
                        currentTid = transcriptId;
                        currentChrom = chrName;
                        currentStrand = strand;
                        currentStart = start;
                        currentEnd = end;
                    } else {
                        currentStart = Math.min(currentStart, start);
                        currentEnd = Math.max(currentEnd, end);
                    }
                } else {
                    transcripts.add(new Transcript(recordName, geneName, transcriptId,
                            chrName, chrName.replace("chr", ""), strand, start, end));
                }
            }
            if ("exon".equals(type) && currentRecord != null) {
                transcripts.add(new Transcript(currentRecord, currentGene, currentTid,
                        currentChrom, currentChrom.replace("chr", ""), currentStrand,
                        currentStart, currentEnd));
            }

            List<Transcript> currentBatch = new ArrayList<>(BATCH_SIZE);
            for (Transcript transcript : transcripts) {
                record_name_list.add(transcript.getRecordName());
                currentBatch.add(transcript);
                if (currentBatch.size() == BATCH_SIZE) {
                    geneList_batches.put(batchIndex++, currentBatch);
                    currentBatch = new ArrayList<>(BATCH_SIZE);
                }
            }
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
