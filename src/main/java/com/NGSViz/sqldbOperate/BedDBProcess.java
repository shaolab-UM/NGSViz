package com.NGSViz.sqldbOperate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BedDBProcess {
    private static Set<String> unique_chrname_list = ConcurrentHashMap.newKeySet();
    private static Set<String> unique_nochrname_list = ConcurrentHashMap.newKeySet();
    //public static List<String> exon_record_name_list = new ArrayList<>();
    public static Set<String> record_name_list = ConcurrentHashMap.newKeySet();
    Map<String, List<Transcript>> genesByChromosome = new ConcurrentHashMap<>();
    // Concurrent Map
    //private static Map<String, Transcript> gene_map = new ConcurrentHashMap<>();
    private static List<Double> width_list = Collections.synchronizedList(new ArrayList<>());
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
                unique_chrname_list.add(chr_name);
                unique_nochrname_list.add(nochr_name);
                if (!record_name_list.contains(record_name)){
                    record_name_list.add(record_name);
                }

                width_list.add((double) width);
                Transcript transcript = new Transcript(record_name, gene_name, transcript_id,
                        chr_name, nochr_name, strand, start_pos, end_pos);
                genesByChromosome.computeIfAbsent(chr_name, k -> new ArrayList<>()).add(transcript);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("--- BedDB Build Complete!");
    }




    //public Map<String, Transcript> getGeneMap() { return gene_map; }
    public Set<String> getRecordName() { return record_name_list; }
    public Map<String, List<Transcript>> getGeneListByChromosome() { return genesByChromosome; }


}
