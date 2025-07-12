package com.NGSViz.sqldbOperate;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class ParallelGeneDbProcessor extends DBAtribute{
    private Set<String> unique_chrname_list = ConcurrentHashMap.newKeySet();
    private Set<String> unique_nochrname_list = ConcurrentHashMap.newKeySet();
    private Set<String> record_name_list = ConcurrentHashMap.newKeySet();
    private Set<String> DB_gene_list = ConcurrentHashMap.newKeySet();
    private int region_num = 0;
    // Concurrent Map
    private Map<String, Transcript> gene_map = new ConcurrentHashMap<>();
    private List<Double> width_list = Collections.synchronizedList(new ArrayList<>());
    // Thread pool, used for parallel processing
    private final ExecutorService executorService;
    // Each time the number of records processed
    private final int BATCH_SIZE = 1000;

    public ParallelGeneDbProcessor(int nThreads) {
        this.executorService = Executors.newFixedThreadPool(nThreads);
    }

    public ParallelGeneDbProcessor subsetGeneList(ParallelGeneDbProcessor newGenesBInfo, Set<String> gene_list,
                                                  ParallelGeneDbProcessor GenesBInfo){

        Map<String, Transcript> ori_gene_map = GenesBInfo.getGeneMap();
        for(Transcript transcript : ori_gene_map.values()){
            String gene_name = transcript.getGeneName();
            String record_name = transcript.getRecordName();
            boolean isPresent = gene_list.contains(gene_name);
            if(isPresent){
                newGenesBInfo.gene_map.put(record_name, transcript);
            }
        }
        Map<String, Transcript> gene_map = new ConcurrentHashMap<>();
        return newGenesBInfo;
    }

    // Use database connection pool for better performance
    public void processGeneData(String tbl_name, String biotype, String type) throws SQLException, InterruptedException {
        String query = String.format("SELECT gname, tid, chrom, strand, start, end FROM '%s' " +
                        "WHERE biotype = '%s' AND type = '%s' AND DefaultChoose = 1",
                tbl_name, biotype, type);
        System.out.println("The query keyword is : " + query);
        // Use connection pool instead of single connection
        DatabaseConnectionPool pool = DatabaseConnectionPool.getInstance(DATABASE_URL);
        
        try (ResultSet resultSet = pool.executeQuery(query)) {
            List<Map<String, Object>> batchRecords = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("gname", resultSet.getString("gname"));
                record.put("tid", resultSet.getString("tid"));
                record.put("chrom", resultSet.getString("chrom"));
                record.put("strand", resultSet.getString("strand"));
                record.put("start", resultSet.getInt("start"));
                record.put("end", resultSet.getInt("end"));
                batchRecords.add(record);
                if (batchRecords.size() >= BATCH_SIZE) {
                    // Submit a task to the thread pool to process the current batch
                    // Copy batch to avoid modification
                    List<Map<String, Object>> currentBatch = new ArrayList<>(batchRecords);
                    executorService.submit(() -> processBatch(currentBatch));
                    batchRecords.clear(); // Clear batch
                }
            }
            // Process the remaining records
            if (!batchRecords.isEmpty()) {
                executorService.submit(() -> processBatch(batchRecords));
            }
        }
        // Shut down the thread pool and no longer accept new tasks
        executorService.shutdown();
        // Wait for all tasks to be completed
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        // Print pool statistics
        pool.printPoolStats();
    }

    private void processBatch(List<Map<String, Object>> records) {
        for (Map<String, Object> record : records) {
            String gene_name = (String) record.get("gname");
            String transcript_id = (String) record.get("tid");
            String chr_name = (String) record.get("chrom");
            String strand = (String) record.get("strand");
            int start_pos = (int) record.get("start");
            int end_pos = (int) record.get("end");
            int width = end_pos - start_pos + 1;

            String nochr_name = chr_name.replace("chr", "");
            String record_name = gene_name + ":" + transcript_id; //

            // Thread-safe method using concurrent collections (no synchronization needed)
            unique_chrname_list.add(chr_name);
            unique_nochrname_list.add(nochr_name);
            DB_gene_list.add(gene_name);
            record_name_list.add(record_name);
            width_list.add((double) width);

            Transcript transcript = new Transcript(record_name, gene_name, transcript_id,
                    chr_name, nochr_name, strand, start_pos, end_pos);
            gene_map.put(record_name, transcript);
        }
    }

    // Methods
    public Set<String> getUniqueChrNameList() { return unique_chrname_list; }
    public Set<String> getUniqueNoChrNameList() { return unique_nochrname_list; }
    public Set<String> getRecordName() { return record_name_list; }
    public Set<String> getDBGeneList() { return DB_gene_list; }
    public Map<String, Transcript> getGeneMap() { return gene_map; }
    public List<Double> getWidthList() { return width_list; }
    public int getRegionNum() {
        region_num = width_list.toArray().length;
        return region_num;
    }
    public void setRecordName(Set<String> bed_record_name_list) {
        record_name_list = bed_record_name_list;
    }
    public void setGeneMap(Map<String, Transcript> bed_gene_map) {
        gene_map = bed_gene_map;
    }
}

