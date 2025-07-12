package com.NGSViz.sqldbOperate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BedDBProcess {
    private static Set<String> unique_chrname_list = ConcurrentHashMap.newKeySet();
    private static Set<String> unique_nochrname_list = ConcurrentHashMap.newKeySet();
    private static Set<String> record_name_list = ConcurrentHashMap.newKeySet();
    private static Set<String> DB_gene_list = ConcurrentHashMap.newKeySet();
    // Concurrent Map
    private static Map<String, Transcript> gene_map = new ConcurrentHashMap<>();
    public static Map<String, List<Transcript>> gene_map_exon = new HashMap<>();
    private static List<Double> width_list = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        String bedFilePath = "/Users/benche/myProj/ngsPlot/test.bed";
        buildBedExonDB(bedFilePath);
    }

    public static void buildBedNotExonDB(String bedFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(bedFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip comment lines
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                String[] records = line.split("\t");
                //
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
                if (!DB_gene_list.contains(gene_name)){
                    DB_gene_list.add(gene_name);
                }
                // DB_gene_list.add(gene_name);
                record_name_list.add(record_name);
                width_list.add((double) width);
                Transcript transcript = new Transcript(record_name, gene_name, transcript_id,
                        chr_name, nochr_name, strand, start_pos, end_pos);
                gene_map.put(record_name, transcript);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("--- BedDB Build Complete!");
    }

    public static void buildBedExonDB(String bedFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(bedFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip comment lines
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                String[] records = line.split("\t");
                //
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
                if (!DB_gene_list.contains(gene_name)){
                    DB_gene_list.add(gene_name);
                }
                // DB_gene_list.add(gene_name);
                record_name_list.add(record_name);
                width_list.add((double) width);

                Transcript transcript = new Transcript(record_name, gene_name, transcript_id,
                        chr_name, nochr_name, strand, start_pos, end_pos);
                gene_map_exon.computeIfAbsent(gene_name, k -> new ArrayList<>()).add(transcript);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("--- BedDB Build Complete!");
    }


    public Set<String> getRecordName() { return record_name_list; }
    public Map<String, Transcript> getGeneMap() { return gene_map; }


}
